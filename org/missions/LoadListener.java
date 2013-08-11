package org.missions;

public interface LoadListener {
	public void missionStarted(String missionID);
	public void missionEnded(String missionID);
	public void missionPhaseChanged(String missionID, String newPhase);
}
