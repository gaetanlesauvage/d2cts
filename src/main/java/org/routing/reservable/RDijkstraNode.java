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

import org.routing.path.RoutingNode;
import org.time.Time;



public class RDijkstraNode extends RoutingNode implements Comparable<RDijkstraNode>{
	//Required time to go from ORIGIN to this node
	//private double cost;
	//Waiting time on PARENT before being able to go to this node
	private double w;
	
	private RDijkstraNode parent;
	
	//Arrival date on this node
	private Time t;
	
	
	
	
	public RDijkstraNode (String nodeId){
		super(nodeId, null,Double.POSITIVE_INFINITY, 0);
		this.parent = null;
	}
	
	
	public void setParent(RDijkstraNode parent){
		this.parent = parent;
		super.parent = (RoutingNode)parent;
	}
	
	public RDijkstraNode getParent(){
		return this.parent;
	}
	
	public int compareTo(RDijkstraNode n){
		if(n.id.equals(id)) return 0;
		int comp = Double.compare(getCost(), n.getCost());
		if(comp == 0) {
			int comp2 = Double.compare(this.w, n.w);
			if(comp2 == 0)	return id.compareTo(n.getNodeId());
			else return comp2;
		}
		else return comp;
	}
	
	
	public String toString(){
		StringBuilder sb = new StringBuilder();
		String sParent = "null";
		if(parent != null)
			sParent = parent.getNodeId();
		
		sb.append(id+" cost= "+costToOrigin+" parent = "+sParent+" w="+w+" t="+t);
		return sb.toString();

	}

	public void setWaitingTime(double time) {
		w = time;
	}
	public double getWaitingTime(){
		return w;
	}
	public void setArrivalTime(Time time) {
		t = time;
	}

	public Time getArrivalTime() {
		return t;
	}
}
