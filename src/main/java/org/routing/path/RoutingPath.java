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

import java.util.ArrayList;

/**
 * A simple node path. Only used with shortest path algorithms
 * @author gaetan
 *
 */
public class RoutingPath {
	private ArrayList<RoutingNode> path;
	private double cost;
	private double costInMeters;
	
	public RoutingPath(){
		path = new ArrayList<RoutingNode>();
	}
	
	public RoutingPath(RoutingPath l) {
		this.path =  new ArrayList<RoutingNode>(l.size());
		for(RoutingNode n : l.path) path.add(n);
		this.cost = l.cost;
		this.costInMeters = l.costInMeters;
	}

	public void add(RoutingNode n){
		path.add(n);
	}
	
	public void add(int index , RoutingNode n){
		path.add(index, n);
	}
	
	public int size(){
		return path.size();
	}
	
	public void setCost(double cost){
		this.cost = cost;
	}
	
	public void setCostInMeters(double costInMeters){
		this.costInMeters = costInMeters;
	}

	public double getCostInMeters(){
		return costInMeters;
	}
	
	public double getCost(){
		return cost;
	}
	public RoutingNode get(int index){
		return path.get(index);
	}

	public void clear() {
		path.clear();
		cost = 0;
		costInMeters = 0;
	}
	
	public String toString(){
		StringBuilder sb = new StringBuilder();
		sb.append("Path : \n");
		for(RoutingNode rn : path){
			sb.append("\t"+rn+"\n");
		}
		sb.append("Cost = "+cost+" Cost in meters = "+costInMeters);
		return sb.toString();
	}
	
	
}
