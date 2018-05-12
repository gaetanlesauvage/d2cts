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

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

import org.exceptions.MissionNotFoundException;
import org.missions.Mission;
import org.scheduling.LocalScore;
import org.scheduling.MissionScheduler;
import org.scheduling.ScheduleEdge;
import org.scheduling.ScheduleResource;
import org.scheduling.ScheduleTask;
import org.scheduling.aco.graph.AntEdge;
import org.scheduling.aco.graph.AntMissionNode;
import org.scheduling.aco.graph.AntNode;
import org.vehicles.StraddleCarrier;
import org.vehicles.StraddleCarrierColor;



/**
 * Ant hill of the ACO based algorithm for the missions scheduling. Each hill is represented by a distinct color and regroup ants of its color.
 * 
 * @author Ga&euml;tan Lesauvage
 * @since 2011
 */
public class AntHill extends ScheduleResource {
	//GUI
	/**
	 * Corresponding color
	 */
	private Color color;

	//ACO
	/**
	 * Ants of the colony
	 */
	private ArrayList<Ant> ants;

	//BEST PATH
	/**
	 * Criteria based on edges weight
	 */
	public static final String WEIGHT_CRITERIA = "weight";
	/**
	 * Criteria based on the pheromone track
	 */
	public static final String PHEROMONE_CRITERIA = "pheromone";
	/**
	 * BestPath in the graph for the corresponding resource 
	 */
	private BestPath currentBest;

	//MAP listing all the matching missions and the weight of the edge between the hill node and each mission
	/**
	 * Map listing all the matching missions
	 */
	private SortedMap<String, AntMissionNode> missions;

	/**
	 * Constructor
	 * @param rsc Resource associated with the new colony
	 * @param scheduler The mission scheduler
	 * @param parameters ACO parameters
	 * @
	 */
	public AntHill(StraddleCarrier rsc)  {
		super(rsc);
		this.color = StraddleCarrierColor.getColor(rsc.getColor());
		this.ants = new ArrayList<Ant>();
		this.missions = new TreeMap<>();
	}

	/* =========================== GETTERS =========================== */
	/**
	 * Getter on the color of the colony in the rgb(r,g,b) form
	 * @return The color of the colony in the rgb(r,g,b) form
	 */
	public String getStyleColor(){
		return "rgb("+color.getRed()+","+color.getGreen()+","+color.getBlue()+")";
	}

	/**
	 * Getter on the color of the colony
	 * @return The color of the colony
	 */
	public Color getColor(){
		return color;
	}

	/**
	 * Get the number of ants in the colony
	 * @return The number of ants in the colony
	 */
	public int getAntCount(){
		return ants.size();
	}

	/**
	 * Get the ant stocked at the given index
	 * @param i Index
	 * @return The ant at the given index
	 */
	public Ant getAnt(int i){
		if(ants.size()>i) return ants.get(i);
		else return null;
	}

	/**
	 * Return true if there is matching missions for the resource, false otherwise
	 * @return True if there is matching missions for the resource, false otherwise
	 */
	public boolean hasMissions(){
		return missions !=null && missions.size()>0; 
	}

	/**
	 * Getter on the matching missions
	 * @return The list of matching missions
	 */
	public List<AntMissionNode> getMissions() {
		return new ArrayList<AntMissionNode>(missions.values());
	}

	/* =========================== MISSIONS HANDLING =========================== */
	/**
	 * Add a mission node
	 * @param n Mission node to add
	 */
	public void addAntMissionNode(AntMissionNode n){
		missions.put(n.getID(), n);
		missionAdded();
	}

	/**
	 * Remove a mission node
	 * @param n Mission to remove
	 */
	public void removeAntMissionNode(AntMissionNode n){
		missions.remove(n.getID());
		missionRemoved(n.getID());
		if(currentBest != null) {
			//System.err.print(ID+" => ");
			currentBest.removeNode(n);
		}
		//else System.err.println(ID+" => currentBest == null");
	}

	/**
	 * Update needed after the add of a mission
	 */
	private void missionAdded(){
		//ADD an ant
		ants.add(new Ant(this));
		OnlineACOScheduler.getInstance().antAdded(getAntCount());
	}

