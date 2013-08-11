package org.com.model.scheduling;

public interface SchedulingParametersBeanInterface {
	public void setValue(Double value);
	public Double getValueAsDouble();
	public Integer getValueAsInteger();
	public String getValueAsString();
	public Integer getSQLID();
	public void setSQLID(Integer id);
	public String name();
	
}
