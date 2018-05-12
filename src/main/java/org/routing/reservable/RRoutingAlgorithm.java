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
package org.routing.reservable;

import java.util.List;

import org.display.TextDisplay;
import org.exceptions.NoPathFoundException;
import org.routing.Routing;
import org.routing.path.Path;
import org.routing.path.RoutingPath;
import org.system.Reservation;
import org.system.Road;
import org.system.RoadPoint;
import org.time.Time;
import org.time.TimeScheduler;
import org.util.Location;
import org.vehicles.StraddleCarrier;
import org.vehicles.models.SpeedCharacteristics;

public abstract class RRoutingAlgorithm implements Routing {

	protected String straddleCarrierId;
	protected String rmiBindingName;
	private boolean locked = false;
	protected boolean graphChanged = true;
	protected StraddleCarrier vehicle;

	private TextDisplay out;

	protected RRoutingAlgorithm(StraddleCarrier rsc, String rmiBindingName) {
		this.rmiBindingName = rmiBindingName;
		this.straddleCarrierId = rsc != null ? rsc.getId() : "";

		vehicle = rsc;
	}

	protected RRoutingAlgorithm(StraddleCarrier rsc, String rmiBindingName,
			TextDisplay uriRemoteDisplay) {
		this(rsc, rmiBindingName);
		setTextDisplay(uriRemoteDisplay);
	}

	/**
	 * First fitness to call : start <-> a && start <-> b
	 */
	public double fitness(StraddleCarrier vehicle, Location vehicleLocation,
			RoadPoint b, Time t) {
		double result;
		if (b == null)
			System.out.println("b is null : " + vehicle.getId() + " time " + t
					+ " vehicleLocation= " + vehicleLocation.getRoad().getId());
		if (vehicleLocation == null)
			System.out.println("vehicleLocation is null : " + vehicle.getId()
					+ " time " + t + " b= " + b.getId());
		if (vehicleLocation != null && vehicleLocation.getRoad() == null)
			System.out.println("vehicleLocation.getRoad is null : "
					+ vehicle.getId() + " time " + t + " b= " + b.getId());
		double bRate = Location.getAccuratePourcent(b.getLocation(),
				vehicleLocation.getRoad());
		double vRate = vehicleLocation.getPourcent();
		//Le chariot cavalier doit aller dans le sens de la route
		if (bRate > vRate || vRate == 1.0) {
			if (vRate == 1.0 && vehicleLocation.getRoad().isDirected())
				result = Double.POSITIVE_INFINITY;
			else {
				double distance = vehicleLocation.getLength(bRate
						- vehicleLocation.getPourcent());
				result = distance
						/ vehicle.getSpeed(vehicleLocation.getRoad(), t);
			}
			// Turn back ?
			if (!vehicleLocation.getDirection())
				result += vehicle.getModel().getSpeedCharacteristics()
						.getTurnBackTime();
			
		} else {//Le chariot doit aller dans le sens inverse de la route
			if (vehicleLocation.getRoad().isDirected()) //Or la route est à sens unique...
				result = Double.POSITIVE_INFINITY;
			else {
				double distance = vehicleLocation.getLength(vehicleLocation
						.getPourcent());
				result = distance
						/ vehicle.getSpeed(vehicleLocation.getRoad(), t);
				// Turn back ?
				if (vehicleLocation.getDirection())
					result += vehicle.getModel().getSpeedCharacteristics()
							.getTurnBackTime();
			}
		}
		// Waiting ?
		double w = vehicleLocation.getRoad().getWaitingCost(vehicle, t, result,
				Reservation.PRIORITY_GO_OUT);
		if (w > 0)
			System.out.println("W=" + w + " for " + vehicle.getId()
					+ " to go on " + vehicleLocation.getRoad().getId() + " at "
					+ t + " going out !");
		// result += vehicleLocation.getRoad().getWaitingCost(vehicle,
		// t,Reservation.PRIORITY_GO_OUT);
		result += w;
		return result;
	}

