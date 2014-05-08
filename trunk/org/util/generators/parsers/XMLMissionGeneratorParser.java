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

package org.util.generators.parsers;

import java.util.ArrayList;
import java.util.List;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;

/**
 * XML parser to setup the generator
 * @author Ga&euml;tan Lesauvage
 * @since 2011
 */
public class XMLMissionGeneratorParser implements  ContentHandler{
	private boolean comment = false;
	/**
	 * Random seed of the generator
	 */
	private Long seed;
	
	/**
	 * XML file of the terminal
	 */
	private String terminalFile;
	/**
	 * XML file of the vehicles
	 */
	private String vehiclesFile;
	/**
	 * Train generation data
	 */
	private List<TrainGenerationData> trainsData;
	/**
	 * Trucks generation data
	 */
	private List<TruckGenerationData> trucksData;
	/**
	 * Ships generation data
	 */
	private List<ShipGenerationData> shipsData;
	/**
	 * Stock generation data
	 */
	private List<StockGenerationData> stockData;
	
	private String containersFile;
	/**
	 * Constructor
	 */
	public XMLMissionGeneratorParser(){
		super();
		trainsData = new ArrayList<TrainGenerationData>();
		trucksData = new ArrayList<TruckGenerationData>();
		shipsData = new ArrayList<ShipGenerationData>();
		stockData = new ArrayList<StockGenerationData>();
	}

	@Override
	public void characters(char[] ch, int start, int length)
	throws SAXException {
	}

	@Override
	public void endDocument() throws SAXException {
	}

	@Override
	public void endElement(String uri, String localName, String qName)
	throws SAXException {
		if(localName.equals("comment")){
			comment = false;
		}
	}

	@Override
	public void endPrefixMapping(String prefix) throws SAXException {

	}

	@Override
	public void ignorableWhitespace(char[] ch, int start, int length)
	throws SAXException {
	}
	@Override
	public void processingInstruction(String target, String data)
	throws SAXException {
	}
	@Override
	public void setDocumentLocator(Locator locator) {
	}
	@Override
	public void skippedEntity(String name) throws SAXException {
	}
	@Override
	public void startDocument() throws SAXException {
	}
	/**
	 * @param localName belongs to {"comment" ,  "random",  "terminalFile", "vehiclesFile", "train",  "trucks", "ship", "stocks"}
	 */
	@Override
	public void startElement(String uri, String localName, String qName,Attributes atts) throws SAXException{
		if(!comment){
			if(localName.equals("comment")){
				comment = true;
			}
			else if(localName.equals("random")){
				seed = Long.parseLong(atts.getValue("seed"));
				System.out.println("SEED = "+seed);
			}
			else if(localName.equals("terminalFile")){
				terminalFile = atts.getValue("file");
			}
			else if(localName.equals("vehiclesFile")){
				vehiclesFile = atts.getValue("file");
			}
			else if(localName.equals("containersFile")){
				containersFile = atts.getValue("file");
			}
			else if(localName.equals("train")){
				double fullRate = Double.parseDouble(atts.getValue("fullRate"));
				double afterUnload = Double.parseDouble(atts.getValue("afterUnload"));
				double afterReload = Double.parseDouble(atts.getValue("afterReload"));
				String minTime = atts.getValue("minTime");
				String maxTime = atts.getValue("maxTime");
				double marginRate = Double.parseDouble(atts.getValue("marginRate"));
				//TrainGenerationData td = new TrainGenerationData(minTime, maxTime, fullRate, afterUnload, afterReload, marginRate);
				//trainsData.add(td);
			}
			else if(localName.equals("trucks")){
				int nb = Integer.parseInt(atts.getValue("nb"));
				double rateComeEmpty = Double.parseDouble(atts.getValue("rateComeEmpty"));
				double rateLeaveEmpty = Double.parseDouble(atts.getValue("rateLeaveEmpty"));
				String avgTruckTimeBeforeLeaving = atts.getValue("avgTruckTimeBeforeLeaving");
				String minTime = atts.getValue("minTime");
				String maxTime = atts.getValue("maxTime");
				
				String groupID = "";
				if(atts.getIndex("groupID")>0) atts.getValue("groupID");
				
				//TruckGenerationData td = new TruckGenerationData(nb, rateComeEmpty, rateLeaveEmpty, avgTruckTimeBeforeLeaving, minTime, maxTime, groupID);
				//trucksData.add(td);
			}
			else if(localName.equals("ship")){
				int minTeuCapacity = Integer.parseInt(atts.getValue("minTEUCapacity"));
				int maxTeuCapacity = Integer.parseInt(atts.getValue("maxTEUCapacity"));
				
				double fullRate = Double.parseDouble(atts.getValue("fullRate"));
				double twentyFeetRate = Double.parseDouble(atts.getValue("twentyFeetRate"));
				double fortyFeetRate = Double.parseDouble(atts.getValue("fortyFeetRate"));
				double afterUnload = Double.parseDouble(atts.getValue("afterUnload"));
				double afterReload = Double.parseDouble(atts.getValue("afterReload"));
				double marginRate = Double.parseDouble(atts.getValue("marginRate"));
				int capacityFactor =  Integer.parseInt(atts.getValue("capacityFactor"));
				
				String maxArrivalTime = atts.getValue("maxArrivalTime");
				String minBerthTimeLength = atts.getValue("minBerthTimeLength");
				String maxDepartureTime = atts.getValue("maxDepartureTime");
				String timePerContainerOperation = atts.getValue("timePerContainerOperation");
				//ShipGenerationData sd = new ShipGenerationData(maxArrivalTime, minBerthTimeLength, maxDepartureTime, timePerContainerOperation, minTeuCapacity, maxTeuCapacity, capacityFactor, fullRate, twentyFeetRate, fortyFeetRate, afterUnload, afterReload, marginRate);
				//shipsData.add(sd);
			}
			else if(localName.equals("stocks")){
				int nb = Integer.parseInt(atts.getValue("nb"));
				String minTime = atts.getValue("minTime");
				String maxTime = atts.getValue("maxTime");
				//TODO Change by a rate ?
				String marginTime = atts.getValue("marginTime");
				String groupID = "";
				//if(atts.getIndex("groupID")>0) groupID = atts.getValue("groupID");
				//StockGenerationData sd = new StockGenerationData(nb, minTime, maxTime, marginTime, groupID);
				
				//stockData.add(sd);
			}
		}
	}	
	@Override
	public void startPrefixMapping(String prefix, String uri)
	throws SAXException {
	}

	public Long getSeed() {
		return seed;
	}

	public String getTerminalFile() {
		return terminalFile;
	}

	public String getVehiclesFile() {
		return vehiclesFile;
	}

	public List<TrainGenerationData> getTrainsData() {
		return trainsData;
	}

	public List<TruckGenerationData> getTrucksData() {
		return trucksData;
	}

	public List<ShipGenerationData> getShipsData() {
		return shipsData;
	}

	public List<StockGenerationData> getStockData() {
		return stockData;
	}

	public String getContainersFile() {
		return containersFile;
	}
}