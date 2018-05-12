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
package org.display.panes;

public enum ReservationColumns implements PaneColumn{
	ROAD(0, 125),
	VEHICLE(1, 125),
	TIMEWINDOW(2, 200),
	PRIORITY(3, 100),
	DATE(4, 150),
	UNRESERVATIONDATE(5,150);
	
	private int index;
	private int width;
	
	private ReservationColumns (int index, int width){
		this.index = index;
		this.width = width;
	}
	
	public int getIndex(){
		return index;
	}
	
	public int getWidth(){
		return width;
	}
}