	/**
	 * Update needed after a mission removal
	 * @param mID The ID of the removed mission
	 */
	private void missionRemoved(String mID){
		for(Ant a : ants){
			if(missions.size()==0) a.destroy();
			else if(a.getLocation()!=null && a.getLocation().getID().equals(mID)) a.reset();
		}
		//Clear after to avoid concurrent modification exceptions
		Ant a =	ants.remove(0);
		a.destroy();
		OnlineACOScheduler.getInstance().antRemoved();

		//	boolean notfound = false;
		try {
			vehicle.removeMissionInWorkload(mID);
		} catch (MissionNotFoundException e) {
			//Alright if the mission is not in the workload...
			//		notfound = true;
		}
		//	if(!notfound) System.out.println("MISSION "+mID+" REMOVED IN "+ID+"'s WORKLOAD");
	}


	/* =========================== BEST PATH HANDLING =========================== */
	//	public BestPath getDisjkstraBestPath(String criteria){
	//
	//		//Build the graph associated with the color of the hill
	//		Graph graph = ACOMissionScheduler.getSubGraph(ID);
	//		System.out.print(ID+"'s ");
	//		ACOMissionScheduler.debug(graph);
	//
	//		Dijkstra d = null;
	//		if(criteria.equals(WEIGHT_CRITERIA)) d = new Dijkstra(Element.EDGE,"result", "weight");
	//		else if(criteria.equals(PHEROMONE_CRITERIA)) d = new Dijkstra(Element.NODE, "result", "weight");
	//		else new Exception("Unknown criteria "+criteria+" exception !").printStackTrace();
	//
	//		d.init(graph);
	//		d.setSource(graph.getNode(DepotNode.ID));
	//		d.compute();
	//
	//		List<AntNode> list = new ArrayList<AntNode>();
	//		double scorePH = 0;
	//		double scoreWeight = 0;
	//		AntNode destination = null;
	//		Iterator<Node> it = d.getPathNodesIterator(graph.getNode(DepotNode.ID));
	//		while(it.hasNext()){
	//			Node n = it.next();
	//			AntNode an = ACOMissionScheduler.getNode(n.getId());
	//			list.add(0,an);
	//
	//			if(an instanceof AntMissionNode) scorePH += ((AntMissionNode)an).getPheromone(ID);
	//
	//			if(destination!=null){
	//				AntEdge e = destination.in.get(an.getID());
	//				scoreWeight+= e.getCost(ID);
	//			}
	//
	//			destination = an;
	//		}
	//
	//		//NO PATH TO END NODE ?
	//		if(list.size() == 0){
	//			//GET BEST PATH  p WITH p.length = 1
	//			Node depot = graph.getNode(DepotNode.ID);
	//			Iterator<Edge> itOut = depot.getEachLeavingEdge().iterator();
	//			double maxPH = 0;
	//			double minWeight = Double.POSITIVE_INFINITY;
	//			AntNode bestNode = null;
	//			AntEdge bestEdge = null;
	//			while(itOut.hasNext()){
	//				Edge e = itOut.next();
	//				AntNode dest = ACOMissionScheduler.getNode(e.getTargetNode().getId());
	//				if(dest instanceof AntMissionNode) {
	//					double s = ((AntMissionNode)dest).getPheromone(ID);
	//					if(s>maxPH) {
	//						maxPH = s;
	//						bestNode = dest; 
	//					}
	//				}
	//				AntEdge edge = dest.in.get(e.getSourceNode().getId());
	//				double w = edge.getCost(ID); 
	//				if(w < minWeight){
	//					minWeight = w;
	//					bestEdge = edge;
	//				}
	//			}
	//			if(criteria.equals(WEIGHT_CRITERIA)&&bestEdge!=null){
	//				AntNode nTo = (AntNode) bestEdge.getNodeTo(); 
	//				list.add(nTo);
	//				if(nTo instanceof AntMissionNode) scorePH += ((AntMissionNode)nTo).getPheromone(ID);
	//				scoreWeight+=bestEdge.getCost(ID);
	//			}
	//			else if(criteria.equals(PHEROMONE_CRITERIA)&&bestNode!=null){
	//				list.add(bestNode);
	//				if(bestNode instanceof AntMissionNode) scorePH += ((AntMissionNode)bestNode).getPheromone(ID);
	//				scoreWeight+=bestNode.in.get(depot.getId()).getCost(ID);
	//			}
	//		}
	//
	//		return new BestPath(scorePH, scoreWeight, list);
	//	}
	/**
	 * Computes and retrieves the best path from the last node of the given best path to the end node according to the given criteria
	 * @param path First part of the path. The last node of this path will be used has start node for the current best path.
	 * @param criteria Criteria
	 * @return The new best path
	 */
	public BestPath getBestPath(BestPath path, String criteria){
		BestPath MAX = null;
		AntNode source = path.getLastNode();
		for(AntEdge e : source.getDestinations()){
			AntNode destination = (AntNode) e.getNodeTo();

			if(destination != OnlineACOScheduler.getEndNode()){
				AntMissionNode target = (AntMissionNode)destination;
				if(target.getColor().equals(ID)){
					ArrayList<AntNode> l = new ArrayList<AntNode>(path.getPath().size()+1);
					for(AntNode n : path.getPath()) l.add(n);
					l.add(destination);

					double eCost = 0.0;
					if(source == OnlineACOScheduler.getDepotNode()) eCost = e.getCost(ID);
					else eCost = e.getCost(modelID);

					BestPath b = getBestPath(new BestPath(path.getScoreInPH()+target.getPheromone(ID), path.getScoreInWeight()+eCost, l), criteria);
					if(MAX == null) MAX = b;
					else{
						if(criteria.equals(WEIGHT_CRITERIA)){
							if(b.getScoreInWeight() < MAX.getScoreInWeight()){
								MAX = b;
							}
						}
						else if(criteria.equals(PHEROMONE_CRITERIA)){
							if(b.getScoreInPH()>MAX.getScoreInPH()) {
								MAX = b;
							}
						}
						else new Exception("CRITERIA "+criteria+" UNLNOWN !").printStackTrace();
					}
				}

			}

		}
		return MAX == null ? path : MAX;
	}

