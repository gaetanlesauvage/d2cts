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
import java.util.HashMap;
import java.util.List;

import org.routing.path.RoutingNode;
import org.routing.path.RoutingPath;
import org.system.RoadPoint;

public class Info extends RoutingNode {
	private HashMap<String, TargetPath> targets;

	public Info(APSP apsp, RoadPoint from) {
		super(from.getId(), null, 0, 0);
		targets = new HashMap<String, TargetPath>();
		List<String> l = from.getConnexNodesIds();
		setLengthTo(this.getId(), 0, null);
		for (String other : l) {
			targets.put(other,
					new TargetPath(other,
							apsp.getDistance(from.getId(), other), null));
		}
	}

	protected int expandPath(int pos, Info source, TargetPath path,
			ArrayList<String> nodePath) {
		if (path.getPassBy() != null) {
			nodePath.add(pos, path.getPassBy().id);
			TargetPath path1 = source.targets.get(path.getPassBy().id);
			TargetPath path2 = path.getPassBy().targets.get(path.getTarget());

			int added1 = expandPath(pos, source, path1, nodePath);
			int added2 = expandPath(pos + 1 + added1, path.getPassBy(), path2,
					nodePath);

			return added1 + added2 + 1;
		} else
			return 0;
	}

	public String getId() {
		return id;
	}

	public double getLengthTo(String dest) {
		if (targets.containsKey(dest))
			return targets.get(dest).getDistance();

		return -1;

	}

	public RoutingPath getShortestNodePathTo(String destination) {
		TargetPath tpath = targets.get(destination);
		if (tpath == null)
			System.out.println("Tpath = null !!! (" + this.id + " to "
					+ destination + ")");

		if (tpath != null) {
			ArrayList<String> nodePath = new ArrayList<String>();
			RoutingPath p = new RoutingPath();

			nodePath.add(this.id);
			nodePath.add(tpath.getTarget());

			expandPath(1, this, tpath, nodePath);
			RoutingNode parent = null;
			for (String s : nodePath) {
				double len = getLengthTo(s);
				RoutingNode n = new RoutingNode(s, parent, len, len);
				// DijkstraNode d = new DijkstraNode(null, s,0);
				p.add(n);
				parent = n;
			}
			p.setCost(getLengthTo(destination));
			p.setCostInMeters(getLengthTo(destination));
			return p;
		}
		return null;
	}

	public void setLengthTo(String other, double length, Info passBy) {
		targets.put(other, new TargetPath(other, length, passBy));
	}

	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("PATHES FROM " + id + " \n");
		for (String dest : targets.keySet()) {
			sb.append("PATH TO " + dest + " : " + targets.get(dest) + "\n");
		}
		return sb.toString();
	}

	@Override
	public double getCost() {
		return 0;
	}
}
