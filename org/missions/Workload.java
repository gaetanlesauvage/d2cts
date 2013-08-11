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
package org.missions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.conf.parameters.ReturnCodes;
import org.exceptions.MissionNotFoundException;
import org.exceptions.NoPathFoundException;
import org.routing.path.Path;
import org.scheduling.greedy.ScheduleScore;
import org.system.Terminal;
import org.system.container_stocking.Bay;
import org.system.container_stocking.ContainerLocation;
import org.system.container_stocking.Slot;
import org.time.Time;
import org.time.TimeWindow;
import org.util.Location;
import org.vehicles.StraddleCarrier;

public class Workload {
	// TODO improve data structure ?
	private List<Load> workload;
	private Map<String, Load> workloadMap;
	private String straddleCarrierID;
	private StraddleCarrier straddleCarrier;
	private static Terminal terminal;

	public static final String NON_AFFECTED = "NA";

	public Workload(StraddleCarrier straddleCarrier) {

		this.straddleCarrier = straddleCarrier;
		straddleCarrierID = straddleCarrier.getId();

		if (terminal == null)
			terminal = Terminal.getInstance();
		workload = Collections.synchronizedList(new ArrayList<Load>());
		workloadMap = Collections.synchronizedMap(new HashMap<String, Load>());
	}

	public/* synchronized */Load checkLoad(Time time) {
		for (int i = 0; i < workload.size(); i++) {
			Load l = workload.get(i);
			if (l.getState() == MissionState.STATE_TODO) {
				if (l.getStartTime().compareTo(time) <= 0) {
					if (!l.isLinked()) {
						return l;
					} else if (l.getLinkedLoad().getState() == MissionState.STATE_ACHIEVED)
						return l;
					else
						return null;
				} else
					return null;
			}

		}
		return null;
	}

	public/* synchronized */void endMission(String id) {
		int iFound = -1;
		for (int i = 0; i < workload.size() && iFound < 0; i++) {
			Load l = workload.get(i);
			if (l.getMission().getId().equals(id)) {
				l.done();
				iFound = i;
			}

		}
		/*
		 * workload.remove(iFound);
		 * System.out.println("Mission found at "+iFound);
		 */
	}

	public/* synchronized */Load getCurrentLoad() {
		for (Load l : workload) {
			if (l.getState() == MissionState.STATE_CURRENT)
				return l;

		}
		return null;
	}

	public boolean contains(String missionID) {
		return workloadMap.containsKey(missionID);
	}

	public Load getCopyOfLoad(String missionID) {
		return new Load(getLoad(missionID));
	}

	public Load getLoad(String misisonID) {
		for (Load l : workload) {
			if (l.getMission().getId().equals(misisonID))
				return l;
		}
		return null;
	}

	public/* synchronized */List<Load> getLoads() {
		return workload;
	}

	private void insertAfter(Load toInsert, Load afterThatLoad) {
		boolean added = false;
		for (int i = 0; i < workload.size() && !added; i++) {
			Load load = workload.get(i);
			if (load.getMission().getId()
					.equals(afterThatLoad.getMission().getId())) {
				added = true;
				workload.add(i + 1, toInsert);
			}
		}
		if (!added) {
			new Exception("LINK MISSION NOT FOUND").printStackTrace();
			System.exit(ReturnCodes.LINK_MISSION_NOT_FOUND.getCode());
		}
	}

	public void insertAtRightPlace(Mission m) {
		ScheduleScore best = new ScheduleScore();
		// workloadMap.put(l.getMission().getId(), l);

		// Insert at last position
		TimeWindow tw = new TimeWindow(m.getPickupTimeWindow().getMin(), m
				.getDeliveryTimeWindow().getMax());
		ContainerLocation cl = m.getContainer().getContainerLocation();
		Time startableTime = tw.getMin();
		Load l = new Load(tw, m, startableTime);
		if (workload.size() == 0) {
			if (cl != null) {
				Slot sDest = terminal.getSlot(cl.getSlotId());
				try {
					startableTime = new Time(tw.getMin(), new Time(
							straddleCarrier
							.getRouting()
							.getShortestPath(
									straddleCarrier.getSlot()
									.getLocation(),
									sDest.getLocation()).getCost()
									), false);
				} catch (NoPathFoundException e) {
					e.printStackTrace();
				}
			}
			l = new Load(tw, m, startableTime);
			workload.add(l);
		} else {
			best.distance = Double.POSITIVE_INFINITY;
			best.tardiness = new Time(Time.MAXTIME);
			int index = workload.size();
			int bestIndex = index;
			while (index >= 0) {
				workload.add(index, l);
				ScheduleScore score = getScore();
				if (best.compareTo(score) > 0) {
					best = score;
					bestIndex = index;
				}
				workload.remove(index);
				index--;
			}
			// ADD at right index
			workload.add(bestIndex, l);
			// Update startTimes
			for (int i = 0; i < workload.size(); i++) {
				Load load = workload.get(i);
				Time st = load.getMission().getPickupTimeWindow().getMin();
				if (i == 0) {
					ContainerLocation contLoc = load.getMission()
							.getContainer().getContainerLocation();
					if (contLoc != null) {
						Slot sDest = terminal.getSlot(contLoc.getSlotId());
						try {
							st = new Time(load.getMission()
									.getPickupTimeWindow().getMin(), new Time(
											straddleCarrier
											.getRouting()
											.getShortestPath(
													straddleCarrier
													.getLocation(),
													sDest.getLocation())
													.getCost()
													), false);
						} catch (NoPathFoundException e) {
							e.printStackTrace();
						}
					}
					load.setStartableTime(st);
				}// TODO update startTime when container arrives on the terminal
				else {
					Load link = workload.get(i - 1);
					load.setLinkedLoad(link);
					st = link.getMission().getDeliveryTimeWindow().getMin();
					load.setStartableTime(st);
				}
			}
		}

		workloadMap.put(m.getId(), l);
		terminal.missionAffected(l, straddleCarrierID);
	}

