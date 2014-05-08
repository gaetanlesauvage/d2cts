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
package org.system;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import org.apache.log4j.Logger;
import org.display.GraphicDisplayPanel;
import org.display.TextDisplay;
import org.display.system.GraphicTerminalListenerImpl;
import org.exceptions.ContainerDimensionException;
import org.exceptions.ContainerNotFoundException;
import org.exceptions.EmptyLevelException;
import org.exceptions.EmptySlotException;
import org.exceptions.NoPathFoundException;
import org.exceptions.NotAccessibleContainerException;
import org.exceptions.container_stocking.delivery.CollisionException;
import org.exceptions.container_stocking.delivery.FallingContainerException;
import org.exceptions.container_stocking.delivery.LevelException;
import org.exceptions.container_stocking.delivery.MissionContainerDeliveryException;
import org.exceptions.container_stocking.delivery.NotEnoughSpaceException;
import org.exceptions.container_stocking.delivery.SlotContainerIncompatibilityException;
import org.exceptions.container_stocking.delivery.UnstackableContainerException;
import org.missions.Load;
import org.missions.Mission;
import org.missions.MissionKinds;
import org.missions.MissionPhase;
import org.missions.MissionState;
import org.missions.TruckMission;
import org.missions.Workload;
import org.positioning.Coordinates;
import org.positioning.LaserSystem;
import org.routing.Routing;
import org.routing.path.Path;
import org.scheduling.MissionScheduler;
import org.system.container_stocking.Bay;
import org.system.container_stocking.BayCrossroad;
import org.system.container_stocking.Block;
import org.system.container_stocking.BlockType;
import org.system.container_stocking.Container;
import org.system.container_stocking.ContainerLocation;
import org.system.container_stocking.Level;
import org.system.container_stocking.Quay;
import org.system.container_stocking.Slot;
import org.time.Time;
import org.time.TimeScheduler;
import org.time.TimeWindow;
import org.time.event.ChangeContainerLocation;
import org.time.event.NewContainer;
import org.time.event.StraddleCarrierRepaired;
import org.util.Location;
import org.util.RecordableObject;
import org.vehicles.DeliveryOrder;
import org.vehicles.Ship;
import org.vehicles.StraddleCarrier;
import org.vehicles.Truck;
import org.vehicles.models.StraddleCarrierModel;

/**
 * Implementation of the Terminal through the RMI Interface
 * 
 * @author Ga&euml;tan Lesauvage
 * @since 2009
 */
public class Terminal implements RecordableObject {

	private static final Logger log = Logger.getLogger(Terminal.class);

	private static final String rmiBindingName = null;

	public static final long DEFAULT_SEED = 21;

	public static String IMAGE_FOLDER = "etc/images/";

	private static Terminal instance;

	/**
	 * Graphic listener used to communicate with the 2D view if any.
	 */
	public GraphicTerminalListenerImpl listener = new GraphicTerminalListenerImpl();

	public GraphicDisplayPanel out;

	/**
	 * Random generator of the simulator
	 */
	private Random random;

	public static Terminal getInstance() {
		if (instance == null) {
			instance = new Terminal();
		}
		return instance;
	}

	/**
	 * Reservations table of each road/lane of the terminal. Indexed by the ID
	 * of the roads/lanes
	 */
	private SortedMap<String, Reservations> reservations;
	/**
	 * Depots table indexed by ID
	 */
	private SortedMap<String, Depot> depots;
	/**
	 * Straddle carriers parking slots table. This structure links a straddle
	 * carrier ID to a parking slot
	 */
	private SortedMap<String, StraddleCarrierSlot> straddleCarriersSlots;
	/**
	 * Pave map. This map is indexed by the type of the paves. Each pave have a
	 * type (STOCK, ROAD, TRAIN, SHIP). Each type contains a map of IDs and
	 * instances of pave of this type.
	 */
	private SortedMap<BlockType, SortedMap<String, Block>> paves;
	/**
	 * Lanes map by ID
	 */
	private SortedMap<String, Bay> bays;
	/**
	 * Slots map by Lane ID. Stocks the slots list of each lane.
	 */
	private SortedMap<String, List<Slot>> slots;

	private SortedMap<String, Slot> slotsByIds;

	/**
	 * Straddle carriers models map by model ID
	 */
	private SortedMap<String, StraddleCarrierModel> straddleCarrierModels;
	/**
	 * Straddle carriers map by ID. Retrieves the RMI interface of a distributed
	 * straddle carrier.
	 */
	private SortedMap<String, StraddleCarrier> straddleCarriers;
	/**
	 * Crossroads map by ID
	 */
	private SortedMap<String, Crossroad> crossroads;
	/**
	 * Road points map by ID
	 */
	private SortedMap<String, RoadPoint> roadPoints;
	/**
	 * Roads map by ID
	 */
	private SortedMap<String, Road> roads;

	/**
	 * Missions map by ID
	 */
	private SortedMap<String, Mission> missions;
	/**
	 * Containers map by ID
	 */
	private SortedMap<String, Container> containers;

	private List<Ship> boats;
	/**
	 * Trucks by ID
	 */
	private SortedMap<String, Truck> trucks;

	private SortedMap<String, String> allocations;

	private boolean showLaserHead;

	private Integer simID;

	public void setSimulationID(Integer simID) {
		this.simID = simID;
	}

	/**
	 * Default Terminal with no remote display @ * @throws SingletonException
	 */
	protected Terminal() {
		log.info("Creating Terminal");

		allocations = new TreeMap<>();
		reservations = new TreeMap<>();
		depots = new TreeMap<>();
		straddleCarriersSlots = new TreeMap<>();
		paves = new TreeMap<>();
		bays = new TreeMap<>();

		slots = new TreeMap<>();
		slotsByIds = new TreeMap<>();

		straddleCarrierModels = new TreeMap<>();
		straddleCarriers = new TreeMap<>();
		crossroads = new TreeMap<>();
		roadPoints = new TreeMap<>();
		roads = new TreeMap<>();
		missions = new TreeMap<>();
		containers = new TreeMap<>();
		boats = new ArrayList<>(6);
		trucks = new TreeMap<>();

	}

