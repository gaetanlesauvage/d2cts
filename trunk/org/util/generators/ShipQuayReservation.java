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

package org.util.generators;

import java.io.Serializable;

import org.time.TimeWindow;

/**
 * Quay Reservation. It is useful for avoiding berth collisions due to an intersection of the berth reservation of two different ships.
 * @author Ga&euml;tan Lesauvage
 * @since 2011
 */
class ShipQuayReservation implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 3242480902507294794L;

	/**
	 * Quay ID
	 */
	private String quayID;
	/**
	 * Time window of the reservation
	 */
	private TimeWindow tw;
	/**
	 * Berth origin rate on this quay  
	 */
	private double berthRateFrom;
	/**
	 * Berth end rate on this quay
	 */
	private double berthRateTo;

	/**
	 * Constructor
	 * @param quayID Quay ID
	 * @param tw Time window of the reservation
	 * @param berthRateFrom Berth origin rate on this quay ( belongs to [0..1] )
	 * @param berthRateTo Berth end rate on this quay ( belongs to [0..1] and strictly greater than berthRateFrom)
	 */
	public ShipQuayReservation (String quayID, TimeWindow tw, double berthRateFrom, double berthRateTo){
		this.quayID = quayID;
		this.tw = tw;
		this.berthRateFrom = berthRateFrom;
		this.berthRateTo = berthRateTo;
	}

	/**
	 * Tells if this reservation contains neither time nor space intersection with the given other reservation.
	 * @param otherReservation Other reservation to compare.
	 * @return <b>true</b> if there is no space and no time intersection withe the given other reservation, <b>false</b> otherwise.
	 */
	public boolean isCompatibleWith(ShipQuayReservation otherReservation){
		if(quayID.equals(otherReservation.quayID)){
			if(TimeWindow.intersection(tw, otherReservation.tw)!=null){
				double left, right;
				int compBegin = 0;
				if(berthRateFrom < otherReservation.berthRateFrom) compBegin = -1;
				else if(berthRateFrom > otherReservation.berthRateFrom) compBegin = 1;


				int compEnd = 0;
				if(berthRateTo < otherReservation.berthRateTo) compEnd = -1;
				else if(berthRateTo > otherReservation.berthRateTo) compEnd = 1;

				if(compBegin < 0) left = otherReservation.berthRateFrom;
				else left = berthRateFrom;

				if(compEnd > 0) right = otherReservation.berthRateTo;
				else right = berthRateTo;

				if(left<=right){
					return false;
				}
				else return true;
			}
			else return true;
		}
		else return true;
	}
}