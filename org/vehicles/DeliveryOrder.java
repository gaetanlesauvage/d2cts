package org.vehicles;

import java.io.Serializable;

import org.missions.Mission;
import org.system.container_stocking.ContainerLocation;
import org.time.Time;
import org.util.Location;


public class DeliveryOrder implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -1778975550779425546L;
	private ContainerLocation newDeliveryLocation;
	private Mission m;
	private Location gotoLocation;
	private Time waitingTime;
	
	public DeliveryOrder (Mission m, ContainerLocation newDeliveryLocation){
		this.m = m;
		this.newDeliveryLocation = newDeliveryLocation;
	}
	
	public DeliveryOrder (Mission m, Location gotoLocation, Time waitingTime){
		this.m = m;
		this.gotoLocation = gotoLocation;
		this.waitingTime = waitingTime;
	}
	
	public Mission getMission(){
		return m;
	}
	
	public ContainerLocation getNewDeliveryLocation(){
		return newDeliveryLocation;
	}
	
	public Location getGotoLocation(){
		return gotoLocation;
	}
	
	public Time getWaitingTime(){
		return waitingTime;
	}
}