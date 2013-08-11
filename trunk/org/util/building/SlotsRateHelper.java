package org.util.building;

public class SlotsRateHelper {
	private String type;
	private Double rate;
	
	public SlotsRateHelper (String type, Double rate){
		this.type = type;
		this.rate = rate;
	}

	public String getType() {
		return type;
	}

	public Double getRate() {
		return rate;
	}
	
	
}
