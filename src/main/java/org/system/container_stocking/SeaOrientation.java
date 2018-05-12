package org.system.container_stocking;

public enum SeaOrientation {
	N("NORTH"),
	E("EAST"),
	S("SOUTH"),
	W("WEST"),
	NE("NORTH EAST"),
	SE("SOUTH EAST"),
	SW("SOUTH WEST"),
	NW("NORTH WEST");
	
	private String description;
	
	private SeaOrientation (String description){
		this.description = description;
	}
	
	public String getDescription(){
		return description;
	}

	public static SeaOrientation getOrientation(String value) {
		for(SeaOrientation so : SeaOrientation.values()){
			if(so.getDescription().equals(value)) return so;
		}
		return null;
	}
}
