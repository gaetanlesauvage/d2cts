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

import org.scheduling.aco.graph.AntEdge;
import org.scheduling.aco.graph.AntNode;

/**
 * Graphical node layout used to represent a workload as a graph
 * 		 ---<+>---<+>-
 * 		/			  \
 *  <+>----	<+> -------<+>
 *  	\			  /
 * 		 ---<+> ------
 * 
 * @author Ga&euml;tan Lesauvage
 * @since 2011
 */
public class ACOLayout {
	/**
	 * Nodes to locate
	 */
	private List<List<AntNode>> nodes;
	/**
	 * MAX number of node on the same column
	 */
	private int maxSize = 0;

	private boolean enable = true;
	
	/**
	 * Constructor
	 */
	public ACOLayout(){
		nodes = new ArrayList<List<AntNode>>();
	}

	/**
	 * Called when a node is added to the graph
	 * @param n The node added
	 */
	public void nodeAdded (AntNode n){
		int x = 0;
		int xMin = Integer.MAX_VALUE;
		boolean toAdd = false;
		for(AntEdge inEdge : n.in.values()){
			AntNode origin = (AntNode) inEdge.getNodeFrom();
			int xx = getX(origin);
			if(xx >= x){
				x = getX(origin) +1;
			}
			if(xx <= xMin){
				xMin = xx;
			}
		}

		List<AntNode> l = null;
		if(x < nodes.size()){
			l = nodes.get(x);
		}
		else {
			l = new ArrayList<AntNode>();
			toAdd = true;
		}
		l.add(n);
		if(toAdd) nodes.add(x, l);
		else nodes.set(x, l);
		if(l.size() > maxSize) maxSize = l.size();



		n.node.addAttribute("x", x);
		n.node.addAttribute("y", l.size()-1);


		for(AntEdge outEdge : n.getDestinations()){
			if(outEdge.getID()!=null){
				AntNode dest = (AntNode) outEdge.getNodeTo();
				if(dest==null) {
					System.err.println("dest is null!!! "+outEdge.getID());
					new Exception().printStackTrace();
				}
				nodeUpdated(dest);
			}
		}

		optimizeY();

		for(AntEdge outEdge : n.getDestinations()){
			if(outEdge.getID()!=null){
				AntNode dest = (AntNode) outEdge.getNodeTo();
				nodeUpdated(dest);
			}
		}
	}

	/**
	 * Compute the location of each nodes
	 */
	public void apply(){
		for(int i=0 ; i<nodes.size(); i++){
			List<AntNode> level = nodes.get(i);
			for(int j=0 ; j<level.size() ; j++){
				AntNode n = level.get(j);
				n.node.setAttribute("x",i);
			}
		}
		optimizeY();
	}

	/**
	 * Compute the Y location of the nodes
	 */
	private void optimizeY(){
		for(List<AntNode> level : nodes){

			for(int i=0 ; i<level.size() ; i++){
				AntNode n = level.get(i);
				double y = i;
				if(level.size()!=maxSize) y = ((maxSize-1.0)/(level.size()+1.0))*(i+1);
				n.node.addAttribute("y",y);
			}
		}
	}

	/**
	 * Update the location of the given node
	 * @param n Node to recompute
	 */
	public void nodeUpdated (AntNode n){
		if(n==null) {
			System.err.println("n is null!!!");
			new Exception().printStackTrace();
		}
		nodeRemoved(n);
		nodeAdded(n);
	}

	/**
	 * Get the abscissa of the given node
	 * @param n Concerned node
	 * @return Abscissa of the given node
	 */
	private int getX(AntNode n){
		if(n==null) {
			System.err.println("n is null!!!");
			new Exception().printStackTrace();
		}
		else if (n.node == null) System.err.println("n.node is null ("+n.getID()+") !!!");
		if(n.node.hasAttribute("x")){
			return n.node.getAttribute("x");
		}
		else{
			System.err.println(n.getID()+" has no X attribute => return 1");
			return 1;
		}

	}

	/**
	 * Called when a node is removed from the graph. The location of each other node is then recomputed.
	 * @param n The removed node
	 */
	public void nodeRemoved (AntNode n){
		if(n==null) {
			System.err.println("n is null!!!");
			new Exception().printStackTrace();
		}
		List<AntNode> oldLevel = nodes.get(getX(n));
		//If it is the last node of the level then shift down every other levels
		if(oldLevel.size()==1) {
			//Will be removed
			if(getX(n) < nodes.size()-1){
				for(int j = getX(n)+1 ; j<nodes.size(); j++){
					List<AntNode> level = nodes.get(j);
					nodes.set(j-1, level);
					//Apply the row
					for(AntNode node : level) node.node.addAttribute("x", j-1);
				}

			}
			nodes.remove(nodes.size()-1);
		}
		//Else shift left other nodes of the level
		else{
			if(oldLevel.size()==maxSize){
				int times =0;
				for(List<AntNode> l : nodes){
					if(l.size()==maxSize) times++;

				}
				if(times==1) maxSize--;
			}
			for(int j=0;j<oldLevel.size();j++){
				if(oldLevel.get(j).getID().equals(n.getID())){
					oldLevel.remove(j);
					break;
				}
			}
		}
		optimizeY();
	}

	public void enable(){
		enable = true;
	}
	
	public void disable(){
		enable = false;
	}
	
	public boolean isEnabled(){
		return enable;
	}
	@Override
	public String toString(){
		StringBuilder sb = new StringBuilder();
		for(int i=0; i<nodes.size(); i++){
			List<AntNode> level = nodes.get(i);
			sb.append("Level "+i+" : ");
			for(int j=0; j<level.size();j++){
				sb.append("["+level.get(j).node.getAttribute("y")+"]="+level.get(j).getID()+" | ");
			}
			sb.append("\n");
		}
		sb.trimToSize();
		return sb.toString();
	}
}