	/**
	 * Update the workload of the straddle carrier according to the bestpath changes 
	 * @param bestBeforeUpdate Former best path
	 * @param newBest New best path
	 */
	private void gBestChanged(BestPath bestBeforeUpdate, BestPath newBest){
		//Remove missions of old best of the workload
		if(bestBeforeUpdate != null && bestBeforeUpdate.size()>0){
			List<AntNode> path = bestBeforeUpdate.getPath();
			AntNode n = path.get(path.size()-1);
			if(n instanceof AntMissionNode){
				Mission m = ((AntMissionNode)n).getMission();
				try {
					vehicle.removeMissionInWorkload(m.getId());
				} catch (MissionNotFoundException e) {
					//System.err.println(ID+" => MISSION "+m.getId()+" NOT FOUND IN WORKLOAD So not removed!");
				}
			}
		}

		if(newBest != null){
			List<AntNode> path = newBest.getPath();
			ArrayList<Mission> l = new ArrayList<Mission>();
			for(AntNode n : path){
				if (n instanceof AntMissionNode){
					l.add(((AntMissionNode)n).getMission());
				}
			}
			vehicle.addMissionsInWorkload(l);
			
		}
		currentBest = newBest;

		//DEBUG
		//System.out.println("BEST PATH FOR "+ID+" : "+currentBest+(currentBest == null ? "" : "(SIZE="+currentBest.size()+")"));

	}

	/**
	 * Recompute the best path and update the straddle carrier workload accordingly
	 */
	public void updateBestPath() {

		ArrayList<AntNode> l = new ArrayList<AntNode>();
		l.add(OnlineACOScheduler.getDepotNode());
		BestPath bW = getBestPath(new BestPath(0,0,l), PHEROMONE_CRITERIA);
		//System.err.println(ID+"'s BEST PATH : "+bW);
		if(bW.size()==1) {
			//System.err.println("SET TO NULL");
			bW = null;
		}

		//BestPath bW = getDisjkstraBestPath(PHEROMONE_CRITERIA);
		if(currentBest != null || bW != null){
			gBestChanged(currentBest, bW);
		}
	}

