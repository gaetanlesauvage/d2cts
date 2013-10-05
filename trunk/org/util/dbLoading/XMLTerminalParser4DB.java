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
package org.util.dbLoading;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.StringTokenizer;

import org.apache.log4j.Logger;
import org.com.DbMgr;
import org.com.dao.scheduling.BBParametersDAO;
import org.com.dao.scheduling.BranchAndBoundParametersDAO;
import org.com.dao.scheduling.GreedyParametersDAO;
import org.com.dao.scheduling.LinearParametersDAO;
import org.com.dao.scheduling.OfflineACO2ParametersDAO;
import org.com.dao.scheduling.OfflineACOParametersDAO;
import org.com.dao.scheduling.OnlineACOParametersDAO;
import org.com.dao.scheduling.RandomParametersDAO;
import org.com.model.scheduling.BBParametersBean;
import org.com.model.scheduling.BranchAndBoundParametersBean;
import org.com.model.scheduling.GreedyParametersBean;
import org.com.model.scheduling.LinearParametersBean;
import org.com.model.scheduling.OfflineACO2ParametersBean;
import org.com.model.scheduling.OfflineACOParametersBean;
import org.com.model.scheduling.OnlineACOParametersBean;
import org.com.model.scheduling.RandomParametersBean;
import org.exceptions.LaneTooSmallException;
import org.exceptions.NotInContainerException;
import org.exceptions.NotInDepotException;
import org.exceptions.NotInPaveException;
import org.graphstream.graph.ElementNotFoundException;
import org.missions.MissionKinds;
import org.positioning.Coordinates;
import org.scheduling.LinearMissionScheduler;
import org.scheduling.bb.BB;
import org.scheduling.branchAndBound.BranchAndBound;
import org.scheduling.greedy.GreedyMissionScheduler;
import org.scheduling.offlineACO.OfflineACOScheduler;
import org.scheduling.offlineACO2.OfflineACOScheduler2;
import org.scheduling.onlineACO.OnlineACOScheduler;
import org.scheduling.random.RandomMissionScheduler;
import org.system.Crossroad;
import org.system.Road;
import org.system.StraddleCarrierSlot;
import org.system.container_stocking.Bay;
import org.system.container_stocking.BayCrossroad;
import org.system.container_stocking.BlockType;
import org.system.container_stocking.ContainerKind;
import org.system.container_stocking.Slot;
import org.time.Time;
import org.time.TimeWindow;
import org.time.event.LaserHeadFailure;
import org.util.Location;
import org.util.building.SlotBuilderHelper;
import org.util.building.SlotsRateHelper;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

/**
 * XML to database parser.
 * 
 * @author Gaëtan Lesauvage
 * @since April 2013
 * @version 1.0
 */
public class XMLTerminalParser4DB implements ContentHandler {
	private static final Logger log = Logger.getLogger(XMLTerminalParser4DB.class);

	private static final String DEPOT_TYPE = "depot";
	private Connection connection;

	// NAMES
	private String scenarioName;
	private String terminalName;
	// IDS
	private Integer scenarioID;
	private Integer terminalID;

	private boolean inDepot;

	private String roadName;
	private int roadPointIndex;

	private String blockName;

	private ArrayList<Time> current_event_time;
	private ArrayList<String> current_event_type;
	private int inEvent = 0;

	// private ShipIn currentShipIn;
	// private ShipOut currentShipOut;

	private String currentStraddleCarrierId;
	private boolean inContainer;
	private String current_container_id;

	// private ContainerLocation current_container_location;
	private boolean inMission = false;
	private String idMission, missionContainerId;
	private int missionKind;
	private TimeWindow twPickup, twDelivery;
	// private ContainerLocation missionLocation;

	// private Mission current_mission;

	private boolean comment = false;
	// private HashMap<String, RoadPoint> roadPoints;
	// private String currentHost;

	private boolean inShipContainer = false;
	private String current_ship_container_id = "";
	// private Time current_ship_container_time = null;
	// private Double current_container_teu;

	private String current_event_newMission_truckID;

	private String current_mission_slot;

	private int current_mission_level;

	private Integer current_mission_alignment;
	private List<String> currentEventDescription;

	private PreparedStatement psCurrentEvent;

	// FIXME : load simulation ID when creating the association
	// terminal/scenario
	private Integer simulationID;

	public XMLTerminalParser4DB(Connection connection) throws SQLException {
		super();
		this.connection = connection;

		this.current_event_time = new ArrayList<Time>(2);
		this.current_event_type = new ArrayList<String>(2);

		// retrieves types-IDs associations from database
		inDepot = false;
		// inRoad = false;
		// inPave = false;
		inContainer = false;
	}

	/*
	 * private void insertVehiclesFile() throws SQLException{ try{ String query
	 * = "INSERT INTO SC_INSTANCES (FILE) VALUES (?)"; PreparedStatement
	 * statement = connection.prepareStatement(query); statement.setString(1,
	 * vehicleFile); executeQuery(statement); } catch (SQLException e) {
	 * if(e.getErrorCode()==1062){
	 * System.err.println("Vehicle file already inserted!"); } else{ throw e; }
	 * } }
	 */
	/*
	 * public static void setRemoteTimeScheduler (RemoteTimeScheduler
	 * timeScheduler) { XMLTerminalParser4DB.scheduler = timeScheduler; }
	 */

	/*
	 * public static void setRemoteTerminal (RemoteTerminal terminal) throws
	 * RemoteException{ XMLTerminalParser4DB.terminal = terminal; }
	 */

	@Override
	public void characters(char[] ch, int start, int length) throws SAXException {

	}

	@SuppressWarnings("unused")
	private void containerLocation(Attributes atts) throws NotInContainerException, SQLException {
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

		// FIXME remove from xml containerLocation desciption
		// String block = atts.getValue("pave");
		// String bay = atts.getValue("lane");

		String slot = atts.getValue("slot");
		int level = Integer.parseInt(atts.getValue("level"));

		String slot_level_name = slot + "-" + level;

		String align = atts.getValue("align");

		String query = null;
		PreparedStatement ps = null;
		// ContainerLocation cl = new ContainerLocation(container, block, bay,
		// slot, level, ContainerAlignment.valueOf(align).getValue());
		if (inMission) {
			// TODO
			current_mission_slot = slot;
			current_mission_level = level;
			current_mission_alignment = DbMgr.getInstance().getDatabaseIDsRetriever().getAlignment(align);
			// log.fatal("TODO : containerLocation inMission!");
		} else if (inContainer) {
			query = "SELECT 1 FROM CONTAINERS_INIT_LOCATION WHERE CONTAINER_NAME = ? AND SCENARIO = ?";
			ps = connection.prepareStatement(query);
			ps.setString(1, container);
			ps.setInt(2, scenarioID);
			ResultSet rs = ps.executeQuery();
			if (!rs.next()) {
				rs.close();
				ps.close();

				query = "INSERT INTO CONTAINERS_INIT_LOCATION (CONTAINER_NAME,SCENARIO,SLOT_LEVEL,SLOT_ALIGNMENT,VEHICLE) VALUES (?,?,?,?,?)";
				ps = connection.prepareStatement(query);
				ps.setString(1, container);
				ps.setInt(2, scenarioID);
				ps.setString(3, slot_level_name);
				ps.setInt(4, DbMgr.getInstance().getDatabaseIDsRetriever().getAlignment(align));
				ps.setNull(5, Types.VARCHAR);
			} else {
				ps.close();
				ps = null;
			}
		} else if (inShipContainer) {
			// TODO
			// current_container_location = cl;
			log.fatal("TODO : containerLocation inShipContainer!");
		} else
			throw new NotInContainerException();

		if (ps != null) {
			executeQuery(ps);
		}
	}

	@SuppressWarnings("unused")
	private void wallPoint(Attributes atts) throws SQLException {
		String id = atts.getValue("id");
		double x = Double.parseDouble(atts.getValue("x"));
		double y = Double.parseDouble(atts.getValue("y"));
		double z = 0.0;

		String query = "INSERT INTO BLOCK_WALL_POINT (NAME,TERMINAL,X,Y,Z) VALUES (?,?,?,?,?)";
		PreparedStatement statement = connection.prepareStatement(query);
		statement.setString(1, id);
		statement.setInt(2, terminalID);
		statement.setDouble(3, x);
		statement.setDouble(4, y);
		statement.setDouble(5, z);
		executeQuery(statement);

		log.info("add wallpoint " + id);
	}

	@SuppressWarnings("unused")
	private void crossroad(Attributes atts) throws SQLException {
		String id = atts.getValue("id");
		double x = Double.parseDouble(atts.getValue("x"));
		double y = Double.parseDouble(atts.getValue("y"));
		double z = 0.0;
		String query = "INSERT INTO CROSSROAD (NAME, TERMINAL, TYPE, X,Y,Z) VALUES(?,?,?,?,?,?)";

		PreparedStatement statement = connection.prepareStatement(query);
		statement.setString(1, id);
		statement.setInt(2, terminalID);
		statement.setInt(3, DbMgr.getInstance().getDatabaseIDsRetriever().getCrossroadTypeID(Crossroad.class.getName()));
		statement.setDouble(4, x);
		statement.setDouble(5, y);
		statement.setDouble(6, z);
		log.info("Adding crossroad " + id);
		executeQuery(statement);

	}

