package org.com.dao.scheduling;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.com.DbMgr;
import org.com.dao.D2ctsDao;
import org.com.model.scheduling.ParameterBean;
import org.scheduling.LinearMissionScheduler;
import org.scheduling.MissionScheduler;
import org.scheduling.bb.BB;
import org.scheduling.branchAndBound.BranchAndBound;
import org.scheduling.greedy.GreedyMissionScheduler;
import org.scheduling.offlineACO.OfflineACOScheduler;
import org.scheduling.offlineACO2.OfflineACOScheduler2;
import org.scheduling.onlineACO.OnlineACOScheduler;
import org.scheduling.random.RandomMissionScheduler;

public abstract class AbstractSchedulingParameterDAO<E extends MissionScheduler> implements D2ctsDao<ParameterBean> {
	protected static final Logger log = Logger.getLogger(OnlineACOParametersDAO.class);

	protected final String LOAD_QUERY = "SELECT p.ID, p.NAME, p.VALUE FROM SIMULATION_SCHEDULING_PARAMETERS ssp"
			+ " INNER JOIN SIMULATION s ON s.ID = ssp.ID_SIM" + " INNER JOIN SCHEDULING_ALGORITHM sa ON s.SCHEDULING_ALGORITHM = sa.ID"
			+ " INNER JOIN SCHEDULING_PARAMETER p ON ssp.ID_PARAM = p.ID" + " WHERE sa.NAME = ? AND s.ID = ?";

	protected final String CHECK_IF_EXISTS_SIM_SCHED_PARAMS = "SELECT ID FROM SIMULATION_SCHEDULING_PARAMETERS WHERE ID_SIM = ? AND ID_PARAM = ?";
	protected final String SAVE_PARAMETERS_VALUES_QUERY = "INSERT INTO SCHEDULING_PARAMETER (NAME,VALUE) VALUES (?,?)";
	protected final String CHECK_IF_EXISTS_QUERY = "SELECT ID FROM SCHEDULING_PARAMETER WHERE NAME = ? AND VALUE = ?";
	protected final String SAVE_SIMULATION_PARAMETERS_VALUES_QUERY = "INSERT INTO SIMULATION_SCHEDULING_PARAMETERS (ID_SIM,ID_PARAM) VALUES (?,?)";

	protected PreparedStatement psLoad;
	protected PreparedStatement psInsertParametersValues;
	protected PreparedStatement psCheckParametersValues;
	protected PreparedStatement psInsertSimulationParametersValues;
	protected PreparedStatement psCheckSimulationParametersValues;

	protected List<ParameterBean> beans;

	protected Integer simID;
	protected boolean loaded;
	
	protected AbstractSchedulingParameterDAO(Integer simID) {
		this.loaded = false;
		this.simID = simID;
	}

	@Override
	public Iterator<ParameterBean> iterator() {
		if (beans == null)
			beans = new ArrayList<>(1);
		return beans.iterator();
	}

	@Override
	public void close() throws SQLException {
		if (psLoad != null) {
			psLoad.close();
		}
		if (psInsertParametersValues != null) {
			psInsertParametersValues.close();
		}
		if (psInsertSimulationParametersValues != null) {
			psInsertSimulationParametersValues.close();
		}
		if (psCheckParametersValues != null) {
			psCheckParametersValues.close();
		}
		if (psCheckSimulationParametersValues != null) {
			psCheckSimulationParametersValues.close();
		}
		
		BBParametersDAO.closeInstance();
		BranchAndBoundParametersDAO.closeInstance();
		DefaultParametersDAO.closeInstance();
		GreedyParametersDAO.closeInstance();
		LinearParametersDAO.closeInstance();
		MissionLoadDAO.closeInstance();
		OfflineACO2ParametersDAO.closeInstance();
		OfflineACOParametersDAO.closeInstance();
		OnlineACOParametersDAO.closeInstance();
		RandomParametersDAO.closeInstance();
		
		log.info("DAO of " + E.rmiBindingName + " closed for simulation " + simID + ".");
	}

	@Override
	public int insert(ParameterBean param) throws SQLException {
		if (psInsertParametersValues == null) {
			psInsertParametersValues = DbMgr.getInstance().getConnection()
					.prepareStatement(SAVE_PARAMETERS_VALUES_QUERY, Statement.RETURN_GENERATED_KEYS);
		}

		param.setSQLID(insert(param.name(), param.getValueAsString()));
		return 1;
	}

	private Integer insert(String name, String value) throws SQLException {
		Integer ID = getID(name, value);
		if (ID == null) {
			psInsertParametersValues.setString(1, name);
			psInsertParametersValues.setString(2, value);
			ID = psInsertParametersValues.executeUpdate();
			if (ID > 0) {
				ResultSet rsID = psInsertParametersValues.getGeneratedKeys();
				if (rsID.next()) {
					ID = rsID.getInt(1);
				}
				rsID.close();
			}
		}

		return ID;
	}

