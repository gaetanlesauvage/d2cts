package org.com.model.scheduling;

import org.scheduling.MissionSchedulerEvalParameters;
import org.scheduling.onlineACO.ACOParameters;

/**
 * OnlineACO Scheduling Algorithm Parameters Bean
 * @author gaetan
 *
 */
public enum OnlineACOParametersBean implements SchedulingParametersBeanInterface {
	ALPHA("Alpha"), BETA("Beta"), GAMMA("Gamma"), DELTA("Delta"),LAMBDA("Lambda"), LAMBDA_RETURN("LAMBDA"),ETA("Eta"),SYNC("Sync"),
	F1("F1"),F2("F2"),F3("F3"),L("L"),T("T"),E("E");
	
	
	private String name;
	private Double value;
	
	private Integer sqlId;
	
	private OnlineACOParametersBean (String name){
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
	public void setSQLID(Integer id){
		this.sqlId = id;
	}
	@Override
	public String toString(){
		return this.name+"="+getValueAsString();
	}
	
	public ParameterType getType(){
		return ParameterType.DOUBLE;
	}
	
	public static MissionSchedulerEvalParameters getEvalParameters(){
		return new MissionSchedulerEvalParameters(T.getValueAsDouble(), L.getValueAsDouble(), E.getValueAsDouble());
	}
	
	public static ACOParameters getACOParameters(){
		return new ACOParameters(ALPHA.getValueAsDouble(), BETA.getValueAsDouble(),
				GAMMA.getValueAsDouble(),
				DELTA.getValueAsDouble(),
				ETA.getValueAsDouble(),
				LAMBDA.getValueAsDouble(),
				LAMBDA_RETURN.getValueAsDouble(),
				SYNC.getValueAsDouble().intValue(),
				F1.getValueAsDouble(),
				F2.getValueAsDouble(),
				F3.getValueAsDouble());
	}
	
	public static OnlineACOParametersBean get(String name){
		for(OnlineACOParametersBean param : OnlineACOParametersBean.values()){
			if(param.name.equals(name))
				return param;
		}
		return null;
	}
	
	public static String[] names(){
		String[] t = new String[values().length];
		int i=0;
		for(OnlineACOParametersBean p : values()){
			t[i++] = p.name;
		}
		return t;
	}
	
	public static ParameterType[] types(){
		ParameterType[] t = new ParameterType[values().length];
		for(int i=0; i<values().length; i++){
			t[i] = ParameterType.DOUBLE;
		}
		return t;
	}
	
	@Override
	public Integer getValueAsInteger() {
		return this.value.intValue();
	}
	
}