	@Override
	public void endDocument() throws SAXException {

	}

	// FIXME : Use XMLTags
	@Override
	public void endElement(String uri, String localName, String qName) throws SAXException {
		if (localName.equals("comment"))
			comment = false;
		else if (comment == false) {
			switch (localName) {
			case DEPOT_TYPE: // FIXME
				inDepot = false;
				break;
			case "road":
				roadName = null;
				break;
			case "pave":
				// inPave = false;
				break;
			case "lane":
				// inLane = false;
				break;
			case "exchangeLane":
				// FIXME
				new Exception("Exchange lane found !!!").printStackTrace();
				break;
			case "mission":
				try {
					mission();
				} catch (SQLException e) {
					throw new SAXException(e);
				}
				break;
			case "event":
				StringBuilder description = new StringBuilder(500);
				for (String s : currentEventDescription) {
					description.append(s + ",");
				}
				if (description.indexOf(",") >= 0)
					description = description.deleteCharAt(description.length() - 1);
				try {
					psCurrentEvent.setString(4, description.toString());
					executeQuery(psCurrentEvent);
				} catch (SQLException e) {
					throw new SAXException(e);
				}

				current_event_time.remove(inEvent - 1);
				current_event_type.remove(inEvent - 1);
				inEvent--;
				break;
			case "container":
				inContainer = false;
				break;
			case "straddleCarrier":
				// TODO handle StraddleCarriers
				currentStraddleCarrierId = null;
				break;
			case "terminal":
				// TODO ?
				// insertTerminal();
				// terminal.drawElements();
				/*
				 * currentStraddleCarrier = null; lane = null;
				 */
				/*
				 * int nSlots = 0; for(String s : terminal.slots.keySet()){
				 * nSlots+=terminal.slots.get(s).size(); }
				 * System.out.println("Slots : "+nSlots);
				 * System.out.println("Crossroads : "
				 * +terminal.crossroads.size());
				 * System.out.println("Roads : "+terminal.roads.size());
				 * System.out.println("Lanes : "+terminal.lanes.size());
				 */
				break;
			case "shipContainerIn":
				// TODO
				/*
				 * currentShipIn.addContainerToUnload(current_ship_container_id,
				 * current_container_teu); NewShipContainer nsc = new
				 * NewShipContainer(current_ship_container_time,
				 * current_ship_container_id, current_container_teu,
				 * current_container_location, currentShipIn.getQuayID(),
				 * currentShipIn.getShipBerthFromRate(),
				 * currentShipIn.getShipBerthToRate());
				 * scheduler.registerDynamicEvent(nsc);
				 * current_ship_container_id = ""; current_ship_container_time =
				 * null; current_container_location = null;
				 * current_container_teu = -1; inShipContainer = false;
				 */
				break;
			}
		}
	}

	@SuppressWarnings("unused")
	private void mission(Attributes atts) throws SQLException {
		idMission = atts.getValue("id");
		missionKind = Integer.parseInt(atts.getValue("kind"));
		missionContainerId = atts.getValue("container");

		// If MissionKinds == IN OR IN_OUT then add container into database
		if (missionKind == MissionKinds.IN.getIntValue() || missionKind == MissionKinds.IN_AND_OUT.getIntValue()) {
			ContainerAttributes a = new ContainerAttributes();
			a.set("id", missionContainerId);
			container(a);
		}
		inMission = true;
	}

	private void mission() throws SQLException {
		// Mission m = new Mission(idMission, missionKind, twPickup, twDelivery,
		// missionContainerId, missionLocation);
		if (inEvent == 0) {
			String query = "INSERT INTO MISSION (NAME,SCENARIO,TYPE,CONTAINER,TRUCK,TW_P_MIN,TW_P_MAX,TW_D_MIN,TW_D_MAX,"
					+ "SLOT_LEVEL_DESTINATION, SLOT_ALIGNMENT) VALUES (?,?,?,?,?,?,?,?,?,?,?)";
			PreparedStatement ps = connection.prepareStatement(query);

			// <mission id='unload_train0_1' container='OQEU 777022 0' kind='1'>
			// <timewindow start='01:15:20.60' end='01:53:00.90'/>
			// <timewindow start='01:16:48.77' end='01:55:13.16'/>
			// <containerLocation container='OQEU 777022 0' pave='E'
			// lane='E-2/17' slot='E-2/17-1' level='0' align='center'/>
			// </mission>
			ps.setString(1, idMission);
			ps.setInt(2, scenarioID);
			ps.setInt(3, missionKind);
			ps.setString(4, missionContainerId);
			if ("".equals(current_event_newMission_truckID)) {
				ps.setNull(5, Types.VARCHAR);
			} else {
				ps.setString(5, current_event_newMission_truckID); // FIXME set
				// truckID
			}
			ps.setTime(6, twPickup.getMin().getSQLTime());
			ps.setTime(7, twPickup.getMax().getSQLTime());
			ps.setTime(8, twDelivery.getMin().getSQLTime());
			ps.setTime(9, twDelivery.getMax().getSQLTime());
			// Build slot level id : slotID+"-"+level
			ps.setString(10, current_mission_slot + "-" + current_mission_level);
			ps.setInt(11, current_mission_alignment);

			log.trace("Inserting mission " + idMission + " with type=" + missionKind + " and container=" + missionContainerId);
			executeQuery(ps);
		} else {
			currentEventDescription.add("id=" + idMission);
			currentEventDescription.add("kind=" + missionKind);
			currentEventDescription.add("container=" + missionContainerId);
			if (!"".equals(current_event_newMission_truckID)) // FIXME set
				// truckID
				currentEventDescription.add("truckID=" + current_event_newMission_truckID);
			currentEventDescription.add("twPMin=" + twPickup.getMin());
			currentEventDescription.add("twPMax=" + twPickup.getMax());
			currentEventDescription.add("twDMin=" + twDelivery.getMin());
			currentEventDescription.add("twDMax=" + twDelivery.getMax());
			currentEventDescription.add("slot=" + current_mission_slot);
			currentEventDescription.add("level=" + current_mission_level);
			currentEventDescription.add("alignement=" + current_mission_alignment);
		}
		twDelivery = twPickup = null;
		this.idMission = "";
		this.missionContainerId = "";

		inMission = false;

	}

	@Override
	public void endPrefixMapping(String prefix) throws SAXException {

	}

	@SuppressWarnings("unused")
	private void event(Attributes atts) throws SQLException {
		inEvent++;
		Time t = new Time(atts.getValue("time"));
		current_event_time.add(t);
		current_event_type.add(atts.getValue("type"));
		String query = "INSERT INTO EVENT(TYPE,SCENARIO,T,DESCRIPTION) VALUES (?,?,?,?)";
		psCurrentEvent = connection.prepareStatement(query);
		psCurrentEvent.setString(1, atts.getValue("type"));
		psCurrentEvent.setInt(2, scenarioID);
		psCurrentEvent.setTime(3, t.getSQLTime());
		currentEventDescription = new ArrayList<>(10);
		if (current_event_type.get(inEvent - 1) == null) {
			log.error("ERROR : type value must be given in a <event> xml tag !");
		} else if ("shipIn".equals(current_event_type.get(inEvent - 1)) || "shipOut".equals(current_event_type.get(inEvent - 1))) {
			int capacity = Integer.parseInt(atts.getValue("capacity"));
			String quay = atts.getValue("quay");
			double from = Double.parseDouble(atts.getValue("from"));
			double to = Double.parseDouble(atts.getValue("to"));
			currentEventDescription.add("capacity=" + capacity);
			currentEventDescription.add("quay=" + quay);
			currentEventDescription.add("from=" + from);
			currentEventDescription.add("to=" + to);
		} else if ("straddleCarrierFailure".equals(current_event_type.get(inEvent - 1))) {
			String straddleCarrierID = atts.getValue("straddleCarrierID");
			String failureType = atts.getValue("failureType");
			String repairDuration = atts.getValue("repairDuration");
			currentEventDescription.add("straddleCarrierID=" + straddleCarrierID);
			currentEventDescription.add("failureType=" + failureType);
			currentEventDescription.add("repariDuration=" + repairDuration);
		} else if ("straddleRepair".equals(current_event_type.get(inEvent - 1))) {
			String straddleId = atts.getValue("straddleId");
			currentEventDescription.add("straddleId=" + straddleId);
		} else if ("containerOut".equals(current_event_type.get(inEvent - 1))) {
			String containerId = atts.getValue("containerId");
			currentEventDescription.add("containerId=" + containerId);
		} else if ("shipContainerOut".equals(current_event_type.get(inEvent - 1))) {
			String containerId = atts.getValue("containerId");
			String destSlotId = atts.getValue("slotId");
			currentEventDescription.add("containerId=" + containerId);
			currentEventDescription.add("slotId=" + destSlotId);
		} else if ("affectMission".equals(current_event_type.get(inEvent - 1))) {
			String missionId = atts.getValue("mission");
			String straddleCarrierId = atts.getValue("straddleCarrier");
			currentEventDescription.add("mission=" + missionId);
			currentEventDescription.add("straddleCarrier=" + straddleCarrierId);
		} else if ("vehicleIn".equals(current_event_type.get(inEvent - 1)) || "vehicleOut".equals(current_event_type.get(inEvent - 1))) {
			String current_event_vehicle_ID = atts.getValue("id");
			String lanesIds = atts.getValue("lanes");
			currentEventDescription.add("id=" + current_event_vehicle_ID);
			currentEventDescription.add("lanes=" + lanesIds);
		} else if (LaserHeadFailure.getEventType().equals(current_event_type.get(inEvent - 1))) {
			String lhID = atts.getValue("laserHeadID");
			double range = Double.parseDouble(atts.getValue("range"));
			String duration = null;
			if (atts.getIndex("duration") >= 0) {
				duration = atts.getValue("duration");
			}
			currentEventDescription.add("laserHeadID=" + lhID);
			currentEventDescription.add("range=" + range);
			if (duration != null)
				currentEventDescription.add("duration=" + duration);
		}

	}