	public GraphicTerminalListenerImpl getListener() {
		return listener;
	}

	public void addConnexCrossroad(String nodeId, String connexNodeId, String by) {
		try {
			RoadPoint rp = getNode(nodeId);

			if (rp == null) {
				log.error("NullRoadPointException : ID = " + nodeId + " CONNEX_ID = " + connexNodeId);
			}
			rp.addConnexCrossroad(connexNodeId, by);
			log.trace("Connex crossroads : " + nodeId + " with " + connexNodeId + " by " + by);
		} catch (NullPointerException e) {
			e.printStackTrace();
			log.error(e.getMessage(), e);
		}
	}

	private void addContainer(Container c) {
		containers.put(c.getId(), c);
		listener.containerAdded(c);

		if (c.getContainerLocation() != null) {
			Slot s = getSlot(c.getContainerLocation().getSlotId());
			if (out != null && s != null) {
				out.slotContentChanged(s);
			}
		}
	}

	public void addContainer(final String id, final double teu, final ContainerLocation location) throws CollisionException, NotEnoughSpaceException {
		Container c;
		try {
			c = new Container(id, teu);

			if (location != null) {
				Slot slot = getSlot(location.getSlotId());
				Coordinates coords = slot.stockContainer(c, location.getLevel(), location.getAlign());

				// Modify to add Z coord !
				Location l = new Location(slot.getLocation().getRoad(), coords, slot.getLocation().getDirection());

				c.move(l);

				// TO be removed ?
				c.setContainerLocation(location);
			}
			Terminal.this.addContainer(c);

			if (out != null)
				out.addContainer(c);

			for (Mission m : missions.values()) {
				if (m.getContainerId().equals(id)) {
					MissionScheduler.getInstance().updateMission(getTime(), m);
					break;
				}
			}

		} catch (ContainerDimensionException e) {
			e.printStackTrace();
		} catch (SlotContainerIncompatibilityException e) {
			e.printStackTrace();
		} catch (FallingContainerException e) {
			e.printStackTrace();
		} catch (UnstackableContainerException e) {
			e.printStackTrace();
		} catch (LevelException e) {
			e.printStackTrace();
		}

	}

	public void addCoordsToDepot(String depot, String id, Coordinates coordinates) {
		depots.get(depot).addCoords(id, coordinates);
	}

	public void addCrossroad(Crossroad crossroad) {
		crossroads.put(crossroad.getId(), crossroad);
	}

	public void addDepot(final Depot d) {
		depots.put(d.getId(), d);
	}

	public void addBay(Bay lane) {
		bays.put(lane.getId(), lane);
	}

	public void addBayCrossroad(BayCrossroad crossroad) {
		crossroads.put(crossroad.getId(), crossroad);
	}

	public void addlaserHead(String id) {
		if (listener != null && showLaserHead){
			listener.laserHeadAdded(id);
		}
	}

	public void addMission(Mission m) {
		missions.put(m.getId(), m);
		if (out != null){
			out.addMission(m);
		}
		MissionScheduler.getInstance().addMission(getTime(), m);

		log.info("Incoming Mission: " + m);
	}

	public synchronized void addPave(final Block p) {
		if (!paves.containsKey(p.getType())) {
			paves.put(p.getType(), new TreeMap<String, Block>());
		}
		SortedMap<String, Block> tmp = paves.get(p.getType());
		tmp.put(p.getId(), p);
		paves.put(p.getType(), tmp);
	}

	public void addRoad(Road road) {
		roads.put(road.getId(), road);
	}

	public void addRoadPoint(final RoadPoint rp) {
		roadPoints.put(rp.getId(), rp);
	}

	public void addSlots(String lane, List<Slot> list) {
		slots.put(lane, list);
		for (Slot s : list) {
			slotsByIds.put(s.getId(), s);
		}
		if (out != null) {
			out.addSlots(list);

		}
	}

	public void addStraddleCarrier(StraddleCarrier rsc) {
		final String id = rsc.getId();

		straddleCarriers.put(id, rsc);

		LaserSystem.getInstance().addDetectableStraddleCarrier(rsc);

		MissionScheduler.getInstance().addResource(getTime(), rsc);

		if (out != null)
			out.addStraddleCarrier(rsc);

		graphChangedFor(rsc);
	}

	public void addStraddleCarrierModel(StraddleCarrierModel straddleCarrierModel) {
		straddleCarrierModels.put(straddleCarrierModel.getId(), straddleCarrierModel);
	}

	public void addStraddleCarrierSlot(StraddleCarrierSlot slot) {
		String depotId = slot.getDepotId();
		Depot d = depots.get(depotId);
		d.addStraddleCarrierSlot(slot);
		straddleCarriersSlots.put(slot.getId(), slot);
	}

	public void addWallToDepot(String depot, String from, String to) {
		depots.get(depot).addWall(from, to);

	}

	public boolean shipIn(int capacity, String paveID, double berthFromRate, double berthToRate, Set<String> containersToUnload) {
		Quay p = (Quay) paves.get(BlockType.SHIP).get(paveID);
		for (Ship boat : boats) {
			if (boat.getQuay().getId().equals(paveID)) {
				if (boat.getRateFrom() < berthFromRate) {
					if (boat.getRateTo() > berthFromRate) {
						return false;
					}
				} else if (berthToRate > boat.getRateFrom())
					return false;
			}
		}
		// Get the slots concerned by the boat
		ArrayList<String> concernedSlots = new ArrayList<String>();
		for (int i = 0; i < p.getLanes().size(); i++) {
			Bay l = p.getLanes().get(i);
			List<Slot> slots = this.slots.get(l.getId());
			for (int j = 0; j < slots.size(); j++) {
				Slot slot = slots.get(j);
				double r = slot.getRateOnLane();
				if (r >= berthFromRate && r <= berthToRate)
					concernedSlots.add(slot.getId());
			}
		}

		Ship b = new Ship(capacity, p, berthFromRate, berthToRate, concernedSlots, containersToUnload);
		boats.add(b);
		if (listener != null) {
			listener.addShip(b);
		}
		return true;
	}

