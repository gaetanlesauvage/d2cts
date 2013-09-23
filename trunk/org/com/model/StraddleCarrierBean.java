package org.com.model;


public class StraddleCarrierBean {
	private String name;
	private Integer scenario;
	private StraddleCarrierModelBean model;
	private String color;
	private String slot;
	private String originRoad;
	private Double originRate;
	private boolean originDirection;
	private Integer originAvailability; // Move into Enum type
	private boolean autoHandling;
	private String routingAlgorithm;
	private String routingHeuristic;
	//private Map<Time, LoadBean> workload;
	

	public StraddleCarrierBean() {

	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Integer getScenario() {
		return scenario;
	}

	public void setScenario(Integer scenario) {
		this.scenario = scenario;
	}

	public StraddleCarrierModelBean getModel() {
		return model;
	}

	public void setModel(StraddleCarrierModelBean model) {
		this.model = model;
	}

	public String getColor() {
		return color;
	}

	public void setColor(String color) {
		this.color = color;
	}

	public String getSlot() {
		return slot;
	}

	public void setSlot(String slot) {
		this.slot = slot;
	}

	public String getOriginRoad() {
		return originRoad;
	}

	public void setOriginRoad(String originRoad) {
		this.originRoad = originRoad;
	}

	public Double getOriginRate() {
		return originRate;
	}

	public void setOriginRate(Double originRate) {
		this.originRate = originRate;
	}

	public boolean isOriginDirection() {
		return originDirection;
	}

	public void setOriginDirection(boolean originDirection) {
		this.originDirection = originDirection;
	}

	public Integer getOriginAvailability() {
		return originAvailability;
	}

	public void setOriginAvailability(Integer originAvailability) {
		this.originAvailability = originAvailability;
	}

	public boolean isAutoHandling() {
		return autoHandling;
	}

	public void setAutoHandling(boolean autoHandling) {
		this.autoHandling = autoHandling;
	}

	public String getRoutingAlgorithm() {
		return routingAlgorithm;
	}

	public void setRoutingAlgorithm(String routingAlgorithm) {
		this.routingAlgorithm = routingAlgorithm;
	}

	public String getRoutingHeuristic() {
		return routingHeuristic;
	}

	public void setRoutingHeuristic(String routingHeuristic) {
		this.routingHeuristic = routingHeuristic;
	}

	@Override
	public int hashCode() {
		return name.hashCode();
	}

	@Override
	public boolean equals(Object o) {
		return hashCode() == o.hashCode();
	}

}
