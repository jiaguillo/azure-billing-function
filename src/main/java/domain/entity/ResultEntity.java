package domain.entity;

import org.apache.commons.lang3.builder.ToStringBuilder;

import com.microsoft.azure.storage.table.TableServiceEntity;

public class ResultEntity extends TableServiceEntity {

	public ResultEntity(String period, String subscriptionName) {
		this.partitionKey = period;
		this.rowKey = subscriptionName;
	}
	
	public ResultEntity() {};
	
	private String codigo;
	private String elemento;
	private String owner;
	private Double margin;
	private Double cost;
	private Double extendedCost;

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
	
	public Double getCost() {
		return cost;
	}

	public void setCost(Double cost) {
		this.cost = cost;
	}

	public Double getExtendedCost() {
		return extendedCost;
	}

	public void setExtendedCost(Double extendedCost) {
		this.extendedCost = extendedCost;
	}

	@Override
	public String toString() {		
		return ToStringBuilder.reflectionToString(this);
	}
	
}
