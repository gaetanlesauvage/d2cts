package org.util.generators;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.com.DbMgr;
import org.com.dao.ContainerDAO;
import org.com.dao.EventDAO;
import org.com.dao.ScenarioDAO;
import org.com.dao.SchedulingAlgorithmDAO;
import org.com.dao.StraddleCarrierDAO;
import org.com.dao.TerminalDAO;
import org.com.dao.scheduling.DefaultParametersDAO;
import org.com.model.ContainerBean;
import org.com.model.EventBean;
import org.com.model.ScenarioBean;
import org.com.model.SchedulingAlgorithmBean;
import org.com.model.TerminalBean;
import org.com.model.scheduling.BBParametersBean;
import org.com.model.scheduling.BranchAndBoundParametersBean;
import org.com.model.scheduling.DefaultParametersBean;
import org.com.model.scheduling.GreedyParametersBean;
import org.com.model.scheduling.LinearParametersBean;
import org.com.model.scheduling.OfflineACO2ParametersBean;
import org.com.model.scheduling.OfflineACOParametersBean;
import org.com.model.scheduling.OnlineACOParametersBean;
import org.com.model.scheduling.ParameterBean;
import org.com.model.scheduling.ParameterType;
import org.com.model.scheduling.RandomParametersBean;
import org.exceptions.ContainerDimensionException;
import org.exceptions.EmptyLevelException;
import org.exceptions.NoPathFoundException;
import org.scheduling.LinearMissionScheduler;
import org.scheduling.bb.BB;
import org.scheduling.branchAndBound.BranchAndBound;
import org.scheduling.greedy.GreedyMissionScheduler;
import org.scheduling.offlineACO.OfflineACOScheduler;
import org.scheduling.offlineACO2.OfflineACOScheduler2;
import org.scheduling.onlineACO.OnlineACOScheduler;
import org.scheduling.random.RandomMissionScheduler;
import org.time.Time;
import org.time.event.EventType;
import org.util.Edodizer;
import org.util.dbLoading.SimulationBuilder;
import org.util.generators.parsers.ShipGenerationData;
import org.util.generators.parsers.StockGenerationData;
import org.util.generators.parsers.TrainGenerationData;
import org.util.generators.parsers.TruckGenerationData;
import org.util.generators.v2.ScenarioGenerator;

/**
 * This class aims at creating several scenarios according generation parameters :
 *  - Terminal ID
 *  - Generation seed
 *  - 20 feet containers count
 *  - 40 feet containers count
 *  - 45 feet containers count
 *  - StraddleCarriers count distribution (array)
 *  - Stock missions parameters (array)
 *  - Road missions parameters (array)
 *  - Sea missions parameters (array)
 *  - Train missions parameters (array)
 *  - Dod and EDod distribution (array)
 * For each created scenario one simulation will be created for each scheduling algorithm available (with default parameters)
 * @author gaetan
 *
 */
