package org.system.container_stocking;

public class Quay extends Block {
	private SeaOrientation seaOrientation;
	private String borderID;
	
	public Quay (String id, SeaOrientation seaOrientation, String borderID){
		super(id,BlockType.SHIP);
		this.seaOrientation = seaOrientation;
		this.borderID = borderID;
	}
	
	public SeaOrientation getSeaOrientation(){
		return seaOrientation;
	}
	
	public String getBorderID(){
		return borderID;
	}
}
