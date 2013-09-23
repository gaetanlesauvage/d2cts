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


public class Coordinates implements Comparable<Coordinates> {
	public Double x,y,z;
	
	public Coordinates(Double x, Double y){
		this(x,y,0.0);
	}
	
	public Coordinates(Double x, Double y, Double z){
		this.x = x;
		this.y = y;
		if(z == null) z = 0.0;
		this.z = z;
	}
	
	public int compareTo(Coordinates c){
		if(c.x == x && c.y == y && c.z == z) return 0;
		else return 1;
	}
	public String toString(){
		StringBuffer buffer = new StringBuffer();
		buffer.append("(");
		buffer.append(x);
		buffer.append(" , ");
		buffer.append(y);
		buffer.append(" , ");
		buffer.append(z);
		buffer.append(")");
		buffer.trimToSize();
		return buffer.toString();
	}
}
