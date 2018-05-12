package org.com.dao;

import java.sql.Connection;
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
import org.com.model.ContainerBean;
import org.com.model.ContainerType;
import org.system.container_stocking.ContainerAlignment;

public class ContainerDAO implements D2ctsDao<ContainerBean> {
	private static final Logger log = Logger.getLogger(ContainerDAO.class);

	private static Map<Integer, ContainerDAO> instances;

	private static final String LOAD_QUERY = "SELECT c.NAME, c.TYPE, c.SCENARIO, t.TEU, "
			+ "cil.SLOT_LEVEL, cil.VEHICLE, sl.SLOT, sl.LEVEL_NUMBER, a.LABEL as ALIGNMENT "
			+ "FROM CONTAINER c "
			+ "INNER JOIN CONTAINER_TYPE t ON c.TYPE = t.ID "
			+ "INNER JOIN SCENARIO scenario ON c.SCENARIO = scenario.ID "
			+ "INNER JOIN CONTAINERS_INIT_LOCATION cil ON cil.CONTAINER_NAME = c.NAME AND c.SCENARIO = cil.SCENARIO "
			+ "LEFT JOIN SLOT_LEVEL sl ON cil.SLOT_LEVEL = sl.NAME AND sl.TERMINAL = scenario.TERMINAL "
			+ "LEFT JOIN ALIGNMENT a ON cil.SLOT_ALIGNMENT = a.ID "
			+ "WHERE c.SCENARIO = ? ORDER BY sl.LEVEL_NUMBER";
	
	private static final String INSERT_CONTAINER_QUERY = "INSERT INTO CONTAINER(NAME, TYPE, SCENARIO) VALUES (?, ?, ?)";
	private static final String INSERT_CONTAINERS_INIT_LOCATION_QUERY = "INSERT INTO CONTAINERS_INIT_LOCATION ("
			+ "CONTAINER_NAME, SCENARIO, SLOT_LEVEL, SLOT_ALIGNMENT, VEHICLE) VALUES (?, ?, ?, ?, ?)";
	

	private PreparedStatement psLoad;
	private PreparedStatement psInsertContainer;
	private PreparedStatement psInsertContainersInitLocation;

	private List<ContainerBean> beans;
	private Integer scenarioID;

	private ContainerDAO(Integer scenarioID) {
		this.scenarioID = scenarioID;
		beans = new ArrayList<ContainerBean>();
	}

	public static ContainerDAO getInstance(Integer scenarioID) {
		if (instances == null) {
			instances = new HashMap<>();

		}
		if (instances.containsKey(scenarioID))
			return instances.get(scenarioID);
		else {
			ContainerDAO instance = new ContainerDAO(scenarioID);
			instances.put(scenarioID, instance);
			return instance;
		}
	}

	public static Iterator<ContainerDAO> getInstances() {
		if (instances == null) {
			instances = new HashMap<>();
		}
		return instances.values().iterator();
	}

	@Override
	public Iterator<ContainerBean> iterator() {
		return beans.iterator();
	}

	@Override
	public void close() throws SQLException {
		if (psLoad != null) {
			psLoad.close();
		}
		if(psInsertContainer != null){
			psInsertContainer.close();
		}
		if(psInsertContainersInitLocation != null){
			psInsertContainersInitLocation.close();
		}
		instances = null;
		log.info("ContainerDAO of terminal " + scenarioID + " closed.");
	}

	@Override
	public void load() throws SQLException {
		if (psLoad == null) {
			psLoad = DbMgr.getInstance().getConnection()
					.prepareStatement(LOAD_QUERY);
		}

		beans = new ArrayList<>(5000);

		psLoad.setInt(1, scenarioID);

		ResultSet rs = psLoad.executeQuery();
		while (rs.next()) {
			ContainerBean bean = new ContainerBean();
			bean.setName(rs.getString("NAME"));
			bean.setScenario(rs.getInt("SCENARIO"));
			bean.setType(ContainerType.get(rs.getInt("TYPE")));
			bean.setTeu(rs.getDouble("TEU"));

			bean.setSlot(rs.getString("SLOT"));
			bean.setSlotLevel(rs.getInt("LEVEL_NUMBER"));
			bean.setAlignment(ContainerAlignment.get(rs.getString("ALIGNMENT")));
			bean.setVehicle(rs.getString("VEHICLE"));

			beans.add(bean);
		}

		if (rs != null) {
			rs.close();
		}
	}

	@Override
	public int insert(ContainerBean bean) throws SQLException {
		if (psInsertContainer == null) {
			Connection c = DbMgr.getInstance().getConnection();
			psInsertContainer = c.prepareStatement(INSERT_CONTAINER_QUERY);
			psInsertContainersInitLocation = c.prepareStatement(INSERT_CONTAINERS_INIT_LOCATION_QUERY);
		}

		psInsertContainer.setString(1, bean.getName());
		psInsertContainer.setInt(2, bean.getType().getID());
		psInsertContainer.setInt(3, bean.getScenario());
		int res = psInsertContainer.executeUpdate();

		beans.add(bean);
		
		psInsertContainersInitLocation.setString(1,bean.getName());
		psInsertContainersInitLocation.setInt(2, bean.getScenario());
		psInsertContainersInitLocation.setString(3, bean.getSlot()+"-"+bean.getSlotLevel());
		psInsertContainersInitLocation.setInt(4, bean.getAlignment().getValue());
		psInsertContainersInitLocation.setString(5, bean.getVehicle());
		res += psInsertContainersInitLocation.executeUpdate();
		if(res == 2){
			DbMgr.getInstance().getConnection().commit();
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
}
