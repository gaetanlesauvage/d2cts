package org.scheduling.offlineACO2;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.com.model.scheduling.OfflineACO2ParametersBean;
import org.exceptions.EmptyResourcesException;
import org.graphstream.graph.Edge;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.DefaultGraph;
import org.missions.Mission;
import org.routing.path.Path;
import org.scheduling.GlobalScore;
import org.scheduling.LocalScore;
import org.scheduling.MissionScheduler;
import org.scheduling.ScheduleEdge;
import org.scheduling.ScheduleResource;
import org.scheduling.ScheduleTask;
import org.scheduling.aco.graph.DepotNode;
import org.scheduling.display.JMissionScheduler;
import org.scheduling.offlineACO.OfflineSchedulerParameters;
import org.scheduling.offlineACO.OfflineSchedulerStats;
import org.system.Terminal;
import org.time.Time;
import org.time.TimeScheduler;
import org.vehicles.StraddleCarrier;

/**
 * MTSP Uses a colony of ants which colonizes parts of the complete graph. A ant
 * is authorized to pass through the DEPOT node (M-1) times (where M is the
 * number of salesmen) during its journey. Each time it chooses the DEPOT the
 * ant choose a salesman to represent among the salesmen not already used. The
 * ant must go through each node to build a solution.
 * 
 * Local pheromone rule ?
 * 
 * When each ant built a solution, the global pheromone rule is applyied.
 * 
 * 
 * @author gaetan
 * 
 */
public class OfflineACOScheduler2 extends MissionScheduler {
	public static final String rmiBindingName = "OfflineACOScheduler2";

	private List<OfflineAnt2> ants;
	private OfflineSchedulerParameters parameters;
	private GlobalScore currentBest;
	// private OfflineAnt2 currentBestAnt;
	private static final int MAX_ANTS = 12; // SET TO 1 FOR DEBUG //FIXME To be
	// parameterized !
	private int syncSize;
	private OfflineSchedulerStats stats;

	public OfflineACOScheduler2() {
		super();
		if (parameters == null) {
			parameters = OfflineACO2ParametersBean.getParameters();
			evalParameters = OfflineACO2ParametersBean.getEvalParameters();
			syncSize = parameters.getSync();
			log.info("MTSP ACO PARAMETERS: " + parameters);
			log.info("MTSP EVAL PARAMETERS: " + evalParameters);
		}

		MissionScheduler.instance = this;
		if (!init)
			init();
	}

	public static void closeInstance() {
		// Nothing to do
	}

	public static OfflineACOScheduler2 getInstance() {
		return (OfflineACOScheduler2) MissionScheduler.instance;
	}

