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
package org.routing.APSP;

import java.io.Serializable;


public class TargetPath implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1163583162809687457L;
	private String target;
	private double distance;
	private Info passBy;
	
	public TargetPath (String other, double distance, Info passBy){
		this.target = other;
		this.distance = distance;
		this.passBy = passBy;
	}
	
	public double getDistance () {
		return distance;
	}
	
	public Info getPassBy() {
		return passBy;
	}
	
	public String getTarget() {
		return target;
	}
	
	public String toString(){
		String result = target+" "+distance;
		if(passBy != null) result+=" passing by "+passBy.getId();
		else result+="direct";
		return result;
	}
}

