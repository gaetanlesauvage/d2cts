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

import java.io.IOException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.StringTokenizer;

import org.exceptions.ContainerDimensionException;
import org.exceptions.LaneTooSmallException;
import org.exceptions.NotInContainerException;
import org.exceptions.NotInDepotException;
import org.exceptions.NotInPaveException;
import org.exceptions.RegisteredObjectNotFoundException;
import org.missions.Mission;
import org.missions.TruckMission;
import org.positioning.Coordinates;
import org.positioning.LaserSystem;
import org.positioning.Range;
import org.routing.APSP.APSP;
import org.routing.AStar.AStarHandler;
import org.routing.AStar.DijkstraHeuristic;
import org.routing.AStar.DistanceAndSpeedHeuristic;
import org.routing.AStar.YDistanceHeuristic;
import org.routing.dijkstra.DijkstraHandler;
import org.routing.reservable.RDijkstraHandler;
import org.system.Crossroad;
import org.system.Depot;
import org.system.Road;
import org.system.RoadPoint;
import org.system.StraddleCarrierSlot;
import org.system.Terminal;
import org.system.container_stocking.Bay;
import org.system.container_stocking.BayCrossroad;
import org.system.container_stocking.Block;
import org.system.container_stocking.BlockType;
import org.system.container_stocking.Container;
import org.system.container_stocking.ContainerAlignment;
import org.system.container_stocking.ContainerLocation;
import org.system.container_stocking.ExchangeBay;
import org.system.container_stocking.Quay;
import org.system.container_stocking.SeaOrientation;
import org.system.container_stocking.Slot;
import org.time.Time;
import org.time.TimeScheduler;
import org.time.TimeWindow;
import org.time.event.AffectMission;
import org.time.event.ContainerOut;
import org.time.event.LaserHeadFailure;
import org.time.event.NewContainer;
import org.time.event.NewMission;
import org.time.event.NewShipContainer;
import org.time.event.ShipContainerOut;
import org.time.event.ShipIn;
import org.time.event.ShipOut;
import org.time.event.StraddleCarrierFailure;
import org.time.event.StraddleCarrierRepaired;
import org.time.event.VehicleIn;
import org.time.event.VehicleOut;
import org.util.Location;
import org.util.building.SlotBuilderHelper;
import org.vehicles.StraddleCarrier;
import org.vehicles.models.RandomSpeed;
import org.vehicles.models.StraddleCarrierModel;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

public class XMLTerminalComponentParser implements ContentHandler {
	public static final String rmiBindingName = "XMLTerminalComponentParser";
	private static Terminal terminal;
	private static TimeScheduler scheduler;

	private String depot;
	private boolean inDepot;

	private boolean inRoad, inPave;

	private Road road;
	private Bay lane;

	private Block pave;

	private ArrayList<Time> current_event_time;
	private ArrayList<String> current_event_type;
	private int inEvent = 0;

	private ShipIn currentShipIn;
	private ShipOut currentShipOut;

	private String currentStraddleCarrierId;
	private StraddleCarrier currentStraddleCarrier;
	private boolean inContainer;
	private String current_container_id;
	private double current_container_teu;

	private List<String> vehicleInOutSlotIds;
	private List<String> vehicleInOutLaneIds;
	private List<Container> vehicleInOutContainers;

	private ContainerLocation current_container_location;
	private boolean inMission = false;
	private String idMission, missionContainerId;
	private int missionKind;
	private TimeWindow twPickup, twDelivery;
	private ContainerLocation missionLocation;

	private Mission current_mission;

	private boolean comment = false;
	private HashMap<String, RoadPoint> roadPoints;
	private String currentHost;

	private boolean inShipContainer = false;
	private String current_ship_container_id = "";
	private Time current_ship_container_time = null;

	private String current_event_vehicle_ID = "";
	private String current_event_newMission_truckID = "";

