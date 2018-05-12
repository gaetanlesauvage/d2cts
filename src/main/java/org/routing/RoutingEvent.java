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
package org.routing;

import java.io.Serializable;

/**
 * Dynamic handling of events onto the routing graph
 * r is the road concerned by the event
 * event is the kind of event (state : available-unavalaible OR length : number)
 * newValue is the new value of the event (-----^            OR    --------^
 * @author gaetan
 *
 */
public class RoutingEvent implements Serializable{
	/**
	 * Exception !
	 * @author gaetan
	 */
	private class UnHandledException extends Exception{
		private static final long serialVersionUID = 1L;

		public UnHandledException (String eventKind){
			super("Unhandled Exception : "+eventKind+" ! Event must be \"state\" or \"length\" !");
		}
	}
	/**
	 * 
	 */
	private static final long serialVersionUID = -749052389256390396L;
	private String id;
	private String event;
	private String newValue;
	
	private int type;
	public static final String STATE_CLOSED = "closed";
	
	public static final String STATE_OPENED = "opened";
	public static final int TYPE_CROSSROAD = 0;
	public static final int TYPE_ROAD = 1;
	
	public static final int GRAPH_CREATED = 2;
	
	public RoutingEvent (int type, String id, String event, String newValue){
		if(!event.equals("state")||!event.equals("length")||(type<0&&type>1)) new UnHandledException(event);
		this.id = id;
		this.event = event;
		this.newValue = newValue;
		this.type = type;
	}
	
	public String getEvent(){
		return event;
	}
	
	public String getObjectId(){
		return id;
	}
	
	public int getType(){
		return type;
	}
	
	public String getValue(){
		return newValue;
	}
	public String toString(){
		StringBuilder sb = new StringBuilder();
		String sType = "crossroad";
		if(type>0) sType = "road";
		sb.append(id+" "+sType+" -> "+event+" "+newValue);
		return sb.toString();
	}
}
