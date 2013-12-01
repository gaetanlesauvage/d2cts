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
package org.vehicles;

import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import javax.swing.ImageIcon;

import org.display.GraphicDisplayPanel;
import org.display.TextDisplay;
import org.exceptions.DatabaseNotConfiguredException;
import org.exceptions.MissionNotFoundException;
import org.exceptions.NoPathFoundException;
import org.exceptions.container_stocking.UnreachableContainerException;
import org.exceptions.container_stocking.delivery.UnreachableSlotException;
import org.missions.GoTo;
import org.missions.Load;
import org.missions.Mission;
import org.missions.MissionKinds;
import org.missions.MissionPhase;
import org.missions.MissionState;
import org.missions.Workload;
import org.positioning.Coordinates;
import org.positioning.LaserSystem;
import org.routing.Routing;
import org.routing.path.Path;
import org.routing.path.PathNode;
import org.system.Reservation;
import org.system.Reservations;
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
import org.system.container_stocking.ContainerKind;
import org.system.container_stocking.ContainerLocation;
import org.system.container_stocking.Slot;
import org.time.DiscretObject;
import org.time.Time;
import org.time.TimeScheduler;
import org.time.TimeWindow;
import org.time.event.StraddleCarrierFailure;
import org.util.Location;
import org.util.parsers.XMLStraddleCarrierMessageParser;
import org.vehicles.models.SpeedCharacteristics;
import org.vehicles.models.StraddleCarrierModel;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

/**
 * A straddle carrier is a handling truck able to take one container on a stack
 * and to move it within the terminal.
 * 
 * @author Ga&euml;tan Lesauvage
 * @since 2009
 */
public class StraddleCarrier implements DiscretObject {
	public static final String STRADDLECARRIER_ICON_PREFIX_URL = "/etc/images/sc_";
	public static final String STRADDLECARRIER_ICON_SUFFIX_URL = ".png";

	/**
	 * Database manager of the simulation
	 */
	// FIXME
	// private static DatabaseManager databaseManager;

	private StraddleCarrierSlot slot;
	private StraddleCarrierProblemListenerImpl listener;

	private String id;
	private String color;
	private StraddleCarrierModel model;

	private Time roadStartTime;

	private TextDisplay out;
	private Location currentLocation, futureLocation, currentDestination;
	private XMLReader parser;

	private String handledContainerId = "";

	private int available; // 0 = true //1 = false but can move //2 = false and
	// can't move by itself
	private Load loadToResume;
	private String failureType;
	private Time repairDuration;

	private Workload workload;
	private Load currentLoad, lastLoad;
	private boolean destinationReached;
	private Path currentPath;
	private Time handlingTimeEnd;
	private Time overDoneTime;

	private double waitTime;
	private boolean turnBack;

	private ArrayList<GoTo> goToList;

	private Routing routing;

	private double secByStep = -1;
	private XMLStraddleCarrierMessageParser straddleMessageParser;

	private boolean autoHandling;
	private boolean handledContainerChanged;
	private boolean checkedOnce;
	private boolean recomputeDestination = false;

	public StraddleCarrier(String id, StraddleCarrierSlot slot, String model, String color, boolean autoHandling) {
		this.slot = slot;
		this.color = color;
		this.autoHandling = autoHandling;
		this.currentLocation = new Location(slot, 0.5, false);

		this.id = id;

		this.model = Terminal.getInstance().getStraddleCarrierModel(model);

		Terminal.getInstance().addStraddleCarrier(this);

		try {
			parser = XMLReaderFactory.createXMLReader("com.sun.org.apache.xerces.internal.parsers.SAXParser");
			straddleMessageParser = new XMLStraddleCarrierMessageParser(this);
			parser.setContentHandler(straddleMessageParser);
		} catch (SAXException e) {
			e.printStackTrace();
		}

		TimeScheduler.getInstance().recordDiscretObject(this);
		waitTime = 0;

		goToList = new ArrayList<GoTo>();
		listener = new StraddleCarrierProblemListenerImpl();

		available = 0;
		destinationReached = true;
		turnBack = false;
		handledContainerChanged = false;

		// FIXME
		/*
		 * if (databaseManager == null) { databaseManager =
		 * DatabaseManager.getInstance(); }
		 * 
		 * if (databaseManager.isReplay()) { this.autoHandling = true;
		 * readLocationInDB(); }
		 */

		if (!this.autoHandling)
			writeLocationInDB();
		workload = new Workload(this);
	}

	public StraddleCarrier(String id, StraddleCarrierSlot slot, String model, String color, Location l, boolean autoHandling) {
		this.autoHandling = autoHandling;
		this.slot = slot;
		this.color = color;
		this.currentLocation = l;
		this.id = id;

		this.model = Terminal.getInstance().getStraddleCarrierModel(model);

		Terminal.getInstance().addStraddleCarrier(this);

		try {
			parser = XMLReaderFactory.createXMLReader("com.sun.org.apache.xerces.internal.parsers.SAXParser");
			straddleMessageParser = new XMLStraddleCarrierMessageParser(this);
			parser.setContentHandler(straddleMessageParser);
		} catch (SAXException e) {
			e.printStackTrace();
		}

		TimeScheduler.getInstance().recordDiscretObject(this);

		waitTime = 0;
		listener = new StraddleCarrierProblemListenerImpl();
		goToList = new ArrayList<GoTo>();

		available = 0;
		destinationReached = true;
		turnBack = false;
		handledContainerChanged = false;

		// FIXME
		/*
		 * if (databaseManager == null) { databaseManager =
		 * DatabaseManager.getInstance(); } if (databaseManager != null &&
		 * databaseManager.isReplay()) { this.autoHandling = true;
		 * readLocationInDB(); }
		 */

		if (!this.autoHandling)
			writeLocationInDB();
		workload = new Workload(this);
	}

	private void abortCurrentMission() {
		workload.getCurrentLoad().abort();
		try {
			workload.remove(currentLoad.getMission().getId());
		} catch (MissionNotFoundException e) {
			e.printStackTrace();
		}
	}

	public void removeMissionInWorkload(String mID) throws MissionNotFoundException {
		if (workload == null) {
			throw new MissionNotFoundException(mID);
		} else {
			Load l = workload.getLoad(mID);
			if (l == null)
				throw new MissionNotFoundException(mID);
			if (l.getState() == MissionState.STATE_TODO/* Load.STATE_CURRENT */) {
				// System.out.println(getId()+" REMOVE "+mID+" @"+terminal.getTime());
				workload.remove(mID);
			}
			// else
			// System.err.println("CURRENT OR ACHIEVED MISSION "+mID+" REMOVED FROM "+id+"'S WORKLOAD");
			// System.out.println("Mission "+mID+" Removed!");
			// System.out.println(id+"'s workload : \n"+workload);
		}
	}

	public void addMissionsInWorkload(List<Mission> list) {
		if (list.size() > 0) {
			Mission link = null;
			if (lastLoad != null)
				link = lastLoad.getMission();

			Mission previous = link;
			for (int i = 0; i < list.size(); i++) {
				Mission current = list.get(i);
				if (!workload.contains(current.getId())) {
					workload.insert(current, previous);
				}
				previous = current;
			}
		}
	}

	public void addMissionInWorkload(Mission m) {
		if (!workload.contains(m.getId())) {
			workload.insert(m);
		} else {
			Load l = workload.getLoad(m.getId());
			Terminal.getInstance().missionAffected(l, id);
		}

	}

	public boolean apply() {
		boolean returnCode = NOTHING_CHANGED;
		if (!autoHandling) {
			if (futureLocation.getPourcent() != currentLocation.getPourcent()
					|| !futureLocation.getRoad().getId().equals(currentLocation.getRoad().getId()) || handledContainerChanged) {
				writeLocationInDB();
				if (handledContainerChanged)
					handledContainerChanged = false;
			}
		}
		if(currentLoad != null || !workload.isEmpty() || !goToList.isEmpty()){
			returnCode = SOMETHING_CHANGED;
		} //else {
//			System.err.println("nothing changed for "+getId()+" at "+TimeScheduler.getInstance().getTime());
//		}
		currentLocation = futureLocation;
		return returnCode;
	}

	// FIXME
	private void writeLocationInDB() {
		/*
		 * if (databaseManager != null) { String contID = null; if
		 * (!getHandledContainerId().equals("")) contID = handledContainerId;
		 * databaseManager.writeLocation(id,
		 * TimeScheduler.getInstance().getTime().toString(), currentLocation,
		 * contID, currentLocation.getDirection() ? currentLocation.getRoad()
		 * .getDestinationId() : currentLocation.getRoad() .getOriginId()); }
		 */
	}

