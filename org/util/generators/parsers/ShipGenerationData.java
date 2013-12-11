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

package org.util.generators.parsers;
/**
 * Parameters value for generating a new ship and its associated missions
 * @author Ga&euml;tan Lesauvage
 * @since 2011
 */
public class ShipGenerationData {
	private String maxArrivalTime;
	private String minBerthTimeLength;
	private String maxDepartureTime;
	private String timePerContainerOperation;
	
	private int minTeuCapacity;
	private int maxTeuCapacity;
	private int capacityFactor;
	
	private double fullRate;
	private double twentyFeetRate;
	private double fortyFeetRate;
	private double afterUnload;
	private double afterReload;
	private double marginRate;
	
	public ShipGenerationData(String maxArrivalTime, String minBerthTimeLength, String maxDepartureTime, String timePerContainerOperation, int minTeuCapacity, int maxTeuCapacity, int capacityFactor,
			double fullRate, double twentyFeetRate, double fortyFeetRate, double afterUnload, double afterReload, double marginRate){
		this.maxArrivalTime = maxArrivalTime;
		this.minBerthTimeLength = minBerthTimeLength;
		this.maxDepartureTime = maxDepartureTime;
		this.timePerContainerOperation = timePerContainerOperation;
		this.minTeuCapacity = minTeuCapacity;
		this.maxTeuCapacity = maxTeuCapacity;
		this.capacityFactor = capacityFactor;
		this.fullRate = fullRate;
		this.twentyFeetRate = twentyFeetRate;
		this.fortyFeetRate = fortyFeetRate;
		this.afterUnload = afterUnload;
		this.afterReload = afterReload;
		this.marginRate = marginRate;
	}

	public String getMaxArrivalTime() {
		return maxArrivalTime;
	}

	public String getMinBerthTimeLength() {
		return minBerthTimeLength;
	}

	public String getMaxDepartureTime() {
		return maxDepartureTime;
	}

	public String getTimePerContainerOperation() {
		return timePerContainerOperation;
	}

	public int getMinTeuCapacity() {
		return minTeuCapacity;
	}
	
	public int getMaxTeuCapacity() {
		return maxTeuCapacity;
	}

	public int getCapacityFactor() {
		return capacityFactor;
	}

	public double getFullRate() {
		return fullRate;
	}

	public double getTwentyFeetRate() {
		return twentyFeetRate;
	}

	public double getFortyFeetRate() {
		return fortyFeetRate;
	}

	public double getAfterUnload() {
		return afterUnload;
	}

	public double getAfterReload() {
		return afterReload;
	}
	
	public double getMarginRate(){
		return marginRate;
	}
}