	public void containerMoved(String id, Location newLocation) {
		Container c = containers.get(id);
		c.move(newLocation);
		listener.containerMoved(id);
		if (out != null)
			out.containerLocationChanged(id, c.getContainerLocation());
	}

	public boolean deliverContainer(String straddleCarrier, Mission m, Time handlingTime) {
		String slotID = m.getDestination().getSlotId();

		Slot slot = getSlot(slotID);
		if (!slot.isReady()) {
			log.warn(getTime()+":> Slot " + slotID + " is not ready !");
			return false;
		}
		try {
			Coordinates coords = slot.stockContainer(m);

			Container container = containers.get(m.getContainer().getId());
			container.move(new Location(slot.getLocation().getRoad(), coords, true));

			Time deliveryEndTime = new Time(TimeScheduler.getInstance().getTime(), handlingTime);
			ChangeContainerLocation ccl = new ChangeContainerLocation(deliveryEndTime, container.getId(), m.getDestination());
			TimeScheduler.getInstance().registerDynamicEvent(ccl);

			listener.containerMoved(container.getId());
			if (out != null) {
				out.slotContentChanged(slot);
			}
		} catch (MissionContainerDeliveryException e) {
			// e.printStackTrace();
			StraddleCarrier rsc = getStraddleCarrier(straddleCarrier);
			DeliveryOrder dOrder = rsc.getListener().cantDeliverContainer(rsc.getId(), e, out);

			if (dOrder.getNewDeliveryLocation() != null) {
				final ContainerLocation nl = dOrder.getNewDeliveryLocation();
				Block p = getBlock(m.getDestination().getPaveId());
				if (p.getType() == BlockType.SHIP) {
					TimeScheduler.getInstance().changeShipContainerOutDestination(m.getContainer().getId(),
							dOrder.getNewDeliveryLocation().getSlotId());
				}

				rsc.messageReceived("<modMission id='" + m.getId() + "' phase='" + MissionPhase.PHASE_DELIVERY.getCode() + "'>" + nl.toXML()
						+ "</modMission>");
			} else {
				Location gotoLoc = dOrder.getGotoLocation();
				if (rsc.getLocation().getDirection())
					gotoLoc.setDirection(false);
				Time waitTime = dOrder.getWaitingTime();
				String msg = "<modMission id='" + m.getId() + "' phase='" + MissionPhase.PHASE_DELIVERY.getCode() + "'>" + "<goto road='"
						+ gotoLoc.getRoad().getId() + "' rate='" + gotoLoc.getPourcent() + "' direction='" + gotoLoc.getDirection()
						+ "'/> <wait time='" + waitTime + "'/></modMission>\n";
				log.trace("Sending Msg : " + msg);
				rsc.messageReceived(msg);

			}
			return false;
		}

		return true;
	}

	public void modMission(String modMissionId, MissionPhase modMissionPhase, ContainerLocation newDestination) {
		Mission m = missions.get(modMissionId);
		if (m.getMissionKind() == MissionKinds.OUT || m.getMissionKind() == MissionKinds.IN_AND_OUT) {
			Block pDest = getBlock(m.getDestination().getPaveId());
			if (pDest.getType() == BlockType.ROAD) {
				TruckMission tm = (TruckMission) m;
				TimeScheduler.getInstance().setTruckInLocation(tm.getTruckID(), newDestination);
			} else
				log.error("Mod mission is not allowed if the mission is not a road one!");
		}
	}

	public void drawElements() {
		if (listener == null)
			listener = new GraphicTerminalListenerImpl();
		listener.crossroadsAdded(getNodes().values());

		listener.roadsAdded(roads.values());

		ArrayList<Block> pavesList = new ArrayList<Block>(getPaveNames().length);
		for (BlockType pt : paves.keySet()) {
			for (String s : paves.get(pt).keySet()) {
				pavesList.add(paves.get(pt).get(s));
			}
		}
		listener.pavesAdded(pavesList);
		listener.lanesAdded(bays.values());

		listener.depotsAdded(depots.values());

		listener.containersAdded(containers.values());

	}

	public boolean isLaneDrivable(String laneID) {
		for (StraddleCarrier rsc : straddleCarriers.values()) {
			Location l = rsc.getLocation();
			if (l.getRoad().getId().equals(laneID) && l.getPourcent() > 0 && l.getPourcent() < 1.0)
				return false;
		}
		return true;
	}

	public void drawVehicles() {
		listener.straddleCarriersAdded(straddleCarriers.values());
	}

	public List<RoadPoint> getConnexNodes(String from) {
		List<String> connexIds = getNode(from).getConnexNodesIds();
		List<RoadPoint> l = new ArrayList<RoadPoint>(connexIds.size());
		for (String id : connexIds) {
			l.add(getNode(id));
		}
		return l;
	}

	public List<Road> getConnexRoads(String from) {
		List<String> names = crossroads.get(from).getConnexRoadIDs();
		List<Road> result = new ArrayList<Road>(names.size());

		for (String s : names) {
			Road r = getRoad(s);
			result.add(r);
		}

		return result;
	}

	public double getContainerTEU(String containerId) {
		Container c = getContainer(containerId);
		if (c == null) {
			return TimeScheduler.getInstance().getIncomingContainerTeu(containerId);
		}
		return c.getTEU();
	}

	public Container getContainer(String containerId) {
		return containers.get(containerId);
	}

	public List<String> getContainerNames() {
		ArrayList<String> l = new ArrayList<String>(containers.size());
		for (String s : containers.keySet()) {
			l.add(s);
		}
		return l;
	}

	public Crossroad getCrossroad(String id) {
		if (crossroads.containsKey(id))
			return crossroads.get(id);
		else
			return null;
	}

