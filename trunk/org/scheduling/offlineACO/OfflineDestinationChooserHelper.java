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

package org.scheduling.offlineACO;

import org.scheduling.MissionScheduler;
import org.scheduling.ScheduleEdge;
import org.scheduling.offlineACO2.OfflineACOScheduler2;

/**
 * Helper to be able to sort and compute probabilities easyly
 * @author Ga&euml;tan Lesauvage
 * @since 2012
 */
public class OfflineDestinationChooserHelper implements Comparable<OfflineDestinationChooserHelper>{
	
	public static OfflineSchedulerParameters parameters;
	
	/**
	 * Probability of choosing this destination
	 */
	private double proba;
	
	private double pheromone;
	private double weight;
	private double foreignPheromone;
	/**
	 * Corresponding destination
	 */
	ScheduleEdge destination;

	/**
	 * Constructor
	 * @param e Destination
	 * @param p Probability
	 
	public OfflineDestinationChooserHelper(ScheduleEdge e , double p){
		this.proba = p;
		this.destination = e;
		if(parameters == null) parameters = OfflineACOScheduler.getGlobalParameters();
	}*/

	/**
	 * Constructor
	 * @param e Destination
	 * 
	 */
	public OfflineDestinationChooserHelper(ScheduleEdge e , double pheromone, double weight, double foreignPheromone){
		this.proba = Double.NEGATIVE_INFINITY;
		
		this.destination = e;
		this.pheromone = pheromone;
		this.weight = weight;
		this.foreignPheromone = foreignPheromone;
		
		if(parameters == null) {
			if(MissionScheduler.getInstance() instanceof OfflineACOScheduler){
				parameters = OfflineACOScheduler.getInstance().getGlobalParameters();
			} else {
				parameters = OfflineACOScheduler2.getInstance().getGlobalParameters();
			}
			 
		}
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
	public ScheduleEdge getDestination(){
		return destination;
	}

	public double getPheromone() {
		return pheromone;
	}

	public double getWeight() {
		return weight;
	}

	public double getForeignPheromone() {
		return foreignPheromone;
	}

	@Override
	/**
	 * Comparator
	 */
	public int compareTo(OfflineDestinationChooserHelper d){
		if(proba < 0) System.err.println("Proba should not be negative");
		if(proba < d.proba) return -1;
		else if(proba == d.proba) return 0;
		else return 1;
	}

	@Override
	public String toString(){
		return destination+" ["+proba+"]";
	}
	
	public void destroy(){
		destination = null;
	}
}