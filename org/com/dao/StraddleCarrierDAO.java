package org.com.dao;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.log4j.Logger;
import org.com.DbMgr;
import org.com.model.StraddleCarrierBean;

import com.mysql.jdbc.exceptions.jdbc4.MySQLIntegrityConstraintViolationException;

public class StraddleCarrierDAO implements D2ctsDao<StraddleCarrierBean>{
	private static final String[] SQL_INSERT_WHERE_CLAUSES = {
			"VALUES ('straddle_carrier_1',	?, 'standard', 'red',	'Central_1',	'Central_1',	0.5,	false,	0,	false,	'RDijkstra',	null)",
			"VALUES ('straddle_carrier_10',	?, 'standard', 'LightSeaGreen',	'Central_15',	'Central_15',	0.5,	false,	0,	false,	'RDijkstra',	null)",
			"VALUES ('straddle_carrier_2',	?, 'standard', 'blue',	'Central_2',	'Central_2',	0.5,	false,	0,	false,	'RDijkstra',	null)",
			"VALUES ('straddle_carrier_3',	?, 'standard', 'green',	'Central_3',	'Central_3',	0.5,	false,	0,	false,	'RDijkstra',	null)",
			"VALUES ('straddle_carrier_4',	?, 'standard', 'yellow',	'Central_4',	'Central_4',	0.5,	false,	0,	false,	'RDijkstra',	null)",
			"VALUES ('straddle_carrier_5',	?, 'standard', 'sienna',	'Central_5',	'Central_5',	0.5,	false,	0,	false,	'RDijkstra',	null)",
			"VALUES ('straddle_carrier_6',	?, 'standard', 'orange',	'Central_11',	'Central_11',	0.5,	false,	0,	false,	'RDijkstra',	null)",
			"VALUES ('straddle_carrier_7',	?, 'standard', 'magenta',	'Central_12',	'Central_12',	0.5,	false,	0,	false,	'RDijkstra',	null)",
			"VALUES ('straddle_carrier_8',	?, 'standard', 'olive',	'Central_13',	'Central_13',	0.5,	false,	0,	false,	'RDijkstra',	null)",
			"VALUES ('straddle_carrier_9',	?, 'standard', 'DarkOrchid',	'Central_14',	'Central_14',	0.5,	false,	0,	false,	'RDijkstra',	null)"
	};
	
	private static final String SQL_INSERT_QUERY = "INSERT INTO STRADDLECARRIER (NAME, SCENARIO, MODEL, COLOR, SLOT, ORIGIN_ROAD, ORIGIN_RATE, ORIGIN_DIRECTION, ORIGIN_AVAILABILITY, AUTOHANDLING, ROUTING_ALGORITHM, ROUTING_HEURISTIC) ";
	
	private static final Logger log = Logger.getLogger(StraddleCarrierDAO.class);
	
	private static Map<Integer, StraddleCarrierDAO> instances;
	
	private static final String LOAD_QUERY = "SELECT s.NAME, s.SCENARIO, s.MODEL, s.COLOR, s.SLOT, s.ORIGIN_ROAD, s.ORIGIN_RATE, " +
			"s.ORIGIN_DIRECTION, s.ORIGIN_AVAILABILITY, s.AUTOHANDLING, s.ROUTING_ALGORITHM, s.ROUTING_HEURISTIC " +
			"FROM STRADDLECARRIER s " +
			"WHERE SCENARIO = ?";
	
	private PreparedStatement psLoad;
	
	private Map<String, StraddleCarrierBean> beans;
	private Integer scenarioID;
	
	private StraddleCarrierDAO(Integer scenarioID){
		this.scenarioID = scenarioID;
	}
	
	public static StraddleCarrierDAO getInstance(Integer scenarioID){
		if(instances == null){
			instances = new HashMap<>();
			
		}
		if(instances.containsKey(scenarioID)) return instances.get(scenarioID);
		else {
			StraddleCarrierDAO instance = new StraddleCarrierDAO(scenarioID);
			instances.put(scenarioID, instance);
			return instance;
		}
	}

	public static Iterator<StraddleCarrierDAO> getInstances(){
		if(instances == null){
			instances = new HashMap<>();
		}
		return instances.values().iterator();
	}
	
	@Override
	public Iterator<StraddleCarrierBean> iterator() {
		if(beans == null) beans = new HashMap<>();
		return beans.values().iterator();
	}

	@Override
	public void close() throws SQLException {
		if(psLoad != null){
			psLoad.close();
		}
		instances = null;
		log.info("StraddleCarrierDAO of scenario "+scenarioID+" closed.");
	}

	@Override
	public void load() throws SQLException {
		if(psLoad == null){
			psLoad = DbMgr.getInstance().getConnection().prepareStatement(LOAD_QUERY);
		}
		beans = new HashMap<>();
		
		psLoad.setInt(1, scenarioID);
		ResultSet rs = psLoad.executeQuery();
		StraddleCarrierModelDAO models = StraddleCarrierModelDAO.getInstance();
		models.load();
		//TODO add get(beanID) method to retrieve the straddle carrier model
		while(rs.next()){
			StraddleCarrierBean bean = new StraddleCarrierBean();

			bean.setName(rs.getString("NAME"));
			bean.setScenario(rs.getInt("SCENARIO"));
			bean.setModel(models.get(rs.getString("MODEL")));
			bean.setColor(rs.getString("COLOR"));
			bean.setSlot(rs.getString("SLOT"));
			bean.setOriginRoad(rs.getString("ORIGIN_ROAD"));
			bean.setOriginRate(rs.getDouble("ORIGIN_RATE"));
			int od = rs.getInt("ORIGIN_DIRECTION");
			bean.setOriginDirection(od == 0 ? false : true);
			bean.setOriginAvailability(rs.getInt("ORIGIN_AVAILABILITY"));
			int ah = rs.getInt("AUTOHANDLING");
			bean.setAutoHandling(ah == 0 ? false : true);
			bean.setRoutingAlgorithm(rs.getString("ROUTING_ALGORITHM"));
			bean.setRoutingHeuristic(rs.getString("ROUTING_HEURISTIC")); 			
			beans.put(bean.getName(), bean);
		}
		
		if(rs!=null){
			rs.close();
		}
	}

	@Override
	public int insert(StraddleCarrierBean bean) throws SQLException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public String getLoadQuery() {
		return LOAD_QUERY;
	}
	
	@Override
	public int size(){
		return beans == null ? -1 : beans.size(); 
	}

	/**
	 * Add a given number of straddle carriers into the scenario bean.
	 * @param straddleCarriersCount
	 */
	public void add(Integer straddleCarriersCount) throws SQLException {
		int added = 0;
		Statement statement = DbMgr.getInstance().getConnection().createStatement();
		for(int i=0; i<straddleCarriersCount; i++){
			String where = SQL_INSERT_WHERE_CLAUSES[i];
			String sql = SQL_INSERT_QUERY+where;
			sql = sql.replaceFirst("\\?", scenarioID.toString());
		try{
			added += statement.executeUpdate(sql);
		} catch (MySQLIntegrityConstraintViolationException e) {
			added++;
		}
		}
		if(added != straddleCarriersCount){
			log.error("Error while adding straddle carriers!");
			DbMgr.getInstance().getConnection().rollback();
		} else {
			load();
			DbMgr.getInstance().getConnection().commit();
		}
	}
}
