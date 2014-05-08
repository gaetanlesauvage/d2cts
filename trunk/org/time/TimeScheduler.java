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
package org.time;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.SortedMap;
import java.util.TreeMap;

import org.apache.log4j.Logger;
import org.com.dao.scheduling.ResultsDAO;
import org.com.model.scheduling.ResultsBean;
import org.display.TextDisplay;
import org.exceptions.IllegalSlotChangeException;
import org.scheduling.MissionScheduler;
import org.system.Terminal;
import org.system.container_stocking.ContainerLocation;
import org.time.event.ContainerOut;
import org.time.event.DynamicEvent;
import org.time.event.NewContainer;
import org.time.event.ShipContainerOut;
import org.time.event.VehicleIn;
import org.time.event.VehicleOut;
import org.util.RecordableObject;
import org.vehicles.Truck;

public class TimeScheduler implements RecordableObject {
	private static final Logger logger = Logger.getLogger(TimeScheduler.class);

	private static TimeScheduler instance;

	//private List<String> discretsRemoteObjects;

	private SortedMap<Integer, List<DiscretObject>> discretObjects;

	private DiscretObject missionScheduler;

	private TreeMap<Time, List<DynamicEvent>> events;
	private List<DynamicEvent> eventsToAdd;
	private TreeMap<Time, List<DynamicEvent>> doneEvents;

	private long step;
	private long startTime;
	private long cpteTime = 0;
	
	private Time t;
	private double secondsPerStep;

	private boolean sync = false;
	//private boolean threaded = false;
	private int normalization_time_in_ms = 0;
	private long outOfSyncItCount = 0;
	private long catchupTime = 0;

	private static final double INIT_SEC_PER_STEP = 1;
	public static final String rmiBindingName = "TimeScheduler";



	//	private ArrayList<Thread> prioritaryThreads;
	//	private ArrayList<Thread> otherThreads;
	//	private ArrayList<Thread> todoLast;

	public static TimeScheduler getInstance() {
		if (instance == null) {
			instance = new TimeScheduler(INIT_SEC_PER_STEP);
			instance.t = new Time(0, 0, 0);
		}
		return instance;
	}

	public static void closeInstance(){
		if(instance != null){
			//			instance.destroy();
			instance = null;
		}
	}

	public boolean hasMoreEvents (){
		return !events.isEmpty();
	}

	private String id;

	private TextDisplay out;

	private boolean somethingChanged;

	private TimeScheduler(double secondsPerSep) {
		this(secondsPerSep, "Scheduler");
	}

	private TimeScheduler(double secondsPerSep, String id) {
		this.id = id;
		this.secondsPerStep = secondsPerSep;

		//discretsRemoteObjects = new ArrayList<String>();
		discretObjects = new TreeMap<Integer, List<DiscretObject>>();
		events = new TreeMap<Time, List<DynamicEvent>>();
		eventsToAdd = new ArrayList<DynamicEvent>();
		doneEvents = new TreeMap<Time, List<DynamicEvent>>();

		step = 0;
		catchupTime = 0;

		//		prioritaryThreads = new ArrayList<Thread>(1);
		//		otherThreads = new ArrayList<Thread>(20);
		//		todoLast = new ArrayList<Thread>(1);

		//System.out.println("Time Scheduler Created!");
	}

	public void commitRegistration() {
		for (DynamicEvent e : eventsToAdd) {
			List<DynamicEvent> list;
			if (events.containsKey(e.getTime())) {
				list = events.get(e.getTime());
			} else {
				list = new ArrayList<DynamicEvent>();
			}
			list.add(e);
			events.put(e.getTime(), list);
		}
		eventsToAdd.clear();
	}