public class ScenariosGenerator {
	public ScenariosGenerator(Integer terminalID, long[] seeds, int twenty, int forty, int fortyFive, String[] names, int[] straddleCarriers, 
			StockGenerationData[] stock, TruckGenerationData[] road, ShipGenerationData[] sea, TrainGenerationData[] rail,
			DegreeDistribution[] degrees) throws SQLException, NoPathFoundException, ContainerDimensionException, EmptyLevelException{

		//Scheduling parameters
		Map<String, ParameterBean[]> defaultSchedulingParameters = getDefaultSchedulingParameters();
		
		//For each random seed
		for(long seed : seeds){
			Random r = new Random(seed);

			//Load Terminal
			TerminalBean terminal = TerminalDAO.getInstance().getTerminal(terminalID);

			//Create one secnario for each straddleCarriers distribution
			for(int i=0; i<names.length; i++){
				//Scenario
				ScenarioBean scenario = ScenarioGenerator.getInstance().generate(names[i], terminal, twenty, forty, fortyFive, straddleCarriers[i], null);
				
				//Create missions of this scenario
				new MissionsFileGenerator(scenario,seed,rail[i],road[i],sea[i],stock[i]);

				for(int j=0; j<degrees.length; j++){
					for(int k=0; k<degrees[j].getEDoD().length; k++){
						//Dupliquer le scenario
						ScenarioBean edodizedScenario = new ScenarioBean();
						edodizedScenario.setFile(scenario.getFile());
						edodizedScenario.setName(scenario.getName()+"["+degrees[j].getDoD()+";"+degrees[j].getEDoD()[k]+"]");
						edodizedScenario.setTerminal(scenario.getTerminal());
						ScenarioDAO.getInstance().insert(edodizedScenario);
						StraddleCarrierDAO.getInstance(edodizedScenario.getId()).add(straddleCarriers[i]);;

						//Implique de copier toutes les missions du scenario père
						Iterator<EventBean> eventsIt = EventDAO.getInstance(scenario.getId()).iterator();
						int eventsCount = 0;
						while(eventsIt.hasNext()){
							EventBean event = eventsIt.next();
							if(event.getType() == EventType.NewMission)
								eventsCount++;
							EventBean copy = new EventBean(event);
							EventDAO.getInstance(edodizedScenario.getId()).insert(copy);
						}
						Iterator<ContainerBean> contIt = ContainerDAO.getInstance(scenario.getId()).iterator();
						while(contIt.hasNext()){
							ContainerBean container = contIt.next();
							ContainerBean copy = new ContainerBean(container);
							copy.setScenario(edodizedScenario.getId());
							ContainerDAO.getInstance(edodizedScenario.getId()).insert(copy);
						}
						//Edodize missions
						Edodizer.edodize(edodizedScenario, degrees[j].getDoD(), degrees[j].getEDoD()[k], r);

						//Create one simulation for each available scheduler
						SchedulingAlgorithmDAO dao = SchedulingAlgorithmDAO.getInstance(null);
						Iterator<SchedulingAlgorithmBean> iterator = dao.iterator();
						while(iterator.hasNext()){
							
							SchedulingAlgorithmBean schedulingBean = iterator.next();
							
							if("BranchAndBound".equals(schedulingBean.getName()) && eventsCount > 4 && straddleCarriers[i] > 3) //Too complex!
									continue;
							
							schedulingBean.setParameters(defaultSchedulingParameters.get(schedulingBean.getName()));
							SimulationBuilder builder = new SimulationBuilder(edodizedScenario.getId(), schedulingBean, seed);
							builder.build();
						}					
					}
				}
				//Delete the father scenario
				ScenarioDAO.getInstance().delete(scenario);
			}
			DbMgr.getInstance().getConnection().commit();
		}	
		//close
		try{
			DbMgr.getInstance().getConnection().close();
		} catch (Exception e){
			e.printStackTrace(); //Any other idea ?
		}
		System.exit(0);
	}

