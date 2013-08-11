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
package org.positioning;

import java.io.Serializable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.conf.parameters.ReturnCodes;
import org.util.Location;


public class LaserHead implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 7965234050471759790L;
	//private RemoteLaserSystem system;
	private Coordinates location;
	private Range range;
	private String id;
	private HashMap<String,Location> visibles;
	
	
	public LaserHead (String id,/* RemoteLaserSystem system,*/ Coordinates location, Range range){
		//this.system = system; 
		this.location = location;
		this.range = range;
		this.id = id;
		visibles = new HashMap<String, Location>();
	}
	
	/**
	 * La détection est ici simulée.
	 * Le chariot cavalier connaissant la position des bornes ainsi que leur portée,
	 * il detecte la station et lui envoit sa position a chaque iteration.
	 * La station n'a plus qu'a se charger de transmettre la position du chariot cavalier au
	 * système central.  
	 */
	public void detectStraddleCarrier (String straddleCarrierId, Location location) {
			visibles.put(straddleCarrierId,location);
	}
	
	public String getCSSStyle() {
		double xgu = getRange().x*2.0;
		double ygu = getRange().y*2.0;
		String color = "rgba(255,255,255,50)";
		String borderColor = "rgba(100,100,100,255)";
		String style = "size:"+xgu+"gu, "+ygu+"gu;"+
		"stroke-mode: plain;"+
		"stroke-width: 1px;"+
		"stroke-color: "+borderColor+";"+
		"fill-mode: plain; fill-color: "+color+";"+
		"z-index: 0;";
		return style;
	}
	
	public String getId(){
		return id;
	}
	
	public Coordinates getLocation(){
		return location;
	}
	
	public void clearVisibles(){
		visibles.clear();
	}
	
	public List<String> getVisibleStraddleCarriers(){
		return new ArrayList<String>(visibles.keySet());
	}
	
	public Location getLocation(String visibleStraddleCarrierId){
		return visibles.get(visibleStraddleCarrierId);
	}
	
	public Range getRange() {
		return range;
	}
	
	public void destroy(){
		id = null;
		location = null;
		range = null;
		visibles = null;
		
	}

	public void setRangeRate(double rate) {
		if(rate<0 || rate>1){
			new Exception("Rate out of bound exception : "+rate).printStackTrace();
			System.exit(ReturnCodes.RATE_OUT_OF_BOUND.getCode());
			//TODO allow rate > 1 ?
		}
		this.range.setRange(rate);
		
	}
}
