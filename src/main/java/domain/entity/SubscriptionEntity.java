package domain.entity;

import org.apache.commons.lang3.builder.ToStringBuilder;

import com.microsoft.azure.storage.table.TableServiceEntity;

public class SubscriptionEntity extends TableServiceEntity {

	public SubscriptionEntity(String subscriptionName, String subscriptionId) {
		this.partitionKey = subscriptionName;
		this.rowKey = subscriptionId;
	}
	
	public SubscriptionEntity() {};
	
	private String codigo;
	private String elemento;
	private String subscriptions;
	private String owner;
	private Double margin;
	private Double fixedAmount;

	public String getCodigo() {
		return codigo;
	}

	public void setCodigo(String codigo) {
		this.codigo = codigo;
	}

	public String getElemento() {
		return elemento;
	}

	public void setElemento(String elemento) {
		this.elemento = elemento;
	}

	public String getSubscriptions() {
		return subscriptions;
	}

	public void setSubscriptions(String subscriptions) {
		this.subscriptions = subscriptions;
	}

	public String getOwner() {
		return owner;
	}

	public void setOwner(String owner) {
		this.owner = owner;
	}

	public Double getMargin() {
		return margin;
	}

	public void setMargin(Double margin) {
		this.margin = margin;
	}

	public Double getFixedAmount() {
		return fixedAmount;
	}

	public void setFixedAmount(Double fixedAmount) {
		this.fixedAmount = fixedAmount;
	}
	
	@Override
	public String toString() {		
		return ToStringBuilder.reflectionToString(this);
	}
	
}
