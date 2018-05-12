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
package org.routing.APSP;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import org.display.TextDisplay;
import org.routing.RoutingAlgorithm;
import org.routing.path.RoutingPath;
import org.system.Road;
import org.system.RoadPoint;
import org.system.Terminal;
import org.system.container_stocking.Bay;
import org.vehicles.StraddleCarrier;
import org.vehicles.models.SpeedCharacteristics;

public class APSP extends RoutingAlgorithm {

	private String straddleCarrierId;
	protected SpeedCharacteristics speed;
	private HashMap<String, Info> map;
	public static final String rmiBindingName = "APSP";
	private HashMap<String, HashMap<String, Double>> distances;

	public APSP(StraddleCarrier straddleCarrier) {
		super(straddleCarrier, rmiBindingName + ":" + straddleCarrier.getId());

		this.straddleCarrierId = straddleCarrier.getId();

		map = new HashMap<String, Info>();
		if (!isLocked()) {
			compute();
		}
	}

	public APSP(StraddleCarrier straddleCarrier, TextDisplay uriRemoteDisplay) {
		super(straddleCarrier, rmiBindingName + ":" + straddleCarrier.getId(),
				uriRemoteDisplay);

		this.straddleCarrierId = straddleCarrier.getId();

		map = new HashMap<String, Info>();
		if (!isLocked()) {
			compute();
		}
	}

	public void compute() {

		HashMap<String, RoadPoint> nodes = Terminal.getInstance().getNodes();
		String[] crossroadsNames = nodes.keySet().toArray(
				new String[nodes.size()]);
		init(nodes.values());
		int nbEdges = Terminal.getInstance().getEdgeCount();
		System.out.print(" COMPUTING NEW APSP (with " + crossroadsNames.length
				+ " vertices and " + nbEdges + " edges) FOR "
				+ straddleCarrierId + " ...");
		long timeBefore = System.currentTimeMillis();
		int i = 0;
		int dixPourcent = crossroadsNames.length / 10;
		for (String from : crossroadsNames) {
			compute(from, crossroadsNames);
			if (i == dixPourcent) {
				System.out.print("*");
				i = 0;
			}
			i++;
		}
		long timeAfter = System.currentTimeMillis();
		System.out.println(" DONE (" + (timeAfter - timeBefore) + "ms).");

		/*
		 * TODO Check why ! try { StraddleCarrier rsc =
		 * (StraddleCarrier
		 * )NetworkConfiguration.getRMIInstance().get(straddleCarrierId);
		 * rsc.changePath(); } catch (RegisteredObjectNotFoundException e) {
		 * 
		 * e.printStackTrace(); }
		 */

	}

	private void compute(String kId, String[] crossroadsNames) {
		// int i=1;
		boolean debug = false;

		// i++;
		for (String iId : crossroadsNames) {
			for (String jId : crossroadsNames) {
				// System.out.println("k: "+kId+" i: "+iId+" j: "+jId);
				Info I = map.get(iId);

				Info K = map.get(kId);
				// if(I == null) System.out.println("I is null !");
				double Dij = I.getLengthTo(jId);
				double Dik = I.getLengthTo(kId);
				double Dkj = K.getLengthTo(jId);
				if (Dik >= 0 && Dkj >= 0) {
					double sum = Dik + Dkj;

					if (Dij >= 0) {
						if (sum < Dij) {
							I.setLengthTo(jId, sum, K);
							map.put(iId, I);
						}
					} else {
						I.setLengthTo(jId, sum, K);
						map.put(iId, I);
					}
				}

			}
		}
		if (debug) {
			System.out.println("TABLE for " + kId + " : ");
			Info info = map.get(kId);
			System.out.println(info);
		}
	}

	@Override
	public RoutingPath getShortestPath(String from, String to) {
		Info iFrom = map.get(from);
		return iFrom.getShortestNodePathTo(to);
	}

	@Override
	public void graphHasChanged() {
		if (!isLocked()) {
			compute();
		}
	}

	private void init(Collection<RoadPoint> nodes) {
		System.out.println("Initializing APSP...");
		if (speed == null) {
			speed = vehicle.getModel().getSpeedCharacteristics();
		}

		distances = new HashMap<String, HashMap<String, Double>>(nodes.size());
		for (RoadPoint rp : nodes) {
			List<String> neighbors = rp.getConnexNodesIds();
			HashMap<String, Double> d = new HashMap<String, Double>(
					neighbors.size());
			for (String s : neighbors) {
				Road r = Terminal.getInstance().getRoadBetween(rp.getId(), s);
				double speed = r instanceof Bay ? this.speed.getLaneSpeed()
						: this.speed.getSpeed();
				d.put(s, (r.getExactLengthBetween(rp.getId(), s)) / speed);
			}
			distances.put(rp.getId(), d);

		}

		map.clear();
		for (RoadPoint rp : nodes) {
			map.put(rp.getId(), new Info(this, rp));
		}
		System.out.println("Init done !");
	}

	public double getDistance(String from, String to) {
		return distances.get(from).get(to);
	}

	public List<String> getConnexNodes(String node) {
		return new ArrayList<String>(distances.get(node).keySet());
	}
}
