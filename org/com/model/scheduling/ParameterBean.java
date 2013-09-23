package org.com.model.scheduling;

public class ParameterBean implements SchedulingParametersBeanInterface{
	private String name;
	private Object value;
	private ParameterType type;
	private Integer sqlId;
	
	public ParameterBean (String name, ParameterType type){
		this.name = name;
		this.type = type;
	}
	
	@Override
	public void setValue(Object o) {
		if(o instanceof String && type != ParameterType.STRING){
			switch(type){
			case DOUBLE :
				this.value = Double.parseDouble((String)o);
				break;
			case INTEGER :
				this.value = Integer.parseInt((String)o);
				break;
			default:
				break;
			}
		} else {
			this.value = o;
		}
	}

	@Override
	public Double getValueAsDouble() {
		if(type == ParameterType.DOUBLE)
			return (Double)this.value;
		else 
			return null;
	}

	@Override
	public Integer getValueAsInteger(){
		if(type == ParameterType.INTEGER)
			return (Integer)this.value;
		else if(type == ParameterType.DOUBLE)
			return ((Double)this.value).intValue();
		else
			return null;
	}
	
	@Override
	public String getValueAsString() {
		return this.value == null ? "null" : this.value.toString();
	}

	@Override
	public Integer getSQLID() {
		return this.sqlId;
	}

	@Override
	public void setSQLID(Integer id) {
		this.sqlId = id;
	}

	@Override
	public String toString() {
		return this.name + "=" + getValueAsString();
	}
	
	public String name(){
		return this.name;
	}
	
	@Override
	public ParameterBean getParameter(){
		ParameterBean p = new ParameterBean(name, type);
		p.setValue(value);
		p.setSQLID(getSQLID());
		return p;
	}
}
