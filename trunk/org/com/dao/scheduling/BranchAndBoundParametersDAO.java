package org.com.dao.scheduling;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.com.DbMgr;
import org.com.model.scheduling.BranchAndBoundParametersBean;
import org.scheduling.branchAndBound.BranchAndBound;

public class BranchAndBoundParametersDAO extends AbstractSchedulingParameterDAO<BranchAndBound, BranchAndBoundParametersBean> {
	private static BranchAndBoundParametersDAO instance;
	private boolean loaded;

	private BranchAndBoundParametersDAO(Integer simID) {
		super();
		this.simID = simID;
		loaded = false;
	}

	public static BranchAndBoundParametersDAO getInstance(Integer simID) {
		if (instance == null) {
			instance = new BranchAndBoundParametersDAO(simID);
		}
		return instance;
	}

	@Override
	public void load() throws SQLException {
		if (psLoad == null) {
			psLoad = DbMgr.getInstance().getConnection().prepareStatement(LOAD_QUERY);
		}
		psLoad.setInt(1, simID);
		ResultSet rs = psLoad.executeQuery();
		beans = new ArrayList<>();
		while (rs.next()) {
			BranchAndBoundParametersBean param = BranchAndBoundParametersBean.get(rs.getString("NAME"));
			param.setValue(rs.getString("VALUE"));
			param.setSQLID(rs.getInt("ID") == 0 ? null : rs.getInt("ID"));
			beans.add(param);
		}

		if (rs != null) {
			rs.close();
		}
		loaded = true;
	}

	@Override
	public List<BranchAndBoundParametersBean> get() throws SQLException {
		if (!loaded) {
			load();
		}

		return BranchAndBoundParametersBean.getAll();
	}
}
