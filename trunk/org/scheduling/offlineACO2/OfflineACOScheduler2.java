package org.scheduling.offlineACO2;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.com.model.scheduling.OfflineACO2ParametersBean;
import org.exceptions.EmptyResourcesException;
import org.exceptions.MissionNotFoundException;
import org.graphstream.graph.Edge;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.DefaultGraph;
import org.missions.Load;
import org.missions.Mission;
import org.missions.MissionState;
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
import org.time.Time;
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

	private static List<OfflineAnt2> ants;
	private static OfflineSchedulerParameters parameters;
	private GlobalScore currentBest;
	// private OfflineAnt2 currentBestAnt;
	private static final int MAX_ANTS = 12; // SET TO 1 FOR DEBUG
	private static int syncSize;
	private OfflineSchedulerStats stats;

	public OfflineACOScheduler2() {
		super();
		if (parameters == null) {
			parameters = OfflineACO2ParametersBean.getParameters();
			evalParameters = OfflineACO2ParametersBean.getEvalParameters();
			syncSize = parameters.getSync();
			System.out.println("MTSP ACO PARAMETERS: " + parameters + "\nEVAL PARAMETERS: " + MissionScheduler.getEvalParameters());
		}
		
		MissionScheduler.instance = this;
		if(!init)
			init();
	}

	@Override
	protected void init() {
		init = true;
		
		// DEBUG
		try {
			debug = new PrintWriter(new File("/home/gaetan/debug.dat/"));
			traceScores = new PrintWriter(new File(
					"/home/gaetan/traceScores.dat/"));
		} catch (FileNotFoundException e2) {
			e2.printStackTrace();
		}

		System.out.println("OfflineACO2 Mission Scheduler parameters : "
				+ getGlobalParameters());

		computeTime = 0;
		step = 0;
		sstep = rts.getStep() + 1;

		lock.lock();
		graphChanged = true;
		lock.unlock();

		SOURCE_NODE = new OfflineNode2((Mission) null);

		jms = new JMissionScheduler();
		for (StraddleCarrier rsc : resources) {
			jms.addResource(rsc);
		}
		if (out != null) {
			out.println("MissionGraph created !");
		} else
			System.out.println("MissionGraph created !");

		int current = 1;
		List<StraddleCarrier> lResources = rt.getStraddleCarriers();
		int overall = lResources.size();
		for (StraddleCarrier rsc : lResources) {
			System.out.println("Adding resource " + rsc.getId() + " ("
					+ current + "/" + overall + ")");
			current++;
			insertResource(rsc);
		}
		current = 1;
		List<Mission> lTasks = rt.getMissions();
		overall = lTasks.size();
		for (Mission m : lTasks) {
			System.out.println("Adding task " + m.getId() + " (" + current
					+ "/" + overall + ")");
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
			List<? extends ScheduleEdge> dests = MissionScheduler.getInstance().SOURCE_NODE
					.getDestinations();
			for (ScheduleEdge e : dests) {
				graph.addEdge(e.getID(), e.getNodeFrom().getID(), e.getNodeTo()
						.getID(), true);
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
					System.out
							.println("\te=" + e.getID() + " t2=" + t2.getID());
					Edge de = graph.addEdge(e.getID(), t.getID(), t2.getID(),
							true);
					// de.addAttribute("ui.label", e.get)
				}
			}
			graph.display();
		}

	}

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
				if (scheduleTasks.size() > 0) {
					// CHECK ANTS
					int fitSize = Math.min(scheduleResources.size()
							* scheduleTasks.size(), MAX_ANTS);
					if (ants == null || ants.size() != fitSize) {
						if (ants == null)
							ants = new ArrayList<OfflineAnt2>(Math.min(
									MAX_ANTS, scheduleResources.size()
											* scheduleTasks.size()));
						if (ants.size() < fitSize) {
							// ADD ANTS
							for (int i = ants.size(); i < fitSize; i++) {
								OfflineAnt2 a = new OfflineAnt2();
								ants.add(a);
								System.err.println("ANT " + a.getID()
										+ " ADDED");
							}
						} else {
							if (ants.size() > fitSize) {
								int dif = ants.size() - fitSize;
								for (int i = 0; i < dif; i++) {
									OfflineAnt2 a = ants.remove(RANDOM
											.nextInt(ants.size()));
									System.err.println("ANT " + a.getID()
											+ " REMOVED");
								}
							}
						}

					}
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
			// System.out.println("APPLY : ");
			// AFFECT SOLUTION
			HashMap<String, LocalScore> solutions = best.getSolution();
			for (String colonyID : solutions.keySet()) {
				StraddleCarrier vehicle = vehicles.get(colonyID);

				// TO REMOVE
				List<String> toRemove = new ArrayList<String>();
				for (Load l : vehicle.getWorkload().getLoads()) {
					if (l.getState() == MissionState.STATE_TODO) {
						toRemove.add(l.getMission().getId());
					}
				}

				for (String mID : toRemove) {
					try {
						vehicle.removeMissionInWorkload(mID);
					} catch (MissionNotFoundException e1) {
						// ALREADY REMOVED
					}
				}
			}
			for (String colonyID : solutions.keySet()) {
				StraddleCarrier vehicle = vehicles.get(colonyID);
				LocalScore score = solutions.get(colonyID);
				List<ScheduleEdge> path = score.getPath();
				if (path.size() > 0) {
					List<Mission> toAffect = new ArrayList<Mission>(
							path.size() - 1);
					for (ScheduleEdge e : path) {
						if (e.getNodeTo() != SOURCE_NODE) {
							ScheduleTask<? extends ScheduleEdge> missionNode = e
									.getNodeTo();
							toAffect.add(missionNode.getMission());
						}
					}
					vehicle.addMissionsInWorkload(toAffect);

				}
			}

			rt.flushAllocations();

			lock.lock();
			precomputed = false;
			lock.unlock();

			long after = System.nanoTime();
			computeTime += (after - before);

			System.out.println("AVG Scores : distance= "
					+ stats.getAVG_Distance() + " overspent_time= "
					+ new Time(stats.getAVG_OverspentTime()) + " score= "
					+ stats.getAVG_Score());
			System.out.println("SOLUTION : \n" + best.getSolutionString());
			stats.exportGNUPlot("/home/gaetan/TSPStats.dat");
			System.out.println("CPT TIME = "
					+ new Time(getComputingTime()));
		}
		sstep++;
	}

	private void initSolution() {
		Collections.sort(pool);

		// CMPTE MAX ETA :
		// MAX TRAVEL TIME
		double sumD = 0.0;
		for (ScheduleEdge e : MissionScheduler.getInstance().SOURCE_NODE
				.getDestinations()) {
			if (e.getNodeTo() != e.getNodeFrom()) {
				// System.out.println(e.getID()+" max cost = "+e.getMaxCost());
				sumD += e.getMaxCost();
			}
		}

		for (ScheduleTask<? extends ScheduleEdge> n : MissionScheduler
				.getInstance().getTasks()) {
			for (ScheduleEdge e : n.getDestinations()) {
				// System.out.println(e.getID()+" max cost = "+e.getMaxCost());
				sumD += e.getMaxCost();
			}
		}
		System.out.println("DMAX = " + sumD + " t=" + new Time(sumD));

		// MAX LATENESS
		double endS = 0.0;
		for (ScheduleTask<?> n : MissionScheduler.getInstance().getTasks()) {
			double tw = n.getMission().getDeliveryTimeWindow().getMax()
					.getInSec();
			if (tw > endS)
				endS = tw;
		}
		endS *= 2;
		double sumL = 0.0;
		for (ScheduleTask<?> n : MissionScheduler.getInstance().getTasks()) {
			double tP = n.getMission().getPickupTimeWindow().getMax()
					.getInSec();
			double tD = n.getMission().getDeliveryTimeWindow().getMax()
					.getInSec();
			sumL += (endS - tP) + (endS - tD);
		}
		System.out.println("LMAX=" + sumL + " t=" + new Time(sumL));

		// MAX EARLINESS
		double sumE = 0.0;
		for (ScheduleTask<?> n : MissionScheduler.getInstance().getTasks()) {
			sumE += n.getMission().getDeliveryTimeWindow().getMin().getInSec();
		}
		System.out.println("EMAX=" + sumE + " t=" + new Time(sumE));

		double maxEta = sumD * parameters.getF1() + sumL * parameters.getF2()
				+ sumE * parameters.getF3();
		System.out.println("MAX_ETA = " + maxEta);
		parameters.setLambda(maxEta);

		/*
		 * GlobalScore gs = new GlobalScore(); for(ScheduleResource res :
		 * scheduleResources.values()) gs.addScore(new LocalScore(res));
		 * 
		 * Iterator<String> resIDIterator =
		 * gs.getSolution().keySet().iterator(); for(Mission m : pool){
		 * if(!resIDIterator.hasNext()) { resIDIterator =
		 * gs.getSolution().keySet().iterator(); } String resID =
		 * resIDIterator.next();
		 * 
		 * ScheduleTask<? extends ScheduleEdge> t =
		 * scheduleTasks.get(m.getId()); ScheduleResource r =
		 * scheduleResources.get(resID); LocalScore currentScore =
		 * gs.getSolution().get(resID);
		 * gs.addScore(r.simulateVisitNode(currentScore, (OfflineNode2)t)); }
		 * System
		 * .out.println("After initializing solution best score = "+gs.getDistance
		 * ()+" | "+new Time(gs.getDistanceInSeconds()+"s")+" | "+new
		 * Time(gs.getOverspentTime()+"s")+" | "+new
		 * Time(gs.getWaitTime()+"s")+" | "
		 * +gs.getScore()+" => \n"+gs.getSolutionString()); best = gs;
		 * traceScores.append(step+"\t"+best.getScore()+"\n");
		 * traceScores.flush(); //for(int i=0; i<100; i++)
		 * OfflineAnt2.spreadPheromone(best); for(int i = 0 ; i<ants.size() ;
		 * i++) OfflineAnt2.spreadPheromone(best);
		 */
	}

	@Override
	public void compute() {
		stats = new OfflineSchedulerStats(getGlobalParameters());

		if (jms == null) {
			execute();
		} else {
			initSolution();
			// TODO assert syncSize > 0
			for (int i = 0; i < syncSize; i++) {
				execute();
			}

			double avgPH = OfflineAnt2.sumPH / (OfflineAnt2.phTimes + 0.0);
			double avgAlpha = OfflineAnt2.sumAlpha
					/ (OfflineAnt2.alphaTimes + 0.0);
			double avgBeta = OfflineAnt2.sumBeta
					/ (OfflineAnt2.betaTimes + 0.0);
			double avgGamma = OfflineAnt2.sumGamma
					/ (OfflineAnt2.gammaTimes + 0.0);

			System.out.println("PH SPREAD :\t" + OfflineAnt2.minPHSpread
					+ "\t|\t" + OfflineAnt2.maxPHSpread + "\t|\t" + avgPH);
			System.out.println("ALPHA :\t" + OfflineAnt2.minAlpha + "\t|\t"
					+ OfflineAnt2.maxAlpha + "\t|\t" + avgAlpha);
			System.out.println("BETA :\t" + OfflineAnt2.minBeta + "\t|\t"
					+ OfflineAnt2.maxBeta + "\t|\t" + avgBeta);
			System.out.println("GAMMA :\t" + OfflineAnt2.minGamma + "\t|\t"
					+ OfflineAnt2.maxGamma + "\t|\t" + avgGamma);

			if (debug != null) {
				debug.flush();
				debug.close();
			}
			if (traceScores != null) {
				traceScores.flush();
				traceScores.close();
			}
		}
	}

	/**
	 * Execute 1 step of the ACO algorithm
	 */
	private void execute() {
		long before = System.nanoTime();
		if (MissionScheduler.DEBUG)
			System.err.println("TSP:> Step " + step);
		currentBest = null;

		OfflineAnt2 currentBestAnt = null;

		for (OfflineAnt2 ant : ants) {
			ant.compute();
			if (currentBest == null
					|| currentBest.getScore() > ant.getGlobalScore().getScore()) {
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
		if (currentBestAnt != null)
			currentBestAnt.spreadPheromone();

		if (currentBest != null) {
			stats.addScore(new GlobalScore(currentBest));
			traceScores.append(step + "\t" + currentBest.getScore() + "\n");
			traceScores.flush();

			if (best == null) {
				best = new GlobalScore(currentBest);
				// currentBestAnt.spreadPheromone();

				System.out.println("OfflineACO2:> [" + step + "/"
						+ parameters.getSync() + "] NEW RECORD " + best);
				debug.append(step + "\t" + best.getScore() + "\n");
				debug.flush();

				// System.out.println("LOCAL AVG Scores : distance= "+statsLocal.getAVG_Distance()+" overspent_time= "+new
				// Time(statsLocal.getAVG_OverspentTime()+"s")+" score= "+statsLocal.getAVG_Score());
			} else {
				if (best.compareTo(currentBest) > 0) {
					best = new GlobalScore(currentBest);

					// currentBestAnt.spreadPheromone();

					System.out.println("OfflineACO2:> [" + step + "/"
							+ parameters.getSync() + "] NEW RECORD " + best);
					debug.append(step + "\t" + best.getScore() + "\n");
					debug.flush();
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

	public void evaporation() {
		// Evaporation
		for (ScheduleTask<OfflineEdge2> n : MissionScheduler.getInstance()
				.getTasks()) {
			List<OfflineEdge2> destinations = n.getDestinations();
			for (OfflineEdge2 edge : destinations) {
				edge.evaporate();
			}
		}
		@SuppressWarnings("unchecked")
		List<OfflineEdge2> l = (List<OfflineEdge2>) MissionScheduler
				.getInstance().SOURCE_NODE.getDestinations();
		for (OfflineEdge2 edge : l) {
			edge.evaporate();
		}
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
			for (ScheduleTask<? extends ScheduleEdge> n : scheduleTasks
					.values()) {
				n.addCostFor(rsc);
			}
		} else {
			lModel = resourceModels.get(modelID);

		}

		lModel.add(rscID);
		resourceModels.put(modelID, lModel);

		System.out.println("MTSP:> Resource " + rscID + " (" + modelID
				+ ") added");
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
				Path p = getTravelPath(m, m,
						vehicles.get(resourceModels.get(modelID).get(0)));
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

		OfflineEdge2 e3 = new OfflineEdge2(
				(OfflineNode2) MissionScheduler.getInstance().SOURCE_NODE, n);
		OfflineEdge2 e4 = new OfflineEdge2(n,
				(OfflineNode2) MissionScheduler.getInstance().SOURCE_NODE);

		for (String vehicleID : vehicles.keySet()) {
			e3.addCost(vehicles.get(vehicleID));
			((OfflineNode2) (MissionScheduler.getInstance().SOURCE_NODE))
					.addDestination(e3);
			e4.addCost(vehicles.get(vehicleID));
			n.addDestination(e4);
		}

		scheduleTasks.put(m.getId(), n);
		System.out.println("MTSP:> Task " + m.getId() + " added");
	}

	/**
	 * Setter on the global parameters of the ACO algorithm
	 * 
	 * @param p
	 */
	public static void setGlobalParameters(OfflineSchedulerParameters p) {
		parameters = p;
	}

	public static List<OfflineNode2> getNodes() {
		ArrayList<OfflineNode2> l = new ArrayList<OfflineNode2>(
				MissionScheduler.getInstance().getTasks().size());
		for (ScheduleTask<? extends ScheduleEdge> t : MissionScheduler
				.getInstance().getTasks()) {
			OfflineNode2 n = (OfflineNode2) t;
			l.add(n);
		}
		return l;
	}

	public static ScheduleResource getASalesman() {
		return MissionScheduler.getInstance().getScheduleResources().iterator()
				.next();
	}

	public static ScheduleResource getASalesman(List<String> alreadyUsed) {
		Iterator<ScheduleResource> it = MissionScheduler.getInstance()
				.getScheduleResources().iterator();
		while (it.hasNext()) {
			ScheduleResource res = it.next();
			if (!alreadyUsed.contains(res.getID()))
				return res;
		}
		return null;
	}

	public static OfflineSchedulerParameters getGlobalParameters() {
		return parameters;
	}
}
