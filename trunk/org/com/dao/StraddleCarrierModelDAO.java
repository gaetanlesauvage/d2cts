package org.com.dao;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.log4j.Logger;
import org.com.DbMgr;
import org.com.model.StraddleCarrierModelBean;

public class StraddleCarrierModelDAO implements D2ctsDao<StraddleCarrierModelBean>{
	private static final Logger log = Logger.getLogger(StraddleCarrierModelDAO.class);

	private static StraddleCarrierModelDAO instance;

	private static final String LOAD_QUERY = "SELECT NAME, WIDTH, HEIGHT, LENGTH, INNER_WIDTH, INNER_LENGTH, BACK_OVER_LENGTH, " +
			"FRONT_OVER_LENGTH, CAB_WIDTH, COMPATIBILITY, EMPTY_SPEED, LOADED_SPEED, BAY_SPEED, CONTAINER_HANDLING_FROM_TRUCK_MIN, " +
			"CONTAINER_HANDLING_FROM_TRUCK_MAX, CONTAINER_HANDLING_FROM_GROUND_MIN, CONTAINER_HANDLING_FROM_GROUND_MAX, "+
			"ENTER_EXIT_BAY_TIME_MIN, ENTER_EXIT_BAY_TIME_MAX, TURN_BACK_TIME " +
			"FROM STRADDLECARRIER_MODEL";

	private PreparedStatement psLoad;

	private Map<String, StraddleCarrierModelBean> beans;

	private StraddleCarrierModelDAO(){

	}

	public static StraddleCarrierModelDAO getInstance(){
		if(instance == null){
			instance = new StraddleCarrierModelDAO();
		}
		return instance;
	}

	@Override
	public Iterator<StraddleCarrierModelBean> iterator() {
		if(beans == null) beans = new HashMap<>();
		return beans.values().iterator();
	}

	@Override
	public void close() throws SQLException {
		if(psLoad != null){
			psLoad.close();
		}
		log.info("StraddleCarrierModelDAO closed.");
	}

	@Override
	public void load() throws SQLException {
		if(psLoad == null){
			psLoad = DbMgr.getInstance().getConnection().prepareStatement(LOAD_QUERY);
		}
		beans = new HashMap<>();

		ResultSet rs = psLoad.executeQuery();
		while(rs.next()){
			StraddleCarrierModelBean bean = new StraddleCarrierModelBean();

			bean.setName(rs.getString("NAME"));
			
			bean.setWidth(rs.getDouble("WIDTH"));
			bean.setHeight(rs.getDouble("HEIGHT"));
			bean.setLength(rs.getDouble("LENGTH"));

			bean.setInnerWidth(rs.getDouble("INNER_WIDTH"));
			bean.setInnerLength(rs.getDouble("INNER_LENGTH"));
			bean.setBackOverLength(rs.getDouble("BACK_OVER_LENGTH"));
			bean.setFrontOverLength(rs.getDouble("FRONT_OVER_LENGTH"));
			bean.setCabWidth(rs.getDouble("CAB_WIDTH"));
			bean.setCompatibility(rs.getString("COMPATIBILITY"));
			bean.setEmptySpeed(rs.getDouble("EMPTY_SPEED"));
			bean.setLoadedSpeed(rs.getDouble("LOADED_SPEED"));
			bean.setBaySpeed(rs.getDouble("BAY_SPEED"));
			bean.setContainerHandlingFromTruckMin(rs.getDouble("CONTAINER_HANDLING_FROM_TRUCK_MIN"));
			bean.setContainerHandlingFromTruckMax(rs.getDouble("CONTAINER_HANDLING_FROM_TRUCK_MAX"));
			bean.setContainerHandlingFromGroundMin(rs.getDouble("CONTAINER_HANDLING_FROM_GROUND_MIN"));
			bean.setContainerHandlingFromGroundMax(rs.getDouble("CONTAINER_HANDLING_FROM_GROUND_MAX"));
			bean.setEnterExitBayTimeMin(rs.getDouble("ENTER_EXIT_BAY_TIME_MIN"));
			bean.setEnterExitBayTimeMax(rs.getDouble("ENTER_EXIT_BAY_TIME_MAX"));
			bean.setTurnBackTime(rs.getDouble("TURN_BACK_TIME")); 
			beans.put(bean.getName(), bean);
		}

		if(rs!=null){
			rs.close();
		}
	}

	@Override
	public int insert(StraddleCarrierModelBean bean) throws SQLException {
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
	
	public StraddleCarrierModelBean get(String id){
		return beans.get(id);
	}
}
