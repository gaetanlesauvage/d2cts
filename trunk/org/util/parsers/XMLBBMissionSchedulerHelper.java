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
package org.util.parsers;

import org.scheduling.MissionSchedulerEvalParameters;

/**
 * Helper used to set the Branch and Bound mission scheduler parameters
 * @author Ga&euml;tan Lesauvage
 * @since 2012
 */
public class XMLBBMissionSchedulerHelper extends XMLMissionSchedulerHelper{
	/**
	 * Name of the file describing the time matrix costs
	 */
	private String timeMatrixFile;
	/**
	 * Name of the file describing the distance matrix costs
	 */
	private String distanceMatrixFile;
	/**
	 * If TRUE then the time and distance matrix will be computed by the algorithm and stored in the corresponding files, 
	 * else the matrices will be read from the corresponding files
	 */
	private boolean evalCosts;
	
	private String solutionInitFile;
	private String solutionFile;
	
	/**
	 * Constructor
	 * @param timeMatrixFile Name of the file describing the time matrix costs
	 * @param distanceMatrixFile Name of the file describing the distance matrix costs
	 * @param evalCosts If TRUE then the time and distance matrix will be computed by the algorithm and stored in the corresponding files, else the matrices will be read from the corresponding files
	 */
	public XMLBBMissionSchedulerHelper (String timeMatrixFile, String distanceMatrixFile, boolean evalCosts, String solutionInitFile, String solutionFile, MissionSchedulerEvalParameters evalParameters){
		super(evalParameters);
		this.timeMatrixFile = timeMatrixFile;
		this.distanceMatrixFile = distanceMatrixFile;
		this.evalCosts = evalCosts;
		this.solutionInitFile = solutionInitFile;
		this.solutionFile = solutionFile;
	}
	
	/**
	 * Get the time matrix file name
	 * @return The time matrix file name
	 */
	public String getTimeMatrixFile(){
		return timeMatrixFile;
	}
	
	/**
	 * Get the distance matrix file name
	 * @return The distance matrix file name
	 */
	public String getDistanceMatrixFile(){
		return distanceMatrixFile;
	}
	
	/**
	 * If TRUE then the time and distance matrix will be computed by the algorithm and stored in the corresponding files, else the matrices will be read from the corresponding files
	 * @return If TRUE then the time and distance matrix will be computed by the algorithm and stored in the corresponding files, else the matrices will be read from the corresponding files
	 */
	public boolean evalCosts(){
		return evalCosts;
	}

	public String getSolutionInitFile() {
		return solutionInitFile;
	}
	
	public String getSolutionFile() {
		return solutionFile;
	}
}