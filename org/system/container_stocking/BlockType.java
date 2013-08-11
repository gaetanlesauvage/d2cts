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

public enum BlockType {
	SHIP(Block.TYPE_SHIP),
	YARD(Block.TYPE_YARD),
	ROAD(Block.TYPE_ROAD),
	RAILWAY(Block.TYPE_RAILWAY),
	DEPOT(Block.TYPE_DEPOT);
	
	private final int value;

	
	private BlockType(int value){
		this.value = value;
	}
	
	public int getValue() {
		return value;
	}
	
	public static int getIntValue(String strValue){
		if(strValue.equals(SHIP.value-1+"")) return SHIP.value;
		else if(strValue.equals(YARD.value-1+"")) return YARD.value;
		else if(strValue.equals(ROAD.value-1+"")) return ROAD.value;
		else if(strValue.equals(RAILWAY.value-1+"")) return RAILWAY.value;
		else return DEPOT.value;
	}
	
	public static BlockType getType(String strValue){
		if(strValue.equalsIgnoreCase(SHIP.name())) return SHIP;
		else if(strValue.equalsIgnoreCase(YARD.name())) return YARD;
		else if(strValue.equalsIgnoreCase(ROAD.name())) return ROAD;
		else if(strValue.equalsIgnoreCase(RAILWAY.name())) return RAILWAY;
		else return DEPOT;
	}
	
	public static BlockType getType(int value){
		if(value == SHIP.value-1) return SHIP;
		else if(value == YARD.value-1) return YARD;
		else if(value == ROAD.value-1) return ROAD;
		else if(value == RAILWAY.value-1) return RAILWAY;
		else return DEPOT;
	}
	
	
}