	public XMLTerminalComponentParser() {
		super();
		roadPoints = new HashMap<String, RoadPoint>();
		this.current_event_time = new ArrayList<Time>(2);
		this.current_event_type = new ArrayList<String>(2);
		inDepot = false;
		inRoad = false;
		inPave = false;
		inContainer = false;
	}

	@Override
	public void characters(char[] ch, int start, int length)
			throws SAXException {

	}

	private void containerLocation(Attributes atts)
			throws NotInContainerException {
		String container;
		if (atts.getIndex("container") > 0) {
			container = atts.getValue("container");
		} else if (inMission) {
			container = missionContainerId;
		} else if (inContainer) {
			container = current_container_id;
		} else if (inShipContainer) {
			container = current_ship_container_id;
		} else {
			throw new NotInContainerException();
		}

		String pave = atts.getValue("pave");
		String lane = atts.getValue("lane");
		String slot = atts.getValue("slot");
		int level = Integer.parseInt(atts.getValue("level"));
		String align = atts.getValue("align");
		ContainerLocation cl = new ContainerLocation(container, pave, lane,
				slot, level, ContainerAlignment.valueOf(align).getValue());
		if (inMission) {
			missionLocation = cl;
		} else if (inContainer) {
			current_container_location = cl;
		} else if (inShipContainer) {
			current_container_location = cl;
		} else
			throw new NotInContainerException();
	}

	private void coordinate(Attributes atts) {
		String id = atts.getValue("id");
		double x = Double.parseDouble(atts.getValue("x"));
		double y = Double.parseDouble(atts.getValue("y"));
		// System.err.println("Coord : ("+x+";"+y+")");
		if (inDepot) {
			getRemoteTerminal().addCoordsToDepot(this.depot, id,
					new Coordinates(x, y));
		} else if (inPave)
			pave.addCoords(id, new Coordinates(x, y));
	}

