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

import java.util.HashSet;
import java.util.Set;

import org.system.Terminal;
import org.time.Time;
import org.time.TimeScheduler;

public class ShipIn extends DynamicEvent {
	private static final String TYPE = "ShipIn";
	private boolean alreadyAdviced = false;
	private String paveID;
	private double berthFromRate;
	private double berthToRate;
	private int capacity;

	private Set<String> containersToUnload;

	public ShipIn(Time time, int capacity, String paveID, double berthFromRate,
			double berthToRate, Set<String> containersToUnload) {
		super(time, TYPE, "<event time='" + time + "' type='" + TYPE
				+ "' capacity='" + capacity + "' quay='" + paveID + "' from='"
				+ berthFromRate + "' to='" + berthToRate + "'/>");
		this.capacity = capacity;
		this.paveID = paveID;
		this.berthFromRate = berthFromRate;
		this.berthToRate = berthToRate;
		this.containersToUnload = containersToUnload;

	}

	public ShipIn(Time time, int capacity, String paveID, double berthFromRate,
			double berthToRate) {
		super(time, TYPE, "<event time='" + time + "' type='" + TYPE
				+ "' capacity='" + capacity + "' quay='" + paveID + "' from='"
				+ berthFromRate + "' to='" + berthToRate + "'/>");
		this.capacity = capacity;
		this.paveID = paveID;
		this.berthFromRate = berthFromRate;
		this.berthToRate = berthToRate;
		this.containersToUnload = new HashSet<>();
	}

	public void addContainerToUnload(String containerID) {
		containersToUnload.add(containerID);
	}

	public String toString() {
		String s = "The ship is berthed at " + paveID + " [" + berthFromRate
				+ " to " + berthToRate + "]";
		return s;
	}

	@Override
	public void execute() {
		boolean b = Terminal.getInstance().shipIn(capacity, paveID, berthFromRate,
				berthToRate, containersToUnload);
		if (!b) {
			if (!alreadyAdviced) {
				System.out.println(this);
				alreadyAdviced = true;
			}
			TimeScheduler.getInstance().registerDynamicEvent(this);
		} else
			writeEventInDb();
	}

	public String getQuayID() {
		return this.paveID;
	}

	public double getShipBerthToRate() {
		return this.berthToRate;
	}

	public double getShipBerthFromRate() {
		return this.berthFromRate;
	}
}