	/**
	 * Reinforce the best path with pheromone (according to lAMBDA parameter) 
	 */
	public void markBestPath() {
		for(AntNode n : currentBest.getPath()){
			if(n instanceof AntMissionNode){
				AntMissionNode amn = (AntMissionNode)n;
				double q = OnlineACOScheduler.getInstance().getGlobalParameters().getLAMBDA();
				amn.spreadInstantPheromone(ID, q);
				System.out.println(q+" pheromone spread on "+amn.getID()+" ph="+amn.getPheromone(ID));
			}
		}
	}
	
	//TODO Online / offline[2] => TEST!
	@SuppressWarnings("unchecked")
	public void visitNode(ScheduleTask<? extends ScheduleEdge> task){
//		System.err.println("VISIT NODE "+task.getID()+" BY "+getID());
		
		List<ScheduleTask<? extends ScheduleEdge>> nodePath = score.getNodePath();
		ScheduleTask<ScheduleEdge> previousTask = (ScheduleTask<ScheduleEdge>)nodePath.get(nodePath.size()-1);
		
		ScheduleEdge edge = null;
		if(previousTask.exists()) {
			edge = previousTask.getEdgeTo((ScheduleTask<ScheduleEdge>) task);
		}
		else{
			edge = OnlineACOScheduler.getDepotNode().getEdgeTo((ScheduleTask<AntEdge>) task);
		}

		if(edge==null){
			AntNode origin = OnlineACOScheduler.getDepotNode();
			if(!previousTask.getID().equals(MissionScheduler.SOURCE_NODE.getID()))
				origin = OnlineACOScheduler.getInstance().getNode(previousTask.getID());
			edge = new AntEdge(origin, (AntNode)task);
			for(StraddleCarrier rsc : MissionScheduler.getInstance().getResources()){
				edge.addCost(rsc);
			}
		}
		
		else if(edge.getNodeTo()==null) new Exception("Edge has no destination").printStackTrace();
		else if(edge.getNodeTo().getMission()==null) new Exception("Node "+edge.getNodeTo().getID()+" has no attached mission").printStackTrace();
		Mission m = edge.getNodeTo().getMission();
		
		//Current Location to Pickup location of the next mission
		score.addDistance(edge.getDistance());
		double cost = 0.0;
		if(edge.getNodeFrom()==MissionScheduler.SOURCE_NODE) cost = edge.getCost(getID());
		else cost = edge.getCost(getModelID());
		score.addTravelTime(cost);
		
		//Pickup to delivery locations
		score.addDistance(edge.getNodeTo().getDistance());
		score.addTravelTime(edge.getNodeTo().getCost(getModelID()));

//		System.err.println("3");
		//Update Time after P
		double tP = currentTime + cost;// + colony.getHandlingTime().getInSec();
		double tLeaveP = 0;
		if(tP < m.getPickupTimeWindow().getMin().getInSec()){
			if(previousTask != MissionScheduler.SOURCE_NODE) score.addEarliness(m.getPickupTimeWindow().getMin().getInSec() - tP);
			tP = m.getPickupTimeWindow().getMin().getInSec();
		}
		tLeaveP = tP + handlingTime;
		
		
		if(tLeaveP > m.getPickupTimeWindow().getMax().getInSec()){
			score.addLateness(tLeaveP-m.getPickupTimeWindow().getMax().getInSec());
		}
		
		//Update Time after D
		double tD = tLeaveP + edge.getNodeTo().getCost(getModelID());// + colony.getHandlingTime().getInSec();
		if(tD < m.getDeliveryTimeWindow().getMin().getInSec()){
			addEarliness(m.getDeliveryTimeWindow().getMin().getInSec()- tD);
			tD = m.getDeliveryTimeWindow().getMin().getInSec();
		}
		double tLeaveD = tD + handlingTime;
		
//		System.err.println("4");
		
		//time = new Time(tD+"s");
		if(tLeaveD > m.getDeliveryTimeWindow().getMax().getInSec()){
			addLateness(tLeaveD - m.getDeliveryTimeWindow().getMax().getInSec());
		}
		
		currentTime = tLeaveD;
//		System.err.println("5");
		
		score.addEdge(edge, currentTime);
		score.addNode(task);
//		System.err.println("6");
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public LocalScore simulateVisitNode(LocalScore from, ScheduleTask<? extends ScheduleEdge> task){
		LocalScore score = new LocalScore(from);
		
		//System.out.println("SCORE : "+from.getNodePathString());
		AntNode previousTask = (AntNode) score.getNodePath().get(score.getNodePath().size()-1);

		AntEdge edge = null;
		if(previousTask.exists()) {
			edge = previousTask.getEdgeTo((ScheduleTask<AntEdge>) task);
		}
		else{
			edge = OnlineACOScheduler.getDepotNode().getEdgeTo((ScheduleTask<AntEdge>) task);
		}
		
		if(edge==null || edge.getID()==null) new Exception("Edge is null").printStackTrace();
		else if(edge.getNodeTo()==null) new Exception("Edge has no destination").printStackTrace();
		else if(edge.getNodeTo().getMission()==null) {
			//GO BACK TO DEPOT
			score.addDistance(edge.getDistance());
			score.addTravelTime(edge.getCost(getID()));
			//Update Time after reaching Depot
			double currentTime = score.getTimes().get(score.getTimes().size()-1);
			currentTime += edge.getCost(getID());
			score.addEdge(edge, currentTime);
			return score;
		}
		

		Mission m = edge.getNodeTo().getMission();

		//Current Location to Pickup location of the next mission
		
		score.addDistance(edge.getDistance());
		double cost = 0.0;
		if(edge.getNodeFrom()==MissionScheduler.SOURCE_NODE) cost = edge.getCost(getID());
		else cost = edge.getCost(getModelID());
		score.addTravelTime(cost);
		
		//Pickup to delivery locations
		score.addDistance(edge.getNodeTo().getDistance());
		score.addTravelTime(edge.getNodeTo().getCost(getModelID()));

		//Update Time after P
		double currentTime = score.getTimes().get(score.getTimes().size()-1);
//		System.err.println("TIME = "+new Time(currentTime+"s")+" | "+new Time(this.currentTime+"s"));
		
		double tP = currentTime + cost;// + colony.getHandlingTime().getInSec();
//		System.err.println("TIME@P = "+new Time(tP+"s"));
		
		double tLeaveP = 0;
		if(tP < m.getPickupTimeWindow().getMin().getInSec()){
			if(previousTask != MissionScheduler.SOURCE_NODE) score.addEarliness(m.getPickupTimeWindow().getMin().getInSec() - tP);
			tP = m.getPickupTimeWindow().getMin().getInSec();
		}
		tLeaveP = tP + handlingTime;
//		System.err.println("TIME@LeaveP = "+new Time(tLeaveP+"s"));
		
		if(tLeaveP > m.getPickupTimeWindow().getMax().getInSec()){
			score.addLateness(tLeaveP-m.getPickupTimeWindow().getMax().getInSec());
		}
		
		//Update Time after D
		double tD = tLeaveP + edge.getNodeTo().getCost(getModelID());// + colony.getHandlingTime().getInSec();
//		System.err.println("TIME@D = "+new Time(tD+"s"));
		if(tD < m.getDeliveryTimeWindow().getMin().getInSec()){
			score.addEarliness(m.getDeliveryTimeWindow().getMin().getInSec()- tD);
			tD = m.getDeliveryTimeWindow().getMin().getInSec();
		}
		double tLeaveD = tD + handlingTime;
//		System.err.println("TIME@LeaveD = "+new Time(tLeaveD+"s"));
		
		//time = new Time(tD+"s");
		if(tLeaveD > m.getDeliveryTimeWindow().getMax().getInSec()){
			score.addLateness(tLeaveD - m.getDeliveryTimeWindow().getMax().getInSec());
		}
		
		currentTime = tLeaveD;
//		System.err.println("DOVERALL = "+dOverall);
		score.addEdge(edge, currentTime);
		score.addNode(task);
		return score;
	}

	/* =========================== TO STRING =========================== */
	@Override
	public String toString(){
		StringBuilder sb = new StringBuilder();
		sb.append(ID+" :\n");
		for(Ant a : ants){
			sb.append(a.toString()+"\n");
		}
		sb.trimToSize();
		return sb.toString();
	}

	/* =========================== DESTRUCTOR =========================== */
	/**
	 * Destructor
	 */
	public void destroy(){
		super.destroy();

		for(Ant a : ants) 
			a.destroy();
		ants.clear();
		ants = null;

		color = null;
		currentBest = null;
		missions.clear();
		missions = null;
	}

	public StraddleCarrier getStraddleCarrier() {
		return vehicle;
	}
}