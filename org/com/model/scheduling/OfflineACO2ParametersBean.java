package org.com.model.scheduling;

import java.util.ArrayList;
import java.util.List;

import org.scheduling.MissionSchedulerEvalParameters;
import org.scheduling.offlineACO.OfflineSchedulerParameters;

public enum OfflineACO2ParametersBean implements SchedulingParametersBeanInterface {
	ALPHA("Alpha"), BETA("Beta"), GAMMA("Gamma"), RHO("Rho"), LAMBDA("Lambda"), SYNC("Sync"), F1("F1"), F2("F2"), F3("F3"), L("L"), T("T"), E("E");

	private String name;
	private Double value;

	private Integer sqlId;

	private OfflineACO2ParametersBean(String name) {
		this.name = name;
		this.value = null;
	}

	@Override
	public void setValue(Object o) {
		if (o instanceof String) {
			this.value = Double.parseDouble((String) o);
		} else {
			this.value = (Double) o;
		}
	}

	@Override
	public Double getValueAsDouble() {
		return this.value;
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

	public ParameterType getType() {
		return ParameterType.DOUBLE;
	}

	public static OfflineSchedulerParameters getParameters() {
		return new OfflineSchedulerParameters(ALPHA.value, BETA.value, GAMMA.value, RHO.value, LAMBDA.value, SYNC.value.intValue(), F1.value,
				F2.value, F3.value);
	}

	public static MissionSchedulerEvalParameters getEvalParameters() {
		return new MissionSchedulerEvalParameters(T.value, L.value, E.value);
	}

	public static OfflineACO2ParametersBean get(String name) {
		for (OfflineACO2ParametersBean param : OfflineACO2ParametersBean.values()) {
			if (param.name.equalsIgnoreCase(name))
				return param;
		}
		return null;
	}

	public static String[] names() {
		String[] t = new String[values().length];
		int i = 0;
		for (OfflineACO2ParametersBean p : values()) {
			t[i++] = p.name;
		}
		return t;
	}

	public static ParameterType[] types() {
		ParameterType[] t = new ParameterType[values().length];
		for (int i = 0; i < values().length; i++) {
			t[i] = ParameterType.DOUBLE;
		}
		return t;
	}

	@Override
	public Integer getValueAsInteger() {
		return value.intValue();
	}

	public static List<ParameterBean> getAll() {
		List<ParameterBean> l = new ArrayList<>(values().length);
		for (OfflineACO2ParametersBean b : values()) {
			l.add(b.getParameter());
		}
		return l;
	}

	@Override
	public ParameterBean getParameter() {
		ParameterBean p = new ParameterBean(name, ParameterType.DOUBLE);
		p.setValue(value);
		p.setSQLID(getSQLID());
		return p;
	}
}