	public void setTruckInLocation(String truckID,
			ContainerLocation newDestination) {
		boolean okIn = false;
		boolean okOut = false;

		for (List<DynamicEvent> list : events.values()) {
			for (DynamicEvent e : list) {

				if (e.getType().equals(VehicleIn.TYPE)) {

					VehicleIn v = (VehicleIn) e;
					// System.out.println("DynamicEvent : "+v);
					if (v.getVehicleID().equals(truckID)) {
						try {
							v.changeSlot(newDestination.getSlotId());
							okIn = true;
						} catch (IllegalSlotChangeException e1) {
							e1.printStackTrace();
						}
					}
				} else if (e.getType().equals(VehicleOut.TYPE)) {
					VehicleOut v = (VehicleOut) e;
					// System.out.println("DynamicEvent : "+v);
					if (v.getVehicleID().equals(truckID)) {
						try {
							v.changeSlot(newDestination.getSlotId());
							okOut = true;
						} catch (IllegalSlotChangeException e1) {
							e1.printStackTrace();
						}
					}
				}
			}
		}
		if (!okIn || !okOut) {
			// System.out.println("VEHICLE NOT FOUND : "+okIn+" | "+okOut);
			for (DynamicEvent e : eventsToAdd) {
				if (!okIn && e.getType().equals(VehicleIn.TYPE)) {

					VehicleIn v = (VehicleIn) e;
					// System.out.println("DynamicEvent : "+v);
					if (v.getVehicleID().equals(truckID)) {
						try {
							v.changeSlot(newDestination.getSlotId());
							okIn = true;
							if (okOut)
								break;
						} catch (IllegalSlotChangeException e1) {
							e1.printStackTrace();
						}
					}
				}
				if (!okOut && e.getType().equals(VehicleOut.TYPE)) {
					VehicleOut v = (VehicleOut) e;
					// System.out.println("DynamicEvent : "+v);
					if (v.getVehicleID().equals(truckID)) {
						try {
							v.changeSlot(newDestination.getSlotId());
							okOut = true;
							if (okIn)
								break;
						} catch (IllegalSlotChangeException e1) {
							e1.printStackTrace();
						}
					}
				}

			}
		}
		if (!okIn)
			logger.error("VEHICLE IN NOT FOUND !");
		if (!okOut)
			logger.error("VEHICLE OUT NOT FOUND !");
	}

	@Override
	public String getId() {
		return id;
	}

	@Override
	public TextDisplay getTextDisplay() {
		return out;
	}

	public double getSecondsPerStep() {
		return secondsPerStep;
	}

	public long getStep() {
		return step;
	}

	public Time getTime() {
		return t;
	}

	public void recordMissionsScheduler (DiscretObject d){
		missionScheduler = d;
	}

	public void recordDiscretObject(DiscretObject d) {
		if(!discretObjects.containsKey(d.getDiscretPriority())){
			discretObjects.put(d.getDiscretPriority(), new ArrayList<DiscretObject>());
		}
		discretObjects.get(d.getDiscretPriority()).add(d);
	}

	/*public void recordDiscretObject(String discretObjectName) {
		discretsRemoteObjects.add(discretObjectName);
	}*/

	public void registerDynamicEvent(DynamicEvent e) {
		if (e.getTime().toStep() == step) {
			e.execute();
			List<DynamicEvent> l = doneEvents.get(e.getTime());
			if (l == null)
				l = new ArrayList<DynamicEvent>();
			l.add(e);
			doneEvents.put(e.getTime(), l);
		} else
			eventsToAdd.add(e);
	}

	@Override
	public void setTextDisplay(TextDisplay out) {
		this.out = out;
	}

	public void setSecondsPerStep(double newStepSize) {
		logger.info("Step size = "+newStepSize);
		this.secondsPerStep = newStepSize;
	}

