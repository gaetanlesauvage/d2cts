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
package org.util;


import java.util.List;

import org.positioning.Coordinates;
import org.system.Road;
import org.system.RoadPoint;



/**
 * The Location object is used to describe a location within the container terminal by :
 * <ul>
 * <li> a road </li>
 * <li> a rate on this road </li>
 * <li> and a direction </li>
 * </ul>
 * @author Ga&euml;tan Lesauvage
 * @since 2009
 *
 */
public class Location implements Comparable<Location> {
	
	@Override
	public int compareTo(Location l){
		return equals(l) ? 0 : 1; 
	}
	
	@Override
	public boolean equals(Object o){
		if(o instanceof Location){
			Location l = (Location)o;
			//return l.coords.equals(coords);
			return l.getRoad().getId().equals(getRoad().getId()) && l.getPourcent() == getPourcent();
		}
		return false;
	}
	
	/**
	 * Gives the rate of a location on a given road taking into account the non-linearity of this road
	 * @param c The location
	 * @param r The road
	 * @return The rate of the location on a given road taking into account the non-linearity of this road
	 */
	public static double getAccuratePourcent(Coordinates c , Road r){
		double dist = 0;
		double epsilon = 0.1;
		List<RoadPoint> l = r.getRoadPoints();
		RoadPoint current = l.get(0);
		for(int i = 1 ; i<l.size() ; i++){
			RoadPoint next = l.get(i);
			Coordinates nextCoords = next.getLocation();
			Coordinates currentCoords = current.getLocation();
			
			Coordinates vPrime = new Coordinates(nextCoords.x - currentCoords.x, nextCoords.y - currentCoords.y, nextCoords.z-currentCoords.z);
			Coordinates v = new Coordinates(c.x - currentCoords.x, c.y - currentCoords.y, c.z-currentCoords.z);
			double k = vPrime.x * v.y;
			double k2 = vPrime.y * v.x;
			
			if((k - epsilon) <= k2 && (k + epsilon) >= k2){
				dist += Location.getLength(currentCoords, c);
				dist/= r.getLength();
				return dist;
				
			}
			dist += Location.getLength(currentCoords, nextCoords);
			current = next;
		}
		return 0;
	}
	
	
	/**
	 * Gives the length between two points
	 * @param origin First point
	 * @param destination Second point
	 * @return The length between the given points
	 */
	public static double getLength(Coordinates origin, Coordinates destination){
		return Math.sqrt(Math.pow(destination.x-origin.x,2f)+Math.pow(destination.y-origin.y,2f)+Math.pow(destination.z-origin.z,2f));
	}
	
	/**
	 * Gives the rate of a location on a given road
	 * @param l The location
	 * @param r The road
	 * @return The rate of the location on the road ( belongs to [0..1])
	 */
	public static double getPourcent(Coordinates l, Road r) {
		double length = getLength(r.getOrigin().getLocation(), l);
		return length/r.getLength();
	}
	
	/**
	 * Gives the Location of a position after moving 
	 * @param location The start Location
	 * @param oneMoveRate the rate of this move
	 * @return The new location after the move
	 */
	public static Location moveFromOf(Location location,	double oneMoveRate) {
		double pourcent = location.getPourcent()+oneMoveRate;
		return new Location(location.road, pourcent, location.direction); 
	}
	
	private Road road;
	private Coordinates coords;
	private double pourcent;
	private boolean direction; //true = origin -> destination | false = destination -> origin

	/**
	 * Builds a location on the given road at 0% in the given direction
	 * @param r The road
	 * @param direction The direction of the vehicle (true = origin->destination | false = destination->origin)
	 */
	public Location (Road r, boolean direction) {
		this.road = r;
		this.direction = direction;
		if(direction){
			coords = road.getOrigin().getLocation();
			pourcent = 0f;
		}
		else{
			coords = road.getDestination().getLocation();
			pourcent = 1.0f;
		}
	}
	
	/**
	 * Builds a Location
	 * @param r Road of the Location
	 * @param coordinates Coordinates of the location on the given road
	 * @param direction The direction of the vehicle (true = origin->destination | false = destination->origin)
	 */
	public Location (Road r, Coordinates coordinates, boolean direction){
		this.road = r;
		this.coords = coordinates;
		this.direction = direction;
		double length = Math.sqrt(Math.pow(coords.x-road.getOrigin().getLocation().x,2.0) + Math.pow(coords.y- road.getOrigin().getLocation().y,2.0) + Math.pow(coords.z- road.getOrigin().getLocation().z,2.0));
		if(!direction) length = road.getLength() - length;
		pourcent = length / road.getLength() ; 
		
	}

	
	/**
	 * Builds a Location on the given road at the given rate in the same direction than the road
	 * @param r Road of the Location
	 * @param pourcent Rate of the location on the given road
	 */
	public Location (Road r , double pourcent){
		this(r,pourcent, true);
	}

	/**
	 * Builds a Location on the given road at the given rate in towards the given direction
	 * @param r Road of the Location
	 * @param pourcent Rate of the Location
	 * @param direction The direction of the vehicle (true = origin->destination | false = destination->origin)
	 */
	public Location (Road r, double pourcent, boolean direction){
		this.road = r;
		this.pourcent = pourcent;
		this.coords = getCoords(pourcent);
		this.direction = direction;
	}

	/**
	 * Retrieves the coordinates of the Location
	 * @return The coordinates of the Location
	 */
	public Coordinates getCoords() {
		return coords;
	}
	
