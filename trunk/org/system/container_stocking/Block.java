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
package org.system.container_stocking;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import org.positioning.Coordinates;



/**
 * A "pavé" is a square area where containers are stacked in many long lanes.
 * @author gaetan
 *
 */
public class Block {
	public static final int TYPE_SHIP = 0;
	public static final int TYPE_YARD = 1;
	public static final int TYPE_ROAD = 2;
	public static final int TYPE_RAILWAY = 3;
	public static final int TYPE_DEPOT = 4;
		
	private HashMap<String, Bay> lanes;
	protected ConcurrentHashMap<String, Coordinates> coords;
	protected List<String> sortedCoordsName;
	protected ConcurrentHashMap<String, String> walls;
	protected String id;
	private BlockType type;
	
	
	/*public Pave(String id){
		this.id = id;
		type = PaveType.STOCK;
	}*/
	public Block(String id, BlockType type){
		this.id = id;
		this.type = type;
		//TODO ajouter le type dans les fichiers xml
		// modifier le parser
		// modifier la façon de stocker les paves ?
		// => le but est de faciliter la génération de missions
		// 1 mission = 1 conteneur + 1 destination + 2 TW 
		// la source + destination définissent le type de la mission
		// donc il faut etre capable en fonction du type de mission a accomplir
		// de trouver les conteneurs et les emplacements qui remplissent les criteres 
	}
	public void addCoords(String name, Coordinates coords){
		if(this.coords==null){
			this.coords = new ConcurrentHashMap<String, Coordinates>();
			//this.sortedCoordsName = Collections.synchronizedList(new ArrayList<String>());
			this.sortedCoordsName = new ArrayList<String>();
		}
		this.coords.put(name, coords);
		this.sortedCoordsName.add(name);
	}
	
	public void addLane(Bay l){
		if(lanes == null) lanes = new HashMap<String, Bay>();
		if(!l.getPaveId().equals(id)){
			new Exception("Wrong pave exception for lane "+l.getId()+" pave "+id+" l.paveID = "+l.getPaveId()).printStackTrace();
		}
		lanes.put(l.getId(), l);
	}
	
	public void addWall(String from, String to){
		if(this.walls==null) this.walls = new ConcurrentHashMap<String, String>();
		this.walls.put(from, to);
	}
	
	public ConcurrentHashMap<String, Coordinates> getCoordinates(){
		return coords;
	}
	
	public Coordinates getCoordinates(String point){
		return coords.get(point);
	}
	
	public String getId() {
		return id;
	}
	
	public ConcurrentHashMap<String, String> getWalls(){
		return walls;
	}
	public BlockType getType() {
		return type;
	}
	
	public String toString(){
		return id+" type "+type;
	}
	
	public List<Bay> getLanes(){
		return new ArrayList<Bay>(lanes.values());
	}
//	public void destroy() {
//		if(coords!=null){
//			coords.clear();
//			coords=null;
//		}
//		if(lanes!=null){
//			lanes.clear();
//			lanes=null;
//		}
//		if(sortedCoordsName!=null){
//		sortedCoordsName.clear();
//		sortedCoordsName=null;
//		}
//		type=null;
//		if(walls!=null){
//			walls.clear();
//			walls = null;
//		}
//	}

}
