package org.com.dao.scheduling;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.com.DbMgr;
import org.com.dao.D2ctsDao;
import org.com.model.scheduling.LoadBean;
import org.missions.MissionPhase;
import org.missions.MissionState;
import org.time.TimeScheduler;

public class MissionLoadDAO implements D2ctsDao<LoadBean> {
	private static final Logger log = Logger.getLogger(MissionLoadDAO.class);

	//1 instance for each simulation and each straddle carrier (<simID, <scName, instance>>)
	private static Map<Long, Map<String, MissionLoadDAO>> instances;
	//private static Map<String, MissionLoadDAO> instances;

	private static final String LOAD_QUERY = "SELECT l.ID, l.T, l.TW_MIN, l.TW_MAX, l.MISSION, l.STARTABLE_TIME, l.STATE, l.PHASE, " +
			"l.EFFECTIVE_START_TIME, l.PICKUP_REACH_TIME, l.LOAD_START_TIME, l.LOAD_END_TIME, l.DELIVERY_START_TIME, " +
			"l.DELIVERY_REACH_TIME, l.UNLOAD_START_TIME, l.END_TIME, l.WAIT_TIME, l.LINKED_LOAD, w.LOAD_INDEX " +
			"FROM MISSION_LOAD l INNER JOIN STRADDLECARRIERS_WORKLOAD w ON w.LOAD_ID = l.ID " +
			"WHERE w.STRADDLECARRIER_NAME = ?";

	private static final String INSERT_QUERY = "INSERT INTO MISSION_LOAD (T, TW_MIN, TW_MAX, MISSION, STARTABLE_TIME, STATE, PHASE,"
			+ " EFFECTIVE_START_TIME, PICKUP_REACH_TIME, LOAD_START_TIME, LOAD_END_TIME, DELIVERY_START_TIME, DELIVERY_REACH_TIME,"
			+ " UNLOAD_START_TIME, END_TIME, WAIT_TIME, LINKED_LOAD) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

	private static final String WORKLOAD_INSERT_QUERY = "INSERT INTO STRADDLECARRIERS_WORKLOAD (STRADDLECARRIER_NAME, SIMULATION, T, LOAD_ID, LOAD_INDEX)"
			+ " VALUES (?, ?, ?, ?, ?)";
	
	private PreparedStatement psLoad;
	private PreparedStatement psInsert;
	private PreparedStatement psWorkloadInsert;
	
	private List<LoadBean> beans;

	private String straddleCarrierName;
	private Long simID;

	private MissionLoadDAO(Long simID, String straddleCarrierName) {
		this.simID = simID;
		this.straddleCarrierName = straddleCarrierName;
		try {
			load();
		} catch (SQLException e) {
			log.error(e.getMessage(), e);
		}
	}

	public static MissionLoadDAO getInstance(Long simID, String straddleCarrierName) {
		if (instances == null) {
			instances = new HashMap<>();
		}
		Map<String, MissionLoadDAO> map = null;
		if(!instances.containsKey(simID)){
			map = new HashMap<String, MissionLoadDAO>();
			instances.put(simID, map);
		} else {
			map = instances.get(simID);
		}
		if(!map.containsKey(straddleCarrierName))
			map.put(straddleCarrierName, new MissionLoadDAO(simID, straddleCarrierName));
		
		return map.get(straddleCarrierName);
	}

	@Override
	public Iterator<LoadBean> iterator() {
		if (beans == null){
			beans = new ArrayList<>(1);
		}
		return beans.iterator();
	}

	public static Iterator<Map<String, MissionLoadDAO>> getInstances() {
		if (instances == null) {
			instances = new HashMap<>();
		}
		return instances.values().iterator();
	}


	@Override
	public void close() throws SQLException {
		if (psLoad != null) {
			psLoad.close();
		}
		if(psInsert != null){
			psInsert.close();
		}
		if(psWorkloadInsert != null){
			psWorkloadInsert.close();
		}
		instances.clear();
		log.info("MissionLoadDAO closed.");
	}

