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

import org.scheduling.GlobalScore;
import org.scheduling.LocalScore;
import org.scheduling.MissionScheduler;
import org.scheduling.aco.graph.AntEdge;
import org.scheduling.aco.graph.AntMissionNode;
import org.scheduling.aco.graph.AntNode;


/**
 * Ant agent of the ACO based algorithm for the missions scheduling
 * 
 * @author Ga&euml;tan Lesauvage
 * @since 2011
 */
public class Ant {
	/**
	 * Counter
	 */
	private static int count;
	/**
	 * Colony of the ant
	 */
	private AntHill hill;
	/**
	 * Current location
	 */
	private AntNode location;
	/**
	 * Current path
	 */
	private ArrayList<AntMissionNode> path;
	/**
	 * ID of the ant
	 */
	private String ID;
	private String colonyID;
	
	/**
	 * FOR DEBUG ONLY
	 */
//	private String lastChoice="";


	private LocalScore currentScore;

	/**
	 * Constructor
	 * @param hill Colony of the new ant
	 */
	public Ant(AntHill hill){
		this.hill = hill;
		path = new ArrayList<AntMissionNode>();
		ID = "Ant["+(++count)+"]";
		colonyID = hill.getID();
		currentScore = new LocalScore(hill.getLocalScore());
		location = OnlineACOScheduler.getDepotNode();
	}

	/**
	 * Getter on the colony of the ant
	 * @return The colony hill of the ant
	 */
	public AntHill getHill(){
		return hill;
	}

	/**
	 * Getter ont the ID of the ant
	 * @return The ID of the ant
	 */
	public String getID() {
		return ID;
	}

	/**
	 * Getter on the current location of the ant in the graph
	 * @return The current location of the ant in the graph
	 */
	public AntNode getLocation(){
		return location;
	}

	/**
	 * Reset the ant at the start node
	 */
	public void reset(){

		path.clear();
		if(location != null && location instanceof AntMissionNode) ((AntMissionNode)location).removeAnt(this);

		location = OnlineACOScheduler.getDepotNode();
		currentScore = new LocalScore(hill.getLocalScore());
	}

	public LocalScore getScore(){
		return currentScore;
	}
	/**
	 * Spread pheromone on the location according to the quality
	 */
	private void spreadPheromone(){
		if(location instanceof AntMissionNode){
			AntMissionNode loc = (AntMissionNode)location;
			//AntNode previous = null;
			//if(path.size()<2) previous = ACOMissionScheduler.getDepotNode();
			//else previous =  path.get(path.size()-2);
			double weight = 0.0;
			//AntEdge edge = previous.getEdgeTo(loc);
			GlobalScore gs = new GlobalScore();
			gs.addScore(currentScore);
			weight = gs.getScore();
			//System.err.println(getID()+"@"+hill.getID()+" SCORE IS : "+weight+" on "+location.getID());
			//new java.util.Scanner(System.in).nextLine();
			//weight = edge.getWeight(hill.getID());
			//avoid div by zero
			if(weight == 0) weight = MissionScheduler.CLOSE_TO_ZERO;

			double q = OnlineACOScheduler.getGlobalParameters().getLambda()/weight;

			loc.spreadInstantPheromone(colonyID, q);
			//System.err.println(getID()+"@"+hill.getID()+" SPREAD : "+q+" on "+location.getID());
		//	new java.util.Scanner(System.in).nextLine();
		}
		//else if(location instanceof EndNode) spreadPheromoneOnWholePath();
	}

	/**
	 * Spread pheromone on the whole path according to the quality
	 */
	private void spreadPheromoneOnWholePath(){
		GlobalScore global = new GlobalScore();
		global.addScore(currentScore);
		double score = global.getScore();
		if(score == 0.0) score = MissionScheduler.CLOSE_TO_ZERO;
		double q = OnlineACOScheduler.getGlobalParameters().getLambda()/score;

		for(int i=0; i<path.size(); i++){
			AntMissionNode current = path.get(i);
			current.spreadInstantPheromone(colonyID, q);
		}
	}

