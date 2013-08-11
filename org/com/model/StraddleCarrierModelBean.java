package org.com.model;

public class StraddleCarrierModelBean {
	private String name;
	private Double width;
	private Double height;
	private Double length;
	private Double innerWidth;
	private Double innerLength;
	private Double backOverLength;
	
	private Double frontOverLength;
	private Double cabWidth;
	private String compatibility;
	private Double emptySpeed;
	private Double loadedSpeed;
	private Double baySpeed;
	private Double containerHandlingFromTruckMin;
	private Double containerHandlingFromTruckMax;
	private Double containerHandlingFromGroundMin;
	private Double containerHandlingFromGroundMax;
	private Double enterExitBayTimeMin;
	private Double enterExitBayTimeMax;
	private Double turnBackTime;
	
	public StraddleCarrierModelBean() {

	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Double getWidth() {
		return width;
	}

	public void setWidth(Double width) {
		this.width = width;
	}

	public Double getHeight() {
		return height;
	}

	public void setHeight(Double height) {
		this.height = height;
	}

	public Double getLength() {
		return length;
	}

	public void setLength(Double length) {
		this.length = length;
	}

	public Double getInnerWidth() {
		return innerWidth;
	}

	public void setInnerWidth(Double innerWidth) {
		this.innerWidth = innerWidth;
	}

	public Double getInnerLength() {
		return innerLength;
	}

	public void setInnerLength(Double innerLength) {
		this.innerLength = innerLength;
	}

	public Double getBackOverLength() {
		return backOverLength;
	}

	public void setBackOverLength(Double backOverLength) {
		this.backOverLength = backOverLength;
	}

	public Double getFrontOverLength() {
		return frontOverLength;
	}

	public void setFrontOverLength(Double frontOverLength) {
		this.frontOverLength = frontOverLength;
	}

	public Double getCabWidth() {
		return cabWidth;
	}

	public void setCabWidth(Double cabWidth) {
		this.cabWidth = cabWidth;
	}

	public String getCompatibility() {
		return compatibility;
	}

	public void setCompatibility(String compatibility) {
		this.compatibility = compatibility;
	}

	public Double getEmptySpeed() {
		return emptySpeed;
	}

	public void setEmptySpeed(Double emptySpeed) {
		this.emptySpeed = emptySpeed;
	}

	public Double getLoadedSpeed() {
		return loadedSpeed;
	}

	public void setLoadedSpeed(Double loadedSpeed) {
		this.loadedSpeed = loadedSpeed;
	}

	public Double getBaySpeed() {
		return baySpeed;
	}

	public void setBaySpeed(Double baySpeed) {
		this.baySpeed = baySpeed;
	}

	public Double getContainerHandlingFromTruckMin() {
		return containerHandlingFromTruckMin;
	}

	public void setContainerHandlingFromTruckMin(
			Double containerHandlingFromTruckMin) {
		this.containerHandlingFromTruckMin = containerHandlingFromTruckMin;
	}

	public Double getContainerHandlingFromTruckMax() {
		return containerHandlingFromTruckMax;
	}

	public void setContainerHandlingFromTruckMax(
			Double containerHandlingFromTruckMax) {
		this.containerHandlingFromTruckMax = containerHandlingFromTruckMax;
	}

	public Double getContainerHandlingFromGroundMin() {
		return containerHandlingFromGroundMin;
	}

	public void setContainerHandlingFromGroundMin(
			Double containerHandlingFromGroundMin) {
		this.containerHandlingFromGroundMin = containerHandlingFromGroundMin;
	}

	public Double getContainerHandlingFromGroundMax() {
		return containerHandlingFromGroundMax;
	}

	public void setContainerHandlingFromGroundMax(
			Double containerHandlingFromGroundMax) {
		this.containerHandlingFromGroundMax = containerHandlingFromGroundMax;
	}

	public Double getEnterExitBayTimeMin() {
		return enterExitBayTimeMin;
	}

	public void setEnterExitBayTimeMin(Double enterExitBayTimeMin) {
		this.enterExitBayTimeMin = enterExitBayTimeMin;
	}

	public Double getEnterExitBayTimeMax() {
		return enterExitBayTimeMax;
	}

	public void setEnterExitBayTimeMax(Double enterExitBayTimeMax) {
		this.enterExitBayTimeMax = enterExitBayTimeMax;
	}

	public Double getTurnBackTime() {
		return turnBackTime;
	}

	public void setTurnBackTime(Double turnBackTime) {
		this.turnBackTime = turnBackTime;
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
