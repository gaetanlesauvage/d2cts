package org.scheduling.display;

public enum IndicatorPaneColumnsWithIcon implements IndicatorPaneColumn{
	ID("Resource ID", 0);

	private int index;
	private String label;

	private IndicatorPaneColumnsWithIcon(String label, int index){
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

