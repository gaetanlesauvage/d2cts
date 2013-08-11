package org.com.dao.scheduling;

import java.sql.SQLException;

import org.com.model.scheduling.BBParametersBean;
import org.scheduling.bb.BB;

public class BBParametersDAO extends
		AbstractSchedulingParameterDAO<BB, BBParametersBean> {
	private static BBParametersDAO instance;

	private BBParametersDAO(Integer simID) {
		super();
		this.simID = simID;
	}

	public static BBParametersDAO getInstance(Integer simID) {
		if (instance == null) {
			instance = new BBParametersDAO(simID);
		}
		return instance;
	}

	@Override
	public void load() throws SQLException {
	}
}
