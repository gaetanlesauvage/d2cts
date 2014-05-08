package org.com.dao;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.log4j.Logger;
import org.com.DbMgr;
import org.com.model.LaserHeadBean;

public class LaserHeadDAO implements D2ctsDao<LaserHeadBean>{
	private static final Logger log = Logger.getLogger(LaserHeadDAO.class);

	private static Map<Integer, LaserHeadDAO> instances;

	private static final String LOAD_QUERY = "SELECT NAME, SCENARIO, X, Y, Z, RX, RY, RZ FROM LASERHEAD WHERE SCENARIO = ?";
	private static final String INSERT_QUERY = "INSERT INTO LASERHEAD (NAME, SCENARIO, X, Y, Z, RX, RY, RZ) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

	private PreparedStatement psLoad;
	private PreparedStatement psInsert;

	private Map<String, LaserHeadBean> beans;
	private Integer scenarioID;

	private LaserHeadDAO(Integer scenarioID){
		this.scenarioID = scenarioID;
	}

	public static LaserHeadDAO getInstance(Integer scenarioID){
		if(instances == null){
			instances = new HashMap<>();

		}
		if(instances.containsKey(scenarioID)) return instances.get(scenarioID);
		else {
			LaserHeadDAO instance = new LaserHeadDAO(scenarioID);
			instances.put(scenarioID, instance);
			return instance;
		}
	}

	public static Iterator<LaserHeadDAO> getInstances(){
		if(instances == null){
			instances = new HashMap<>();
		}
		return instances.values().iterator();
	}

	@Override
	public Iterator<LaserHeadBean> iterator() {
		if(beans == null) beans = new HashMap<>();
		return beans.values().iterator();
	}

	@Override
	public void close() throws SQLException {
		if(psLoad != null){
			psLoad.close();
		}
		if(psInsert != null){
			psInsert.close();
		}
		instances = null;
		log.info("LaserHeadDAO of terminal "+scenarioID+" closed.");
	}

	@Override
	public void load() throws SQLException {
		if(psLoad == null){
			psLoad = DbMgr.getInstance().getConnection().prepareStatement(LOAD_QUERY);
		}
		beans = new HashMap<>();

		psLoad.setInt(1, scenarioID);
		ResultSet rs = psLoad.executeQuery();
		while(rs.next()){
			LaserHeadBean bean = new LaserHeadBean();
			bean.setName(rs.getString("NAME"));
			bean.setScenario(rs.getInt("SCENARIO"));
			bean.setX(rs.getDouble("X"));
			bean.setY(rs.getDouble("Y"));
			bean.setZ(rs.getDouble("Z"));
			bean.setRx(rs.getDouble("RX"));
			bean.setRy(rs.getDouble("RY"));
			bean.setRz(rs.getDouble("RZ"));
			beans.put(bean.getName(), bean);
		}

		if(rs!=null){
			rs.close();
		}
	}

	@Override
	public int insert(LaserHeadBean bean) throws SQLException {
		if(psInsert == null){
			psInsert = DbMgr.getInstance().getConnection().prepareStatement(INSERT_QUERY);
		}
		psInsert.setString(1, bean.getName());
		psInsert.setInt(2, bean.getScenario());
		psInsert.setDouble(3, bean.getX());
		psInsert.setDouble(4, bean.getY());
		psInsert.setDouble(5, bean.getZ());
		psInsert.setDouble(6, bean.getRx());
		psInsert.setDouble(7, bean.getRy());
		psInsert.setDouble(8, bean.getRz());

		return psInsert.executeUpdate();
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
