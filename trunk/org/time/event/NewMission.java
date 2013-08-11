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

import org.missions.Mission;
import org.missions.TruckMission;
import org.time.Time;

public class NewMission extends DynamicEvent implements Comparable<NewMission> {
	private Mission m;
	private static final String TYPE = "newMission";

	public NewMission(Time time, Mission m) {
		super(time, TYPE, "<event time='" + time + "' type='" + TYPE + "'>"
				+ m.toXML() + "</event>");
		this.m = m;
	}

	public NewMission(Time time, TruckMission m) {
		super(time, TYPE, m.toXML());
		this.m = m;
	}

	@Override
	public void execute() {
		terminal.addMission(m);
		writeEventInDb();
	}

	public String toXML() {
		String s = super.getDatabaseValue();
		s = s.replaceAll(">", ">\n");
		return s;
	}

	public String toString() {
		return time + " " + m.getId();
	}

	@Override
	public int compareTo(NewMission n) {
		// System.out.print("COMPARETO "+m.getId()+" "+n.m.getId()+" : ");

		int i = time.compareTo(n.time);
		if (i == 0) {
			int j = m.getId().compareTo(n.m.getId());
			System.out.println("A) " + i + " " + j);
			return j;
		} else {
			System.out.println("B) " + i);
			return i;
		}
	}
}
