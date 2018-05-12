package org.com.dao.scheduling;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.com.DbMgr;
import org.com.model.scheduling.BBParametersBean;
import org.com.model.scheduling.ParameterBean;
import org.scheduling.bb.BB;

public class BBParametersDAO extends AbstractSchedulingParameterDAO<BB> {
	private static Map<Integer, BBParametersDAO> instances;

	private BBParametersDAO(Integer simID) {
		super(simID);
	}

	public static BBParametersDAO getInstance(Integer simID) {
		if(instances == null)
			instances = new HashMap<>();
		
		if (instances.containsKey(simID))
			return instances.get(simID);
		else {
			BBParametersDAO instance = new BBParametersDAO(simID);
			instances.put(simID, instance);
			return instance;
		}
	}
	
	public static Iterator<BBParametersDAO> getInstances(){
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
		psLoad.setString(1, BB.rmiBindingName);
		psLoad.setInt(2, simID);

		ResultSet rs = psLoad.executeQuery();

		beans = new ArrayList<>();

		while (rs.next()) {
			String nameParam = rs.getString("NAME");
			String value = rs.getString("VALUE");

			BBParametersBean param = BBParametersBean.get(nameParam);
			ParameterBean p = new ParameterBean(param.name(), param.getType());
			p.setValue(value);
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