	// private void exchangeLane (Attributes atts) throws NotInPaveException,
	// RemoteException, LaneTooSmallException {
	// String id = atts.getValue("id");
	// String cOriginId = atts.getValue("origin");
	// String cDestinationId = atts.getValue("destination");
	// boolean directed = false;
	// if(atts.getIndex("directed")>= 0){
	// directed = Boolean.parseBoolean(atts.getValue("directed"));
	// }
	// String pave = "";
	// if(inPave) pave = this.pave.getId();
	// else throw new NotInPaveException();
	//
	//
	// String in = atts.getValue("in");
	//
	// StringTokenizer st = new StringTokenizer(in, ",");
	// double inX = Double.parseDouble(st.nextToken());
	// double inY = Double.parseDouble(st.nextToken());
	//
	// String out = atts.getValue("out");
	// st = new StringTokenizer(out, ",");
	// double outX = Double.parseDouble(st.nextToken());
	// double outY = Double.parseDouble(st.nextToken());
	//
	// RemoteTerminal terminal = getRemoteTerminal();
	// BayCrossroad cOrigin = (BayCrossroad)terminal.getCrossroad(cOriginId);
	// BayCrossroad cDestination =
	// (BayCrossroad)terminal.getCrossroad(cDestinationId);
	// lane = new ExchangeBay(id, cOrigin, cDestination, directed, pave, new
	// Coordinates(inX,inY), new Coordinates(outX,outY));
	// this.pave.addLane(lane);
	// String slots = atts.getValue("slots");
	// StringTokenizer stSlots = new StringTokenizer(slots,"-");
	// SlotBuilderHelper[] tSlots = new
	// SlotBuilderHelper[stSlots.countTokens()];
	// int i=0;
	// while (stSlots.hasMoreTokens()){
	// String slots1 = stSlots.nextToken();
	// StringTokenizer stSlots1 = new StringTokenizer(slots1,"*");
	// String type = stSlots1.nextToken();
	// int quantity = Integer.parseInt(stSlots1.nextToken());
	// tSlots[i++] = new SlotBuilderHelper(type, quantity);
	// }
	// //if(lane.getId().equals("C-41/41"))
	// System.out.println("Adding Exchange lane C-41/41 in pave "+pave+" type "+this.pave.getType());
	// getRemoteTerminal().addSlots(lane.getId(), lane.addSlots(tSlots,
	// this.pave.getType()));
	// }

	/*
	 * public RemoteTerminal getRemoteTerminal(){ return terminal; }
	 */

	@Override
	public void ignorableWhitespace(char[] ch, int start, int length) throws SAXException {

	}

	@SuppressWarnings("unused")
	private void include(Attributes atts) throws SAXException {
		String file = atts.getValue("file");
		XMLReader saxReader = XMLReaderFactory.createXMLReader("com.sun.org.apache.xerces.internal.parsers.SAXParser");
		saxReader.setContentHandler(this);
		log.info("Include file " + file);
		try {
			saxReader.parse(new InputSource(this.getClass().getResourceAsStream("/" + file)));
		} catch (IOException e) {
			log.error("Include error : " + file, e);
			throw new SAXException(e);
		}
	}

	@SuppressWarnings("unused")
	private void bay(Attributes atts) throws SQLException, LaneTooSmallException, NotInPaveException {
		if (blockName == null || "".trim().equals(blockName))
			throw new NotInPaveException();

		String id = atts.getValue("id");
		String cOriginId = atts.getValue("origin");
		String cDestinationId = atts.getValue("destination");

		String query = "INSERT INTO ROAD (NAME,TERMINAL,TYPE,ORIGIN,DESTINATION,DIRECTED,BLOCK) VALUES (?,?,?,?,?,?,?)";
		PreparedStatement statement = connection.prepareStatement(query);

		int parameterIndex = 1;
		statement.setString(parameterIndex++, id);
		statement.setInt(parameterIndex++, terminalID);
		statement.setInt(parameterIndex++, DbMgr.getInstance().getDatabaseIDsRetriever().getRoadTypeID(Bay.class.getName()));
		// test
		statement.setString(parameterIndex++, cOriginId);
		statement.setString(parameterIndex++, cDestinationId);
		if (atts.getIndex("directed") >= 0) {
			statement.setBoolean(parameterIndex++, Boolean.parseBoolean(atts.getValue("directed")));
		} else {
			statement.setBoolean(parameterIndex++, false);
		}
		statement.setString(parameterIndex, blockName);
		executeQuery(statement);

		// Recuperer les coordonnees des entrees sorties de bay pour calculer la
		// longueur
		Coordinates coordOrigin = null;
		Coordinates coordDestination = null;
		String queryLength = "SELECT X,Y,Z FROM CROSSROAD WHERE NAME = ? AND TERMINAL = ?";
		PreparedStatement ps = connection.prepareStatement(queryLength);
		ps.setString(1, cOriginId);
		ps.setInt(2, terminalID);
		ResultSet rs = ps.executeQuery();
		if (rs.next()) {
			coordOrigin = new Coordinates(rs.getDouble(1), rs.getDouble(2), rs.getDouble(3));
		} else {
			throw new SQLException("Crossroad " + cOriginId + " not found!");
		}
		rs.close();
		ps.setString(1, cDestinationId);
		rs = ps.executeQuery();
		if (rs.next()) {
			coordDestination = new Coordinates(rs.getDouble(1), rs.getDouble(2), rs.getDouble(3));
		} else {
			throw new SQLException("Crossroad " + cDestinationId + " not found!");
		}
		rs.close();
		ps.close();

		String in = atts.getValue("in");
		StringTokenizer st = new StringTokenizer(in, ",");
		double inX = Double.parseDouble(st.nextToken());
		double inY = Double.parseDouble(st.nextToken());

		String out = atts.getValue("out");
		st = new StringTokenizer(out, ",");
		double outX = Double.parseDouble(st.nextToken());
		double outY = Double.parseDouble(st.nextToken());

		double bayLength = Location.getLength(coordOrigin, coordDestination);
		double in_rate = Location.getLength(new Coordinates(inX, inY), coordOrigin) / bayLength;
		double out_rate = Location.getLength(new Coordinates(outX, outY), coordOrigin) / bayLength;

		// statement.setDouble(parameterIndex++, in_rate);
		// statement.setDouble(parameterIndex++, out_rate);

		String slots = atts.getValue("slots");
		StringTokenizer stSlots = new StringTokenizer(slots, "-");
		SlotBuilderHelper[] tSlots = new SlotBuilderHelper[stSlots.countTokens()];
		int i = 0;
		while (stSlots.hasMoreTokens()) {
			// TODO 26/04/2013 compute slot ID ? or get it from somewhere else!
			String slots1 = stSlots.nextToken();
			StringTokenizer stSlots1 = new StringTokenizer(slots1, "*");
			String type = stSlots1.nextToken();
			int quantity = Integer.parseInt(stSlots1.nextToken());

			tSlots[i++] = new SlotBuilderHelper(type, quantity);
		}

		String querySlots = "INSERT INTO SLOT (NAME,TERMINAL,BAY,LEN,RATE) VALUES (?,?,?,?,?)";
		String querySlotLevels = "INSERT INTO SLOT_LEVEL (NAME,TERMINAL,SLOT,LEVEL_NUMBER) VALUES (?,?,?,?)";

		ps = connection.prepareStatement(querySlots);
		PreparedStatement psSlotsLevels = connection.prepareStatement(querySlotLevels);

		double innerLength = Location.getLength(new Coordinates(inX, inY), new Coordinates(outX, outY));

		Map<String, SlotsRateHelper> slotsMap = Bay.getSlotsIdsAndRates(tSlots, innerLength, bayLength, id, in_rate);

		for (Entry<String, SlotsRateHelper> e : slotsMap.entrySet()) {
			String slotID = e.getKey();
			ps.setString(1, slotID);
			ps.setInt(2, terminalID);
			ps.setString(3, id);
			ps.setInt(4, ContainerKind.getType(e.getValue().getType())); // TODO
			// test
			// type
			ps.setDouble(5, e.getValue().getRate());
			ps.addBatch();

			for (i = 0; i < Slot.SLOT_MAX_LEVEL; i++) {
				psSlotsLevels.setString(1, slotID + "-" + i);
				psSlotsLevels.setInt(2, terminalID);
				psSlotsLevels.setString(3, slotID);
				psSlotsLevels.setInt(4, i);
				psSlotsLevels.addBatch();
			}

		}
		ps.executeBatch();
		ps.close();
		psSlotsLevels.executeBatch();
		psSlotsLevels.close();

		// SLOT LEVELS

		/*
		 * String queryAssocBaySlots =
		 * "INSERT INTO LINK_BAYS_SLOTS (ID_LINK_BAYS_SLOTS,SLOT,TERMINAL,BAY,RATE) VALUES (?,?,?,?,?)"
		 * ; String queryNextID =
		 * "SELECT MAX(ID_LINK_BAYS_SLOTS) FROM LINK_BAYS_SLOTS";
		 * PreparedStatement psNextID =
		 * connection.prepareStatement(queryNextID); long nextID = 0; ResultSet
		 * rsNextID = psNextID.executeQuery(); if((rsNextID.next())) {
		 * if(rsNextID.wasNull()) nextID=0; else nextID = rsNextID.getLong(1)+1;
		 * } rsNextID.close(); psNextID.close();
		 * statement.setLong(lastParameterIndex, nextID);
		 * 
		 * 
		 * ps = connection.prepareStatement(queryAssocBaySlots);
		 * for(Entry<String,SlotsRateHelper> e : slotsMap.entrySet()){
		 * ps.setLong(1, nextID); ps.setString(2, e.getKey()); ps.setString(3,
		 * terminalName); ps.setString(4, id); ps.setDouble(5,
		 * e.getValue().getRate()); ps.addBatch(); } //TODO handle query failure
		 * ps.executeBatch(); ps.close();
		 * 
		 * statement.setLong(lastParameterIndex, nextID);
		 * executeQuery(statement);
		 */
	}

