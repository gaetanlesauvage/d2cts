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

import org.system.Terminal;
import org.system.container_stocking.ContainerLocation;
import org.time.Time;
import org.time.TimeScheduler;

public class NewContainer extends DynamicEvent {
	protected String id;
	protected double teu;
	protected ContainerLocation location;

	private static final String TYPE = "NewContainer";

	public NewContainer(Time time, String containerId, double teu,
			ContainerLocation location) {
		super(time, TYPE, "<event time='" + time + "' type='" + TYPE
				+ "><container id='" + containerId + "' teu='" + teu + "'>"
				+ location.toXML() + "</container></event>");
		this.id = containerId;
		this.teu = teu;
		this.location = location;
	}

	@Override
	public void execute() {
		try {
			Terminal.getInstance().addContainer(id, teu, location);
			writeEventInDb();
		} catch (Exception e) {
			// System.out.println("Event delayed : "+getType()+" "+location);
			TimeScheduler.getInstance().registerDynamicEvent(this);
		}
	}

	public String getContainerID() {
		return id;
	}

	public double getTEU() {
		return teu;
	}

	public static String getNewContainerType() {
		return TYPE;
	}

}
