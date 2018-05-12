package org.com.dao;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.log4j.Logger;
import org.com.DbMgr;
import org.com.model.SlotBean;

public class SlotDAO implements D2ctsDao<SlotBean> {
	private static final Logger log = Logger.getLogger(SlotDAO.class);

	private static Map<Integer, SlotDAO> instances;

	private Map<String, SlotBean> beans;
	private Integer terminalID;

	private static final String LOAD_QUERY = "SELECT NAME, TERMINAL, BAY, LEN, RATE FROM SLOT WHERE TERMINAL = ?";

	private PreparedStatement psLoad;

	public static SlotDAO getInstance(Integer terminalID) {
		if (instances == null) {
			instances = new HashMap<>();

		}
		if (instances.containsKey(terminalID))
			return instances.get(terminalID);
		else {
			SlotDAO instance = new SlotDAO(terminalID);
			instances.put(terminalID, instance);
			return instance;
		}
	}

	public static Iterator<SlotDAO> getInstances() {
		if (instances == null) {
			instances = new HashMap<>();
		}
		return instances.values().iterator();
	}

	private SlotDAO(Integer terminalID) {
		this.terminalID = terminalID;
	}

	@Override
	public Iterator<SlotBean> iterator() {
		if (beans == null)
			beans = new HashMap<>();
		return beans.values().iterator();
	}

	@Override
	public void close() throws SQLException {
		if (psLoad != null) {
			psLoad.close();
		}
		instances = null;
		log.info("SlotDAO of terminal " + terminalID + " closed.");
	}

	@Override
	public void load() throws SQLException {
		if (psLoad == null) {
			psLoad = DbMgr.getInstance().getConnection()
					.prepareStatement(LOAD_QUERY);
		}

		beans = new HashMap<>();

		psLoad.setInt(1, terminalID);
		ResultSet rs = psLoad.executeQuery();
		while (rs.next()) {
			SlotBean bean = new SlotBean();
			bean.setName(rs.getString("NAME"));
			bean.setTerminal(rs.getInt("TERMINAL"));
			bean.setBay(rs.getString("BAY"));
			bean.setLen(rs.getInt("LEN"));
			bean.setRate(rs.getDouble("RATE"));
			beans.put(bean.getName(), bean);
		}

		if (rs != null) {
			rs.close();
		}
	}

	@Override
	public int insert(SlotBean bean) throws SQLException {
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
}