	private Integer getID(String name, String value) throws SQLException {
		// Check if already in table
		Integer id = null;

		if (psCheckParametersValues == null)
			psCheckParametersValues = DbMgr.getInstance().getConnection().prepareStatement(CHECK_IF_EXISTS_QUERY);

		psCheckParametersValues.setString(1, name);
		psCheckParametersValues.setString(2, value == null ? "null" : value.toString());
		ResultSet rs = psCheckParametersValues.executeQuery();
		if (rs.next())
			id = rs.getInt("ID");
		if (rs != null)
			rs.close();
		return id;
	}

	// Affect bean parameters to the given simulation
	public boolean save() throws SQLException {
		if (simID != null) {
			if (psInsertSimulationParametersValues == null)
				psInsertSimulationParametersValues = DbMgr.getInstance().getConnection().prepareStatement(SAVE_SIMULATION_PARAMETERS_VALUES_QUERY);
			if (psCheckSimulationParametersValues == null)
				psCheckSimulationParametersValues = DbMgr.getInstance().getConnection().prepareStatement(CHECK_IF_EXISTS_SIM_SCHED_PARAMS);

			// check if already inserted
			psCheckSimulationParametersValues.setInt(1, simID);
			psInsertSimulationParametersValues.setInt(1, simID);

			for(ParameterBean p : get()){
				insert(p);
				psCheckSimulationParametersValues.setInt(2, p.getSQLID());
				ResultSet rs = psCheckSimulationParametersValues.executeQuery();
				int sID = 0;
				
				if(rs.next()){
					sID = rs.getInt("ID");
					rs.close();
				}
				if (sID == 0) {
					psInsertSimulationParametersValues.setInt(2, p.getSQLID());
					psInsertSimulationParametersValues.addBatch();
				}

			}
			psInsertSimulationParametersValues.executeBatch();
			
			return true;
		}
		return false;
	}

	public boolean save(ParameterBean[] parameters) throws SQLException {
		if (simID != null) {
			if (psInsertSimulationParametersValues == null)
				psInsertSimulationParametersValues = DbMgr.getInstance().getConnection().prepareStatement(SAVE_SIMULATION_PARAMETERS_VALUES_QUERY);
			if (psCheckSimulationParametersValues == null)
				psCheckSimulationParametersValues = DbMgr.getInstance().getConnection().prepareStatement(CHECK_IF_EXISTS_SIM_SCHED_PARAMS);

			// check if already inserted
			psCheckSimulationParametersValues.setInt(1, simID);
			psInsertSimulationParametersValues.setInt(1, simID);

			for(ParameterBean p : parameters){
				insert(p);
				psCheckSimulationParametersValues.setInt(2, p.getSQLID());
				ResultSet rs = psCheckSimulationParametersValues.executeQuery();
				int sID = 0;
				
				if(rs.next()){
					sID = rs.getInt("ID");
					rs.close();
				}
				if (sID == 0) {
					psInsertSimulationParametersValues.setInt(2, p.getSQLID());
					psInsertSimulationParametersValues.addBatch();
				}

			}
			
			psInsertSimulationParametersValues.executeBatch();
			
			return true;
		}
		return false;
	}
	
	@Override
	public String getLoadQuery() {
		return LOAD_QUERY;
	}

	@Override
	public int size() {
		return beans.size();
	}

	public ParameterBean[] get() throws SQLException {
		if(!loaded){
			load();
		}
		ParameterBean[] t = new ParameterBean[beans.size()];
		for(int i=0; i<beans.size();i++)
			t[i] = beans.get(i);
		
		return t;
	}
	
	public static AbstractSchedulingParameterDAO<?> getInstance(String className, Integer simID) {
		switch (className) {
		case OnlineACOScheduler.rmiBindingName:
			return OnlineACOParametersDAO.getInstance(simID);
		case LinearMissionScheduler.rmiBindingName:
			return LinearParametersDAO.getInstance(simID);
		case RandomMissionScheduler.rmiBindingName:
			return RandomParametersDAO.getInstance(simID);
		case GreedyMissionScheduler.rmiBindingName:
			return GreedyParametersDAO.getInstance(simID);
		case BB.rmiBindingName:
			return BBParametersDAO.getInstance(simID);
		case BranchAndBound.rmiBindingName:
			return BranchAndBoundParametersDAO.getInstance(simID);
		case OfflineACOScheduler.rmiBindingName:
			return OfflineACOParametersDAO.getInstance(simID);
		case OfflineACOScheduler2.rmiBindingName:
			return OfflineACO2ParametersDAO.getInstance(simID);
		}
		return null;
	}
}