	private Map<String, ParameterBean[]> getDefaultSchedulingParameters(){
		Map<String, ParameterBean[]> result = new HashMap<String, ParameterBean[]>(); 
		DefaultParametersDAO defaultParameters = DefaultParametersDAO.getInstance();
		for(Iterator<SchedulingAlgorithmBean> it = SchedulingAlgorithmDAO.getInstance(null).iterator(); it.hasNext(); ){
			ParameterBean[] parameters = null;
			SchedulingAlgorithmBean bean = it.next();
			String[] names = null;
			ParameterType[] types = null;

			switch (bean.getName()) {
			case OnlineACOScheduler.rmiBindingName:
				names = OnlineACOParametersBean.names();
				types = OnlineACOParametersBean.types();
				break;
			case OfflineACOScheduler.rmiBindingName:
				names = OfflineACOParametersBean.names();
				types = OfflineACOParametersBean.types();
				break;
			case OfflineACOScheduler2.rmiBindingName:
				names = OfflineACO2ParametersBean.names();
				types = OfflineACO2ParametersBean.types();
				break;
			case LinearMissionScheduler.rmiBindingName:
				names = LinearParametersBean.names();
				types = LinearParametersBean.types();
				break;
			case GreedyMissionScheduler.rmiBindingName:
				names = GreedyParametersBean.names();
				types = GreedyParametersBean.types();
				break;
			case RandomMissionScheduler.rmiBindingName:
				names = RandomParametersBean.names();
				types = RandomParametersBean.types();
				break;
			case BranchAndBound.rmiBindingName:
				names = BranchAndBoundParametersBean.names();
				types = BranchAndBoundParametersBean.types();
				break;
			case BB.rmiBindingName:
				names = BBParametersBean.names();
				types = BBParametersBean.types();
				break;
			}

			parameters = new ParameterBean[names.length];
			if (names != null && types != null) {
				int i = 0;
				for (String name : names) {
					DefaultParametersBean dfBean = defaultParameters.get(name.toUpperCase());
//					SchedulingParametersBeanInterface parameter = null;
					ParameterBean paramBean = new ParameterBean(name, types[i]);
					
//					switch (bean.getName()) {
//					case OnlineACOScheduler.rmiBindingName:
//						parameter = OnlineACOParametersBean.get(name);
//						break;
//					case LinearMissionScheduler.rmiBindingName:
//						parameter = LinearParametersBean.get(name);
//						break;
//					case RandomMissionScheduler.rmiBindingName:
//						parameter = RandomParametersBean.get(name);
//						break;
//					case GreedyMissionScheduler.rmiBindingName:
//						parameter = GreedyParametersBean.get(name);
//						break;
//					case BB.rmiBindingName:
//						parameter = BBParametersBean.get(name);
//						break;
//					case BranchAndBound.rmiBindingName:
//						parameter = BranchAndBoundParametersBean.get(name);
//						break;
//					case OfflineACOScheduler.rmiBindingName:
//						parameter = OfflineACOParametersBean.get(name);
//						break;
//					case OfflineACOScheduler2.rmiBindingName:
//						parameter = OfflineACO2ParametersBean.get(name);
//						break;
//					}
//					if (parameter != null) {
//						parameter.setValue(dfBean.getValue());
//					}
					paramBean.setValue(dfBean != null ? dfBean.getValue() : null);
					parameters[i++] = paramBean;
				}

			}
			result.put(bean.getName(), parameters);
		}

		return result;
	}

	static class DegreeDistribution {
		double dod;
		double[] edod;

		public DegreeDistribution (double dod, double[] edod){
			this.dod = dod;
			this.edod = edod;
		}

		public double getDoD(){
			return dod;
		}

		public double[] getEDoD(){
			return edod;
		}

		//		public String getName(int i){
		//			return dod+"|"+edod[i];
		//		}
	}

