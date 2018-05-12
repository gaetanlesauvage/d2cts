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

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.apache.log4j.Logger;
import org.conf.parameters.ReturnCodes;
import org.time.Time;
import org.time.TimeWindow;

/**
 * Reservation table of a lane
 * 
 * @author Ga&euml;tan Lesauvage
 * @since 2009
 */
public class Reservations {
	public static final Logger log = Logger.getLogger(Reservations.class);
	/**
	 * Reservation map sorted according to the beginning of the reservations'
	 * time windows
	 */
	private TreeMap<Time, Reservation> table;
	/**
	 * Reservation map sorted according to the end of the reservations' time
	 * windows
	 */
	private TreeMap<Time, Reservation> endTimeTable;
	/**
	 * Index map of the reservations according to the straddle carriers' IDs
	 */
	private Hashtable<String, ArrayList<Time>> index;
	/**
	 * Reserved road/lane
	 */
	private Road road;

	/**
	 * Build a reservations table for the given road
	 * 
	 * @param r
	 *            The road of the new reservations table
	 */
	public Reservations(Road r) {
		this.road = r;
		table = new TreeMap<Time, Reservation>();
		endTimeTable = new TreeMap<Time, Reservation>();
		index = new Hashtable<String, ArrayList<Time>>();
	}

	/**
	 * Add a reservation to the table
	 * 
	 * @param r
	 *            Reservation to add
	 * @return <b>true</b> if the reservation has been setup, <b>false</b>
	 *         otherwise
	 */
	public boolean addReservation(Reservation r) {
		if (r.getTimeWindow() == null)
			return true;

		Time t = r.getTimeWindow().getMin();

		if (table.containsKey(t)) {
			System.out.println("Key already in map ! " + t + " content = " + table.get(t));
			// removeReservation(t);
			return false;
		} else {
			table.put(r.getTimeWindow().getMin(), r);
			endTimeTable.put(r.getTimeWindow().getMax(), r);
			ArrayList<Time> l;
			if (index.containsKey(r.getStraddleCarrierId()))
				l = index.get(r.getStraddleCarrierId());
			else
				l = new ArrayList<Time>();
			l.add(r.getTimeWindow().getMin());
			index.put(r.getStraddleCarrierId(), l);
			/*
			 * try { System.err.println("Reservation "+r+" added at "+Time.
			 * getTimeScheduler().getTime()); } catch (RemoteException e) {
			 * e.printStackTrace(); } System.out.println(this);
			 */
		}
		return true;
	}

	/**
	 * Bring forward or put back any blocking reservation to be able to add the
	 * given one. Then, add the reservation.
	 * 
	 * @param toAdd
	 *            Reservation to add @
	 */
	private void bringForwardPutBackAndAdd(Reservation toAdd) {
		TreeMap<Time, Reservation> toReAdd = new TreeMap<Time, Reservation>();

		// Before
		Time t = toAdd.getTimeWindow().getMin();

		Time tAvant = table.lowerKey(t);
		while (tAvant != null) {
			Reservation avant = table.get(tAvant);
			if (avant.getTimeWindow().getMax().compareTo(t) >= 0) {
				Terminal.getInstance().unreserve(avant.getRoadId(), avant.getStraddleCarrierId(), avant.getTimeWindow());
				toReAdd.put(tAvant, avant);
				// System.out.println("1) ToReAdd.put("+tAvant+", "+avant+") time = "+Time.getTimeScheduler().getTime());
				tAvant = table.lowerKey(tAvant);
			} else
				tAvant = null;
		}

		// Meanwhile
		if (table.containsKey(t)) {
			Reservation pendant = table.get(t);
			Terminal.getInstance().unreserve(pendant.getRoadId(), pendant.getStraddleCarrierId(), pendant.getTimeWindow());
			toReAdd.put(t, pendant);
			// System.out.println("2) ToReAdd.put("+t+", "+pendant+") time = "+Time.getTimeScheduler().getTime());
		}

		// After
		Time tApres = table.higherKey(t);
		Time tMax = toAdd.getTimeWindow().getMax();
		while (tApres != null) {
			Reservation apres = table.get(tApres);
			if (apres.getTimeWindow().getMin().compareTo(tMax) <= 0) {
				// System.err.println("3) tApres = "+tApres+" apres = "+apres.getRoadId()+" "+apres.getTimeWindow()+" time = "+Time.getTimeScheduler().getTime());
				Terminal.getInstance().unreserve(apres.getRoadId(), apres.getStraddleCarrierId(), apres.getTimeWindow());

				toReAdd.put(tApres, apres);
				// System.out.println("3) ToReAdd.put("+tApres+", "+apres+") time = "+Time.getTimeScheduler().getTime());
				tApres = table.higherKey(tApres);
			} else
				tApres = null;
		}

		// Now add reservation !
		Terminal.getInstance().reserveRoad(this.road.getId(), toAdd.getStraddleCarrierId(), toAdd.getTimeWindow(), toAdd.getPriority());
		// System.out.println("After adding toAdd "+this);

		// Re add other reservations
		Time startTime = new Time(toAdd.getTimeWindow().getMax(), new Time(1));
		while (toReAdd.size() > 0) {
			Entry<Time, Reservation> entry = toReAdd.pollFirstEntry();
			Reservation r = entry.getValue();

			// Compute the new time window
			double startTimeToS = startTime.getInSec();
			double rStartTimeToS = r.getTimeWindow().getMin().getInSec();
			Time gap = new Time(Math.abs(rStartTimeToS - startTimeToS));
			TimeWindow tw = new TimeWindow(new Time(r.getTimeWindow().getMin(), gap), new Time(r.getTimeWindow().getMax(), gap));

			Reservation r2 = new Reservation(Terminal.getInstance().getTime(), new String(r.getStraddleCarrierId()), new String(r.getRoadId()), tw,
					new Integer(r.getPriority()));
			// System.out.println("ReAdd r2 = "+r2);
			// Add the new reservation
			// addReservation(r2);
			Terminal.getInstance().reserveRoad(road.getId(), r2.getStraddleCarrierId(), r2.getTimeWindow(), r2.getPriority());
			// System.out.println("After adding r2 "+this);

			// Tell to the vehicle to recompute its path !
			Terminal.getInstance().getStraddleCarrier(r2.getStraddleCarrierId()).changePath();
			startTime = new Time(r2.getTimeWindow().getMax(), new Time(1));
		}

	}

