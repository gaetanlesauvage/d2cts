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

import java.awt.Color;
/**
 * ISO dimensions of Maritime Containers
 * @author Gaetan Lesauvage
 *
 */
public abstract class ContainerKind {
	
		private static final int TYPE_NOT_FOUND = -99;

		/**
		 * Dimensions of containers stocked by type index
		 */
		private static double[][] datas = {
			{12.192,2.438,2.591},
			{6.058,2.438,2.591},
			{13.716,2.438,2.896}
		};
		
		private static float[] teu = {2 , 1, 2.25f};
		
		/**
		 * Color of containers type
		 */
		private static Color[] colors = {
			new Color(150,0,0,255),
			new Color(0,150,0,255),
			new Color(0,0,150,255),
		};
		
		/**
		 * Color of a container type
		 * @param type type
		 * @return color
		 */
		public static Color getColor(int type){
			return colors[type];
		}
		
		/**
		 * Return the color of a level
		 * @param level level
		 * @return color
		 */
		public static Color getColorOfLevel(int level) {
			switch(level){
			case 0: return new Color(0,0,0,255);
			case 1: return new Color(50,50,50,255);
			case 2: return new Color(150,150,150,255);
			case 3: return new Color(255,255,255,255);
			}
			return new Color(0,0,0,255);
		}
		
		/**
		 * Get the height of a container type
		 * @param type type
		 * @return height
		 */
		public static double getHeight(int type){
			return datas[type][2];
		}
		
		/**
		 * Get the length of a container type
		 * @param type type
		 * @return length
		 */
		public static double getLength(int type){
			return datas[type][0];
		}
		
		public static int getNbOfTypes(){
			return datas.length;
		}
		
		public static float getTeu(int type){
			return teu[type]; 
		}
		/**
		 * Get the width of a container type
		 * @param type type
		 * @return width
		 */
		public static double getWidth(int type){
			return datas[type][1];
		}

		public static int getType(double teu) {
			for(int i=0; i<ContainerKind.teu.length ; i++){
				if(ContainerKind.teu[i]==teu) return i;
			}
			
			return -1;
		}
		
		/**
		 * Retrieve the type according to its string representation in feet
		 * @param type "20" or "40" or "45"
		 * @return
		 */
		public static int getType(String type){
			switch(type){
			case "20": return getType(1.0);
			case "40": return getType(2.0);
			case "45": return getType(2.25);
			default:
				return TYPE_NOT_FOUND; 
			}
		}
}
