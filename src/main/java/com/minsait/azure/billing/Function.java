package com.minsait.azure.billing;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.EnumSet;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.apache.commons.beanutils.BeanUtils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.microsoft.azure.functions.ExecutionContext;
import com.microsoft.azure.functions.annotation.FunctionName;
import com.microsoft.azure.functions.annotation.TimerTrigger;
import com.microsoft.azure.keyvault.KeyVaultClient;
import com.microsoft.azure.keyvault.authentication.KeyVaultCredentials;
import com.microsoft.azure.storage.CloudStorageAccount;
import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.blob.CloudBlobClient;
import com.microsoft.azure.storage.blob.CloudBlobContainer;
import com.microsoft.azure.storage.blob.CloudBlockBlob;
import com.microsoft.azure.storage.blob.SharedAccessBlobPermissions;
import com.microsoft.azure.storage.blob.SharedAccessBlobPolicy;
import com.microsoft.azure.storage.table.CloudTable;
import com.microsoft.azure.storage.table.CloudTableClient;
import com.microsoft.azure.storage.table.TableBatchOperation;
import com.microsoft.azure.storage.table.TableOperation;
import com.microsoft.azure.storage.table.TableQuery;
import com.microsoft.azure.storage.table.TableQuery.QueryComparisons;
import com.sendgrid.Email;
import com.sendgrid.Mail;
import com.sendgrid.Method;
import com.sendgrid.Personalization;
import com.sendgrid.Request;
import com.sendgrid.Response;
import com.sendgrid.SendGrid;
import com.squareup.okhttp.HttpUrl;
import com.squareup.okhttp.OkHttpClient;

import domain.AttributeConfig;
import domain.AttributeConfigDeserializer;
import domain.DetailWrapper;
import domain.Resource;
import domain.Result;
import domain.SubscriptionConfig;
import domain.entity.ResultEntity;
import domain.entity.SubscriptionEntity;
import domain.export.RecordNotFound;
import io.swagger.client.api.BillingPeriodsApi;
import io.swagger.client.api.MarketplaceChargesApi;
import io.swagger.client.api.UsageDetailsApi;
import io.swagger.client.model.BillingPeriodV1;
import io.swagger.client.model.PagedJsonData;
import io.swagger.client.model.UsageDetailsDeNormalized;

public class Function {

