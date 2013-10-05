package org.com.model;

import java.util.HashMap;
import java.util.Map;

import org.com.model.scheduling.ParameterBean;
import org.scheduling.MissionSchedulerEvalParameters;

public class SchedulingAlgorithmBean {
	private Integer id;
	private String name;
	private String _class;
	private Map<String, ParameterBean> parameters;
	private MissionSchedulerEvalParameters evalParameter;

	public SchedulingAlgorithmBean() {
		evalParameter = new MissionSchedulerEvalParameters(Double.NaN, Double.NaN, Double.NaN);
	}

	public void setParameters(ParameterBean[] parameters) {
		this.parameters = new HashMap<>(parameters.length);
		for (ParameterBean p : parameters) {
			if (p.name().equals("T"))
				evalParameter.setT(p.getValueAsDouble());
			else if (p.name().equals("L"))
				evalParameter.setL(p.getValueAsDouble());
			else if (p.name().equals("E"))
				evalParameter.setE(p.getValueAsDouble());
			else
				this.parameters.put(p.name(), p);
		}

	}

	public Map<String, ParameterBean> getParameters() {
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
		if (o == null)
			return false;
		return hashCode() == o.hashCode();
	}

	@Override
	public String toString() {
		return name;
	}

	public MissionSchedulerEvalParameters getEvalParameters() {
		return evalParameter;
	}

}
