package org.com.dao.scheduling;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
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

public class MissionLoadDAO implements D2ctsDao<LoadBean> {
	private static final Logger log = Logger.getLogger(MissionLoadDAO.class);

	//1 instance for each straddle carrier (scName, instance)
	private static Map<String, MissionLoadDAO> instances;

	private static final String LOAD_QUERY = "SELECT l.ID, l.TW_MIN, l.TW_MAX, l.MISSION, l.STARTABLE_TIME, l.STATE, l.PHASE, " +
			"l.EFFECTIVE_START_TIME, l.PICKUP_REACH_TIME, l.LOAD_START_TIME, l.LOAD_END_TIME, l.DELIVERY_START_TIME, " +
			"l.DELIVERY_REACH_TIME, l.UNLOAD_START_TIME, l.END_TIME, l.WAIT_TIME, l.LINKED_LOAD, w.LOAD_INDEX " +
			"FROM MISSION_LOAD l INNER JOIN STRADDLECARRIERS_WORKLOAD w ON w.LOAD_ID = l.ID " +
			"WHERE w.STRADDLECARRIER_NAME = ?";

	private PreparedStatement psLoad;

	private List<LoadBean> beans;

	private String straddleCarrierName;

	private MissionLoadDAO(String straddleCarrierName) {
		this.straddleCarrierName = straddleCarrierName;
		try {
			load();
		} catch (SQLException e) {
			log.error(e.getMessage(), e);
		}
	}

	public static MissionLoadDAO getInstance(String straddleCarrierName) {
		if (instances == null) {
			instances = new HashMap<>();
		}
		if(!instances.containsKey(straddleCarrierName)){
			instances.put(straddleCarrierName, new MissionLoadDAO(straddleCarrierName));
		}
		return instances.get(straddleCarrierName);
	}

	@Override
	public Iterator<LoadBean> iterator() {
		if (beans == null){
			beans = new ArrayList<>(1);
		}
		return beans.iterator();
	}

	public static Iterator<MissionLoadDAO> getInstances() {
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

			bean.setID(rs.getInt("ID"));
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
			bean.setLinkedLoad(rs.getInt("LINKED_LOAD"));
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

	public static void closeInstance() {
		instances = null;
	}
}
