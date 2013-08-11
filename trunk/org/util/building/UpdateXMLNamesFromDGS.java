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
package org.util.building;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;
import java.util.StringTokenizer;

import org.conf.parameters.ReturnCodes;

public class UpdateXMLNamesFromDGS {


	public static void compareFiles (String dgsFile, String xmlFile){
		try{


			File f1 = new File(dgsFile);
			File f2 = new File(xmlFile);

			Scanner scan1 = new Scanner(f1);
			Scanner scan2 = new Scanner(f2);

			HashMap<Float, ArrayList<String>> mapDGS_XCoords = new HashMap<Float, ArrayList<String>>();
			HashMap<String , Float> mapDGS_YCoords = new HashMap<String, Float>();

			HashMap<Float, ArrayList<String>> mapXML_XCoords = new HashMap<Float, ArrayList<String>>();
			HashMap<String , Float> mapXML_YCoords = new HashMap<String , Float>();

			System.out.println("Comparing "+dgsFile+" and "+xmlFile);
			while(scan1.hasNextLine()){
				StringTokenizer st = new StringTokenizer(scan1.nextLine());
				if(st.hasMoreTokens()){
					String operation = st.nextToken();

					if(operation.equals("an")){
						Float x = 0f;
						Float y = 0f; 
						String id = st.nextToken();

						String xCoord = st.nextToken();
						if(xCoord.contains("x="))
						{
							xCoord = xCoord.substring(2);
						}
						else{
							System.out.println("X COORD ERROR !");
							System.exit(ReturnCodes.EXIT_ON_X_COORD_ERROR.getCode());
						}
						x = Float.parseFloat(xCoord);
						String yCoord = st.nextToken();
						if(yCoord.contains("y=")) y = Float.parseFloat(yCoord.substring(2)); 
						else{
							System.out.println("Y COORD ERROR ! ("+yCoord+")");
							System.exit(ReturnCodes.EXIT_ON_Y_COORD_ERROR.getCode());
						}

						ArrayList<String> l;
						if(mapDGS_XCoords.containsKey(x)){
							l = mapDGS_XCoords.get(x);
						}
						else l = new ArrayList<String>();
						l.add(id);
						mapDGS_XCoords.put(x, l);
						mapDGS_YCoords.put(id , y);
					}
				}
			}

			while(scan2.hasNextLine()){
				StringTokenizer st = new StringTokenizer(scan2.nextLine());
				if(st.hasMoreTokens()){
					String tag = st.nextToken();
					if(tag.contains("crossroad")||tag.contains("roadpoint")){
						String id = st.nextToken().substring(4);
						id = id.substring(0, id.length()-1);
						if(!id.contains("/")){
							String xCoord = st.nextToken().substring(3);
							xCoord = xCoord.substring(0, xCoord.length()-1);

							String yCoord = st.nextToken().substring(3);
							yCoord = yCoord.substring(0, yCoord.length()-1);


							Float x = Float.parseFloat(xCoord);
							Float y = Float.parseFloat(yCoord);
							ArrayList<String> lX;
							if(mapXML_XCoords.containsKey(x)) lX = mapXML_XCoords.get(x);
							else lX = new ArrayList<String>();

							lX.add(id);
							mapXML_XCoords.put(x, lX);
							mapXML_YCoords.put(id, y);
						}
					}
				}
			}


			// MAP ( XML_Name , DGS_Name)
			HashMap<String, String> equivalenceMap = new HashMap<String, String>(mapXML_YCoords.size());
			// MAP ( DGSL_Name , XML_Name)
			HashMap<String, String> equivalenceMapInv = new HashMap<String, String>(mapXML_YCoords.size());

			int nbMatches = 0;
			for(Float dgsX : mapDGS_XCoords.keySet()){
				if(mapXML_XCoords.containsKey(dgsX)){
					for(String idXML_X : mapXML_XCoords.get(dgsX)){
						//Coord Y coté XML
						Float yCoordXML = mapXML_YCoords.get(idXML_X);

						//Coords Y cote DGS : 
						for(String idDGS_X : mapDGS_XCoords.get(dgsX)){
							Float yCoordDgs = mapDGS_YCoords.get(idDGS_X);
							if(yCoordDgs.floatValue() == yCoordXML.floatValue()){
								System.out.println("DGS : "+idDGS_X+" <-> XML "+idXML_X+" (coords : x="+dgsX+" y= "+yCoordDgs+")");
								equivalenceMap.put(idXML_X, idDGS_X);
								equivalenceMapInv.put(idDGS_X, idXML_X);
								nbMatches++;
							}
						}
					}
				}
			}
			System.out.println(nbMatches+" equivalences found (DGS:"+mapDGS_YCoords.size()+" | XML:"+mapXML_YCoords.size()+") !");

			File newFileXML = new File(f2.getAbsolutePath()+"converted.xml");
			PrintWriter pw = new PrintWriter(newFileXML);

			scan2 = new Scanner(f2);
			while(scan2.hasNextLine()){
				String line = scan2.nextLine();

				StringTokenizer st = new StringTokenizer(line);
				if(st.hasMoreTokens()){
					String tag = st.nextToken();
					if(tag.contains("crossroad")||tag.contains("roadpoint")){
						String id = st.nextToken().substring(4);
						id = id.substring(0, id.length()-1);
						if(id.contains("/")) id= id.substring(0,id.length()-1);
						if(id.contains("\""))id= id.substring(0,id.length()-1);
						String equiv = equivalenceMap.get(id);
						
						System.out.println("Id = "+id+" equiv = "+equiv);
						String lineModif = line.replace("id=\""+id, "id=\""+equiv);
						pw.append(lineModif+"\n");

					}
					else{
						if(tag.equals("<road")||tag.equals("</road>")||tag.equals("</document>")){
						/*	String id = st.nextToken();
							id+=""; //TODO DELETE THIS USELESS LINE
							String idOrigin = st.nextToken().substring(8);
							idOrigin = idOrigin.substring(0,idOrigin.length()-1);
							//System.out.println(idOrigin);
							String idDestination = st.nextToken().substring(12);
							
							idDestination = idDestination.substring(0,idDestination.length()-1);
							if(idDestination.endsWith("/")) idDestination= idDestination.substring(0, idDestination.length()-1);
							//System.out.println(idDestination);
							String equivOrigin = equivalenceMap.get(idOrigin);
							String equivDestination = equivalenceMap.get(idDestination);

							String lineModif = line.replace("origin=\""+idOrigin, "origin=\""+equivOrigin);
							lineModif = lineModif.replace("destination=\""+idDestination, "destination=\""+equivDestination);
							pw.append(lineModif+"\n");*/
						}
						else{
							pw.append(line+"\n");
						}
					}

					pw.flush();
				}
			}
			scan1 = new Scanner(f1);
			while(scan1.hasNextLine()){
				String line = scan1.nextLine();

				StringTokenizer st = new StringTokenizer(line);
				if(st.hasMoreTokens()){
					String tag = st.nextToken();
					if(tag.equals("an")){
						String idNode = st.nextToken();
						if(!equivalenceMapInv.containsKey(idNode)){
							String x = st.nextToken();
							x = x.substring(2);
							String y = st.nextToken();
							y = y.substring(2);
							pw.append("<crossroad id=\""+idNode+"\" x=\""+x+"\" y=\""+y+"\" />\n");
						}
					}
				}
				pw.flush();
			}
			pw.append("</document>\n");
			pw.flush();
			pw.close();
		}catch(IOException e){
			e.printStackTrace();
		}
		
	}


	public static void main (String [] args){
		compareFiles(args[0],args[1]);
	}
}
