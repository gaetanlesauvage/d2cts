package org.com.dao.scheduling;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.com.DbMgr;
import org.com.model.scheduling.LinearParametersBean;
import org.com.model.scheduling.ParameterBean;
import org.scheduling.LinearMissionScheduler;

public class LinearParametersDAO extends AbstractSchedulingParameterDAO<LinearMissionScheduler> {

	private static Map<Integer, LinearParametersDAO> instances;

	private LinearParametersDAO(Integer simID) {
		super(simID);
	}

	public static LinearParametersDAO getInstance(Integer simID) {
		if (instances == null)
			instances = new HashMap<>();

		if (instances.containsKey(simID))
			return instances.get(simID);
		else {
			LinearParametersDAO instance = new LinearParametersDAO(simID);
			instances.put(simID, instance);
			return instance;
		}
	}

	public static Iterator<LinearParametersDAO> getInstances() {
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
		beans = new ArrayList<>();

		psLoad.setString(1, LinearMissionScheduler.rmiBindingName);
		psLoad.setInt(2, simID);

		ResultSet rs = psLoad.executeQuery();

		while (rs.next()) {
			LinearParametersBean parameter = LinearParametersBean.get(rs.getString("NAME"));
			ParameterBean p = new ParameterBean(parameter.name(), parameter.getType());
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
