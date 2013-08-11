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
package org.util;

import org.positioning.Coordinates;

public class Distances {
	public static double getDistance(Coordinates a, Coordinates b){
		return Math.sqrt(Math.pow(b.x-a.x, 2)+Math.pow(b.y-a.y,2)+Math.pow(b.z-a.z,2));
	}
	
	public static Coordinates getNewCoordinates(Coordinates from, Coordinates offset){
		double x = from.x+offset.x;
		double y = from.y+offset.y;
		double z = from.z+offset.z;
		return new Coordinates(x, y, z);
	}
}
