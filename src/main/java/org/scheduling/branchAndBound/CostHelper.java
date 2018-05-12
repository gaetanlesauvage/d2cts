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
package org.scheduling.branchAndBound;

import java.util.HashMap;

import org.time.Time;

/**
 * Helper to compute and store costs
 * 
 * @author Ga&euml;tan Lesauvage
 * @since 2012
 */
class CostHelper {
	/**
	 * Origin task
	 */
	String originTask;
	/**
	 * Destination task
	 */
	String destinationTask;

	/**
	 * Distance matrix (<Resource ID , Distance in meters from originTask to destinationTask for this resource>)
	 */
	HashMap<String, Double> distanceMatrix;
	/**
	 * Time matrix (<Resource ID , Time required to go from originTask to destinationTask for this resource>)
	 */
	HashMap<String, Time> timeMatrix;

	int deliveryOverrunPenalty;
	int pickupOverrunPenalty;
	/**
	 * Constructor
	 * @param originTask Origin task
	 * @param destinationTask Destination task
	 */
	public CostHelper (String originTask, String destinationTask){
		this.originTask = originTask;
		this.destinationTask = destinationTask;

		distanceMatrix = new HashMap<String, Double>(BranchAndBound.getInstance().getResources().size());
		timeMatrix = new HashMap<String, Time>(BranchAndBound.getInstance().getResources().size());
	}
	
	@Override
	public String toString(){
		StringBuilder sb = new StringBuilder();
		for(String s : distanceMatrix.keySet()){
			sb.append(s+": ("+distanceMatrix.get(s)+" | "+timeMatrix.get(s)+")\t");
		}
		sb.append("\n");
		return sb.toString();
	}
	
	/**
	 * Set the distance cost in meters for going from the origin task to the destination task for the given resource
	 * @param resourceID ID of the resource concerned by the distance
	 * @param distance Distance in meters for going from the origin task to the destination task for the given resource
	 */
	public void setDistanceCost(String resourceID, double distance){
		distanceMatrix.put(resourceID, distance);
	}

	/**
	 * Set the time cost required for going from the origin task to the destination task for the given resource
	 * @param resourceID ID of the concerned resource
	 * @param timeCost Time cost required for going from the origin task to the destination task for the given resource
	 */
	public void setTimeCost(String resourceID, Time timeCost){
		timeMatrix.put(resourceID, timeCost);
	}
	
	public void setDeliveryOverrunPenalty(int penalty){
		this.deliveryOverrunPenalty = penalty;
	}
	
	public void setPickupOverrunPenalty(int penalty){
		this.pickupOverrunPenalty = penalty;
	}
	
	
	/**
	 * Return the cost in meters for the given resource to go from the origin task to the destination one
	 * @param resourceID ID of the resource
	 * @return The cost in meters for the given resource to go from the origin task to the destination one
	 */
	public double getCostInMeters(String resourceID){
		return distanceMatrix.get(resourceID);
	}

	/**
	 * Return the cost in time for the given resource to go from the origin task to the destination one
	 * @param resourceID ID of the resource
	 * @return The cost in time for the given resource to go from the origin task to the destination one
	 */
	public Time getCostInTime(String resourceID){
		return timeMatrix.get(resourceID);
	}
	
	public int getDeliveryOverrunPenalty(){
		return deliveryOverrunPenalty;
	}
	
	public int getPickupOverrunPenalty(){
		return pickupOverrunPenalty;
	}
}