	public String[] getCrossroadsNames() {
		ArrayList<String> t = new ArrayList<String>(crossroads.size() + roadPoints.size());

		for (String id : crossroads.keySet()) {
			Crossroad c = crossroads.get(id);
			if (c instanceof BayCrossroad) {
				t.add(id);
			} else {
				t.add(id);
			}
		}
		for (String id : roadPoints.keySet()) {
			if (crossroads.containsKey(id))
				log.error("RoadPoint "+id + " added 2 times !");
			t.add(id);
		}

		t.trimToSize();

		String[] tab = new String[t.size()];
		tab = t.toArray(tab);
		return tab;
	}

	public Depot getDepot(String name) {
		return depots.get(name);
	}

	public List<String> getDepotNames() {
		List<String> l = new ArrayList<String>(depots.size());
		for (String id : depots.keySet()) {
			l.add(id);
		}
		return l;
	}

	public int getEdgeCount() {
		return roads.size() + bays.size();
	}

	public String getId() {
		return rmiBindingName;
	}

	public Bay getBay(String id) {
		return bays.get(id);
	}

	public String[] getLaneNames(String paveId) {
		if (paveId.equals(""))
			return new String[0];
		ArrayList<String> result = new ArrayList<String>(bays.size());
		for (String lId : bays.keySet()) {
			Bay l = bays.get(lId);
			if (l.getPaveId().equals(paveId))
				result.add(lId);
		}
		Collections.sort(result);

		return result.toArray(new String[result.size()]);
	}

	public ArrayList<String> getLaneNamesInArrayList(String paveId) {
		if (paveId.equals(""))
			return new ArrayList<String>();
		ArrayList<String> result = new ArrayList<String>(bays.size());
		for (String lId : bays.keySet()) {
			Bay l = bays.get(lId);
			if (l.getPaveId().equals(paveId))
				result.add(lId);
		}
		Collections.sort(result);

		return result;
	}

	public Mission getMission(String missionId) {
		return missions.get(missionId);
	}

	public List<String> getMissionsName() {
		ArrayList<String> names = new ArrayList<String>(missions.size());
		for (String n : missions.keySet()) {
			names.add(n);
		}
		return names;
	}

	public RoadPoint getNode(String id) {
		Crossroad c = getCrossroad(id);
		if (c == null)
			return getRoadPoint(id);
		else
			return c;
	}

	public HashMap<String, RoadPoint> getNodes() {
		HashMap<String, RoadPoint> map = new HashMap<String, RoadPoint>(crossroads.size() + roadPoints.size(), 1f);

		for (Crossroad c : crossroads.values()) {
			map.put(c.getId(), c);
		}
		for (RoadPoint rp : roadPoints.values()) {
			map.put(rp.getId(), rp);
		}

		return map;
	}

	public Block getBlock(String id) {
		for (BlockType pt : paves.keySet()) {
			if (paves.get(pt).containsKey(id))
				return paves.get(pt).get(id);
		}
		return depots.get(id);
	}

	public String[] getPaveNames() {

		List<String> l = new ArrayList<String>();
		for (BlockType pType : paves.keySet()) {
			for (String s2 : paves.get(pType).keySet()) {
				l.add(s2);
			}

		}
		Collections.sort(l);

		String[] names = l.toArray(new String[paves.size()]);
		return names;

	}

	public Random getRandom() {
		if (random == null) {
			Exception e = new Exception("Seed not supplied! Random generator will be created without any fixed seed.");
			e.printStackTrace();
			log.warn(e.getMessage(), e);
			random = new Random();
		}
		return random;
	}

	public GraphicDisplayPanel getTextDisplay() {
		return out;
	}

	public Road getRoad(Coordinates position) {
		// TODO : add an N-tree implementation for the road indexing
		for (Road r : roads.values()) {
			if (r.contains(position))
				return r;
		}
		return null;
	}

	public Road getRoad(String id) {
		if (roads == null)
			log.error("Roads collection has not been initialized!");
		Road r;
		if (roads.containsKey(id))
			r = roads.get(id);
		else
			r = bays.get(id);

		return r;
	}

	public Road getRoadBetween(String from, String to) {
		RoadPoint cFrom = getNode(from);
		return getRoad(cFrom.getConnexRoadIDTowardsNode(to));
	}

	public String getRoadIDBetween(String from, String to) {
		RoadPoint cFrom = getNode(from);
		return cFrom.getConnexRoadIDTowardsNode(to);
	}

	public RoadPoint getRoadPoint(String id) {
		return roadPoints.get(id);
	}

	public String[] getRoadsName() {
		String[] t = new String[roads.size()];
		int i = 0;
		for (String id : roads.keySet())
			t[i++] = id;
		return t;
	}

	public Ship getShip(String quayID, double berthFromRate, double berthToRate) {
		for (Ship boat : boats) {
			if (boat.getQuay().getId().equals(quayID)) {
				if (boat.getRateFrom() == berthFromRate) {
					if (boat.getRateTo() == berthToRate) {
						return boat;
					}
				}
			}
		}
		return null;
	}

	public Slot getSlot(String slotId) {
		return slotsByIds.get(slotId);
	}

	public String[] getSlotNames(String laneId) {
		if (laneId.equals("null") || laneId.equals(""))
			return new String[0];

		List<Slot> list = slots.get(laneId);
		List<String> names = new ArrayList<String>(list.size());
		for (Slot s : list) {
			names.add(s.getId());
		}
		Collections.sort(names);
		String[] result = new String[list.size()];
		int i = 0;
		for (Slot s : list)
			result[i++] = s.getId();
		return result;
	}

	public StraddleCarrier getStraddleCarrier(String id) {
		return straddleCarriers.get(id);
	}

	public StraddleCarrierModel getStraddleCarrierModel(String straddleCarrierModelId) {
		return straddleCarrierModels.get(straddleCarrierModelId);
	}

	public int getStraddleCarriersCount() {
		return straddleCarriers.size();
	}

	public StraddleCarrierSlot getStraddleCarrierSlot(String slotId) {
		return straddleCarriersSlots.get(slotId);
	}

	public List<String> getStraddleCarriersName() {
		ArrayList<String> names = new ArrayList<String>(straddleCarriers.size());
		for (String n : straddleCarriers.keySet()) {
			names.add(n);
		}
		return names;
	}

	public Time getTime() {
		return TimeScheduler.getInstance().getTime();
	}