	@Override
	protected void init() {
		init = true;

		// DEBUG
		// try {
		// debug = new PrintWriter(new File("/home/gaetan/debug.dat/"));
		// traceScores = new PrintWriter(new File(
		// "/home/gaetan/traceScores.dat/"));
		// } catch (FileNotFoundException e2) {
		// e2.printStackTrace();
		// }

		computeTime = 0;
		step = 0;
		sstep = TimeScheduler.getInstance().getStep() + 1;

		lock.lock();
		graphChanged = true;
		lock.unlock();

		SOURCE_NODE = new OfflineNode2((Mission) null);

		jms = new JMissionScheduler();
		for (StraddleCarrier rsc : resources) {
			jms.addResource(rsc);
		}
		log.info("MissionGraph created !");

		int current = 1;
		List<StraddleCarrier> lResources = Terminal.getInstance().getStraddleCarriers();
		int overall = lResources.size();
		for (StraddleCarrier rsc : lResources) {
			log.info("Adding resource " + rsc.getId() + " (" + current + "/" + overall + ")");
			current++;
			insertResource(rsc);
		}
		current = 1;
		List<Mission> lTasks = Terminal.getInstance().getMissions();
		overall = lTasks.size();
		for (Mission m : lTasks) {
			log.info("Adding task " + m.getId() + " (" + current + "/" + overall + ")");
			current++;
			insertMission(m);
		}
		// TODO
		if (MissionScheduler.DEBUG) {
			DefaultGraph graph = new DefaultGraph("DEBUGMG");
			Node dn = graph.addNode(DepotNode.ID);
			dn.addAttribute("ui.label", DepotNode.ID);
			for (ScheduleTask<?> t : scheduleTasks.values()) {
				Node n = graph.addNode(t.getID());
				n.addAttribute("ui.label", n.getId());
			}
			List<? extends ScheduleEdge> dests = MissionScheduler.SOURCE_NODE.getDestinations();
			for (ScheduleEdge e : dests) {
				graph.addEdge(e.getID(), e.getNodeFrom().getID(), e.getNodeTo().getID(), true);
				System.out.println("Edge : " + e.getID() + " added");
			}

			for (@SuppressWarnings("rawtypes")
			ScheduleTask t : scheduleTasks.values()) {
				System.out.println("t=" + t.getID());
				// if(t == MissionScheduler.SOURCE_NODE)
				// System.err.println("DEPOT NODE ALREADY IN SCHEDULE TASKS");
				@SuppressWarnings("unchecked")
				List<OfflineEdge2> destinations = t.getDestinations();
				for (OfflineEdge2 e : destinations) {

					ScheduleTask<? extends ScheduleEdge> t2 = e.getNodeTo();
					System.out.println("\te=" + e.getID() + " t2=" + t2.getID());
					Edge de = graph.addEdge(e.getID(), t.getID(), t2.getID(), true);
					// de.addAttribute("ui.label", e.get)
				}
			}
			graph.display();
		}

	}

	/**
	 * Check if the number of ants is correct (MIN(|vehicles|*|missions|, MAX_ANTS))
	 */
	private void checkAnts(){
		int fitSize = Math.min(scheduleResources.size() * scheduleTasks.size(), MAX_ANTS);
		if (ants == null || ants.size() != fitSize) {
			if(ants != null){
				/*for(OfflineAnt2 a : ants){
					a.reset();
				}*/
				ants.clear();
			}
			ants = new ArrayList<OfflineAnt2>(fitSize);
			// ADD ANTS
			for (int i = 0; i < fitSize; i++) {
				OfflineAnt2 a = new OfflineAnt2();
				ants.add(a);
				log.info("ANT " + a.getID() + " ADDED");
			}
		}
		//	} else {
		//				if (ants.size() > fitSize) {
		//					int dif = ants.size() - fitSize;
		//					for (int i = 0; i < dif; i++) {
		//						OfflineAnt2 a = ants.remove(Terminal.getInstance().getRandom().nextInt(ants.size()));
		//						log.info("ANT " + a.getID() + " REMOVED");
		//					}
		//				}
		//}

		//}
	}

	//	@Override
	//	public void missionStarted(Time t, Mission m, String resourceID) {
	//		// DO NOT RECOMPUTE SOLUTION WHEN THE CURRENT ONE IS GOING ON
	//		// lock.lock();
	//		// graphChanged = true;
	//		// lock.unlock();
	//
	//		// REMOVE MISSION FROM SCHEDULER
	//		Set<MissionStartedHelper> toStartList = null;
	//		if (missionsToStart.containsKey(t))
	//			toStartList = missionsToStart.get(t);
	//		else
	//			toStartList = new HashSet<MissionStartedHelper>();
	//
	//		toStartList.add(new MissionStartedHelper(m, resourceID));
	//		missionsToStart.put(t, toStartList);
	//	}

