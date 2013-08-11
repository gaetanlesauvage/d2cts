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
 * Parameters value for generating new trucks and their associated missions
 * @author Ga&euml;tan Lesauvage
 * @since 2011
 */
public class TrucksGenerationData {
	
	private String outputFile;
	private int nb;
	private double rateComeEmpty;
	private double rateLeaveEmpty;
	private String avgTruckTimeBeforeLeaving;
	private String minTime;
	private String maxTime;
	private String groupID;
	
	public TrucksGenerationData (String outputFile, int nb, double rateComeEmpty, double rateLeaveEmpty, String avgTruckTimeBeforeLeaving, String minTime, String maxTime, String groupID){
		this.outputFile = outputFile;
		this.nb = nb;
		this.rateComeEmpty = rateComeEmpty;
		this.rateLeaveEmpty = rateLeaveEmpty;
		this.avgTruckTimeBeforeLeaving = avgTruckTimeBeforeLeaving;
		this.minTime = minTime;
		this.maxTime = maxTime;
		this.groupID = groupID;
	}

	public String getGroupID() {
		return groupID;
	}

	public String getOutputFile() {
		return outputFile;
	}

	public int getNb() {
		return nb;
	}

	public double getRateComeEmpty() {
		return rateComeEmpty;
	}

	public double getRateLeaveEmpty() {
		return rateLeaveEmpty;
	}

	public String getAvgTruckTimeBeforeLeaving() {
		return avgTruckTimeBeforeLeaving;
	}

	public String getMinTime() {
		return minTime;
	}

	public String getMaxTime() {
		return maxTime;
	}
	
}