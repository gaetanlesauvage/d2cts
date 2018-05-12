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
package org.routing.path;


public class RoutingNode {

	protected String id;
	protected RoutingNode parent;
	protected double costToOrigin;
	protected double costToOriginInMeters;

	// Time when the vehicle will reach this node
	// protected Time arrivalTime;
	// protected Time waitingTime;

	public RoutingNode(String id, RoutingNode parent, double costToOrigin,
			double costToOriginInMeters) {
		this.id = id;
		this.parent = parent;
		this.costToOrigin = costToOrigin;
		this.costToOriginInMeters = costToOriginInMeters;
	}

	/*
	 * public RoutingNode(String id, RoutingNode parent, double costToOrigin,
	 * Time arrivalTime, Time waitingTime) { this.id = id; this.parent = parent;
	 * this.costToOrigin = costToOrigin; this.arrivalTime = arrivalTime;
	 * this.waitingTime = waitingTime; }
	 */
	public String getNodeId() {
		return id;
	}

	public RoutingNode getParent() {
		return parent;
	}

	public boolean hasParent() {
		return parent != null;
	}

	public double getCost() {
		return costToOrigin;
	}

	public double getCostInMeters() {
		return costToOriginInMeters;
	}

	public void setCostInMeters(double costInMeters) {
		this.costToOriginInMeters = costInMeters;
	}

	public void setCost(double cost) {
		this.costToOrigin = cost;
	}

	/*
	 * public Time getArrivalTime(){ return arrivalTime; } public Time
	 * getWaitingTime(){ return waitingTime; }
	 */
	public void setParent(RoutingNode parent) {
		this.parent = parent;
	}

	public String toString() {
		StringBuilder sb = new StringBuilder();
		String sParent = "null";
		if (parent != null)
			sParent = parent.id;

		sb.append(id + " cost= " + costToOrigin + " parent = " + sParent);
		return sb.toString();
	}
}
