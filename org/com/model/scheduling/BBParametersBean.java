package org.com.model.scheduling;

import java.util.ArrayList;
import java.util.List;

import org.scheduling.MissionSchedulerEvalParameters;

public enum BBParametersBean implements SchedulingParametersBeanInterface {
	E("E", ParameterType.DOUBLE), L("L", ParameterType.DOUBLE), T("T", ParameterType.DOUBLE), SOLUTION_FILE("SolutionFile", ParameterType.STRING), SOLUTION_INIT_FILE(
			"SolutionInitFile", ParameterType.STRING);

	private String name;
	private Object value;
	private ParameterType type;

	private Integer sqlId;

	private BBParametersBean(String name, ParameterType type) {
		this.name = name;
		this.value = null;
		this.type = type;
	}

	@Override
	public void setValue(Object o) {
		if (o instanceof String && type != ParameterType.STRING) {
			switch (type) {
			case DOUBLE:
				this.value = Double.parseDouble((String) o);
				break;
			case INTEGER:
				this.value = Integer.parseInt((String) o);
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
		if (type == ParameterType.DOUBLE)
			return (Double) this.value;
		else
			return null;
	}

	@Override
	public Integer getValueAsInteger() {
		if (type == ParameterType.INTEGER)
			return (Integer) this.value;
		else if (type == ParameterType.DOUBLE)
			return ((Double) this.value).intValue();
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

	public static MissionSchedulerEvalParameters getEvalParameters() {
		return new MissionSchedulerEvalParameters(T.getValueAsDouble(), L.getValueAsDouble(), E.getValueAsDouble());
	}

	public static BBParametersBean get(String name) {
		for (BBParametersBean param : BBParametersBean.values()) {
			if (param.name().equals(name) || param.name.equalsIgnoreCase(name))
				return param;
		}
		return null;
	}

	public static List<ParameterBean> getAll() {
		List<ParameterBean> l = new ArrayList<>(BBParametersBean.values().length);
		for (BBParametersBean b : values()) {
			l.add(b.getParameter());
		}
		return l;
	}

	public static String[] names() {
		String[] t = new String[values().length];
		int i = 0;
		for (BBParametersBean p : values()) {
			t[i++] = p.name;
		}
		return t;
	}

	public static ParameterType[] types() {
		ParameterType[] t = new ParameterType[values().length];
		int i = 0;
		for (BBParametersBean p : values()) {
			t[i++] = p.type;
		}
		return t;
	}

	public ParameterType getType() {
		return type;
	}

	@Override
	public ParameterBean getParameter() {
		ParameterBean p = new ParameterBean(name, type);
		p.setValue(value);
		p.setSQLID(getSQLID());
		return p;
	}
}
