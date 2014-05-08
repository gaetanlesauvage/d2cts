package org.missions;

public class PrethreadCondition implements Condition {
	private Load prethreadLoad;
	
	public PrethreadCondition(Load prethreadLoad){
		this.prethreadLoad = prethreadLoad;
	}
	
	public boolean canStart() {
		return prethreadLoad.getState() != MissionState.STATE_TODO;
	}
	
	public Mission getConditionMission(){
		return prethreadLoad.getMission();
	}
	
	public Load getConditionLoad(){
		return prethreadLoad;
	}
}
