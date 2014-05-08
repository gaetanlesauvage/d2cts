package org.com.dao;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.log4j.Logger;
import org.com.DbMgr;
import org.com.model.RoadBean;
import org.com.model.RoadPointBean;

public class RoadDAO implements D2ctsDao<RoadBean>{
	private static final Logger log = Logger.getLogger(RoadDAO.class);

	private static Map<Integer, RoadDAO> instances;
	
	private Map<String, RoadBean> beans;
	private Integer terminalID;
	
	private static final String LOAD_QUERY = "SELECT NAME, TERMINAL, TYPE, ORIGIN, DESTINATION, DIRECTED, BLOCK, BAY_GROUP FROM ROAD WHERE TERMINAL = ?";
	private static final String ROAD_POINTS_QUERY = "SELECT NAME, TERMINAL, ROAD, INDEX_IN_ROAD, X, Y, Z FROM ROADPOINTS WHERE TERMINAL = ? AND ROAD = ?";
	
	private PreparedStatement psLoad;
	private PreparedStatement psLoadRoadPoints;
	
	public static RoadDAO getInstance(Integer terminalID){
		if(instances == null){
			instances = new HashMap<>();
			
		}
		if(instances.containsKey(terminalID)) return instances.get(terminalID);
		else {
			RoadDAO instance = new RoadDAO(terminalID);
			instances.put(terminalID, instance);
			return instance;
		}
	}

	public static Iterator<RoadDAO> getInstances(){
		if(instances == null){
			instances = new HashMap<>();
		}
		return instances.values().iterator();
	}
	
	private RoadDAO(Integer terminalID){
		this.terminalID = terminalID;
	}
	
	@Override
	public Iterator<RoadBean> iterator() {
		if(beans == null) beans = new HashMap<>();
		return beans.values().iterator();
	}

	@Override
	public void close() throws SQLException {
		if(psLoad != null){
			psLoad.close();
		}
		if(psLoadRoadPoints != null){
			psLoadRoadPoints.close();
		}
		instances = null;
		log.info("RoadDAO of terminal "+terminalID+" closed.");
	}

	@Override
	public void load() throws SQLException {
		if(psLoad == null){
			psLoad = DbMgr.getInstance().getConnection().prepareStatement(LOAD_QUERY);
		}
		if(psLoadRoadPoints == null){
			psLoadRoadPoints = DbMgr.getInstance().getConnection().prepareStatement(ROAD_POINTS_QUERY);
		}
		
		beans = new HashMap<>();
		
		psLoad.setInt(1, terminalID);
		ResultSet rs = psLoad.executeQuery();
		while(rs.next()){
			RoadBean bean = new RoadBean();
			bean.setName(rs.getString("NAME"));
			bean.setTerminal(rs.getInt("TERMINAL"));
			bean.setType(rs.getInt("TYPE"));
			bean.setOrigin(rs.getString("ORIGIN"));
			bean.setDestination(rs.getString("DESTINATION"));
			bean.setDirected(rs.getBoolean("DIRECTED"));
			bean.setBlock(rs.getString("BLOCK"));
			bean.setGroup(rs.getString("BAY_GROUP"));
			psLoadRoadPoints.setInt(1, terminalID);
			psLoadRoadPoints.setString(2, bean.getName());
			ResultSet rsRoadPoints = psLoadRoadPoints.executeQuery();
			while(rsRoadPoints.next()){
				RoadPointBean rpBean = new RoadPointBean();
				rpBean.setName(rsRoadPoints.getString("NAME"));
				rpBean.setIndexInRoad(rsRoadPoints.getInt("INDEX_IN_ROAD"));
				rpBean.setX(rsRoadPoints.getDouble("X"));
				rpBean.setY(rsRoadPoints.getDouble("Y"));
				rpBean.setZ(rsRoadPoints.getDouble("Z"));
				bean.addRoadPoint(rpBean);
			}
			if(rsRoadPoints != null)
				rsRoadPoints.close();
			
			beans.put(bean.getName(), bean);
		}
		
		
		
		if(rs!=null){
			rs.close();
		}		
	}

	@Override
	public int insert(RoadBean bean) throws SQLException {
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
