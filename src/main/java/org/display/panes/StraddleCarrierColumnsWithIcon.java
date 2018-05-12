package org.display.panes;

public enum StraddleCarrierColumnsWithIcon implements PaneColumn {
	ID(0, 50);
	private int index;
	private int width;
	
	private StraddleCarrierColumnsWithIcon (int index, int width){
		this.index = index;
		this.width = width;
	}
	
	public int getIndex(){
		return index;
	}
	
	public int getWidth(){
		return width;
	}
}