	private void readLocationInDB() {
		// FIX%E
		/*
		 * Time t = TimeScheduler.getInstance().getTime();
		 * 
		 * String[] datas = databaseManager.readLocation(id, t.toString()); if
		 * (datas != null) { String rID = datas[0]; String direction = datas[1];
		 * String rate = datas[2]; boolean dir = true; if
		 * (direction.equals("false")) dir = false; Location l = new
		 * Location(terminal.getRoad(rID), Double.parseDouble(rate), dir); if
		 * (!datas[3].equals("NA")) { String contID = datas[3]; if
		 * (!contID.equals(handledContainerId)) { setHandledContainerId(contID);
		 * } } else if (!handledContainerId.equals("")) {
		 * setHandledContainerId(""); } futureLocation = l; } else
		 * futureLocation = currentLocation;
		 */
	}

	public void changePath() {
		// On dereserve !
		// boolean unreservation =
		// terminal.unreserve(currentLocation.getRoad().getId(), this.id);
		// if(!unreservation)
		// System.err.println("Unreservation not found = "+unreservation+" for  "+currentLocation.getRoad().getId()+" "+this.id);
		// else
		// System.err.println("Unreservation = "+unreservation+" for  "+currentLocation.getRoad().getId()+" "+this.id);
		if (currentPath != null && currentPath.size() > 0) {

			for (int i = 0; i < currentPath.size(); i++) {
				PathNode pn = currentPath.get(i);
				if (pn.getTimeWindow() != null)
					Terminal.getInstance().unreserve(pn.getLocation().getRoad().getId(), this.getId(), pn.getTimeWindow());
			}
			// terminal.unreserve(currentLocation.getRoad().getId(), this.id);
		}
		if (currentDestination != null) {
			Routing rr = getRouting();
			try {

				Path newPath = rr.getShortestPath(currentLocation, currentDestination, TimeScheduler.getInstance().getTime());
				boolean b = newPath.reserve();
				if (!b) {
					System.out.println(roadStartTime + "> CAN'T RESERVE PATH : \n" + newPath);
					new Exception().printStackTrace();
					//Try to recompute
					newPath = rr.getShortestPath(currentLocation, currentDestination, TimeScheduler.getInstance().getTime());
					newPath.reserve();
				} else {
					currentPath = newPath;
					roadStartTime = TimeScheduler.getInstance().getTime();
					double waitTimeBefore = waitTime;
					boolean directionBefore = currentLocation.getDirection();

					Location l2;
					if (currentPath.size() > 0) {
						l2 = currentPath.peek().getLocation();
					} else
						l2 = currentDestination;

					if (currentPath.size() == 1) {
						if (currentLocation.getRoad().isDirected() == false) {
							if (currentLocation.getDirection() == l2.getDirection()) {

								// Demi tour
								waitTime += model.getSpeedCharacteristics().getTurnBackTime();
								turnBack = true;
								currentLocation.setDirection(!l2.getDirection());
							}
							/*
							 * else{ System.out.println("In good direction !");
							 * }
							 */

						}
					} else {

						if (currentLocation.getRoad().getId().equals(l2.getRoad().getId())) {
							if (currentLocation.getRoad().isDirected() == false) {
								if (currentLocation.getDirection() != l2.getDirection()) {

									// Demi tour
									waitTime += model.getSpeedCharacteristics().getTurnBackTime();
									turnBack = true;
									currentLocation.setDirection(l2.getDirection());
								}

							}

						}
					}
					if (currentPath.getCost() == Double.POSITIVE_INFINITY) {
						// DO SOMETHING !!!
						waitTime = waitTimeBefore;
						currentLocation.setDirection(directionBefore);
					}
				}
			} catch (NoPathFoundException e) {
				e.printStackTrace();
			}

		}
	}

	public void fail(String failureType, Time repairDuration) {
		this.failureType = failureType;
		this.repairDuration = repairDuration;
		System.err.println(TimeScheduler.getInstance().getTime() + "> " + id + " failure! type=" + failureType);
		if (failureType.equals("move") || failureType.equals("both")) {
			// Mod speed with towing speed
			available = 2;
			try {
				waitTime += getRouting().getShortestPath(new Location(slot, 0.5), currentLocation, TimeScheduler.getInstance().getTime()).getCost();
				// System.err.println("STRADDLECARRIER : "+new
				// Time(waitTime+"s")+" to wait for the tailing truck!");
			} catch (NoPathFoundException e) {
				e.printStackTrace();
			}
		} else
			available = 1;

		if (failureType.equals("move")) {
			// Mission handling
			try {
				workload.remove(currentLoad.getMission().getId());
			} catch (MissionNotFoundException e) {
				e.printStackTrace();
			}
			System.err.println(TimeScheduler.getInstance().getTime() + "> " + id + " : load removed of workload!");
			currentLoad.reschedule();
			System.err.println(TimeScheduler.getInstance().getTime() + "> " + id + " : mission rescheduled!");
			currentLoad = null;
		} else {
			loadToResume = new Load(currentLoad);
			currentLoad = null;
		}

		checkedOnce = false;
		System.err.println(TimeScheduler.getInstance().getTime() + "> " + id + " : available = " + available);

		// if handling a container
		if (!handledContainerId.equals("")) {
			System.err.println(TimeScheduler.getInstance().getTime() + "> " + id + " : handling a container (" + handledContainerId + ")!");
			// If can't use the spreader
			if (!failureType.equals("spreader") && !failureType.equals("both")) {
				System.err.println(TimeScheduler.getInstance().getTime() + "> " + id + " : can use its spreader!");
				// can use the spreader
				// Drop container
				Container load = Terminal.getInstance().getContainer(handledContainerId);
				List<String> alreadyTried = new ArrayList<String>();

				Bay l = Terminal.getInstance().findClosestLane(currentLocation, alreadyTried);

				Slot closest = null;
				int level = -1;
				ContainerAlignment alignment = null;

				while (closest == null) {
					List<Slot> slots = Terminal.getInstance().getExitsSlots(l.getId());

					List<Slot> sortedSlots = new ArrayList<Slot>(slots.size());
					List<Double> sortedDistances = new ArrayList<Double>(slots.size());
					// Sort slots
					for (int i = 0; i < slots.size(); i++) {
						Slot s = slots.get(i);
						if (s.getTEU() >= load.getTEU() && Terminal.getInstance().getBlock(s.getPaveId()).getType() == BlockType.YARD) {
							double d = Location.getLength(currentLocation.getCoords(), s.getLocation().getCoords());
							int index = 0;
							while (index < sortedDistances.size() && sortedDistances.get(index) < d) {
								index++;
							}
							sortedDistances.add(index, d);
							sortedSlots.add(index, s);
						}
					}

					// Choose slot
					for (Slot s : sortedSlots) {
						for (int i = 0; i < s.getLevels().size() + 1 && closest == null; i++) {
							for (ContainerAlignment align : ContainerAlignment.values()) {
								if (s.canAddContainer(load, i, align.getValue())) {
									closest = s;
									level = i;
									alignment = align;
									break;
								}
							}
						}
						if (closest != null)
							break;
					}

					if (closest == null) {
						// Choose new lane and retry
						alreadyTried.add(l.getId());
						l = Terminal.getInstance().findClosestLane(currentLocation, alreadyTried);
						if (l == null)
							break;
					}
				}

				if (closest == null) {
					// NO lane containing an available slot found !
					// SO go to the depot with container !
					System.err.println(TimeScheduler.getInstance().getTime() + "> " + id + " : no compatible slot found!");
					// TODO choose between the found slot or the depot + repair
					// time (currentLocation->slot <
					// (currentLocation->depot+repairTime))
				} else {

					ContainerLocation cl = new ContainerLocation(handledContainerId, closest.getPaveId(), closest.getLocation().getRoad().getId(),
							closest.getId(), level, alignment.getValue());
					System.err.println(TimeScheduler.getInstance().getTime() + "> " + id + " : handled container will be dropped at " + cl);
					Mission dropMission = new Mission("drop" + handledContainerId, MissionKinds.STAY.getIntValue(), new TimeWindow(TimeScheduler
							.getInstance().getTime(), TimeScheduler.getInstance().getTime()), new TimeWindow(TimeScheduler.getInstance().getTime(),
							TimeScheduler.getInstance().getTime()), handledContainerId, cl);
					addMissionInWorkload(dropMission);
				}
			}
		}
	}

	public String getColor() {
		return color;
	}

	public String getCSSStyle() {
		// With scala
		// String style =
		// "shape: box; fill-mode: plain; size-mode: normal; size: "+model.getLength()+"gu,"+model.getWidth()+"gu; fill-color: "+color+";";

		// With viewer
		// String style =
		// "sprite-shape: image; sprite-orientation: origin; width:"+this.model.getWidth()+"gu;height:"+this.model.getHeight()+"gu;border-width: 1gu; border-color: black;z-index: 100;";

		String imageFileName = getImageURL();
		String style = "shape: box; fill-mode: image-scaled; fill-image: url('" + Terminal.IMAGE_FOLDER.substring(1) + "sc_" + imageFileName
				+ "'); sprite-orientation: " + (currentLocation.getDirection() ? "to" : "from") + "; size: " + this.model.getLength() + "gu, "
				+ this.model.getWidth() + "gu; z-index: 100;";
		return style;
	}

