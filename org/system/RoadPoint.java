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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import org.positioning.Coordinates;



/**
 * A point on a road. It is used to model a turn in a road. Such an object can not be connected to two different roads unlike the derivated <a href="Crossroad.html">Crossroad</a> object.
 * @author Ga&euml;tan Lesauvage
 * @since 2009
 */
public class RoadPoint implements Serializable{
	
	private static final long serialVersionUID = 819831953516594797L;
	
	/**
	 * Location of the RoadPoint
	 */
	private Coordinates location;
	/**
	 * ID of the RoadPoint : it must be UNIQUE !
	 */
	protected String id;
	/**
	 * Neighborhood map.
	 */
	private ConcurrentHashMap<String,String> connexCrossroads;
	/**
	 * Parts of a road connected to this object according to their target RoadPoint.
	 */
	private ConcurrentHashMap<String, String> connexRoadsAccordingToConnexRoadPoint;
		
	/**
	 * Constructor
	 * @param id ID of the RoadPoint (UNIQUE)
	 * @param location Coordinates of this object
	 */
	public RoadPoint (String id, Coordinates location){
		this.location = location;
		this.id = id;
		connexCrossroads = new ConcurrentHashMap<String, String>();
		connexRoadsAccordingToConnexRoadPoint = new ConcurrentHashMap<String, String>();
		//if(terminal == null) terminal = Terminal.getRMIInstance();
	}

	/**
	 * Add a connex point to the neighborhood map
	 * @param roadpointID ID of the neighbor 
	 * @param segmentID ID of the arc used to reach this neighbor
	 */
	public void addConnexCrossroad (String roadpointID, String segmentID) {
		connexCrossroads.put(roadpointID, roadpointID);
		connexRoadsAccordingToConnexRoadPoint.put(roadpointID, segmentID);
	}

	/**
	 * Get the list of the IDs of the neighbors
	 * @return A list of the neighbors' IDs
	 */
	public List<String> getConnexNodesIds() {
		List<String> result = new ArrayList<String>(connexCrossroads.size());
		for(String s : connexCrossroads.values()){
			result.add(s);
		}
		return result;
	}

	/**
	 * Get the connex roads IDs
	 * @return A list containing the connex roads IDs
	 */
	public List<String> getConnexRoadIDs() {
		List<String> connexRoadNames = new ArrayList<String>(connexRoadsAccordingToConnexRoadPoint.size());
		for(Enumeration<String> e = connexRoadsAccordingToConnexRoadPoint.keys() ; e.hasMoreElements() ; ){
			connexRoadNames.add(connexRoadsAccordingToConnexRoadPoint.get(e));
		}
		return connexRoadNames; 
	}
	
	/**
	 * Get the connex road ID toward a given node ID
	 * @param nodeID Destination node ID
	 * @return The connex road ID toward the given node
	 */
	public String getConnexRoadIDTowardsNode(String nodeID) {
		return connexRoadsAccordingToConnexRoadPoint.get(nodeID);
	}
	
	/**
	 * Get the ID of this point
	 * @return ID of this point
	 */
	public String getId() {
		return id;
	}

	/**
	 * Get the coordinates of this point
	 * @return Coordinates of this point
	 */
	public Coordinates getLocation() {
		return location;
	}

	/**
	 * Remove a point from the neighborhood map
	 * @param crossroadId ID of the connex crossroad to remove 
	 */
	public void removeConnexCrossroad (String crossroadId){
		connexCrossroads.remove(crossroadId);
		connexRoadsAccordingToConnexRoadPoint.remove(crossroadId);
	}
	
	/**
	 * String representation of the RoadPoint 
	 */
	public String toString(){
		return id+"("+location+")";
	}

	public void destroy() {
		connexCrossroads.clear();
		connexCrossroads = null;
		connexRoadsAccordingToConnexRoadPoint.clear();
		connexRoadsAccordingToConnexRoadPoint = null;
		location = null;
	}
	
}