	/**
	 * Tests if a given reservation can be set up
	 * 
	 * @param r
	 *            The reservation to set up
	 * @return <b>true</b> if the reservation can be setup, <b>false</b>
	 *         otherwise @
	 */
	public boolean canMakeReservation(Reservation r) {
		/*
		 * if(r.getTimeWindow() == null) { new
		 * Exception("Trying to make a reservation with no time window !!!"
		 * ).printStackTrace(); return true; }
		 */
		ArrayList<Reservation> toUnreserve = new ArrayList<Reservation>(10);
		Time tMin = r.getTimeWindow().getMin();
		Time tPrev = table.lowerKey(tMin);

		// if (another reservation starts at the same time) => cannot make
		// reservation
		if (tPrev != null) {
			Reservation previous = table.get(tPrev);
			// if (previous ends after current starts) => cannot make
			// reservation
			if (previous.getStraddleCarrierId().equals(r.getStraddleCarrierId())) {
				toUnreserve.add(previous);
			} else if (previous.getTimeWindow().getMax().compareTo(r.getTimeWindow().getMin()) >= 0) {
				// Pas ok
				if (r.getPriority() > previous.getPriority()) {
					// System.out.println("Trying to know if we can make reservation "
					// + r);
					// System.out.println("Must decal previous reservations. : TODO !");
					bringForwardPutBackAndAdd(r);
					return true;
				} else
					return false; //
			}
		}
		Reservation r2 = table.get(tMin);
		if (r2 != null) {
			if (!r2.getStraddleCarrierId().equals(r.getStraddleCarrierId())) {
				if (r.getPriority() > r2.getPriority()) {
					// System.out.println("Trying to know if we can make reservation "
					// + r);
					// System.out.println("Must decal current and next reservations.");
					bringForwardPutBackAndAdd(r);
					return true;
				} else
					return false;
			} else {
				toUnreserve.add(r2);
			}
		}

		Time tAfter = table.higherKey(tMin);
		if (tAfter != null) {
			Reservation after = table.get(tAfter);
			// if (after starts before current ends) => cannot make reservation
			try {
				if (after.getStraddleCarrierId().equals(r.getStraddleCarrierId())) {
					if (after.getTimeWindow().getMin().compareTo(r.getTimeWindow().getMax()) <= 0) {
						// Pas ok
						if (r.getPriority() > after.getPriority()) {
							// System.out.println("Trying to know if we can make reservation "
							// + r);
							// System.out.println("Must decal next reservations.");
							// System.out.println("Before decal next reservations : \n"
							// + this);
							bringForwardPutBackAndAdd(r);
							// System.out.println("After decal next reservations : \n"
							// + this);
							return true;
						} else
							return false;
					}
				} else {
					toUnreserve.add(after);
				}
			} catch (NullPointerException e) {
				e.printStackTrace();
				log.error("After = " + after + " R = " + r);
				System.exit(ReturnCodes.EXIT_RESERVATION_ERROR.getCode());
			}
		}

		for (Reservation rTmp : toUnreserve) {
			Terminal.getInstance().unreserve(rTmp);
		}

		return true;
	}

