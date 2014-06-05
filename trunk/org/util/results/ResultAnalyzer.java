package org.util.results;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.com.dao.ScenarioDAO;
import org.com.dao.SimulationDAO;
import org.com.dao.scheduling.ResultsDAO;
import org.com.model.ScenarioBean;
import org.com.model.SchedulingAlgorithmBean;
import org.com.model.SimulationBean;
import org.com.model.scheduling.ResultsBean;
import org.scheduling.MissionSchedulerEvalParameters;
import org.time.Time;

public class ResultAnalyzer {
	private Map<ScenarioBean, Set<ResultsBean>> beans; 

	/**
	 * Analyze results of every simulation in database
	 * using the given scenario.
	 * @param scenarioID
	 */
	public ResultAnalyzer(ScenarioBean[] scenarios){
		beans = new HashMap<>();
		for(ScenarioBean scenario : scenarios){
			try {
				ResultsDAO.getInstance().load();
				SimulationDAO simDAO = SimulationDAO.getInstance();
				Set<SimulationBean> simBeans = simDAO.getSimulationsOfScenario(scenario.getId());
				for(SimulationBean sim : simBeans){
					ResultsBean r = ResultsDAO.getInstance().get(sim.getId());
					if(r!=null) {
						Set<ResultsBean> s = beans.get(scenario);
						if(s == null){
							s = new HashSet<ResultsBean>();
							beans.put(scenario, s);
						}
						s.add(r);
					}
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}

	public String toString() {
		StringBuilder builder = new StringBuilder();
		for(Entry<ScenarioBean, Set<ResultsBean>> e : beans.entrySet()){
			builder.append("Results of scenario "+e.getKey().getId()+" - "+e.getKey().getName()+" ("+e.getValue().size()+") :\n");
			builder.append("\tSimID\tALgorithm\tTime\tScheduling Time (ns)\tDistance\tTardiness\tEarliness\tOT\tT\tL\tE\tFitness\n");
			for(ResultsBean r : e.getValue()){
				SimulationBean sim = SimulationDAO.getInstance().get(r.getSimulation());
				SchedulingAlgorithmBean algo = sim.getSchedulingAlgorithm();
				MissionSchedulerEvalParameters evalParameters = algo.getEvalParameters();
				builder.append("\t"+r.getSimulation()+"\t"+sim.getSchedulingAlgorithm()+"\t\t"+new Time(r.getOverallTime())+"\t"+r.getSchedulingTime()+"\t"+r.getDistance()+"\t"+r.getLateness()+"\t"+r.getEarliness()+"\t"+r.getOt());
				builder.append("\t"+evalParameters.getTravelTimeCoeff()+"\t"+evalParameters.getLatenessCoeff()+"\t"+evalParameters.getEarlinessCoeff()+"\t"+r.getScore(evalParameters)+"\n");
			}
			builder.append("\n");
		}
		return builder.toString();
	}

	public String toHTML() {
		StringBuilder builder = new StringBuilder();
		for(Entry<ScenarioBean, Set<ResultsBean>> e : beans.entrySet()){
			builder.append("<p><h2> Scenario "+e.getKey().getId()+" - "+e.getKey().getName()+"</h2></p>\n");
			builder.append("<table border=\"1\">\n");
			builder.append("<tr>\n");
			builder.append("<th>SimID</th>\n");
			builder.append("<th>Algorithm</th>\n");
			builder.append("<th>Computing Time</th>\n");
			builder.append("<th>Scheduling Time (ns)</th>\n");
			builder.append("<th>Travel Time</th>\n");
			builder.append("<th>Tardiness</th>\n");
			builder.append("<th>Earliness</th>\n");
			builder.append("<th>Overspent TimeWindows</th>\n");
			builder.append("<th>T</th>\n");
			builder.append("<th>L</th>\n");
			builder.append("<th>E</th>\n");
			builder.append("<th>Fitness</th>\n");
			builder.append("</tr>\n");
			for(ResultsBean r : e.getValue()){
				SimulationBean sim = SimulationDAO.getInstance().get(r.getSimulation());
				SchedulingAlgorithmBean algo = sim.getSchedulingAlgorithm();
				MissionSchedulerEvalParameters evalParameters = algo.getEvalParameters();
				builder.append("<tr>\n");
				builder.append("<td>"+r.getSimulation()+"</td>\n");
				builder.append("<td>"+algo.getName()+"</td>\n");
				builder.append("<td>"+new Time(r.getOverallTime())+"</td>\n");
				builder.append("<td>"+r.getSchedulingTime()+"</td>\n");
				builder.append("<td>"+new Time(r.getDistance())+"</td>\n");
				builder.append("<td>"+new Time(r.getLateness())+"</td>\n");
				builder.append("<td>"+new Time(r.getEarliness())+"</td>\n");
				builder.append("<td>"+r.getOt()+"</td>\n");
				builder.append("<td>"+evalParameters.getTravelTimeCoeff()+"</td>\n");
				builder.append("<td>"+evalParameters.getLatenessCoeff()+"</td>\n");
				builder.append("<td>"+evalParameters.getEarlinessCoeff()+"</td>\n");
				builder.append("<td>"+r.getScore(evalParameters)+"</td>\n");
				builder.append("</tr>\n");
			}
			builder.append("</table>\n");
		}
		return builder.toString();
	}

	public static String getHtmlOpening(String date){
		StringBuilder builder = new StringBuilder();
		builder.append("<html>\n");
		builder.append("<head><title>Results of scenarios at "+date+"</title></head>\n");
		builder.append("<body>\n");
		return builder.toString();

	}

	public static String getHtmlClosure(){
		StringBuilder builder = new StringBuilder();
		builder.append("</body>\n");
		builder.append("</html>\n");
		return builder.toString();
	}

	public void analyze() throws IOException {
		Date d = new Date();
		DateFormat df = new SimpleDateFormat("ddMMyyyy");
		File f = new File("./Results_"+df.format(d)+".html");
		FileWriter fw = new FileWriter(f);
		fw.append(ResultAnalyzer.getHtmlOpening(df.format(d)));
		System.out.println("-----------------------------------------");
		System.out.println(toString());
		System.out.println("-----------------------------------------");
		fw.append(toHTML());
		fw.flush();
		fw.append(getHtmlClosure());
		fw.flush();
		fw.close();
	}

	public static void main(String [] args) throws IOException{
		Date d = new Date();
		DateFormat df = new SimpleDateFormat("ddMMyyyy");
		File f = new File("./Results_"+df.format(d)+".html");
		FileWriter fw = new FileWriter(f);
		fw.append(ResultAnalyzer.getHtmlOpening(df.format(d)));
		Iterator<ScenarioBean> iterator = ScenarioDAO.getInstance().iterator();
		ScenarioBean[] array = new ScenarioBean[ScenarioDAO.getInstance().size()];
		int i = 0;
		while(iterator.hasNext()){
			array[i++] = iterator.next();
		}
		ResultAnalyzer analyzer = new ResultAnalyzer(array);
		System.out.println("-----------------------------------------");
		System.out.println(analyzer.toString());
		System.out.println("-----------------------------------------");
		fw.append(analyzer.toHTML());
		fw.flush();

		fw.append(ResultAnalyzer.getHtmlClosure());
		fw.flush();
		fw.close();
	}

}
