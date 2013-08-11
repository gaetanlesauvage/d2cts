package org.com.model;

import java.util.HashMap;
import java.util.Map;

import org.com.model.scheduling.SchedulingParametersBeanInterface;

public class SchedulingAlgorithmBean {
	private Integer id;
	private String name;
	private String _class;
	private Map<String, SchedulingParametersBeanInterface> parameters;
	
	
	public SchedulingAlgorithmBean() {
		
	}

	public void setParameters(SchedulingParametersBeanInterface[] parameters){
		this.parameters = new HashMap<>(parameters.length);
		for(SchedulingParametersBeanInterface p : parameters){
			this.parameters.put(p.name(),p);
		}
	}
	
	public Map<String,SchedulingParametersBeanInterface> getParameters(){
		return this.parameters;
	}
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getJavaClass() {
		return _class;
	}

	public void setJavaClass(String _class) {
		this._class = _class;
	}

	@Override
	public int hashCode() {
		return name.hashCode();
	}

	@Override
	public boolean equals(Object o) {
		if(o == null) return false;
		return hashCode() == o.hashCode();
	}
	
	@Override
	public String toString(){
		return name;
	}

}