	public ScheduleScore getScore() {
		ScheduleScore score = new ScheduleScore();
		try {

			Location origin = straddleCarrier.getSlot().getLocation();
			Time tOrigin = terminal.getTime();
			if (straddleCarrier.getCurrentLoad() != null) {
				ContainerLocation cl = straddleCarrier.getCurrentLoad()
						.getMission().getDestination();
				origin = new Location(terminal.getRoad(cl.getLaneId()),
						cl.getCoordinates(), true);
				tOrigin = straddleCarrier.getCurrentLoad().getMission()
						.getDeliveryTimeWindow().getMax();
			}

			for (Load l : workload) {
				if (l.getState() == MissionState.STATE_TODO) {
					ContainerLocation cl = l.getMission().getContainer()
							.getContainerLocation();
					Bay lane = terminal.getBay(cl.getLaneId());
					Location pickup = new Location(lane, cl.getCoordinates(),
							true);
					Path p;
					p = straddleCarrier.getRouting().getShortestPath(origin,
							pickup, tOrigin);

					Time t = new Time(p.getCost());
					score.distance += p.getCostInMeters();
					Time pickupTime = new Time(tOrigin, t);
					pickupTime = new Time(pickupTime,
							straddleCarrier.getMaxContainerHandlingTime(lane));
					long lateness = pickupTime.toStep()
							- l.getMission().getPickupTimeWindow().getMax()
							.toStep();
					// Pickup tardiness
					Time localTardiness = new Time(Math.max(0, lateness));
					score.tardiness = new Time(score.tardiness, localTardiness);

					tOrigin = new Time(Math.max(l.getMission()
							.getPickupTimeWindow().getMax().toStep(),
							pickupTime.toStep()));

					// -> Delivery
					cl = l.getMission().getDestination();
					lane = terminal.getBay(cl.getLaneId());
					Location delivery = new Location(lane, cl.getCoordinates(),
							true);
					p = straddleCarrier.getRouting().getShortestPath(pickup,
							delivery, tOrigin);
					t = new Time(p.getCost());
					score.distance += p.getCostInMeters();
					Time deliveryTime = new Time(tOrigin, t);
					deliveryTime = new Time(deliveryTime,
							straddleCarrier.getMaxContainerHandlingTime(lane));
					lateness = deliveryTime.toStep()
							- l.getMission().getDeliveryTimeWindow().getMax()
							.toStep();
					// Delivery tardiness
					score.tardiness = new Time(score.tardiness, new Time(
							Math.max(0, lateness)));

					// Next load
					tOrigin = new Time(Math.max(l.getMission()
							.getDeliveryTimeWindow().getMax().toStep(),
							deliveryTime.toStep()));
					origin = delivery;
				}
			}

			// Delivery->Depot
			Path p = straddleCarrier.getRouting().getShortestPath(origin,
					straddleCarrier.getSlot().getLocation(), tOrigin);
			score.distance += p.getCostInMeters();
		} catch (NoPathFoundException e) {
			e.printStackTrace();
		}
		return score;
	}

	public void insert(Load l) {
		boolean added = false;
		if (l.isLinked())
			insertAfter(l, l.getLinkedLoad());
		else {
			for (int i = 0; i < workload.size() && !added; i++) {
				Load load = workload.get(i);
				if (l.compareTo(load) < 1) {
					added = true;
					workload.add(i, l);
				}
			}
			if (!added)
				workload.add(l);
		}
		workloadMap.put(l.getMission().getId(), l);

		terminal.missionAffected(l, straddleCarrierID);
	}

