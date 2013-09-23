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

import org.missions.MissionPhase;
import org.system.Terminal;
import org.system.container_stocking.ContainerAlignment;
import org.system.container_stocking.ContainerLocation;
import org.time.Time;
import org.util.Location;
import org.vehicles.StraddleCarrier;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;

public class XMLStraddleCarrierMessageParser implements ContentHandler {
	private StraddleCarrier straddleCarrier;
	private boolean inModMission = false;
	private String modMissionId;
	private MissionPhase modMissionPhase;
	private String pave;
	private String lane;
	private String slot;
	private int level;
	private int align;
	private String cont;
	private ContainerLocation contLocation;
	private Time waitTime;

	public XMLStraddleCarrierMessageParser(StraddleCarrier sc) {
		super();
		straddleCarrier = sc;
		// FIXME
		// if(databaseManager==null) databaseManager =
		// NetworkConfiguration.databaseManager;
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
		if (localName.equals("containerLocation")) {
			contLocation = new ContainerLocation(cont, pave, lane, slot, level,
					align);
		} else if (localName.equals("modMission")) {
			inModMission = false;
			if (waitTime == null) {
				straddleCarrier.modMission(modMissionId, this.modMissionPhase,
						contLocation);
				// FIXME
				// if(databaseManager!=null&&databaseManager.getServer()!=null)
				// databaseManager.writeEvent(terminal.getTime(),
				// "userModification",
				// "<modMission id='"+modMissionId+"' phase='"+modMissionPhase+"'>"+contLocation.toXML()+"</modMission>\n");
			} else {
				straddleCarrier.modMission(modMissionId, this.modMissionPhase,
						waitTime);
				// FIXME
				// if(databaseManager!=null&&databaseManager.getServer()!=null)
				// databaseManager.writeEvent(terminal.getTime(),
				// "userModification",
				// "<modMission id='"+modMissionId+"' phase='"+modMissionPhase+"'><goto road='"+lastGotoLocation.getRoad().getId()+"' rate='"+lastGotoLocation.getPourcent()+"' direction='"+lastGotoLocation.getDirection()+"'/> <wait time='"+waitTime+"'/></modMission>\n");
				waitTime = null;
			}

			contLocation = null;
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

	@Override
	public void startElement(String uri, String localName, String qName,
			Attributes atts) throws SAXException {
		if (localName.equals("goto")) {
			String roadName = atts.getValue("road");
			double rate = 0.0;
			if (atts.getIndex("rate") >= 0) {
				rate = Double.parseDouble(atts.getValue("rate"));
			}

			boolean direction = true;
			if (atts.getIndex("direction") >= 0)
				direction = Boolean.parseBoolean(atts.getValue("direction"));
			Location l = new Location(Terminal.getInstance().getRoad(roadName),
					rate, direction);
			// System.out.println("Parsing GOTO : "+l+" inModMission = "+inModMission);
			straddleCarrier.goTo(l, !inModMission);

		} else if (localName.equals("domission")) {
			String missionId = atts.getValue("id");
			straddleCarrier.addMissionInWorkload(Terminal.getInstance()
					.getMission(missionId));

			System.out.println("DOMISSION " + missionId);
		} else if (localName.equals("modMission")) {
			inModMission = true;
			modMissionId = atts.getValue("id");
			modMissionPhase = MissionPhase.get(Integer.parseInt(atts.getValue("phase")));
		} else if (localName.equals("containerLocation")) {
			if (inModMission) {
				cont = atts.getValue("container");
				pave = atts.getValue("pave");
				lane = atts.getValue("lane");
				slot = atts.getValue("slot");
				level = Integer.parseInt(atts.getValue("level"));
				align = ContainerAlignment.getIntValue(atts.getValue("align"));

			}
		} else if (localName.equals("wait")) {
			waitTime = new Time(atts.getValue("time"));
		}

	}

	@Override
	public void startPrefixMapping(String prefix, String uri)
			throws SAXException {

	}

	
}
