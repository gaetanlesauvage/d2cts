package org.com.dao.scheduling;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.com.DbMgr;
import org.com.model.scheduling.OnlineACOParametersBean;
import org.com.model.scheduling.ParameterBean;
import org.scheduling.onlineACO.OnlineACOScheduler;

public class OnlineACOParametersDAO extends AbstractSchedulingParameterDAO<OnlineACOScheduler> {
	private static Map<Integer, OnlineACOParametersDAO> instances;

	private OnlineACOParametersDAO(Integer simID) {
		super(simID);
	}

	public static OnlineACOParametersDAO getInstance(Integer simID) {
		if (instances == null)
			instances = new HashMap<>();

		if (instances.containsKey(simID))
			return instances.get(simID);
		else {
			OnlineACOParametersDAO instance = new OnlineACOParametersDAO(simID);
			instances.put(simID, instance);
			return instance;
		}
	}

	public static Iterator<OnlineACOParametersDAO> getInstances() {
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

		psLoad.setString(1, OnlineACOScheduler.rmiBindingName);
		psLoad.setInt(2, simID);

		ResultSet rs = psLoad.executeQuery();

		while (rs.next()) {
			OnlineACOParametersBean parameter = OnlineACOParametersBean.get(rs.getString("NAME"));
			ParameterBean p = new ParameterBean(parameter.getName(),parameter.getType());
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
