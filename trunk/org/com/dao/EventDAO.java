package org.com.dao;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.log4j.Logger;
import org.com.DbMgr;
import org.com.model.EventBean;
import org.time.Time;
import org.time.event.EventType;

public class EventDAO implements D2ctsDao<EventBean> {
	private static final Logger log = Logger.getLogger(EventDAO.class);

	private static Map<Integer, EventDAO> instances;

	private static final String LOAD_QUERY = "SELECT e.ID, e.TYPE, e.T, e.DESCRIPTION " + "FROM EVENT e WHERE SCENARIO = ?";
	private static final String LOAD_TYPES_QUERY = "SELECT NAME FROM EVENT_TYPE WHERE CLASS_NAME = ?";

	private static final String INSERT_QUERY = "INSERT INTO EVENT (TYPE, SCENARIO, T, DESCRIPTION) VALUES (?, ?, ?, ?, ?)";

	private PreparedStatement psLoad;
	private PreparedStatement psLoadTypes;
	private PreparedStatement psInsert;

	private Map<Integer, EventBean> beans;
	private Integer scenarioID;

	private EventDAO(Integer scenarioID) {
		this.scenarioID = scenarioID;
	}

	public static EventDAO getInstance(Integer scenarioID) {
		if (instances == null) {
			instances = new HashMap<>();
		}
		if (instances.containsKey(scenarioID))
			return instances.get(scenarioID);
		else {
			EventDAO instance = new EventDAO(scenarioID);
			instances.put(scenarioID, instance);
			return instance;
		}
	}

	public static Iterator<EventDAO> getInstances() {
		if (instances == null) {
			instances = new HashMap<>();
		}
		return instances.values().iterator();
	}

	@Override
	public Iterator<EventBean> iterator() {
		if (beans == null)
			beans = new HashMap<>();
		return beans.values().iterator();
	}

	@Override
	public void close() throws SQLException {
		if (psLoad != null) {
			psLoad.close();
		}
		if (psInsert != null) {
			psInsert.close();
		}
		if (psLoadTypes != null) {
			psLoadTypes.close();
		}
		instances = null;
		log.info("EventDAO of scenario " + scenarioID + " closed.");
	}

	@Override
	public void load() throws SQLException {
		if (psLoad == null) {
			psLoad = DbMgr.getInstance().getConnection().prepareStatement(LOAD_QUERY);
		}

		beans = new HashMap<>();

		psLoad.setInt(1, scenarioID);

		ResultSet rs = psLoad.executeQuery();
		while (rs.next()) {
			EventBean bean = new EventBean();

			bean.setId(rs.getInt("ID"));
			bean.setType(EventType.get(rs.getString("TYPE")));
			bean.setTime(new Time(rs.getTime("T")));
			bean.setDescription(rs.getString("DESCRIPTION"));

			beans.put(bean.getId(), bean);
		}
		if (rs != null) {
			rs.close();
		}
	}

	@Override
	public int insert(EventBean bean) throws SQLException {
		if (psInsert == null) {
			psInsert = DbMgr.getInstance().getConnection().prepareStatement(INSERT_QUERY, PreparedStatement.RETURN_GENERATED_KEYS);
		}
		if (psLoadTypes == null) {
			psLoadTypes = DbMgr.getInstance().getConnection().prepareStatement(LOAD_TYPES_QUERY);
		}

		psLoadTypes.setString(1, bean.getType().getClassForName());
		ResultSet rsType = psLoadTypes.executeQuery();
		int res = 0;
		if (rsType.next()) {
			psInsert.setString(1, rsType.getString(1));
			psInsert.setInt(2, scenarioID);
			Calendar c = Calendar.getInstance();
			c.set(Calendar.HOUR_OF_DAY, bean.getTime().getHours());
			c.set(Calendar.MINUTE, bean.getTime().getHours());
			c.set(Calendar.SECOND, (int) bean.getTime().getSeconds());
			c.set(Calendar.MILLISECOND, (int) ((int) bean.getTime().getSeconds() - bean.getTime().getSeconds()) * 1000);
			psInsert.setTime(3, new java.sql.Time(c.getTimeInMillis()));
			psInsert.setString(4, bean.getDescription());
			res = psInsert.executeUpdate();
			if(res>0){
				ResultSet genKeys = psInsert.getGeneratedKeys();
				if(genKeys.next()){
					bean.setId(genKeys.getInt(1));
					beans.put(bean.getId(), bean);
				}
				genKeys.close();
			}
		}
		rsType.close();
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
}
