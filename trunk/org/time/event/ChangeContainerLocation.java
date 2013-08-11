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

import java.io.Serializable;

import org.system.container_stocking.ContainerLocation;
import org.time.Time;

public class ChangeContainerLocation extends DynamicEvent implements
		Serializable {
	private String containerId;
	private ContainerLocation cl;
	/**
	 * 
	 */
	private static final long serialVersionUID = 8156636782298087496L;
	private static final String TYPE = "ChangeContainerLocation";

	public ChangeContainerLocation(Time time, String containerId,
			ContainerLocation cl) {
		super(time, TYPE, "<event time='" + time + "' type='" + TYPE
				+ "' container='" + containerId + "'>" + cl.toXML()
				+ "</event>");
		this.containerId = containerId;
		this.cl = cl;
	}

	@Override
	public void execute() {
		terminal.setContainerLocation(containerId, cl);
		writeEventInDb();
	}

}
