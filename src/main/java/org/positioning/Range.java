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
package org.positioning;


public class Range extends Coordinates {
	private Coordinates initialRange;
	
	public Range (double rangeX, double rangeY){
		super(rangeX,rangeY);
		initialRange = new Coordinates(rangeX, rangeY);
	}
	public Range (Double rangeX, Double rangeY, Double rangeZ){
		super(rangeX,rangeY,rangeZ);
		initialRange = new Coordinates(rangeX, rangeY,rangeZ);
	}
	public void setRange(double rate){
		x = initialRange.x*rate;
		y = initialRange.y*rate;
		z = initialRange.z*rate;
	}
}
