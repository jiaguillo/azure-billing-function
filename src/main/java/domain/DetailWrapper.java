package domain;

import org.joda.time.DateTime;

import com.google.gson.annotations.SerializedName;

public class DetailWrapper {

	public DetailWrapper() {
		super();
	}

	  private Double cost = null;

	  private Integer accountId = null;

	  private Integer productId = null;

	  private Integer resourceLocationId = null;

	  private Integer consumedServiceId = null;

	  private Integer departmentId = null;

	  private String accountOwnerEmail = null;

	  private String accountName = null;

	  private String serviceAdministratorId = null;

	  private Long subscriptionId = null;

	  private String subscriptionGuid = null;

	  private String subscriptionName = null;

	  private DateTime date = null;

	  private String product = null;

	  private String meterId = null;

	  private String meterCategory = null;

	  private String meterSubCategory = null;

	  private String meterRegion = null;

	  private String meterName = null;

	  private Double consumedQuantity = null;

	  private Double resourceRate = null;

	  private String resourceLocation = null;

	  private String consumedService = null;

	  private String instanceId = null;

	  private String serviceInfo1 = null;

	  private String serviceInfo2 = null;

	  private String additionalInfo = null;

	  private String tags = null;

	  private String storeServiceIdentifier = null;

	  private String departmentName = null;

	  private String costCenter = null;

	  private String unitOfMeasure = null;

	  private String resourceGroup = null;
	  
	  /* Additional types for Marketplace charges */
	  private String id = null;

	  @SerializedName("usageStartDate")
	  private DateTime usageStartDate = null;

	  @SerializedName("usageEndDate")
	  private DateTime usageEndDate = null;

	  @SerializedName("offerName")
	  private String offerName = null;

	  @SerializedName("orderNumber")
	  private String orderNumber = null;

	  @SerializedName("accountOwnerId")
	  private String accountOwnerId = null;

	  @SerializedName("publisherName")
	  private String publisherName = null;

	  @SerializedName("planName")
	  private String planName = null;

	  @SerializedName("extendedCost")
	  private Double extendedCost = null;
	  

	public Double getCost() {
		return cost != null ? cost : extendedCost;
	}

	public void setCost(Double cost) {
		this.cost = cost;
	}

	public Integer getAccountId() {
		return accountId;
	}

	public void setAccountId(Integer accountId) {
		this.accountId = accountId;
	}

	public Integer getProductId() {
		return productId;
	}

	public void setProductId(Integer productId) {
		this.productId = productId;
	}

	public Integer getResourceLocationId() {
		return resourceLocationId;
	}

	public void setResourceLocationId(Integer resourceLocationId) {
		this.resourceLocationId = resourceLocationId;
	}

	public Integer getConsumedServiceId() {
		return consumedServiceId;
	}

	public void setConsumedServiceId(Integer consumedServiceId) {
		this.consumedServiceId = consumedServiceId;
	}

	public Integer getDepartmentId() {
		return departmentId;
	}

	public void setDepartmentId(Integer departmentId) {
		this.departmentId = departmentId;
	}

	public String getAccountOwnerEmail() {
		return accountOwnerEmail;
	}

	public void setAccountOwnerEmail(String accountOwnerEmail) {
		this.accountOwnerEmail = accountOwnerEmail;
	}

	public String getAccountName() {
		return accountName;
	}

	public void setAccountName(String accountName) {
		this.accountName = accountName;
	}

	public String getServiceAdministratorId() {
		return serviceAdministratorId;
	}

	public void setServiceAdministratorId(String serviceAdministratorId) {
		this.serviceAdministratorId = serviceAdministratorId;
	}

	public Long getSubscriptionId() {
		return subscriptionId;
	}

	public void setSubscriptionId(Long subscriptionId) {
		this.subscriptionId = subscriptionId;
	}

	public String getSubscriptionGuid() {
		return subscriptionGuid;
	}

	public void setSubscriptionGuid(String subscriptionGuid) {
		this.subscriptionGuid = subscriptionGuid;
	}

	public String getSubscriptionName() {
		return subscriptionName;
	}

	public void setSubscriptionName(String subscriptionName) {
		this.subscriptionName = subscriptionName;
	}

	public DateTime getDate() {
		return date;
	}

	public void setDate(DateTime date) {
		this.date = date;
	}

	public String getProduct() {
		return product;
	}

	public void setProduct(String product) {
		this.product = product;
	}

	public String getMeterId() {
		return meterId;
	}

	public void setMeterId(String meterId) {
		this.meterId = meterId;
	}

	public String getMeterCategory() {
		return meterCategory;
	}

	public void setMeterCategory(String meterCategory) {
		this.meterCategory = meterCategory;
	}

	public String getMeterSubCategory() {
		return meterSubCategory;
	}

	public void setMeterSubCategory(String meterSubCategory) {
		this.meterSubCategory = meterSubCategory;
	}

	public String getMeterRegion() {
		return meterRegion;
	}

	public void setMeterRegion(String meterRegion) {
		this.meterRegion = meterRegion;
	}

	public String getMeterName() {
		return meterName;
	}

	public void setMeterName(String meterName) {
		this.meterName = meterName;
	}

	public Double getConsumedQuantity() {
		return consumedQuantity;
	}

	public void setConsumedQuantity(Double consumedQuantity) {
		this.consumedQuantity = consumedQuantity;
	}

	public Double getResourceRate() {
		return resourceRate;
	}

	public void setResourceRate(Double resourceRate) {
		this.resourceRate = resourceRate;
	}

	public String getResourceLocation() {
		return resourceLocation;
	}

	public void setResourceLocation(String resourceLocation) {
		this.resourceLocation = resourceLocation;
	}

	public String getConsumedService() {
		return consumedService;
	}

	public void setConsumedService(String consumedService) {
		this.consumedService = consumedService;
	}

	public String getInstanceId() {
		return instanceId;
	}

	public void setInstanceId(String instanceId) {
		this.instanceId = instanceId;
	}

	public String getServiceInfo1() {
		return serviceInfo1;
	}

	public void setServiceInfo1(String serviceInfo1) {
		this.serviceInfo1 = serviceInfo1;
	}

	public String getServiceInfo2() {
		return serviceInfo2;
	}

	public void setServiceInfo2(String serviceInfo2) {
		this.serviceInfo2 = serviceInfo2;
	}

	public String getAdditionalInfo() {
		return additionalInfo;
	}

	public void setAdditionalInfo(String additionalInfo) {
		this.additionalInfo = additionalInfo;
	}

	public String getTags() {
		return tags;
	}

	public void setTags(String tags) {
		this.tags = tags;
	}

	public String getStoreServiceIdentifier() {
		return storeServiceIdentifier;
	}

	public void setStoreServiceIdentifier(String storeServiceIdentifier) {
		this.storeServiceIdentifier = storeServiceIdentifier;
	}

	public String getDepartmentName() {
		return departmentName;
	}

	public void setDepartmentName(String departmentName) {
		this.departmentName = departmentName;
	}

	public String getCostCenter() {
		return costCenter;
	}

	public void setCostCenter(String costCenter) {
		this.costCenter = costCenter;
	}

	public String getUnitOfMeasure() {
		return unitOfMeasure;
	}

	public void setUnitOfMeasure(String unitOfMeasure) {
		this.unitOfMeasure = unitOfMeasure;
	}

	public String getResourceGroup() {
		return resourceGroup;
	}

	public void setResourceGroup(String resourceGroup) {
		this.resourceGroup = resourceGroup;
	}
	  
}
