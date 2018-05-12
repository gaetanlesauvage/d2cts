package org.scheduling.greedy;

import org.scheduling.MissionScheduler;
import org.scheduling.MissionSchedulerEvalParameters;
import org.time.Time;

public class ScheduleScore implements Comparable<ScheduleScore>{
	private double distance;
	private double distanceInSec;
	private Time tardiness;
	private Time earliness;
	private int overspentTW;

	public ScheduleScore(){
		distance = 0;
		tardiness = new Time(Time.MIN_TIME);
		earliness = new Time(Time.MIN_TIME);
		distanceInSec = 0;
		overspentTW = 0;
	}

	public double getScore() {
		MissionSchedulerEvalParameters evalParameters = MissionScheduler.getEvalParameters();
		return (evalParameters.T()*distanceInSec + evalParameters.L() * tardiness.getInSec() + evalParameters.E()*earliness.getInSec());
	}
	
	public int compareTo(ScheduleScore score){
		return (int)(score.getScore() - getScore());
	}
	
	public double getDistance(){
		return distance;
	}
	
	public int getOverspentTW(){
		return overspentTW;
	}
	
	public Time getTardiness(){
		return tardiness;
	}
	
	public Time getEarliness(){
		return earliness;
	}
	
	public void setDistance(double dInMeters, Double dInSec){
		this.distance = dInMeters;
		this.distanceInSec = dInSec;
	}
	
	public Double getDistanceInSec() {
		return this.distanceInSec;
	}
	
	public void setTardiness(Time t){
		this.tardiness = t;
		if(t.getInSec()>0) 
			this.overspentTW++;
	}
	
	public void setEarliness(Time t){
		this.earliness = t;
	}
	
	public void setOverspentTW(int count){
		this.overspentTW = count;
	}
	
	public void setDistanceInSec(Double sec){
		this.distanceInSec = sec;
	}

	public void add(ScheduleScore score) {
		this.distance += score.getDistance();
		this.distanceInSec += score.getDistanceInSec();
		this.overspentTW += score.getOverspentTW();
		this.earliness.add(score.getEarliness());
		this.tardiness.add(score.getTardiness());
	}
	
	public String toString() {
		MissionSchedulerEvalParameters evalParameters = MissionScheduler.getEvalParameters();
		return evalParameters.T()+" * "+new Time(distanceInSec) +"("+distance+"m) + "+ evalParameters.L() +" * "+ tardiness +" + "+ evalParameters.E()+" * "+earliness+" = "+new Time(getScore())+" ("+getScore()+")";
	}
}