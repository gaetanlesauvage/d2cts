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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.missions.Mission;
import org.missions.MissionPhase;
import org.scheduling.MissionScheduler;
import org.scheduling.aco.graph.DepotNode;
import org.system.Terminal;
import org.time.Time;
import org.vehicles.StraddleCarrier;

/**
 * Solution of the scheduling problem (node in the solutions tree)
 * 
 * @author Ga&euml;tan Lesauvage
 * @since 2012
 */
class Solution implements Comparable<Solution> {
	/**
	 * Branch and bound
	 */
	private static BranchAndBound branchAndBound;

	/**
	 * Level of the solution in the tree
	 */
	int level;
	/**
	 * Resource allocated to the task
	 */
	String resource;
	/**
	 * Task allocated to the resource
	 */
	String task;

	// Hierarchy
	/**
	 * Parent solution in the tree
	 */
	Solution parent;
	/**
	 * Previous task allocated to the resource in the tree
	 */
	String previousTask;

	// Time handling
	/**
	 * Time before executing the task for each resource
	 */
	private HashMap<String, Time> timeBeforeStart;
	/**
	 * Execution time of the task
	 */
	private Time execTime;

	// F(s) take into account 2 criteria
	/**
	 * Overall distance covered by the resources to realize the tasks from the
	 * root node to the current solution node
	 */
	double distance;
	/**
	 * Overall overspent time from the root node to the current solution node
	 */
	Time overspentTime;
	long overspentTimeStep = -1;
	Time endTime;

	long overrunPenalties;

	// GO BACK TO DEPOT
	/**
	 * Distance covered by the resource to go back to the depot after realizing
	 * the curent task
	 */
	double goBackDepotDistance;
	/**
	 * Time required for the resource to go back to the depot
	 */
	Time goBackDepotTime;

	/**
	 * Constructor
	 * 
	 * @param parent
	 *            Parent solution node
	 * @param resource
	 *            Resource allocated in the constructed solution
	 * @param task
	 *            Task concerned in the constructed solution
	 */
	public Solution(Solution parent, String resource, String task) {
		this.parent = parent;

		this.resource = resource;
		this.task = task;

		if (parent != null) {
			this.overspentTime = new Time(parent.overspentTime);

			this.timeBeforeStart = new HashMap<String, Time>();
			for (String vehicle : parent.timeBeforeStart.keySet()) {

				if (vehicle.equals(parent.resource)) {
					this.timeBeforeStart.put(vehicle, new Time(
							parent.timeBeforeStart.get(vehicle),
							parent.execTime));
				} else
					this.timeBeforeStart.put(vehicle, new Time(
							parent.timeBeforeStart.get(vehicle)));
			}
			this.level = parent.level + 1;
			this.overrunPenalties = parent.overrunPenalties;
		} else {
			this.overspentTime = new Time(0);
			this.timeBeforeStart = new HashMap<String, Time>();
			this.execTime = new Time(0);
			this.level = 0;
			this.overrunPenalties = 0;
		}

	}

	/**
	 * Watchout : Not a distinct copy !
	 * 
	 * @param toCopy
	 */
	public Solution(Solution toCopy) {
		this.distance = toCopy.distance;
		this.overspentTimeStep = toCopy.overspentTimeStep;
	}

