package org.scheduling.display;

public enum IndicatorPaneOverallColumns {
	DISTANCE("Distance (meters)",0), OVERSPENT_TIME("Overspent time (hh:mm:ss)",1), TW_OVERRUN("Number of TW overruns",2), MISSIONS_DONE("Completed missions",3), WAIT_TIME("Wait time",4), SCORE("Fitness",5);
	
	private int index;
	private String label;
	
	private IndicatorPaneOverallColumns(String label, int index){
		this.label = label;
		this.index = index;
		
	}
	
	public String getLabel(){
		return label;
	}
	
	public int getIndex(){
		return index;
	}
	
	public String toString(){
		return label;
	}
}
