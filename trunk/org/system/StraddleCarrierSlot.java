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
package org.system;

import org.positioning.Coordinates;
import org.system.container_stocking.Bay;
import org.system.container_stocking.BayCrossroad;
import org.util.Location;


/**
 * Parking slot of a straddle carrier. Each straddle carrier has a dedicated parking slot.
 * A slot is a particular lane which can stock no container but only straddle carriers.
 * @author Ga&euml;tan Lesauvage
 * @since 2009
 */
public class StraddleCarrierSlot extends Bay{
	/**
	 * ID of the depot containing this slot
	 */
	private String depotId;
	/**
	 * Coordinates of the center of this slot
	 */
	private Coordinates center;
	/**
	 * A slot has an unique ID, a center, the connexion of the lane to the road network and the ID of its depot. 
	 * @param id unique ID
	 * @param location location of its center
	 * @param cOrigin Origin of the lane
	 * @param cDestination Destination of the lane
	 * @param depotId ID of its depot
	 */
	public StraddleCarrierSlot (String id, Coordinates location, BayCrossroad cOrigin, BayCrossroad cDestination, String depotId) {
		super(id, cOrigin, cDestination, false, depotId);
		this.depotId = depotId;
		this.center = location;
	}
	/**
	 * Get the depot ID
	 * @return ID of the depot containing this slot
	 */
	public String getDepotId(){
		return depotId;
	}
	/**
	 * Get the center coordinates
	 * @return Coordinates of the center of the slot
	 */
	public Coordinates getCenterLocation(){
		return center;
	}
	
	public Location getLocation(){
		return new Location(this, 0.5, false);
	}
}
