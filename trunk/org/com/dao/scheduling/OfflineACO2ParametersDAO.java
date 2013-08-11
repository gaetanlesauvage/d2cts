package org.com.dao.scheduling;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import org.com.DbMgr;
import org.com.model.scheduling.OfflineACO2ParametersBean;
import org.scheduling.offlineACO2.OfflineACOScheduler2;

public class OfflineACO2ParametersDAO
		extends
		AbstractSchedulingParameterDAO<OfflineACOScheduler2, OfflineACO2ParametersBean> {
	private static OfflineACO2ParametersDAO instance;

	private OfflineACO2ParametersDAO(Integer simID) {
		super();
		this.simID = simID;
	}

	public static OfflineACO2ParametersDAO getInstance(Integer simID) {
		if (instance == null) {
			instance = new OfflineACO2ParametersDAO(simID);
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
			OfflineACO2ParametersBean parameter = OfflineACO2ParametersBean
					.get(rs.getString("NAME"));
			parameter.setValue(Double.parseDouble(rs.getString("VALUE")));
			parameter.setSQLID(rs.getInt("ID") == 0 ? null : rs.getInt("ID"));
		}

		if (rs != null) {
			rs.close();
		}
	}
}