	/**
	 * Last fitness to call (C-goal D-goal)
	 */
	public Double[] fitness(StraddleCarrier vehicle, RoadPoint cCrossroad,
			Location b, Time t) {
		double weight;
		Road r = b.getRoad();
		/*
		 * if(! r.isOpenFor(straddleCarrierId)) { weight =
		 * Double.POSITIVE_INFINITY; } else{
		 */
		double rate = Location.getAccuratePourcent(cCrossroad.getLocation(), r);
		double gap = Math.abs(b.getPourcent() - rate);
		if (b.getPourcent() < rate) {
			// It means we come from the end of the road
			if (b.getRoad().isDirected())
				weight = Double.POSITIVE_INFINITY;
			else
				weight = b.getLength(gap);
		} else {
			weight = b.getLength(gap);

		}
		// }
		double duration = (weight / vehicle.getSpeed(r, t));
		double w = r.getWaitingCost(vehicle, t, duration,
				Reservation.PRIORITY_GO_IN);

		if (w > 0)
			System.out.println("warning : W=" + w + " for " + vehicle.getId()
					+ " to go on " + r.getId() + " at " + t
					+ " going in (duration =" + duration + "s [="
					+ new Time(duration) + "])! Reservations : "
					+ r.getReservationsTableToString());
		Double[] tResult = { duration + w, w };
		return tResult;
	}

	/**
	 * Second Fitnes to call (A-C A-D B-C B-D)
	 */
	public RoutingPath fitness(RoadPoint a, RoadPoint b, Time t) {
		RoutingPath p = getShortestPath(a.getId(), b.getId(), t);
		// double length = p.getLength();
		// return length;
		return p;
	}

	@Override
	public String getId() {
		return rmiBindingName;
	}

	@Override
	public TextDisplay getTextDisplay() {
		return out;
	}

	public String getRmiBindingName() {
		return rmiBindingName;
	}

	public abstract double getDistance(String from, String to);

	public abstract RoutingPath getShortestPath(String from, String to, Time t);