	/**
	 * Compute $distance and $overspentTime of the current solution @
	 */
	public void compute() {
		previousTask = getLastTaskOfResource();
		CostHelper parentToCurrentCost = branchAndBound.costs.getCosts(
				previousTask, task);
		CostHelper currentCost = branchAndBound.costs.getCosts(task, task);
		// parent.D -> current.P
		distance = parentToCurrentCost.getCostInMeters(resource);
		Time t = parentToCurrentCost.getCostInTime(resource);
		// current.P -> current.D
		distance += currentCost.getCostInMeters(resource);
		t = new Time(t, currentCost.getCostInTime(resource));

		distance += parent.distance;

		// Beware of the TWs !
		Mission m = Terminal.getInstance().getMission(task);
		Time tBS = timeBeforeStart.get(resource);
		if (tBS == null) {
			timeBeforeStart.put(resource,
					branchAndBound.getMissionStartTime(task, resource));
			tBS = timeBeforeStart.get(resource);
		}

		// END TIME = START TIME + ?
		endTime = new Time(tBS);
		if (new Time(endTime, parentToCurrentCost.getCostInTime(resource))
				.toStep() >= m.getPickupTimeWindow().getMin().toStep()) {
			// TRAVEL TIME FROM PARENT + PARENT MISSION UNLOADING
			endTime = new Time(endTime,
					parentToCurrentCost.getCostInTime(resource));
		} else {
			// TW_P.min + PARENT CONTAINER UNLOADING
			endTime = new Time(m.getPickupTimeWindow().getMin(),
					branchAndBound.getContainerHandlingTime(previousTask,
							resource, MissionPhase.PHASE_DELIVERY));
		}
		// OverspentTime for TW_P
		long realPickupEndStep = endTime.toStep();
		long twpMaxStep = m.getPickupTimeWindow().getMax().toStep();
		if (realPickupEndStep > twpMaxStep) {
			overspentTime = new Time(overspentTime, new Time(realPickupEndStep
					- twpMaxStep));
			overrunPenalties += (realPickupEndStep - twpMaxStep)
					* currentCost.pickupOverrunPenalty;
		}

		// END TIME = START TIME + TIME FROM PARENT TO CURRENT + TIME PARENT
		// UNLOADING + ?
		if (new Time(endTime, currentCost.getCostInTime(resource)).toStep() >= m
				.getDeliveryTimeWindow().getMin().toStep()) {
			// TIME FROM PICKUP TO DELIVERY + CONTAINER LOADING
			endTime = new Time(endTime, currentCost.getCostInTime(resource));
		} else {
			// TW_D.min + CONTAINER LOADING
			endTime = new Time(m.getDeliveryTimeWindow().getMin(),
					branchAndBound.getContainerHandlingTime(task, resource,
							MissionPhase.PHASE_PICKUP));
		}

		// OverspentTime for TW_D
		long realDeliveryEndStep = endTime.toStep();
		long twdMaxStep = m.getDeliveryTimeWindow().getMax().toStep();
		if (realDeliveryEndStep > twdMaxStep) {
			overspentTime = new Time(overspentTime, new Time(
					realDeliveryEndStep - twdMaxStep));
			overrunPenalties += (realDeliveryEndStep - twdMaxStep)
					* currentCost.deliveryOverrunPenalty;
		}

		// GO BACK TO DEPOT
		goBackDepotDistance = branchAndBound.costs.getCosts(task, DepotNode.ID)
				.getCostInMeters(resource);
		goBackDepotTime = branchAndBound.costs.getCosts(task, DepotNode.ID)
				.getCostInTime(resource);

		if (isLeaf()) {
			// UPDATE PATH WITH DEPOT BACK TIME
			HashMap<String, Double> map = new HashMap<String, Double>(
					timeBeforeStart.size());
			HashMap<String, Time> mapTime = new HashMap<String, Time>(
					timeBeforeStart.size());
			for (String s : timeBeforeStart.keySet()) {
				Solution current = this;
				boolean found = false;
				while (current != null && !found) {
					if (current.resource.equals(s)) {
						found = true;
						map.put(s, current.goBackDepotDistance);
						mapTime.put(s, current.goBackDepotTime);
					}
					current = current.parent;
				}
				if (!found) {
					map.put(s, 0.0);
					mapTime.put(s, new Time(0));
				}

			}
			for (Double d : map.values()) {
				distance += d;
			}
			for (Time tt : mapTime.values()) {
				endTime = new Time(endTime, tt);
			}
		}

		// Execution time of the addition of the last mission (endTime =
		// timeBeforeStart + execTime)
		execTime = new Time(endTime, tBS, false);

		overspentTimeStep = this.overspentTime.toStep();
	}

	/**
	 * 
	 * @return True if the current solution contains all the missions in the
	 *         pool, False otherwise
	 */
	public boolean isLeaf() {
		if (level == MissionScheduler.getInstance().getPoolSize())
			return true;
		else
			return false;
	}

