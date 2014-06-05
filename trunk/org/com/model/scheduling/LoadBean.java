package org.com.model.scheduling;

import java.sql.Time;

import org.missions.MissionPhase;
import org.missions.MissionState;

public class LoadBean {
	private Long ID;
	private Time t;
	private Time twMin;
	private Time twMax;
	private String mission;
	private Time startableTime;
	private MissionState state;
	private MissionPhase phase;
	private Time effectiveStartTime;
	private Time pickupReachTime;
	private Time loadStartTime;
	private Time loadEndTime;
	private Time deliveryStartTime;
	private Time deliveryReachTime;
	private Time unloadStartTime;
	private Time endTime;
	private Time waitTime;
	private Long linkedLoad;
	private Integer loadIndex;
	private String straddleCarrierName;
	
	
	
	public String getStraddleCarrierName() {
		return straddleCarrierName;
	}

	public void setStraddleCarrierName(String straddleCarrierName) {
		this.straddleCarrierName = straddleCarrierName;
	}

	public LoadBean(){
		
	}

	public Long getID() {
		return ID;
	}

	public void setID(Long iD) {
		ID = iD;
	}

	public org.time.Time getTwMin() {
		return new org.time.Time(twMin);
	}

	public void setTwMin(Time twMin) {
		this.twMin = twMin;
	}

	public org.time.Time getTwMax() {
		return new org.time.Time(twMax);
	}

	public void setTwMax(Time twMax) {
		this.twMax = twMax;
	}

	public String getMission() {
		return mission;
	}

	public void setMission(String mission) {
		this.mission = mission;
	}

	public org.time.Time getStartableTime() {
		return new org.time.Time(startableTime);
	}

	public void setStartableTime(Time startableTime) {
		this.startableTime = startableTime;
	}

	public MissionState getState() {
		return state;
	}

	public void setState(MissionState state) {
		this.state = state;
	}

	public MissionPhase getPhase() {
		return phase;
	}

	public void setPhase(MissionPhase phase) {
		this.phase = phase;
	}

	public org.time.Time getEffectiveStartTime() {
		return new org.time.Time(effectiveStartTime);
	}

	public void setEffectiveStartTime(Time effectiveStartTime) {
		this.effectiveStartTime = effectiveStartTime;
	}

	public org.time.Time getPickupReachTime() {
		return new org.time.Time(pickupReachTime);
	}

	public void setPickupReachTime(Time pickupReachTime) {
		this.pickupReachTime = pickupReachTime;
	}

	public org.time.Time getLoadStartTime() {
		return new org.time.Time(loadStartTime);
	}

	public void setLoadStartTime(Time loadStartTime) {
		this.loadStartTime = loadStartTime;
	}

	public org.time.Time getLoadEndTime() {
		return new org.time.Time(loadEndTime);
	}

	public void setLoadEndTime(Time loadEndTime) {
		this.loadEndTime = loadEndTime;
	}

	public org.time.Time getDeliveryStartTime() {
		return new org.time.Time(deliveryStartTime);
	}

	public void setDeliveryStartTime(Time deliveryStartTime) {
		this.deliveryStartTime = deliveryStartTime;
	}

	public org.time.Time getDeliveryReachTime() {
		return new org.time.Time(deliveryReachTime);
	}

	public void setDeliveryReachTime(Time deliveryReachTime) {
		this.deliveryReachTime = deliveryReachTime;
	}

	public org.time.Time getUnloadStartTime() {
		return new org.time.Time(unloadStartTime);
	}

	public void setUnloadStartTime(Time unloadStartTime) {
		this.unloadStartTime = unloadStartTime;
	}

	public org.time.Time getEndTime() {
		return new org.time.Time(endTime);
	}

	public void setEndTime(Time endTime) {
		this.endTime = endTime;
	}

	public org.time.Time getWaitTime() {
		return new org.time.Time(waitTime);
	}

	public void setWaitTime(Time waitTime) {
		this.waitTime = waitTime;
	}

	public Long getLinkedLoad() {
		return linkedLoad;
	}

	public void setLinkedLoad(Long linkedLoad) {
		this.linkedLoad = linkedLoad;
	}

	public void setLoadIndex(Integer index) {
		this.loadIndex = index;
	}
	
	public Integer getLoadIndex(){
		return this.loadIndex;
	}

	public void setT(Time time) {
		this.t = time;
	}
	
	public Time getT() {
		return this.t;
	}
	
}
