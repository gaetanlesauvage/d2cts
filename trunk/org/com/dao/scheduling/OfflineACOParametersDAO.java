package org.com.dao.scheduling;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.com.DbMgr;
import org.com.model.scheduling.OfflineACOParametersBean;
import org.scheduling.offlineACO.OfflineACOScheduler;

public class OfflineACOParametersDAO
		extends
		AbstractSchedulingParameterDAO<OfflineACOScheduler, OfflineACOParametersBean> {
	private static OfflineACOParametersDAO instance;
	private boolean loaded;

	private OfflineACOParametersDAO(Integer simID) {
		super();
		this.simID = simID;
		this.loaded = false;
	}

	public static OfflineACOParametersDAO getInstance(Integer simID) {
		if (instance == null) {
			instance = new OfflineACOParametersDAO(simID);
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
			OfflineACOParametersBean parameter = OfflineACOParametersBean.get(rs.getString("NAME"));
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
	public List<OfflineACOParametersBean> get() throws SQLException {
		if (!loaded) {
			load();
		}
		return OfflineACOParametersBean.getAll();
	}
}