	/**
	 * Compute and retrieves the children of the given solution
	 * 
	 * @return The list of children solutions @
	 */
	public List<Solution> getChildren() {
		List<Solution> children = new ArrayList<Solution>();

		List<String> remainingTasks = new ArrayList<String>(MissionScheduler
				.getInstance().getPoolSize() - level);
		List<String> tabu = new ArrayList<String>();
		Solution current = this;
		while (current != null) {
			tabu.add(current.task);
			current = current.parent;
		}

		for (Mission m : MissionScheduler.getInstance().getPool()) {
			boolean found = false;
			String mID = m.getId();
			for (String s : tabu) {
				if (mID.equals(s)) {
					found = true;
					break;
				}
			}
			if (!found) {
				remainingTasks.add(mID);
			}
		}

		for (StraddleCarrier rsc : MissionScheduler.getInstance()
				.getResources()) {
			for (String mID : remainingTasks) {
				Solution s = new Solution(this, rsc.getId(), mID);
				s.compute();
				children.add(s);
			}
		}

		return children;
	}

	/**
	 * Get the previous task in the tree allocated to the current resource
	 * 
	 * @return The previous task in the tree allocated to the current resource
	 */
	public String getLastTaskOfResource() {
		Solution current = this;
		while (current.parent != null) {
			if (current.parent.resource.equals(resource))
				return current.parent.task;
			current = current.parent;
		}
		return DepotNode.ID;
	}

	// TODO take overspent time into account ?
	/**
	 * Comparator
	 * 
	 * @param s
	 *            Solution to compare
	 * @return
	 */
	public int compareTo(Solution s) {
		int diff = (int) (overspentTimeStep - s.overspentTimeStep);
		if (diff == 0) {
			if (distance > s.distance)
				return 1;
			else if (distance < s.distance)
				return -1;
			else
				return 0;
		} else if (diff > 0)
			return 1;
		else
			return -1;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		if (parent != null) {
			sb.append(parent.toString() + "\n");
			double oldCurrentMeters = branchAndBound.costs.getCosts(
					previousTask, task).getCostInMeters(resource);
			Time oldCurrentTime = branchAndBound.costs.getCosts(previousTask,
					task).getCostInTime(resource);
			double currentMeters = branchAndBound.costs.getCosts(task, task)
					.getCostInMeters(resource);
			Time currentTime = branchAndBound.costs.getCosts(task, task)
					.getCostInTime(resource);
			Time currentPenalty = new Time(overrunPenalties);
			sb.append(resource + "@" + task + " d=" + distance + " t="
					+ timeBeforeStart.get(resource) + " endT=" + this.endTime
					+ " origin=" + previousTask + " origin->this=("
					+ oldCurrentMeters + " | " + oldCurrentTime
					+ ") this->this=(" + currentMeters + " | " + currentTime
					+ ")" + " penalty=" + currentPenalty);
		}
		return sb.toString();
	}

	/**
	 * Get the previous Solution in the tree containing a task allocated to the
	 * given resource
	 * 
	 * @param leaf
	 *            Start point of the search
	 * @param resourceID
	 *            Resource ID
	 * @return The previous Solution in the tree containing a task allocated to
	 *         the given resource
	 */
	public static Solution getLastResourceSolution(Solution leaf,
			String resourceID) {
		Solution current = leaf;
		while (current.parent != null) {
			if (current.parent.resource.equals(resourceID))
				return current.parent;
			current = current.parent;
		}
		return null;
	}

	/**
	 * Set the Branch and Bound algorithm
	 * 
	 * @param bb
	 *            Used branch and bound algorithm
	 */
	public static void setBranchAndBound(BranchAndBound bb) {
		Solution.branchAndBound = bb;
	}

	public String toResourceString(String r) {
		StringBuilder sb = new StringBuilder();
		// if(resource.equals(r)){
		Solution s = getLastResourceSolution(this, r);
		if (s != null) {
			sb.append(s.toResourceString(r));
		}
		if (resource.equals(r))
			sb.append(" -> " + task);
		// }
		return sb.toString();
	}
}