	private void graphChangedFor(StraddleCarrier rsc) {
		Routing rr = rsc.getRouting();
		if (rr != null){
			rr.graphHasChanged();
		}
	}

	// TODO TO BE MODIFIED !!
	public void incomingMission(String straddleCarrier, final Location destination) {
		final StraddleCarrier rsc = straddleCarriers.get(straddleCarrier);
		rsc.messageReceived("<goto road=\"" + destination.getRoad().getId() + "\" rate=\"" + destination.getPourcent() + "\"/>");
	}

	public void incomingMission(final String straddleCarrier, final String missionId) {
		final StraddleCarrier rsc = straddleCarriers.get(straddleCarrier);
		rsc.messageReceived("<domission id=\"" + missionId + "\"/>");
		missionAffected(rsc.getWorkload().getCopyOfLoad(missionId), straddleCarrier);
		flushAllocations();
	}

	public void straddleCarrierActivityChanged(final String missionID, final String straddleCarrierID) {
		if (out != null) {
			out.setMissionToVehicle(straddleCarrierID, missionID);
		}
	}

	public synchronized void flushAllocations() {
		if (out != null) {
			for (String mID : allocations.keySet()) {
				out.setVehicleToMission(mID, allocations.get(mID));
			}
			allocations.clear();
		}

	}

	public synchronized void missionAffected(final Load load, final String straddleCarrierID) {
		if (out != null) {
			if (straddleCarrierID.equals("NA")) {
				for (StraddleCarrier rsc : straddleCarriers.values()) {
					Workload w = rsc.getWorkload();
					if (w.contains(load.getMission().getId())) {
						return;
					}
				}
			}
			if (load.getState() == MissionState.STATE_TODO) {
				allocations.put(load.getMission().getId(), straddleCarrierID);
			} else {
				if (load.getStraddleCarrierID().equals(straddleCarrierID)) {
					allocations.put(load.getMission().getId(), straddleCarrierID);
				} else{
					log.warn(getTime()+":> Trying to affect a mission which has already been started! (" + load.getMission().getId() + " to "
							+ straddleCarrierID + ")");
				}
			}
		}
	}

	public void missionChanged(Load l) {
		if (out != null) {
			out.missionChanged(l);
		}
	}

	public void missionStatusChanged(MissionPhase phase, MissionState state, final String scID, final Time overspentTime, final Time waitTime,
			Mission mission) {
		if (out != null) {
			String s = "";

			if (state == MissionState.STATE_CURRENT) {
				switch (phase) {
				case PHASE_PICKUP:
					s = "Pickup";
					MissionScheduler.getInstance().missionStarted(getTime(), mission, scID);
					break;
				case PHASE_LOAD:
					s = "Loading";
					MissionScheduler.getInstance().updateWaitTime(scID, waitTime);
					break;
				case PHASE_DELIVERY:
					s = "Delivery";
					MissionScheduler.getInstance().updateOverspentTime(scID, overspentTime);
					break;
				case PHASE_UNLOAD:
					s = "Unloading";
					MissionScheduler.getInstance().updateWaitTime(scID, waitTime);
				}
			} else if (state == MissionState.STATE_TODO) {
				s = "Waiting";
			} else if (state == MissionState.STATE_ACHIEVED) {
				s = "Achieved";
				MissionScheduler.getInstance().updateOverspentTime(scID, overspentTime);
				MissionScheduler.getInstance().incrementNumberOfCompletedMissions(scID);
			}

			out.missionStateChanged(mission.getId(), s);
		}
	}

	public List<Slot> getSlots(String id) {
		return slots.get(id);
	}

	public List<Slot> getExitsSlots(String id) {
		ArrayList<Slot> exitsSlots = new ArrayList<Slot>(2);
		Collection<Slot> l = getSlots(id);
		double min = Double.POSITIVE_INFINITY;
		double max = Double.NEGATIVE_INFINITY;
		Slot sMin = null;
		Slot sMax = null;
		for (Slot s : l) {
			double rate = s.getRateOnLane();
			if (rate < min) {
				min = rate;
				sMin = s;
			}
			if (rate > max) {
				max = rate;
				sMax = s;
			}
		}
		exitsSlots.add(sMin);
		exitsSlots.add(sMax);
		return exitsSlots;
	}

	public void removeConnexCrossroad(String nodeId, String formerConnexNodeId) {
		getNode(nodeId).removeConnexCrossroad(formerConnexNodeId);
	}

	public void removeContainer(String containerId) {
		Container c = containers.remove(containerId);
		if (c == null)
			log.warn(getTime()+":> Can't find container " + containerId + " in terminal !");
		if (out != null) {
			Slot slot = null;
			if (c.getContainerLocation() == null) {
				for (String laneID : bays.keySet()) {
					List<Slot> l = slots.get(laneID);
					if ( l != null ){
						for (Slot s : l) {
							if( s.contains(containerId)) {
								slot = s;
								break;
							}
						}
						if (slot != null){
							break;
						}
					}
				}
			} else {
				List<Slot> l = slots.get(c.getContainerLocation().getLaneId());
				for (Slot s : l) {
					if (s.getId().equals(c.getContainerLocation().getSlotId())) {
						slot = s;
						break;
					}
				}
			}

			if (slot == null) {
				log.error("Slot not found !!!");
			} else {
				try {
					slot.pop(c.getId());
				} catch (EmptySlotException e) {
					e.printStackTrace();
				} catch (NotAccessibleContainerException e) {
					e.printStackTrace();
				} catch (ContainerNotFoundException e) {
					e.printStackTrace();
				}
				out.slotContentChanged(slot);
			}
		}
		listener.containerRemoved(containerId);
		if (out != null){
			out.removeContainer(containerId);
		}
	}

	public boolean reserveRoad(String roadId, String vehicle, TimeWindow tw, int priority) {
		Road road = getBay(roadId);
		if (road != null) {
			Reservations rs = reservations.get(roadId);
			Reservation r = new Reservation(getTime(), vehicle, roadId, tw, priority);
			if (rs == null) {
				rs = new Reservations(road);
			}

			if (rs.canMakeReservation(r)) {
				rs.addReservation(r);
				reservations.put(roadId, rs);
				if (out != null)
					out.addReservation(r);
				return true;
			} else {
				// Why can't ?
				return false;
			}
		} else
			return true;
	}

