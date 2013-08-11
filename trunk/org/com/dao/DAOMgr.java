package org.com.dao;

import java.sql.SQLException;
import java.util.Iterator;

import org.apache.log4j.Logger;
import org.com.dao.scheduling.BBParametersDAO;
import org.com.dao.scheduling.BranchAndBoundParametersDAO;
import org.com.dao.scheduling.DefaultParametersDAO;
import org.com.dao.scheduling.GreedyParametersDAO;
import org.com.dao.scheduling.LinearParametersDAO;
import org.com.dao.scheduling.MissionLoadDAO;
import org.com.dao.scheduling.OfflineACO2ParametersDAO;
import org.com.dao.scheduling.OfflineACOParametersDAO;
import org.com.dao.scheduling.OnlineACOParametersDAO;
import org.com.dao.scheduling.RandomParametersDAO;
import org.com.model.SimulationBean;

public class DAOMgr {
	private static final Logger log = Logger.getLogger(DAOMgr.class);
	
	public static void closeAllDAO() throws SQLException{
		//TODO change with getLastLoadedSimulationID();
		SimulationBean lastSim = SimulationDAO.getInstance().getLastInsertedBean();
		Integer simulationID = null;
		//Integer scenarioID = null;
		if(lastSim != null){
			simulationID = lastSim.getId();
			//scenarioID = lastSim.getContent();
		}
		
		SimulationDAO.getInstance().close();
		ScenarioDAO.getInstance().close();
		TerminalDAO.getInstance().close();
		for(Iterator<BlockDAO> it = BlockDAO.getInstances(); it.hasNext(); ){
			it.next().close();
		}
		for(Iterator<CrossroadDAO> it = CrossroadDAO.getInstances(); it.hasNext(); ){
			it.next().close();
		}
		for(Iterator<RoadDAO> it = RoadDAO.getInstances(); it.hasNext(); ){
			it.next().close();
		}
		BlockTypeDAO.getInstance().close();
		SeaOrientationDAO.getInstance().close();
		
		OnlineACOParametersDAO.getInstance(simulationID).close();
		OfflineACO2ParametersDAO.getInstance(simulationID).close();
		OfflineACOParametersDAO.getInstance(simulationID).close();
		LinearParametersDAO.getInstance(simulationID).close();
		GreedyParametersDAO.getInstance(simulationID).close();
		RandomParametersDAO.getInstance(simulationID).close();
		BranchAndBoundParametersDAO.getInstance(simulationID).close();
		BBParametersDAO.getInstance(simulationID).close();
		
		for(Iterator<SlotDAO> it = SlotDAO.getInstances(); it.hasNext(); ){
			it.next().close();
		}
		for(Iterator<LaserHeadDAO> it = LaserHeadDAO.getInstances(); it.hasNext(); ){
			it.next().close();
		}
		for(Iterator<ContainerDAO> it = ContainerDAO.getInstances(); it.hasNext(); ){
			it.next().close();
		}
		StraddleCarrierModelDAO.getInstance().close();
		for(Iterator<StraddleCarrierDAO> it = StraddleCarrierDAO.getInstances(); it.hasNext(); ){
			it.next().close();
		}
		
		SchedulingAlgorithmDAO.getInstance().close();
		DefaultParametersDAO.getInstance().close();
		
		for(Iterator<MissionLoadDAO> it = MissionLoadDAO.getInstances(); it.hasNext(); ){
			it.next().close();
		}
		
		for(Iterator<EventDAO> it = EventDAO.getInstances(); it.hasNext(); ){
			it.next().close();
		}
		
		log.info("DAO objects closed.");
	}
	
}
