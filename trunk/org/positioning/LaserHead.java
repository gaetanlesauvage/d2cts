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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.conf.parameters.ReturnCodes;
import org.system.Terminal;
import org.util.Location;


public class LaserHead{
	private Coordinates location;
	private Range range;
	private String id;
	private Map<String,Location> visibles;
	private Set<String> visiblesIds;
	
	@Override
	public int hashCode(){
		return id.hashCode();
	}
	@Override
	public boolean equals(Object o){
		return hashCode() == o.hashCode();
	}
	
	public LaserHead (String id, Coordinates location, Range range){
		this.location = location;
		this.range = range;
		this.id = id;
		int defaultSize = Terminal.getInstance().getStraddleCarriersCount();
		visibles = new HashMap<String, Location>(defaultSize);
		visiblesIds = new HashSet<>(defaultSize);
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
		visiblesIds.clear();
	}
	
	public Set<String> getVisibleStraddleCarriers(){
		return visiblesIds;
		//return new ArrayList<String>(visibles.keySet());
	}
	
	public Location getLocation(String visibleStraddleCarrierId){
		return visibles.get(visibleStraddleCarrierId);
	}
	
	public Range getRange() {
		return range;
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
