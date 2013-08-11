package org.com.model.scheduling;

import org.scheduling.MissionSchedulerEvalParameters;

public enum GreedyParametersBean implements SchedulingParametersBeanInterface{
	L("L"),T("T"),E("E");
	
	private String name;
	private Double value;
	
	private Integer sqlId;
	
	private GreedyParametersBean (String name){
		this.name = name;
		this.value = null;
	}

	@Override
	public void setValue(Double value){
		this.value = value;
	}
	@Override
	public Double getValueAsDouble(){
		return this.value;
	}
	@Override
	public String getValueAsString(){
		return this.value == null ? "null" : this.value.toString(); 
	}
	@Override
	public Integer getSQLID(){
		return this.sqlId;
	}
	
	@Override
	public Integer getValueAsInteger(){
		return this.value.intValue();
	}
	
	@Override
	public void setSQLID(Integer id){
		this.sqlId = id;
	}
	@Override
	public String toString(){
		return this.name+"="+getValueAsString();
	}
	
	public static MissionSchedulerEvalParameters getEvalParameters() {
		return new MissionSchedulerEvalParameters(T.value, L.value, E.value);
	}

	public static GreedyParametersBean get(String name){
		for(GreedyParametersBean param : GreedyParametersBean.values()){
			if(param.name.equals(name))
				return param;
		}
		return null;
	}
	
	public static ParameterType[] types(){
		ParameterType[] t = new ParameterType[values().length];
		for(int i=0; i<values().length; i++){
			t[i] = ParameterType.DOUBLE;
		}
		return t;
	}
	
	public ParameterType getType(){
		return ParameterType.DOUBLE;
	}
	
	public static String[] names() {
		String[] t = new String[GreedyParametersBean.values().length];
		int i = 0;
		for(GreedyParametersBean p : GreedyParametersBean.values())
			t[i++] = p.name;
		
		return t;
	}
}
