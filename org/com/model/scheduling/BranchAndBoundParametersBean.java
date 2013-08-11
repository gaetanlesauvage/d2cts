package org.com.model.scheduling;

import org.scheduling.MissionSchedulerEvalParameters;

public enum BranchAndBoundParametersBean implements SchedulingParametersBeanInterface {
	COMPUTE_COST("ComputeCosts",ParameterType.INTEGER), DISTANCE_MATRIX_FILE("DistanceMatrixFile",ParameterType.STRING), E(
			"E",ParameterType.DOUBLE), L("L",ParameterType.DOUBLE), SOLUTION_FILE("SolutionFile",ParameterType.STRING), SOLUTION_INIT_FILE(
			"SolutionInitFile",ParameterType.STRING), T("T",ParameterType.DOUBLE), TIME_MATRIX_FILE("TimeMatrixFile",ParameterType.STRING);

	private String name;
	private Object value;

	private ParameterType type;
	private Integer sqlId;

	private BranchAndBoundParametersBean(String name, ParameterType type) {
		this.name = name;
		this.value = null;
		this.type = type;
	}

	@Override
	public void setValue(Double value) {
		this.value = value;
	}

	@Override
	public Double getValueAsDouble() {
		if(type == ParameterType.DOUBLE)
		return (Double) this.value;
		else 
			return null;
	}
	
	@Override
	public Integer getValueAsInteger(){
		if(type == ParameterType.INTEGER)
			return (Integer)this.value;
		else if (type == ParameterType.DOUBLE) return ((Double)this.value).intValue();
		else return null;
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

	public static MissionSchedulerEvalParameters getEvalParameters() {
		return new MissionSchedulerEvalParameters(T.getValueAsDouble(), L.getValueAsDouble(), E.getValueAsDouble());
	}

	public static BranchAndBoundParametersBean get(String name) {
		for (BranchAndBoundParametersBean param : BranchAndBoundParametersBean
				.values()) {
			if (param.name.equals(name))
				return param;
		}
		return null;
	}
	
	public static String[] names(){
		String[] t = new String[values().length];
		int i=0;
		for(BranchAndBoundParametersBean p : values()){
			t[i++] = p.name;
		}
		return t;
	}
	
	public static ParameterType[] types(){
		ParameterType[] t = new ParameterType[values().length];
		int i=0;
		for(BranchAndBoundParametersBean p : values()){
			t[i++] = p.type;
		}
		return t;
	}
}
