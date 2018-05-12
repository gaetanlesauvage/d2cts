package org.com.dao.scheduling;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.com.DbMgr;
import org.com.model.scheduling.OfflineACO2ParametersBean;
import org.com.model.scheduling.ParameterBean;
import org.scheduling.offlineACO2.OfflineACOScheduler2;

public class OfflineACO2ParametersDAO extends AbstractSchedulingParameterDAO<OfflineACOScheduler2> {
	private static Map<Integer, OfflineACO2ParametersDAO> instances;

	private OfflineACO2ParametersDAO(Integer simID) {
		super(simID);
	}

	public static OfflineACO2ParametersDAO getInstance(Integer simID) {
		if (instances == null)
			instances = new HashMap<>();

		if (instances.containsKey(simID))
			return instances.get(simID);
		else {
			OfflineACO2ParametersDAO instance = new OfflineACO2ParametersDAO(simID);
			instances.put(simID, instance);
			return instance;
		}
	}

	public static Iterator<OfflineACO2ParametersDAO> getInstances() {
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

		psLoad.setString(1, OfflineACOScheduler2.rmiBindingName);
		psLoad.setInt(2, simID);

		ResultSet rs = psLoad.executeQuery();

		while (rs.next()) {
			OfflineACO2ParametersBean parameter = OfflineACO2ParametersBean.get(rs.getString("NAME"));
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
