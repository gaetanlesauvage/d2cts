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
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;
import java.util.StringTokenizer;

import org.apache.log4j.Logger;
import org.exceptions.EmptyResourcesException;
import org.exceptions.NoPathFoundException;
import org.missions.Mission;
import org.missions.MissionPhase;
import org.routing.path.Path;
import org.scheduling.MissionScheduler;
import org.scheduling.aco.graph.DepotNode;
import org.system.container_stocking.BlockType;
import org.time.Time;
import org.vehicles.StraddleCarrier;

/**
 * Costs in both time and distance of the tasks for each resource
 * 
 * @author Ga&euml;tan Lesauvage
 * @since 2012
 */
class Costs {
	private static final Logger log = Logger.getLogger(Costs.class);

	/**
	 * Matrix <origin task , Matrix<destination task , CostHelper>
	 */
	HashMap<String, HashMap<String, CostHelper>> matrix;

	/**
	 * Constructor
	 */
	public Costs() {
		matrix = new HashMap<String, HashMap<String, CostHelper>>(
				1 + BranchAndBound.getInstance().getPoolSize());
	}

	/**
	 * Compute the costs and stored them in the matrix @ * @throws
	 * NoPathFoundException
	 */
	public void compute() throws NoPathFoundException {
		// DEPOT -> DEPOT
		HashMap<String, CostHelper> mapDepot = new HashMap<String, CostHelper>();
		CostHelper mDepot = new CostHelper(DepotNode.ID, DepotNode.ID);

		for (StraddleCarrier rsc : MissionScheduler.getInstance()
				.getResources()) {
			mDepot.setDistanceCost(rsc.getId(), 0.0);
			mDepot.setTimeCost(rsc.getId(), new Time(0));
		}
		mapDepot.put(DepotNode.ID, mDepot);
		matrix.put(DepotNode.ID, mapDepot);

		// MISSIONS
		int current = 1;
		for (Mission mOrigin : MissionScheduler.getInstance().getPool()) {
			System.out.println("INIT COSTS " + current + "/"
					+ MissionScheduler.getInstance().getPoolSize());
			current++;

			HashMap<String, CostHelper> map = new HashMap<String, CostHelper>();
			for (Mission mDestination : MissionScheduler.getInstance()
					.getPool()) {
				CostHelper submatrix = new CostHelper(mOrigin.getId(),
						mDestination.getId());
				if (mOrigin.getId().equals(mDestination.getId())) {
					for (StraddleCarrier resource : MissionScheduler
							.getInstance().getResources()) {
						// Slot sDest =
						// BranchAndBound.rt.getSlot(mOrigin.getDestination().getSlotId());
						try {
							Path p = MissionScheduler.getInstance()
									.getTravelPath(mOrigin, mOrigin, resource);
							double ht = getContainerHandlingTime(resource,
									mOrigin, MissionPhase.PHASE_DELIVERY);
							double pCost = p.getCost() + ht;
							System.out.println(mOrigin.getId() + "@"
									+ resource.getId() + " : " + p.getCost()
									+ " + " + ht + " = " + pCost);
							submatrix.setDistanceCost(resource.getId(),
									p.getCostInMeters());
							submatrix.setTimeCost(resource.getId(), new Time(
									pCost));
							submatrix
									.setPickupOverrunPenalty(mOrigin
											.getMissionKind()
											.getOverrunPickupPenalty());
							submatrix.setDeliveryOverrunPenalty(mOrigin
									.getMissionKind()
									.getOverrunDeliveryPenalty());
						} catch (EmptyResourcesException e) {
							e.printStackTrace();
						}

						// Path p =
						// resource.getRemoteRouting().getShortestPath(mOrigin.getContainer().getLocation(),
						// sDest.getLocation());
						// Deliver the container

					}
				} else {
					for (StraddleCarrier resource : MissionScheduler
							.getInstance().getResources()) {
						// Slot sOrigin =
						// BranchAndBound.rt.getSlot(mOrigin.getDestination().getSlotId());
						try {
							Path p = MissionScheduler.getInstance()
									.getTravelPath(mOrigin, mDestination,
											resource);
							// Path p =
							// resource.getRemoteRouting().getShortestPath(sOrigin.getLocation(),
							// mDestination.getContainer().getLocation());
							// Pickup the container
							double ht = getContainerHandlingTime(resource,
									mOrigin, MissionPhase.PHASE_PICKUP);
							double pCost = p.getCost() + ht;
							System.out.println(mOrigin.getId() + "->"
									+ mDestination.getId() + "@"
									+ resource.getId() + " : " + p.getCost()
									+ " + " + ht + " = " + pCost);
							submatrix.setDistanceCost(resource.getId(),
									p.getCostInMeters());
							submatrix.setTimeCost(resource.getId(), new Time(
									pCost));
						} catch (EmptyResourcesException e) {
							e.printStackTrace();
						}
					}
				}
				map.put(mDestination.getId(), submatrix);
			}
			// DEPOT -> MOrigin
			CostHelper submatrix = new CostHelper(DepotNode.ID, mOrigin.getId());
			for (StraddleCarrier resource : MissionScheduler.getInstance()
					.getResources()) {
				// Location lFrom = resource.getSlot().getLocation();
				try {
					Path p = MissionScheduler.getInstance().getTravelPath(null,
							mOrigin, resource);
					// Path p =
					// resource.getRemoteRouting().getShortestPath(lFrom,
					// mOrigin.getContainer().getLocation());
					// Pickup the container
					double ht = getContainerHandlingTime(resource, mOrigin,
							MissionPhase.PHASE_PICKUP);
					double pCost = p.getCost() + ht;
					System.out.println(DepotNode.ID + "->" + mOrigin.getId()
							+ "@" + resource.getId() + " : " + p.getCost()
							+ " + " + ht + " = " + pCost);
					submatrix.setDistanceCost(resource.getId(),
							p.getCostInMeters());
					submatrix.setTimeCost(resource.getId(), new Time(pCost));
				} catch (EmptyResourcesException e) {
					e.printStackTrace();
				}
			}
			HashMap<String, CostHelper> subMatrixDepot = matrix
					.get(DepotNode.ID);
			subMatrixDepot.put(mOrigin.getId(), submatrix);

			// MOrigin -> DEPOT
			submatrix = new CostHelper(mOrigin.getId(), DepotNode.ID);
			for (StraddleCarrier resource : MissionScheduler.getInstance()
					.getResources()) {
				// Location lDest = resource.getSlot().getLocation();
				// Slot sFrom =
				// BranchAndBound.rt.getSlot(mOrigin.getDestination().getSlotId());
				// Path p =
				// resource.getRemoteRouting().getShortestPath(sFrom.getLocation(),
				// lDest);
				try {
					Path p = MissionScheduler.getInstance().getTravelPath(
							mOrigin, null, resource);
					double pCost = p.getCost();
					System.out.println(mOrigin.getId() + "->" + DepotNode.ID
							+ "@" + resource.getId() + " : " + pCost);
					submatrix.setDistanceCost(resource.getId(),
							p.getCostInMeters());
					submatrix.setTimeCost(resource.getId(), new Time(pCost));
				} catch (EmptyResourcesException e) {
					e.printStackTrace();
				}
			}
			map.put(DepotNode.ID, submatrix);

			matrix.put(mOrigin.getId(), map);
		}
	}

