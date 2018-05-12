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

import org.routing.path.RoutingNode;

public class AStarNode extends RoutingNode implements Comparable<AStarNode> {
	private AStarNode parent;
	private double g;
	private double h;

	public AStarNode(String id, AStarNode parent, double g, double h) {
		super(id, parent, g + h, g + h);
		this.parent = parent;
		this.g = g;
		this.h = h;
		// super.costToOrigin = g+h;
	}

	public int compareTo(AStarNode asn) {
		if (costToOrigin == asn.costToOrigin) {
			return id.compareTo(asn.id);
		} else if (costToOrigin < asn.costToOrigin)
			return -1;
		else
			return 1;
	}

	public double getG() {
		return g;
	}

	public double getH() {
		return h;
	}

	public AStarNode getParent() {
		return parent;
	}

	public double getSumGH() {
		return costToOrigin;
	}

	public String toString() {
		return id + " from " + (parent == null ? "null" : parent.id) + " g="
				+ g + " h=" + h + " f=" + costToOrigin;
	}
}
