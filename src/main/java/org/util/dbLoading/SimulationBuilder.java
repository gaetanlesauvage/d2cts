package org.util.dbLoading;

import java.sql.SQLException;

import org.apache.log4j.Logger;
import org.com.dao.SimulationDAO;
import org.com.model.SchedulingAlgorithmBean;
import org.com.model.SimulationBean;

public class SimulationBuilder {
	private static final Logger logger = Logger
			.getLogger(SimulationBuilder.class);

	private SimulationBean simulation;
	
	public SimulationBuilder(Integer scenarioID, SchedulingAlgorithmBean schedulingAlgorithmBean, Long seed) {
		simulation = new SimulationBean(scenarioID,schedulingAlgorithmBean);
		simulation.setSeed(seed);
	}

	public void build() {
		try {
			SimulationDAO.getInstance(true).insert(simulation);
			simulation = SimulationDAO.getInstance().getLastInsertedBean();
			logger.info("Simulation created: " + simulation);
		} catch (SQLException e) {
			logger.fatal(e.getMessage(), e);
		}
	}

	public SimulationBean getSimulationBean() {
		return simulation;
	}

}