	public void setContainerLocation(String containerId, ContainerLocation cl) {
		if (containers.containsKey(containerId)) {
			containers.get(containerId).setContainerLocation(cl);
			if (out != null)
				out.containerLocationChanged(containerId, cl);
		}
	}

	@Override
	public void setTextDisplay(TextDisplay display) {
		if (display instanceof GraphicDisplayPanel)
			this.out = (GraphicDisplayPanel) display;
	}

	public void setSeed(long newSeed) {
		if (random == null)
			random = new Random(newSeed);
		else
			random.setSeed(newSeed);
	}

	public boolean shipOut(int capacity, String paveID, double berthFromRate, double berthToRate, Set<String> containersToLoad) {
		// Find the boat
		Ship ship = null;
		int iBoat = -1;
		for (int i = 0; i < boats.size() && iBoat == -1; i++) {
			Ship b = boats.get(i);
			if (b.getCapacity() == capacity && b.getQuay().getId().equals(paveID) && b.getRateFrom() == berthFromRate && b.getRateTo() == berthToRate) {
				ship = b;
				for (String contID : containersToLoad) {
					if (containers.get(contID) != null)
						ship.addContainerToLoad(contID);
				}
				iBoat = i;
			}
		}
		if (ship == null) {
			log.error("Boat not found !");
			return false;
		} else {
			// Check if all containers are here :
			if (ship.getToUnload().size() == 0 && ship.getToLoad().size() == 0) {
				boats.remove(iBoat);
				if (listener != null) {
					listener.removeShip(ship);
				}
				log.info(getTime()+":> Ship "+ship.getID()+" out.");
				return true;
			} else {
				log.warn("boat.getToUnload().size()==" + ship.getToUnload().size() + "  boat.getToLoad().size()=="
						+ ship.getToLoad().size());
				for (String id : ship.getToUnload()) {
					log.error("Unload " + id);
				}
				for (String id : ship.getToLoad()) {
					log.error("Load " + id);
				}
				ship.resetContainerToLoad();
				return false;
			}
		}
	}

	public void showLaserHeads(boolean selected) {
		if (selected)
			listener.unhideLaserHeads();
		else
			listener.hideLaserHeads();
		this.showLaserHead = selected;
	}

	public void straddleCarrierStopped(String id) {
		if (out != null) {
			out.setVehicleSpeed(id, "0");
		}
	}

	public void straddleCarrierMoved(String straddleCarrierId, Location oldLocation, Location newLocation, double speed, String cssStyle) {
		// update coordinates
		listener.straddleCarrierMoved(straddleCarrierId, newLocation, cssStyle);
		if (out != null) {
			out.setVehicleLocation(straddleCarrierId, newLocation);
			out.setVehicleSpeed(straddleCarrierId, "" + speed);
		}

		Time t = getTime();
		double distance = 0;
		double travelTime = 0;
		if (oldLocation != null) {
			if (oldLocation.getRoad().getId().equals(newLocation.getRoad().getId())) {
				distance = Location.getLength(oldLocation.getCoords(), newLocation.getCoords());
				travelTime = distance / straddleCarriers.get(straddleCarrierId).getCurrentSpeed();
			} else {

				try {
					Path p = straddleCarriers.get(straddleCarrierId).getRouting().getShortestPath(oldLocation, newLocation);
					distance = p.getCostInMeters();
					travelTime = p.getCost();
				} catch (NoPathFoundException e) {
					e.printStackTrace();
				}
			}
		}
		MissionScheduler.getInstance().updateResourceLocation(t, straddleCarrierId, distance, travelTime);
	}

	public boolean unreserve(String laneID, String scID) {
		Road r = getBay(laneID);
		if (r != null) {
			Time now = getTime();
			Reservations rs = reservations.get(laneID);
			if (rs == null)
				rs = new Reservations(r);
			Reservation reservation = rs.getCurrentReservation(now, scID);

			if (reservation != null) {
				rs.removeReservation(reservation);
				reservations.put(laneID, rs);
				if (out != null)
					out.removeReservation(reservation, now);
				return true;
			} else
				return false;
		}
		return true;
	}

	public boolean unreserve(Reservation r) {
		Reservations rs = reservations.get(r.getRoadId());
		if (rs == null)
			rs = new Reservations(getBay(r.getRoadId()));
		Reservation resa = rs.removeReservation(r);
		if (resa == null)
			return false;
		else if (out != null) {
			out.removeReservation(resa, getTime());
		}
		return true;
	}

	public boolean unreserve(String roadId, String vehicle, TimeWindow tw) {

		Road r = getBay(roadId);
		if (r != null) {
			Time now = getTime();

			Reservations rs = reservations.get(roadId);
			if (rs == null)
				rs = new Reservations(r);

			Reservation reservation = rs.removeReservation(tw.getMin());

			if (reservation != null) {
				reservations.put(roadId, rs);
				if (out != null) {
					out.removeReservation(reservation, now);
				}
			}

			return reservation != null;
		}
		return true;
	}

