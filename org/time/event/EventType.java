package org.time.event;

public enum EventType {
	AffectMission("org.time.event.AffectMission"),
	ChangeContainerLocation("org.time.event.ChangeContainerLocation"),
	ContainerOut("org.time.event.ContainerOut"),
	LaserHeadFailure("org.time.event.LaserHeadFailure"),
	NewContainer("org.time.event.NewContainer"),
	NewMission("org.time.event.NewMission"),
	NewShipContainer("org.time.event.NewShipContainer"),
	ShipContainerOut("org.time.event.ShipContainerOut"),
	ShipIn("org.time.event.ShipIn"),
	ShipOut("org.time.event.ShipOut"),
	StraddleCarrierFailure("org.time.event.StraddleCarrierFailure"),
	StraddleCarrierRepaired("org.time.event.StraddleCarrierRepaired"),
	VehicleIn("org.time.event.VehicleIn"),
	VehicleOut("org.time.event.VehicleOut");
	
	private String classForName;
	
	private EventType ( String classForName ){
		this.classForName = classForName;
	}
	
	public static EventType get(String name){
		for(EventType t : values()){
			if(t.name().equalsIgnoreCase(name)){
				return t;
			}
		}
		return null;
	}
	
	public String getClassForName(){
		return this.classForName;
	}
}
