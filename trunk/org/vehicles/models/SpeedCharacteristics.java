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
/**
 * 
 * @author Ga&euml;tan Lesauvage
 * @since 2009
 *
 *	This object is used to model the speed characteristics of a straddle carrier model
 *
 */
public class SpeedCharacteristics implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 468701886958921292L;
	private double emptySpeed;
	private double loadedSpeed;
	private double baySpeed;
	private RandomSpeed containerOnTruckHandlingTime;
	private RandomSpeed containerOnGroundHandlingTime;
	private RandomSpeed timeToEnterExitABay;
	
	private double turnBackTime;
	
	public SpeedCharacteristics(double emptySpeed, double loadedSpeed, double baySpeed,
			RandomSpeed containerHandlingTimeFromTruck, RandomSpeed containerHandlingTimeFromGround, RandomSpeed timeToEnterABay,
			double turnBackTime){
		this.emptySpeed = emptySpeed;
		this.loadedSpeed = loadedSpeed;
		this.baySpeed = baySpeed;
		this.containerOnTruckHandlingTime = containerHandlingTimeFromTruck;
		this.containerOnGroundHandlingTime = containerHandlingTimeFromGround;
		this.timeToEnterExitABay = timeToEnterABay;
		this.turnBackTime = turnBackTime;
	}
	
	public double getLaneSpeed() {
		return baySpeed;
	}
	
	public double getSpeed(){
		return emptySpeed;
	}

	public double getTurnBackTime() {
		return turnBackTime;
	}
	
	public double getLoadedSpeed(){
		return loadedSpeed;
	}
	
	public double getContainerHandlingTimeFromTruck(){
		return containerOnTruckHandlingTime.getAValue();
	}
	
	public double getContainerHandlingTimeFromGround(){
		return containerOnGroundHandlingTime.getAValue();
	}
	
	public double getContainerHandlingTimeFromTruckMIN(){
		return containerOnTruckHandlingTime.getMin();
	}
	
	public double getContainerHandlingTimeFromGroundMIN(){
		return containerOnGroundHandlingTime.getMin();
	}
	
	
	public double getContainerHandlingTimeFromGroundMAX(){
		return containerOnGroundHandlingTime.getMax();
	}
	public double getContainerHandlingTimeFromTruckMAX(){
		return containerOnTruckHandlingTime.getMax();
	}
	public double getTimeToEnterExitABay(){
		return timeToEnterExitABay.getAValue();
	}
}
