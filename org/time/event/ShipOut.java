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
package org.time.event;

import java.util.HashMap;

import org.time.Time;

public class ShipOut extends DynamicEvent {
	private String paveID;
	private double berthFromRate;
	private double berthToRate;
	private int capacity;
	private boolean alreadyAdviced = false;

	private HashMap<String, Double> containersToLoad;
	private static final String TYPE = "ShipOut";

	public ShipOut(Time time, int capacity, String paveID,
			double berthFromRate, double berthToRate) {
		super(time, TYPE, "<event time='" + time + "' type='" + TYPE
				+ "' capacity='" + capacity + "' quay='" + paveID + "' from='"
				+ berthFromRate + "' to='" + berthToRate + "'/>");
		this.capacity = capacity;
		this.paveID = paveID;
		this.berthFromRate = berthFromRate;
		this.berthToRate = berthToRate;
		this.containersToLoad = new HashMap<String, Double>();
	}

	public void addContainerToLoad(String containerID, double teu) {
		containersToLoad.put(containerID, teu);
	}

	public String toString() {
		String s = "The ship on " + paveID + " [" + berthFromRate + " to "
				+ berthToRate + "] will leave at " + super.time;
		return s;
	}

	@Override
	public void execute() {
		boolean b = terminal.shipOut(capacity, paveID, berthFromRate,
				berthToRate, containersToLoad);
		if (!b) {
			if (!alreadyAdviced) {
				System.out.println(scheduler.getTime() + ":> ship on " + paveID
						+ " [" + berthFromRate + " to " + berthToRate
						+ "] can't leave !");
				alreadyAdviced = true;
			}
			scheduler.registerDynamicEvent(this);
		} else
			writeEventInDb();
	}

}
