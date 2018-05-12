package org.com.dao;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.log4j.Logger;
import org.com.DbMgr;
import org.com.model.StraddleCarrierLocationBean;

public class StraddleCarrierLocationDAO implements D2ctsDao<StraddleCarrierLocationBean> {
	private static final Logger log = Logger.getLogger(StraddleCarrierLocationDAO.class);

	private static Map<Integer, StraddleCarrierLocationDAO> instances;

	private static final String LOAD_QUERY = "SELECT STRADDLECARRIER_NAME, SIMULATION, T, ROAD, DIRECTION, X, Y, Z "
			+ "FROM STRADDLECARRIERS_LOCATION " + "WHERE SIMULATION = ?";

	private static final String SAVE_QUERY = "INSERT INTO STRADDLECARRIERS_LOCATION (STRADDLECARRIER_NAME, "
			+"SIMULATION, T, ROAD, DIRECTION, X, Y, Z) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

	private static final String UPDATE_QUERY = "UPDATE STRADDLECARRIERS_LOCATION SET ROAD = ?, DIRECTION = ?, X = ?, Y = ?, Z = ? "
			+ "WHERE STRADDLECARRIER_NAME = ? AND SIMULATION = ? AND T = ?";

	private static final int BUFFER_SIZE = 100;


	private PreparedStatement psLoad;
	private PreparedStatement psSave;
	private PreparedStatement psUpdate;

	private Map<String, TreeMap<Time, StraddleCarrierLocationBean>> beans;

	private Integer simID;
	private int batchSize;

	private StraddleCarrierLocationDAO(Integer simID) {
		this.simID = simID;
		this.batchSize = 0;
	}

	public static StraddleCarrierLocationDAO getInstance(Integer simID) {
		if (instances == null) {
			instances = new HashMap<>();

		}
		if (instances.containsKey(simID))
			return instances.get(simID);
		else {
			StraddleCarrierLocationDAO instance = new StraddleCarrierLocationDAO(simID);
			instances.put(simID, instance);
			return instance;
		}
	}

	public static Iterator<StraddleCarrierLocationDAO> getInstances() {
		if (instances == null) {
			instances = new HashMap<>();
		}
		return instances.values().iterator();
	}

	@Override
	public Iterator<StraddleCarrierLocationBean> iterator() {
		if (beans == null) {
			beans = new HashMap<>();
		}
		List<StraddleCarrierLocationBean> l = new ArrayList<>();
		for (TreeMap<Time, StraddleCarrierLocationBean> tm : beans.values()) {
			for (StraddleCarrierLocationBean b : tm.values()) {
				l.add(b);
			}
		}
		return l.iterator();
	}

	@Override
	public void close() throws SQLException {
		if (psLoad != null) {
			psLoad.close();
		}
		if(psSave != null) {
			psSave.executeBatch();
			psSave.close();
		}
		if(psUpdate != null){
			psUpdate.close();
		}
		instances = null;
		log.info("StraddleCarrierLocationDAO of simulation " + simID + " closed.");
	}

	@Override
	public void load() throws SQLException {
		if (psLoad == null) {
			psLoad = DbMgr.getInstance().getConnection().prepareStatement(LOAD_QUERY);
		}
		beans = new HashMap<>();

		psLoad.setInt(1, simID);
		ResultSet rs = psLoad.executeQuery();

		while (rs.next()) {
			StraddleCarrierLocationBean bean = new StraddleCarrierLocationBean();
			bean.setSimID(simID);
			bean.setStraddleCarrierName(rs.getString("STRADDLECARRIER_NAME"));
			bean.setT(rs.getTime("T"));
			bean.setRoad(rs.getString("ROAD"));
			bean.setDirection(rs.getInt("DIRECTION") == 0 ? false : true);
			bean.setX(rs.getDouble("X"));
			bean.setY(rs.getDouble("Y"));
			bean.setZ(rs.getDouble("Z"));

			TreeMap<Time, StraddleCarrierLocationBean> tm = beans.get(bean.getStraddleCarrierName());
			if (tm == null) {
				tm = new TreeMap<>();
				beans.put(bean.getStraddleCarrierName(), tm);
			}
			tm.put(bean.getT(), bean);
		}

		if (rs != null) {
			rs.close();
		}
	}

	@Override
	public int insert(StraddleCarrierLocationBean bean) throws SQLException {
		if(psSave == null){
			psSave = DbMgr.getInstance().getConnection().prepareStatement(SAVE_QUERY);
		}
		if(psUpdate == null){
			psUpdate = DbMgr.getInstance().getConnection().prepareStatement(UPDATE_QUERY);
		}

		psUpdate.setString(1, bean.getRoad());
		psUpdate.setInt(2, bean.isDirection() ? 1 : 0);
		psUpdate.setDouble(3, bean.getX());
		psUpdate.setDouble(4, bean.getY());
		psUpdate.setDouble(5, bean.getZ());
		psUpdate.setString(6, bean.getStraddleCarrierName());
		psUpdate.setInt(7, bean.getSimID());
		psUpdate.setTime(8, bean.getT());

		int result = psUpdate.executeUpdate();

		if(result == 0){
			psSave.setString(1, bean.getStraddleCarrierName());
			psSave.setInt(2, bean.getSimID());
			psSave.setTime(3, bean.getT());
			psSave.setString(4, bean.getRoad());
			psSave.setInt(5, bean.isDirection() ? 1 : 0);
			psSave.setDouble(6, bean.getX());
			psSave.setDouble(7, bean.getY());
			psSave.setDouble(8, bean.getZ());

			psSave.addBatch();

			if(batchSize == BUFFER_SIZE){
				psSave.executeBatch();
				DbMgr.getInstance().getConnection().commit();
				batchSize = 0;
			} else {
				batchSize++;
			}
		}
		return result;
	}

	@Override
	public String getLoadQuery() {
		return LOAD_QUERY;
	}

	@Override
	public int size() {
		int count = 0;
		if (beans != null) {
			for (TreeMap<Time, StraddleCarrierLocationBean> tm : beans.values()) {
				count += tm.size();
			}
		}
		return count;
	}

}
