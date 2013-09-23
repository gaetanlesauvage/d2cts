package org.com.dao.scheduling;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.log4j.Logger;
import org.com.DbMgr;
import org.com.dao.D2ctsDao;
import org.com.model.scheduling.DefaultParametersBean;

public class DefaultParametersDAO implements D2ctsDao<DefaultParametersBean> {
	private static final Logger log = Logger.getLogger(DefaultParametersDAO.class);
	
	private static DefaultParametersDAO instance;

	private static final String LOAD_QUERY = "SELECT PARAM_NAME, VALUE FROM DEFAULT_SCHEDULING_PARAMETERS";
	
	private PreparedStatement psLoad;
	
	private Map<String, DefaultParametersBean> beans;
	
	
	private DefaultParametersDAO() {
		super();
		try{
			load();
		} catch (SQLException e){
			log.error(e.getMessage(), e);
		}
	}

	public static DefaultParametersDAO getInstance() {
		if (instance == null) {
			instance = new DefaultParametersDAO();
		}
		return instance;
	}

	@Override
	public Iterator<DefaultParametersBean> iterator() {
		if (beans == null)
			beans = new HashMap<>();
		return beans.values().iterator();
	}

	@Override
	public void close() throws SQLException {
		if (psLoad != null) {
			psLoad.close();
		}
		log.info("DefaultSchedulingParametersDAO closed.");
	}

	@Override
	public void load() throws SQLException {
		if (psLoad == null) {
			psLoad = DbMgr.getInstance().getConnection().prepareStatement(LOAD_QUERY);
		}
		beans = new HashMap<>();
		ResultSet rs = psLoad.executeQuery();
		while (rs.next()) {
			DefaultParametersBean bean = new DefaultParametersBean();
			
			bean.setName(rs.getString("PARAM_NAME").toUpperCase());
			bean.setValue(rs.getString("VALUE"));
			beans.put(bean.getName(), bean);
		}
		if (rs != null) {
			rs.close();
		}
	}

	@Override
	public int insert(DefaultParametersBean bean) throws SQLException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public String getLoadQuery() {
		return LOAD_QUERY;
	}

	@Override
	public int size() {
		return beans.size();
	}

	public DefaultParametersBean get(String name) {
		return beans.get(name);
	}

	public static void closeInstance() {
		instance = null;
	}
}
