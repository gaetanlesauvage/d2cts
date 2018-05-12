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
package org.scheduling.onlineACO;

import java.io.Serializable;
/**
 * Parameters of ACOMissionScheduler algorithm
 *  
 * @author Ga&euml;tan Lesauvage
 * @since 2011
 */
public class ACOParameters implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = -2864882909929805953L;
	
	//IMPORTANCE OF THE PHEROMONE TRACK IN THE CHOICE OF THE DESTINATION
	private double alpha;
	//IMPORTANCE OF EDGE WEIGHT IN THE CHOICE OF THE DESTINATION
	private double beta;
	//IMPORTANCE OF FOREIGN PHEROMONE TRACK IN THE CHOICE OF THE DESTINATION
	private double gamma;

	//PERSISTENCE OF THE PREVIOUS PHEROMONE TRACK (in [0,1])
	private double eta;
	
	
	//Threshold validation of a chosen destination (if( (ph(c)/sum(ph)) > delta) then validate choice else go back to depot)
	private double delta;
	
	//Quantity of pheromone to spread each time an ant choose a node
	private double lambda;
	//Quantity of pheromone to spread on each node of the best path when the first mission of the path has been started
	private double lAMBDA;
	
	private int sync;
	
	private double F1;
	private double F2;
	private double F3;
	/**
	 * Constructor
	 * @param alpha Importance of the pheromone track in the choice of the destination
	 * @param beta Importance of the edge weight in the choice of the destination
	 * @param gamma Importance of foreign pheromone track in the choice of the destination
	 * @param delta Threshold validation of a chosen destination (if( (ph(c)/sum(ph)) > delta) then validate choice else go back to depot)
	 * @param eta Persistence of the previous pheromone track (in [0,1])
	 * @param lambda Quantity of pheromone to spread each time an ant choose a node
	 * @param lAMBDA Quantity of pheromone to spread on each node of the best path when the first mission of the path has been started
	 */
	public ACOParameters (double alpha, double beta, double gamma, double delta, double eta, double lambda, double lAMBDA, int sync, double F1, double F2, double F3){
		this.alpha = alpha;
		this.beta = beta;
		this.eta = eta;
		this.gamma = gamma;
		this.delta = delta;
		this.lambda = lambda;
		this.lAMBDA = lAMBDA;
		this.sync = sync;
		this.F1 = F1;
		this.F2 = F2;
		this.F3 = F3;
	}
	
	public int getSync(){
		return sync;
	}
	
	public double getLambda(){
		return lambda;
	}
	
	public double getLAMBDA(){
		return lAMBDA;
	}
	
	public double getGamma(){
		return gamma;
	}
	
	public double getDelta(){
		return delta;
	}
	
	public double getAlpha(){
		return alpha;
	}
	
	public double getBeta(){
		return beta;
	}
	
	public double getPersistence(){
		return eta;
	}
	
	public double getF1(){
		return F1;
	}
	
	public double getF2(){
		return F2;
	}
	
	public double getF3(){
		return F3;
	}
	
	@Override
	public String toString(){
		return "Q="+lambda+" QR= "+lAMBDA+" ALPHA: "+alpha+" BETA: "+beta+" GAMMA: "+gamma+" DELTA: "+delta+" PERSISTENCE: "+eta+" F1="+F1+" F2="+F2+" F3="+F3; 
	}
}
