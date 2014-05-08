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
import org.com.dao.scheduling.ResultsDAO;

public class DAOMgr {
	private static final Logger log = Logger.getLogger(DAOMgr.class);
	
	public static void closeAllDAO() throws SQLException{
		SimulationDAO.getInstance().close();
		ScenarioDAO.getInstance().close();
		TerminalDAO.getInstance().close();
		ResultsDAO.getInstance().close();
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
		
		//Scheduling parameters
		for(Iterator<OnlineACOParametersDAO> it = OnlineACOParametersDAO.getInstances(); it.hasNext(); ){
			it.next().close();
		}
		for(Iterator<OfflineACO2ParametersDAO> it = OfflineACO2ParametersDAO.getInstances(); it.hasNext(); ){
			it.next().close();
		}
		for(Iterator<OfflineACOParametersDAO> it = OfflineACOParametersDAO.getInstances(); it.hasNext(); ){
			it.next().close();
		}
		for(Iterator<LinearParametersDAO> it = LinearParametersDAO.getInstances(); it.hasNext(); ){
			it.next().close();
		}
		for(Iterator<GreedyParametersDAO> it = GreedyParametersDAO.getInstances(); it.hasNext(); ){
			it.next().close();
		}
		for(Iterator<RandomParametersDAO> it = RandomParametersDAO.getInstances(); it.hasNext(); ){
			it.next().close();
		}
		for(Iterator<BranchAndBoundParametersDAO> it = BranchAndBoundParametersDAO.getInstances(); it.hasNext(); ){
			it.next().close();
		}
		for(Iterator<BBParametersDAO> it = BBParametersDAO.getInstances(); it.hasNext(); ){
			it.next().close();
		}
		
		
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
		for(Iterator<SchedulingAlgorithmDAO> it = SchedulingAlgorithmDAO.getInstances(); it.hasNext(); ){
			it.next().close();
		}
		DefaultParametersDAO.getInstance().close();
		
		for(Iterator<MissionLoadDAO> it = MissionLoadDAO.getInstances(); it.hasNext(); ){
			it.next().close();
		}
		
		for(Iterator<EventDAO> it = EventDAO.getInstances(); it.hasNext(); ){
			it.next().close();
		}
		
		for(Iterator<StraddleCarrierLocationDAO> it = StraddleCarrierLocationDAO.getInstances(); it.hasNext(); ){
			it.next().close();
		}
				
		log.info("DAO objects closed.");
	}
	
}
