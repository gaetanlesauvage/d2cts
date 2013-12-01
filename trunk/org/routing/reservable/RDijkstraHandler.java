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
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.display.TextDisplay;
import org.routing.path.RoutingPath;
import org.system.Road;
import org.system.RoadPoint;
import org.system.Terminal;
import org.time.Time;
import org.vehicles.StraddleCarrier;


public class RDijkstraHandler extends RRoutingAlgorithm {
	private static final Logger logger = Logger.getLogger(RDijkstraHandler.class); 
	
	protected String straddleCarrierId;
	private StraddleCarrier vehicle;

	private static Map<String, Map<String, RDijkstraHelper>> distances;

	public static final String rmiBindingName = "RDijkstra";

	public RDijkstraHandler(StraddleCarrier straddleCarrier) {
		super(straddleCarrier, rmiBindingName + ":" + straddleCarrier.getId());
		this.straddleCarrierId = straddleCarrier.getId();
		vehicle = straddleCarrier;
		init();
	}

	public RDijkstraHandler(StraddleCarrier straddleCarrier, TextDisplay display) {
		super(straddleCarrier, rmiBindingName + ":" + straddleCarrier.getId(),
				display);
		this.straddleCarrierId = straddleCarrier.getId();
		vehicle = straddleCarrier;

		init();
	}

	private RDijkstra compute(String from, Time t) {
		// long timeBefore = System.currentTimeMillis();

		RDijkstra nd = new RDijkstra(this, from, t);

		nd.compute();
		return nd;
		// long timeAfter = System.currentTimeMillis();
	}

	@Override
	public RoutingPath getShortestPath(String from, String to, Time t) {

		RDijkstra nd = compute(from, t);
		RoutingPath l = nd.getShortestNodePathTo(to);

		return new RoutingPath(l);
	}

	@Override
	public void graphHasChanged() {
		// TODO
	}

	public List<String> getConnexNodes(String from) {
		return new ArrayList<String>(distances.get(from).keySet());
	}

	private void init() {
		if (distances == null) {
			logger.info("Initializing RDijkstra for "+vehicle.getId());
			//System.out.println("Initializing RDijkstra...");
			long t1 = System.currentTimeMillis();

			Collection<RoadPoint> list = Terminal.getInstance().getNodes().values();

			distances = new HashMap<>(list.size());
			for (RoadPoint rp : list) {
				List<String> neighbors = rp.getConnexNodesIds();
				Map<String, RDijkstraHelper> d = new HashMap<>(
						neighbors.size());
				for (String s : neighbors) {
					Road r = Terminal.getInstance().getRoadBetween(rp.getId(), s);
					double distance = r.getExactLengthBetween(rp.getId(), s);
					d.put(s, new RDijkstraHelper(r, distance));
//					logger.debug(rp.getId()+"->"+s+" = "+distance);
				}
				distances.put(rp.getId(), d);
			}
			long t2 = System.currentTimeMillis();
			logger.info("RDijkstra init done ! (" + (t2 - t1) + "ms)");
		}
	}

	@Override
	public double getCost(String from, String to, Time arrivalTimeOnFrom,
			int priority) {
		double d = getDistance(from, to);// r.getExactLengthBetween(i.getId(),
		// j.getId());
		double s = getSpeed(from, to, arrivalTimeOnFrom);// vehicle.getSpeed(r,
		// t);
		Road r = distances.get(from).get(to).getRoad();
		double duration = d / s;
		if(Double.isNaN(duration)||Double.isInfinite(duration)){
			System.err.println("Duration is Nan or +Inf!");
		}
		double w = 0;
		w = r.getWaitingCost(vehicle, arrivalTimeOnFrom, duration, priority);
		return duration + w;
	}

	@Override
	public double getDistance(String from, String to) {
		return distances.get(from).get(to).getDistance();
	}

	@Override
	public double getSpeed(String from, String to, Time t) {
		double s = 0;
		s = vehicle.getSpeed(distances.get(from).get(to).getRoad(), t);
		return s;
	}

	public List<String> getNodesIds() {
		return new ArrayList<String>(distances.keySet());
	}

	public double getWaitingTime(String from, String to, Time arrivalTime,
			int priority) {
		Road r;
		r = distances.get(from).get(to).getRoad();
		double w = r.getWaitingCost(vehicle, arrivalTime, priority);
		// if(w>0)
		// System.out.println("W = "+w+" for going on "+from+" -> "+to+" at "+arrivalTime+" with priority "+priority+" !!!!!!!!!!!!");
		return w;
	}
}