	/*
	 * private void executeQueryWithoutClosing(PreparedStatement statement)
	 * throws SQLException{
	 * 
	 * statement.execute();
	 * 
	 * //FIXME... //connection.commit(); }
	 */

	@SuppressWarnings("unused")
	private void bayCrossroad(Attributes atts) throws SQLException {
		String id = atts.getValue("id");
		String mainRoad = roadName;

		double x = Double.parseDouble(atts.getValue("x"));
		double y = Double.parseDouble(atts.getValue("y"));
		double z = 0.0;

		String query = "INSERT INTO CROSSROAD (NAME, TERMINAL, TYPE, ROAD, BLOCK, X, Y, Z) VALUES (?,?,?,?,?,?,?,?)";

		PreparedStatement statement = connection.prepareStatement(query);
		statement.setString(1, id);
		statement.setInt(2, terminalID);
		statement.setInt(3, DbMgr.getInstance().getDatabaseIDsRetriever().getCrossroadTypeID(BayCrossroad.class.getName()));
		statement.setString(4, mainRoad);
		statement.setString(5, blockName);
		statement.setDouble(6, x);
		statement.setDouble(7, y);
		statement.setDouble(8, z);
		log.info("Adding bay crossroad " + id);
		executeQuery(statement);
	}

	private void executeQuery(PreparedStatement statement) throws SQLException {
		statement.execute();
		statement.close();
		// FIXME...
		// connection.commit();
	}

	@SuppressWarnings("unused")
	private void laserHead(Attributes atts) throws SQLException {
		String id = atts.getValue("id");
		double x = Double.parseDouble(atts.getValue("x"));
		double y = Double.parseDouble(atts.getValue("y"));
		double z = Double.parseDouble(atts.getValue("z"));
		double rangeX = Double.parseDouble(atts.getValue("rangeX"));
		double rangeY = Double.parseDouble(atts.getValue("rangeY"));
		double rangeZ = Double.parseDouble(atts.getValue("rangeZ"));

		String query = "SELECT 1 FROM LASERHEAD WHERE NAME = ? AND SCENARIO = ?";
		PreparedStatement statement = connection.prepareStatement(query);
		statement.setString(1, id);
		statement.setInt(2, scenarioID);
		ResultSet rs = statement.executeQuery();
		if (!rs.next()) {
			rs.close();
			statement.close();

			query = "INSERT INTO LASERHEAD (NAME,SCENARIO,X,Y,Z,RX,RY,RZ) VALUES (?,?,?,?,?,?,?,?)";
			statement = connection.prepareStatement(query);

			statement.setString(1, id);
			statement.setInt(2, scenarioID);
			statement.setDouble(3, x);
			statement.setDouble(4, y);
			statement.setDouble(5, z);
			statement.setDouble(6, rangeX);
			statement.setDouble(7, rangeY);
			statement.setDouble(8, rangeZ);

			executeQuery(statement);
			log.info("Laser Head added : " + id);
		} else {
			rs.close();
			statement.close();
		}
	}

	@Override
	public void processingInstruction(String target, String data) throws SAXException {

	}

	@SuppressWarnings("unused")
	private void road(Attributes atts) throws SQLException {
		String id = atts.getValue("id");
		String origin = atts.getValue("origin");
		String destination = atts.getValue("destination");

		String query = null;
		PreparedStatement statement = null;
		query = "INSERT INTO ROAD (NAME,TERMINAL,TYPE,ORIGIN,DESTINATION,DIRECTED,BLOCK) VALUES (?,?,?,?,?,?,?)";
		statement = connection.prepareStatement(query);

		statement.setString(1, id);
		statement.setInt(2, terminalID);
		statement.setInt(3, DbMgr.getInstance().getDatabaseIDsRetriever().getRoadTypeID(Road.class.getName()));
		statement.setString(4, origin);
		statement.setString(5, destination);
		if (atts.getIndex("directed") >= 0) {
			statement.setBoolean(6, Boolean.parseBoolean(atts.getValue("directed")));
		} else {
			statement.setNull(6, Types.BOOLEAN);
		}
		statement.setString(7, blockName);

		log.info("Adding road " + id);

		executeQuery(statement);
		roadName = id;
		roadPointIndex = 0;
	}

	@SuppressWarnings("unused")
	private List<Coordinates> roadsCoordinates(String roadName) throws ElementNotFoundException, SQLException {
		String queryRoads = "SELECT origin.X as ox, origin.Y as oy, origin.Z as oz, destination.X as dx, destination.Y as dy, destination.Z as dz"
				+ " FROM ROADS r INNER JOIN CROSSROADS origin ON r.ORIGIN = origin.NAME AND r.TERMINAL = origin.TERMINAL"
				+ " INNER JOIN CROSSROADS destination ON r.DESTINATION = destination.NAME AND r.TERMINAL = destination.TERMINAL"
				+ " WHERE r.NAME = ? AND r.TERMINAL = ?";
		String queryBays = "SELECT origin.X as ox, origin.Y as oy, origin.Z as oz, destination.X as dx, destination.Y as dy, destination.Z as dz"
				+ " FROM BAYS r INNER JOIN BAYCROSSROADS origin ON r.ORIGIN = origin.NAME AND r.TERMINAL = origin.TERMINAL"
				+ " INNER JOIN BAYCROSSROADS destination ON r.DESTINATION = destination.NAME AND r.TERMINAL = destination.TERMINAL"
				+ " WHERE r.NAME = ? AND r.TERMINAL = ?";
		String queryRoadPoints = "SELECT X as x, Y as y, Z as z FROM ROADPOINTS WHERE ROAD=? AND TERMINAL=? ORDER BY INDEX_IN_ROAD";

		PreparedStatement statement = connection.prepareStatement(queryRoads);
		PreparedStatement statementRoadPoints = connection.prepareStatement(queryRoadPoints);
		statement.setString(1, roadName);
		statement.setString(2, terminalName);
		statementRoadPoints.setString(1, roadName);
		statementRoadPoints.setString(2, terminalName);

		ResultSet rs = statement.executeQuery();
		ResultSet rsRoadPoints = statementRoadPoints.executeQuery();

		Coordinates cO = null;
		Coordinates cD = null;
		List<Coordinates> points = new ArrayList<>();
		if (rs.next()) {
			double ox = rs.getDouble("ox");
			double oy = rs.getDouble("oy");
			double oz = rs.getDouble("oz");
			double dx = rs.getDouble("dx");
			double dy = rs.getDouble("dy");
			double dz = rs.getDouble("dz");
			cO = new Coordinates(ox, oy, oz);
			cD = new Coordinates(dx, dy, dz);

			while (rsRoadPoints.next()) {
				Double x = rsRoadPoints.getDouble("x");
				Double y = rsRoadPoints.getDouble("y");
				Double z = rsRoadPoints.getDouble("y");
				points.add(new Coordinates(x, y, z));
			}

		} else {
			// Try with bays ?
			rs.close();
			rsRoadPoints.close();
			statement.close();
			statementRoadPoints.close();

			statement = connection.prepareStatement(queryBays);
			statement.setString(1, roadName);
			statement.setString(2, terminalName);
			if (rs.next()) {
				double ox = rs.getDouble("ox");
				double oy = rs.getDouble("oy");
				double oz = rs.getDouble("oz");
				double dx = rs.getDouble("dx");
				double dy = rs.getDouble("dy");
				double dz = rs.getDouble("dz");
				cO = new Coordinates(ox, oy, oz);
				cD = new Coordinates(dx, dy, dz);
			} else
				throw new ElementNotFoundException("Road or Bay " + roadName + " not found!");
		}
		points.add(cD);
		points.add(0, cO);
		return points;
	}

