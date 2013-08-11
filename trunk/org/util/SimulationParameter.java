package org.util;

import org.com.model.SchedulingAlgorithmBean;

public class SimulationParameter {
	private Integer scenarioID;
	private SchedulingAlgorithmBean schedulingAlgorithmBean;
	private Long seed;
	
	public SimulationParameter (Integer scenarioID, SchedulingAlgorithmBean schedulingAlgorithmBean, Long seed){
		this.scenarioID = scenarioID;
		this.schedulingAlgorithmBean = schedulingAlgorithmBean;
		this.seed = seed;
	}

	public Integer getScenarioID() {
		return scenarioID;
	}

	public SchedulingAlgorithmBean getSchedulingAlgorithmBean() {
		return schedulingAlgorithmBean;
	}
	
	public Long getSeed(){
		return seed;
	}
}
