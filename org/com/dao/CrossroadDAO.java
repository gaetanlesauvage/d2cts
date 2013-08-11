package org.com.dao;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.log4j.Logger;
import org.com.DbMgr;
import org.com.model.CrossroadBean;

public class CrossroadDAO implements D2ctsDao<CrossroadBean>{
	private static final Logger log = Logger.getLogger(CrossroadDAO.class);
	
	private static Map<Integer, CrossroadDAO> instances;
	
	private static final String LOAD_QUERY = "SELECT NAME, TERMINAL, TYPE, X, Y, Z, ROAD, BLOCK FROM CROSSROAD WHERE TERMINAL = ?";
	
	private PreparedStatement psLoad;
	
	private Map<String, CrossroadBean> beans;
	private Integer terminalID;
	
	private CrossroadDAO(Integer terminalID){
		this.terminalID = terminalID;
	}
	
	public static CrossroadDAO getInstance(Integer terminalID){
		if(instances == null){
			instances = new HashMap<>();
			
		}
		if(instances.containsKey(terminalID)) return instances.get(terminalID);
		else {
			CrossroadDAO instance = new CrossroadDAO(terminalID);
			instances.put(terminalID, instance);
			return instance;
		}
	}

	public static Iterator<CrossroadDAO> getInstances(){
		if(instances == null){
			instances = new HashMap<>();
		}
		return instances.values().iterator();
	}
	
	@Override
	public Iterator<CrossroadBean> iterator() {
		if(beans == null) beans = new HashMap<>();
		return beans.values().iterator();
	}

	@Override
	public void close() throws SQLException {
		if(psLoad != null){
			psLoad.close();
		}
		log.info("CrossroadDAO of terminal "+terminalID+" closed.");
	}

	@Override
	public void load() throws SQLException {
		if(psLoad == null){
			psLoad = DbMgr.getInstance().getConnection().prepareStatement(LOAD_QUERY);
		}
		beans = new HashMap<>();
		
		psLoad.setInt(1, terminalID);
		ResultSet rs = psLoad.executeQuery();
		while(rs.next()){
			CrossroadBean bean = new CrossroadBean();

			bean.setName(rs.getString("NAME"));
			bean.setTerminal(rs.getInt("TERMINAL"));
			bean.setType(rs.getInt("TYPE"));
			bean.setX(rs.getDouble("X"));
			bean.setY(rs.getDouble("Y"));
			bean.setZ(rs.getDouble("Z"));
			bean.setBlock(rs.getString("BLOCK"));
			bean.setRoad(rs.getString("ROAD"));

			beans.put(bean.getName(), bean);
		}
		
		if(rs!=null){
			rs.close();
		}
	}

	@Override
	public int insert(CrossroadBean bean) throws SQLException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public String getLoadQuery() {
		return LOAD_QUERY;
	}
	
	@Override
	public int size(){
		return beans.size();
	}
}
