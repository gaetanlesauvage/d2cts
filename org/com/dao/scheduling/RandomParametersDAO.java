package org.com.dao.scheduling;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.com.DbMgr;
import org.com.model.scheduling.ParameterBean;
import org.com.model.scheduling.ParameterType;
import org.com.model.scheduling.RandomParametersBean;
import org.scheduling.random.RandomMissionScheduler;

public class RandomParametersDAO extends AbstractSchedulingParameterDAO<RandomMissionScheduler> {
	private static Map<Integer, RandomParametersDAO> instances;

	private RandomParametersDAO(Integer simID) {
		super(simID);
	}

	public static RandomParametersDAO getInstance(Integer simID) {
		if (instances == null)
			instances = new HashMap<>();

		if (instances.containsKey(simID))
			return instances.get(simID);
		else {
			RandomParametersDAO instance = new RandomParametersDAO(simID);
			instances.put(simID, instance);
			return instance;
		}
	}

	public static Iterator<RandomParametersDAO> getInstances() {
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

		psLoad.setString(1, RandomMissionScheduler.rmiBindingName);
		psLoad.setInt(2, simID);

		ResultSet rs = psLoad.executeQuery();

		while (rs.next()) {
			RandomParametersBean parameter = RandomParametersBean.get(rs.getString("NAME"));
			ParameterBean p = new ParameterBean(parameter.name(),ParameterType.DOUBLE);
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

	/*@Override
	public List<RandomParametersBean> get() throws SQLException {
		if (!loaded) {
			load();
		}
		return RandomParametersBean.getAll();
	}*/
}
