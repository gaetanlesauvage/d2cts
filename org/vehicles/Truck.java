package org.vehicles;

import java.io.Serializable;

import org.time.Time;


public class Truck implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = -856825000507941115L;

	private String ID;
	private Time arrivalTime;
	private Time realArrivalTime;
	private Time departureTime;
	private Time realDepartureTime;
	private String slotID;
	
	public Truck(String ID, Time arrivalTime, String slotID){
		this.ID = ID;
		this.slotID = slotID;
		
		this.arrivalTime = arrivalTime;
	}
	
	public void setDepartureTime(Time departureTime){
		this.departureTime = departureTime;
	}
	
	public void setRealArrivalTime(Time realArrivalTime) {
		this.realArrivalTime = realArrivalTime;
	}

	public void setRealDepartureTime(Time realDepartureTime) {
		this.realDepartureTime = realDepartureTime;
	}

	public void in (Time realArrivalTime) {
		this.realArrivalTime = realArrivalTime;
	}
	
	public void out (Time departureTime){
		this.realDepartureTime = departureTime;
	}
	
	public String getID(){
		return ID;
	}
	
	public String getSlotID(){
		return slotID;
	}

	public Time getArrivalTime() {
		return arrivalTime;
	}

	public Time getRealArrivalTime() {
		return realArrivalTime;
	}

	public Time getDepartureTime() {
		return departureTime;
	}

	public Time getRealDepartureTime() {
		return realDepartureTime;
	}
	
	
}
