package org.util.dbLoading;

import java.util.Comparator;


public enum XMLTags {
	BAY("bay","bay"),
	BAYCROSSROAD("bayCrossroad","bayCrossroad"),
	BLOCK("block","block"),
	CONTAINER("container","container"),
	CONTAINERLOCATION("containerLocation","containerLocation"),
	COORDINATE("coordinate","wallPoint"),
	CROSSROAD("crossroad","crossroad"),
	CURRENT(),
	DEPOT("depot","depot"),
	EVENT("event","event"),
	EXCHANGEBAY("exchangebay","bay"),
	INCLUDE("include","include"),
	LASERHEAD("laserhead","laserHead"),
	MISSION("mission","mission"),
	MISSIONS("missions","missions"),
	ROAD("road","road"),
	ROADPOINT("roadpoint","roadPoint"),
	ROUTING("routing","routing"),
	SCENARIO("scenario","scenario"),
	STRADDLECARRIER("straddleCarrier","straddleCarrier"),
	STRADDLECARRIERSLOT("straddleCarrierSlot","straddleCarrierSlot"),
	TERMINAL("terminal","terminal"),
	TIMEWINDOW("timewindow","timeWindow"),
	TYPE("type","straddleCarrierModel"),
	VEHICLES("vehicles",null),
	WALL("wall","wall"),
	SCHEDULER("scheduler","scheduler");

	private static final class XMLTagComparator implements Comparator<XMLTags>{
		public int compare(XMLTags t1, XMLTags t2){
			if(t1 != t2){
				if(t1.tag.equals(t2.tag)) return 0;
				else return -1;
			} else {
				return 2;
			}
			
		}
	}
	public static XMLTags getTag(String tagName){
		CURRENT.setTag(tagName);
		XMLTagComparator c = new XMLTagComparator();
		for(XMLTags t : XMLTags.values()){
			if(c.compare(CURRENT, t) == 0){
				return t;
			}
		}
		return null;
	}
	
	private String method;
	
	private String tag;
	
	private XMLTags(){}
	
	private XMLTags (String tag,String method){
		this.tag = tag;
		this.method = method;
	}
	
	public String getMethod(){
		return this.method;
	}
	
	public final String getTag(){
		return this.tag;
	}
	
	public void setTag(String tag){
		this.tag = tag;
	}
}
