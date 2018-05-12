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

import org.routing.path.RoutingNode;
import org.system.Road;
import org.system.RoadPoint;
import org.system.Terminal;
import org.util.Location;

public class DijkstraNode extends RoutingNode implements Comparable<DijkstraNode> {

	private Location location;

	public DijkstraNode(String nodeId, double cost, double costInMeters) {
		super(nodeId, null, cost, costInMeters);
	}

	public void setCost(double cost) {
		this.costToOrigin = cost;
	}

	public int compareTo(DijkstraNode n) {
		if (n.id.equals(this.id))
			return 0;
		int comp = Double.compare(this.costToOrigin, n.costToOrigin);
		if (comp == 0)
			return id.compareTo(n.getNodeId());
		else
			return comp;
	}

	public Location getLocation() {
		if (location == null) {
			Terminal rt = Terminal.getInstance();

			RoadPoint next = rt.getNode(id);
			RoadPoint parent = rt.getNode(getParent().getNodeId());
			Road r = rt.getRoadBetween(parent.getId(), id);

			double currentRate = Location.getAccuratePourcent(
					parent.getLocation(), r);

			double nextRate = Location.getAccuratePourcent(next.getLocation(),
					r);
			boolean direction = false;
			if (nextRate >= currentRate)
				direction = true;

			location = new Location(r, nextRate, direction);

		}
		return location;
	}
}
