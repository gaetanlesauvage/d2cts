package org.com.dao.scheduling;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.com.DbMgr;
import org.com.model.scheduling.GreedyParametersBean;
import org.com.model.scheduling.ParameterBean;
import org.scheduling.greedy.GreedyMissionScheduler;

public class GreedyParametersDAO extends AbstractSchedulingParameterDAO<GreedyMissionScheduler> {
	private static Map<Integer, GreedyParametersDAO> instances;
	
	private GreedyParametersDAO(Integer simID) {
		super(simID);
	}

	public static GreedyParametersDAO getInstance(Integer simID) {
		if(instances == null)
			instances = new HashMap<>();
		
		if (instances.containsKey(simID))
			return instances.get(simID);
		else {
			GreedyParametersDAO instance = new GreedyParametersDAO(simID);
			instances.put(simID, instance);
			return instance;
		}
	}
	
	public static Iterator<GreedyParametersDAO> getInstances(){
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

		psLoad.setString(1, GreedyMissionScheduler.rmiBindingName);
		psLoad.setInt(2, simID);
		
		ResultSet rs = psLoad.executeQuery();

		while (rs.next()) {
			GreedyParametersBean parameter = GreedyParametersBean.get(rs.getString("NAME"));
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
