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
package org.scheduling.aco.graph;

import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import org.graphstream.graph.Node;
import org.missions.Mission;
import org.scheduling.ScheduleTask;
import org.scheduling.onlineACO.OnlineACOScheduler;

/**
 * Node of the ACO graph. Each AntNode can be used by ants.
 *  
 * @author Ga&euml;tan Lesauvage
 * @since 2011
 */
public abstract class AntNode extends ScheduleTask<AntEdge>{
	static final String DEFAULT_STYLE_COLOR = "rgb(0,0,0)";
	public static final String END_NODE_ID = "SINK";

	//<Origin node ID , AntEdge origin -> this >
	public  SortedMap<String, AntEdge> in;

	public Node node;

	protected AntNode(Mission m) {
		super(m);
		in = new TreeMap<>();
		node = OnlineACOScheduler.getInstance().getGraph().addNode(getID());
		if(OnlineACOScheduler.getInstance().getLayout().isEnabled()) OnlineACOScheduler.getInstance().getLayout().nodeAdded(this);
	}


	public void addOutgoingEdge(AntEdge edge) {
		boolean removeEndLink = false;
		if(outgoingEdges.size()>0 && outgoingEdges.containsKey(EndNode.ID)){
			removeEndLink = true;
		}
		super.addDestination(edge);

		if(removeEndLink){
			AntNode end = OnlineACOScheduler.getInstance().getEndNode();
			OnlineACOScheduler.getInstance().removeEdge(this,end);
			if(OnlineACOScheduler.getInstance().getLayout().isEnabled()) OnlineACOScheduler.getInstance().getLayout().nodeUpdated(end);
		}
	}

	public void addIncomingEdge(AntEdge edge) {
		if(!(in.size()>0 && edge.getNodeFrom().getID().equals(DepotNode.ID))){
			boolean removeDepotLink = false;
			if(in.size()>0 && in.containsKey(DepotNode.ID)){
				removeDepotLink = true;
			}
			in.put(edge.getNodeFrom().getID(), edge);

			if(removeDepotLink){
				AntNode depot = OnlineACOScheduler.getInstance().getDepotNode();
				OnlineACOScheduler.getInstance().removeEdge(depot, this);
			}
			if(OnlineACOScheduler.getInstance().getLayout().isEnabled()) OnlineACOScheduler.getInstance().getLayout().nodeUpdated(this);
		}
	}

	public boolean removeIncomingEdge(AntNode origin){
		AntEdge edge = in.remove(origin.getID());
		if(edge!=null){
			if(edge.getID()!=null){
				edge.destroy();
			}

			if(in.size()==0&&!getID().equals(EndNode.ID)){
				AntNode depot = OnlineACOScheduler.getInstance().getDepotNode();
				if(!depot.outgoingEdges.containsKey(getID())) {
					AntEdge e = new AntEdge(depot, this);
					depot.addOutgoingEdge(e);
					addIncomingEdge(e);
					e.computeWeight();
				}
			}
			return true;
		}
		return false;
	}

	public boolean removeOutgoingEdge(AntNode destination){
		AntEdge edge = outgoingEdges.remove(destination.getID());
		if(edge!=null){
			if(edge.getID()!=null) edge.destroy();

			if(outgoingEdges.size()==0){
				AntNode end = OnlineACOScheduler.getInstance().getEndNode();
				AntEdge e = new AntEdge(this, end);
				end.addIncomingEdge(e);
				addOutgoingEdge(e);
				e.computeWeight();
			}
			return true;
		}
		return false;
	}

	public void destroy(){
		//System.err.println("DESTOY "+getID());
		//IN -> N -> OUT => IN -> OUT
		if(in!=null){
			for(AntEdge e1 : in.values()) {

				AntNode inNode = (AntNode) e1.getNodeFrom();
				if(inNode!=OnlineACOScheduler.getInstance().getDepotNode()){
					for(AntEdge e2 : outgoingEdges.values()){
						AntNode outNode = (AntNode) e2.getNodeTo();
						if(outNode!=OnlineACOScheduler.getInstance().getEndNode()){
							if(inNode.outgoingEdges.containsKey(outNode.getID())) System.err.println("already a link between "+inNode.getID()+" and "+outNode.getID());
							else {
								AntEdge e = new AntEdge(inNode, outNode);
								inNode.addOutgoingEdge(e);
								outNode.addIncomingEdge(e);
								e.computeWeight();
							}
						}
					}
				}
				inNode.removeOutgoingEdge(this);
			}
			for(AntEdge e1 : in.values()) e1.destroy();
			in.clear();
			in = null;
		}
		if(outgoingEdges!=null){
			for(AntEdge e : outgoingEdges.values()){
				((AntNode) e.getNodeTo()).removeIncomingEdge(this);
			}
			for(AntEdge e2 : outgoingEdges.values()){
				e2.destroy();
			}
			outgoingEdges.clear();
		}


		if(node!=null){
			if(OnlineACOScheduler.getInstance().getLayout().isEnabled()) OnlineACOScheduler.getInstance().getLayout().nodeRemoved(this);
			OnlineACOScheduler.getInstance().removeNode(node.getId());
			node = null;
		}
		super.destroy();
	}

	protected AntEdge getIncomingEdgeFrom(AntNode origin){
		return in.get(origin.getID());
	}
	protected Map<String, AntEdge> getOutgoingEdges(){
		return outgoingEdges;
	}
}