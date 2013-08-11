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
package org.routing.reservable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeSet;

import org.routing.path.RoutingPath;
import org.system.Reservation;
import org.time.Time;

public class RDijkstra {
	private Map<String, RDijkstraNode> nodes;
	private String origin;
	private RDijkstraHandler handler;
	private Time startTime;

	public RDijkstra(RDijkstraHandler handler, String origin, Time startTime) {
		this.origin = origin;
		nodes = new HashMap<>();
		this.handler = handler;
		this.startTime = startTime;
	}

	public void compute() {
		init();
		ArrayList<String> computed = new ArrayList<String>();
		TreeSet<RDijkstraNode> list = new TreeSet<RDijkstraNode>();
		list.add(nodes.get(origin));
		while (!list.isEmpty()) {
			RDijkstraNode runningNode = list.pollFirst();
			String runningNodeId = runningNode.getNodeId();
			for (String neighborNodeId : handler.getConnexNodes(runningNodeId)) {
				RDijkstraNode neighboorNode = nodes.remove(neighborNodeId);

				if (!neighborNodeId.equals(runningNodeId)&& !computed.contains(neighborNodeId)) {
					// double d = handler.getDistance(runningNodeId,
					// neighborNodeId);
					// double speed = handler.getSpeed(runningNodeId,
					// neighborNodeId, runningNode.getArrivalTime());
					// double w = handler.getWaitingTime(runningNodeId,
					// neighborNodeId, runningNode.getArrivalTime(),
					// Reservation.PRIORITY_GO_THROUGH);

					// double neighboorCost = (d / speed) + w;
					double neighboorCost = handler.getCost(runningNodeId,
							neighborNodeId, runningNode.getArrivalTime(),
							Reservation.PRIORITY_GO_THROUGH);
					double neighboorCostInM = handler.getDistance(
							runningNodeId, neighborNodeId);
					double newCost = runningNode.getCost() + neighboorCost;

					double oldCost = neighboorNode.getCost();

					if (oldCost >= newCost) {
						neighboorNode.setParent(runningNode);

						if (newCost < oldCost) {
							if (list.contains(neighboorNode)) {
								boolean b = list.remove(neighboorNode);
								if (!b)
									System.out
											.println("NO WAY TO REMOVE ELEM !!!!");
							}
							neighboorNode.setCost(newCost);
							// TODO;
							neighboorNode.setCostInMeters(runningNode
									.getCostInMeters() + neighboorCostInM);

							if(runningNode.getArrivalTime() == null){
								System.err.println("Aoutch!");
							}
							neighboorNode.setArrivalTime(new Time(runningNode.getArrivalTime(), new Time(neighboorCost)));
							// neighboorNode.setArrivalTime(new
							// Time(runningNode.getArrivalTime(), new
							// Time(newCost+"s")));
							neighboorNode.setWaitingTime(handler
									.getWaitingTime(runningNodeId,
											neighborNodeId,
											runningNode.getArrivalTime(),
											Reservation.PRIORITY_GO_THROUGH));
							boolean b = list.add(neighboorNode);
							if (!b)
								System.out.println("ELEM NOT ADDED !!!!");
						}
					}
				}
				nodes.put(neighborNodeId, neighboorNode);
			}
			computed.add(runningNodeId);
		}
	}

	public RoutingPath getShortestNodePathTo(String dest) {
		RoutingPath path = new RoutingPath();
		boolean nopath = false;
		RDijkstraNode destNode = nodes.get(dest);
		RDijkstraNode d = destNode;
		boolean waitingTimePositive = false;
		if (d.getWaitingTime() > 0)
			waitingTimePositive = true;
		path.add(0, d);

		while (!d.getNodeId().equals(origin) && !nopath) {
			if (!d.hasParent() || d.getParent().getNodeId().equals(d.getNodeId()))
				nopath = true;
			else {
				RDijkstraNode parent = d.getParent();

				path.add(0, parent);
				d = parent;
				if (d.getWaitingTime() > 0)
					waitingTimePositive = true;
			}
		}

		path.setCost(destNode.getCost());
		path.setCostInMeters(destNode.getCostInMeters());

		if (waitingTimePositive)
			System.out.println("This path contains a waiting time !!! : "
					+ path);
		return path;
	}

	public void init() {
		nodes.clear();
		for (String s : handler.getNodesIds()) {
			nodes.put(s, new RDijkstraNode(s));
		}
		RDijkstraNode origin = nodes.get(this.origin);
		origin.setCost(0.0);
		origin.setWaitingTime(0.0);
		if(startTime == null){
			System.err.println("Aoutch!");
		}
		origin.setArrivalTime(startTime);
	}

	public RDijkstraNode getNode(String id) {
		return nodes.get(id);
	}
}
