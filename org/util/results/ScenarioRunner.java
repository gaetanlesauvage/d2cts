package org.util.results;

import java.sql.SQLException;
import java.util.Set;
import java.util.SortedSet;

import org.apache.log4j.PropertyConfigurator;
import org.com.DbMgr;
import org.com.dao.ScenarioDAO;
import org.com.dao.SimulationDAO;
import org.com.model.ScenarioBean;
import org.com.model.SimulationBean;
import org.display.GraphicDisplayPanel;
import org.display.system.JTerminal;
import org.positioning.LaserSystem;
import org.runner.RunWithGUI;
import org.scheduling.MissionScheduler;
import org.scheduling.onlineACO.Ant;
import org.system.Terminal;
import org.time.TimeController;
import org.time.TimeScheduler;
import org.util.building.SimulationLoader;

public class ScenarioRunner {

	public ScenarioRunner (ScenarioBean scenario){
		SimulationDAO dao = SimulationDAO.getInstance();
		SortedSet<SimulationBean> beans = dao.getSimulationsOfScenario(scenario.getId());
		for(SimulationBean simBean : beans){
			System.err.println("Loading simulation "+simBean.getId());
			Terminal.getInstance().setTextDisplay(GraphicDisplayPanel.getInstance());
			SimulationLoader.getInstance().load(simBean);
			TimeController tc = new TimeController();
			boolean keepGoing = true;
			System.err.println("Running simulation "+simBean.getId());
			while(keepGoing){
				keepGoing = tc.nextStep(false);
			}
			System.err.println("End of simulation "+simBean.getId());
			try {
				closeSimulation();
			} catch (SQLException e) {
				e.printStackTrace();
			}
			
		}
	}

	private void closeSimulation () throws SQLException {
		DbMgr.getInstance().commitAndClose();
		Terminal.closeInstance();
		MissionScheduler.closeInstance();
		LaserSystem.closeInstance();
		JTerminal.closeInstance();
		TimeScheduler.closeInstance();
		GraphicDisplayPanel.closeInstance();
		SimulationLoader.closeInstance();
		Ant.resetAll();
	}
	
	public static void main(String [] args){
		PropertyConfigurator.configure(RunWithGUI.class.getClassLoader().getResource(("conf/log4j.properties")));

		//testRandom();

		System.setProperty("sun.java2d.opengl", "true");
		System.setProperty("sun.java2d.noddraw", "true"); 
		System.setProperty("sun.java2d.opengl.fbobject", "false");
		System.setProperty("sun.java2d.translaccel", "true");
		System.setProperty("sun.java2d.ddforcevram", "true");
		//System.setProperty("awt.nativeDoubleBuffering", "true");
		System.setProperty("swing.aatext","true");


		
		String name = args.length > 0 ? args[0] : "1) instance triviale, sans problème d'affectation[0.0;0.0]";
		name = name.substring(0, name.indexOf("["));
		SortedSet<ScenarioBean> beansOfAKind = ScenarioDAO.getInstance().getScenariosOfAKind(name);
		ScenarioBean[] beans = beansOfAKind.toArray(new ScenarioBean[beansOfAKind.size()]);
		for(ScenarioBean bean : beans){
			new ScenarioRunner(bean);
		}
		new ResultAnalyzer(beans);
	}
}