	/**
	 * Retrieves the coordinates of the point at the given rate on the road of the Location
	 * @param pourcent Rate of the Location
	 * @return Coordinates of the point at the given rate on the road of the Location
	 */
	public Coordinates getCoords(double pourcent){
		double done = 0;
		RoadPoint current = road.getOrigin();
		double segmentLength = 0;
		for(RoadPoint next : road.getPoints()){
			segmentLength = Math.sqrt(Math.pow(next.getLocation().x-current.getLocation().x,2)+Math.pow(next.getLocation().y-current.getLocation().y,2)+Math.pow(next.getLocation().z-current.getLocation().z,2));
			double segmentRate = segmentLength / road.getLength();
			
			if(done+segmentRate>=pourcent){
				double missingRate = pourcent-done;
				double missingLength = missingRate*road.getLength();
				double localMissingRate = missingLength / segmentLength;
				Coordinates vectAC = new Coordinates(next.getLocation().x-current.getLocation().x, next.getLocation().y-current.getLocation().y, next.getLocation().z-current.getLocation().z);
				Coordinates c = new Coordinates((current.getLocation().x+(vectAC.x*localMissingRate)), (current.getLocation().y+(vectAC.y*localMissingRate)), (current.getLocation().z+(vectAC.z*localMissingRate)));
				return c;
			}
			done+=segmentRate;
			current = next;
		}
		RoadPoint next = road.getDestination();
		segmentLength = Math.sqrt(Math.pow(next.getLocation().x-current.getLocation().x,2)+Math.pow(next.getLocation().y-current.getLocation().y,2)+Math.pow(next.getLocation().z-current.getLocation().z,2));
		double missingRate = pourcent - done ;
		double missingLength = missingRate*road.getLength();
		double localMissingRate = missingLength / segmentLength;
		Coordinates vectAC = new Coordinates(next.getLocation().x-current.getLocation().x, next.getLocation().y-current.getLocation().y, next.getLocation().z-current.getLocation().z);
		Coordinates c = new Coordinates((current.getLocation().x+(vectAC.x*localMissingRate)), (current.getLocation().y+(vectAC.y*localMissingRate)), (current.getLocation().z+(vectAC.z*localMissingRate)));
		return c;
	}
	
	/**
	 * The direction of the vehicle (true = origin->destination | false = destination->origin)
	 * @return The direction of the vehicle (true = origin->destination | false = destination->origin)
	 */
	public boolean getDirection(){
		return direction;
	}

	/**
	 * Gives the length between the origin of the road and the point at the given rate
	 * @param rate Target rate of the distance to compute
	 * @return Length between the origin of the road and the point at the given rate
	 */
	public double getLength(double rate) {
		return road.getLength()*rate;
	}

	/**
	 * Retrieves the rate of the current Location
	 * @return Rate of the current Location
	 */
	public double getPourcent() {
		return pourcent;
	}
	
	/**
	 * Gives the rate corresponding to the given length on the current road
	 * @param length 
	 * @return The rate corresponding to the given length on the current road
	 */
	public double getPourcent(double length){
		return length / road.getLength();
	}
	
	/**
	 * Retrieves the road of the current Location
	 * @return Retrieves the road of the current Location
	 */
	public Road getRoad() {
		return road;
	}
	
	/**
	 * Tests if the given rate has been reached according to the direction on the current road
	 * @param rate Target rate
	 * @return true if the given rate has been reached according to the direction on the current road, false otherwise
	 */
	public boolean hasReachedRate(double rate) {
		if(direction) return pourcent >= rate;
		else return pourcent < rate;
	}

	/**
	 * Computes the new location of the instance after a move of a given rate
	 * @param oneMoveRate Rate of the move
	 */
	public void move(double oneMoveRate) {
		pourcent += oneMoveRate;
		coords = getCoords(pourcent);
	}

	/**
	 * Changes the coordinates of the Location by the given ones.<br>To handle carefully, this function won't change the rate of the Location ! 
	 * @param coords New coordinates
	 */
	public void setCoords(Coordinates coords) {
		this.coords = coords;
	}

	/**
	 * Changes the direction of the Location by the given one (true = origin->destination | false = destination->origin)
	 * @param direction <ul><li>true = origin->destination</li><li>false = destination->origin</li></ul>
	 */
	public void setDirection (boolean direction){
		this.direction = direction;
	}

	/**
	 * Changes the rate of the Location by the given one.<br>To handle carefully, this function won't change the coordinates of the Location !
	 * @param pourcent The new rate on the current road : <ul><li>0 = origin</li><li>1 = destination</li></ul>
	 */
	public void setPourcent(double pourcent) {
		this.pourcent = pourcent;
	}
	
	/**
	 * Changes the road of the Location.<br>To handle carefully, this function won't change neither the coordinates, the rate or the direction of the Location !
	 * @param road The new road
	 */
	public void setRoad(Road road) {
		this.road = road;
	}

	/**
	 * Retrieves a string representation of the Location
	 */
	public String toString() {
		return road.getOriginId()+"->"+road.getDestinationId()+" Direction : "+direction+" rate : "+pourcent;
	}
	
	/**
	 * Tests if a given target rate will be reached after a move of a given rate (belongs to [0..1])
	 * @param targetRate Goal to test
	 * @param moveRate Rate of a move
	 * @return True if this move will reach the target rate, else otherwise
	 */
	public boolean willReachRate(double targetRate, double moveRate) {
		double nextPourcent = pourcent+moveRate;
		if(direction) return nextPourcent >= targetRate;
		else return nextPourcent < targetRate;
	}

}
