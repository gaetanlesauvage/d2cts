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

import java.util.ArrayList;
import java.util.HashMap;

import org.apache.log4j.Logger;
import org.exceptions.NotInContainerException;
import org.missions.Mission;
import org.missions.TruckMission;
import org.system.container_stocking.ContainerAlignment;
import org.system.container_stocking.ContainerLocation;
import org.time.Time;
import org.time.TimeWindow;
import org.time.event.NewMission;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;

public class XMLMissionsParser implements ContentHandler {
	private static final Logger log = Logger.getLogger(XMLMissionsParser.class);

	private ArrayList<Time> current_event_time;
	private ArrayList<String> current_event_type;
	private int inEvent = 0;

	private boolean inMission = false;
	private String idMission, missionContainerId;
	private int missionKind;
	private TimeWindow twPickup, twDelivery;
	private ContainerLocation missionLocation;

	private Mission current_mission;

	private boolean comment = false;
	private String current_event_newMission_truckID = "";

	private HashMap<Mission, Time> missionMap;

	public XMLMissionsParser() {
		super();
		this.current_event_time = new ArrayList<Time>(2);
		this.current_event_type = new ArrayList<String>(2);
		missionMap = new HashMap<Mission, Time>();
		inEvent = 0;
	}

	@Override
	public void characters(char[] ch, int start, int length)
			throws SAXException {

	}

	private void containerLocation(Attributes atts)
			throws NotInContainerException {
		String container = null;
		if (atts.getIndex("container") > 0) {
			container = atts.getValue("container");
		} else if (inMission) {
			container = missionContainerId;
		}

		if (container != null) {
			String pave = atts.getValue("pave");
			String lane = atts.getValue("lane");
			String slot = atts.getValue("slot");
			int level = Integer.parseInt(atts.getValue("level"));
			String align = atts.getValue("align");
			ContainerLocation cl = new ContainerLocation(container, pave, lane,
					slot, level, ContainerAlignment.valueOf(align).getValue());
			if (inMission) {
				missionLocation = cl;
			} else
				throw new NotInContainerException();
		}
	}

	public HashMap<Mission, Time> getMap() {
		return missionMap;
	}

	@Override
	public void endDocument() throws SAXException {

	}

	@Override
	public void endElement(String uri, String localName, String qName)
			throws SAXException {
		if (localName.equals("comment"))
			comment = false;
		else if (comment == false) {
			if (localName.equals("mission")) {
				if (current_event_newMission_truckID.equals("")) {
					Mission m = new Mission(idMission, missionKind, twPickup,
							twDelivery, missionContainerId, missionLocation);
					// System.out.println("m.toString : "+m.toString());
					if (inEvent == 0) {
						missionMap.put(m, new Time(0));
					} else
						current_mission = m;
				} else {
					TruckMission m = new TruckMission(idMission,
							current_event_newMission_truckID, missionKind,
							twPickup, twDelivery, missionContainerId,
							missionLocation);
					// System.out.println("m.toString : "+m.toString());
					if (inEvent == 0) {
						missionMap.put(m, new Time(0));
					} else
						current_mission = m;
				}

				inMission = false;
				twDelivery = twPickup = null;
				this.idMission = "";
				this.missionContainerId = "";
				this.missionLocation = null;
			} else if (localName.equals("event")) {
				if (current_event_type.get(inEvent - 1).equals("newMission")) {
					NewMission nm = null;
					if (current_event_newMission_truckID.equals(""))
						nm = new NewMission(
								current_event_time.get(inEvent - 1),
								current_mission);
					else
						nm = new NewMission(
								current_event_time.get(inEvent - 1),
								(TruckMission) current_mission);
					missionMap.put(current_mission, nm.getTime());
					current_mission = null;
				}

				current_event_time.remove(inEvent - 1);
				current_event_type.remove(inEvent - 1);
				inEvent--;

			}

		}
	}

	@Override
	public void endPrefixMapping(String prefix) throws SAXException {

	}

	private void event(Attributes atts) {
		inEvent++;
		Time t = new Time(atts.getValue("time"));
		current_event_time.add(t);
		current_event_type.add(atts.getValue("type"));
		if (current_event_type.get(inEvent - 1) == null) {
			System.out
					.println("ERROR : type value must be given in a <event> xml tag !");
		}
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

		if (!comment) {

			try {
				if (localName.equals("comment")) {
					comment = true;
				} else if (localName.equals("mission")) {
					inMission = true;
					idMission = atts.getValue("id");
					missionContainerId = atts.getValue("container");
					missionKind = Integer.parseInt(atts.getValue("kind"));
					if (atts.getIndex("truck") >= 0) {
						current_event_newMission_truckID = atts
								.getValue("truck");
					}
				} else if (localName.equals("timewindow")) {
					timeWindow(atts);
				}

				else if (localName.equals("containerLocation")) {
					containerLocation(atts);
				} else if (localName.equals("event")) {
					event(atts);
				}
			} catch (NotInContainerException e) {
				e.printStackTrace();
				log.error(e.getMessage(), e);
			}
		}
	}

	@Override
	public void startPrefixMapping(String prefix, String uri)
			throws SAXException {

	}

	private void timeWindow(Attributes atts) {
		Time start = new Time(atts.getValue("start"));
		Time end = new Time(atts.getValue("end"));
		TimeWindow tw = new TimeWindow(start, end);
		if (inMission) {
			// System.out.println("IN MISSION -> ");
			if (twPickup == null) {
				twPickup = tw;
				// System.out.println("TW_PICKUP= "+twPickup);
			} else {
				twDelivery = tw;
				// System.out.println("TW_DELIVERY= "+twDelivery);
			}
		}
	}

}