	/*
	 * private Double getRateOnRoad(String roadID, Coordinates coords) throws
	 * ElementNotFoundException, SQLException{ List<Coordinates> points =
	 * roadsCoordinates(roadID); int index = 0;
	 * 
	 * Coordinates from = points.get(index);
	 * for(index=1;index<points.size();index++){ //TODO 25/03/2013 }
	 * 
	 * }
	 */

	@SuppressWarnings("unused")
	private Double getRoadLength(String roadName) throws ElementNotFoundException, SQLException {
		Double length = null;

		String queryRoads = "SELECT origin.X as ox, origin.Y as oy, origin.Z as oz, destination.X as dx, destination.Y as dy, destination.Z as dz"
				+ " FROM ROADS r INNER JOIN CROSSROADS origin ON r.ORIGIN = origin.NAME AND r.TERMINAL = origin.TERMINAL"
				+ " INNER JOIN CROSSROADS destination ON r.DESTINATION = destination.NAME AND r.TERMINAL = destination.TERMINAL"
				+ " WHERE r.NAME = ? AND r.TERMINAL = ?";
		String queryBays = "SELECT origin.X as ox, origin.Y as oy, origin.Z as oz, destination.X as dx, destination.Y as dy, destination.Z as dz"
				+ " FROM BAYS r INNER JOIN BAYCROSSROADS origin ON r.ORIGIN = origin.NAME AND r.TERMINAL = origin.TERMINAL"
				+ " INNER JOIN BAYCROSSROADS destination ON r.DESTINATION = destination.NAME AND r.TERMINAL = destination.TERMINAL"
				+ " WHERE r.NAME = ? AND r.TERMINAL = ?";
		String queryRoadPoints = "SELECT X as x, Y as y, Z as z FROM ROADPOINTS WHERE ROAD=? AND TERMINAL=? ORDER BY INDEX_IN_ROAD";

		PreparedStatement statement = connection.prepareStatement(queryRoads);
		PreparedStatement statementRoadPoints = connection.prepareStatement(queryRoadPoints);
		statement.setString(1, roadName);
		statement.setString(2, terminalName);
		statementRoadPoints.setString(1, roadName);
		statementRoadPoints.setString(2, terminalName);

		ResultSet rs = statement.executeQuery();
		ResultSet rsRoadPoints = statementRoadPoints.executeQuery();

		Coordinates cO = null;
		Coordinates cD = null;
		List<Coordinates> roadPoints = new ArrayList<>();
		if (rs.next()) {
			double ox = rs.getDouble("ox");
			double oy = rs.getDouble("oy");
			double oz = rs.getDouble("oz");
			double dx = rs.getDouble("dx");
			double dy = rs.getDouble("dy");
			double dz = rs.getDouble("dz");
			cO = new Coordinates(ox, oy, oz);
			cD = new Coordinates(dx, dy, dz);

			while (rsRoadPoints.next()) {
				Double x = rsRoadPoints.getDouble("x");
				Double y = rsRoadPoints.getDouble("y");
				Double z = rsRoadPoints.getDouble("y");
				roadPoints.add(new Coordinates(x, y, z));
			}

		} else {
			// Try with bays ?
			rs.close();
			rsRoadPoints.close();
			statement.close();
			statementRoadPoints.close();

			statement = connection.prepareStatement(queryBays);
			statement.setString(1, roadName);
			statement.setString(2, terminalName);
			if (rs.next()) {
				double ox = rs.getDouble("ox");
				double oy = rs.getDouble("oy");
				double oz = rs.getDouble("oz");
				double dx = rs.getDouble("dx");
				double dy = rs.getDouble("dy");
				double dz = rs.getDouble("dz");
				cO = new Coordinates(ox, oy, oz);
				cD = new Coordinates(dx, dy, dz);
			} else
				throw new ElementNotFoundException("Road or Bay " + roadName + " not found!");
		}
		Coordinates from = cO;
		length = 0.0;
		for (int i = 0; i < roadPoints.size(); i++) {
			Coordinates to = roadPoints.get(i);
			length += Math.sqrt(Math.pow(to.x - from.x, 2f) + Math.pow(to.y - from.y, 2f) + Math.pow(to.z - from.z, 2f));
			from = to;
		}
		length += Math.sqrt(Math.pow(cD.x - from.x, 2f) + Math.pow(cD.y - from.y, 2f) + Math.pow(cD.z - from.z, 2f));
		return length;
	}

	@SuppressWarnings("unused")
	private void roadPoint(Attributes atts) throws SQLException {
		String id = atts.getValue("id");

		double x = 0.0;
		double y = 0.0;
		double z = 0.0;

		if (atts.getIndex("x") >= 0) {
			x = Double.parseDouble(atts.getValue("x"));
			y = Double.parseDouble(atts.getValue("y"));

			if (atts.getIndex("z") >= 0)
				z = Double.parseDouble(atts.getValue("z"));
		}
		String query = "INSERT INTO ROADPOINTS (NAME,TERMINAL,ROAD,INDEX_IN_ROAD,X,Y,Z) VALUES (?,?,?,?,?,?,?)";
		PreparedStatement statement = connection.prepareStatement(query);
		statement.setString(1, id);
		statement.setInt(2, terminalID);
		statement.setString(3, roadName);
		statement.setInt(4, roadPointIndex++);
		statement.setDouble(5, x);
		statement.setDouble(6, y);
		statement.setDouble(7, z);
		executeQuery(statement);
	}

