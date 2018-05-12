package org.com.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.log4j.Logger;
import org.com.DbMgr;
import org.com.dao.scheduling.AbstractSchedulingParameterDAO;
import org.com.model.SchedulingAlgorithmBean;
import org.com.model.SimulationBean;
import org.com.model.scheduling.ParameterBean;
import org.scheduling.MissionSchedulerEvalParameters;

public class SimulationDAO implements D2ctsDao<SimulationBean> {
	private static final Logger log = Logger.getLogger(SimulationDAO.class);

	private static final String LOAD_QUERY = "SELECT ID, DATE_REC, CONTENT, SCHEDULING_ALGORITHM, SEED FROM SIMULATION ORDER BY ID";
	private static final String INSERT_SIMULATION_QUERY = "INSERT INTO SIMULATION (DATE_REC, CONTENT, SCHEDULING_ALGORITHM, SEED) VALUES (?, ?, ?, ?)";

	private static final String LAST_INSERTED_BEAN_QUERY = "SELECT ID, DATE_REC, CONTENT, SCHEDULING_ALGORITHM, SEED FROM SIMULATION WHERE DATE_REC IN (SELECT MAX(DATE_REC) FROM SIMULATION) ORDER BY ID";

	private static SimulationDAO instance;

	private List<SimulationBean> beans;

	private PreparedStatement loadStatement;
	private PreparedStatement insertStatement;
	private PreparedStatement lastInsertedBeanStatement;


	public static final SimulationDAO getInstance() {
		if (instance == null) {
			instance = new SimulationDAO(false);
		}
		return instance;
	}

	private SimulationDAO(boolean dontLoad) {
		if(!dontLoad ){
			try {
				load();
			} catch (SQLException e) {
				log.fatal(e.getMessage(), e);
			}
		}
	}

	public SimulationBean get(Integer simID) {
		if (simID != null) {
			for (SimulationBean bean : beans) {
				if (bean.getId().intValue() == simID.intValue())
					return bean;
			}
		}
		return null;
	}
	
	/**
	 * Retrieve every simulation using the given scenario.
	 * @param scenarioID
	 * @return
	 */
	public SortedSet<SimulationBean> getSimulationsOfScenario(Integer scenarioID){
		SortedSet<SimulationBean> set = new TreeSet<>();
		for(SimulationBean bean : beans){
			if(bean.getContent().intValue() == scenarioID.intValue()){
				set.add(bean);
			}
		}
		return set;
	}

	@Override
	public void close() throws SQLException {
		if (loadStatement != null) {
			loadStatement.close();
		}
		if (insertStatement != null) {
			insertStatement.close();
		}
		if (lastInsertedBeanStatement != null) {
			lastInsertedBeanStatement.close();
		}
		instance = null;
		log.info("SimulationDAO closed.");
	}

	@Override
	public void load() throws SQLException {
		if (loadStatement == null) {
			Connection c = DbMgr.getInstance().getConnection();
			loadStatement = c.prepareStatement(getLoadQuery());
		}

		beans = new ArrayList<>();

		ResultSet rs = loadStatement.executeQuery();
		
		while (rs.next()) {
			
			SimulationBean bean = new SimulationBean();
			bean.setId(rs.getInt("ID"));
			bean.setDate_rec(new Date(rs.getTimestamp("DATE_REC").getTime()));
			bean.setContent(rs.getInt("CONTENT"));

			SchedulingAlgorithmDAO saDAO = SchedulingAlgorithmDAO.getInstance(bean.getId());
			SchedulingAlgorithmBean algo = saDAO.get(rs.getInt("SCHEDULING_ALGORITHM"));
			AbstractSchedulingParameterDAO<?> parametersDAO = AbstractSchedulingParameterDAO.getInstance(algo.getName(), bean.getId());

			algo.setParameters(parametersDAO.get());

			bean.setSchedulingAlgorithm(algo);
			long seed = rs.getLong("SEED");
			if (!rs.wasNull())
				bean.setSeed(new Long(seed));
			beans.add(bean);
		}
		if (rs != null) {
			rs.close();
		}
	}

