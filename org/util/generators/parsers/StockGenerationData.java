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
 * Parameters value for generating new stock missions
 * @author Ga&euml;tan Lesauvage
 * @since 2011
 */

public class StockGenerationData {
	private String minTime;
	private String maxTime;
	private String marginTime;
	private String groupID;
	private int nb;
	
	public StockGenerationData(int nb, String minTime, String maxTime, String marginTime, String groupID){
		this.nb = nb;
		this.minTime = minTime;
		this.maxTime = maxTime;
		this.marginTime = marginTime;
		this.groupID = groupID;
	}

	public String getMinTime() {
		return minTime;
	}

	public String getMaxTime() {
		return maxTime;
	}

	public String getMarginTime() {
		return marginTime;
	}

	public int getNb() {
		return nb;
	}
	
	public String getGroupID() {
		return groupID;
	}
}