	private void crossroad(Attributes atts) {
		Terminal terminal = getRemoteTerminal();
		String id = atts.getValue("id");
		double x = Double.parseDouble(atts.getValue("x"));
		double y = Double.parseDouble(atts.getValue("y"));
		terminal.addCrossroad(new Crossroad(id, new Coordinates(x, y)));
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
			if (localName.equals("depot")) {

				inDepot = false;
				/*
				 * Terminal terminal = getRemoteTerminal();
				 * terminal.validateDepot(depot);
				 */
			} else if (localName.equals("road")) {
				inRoad = false;
				// Terminal terminal = getRemoteTerminal();
				// terminal.validateRoad(road.getId());
			} else if (localName.equals("pave")) {
				inPave = false;
				Terminal terminal = getRemoteTerminal();
				// System.out.println("Adding pave "+pave.getId()+" type "+pave.getType());
				terminal.addPave(pave);
			} else if (localName.equals("lane")
					|| localName.equals("exchangeLane")) {
				Terminal terminal = getRemoteTerminal();
				terminal.addBay(lane);
			} else if (localName.equals("mission")) {
				if (current_event_newMission_truckID.equals("")) {
					Mission m = new Mission(idMission, missionKind, twPickup,
							twDelivery, missionContainerId, missionLocation);
					// System.out.println("m.toString : "+m.toString());
					if (inEvent == 0) {
						getRemoteTerminal().addMission(m);
					} else
						current_mission = m;
				} else {
					TruckMission m = new TruckMission(idMission,
							current_event_newMission_truckID, missionKind,
							twPickup, twDelivery, missionContainerId,
							missionLocation);
					// System.out.println("m.toString : "+m.toString());
					if (inEvent == 0) {
						getRemoteTerminal().addMission(m);
					} else
						current_mission = m;
				}

				inMission = false;
				twDelivery = twPickup = null;
				this.idMission = "";
				this.missionContainerId = "";
				this.missionLocation = null;
			} else if (localName.equals("event")) {
				if (current_event_type.get(inEvent - 1).equals("shipIn")) {
					scheduler.registerDynamicEvent(currentShipIn);
					currentShipIn = null;
				} else if (current_event_type.get(inEvent - 1)
						.equals("shipOut")) {
					scheduler.registerDynamicEvent(currentShipOut);
					currentShipOut = null;
				} else if (current_event_type.get(inEvent - 1).equals(
						"newMission")) {
					NewMission nm = null;
					if (current_event_newMission_truckID.equals(""))
						nm = new NewMission(
								current_event_time.get(inEvent - 1),
								current_mission);
					else
						nm = new NewMission(
								current_event_time.get(inEvent - 1),
								(TruckMission) current_mission);
					scheduler.registerDynamicEvent(nm);
					current_mission = null;
				} else if (current_event_type.get(inEvent - 1).equals(
						"newContainer")) {
					NewContainer nc = new NewContainer(
							current_event_time.get(inEvent - 1),
							current_container_id, current_container_teu,
							current_container_location);
					scheduler.registerDynamicEvent(nc);

				} else if (current_event_type.get(inEvent - 1).equals(
						"vehicleIn")) {

					VehicleIn vi = new VehicleIn(
							current_event_time.get(inEvent - 1),
							current_event_vehicle_ID, vehicleInOutLaneIds,
							vehicleInOutSlotIds, vehicleInOutContainers);

					scheduler.registerDynamicEvent(vi);
					// inVehicleIn = false;
					current_event_vehicle_ID = "";
					vehicleInOutContainers = null;
					vehicleInOutSlotIds = null;
					vehicleInOutLaneIds = null;
				} else if (current_event_type.get(inEvent - 1).equals(
						"vehicleOut")) {
					VehicleOut vo = new VehicleOut(
							current_event_time.get(inEvent - 1),
							current_event_vehicle_ID, vehicleInOutLaneIds,
							vehicleInOutSlotIds, vehicleInOutContainers);
					scheduler.registerDynamicEvent(vo);
					// inVehicleIn = false;
					current_event_vehicle_ID = "";
					vehicleInOutContainers = null;
					vehicleInOutSlotIds = null;
				}

				current_event_time.remove(inEvent - 1);
				current_event_type.remove(inEvent - 1);
				inEvent--;
			} else if (localName.equals("container")) {
				if (inEvent == 0) {
					try {
						getRemoteTerminal().addContainer(current_container_id,
								current_container_teu,
								current_container_location);
					} catch (Exception e) {
						e.printStackTrace();
					}

				} else if (current_event_type.get(inEvent - 1).equals(
						"vehicleIn")
						|| current_event_type.get(inEvent - 1).equals(
								"vehicleOut")) {
					if (vehicleInOutContainers == null)
						vehicleInOutContainers = new ArrayList<Container>();
					try {
						vehicleInOutContainers.add(new Container(
								current_container_id,
								current_container_location,
								current_container_teu));
					} catch (ContainerDimensionException e) {
						e.printStackTrace();
					}
				} else if (current_event_type.get(inEvent - 1)
						.equals("shipOut")) {
					currentShipOut.addContainerToLoad(current_container_id,
							current_container_teu);
				}
				inContainer = false;
			} else if (localName.equals("straddleCarrier")) {
				currentStraddleCarrierId = null;
				currentStraddleCarrier = null;
			} else if (localName.equals("terminal")) {
				terminal.drawElements();
				currentStraddleCarrier = null;
				lane = null;
				/*
				 * int nSlots = 0; for(String s : terminal.slots.keySet()){
				 * nSlots+=terminal.slots.get(s).size(); }
				 * System.out.println("Slots : "+nSlots);
				 * System.out.println("Crossroads : "
				 * +terminal.crossroads.size());
				 * System.out.println("Roads : "+terminal.roads.size());
				 * System.out.println("Lanes : "+terminal.lanes.size());
				 */
			} else if (localName.equals("vehicles")) {
				terminal.drawVehicles();
			} else if (localName.equals("shipContainerIn")) {
				currentShipIn.addContainerToUnload(current_ship_container_id,
						current_container_teu);
				NewShipContainer nsc = new NewShipContainer(
						current_ship_container_time, current_ship_container_id,
						current_container_teu, current_container_location,
						currentShipIn.getQuayID(),
						currentShipIn.getShipBerthFromRate(),
						currentShipIn.getShipBerthToRate());
				scheduler.registerDynamicEvent(nsc);
				current_ship_container_id = "";
				current_ship_container_time = null;
				current_container_location = null;
				current_container_teu = -1;
				inShipContainer = false;
			}

		}

	}

	@Override
	public void endPrefixMapping(String prefix) throws SAXException {

	}

	private void shipContainerIn(Attributes atts) {
		Time t = new Time(atts.getValue("time"));
		String id = atts.getValue("id");
		current_container_teu = Double.parseDouble(atts.getValue("teu"));
		current_ship_container_id = id;
		current_ship_container_time = t;

		inShipContainer = true;
	}

	private void event(Attributes atts) {
		inEvent++;
		Time t = new Time(atts.getValue("time"));
		current_event_time.add(t);
		current_event_type.add(atts.getValue("type"));
		if (current_event_type.get(inEvent - 1) == null) {
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
		/*
		 * else
		 * if(current_event_type.get(inEvent-1).equals("containerShipOut")){
		 * String contID = atts.getValue("containerId"); ContainerShipOut
		 * contOut = new ContainerShipOut(t, contID);
		 * scheduler.registerDynamicEvent(contOut); }
		 */
	}

	private void exchangeLane(Attributes atts) throws NotInPaveException,
			RemoteException, LaneTooSmallException {
		String id = atts.getValue("id");
		String cOriginId = atts.getValue("origin");
		String cDestinationId = atts.getValue("destination");
		boolean directed = false;
		if (atts.getIndex("directed") >= 0) {
			directed = Boolean.parseBoolean(atts.getValue("directed"));
		}
		String pave = "";
		if (inPave)
			pave = this.pave.getId();
		else
			throw new NotInPaveException();

		String in = atts.getValue("in");

		StringTokenizer st = new StringTokenizer(in, ",");
		double inX = Double.parseDouble(st.nextToken());
		double inY = Double.parseDouble(st.nextToken());

		String out = atts.getValue("out");
		st = new StringTokenizer(out, ",");
		double outX = Double.parseDouble(st.nextToken());
		double outY = Double.parseDouble(st.nextToken());

		Terminal terminal = getRemoteTerminal();
		BayCrossroad cOrigin = (BayCrossroad) terminal.getCrossroad(cOriginId);
		BayCrossroad cDestination = (BayCrossroad) terminal
				.getCrossroad(cDestinationId);
		lane = new ExchangeBay(id, cOrigin, cDestination, directed, pave);
		//FIXME new Coordinates(inX, inY), new Coordinates(outX, outY)
		this.pave.addLane(lane);
		String slots = atts.getValue("slots");
		StringTokenizer stSlots = new StringTokenizer(slots, "-");
		SlotBuilderHelper[] tSlots = new SlotBuilderHelper[stSlots
				.countTokens()];
		int i = 0;
		while (stSlots.hasMoreTokens()) {
			String slots1 = stSlots.nextToken();
			StringTokenizer stSlots1 = new StringTokenizer(slots1, "*");
			String type = stSlots1.nextToken();
			int quantity = Integer.parseInt(stSlots1.nextToken());
			tSlots[i++] = new SlotBuilderHelper(type, quantity);
		}
		// if(lane.getId().equals("C-41/41"))
		// System.out.println("Adding Exchange lane C-41/41 in pave "+pave+" type "+this.pave.getType());
		//FIXMElane.addSlots(tSlots, this.pave.getType());
		//getRemoteTerminal().addSlots(lane.getId(), //FIXE
	}

	public Terminal getRemoteTerminal() {
		return terminal;
	}

	@Override
	public void ignorableWhitespace(char[] ch, int start, int length)
			throws SAXException {

	}

	private void include(String file) throws SAXException {
		XMLReader saxReader = XMLReaderFactory
				.createXMLReader("com.sun.org.apache.xerces.internal.parsers.SAXParser");
		saxReader.setContentHandler(this);
		try {
			saxReader.parse(new InputSource(this.getClass()
					.getResourceAsStream("/" + file)));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void lane(Attributes atts) throws LaneTooSmallException,
			NotInPaveException {
		String id = atts.getValue("id");
		String cOriginId = atts.getValue("origin");
		String cDestinationId = atts.getValue("destination");
		boolean directed = false;
		if (atts.getIndex("directed") >= 0) {
			directed = Boolean.parseBoolean(atts.getValue("directed"));
		}
		String pave = "";
		if (inPave)
			pave = this.pave.getId();
		else
			throw new NotInPaveException();

		Terminal terminal = getRemoteTerminal();
		BayCrossroad cOrigin = (BayCrossroad) terminal.getCrossroad(cOriginId);
		BayCrossroad cDestination = (BayCrossroad) terminal
				.getCrossroad(cDestinationId);

		String in = atts.getValue("in");

		StringTokenizer st = new StringTokenizer(in, ",");
		double inX = Double.parseDouble(st.nextToken());
		double inY = Double.parseDouble(st.nextToken());

		String out = atts.getValue("out");
		st = new StringTokenizer(out, ",");
		double outX = Double.parseDouble(st.nextToken());
		double outY = Double.parseDouble(st.nextToken());
		if (atts.getIndex("laneGroup") >= 0) {
			/*lane = new Bay(id, cOrigin, cDestination, directed, pave,
					new Coordinates(inX, inY), new Coordinates(outX, outY),
					atts.getValue("laneGroup"));
			*/
			lane = new Bay(id, cOrigin, cDestination, directed, pave, atts.getValue("laneGroup"));
		} else {
			//lane = new Bay(id, cOrigin, cDestination, directed, pave, new Coordinates(inX, inY), new Coordinates(outX, outY));
			lane = new Bay(id, cOrigin, cDestination, directed, pave);
		}
		this.pave.addLane(lane);
		String slots = atts.getValue("slots");
		StringTokenizer stSlots = new StringTokenizer(slots, "-");
		SlotBuilderHelper[] tSlots = new SlotBuilderHelper[stSlots
				.countTokens()];
		int i = 0;
		while (stSlots.hasMoreTokens()) {
			String slots1 = stSlots.nextToken();
			StringTokenizer stSlots1 = new StringTokenizer(slots1, "*");
			String type = stSlots1.nextToken();
			int quantity = Integer.parseInt(stSlots1.nextToken());
			tSlots[i++] = new SlotBuilderHelper(type, quantity);
		}
		//getRemoteTerminal().addSlots(lane.getId(), lane.addSlots(tSlots, this.pave.getType()));
	}

	private void laneCrossroad(Attributes atts) {
		String id = atts.getValue("id");
		Terminal terminal = getRemoteTerminal();
		String mainRoad = "";

		if (inRoad)
			mainRoad = road.getId();
		double x = Double.parseDouble(atts.getValue("x"));
		double y = Double.parseDouble(atts.getValue("y"));
		terminal.addBayCrossroad(new BayCrossroad(id, new Coordinates(x, y),
				mainRoad));
	}

	private void laserHead(Attributes atts) {
		String id = atts.getValue("id");
		double x = Double.parseDouble(atts.getValue("x"));
		double y = Double.parseDouble(atts.getValue("y"));
		double z = Double.parseDouble(atts.getValue("z"));
		double rangeX = Double.parseDouble(atts.getValue("rangeX"));
		double rangeY = Double.parseDouble(atts.getValue("rangeY"));
		double rangeZ = Double.parseDouble(atts.getValue("rangeZ"));

		Coordinates location = new Coordinates(x, y, z);
		Range range = new Range(rangeX, rangeY, rangeZ);
		LaserSystem ld = LaserSystem.getInstance();

		ld.addLaserHead(id, location, range);
		// System.out.println("Laser Head added : "+id);
	}

	@Override
	public void processingInstruction(String target, String data)
			throws SAXException {

	}

	private void road(Attributes atts) {
		String id = atts.getValue("id");
		String origin = atts.getValue("origin");
		String destination = atts.getValue("destination");
		boolean directed = false;
		if (atts.getIndex("directed") >= 0) {
			directed = Boolean.parseBoolean(atts.getValue("directed"));
		}
		inRoad = true;
		Terminal terminal = getRemoteTerminal();
		Crossroad cOrigin = terminal.getCrossroad(origin);
		Crossroad cDestination = terminal.getCrossroad(destination);
		road = new Road(id, cOrigin, cDestination, directed);
		terminal.addRoad(road);
	}

	private void roadPoint(Attributes atts) {
		String id = atts.getValue("id");

		double x = 0f;
		double y = 0f;

		RoadPoint rp;
		if (atts.getIndex("x") >= 0) {
			x = Double.parseDouble(atts.getValue("x"));
			y = Double.parseDouble(atts.getValue("y"));

			rp = new RoadPoint(id, new Coordinates(x, y));
			roadPoints.put(id, rp);
		} else {
			rp = roadPoints.get(id);
		}
		/*String mainRoad = "";
		if (inRoad) {
			mainRoad = road.getId();
		}*/
		getRemoteTerminal().addRoadPoint(rp);
	}

	private void routing(Attributes atts)
			throws RegisteredObjectNotFoundException {
		String type = atts.getValue("type");
		String host = atts.getValue("host");
		if (currentHost.equals(host)) {
			// StraddleCarrier rsc =
			// (StraddleCarrier)NetworkConfiguration.getRMIInstance().get(currentStraddleCarrierId);
			StraddleCarrier rsc = currentStraddleCarrier;
			if (type.equals(APSP.rmiBindingName)) {
				rsc.setRoutingAlgorithm(new APSP(rsc));
			} else if (type.equals(DijkstraHandler.rmiBindingName)) {
				rsc.setRoutingAlgorithm(new DijkstraHandler(rsc));
			} else if (type.equals(AStarHandler.rmiBindingName)) {
				AStarHandler ash = new AStarHandler(rsc);
				if (atts.getIndex("heuristic") >= 0) {
					String h = atts.getValue("heuristic");
					if (h.equals(DistanceAndSpeedHeuristic.NAME)) {
						ash.setHeuristic(new DistanceAndSpeedHeuristic(rsc
								.getModel().getSpeedCharacteristics()));
					} else {
						if (h.equals(DijkstraHeuristic.NAME)) {
							ash.setHeuristic(new DijkstraHeuristic(rsc
									.getModel().getSpeedCharacteristics()));
						} else if (h.equals(YDistanceHeuristic.NAME)) {
							ash.setHeuristic(new YDistanceHeuristic(rsc
									.getModel().getSpeedCharacteristics()));
						}
					}
				}
				rsc.setRoutingAlgorithm(ash);
			} else if (type.equals(RDijkstraHandler.rmiBindingName)) {
				rsc.setRoutingAlgorithm(new RDijkstraHandler(rsc));// FIXME
																	// COMMON
																	// INTERFACE
																	// FOR USING
																	// EITHER
																	// RROUTING
																	// OR
																	// ROUTING
			}
		}
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
				} else if (localName.equals("include")) {
					String file = atts.getValue("file");
					include(file);
				} else if (localName.equals("crossroad")) {
					crossroad(atts);
				} else if (localName.equals("laneCrossroad")) {
					laneCrossroad(atts);
				} else if (localName.equals("road")) {
					road(atts);
				} else if (localName.equals("lane")) {
					lane(atts);
				} else if (localName.equals("exchangeLane")) {
					exchangeLane(atts);
				} else if (localName.equals("roadpoint")) {
					roadPoint(atts);
				} else if (localName.equals("pave")) {
					String id = atts.getValue("id");
					String type = atts.getValue("type");
					if (type.equals(BlockType.SHIP.toString())) {
						SeaOrientation orientation = SeaOrientation
								.getOrientation(atts.getValue("seaOrientation"));
						pave = new Quay(id, orientation,
								atts.getValue("borderRoadID"));
					} else
						pave = new Block(id, BlockType.getType(type));
					inPave = true;

				} else if (localName.equals("depot")) {
					inDepot = true;
					String id = atts.getValue("id");
					depot = id;
					Depot depot = new Depot(id);
					getRemoteTerminal().addDepot(depot);
				} else if (localName.equals("coordinate")) {
					coordinate(atts);
				} else if (localName.equals("wall")) {
					wall(atts);
				} else if (localName.equals("type")) {
					straddleCarrierModel(atts);
				} else if (localName.equals("straddleCarrier")) {
					String id = atts.getValue("id");
					String host = atts.getValue("host");
					if (host.equals(currentHost)) {
						straddleCarrier(atts);
					} else {
						while (currentStraddleCarrier == null) {
							currentStraddleCarrier = getRemoteTerminal()
									.getStraddleCarrier(id);
						}
					}
					currentStraddleCarrierId = id;
				} else if (localName.equals("routing")) {
					if (currentStraddleCarrierId != null
							&& currentStraddleCarrier != null) {
						routing(atts);
					}
				} else if (localName.equals("laserhead")) {
					laserHead(atts);
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

				else if (localName.equals("container")) {
					/**
					 * <container id="cont1" teu="1"> <containerLocation
					 * lane="A-1/40" slot="A-1/40-2" level="lowest"
					 * align="center"/> </container>
					 */

					inContainer = true;
					current_container_id = atts.getValue("id");
					if (atts.getIndex("teu") >= 0)
						current_container_teu = Double.parseDouble(atts
								.getValue("teu"));
				} else if (localName.equals("straddleCarrierSlot")) {
					straddleCarrierSlot(atts);
				} else if (localName.equals("shipContainerIn")) {
					shipContainerIn(atts);
				}
			} catch (RemoteException e) {
				e.printStackTrace();
			} catch (NotInPaveException e) {
				e.printStackTrace();
			} catch (LaneTooSmallException e) {
				e.printStackTrace();
			} catch (NotInDepotException e) {
				e.printStackTrace();
			} catch (NotInContainerException e) {
				e.printStackTrace();
			} catch (RegisteredObjectNotFoundException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void startPrefixMapping(String prefix, String uri)
			throws SAXException {

	}

	private void straddleCarrier(Attributes atts) {
		Terminal terminal = getRemoteTerminal();
		String type = atts.getValue("type");
		String slot = atts.getValue("slot");
		String color = atts.getValue("color");
		// System.out.println("color = "+color);
		String id = atts.getValue("id");
		boolean autoHandling = false;
		if (atts.getIndex("handling") >= 0) {
			String s = atts.getValue("handling");
			if (s.equals("man"))
				autoHandling = false;
		}
		if (atts.getIndex("locationRoad") >= 0) {
			String location = atts.getValue("locationRoad");
			double pct = 0.0;
			if (atts.getIndex("locationPourcent") >= 0) {
				pct = Double.parseDouble(atts.getValue("locationPourcent"));
			}
			boolean direction = true;
			if (atts.getIndex("direction") >= 0) {
				direction = Boolean.parseBoolean(atts.getValue("direction"));
			}
			Road r = terminal.getRoad(location);
			Location l = new Location(r, pct, direction);
			currentStraddleCarrier = new StraddleCarrier(id, Terminal
					.getInstance().getStraddleCarrierSlot(slot), type, color,
					l, autoHandling);
		} else
			currentStraddleCarrier = new StraddleCarrier(id,
					terminal.getStraddleCarrierSlot(slot), type, color,
					autoHandling);
	}

	private void straddleCarrierModel(Attributes atts) {
		String id = atts.getValue("id");
		double width = Double.parseDouble(atts.getValue("width"));
		double height = Double.parseDouble(atts.getValue("height"));
		double length = Double.parseDouble(atts.getValue("length"));
		double innerWidth = Double.parseDouble(atts.getValue("innerWidth"));
		double innerLength = Double.parseDouble(atts.getValue("innerLength"));
		double backOverLength = Double.parseDouble(atts
				.getValue("backOverLength"));
		double frontOverLength = Double.parseDouble(atts
				.getValue("frontOverLength"));
		double cabWidth = Double.parseDouble(atts.getValue("cabWidth"));
		String compatibility = atts.getValue("compatibility");
		double speed = Double.parseDouble(atts.getValue("emptySpeed"));
		double loadedSpeed = Double.parseDouble(atts.getValue("loadedSpeed"));
		double baySpeed = Double.parseDouble(atts.getValue("baySpeed"));
		RandomSpeed containerHandlingTimeFromTruck = new RandomSpeed(
				Double.parseDouble(atts
						.getValue("containerHandlingFromTruck_MIN")),
				Double.parseDouble(atts
						.getValue("containerHandlingFromTruck_MAX")), "seconds");
		RandomSpeed containerHandlingTimeFromGround = new RandomSpeed(
				Double.parseDouble(atts
						.getValue("containerHandlingFromGround_MIN")),
				Double.parseDouble(atts
						.getValue("containerHandlingFromGround_MAX")),
				"seconds");
		RandomSpeed timeToEnterABay = new RandomSpeed(Double.parseDouble(atts
				.getValue("enterExitBayTime_MIN")), Double.parseDouble(atts
				.getValue("enterExitBayTime_MAX")), "seconds");
		double turnBackTime = Double.parseDouble(atts.getValue("turnBackTime"));

		StraddleCarrierModel model = new StraddleCarrierModel(id, width,
				height, length, innerWidth, innerLength, backOverLength,
				frontOverLength, cabWidth, compatibility, speed, loadedSpeed,
				baySpeed, containerHandlingTimeFromTruck,
				containerHandlingTimeFromGround, timeToEnterABay, turnBackTime);
		getRemoteTerminal().addStraddleCarrierModel(model);
	}

	private void straddleCarrierSlot(Attributes atts)
			throws NotInDepotException {
		String id = atts.getValue("id");
		String depotId;

		double x = Double.parseDouble(atts.getValue("x"));
		double y = Double.parseDouble(atts.getValue("y"));
		String cOriginId = atts.getValue("origin");
		String cDestinationId = atts.getValue("destination");
		Terminal terminal = getRemoteTerminal();
		BayCrossroad cOrigin = (BayCrossroad) terminal.getCrossroad(cOriginId);
		BayCrossroad cDestination = (BayCrossroad) terminal
				.getCrossroad(cDestinationId);
		getRemoteTerminal().addRoad(new Road(id, cOrigin, cDestination, false));
		if (inDepot) {
			depotId = this.depot;
		} else
			throw new NotInDepotException();
		StraddleCarrierSlot slot = new StraddleCarrierSlot(id, new Coordinates(
				x, y), cOrigin, cDestination, depotId);

		terminal.addStraddleCarrierSlot(slot);
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

	public void destroy() {
		scheduler = null;
		terminal = null;
	}

	private void wall(Attributes atts) {
		String from = atts.getValue("from");
		String to = atts.getValue("to");
		if (inDepot) {

			getRemoteTerminal().addWallToDepot(depot, from, to);

		} else if (inPave)
			pave.addWall(from, to);
	}
}
