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

import org.time.Time;
import org.time.TimeWindow;


/**
 * A lane reservation of a straddle carrier 
 * @author Ga&euml;tan Lesauvage
 * @since 2009
 */
public class Reservation implements Comparable<Reservation>{
	
	/**
	 * The priority is given to the vehicle who has to go through the lane
	 */
	public static final int PRIORITY_GO_THROUGH = 0;
	/**
	 * The priority is given to the vehicle coming in the lane
	 */
	public static final int PRIORITY_GO_IN = 1;
	/**
	 * The priority is given to the vehicle going out of the lane
	 */
	public static final int PRIORITY_GO_OUT = 2;
	
	/**
	 * ID of the vehicle who has set the reservation
	 */
	private String vehicleID;
	/**
	 * Time window of the reservation
	 */
	private TimeWindow tw;
	/**
	 * ID of the concerned lane
	 */
	private String laneID;
	/**
	 * Date of the reservation setup
	 */
	private Time date;
	/**
	 * Priority of the reservation (either PRIORITY_GO_THROUGH, PRIORITY_GO_IN or PRIORITY_GO_OUT)
	 */
	private int priority;
	
	/**
	 * Set a reservation 
	 * @param date Date of the reservation setup
	 * @param vehicleID Vehicle reserving the road
	 * @param roadID Road reserved
	 * @param tw Time window of the reservation
	 * @param priority Priority of this reservation in case of conflict with any other reservation
	 */
	public Reservation (Time date, String vehicleID, String roadID, TimeWindow tw, int priority){
		this.vehicleID = vehicleID;
		this.tw = tw;
		this.laneID = roadID;
		this.priority = priority;
		this.date = date;
	}
	
	/**
	 * Compare the given reservation with the current one.
	 * @param r Reservation to compare with the current one
	 * @return 	<ul> 
	 * 				<li> <b>0</b> : if the two reservation start and end at the same time and have the same priority </li>
	 * 				<li> <b>-1</b> : if the current reservation starts before the given one or starts at the same time but ends earlier </li>
	 * 				<li> <b>1</b> :  if the current reservation starts after the given one or starts at the same time but ends after </li>
	 * 			</ul>
	 */
	public int compareTo(Reservation r){
		int beg = tw.compareBeginTo(r.tw);
		if(beg == 0) {
			int end = tw.compareEndTo(r.tw);
			if(end==0) {
				if(priority > r.priority) return -1;
				else if(priority < r.priority) return 1;
				else return 0;
			}
			else return end;
		}
		else return beg;
	}
	/**
	 * Get the time when the reservation had been setup 
	 * @return Time when the reservation had been setup 
	 */
	public Time getDate(){
		return date;
	}
	/**
	 * The priority of the reservation 
	 * @return Priority of the reservation
	 */
	public int getPriority(){
		return priority;
	}
	/**
	 * Get the ID of the reserved road
	 * @return ID of the reserved road
	 */
	public String getRoadId(){
		return laneID;
	}
	/**
	 * Get the ID of the concerned vehicle 
	 * @return ID of the concerned vehicle
	 */
	public String getStraddleCarrierId(){
		return vehicleID;
	}
	/**
	 * Get the time window of the reservation
	 * @return Time window of the reservation
	 */
	public TimeWindow getTimeWindow(){
		return tw;
	}
	
	/**
	 * String representation of this object
	 */
	public String toString(){
		return "Reservation of "+laneID+" for "+vehicleID+" on "+tw+" priority="+priority;
	}

}