	public boolean unstackContainer(Container c, String straddleCarrierID) {
		if (c == null) {
			log.error(getTime()+":> Cannot unstack a null container for " + straddleCarrierID);
			return false;
		} else if (c.getContainerLocation() == null) {
			log.error(getTime()+":> Container " + c.getId() + " has no location");
			return false;
		}
		String slotId = c.getContainerLocation().getSlotId();
		Slot s = getSlot(slotId);
		if (!s.isReady())
			return false;
		else {
			c.setSlot(null);

			try {
				s.pop(c.getId());
			} catch (EmptySlotException e) {
				e.printStackTrace();
			} catch (NotAccessibleContainerException e) {
				// ? SHIFT CONTAINER
				Time now = TimeScheduler.getInstance().getTime();
				TimeWindow tw = new TimeWindow(now, now);
				Level maxLevel = s.getLevel(s.getLevels().size() - 1);
				Slot tmpSlot = null;
				int tmpLevel = -1;
				boolean ok = false;
				Bay l = (Bay) s.getLocation().getRoad();
				while (!ok) {
					List<Slot> laneSlots = new ArrayList<Slot>(getSlots(l.getId()));
					while (tmpLevel == -1 && tmpSlot == null && laneSlots.size() > 0) {

						Slot destSlot = laneSlots.remove(random.nextInt(laneSlots.size()));

						if (!destSlot.getId().equals(s.getId())) {
							int level = destSlot.getLevels().size() - 1;
							if (level < 0)
								level = 0;

							boolean okLevel = false;
							while (level <= destSlot.getMaxLevel() && okLevel == false) {

								for (String contID : maxLevel.getContainersID()) {
									Container blockingContainer = getContainer(contID);
									if (destSlot.canAddContainer(blockingContainer, level, blockingContainer.getContainerLocation().getAlign())) {
										okLevel = true;
									} else {
										break;
									}
								}
								if (okLevel == false)
									level++;
							}
							if (okLevel) {
								tmpSlot = destSlot;
								tmpLevel = level;
								ok = true;
							}
						}

					}
					if (!ok) {
						Block p = getBlock(l.getPaveId());
						l = p.getLanes().get(random.nextInt(p.getLanes().size()));
					}
				}
				List<Mission> lMissions = new ArrayList<Mission>(maxLevel.getContainersID().size() * 2);
				int i = 1;
				for (String contID : maxLevel.getContainersID()) {
					Container blockingContainer = getContainer(contID);
					Mission mBlock = new Mission("shift " + c.getId() + "-" + i + "/" + maxLevel.getContainersID().size(),
							MissionKinds.STAY.getIntValue(), tw, tw, contID, new ContainerLocation(contID, l.getPaveId(), l.getId(), tmpSlot.getId(),
									tmpLevel, blockingContainer.getContainerLocation().getAlign()));

					lMissions.add(mBlock);
					i++;
				}
				TimeWindow tw2 = new TimeWindow(new Time(now, new Time(2)), new Time(now, new Time(2)));
				for (String contID : maxLevel.getContainersID()) {
					Container blockingContainer = getContainer(contID);
					Mission mBlock = new Mission("replace " + c.getId() + "-" + i + "/" + maxLevel.getContainersID().size(),
							MissionKinds.STAY.getIntValue(), tw2, tw2, contID, new ContainerLocation(contID, l.getPaveId(), l.getId(), s.getId(),
									maxLevel.getLevelIndex() - 1, blockingContainer.getContainerLocation().getAlign()));
					lMissions.add(mBlock);
					i++;
				}

				// AFFECT MISSIONS TO CONCERNED STRADDLE CARRIER
				StraddleCarrier rsc = getStraddleCarrier(straddleCarrierID);
				for (Mission m : lMissions)
					rsc.addMissionInWorkload(m);

				// RESCHEDULE the blocked mission between the 2 phases of the
				// shift
				Load currentLoad = rsc.getWorkload().getCurrentLoad();
				Mission currentMission = currentLoad.getMission();
				TimeWindow tw3 = new TimeWindow(new Time(now, new Time(1)), new Time(now, new Time(1)));
				Mission rescheduledMission = new Mission(currentMission.getId(), currentMission.getMissionKind().getIntValue(), tw3,
						currentMission.getDeliveryTimeWindow(), currentMission.getContainerId(), currentMission.getDestination());
				rsc.abortCurrentLoad();
				rsc.addMissionInWorkload(rescheduledMission);
			} catch (ContainerNotFoundException e) {
				e.printStackTrace();
			}

			if (out != null) {
				out.slotContentChanged(s);
			}
		}
		return true;

	}

	public void updateShip(Ship ship) {
		for (int i = 0; i < boats.size(); i++) {
			Ship boat = boats.get(i);
			if (boat.getQuay().getId().equals(ship.getQuay().getId())) {
				if (boat.getRateFrom() == ship.getRateFrom()) {
					if (boat.getRateTo() == ship.getRateTo()) {
						boats.remove(i);
						boats.add(i, ship);
					}
				}
			}
		}
	}

	public boolean vehicleIn(String ID, Time arrivalTime, List<String> slotIds, List<Container> containers) {
		if (slotIds.size() == 1) {
			Slot s = getSlot(slotIds.get(0));
			if (getBlock(s.getPaveId()).getType() == BlockType.ROAD) {
				if (trucks.get(ID) == null) {
					Truck t = new Truck(ID, arrivalTime, s.getId());
					trucks.put(ID, t);
				}
			}
		}
		// if a slot is not empty => Can't add vehicle on it !
		for (String s : slotIds) {
			Slot slot = getSlot(s);
			if (slot.getLevels().size() > 0 && !slot.getLevels().isEmpty())
				return false;
		}
		for (String s : slotIds) {
			Slot slot = getSlot(s);
			slot.setVehicleOn(true);
			listener.slotUpdated(slot);
		}
		if (containers != null && containers.size() > 0) {
			ArrayList<Container> l = new ArrayList<Container>(containers.size());
			for (Container c : containers) {
				NewContainer nc = new NewContainer(getTime(), c.getId(), c.getTEU(), c.getContainerLocation());
				nc.writeEventInDb();
				try {
					addContainer(c.getId(), c.getTEU(), c.getContainerLocation());
				} catch (Exception e) {
					e.printStackTrace();
				}
				l.add(getContainer(c.getId()));

			}
			listener.containersAdded(l);

		}
		if (slotIds.size() == 1) {
			Slot s = getSlot(slotIds.get(0));
			if (getBlock(s.getPaveId()).getType() == BlockType.ROAD) {
				Truck t = trucks.get(ID);
				t.setRealArrivalTime(getTime());
			}
		}
		return true;
	}

