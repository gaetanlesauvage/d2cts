package org.com.model;


public enum ContainerType {
	fortyFeet(0, 2.0),
	twentyFeet(1, 1.0),
	fortyFiveFeet(2, 2.25);
	
	public static ContainerType get(int id){
		for(ContainerType t : values()){
			if(t.id == id)
				return t;
		}
		return null;
	}
	
	public static ContainerType get(double teu){
		for(ContainerType t : values()){
			if(t.teu == teu)
				return t;
		}
		return null;
	}
	
	public Integer getID() {
		return id;
	}
	
	public static Integer getID(double teu){
		return get(teu).id;
	}
	
	private final int id;
	private final double teu;
	
	private ContainerType(int id, double teu){
		this.id = id;
		this.teu = teu;
	}

	public double getTEU() {
		return teu;
	}

}