	@SuppressWarnings("unused")
	private void routing(Attributes atts) throws SQLException {
		String type = atts.getValue("type");
		String heuristic = null;
		if (atts.getIndex("heuristic") >= 0)
			heuristic = atts.getValue("heuristic");

		String query = "UPDATE STRADDLECARRIER SET ROUTING_ALGORITHM = ?, ROUTING_HEURISTIC=? WHERE NAME = ? AND SCENARIO = ?";
		PreparedStatement ps = connection.prepareStatement(query);
		ps.setString(1, type);
		if (heuristic != null)
			ps.setString(2, heuristic);
		else
			ps.setNull(2, Types.VARCHAR);
		ps.setString(3, currentStraddleCarrierId);
		ps.setInt(4, scenarioID);
		executeQuery(ps);
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
	// FIXME add a type field over bays to set if it is a classical one or an
	// ExchangeBay
	public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {
		if (!this.comment) {
			if ("comment".equals(localName)) {
				this.comment = true;
			} else {
				// try{
				XMLTags tag = XMLTags.getTag(localName);

				if (tag != null) {
					String method = tag.getMethod();
					if (method != null) {
						try {
							Method m = this.getClass().getDeclaredMethod(method, Attributes.class);
							m.setAccessible(true);
							m.invoke(this, atts);

						} catch (InvocationTargetException e) {
							log.debug("Exception on tag " + tag.getTag() + "@" + tag.getMethod());
							if (e.getCause() instanceof SQLException) {
								log.error("SQLException", (SQLException) (e.getCause()));
							} else {
								log.error("SAXException", e);
								throw new SAXException(e);
							}
						} catch (Exception e) {
							log.error("OTHER Exception", e);
							throw new SAXException(e);
						}
					}
				} else {
					if (!localName.equals("document"))
						log.warn("Unrecognized XML tag : " + localName + "!");
				}
			}
		}

		// switch(localName){
		// case XMLTags.SCENARIO.getTag():
		// scenario(atts);
		// break;
		// case "terminal":
		// terminal(atts);
		// break;
		// case "include":
		// String file = atts.getValue("file");
		// include(file);
		// break;
		// case "crossroad":
		// crossroad(atts);
		// break;
		// case "laneCrossroad":
		// bayCrossroad(atts);
		// break;
		// case "road":
		// road(atts);
		// break;
		// case "lane":
		// lane(atts);
		// break;
		// case "exchangeLane":
		// lane(atts);
		// break;
		// case "roadpoint":
		// roadPoint(atts);
		// break;
		// case "pave":
		// block(atts);
		// break;
		// case DEPOT_TYPE:
		// block(atts);
		// inDepot = true;
		// break;
		// case "coordinate":
		// wallPoint(atts);
		// break;
		// case "wall":
		// wall(atts);
		// break;
		// case "type":
		// straddleCarrierModel(atts);
		// break;
		// case "straddleCarrier":
		// //FIXME remove all distribution properties from simulator in a brand
		// new branch to get better perf results
		// straddleCarrier(atts);
		// break;
		// case "routing":
		// if(currentStraddleCarrierId != null) {
		// routing(atts);
		// }
		// break;
		// case "laserhead":
		// laserHead(atts);
		// break;
		// case "missions":
		// missions(atts);
		// break;
		// case "mission":
		// inMission = true;
		// idMission = atts.getValue("id");
		// missionContainerId = atts.getValue("container");
		// missionKind = Integer.parseInt(atts.getValue("kind"));
		// if(atts.getIndex("truck")>=0){
		// current_event_newMission_truckID = atts.getValue("truck");
		// }
		// break;
		// case "timewindow":
		// //TODO
		// //timeWindow(atts)
		// break;
		// case "containerLocation":
		// containerLocation(atts);
		// break;
		// case "event":
		// //TODO
		// //event(atts);
		// break;
		// case "container":
		// container(atts);
		// break;
		// case "straddleCarrierSlot":
		// try {
		// straddleCarrierSlot(atts);
		// } catch (NotInDepotException e) {
		// e.printStackTrace();
		// }
		// break;
		// case "shipContainerIn":
		// //TODO
		// //shipContainerIn(atts);
		// break;
		// }
		// } catch(SQLException e) {
		// e.printStackTrace();
		// } catch (NotInPaveException e) {
		// e.printStackTrace();
		// } catch (LaneTooSmallException e) {
		// e.printStackTrace();
		// } catch (NotInContainerException e) {
		// e.printStackTrace();
		// }
		// }
		// }
	}

	@SuppressWarnings("unused")
	private void missions(Attributes atts) throws SQLException {
		if (scenarioName == null || !atts.getValue("scenario").equals(scenarioName)) {
			this.scenarioName = atts.getValue("scenario");
			retrieveScenarioID();
		}
		log.info("Parsing missions " + atts.getValue("scenario") + "...");
	}

	public boolean containerExists(String containerID) throws SQLException {
		boolean found = false;
		String query = "SELECT NAME FROM CONTAINER WHERE NAME = ? AND SCENARIO = ?";
		PreparedStatement ps = connection.prepareStatement(query);
		ps.setString(1, containerID);
		ps.setInt(2, scenarioID);
		ResultSet rs = ps.executeQuery();
		if (rs.next()) {
			found = true;
		}
		if (rs != null)
			rs.close();
		if (ps != null)
			ps.close();
		return found;
	}

	private void container(Attributes atts) throws SQLException {
		String id = atts.getValue("id");
		Double teu = null;
		if (inEvent > 0 && "vehicleOut".equals(current_event_type.get(inEvent - 1))) {
			currentEventDescription.add(id);
		} else {
			if (atts.getIndex("teu") >= 0)
				teu = Double.parseDouble(atts.getValue("teu"));
			PreparedStatement ps = null;
			if (containerExists(id)) {
				String query = "UPDATE CONTAINER SET TYPE=? WHERE NAME = ? AND SCENARIO = ?";
				ps = connection.prepareStatement(query);
				if (teu != null)
					ps.setInt(1, DbMgr.getInstance().getDatabaseIDsRetriever().getContainerTypeID(teu));
				else
					ps.setNull(1, Types.INTEGER);
				ps.setString(2, id);
				ps.setInt(3, scenarioID);
			} else {
				String query = "INSERT INTO CONTAINER (NAME, TYPE, SCENARIO) VALUES (?,?,?)";
				ps = connection.prepareStatement(query);
				ps.setString(1, id);
				if (teu != null)
					ps.setInt(2, DbMgr.getInstance().getDatabaseIDsRetriever().getContainerTypeID(teu));
				else
					ps.setNull(2, Types.INTEGER);

				ps.setInt(3, scenarioID);
			}
			executeQuery(ps);
		}
		inContainer = true;
		current_container_id = id;
	}

	private void retrieveTerminalID() throws SQLException {
		String query = "SELECT ID FROM TERMINAL WHERE NAME = ? AND DATE_REC = (SELECT MAX(DATE_REC) FROM TERMINAL WHERE NAME=?)";
		PreparedStatement statement = null;
		ResultSet rs = null;
		statement = connection.prepareStatement(query);
		statement.setString(1, terminalName);
		statement.setString(2, terminalName);
		rs = statement.executeQuery();
		if (rs.next()) {
			this.terminalID = rs.getInt("ID");
		} else {
			log.fatal("Terminal " + terminalName + " not found!");
		}
		rs.close();
		statement.close();
	}

	/*
	 * private void deleteTerminal(String name) throws SQLException{
	 * 
	 * String query = "DELETE FROM TERMINAL WHERE NAME = ?"; PreparedStatement
	 * ps = connection.prepareStatement(query); ps.setString(1, name);
	 * executeQuery(ps); }
	 */

	@SuppressWarnings("unused")
	private void terminal(Attributes atts) throws SQLException {
		this.terminalName = atts.getValue("name");

		String terminalLabel = null;
		if (atts.getIndex("label") >= 0)
			terminalLabel = atts.getValue("label");

		String terminalFile = atts.getValue("file");

		// deleteTerminal(this.terminalName);
		String query = "INSERT INTO TERMINAL (NAME,FILE,LABEL) VALUES (?,?,?)";
		PreparedStatement statement;
		try {
			statement = connection.prepareStatement(query);

			statement.setString(1, terminalName);
			statement.setString(2, terminalFile);
			if (terminalLabel != null)
				statement.setString(3, terminalLabel);
			else
				statement.setNull(3, Types.VARCHAR);

			executeQuery(statement);
		} catch (SQLException e) {
			if (e.getErrorCode() == 1062) {
				log.fatal("Terminal already inserted!", e);
			} else {
				throw e;
			}
		}
		// Retrieve the ID //TODO test if no commit is done that the value can
		// be retrieved
		retrieveTerminalID();
	}

	@SuppressWarnings("unused")
	private void scenario(Attributes atts) throws SQLException {
		this.scenarioName = atts.getValue("name");
		this.terminalName = atts.getValue("terminal");

		String query = "INSERT INTO SCENARIO (NAME, TERMINAL, FILE) VALUES (?,?,?)";
		if (this.terminalID == null)
			retrieveTerminalID();
		try {
			PreparedStatement statement = connection.prepareStatement(query);
			statement.setString(1, this.scenarioName);
			statement.setInt(2, this.terminalID);
			statement.setString(3, atts.getValue("file"));
			executeQuery(statement);
		} catch (SQLException e) {
			if (e.getErrorCode() == 1062) {
				log.fatal("Scenario already inserted!", e);
			} else {
				throw e;
			}
		}
		retrieveScenarioID();
	}

	@SuppressWarnings("unused")
	private void scheduler(Attributes atts) throws SQLException {
		String type = atts.getValue("type");
		switch (type) {
		case OnlineACOScheduler.rmiBindingName:

			// Retrieve parameters
			for (OnlineACOParametersBean parameter : OnlineACOParametersBean.values()) {
				if (atts.getIndex(parameter.name()) > 0) {
					String sValue = atts.getValue(parameter.name());
					try {
						Double dValue = Double.parseDouble(sValue);
						parameter.setValue(dValue);
						OnlineACOParametersDAO.getInstance(simulationID).insert(parameter.getParameter());
					} catch (NumberFormatException e) {
						e.printStackTrace();
						log.error(e.getMessage(), e);
					}
				}
			}
			break;
		case OfflineACOScheduler.rmiBindingName:
			// Retrieve parameters
			for (OfflineACOParametersBean parameter : OfflineACOParametersBean.values()) {
				if (atts.getIndex(parameter.name()) > 0) {
					String sValue = atts.getValue(parameter.name());
					try {
						Double dValue = Double.parseDouble(sValue);
						parameter.setValue(dValue);
						OfflineACOParametersDAO.getInstance(simulationID).insert(parameter.getParameter());
					} catch (NumberFormatException e) {
						e.printStackTrace();
						log.error(e.getMessage(), e);
					}
				}
			}
			break;
		case OfflineACOScheduler2.rmiBindingName:
			// Retrieve parameters
			for (OfflineACO2ParametersBean parameter : OfflineACO2ParametersBean.values()) {
				if (atts.getIndex(parameter.name()) > 0) {
					String sValue = atts.getValue(parameter.name());
					try {
						Double dValue = Double.parseDouble(sValue);
						parameter.setValue(dValue);
						OfflineACO2ParametersDAO.getInstance(simulationID).insert(parameter.getParameter());
					} catch (NumberFormatException e) {
						e.printStackTrace();
						log.error(e.getMessage(), e);
					}
				}
			}
			break;
		case LinearMissionScheduler.rmiBindingName:
			// Retrieve parameters
			for (LinearParametersBean parameter : LinearParametersBean.values()) {
				if (atts.getIndex(parameter.name()) > 0) {
					String sValue = atts.getValue(parameter.name());
					try {
						Double dValue = Double.parseDouble(sValue);
						parameter.setValue(dValue);
						LinearParametersDAO.getInstance(simulationID).insert(parameter.getParameter());
					} catch (NumberFormatException e) {
						e.printStackTrace();
						log.error(e.getMessage(), e);
					}
				}
			}
			break;
		case GreedyMissionScheduler.rmiBindingName:
			// Retrieve parameters
			for (GreedyParametersBean parameter : GreedyParametersBean.values()) {
				if (atts.getIndex(parameter.name()) > 0) {
					String sValue = atts.getValue(parameter.name());
					try {
						Double dValue = Double.parseDouble(sValue);
						parameter.setValue(dValue);
						GreedyParametersDAO.getInstance(simulationID).insert(parameter.getParameter());
					} catch (NumberFormatException e) {
						e.printStackTrace();
						log.error(e.getMessage(), e);
					}
				}
			}
			break;
		case RandomMissionScheduler.rmiBindingName:
			// Retrieve parameters
			for (RandomParametersBean parameter : RandomParametersBean.values()) {
				if (atts.getIndex(parameter.name()) > 0) {
					String sValue = atts.getValue(parameter.name());
					try {
						Double dValue = Double.parseDouble(sValue);
						parameter.setValue(dValue);
						RandomParametersDAO.getInstance(simulationID).insert(parameter.getParameter());
					} catch (NumberFormatException e) {
						e.printStackTrace();
						log.error(e.getMessage(), e);
					}
				}
			}
			break;
		case BranchAndBound.rmiBindingName:
			// Retrieve parameters
			for (BranchAndBoundParametersBean parameter : BranchAndBoundParametersBean.values()) {
				if (atts.getIndex(parameter.name()) > 0) {
					String sValue = atts.getValue(parameter.name());
					try {
						Double dValue = Double.parseDouble(sValue);
						parameter.setValue(dValue);
						BranchAndBoundParametersDAO.getInstance(simulationID).insert(parameter.getParameter());
					} catch (NumberFormatException e) {
						e.printStackTrace();
						log.error(e.getMessage(), e);
					}
				}
			}
			break;
		case BB.rmiBindingName:
			// Retrieve parameters
			for (BBParametersBean parameter : BBParametersBean.values()) {
				if (atts.getIndex(parameter.name()) > 0) {
					String sValue = atts.getValue(parameter.name());
					try {
						Double dValue = Double.parseDouble(sValue);
						parameter.setValue(dValue);
						BBParametersDAO.getInstance(simulationID).insert(parameter.getParameter());
					} catch (NumberFormatException e) {
						e.printStackTrace();
						log.error(e.getMessage(), e);
					}
				}
			}
			break;
		}
	}

	/*
	 * @SuppressWarnings("unused") private void loadScheduler(Attributes atts)
	 * throws SQLException { String type = atts.getValue("type"); switch (type)
	 * { case OnlineACOScheduler.rmiBindingName: // Retrieve parameters
	 * OnlineACOSchedulingParametersDAO.getInstance(simulationID).load();
	 * 
	 * OnlineACOScheduler .setEvalParameters(OnlineACOSchedulingParametersBean
	 * .getEvalParameters()); OnlineACOScheduler
	 * .setGlobalParameters(OnlineACOSchedulingParametersBean
	 * .getACOParameters()); MissionScheduler.rmiBindingName =
	 * OnlineACOScheduler.rmiBindingName; break; case
	 * OfflineACOScheduler.rmiBindingName: // Retrieve parameters
	 * OfflineACOSchedulingParametersDAO.getInstance(simulationID).load();
	 * OfflineACOSchedulingParametersBeanOld offlineACOParametersBean =
	 * OfflineACOSchedulingParametersDAO
	 * .getInstance(simulationID).iterator().next();
	 * OfflineACOScheduler.setEvalParameters(offlineACOParametersBean
	 * .getEvalParameters());
	 * OfflineACOScheduler.setGlobalParameters(offlineACOParametersBean
	 * .getParameters()); MissionScheduler.rmiBindingName =
	 * OfflineACOScheduler.rmiBindingName; break; case
	 * OfflineACOScheduler2.rmiBindingName: // Retrieve parameters
	 * OfflineACOScheduling2ParametersDAO.getInstance(simulationID).load();
	 * OfflineACOScheduling2ParametersBeanOld offlineACO2ParametersBean =
	 * OfflineACOScheduling2ParametersDAO
	 * .getInstance(simulationID).iterator().next();
	 * OfflineACOScheduler2.setEvalParameters(offlineACO2ParametersBean
	 * .getEvalParameters());
	 * OfflineACOScheduler2.setGlobalParameters(offlineACO2ParametersBean
	 * .getParameters()); MissionScheduler.rmiBindingName =
	 * OfflineACOScheduler2.rmiBindingName; break; case
	 * LinearMissionScheduler.rmiBindingName: // Retrieve parameters
	 * LinearSchedulingParametersDAO.getInstance(simulationID).load();
	 * LinearSchedulingParametersBean linearParametersBean =
	 * LinearSchedulingParametersDAO
	 * .getInstance(simulationID).iterator().next();
	 * LinearMissionScheduler.setEvalParameters(linearParametersBean
	 * .getEvalParameters()); MissionScheduler.rmiBindingName =
	 * LinearMissionScheduler.rmiBindingName; break; case
	 * GreedyMissionScheduler.rmiBindingName: // Retrieve parameters
	 * GreedySchedulingParametersDAO.getInstance(simulationID).load();
	 * GreedySchedulingParametersBean greedyParametersBean =
	 * GreedySchedulingParametersDAO
	 * .getInstance(simulationID).iterator().next();
	 * GreedyMissionScheduler.setEvalParameters(greedyParametersBean
	 * .getEvalParameters()); MissionScheduler.rmiBindingName =
	 * GreedyMissionScheduler.rmiBindingName; break; case
	 * RandomMissionScheduler.rmiBindingName: // Retrieve parameters
	 * RandomSchedulingParametersDAO.getInstance(simulationID).load();
	 * RandomSchedulingParametersBean randomParametersBean =
	 * RandomSchedulingParametersDAO
	 * .getInstance(simulationID).iterator().next();
	 * RandomMissionScheduler.setEvalParameters(randomParametersBean
	 * .getEvalParameters()); MissionScheduler.rmiBindingName =
	 * RandomMissionScheduler.rmiBindingName; break; case
	 * BranchAndBound.rmiBindingName: // Retrieve parameters
	 * BranchAndBoundSchedulingParametersDAO.getInstance(simulationID) .load();
	 * BranchAndBoundParametersBean branchAndBoundParametersBean =
	 * BranchAndBoundSchedulingParametersDAO
	 * .getInstance(simulationID).iterator().next();
	 * BranchAndBound.setEvalParameters(branchAndBoundParametersBean
	 * .getEvalParameters()); BranchAndBound.distanceMatrixFile =
	 * branchAndBoundParametersBean .getDistanceMatrixFile();
	 * BranchAndBound.evalCosts = branchAndBoundParametersBean
	 * .isComputeCosts(); BranchAndBound.solutionFile =
	 * branchAndBoundParametersBean .getSolutionFile();
	 * BranchAndBound.solutionInitFile = branchAndBoundParametersBean
	 * .getSolutionInitFile(); BranchAndBound.timeMatrixFile =
	 * branchAndBoundParametersBean .getTimeMatrixFile();
	 * MissionScheduler.rmiBindingName = BranchAndBound.rmiBindingName; break;
	 * case BB.rmiBindingName: // Retrieve parameters
	 * BBSchedulingParametersDAO.getInstance(simulationID).load();
	 * BBParametersBean bbParametersBean = BBSchedulingParametersDAO
	 * .getInstance(simulationID).iterator().next();
	 * BB.setEvalParameters(bbParametersBean.getEvalParameters());
	 * BB.solutionFile = bbParametersBean.getSolutionFile(); BB.solutionInitFile
	 * = bbParametersBean.getSolutionInitFile(); MissionScheduler.rmiBindingName
	 * = BB.rmiBindingName; break; }
	 * 
	 * // Build MissionScheduler MissionScheduler.getInstance(); }
	 */

	private void retrieveScenarioID() throws SQLException {
		String query = "SELECT ID FROM SCENARIO WHERE NAME = ?";
		PreparedStatement statement = null;
		ResultSet rs = null;
		statement = connection.prepareStatement(query);
		statement.setString(1, scenarioName);
		rs = statement.executeQuery();
		if (rs.next()) {
			this.scenarioID = rs.getInt("ID");
		} else {
			log.fatal("Scenario " + scenarioName + " not found!");
		}
		rs.close();
		statement.close();
	}

	@Override
	public void startPrefixMapping(String prefix, String uri) throws SAXException {

	}

	@SuppressWarnings("unused")
	private void block(Attributes atts) throws SQLException {

		String type = atts.getValue("type");
		Integer typeID = DbMgr.getInstance().getDatabaseIDsRetriever().getBlockTypeID(type);

		String name = atts.getValue("id");

		String query = null;
		PreparedStatement statement = null;

		if (type.equals(BlockType.SHIP.toString())) {
			String seaOrientation = atts.getValue("seaOrientation");
			Integer seaOrientationID = DbMgr.getInstance().getDatabaseIDsRetriever().getSeaOrientation(seaOrientation);

			String borderRoad = atts.getValue("borderRoadID");
			query = "INSERT INTO BLOCK (NAME,TERMINAL,TYPE,SEA_ORIENTATION,BORDER_ROAD) VALUES (?,?,?,?,?)";
			statement = connection.prepareStatement(query);
			statement.setString(1, name);
			statement.setInt(2, terminalID);
			statement.setInt(3, typeID);
			statement.setInt(4, seaOrientationID);
			statement.setString(5, borderRoad);
		} else {
			if (type.equals(BlockType.DEPOT.toString())) {
				inDepot = true;
			}
			query = "INSERT INTO BLOCK (NAME,TERMINAL,TYPE) VALUES (?,?,?)";
			statement = connection.prepareStatement(query);
			statement.setString(1, name);
			statement.setInt(2, terminalID);
			statement.setInt(3, typeID);
		}

		executeQuery(statement);
		blockName = name;
	}

	@SuppressWarnings("unused")
	private void straddleCarrier(Attributes atts) throws SQLException {
		String type = atts.getValue("type");
		String slot = atts.getValue("slot");
		String color = atts.getValue("color");
		String id = atts.getValue("id");

		boolean autoHandling = false;
		if (atts.getIndex("handling") >= 0) {
			String s = atts.getValue("handling");
			if (s.equals("man"))
				autoHandling = false;
		}
		String query = "SELECT 1 FROM STRADDLECARRIER WHERE NAME= ? AND SCENARIO = ?";
		PreparedStatement ps = connection.prepareStatement(query);
		ps.setString(1, id);
		ps.setInt(2, scenarioID);
		ResultSet rs = ps.executeQuery();
		if (!rs.next()) {
			rs.close();
			ps.close();
			query = "INSERT INTO STRADDLECARRIER (NAME,SCENARIO,MODEL,COLOR,SLOT,ORIGIN_ROAD,ORIGIN_RATE,ORIGIN_DIRECTION,ORIGIN_AVAILABILITY,AUTOHANDLING) "
					+ "VALUES (?,?,?,?,?,?,?,?,?,?)";
			ps = connection.prepareStatement(query);
			ps.setString(1, id);
			ps.setInt(2, scenarioID);
			ps.setString(3, type);
			ps.setString(4, color);
			ps.setString(5, slot);

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
				ps.setString(6, location);
				ps.setDouble(7, pct);
				ps.setBoolean(8, direction);
			} else {
				ps.setString(6, slot);
				ps.setDouble(7, 0.5);
				ps.setBoolean(8, false);
			}
			ps.setInt(9, 0);
			ps.setBoolean(10, autoHandling);

			executeQuery(ps);

			currentStraddleCarrierId = id;
		} else {
			rs.close();
			ps.close();
		}
	}

