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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.positioning.Coordinates;
import org.system.Crossroad;



public class BayCrossroad extends Crossroad implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 230369341802408625L;

	private String mainRoad;
	private ArrayList<String> laneIDs;

	public BayCrossroad (String id, Coordinates position, String mainRoad){
		super(id, position);
		this.mainRoad = mainRoad;
		laneIDs = new ArrayList<String>();
	}
	public void addLaneId(String id){
		laneIDs.add(id);
	}
	public List<String> getLaneIDs(){
		return laneIDs;
	}
	public String getMainRoad() {
		return mainRoad;
	}
	

}
