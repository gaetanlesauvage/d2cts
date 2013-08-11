package org.scheduling.display;

public enum IndicatorPaneColumns implements IndicatorPaneColumn{
	DISTANCE("Distance (meters)",1), OVERSPENT_TIME("Overspent time (hh:mm:ss)",2), TW_OVERRUN("Number of TW overruns",3), MISSIONS_DONE("Completed missions",4), WAIT_TIME("Wait time",5), SCORE("Fitness",6);
	
	private int index;
	private String label;
	
	private IndicatorPaneColumns(String label, int index){
		this.label = label;
		this.index = index;
		
	}
	
	public int getIndex(){
		return index;
	}
	
	public String toString(){
		return label;
	}
}