	@SuppressWarnings("unused")
	private void straddleCarrierModel(Attributes atts) throws SQLException {
		String id = atts.getValue("id");
		Double width = Double.parseDouble(atts.getValue("width"));
		Double height = Double.parseDouble(atts.getValue("height"));
		Double length = Double.parseDouble(atts.getValue("length"));
		Double innerWidth = Double.parseDouble(atts.getValue("innerWidth"));
		Double innerLength = Double.parseDouble(atts.getValue("innerLength"));
		Double backOverLength = Double.parseDouble(atts.getValue("backOverLength"));
		Double frontOverLength = Double.parseDouble(atts.getValue("frontOverLength"));
		Double cabWidth = Double.parseDouble(atts.getValue("cabWidth"));
		String compatibility = atts.getValue("compatibility");
		Double emptySpeed = Double.parseDouble(atts.getValue("emptySpeed"));
		Double loadedSpeed = Double.parseDouble(atts.getValue("loadedSpeed"));
		Double baySpeed = Double.parseDouble(atts.getValue("baySpeed"));

		Double containerHandlingFromTruck_MIN = Double.parseDouble(atts.getValue("containerHandlingFromTruck_MIN"));
		Double containerHandlingFromTruck_MAX = Double.parseDouble(atts.getValue("containerHandlingFromTruck_MAX"));

		Double containerHandlingFromGround_MIN = Double.parseDouble(atts.getValue("containerHandlingFromGround_MIN"));
		Double containerHandlingFromGround_MAX = Double.parseDouble(atts.getValue("containerHandlingFromGround_MAX"));

		Double enterExitBayTime_MIN = Double.parseDouble(atts.getValue("enterExitBayTime_MIN"));
		Double enterExitBayTime_MAX = Double.parseDouble(atts.getValue("enterExitBayTime_MAX"));

		Double turnBackTime = Double.parseDouble(atts.getValue("turnBackTime"));

		String query = "SELECT 1 FROM STRADDLECARRIER_MODEL WHERE NAME = ?";
		PreparedStatement statement = connection.prepareStatement(query);
		statement.setString(1, id);
		ResultSet rs = statement.executeQuery();
		if (!rs.next()) {
			rs.close();
			statement.close();

			query = "INSERT INTO STRADDLECARRIER_MODEL (NAME,WIDTH,HEIGHT,LENGTH,INNER_WIDTH,INNER_LENGTH,BACK_OVER_LENGTH,FRONT_OVER_LENGTH,CAB_WIDTH,COMPATIBILITY,"
					+ "EMPTY_SPEED,LOADED_SPEED,BAY_SPEED,CONTAINER_HANDLING_FROM_TRUCK_MIN,CONTAINER_HANDLING_FROM_TRUCK_MAX,CONTAINER_HANDLING_FROM_GROUND_MIN,CONTAINER_HANDLING_FROM_GROUND_MAX,"
					+ "ENTER_EXIT_BAY_TIME_MIN,ENTER_EXIT_BAY_TIME_MAX,TURN_BACK_TIME) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
			statement = connection.prepareStatement(query);
			int i = 1;
			statement.setString(i++, id);
			statement.setDouble(i++, width);
			statement.setDouble(i++, height);
			statement.setDouble(i++, length);
			statement.setDouble(i++, innerWidth);
			statement.setDouble(i++, innerLength);
			statement.setDouble(i++, backOverLength);
			statement.setDouble(i++, frontOverLength);
			statement.setDouble(i++, cabWidth);
			statement.setString(i++, compatibility);
			statement.setDouble(i++, emptySpeed);
			statement.setDouble(i++, loadedSpeed);
			statement.setDouble(i++, baySpeed);
			statement.setDouble(i++, containerHandlingFromTruck_MIN);
			statement.setDouble(i++, containerHandlingFromTruck_MAX);
			statement.setDouble(i++, containerHandlingFromGround_MIN);
			statement.setDouble(i++, containerHandlingFromGround_MAX);
			statement.setDouble(i++, enterExitBayTime_MIN);
			statement.setDouble(i++, enterExitBayTime_MAX);
			statement.setDouble(i++, turnBackTime);
			executeQuery(statement);
		} else {
			rs.close();
			statement.close();
		}
	}