	public static void main(String [] args) throws SQLException, NoPathFoundException, ContainerDimensionException, EmptyLevelException{
		  //Doit-on gérer une matrice pour les descripteurs de mission ? [scenario][stocks missions]... ou garde t-on un vecteur ? [missions du scenario i]
		StockGenerationData stock1 = new StockGenerationData(3, new Time(0d).getDate(), new Time("00:30:00").getDate(), new Time("00:15:00").getDate(), "1-stock");
		StockGenerationData stock2 = new StockGenerationData(1, new Time(0d).getDate(), new Time("00:30:00").getDate(), new Time("00:15:00").getDate(), "2-stock");
		StockGenerationData stock3 = new StockGenerationData(2, new Time(0d).getDate(), new Time("00:30:00").getDate(), new Time("00:15:00").getDate(), "3-stock");
		StockGenerationData stock4 = new StockGenerationData(5, new Time(0d).getDate(), new Time("00:30:00").getDate(), new Time("00:15:00").getDate(), "4-stock");
		StockGenerationData stock5 = new StockGenerationData(10, new Time(0d).getDate(), new Time("01:00:00").getDate(), new Time("00:15:00").getDate(), "5-stock");
		StockGenerationData stock6 = stock5;
		StockGenerationData stock7 = new StockGenerationData(20, new Time(0d).getDate(), new Time("02:00:00").getDate(), new Time("00:15:00").getDate(), "7-stock");
		StockGenerationData stock8 = new StockGenerationData(25, new Time(0d).getDate(), new Time("02:00:00").getDate(), new Time("00:15:00").getDate(), "8-stock");
		StockGenerationData stock9 = new StockGenerationData(30, new Time(0d).getDate(), new Time("02:00:00").getDate(), new Time("00:15:00").getDate(), "9-stock");
		StockGenerationData stock10 = new StockGenerationData(35, new Time(0d).getDate(), new Time("02:00:00").getDate(), new Time("00:15:00").getDate(), "10-stock");
		
		Integer terminalID = 1;
		long[] seeds = {1L};
		int twenty = 50;
		int forty = 450;
		int fortyFive = 0;
		double[] edod1 = {0d};
		double[] edod2 = {0.25d, 0.5d, 0.75d, 1d};
		
		DegreeDistribution[] degrees = {
				new DegreeDistribution(0d, edod1),
				new DegreeDistribution(0.25d, edod2),
				new DegreeDistribution(0.5d, edod2),
				new DegreeDistribution(0.75d, edod2),
				new DegreeDistribution(1d, edod2)
		};
		int[] straddleCarriers = {1, 2, 10, 2, 4, 5, 5, 10, 10, 10};
		StockGenerationData[] stock = {stock1, stock2, stock3, stock4, stock5, stock6, stock7, stock8, stock9, stock10};
		TruckGenerationData[] road = {null, null, null, null, null, null, null, null, null, null};
		ShipGenerationData[] sea = {null, null, null, null, null, null, null, null, null, null};
		TrainGenerationData[] rail = {null, null, null, null, null, null, null, null, null, null};
		//
		//		int[] missionCount = new int[straddleCarriers.length];
		//		for(int i = 0; i<straddleCarriers.length; i++){
		//			missionCount[i] = 0;
		//			if(i<stock.length)
		//				missionCount[i] += stock[i].getNb();
		//			if(i<road.length)
		//				missionCount[i] += road[i].getNb();
		//			if(i<sea.length)
		//				missionCount[i] += sea[i].get;
		//			if(i<rail.length)
		//				missionCount[i] += rail[i].getNb();
		//		}
		
		String[] names = {"1) instance triviale, sans probleme d'affectation",
				  "2) instance triviale, sans problème d'ordonnancement",
				  "3) peu de missions sont à planifier alors qu'un grand nombre de ressources sont disponibles (comparable à l'optimum)",
				  "4) instance de petite taille où une solution optimale est calculable",
				  "5) instance moyenne du problème à petite échelle",
				  "6) instance moyenne du problème permettant de mesurer l'impact de l'ajout d'une ressource dans le calcul des algorithmes : qualité de la solution / performance calculatoire. Les missions sont identiques à celles du scénario 5",
				  "7) instance avec une lourde charge pour les véhicules afin de mesurer la capacité de l'algorithme à résister à des scénarios critiques",
				  "8) instance moyenne à grande échelle",
				  "9) seconde instance moyenne à grande échelle",
				  "10) troisième instance moyenne à grande échelle"
		  };
		List<String> edodizedNames = new ArrayList<>();
		for(int k=0; k<names.length; k++){
			for(int i=0; i<degrees.length; i++){
				for(int j=0; j<degrees[i].getEDoD().length; j++){
					edodizedNames.add(names[k]+" "+degrees[i].getDoD()+"|"+degrees[i].getEDoD()[j]);
				}
			}
		}

		new ScenariosGenerator(terminalID, seeds, twenty, forty, fortyFive, names, straddleCarriers, stock, road, sea, rail, degrees);
	}
}

