package org.scheduling;

import org.time.Time;

public class UpdateInfo {
	public double distance;
	public double travelTime;
	public UpdateInfo (double distance, double travelTime){
		this.distance = distance;
		this.travelTime = travelTime;
	}
	
	@Override
	public String toString(){
		return distance +" ("+new Time(travelTime)+")";
	}
}