	@Override
	public void precompute() {
		if (graphChanged || graphChangedByUpdate > 0) {
			long before = System.nanoTime();
			processEvents();
			// GraphChanged may have been set to false in processEvents() sub
			// methods
			if (graphChanged/* ||graphChangedByUpdate>0 */) {
				// new
				// Exception("GO IN PRECOMPUTE : "+graphChanged+" "+graphChangedByUpdate).printStackTrace();
				if (!resources.isEmpty() && !scheduleTasks.isEmpty()) {
					// CHECK ANTS
					checkAnts();

					lock.lock();
					precomputed = true;
					lock.unlock();

					compute();

					lock.lock();
					graphChanged = false;
					lock.unlock();
				}

			}

			lock.lock();
			graphChangedByUpdate = 0;
			lock.unlock();

			long after = System.nanoTime();
			computeTime += (after - before);
		}

	}

	@Override
	public void apply() {
		if (precomputed) {
			long before = System.nanoTime();
			// AFFECT SOLUTION
			 //algo : supprimer toutes les missions non commencées des plans de charge
			 for(StraddleCarrier sc : resources){
				 sc.clearWorkload();
			 }
			 
			Map<String, LocalScore> solutions = best.getSolution();
			for (String colonyID : solutions.keySet()) {
				StraddleCarrier vehicle = vehicles.get(colonyID);
				LocalScore score = solutions.get(colonyID);
				List<ScheduleEdge> path = score.getPath();
				if (path.size() > 0) {
					List<Mission> toAffect = new ArrayList<Mission>(path.size() - 1);
					for (ScheduleEdge e : path) {
						if (e.getNodeTo() != SOURCE_NODE) {
							ScheduleTask<? extends ScheduleEdge> missionNode = e.getNodeTo();
							toAffect.add(missionNode.getMission());
						}
					}
					vehicle.addMissionsInWorkload(toAffect);

				}
			}

			Terminal.getInstance().flushAllocations();

			lock.lock();
			precomputed = false;
			lock.unlock();

			long after = System.nanoTime();
			computeTime += (after - before);

			log.info("AVG Scores : distance= " + stats.getAVG_Distance() + " overspent_time= " + new Time(stats.getAVG_OverspentTime())
			+ " score= " + stats.getAVG_Score());
			log.info("SOLUTION : \n" + best.getSolutionString());
			stats.exportGNUPlot("/home/gaetan/TSPStats.dat");
			log.info("CPT TIME = " + new Time(getComputingTime()));
		}
		sstep++;
	}

	@SuppressWarnings("unchecked")
	private void initSolution() {
		//Useless : sortedSet
		//Collections.sort(pool);

		// CMPTE MAX ETA :
		// MAX TRAVEL TIME
		double sumD = 0.0;
		for (ScheduleEdge e : MissionScheduler.SOURCE_NODE.getDestinations()) {
			if (e.getNodeTo() != e.getNodeFrom()) {
				// System.out.println(e.getID()+" max cost = "+e.getMaxCost());
				sumD += e.getMaxCost();
			}
		}

		for (ScheduleTask<? extends ScheduleEdge> n : MissionScheduler.getInstance().getTasks()) {
			for (ScheduleEdge e : n.getDestinations()) {
				// System.out.println(e.getID()+" max cost = "+e.getMaxCost());
				sumD += e.getMaxCost();
			}
		}
		log.info("DMAX = " + sumD + " t=" + new Time(sumD));

		// MAX LATENESS
		double endS = 0.0;
		for (ScheduleTask<?> n : MissionScheduler.getInstance().getTasks()) {
			double tw = n.getMission().getDeliveryTimeWindow().getMax().getInSec();
			if (tw > endS)
				endS = tw;
		}
		endS *= 2;
		double sumL = 0.0;
		for (ScheduleTask<?> n : MissionScheduler.getInstance().getTasks()) {
			double tP = n.getMission().getPickupTimeWindow().getMax().getInSec();
			double tD = n.getMission().getDeliveryTimeWindow().getMax().getInSec();
			sumL += (endS - tP) + (endS - tD);
		}
		log.info("LMAX=" + sumL + " t=" + new Time(sumL));

		// MAX EARLINESS
		double sumE = 0.0;
		for (ScheduleTask<?> n : MissionScheduler.getInstance().getTasks()) {
			sumE += n.getMission().getDeliveryTimeWindow().getMin().getInSec();
		}
		log.info("EMAX=" + sumE + " t=" + new Time(sumE));

		double maxEta = sumD * parameters.getF1() + sumL * parameters.getF2() + sumE * parameters.getF3();
		log.info("MAX_ETA = " + maxEta);
		parameters.setLambda(maxEta);
	}

