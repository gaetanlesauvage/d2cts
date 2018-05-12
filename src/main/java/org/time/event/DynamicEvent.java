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
package org.time.event;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import org.apache.log4j.Logger;
import org.com.model.EventBean;
import org.exceptions.ContainerDimensionException;
import org.missions.Mission;
import org.missions.TruckMission;
import org.system.Terminal;
import org.system.container_stocking.Container;
import org.system.container_stocking.ContainerLocation;
import org.system.container_stocking.Slot;
import org.time.Time;
import org.time.TimeWindow;

public abstract class DynamicEvent {
	protected static final Logger log = Logger.getLogger(DynamicEvent.class);
	protected Time time;
	protected String value;
	protected String type;

	protected DynamicEvent(Time time, String type) {
		this.time = time;
		this.type = type;
	}

	protected DynamicEvent(Time time, String type, String value) {
		this.time = time;
		this.type = type;
		this.value = value;
	}

	public abstract void execute();

	public Time getTime() {
		return time;
	}

	public String getType() {
		return type;
	}

	public String getDatabaseValue() {
		return value;
	}

	public void writeEventInDb() {
		// if(databaseManager!=null&&databaseManager.getServer()!=null)
		// databaseManager.writeEvent(time, type, value);
		// FIXME
	}

	public static DynamicEvent create(EventBean bean){
		DynamicEvent event = null;

		Time t = bean.getTime();
		Map<String,String> properties = EventDescriptionParser.getInstance().parse(bean.getDescription());

		List<String> slots = null;
		List<String> lanes = null;
		String lanesProperty = null;
		StringTokenizer lanesTokenizer = null;
		String containersProperty = null;
		StringTokenizer containersTokenizer = null;

		switch(bean.getType()){
		case ShipIn:
			event = new ShipIn(t, Integer.parseInt(properties.get("capacity")), properties.get("quay"), 
					Double.parseDouble(properties.get("from")), Double.parseDouble(properties.get("to")));
			String containersOnBoard = properties.get("containers").substring(1);
			containersOnBoard = containersOnBoard.substring(0, containersOnBoard.indexOf('}'));
			StringTokenizer st = new StringTokenizer(containersOnBoard, ";");
			while(st.hasMoreTokens()){
				((ShipIn)event).addContainerToUnload(st.nextToken());
			}
			//Capacity
			//Quay
			//From
			//To
			//event = new ShipIn(...);
			break;
		case ShipOut:
			event = new ShipOut(t, Integer.parseInt(properties.get("capacity")), properties.get("quay"), 
					Double.parseDouble(properties.get("from")), Double.parseDouble(properties.get("to")));
			String containersToLoad = properties.get("containers").substring(1);
			containersToLoad = containersToLoad.substring(0, containersToLoad.indexOf('}'));
			StringTokenizer stShipOut = new StringTokenizer(containersToLoad, ";");
			while(stShipOut.hasMoreTokens()){
				((ShipOut)event).addContainerToLoad(stShipOut.nextToken());
			}
			break;
		case StraddleCarrierFailure:
			break;
		case StraddleCarrierRepaired:
			break;
		case ContainerOut:
			break;
		case ShipContainerOut:
			event = new ShipContainerOut(t, properties.get("containerId"), properties.get("slotId"));
			break;
		case AffectMission:
			break;
		case VehicleIn:
			lanes = new ArrayList<>();
			slots = new ArrayList<>();
			lanesProperty =  properties.get("lanes");
			lanesTokenizer = new StringTokenizer(lanesProperty.substring(1, lanesProperty.length()-1),":");
			while(lanesTokenizer.hasMoreTokens()){
				lanes.add(lanesTokenizer.nextToken());	
			}

			for(String lane : lanes){
				String[] slotID = Terminal.getInstance().getSlotNames(lane);
				for(String slot : slotID){
					slots.add(slot);
				}
			}
			List<Container> containers = new ArrayList<>();
			containersProperty = properties.get("containers");
			if(containersProperty!=null){
				containersTokenizer = new StringTokenizer(containersProperty.substring(1, containersProperty.length()-1),"|");
				while(containersTokenizer.hasMoreTokens()){
					String containerDescription = containersTokenizer.nextToken();
					Map<String, String> descriptionParsed = EventDescriptionParser.getInstance().parse(containerDescription,";",":");
					Container c;
					try {
						c = new Container(descriptionParsed.get("id"), Double.parseDouble(descriptionParsed.get("teu")));

						String slotID = descriptionParsed.get("slot");
						Slot slot = Terminal.getInstance().getSlot(slotID);
						int level = Integer.parseInt(descriptionParsed.get("level"));

						int alignment = Integer.parseInt(descriptionParsed.get("alignement"));
						ContainerLocation cl = new ContainerLocation(c.getId(), slot.getPaveId(), slot.getLocation().getRoad().getId(), slotID, level, alignment);
						c.setContainerLocation(cl);
						containers.add(c);
					} catch (NumberFormatException e) {
						e.printStackTrace();
					} catch (ContainerDimensionException e) {
						e.printStackTrace();
					}
				}
			}
			event = new  VehicleIn(t, properties.get("id"), lanes, slots, containers);
			break;
		case VehicleOut:
			lanes = new ArrayList<>();
			slots = new ArrayList<>();
			lanesProperty = properties.get("lanes");
			lanesTokenizer = new StringTokenizer(lanesProperty.substring(1, lanesProperty.length()-1),":");
			while(lanesTokenizer.hasMoreTokens()){
				lanes.add(lanesTokenizer.nextToken());	
			}

			for(String lane : lanes){
				String[] slotID = Terminal.getInstance().getSlotNames(lane);
				for(String slot : slotID){
					slots.add(slot);
				}
			}
			List<String> containersIds = new ArrayList<>();
			containersProperty = properties.get("containers");
			if(containersProperty != null){
				containersTokenizer = new StringTokenizer(containersProperty.substring(1, containersProperty.length()-1),";");
				while(containersTokenizer.hasMoreTokens()){
					containersIds.add(containersTokenizer.nextToken());
				}
			}
			event = new  VehicleOut(t, properties.get("id"), lanes, slots, containersIds);
			break;
		case LaserHeadFailure:
			break;
		case NewMission:
			if(properties.get("truck")==null || properties.get("truck").equals("null")){
				Slot slot = Terminal.getInstance().getSlot(properties.get("slot"));
				ContainerLocation missionLocation = new ContainerLocation(
						properties.get("container"),
						slot.getPaveId(),
						slot.getLocation().getRoad().getId(),
						slot.getId(),
						Integer.parseInt(properties.get("level")),
						Integer.parseInt(properties.get("alignement")));

				Mission m = new Mission(
						properties.get("id"),
						Integer.parseInt(properties.get("kind")),
						new TimeWindow(new Time(properties.get("twPMin")), new Time(properties.get("twPMax"))),
						new TimeWindow(new Time(properties.get("twDMin")), new Time(properties.get("twDMax"))),
						properties.get("container"),
						missionLocation);
				event = new NewMission(t, m);
			} else {
				Slot slot = Terminal.getInstance().getSlot(properties.get("slot"));
				ContainerLocation missionLocation = new ContainerLocation(
						properties.get("container"),
						slot.getPaveId(),
						slot.getLocation().getRoad().getId(),
						slot.getId(),
						Integer.parseInt(properties.get("level")),
						Integer.parseInt(properties.get("alignement")));

				Mission m = new TruckMission(
						properties.get("id"), properties.get("truck"),
						Integer.parseInt(properties.get("kind")),
						new TimeWindow(new Time(properties.get("twPMin")), new Time(properties.get("twPMax"))),
						new TimeWindow(new Time(properties.get("twDMin")), new Time(properties.get("twDMax"))),
						properties.get("container"),
						missionLocation);
				event = new NewMission(t, m);
			}
			break;
		case ChangeContainerLocation:
			break;
		case NewContainer:
			break;
		case NewShipContainer:
			Slot slot = Terminal.getInstance().getSlot(properties.get("slot"));
			ContainerLocation missionLocation = new ContainerLocation(
					properties.get("id"),
					slot.getPaveId(),
					slot.getLocation().getRoad().getId(),
					slot.getId(),
					Integer.parseInt(properties.get("level")),
					Integer.parseInt(properties.get("alignement")));

			event = new NewShipContainer(t, properties.get("id"), 
					Double.parseDouble(properties.get("teu")), 
					missionLocation, properties.get("quay"),
					Double.parseDouble(properties.get("from")),
					Double.parseDouble(properties.get("to")));
			break;
		}
		/*if (current_event_type.get(inEvent - 1) == null) {
			System.out
			.println("ERROR : type value must be given in a <event> xml tag !");
		} else if (current_event_type.get(inEvent - 1).equals("shipIn")) {
			int capacity = Integer.parseInt(atts.getValue("capacity"));
			String quay = atts.getValue("quay");
			double from = Double.parseDouble(atts.getValue("from"));
			double to = Double.parseDouble(atts.getValue("to"));
			currentShipIn = new ShipIn(t, capacity, quay, from, to);
		} else if (current_event_type.get(inEvent - 1).equals("shipOut")) {
			int capacity = Integer.parseInt(atts.getValue("capacity"));
			String quay = atts.getValue("quay");
			double from = Double.parseDouble(atts.getValue("from"));
			double to = Double.parseDouble(atts.getValue("to"));
			currentShipOut = new ShipOut(t, capacity, quay, from, to);
		} else if (current_event_type.get(inEvent - 1).equals(
				"straddleCarrierFailure")) {
			String straddleCarrierID = atts.getValue("straddleCarrierID");
			String failureType = atts.getValue("failureType");
			String repairDuration = atts.getValue("repairDuration");

			StraddleCarrierFailure scf = new StraddleCarrierFailure(t,
					straddleCarrierID, failureType, new Time(repairDuration));
			scheduler.registerDynamicEvent(scf);
		} else if (current_event_type.get(inEvent - 1).equals("straddleRepair")) {
			String straddleId = atts.getValue("straddleId");
			StraddleCarrierRepaired scr = new StraddleCarrierRepaired(t,
					straddleId);
			scheduler.registerDynamicEvent(scr);
		} else if (current_event_type.get(inEvent - 1).equals("containerOut")) {
			String containerId = atts.getValue("containerId");
			ContainerOut co = new ContainerOut(t, containerId);
			scheduler.registerDynamicEvent(co);
		} else if (current_event_type.get(inEvent - 1).equals(
				"shipContainerOut")) {
			String containerId = atts.getValue("containerId");
			String destSlotId = atts.getValue("slotId");
			ShipContainerOut sco = new ShipContainerOut(t, containerId,
					destSlotId);
			scheduler.registerDynamicEvent(sco);
		} else if (current_event_type.get(inEvent - 1).equals("affectMission")) {
			String missionId = atts.getValue("mission");
			String straddleCarrierId = atts.getValue("straddleCarrier");
			AffectMission am = new AffectMission(
					current_event_time.get(inEvent - 1), missionId,
					straddleCarrierId);
			scheduler.registerDynamicEvent(am);
		} else if (current_event_type.get(inEvent - 1).equals("vehicleIn")
				|| current_event_type.get(inEvent - 1).equals("vehicleOut")) {
			current_event_vehicle_ID = atts.getValue("id");
			String lanesIds = atts.getValue("lanes");
			StringTokenizer st = new StringTokenizer(lanesIds, ",");
			ArrayList<String> l = new ArrayList<String>();
			vehicleInOutLaneIds = new ArrayList<String>(st.countTokens());
			while (st.hasMoreTokens()) {
				String lID = st.nextToken();
				vehicleInOutLaneIds.add(lID);
				List<Slot> slotsIds = getRemoteTerminal().getSlots(lID);

				for (Slot s : slotsIds) {
					l.add(s.getId());

				}
			}
			vehicleInOutSlotIds = l;
		} else if (current_event_type.get(inEvent - 1).equals(
				LaserHeadFailure.getEventType())) {
			String lhID = atts.getValue("laserHeadID");
			double range = Double.parseDouble(atts.getValue("range"));
			Time duration = null;
			if (atts.getIndex("duration") >= 0) {
				duration = new Time(atts.getValue("duration"));
			}
			LaserHeadFailure lhf = new LaserHeadFailure(
					current_event_time.get(inEvent - 1), lhID, range, duration);
			scheduler.registerDynamicEvent(lhf);
		}
		 */
		return event;
	}

	static final class EventDescriptionParser {
		private static final EventDescriptionParser instance = new EventDescriptionParser();

		protected static EventDescriptionParser getInstance() {
			return instance;
		}

		public Map<String,String> parse(String description){
			return parse(description,",","=");
		}

		public Map<String,String> parse(String description, String delimiter1, String delimiter2){
			Map<String, String> properties = new HashMap<>();

			StringTokenizer tokenizer = new StringTokenizer(description,delimiter1);
			while (tokenizer.hasMoreTokens()){
				StringTokenizer propertiesTokenizer = new StringTokenizer(tokenizer.nextToken(), delimiter2);
				String property = propertiesTokenizer.nextToken();
				String value = propertiesTokenizer.nextToken();
				properties.put(property, value);
			}
			return properties;
		}
	}
}
