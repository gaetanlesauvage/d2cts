package org.com.dao;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.log4j.Logger;
import org.com.DbMgr;
import org.com.model.SchedulingAlgorithmBean;

public class SchedulingAlgorithmDAO implements
		D2ctsDao<SchedulingAlgorithmBean> {
	private static final Logger log = Logger
			.getLogger(SchedulingAlgorithmDAO.class);

	private static SchedulingAlgorithmDAO instance;

	private static final String LOAD_QUERY = "SELECT ID, NAME, CLASS FROM SCHEDULING_ALGORITHM";

	private PreparedStatement psLoad;

	private Map<Integer, SchedulingAlgorithmBean> beans;

	private SchedulingAlgorithmDAO() {
		try{
			load();
		} catch (SQLException e){
			log.fatal(e.getMessage(),e);
		}
	}

	public static SchedulingAlgorithmDAO getInstance() {
		if (instance == null) {
			instance = new SchedulingAlgorithmDAO();

		}
		return instance;
	}

	@Override
	public Iterator<SchedulingAlgorithmBean> iterator() {
		if (beans == null)
			beans = new HashMap<>();
		return beans.values().iterator();
	}

	@Override
	public void close() throws SQLException {
		if (psLoad != null) {
			psLoad.close();
		}
		log.info("SchedulingAlgorithmDAO closed.");
	}

	@Override
	public void load() throws SQLException {
		if (psLoad == null) {
			psLoad = DbMgr.getInstance().getConnection()
					.prepareStatement(LOAD_QUERY);
		}
		beans = new HashMap<>();

		ResultSet rs = psLoad.executeQuery();
		while (rs.next()) {
			SchedulingAlgorithmBean bean = new SchedulingAlgorithmBean();

			bean.setId(rs.getInt("ID"));
			bean.setName(rs.getString("NAME"));
			bean.setJavaClass(rs.getString("CLASS"));
			
			beans.put(bean.getId(), bean);
		}

		if (rs != null) {
			rs.close();
		}
	}

	@Override
	public int insert(SchedulingAlgorithmBean bean) throws SQLException {
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

	public SchedulingAlgorithmBean get(Integer schedulingAlgorithmID) {
		return beans.get(schedulingAlgorithmID);
	}
}
