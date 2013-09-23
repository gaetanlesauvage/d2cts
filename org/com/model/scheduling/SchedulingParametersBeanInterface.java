package org.com.model.scheduling;

public interface SchedulingParametersBeanInterface {
	public void setValue(Object value);
	public Double getValueAsDouble();
	public Integer getValueAsInteger();
	public String getValueAsString();
	public Integer getSQLID();
	public void setSQLID(Integer id);
	public String name();
	public ParameterBean getParameter();
	
}
