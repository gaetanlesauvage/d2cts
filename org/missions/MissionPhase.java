package org.missions;

public enum MissionPhase {
	PHASE_PICKUP(0, "Pickup"),
	PHASE_LOAD(1, "Loading"),
	PHASE_DELIVERY(2, "Delivery"),
	PHASE_UNLOAD(3, "Unloading");
	
	private int code;
	private String label;
	
	private MissionPhase(int code, String label){
		this.code = code;
		this.label = label;
	}
	
	public int getCode(){
		return this.code;
	}

	public static MissionPhase get(int code){
		if(code >= 0 && code < values().length)
			return values()[code];
		else 
			return null;
	}
	
	public MissionPhase next() {
		return get(code+1);
	}
	
	public String getLabel(){
		return label;
	}
	
	public String toString() {
		return name()+" : "+code+" - "+label;
	}
}
