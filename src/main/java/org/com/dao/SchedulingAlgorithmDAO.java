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

public class SchedulingAlgorithmDAO implements D2ctsDao<SchedulingAlgorithmBean> {
	private static final Logger log = Logger.getLogger(SchedulingAlgorithmDAO.class);

	private static Map<Integer, SchedulingAlgorithmDAO> instances;

	private static final String LOAD_QUERY = "SELECT sa.ID, sa.NAME, sa.CLASS FROM SCHEDULING_ALGORITHM sa INNER JOIN SIMULATION s ON sa.ID = s.SCHEDULING_ALGORITHM AND s.ID = ?";
	private static final String LOAD_QUERY_ALL = "SELECT sa.ID, sa.NAME, sa.CLASS FROM SCHEDULING_ALGORITHM sa";

	private PreparedStatement psLoad;
	private Integer simID;
	private Map<Integer, SchedulingAlgorithmBean> beans;

	private SchedulingAlgorithmDAO(Integer simID) {
		this.simID = simID;
		try {
			load();
		} catch (SQLException e) {
			log.fatal(e.getMessage(), e);
		}
	}

	public static SchedulingAlgorithmDAO getInstance(Integer simID) {
		if (instances == null) {
			instances = new HashMap<>();
		}
		if(!instances.containsKey(simID)){
			instances.put(simID, new SchedulingAlgorithmDAO(simID));
		}
		
		return instances.get(simID);
	}

	public static Iterator<SchedulingAlgorithmDAO> getInstances(){
		if(instances == null){
			instances = new HashMap<>();
		}
		return instances.values().iterator();
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
		//instances.remove(simID);
		instances = null;
		log.info("SchedulingAlgorithmDAO of sim "+simID+" closed.");
	}

	@Override
	public void load() throws SQLException {
		if (psLoad == null) {
			if(simID == null){
				psLoad = DbMgr.getInstance().getConnection().prepareStatement(LOAD_QUERY_ALL);
			} else {
				psLoad = DbMgr.getInstance().getConnection().prepareStatement(LOAD_QUERY);
				psLoad.setInt(1, simID);
			}
			
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
