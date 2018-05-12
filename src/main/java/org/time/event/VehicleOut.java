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

import java.util.List;

import org.exceptions.IllegalSlotChangeException;
import org.system.Terminal;
import org.time.Time;
import org.time.TimeScheduler;

public class VehicleOut extends DynamicEvent {
	
	private List<String> slotIds;
	private List<String> containers;
	private boolean alreadyAdviced = false;
	public static final String TYPE = "vehicleOut";
	private String ID;
	private Time originalDepartureTime;

	/**
	 * 
	 * @param time
	 * @param slotIds
	 *            List of the slots concerned by the arrival of the vehicle
	 * @param containers
	 *            List of the containers brought by the vehicle
	 */
	public VehicleOut(Time time, String truckID, List<String> lanes,
			List<String> slotIds, List<String> containers) {
		super(time, TYPE);
		this.ID = truckID;
		String laneIds = "";
		for (int i = 0; i < lanes.size(); i++) {
			if (i == lanes.size() - 1)
				laneIds += lanes.get(i);
			else
				laneIds += lanes.get(i) + ",";
		}
		value = "<event time='" + time + "' type='" + TYPE + "' lanes='"
				+ laneIds + "'/>";
		this.originalDepartureTime = time;
		this.slotIds = slotIds;
		this.containers = containers;

	}

	private String cannotReachSlotMsg() {
		String s = "Vehicle can't leave slots  : ";
		for (String s2 : slotIds)
			s += s2 + ",";
		return s;
	}

	public String toString() {
		String s = "Vehicle Out : " + ID + " slots : ";
		for (int i = 0; i < slotIds.size(); i++) {
			if (i == slotIds.size() - 1)
				s += slotIds.get(i);
			else
				s += slotIds.get(i) + ",";
		}
		if (containers != null) {
			s += " | containers : ";

			for (int i = 0; i < containers.size(); i++) {
				if (i == containers.size() - 1)
					s += containers.get(i);
				else
					s += containers.get(i) + ",";
			}
		}
		return s;

	}

	@Override
	public void execute() {
		boolean b = Terminal.getInstance().vehicleOut(ID, originalDepartureTime, slotIds,
				containers);
		// System.out.println("VEHICLE OUT : "+ID+" = "+b);
		if (!b) {

			super.time = new Time(TimeScheduler.getInstance().getTime(), new Time(1));
			if (!alreadyAdviced) {
				log.warn(time + " : " + cannotReachSlotMsg());
				alreadyAdviced = true;
			}
			TimeScheduler.getInstance().registerDynamicEvent(this);
		} else {
			log.info(TimeScheduler.getInstance().getTime() + " - Vehicle OUT : " + ID + " from "+slotIds.get(0)+" with "+containers.get(0));
			writeEventInDb();
		}
	}

	public boolean containSlot(String slotID) {
		for (String id : slotIds)
			if (slotID.equals(id))
				return true;
		return false;
	}

	public void changeSlot(String slotId2) throws IllegalSlotChangeException {
		if (slotIds.size() == 1) {
			slotIds.remove(0);
			slotIds.add(slotId2);
		} else
			throw new IllegalSlotChangeException();

	}

	public String getVehicleID() {
		return ID;
	}

}
