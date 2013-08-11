package org.com.dao.scheduling;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import org.com.DbMgr;
import org.com.model.scheduling.RandomParametersBean;
import org.scheduling.random.RandomMissionScheduler;

public class RandomParametersDAO
		extends
		AbstractSchedulingParameterDAO<RandomMissionScheduler, RandomParametersBean> {
	private static RandomParametersDAO instance;

	private RandomParametersDAO(Integer simID) {
		super();
		this.simID = simID;
	}

	public static RandomParametersDAO getInstance(Integer simID) {
		if (instance == null) {
			instance = new RandomParametersDAO(simID);
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
			RandomParametersBean parameter = RandomParametersBean
					.get(rs.getString("NAME"));
			parameter.setValue(Double.parseDouble(rs.getString("VALUE")));
			parameter.setSQLID(rs.getInt("ID") == 0 ? null : rs.getInt("ID"));
		}

		if (rs != null) {
			rs.close();
		}
	}
}
