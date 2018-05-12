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
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

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
	private List<Load> workload;
	private SortedMap<String, Load> workloadMap;
	private String straddleCarrierID;
	private StraddleCarrier straddleCarrier;

	public static final String NON_AFFECTED = "NA";

	public Workload(StraddleCarrier straddleCarrier) {

		this.straddleCarrier = straddleCarrier;
		straddleCarrierID = straddleCarrier.getId();

		workload = new ArrayList<>();
		workloadMap = new TreeMap<>();
	}

	public Load checkLoad(Time time) {
		for (int i = 0; i < workload.size(); i++) {
			Load l = workload.get(i);
			if (l.getState() == MissionState.STATE_TODO) {
				if (l.getStartTime().compareTo(time) <= 0) {
					if (!l.isLinked()) {
						return l;
					} else if (l.getPrethreadCondition().canStart()){
						return l;
					} else{
						return null;
					}
				} else{
					return null;
				}
			}

		}
		return null;
	}

	public boolean endMission(String id) {
		for (int i = 0; i < workload.size(); i++) {
			Load l = workload.get(i);
			if (l.getMission().getId().equals(id)) {
				l.done();
				return true;
			}

		}
		return false;
	}

	public Load getCurrentLoad() {
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
		//ContainerLocation cl = m.getContainer().getContainerLocation();
		Time startableTime = tw.getMin();
		Load l = new Load(tw, m, startableTime);
		if (workload.size() == 0) {
			/*if (cl != null) {
				Slot sDest = Terminal.getInstance().getSlot(cl.getSlotId());
				try {
					startableTime = new Time(tw.getMin(), new Time(straddleCarrier.getRouting().getShortestPath(straddleCarrier.getSlot().getLocation(),sDest.getLocation()).getCost()), false);
				} catch (NoPathFoundException e) {
					e.printStackTrace();
				}
			}
			l.setStartableTime(startableTime);*/
			workload.add(l);
		} else {
			best = null;
			int index = workload.size();
			int bestIndex = index;
			while (index >= 0) {
				workload.add(index, l);
				ScheduleScore score = getScore();
				if (best == null || score.compareTo(best) > 0) {
					best = score;
					display("[score at "+index+" : "+best.toString()+" new best!");
					bestIndex = index;
				} else {
					display("[score at "+index+" : "+score.toString());
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
						Slot sDest = Terminal.getInstance().getSlot(contLoc.getSlotId());
						try {
							st = new Time(load.getMission().getPickupTimeWindow().getMin(),
									new Time(straddleCarrier.getRouting().getShortestPath(
											straddleCarrier.getLocation(),
											sDest.getLocation())
											.getCost()),
											false);
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
		Terminal.getInstance().missionAffected(l, straddleCarrierID);
	}

	private void display(String txt){
		//System.err.println(txt);
	}
	
	public ScheduleScore getScore() {
		display("---- debut ----");
		ScheduleScore score = new ScheduleScore();
		try {

			Location origin = straddleCarrier.getLocation();
			Time tOrigin = Terminal.getInstance().getTime();
			if (straddleCarrier.getCurrentLoad() != null) {
				ContainerLocation cl = straddleCarrier.getCurrentLoad()
						.getMission().getDestination();
				origin = new Location(Terminal.getInstance().getRoad(cl.getLaneId()),
						cl.getCoordinates(), true);
				tOrigin = straddleCarrier.getCurrentLoad().getMission()
						.getDeliveryTimeWindow().getMax();
			}
			display("-- origin : "+origin);
			display("-- tOrigin : "+tOrigin);
			int i = 0;
			for (Load l : workload) {
				if (l.getState() == MissionState.STATE_TODO) {
					i++;
					Time startableTime = l.getStartTime();
					display("-- startableTime : "+startableTime);
					if(startableTime != null){
						tOrigin = Time.max(tOrigin, startableTime);
					} else {
						tOrigin = Time.max(tOrigin, l.getMission().getPickupTimeWindow().getMin());
					}
					
					display("-- tOrigin : "+tOrigin);
					
					display("-- score["+i+"] = "+l.getMission().getId()+" "+startableTime+" "+tOrigin+" "+origin.toString()+" => "+score.toString());
					ContainerLocation cl = l.getMission().getContainer()
							.getContainerLocation();
					Bay lane = Terminal.getInstance().getBay(cl.getLaneId());
					Location pickup = new Location(lane, cl.getCoordinates(),
							true);
					Path p;
					p = straddleCarrier.getRouting().getShortestPath(origin,
							pickup, tOrigin);

					Time t = new Time(p.getCost());
					score.setDistance(score.getDistance() + p.getCostInMeters(), score.getDistanceInSec()+p.getCost());
					Time pickupTime = new Time(tOrigin, t);
					
					Time hTime = straddleCarrier.getMaxContainerHandlingTime(lane);
					pickupTime.add(hTime);
					display("--- P at "+pickupTime+" ("+ l.getMission().getPickupTimeWindow().getMin()+") handlingT : "+hTime);
					
					Time lateness = new Time(pickupTime, l.getMission().getPickupTimeWindow().getMax(), false);
					// Pickup tardiness
					Time localTardiness = Time.max(Time.MIN_TIME, lateness);
					
					
					score.getTardiness().add(localTardiness);
					display("-- Pickup tardiness : "+localTardiness+" ("+score.getTardiness()+")");
					//Pickup earliness
					Time earliness = new Time(l.getMission().getPickupTimeWindow().getMin(), pickupTime, false);
					Time localEarliness = Time.max(Time.MIN_TIME, earliness);
					score.getEarliness().add(localEarliness);
					display("-- Pickup earliness : "+localEarliness+" ("+score.getEarliness()+")");
										
					tOrigin = new Time(pickupTime, localEarliness);

					// -> Delivery
					cl = l.getMission().getDestination();
					lane = Terminal.getInstance().getBay(cl.getLaneId());
					Location delivery = new Location(lane, cl.getCoordinates(),
							true);
					p = straddleCarrier.getRouting().getShortestPath(pickup,
							delivery, tOrigin);
					t = new Time(p.getCost());
					score.setDistance(score.getDistance() + p.getCostInMeters(), score.getDistanceInSec() + p.getCost());
					Time deliveryTime = new Time(tOrigin, t);
					hTime = straddleCarrier.getMaxContainerHandlingTime(lane);
					display("--- D at "+deliveryTime+" ("+ l.getMission().getDeliveryTimeWindow().getMin()+") handlingT : "+hTime);
					
					lateness = new Time(deliveryTime, l.getMission().getDeliveryTimeWindow().getMax(), false);
					localTardiness = Time.max(Time.MIN_TIME, lateness);
					
					// Delivery tardiness
					score.getTardiness().add(localTardiness);
					display("-- Delivery tardiness : "+localTardiness+" ("+score.getTardiness()+")");
					//Delivery earliness
					earliness = new Time(l.getMission().getDeliveryTimeWindow().getMin(), deliveryTime, false);
					localEarliness = Time.max(Time.MIN_TIME, earliness);
					score.getEarliness().add(localEarliness);
					display("-- Delivery earliness :"+localEarliness+" ("+score.getEarliness()+")");

					deliveryTime.add(hTime);
					
					// Next load
					tOrigin = new Time(deliveryTime, localEarliness);
					origin = delivery;
				}
			}

			// Delivery->Depot
			Path p = straddleCarrier.getRouting().getShortestPath(origin,
					straddleCarrier.getSlot().getLocation(), tOrigin);
			score.setDistance(score.getDistance() + p.getCostInMeters(), score.getDistanceInSec() + p.getCost());
		} catch (NoPathFoundException e) {
			e.printStackTrace();
		}
		return score;
	}

	public void insert(Load l) {
		boolean added = false;
		if (l.isLinked())
			insertAfter(l, l.getPrethreadCondition().getConditionLoad());
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

		Terminal.getInstance().missionAffected(l, straddleCarrierID);
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
		//ContainerLocation cl = m.getContainer().getContainerLocation();
		Time startableTime = tw.getMin();
		/*if (cl != null) {
			Slot sDest = Terminal.getInstance().getSlot(cl.getSlotId());
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
		}*/

		Load l = new Load(tw, m, startableTime);
		insert(l);
	}

	public void removeMission(String mission)
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
			Condition condition = l.getPrethreadCondition();
			if (index < workload.size() - 1) {
				Load next = workload.get(index + 1);
				if (next.getPrethreadCondition() != null && next.getPrethreadCondition().getConditionLoad() == l) {
					next.setLinkedLoad(condition == null ? null : condition.getConditionLoad());
					if (condition != null) {
						next.setStartableTime(new Time(condition.getConditionMission()
								.getDeliveryTimeWindow().getMin()));
					} else {
						ContainerLocation cl = next.getMission().getContainer()
								.getContainerLocation();
						if (cl != null) {
							Slot sDest = Terminal.getInstance().getSlot(cl.getSlotId());
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
					Terminal.getInstance().missionAffected(l, NON_AFFECTED);

					Load removed = workload.remove(i);
					workloadMap.remove(removed.getMission().getId());
					// System.err.println("REMOVING "+mission+" of "+straddleCarrierID+"'s WORKLOAD : done.");
					if (removed.getPrethreadCondition() != null)
						return remove(removed.getPrethreadCondition().getConditionMission()
								.getId());
					else
						return removed;
				} else {
					Load toRemove = workload.get(i);
					// System.err.println("REMOVING "+mission+" of "+straddleCarrierID+"'s WORKLOAD : not done.");
					if (toRemove.getPrethreadCondition() != null)
						return remove(toRemove.getPrethreadCondition().getConditionMission()
								.getId());
					else
						return workload.remove(i);
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


	public boolean isEmpty() {
		for(Load l : workload){
			if(l.getState()!=MissionState.STATE_ACHIEVED){
				return false;
			}
		}
		return true;
	}

	public void removeUnstartedMissions() {
		List<String> mList = new ArrayList<>(workload.size());
		for(Load l : workload)
			if(l.getState() == MissionState.STATE_TODO)
				mList.add(l.getMission().getId());
		for(String mID : mList){
			try {
				remove(mID);
			} catch (MissionNotFoundException e) {
				//Mission already removed by chained process.
			}
		}
	}
}
