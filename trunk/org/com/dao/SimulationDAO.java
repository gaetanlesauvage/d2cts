package org.com.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.HashSet;
import java.util.Iterator;

import org.apache.log4j.Logger;
import org.com.DbMgr;
import org.com.model.SimulationBean;

public class SimulationDAO implements D2ctsDao<SimulationBean> {
	private static final Logger log = Logger.getLogger(SimulationDAO.class);

	private static final String LOAD_QUERY = "SELECT ID, DATE_REC, CONTENT, SCHEDULING_ALGORITHM, SEED FROM SIMULATION";
	private static final String INSERT_QUERY = "INSERT INTO SIMULATION (DATE_REC, CONTENT, SCHEDULING_ALGORITHM, SEED) VALUES (?, ?, ?, ?)";
	private static final String LAST_INSERTED_BEAN_QUERY = LOAD_QUERY
			+ " WHERE DATE_REC IN (SELECT MAX(DATE_REC) FROM SIMULATION)";

	private static SimulationDAO instance;

	private HashSet<SimulationBean> beans;

	private PreparedStatement loadStatement;
	private PreparedStatement insertStatement;
	private PreparedStatement lastInsertedBeanStatement;

	public static final SimulationDAO getInstance() {
		if (instance == null) {
			instance = new SimulationDAO();
		}
		return instance;
	}

	private SimulationDAO() {

		try {
			load();
		} catch (SQLException e) {
			log.fatal(e.getMessage(), e);
		}
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
		log.info("SimulationDAO closed.");
	}

	@Override
	public void load() throws SQLException {
		if (loadStatement == null) {
			Connection c = DbMgr.getInstance().getConnection();
			loadStatement = c.prepareStatement(getLoadQuery());
		}

		beans = new HashSet<>();

		ResultSet rs = loadStatement.executeQuery();
		SchedulingAlgorithmDAO saDAO = SchedulingAlgorithmDAO.getInstance();
		while (rs.next()) {
			SimulationBean bean = new SimulationBean();
			bean.setId(rs.getInt("ID"));
			bean.setDate_rec(rs.getDate("DATE_REC"));
			bean.setContent(rs.getInt("CONTENT"));
			bean.setSchedulingAlgorithm(saDAO.get(rs.getInt("SCHEDULING_ALGORITHM")));
			long seed = rs.getLong("SEED");
			if(!rs.wasNull())
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
			lastInsertedBeanStatement = c
					.prepareStatement(LAST_INSERTED_BEAN_QUERY);
		}

		SchedulingAlgorithmDAO saDAO = SchedulingAlgorithmDAO.getInstance();

		ResultSet rs = lastInsertedBeanStatement.executeQuery();
		if (rs.next()) {
			b = new SimulationBean();
			b.setId(rs.getInt("ID"));
			b.setDate_rec(rs.getTimestamp("DATE_REC"));
			b.setContent(rs.getInt("CONTENT"));
			b.setSchedulingAlgorithm(saDAO.get(rs.getInt("SCHEDULING_ALGORITHM")));
			long seed = rs.getLong("SEED");
			if(!rs.wasNull())
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
			insertStatement = c.prepareStatement(INSERT_QUERY);
		}
		insertStatement.setTimestamp(1,
				new Timestamp(System.currentTimeMillis()));
		insertStatement.setInt(2, bean.getContent());

		insertStatement.setInt(3, bean.getSchedulingAlgorithm().getId());
		if(bean.getSeed()!=null)
			insertStatement.setDouble(4, bean.getSeed());
		else
			insertStatement.setNull(4, Types.DOUBLE);
		int res = insertStatement.executeUpdate();
		
		DbMgr.getInstance().getConnection().commit();
		return res;
	}

	@Override
	public int size(){
		return beans.size();
	}
}