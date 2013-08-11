package org.com.dao.scheduling;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import org.com.DbMgr;
import org.com.model.scheduling.LinearParametersBean;
import org.scheduling.LinearMissionScheduler;

public class LinearParametersDAO
		extends
		AbstractSchedulingParameterDAO<LinearMissionScheduler, LinearParametersBean> {

	private static LinearParametersDAO instance;

	private LinearParametersDAO(Integer simID) {
		super();
		this.simID = simID;
	}

	public static LinearParametersDAO getInstance(Integer simID) {
		if (instance == null) {
			instance = new LinearParametersDAO(simID);
		}
		return instance;
	}

	@Override
	public void load() throws SQLException {
		if (psLoad == null) {
			psLoad = DbMgr.getInstance().getConnection()
					.prepareStatement(LOAD_QUERY);
		}
		beans = new ArrayList<>(1);

		psLoad.setInt(1, simID);
		ResultSet rs = psLoad.executeQuery();

		while (rs.next()) {
			LinearParametersBean parameter = LinearParametersBean
					.get(rs.getString("NAME"));
			parameter.setValue(Double.parseDouble(rs.getString("VALUE")));
			parameter.setSQLID(rs.getInt("ID") == 0 ? null : rs.getInt("ID"));
		}

		if (rs != null) {
			rs.close();
		}
	}

}
