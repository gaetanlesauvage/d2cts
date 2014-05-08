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
 * Parameters value for generating new trucks and their associated missions
 * 
 * @author Ga&euml;tan Lesauvage
 * @since 2011
 */
public class TruckGenerationData extends GenerationData {

	private int nb;
	private double rateComeEmpty;
	private double rateLeaveEmpty;
	private Date avgTruckTimeBeforeLeaving;
	private Date minTime;
	private Date maxTime;
	private String groupID;

	public TruckGenerationData(int nb, double rateComeEmpty, double rateLeaveEmpty, Date avgTruckTimeBeforeLeaving, Date minTime, Date maxTime,
			String groupID) {
		super(groupID);
		this.nb = nb;
		this.rateComeEmpty = rateComeEmpty;
		this.rateLeaveEmpty = rateLeaveEmpty;
		this.avgTruckTimeBeforeLeaving = avgTruckTimeBeforeLeaving;
		this.minTime = minTime;
		this.maxTime = maxTime;
		this.groupID = groupID;
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

	public Date getAvgTruckTimeBeforeLeaving() {
		return avgTruckTimeBeforeLeaving;
	}

	public Date getMinTime() {
		return minTime;
	}

	public Date getMaxTime() {
		return maxTime;
	}

	public void setCount(int intValue) {
		this.nb = intValue;
	}

	public void setMinTime(Date date) {
		this.minTime = date;
	}

	public void setMaxTime(Date date) {
		this.maxTime = date;
	}

	public void setAvgTimeBeforeLeaving(Date date) {
		this.avgTruckTimeBeforeLeaving = date;
	}

	public void setRateComeEmpty(Double d) {
		this.rateComeEmpty = d;
	}

	public void setRateLeaveEmpty(Double d) {
		this.rateLeaveEmpty = d;
	}
	
	public String getGroupID() {
		return this.groupID;
	}
}