	@Override
	public void compute() {
		stats = new OfflineSchedulerStats(getGlobalParameters());
		OfflineAnt2.resetAll();
		best = null;
		//OfflineAnt2.antCounter = ants.size();
		//for(OfflineAnt2 ant : ants){
		//	ant.reset();
		//}
		if (jms == null) {
			execute();
		} else {
			initSolution();
			
			// TODO assert syncSize > 0
			for (int i = 0; i < syncSize; i++) {
				execute();
			}

			double avgPH = OfflineAnt2.sumPH / (OfflineAnt2.phTimes + 0.0);
			double avgAlpha = OfflineAnt2.sumAlpha / (OfflineAnt2.alphaTimes + 0.0);
			double avgBeta = OfflineAnt2.sumBeta / (OfflineAnt2.betaTimes + 0.0);
			double avgGamma = OfflineAnt2.sumGamma / (OfflineAnt2.gammaTimes + 0.0);

			log.info("PH SPREAD :\t" + OfflineAnt2.minPHSpread + "\t|\t" + OfflineAnt2.maxPHSpread + "\t|\t" + avgPH);
			log.info("ALPHA :\t" + OfflineAnt2.minAlpha + "\t|\t" + OfflineAnt2.maxAlpha + "\t|\t" + avgAlpha);
			log.info("BETA :\t" + OfflineAnt2.minBeta + "\t|\t" + OfflineAnt2.maxBeta + "\t|\t" + avgBeta);
			log.info("GAMMA :\t" + OfflineAnt2.minGamma + "\t|\t" + OfflineAnt2.maxGamma + "\t|\t" + avgGamma);

			// if (debug != null) {
			// debug.flush();
			// debug.close();
			// }
			// if (traceScores != null) {
			// traceScores.flush();
			// traceScores.close();
			// }
		}
	}

	/**
	 * Execute 1 step of the ACO algorithm
	 */
	private void execute() {
		long before = System.nanoTime();

		if (MissionScheduler.DEBUG){
			System.err.println("TSP:> Step " + step);
			
		}
		
		currentBest = null;

		OfflineAnt2 currentBestAnt = null;
		
		for (OfflineAnt2 ant : ants) {
			ant.reset();
			ant.compute();
			if (currentBest == null || currentBest.getScore() > ant.getGlobalScore().getScore()) {
				currentBest = ant.getGlobalScore();
				currentBestAnt = ant;
			}
		}

		// TODO
		// TEST ELITIST POLICY -> Only the best ant spreads pheromone
		// SUPER ELITIST POLICY -> Only new global best ant spreads pheromone

		// for(OfflineAnt2 ant : ants){
		// ant.spreadPheromone();
		// }
		if (currentBestAnt != null){
			currentBestAnt.spreadPheromone();
		}

		if (currentBest != null) {
			stats.addScore(new GlobalScore(currentBest));
			// traceScores.append(step + "\t" + currentBest.getScore() + "\n");
			// traceScores.flush();

			if (best == null) {
				best = new GlobalScore(currentBest);
				// currentBestAnt.spreadPheromone();

				log.info("OfflineACO2:> [" + step + "/" + parameters.getSync() + "] NEW RECORD " + best);
				// debug.append(step + "\t" + best.getScore() + "\n");
				// debug.flush();

				// System.out.println("LOCAL AVG Scores : distance= "+statsLocal.getAVG_Distance()+" overspent_time= "+new
				// Time(statsLocal.getAVG_OverspentTime()+"s")+" score= "+statsLocal.getAVG_Score());
			} else {
				if (best.compareTo(currentBest) > 0) {
					best = new GlobalScore(currentBest);

					// currentBestAnt.spreadPheromone();

					log.info("OfflineACO2:> [" + step + "/" + parameters.getSync() + "] NEW RECORD " + best);
					// debug.append(step + "\t" + best.getScore() + "\n");
					// debug.flush();
					// System.out.println("LOCAL AVG Scores : distance= "+statsLocal.getAVG_Distance()+" overspent_time= "+new
					// Time(statsLocal.getAVG_OverspentTime()+"s")+" score= "+statsLocal.getAVG_Score());

				}
			}
		}
		evaporation();
		step++;
		long after = System.nanoTime();
		computeTime += (after - before);
	}

