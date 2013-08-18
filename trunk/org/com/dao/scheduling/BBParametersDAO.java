package org.com.dao.scheduling;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.com.DbMgr;
import org.com.model.scheduling.BBParametersBean;
import org.scheduling.bb.BB;

public class BBParametersDAO extends AbstractSchedulingParameterDAO<BB, BBParametersBean> {
	private static BBParametersDAO instance;

	private boolean loaded;

	private BBParametersDAO(Integer simID) {
		super();
		this.simID = simID;
		loaded = false;
	}

	public static BBParametersDAO getInstance(Integer simID) {
		if (instance == null) {
			instance = new BBParametersDAO(simID);
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
			// Integer idParam = rs.getInt("ID");
			String nameParam = rs.getString("NAME");

			String value = rs.getString("VALUE");

			BBParametersBean param = BBParametersBean.get(nameParam);
			param.setValue(value);
			param.setSQLID(rs.getInt("ID") == 0 ? null : rs.getInt("ID"));
			beans.add(param);
		}

		if (rs != null) {
			rs.close();
		}
		loaded = true;
	}

	@Override
	public List<BBParametersBean> get() throws SQLException {
		if (!loaded) {
			load();
		}

		return BBParametersBean.getAll();
	}

}
