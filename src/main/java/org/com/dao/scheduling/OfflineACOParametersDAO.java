package org.com.dao.scheduling;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.com.DbMgr;
import org.com.model.scheduling.OfflineACOParametersBean;
import org.com.model.scheduling.ParameterBean;
import org.scheduling.offlineACO.OfflineACOScheduler;

public class OfflineACOParametersDAO extends AbstractSchedulingParameterDAO<OfflineACOScheduler> {
	private static Map<Integer, OfflineACOParametersDAO> instances;

	private OfflineACOParametersDAO(Integer simID) {
		super(simID);
	}

	public static OfflineACOParametersDAO getInstance(Integer simID) {
		if(instances == null)
			instances = new HashMap<>();
		
		if (instances.containsKey(simID))
			return instances.get(simID);
		else {
			OfflineACOParametersDAO instance = new OfflineACOParametersDAO(simID);
			instances.put(simID, instance);
			return instance;
		}
	}
	
	public static Iterator<OfflineACOParametersDAO> getInstances(){
		if(instances == null){
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

		psLoad.setString(1, OfflineACOScheduler.rmiBindingName);
		psLoad.setInt(2, simID);
		
		ResultSet rs = psLoad.executeQuery();

		while (rs.next()) {
			OfflineACOParametersBean parameter = OfflineACOParametersBean.get(rs.getString("NAME"));
			ParameterBean p = new ParameterBean(parameter.name(), parameter.getType());
			p.setValue(rs.getString("VALUE"));
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
