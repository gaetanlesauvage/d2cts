package org.com.model.scheduling;

import org.scheduling.MissionSchedulerEvalParameters;


public class ResultsBean {
	private Integer simulation;
	private Double distanceInSec;
	private Double lateness;
	private Double earliness;
	private Integer ot;
	private Double timeInSec;
	private Double schedulingTimeInMs;
	
	public ResultsBean () {
		
	}

	public Integer getSimulation() {
		return simulation;
	}

	public void setSimulation(Integer simulation) {
		this.simulation = simulation;
	}

	public Double getDistance() {
		return distanceInSec;
	}

	public void setDistance(Double distance) {
		this.distanceInSec = distance;
	}

	public Double getLateness() {
		return lateness;
	}

	public void setLateness(Double lateness) {
		this.lateness = lateness;
	}

	public Double getEarliness() {
		return earliness;
	}

	public void setEarliness(Double earliness) {
		this.earliness = earliness;
	}

	public Integer getOt() {
		return ot;
	}

	public void setOt(Integer ot) {
		this.ot = ot;
	}
	
	public Double getOverallTime(){
		return timeInSec;
	}
	
	public void setOverallTime(Double d){
		this.timeInSec = d;
	}
	
	public Double getScore(MissionSchedulerEvalParameters coef){
		return distanceInSec*coef.getTravelTimeCoeff()+lateness*coef.getLatenessCoeff()+earliness*coef.getEarlinessCoeff();
	}
	
	@Override
	public int hashCode() {
		return simulation;
	}
	
	@Override
	public boolean equals(Object o){
		return hashCode() == o.hashCode();
	}

	public void setSchedulingTime(Double d) {
		this.schedulingTimeInMs = d;
	}
	
	public Double getSchedulingTime(){
		return this.schedulingTimeInMs;
	}
}