	private static CloudTableClient tableClientResults;
	private static CloudTableClient tableClientConfig;
	private static CloudBlobClient blobClientResults;
	static {
		CloudStorageAccount storageAccount = null;
		try {
			storageAccount = CloudStorageAccount.parse(System.getenv("ResultsStorageConnection"));
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (storageAccount != null) {
			tableClientResults = storageAccount.createCloudTableClient();
			blobClientResults = storageAccount.createCloudBlobClient();
		}
		try {
			storageAccount = CloudStorageAccount.parse(System.getenv("SubscriptionsStorageConnection"));
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (storageAccount != null) {
			tableClientConfig = storageAccount.createCloudTableClient();
		}
	}

	@FunctionName("processBillingPeriodically")
	public void processBilling(@TimerTrigger(name = "trigger", schedule = "0 0 6 * * *") String timerInfo,
			final ExecutionContext context) {
		try {
			long startTime = System.currentTimeMillis();
			
			KeyVaultClient kvc = getKeyVaultClient(context);
			
			
			final String apiKey = kvc.getSecret(System.getenv("keyVaultURI"), "apiKey").value();

			BillingPeriodsApi bpa = new BillingPeriodsApi();
			List<BillingPeriodV1> billingPeriods = bpa.billingPeriodsV1Get("bearer " + apiKey,
					System.getenv("enrollment"));
			String lastBillingPeriod = billingPeriods.get(0).getBillingPeriodId();
			String previousBillingPeriod = billingPeriods.get(1).getBillingPeriodId();

			GsonBuilder gsonBuilder = new GsonBuilder();
			JsonDeserializer<AttributeConfig> deserializer = new AttributeConfigDeserializer(); // implementation detail
			gsonBuilder.registerTypeAdapter(AttributeConfig.class, deserializer);
			Gson gson = gsonBuilder.create();

			Map<String, List<SubscriptionConfig>> subsMapper = new HashMap<String, List<SubscriptionConfig>>();
			Map<String, Result> res = new HashMap<String, Result>();

			CloudTable subscriptionsTable = tableClientConfig.getTableReference("subscriptions");

			// Specify a partition query, using "Smith" as the partition key filter.
			TableQuery<SubscriptionEntity> query = TableQuery.from(SubscriptionEntity.class);

			// Loop through the results, displaying information about the entity.
			for (SubscriptionEntity entity : subscriptionsTable.execute(query)) {
				res.put(entity.getPartitionKey(), new Result(entity.getCodigo(), entity.getElemento(),
						entity.getPartitionKey(), entity.getOwner(), entity.getMargin(), entity.getFixedAmount()));
				SubscriptionConfig[] scs = gson.fromJson(entity.getSubscriptions(), SubscriptionConfig[].class);
				for (SubscriptionConfig sc : scs) {
					List<SubscriptionConfig> subs = subsMapper.get(sc.getName());
					if (subs == null) {
						subs = new ArrayList<SubscriptionConfig>();
						subsMapper.put(sc.getName(), subs);
					}
					sc.setName(entity.getPartitionKey());
					subs.add(sc);
				}
			}

			subsMapper.forEach((k, v) -> {
				context.getLogger().info(k + " \t" + v);
			});

			UsageDetailsApi uda = new UsageDetailsApi();
			uda.getApiClient().getHttpClient().setReadTimeout(300000, TimeUnit.MILLISECONDS);

			MarketplaceChargesApi mca = new MarketplaceChargesApi();
			
			CloudTable tableResults = tableClientResults.getTableReference("results");
			boolean closingPeriod = !existPeriodInTable(lastBillingPeriod, tableResults);
			processPeriod(lastBillingPeriod, apiKey, uda, mca, tableClientResults, subsMapper, res, kvc, context);						
			closingPeriod = closingPeriod && existPeriodInTable(lastBillingPeriod, tableResults);

			if (closingPeriod) {
				context.getLogger().info("Closing '" + previousBillingPeriod + "' period.");
				resetResults(res);
				boolean success = processPeriod(previousBillingPeriod, apiKey, uda, mca, tableClientResults, subsMapper, res, kvc,
						context);
				if(!success) {
					deleteResults(lastBillingPeriod, tableClientResults.getTableReference("results"));
				} else {
					CloudBlobContainer container = blobClientResults.getContainerReference("billingresults");
					container.createIfNotExists();
					
					Path billingResults = Files.createTempFile("billingResults", ".txt");
					try (final BufferedWriter writer = Files.newBufferedWriter(billingResults, StandardCharsets.ISO_8859_1)) {
						for(Result record : res.values()) {
							if("UNK".equals(record.getCodigo())) {
								continue;
							}
							writer.write(record.getSubscription() + "\t" + record.getOwner() + "\t" + record.getCodigo() + "\t" + record.getElemento() + "\t" + record.getMargin() + "\t" + String.format(Locale.forLanguageTag("es-ES"), "%.2f", record.getCost()) + "\t" + String.format(Locale.forLanguageTag("es-ES"), "%.2f", record.getExtendedCost()));
							writer.newLine();							
						}
						writer.close();
					}
					CloudBlockBlob blob = container.getBlockBlobReference(previousBillingPeriod + "_BillingResults.txt");
					File f = billingResults.toFile();
				    blob.upload(new FileInputStream(f), f.length());
				    
			        SharedAccessBlobPolicy policy = createSharedAccessPolicy(
			                EnumSet.of(SharedAccessBlobPermissions.READ), Integer.valueOf(System.getenv("SASExpiration")));
			        String sas = blob.generateSharedAccessSignature(policy, null);
			        
			        sendBillingPeriodClosedMail(blob.getUri().toString() + "?" + sas, kvc, previousBillingPeriod, context);
				}				
			}
			context.getLogger().info("Elapsed time (ms): " + (System.currentTimeMillis() - startTime));
		} catch (Exception e) {
			context.getLogger().severe(e.getMessage() + Arrays.asList(e.getStackTrace())
            .stream()
            .map(Objects::toString)
            .collect(Collectors.joining("\n")));
		}
	}

	private boolean existPeriodInTable(String period, CloudTable cloudTable) throws StorageException {
		if (cloudTable.exists()) {
			// Create a filter condition where the partition key is "Smith".
			String partitionFilter = TableQuery.generateFilterCondition("PartitionKey", QueryComparisons.EQUAL,
					period);

			// Specify a partition query, using "Smith" as the partition key filter.
			TableQuery<ResultEntity> partitionQuery = TableQuery.from(ResultEntity.class).where(partitionFilter);
			
			return cloudTable.execute(partitionQuery).iterator().hasNext();
		}
		return false;
	}

	private void deleteResults(String period, CloudTable resultsTable) throws Exception {		
		final TableBatchOperation batchOperation = new TableBatchOperation();

		// Create a filter condition where the partition key is "Smith".
		String partitionFilter = TableQuery.generateFilterCondition("PartitionKey", QueryComparisons.EQUAL,
				period);

		// Specify a partition query, using "Smith" as the partition key filter.
		TableQuery<ResultEntity> partitionQuery = TableQuery.from(ResultEntity.class).where(partitionFilter);
		Iterable<ResultEntity> res = resultsTable.execute(partitionQuery);
		
		res.forEach((result) -> {
			batchOperation.delete(result);
		});
		resultsTable.execute(batchOperation);
	}

	private static KeyVaultClient getKeyVaultClient(ExecutionContext context) {
		return new KeyVaultClient(new KeyVaultCredentials() {

			@Override
			public String doAuthenticate(String authorization, String resource, String scope) {
				try {
					HttpUrl.Builder urlBuilder = HttpUrl.parse(System.getenv("MSI_ENDPOINT")).newBuilder();
					urlBuilder.addQueryParameter("resource", "https://vault.azure.net");
					urlBuilder.addQueryParameter("api-version", "2017-09-01");

					String url = urlBuilder.build().toString();
					com.squareup.okhttp.Request req = new com.squareup.okhttp.Request.Builder().url(url)
							.addHeader("Secret", System.getenv("MSI_SECRET")).build();
					OkHttpClient client = new OkHttpClient();
					com.squareup.okhttp.Response resp = client.newCall(req).execute();
					
					String r = resp.body().string();
					
					JsonParser parser = new JsonParser();
					JsonObject jo = (JsonObject)parser.parse(r);
			
					return jo.get("access_token").getAsString();
				} catch(Exception e) {
					context.getLogger().info("Error accessing to Key vault: " + e.getMessage());
					return null;
				}
			}

		});
	}

	private static void resetResults(Map<String, Result> res) {
		for (Result r : res.values()) {
			r.setCost(0d);
		}
	}

	private static boolean processPeriod(String period, String apiKey, UsageDetailsApi api, MarketplaceChargesApi apiMarket, CloudTableClient tableClient,
			Map<String, List<SubscriptionConfig>> subsMapper, Map<String, Result> res, KeyVaultClient kvc,
			final ExecutionContext context) throws Exception {
		context.getLogger().info("++++++++++++++++ STARTING PROCESSING PERIOD '" + period + "' ++++++++++++++++");
		boolean success = true;
		PagedJsonData pjd;
		Set<RecordNotFound> recordsNotFound = new HashSet<RecordNotFound>();		

		HttpUrl nextUrl = null;
		do {
			pjd = api.usageDetailsGetByBillingPeriod("bearer " + apiKey, System.getenv("enrollment"),
					period, nextUrl);
			for (UsageDetailsDeNormalized udd : pjd.getData()) {
				if (!"MINSAIT".equals(udd.getAccountName()) || udd.getCost() == 0d)
					continue;
				String sourceSubscription = udd.getSubscriptionName();
				if (subsMapper.get(sourceSubscription) == null) {
					context.getLogger().warning(period + " - No table record for '" + sourceSubscription
							+ "' subscription, making default mapping");
					List<SubscriptionConfig> l = new ArrayList<SubscriptionConfig>();
					l.add(new SubscriptionConfig(sourceSubscription, null));
					subsMapper.put(sourceSubscription, l);
					res.put(sourceSubscription, new Result("UNK", "UNK", sourceSubscription, "UNK", 0d, null));
				}
				DetailWrapper dw = new DetailWrapper();
				BeanUtils.copyProperties(dw, udd);
				compute(subsMapper.get(udd.getSubscriptionName()), res, dw, recordsNotFound);
			}
			if (pjd.getNextLink() != null) {
				nextUrl = HttpUrl.parse(pjd.getNextLink());
			} else {
				nextUrl = null;
			}
		} while (nextUrl != null);
		
		/*
		List<MarketplacePropertiesV1> marketConsumptions = apiMarket.marketplacesV1GetMarketplacesByPeriod("bearer " + apiKey, System.getenv("enrollment"), period);
		
		for(MarketplacePropertiesV1 record : marketConsumptions) {
			if (!"schicoh".equals(record.getAccountName()) || record.getExtendedCost() == 0d)
				continue;
			String sourceSubscription = record.getSubscriptionName();
			if (subsMapper.get(sourceSubscription) == null) {
				context.getLogger().warning(period + " - No table record for '" + sourceSubscription
						+ "' subscription, making default mapping");
				List<SubscriptionConfig> l = new ArrayList<SubscriptionConfig>();
				l.add(new SubscriptionConfig(sourceSubscription, null));
				subsMapper.put(sourceSubscription, l);
				res.put(sourceSubscription, new Result("UNK", "UNK", sourceSubscription, "UNK", 0d, null));
			}
			DetailWrapper dw = new DetailWrapper();
			BeanUtils.copyProperties(dw, record);
			compute(subsMapper.get(record.getSubscriptionName()), res, dw, recordsNotFound);
		}
		*/
		final Double[] sum = { 0d, 0d };
		// Create a cloud table object for the table.
		CloudTable cloudTable = tableClient.getTableReference("results");
		// Define a batch operation.
		TableBatchOperation batchOperation = new TableBatchOperation();

		res.forEach((k, v) -> {
			ResultEntity resultEntity = new ResultEntity(period, k);
			resultEntity.setCodigo(v.getCodigo());
			resultEntity.setElemento(v.getElemento());
			resultEntity.setOwner(v.getOwner());
			resultEntity.setMargin(v.getMargin());
			resultEntity.setCost(v.getCost());

			context.getLogger().info(
					k + "\t" + String.format("%.2f", v.getCost()) + "\t" + String.format("%.2f", v.getExtendedCost()));
			sum[0] += v.getCost();
			if (!"UNK".equals(v.getCodigo())) {
				Double extendedCost = v.getExtendedCost();
				sum[1] += extendedCost;
				resultEntity.setExtendedCost(extendedCost);
			}
			batchOperation.insertOrReplace(resultEntity);
		});
		
		if(sum[0] > 0) {
			// Add total 
			ResultEntity resultEntity = new ResultEntity(period, "TOTAL");
			resultEntity.setCodigo("-");
			resultEntity.setElemento("-");
			resultEntity.setOwner("-");
			resultEntity.setMargin(null);
			resultEntity.setCost(sum[0]);
			resultEntity.setExtendedCost(sum[1]);
			batchOperation.insertOrReplace(resultEntity);
			
			success = false;
			if(cloudTable.exists()) {
				// Get TOTAL
				ResultEntity totalResult = cloudTable.execute(TableOperation.retrieve(period, "TOTAL", ResultEntity.class)).getResultAsType();
				success = totalResult == null || Math.abs(totalResult.getCost() - sum[0]) < 0.01d; 
			} else {
				cloudTable.create();
			}
			cloudTable.execute(batchOperation);			
			context.getLogger().info("Sum: " + String.format("%.2f", sum[0]));
			context.getLogger().info("Sum (extended): " + String.format("%.2f", sum[1]));
						
			if(recordsNotFound.size() > 0) {
				sendRecordNotFoundMail(recordsNotFound, kvc, period, context);
			}	
			success = success && recordsNotFound.size() == 0;
		}
		context.getLogger().info("++++++++++++++++ FINISHED PROCESSING PERIOD '" + period + "' ++++++++++++++++");		
		return success;
	}

	private static void sendRecordNotFoundMail(Set<RecordNotFound> recordsNotFound, KeyVaultClient kvc, String period, ExecutionContext context) {
		final String[] recipients = System.getenv("recipients").split(",");		
		final String sendGridApiKey = kvc.getSecret(System.getenv("keyVaultURI"), "sendGridKey").value();
		SendGrid sg = new SendGrid(sendGridApiKey);
		String templateId = System.getenv("recordsNotFoundTemplateId");
		String subjectRecordsNotFound = System.getenv("subjectRecordsNotFound");
		String html_content = "<ul>";

		for (RecordNotFound rnf : recordsNotFound) {
			context.getLogger().warning("Record not found: " + rnf);
			html_content += "<li>" + rnf + "</li>";
		}
		html_content += "</ul>";
		Mail mail = new Mail();
		mail.setFrom(new Email("SAF@minsait.com"));
		mail.setTemplateId(templateId);

		Personalization p1 = new Personalization();
		for (String recipient : recipients) {
			p1.addTo(new Email(recipient));
		}
		p1.setSubject(subjectRecordsNotFound);
		p1.addSubstitution("%recordsNotFound%", html_content);
		p1.addSubstitution("%period%", period);

		mail.addPersonalization(p1);

		Request request = new Request();
		try {
			request.setMethod(Method.POST);
			request.setEndpoint("mail/send");
			request.setBody(mail.build());
			Response response = sg.api(request);
		} catch (IOException ex) {
			context.getLogger().severe("Error enviando correo: " + ex.getMessage());
		}		
	}

	private static void sendBillingPeriodClosedMail(String fileUri, KeyVaultClient kvc, String period, ExecutionContext context) {
		final String[] recipients = System.getenv("recipients").split(",");		
		final String sendGridApiKey = kvc.getSecret(System.getenv("keyVaultURI"), "sendGridKey").value();
		SendGrid sg = new SendGrid(sendGridApiKey);
		String templateId = System.getenv("billingPeriodClosedTemplateId");
		String subjectRecordsNotFound = System.getenv("subjectBillingPeriodClosed");

		Mail mail = new Mail();
		mail.setFrom(new Email("SAF@minsait.com"));
		mail.setTemplateId(templateId);

		Personalization p1 = new Personalization();
		for (String recipient : recipients) {
			p1.addTo(new Email(recipient));
		}
		p1.setSubject(subjectRecordsNotFound);
		p1.addSubstitution("%fileUri%", fileUri);
		p1.addSubstitution("%period%", period);
		p1.addSubstitution("%SASExpiration%", System.getenv("SASExpiration"));

		mail.addPersonalization(p1);

		Request request = new Request();
		try {
			request.setMethod(Method.POST);
			request.setEndpoint("mail/send");
			request.setBody(mail.build());
			Response response = sg.api(request);
		} catch (IOException ex) {
			context.getLogger().severe("Error enviando correo: " + ex.getMessage());
		}		
	}

	private static void compute(List<SubscriptionConfig> configs, Map<String, Result> res, DetailWrapper dw,
			Set<RecordNotFound> recordsNotFound) throws Exception {
		boolean found = false;
		for (SubscriptionConfig sc : configs) {
			if (null == sc.getResources() || resolve(sc, dw)) {
				res.get(sc.getName()).addCost(dw.getCost());
				found = true;
				break;
			}
		}
		if (!found) {
			recordsNotFound.add(new RecordNotFound(dw.getSubscriptionName(), dw.getInstanceId()));
		}
	}

	private static boolean resolve(SubscriptionConfig sc, DetailWrapper dw) throws Exception {
		for (Resource r : sc.getResources()) {
			if (resolveResource(r, dw))
				return true;
		}
		return false;
	}

	private static boolean resolveResource(Resource r, DetailWrapper dw) throws Exception {
		for (AttributeConfig att : r.getAttributes()) {
			if (!resolveAttribute(att, dw))
				return false;
		}
		return true;
	}

	private static boolean resolveAttribute(AttributeConfig att, DetailWrapper dw) throws Exception {
		String value = BeanUtils.getSimpleProperty(dw, att.getName());
		String desiredValue = att.getIs();
		if (null != desiredValue) {
			return desiredValue.equalsIgnoreCase(value);
		}
		return att.getPattern().matcher(value).matches();
	}

    private final static SharedAccessBlobPolicy createSharedAccessPolicy(EnumSet<SharedAccessBlobPermissions> sap,
            int expireTimeInHours) {

        Calendar calendar = new GregorianCalendar(TimeZone.getTimeZone("UTC"));
        calendar.setTime(new Date());
        calendar.add(Calendar.HOUR, expireTimeInHours);
        SharedAccessBlobPolicy policy = new SharedAccessBlobPolicy();
        policy.setPermissions(sap);
        policy.setSharedAccessExpiryTime(calendar.getTime());
        return policy;
    }	
}
