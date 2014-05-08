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

import java.util.Date;

/**
 * Parameters value for generating a new ship and its associated missions
 * @author Ga&euml;tan Lesauvage
 * @since 2011
 */
public class ShipGenerationData extends GenerationData {
	private Date maxArrivalTime;
	private Date minBerthTimeLength;
	private Date maxDepartureTime;
	private Date timePerContainerOperation;
	
	private int minTeuCapacity;
	private int maxTeuCapacity;
	private int capacityFactor;
	
	private double fullRate;
	private double twentyFeetRate;
	private double fortyFeetRate;
	private double afterUnload;
	private double afterReload;
	private double marginRate;
	
	public ShipGenerationData(Date maxArrivalTime, Date minBerthTimeLength, Date maxDepartureTime, Date timePerContainerOperation, int minTeuCapacity, int maxTeuCapacity, int capacityFactor,
			double fullRate, double twentyFeetRate, double fortyFeetRate, double afterUnload, double afterReload, double marginRate, String groupID){
		super(groupID);
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

	public Date getMaxArrivalTime() {
		return maxArrivalTime;
	}

	public Date getMinBerthTimeLength() {
		return minBerthTimeLength;
	}

	public Date getMaxDepartureTime() {
		return maxDepartureTime;
	}

	public Date getTimePerContainerOperation() {
		return timePerContainerOperation;
	}

	public void setMaxArrivalTime(Date maxArrivalTime) {
		this.maxArrivalTime = maxArrivalTime;
	}

	public void setMinBerthTimeLength(Date minBerthTimeLength) {
		this.minBerthTimeLength = minBerthTimeLength;
	}

	public void setMaxDepartureTime(Date maxDepartureTime) {
		this.maxDepartureTime = maxDepartureTime;
	}

	public void setTimePerContainerOperation(Date timePerContainerOperation) {
		this.timePerContainerOperation = timePerContainerOperation;
	}

	public void setMinTeuCapacity(int minTeuCapacity) {
		this.minTeuCapacity = minTeuCapacity;
	}

	public void setMaxTeuCapacity(int maxTeuCapacity) {
		this.maxTeuCapacity = maxTeuCapacity;
	}

	public void setCapacityFactor(int capacityFactor) {
		this.capacityFactor = capacityFactor;
	}

	public void setFullRate(double fullRate) {
		this.fullRate = fullRate;
	}

	public void setTwentyFeetRate(double twentyFeetRate) {
		this.twentyFeetRate = twentyFeetRate;
	}

	public void setFortyFeetRate(double fortyFeetRate) {
		this.fortyFeetRate = fortyFeetRate;
	}

	public void setAfterUnload(double afterUnload) {
		this.afterUnload = afterUnload;
	}

	public void setAfterReload(double afterReload) {
		this.afterReload = afterReload;
	}

	public void setMarginRate(double marginRate) {
		this.marginRate = marginRate;
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