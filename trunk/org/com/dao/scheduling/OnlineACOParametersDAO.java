package org.com.dao.scheduling;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.com.DbMgr;
import org.com.model.scheduling.OnlineACOParametersBean;
import org.scheduling.onlineACO.OnlineACOScheduler;

public class OnlineACOParametersDAO extends AbstractSchedulingParameterDAO<OnlineACOScheduler, OnlineACOParametersBean> {

	private static OnlineACOParametersDAO instance;
	private boolean loaded;

	private OnlineACOParametersDAO(Integer simID) {
		super();
		this.simID = simID;
		this.loaded = false;
	}

	public static OnlineACOParametersDAO getInstance(Integer simID) {
		if (instance == null) {
			instance = new OnlineACOParametersDAO(simID);
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
			OnlineACOParametersBean parameter = OnlineACOParametersBean.get(rs.getString("NAME"));
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
	public List<OnlineACOParametersBean> get() throws SQLException {
		if (!loaded) {
			load();
		}
		return OnlineACOParametersBean.getAll();
	}
}
