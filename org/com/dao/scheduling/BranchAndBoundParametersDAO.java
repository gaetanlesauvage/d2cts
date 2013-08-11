package org.com.dao.scheduling;

import java.sql.SQLException;

import org.com.model.scheduling.BranchAndBoundParametersBean;
import org.scheduling.branchAndBound.BranchAndBound;

public class BranchAndBoundParametersDAO
		extends
		AbstractSchedulingParameterDAO<BranchAndBound, BranchAndBoundParametersBean> {
	private static BranchAndBoundParametersDAO instance;

	private BranchAndBoundParametersDAO(Integer simID) {
		super();
		this.simID = simID;
	}

	public static BranchAndBoundParametersDAO getInstance(
			Integer simID) {
		if (instance == null) {
			instance = new BranchAndBoundParametersDAO(simID);
		}
		return instance;
	}

	@Override
	public void load() throws SQLException {

	}
}
