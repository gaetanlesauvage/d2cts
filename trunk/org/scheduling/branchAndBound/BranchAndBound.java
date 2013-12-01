/*
 * This file is part of D²CTS : Dynamic and Distributed Container Terminal Simulator.
 *
 * Copyright (C) 2009-2012  Gaëtan Lesauvage
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package org.scheduling.branchAndBound;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.StringTokenizer;
import java.util.concurrent.locks.ReentrantLock;

import org.com.model.scheduling.BranchAndBoundParametersBean;
import org.missions.Load;
import org.missions.Mission;
import org.missions.MissionPhase;
import org.missions.MissionState;
import org.missions.Workload;
import org.scheduling.MissionScheduler;
import org.scheduling.aco.graph.DepotNode;
import org.scheduling.display.JMissionScheduler;
import org.system.Terminal;
import org.time.Time;
import org.time.TimeScheduler;
import org.time.TimeWindow;
import org.vehicles.StraddleCarrier;
import org.vehicles.models.SpeedCharacteristics;

/**
 * Branch and bound algorithm used to get the best solution of the mission
 * scheduling problem
 * 
 * @author Ga&euml;tan Lesauvage
 * @since 2012
 */
public class BranchAndBound extends MissionScheduler {
	/**
	 * RMI BINDING NAME
	 */
	public static final String rmiBindingName = "BranchAndBound";

	/**
	 * If TRUE then the costs (time and distance) are computed by the simulator
	 * and stored in the matrix files
	 */
	private boolean evalCosts = false;

	/**
	 * Used to get each resource
	 */
	private int resourceIndex = 0;

	// BRANCH AND BOUND
	/**
	 * Max bound in distance
	 */
	// private double dMax;
	/**
	 * Min bound in distance (best score)
	 */
	private double dMin;
	/**
	 * Max bound in overspent time steps
	 */
	// private long overspentMaxTime;
	/**
	 * Min bound in overspent time steps (best score)
	 */
	private long overspentMinTime;

	private long overrunMinPenalty;
	/**
	 * Best solution
	 */
	private Solution bestSolution;

	/**
	 * Costs in distance and time
	 */
	Costs costs;

	private Long evalNodes = new Long(0);

	/*
	 * MissionID , <ResourceID , Time>
	 */
	private Map<String, HashMap<String, Time>> missionsStartTime;

	private boolean recompute = true;
	
	@Override
	public String getId() {
		return BranchAndBound.rmiBindingName;
	}

	public static void closeInstance(){

	}

	// CONSTRUCTORS
	/**
	 * Default constructor
	 * 
	 */
	public BranchAndBound() {
		super();
		MissionScheduler.instance = this;
		if(!init)
			init();
	}

