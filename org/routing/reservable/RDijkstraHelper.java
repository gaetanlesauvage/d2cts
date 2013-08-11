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
package org.routing.reservable;

import java.io.Serializable;

import org.system.Road;


public class RDijkstraHelper implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 5415720572092817721L;
	private Road r;
	private double distance;
	
	public RDijkstraHelper(Road r, double distance){
		this.r = r;
		this.distance = distance;
	}
	
	public Road getRoad(){
		return r;
	}
	
	public double getDistance(){
		return distance;
	}
}
