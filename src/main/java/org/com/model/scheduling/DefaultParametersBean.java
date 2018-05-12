package org.com.model.scheduling;


public class DefaultParametersBean{
	private String name;
	private String value;
	
	
	public DefaultParametersBean (){
		
	}

	public void setValue(String value){
		this.value = value;
	}
	
	public String getValue(){
		return this.value;
	}
	
	public String getName(){
		return this.name;
	}
	
	public void setName(String name){
		this.name = name;
	}
}