	public boolean vehicleOut(String ID, Time departureTime, List<String> slotIds, List<String> containers) {
		if (slotIds.size() == 1) {
			Slot s = getSlot(slotIds.get(0));
			if (getBlock(s.getPaveId()).getType() == BlockType.ROAD) {
				if (trucks.get(ID) == null) {
					return false;
				} else {
					Truck t = trucks.get(ID);
					if (t.getDepartureTime() == null)
						t.setDepartureTime(departureTime);
				}
			}
		}

		// Check if all containers are here :
		Hashtable<String, Slot> slotMap = new Hashtable<String, Slot>(slotIds.size());
		for (String s : slotIds) {
			slotMap.put(s, getSlot(s));
		}
		boolean all = true;

		if (containers == null || containers.size() == 0) {
			for (Slot s : slotMap.values()) {
				if (s.getLevels().size() > 0) {
					Level l = s.getLevel(0);
					try {
						l.getFirstContainer();
						return false;
					} catch (EmptyLevelException e) {
						all = true;
					}
				}
			}
		} else {
			for (int i = 0; i < containers.size(); i++) {
				Container c = getContainer(containers.get(i));
				if (c.getContainerLocation() == null)
					return false;
				else if (!slotMap.containsKey(c.getContainerLocation().getSlotId())) {
					return false;
				} else {
					for(StraddleCarrier sc : straddleCarriers.values()){
						if(c.getId().equals(sc.getHandledContainerId())){
							return false;
						}
					}
				}
			}
		}

		if (all) {
			if (containers != null) {
				for (String c : containers) {
					removeContainer(c);
				}
			}
			for (Slot slot : slotMap.values()) {
				slot.setVehicleOn(false);
				listener.slotUpdated(slot);
			}
			if (slotIds.size() == 1) {
				Slot s = getSlot(slotIds.get(0));
				if (getBlock(s.getPaveId()).getType() == BlockType.ROAD) {
					Truck t = trucks.get(ID);
					t.setRealDepartureTime(getTime());
				}
			}
			return true;
		} else
			return false;
	}

	// TODO WATCHOUT ! Time is not used in this method...
	public boolean willBeOpen(String roadId, Time time) {
		Road r = getBay(roadId);

		if (r == null)
			return true;
		else {
			return r.getTraffic().size() == 0;
		}
	}

	public void lockView(boolean selected) {
		listener.lockView(selected);
	}

	public Map<String, Block> getPaves(BlockType type) {
		return paves.get(type);
	}

	public void resetView() {
		if (listener != null) {
			listener.resetView();
		}
	}

	public Bay findClosestLane(Location currentLocation, List<String> alreadyTried) {
		if (currentLocation.getRoad() instanceof Bay && !alreadyTried.contains(currentLocation.getRoad())) {
			return (Bay) currentLocation.getRoad();
		} else {
			double min = Double.POSITIVE_INFINITY;
			Bay closest = null;
			for (Bay l : bays.values()) {
				if (!alreadyTried.contains(l.getId())) {
					Coordinates c1 = new Location(l, 0, true).getCoords();
					Coordinates c2 = new Location(l, 1, true).getCoords();
					double d = Location.getLength(currentLocation.getCoords(), c1);
					if (d < min) {
						min = d;
						closest = l;
					}
					double d2 = Location.getLength(currentLocation.getCoords(), c2);
					if (d2 < min) {
						min = d2;
						closest = l;
					}
				}
			}

			return closest;
		}
	}

	public boolean shipCanLoadContainer(String destinationSlotID, String containerID) {
		Ship ship = getShip(destinationSlotID);
		for (String s : ship.getConcernedSlotsIDs()) {
			Slot s2 = getSlot(s);
			if (s2.contains(containerID))
				return true;
		}
		return false;
	}

	public Ship getShip(String slotID) {
		for (Ship s : boats) {
			for (String slotConcernedID : s.getConcernedSlotsIDs()) {
				if (slotConcernedID.equals(slotID)) {
					return s;
				}
			}
		}
		return null;
	}

	public void updateStraddleFailureRepairTime(String straddleCarrierID, Time repairDuration) {
		StraddleCarrierRepaired scr = new StraddleCarrierRepaired(new Time(new Time(getTime().toStep() + 1), repairDuration), straddleCarrierID);
		TimeScheduler.getInstance().registerDynamicEvent(scr);
	}

	public void updateLaserHeadRange(String lhID, double range) {
		LaserSystem.getInstance().updateLaserHeadRange(lhID, range);
		if (out != null && listener != null)
			listener.laserHeadUpdated(lhID);
	}

	public List<StraddleCarrier> getStraddleCarriers() {
		return new ArrayList<StraddleCarrier>(straddleCarriers.values());
	}

	public List<Mission> getMissions() {
		return new ArrayList<Mission>(missions.values());
	}

	public String getSchedulingAlgorithmComputingTime() {
		double time_in_s = MissionScheduler.getInstance().getComputingTime();
		int h = (int) (time_in_s / 3600);
		time_in_s = time_in_s % 3600;
		int m = (int) (time_in_s / 60);
		double s = (time_in_s % 60);
		NumberFormat nf = NumberFormat.getNumberInstance(new Locale("en"));
		nf.setMaximumFractionDigits(2);
		nf.setMinimumFractionDigits(2);
		String ss = nf.format(s);
		ss = "" + (h < 10 ? "0" + h : h) + ":" + (m < 10 ? "0" + m : m) + ":" + (s < 10 ? "0" + ss : ss);
		return ss;
	}

	public Reservations getReservations(String id) {
		return reservations.get(id);
	}

	public void putReservations(String id, Reservations rs) {
		this.reservations.put(id, rs);
	}

	public int getPaveCount() {
		return this.paves.size();
	}

	public Collection<List<Slot>> getSlots() {
		return slots.values();
	}

	public Container[] getContainersArray() {
		return containers.values().toArray(new Container[containers.size()]);
	}

	public Integer getSimulationID() {
		return simID;
	}

	public static void closeInstance() {
		instance = null;
	}

	public boolean isDisplayed() {
		return simID != null;
	}
	
	public Truck getTruck(String id){
		Truck t = trucks.get(id);
		if(t == null){
			return TimeScheduler.getInstance().getIncomingTruck(id);
		} else {
			return t;
		}
	}

}