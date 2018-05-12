package org.missions;

public interface Condition {
	
	boolean canStart();
	Mission getConditionMission();
	Load getConditionLoad();
}