	@SuppressWarnings("unchecked")
	public void evaporation() {
		// Evaporation
		for (ScheduleTask<OfflineEdge2> n : MissionScheduler.getInstance().getTasks()) {
			List<OfflineEdge2> destinations = n.getDestinations();
			for (OfflineEdge2 edge : destinations) {
				edge.evaporate();
			}
		}

		List<OfflineEdge2> l = (List<OfflineEdge2>) MissionScheduler.SOURCE_NODE.getDestinations();
		for (OfflineEdge2 edge : l) {
			edge.evaporate();
		}
	}

	/**
	 * Remove mission
	 * 
	 * @param m
	 *            Mission to remove
	 * @return
	 */
	@Override
	protected synchronized boolean removeMission(Mission m) {
		//System.err.println(TimeScheduler.getInstance().getTime()+" : removing "+m.getId());
		boolean found = super.removeMission(m);
		if(found){
			// Remove the node and concerned edges
			OfflineNode2 n = (OfflineNode2) scheduleTasks.remove(m.getId());

			@SuppressWarnings("unchecked")
			ScheduleTask<OfflineEdge2> source = (ScheduleTask<OfflineEdge2>) SOURCE_NODE;

			// DEPOT -> n
			source.removeEdgeTo(n);
			n.removeEdgeTo(source);
			// n -> n2 && n2 -> n
			for (ScheduleTask<? extends ScheduleEdge> n2 : scheduleTasks.values()) {
				@SuppressWarnings("unchecked")
				ScheduleTask<OfflineEdge2> n3 = (ScheduleTask<OfflineEdge2>) n2;
				n3.removeEdgeTo(n);
				n.removeEdgeTo(n3);
			}
			n.destroy();

			/*for(OfflineAnt2 ant : ants){
				ant.reset();
			}*/
		} else {
			System.err.println("Mission not removed exception!");
		}
		return found;
	}

	/**
	 * Update mission
	 * 
	 * @param m
	 *            Mission to update
	 */
	@Override
	protected void updateMission(Mission m) {
		// TODO
		// WHAT TO DO IN THIS CASE ?
		new Exception("MTSP:> WARNING : UPDATE MISSION CALLED !").printStackTrace();
	}

	/**
	 * Insert a vehicle as a resource in the algorithm.
	 * 
	 * @param rsc
	 *            The straddle carrier to add.
	 */
	@Override
	protected synchronized void insertResource(StraddleCarrier rsc) {
		// TODO ADD ANTS
		String modelID = rsc.getModel().getId();
		String rscID = rsc.getId();

		resources.add(rsc);
		vehicles.put(rscID, rsc);
		if (jms != null)
			jms.addResource(rsc);

		// Compute costs for rsc
		boolean computeCosts = true;
		for (String model : resourceModels.keySet()) {
			if (model.equals(modelID)) {
				computeCosts = false;
				break;
			}
		}

		scheduleResources.put(rscID, new ScheduleResource(rsc));
		List<String> lModel = null;
		if (computeCosts) {
			lModel = new ArrayList<String>();
			for (ScheduleTask<? extends ScheduleEdge> n : scheduleTasks.values()) {
				n.addCostFor(rsc);
			}
		} else {
			lModel = resourceModels.get(modelID);

		}

		lModel.add(rscID);
		resourceModels.put(modelID, lModel);

		log.info("MTSP:> Resource " + rscID + " (" + modelID + ") added");
	}

