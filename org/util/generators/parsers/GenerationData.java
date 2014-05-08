package org.util.generators.parsers;

public class GenerationData {
	protected String groupID;
	
	protected GenerationData(String groupID){
		this.groupID = groupID;
	}
	

	public String getGroupID(){
		return groupID;
	}
	
	@Override
	public int hashCode () {
		return this.groupID.hashCode();
	}
	
	@Override
	public boolean equals(Object o){
		return o.hashCode() == hashCode();
	}
	
	@Override
	public String toString(){
		return this.groupID;
	}
}
