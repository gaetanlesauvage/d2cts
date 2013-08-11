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
 * Parameters value for generating a new train and its associated missions
 * @author Ga&euml;tan Lesauvage
 * @since 2011
 */
public class TrainGenerationData {
	private String file;
	

	private String minTime, maxTime;
	private double fullRate, afterUnload, afterReload, marginRate;
	
	public TrainGenerationData (String file, String minTime, String maxTime, double fullRate, double afterUnload, double afterReload, double marginRate){
		this.file = file;
		this.minTime = minTime;
		this.maxTime = maxTime;
		this.fullRate = fullRate;
		this.afterUnload = afterUnload;
		this.afterReload = afterReload;
		this.marginRate = marginRate;
	}
	public String getFile() {
		return file;
	}

	public String getMinTime() {
		return minTime;
	}

	public String getMaxTime() {
		return maxTime;
	}

	public double getFullRate() {
		return fullRate;
	}

	public double getAfterUnload() {
		return afterUnload;
	}

	public double getAfterReload() {
		return afterReload;
	}
	public double getMarginRate() {
		return marginRate;
	}
}