	@Override
	public String getLoadQuery() {
		return SimulationDAO.LOAD_QUERY;
	}

	@Override
	public Iterator<SimulationBean> iterator() {
		return beans.iterator();
	}

	public String[] getColumnsName() {
		return new String[] { "ID", "NAME", "DATE_REC", "FILE", "SCHEDULING ALGORITHM", "SEED" };
	}

	public SimulationBean getLastInsertedBean() throws SQLException {
		SimulationBean b = null;

		if (lastInsertedBeanStatement == null) {
			Connection c = DbMgr.getInstance().getConnection();
			lastInsertedBeanStatement = c.prepareStatement(LAST_INSERTED_BEAN_QUERY);
		}

		

		ResultSet rs = lastInsertedBeanStatement.executeQuery();
		if (rs.next()) {
			b = new SimulationBean();
			b.setId(rs.getInt("ID"));
			b.setDate_rec(rs.getTimestamp("DATE_REC"));
			b.setContent(rs.getInt("CONTENT"));
			SchedulingAlgorithmDAO saDAO = SchedulingAlgorithmDAO.getInstance(b.getId());
			SchedulingAlgorithmBean algo = saDAO.get(rs.getInt("SCHEDULING_ALGORITHM"));

			AbstractSchedulingParameterDAO<?> parametersDAO = AbstractSchedulingParameterDAO.getInstance(algo.getName(), b.getId());
			algo.setParameters(parametersDAO.get());

			b.setSchedulingAlgorithm(algo);
			long seed = rs.getLong("SEED");
			if (!rs.wasNull())
				b.setSeed(new Long(seed));
		} else {
			log.error("Cannot retrieve last created simulation!");
		}

		if (rs != null)
			rs.close();

		return b;
	}

	@Override
	public int insert(SimulationBean bean) throws SQLException {
		if (insertStatement == null) {
			Connection c = DbMgr.getInstance().getConnection();
			insertStatement = c.prepareStatement(INSERT_SIMULATION_QUERY, Statement.RETURN_GENERATED_KEYS);
		}
		insertStatement.setTimestamp(1, new Timestamp(System.currentTimeMillis()));
		insertStatement.setInt(2, bean.getContent());

		insertStatement.setInt(3, bean.getSchedulingAlgorithm().getId());
		if (bean.getSeed() != null)
			insertStatement.setDouble(4, bean.getSeed());
		else
			insertStatement.setNull(4, Types.DOUBLE);
		int res = insertStatement.executeUpdate();

		ResultSet rs = insertStatement.getGeneratedKeys();
		if (rs != null && rs.first()) {
			// récupère l'id généré
			bean.setId(rs.getInt(1));
		}

		if (rs != null) {
			rs.close();
		}

		AbstractSchedulingParameterDAO<?> parametersDAO = AbstractSchedulingParameterDAO.getInstance(bean.getSchedulingAlgorithm().getName(),
				bean.getId());
		MissionSchedulerEvalParameters m = bean.getSchedulingAlgorithm().getEvalParameters();
		ParameterBean[] parameters = new ParameterBean[3 + bean.getSchedulingAlgorithm().getParameters().size()];
		int i = 0;
		for (ParameterBean p : m.getParameters()) {
			parameters[i++] = p;
		}
		for (ParameterBean p : bean.getSchedulingAlgorithm().getParameters().values()) {
			parameters[i++] = p;
		}
		parametersDAO.save(parameters);

		DbMgr.getInstance().getConnection().commit();
		return res;
	}

	@Override
	public int size() {
		return beans.size();
	}

	public static SimulationDAO getInstance(boolean b) {
		if (instance == null) {
			instance = new SimulationDAO(b);
		}
		return instance;
	}
}