	/**
	 * Set the cost for going from the origin task to the destination one
	 * 
	 * @param originMission
	 *            Origin task
	 * @param destinationMission
	 *            Destination task
	 * @param costs
	 *            Costs
	 */
	public void setCost(String originMission, String destinationMission,
			CostHelper costs) {
		HashMap<String, CostHelper> m = null;
		if (matrix.containsKey(originMission)) {
			m = matrix.get(originMission);
		} else
			m = new HashMap<String, CostHelper>();

		m.put(destinationMission, costs);

		matrix.put(originMission, m);
	}

	/**
	 * Get the cost for going from the origin task to the destination one
	 * 
	 * @param origin
	 *            Origin task
	 * @param destination
	 *            Destination task
	 * @return The cost for going from the origin task to the destination one
	 */
	public CostHelper getCosts(String origin, String destination) {
		HashMap<String, CostHelper> submap = matrix.get(origin);
		if (submap == null)
			System.err.println("ERROR (" + origin + " , " + destination + ")");
		return submap.get(destination);
	}

	/**
	 * Load the cost stored in the given files
	 * 
	 * @param timeMatrixFile
	 *            Time matrix file name
	 * @param distanceMatrixFile
	 *            Distance matrix file name
	 * @throws Exception
	 */
	public void load(String timeMatrixFile, String distanceMatrixFile)
			throws Exception {
		String[] resourcesID = new String[MissionScheduler.getInstance()
				.getResources().size()];
		int i = 0;
		for (StraddleCarrier rsc : MissionScheduler.getInstance()
				.getResources()) {
			resourcesID[i++] = rsc.getId();
		}

		Scanner scanTime = null;
		Scanner scanDistance = null;
		try {
			scanTime = new Scanner(new File(timeMatrixFile));
			scanDistance = new Scanner(new File(distanceMatrixFile));

			String rNames = scanTime.nextLine();
			String test = scanDistance.nextLine();
			if (!rNames.equals(test))
				throw new Exception("File Format Exception :\n" + rNames + "\n"
						+ test);

			StringTokenizer stTime = new StringTokenizer(rNames);
			StringTokenizer stDistance;

			ArrayList<String> locationsNames = new ArrayList<String>(
					stTime.countTokens());
			while (stTime.hasMoreTokens()) {
				locationsNames.add(stTime.nextToken());
			}

			while (scanTime.hasNextLine() && scanDistance.hasNextLine()) {
				String lineTime = scanTime.nextLine();
				String lineDistance = scanDistance.nextLine();

				stTime = new StringTokenizer(lineTime);
				stDistance = new StringTokenizer(lineDistance);

				String origin = stTime.nextToken();
				test = stDistance.nextToken();
				if (!origin.equals(test))
					throw new Exception("File Format Exception !");

				for (int j = 0; j < locationsNames.size(); j++) {
					String destination = locationsNames.get(j);
					CostHelper m = new CostHelper(origin, destination);
					if (!origin.equals(DepotNode.ID)
							&& origin.equals(destination)) {
						Mission mission = BranchAndBound.rt.getMission(origin);
						m.setPickupOverrunPenalty(mission.getMissionKind()
								.getOverrunPickupPenalty());
						m.setDeliveryOverrunPenalty(mission.getMissionKind()
								.getOverrunDeliveryPenalty());
					}
					for (String resourceID : resourcesID) {
						String sTime = stTime.nextToken().replace(',', '.');
						String sDist = stDistance.nextToken().replace(',', '.');
						Time t = new Time(sTime);
						double d = Double.parseDouble(sDist);
						m.setTimeCost(resourceID, t);
						m.setDistanceCost(resourceID, d);
					}
					setCost(origin, destination, m);
				}
			}

			if (scanTime.hasNextLine() || scanDistance.hasNextLine()) {
				throw new Exception("File Format Exception !");
			}
		} catch (IOException e) {
			log.error(e);
		} finally {
			if (scanDistance != null)
				scanDistance.close();
			if (scanTime != null)
				scanTime.close();
		}
	}

