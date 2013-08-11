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
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.display.TextDisplay;
import org.routing.RoutingAlgorithm;
import org.routing.path.RoutingPath;
import org.system.Road;
import org.system.RoadPoint;
import org.system.Terminal;
import org.system.container_stocking.Bay;
import org.vehicles.StraddleCarrier;
import org.vehicles.models.SpeedCharacteristics;

public class DijkstraHandler extends RoutingAlgorithm {
	protected String straddleCarrierId;
	protected SpeedCharacteristics speed;
	private Map<String, Dijkstra> computed;
	public static final String rmiBindingName = "Dijkstra";
	private static Map<String, Map<String, Double>> distances;

	public DijkstraHandler(StraddleCarrier straddleCarrier) {
		super(straddleCarrier, rmiBindingName + ":" + straddleCarrier.getId());
		this.straddleCarrierId = straddleCarrier.getId();

		speed = straddleCarrier.getModel().getSpeedCharacteristics();

		computed = new HashMap<>();
		init();
	}

	public DijkstraHandler(StraddleCarrier straddleCarrier,
			TextDisplay uriRemoteDisplay) {
		super(straddleCarrier, rmiBindingName + ":" + straddleCarrier.getId(),
				uriRemoteDisplay);
		this.straddleCarrierId = straddleCarrier.getId();
		speed = straddleCarrier.getModel().getSpeedCharacteristics();
		computed = new HashMap<>();
		init();
	}

	public void compute(String from) {
		// long timeBefore = System.currentTimeMillis();
		Dijkstra nd = new Dijkstra(this, from);

		nd.compute();
		computed.put(from, nd);
		// long timeAfter = System.currentTimeMillis();
	}

	@Override
	public RoutingPath getShortestPath(String from, String to) {
		if (!computed.containsKey(from)) {
			compute(from);
		}
		Dijkstra nd = computed.get(from);
		RoutingPath l = nd.getShortestNodePathTo(to);
		return new RoutingPath(l);
	}

	@Override
	public void graphHasChanged() {
		if (!isLocked()) {
			init();
		}
	}

	public List<String> getConnexNodes(String from) {
		Map<String, Double> map = distances.get(from);
		if (map == null)
			System.out.println("distances.get(" + from + ")=null !");

		return new ArrayList<String>(distances.get(from).keySet());
	}

	public void init() {

		computed.clear();

		if (distances == null) {
			System.out.println("Initializing Dijkstra...");
			long t1 = System.currentTimeMillis();

			Collection<RoadPoint> list = Terminal.getInstance().getNodes().values();

			distances = new HashMap<>(list.size());
			for (RoadPoint rp : list) {
				List<String> neighbors = rp.getConnexNodesIds();
				HashMap<String, Double> d = new HashMap<String, Double>(
						neighbors.size());
				for (String s : neighbors) {
					Road r = Terminal.getInstance().getRoadBetween(rp.getId(), s);
					double speed = r instanceof Bay ? this.speed.getLaneSpeed()
							: this.speed.getSpeed();
					double w = r.getExactLengthBetween(rp.getId(), s);
					// System.out.println("W("+rp.getId()+" , "+s+") = "+w+" / "+speed+" = "+(w/speed));
					d.put(s, w / speed);
				}
				distances.put(rp.getId(), d);

			}
			long t2 = System.currentTimeMillis();
			System.out.println("Init done ! (" + (t2 - t1) + "ms)");
		}
	}

	public double getDistance(String from, String to) {
		return distances.get(from).get(to);
	}

	public List<String> getNodesIds() {
		return new ArrayList<String>(distances.keySet());
	}

	@Override
	public void destroy() {
		super.destroy();
		distances = null;

		computed = null;
		speed = null;
		straddleCarrierId = null;
	}
}