	/**
	 * Choose the destination
	 * @return The new destination
	 */
	private AntMissionNode chooseNextLocation(){

		//If the current node has no way out (END NODE) or has been deleted, then go back to the hill and start over
		List<AntEdge> destinations = location.getDestinations(); 
		int outSize = destinations.size(); 
		if( outSize == 0) return null;

		ACOParameters parameters = OnlineACOScheduler.getGlobalParameters();

		//Choose next location
		ArrayList<DestinationChooserHelper> choices = new ArrayList<Ant.DestinationChooserHelper>(outSize);

		double sumPROBA = 0.0;
		boolean endReached = false;
		for(AntEdge e : destinations){
			AntNode tn = (AntNode) e.getNodeTo();
			if(tn instanceof AntMissionNode){
				AntMissionNode target = (AntMissionNode)tn;
				LocalScore tmpScore = hill.simulateVisitNode(currentScore, target);
				double weight = tmpScore.getTravelTime()*parameters.getF1()+tmpScore.getLateness()*parameters.getF2()+tmpScore.getEarliness()*parameters.getF3();
				//System.err.println("SCORE OF "+getID()+"@"+hill.getID()+" VISITING "+target.getID()+" : "+weight+" ("+new Time(tmpScore.getTravelTime()+"s")+"x"+parameters.getF1()+" | "+new Time(tmpScore.getLateness()+"s")+"x"+parameters.getF2()+" | "+new Time(tmpScore.getEarliness()+"s")+"x"+parameters.getF3()+")");
				//new java.util.Scanner(System.in).nextLine();
				if(weight!=Double.POSITIVE_INFINITY){
					double pheromone = target.getPheromone(colonyID);
					double r = target.getForeignPheromone(colonyID);
					double probaTMP = Math.pow(pheromone,parameters.getAlpha())*Math.pow(1f/weight, parameters.getBeta())*Math.pow(pheromone/(r+pheromone),parameters.getGamma());
					choices.add(new DestinationChooserHelper(target,probaTMP));
					sumPROBA+=probaTMP;
				}
			}
			else endReached = true;

		}

//		lastChoice+=" <"+ACOMissionScheduler.getAlgorithmStep()+" @"+Thread.currentThread().getId()+">\n";
		if(choices.size()>0){
			if(choices.size()>1){
				//Normalization for probabilities between [0,1]
				for(DestinationChooserHelper dch : choices){
					dch.setProba(dch.getProba()/sumPROBA);
				}

				//Collections.sort(choices);		

//				for(DestinationChooserHelper dch : choices){
//					lastChoice+=dch+" - ";
//				}
				//Cumulate the probabilities
				double overall = 0.0;
				for(DestinationChooserHelper dch : choices){
					overall += dch.getProba();
					dch.setProba(overall);
				}

				double dChoice = OnlineACOScheduler.RANDOM.nextDouble();
//				lastChoice+=" > "+dChoice;

				for(DestinationChooserHelper dch : choices){
					if(dch.getProba()>= dChoice){
						//Validate ?
						if(dch.getPhRate() >= parameters.getDelta()){
//							lastChoice+=" => "+dch.getDestination().getID()+"\n";
							return dch.getDestination();
						}
						else {
//							lastChoice+=" => NA\n";
							return null;
						}
					}
				}
			}
			//Validate ?
			DestinationChooserHelper dch = choices.get(choices.size()-1);
			if(dch.getPhRate()>= parameters.getDelta()) {
//				lastChoice+=" => "+dch.getDestination().getID()+"\n";
				return dch.getDestination(); 
			}
			else {
//				lastChoice+=" => NA\n";
				return null; 
			}
		}
		if(endReached){
			move(OnlineACOScheduler.getEndNode());
			spreadPheromoneOnWholePath();
		}
//		lastChoice+=" => NA\n";
		return null;
	}

	/**
	 * Move towards the given destination
	 * @param destination The destination
	 */
	public void move(AntNode destination){
		if(location instanceof AntMissionNode){
			AntMissionNode amn = (AntMissionNode)location;
			amn.removeAnt(this);
		}
		if(destination instanceof AntMissionNode){
			path.add((AntMissionNode)destination);
			((AntMissionNode)destination).addAnt(this);
		}
		currentScore = hill.simulateVisitNode(currentScore, destination);
		location = destination;
	}

	/** 
	 * Compute the ant algorithm
	 */
	public void compute() {
		AntMissionNode choice = chooseNextLocation();
		if(choice != null){
			move(choice);
			spreadPheromone();
		}
		else{
			//GO BACK TO DEPOT
			reset();
		}

	}

	/**
	 * Destructor
	 */
	public void destroy(){
		if(location != null && location instanceof AntMissionNode) ((AntMissionNode)location).removeAnt(this);

		hill = null;
		path = null;
		location = null;
		ID = null;

		count--;

	}

//	@Override
//	public String toString(){
//		String locID = "DEPOT";
//		if(location != null) locID = location.getID();
//		String val = "<"+ID+">\n <location: "+locID+"/>\n <choices>\n"+lastChoice+" </choices>\n</"+ID+">\n";
//		lastChoice = "";
//		return val;
//	}

	/**
	 * Helper to be able to sort and compute probabilities easyly
	 * @author Ga&euml;tan Lesauvage
	 * @since 2011
	 */
	private class DestinationChooserHelper implements Comparable<DestinationChooserHelper>{
		/**
		 * Probability of choosing this destination
		 */
		double proba;
		/**
		 * Corresponding destination
		 */
		AntMissionNode destination;

		/**
		 * Constructor
		 * @param n Destination
		 * @param p Probability
		 */
		public DestinationChooserHelper(AntMissionNode n , double p){
			this.proba = p;
			this.destination = n;
		}

		/**
		 * Setter on the probability
		 * @param p New probability
		 */
		public void setProba(double p){
			this.proba = p;
		}

		/**
		 * Getter on the probability
		 * @return The probability
		 */
		public double getProba(){
			return proba;
		}

		/**
		 * Getter on the destination
		 * @return The destination
		 */
		public AntMissionNode getDestination(){
			return destination;
		}

		@Override
		/**
		 * Comparator
		 */
		public int compareTo(DestinationChooserHelper d){
			if(proba < d.proba) return -1;
			else if(proba == d.proba) return 0;
			else return 1;
		}

		/**
		 * Getter on the rate of pheromone on the overall pheromones of the destination
		 * @return The ratio between the pheromone of the ant colony on the destination and the whole pheromone of the destination 
		 */
		public double getPhRate(){
			return destination.getPheromone(hill.getID()) / destination.getPheromoneOverall();
		}

		@Override
		public String toString(){
			return destination.getID()+" ["+proba+"]";
		}
	}
}