	/**
	 * Get the first available time window for the given reservation on the
	 * current road
	 * 
	 * @param r
	 *            Reservation concerned
	 * @return The first available time window for the given reservation on the
	 *         current road @
	 */
	public TimeWindow getFirstAvailableTimeWindow(Reservation r) {
		Reservation tmp = new Reservation(r.getDate(), "" + r.getStraddleCarrierId(), "" + r.getRoadId(), new TimeWindow(new Time(r.getTimeWindow()
				.getMin()), new Time(r.getTimeWindow().getMax())), r.getPriority());

		while (!canMakeReservation(tmp)) {
			Time tMin2 = new Time(tmp.getTimeWindow().getMin().toStep() + 1);
			Time tMax2 = new Time(tmp.getTimeWindow().getMax().toStep() + 1);
			TimeWindow tw = new TimeWindow(tMin2, tMax2);
			tmp = new Reservation(r.getDate(), r.getStraddleCarrierId(), r.getRoadId(), tw, tmp.getPriority());
		}
		return new TimeWindow(tmp.getTimeWindow().getMin(), tmp.getTimeWindow().getMax());
	}

	/**
	 * Get a reservation starting at the given time
	 * 
	 * @param t
	 *            Starting time of the reservation to get
	 * @return A reservation starting at the given time
	 */
	public Reservation getReservation(Time t) {
		return table.get(t);
	}

	/**
	 * Get the list of the reservations
	 * 
	 * @return List of the reservations
	 */
	public ArrayList<Reservation> getReservations() {
		return new ArrayList<Reservation>(table.values());
	}

	public Reservation removeReservation(Reservation r) {
		Reservation r2 = table.remove(r.getTimeWindow().getMin());
		if (r2 == null) {
			log.warn("No reservation has been found on " + this.road.getId() + " for " + r.getStraddleCarrierId() + " starting at "
					+ r.getTimeWindow().getMin());
			log.error(new Exception());
			return r2;
		}

		endTimeTable.remove(r.getTimeWindow().getMax());
		ArrayList<Time> l = index.get(r.getStraddleCarrierId());
		if (l != null) {
			for (int i = 0; i < l.size(); i++) {
				if (l.get(i).equals(r.getTimeWindow().getMin())) {
					l.remove(i);
					break;
				}
			}
			if (l.size() > 0)
				index.put(r.getStraddleCarrierId(), l);
		}

		return r2;
	}

	/**
	 * Remove the reservation of the given straddle carrier at the given time
	 * 
	 * @param straddleCarrierID
	 *            ID of the straddle carrier concerned by the unreservation
	 * @param at
	 *            Start time of the reservation to cancel
	 * @return <b>true</b> if the reservation has been removed, <b>false</b>
	 *         otherwise
	 */
	public boolean removeReservation(String straddleCarrierID, Time at) {
		Reservation r = table.remove(at);
		if (r == null) {
			System.out.println("No reservation has been found on " + this.road.getId() + " for " + straddleCarrierID + " starting at " + at);
			new Exception().printStackTrace();
			return false;
		}

		endTimeTable.remove(r.getTimeWindow().getMax());
		ArrayList<Time> l = index.get(straddleCarrierID);
		if (l != null) {
			for (int i = 0; i < l.size(); i++) {
				if (l.get(i).equals(at)) {
					l.remove(i);
					break;
				}
			}
			if (l.size() > 0)
				index.put(straddleCarrierID, l);
		}

		return r != null;
	}

	/**
	 * Remove the reservation starting at the given time
	 * 
	 * @param t
	 *            Starting time of the reservation to get
	 * @return The removed reservation, <b>null</b> if such a reservation can't
	 *         be found
	 */
	public Reservation removeReservation(Time t) {

		Reservation r = table.remove(t);
		if (r != null) {
			endTimeTable.remove(r.getTimeWindow().getMax());
			ArrayList<Time> l = index.get(r.getStraddleCarrierId());
			if (l != null) {
				for (int i = 0; i < l.size(); i++) {
					if (l.get(i).equals(t)) {
						l.remove(i);
						break;
					}
				}
				if (l.size() > 0)
					index.put(r.getStraddleCarrierId(), l);
			}
		}
		return r;
	}

	/**
	 * String representation of the reservation table
	 */
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("--- RESERVATIONS OF " + road.getId() + " : \n");
		for (Time t : table.keySet()) {
			sb.append(t + " -> " + table.get(t) + "\n");
		}
		sb.append("--- END OF RESERVATIONS OF " + road.getId() + "---");
		return sb.toString();
	}

	public Reservation getCurrentReservation(Time t, String straddleID) {
		ArrayList<Time> l = index.get(straddleID);
		for (Time time : l) {
			Reservation r = table.get(time);
			if (r.getTimeWindow().getMin().getInSec() >= t.getInSec() && r.getTimeWindow().getMax().getInSec() <= t.getInSec()) {
				return r;
			}
		}
		return null;
	}

	public Reservation getNextReservation(Time time, String straddleID) {
		Time from = new Time(time, new Time(1), false);
		Entry<Time, Reservation> e = table.higherEntry(from);
		while (e != null && !e.getValue().getStraddleCarrierId().equals(straddleID)) {
			e = table.higherEntry(e.getKey());
		}

		if (e == null)
			return null;
		else {
			Reservation r = e.getValue();
			if (r.getStraddleCarrierId().equals(straddleID))
				return r;
			else
				return null;
		}
	}

}