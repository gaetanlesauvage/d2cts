package org.com;

public enum ScriptName {
	SIMULATION("writeSimulation.php"),
	LOCATION("writeLocation.php"),
	EVENT("writeEvent.php"),
	MISSIONS("writeMission.php"),
	READLOCATION("selectLocationsAccordingToTime.php"),
	CHECK("checkConnection.php"),
	SELECT("select.php");
	
	private String name;
	private ScriptName(String name){
		this.name = name;
	}
	
	public String getName(){
		return name;
	}
}