	/**
	 * Store the matrix in the given file names
	 * 
	 * @param timeMatrixFile
	 *            Time matrix file name
	 * @param distanceMatrixFile
	 *            Distance matrix file name
	 * @throws Exception
	 */
	public void save(String timeMatrixFile, String distanceMatrixFile)
			throws Exception {
		PrintWriter pwTime = new PrintWriter(timeMatrixFile);
		PrintWriter pwDistance = new PrintWriter(distanceMatrixFile);
		ArrayList<String> l = new ArrayList<String>(matrix.size());
		for (String taskName : matrix.keySet()) {
			pwTime.append(taskName + "\t");
			pwDistance.append(taskName + "\t");
			l.add(taskName);
		}
		pwTime.append("\n");
		pwDistance.append("\n");

		for (String task : l) {
			HashMap<String, CostHelper> map = matrix.get(task);

			pwTime.append(task + "\t");
			pwDistance.append(task + "\t");

			for (String taskTo : l) {
				CostHelper costs = map.get(taskTo);
				for (StraddleCarrier rsc : MissionScheduler.getInstance()
						.getResources()) {
					pwTime.append(costs.getCostInTime(rsc.getId()) + "\t");
					pwDistance
							.append(costs.getCostInMeters(rsc.getId()) + "\t");
				}
			}

			pwTime.append("\n");
			pwDistance.append("\n");
		}

		pwTime.flush();
		pwDistance.flush();

		pwTime.close();
		pwDistance.close();
	}

	private double getContainerHandlingTime(StraddleCarrier resource,
			Mission task, MissionPhase phase) {
		double d = 0;

		if (phase == MissionPhase.PHASE_PICKUP) {
			if (BranchAndBound.rt.getBlock(
					task.getContainer().getContainerLocation().getPaveId())
					.getType() == BlockType.ROAD) {
				d = resource.getModel().getSpeedCharacteristics()
						.getContainerHandlingTimeFromTruckMAX();
			} else {
				d = resource.getModel().getSpeedCharacteristics()
						.getContainerHandlingTimeFromGroundMAX();
			}
		} else {
			if (BranchAndBound.rt.getBlock(task.getDestination().getPaveId())
					.getType() == BlockType.ROAD) {
				d = resource.getModel().getSpeedCharacteristics()
						.getContainerHandlingTimeFromTruckMAX();
			} else {
				d = resource.getModel().getSpeedCharacteristics()
						.getContainerHandlingTimeFromGroundMAX();
			}
		}
		return d;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();

		for (String sOrigin : matrix.keySet()) {
			HashMap<String, CostHelper> map = matrix.get(sOrigin);
			sb.append(" ---------- " + sOrigin + " --------------\n");
			for (String sDestination : map.keySet()) {
				CostHelper m = map.get(sDestination);
				sb.append(sOrigin + " -> " + sDestination + " =\t{" + m + "}\n");
			}
		}
		return sb.toString();
	}
}