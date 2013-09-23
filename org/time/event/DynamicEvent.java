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

import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

import org.com.model.EventBean;
import org.missions.Mission;
import org.system.Terminal;
import org.system.container_stocking.ContainerLocation;
import org.system.container_stocking.Slot;
import org.time.Time;
import org.time.TimeWindow;

public abstract class DynamicEvent {
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

		switch(bean.getType()){
		case ShipIn:
			//Capacity
			//Quay
			//From
			//To
			//event = new ShipIn(...);
			break;
		case ShipOut:
			break;
		case StraddleCarrierFailure:
			break;
		case StraddleCarrierRepaired:
			break;
		case ContainerOut:
			break;
		case ShipContainerOut:
			break;
		case AffectMission:
			break;
		case VehicleIn:
			break;
		case VehicleOut:
			break;
		case LaserHeadFailure:
			break;
		case NewMission:
			if(properties.get("truckID").equals("null")){
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
				//FIXME handle truck vehicle case...
			}
			break;
		case ChangeContainerLocation:
			break;
		case NewContainer:
			break;
		case NewShipContainer:
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
			Map<String, String> properties = new HashMap<>();

			StringTokenizer tokenizer = new StringTokenizer(description,",");
			while (tokenizer.hasMoreTokens()){
				StringTokenizer propertiesTokenizer = new StringTokenizer(tokenizer.nextToken(), "=");
				String property = propertiesTokenizer.nextToken();
				String value = propertiesTokenizer.nextToken();
				properties.put(property, value);
			}
			return properties;
		}
	}
}
