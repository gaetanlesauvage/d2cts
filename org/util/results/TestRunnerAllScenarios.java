package org.util.results;

import org.com.dao.ScenarioDAO;
import org.com.model.ScenarioBean;
import org.display.MainFrame;

/**
 * This class aims at launching simulation runs on each simulation found in database
 *  which is relative to a given scenario.
 *  At the end a ResultAnalyzer instance is created to export the results into an HTML file. 
 * @author gaetan
 */
public class TestRunnerAllScenarios {
	/**
	 * Args : scenario IDs
	 * @param args
	 */
	public static void main (String [] args) {
		try{
			ScenarioBean[] scenarios = new ScenarioBean[args.length];
			int i = 0;
			for(String arg : args){
				Integer scenarioID = Integer.parseInt(arg);
				ScenarioBean scenario = ScenarioDAO.getInstance().getScenario(scenarioID);
				new ScenarioRunner(scenario);
				scenarios[i++] = scenario;
			}
			ResultAnalyzer ra = new ResultAnalyzer(scenarios);
			ra.analyze();
			MainFrame.getInstance().exit();
		} catch (Exception e){
			e.printStackTrace();
		}
	}
}
