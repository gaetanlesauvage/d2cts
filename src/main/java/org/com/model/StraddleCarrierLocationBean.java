package org.com.model;

import java.sql.Time;

public class StraddleCarrierLocationBean {
	private Integer simID;
	private String straddleCarrierName;
	private Time t;
	private String road;
	private boolean direction;
	private Double x;
	private Double y;
	private Double z;
	
	public StraddleCarrierLocationBean(){
		
	}

	public Integer getSimID() {
		return simID;
	}

	public void setSimID(Integer simID) {
		this.simID = simID;
	}

	public String getStraddleCarrierName() {
		return straddleCarrierName;
	}

	public void setStraddleCarrierName(String straddleCarrierName) {
		this.straddleCarrierName = straddleCarrierName;
	}

	public Time getT() {
		return t;
	}

	public void setT(Time t) {
		this.t = t;
	}

	public String getRoad() {
		return road;
	}

	public void setRoad(String road) {
		this.road = road;
	}

	public boolean isDirection() {
		return direction;
	}

	public void setDirection(boolean direction) {
		this.direction = direction;
	}

	public Double getX() {
		return x;
	}

	public void setX(Double x) {
		this.x = x;
	}

	public Double getY() {
		return y;
	}

	public void setY(Double y) {
		this.y = y;
	}

	public Double getZ() {
		return z;
	}

	public void setZ(Double z) {
		this.z = z;
	}
	
	
}
