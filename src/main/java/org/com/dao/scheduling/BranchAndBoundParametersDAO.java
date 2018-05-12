package org.com.dao.scheduling;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.com.DbMgr;
import org.com.model.scheduling.BranchAndBoundParametersBean;
import org.com.model.scheduling.ParameterBean;
import org.scheduling.branchAndBound.BranchAndBound;

public class BranchAndBoundParametersDAO extends AbstractSchedulingParameterDAO<BranchAndBound> {
	private static Map<Integer, BranchAndBoundParametersDAO> instances;

	private BranchAndBoundParametersDAO(Integer simID) {
		super(simID);
	}

	public static BranchAndBoundParametersDAO getInstance(Integer simID) {
		if (instances == null)
			instances = new HashMap<>();

		if (instances.containsKey(simID))
			return instances.get(simID);
		else {
			BranchAndBoundParametersDAO instance = new BranchAndBoundParametersDAO(simID);
			instances.put(simID, instance);
			return instance;
		}
	}

	public static Iterator<BranchAndBoundParametersDAO> getInstances() {
		if (instances == null) {
			instances = new HashMap<>();
		}
		return instances.values().iterator();
	}

	@Override
	public void load() throws SQLException {
		if (psLoad == null) {
			psLoad = DbMgr.getInstance().getConnection().prepareStatement(LOAD_QUERY);
		}

		psLoad.setString(1, BranchAndBound.rmiBindingName);
		psLoad.setInt(2, simID);

		ResultSet rs = psLoad.executeQuery();
		beans = new ArrayList<>();
		while (rs.next()) {
			BranchAndBoundParametersBean param = BranchAndBoundParametersBean.get(rs.getString("NAME"));
			ParameterBean p = new ParameterBean(param.name(), param.getType());
			p.setValue(rs.getString("VALUE"));
			p.setSQLID(rs.getInt("ID") == 0 ? null : rs.getInt("ID"));
			beans.add(p);
		}

		if (rs != null) {
			rs.close();
		}
		loaded = true;
	}

	public static void closeInstance() {
		instances = null;
	}

}