	public Road getDestination() {
		return currentDestination.getRoad();
	}

	public void setHandledContainerId(String contID) {
		this.handledContainerId = contID;
		handledContainerChanged = true;
	}

	public String getHandledContainerId() {
		return handledContainerId;
	}

	public String getId() {
		return id;
	}

	public StraddleCarrierProblemListener getListener() {
		return listener;
	}

	public Location getLocation() {
		return currentLocation;
	}

	public StraddleCarrierModel getModel() {
		return model;
	}

	public TextDisplay getTextDisplay() {
		return out;
	}

	public Routing getRouting() {
		return routing;
	}

	public void setRoutingAlgorithm(Routing routing) {
		this.routing = routing;
	}

	public String getSchedulingCSSStyle() {
		String style = "shape: circle; fill-mode: plain; size-mode: normal; size: 20px; fill-color: " + color + "; visibility-mode: hidden;";
		return style;
	}

	public StraddleCarrierSlot getSlot() {
		return slot;
	}

	public Workload getWorkload() {
		return workload;
	}

	public void goTo(Location location, boolean interruptible) {
		GoTo goTo = new GoTo(location, interruptible);
		// System.out.println("NEW GOTO : "+goTo.getTarget()+" "+goTo.isInterruptible());
		goToList.add(goTo);
		destinationReached = false;
		if (!interruptible) {
			System.out.println("GOTO : " + goTo.getTarget());
			setDestination(goTo.getTarget());
		} else if (goToList.size() == 1) {
			setDestination(goTo.getTarget());
		}
	}

	private boolean hasReachedLocation(Location currentLocation, Location target) {
		return currentLocation.hasReachedRate(target.getPourcent());
	}

