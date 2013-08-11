package org.com.model;

import java.util.Date;

public class SimulationBean {
	private Integer content;
	private Date date_rec;
	private Integer id;
	private SchedulingAlgorithmBean schedulingAlgorithm;
	private Long seed;
	
	public SimulationBean() {

	}

	public SimulationBean(Integer scenarioId, SchedulingAlgorithmBean schedulingAlgorithm) {
		this.content = scenarioId;
		this.schedulingAlgorithm = schedulingAlgorithm;
	}

	@Override
	public boolean equals(Object o) {
		return hashCode() == o.hashCode();
	}

	public Integer getContent() {
		return content;
	}

	public Date getDate_rec() {
		return date_rec;
	}

	public Integer getId() {
		return id;
	}

	public SchedulingAlgorithmBean getSchedulingAlgorithm() {
		return schedulingAlgorithm;
	}

	@Override
	public int hashCode() {
		return id.hashCode();
	}

	public void setContent(Integer scenarioId) {
		this.content = scenarioId;
	}

	public void setDate_rec(Date d) {
		this.date_rec = d;
	}

	public void setId(Integer i) {
		this.id = i;
	}

	public Long getSeed(){
		return this.seed;
	}
	
	public void setSeed(Long seed){
		this.seed = seed;
	}
	
	public void setSchedulingAlgorithm(SchedulingAlgorithmBean schedulingAlgorithm) {
		this.schedulingAlgorithm = schedulingAlgorithm;
	}

	@Override
	public String toString() {
		return id + " | " + date_rec + " | " + content+" | "+schedulingAlgorithm;
	}
}
