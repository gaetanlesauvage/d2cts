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
package org.scheduling.onlineACO;

import java.util.ArrayList;
import java.util.List;

import org.scheduling.aco.graph.AntNode;
/**
 * Path of missions from the start node to the end node with the highest pheromone track
 *  or the minimum cost according to the choice of criteria made by the user.
 *  
 * @author Ga&euml;tan Lesauvage
 * @since 2011
 */
class BestPath {
	/**
	 * Amount of pheromone on the path
	 */
	private double scoreInPH;
	/**
	 * Sum of the weight of edges of the path
	 */
	private double scoreInWeight;
	/**
	 * Path
	 */
	private List<AntNode> path;

	/**
	 * Constructor
	 * @param scoreInPH Amount of pheromone on the path
	 * @param scoreInWeight Sum of the weight of edges of the path
	 * @param path Path
	 */
	public BestPath (double scoreInPH, double scoreInWeight, List<AntNode> path){
		this.scoreInPH = scoreInPH;
		this.scoreInWeight = scoreInWeight;
		this.path = path;
	}

	/**
	 * Getter on the amount of pheromone on the path
	 * @return Amount of pheromone on the path
	 */
	public double getScoreInPH(){
		return scoreInPH;
	}
	/**
	 * Getter on the sum of the weight of edges of the path
	 * @return The sum of the weight of edges of the path
	 */
	public double getScoreInWeight(){
		return scoreInWeight;
	}

	/**
	 * Path
	 * @return The path
	 */
	public List<AntNode> getPath(){
		return path;
	}

	/**
	 * Get the last node of the path or null if it does not exist
	 * @return The last node of the path if any, null otherwise
	 */
	public AntNode getLastNode(){
		if(path.size()>0) return path.get(path.size()-1);
		else return null;
	}

	@Override
	public String toString(){
		StringBuilder sb = new StringBuilder();
		sb.append("SCORE : "+scoreInPH+" ph "+ scoreInWeight+" w PATH : ");
		for(int i=0; i<path.size() ; i++){
			AntNode n = path.get(i);
			sb.append(n.getID());
			if(i<path.size()-1) sb.append(" -> ");
		}
		return sb.toString();
	}

	public boolean contains(String nodeID){
		for(AntNode n : path){
			if(n.getID().equals(nodeID)) return true;
		}
		return false;
	}
	//TODO recompute scores...
	public void removeNode(AntNode n) {
		int index = -1;
		for(int i=0;i<path.size()&&index<0;i++){
			if(path.get(i).getID().equals(n.getID())){
				index = i;
			}
		}
		if(index>-1){
			ArrayList<AntNode> l = new ArrayList<AntNode>(index);
			for(int i=0 ; i<index ; i++){
				l.add(path.get(i));
			}
		//	System.err.println("Node "+n.getId()+" removed from best path");
			path = l;
			/*for(int i=index; i<path.size();i++){
				path.remove(i);
				
			}*/
		}
		//else System.err.println("Node "+n.getId()+" not found in best path");
	}
	
	public int size(){
		return path.size();
	}
}