	public void messageReceived(String xmlMessage) {
		// System.out.println(id+":> Message recieved : "+xmlMessage);
		File f = new File("tmp" + id + ".xml");
		PrintWriter pw;
		try {
			pw = new PrintWriter(f);
			pw.append(xmlMessage);
			pw.flush();
			pw.close();
			parser.parse(f.getAbsolutePath());
			f.delete();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public void getOut() {
		System.out.println("Here -1 " + id);
		new Exception().printStackTrace();
		Terminal.getInstance().straddleCarrierActivityChanged("IDLE", this.id);
		Bay l = (Bay) currentLocation.getRoad();
		System.out.println("Here");
		BayCrossroad c = null;
		if (currentLocation.getDirection()) {
			c = (BayCrossroad) l.getDestination();
			System.out.println("Here 2");
		} else {
			c = (BayCrossroad) l.getOrigin();
			System.out.println("Here 3");
		}
		Road r = Terminal.getInstance().getRoad(c.getMainRoad());
		System.out.println("Here 4");
		Location destination = new Location(r, Location.getPourcent(c.getLocation(), r), currentLocation.getDirection());
		System.out.println("Here 5");
		goTo(destination, true);
		System.out.println("Here 6");
	}

	public void gotoDepot() {
		if (!workload.isEmpty()) {
			Terminal.getInstance().straddleCarrierActivityChanged(Mission.DEPOT, this.id);
			Location destination = new Location(slot, 0.5);
			goTo(destination, true);
		}
		// else getOut();
	}

	public void abortCurrentLoad() {
		try {
			workload.remove(currentLoad.getMission().getId());
		} catch (MissionNotFoundException e) {
			e.printStackTrace();
		}
		currentLoad.abort();
		currentLoad = null;
	}

	private void missionHandling() {
		if (workload == null)
			workload = new Workload(this);
		if (available > 0) {
			if (!checkedOnce) {
				checkedOnce = true;
				if (currentLoad != null && currentLoad.getState() == MissionState.STATE_ACHIEVED) {
					lastLoad = currentLoad;
					currentLoad = null;
					// GO BACK TO DEPOT
					if (!currentLocation.getRoad().getId().equals(slot.getId())) {
						// GO TO DEPOT
						gotoDepot();
						System.out.println("Here 7");
					}
				} else {
					Load l = workload.checkLoad(TimeScheduler.getInstance().getTime());
					if (l != null && l.getMission().getId().startsWith("drop")) {
						try {
							workload.startMission(l.getMission().getId());
						} catch (MissionNotFoundException e) {
							e.printStackTrace();
						}
						l.nextPhase();
						l.nextPhase();
						currentLoad = l;
						lastLoad = l;
						Terminal.getInstance().straddleCarrierActivityChanged(l.getMission().getId(), this.id);

						destinationReached = false;
						setDestination(l.getMission().getDestination());
						System.err.println(TimeScheduler.getInstance().getTime() + "> DROP CONTAINER MISSION STARTED!");
					} else {
						gotoDepot();
						System.out.println("Here 8");
					}
				}
			}
		} else {
			if (currentLoad != null && currentLoad.getState() == MissionState.STATE_ACHIEVED) {
				currentLoad = null;
				// GO BACK TO DEPOT
				if (!currentLocation.getRoad().getId().equals(slot.getId())) {
					// GO TO DEPOT
					gotoDepot();
					// System.out.println("Here 9");
				}
			} else if (currentLoad == null) {
				Time t = TimeScheduler.getInstance().getTime();
				Load l = workload.checkLoad(t);
				if (l != null) {
					// Tell the 3D View that the mission has been started
					// writeTo3DView(t, "missionStarted",
					// l.getMission().getId(), id);

					// <event time="" type="missionStarted" missionId=""
					// straddleCarrierId=""/>
					Mission m = l.getMission();
					// if container is not already in the terminal
					if (m.getContainer() != null) {
						currentLoad = l;
						lastLoad = l;

						Terminal.getInstance().straddleCarrierActivityChanged(m.getId(), this.id);
						Terminal.getInstance().missionAffected(currentLoad, this.id);
						destinationReached = false;
						setDestination(m.getContainer().getLocation());
						if (currentLocation.getRoad().getId().equals(m.getContainer().getLocation().getRoad().getId())) {
							if (m.getPickupTimeWindow().getMin().toStep() < t.toStep()) {
								// Get out and wait
								new Exception(id + " should get out and wait !!!").printStackTrace();

							}
						}
						try {
							workload.startMission(l.getMission().getId());
						} catch (MissionNotFoundException e) {
							e.printStackTrace();
						}
					}
				} else {
					// GO BACK TO DEPOT

					if (!currentLocation.getRoad().getId().equals(slot.getId()) && (goToList == null || goToList.size() == 0)) {
						// GO TO DEPOT
						gotoDepot();
						// System.out.println("Here 10");
					}
				}
			}
		}
	}

	/*
	 * private void writeTo3DView(Time t, String type, String missionId, String
	 * straddleCarrierId) { String line =
	 * "<event time='"+t+"' type='"+type+"' missionId='"
	 * +missionId+"' straddleCarrierId='"+straddleCarrierId+"'/>\n";
	 * pwCom3DView.write(line); pwCom3DView.flush(); }
	 */

	public void modMission(String modMissionId, MissionPhase modMissionPhase, ContainerLocation contLocation) {
		System.out.println("Mod mission : " + modMissionId + " phase = " + modMissionPhase + " time = " + this.waitTime);
		try {
			Load l = workload.remove(modMissionId);

			l.setPhase(modMissionPhase);
			if (modMissionPhase == MissionPhase.PHASE_DELIVERY) {
				l.getMission().setDestination(contLocation);
				Terminal t = Terminal.getInstance();
				t.addMission(l.getMission());
				t.missionChanged(l);
				workload.insert(l);

			}
		} catch (MissionNotFoundException e) {
			e.printStackTrace();
		}
		// Slot slot =
		// terminal.getSlot(l.getMission().getDestination().getSlotId());
		/*
		 * if(slot.isReady()){ //VEHICLE IS ALREADY HERE
		 * slot.setVehicleOn(false); terminal.getListener().slotUpdated(slot);
		 * 
		 * }
		 */
		Terminal.getInstance().modMission(modMissionId, modMissionPhase, contLocation);
		if (currentLoad != null && currentLoad.getMission().getId().equals(modMissionId)) {
			currentLoad = workload.getCurrentLoad();
			setDestination(currentLoad.getMission().getDestination());
			destinationReached = false;
		}
		// System.out.println("Mission "+modMissionId+" modifyied : "+l+" "+l.getPhase()+" "+l.getState()+" mission : "+currentLoad.getMission());
	}

	public void modMission(String modMissionId, MissionPhase modMissionPhase, Time waitTime) {
		System.out.println("Mod mission : " + modMissionId + " phase = " + modMissionPhase + " time = " + this.waitTime);
		try {
			Load l = workload.remove(modMissionId);
			l.setPhase(modMissionPhase);
			if (modMissionPhase == MissionPhase.PHASE_DELIVERY) {
				TimeWindow delTW = l.getMission().getDeliveryTimeWindow();
				Time min = new Time(TimeScheduler.getInstance().getTime(), waitTime);
				Time max = delTW.getMax();
				if (min.toStep() > max.toStep())
					max = new Time(max, waitTime);
				delTW = new TimeWindow(min, max);

				l.getMission().setDeliveryTimeWindow(delTW);
				Terminal t = Terminal.getInstance();
				t.addMission(l.getMission());
				// terminal.missionStatusChanged(l);
				t.missionChanged(l);
				workload.insert(l);
			}
			if (currentLoad != null && currentLoad.getMission().getId().equals(modMissionId)) {
				System.out.println("CurrentLoad updated ! " + l + " " + l.getPhase());
				currentLoad = workload.getCurrentLoad();
			}
		} catch (MissionNotFoundException e) {
			e.printStackTrace();
		}

		// System.out.println("Mission "+modMissionId+" modifyied : "+l+" "+l.getPhase()+" "+l.getState()+" mission : "+currentLoad.getMission());

	}

	private void move() {

		if (secByStep == -1)
			secByStep = TimeScheduler.getInstance().getSecondsPerStep();
		double toDoInS = secByStep;
		if (overDoneTime != null) {
			toDoInS += overDoneTime.getInSec();
			overDoneTime = null;
		}

		// System.out.println("--------  NEXTMOVE  ---------------");
		Location after = move(toDoInS, currentLocation);

		futureLocation = after;

		// System.out.println("futureLocation = "+futureLocation);

	}

	/**
	 * 
	 * @param timeOfMoveInS
	 *            Remaining time for moving
	 * @param l
	 *            : location of the straddleCarrier, l.getDirection() == true ->
	 *            drive, l.getDirection() == false -> reverse
	 * @return new location of the straddleCarrier after this move
	 */
	private Location move(double timeOfMoveInS, Location l) {
		Location tmpFutureLocation = l;
		// Time tNow = TimeScheduler.getInstance().getTime();
		// Time tNextStep = new Time(tNow.toStep()+1);
		if (waitTime == 0) {
			if ((l.getPourcent() == 0 || l.getPourcent() == 1.0) && l.getRoad() instanceof Bay
					&& !Terminal.getInstance().isLaneDrivable(l.getRoad().getId())) {
				return l;
			}

			if (turnBack)
				turnBack = false;
			if (timeOfMoveInS > 0.0) {
				double speed = model.getSpeed((available < 2), !handledContainerId.equals(""), l.getRoad() instanceof Bay);

				double distanceOfThisMove = speed * (timeOfMoveInS);
				double oneMoveRate = distanceOfThisMove / l.getRoad().getLength();
				if (l.getRoad().getId().equals(currentDestination.getRoad().getId())/*
																					 * &&
																					 * currentPath
																					 * .
																					 * size
																					 * (
																					 * )
																					 * ==
																					 * 0
																					 */) {
					// Sur la bonne route
					// System.out.println("MOVE : "+timeOfMoveInS+" Location= "+l+" Target : "+currentDestination);
					// System.out.println("Final road reached !");
					Location destination = currentDestination;
					if (hasReachedLocation(l, destination) || willReachLocation(l, destination, oneMoveRate)) {
						// System.out.println(TimeScheduler.getInstance().getTime()+"> "+id+" : Destination reached !");
						destinationReached = true;
						// On deduis le tps de deplacement inutilement compte
						double overDoneRate = Math.abs(l.getPourcent() - currentDestination.getPourcent());
						double overDoneLength = overDoneRate * l.getRoad().getLength();
						double inS = overDoneLength / speed;
						overDoneTime = new Time(inS);
						// On se place exactement a l'endroit indique
						tmpFutureLocation = new Location(destination.getRoad(), destination.getPourcent(), l.getDirection());

						// Time tNow = TimeScheduler.getInstance().getTime();
						// Time gap = new Time(tNow,roadStartTime,false);
						// System.out.println(id+" took "+tmpFutureLocation.getRoad().getId()+" for "+gap+" overDoneTime = "+overDoneTime);
					} else {
						// On doit encore avancer
						double goRate = oneMoveRate;
						if (l.getPourcent() > currentDestination.getPourcent()) {
							// if(!l.getDirection()) {
							// Sens inverse
							goRate = -oneMoveRate;
						}
						double goalRate = LaserSystem.getInstance().getCollisionRate(id, l, l.getPourcent() + goRate);
						if (goalRate != l.getPourcent() + goRate && goalRate > 0) {
							System.out.println("2) Straddle carrier " + id + " has been blocked at " + goalRate + " ! Traffic : ");
							for (String traffic : l.getRoad().getTraffic()) {
								System.out.println("\t" + traffic);
							}
							// Goto the link between the road and the lane
							/*
							 * if(goRate>=0){ Lane dest = (Lane)
							 * currentLocation.getRoad(); LaneCrossroad lc =
							 * (LaneCrossroad)dest.getOrigin(); Road r =
							 * terminal.getRoad(lc.getMainRoad()); double rate =
							 * Location.getPourcent(lc.getLocation(), r);
							 * goTo(new Location(r, rate, true), false); } else{
							 * Lane dest = (Lane) currentLocation.getRoad();
							 * LaneCrossroad lc =
							 * (LaneCrossroad)dest.getDestination(); Road r =
							 * terminal.getRoad(lc.getMainRoad()); double rate =
							 * Location.getPourcent(lc.getLocation(), r);
							 * goTo(new Location(r, rate, true), false); }
							 */
							new java.util.Scanner(System.in).nextLine();
						}
						tmpFutureLocation = Location.moveFromOf(l, goalRate - l.getPourcent());
					}
				} else {
					double currentRate = l.getPourcent();
					if (currentPath == null || currentPath.size() == 0) {
						changePath();
						return move(timeOfMoveInS, l);
					}
					Location target = currentPath.peek().getLocation();
					// System.out.println("MOVE : "+timeOfMoveInS+" Location= "+l+" Target : "+target);
					double goal = target.getPourcent();
					boolean endReached = false;
					double goRate = oneMoveRate;

					if (target.getDirection()) {
						if (currentRate + goRate >= goal) {
							endReached = true;
						}
					} else {
						// Sens inverse
						goRate = -oneMoveRate;
						if (currentRate + goRate <= goal) {
							endReached = true;
						}
					}
					double goalRate = LaserSystem.getInstance().getCollisionRate(id, l, currentRate + goRate);
					if (goalRate != currentRate + goRate && goalRate > 0) {
						System.out.println("Straddle carrier " + id + " has been blocked at " + goalRate + " ! Traffic : ");
						for (String traffic : l.getRoad().getTraffic()) {
							System.out.println("\t" + traffic);
						}
						// Goto the link between the road and the lane
						if (goRate > 0) {
							Bay dest = (Bay) currentLocation.getRoad();
							BayCrossroad lc = (BayCrossroad) dest.getOrigin();
							Road r = Terminal.getInstance().getRoad(lc.getMainRoad());
							double rate = Location.getPourcent(lc.getLocation(), r);
							goTo(new Location(r, rate, true), false);
						} else {
							Bay dest = (Bay) currentLocation.getRoad();
							BayCrossroad lc = (BayCrossroad) dest.getDestination();
							Road r = Terminal.getInstance().getRoad(lc.getMainRoad());
							double rate = Location.getPourcent(lc.getLocation(), r);
							goTo(new Location(r, rate, true), false);
						}

						new java.util.Scanner(System.in).nextLine();
						endReached = false;
					}
					double percent = goalRate;
					// Est-ce qu'on a atteind la fin de la route ?
					if (endReached) {
						Location poll = currentPath.peek().getLocation();
						// POP Path cleanly
						PathNode pn = pop();

						// Location = target
						double overLength = poll.getLength(Math.abs(percent - poll.getPourcent()));
						double inS = (overLength / speed);
						if (currentPath.size() > 0) {
							Location next = currentPath.peek().getLocation();
							// boolean isOpen =
							// terminal.willBeOpen(next.getRoad().getId(), new
							// Time(TimeScheduler.getInstance().getTime().toStep()+1));
							boolean isOpen = next.getRoad() instanceof Bay ? Terminal.getInstance().isLaneDrivable(next.getRoad().getId()) : true;
							// boolean isOpen = next.getRoad().isOpenFor(this,
							// tNow);
							// if(/*id.equals("cav4")&&*/next.getRoad().getId().equals("F-7/17"))
							// System.out.println(next.getRoad().getId()+"  open at "+tNow+" for "+id+" ? = "+isOpen+" next.reservations = "+next.getRoad().getReservationsTable());
							if (!isOpen) {
								turnBack = false;
								waitTime = Math.max(TimeScheduler.getInstance().getSecondsPerStep(), waitTime);

								if (currentLoad != null && currentLoad.getPhase() == MissionPhase.PHASE_DELIVERY
										&& currentLoad.getMission().getDestination().getLaneId().equals(next.getRoad().getId())) {
									Time nextTime = new Time(TimeScheduler.getInstance().getTime(), new Time(waitTime));
									// Slot sDest =
									// terminal.getSlot(currentLoad.getMission().getDestination().getSlotId());
									Location dest = currentPath.get(currentPath.size() - 1).getLocation();

									double travelTime;
									try {
										// travelTime =
										// getRouting().getShortestPath(poll,
										// dest,
										// nextTime).getCost()-currentLoad.getMission().getContainer().getHandlingTime().getInSec();
										travelTime = getRouting().getShortestPath(poll, dest, nextTime).getCost();// -getMaxContainerHandlingTime((Lane)dest.getRoad()).getInSec();

										Time deliveryTime = new Time(nextTime, new Time(travelTime));
										if (deliveryTime.toStep() < currentLoad.getMission().getDeliveryTimeWindow().getMin().toStep()) {
											double toWait = new Time(currentLoad.getMission().getDeliveryTimeWindow().getMin().toStep()
													- deliveryTime.toStep()).getInSec();
											Reservations resa = Terminal.getInstance().getReservations(dest.getRoad().getId());
											Reservation r = resa.getNextReservation(TimeScheduler.getInstance().getTime(), id);
											if (r != null) {
												Terminal.getInstance().unreserve(dest.getRoad().getId(), id, r.getTimeWindow());
												Time from = new Time(TimeScheduler.getInstance().getTime(), new Time(toWait));
												Time to = new Time(from, r.getTimeWindow().getLength());
												TimeWindow tw2 = new TimeWindow(from, to);
												Terminal.getInstance().reserveRoad(dest.getRoad().getId(), id, tw2, r.getPriority());
											} else {
												Time from = new Time(TimeScheduler.getInstance().getTime(), new Time(toWait));
												Time to = new Time(from, new Time(getRouting().getShortestPath(new Location(dest.getRoad(), 0, true),
														new Location(dest.getRoad(), 1, true)).getCost()));
												TimeWindow tw2 = new TimeWindow(from, to);
												Terminal.getInstance().reserveRoad(dest.getRoad().getId(), id, tw2, Reservation.PRIORITY_GO_IN);
											}

											System.out.println("(1) Straddle " + id + " is too early ! Will wait " + toWait + "s ! (t=" + nextTime
													+ " travelTime[" + poll.getRoad().getId() + "->" + dest.getRoad().getId() + "]=" + travelTime
													+ " delivery_min=" + currentLoad.getMission().getDeliveryTimeWindow().getMin() + ")");
											waitTime += toWait;
										}
									} catch (NoPathFoundException e) {
										e.printStackTrace();
									}
								} else if (currentLoad != null && currentLoad.getPhase() == MissionPhase.PHASE_PICKUP
										&& currentLoad.getMission().getContainer().getLocation().getRoad().getId().equals(next.getRoad().getId())) {
									Time nextTime = new Time(TimeScheduler.getInstance().getTime(), new Time(waitTime));
									// Slot sDest =
									// terminal.getSlot(currentLoad.getMission().getDestination().getSlotId());
									Location dest = currentPath.get(currentPath.size() - 1).getLocation();
									double travelTime;
									try {
										// travelTime =
										// getRouting().getShortestPath(poll,
										// dest,
										// nextTime).getCost()-currentLoad.getMission().getContainer().getHandlingTime().getInSec();
										travelTime = getRouting().getShortestPath(poll, dest, nextTime).getCost();// -getMaxContainerHandlingTime((Lane)dest.getRoad()).getInSec();

										Time pickupTime = new Time(nextTime, new Time(travelTime));
										if (pickupTime.toStep() < currentLoad.getMission().getPickupTimeWindow().getMin().toStep()) {
											double toWait = new Time(currentLoad.getMission().getPickupTimeWindow().getMin().toStep()
													- pickupTime.toStep()).getInSec();
											Reservations resa = Terminal.getInstance().getReservations(dest.getRoad().getId());
											Reservation r = resa.getNextReservation(TimeScheduler.getInstance().getTime(), id);
											if (r != null) {
												Terminal.getInstance().unreserve(dest.getRoad().getId(), id, r.getTimeWindow());
												Time from = new Time(TimeScheduler.getInstance().getTime(), new Time(toWait));
												Time to = new Time(from, r.getTimeWindow().getLength());
												TimeWindow tw2 = new TimeWindow(from, to);
												Terminal.getInstance().reserveRoad(dest.getRoad().getId(), id, tw2, r.getPriority());
											} else {
												Time from = new Time(TimeScheduler.getInstance().getTime(), new Time(toWait));
												Time to = new Time(from, new Time(getRouting().getShortestPath(new Location(dest.getRoad(), 0, true),
														new Location(dest.getRoad(), 1, true)).getCost()));
												TimeWindow tw2 = new TimeWindow(from, to);
												Terminal.getInstance().reserveRoad(dest.getRoad().getId(), id, tw2, Reservation.PRIORITY_GO_IN);
											}

											System.out.println("(1-2) Straddle " + id + " is too early ! Will wait " + toWait + "s ! (t=" + nextTime
													+ " travelTime[" + poll.getRoad().getId() + "->" + dest.getRoad().getId() + "]=" + travelTime
													+ " delivery_min=" + currentLoad.getMission().getPickupTimeWindow().getMin() + ")");
											waitTime += toWait;
										}
									} catch (NoPathFoundException e) {
										e.printStackTrace();
									}

								}
								repush(pn);
							} else {
								if (!next.getRoad().getId().equals(poll.getRoad().getId())) {
									RoadPoint rp = poll.getRoad().getIntersectNode(next.getRoad());
									if (rp == null)
										rp = next.getRoad().getIntersectNode(poll.getRoad());
									if (rp == null)
										System.out.println("THERE IS NO INTERSECT NODE BETWEEN " + next.getRoad().getId() + " AND "
												+ poll.getRoad().getId());

									if (currentLoad != null && currentLoad.getPhase() == MissionPhase.PHASE_DELIVERY
											&& currentLoad.getMission().getDestination().getLaneId().equals(next.getRoad().getId())) {
										Time nextTime = new Time(TimeScheduler.getInstance().getTime(), new Time(waitTime));
										// Slot sDest =
										// terminal.getSlot(currentLoad.getMission().getDestination().getSlotId());
										Location dest = currentPath.get(currentPath.size() - 1).getLocation();

										double travelTime;
										try {
											// travelTime =
											// getRouting().getShortestPath(poll,
											// dest,
											// nextTime).getCost()-currentLoad.getMission().getContainer().getHandlingTime().getInSec();
											travelTime = getRouting().getShortestPath(poll, dest, nextTime).getCost();// -
											// getMaxContainerHandlingTime((Lane)dest.getRoad()).getInSec();

											Time deliveryTime = new Time(nextTime, new Time(travelTime));
											if (deliveryTime.toStep() < currentLoad.getMission().getDeliveryTimeWindow().getMin().toStep()) {
												double toWait = new Time(currentLoad.getMission().getDeliveryTimeWindow().getMin().toStep()
														- deliveryTime.toStep()).getInSec();
												Reservations resa = Terminal.getInstance().getReservations(dest.getRoad().getId());
												Reservation r = resa.getNextReservation(TimeScheduler.getInstance().getTime(), id);
												if (r != null) {
													Terminal.getInstance().unreserve(dest.getRoad().getId(), id, r.getTimeWindow());
													Time from = new Time(TimeScheduler.getInstance().getTime(), new Time(toWait));
													Time to = new Time(from, r.getTimeWindow().getLength());
													TimeWindow tw2 = new TimeWindow(from, to);
													Terminal.getInstance().reserveRoad(dest.getRoad().getId(), id, tw2, r.getPriority());
												} else {
													Time from = new Time(TimeScheduler.getInstance().getTime(), new Time(toWait));
													Time to = new Time(from, new Time(getRouting().getShortestPath(
															new Location(dest.getRoad(), 0, true), new Location(dest.getRoad(), 1, true)).getCost()));
													TimeWindow tw2 = new TimeWindow(from, to);
													Terminal.getInstance().reserveRoad(dest.getRoad().getId(), id, tw2, Reservation.PRIORITY_GO_IN);
												}
												System.out.println("(2) Straddle " + id + " is too early ! Will wait " + toWait + "s ! (t="
														+ nextTime + " travelTime[" + poll.getRoad().getId() + "->" + dest.getRoad().getId() + "]="
														+ travelTime + " delivery_min=" + currentLoad.getMission().getDeliveryTimeWindow().getMin()
														+ ")");
												waitTime += toWait;

											}
										} catch (NoPathFoundException e) {
											e.printStackTrace();
										}
									} else if (currentLoad != null && currentLoad.getPhase() == MissionPhase.PHASE_PICKUP
											&& currentLoad.getMission().getContainer().getLocation().getRoad().getId().equals(next.getRoad().getId())) {
										Time nextTime = new Time(TimeScheduler.getInstance().getTime(), new Time(waitTime));
										// Slot sDest =
										// terminal.getSlot(currentLoad.getMission().getDestination().getSlotId());
										Location dest = currentPath.get(currentPath.size() - 1).getLocation();

										double travelTime;
										try {
											// travelTime =
											// getRouting().getShortestPath(poll,
											// dest,
											// nextTime).getCost()-currentLoad.getMission().getContainer().getHandlingTime().getInSec();
											travelTime = getRouting().getShortestPath(poll, dest, nextTime).getCost();// -
											// getMaxContainerHandlingTime((Lane)dest.getRoad()).getInSec();

											Time pickupTime = new Time(nextTime, new Time(travelTime));
											if (pickupTime.toStep() < currentLoad.getMission().getPickupTimeWindow().getMin().toStep()) {
												double toWait = new Time(currentLoad.getMission().getPickupTimeWindow().getMin().toStep()
														- pickupTime.toStep()).getInSec();
												Reservations resa = Terminal.getInstance().getReservations(dest.getRoad().getId());
												Reservation r = resa.getNextReservation(TimeScheduler.getInstance().getTime(), id);
												if (r != null) {
													Terminal.getInstance().unreserve(dest.getRoad().getId(), id, r.getTimeWindow());
													Time from = new Time(TimeScheduler.getInstance().getTime(), new Time(toWait));
													Time to = new Time(from, r.getTimeWindow().getLength());
													TimeWindow tw2 = new TimeWindow(from, to);
													Terminal.getInstance().reserveRoad(dest.getRoad().getId(), id, tw2, r.getPriority());
												} else {
													Time from = new Time(TimeScheduler.getInstance().getTime(), new Time(toWait));
													Time to = new Time(from, new Time(getRouting().getShortestPath(
															new Location(dest.getRoad(), 0, true), new Location(dest.getRoad(), 1, true)).getCost()));
													TimeWindow tw2 = new TimeWindow(from, to);
													Terminal.getInstance().reserveRoad(dest.getRoad().getId(), id, tw2, Reservation.PRIORITY_GO_IN);
												}
												System.out.println("(2-2) Straddle " + id + " is too early ! Will wait " + toWait + "s ! (t="
														+ nextTime + " travelTime[" + poll.getRoad().getId() + "->" + dest.getRoad().getId() + "]="
														+ travelTime + " delivery_min=" + currentLoad.getMission().getPickupTimeWindow().getMin()
														+ ")");
												waitTime += toWait;

											}
										} catch (NoPathFoundException e) {
											e.printStackTrace();
										}
									}

									Location newLoc = new Location(next.getRoad(), Location.getAccuratePourcent(rp.getLocation(), next.getRoad()),
											next.getDirection());
									// moveContainer(newLoc);
									return move(inS, newLoc);
								} else {
									if (currentLoad != null && currentLoad.getPhase() == MissionPhase.PHASE_DELIVERY
											&& currentLoad.getMission().getDestination().getLaneId().equals(next.getRoad().getId())) {
										Time nextTime = new Time(TimeScheduler.getInstance().getTime(), new Time(waitTime));
										// Slot sDest =
										// terminal.getSlot(currentLoad.getMission().getDestination().getSlotId());
										Location dest = currentPath.get(currentPath.size() - 1).getLocation();

										double travelTime;
										try {
											// travelTime =
											// getRouting().getShortestPath(poll,
											// dest,
											// nextTime).getCost()-currentLoad.getMission().getContainer().getHandlingTime().getInSec();
											travelTime = getRouting().getShortestPath(poll, dest, nextTime).getCost();// -getMaxContainerHandlingTime((Lane)dest.getRoad()).getInSec();

											Time deliveryTime = new Time(nextTime, new Time(travelTime));
											if (deliveryTime.toStep() < currentLoad.getMission().getDeliveryTimeWindow().getMin().toStep()) {
												double toWait = new Time(currentLoad.getMission().getDeliveryTimeWindow().getMin().toStep()
														- deliveryTime.toStep()).getInSec();
												Reservations resa = Terminal.getInstance().getReservations(dest.getRoad().getId());
												Reservation r = resa.getNextReservation(TimeScheduler.getInstance().getTime(), id);
												if (r != null) {
													Terminal.getInstance().unreserve(dest.getRoad().getId(), id, r.getTimeWindow());
													Time from = new Time(TimeScheduler.getInstance().getTime(), new Time(toWait));
													Time to = new Time(from, r.getTimeWindow().getLength());
													TimeWindow tw2 = new TimeWindow(from, to);
													Terminal.getInstance().reserveRoad(dest.getRoad().getId(), id, tw2, r.getPriority());
												} else {
													Time from = new Time(TimeScheduler.getInstance().getTime(), new Time(toWait));
													Time to = new Time(from, new Time(getRouting().getShortestPath(
															new Location(dest.getRoad(), 0, true), new Location(dest.getRoad(), 1, true)).getCost()));
													TimeWindow tw2 = new TimeWindow(from, to);
													Terminal.getInstance().reserveRoad(dest.getRoad().getId(), id, tw2, Reservation.PRIORITY_GO_IN);
												}

												System.out.println("(3) Straddle " + id + " is too early ! Will wait " + toWait + "s ! (t="
														+ nextTime + " travelTime[" + poll.getRoad().getId() + "->" + dest.getRoad().getId() + "]="
														+ travelTime + " delivery_min=" + currentLoad.getMission().getDeliveryTimeWindow().getMin()
														+ ")");
												waitTime += toWait;
											}
										} catch (NoPathFoundException e) {
											e.printStackTrace();
										}
									} else if (currentLoad != null && currentLoad.getPhase() == MissionPhase.PHASE_PICKUP
											&& currentLoad.getMission().getContainer().getLocation().getRoad().getId().equals(next.getRoad().getId())) {
										Time nextTime = new Time(TimeScheduler.getInstance().getTime(), new Time(waitTime));
										// Slot sDest =
										// terminal.getSlot(currentLoad.getMission().getDestination().getSlotId());
										Location dest = currentPath.get(currentPath.size() - 1).getLocation();

										double travelTime;
										try {
											// travelTime =
											// getRouting().getShortestPath(poll,
											// dest,
											// nextTime).getCost()-currentLoad.getMission().getContainer().getHandlingTime().getInSec();
											travelTime = getRouting().getShortestPath(poll, dest, nextTime).getCost();// -getMaxContainerHandlingTime((Lane)dest.getRoad()).getInSec();

											Time pickupTime = new Time(nextTime, new Time(travelTime));
											if (pickupTime.toStep() < currentLoad.getMission().getPickupTimeWindow().getMin().toStep()) {
												double toWait = new Time(currentLoad.getMission().getPickupTimeWindow().getMin().toStep()
														- pickupTime.toStep()).getInSec();
												Reservations resa = Terminal.getInstance().getReservations(dest.getRoad().getId());
												Reservation r = resa.getNextReservation(TimeScheduler.getInstance().getTime(), id);
												if (r != null) {
													Terminal.getInstance().unreserve(dest.getRoad().getId(), id, r.getTimeWindow());
													Time from = new Time(TimeScheduler.getInstance().getTime(), new Time(toWait));
													Time to = new Time(from, r.getTimeWindow().getLength());
													TimeWindow tw2 = new TimeWindow(from, to);
													Terminal.getInstance().reserveRoad(dest.getRoad().getId(), id, tw2, r.getPriority());
												} else {
													Time from = new Time(TimeScheduler.getInstance().getTime(), new Time(toWait));
													Time to = new Time(from, new Time(getRouting().getShortestPath(
															new Location(dest.getRoad(), 0, true), new Location(dest.getRoad(), 1, true)).getCost()));
													TimeWindow tw2 = new TimeWindow(from, to);
													Terminal.getInstance().reserveRoad(dest.getRoad().getId(), id, tw2, Reservation.PRIORITY_GO_IN);
												}

												System.out.println("(3-2) Straddle " + id + " is too early ! Will wait " + toWait + "s ! (t="
														+ nextTime + " travelTime[" + poll.getRoad().getId() + "->" + dest.getRoad().getId() + "]="
														+ travelTime + " delivery_min=" + currentLoad.getMission().getPickupTimeWindow().getMin()
														+ ")");
												waitTime += toWait;
											}
										} catch (NoPathFoundException e) {
											e.printStackTrace();
										}
									}

									return move(inS, poll);
								}
							}
						}
					} else {
						return move(0.0, new Location(l.getRoad(), percent, l.getDirection()));
					}
				}

			}
		} else {
			waitTime -= timeOfMoveInS;
			if (currentLoad != null && !turnBack)
				currentLoad.addWaitTime(timeOfMoveInS);
			if (waitTime < 0) {
				timeOfMoveInS = Math.abs(waitTime);
				if (currentLoad != null && !turnBack)
					currentLoad.removeWaitTime(Math.abs(waitTime));
				waitTime = 0;
				return move(timeOfMoveInS, l);
			}
		}
		return tmpFutureLocation;
	}

	/*
	 * private void moveContainer(Location l)
	 * if(currentLoad!=null&&currentLoad.getPhase()==Load.PHASE_DELIVERY) {
	 * double h =
	 * ContainerKind.getHeight(currentLoad.getMission().getContainer()
	 * .getDimensionType()); double z = (h/2.0) + h*Slot.SLOT_MAX_LEVEL;
	 * Coordinates c = l.getCoords(); c.z = z; l.setCoords(c);
	 * terminal.containerMoved(currentLoad.getMission().getContainer().getId(),
	 * l); } }
	 */

	public void moveContainer() {
		if (available > 0 && currentLoad == null && !handledContainerId.equals("")) {
			if (failureType.equals("move")) {
				setHandledContainerId("");
				Terminal.getInstance().getTextDisplay().setVehicleLoad(this.id, "NA");
			} else {
				Container c = Terminal.getInstance().getContainer(handledContainerId);
				double h = ContainerKind.getHeight(c.getDimensionType());
				double z = (h / 2.0) + h * Slot.SLOT_MAX_LEVEL;
				Location l = new Location(currentLocation.getRoad(), currentLocation.getPourcent(), currentLocation.getDirection());
				Coordinates coords = l.getCoords();
				coords.z = z;
				l.setCoords(coords);
				Terminal.getInstance().containerMoved(handledContainerId, l);
			}
		} else {
			if (currentLoad != null && currentLoad.getPhase() == MissionPhase.PHASE_DELIVERY) {
				if (currentLoad.getMission().getContainer() == null)
					System.err.println("Container null for " + id);
				double h = ContainerKind.getHeight(currentLoad.getMission().getContainer().getDimensionType());
				double z = (h / 2.0) + h * Slot.SLOT_MAX_LEVEL;
				Location l = new Location(currentLocation.getRoad(), currentLocation.getPourcent(), currentLocation.getDirection());
				Coordinates c = l.getCoords();
				c.z = z;
				l.setCoords(c);
				if (!handledContainerId.equals(currentLoad.getMission().getContainerId())) {
					setHandledContainerId(currentLoad.getMission().getContainerId());
					GraphicDisplayPanel out = Terminal.getInstance().getTextDisplay();
					if (out != null)
						out.setVehicleLoad(this.id, handledContainerId);
				}
				Terminal.getInstance().containerMoved(currentLoad.getMission().getContainer().getId(), l);
			} else if (!(currentLoad != null && currentLoad.getPhase() == MissionPhase.PHASE_UNLOAD) && !handledContainerId.equals("")) {
				setHandledContainerId("");
				GraphicDisplayPanel out = Terminal.getInstance().getTextDisplay();
				if (out != null)
					out.setVehicleLoad(this.id, "NA");
			}
		}
		// }
	}

	private void repush(PathNode l) {
		currentPath.push(l);
	}

	private PathNode pop() {
		Time now = TimeScheduler.getInstance().getTime();
		PathNode pn = currentPath.poll();
		roadStartTime = now;
		return pn;
	}

	public void precompute() {
		/*
		 * if(available>0){
		 * System.err.println(TimeScheduler.getInstance().getTime()+"> ");
		 * System.err.println("\tAvailable = "+0+" currentLoad : "+currentLoad);
		 * System
		 * .err.println("\tHandled = "+handledContainerId+(currentLoad==null? ""
		 * : currentLoad.getPhase())); }
		 */

		if (autoHandling) {
			if (available == 0) {
				missionHandling();
			}
			readLocationInDB();
			if (available == 0) {
				if (!handledContainerId.equals("")) {
					Terminal.getInstance().containerMoved(handledContainerId, currentLocation);
				}
			}
		} else {
			if (available > 0 && !checkedOnce)
				missionHandling();
			else if (available == 0)
				missionHandling();

			if (currentLoad != null && (goToList.size() == 0 || goToList.get(0).isInterruptible())) {
				if (destinationReached) {
					// END CURRENT PHASE
					if (currentLoad.getPhase() == MissionPhase.PHASE_PICKUP) {
						// currentLocation = futureLocation;
						if (currentLoad.getMission().getContainer() == null) {
							System.out.println(currentLoad.getMission().getContainerId() + " is not yet on the terminal !");
						} else {
							if (currentLoad.getMission().getContainer().getContainerLocation() != null
									&& Terminal.getInstance().getTime().toStep() >= currentLoad.getMission().getPickupTimeWindow().getMin().toStep()) {
								if (recomputeDestination) {
									recomputeDestination = false;
									setDestination(currentLoad.getMission().getContainer().getContainerLocation());
									destinationReached = false;
								} else {
									boolean b = Terminal.getInstance().unstackContainer(currentLoad.getMission().getContainer(), this.getId());
									if (b) {
										Time handlingT = getContainerHandlingTime();// currentLoad.getMission().getContainer().getHandlingTime();
										Time tPrevious = new Time(TimeScheduler.getInstance().getTime(), new Time(1), false);
										handlingTimeEnd = new Time(tPrevious, new Time(handlingT, overDoneTime, false));
										overDoneTime = null;
										if (currentLoad != null) {
											currentLoad.nextPhase();
											destinationReached = false;
										}
									}
								}
							} else {
								// waitTime +=
								// TimeScheduler.getInstance().getSecondsPerStep();
								recomputeDestination = true;
								// if(terminal.getTime().toStep() <
								// currentLoad.getMission().getPickupTimeWindow().getMin().toStep())
								// System.err.println("TOO EARLY !");

							}
						}
					} else if (currentLoad.getPhase() == MissionPhase.PHASE_LOAD) {
						// Si on a fini de charger, on va livrer
						handlingTimeEnd = null;
						// Here compute overDoneTime for loading
						// (handlingTimeEnd - currentTime)
						ContainerLocation dest = currentLoad.getMission().getDestination();
						currentLoad.nextPhase();
						setDestination(dest);
						destinationReached = false;
					} else if (currentLoad.getPhase() == MissionPhase.PHASE_DELIVERY) {

						// End of delivery so gives container real
						// coordinates :
						// moveContainer();
						// currentLocation = futureLocation;
						// System.out.println("BEFORE "+currentLocation);
						Time handlingTime = getContainerHandlingTime();
						boolean b = Terminal.getInstance().deliverContainer(id, currentLoad.getMission(), handlingTime);
						if (b) {
							currentLoad.nextPhase();
							// int currentPhase = currentLoad.getPhase();
							// int phase = currentLoad.getPhase();

							// if(currentPhase==phase){
							// Time handlingT =
							// getContainerHandlingTime();//currentLoad.getMission().getContainer().getHandlingTime();
							Time tPrevious = new Time(TimeScheduler.getInstance().getTime(), new Time(1), false);
							handlingTimeEnd = new Time(tPrevious, new Time(handlingTime, overDoneTime, false));
							overDoneTime = null;
							destinationReached = false;
							// }
						} else {
							System.out.println(id + "> COULD NOT DELIVER CONTAINER " + currentLoad.getMission().getContainerId() + " !");
						}

					} else if (currentLoad.getPhase() == MissionPhase.PHASE_UNLOAD) {
						// END OF MISSION !!!
						workload.endMission(currentLoad.getMission().getId());
						destinationReached = false;
						if (available > 0)
							checkedOnce = false;
					}

				}

				if (!destinationReached) {
					// COMPUTE NEXT DESTINATION
					if (currentLoad.getPhase() == MissionPhase.PHASE_LOAD) {
						Time t = TimeScheduler.getInstance().getTime();
						if (handlingTimeEnd.compareTo(t) < 0) {
							overDoneTime = new Time(t, handlingTimeEnd, false);
							destinationReached = true;
						}
					} else if (currentLoad.getPhase() == MissionPhase.PHASE_DELIVERY) {
						move();
					} else if (currentLoad.getPhase() == MissionPhase.PHASE_UNLOAD) {
						Time t = TimeScheduler.getInstance().getTime();
						if (handlingTimeEnd.compareTo(t) < 0) {
							destinationReached = true;
							overDoneTime = new Time(t, handlingTimeEnd, false);
						}
					} else if (currentLoad.getPhase() == MissionPhase.PHASE_PICKUP) {
						move();
					}
				}
			} else {
				if (goToList.size() > 0) {

					if (!destinationReached) {
						// if(goToList.get(0).isInterruptible()==false)
						// System.out.println("MOVE FROM GOTO FOR "+id+" handling "+handledContainerId);

						move();
					}

					if (destinationReached) {
						/* GoTo g = */goToList.remove(0);
						if (goToList.size() > 0) {
							setDestination(goToList.get(0).getTarget());
						} else {
							if (available > 0) {
								Terminal.getInstance().updateStraddleFailureRepairTime(id, repairDuration);
							}
							if (currentLoad != null) {
								if (currentLoad.getPhase() == MissionPhase.PHASE_PICKUP)
									setDestination(currentLoad.getMission().getContainer().getContainerLocation());
								else if (currentLoad.getPhase() == MissionPhase.PHASE_DELIVERY)
									setDestination(currentLoad.getMission().getDestination());
							}
						}
						// if(g.isInterruptible()==false)
						// System.out.println("End of GOTO !");
						// System.out.println("GOTO IS NOW NULL !!!");
						destinationReached = false;

					}
				}

				else {
					futureLocation = currentLocation;

				}
			}

		}
	}

	public Time getMaxContainerHandlingTime(Bay l) {
		Block p = Terminal.getInstance().getBlock(l.getPaveId());
		if (p.getType() == BlockType.RAILWAY || p.getType() == BlockType.ROAD) {
			return new Time(model.getSpeedCharacteristics().getContainerHandlingTimeFromTruckMAX());
		} else {
			return new Time(model.getSpeedCharacteristics().getContainerHandlingTimeFromGroundMAX());
		}
	}

	private Time getContainerHandlingTime() {
		if (currentLocation.getRoad() instanceof Bay) {
			Bay l = (Bay) currentLocation.getRoad();
			Block p = Terminal.getInstance().getBlock(l.getPaveId());
			if (p == null)
				return new Time(Math.max(model.getSpeedCharacteristics().getContainerHandlingTimeFromGroundMAX(), model.getSpeedCharacteristics()
						.getContainerHandlingTimeFromTruckMAX()));
			if (p.getType() == BlockType.RAILWAY || p.getType() == BlockType.ROAD) {
				return new Time(model.getSpeedCharacteristics().getContainerHandlingTimeFromTruck());
			} else {
				return new Time(model.getSpeedCharacteristics().getContainerHandlingTimeFromGround());
			}
		} else
			return new Time(Math.max(model.getSpeedCharacteristics().getContainerHandlingTimeFromGroundMAX(), model.getSpeedCharacteristics()
					.getContainerHandlingTimeFromTruckMAX()));
	}

	public void repair() {
		available = 0;

		if (loadToResume != null) {
			// System.err.println("CurrentLoad = "+currentLoad+" toResume = "+loadToResume+" "+loadToResume.getPhase());
			currentLoad = loadToResume;
			try {
				workload.remove(currentLoad.getMission().getId());
			} catch (MissionNotFoundException e) {
				e.printStackTrace();
			}
			workload.insert(currentLoad);
			if (currentLoad.getPhase() == MissionPhase.PHASE_LOAD) {
				if (!handledContainerId.equals(currentLoad.getMission().getContainerId())) {
					currentLoad.setPhase(MissionPhase.PHASE_PICKUP);
				} else
					currentLoad.nextPhase();
			} else if (currentLoad.getPhase() == MissionPhase.PHASE_UNLOAD) {
				currentLoad.setPhase(MissionPhase.PHASE_DELIVERY);
			}

			if (currentLoad.getPhase() == MissionPhase.PHASE_PICKUP)
				setDestination(currentLoad.getMission().getContainer().getContainerLocation());
			else if (currentLoad.getPhase() == MissionPhase.PHASE_DELIVERY)
				setDestination(currentLoad.getMission().getDestination());

			destinationReached = false;
		}
		Terminal.getInstance().straddleCarrierMoved(id, currentLocation, currentLocation, 0, getCSSStyle());
	}

	public void sendMessageTo(String rmiAdress) {

	}

	private void setDestination(ContainerLocation dest) {
		Bay l = Terminal.getInstance().getBay(dest.getLaneId());
		// Slot s = terminal.getSlot(dest.getSlotId());

		// Location l2 = new Location(l, s.getCoords(),true);
		Location l2 = new Location(l, dest.getCoordinates(), true);
		setDestination(l2);
	}

	public void setDestination(Location destination) {
		// TODO mettre une FIFO
		this.currentDestination = destination;
		changePath();
		if (goToList.size() > 0 && goToList.get(0).isInterruptible() == false) {
			System.out.println("Computed path for " + id + " : " + currentPath);
		}
		if (currentPath.getCost() == Double.POSITIVE_INFINITY) {
			// NO WAY !
			if (currentLoad.getPhase() == MissionPhase.PHASE_PICKUP) {
				int action = listener.cantPickupContainer(id, new UnreachableContainerException(currentLoad.getMission().getContainer()
						.getContainerLocation()));
				if (action == StraddleCarrierProblemListener.ABORT_MISSION) {
					abortCurrentMission();
				} else if (action == StraddleCarrierProblemListener.GO_TO_PICKUP_ORIGIN_AND_SEE) {
					goTo(new Location(currentLoad.getMission().getContainer().getLocation().getRoad(), 0.0), false);

				} else if (action == StraddleCarrierProblemListener.GO_TO_PICKUP_DESTINATION_AND_SEE) {
					goTo(new Location(currentLoad.getMission().getContainer().getLocation().getRoad(), 1.0), false);
				}
			} else if (currentLoad.getPhase() == MissionPhase.PHASE_DELIVERY) {
				listener.cantDeliverContainerOnUnreachableSlot(id, new UnreachableSlotException(currentLoad.getMission().getDestination()), out);
			}
		}
		if (currentPath == null)
			System.out.println("1er NULLL :::!!!!");

	}

	public void setTextDisplay(TextDisplay display) {
		this.out = display;
	}

	private boolean willReachLocation(Location currentLocation, Location target, double oneMoveRate) {
		return currentLocation.willReachRate(target.getPourcent(), target.getDirection() ? oneMoveRate : -oneMoveRate);
	}

	/**
	 * Return the speed of the current vehicle on r at time t
	 * 
	 * @param r
	 *            Road
	 * @param t
	 *            Time
	 * @return the speed of v on r at time t
	 */

	public double getSpeed(Road r, Time t) {
		SpeedCharacteristics speed = model.getSpeedCharacteristics();
		double s = r instanceof Bay ? speed.getLaneSpeed() : speed.getSpeed();
		if (available < 2) {
			if (t != null && willBeLoadedAt(t)) {
				return speed.getLoadedSpeed();
			} else
				return s;
		} else
			return s * StraddleCarrierFailure.TOWING_SPEED_RATIO;
	}

	/**
	 * Must tell if the vehicle will be carrying a container at time t
	 * 
	 * @param t
	 *            Time
	 * @return true if the vehicle will be carrying a container at time t
	 */
	// TODO !!!
	private boolean willBeLoadedAt(Time t) {
		return false;
	}

//	public void destroy() {
//		this.routing.destroy();
//	}

	public double getCurrentSpeed() {
		SpeedCharacteristics speed = model.getSpeedCharacteristics();
		Road r = currentLocation.getRoad();
		double s = 0;

		if (available == 0) {
			if (!handledContainerId.equals("")) {
				return speed.getLoadedSpeed();
			} else {
				if (r instanceof Bay) {
					s = speed.getLaneSpeed();
				} else {
					s = speed.getSpeed();
				}
				return s;
			}
		} else {
			if (r instanceof Bay) {
				s = speed.getLaneSpeed();
			} else {
				s = speed.getSpeed();
			}

			return s * StraddleCarrierFailure.TOWING_SPEED_RATIO;
		}
	}

	private String getImageURL() {
		String imageFileName = color;

		if (available == 0) {
			if (currentLoad == null || currentLoad.getState() == MissionState.STATE_ACHIEVED)
				imageFileName += "_free";
		} else
			imageFileName += "_HS";
		if (currentLocation.getDirection()) {
			// Si carre haut gauche : inversion
			if (currentLocation.getRoad().getOrigin().getLocation().x > currentLocation.getRoad().getDestination().getLocation().x) {
				if (currentLocation.getRoad().getOrigin().getLocation().y < currentLocation.getRoad().getDestination().getLocation().y) {
					imageFileName += "_inv";
				}
			} else { // Si carre haut droite : inversion
				if (currentLocation.getRoad().getOrigin().getLocation().y < currentLocation.getRoad().getDestination().getLocation().y) {
					imageFileName += "_inv";
				}
			}
		} else {

			if (currentLocation.getRoad().getOrigin().getLocation().x < currentLocation.getRoad().getDestination().getLocation().x) {
				if (currentLocation.getRoad().getOrigin().getLocation().y > currentLocation.getRoad().getDestination().getLocation().y) {
					imageFileName += "_inv";
				}
			} else {
				if (currentLocation.getRoad().getOrigin().getLocation().y > currentLocation.getRoad().getDestination().getLocation().y) {
					imageFileName += "_inv";
				}
			}
		}
		imageFileName += ".png";
		return imageFileName;
	}

	public Load getCurrentLoad() {
		return currentLoad;
	}

	public String getIconURL() {
		return this.getClass().getResource(STRADDLECARRIER_ICON_PREFIX_URL + color + STRADDLECARRIER_ICON_SUFFIX_URL).getFile();
	}

	public ImageIcon getIcon() {
		return new ImageIcon(this.getClass().getResource(STRADDLECARRIER_ICON_PREFIX_URL + getImageURL()));
	}

	public boolean isAvailable() {
		return available == 0;
	}

	public void setAutoDriven(boolean b) throws DatabaseNotConfiguredException {
		autoHandling = b;
		// FIXME
		// if (b == false && databaseManager == null)
		// throw new DatabaseNotConfiguredException();
	}

	public void clearWorkload() {
		getWorkload().removeUnstartedMissions();
	}
}