	public boolean step(boolean isSynchronized) {

		if (step == 0) {
			startTime = System.nanoTime();
		}

		long tBefore = System.nanoTime();

		//		if (threaded)
		//			stepThread();
		//		else
		boolean keepGoing = stepSeq();
		/*
		 * //Wait for the display try { SwingUtilities.invokeAndWait(new
		 * Runnable() {
		 * 
		 * @Override public void run() {
		 * System.out.println("Action in EDT within TimeScheduler");
		 * 
		 * } }); } catch (InvocationTargetException e1) { e1.printStackTrace();
		 * } catch (InterruptedException e1) { e1.printStackTrace(); }
		 */

		long tAfter = System.nanoTime();
		long duration = tAfter - tBefore;
		// overallTime+=duration;

		if (isSynchronized && sync) {
			long gap = (long) (secondsPerStep * 1000000000) - duration;

			if (gap - catchupTime > 0) {
				// if(catchupTime>0)
				// System.out.println("Catchup "+catchupTime+"ns in this iteration !");
				long millis = (gap - catchupTime) / 1000000;
				int nanos = (int) ((gap - catchupTime) % 1000000);
				catchupTime = 0;
				try {
					Thread.sleep(millis, nanos);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			} else {
				if (gap > 0) {
					catchupTime -= gap;
					// System.out.println("NOT OUT OF SYNC BUT CATCHUP : "+gap+" ns ! ("+outOfSyncItCount+" out of sync steps overall).");
				} else {
					double extraTime = Math.abs(gap) / 1000000.0;
					catchupTime += Math.abs(gap);
					outOfSyncItCount++;
					logger.warn("OUT OF SYNC : " + extraTime + " ms ! ("
							+ outOfSyncItCount
							+ " out of sync steps overall). Catchup = "
							+ catchupTime + "ns");
				}

			}

		} else if (isSynchronized && normalization_time_in_ms > 0) {
			// long averageTimePerStep = overallTime / step;
			long averageTimePerStep = normalization_time_in_ms * 1000000;
			// System.out.println("avg = "+averageTimePerStep+"ns duration = "+duration);
			if (duration < averageTimePerStep) {

				long ns = averageTimePerStep - duration;
				// System.out.println("GAP = "+ns);
				long ms = ns / 1000000;
				// System.out.println("ms = "+ms);
				int nsStay = (int) (((ns / 1000000.0) - ms) * 1000000);
				// System.out.println("sleep = "+ms+" ms "+nsStay+" ns");
				try {
					Thread.sleep(ms, nsStay);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
		if(!keepGoing){
			computeEndTime();
			//Store results
			ResultsBean results = MissionScheduler.getInstance().getIndicatorPane().getResults();
			results.setSimulation(Terminal.getInstance().getSimulationID());
			Time t = getCptTime();
			System.err.println("CPT TIME = "+t);
			results.setOverallTime(t.getInSec());
			
			try {
				ResultsDAO.getInstance().insert(results);
			} catch (SQLException e) {
				logger.error(e);
			}
		}
		cpteTime += (System.nanoTime() - tBefore);
		return keepGoing;
	}

	private Time getCptTime(){
		return new Time(cpteTime / 1000000000d);
	}
	
	private void computeEndTime() {
		long now = System.nanoTime();
		long diff = now - startTime;
		Time simTime = new Time(diff/1000000000d);
		logger.info(getTime()+":> Simulation ran in "+simTime);
	}

	private boolean stepSeq() {
		// writer.append(this.step+"> SEQ\n");
		// VALIDATE REGISTRATION OF EVENTS !
		if (eventsToAdd.size() > 0) {
			// System.out.println(eventsToAdd.size()+" elements to commit !");
			commitRegistration();
		}

		// STEP EVOLUTION
		step++;

		// TIME EVOLUTION
		t = new Time(step);
		/*
		 * double sec = (step*secondsPerStep); int nh =(int) (sec / 3600); sec =
		 * sec % 3600; int nm = (int)(sec /60); sec = sec %60; t.setTime(nh, nm,
		 * sec);
		 */

		// EVENTS :
		Time lower = events.lowerKey(t);

		while (lower != null) {
			List<DynamicEvent> dynEvents = events.get(lower);
			// if(dynEvents!=null && dynEvents.size()>0)
			// writer.append("LOWER DYNEVENT : ");

			if (dynEvents != null) {
				for (DynamicEvent d : dynEvents) {
					d.execute();
					// writer.append(d.getType()+" ");
					// if(!(d instanceof ChangeContainerLocation))
					// System.out.println("Event detected : "+step+" "+t);
				}
				doneEvents.put(lower, events.remove(lower));
			}
			lower = events.lowerKey(lower);
			// if(dynEvents!=null && dynEvents.size()>0) writer.append("\n");
		}

		List<DynamicEvent> dynEvents = events.get(t);
		// if(dynEvents!=null && dynEvents.size()>0)
		// writer.append("DYNEVENT : ");
		if (dynEvents != null) {
			for (DynamicEvent d : dynEvents) {
				d.execute();
				Thread.yield();
				
				// writer.append(d.getType()+" ");
				// if(!(d instanceof ChangeContainerLocation))
				// System.out.println("Event detected : "+step+" "+t);
			}
			doneEvents.put(t, events.remove(t));
			// if(dynEvents!=null && dynEvents.size()>0) writer.append("\n");
		}



		// PRECOMPUTE SC AND LS
		// writer.append("PRECOMPUTE : ");
		for(Entry<Integer, List<DiscretObject>> entry : discretObjects.entrySet()){
			for(Iterator<DiscretObject> it = entry.getValue().iterator(); it.hasNext();) {
				it.next().precompute();
				// writer.append(d.getId()+" ");
				Thread.yield();
			}
		}

		// writer.append("\n");

		// APPLY SC AND LS
		// writer.append("APPLY : ");
		somethingChanged = false;
		for(Entry<Integer, List<DiscretObject>> entry : discretObjects.entrySet()){
			for(Iterator<DiscretObject> it = entry.getValue().iterator(); it.hasNext();) {
				boolean localEnd = it.next().apply();
				if(!somethingChanged && localEnd == DiscretObject.SOMETHING_CHANGED){
					somethingChanged = true;
				}
				// writer.append(d.getId()+" ");
				Thread.yield();
			}
		}

		// writer.append("\n");

		// writer.flush();

		//PRECOMPUTE AND APPLY MISSION SCHEDULER !
		missionScheduler.precompute();

		boolean localEnd = missionScheduler.apply();
		if(!somethingChanged && localEnd == DiscretObject.SOMETHING_CHANGED)
			somethingChanged = true;

		if(!somethingChanged && !hasMoreEvents()){
			return false;
		} else {
			return true;
		}
	}

	//	private boolean stepThread() {
	//		// System.out.println(this.step+"> THREADED");
	//		// VALIDATE REGISTRATION OF EVENTS !
	//
	//		if (eventsToAdd.size() > 0)
	//			commitRegistration();
	//
	//		// STEP EVOLUTION
	//		step++;
	//		// TIME EVOLUTION
	//		t = new Time(step);
	//		/*
	//		 * double sec = step*secondsPerStep; int nh =(int) (sec / 3600) ; sec =
	//		 * sec % 3600; int nm = (int)(sec /60); sec = sec %60; t.setTime(nh, nm,
	//		 * sec);
	//		 */
	//		// EVENTS :
	//		Time lower = events.lowerKey(t);
	//		while (lower != null) {
	//			List<DynamicEvent> dynEvents = events.get(lower);
	//			if (dynEvents != null) {
	//				for (DynamicEvent d : dynEvents) {
	//					d.execute();
	//					Thread.yield();
	//					// if(!(d instanceof ChangeContainerLocation))
	//					// System.out.println("Event detected : "+step+" "+t);
	//				}
	//				doneEvents.put(lower, events.remove(lower));
	//			}
	//			lower = events.lowerKey(lower);
	//		}
	//		List<DynamicEvent> dynEvents = events.get(t);
	//		if (dynEvents != null) {
	//			for (DynamicEvent d : dynEvents) {
	//				d.execute();
	//				Thread.yield();
	//				System.out.println("Event detected : " + step + " " + t);
	//			}
	//			doneEvents.put(t, events.remove(t));
	//		}
	//
	//		// CLEAR LIST OF THREADS
	//		prioritaryThreads.clear();
	//		otherThreads.clear();
	//		todoLast.clear();
	//
	//		// PRECOMPUTE
	//		for (final DiscretObject d : discretObjects) {
	//			Thread t = new Thread("Thread_" + d.getId()) {
	//				public void run() {
	//					d.precompute();
	//					Thread.yield();
	//				}
	//			};
	//			if (d.getId().contains(LaserSystem.rmiBindingName)) {
	//				prioritaryThreads.add(t);
	//			} else {
	//				if (d.getId().contains(MissionScheduler.rmiBindingName)) {
	//					todoLast.add(t);
	//				} else
	//					otherThreads.add(t);
	//			}
	//		}
	//
	//		for (Thread t : prioritaryThreads) {
	//			t.start();
	//		}
	//		for (Thread t : prioritaryThreads) {
	//			try {
	//				t.join();
	//			} catch (InterruptedException e) {
	//				e.printStackTrace();
	//			}
	//		}
	//
	//		for (Thread t : otherThreads) {
	//			t.start();
	//		}
	//		for (Thread t : otherThreads) {
	//			try {
	//				t.join();
	//
	//			} catch (InterruptedException e) {
	//				e.printStackTrace();
	//			}
	//		}
	//
	//		for (Thread t : todoLast) {
	//			t.start();
	//		}
	//		for (Thread t : todoLast) {
	//			try {
	//				t.join();
	//
	//			} catch (InterruptedException e) {
	//				e.printStackTrace();
	//			}
	//		}
	//
	//		prioritaryThreads.clear();
	//		otherThreads.clear();
	//		todoLast.clear();
	//
	//		// APPLY
	//		for (final DiscretObject d : discretObjects) {
	//			Thread t = new Thread() {
	//				public void run() {
	//					d.apply();
	//				}
	//			};
	//			if (d.getId().contains(LaserSystem.rmiBindingName)) {
	//				prioritaryThreads.add(t);
	//			} else {
	//				if (d.getId().contains(MissionScheduler.rmiBindingName)) {
	//					todoLast.add(t);
	//				} else
	//					otherThreads.add(t);
	//			}
	//		}
	//
	//		for (Thread t : prioritaryThreads) {
	//			t.start();
	//		}
	//		for (Thread t : prioritaryThreads) {
	//			try {
	//				t.join();
	//			} catch (InterruptedException e) {
	//				e.printStackTrace();
	//			}
	//		}
	//
	//		for (Thread t : otherThreads) {
	//			t.start();
	//		}
	//		for (Thread t : otherThreads) {
	//			try {
	//				t.join();
	//
	//			} catch (InterruptedException e) {
	//				e.printStackTrace();
	//			}
	//		}
	//		for (Thread t : todoLast) {
	//			t.start();
	//		}
	//		for (Thread t : todoLast) {
	//			try {
	//				t.join();
	//
	//			} catch (InterruptedException e) {
	//				e.printStackTrace();
	//			}
	//		}
	//	}

	public static final double getInitSecByStep() {
		return INIT_SEC_PER_STEP;
	}

	public void setSynchronized(boolean synchro) {
		sync = synchro;
		if (!sync && catchupTime > 0)
			catchupTime = 0;
	}

	//	public void setThreaded(boolean threaded) {
	//		this.threaded = threaded;
	//	}

	//	public void destroy() {
	//		t = null;
	//		out = null;
	//		id = null;
	//		secondsPerStep = 0.0;
	//
	//		discretObjects.clear();
	//		
	//		discretsRemoteObjects.clear();
	//		doneEvents.clear();
	//		events.clear();
	//		eventsToAdd.clear();
	//
	//		if (todoLast != null) {
	//			todoLast.clear();
	//			todoLast = null;
	//		}
	//		if (prioritaryThreads != null) {
	//			prioritaryThreads.clear();
	//			prioritaryThreads = null;
	//		}
	//		if (otherThreads != null) {
	//			otherThreads.clear();
	//			otherThreads = null;
	//		}
	//		catchupTime = 0;
	//		discretObjects = null;
	//		discretsRemoteObjects = null;
	//		doneEvents = null;
	//		events = null;
	//		eventsToAdd = null;
	//		normalization_time_in_ms = 0;
	//		outOfSyncItCount = 0;
	//		startTime = 0;
	//		step = 0;
	//		
	//	}

	public String eventsToString() {
		StringBuilder sb = new StringBuilder();
		sb.append("Scheduler dynamic events toAdd : \n");
		for (DynamicEvent e : eventsToAdd) {
			sb.append("- " + e.toString() + "\n");
		}
		sb.append("         ----------------\n");
		sb.append("Scheduler dynamic events : \n");
		for (Time t : events.keySet()) {
			for (DynamicEvent e : events.get(t)) {
				sb.append("- at " + e.getTime() + " " + e.getType() + " "
						+ e.toString() + "\n");
			}
		}
		sb.append("==================\n");
		return sb.toString();
	}

	public void changeShipContainerOutDestination(String id, String slotId) {
		for (Time t : events.keySet()) {
			for (DynamicEvent d : events.get(t)) {
				if (d.getType().equals(ContainerOut.getContainerOutType())) {
					ContainerOut co = (ContainerOut) d;
					if (co.getContainerID().equals(id)) {
						ShipContainerOut sco = ((ShipContainerOut) co);
						sco.setDestinationSlotID(slotId);
						return;
					}
				}
			}
		}
	}

	public void setNormalizationTime(int ms) {
		normalization_time_in_ms = ms;
	}
	/*public Slot getIncomingTruckLocation(Truck truck){
		
		for (Time t : events.keySet()) {
			for (DynamicEvent d : events.get(t)) {
				if (d.getType().equals(NewContainer.getNewContainerType())) {
					NewContainer nc = (NewContainer) d;
					if (truck.nc.getContainerID().equals(containerId)) {
						return Terminal.getInstance().getSlot(nc.getLocation().getSlotId());
					}
				}
			}
		}
		return null;
	}*/

	public double getIncomingContainerTeu(String containerId) {
		for (Time t : events.keySet()) {
			for (DynamicEvent d : events.get(t)) {
				if (d.getType().equals(NewContainer.getNewContainerType())) {
					NewContainer nc = (NewContainer) d;

					if (nc.getContainerID().equals(containerId)) {
						return nc.getTEU();
					}
				} else if (d.getType().equals(VehicleIn.TYPE)) {
					VehicleIn v = (VehicleIn) d;
					double teu = v.getTeu(containerId);
					if (teu > 0) {
						return teu;
					}
				}

			}
		}

		for (DynamicEvent d : eventsToAdd) {
			if (d.getType().equals(NewContainer.getNewContainerType())) {
				NewContainer nc = (NewContainer) d;
				if (nc.getContainerID().equals(containerId)) {
					return nc.getTEU();
				}
			} else if (d.getType().equals(VehicleIn.TYPE)) {
				VehicleIn v = (VehicleIn) d;
				double teu = v.getTeu(containerId);

				if (teu > 0) {
					return teu;
				}
			}

		}
		return -1;
	}

	public Truck getIncomingTruck(String id) {
		for (Time t : events.keySet()) {
			for (DynamicEvent d : events.get(t)) {
				if (d.getType().equals(VehicleIn.TYPE)) {
					VehicleIn v = (VehicleIn)d;
					if (id.equals(v.getVehicleID())) {
						return new Truck(v.getVehicleID(),v.getTime(),v.getSlots().get(0));
					}
				}
			}
		}
		return null;
	}
}
