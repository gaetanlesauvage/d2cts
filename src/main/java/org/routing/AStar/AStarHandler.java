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
import java.util.List;
import java.util.Map;

import org.display.TextDisplay;
import org.routing.RoutingAlgorithm;
import org.routing.path.RoutingPath;
import org.system.Terminal;
import org.vehicles.StraddleCarrier;
import org.vehicles.models.SpeedCharacteristics;

public class AStarHandler extends RoutingAlgorithm {
	protected SpeedCharacteristics speed;
	private Map<String, Map<String, AStar>> computed;
	private AStarHeuristic h;
	public static final String rmiBindingName = "AStar";

	public AStarHandler(StraddleCarrier rsc) {
		super(rsc, rmiBindingName + ":" + rsc.getId());
		this.straddleCarrierId = rsc.getId();
		if (super.vehicle == null) {
			System.out.println("super.vehicle == null !!!");
		}
		speed = super.vehicle.getModel().getSpeedCharacteristics();
		computed = new HashMap<>();
	}

	public AStarHandler(StraddleCarrier straddleCarrier,
			TextDisplay uriRemoteDisplay) {
		super(straddleCarrier, rmiBindingName + ":" + straddleCarrier.getId(),
				uriRemoteDisplay);
		speed = vehicle.getModel().getSpeedCharacteristics();
		computed = new HashMap<>();
	}

	public void compute(String from, String to) {
		// System.out.print(" COMPUTING AStar FOR "+straddleCarrierId+" FROM "+from+" TO "+to+"...");
		// long timeBefore = System.currentTimeMillis();
		AStar nas = new AStar(from, to, speed);
		nas.setHeuristic(h);

		nas.compute();

		Map<String, AStar> toMap;
		if (computed.containsKey(from)) {
			toMap = computed.get(from);
		} else
			toMap = new HashMap<String, AStar>();
		toMap.put(to, nas);
		computed.put(from, toMap);

		// long timeAfter = System.currentTimeMillis();
		// System.out.println(" DONE ("+(timeAfter-timeBefore)+"ms).");
	}

	@Override
	public RoutingPath getShortestPath(String from, String to) {
		Map<String, AStar> fromAStar = computed.get(from);
		AStar toAStar = null;
		if (fromAStar == null) {
			compute(from, to);
			fromAStar = computed.get(from);
		}
		toAStar = fromAStar.get(to);
		if (toAStar == null) {
			compute(from, to);
			toAStar = fromAStar.get(to);
		}
		RoutingPath l = toAStar.getShortestNodePath();
		return l;
	}

	@Override
	public void graphHasChanged() {
		if (!isLocked()) {
			init();
		}
	}

	public void init() {
		computed.clear();
	}

	public List<String> getConnexNodes(String node) {
		return Terminal.getInstance().getNode(node).getConnexNodesIds();
	}

	public double getDistance(String from, String to) {
		double d = 0;

		d = getShortestPath(from, to).getCost();

		return d;
	}

	public void setHeuristic(AStarHeuristic heuristic) {
		h = heuristic;
	}
}
