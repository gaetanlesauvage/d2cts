package org.com.dao.scheduling;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.com.DbMgr;
import org.com.model.scheduling.GreedyParametersBean;
import org.scheduling.greedy.GreedyMissionScheduler;

public class GreedyParametersDAO extends AbstractSchedulingParameterDAO<GreedyMissionScheduler, GreedyParametersBean> {
	private static GreedyParametersDAO instance;
	private boolean loaded;

	private GreedyParametersDAO(Integer simID) {
		super();
		this.simID = simID;
		loaded = false;
	}

	public static GreedyParametersDAO getInstance(Integer simID) {
		if (instance == null) {
			instance = new GreedyParametersDAO(simID);
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
			GreedyParametersBean parameter = GreedyParametersBean.get(rs.getString("NAME"));
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
	public List<GreedyParametersBean> get() throws SQLException {
		if (!loaded) {
			load();
		}

		return GreedyParametersBean.getAll();
	}
}
