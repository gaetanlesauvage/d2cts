package org.com.dao.scheduling;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.log4j.Logger;
import org.com.DbMgr;
import org.com.dao.D2ctsDao;
import org.com.dao.RoadDAO;
import org.com.model.SimulationBean;
import org.com.model.scheduling.ResultsBean;

/**
 * 
 * @author Gaëtan Lesauvage
 * @since 2014/03
 *
 */
public class ResultsDAO implements D2ctsDao<ResultsBean>{
	private static final Logger log = Logger.getLogger(RoadDAO.class);

	private static ResultsDAO instance;

	private Map<Integer, ResultsBean> beans;

	private static final String LOAD_QUERY = "SELECT SIMULATION, DISTANCE, LATENESS, EARLINESS, OT, TIME, SCHEDULING_TIME FROM RESULTS";
	private static final String UPDATE_QUERY = "UPDATE RESULTS SET DISTANCE = ?, LATENESS = ?, EARLINESS = ?, OT = ?, TIME = ?, SCHEDULING_TIME = ? WHERE SIMULATION = ?";
	private static final String INSERT_QUERY = "INSERT INTO RESULTS (SIMULATION, DISTANCE, LATENESS, EARLINESS, OT, TIME, SCHEDULING_TIME) VALUES (?, ?, ?, ?, ?, ?, ?)";

	private static final String GET_SCORE_QUERY = "SELECT ((r.DISTANCE*spT.VALUE)+(r.LATENESS*spL.VALUE)+(r.EARLINESS*spE.VALUE)) as SCORE FROM SIMULATION s"
			+ "JOIN SIMULATION_SCHEDULING_PARAMETERS sspT ON s.ID = sspT.ID_SIM"
			+ "JOIN SCHEDULING_PARAMETER spT ON sspT.ID_PARAM = spT.ID AND spT.NAME = 'T'"
			+ "JOIN SIMULATION_SCHEDULING_PARAMETERS sspL ON s.ID = sspL.ID_SIM"
			+ "JOIN SCHEDULING_PARAMETER spL ON sspL.ID_PARAM = spL.ID AND spL.NAME = 'L'"
			+ "JOIN SIMULATION_SCHEDULING_PARAMETERS sspE ON s.ID = sspE.ID_SIM"
			+ "JOIN SCHEDULING_PARAMETER spE ON sspE.ID_PARAM = spE.ID AND spE.NAME = 'E'"
			+ "JOIN RESULTS r ON r.SIMULATION = s.ID"
			+ "WHERE s.ID = ?"; 
	
	private PreparedStatement psLoad;
	private PreparedStatement psUpdate;
	private PreparedStatement psInsert;
	private PreparedStatement psGetScore;

	public static ResultsDAO getInstance() throws SQLException{
		if(instance == null){
			instance = new ResultsDAO();
		}
		return instance;
	}

	private ResultsDAO() throws SQLException{
		beans = new HashMap<>();
		load();
	}

	@Override
	public Iterator<ResultsBean> iterator() {
		if(beans == null) beans = new HashMap<>();
		return beans.values().iterator();
	}

	@Override
	public void close() throws SQLException {
		if(psLoad != null){
			psLoad.close();
		}
		if(psUpdate != null){
			psUpdate.close();
		}
		if(psInsert != null){
			psInsert.close();
		}
		if(psGetScore != null){
			psGetScore.close();
		}
		instance = null;
		log.info("ResultsDAO closed.");
	}

	public Double getScore(SimulationBean sim) throws SQLException {
		Double score = null;
		if(psGetScore == null){
			psGetScore = DbMgr.getInstance().getConnection().prepareStatement(GET_SCORE_QUERY);
		}
		psGetScore.setInt(1, sim.getId());
		ResultSet rs = psGetScore.executeQuery();
		if(rs.next()){
			score = rs.getDouble("SCORE");
		}
		rs.close();
		return score;
	}
	
	@Override
	public void load() throws SQLException {
		if(psLoad == null){
			psLoad = DbMgr.getInstance().getConnection().prepareStatement(LOAD_QUERY);
		}

		beans = new HashMap<>();
		ResultSet rs = psLoad.executeQuery();
		while(rs.next()){
			ResultsBean bean = new ResultsBean();
			bean.setSimulation(rs.getInt("SIMULATION"));
			bean.setDistance(rs.getDouble("DISTANCE"));
			bean.setEarliness(rs.getDouble("EARLINESS"));
			bean.setLateness(rs.getDouble("LATENESS"));
			bean.setOt(rs.getInt("OT"));
			bean.setOverallTime(rs.getDouble("TIME"));
			bean.setSchedulingTime(rs.getDouble("SCHEDULING_TIME"));
			beans.put(bean.getSimulation(), bean);
		}
		if(rs!=null){
			rs.close();
		}		
	}

	@Override
	public int insert(ResultsBean bean) throws SQLException {
		int result = 0;
		if(beans.containsKey(bean.getSimulation())){
			//UPDATE
			if(psUpdate == null){
				psUpdate = DbMgr.getInstance().getConnection().prepareStatement(UPDATE_QUERY);
			}
			psUpdate.setDouble(1, bean.getDistance());
			psUpdate.setDouble(2, bean.getLateness());
			psUpdate.setDouble(3, bean.getEarliness());
			psUpdate.setInt(4, bean.getOt());
			psUpdate.setDouble(5, bean.getOverallTime());
			psUpdate.setDouble(6, bean.getSchedulingTime());
			psUpdate.setInt(7, bean.getSimulation());

			result = psUpdate.executeUpdate();
		} else {
			//INSERT
			if(psInsert == null){
				psInsert = DbMgr.getInstance().getConnection().prepareStatement(INSERT_QUERY);
			}
			psInsert.setInt(1, bean.getSimulation());
			psInsert.setDouble(2, bean.getDistance());
			psInsert.setDouble(3, bean.getLateness());
			psInsert.setDouble(4, bean.getEarliness());
			psInsert.setInt(5, bean.getOt());
			psInsert.setDouble(6, bean.getOverallTime());
			psInsert.setDouble(7, bean.getSchedulingTime());
			result = psInsert.executeUpdate();
		}
		return result;		
	}

	@Override
	public String getLoadQuery() {
		return LOAD_QUERY;
	}

	@Override
	public int size(){
		return beans.size();
	}

	public ResultsBean get(Integer simID) {
		return beans.get(simID);
	}
}
