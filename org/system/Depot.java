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

import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

import org.positioning.Coordinates;
import org.system.container_stocking.Block;
import org.system.container_stocking.BlockType;

/**
 * A straddle carriers depot.
 * 
 * @author Ga&euml;tan Lesauvage
 * @since 2009
 */
public class Depot extends Block {
	
	/**
	 * Gravity center of the depot 
	 */
	private Coordinates gravityCenter;
	/**
	 * Does the gravity center have been recomputed since the last add of coordinates
	 */
	private Boolean recomputeGravityCenter;
	/**
	 * Slots of the vehicles
	 */
	private ConcurrentHashMap<String , StraddleCarrierSlot> slots;
	
	/**
	 * Construct a depot name by the given ID (must be unique !)
	 * @param id ID of the depot
	 */
	public Depot(String id) {
		super(id, BlockType.DEPOT);
		recomputeGravityCenter = true;
	}
	/**
	 * Add a point to the outline
	 */
	public void addCoords(String name, Coordinates coords){
		if(this.coords==null){
			this.coords = new ConcurrentHashMap<String, Coordinates>();
			sortedCoordsName = new ArrayList<String>();
		}
		this.coords.put(name, coords);
		this.sortedCoordsName.add(name);
		recomputeGravityCenter = true;
	}

	/**
	 * Add a slot
	 * @param slot Slot to be added
	 */
	public void addStraddleCarrierSlot(StraddleCarrierSlot slot){
		if(slots == null) slots = new ConcurrentHashMap<String, StraddleCarrierSlot>();
		slots.put(slot.getId(), slot);
		addLane(slot);
	}

	/**
	 * Add a border between two points of the outline
	 */
	public  void addWall(String from, String to){
		if(this.walls==null) this.walls = new ConcurrentHashMap<String, String>();
		this.walls.put(from, to);
	}
	/**
	 * Get the coordinates of the depot points
	 */
	public ConcurrentHashMap<String, Coordinates> getCoordinates(){
		return coords;
	}
	/**
	 * Get the coordinates of the given point ID
	 */
	public Coordinates getCoordinates(String pointID){
		return coords.get(pointID);
	}
	
	/**
	 * Get the ID of this depot
	 */
	public String getId() {
		return id;
	}
	/**
	 * Get the gravity center of the shape of the depot
	 * @return Gravity center of the polygon
	 */
	public Coordinates getLocation (){
		if(recomputeGravityCenter){
			double a = 0;
			Coordinates[] coordsArray = new Coordinates[coords.size()];
			double minX = Double.POSITIVE_INFINITY;
			double minY = Double.POSITIVE_INFINITY;

			/*Searching for minima*/
			for(int i=0; i<sortedCoordsName.size() ; i++){
				coordsArray[i]=coords.get(sortedCoordsName.get(i));
				if(coordsArray[i].x<minX) minX = coordsArray[i].x;
				if(coordsArray[i].y<minY) minY = coordsArray[i].y;
			}
			/*Offset*/
			for(int i=0;i<coordsArray.length;i++){
				coordsArray[i] = new Coordinates(coordsArray[i].x-minX, coordsArray[i].y-minY);
			}

			for(int i=0; i<coordsArray.length-1; i++){
				double t = ((double)((double)coordsArray[i].x*(double)coordsArray[i+1].y))-((double)((double)coordsArray[i+1].x*(double)coordsArray[i].y));
				a += t;
			}
			a*=0.5;

			double x = 0;
			for(int i=0; i<coordsArray.length-1; i++){
				x+= ((double)((double)coordsArray[i].x+(double)coordsArray[i+1].x)*((double)((double)coordsArray[i].x*(double)coordsArray[i+1].y)-(double)((double)coordsArray[i+1].x*(double)coordsArray[i].y)));
			}
			x*=1.0/(6.0*a);

			double y = 0f;
			for(int i=0; i<coordsArray.length-1; i++){
				y+= ((double)((double)coordsArray[i].y+(double)coordsArray[i+1].y)*((double)((double)coordsArray[i].x*(double)coordsArray[i+1].y)-(double)((double)coordsArray[i+1].x*(double)coordsArray[i].y)));
			}
			y*=1.0/(6.0*a);

			double xf = x;
			double yf = y;
			//Put back into the right frame
			xf+=minX;
			yf+=minY;
			gravityCenter = new Coordinates(xf, yf);
			recomputeGravityCenter = false;
		}
		return gravityCenter;
	}
	
	/**
	 * Get the slot named by the given ID
	 * @param id ID of the slot to get
	 * @return Slot
	 */
	public StraddleCarrierSlot getStraddleCarrierSlot(String id){
		return slots.get(id);
	}
	
	/**
	 * Get all the straddle carrier's slots within a map indexed according to the IDs of these slots
	 * @return A map containing the IDs of the slots and the slots themselves
	 */
	public ConcurrentHashMap<String, StraddleCarrierSlot> getStraddleCarrierSlots() {
		return slots;
	}

	/**
	 * Get the borders of the depot within a map indexed on the IDs of these borders
	 */
	public ConcurrentHashMap<String, String> getWalls(){
		return walls;
	}
//	public void destroy() {
//		gravityCenter = null;
//		recomputeGravityCenter = null;
//		slots.clear();
//		slots = null;
//	}
}