	private Solution readSolution(String filename) throws FileNotFoundException {
		File f = new File(filename);
		if (!f.exists()) {
			f.getParentFile().mkdirs();
			try {
				f.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		Scanner scan = new Scanner(f);
		Solution root = new Solution(null, "", DepotNode.ID);
		while (scan.hasNextLine()) {
			String line = scan.nextLine();
			StringTokenizer st = new StringTokenizer(line);
			String resource = st.nextToken();
			String task = st.nextToken();
			root = new Solution(root, resource, task);
			root.compute();
		}
		scan.close();
		return root;
	}

	private void writeSolution(Solution toWrite, String fileName)
			throws FileNotFoundException {
		String s = "";
		while (toWrite.parent != null) {
			s = toWrite.resource + "\t" + toWrite.task + "\n" + s;
			toWrite = toWrite.parent;
		}
		PrintWriter pw = new PrintWriter(fileName);
		pw.append(s);
		pw.flush();
		pw.close();

	}

	/**
	 * Add tasks and resources and compute or read the matrix files
	 */
	@Override
	protected void init() {
		lock = new ReentrantLock();
		missionsStartTime = new HashMap<String, HashMap<String, Time>>();

		step = TimeScheduler.getInstance().getStep() + 1;
		sstep = step;
		for (String s : Terminal.getInstance().getStraddleCarriersName()) {
			StraddleCarrier rsc = Terminal.getInstance().getStraddleCarrier(s);
			addResource(new Time(step), rsc);
		}
		for (String s : Terminal.getInstance().getMissionsName()) {
			Mission m = Terminal.getInstance().getMission(s);
			addMission(new Time(step), m);
		}

		jms = new JMissionScheduler();
		for (StraddleCarrier rsc : resources) {
			jms.addResource(rsc);
		}

		costs = new Costs();
		try {
			if (evalCosts) {
				costs.compute();
				costs.save(BranchAndBoundParametersBean.TIME_MATRIX_FILE.getValueAsString(), 
						BranchAndBoundParametersBean.DISTANCE_MATRIX_FILE.getValueAsString());
			} else {
				costs.load(BranchAndBoundParametersBean.TIME_MATRIX_FILE.getValueAsString(), 
						BranchAndBoundParametersBean.DISTANCE_MATRIX_FILE.getValueAsString());
				/*
				 * PrintWriter pw = new PrintWriter("/home/gaetan/test.dat");
				 * pw.append(costs.toString()); pw.flush();
				 */
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		// COMPUTE START TIME
		for (Mission m : pool) {
			HashMap<String, Time> start = new HashMap<String, Time>(
					resources.size());

			for (StraddleCarrier rsc : resources) {
				String rscID = rsc.getId();
				start.put(rscID, computeMissionStartTime(m, rscID));
			}
			missionsStartTime.put(m.getId(), start);
		}

		Solution.setBranchAndBound(this);
		recompute = true;
	}

	@Override
	public boolean apply() {
		boolean returnCode = NOTHING_CHANGED;
		step++;
		sstep++;
		if(precomputed){
			precomputed = false;
			returnCode = SOMETHING_CHANGED;
		}
		return returnCode;
	}

	@Override
	public void precompute() {
		if (graphChanged || graphChangedByUpdate > 0) {
			processEvents();
			lock.lock();
			graphChanged = false;
			graphChangedByUpdate = 0;
			lock.unlock();
		}

		if (recompute && !resources.isEmpty() && !pool.isEmpty()) {
			// COMPUTE ALGORITHM
			compute();
		}
	}

	@Override
	public void addMission(Time t, Mission m) {
		pool.add(m);
		if (costs != null) {
			HashMap<String, Time> start = new HashMap<String, Time>();

			for (StraddleCarrier rsc : resources) {
				String rscID = rsc.getId();
				start.put(rscID, computeMissionStartTime(m, rscID));
			}
			missionsStartTime.put(m.getId(), start);
		}
		recompute = true;
	}

	@Override
	public boolean removeMission(Time t, Mission m) {

		missionsStartTime.remove(m.getId());
		return removeMission(m);
		/*for (int i = 0; i < pool.size(); i++) {
			if (m.getId().equals(pool.get(i).getId())) {
				pool.remove(i);
				missionsStartTime.remove(m.getId());
				return true;
			}
		}
		return false;*/
	}

	@Override
	public void addResource(Time t, StraddleCarrier rsc) {
		resources.add(rsc);
		if (costs != null) {
			String rscID = rsc.getId();

			for (Mission m : pool) {
				HashMap<String, Time> start = missionsStartTime.get(m.getId());
				start.put(rscID, computeMissionStartTime(m, rscID));
			}
		}
		recompute = true;
	}

	@Override
	public boolean removeResource(Time t, StraddleCarrier rsc) {
		String rscID = rsc.getId();
		for (int i = 0; i < resources.size(); i++) {
			if (resources.get(i).getId().equals(rscID)) {
				resources.remove(i);
				for (Mission m : pool) {
					missionsStartTime.get(m.getId()).remove(rscID);
				}
				return true;
			}
		}
		return false;
	}

	/**
	 * Return each resource in the same order at each time
	 * 
	 * @return a resource
	 */
	private StraddleCarrier pickAStraddleCarrier() {
		StraddleCarrier rsc = resources.get(resourceIndex);
		resourceIndex++;
		if (resourceIndex == resources.size())
			resourceIndex = 0;
		return rsc;
	}

	@Override
	public void compute() {
		precomputed = true;
		System.out.println("COMPUTE : " + resources.size() + " ; "
				+ pool.size());
		if (BranchAndBoundParametersBean.SOLUTION_INIT_FILE.getValueAsString().equals("")) {
			// Compute max bound
			HashMap<String, List<String>> boundSolution = new HashMap<String, List<String>>();
			//int missionAffected = 0;
			Iterator<Mission> itMissions = pool.iterator();
			while (itMissions.hasNext()) {
				StraddleCarrier rsc = pickAStraddleCarrier();
				if (rsc.isAvailable()) {
					Mission m = itMissions.next();
					List<String> l = boundSolution.get(rsc.getId());
					if (l == null)
						l = new ArrayList<String>();
					l.add(m.getId());
					boundSolution.put(rsc.getId(), l);
					//	missionAffected++;
				}
			}
			// Eval bound
			bestSolution = new Solution(null, "", DepotNode.ID);
			for (String res : boundSolution.keySet()) {
				for (String addedMission : boundSolution.get(res)) {
					bestSolution = new Solution(bestSolution, res, addedMission);
					bestSolution.compute();
				}
			}
		} else {
			try {
				System.out.println("READ SOLUTION");
				bestSolution = readSolution(BranchAndBoundParametersBean.SOLUTION_INIT_FILE.getValueAsString());
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
		}
		dMin = bestSolution.distance;
		overspentMinTime = bestSolution.overspentTime.toStep();
		overrunMinPenalty = bestSolution.overrunPenalties;

		if (!BranchAndBoundParametersBean.SOLUTION_INIT_FILE.getValueAsString().equals(BranchAndBoundParametersBean.SOLUTION_FILE.getValueAsString())) {
			// Must have found a solution
			System.out.println("BOUND : " + dMin + " && "
					+ new Time(overspentMinTime) + " && "
					+ new Time(overrunMinPenalty) + " SOLUTION :\n"
					+ bestSolution);

			// Now compute B&B with the found max bound
			Solution currentSolution = new Solution(null, "", DepotNode.ID);
			long tBefore = System.nanoTime();
			computeBB(currentSolution);

			long tAfter = System.nanoTime();
			computeTime += tAfter - tBefore;
			Time timeCost = new Time(((tAfter - tBefore) / 1000000000.0));
			System.out.println("BEST SOLUTION FOUND : (" + dMin + " | "
					+ new Time(overspentMinTime) + " | "
					+ new Time(overrunMinPenalty) + ")\n" + bestSolution
					+ "\n (compute time : " + timeCost + " evaluated nodes : "
					+ evalNodes + ")");
			if (!BranchAndBoundParametersBean.SOLUTION_FILE.getValueAsString().equals("")) {
				try {
					System.out.println("WRITE SOLUTION");
					writeSolution(bestSolution, BranchAndBoundParametersBean.SOLUTION_FILE.getValueAsString());
					System.out.println("BEST SOLUTION FOUND : (" + dMin + " | "
							+ new Time(overspentMinTime) + " | "
							+ new Time(overrunMinPenalty) + ")\n"
							+ bestSolution + "\n (compute time : " + timeCost
							+ " evaluated nodes : " + evalNodes + ")");
					for (StraddleCarrier rsc : resources) {
						System.out.println("BEST SOLUTION " + rsc.getId()
								+ " :"
								+ bestSolution.toResourceString(rsc.getId()));
					}
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				}
			}
		} else {
			System.out.println("BEST SOLUTION FOUND : (" + dMin + " | "
					+ new Time(overspentMinTime) + " | "
					+ new Time(overrunMinPenalty) + ")\n" + bestSolution);
		}

		HashMap<String, List<String>> mSolution = getSolution(bestSolution);
		for (StraddleCarrier rsc : resources) {

			if (rsc.isAvailable()) {
				List<String> sol = mSolution.get(rsc.getId());
				if (sol != null) {
					List<Mission> toAdd = new ArrayList<Mission>(sol.size());
					for (String mID : sol) {
						Mission m = removeMission(mID);
						toAdd.add(m);

					}
					rsc.addMissionsInWorkload(toAdd);
				}
			}
		}
		recompute = false;
		Terminal.getInstance().flushAllocations();
	}

	/**
	 * Remove the corresponding mission of the pool of tasks
	 * 
	 * @param mID
	 *            Mission ID
	 * @return The removed mission
	 */
	private Mission removeMission(String mID) {
		Mission m = Terminal.getInstance().getMission(mID);
		if( pool.remove(m)){ 
			return m;
		}
		else{
			return null;
		}
		/*for (int i = 0; i < pool.size(); i++) {
			if (pool.get(i).getId().equals(mID))
				return pool.remove(i);
		}
		return null;*/
	}

	/**
	 * Branch and bound main method It works recursively. At first the root
	 * solution node should be used as current solution. Then the children
	 * solutions are computed and sorted according to their score. For each
	 * child solution if it is not upper the max bound then the method is called
	 * with this solution as current solution Else if it is upper the max bound
	 * then the search is stopped on this branch. If the child solution is lower
	 * the min bound then the max bound is set to the new min bound and the best
	 * solution is updated
	 * 
	 * @param currentSolution
	 *            @
	 */
	private void computeBB(Solution currentSolution) {
		lock.lock();
		evalNodes++;
		lock.unlock();

		boolean display = false;

		List<Solution> children = currentSolution.getChildren();

		if (currentSolution.parent == null) {
			System.out.println("ROOT");
			display = true;
		}

		List<Thread> threads = null;
		if (currentSolution.level == 1)
			threads = new ArrayList<Thread>(children.size());

		// No sort
		// Collections.sort(children);

		int childCount = 1;
		for (final Solution s : children) {
			if (display)
				System.out.println("CHILD n°" + childCount + "/"
						+ children.size());

			childCount++;
			long sop = s.overrunPenalties;
			if (sop <= overrunMinPenalty) {
				// One critera then the other one...
				if (s.isLeaf()
						&& ((sop == overrunMinPenalty && s.distance < dMin) || (sop < overrunMinPenalty))) {
					// New record
					bestSolution = s;
					dMin = s.distance;
					overspentMinTime = s.overspentTime.toStep();
					overrunMinPenalty = sop;

					String sCOST = "";
					Solution tmp = bestSolution;
					while (tmp != null) {
						sCOST += "(" + tmp.resource + "@" + tmp.task + " "
								+ tmp.distance + ")\t";
						tmp = tmp.parent;
					}
					System.out.println("NEW RECORD : " + dMin + " | "
							+ new Time(overspentMinTime) + " | "
							+ new Time(overrunMinPenalty) + " :" + bestSolution
							+ "\n=> " + sCOST);
				} else {
					// CANNOT UPDATE MAX BOUND HERE (partial solution) but can
					// prevent from increasing the distance over the bound
					if (!s.isLeaf()
							&& (sop < overrunMinPenalty || (sop == overrunMinPenalty && s.distance < dMin))) {
						// Keep searching
						if (threads != null) {
							Thread tChild = new Thread("child_"
									+ (childCount - 1) + "/" + children.size()) {
								public void run() {
									computeBB(s);
								}
							};
							threads.add(tChild);
							tChild.start();
						} else
							computeBB(s);
					}
				}
			}
			// else STOP SEARCHING
		}

		if (threads != null) {
			for (Thread t : threads) {
				try {
					t.join();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}

	@Override
	public void updateMission(Time t, Mission m) {
		// TODO
	}

	@Override
	public void incrementNumberOfCompletedMissions(final String resourceID) {
		boolean terminated = true;
		lookup: for (StraddleCarrier rsc : resources) {
			Workload w = rsc.getWorkload();
			for (Load l : w.getLoads()) {
				if (l.getState() != MissionState.STATE_ACHIEVED) {
					terminated = false;
					break lookup;
				}

			}
		}
		if (terminated)
			TimeScheduler.getInstance().computeEndTime();

		super.incrementNumberOfCompletedMissions(resourceID);
	}

	/**
	 * Get the distance from the given leaf to the root solution node
	 * 
	 * @param leaf
	 *            Leaf of the solution to eval
	 * @return The distance from the given leaf to the root solution node
	 */
	public double getDistance(Solution leaf) {
		Solution tmp = leaf.parent;
		double d = leaf.distance - tmp.distance;

		while (tmp.parent != null) {
			double d2 = tmp.distance - tmp.parent.distance;
			d += d2;
			tmp = tmp.parent;
		}
		return d;
	}

	/**
	 * Get the solution from the root node to the given leaf node
	 * 
	 * @param leaf
	 *            The leaf of the solution
	 * @return The solution for each resource
	 */
	public static HashMap<String, List<String>> getSolution(Solution leaf) {
		return getSolution(leaf, new HashMap<String, List<String>>());
	}

	/**
	 * Recursively get the solution
	 * 
	 * @param leaf
	 *            Leaf of the solution path in the tree
	 * @param solution
	 *            The solution for each resource
	 * @return The solution for each resource
	 */
	private static HashMap<String, List<String>> getSolution(Solution leaf,
			HashMap<String, List<String>> solution) {
		if (leaf.parent == null)
			return solution;

		List<String> l = null;
		if (solution.containsKey(leaf.resource)) {
			l = solution.get(leaf.resource);
		} else {
			l = new ArrayList<String>();
		}

		l.add(0, leaf.task);
		solution.put(leaf.resource, l);

		return getSolution(leaf.parent, solution);
	}

	public Time getContainerHandlingTime(String task, String resource, MissionPhase phase) {

		if (task.equals(DepotNode.ID))
			return new Time(0);

		// Mission m = getMission(task);
		StraddleCarrier rsc = Terminal.getInstance().getStraddleCarrier(
				resource);
		Time handlingTime = null;
		SpeedCharacteristics speed = rsc.getModel().getSpeedCharacteristics();

		double d1 = (speed.getContainerHandlingTimeFromGroundMAX() + speed
				.getContainerHandlingTimeFromGroundMIN()) / 2.0;
		double d2 = (speed.getContainerHandlingTimeFromTruckMAX() + speed
				.getContainerHandlingTimeFromTruckMIN()) / 2.0;
		handlingTime = new Time(((d1 + d2) / 2.0));
		return handlingTime;
	}

	public Time getMissionStartTime(String task, String resource) {
		return missionsStartTime.get(task).get(resource);
	}

	private Time computeMissionStartTime(Mission m, String resourceID) {
		Time startTime = null;
		TimeWindow tw = m.getPickupTimeWindow();
		startTime = new Time(tw.getMin(), costs.getCosts(DepotNode.ID,
				m.getId()).getCostInTime(resourceID), false);
		return startTime;
	}
}