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
package org.routing.path;

import org.time.TimeWindow;
import org.util.Location;

/**
 * Node of a path : location and time window of the location road reservation
 * 
 * @author gaetan
 * 
 */
public class PathNode {

	private Location l;
	private TimeWindow tw;

	public PathNode(Location l) {
		this.l = l;
	}

	public PathNode(Location l, TimeWindow tw) {
		this(l);
		this.tw = tw;
	}

	public Location getLocation() {
		return l;
	}

	public TimeWindow getTimeWindow() {
		return tw;
	}

	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(" location = " + this.l);
		if (this.tw != null)
			sb.append(" tw = " + tw);
		return sb.toString();
	}

}