	@SuppressWarnings("unused")
	private void straddleCarrierSlot(Attributes atts) throws SQLException, NotInDepotException {
		// SC slot = lane + 1 slot

		String id = atts.getValue("id");
		String depotName;

		/* Useless ? */
		// Double x = Double.parseDouble(atts.getValue("x"));
		// Double y = Double.parseDouble(atts.getValue("y"));
		// Double z = 0.0;
		// if(atts.getIndex("z")>=0){
		// z = Double.parseDouble(atts.getValue("z"));
		// }

		String cOriginId = atts.getValue("origin");
		String cDestinationId = atts.getValue("destination");

		if (inDepot) {
			depotName = this.blockName;
		} else
			throw new NotInDepotException();

		// Road
		String query = "INSERT INTO ROAD (NAME,TERMINAL,TYPE,ORIGIN,DESTINATION,DIRECTED,BLOCK) VALUES (?,?,?,?,?,?,?)";
		PreparedStatement ps = connection.prepareStatement(query);
		ps.setString(1, id);
		ps.setInt(2, terminalID);
		ps.setInt(3, DbMgr.getInstance().getDatabaseIDsRetriever().getRoadTypeID(StraddleCarrierSlot.class.getName())); // TODO
																														// test
		ps.setString(4, cOriginId);
		ps.setString(5, cDestinationId);
		ps.setBoolean(6, false);
		ps.setString(7, depotName);
		executeQuery(ps);

		// Slot
		query = "INSERT INTO SLOT (NAME, TERMINAL, BAY, LEN, RATE) VALUES (?,?,?,?,?)";
		PreparedStatement statement = connection.prepareStatement(query);
		statement.setString(1, id);
		statement.setInt(2, terminalID);
		statement.setString(3, id);
		statement.setInt(4, DbMgr.getInstance().getDatabaseIDsRetriever().getContainerTypeID(2.25));
		statement.setDouble(5, 0.5);
		executeQuery(statement);

	}

	@SuppressWarnings("unused")
	private void timeWindow(Attributes atts) {
		Time start = new Time(atts.getValue("start"));
		Time end = new Time(atts.getValue("end"));
		TimeWindow tw = new TimeWindow(start, end);
		if (inMission) {
			if (twPickup == null) {
				twPickup = tw;
			} else {
				twDelivery = tw;
			}
		}
	}

	@Deprecated
	public void destroy() {
		// TODO
	}

	@SuppressWarnings("unused")
	private void wall(Attributes atts) throws SQLException {
		String from = atts.getValue("from");
		String to = atts.getValue("to");

		String query = "INSERT INTO BLOCK_WALL(BLOCK_NAME, TERMINAL, WALL_POINT_1, WALL_POINT_2) VALUES (?,?,?,?)";
		PreparedStatement statement = connection.prepareStatement(query);

		statement.setString(1, blockName);
		statement.setInt(2, terminalID);
		statement.setString(3, from);
		statement.setString(4, to);
		executeQuery(statement);
	}
}
