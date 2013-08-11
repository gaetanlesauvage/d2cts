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
package org.routing.AStar;

import java.util.HashMap;
import java.util.TreeSet;

import org.routing.path.RoutingPath;
import org.system.Road;
import org.system.RoadPoint;
import org.system.Terminal;
import org.vehicles.models.SpeedCharacteristics;

public class AStar {
	private String origin, destination;
	private static Terminal rt;
	private SpeedCharacteristics speed;
	private HashMap<String, String> cameFrom;
	private TreeSet<AStarNode> open;
	private HashMap<String, AStarNode> openDic;
	private HashMap<String, AStarNode> close;
	private AStarHeuristic heuristic;
	private RoutingPath path;

	public AStar(String origin, String dest, SpeedCharacteristics s) {
		this.origin = origin;
		this.destination = dest;
		if (AStar.rt == null) {
			AStar.rt = Terminal.getInstance();
		}
		this.speed = s;
	}

	private RoutingPath buildPath(AStarNode endNode) {
		RoutingPath path = new RoutingPath();
		AStarNode n = endNode;
		while (n != null) {
			path.add(0, n);
			n = n.getParent();
		}
		return path;
	}

	public void compute() {
		init();

		RoadPoint rpOrigin = rt.getNode(origin);
		RoadPoint rpDestination = rt.getNode(destination);
		AStarNode first = new AStarNode(origin, null, 0, heuristic.heuristic(
				rpOrigin, rpDestination));
		open.add(first);
		openDic.put(first.getNodeId(), first);

		while (!open.isEmpty()) {
			AStarNode runningNode = open.first();

			if (runningNode.getNodeId().equals(destination)) {
				path = buildPath(runningNode);
				return;
			} else {
				open.pollFirst();
				openDic.remove(runningNode.getNodeId());

				close.put(runningNode.getNodeId(), runningNode);
				RoadPoint rpRuninngNode = rt.getNode(runningNode.getNodeId());
				for (String connexNode : rpRuninngNode.getConnexNodesIds()) {
					RoadPoint rpConnexNode = rt.getNode(connexNode);
					double h = heuristic.heuristic(rpConnexNode, rpDestination);
					Road by = rt.getRoadBetween(rpRuninngNode.getId(),
							connexNode);
					double g = runningNode.getG()
							+ heuristic.cost(rpRuninngNode, rpConnexNode, by);
					double f = g + h;

					AStarNode inOpen = openDic.get(connexNode);
					if (inOpen != null && inOpen.getSumGH() <= f)
						continue;
					AStarNode inClose = close.get(connexNode);
					if (inClose != null && inClose.getSumGH() <= f)
						continue;
					close.remove(connexNode);
					AStarNode connexStarNode = new AStarNode(connexNode,
							runningNode, g, h);
					open.add(connexStarNode);
					openDic.put(connexNode, connexStarNode);
				}

			}
		}
	}

	public RoutingPath getShortestNodePath() {
		return path;
	}

	public void init() {
		if (cameFrom == null)
			cameFrom = new HashMap<String, String>();
		else
			cameFrom.clear();

		if (open == null) {
			open = new TreeSet<AStarNode>();
			openDic = new HashMap<String, AStarNode>();
		} else
			open.clear();

		if (close == null)
			close = new HashMap<String, AStarNode>();
		else
			close.clear();

		if (heuristic == null)
			heuristic = new DistanceAndSpeedHeuristic(speed);
		// System.out.println("HEURISTIC "+heuristic.getHeuristicName());

		if (path == null)
			path = new RoutingPath();
		else
			path.clear();
	}

	public void destroy() {
		rt = null;
	}

	public void setHeuristic(AStarHeuristic h) {
		this.heuristic = h;
	}
}
