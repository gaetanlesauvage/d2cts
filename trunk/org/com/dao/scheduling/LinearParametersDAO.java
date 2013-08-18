package org.com.dao.scheduling;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.com.DbMgr;
import org.com.model.scheduling.LinearParametersBean;
import org.scheduling.LinearMissionScheduler;

public class LinearParametersDAO
		extends
		AbstractSchedulingParameterDAO<LinearMissionScheduler, LinearParametersBean> {

	private static LinearParametersDAO instance;
	private boolean loaded;

	private LinearParametersDAO(Integer simID) {
		super();
		this.simID = simID;
		this.loaded = false;
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
			psLoad = DbMgr.getInstance().getConnection().prepareStatement(LOAD_QUERY);
		}
		beans = new ArrayList<>();

		psLoad.setInt(1, simID);
		ResultSet rs = psLoad.executeQuery();

		while (rs.next()) {
			LinearParametersBean parameter = LinearParametersBean.get(rs.getString("NAME"));
			parameter.setValue(rs.getString("VALUE"));
			parameter.setSQLID(rs.getInt("ID") == 0 ? null : rs.getInt("ID"));
			beans.add(parameter);
		}

		if (rs != null) {
			rs.close();
		}
		
		loaded = true;
	}
	
	@Override
	public List<LinearParametersBean> get() throws SQLException {
		if (!loaded) {
			load();
		}
		return LinearParametersBean.getAll();
	}
}
