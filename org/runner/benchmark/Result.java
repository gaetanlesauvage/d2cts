package org.runner.benchmark;

import java.util.HashMap;

import org.time.Time;


public class Result{
	ConfigFile configFile;
	HashMap<String, Double> distance;
	HashMap<String, Time> overspentTime;
	HashMap<String, Integer> completedMissions;
	HashMap<String, Integer> overruns;
	double overall_distance;
	Time overall_overspentTime;
	int overall_completeMissions;
	int overall_overruns;
	Time msComputingTime;
	Time execTime;

	public Result(ConfigFile configFile){
		this.configFile = configFile;
		this.distance = new HashMap<String, Double>(configFile.resourceSize);
		this.overspentTime = new HashMap<String, Time>(configFile.resourceSize);
		this.completedMissions = new HashMap<String, Integer>(configFile.resourceSize);
		this.overruns = new HashMap<String, Integer>(configFile.resourceSize);
	}

	public void setDistance(String resourceID, Double distance){
		this.distance.put(resourceID, distance);
	}

	public void setDistance(double distance){
		this.overall_distance = distance;
	}

	public void setOverspentTime(String resourceID, Time t){
		this.overspentTime.put(resourceID, t);
	}

	public void setOverspentTime(Time t){
		this.overall_overspentTime = t;
	}

	public void setCompletedMissions(String resourceID, Integer completedMissions){
		this.completedMissions.put(resourceID, completedMissions);
	}

	public void setCompletedMissions(int completedMissions){
		this.overall_completeMissions = completedMissions;
	}

	public void setOverruns(String resourceID, Integer overruns){
		this.overruns.put(resourceID, overruns);
	}

	public void setOverruns(int overruns){
		this.overall_overruns = overruns;
	}

	public void setMSComputingTime(Time msCptTime) {
		this.msComputingTime = msCptTime;
	}

	public void setExecTime(Time execTime) {
		this.execTime = execTime;
	}
	
	public boolean equals(Result r){
		return configFile.equals(r.configFile);
	}

}