	/**
	 * Insert mission
	 * 
	 * @param m
	 *            Mission to insert
	 */
	@Override
	protected synchronized void insertMission(Mission m) {
		pool.add(m);

		OfflineNode2 n = new OfflineNode2(m);
		for (String modelID : resourceModels.keySet()) {
			try {
				Path p = getTravelPath(m, m, vehicles.get(resourceModels.get(modelID).get(0)));
				n.addMissionCostFor(modelID, p.getCost());
				n.setDistance(p.getCostInMeters());
			} catch (EmptyResourcesException e) {
				e.printStackTrace();
			}
		}

		for (Mission m2 : pool) {
			if (!m2.getId().equals(m.getId())) {
				OfflineNode2 n2 = (OfflineNode2) scheduleTasks.get(m2.getId());
				OfflineEdge2 e = new OfflineEdge2(n, n2);
				OfflineEdge2 e2 = new OfflineEdge2(n2, n);

				for (String modelID : resourceModels.keySet()) {
					e.addCost(vehicles.get(resourceModels.get(modelID).get(0)));
					n.addDestination(e);
					e2.addCost(vehicles.get(resourceModels.get(modelID).get(0)));
					n2.addDestination(e2);
				}
			}
		}

		OfflineEdge2 e3 = new OfflineEdge2((OfflineNode2) MissionScheduler.SOURCE_NODE, n);
		OfflineEdge2 e4 = new OfflineEdge2(n, (OfflineNode2) MissionScheduler.SOURCE_NODE);

		for (String vehicleID : vehicles.keySet()) {
			e3.addCost(vehicles.get(vehicleID));
			((OfflineNode2) (MissionScheduler.SOURCE_NODE)).addDestination(e3);
			e4.addCost(vehicles.get(vehicleID));
			n.addDestination(e4);
		}

		scheduleTasks.put(m.getId(), n);
		log.info("MTSP:> Task " + m.getId() + " added");
	}

	/**
	 * Setter on the global parameters of the ACO algorithm
	 * 
	 * @param p
	 */
	public void setGlobalParameters(OfflineSchedulerParameters p) {
		parameters = p;
	}

	@SuppressWarnings("unchecked")
	public static List<OfflineNode2> getNodes() {
		ArrayList<OfflineNode2> l = new ArrayList<OfflineNode2>(MissionScheduler.getInstance().getTasks().size());
		for (ScheduleTask<? extends ScheduleEdge> t : MissionScheduler.getInstance().getTasks()) {
			OfflineNode2 n = (OfflineNode2) t;
			l.add(n);
		}
		return l;
	}

	public static ScheduleResource getASalesman() {
		//List<ScheduleResource> scheduleResources2 = MissionScheduler.getInstance().getScheduleResources();
		//return scheduleResources2.get(Terminal.getInstance().getRandom().nextInt(scheduleResources2.size()));
		return MissionScheduler.getInstance().getScheduleResources().iterator().next();
	}

	public static ScheduleResource getASalesman(Set<String> alreadyUsed) {
		Iterator<ScheduleResource> it = MissionScheduler.getInstance().getScheduleResources().iterator();
		while (it.hasNext()) {
			ScheduleResource res = it.next();
			if (!alreadyUsed.contains(res.getID()))
				return res;
		}
		return null;
	}

	public OfflineSchedulerParameters getGlobalParameters() {
		return parameters;
	}
}