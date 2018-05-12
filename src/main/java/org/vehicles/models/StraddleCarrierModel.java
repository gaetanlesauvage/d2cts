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
package org.vehicles.models;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.system.container_stocking.ContainerKind;
import org.time.event.StraddleCarrierFailure;


public class StraddleCarrierModel implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = -4505350699034867564L;
	private String id;
	private Double width, height, length, innerWidth, innerLength, backOverLength, frontOverLength, cabWidth;
	private List<Integer> compatibility;
	//Speed in m/s
	private SpeedCharacteristics speed;
	
	public StraddleCarrierModel (String id, double width, double height, double length, double innerWidth, double innerLength, double backOverLength,
			double frontOverLength, double cabWidth, String compatibility, double emptySpeed, double loadedSpeed, double baySpeed, RandomSpeed containerHandlingTimeFromTruck, RandomSpeed containerHandlingTimeFromGround, RandomSpeed timeToEnterABay,
			double turnBackTime){
		this.id = id;
		this.width = width;
		this.height = height;
		this.length = length;
		this.innerWidth = innerWidth;
		this.innerLength = innerLength;
		this.backOverLength = backOverLength;
		this.frontOverLength = frontOverLength;
		this.cabWidth = cabWidth;
		this.speed = new SpeedCharacteristics(emptySpeed, loadedSpeed, baySpeed, containerHandlingTimeFromTruck, containerHandlingTimeFromGround, timeToEnterABay, turnBackTime);
		decodeCompatibility(compatibility);
	}
	
	public void decodeCompatibility(String list){
		//System.out.println("Compatibility = "+list);
		this.compatibility = Collections.synchronizedList(new ArrayList<Integer>(ContainerKind.getNbOfTypes()));
		if(list.equals("all")){
			for(int i=0; i<ContainerKind.getNbOfTypes(); i++) compatibility.add(1);
		}
		else{
			for(int i=0 ; i<ContainerKind.getNbOfTypes() ; i++){
				if(i<list.length()){
				int binary = Integer.parseInt(list.charAt(i)+"");
				try{
					compatibility.add(binary);
				}catch(NumberFormatException e){
					System.err.println("Compatibility Decode Exception ! Should be either \"all\" or \"0..1\"!");
				}
				}
				else{
					compatibility.add(0);
				}
			}
		}
	}

	public double getBackOverLength() {
		return backOverLength;
	}

	public double getCabWidth() {
		return cabWidth;
	}

	public List<Integer> getCompatibility() {
		return compatibility;
	}

	public double getFrontOverLength() {
		return frontOverLength;
	}

	public double getHeight() {
		return height;
	}

	public String getId() {
		return id;
	}

	public double getInnerLength() {
		return innerLength;
	}

	public double getInnerWidth() {
		return innerWidth;
	}

	public double getLength() {
		return length;
	}

	public SpeedCharacteristics getSpeedCharacteristics() {
		return speed;
	}

	private double getSpeed(boolean loaded) {
		return loaded ? getSpeedCharacteristics().getLoadedSpeed() : getSpeedCharacteristics().getSpeed();
	}

	/**
	 * 
	 * @param direction
	 * @param lane
	 * @return The speed in m/s
	 */
	public double getSpeed(boolean available, boolean loaded, boolean lane) {
		double s = getSpeed(loaded);
		if(lane) s = getSpeedCharacteristics().getLaneSpeed();
		if(!available) s *= StraddleCarrierFailure.TOWING_SPEED_RATIO;
		
		return s;
	}

	public double getWidth() {
		return width;
	}

	public boolean isCompatible(int containerType) {
		if(compatibility.get(containerType) == 1) return true;
		else return false;
	}
	
}