	public Path getShortestPath(org.util.Location a, org.util.Location b,
			Time fromTime) throws NoPathFoundException {
		// long before = System.currentTimeMillis();
		Time startTime = fromTime;
		RoadPoint aCrossroad = a.getRoad()
				.getPreviousRoadPoint(a.getPourcent());
		RoadPoint bCrossroad = a.getRoad().getNextRoadPoint(a.getPourcent());
		RoadPoint cCrossroad = b.getRoad()
				.getPreviousRoadPoint(b.getPourcent());
		RoadPoint dCrossroad = b.getRoad().getNextRoadPoint(b.getPourcent());
		SpeedCharacteristics speed = vehicle.getModel()
				.getSpeedCharacteristics();
		double directTime = Double.POSITIVE_INFINITY;
		RoadPoint directCrossroad = null;
		if (a.getRoad().isDirected()) {
			if (a.getPourcent() <= b.getPourcent()) {
				if (a.getPourcent() < b.getPourcent())
					directCrossroad = bCrossroad;
				else {
					if (a.getDirection())
						directCrossroad = bCrossroad;
					else
						directCrossroad = aCrossroad;
				}
			}

		} else {
			if (a.getPourcent() > b.getPourcent()) {
				directCrossroad = aCrossroad;
			} else if (a.getPourcent() < b.getPourcent())
				directCrossroad = bCrossroad;
			else {
				if (a.getDirection())
					directCrossroad = bCrossroad;
				else
					directCrossroad = aCrossroad;
			}
		}
		if (directCrossroad != null) {
			if (aCrossroad.getId().equals(cCrossroad.getId())
					&& bCrossroad.getId().equals(dCrossroad.getId())) {
				directTime = 0;
				if (a.getDirection()) {
					if (a.getPourcent() > b.getPourcent()) {
						// Demi tour
						directTime += speed.getTurnBackTime();
					}
					double duration = (a.getLength(Math.abs(a.getPourcent() - b.getPourcent())) / vehicle.getSpeed(a.getRoad(),	startTime));
					directTime += duration + a.getRoad().getWaitingCost(vehicle, startTime, duration, Reservation.PRIORITY_GO_OUT);
				} else {
					if (a.getPourcent() < b.getPourcent()) {
						// Demi tour
						directTime += speed.getTurnBackTime();
					}
					// Pas demi tour
					double duration = (a.getLength(Math.abs(a.getPourcent() - b.getPourcent())) / vehicle.getSpeed(a.getRoad(),	startTime));
					directTime += duration + a.getRoad().getWaitingCost(vehicle, startTime,	duration, Reservation.PRIORITY_GO_OUT);
				}
			}
		}
		/*
		 * System.out.println("A = "+aCrossroad);
		 * System.out.println("B = "+bCrossroad);
		 * System.out.println("C = "+cCrossroad);
		 * System.out.println("D = "+dCrossroad);
		 * System.out.print("DirectTime : "
		 * +directTime+"\nComputing start <-> A : ");
		 */
		Location vehicleLocation = a;
		double startToa = Double.POSITIVE_INFINITY;
		double startTob = Double.POSITIVE_INFINITY;
		double aToc = Double.POSITIVE_INFINITY;
		double aTod = Double.POSITIVE_INFINITY;
		double bToc = Double.POSITIVE_INFINITY;
		double bTod = Double.POSITIVE_INFINITY;
		Double[] acToGoal = { Double.POSITIVE_INFINITY, 0.0 };
		Double[] adToGoal = { Double.POSITIVE_INFINITY, 0.0 };
		Double[] bcToGoal = { Double.POSITIVE_INFINITY, 0.0 };
		Double[] bdToGoal = { Double.POSITIVE_INFINITY, 0.0 };

		startToa = fitness(vehicle, vehicleLocation, aCrossroad, startTime);
		// System.out.print(startToa+"\nComputing (start <-> B) : ");
		startTob = fitness(vehicle, vehicleLocation, bCrossroad, startTime);
		// System.out.print(startTob+"\n");
		RoutingPath aTocPath = null;
		RoutingPath aTodPath = null;

		if (!(startToa == Double.POSITIVE_INFINITY)) {
			Time aTime = new Time(startTime, new Time(startToa));
			// System.out.println("ATime = "+aTime+"\nComputing (A <-> C) : ");
			aTocPath = fitness(aCrossroad, cCrossroad, aTime);
			aToc = aTocPath.getCost();
			// System.out.print(aToc+"\nComputing (A <-> D) : ");
			aTodPath = fitness(aCrossroad, dCrossroad, aTime);
			aTod = aTodPath.getCost();
			// System.out.print(aTod+"\nComputing (B <-> C) : ");
		}
		// else System.out.print("No need !\n");
		RoutingPath bTocPath = null;
		RoutingPath bTodPath = null;
		if (startTob != Double.POSITIVE_INFINITY) {
			Time bTime = new Time(startTime, new Time(startTob));
			// System.out.println("BTime = "+bTime);
			bTocPath = fitness(bCrossroad, cCrossroad, bTime);
			// System.out.print("Computing (B <-> C) : ");
			bToc = bTocPath.getCost();
			// System.out.print(bToc+"\nComputing (B <-> D) : ");
			bTodPath = fitness(bCrossroad, dCrossroad, bTime);
			bTod = bTodPath.getCost();
			// System.out.print(bTod+"\n");
		}
		// else System.out.print("No need !\n");

		Time endFromAC, endFromAD;
		if (aToc != Double.POSITIVE_INFINITY) {
			endFromAC = new Time(startTime, new Time(startToa + aToc));

			// System.out.println("endFromAC = "+endFromAC);

			// System.out.print("Computing (AC <-> goal) : ");

			acToGoal = fitness(vehicle, cCrossroad, b, endFromAC);
			// System.out.print(acToGoal+"\n");

		}
		if (aTod != Double.POSITIVE_INFINITY) {
			endFromAD = new Time(startTime, new Time(startToa + aTod));
			// System.out.println("endFromAD = "+endFromAD);
			// System.out.print("Computing (AD <-> goal) : ");
			adToGoal = fitness(vehicle, dCrossroad, b, endFromAD);
			// System.out.print(adToGoal+"\n");
		}

		Time endFromBC, endFromBD;
		if (bToc != Double.POSITIVE_INFINITY) {
			endFromBC = new Time(startTime, new Time(startTob + bToc));
			// System.out.println("endFromBC = "+endFromBC);

			// System.out.print("Computing (BC <-> goal) : ");
			bcToGoal = fitness(vehicle, cCrossroad, b, endFromBC);
			// System.out.print(bcToGoal+"\n");

		}
		if (bTod != Double.POSITIVE_INFINITY) {
			endFromBD = new Time(startTime, new Time(startTob + bTod));
			// System.out.println("endFromBD = "+endFromBD);

			// System.out.print("Computing (BD <-> goal) : ");
			bdToGoal = fitness(vehicle, dCrossroad, b, endFromBD);
			// System.out.print(bdToGoal+"\n");
		}

		double l1 = startToa + aToc + acToGoal[0];
		double l2 = startToa + aTod + adToGoal[0];
		double l3 = startTob + bToc + bcToGoal[0];
		double l4 = startTob + bTod + bdToGoal[0];
		double[] t = { l1, l2, l3, l4, directTime };
		// System.out.println("l1 (START-A-C-GOAL) = "+l1);
		// System.out.println("l2 (START-A-D-GOAL) = "+l2);
		// System.out.println("l3 (START-B-C-GOAL) = "+l3);
		// System.out.println("l4 (START-B-D-GOAL) = "+l4);
		// System.out.println("direct = "+directTime);
		int min = 0;
		for (int i = min + 1; i < t.length; i++) {
			if (t[i] < t[min])
				min = i;
		}

		RoadPoint from = null;
		RoadPoint to = null;
		RoutingPath p = null;
		double lastWaiting = 0;
		switch (min) {
		case 0:
			from = aCrossroad;
			to = cCrossroad;
			p = aTocPath;
			lastWaiting = acToGoal[1];
			break;
		case 1:
			from = aCrossroad;
			to = dCrossroad;
			p = aTodPath;
			lastWaiting = adToGoal[1];
			break;
		case 2:
			from = bCrossroad;
			to = cCrossroad;
			p = bTocPath;
			lastWaiting = bcToGoal[1];
			break;
		case 3:
			from = bCrossroad;
			to = dCrossroad;
			p = bTodPath;
			lastWaiting = bdToGoal[1];
			break;
		case 4:
			p = new RoutingPath();
			break;
		}
		if (p == null) {
			System.out.println("min=" + min + " t[min]=" + t[min]);
			throw new NoPathFoundException(from.getId(), to.getId());
		}
	
		Time tStart = TimeScheduler.getInstance().getTime();
		Path result = new Path(a, b, tStart, p, vehicle, lastWaiting);
		result.simplify();

		// long after = System.currentTimeMillis();
		// long timeInMS = after - before;
		// System.out.println("SHORTEST PATH : "+result+" COMPUTED IN "+timeInMS+"ms");

		return result;
	}

	public Path getShortestPath(org.util.Location a, org.util.Location b)
			throws NoPathFoundException {
		return getShortestPath(a, b, TimeScheduler.getInstance().getTime());
	}

	public abstract void graphHasChanged();

	public boolean isLocked() {
		return locked;
	}

	public void lock() {
		locked = true;
	}

	@Override
	public void setTextDisplay(TextDisplay display) {
		this.out = display;
	}

	public void unlock() {
		locked = false;
	}

	public abstract double getCost(String from, String to,
			Time arrivalTimeOnFrom, int priority);

	public abstract double getSpeed(String runningNodeId,
			String neighborNodeId, Time arrivalTime);

	public abstract double getWaitingTime(String runningNodeId,
			String neighborNodeId, Time arrivalTime, int priority);

	public abstract List<String> getNodesIds();
}
