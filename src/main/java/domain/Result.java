package domain;

public class Result {
	private String codigo;
	private String elemento;
	private String subscription;
	private String owner;
	private Double cost = 0.0;
	private Double margin = 0.0;
	private Double fixedAmount;
	
	
	public Result(String codigo, String elemento, String subscription, String owner, Double margin, Double fixedAmount) {
		super();
		this.codigo = codigo;
		this.elemento = elemento;
		this.subscription = subscription;
		this.owner = owner;
		this.margin = margin;
		this.fixedAmount = fixedAmount;
	}
	public Double getCost() {
		return cost;
	}
	public void setCost(Double cost) {
		this.cost = cost;
	}
	public void addCost(Double cost) {
		this.cost += cost;
	}
	public Double getExtendedCost() {
		return fixedAmount != null ? fixedAmount : cost / (1 - margin / 100);
	}
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
	public String getSubscription() {
		return subscription;
	}
	public void setSubscription(String subscription) {
		this.subscription = subscription;
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

	
}
