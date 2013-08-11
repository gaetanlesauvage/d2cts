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
package org.missions;

public enum MissionKinds {
	
	STAY(0,1,1),
	IN(1,1,1),
	OUT(2,1,1),
	IN_AND_OUT(3,1,1);
	
	
	private int intValue;
	private int overrunPickupPenalty;
	private int overrunDeliveryPenalty;
	
	private MissionKinds(int i, int pickupPenalty, int deliveryPenalty){
		this.intValue = i;
		this.overrunPickupPenalty = pickupPenalty;
		this.overrunDeliveryPenalty = deliveryPenalty;
	}
	
	public int getIntValue(){
		return intValue;
	}
	
	public int getOverrunPickupPenalty(){
		return overrunPickupPenalty;
	}
	
	public int getOverrunDeliveryPenalty(){
		return overrunDeliveryPenalty;
	}
	
	public static MissionKinds getKind(int i){
		for(MissionKinds m : values()){
			if(m.getIntValue() == i) return m;
		}
		return null;
	}
}