	public void insert(Mission m, Mission link) {
		if (link != null) {
			TimeWindow tw = new TimeWindow(m.getPickupTimeWindow().getMin(), m
					.getDeliveryTimeWindow().getMax());

			Load l = new Load(tw, m, link.getDeliveryTimeWindow().getMin());
			// System.err.println("Load created for mission "+m.getId()+" linked to "+link.getId()+" (startTime from "+l.getStartTime()+")");
			l.setLinkedLoad(getLoad(link.getId()));

			insert(l);
		} else {

			insert(m);
		}
	}

	// DONE : Change policy because this one consist in starting the mission at
	// the very beginning of his time window !
	// TODO test new start policy
	public void insert(Mission m) {

		TimeWindow tw = new TimeWindow(m.getPickupTimeWindow().getMin(), m
				.getDeliveryTimeWindow().getMax());
		ContainerLocation cl = m.getContainer().getContainerLocation();
		Time startableTime = tw.getMin();
		if (cl != null) {
			Slot sDest = terminal.getSlot(cl.getSlotId());
			try {
				startableTime = new Time(tw.getMin(), new Time(straddleCarrier
						.getRouting()
						.getShortestPath(
								straddleCarrier.getSlot().getLocation(),
								sDest.getLocation()).getCost()
								), false);
			} catch (NoPathFoundException e) {
				e.printStackTrace();
			}
		}

		Load l = new Load(tw, m, startableTime);
		insert(l);
	}

	public/* synchronized */void removeMission(String mission)
			throws MissionNotFoundException, NoPathFoundException {
		int index = -1;
		for (int i = 0; i < workload.size() && index < 0; i++) {
			Load l = workload.get(i);
			if (l.getMission().getId().equals(mission)) {
				// Remove
				index = i;
			}
		}
		if (index == -1)
			throw new MissionNotFoundException(mission);
		else {
			Load l = workload.get(index);
			Load link = l.getLinkedLoad();
			if (index < workload.size() - 1) {
				Load next = workload.get(index + 1);
				if (next.getLinkedLoad() == l) {
					next.setLinkedLoad(link);
					if (link != null) {
						next.setStartableTime(new Time(link.getMission()
								.getDeliveryTimeWindow().getMin()));
					} else {
						ContainerLocation cl = next.getMission().getContainer()
								.getContainerLocation();
						if (cl != null) {
							Slot sDest = terminal.getSlot(cl.getSlotId());
							next.setStartableTime(new Time(next.getMission()
									.getPickupTimeWindow().getMin(), new Time(
											straddleCarrier
											.getRouting()
											.getShortestPath(
													straddleCarrier
													.getLocation(),
													sDest.getLocation())
													.getCost()
													), false));
						} else
							next.setStartableTime(next.getMission()
									.getPickupTimeWindow().getMin());
					}
				}
			}
		}
		workload.remove(index);
		workloadMap.remove(mission);
	}

	public Load remove(String mission) throws MissionNotFoundException {

		for (int i = 0; i < workload.size(); i++) {
			Load l = workload.get(i);
			if (l.getMission().getId().equals(mission)) {
				if (l.getState() == MissionState.STATE_TODO) {
					terminal.missionAffected(l, NON_AFFECTED);

					Load removed = workload.remove(i);
					workloadMap.remove(removed.getMission().getId());
					// System.err.println("REMOVING "+mission+" of "+straddleCarrierID+"'s WORKLOAD : done.");
					if (removed.getLinkedLoad() != null)
						return remove(removed.getLinkedLoad().getMission()
								.getId());
					else
						return removed;
				} else {
					Load toRemove = workload.get(i);
					// System.err.println("REMOVING "+mission+" of "+straddleCarrierID+"'s WORKLOAD : not done.");
					if (toRemove.getLinkedLoad() != null)
						return remove(toRemove.getLinkedLoad().getMission()
								.getId());
					else
						return toRemove;
				}

			}

		}
		throw new MissionNotFoundException(mission);
	}

	public void startMission(String id) throws MissionNotFoundException {
		boolean found = false;
		for (int i = 0; i < workload.size() && !found; i++) {
			Load l = workload.get(i);
			if (l.getMission().getId().equals(id)) {
				found = true;
				if (l.getState() != MissionState.STATE_TODO)
					System.err
					.println("TRYING TO START AN ALREADY STARTED MISSION !!!");
				l.start(straddleCarrierID);
			}
		}
		if (!found)
			throw new MissionNotFoundException(id);
	}

	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(straddleCarrierID + "'s workload:");
		int i = 0;
		for (Load l : workload) {
			sb.append("\n\t" + i + "> " + l);
			i++;
		}
		return sb.toString();
	}

	public void destroy() {
		straddleCarrierID = null;
		terminal = null;
		Load.destroy();
		workload = null;
		workloadMap.clear();
		workloadMap = null;
	}

	public boolean isEmpty() {
		return workload.size() == 0;
	}
}
