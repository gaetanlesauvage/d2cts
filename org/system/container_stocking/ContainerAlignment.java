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
package org.system.container_stocking;

public enum ContainerAlignment {
	origin(Slot.ALIGN_ORIGIN, "origin"),
	center(Slot.ALIGN_CENTER, "center"),
	destination(Slot.ALIGN_DESTINATION, "destination");
	
	public static ContainerAlignment get(String stringValue){
		for(ContainerAlignment c : values()){
			if(c.stringValue.equalsIgnoreCase(stringValue))
				return c;
		}
		return null;
	}
	
	public static int getIntValue(String strValue){
		if(strValue.equals(center.stringValue)) return center.getValue();
		else if(strValue.equals(origin.stringValue)) return origin.getValue();
		else return destination.getValue();
	}
	public static String getStringValue(int value){
		if(value == center.value) return center.getStringValue();
		else if(value == origin.value) return origin.getStringValue();
		else return destination.getStringValue();
	}
	
	private final int value;
	
	private final String stringValue;
	
	private ContainerAlignment(int value, String stringValue){
		this.value = value;
		this.stringValue = stringValue;
	}
	
	public String getStringValue(){
		return stringValue;
	}

	public int getValue() {
		return value;
	}
	
	public String toString () {
		return getStringValue();
	}
}