	@Override
	public void load() throws SQLException {
		if (psLoad == null) {
			psLoad = DbMgr.getInstance().getConnection()
					.prepareStatement(LOAD_QUERY);
		}

		beans = new ArrayList<>();

		psLoad.setString(1, straddleCarrierName);

		ResultSet rs = psLoad.executeQuery();
		while (rs.next()) {
			LoadBean bean = new LoadBean();

			bean.setID(rs.getLong("ID"));
			bean.setT(rs.getTime("T"));
			bean.setTwMin(rs.getTime("TW_MIN"));
			bean.setTwMax(rs.getTime("TW_MAX"));
			bean.setMission(rs.getString("MISSION"));
			bean.setStartableTime(rs.getTime("STARTABLE_TIME"));
			bean.setState(MissionState.get(rs.getInt("STATE")));
			bean.setPhase(MissionPhase.get(rs.getInt("PHASE")));
			bean.setEffectiveStartTime(rs.getTime("EFFECTIVE_START_TIME"));
			bean.setPickupReachTime(rs.getTime("PICKUP_REACH_TIME"));
			bean.setLoadStartTime(rs.getTime("LOAD_START_TIME"));
			bean.setLoadEndTime(rs.getTime("LOAD_END_TIME"));
			bean.setDeliveryStartTime(rs.getTime("DELIVERY_START_TIME"));
			bean.setDeliveryReachTime(rs.getTime("DELIVERY_REACH_TIME"));
			bean.setUnloadStartTime(rs.getTime("UNLOAD_START_TIME"));
			bean.setEndTime(rs.getTime("END_TIME"));
			bean.setWaitTime(rs.getTime("WAIT_TIME"));
			bean.setLinkedLoad(rs.getLong("LINKED_LOAD"));
			bean.setLoadIndex(rs.getInt("LOAD_INDEX"));
			bean.setStraddleCarrierName(straddleCarrierName);
			beans.add(bean.getLoadIndex(), bean);

		}

		if (rs != null) {
			rs.close();
		}
	}

	@Override
	public int insert(LoadBean bean) throws SQLException {
		if(psInsert == null){
			psInsert = DbMgr.getInstance().getConnection().prepareStatement(INSERT_QUERY, PreparedStatement.RETURN_GENERATED_KEYS);
		}
		if(psWorkloadInsert == null){
			psWorkloadInsert = DbMgr.getInstance().getConnection().prepareStatement(WORKLOAD_INSERT_QUERY);
		}

		psInsert.setTime(1, TimeScheduler.getInstance().getTime().getSQLTime());
		psInsert.setTime(2, bean.getTwMin().getSQLTime());
		psInsert.setTime(3, bean.getTwMax().getSQLTime());
		psInsert.setString(4, bean.getMission());
		psInsert.setTime(5, bean.getStartableTime().getSQLTime());
		psInsert.setInt(6, bean.getState().getCode());
		psInsert.setInt(7, bean.getPhase().getCode());
		psInsert.setTime(8, bean.getEffectiveStartTime().getSQLTime());
		psInsert.setTime(9, bean.getPickupReachTime().getSQLTime());
		psInsert.setTime(10, bean.getLoadStartTime().getSQLTime());
		psInsert.setTime(11, bean.getLoadEndTime().getSQLTime());
		psInsert.setTime(12, bean.getDeliveryStartTime().getSQLTime());
		psInsert.setTime(13, bean.getDeliveryReachTime().getSQLTime());
		psInsert.setTime(14, bean.getUnloadStartTime().getSQLTime());
		psInsert.setTime(15, bean.getEndTime().getSQLTime());
		psInsert.setTime(16, bean.getWaitTime().getSQLTime());
		if(bean.getLinkedLoad() != null){
			psInsert.setLong(17, bean.getLinkedLoad());
		} else 
			psInsert.setNull(17, Types.NUMERIC);

		int res = psInsert.executeUpdate();
		ResultSet generateKeys = null;
		try{
			generateKeys = psInsert.getGeneratedKeys();
			if (generateKeys.next()){
				bean.setID(generateKeys.getLong(1));
				psWorkloadInsert.setString(1, straddleCarrierName);
				psWorkloadInsert.setLong(2, simID);
				psWorkloadInsert.setTime(3, bean.getT());
				psWorkloadInsert.setLong(4, bean.getID());
				psWorkloadInsert.setLong(5, bean.getLoadIndex());
				res = psWorkloadInsert.executeUpdate();
			}
		} finally {
			if(generateKeys != null){
				generateKeys.close();
			}
		}

		return res;
	}

	@Override
	public String getLoadQuery() {
		return LOAD_QUERY;
	}

	@Override
	public int size() {
		return beans.size();
	}

	public static void closeInstance() {
		instances = null;
	}
}
