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
package org.routing.dijkstra;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.TreeSet;

import org.routing.path.RoutingNode;
import org.routing.path.RoutingPath;

public class Dijkstra {
	private Hashtable<String, DijkstraNode> nodes;

	private String origin;
	private DijkstraHandler handler;

	public Dijkstra(DijkstraHandler handler, String origin) {
		this.origin = origin;

		nodes = new Hashtable<String, DijkstraNode>();
		this.handler = handler;
	}

	public void displayList(TreeSet<DijkstraNode> t) {
		System.out.println("--------- LIST ------------");
		while (!t.isEmpty()) {
			System.out.println(t.pollFirst());
		}
		System.out.println("--------- END LIST ------------");
	}

	public void compute() {
		init();
		ArrayList<String> computed = new ArrayList<String>();
		TreeSet<DijkstraNode> list = new TreeSet<DijkstraNode>();
		list.add(nodes.get(origin));
		while (!list.isEmpty()) {
			DijkstraNode runningNode = list.pollFirst();
			String runningNodeId = runningNode.getNodeId();
			for (String neighborNodeId : handler.getConnexNodes(runningNodeId)) {
				DijkstraNode neighboorNode = nodes.remove(neighborNodeId);

				if (!computed.contains(neighborNodeId)) {
					double w = handler.getDistance(runningNodeId,
							neighborNodeId);
					double newCost = runningNode.getCost() + w;
					double oldCost = neighboorNode.getCost();
					if (oldCost >= newCost) {
						// if(oldCost == newCost)
						// neighboorNode.addToParents(runningNode);
						// else neighboorNode.addParent(runningNode);
						neighboorNode.setParent(runningNode);
						if (newCost < oldCost) {
							if (list.contains(neighboorNode)) {
								boolean b = list.remove(neighboorNode);
								if (!b)
									System.out
											.println("NO WAY TO REMOVE ELEM !!!!");
							}
							neighboorNode.setCost(newCost);
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
		DijkstraNode destNode = nodes.get(dest);
		// System.out.println("DestNode.cost = "+destNode.getCost());
		RoutingNode d = destNode;
		path.add(0, d);

		while (!d.getNodeId().equals(origin) && !nopath) {
			if (!d.hasParent() || d.getParent().getNodeId().equals(d.getNodeId())) {
				// System.out.println(d.getNodeId()+" has no parent !");
				nopath = true;
			} else {
				RoutingNode parent = d.getParent();
				// System.out.println(d.getNodeId()+" parent = "+parent.getNodeId());
				if(!parent.getNodeId().equals(d.getNodeId()))
					path.add(0, parent);
				d = parent;
			}
		}
		path.setCost(destNode.getCost());
		return path;
	}

	public void init() {
		nodes.clear();
		for (String s : handler.getNodesIds()) {
			nodes.put(s, new DijkstraNode(s, Double.POSITIVE_INFINITY, 0));
		}
		nodes.get(origin).setCost(0.0);
	}

	public DijkstraNode getNode(String id) {
		return nodes.get(id);
	}

}
