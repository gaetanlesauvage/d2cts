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
package org.routing;

import org.display.TextDisplay;
import org.exceptions.NoPathFoundException;
import org.routing.path.Path;
import org.routing.path.RoutingPath;
import org.system.Road;
import org.system.RoadPoint;
import org.system.container_stocking.Bay;
import org.time.Time;
import org.time.TimeScheduler;
import org.util.Location;
import org.vehicles.StraddleCarrier;
import org.vehicles.models.SpeedCharacteristics;

public abstract class RoutingAlgorithm implements Routing {
	protected String straddleCarrierId;
	protected String rmiBindingName;
	private boolean locked = false;
	protected boolean graphChanged = true;
	protected StraddleCarrier vehicle;

	private TextDisplay out;

	protected RoutingAlgorithm(StraddleCarrier rsc, String rmiBindingName) {
		this.rmiBindingName = rmiBindingName;
		this.straddleCarrierId = rsc.getId();
		vehicle = rsc;
	}

	protected RoutingAlgorithm(StraddleCarrier rsc, String rmiBindingName,
			TextDisplay display) {
		this(rsc, rmiBindingName);
		setTextDisplay(display);
	}

	/**
	 * First fitness to call : start <-> a && start <-> b
	 */
	public double fitness(SpeedCharacteristics speed, Location vehicleLocation,
			RoadPoint b) {
		double result;
		double bRate = Location.getAccuratePourcent(b.getLocation(),
				vehicleLocation.getRoad());
		double vRate = vehicleLocation.getPourcent();
		if (bRate > vRate || vRate == 1.0) {

			if (vRate == 1.0 && vehicleLocation.getRoad().isDirected())
				result = Double.POSITIVE_INFINITY;
			else {
				double distance = vehicleLocation.getLength(bRate
						- vehicleLocation.getPourcent());
				if (vehicleLocation.getRoad() instanceof Bay) {
					result = distance / speed.getLaneSpeed();
				} else {
					result = distance / speed.getSpeed();
				}
			}
			// Turn back ?
			if (!vehicleLocation.getDirection())
				result += speed.getTurnBackTime();
		} else {
			if (vehicleLocation.getRoad().isDirected())
				result = Double.POSITIVE_INFINITY;
			else {
				double distance = vehicleLocation.getLength(vehicleLocation
						.getPourcent());
				if (vehicleLocation.getRoad() instanceof Bay) {
					result = distance / speed.getLaneSpeed();
				} else {
					result = distance / speed.getSpeed();
				}
				// Turn back ?
				if (vehicleLocation.getDirection())
					result += speed.getTurnBackTime();
			}
		}

		return result;
	}

	/**
	 * Last fitness to call (C-goal D-goal)
	 */
	public double fitness(SpeedCharacteristics speed, RoadPoint cCrossroad,
			Location b) {
		double weight;
		Road r = b.getRoad();
		/*
		 * if(! r.isOpenFor(straddleCarrierId)) { weight =
		 * Double.POSITIVE_INFINITY; }
		 */
		// else{
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
		return weight
				/ (r instanceof Bay ? speed.getLaneSpeed() : speed.getSpeed());
	}

	/**
	 * Second Fitnes to call (A-C A-D B-C B-D)
	 */
	public RoutingPath fitness(SpeedCharacteristics speed, RoadPoint a,
			RoadPoint b, Location v) {
		RoutingPath p = getShortestPath(a.getId(), b.getId());
		// System.out.println("RoutingPath.length = "+p.getCost());
		return p;
	}

	public TextDisplay getDisplay() {
		return out;
	}

	public abstract double getDistance(String from, String to);

	public abstract RoutingPath getShortestPath(String from, String to);

	public Path getShortestPath(Location a, Location b, Time fromTime)
			throws NoPathFoundException {
		return getShortestPath(a, b);
	}

	public Path getShortestPath(Location a, Location b)
			throws NoPathFoundException {
		// long before = System.currentTimeMillis();
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
					directTime += a.getLength(Math.abs(a.getPourcent()
							- b.getPourcent()))
							/ (a.getRoad() instanceof Bay ? speed
									.getLaneSpeed() : speed.getSpeed());
				} else {
					if (a.getPourcent() < b.getPourcent()) {
						// Demi tour
						directTime += speed.getTurnBackTime();
					}
					// else{
					// TODO Check if it works fine without else{}
					// Pas demi tour
					directTime += a.getLength(Math.abs(a.getPourcent()
							- b.getPourcent()))
							/ (a.getRoad() instanceof Bay ? speed
									.getLaneSpeed() : speed.getSpeed());
					// }
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
		double cToGoal = Double.POSITIVE_INFINITY;
		double dToGoal = Double.POSITIVE_INFINITY;

		startToa = fitness(speed, vehicleLocation, aCrossroad);
		// System.out.print(startToa+"\nComputing (start <-> B) : ");
		startTob = fitness(speed, vehicleLocation, bCrossroad);
		// System.out.print(startTob+"\nComputing (A <-> C) : ");
		RoutingPath aTocPath = null;
		RoutingPath aTodPath = null;
		if (!(startToa == Double.POSITIVE_INFINITY)) {
			aTocPath = fitness(speed, aCrossroad, cCrossroad, vehicleLocation);
			aToc = aTocPath.getCost();
			// System.out.print(aToc+"\nComputing (A <-> D) : ");
			aTodPath = fitness(speed, aCrossroad, dCrossroad, vehicleLocation);
			aTod = aTodPath.getCost();
			// System.out.print(aTod+"\nComputing (B <-> C) : ");
		}
		// else System.out.print("No need !\nComputing (B <-> C) : ");

		RoutingPath bTocPath = null;
		RoutingPath bTodPath = null;
		if (startTob != Double.POSITIVE_INFINITY) {
			bTocPath = fitness(speed, bCrossroad, cCrossroad, vehicleLocation);
			bToc = bTocPath.getCost();
			// System.out.print(bToc+"\nComputing (B <-> D) : ");
			bTodPath = fitness(speed, bCrossroad, dCrossroad, vehicleLocation);
			bTod = bTodPath.getCost();
			// System.out.print(bTod+"\nComputing (C <-> goal) : ");
		}
		// else System.out.print("No need !\nComputing (C <-> goal) : ");
		cToGoal = fitness(speed, cCrossroad, b);
		// System.out.print(cToGoal+"\nComputing (D <-> goal) : ");
		dToGoal = fitness(speed, dCrossroad, b);
		// System.out.print(dToGoal+"\n");
		double l1 = startToa + aToc + cToGoal;
		double l2 = startToa + aTod + dToGoal;
		double l3 = startTob + bToc + cToGoal;
		double l4 = startTob + bTod + dToGoal;
		double[] t = { l1, l2, l3, l4, directTime };
		// System.out.println("l1 = "+l1);
		// System.out.println("l2 = "+l2);
		// System.out.println("l3 = "+l3);
		// System.out.println("l4 = "+l4);
		// System.out.println("direct = "+directTime);
		int min = 0;
		for (int i = min + 1; i < t.length; i++) {
			if (t[i] < t[min])
				min = i;
		}

		RoadPoint from = null;
		RoadPoint to = null;
		// ArrayList<RoutingNode> path = new ArrayList<RoutingNode>();

		RoutingPath p = null;
		switch (min) {
		case 0:
			from = aCrossroad;
			to = cCrossroad;
			p = aTocPath;
			break;
		case 1:
			from = aCrossroad;
			to = dCrossroad;
			p = aTodPath;
			break;
		case 2:
			from = bCrossroad;
			to = cCrossroad;
			p = bTocPath;
			break;
		case 3:
			from = bCrossroad;
			to = dCrossroad;
			p = bTodPath;
			break;
		case 4:
			// Create an empty path and then add the direct road
			p = new RoutingPath();
			break;
		}
		if (p == null) {

			throw new NoPathFoundException(from.getId(), to.getId());
		}
		Path path = new Path(a, b, TimeScheduler.getInstance().getTime(), p,
				vehicle);
		// TODO add simplify into path constructor or in RoutingPath too
		path.simplify();
		// long after = System.currentTimeMillis();
		// long timeInMS = after - before;
		// System.out.println("SHORTEST PATH : "+path+" COMPUTED IN "+timeInMS+"ms");
		return path;
	}

	/*
	 * private RoutingPath addOriginDestination(Location from, Location to,
	 * RoutingPath p, Speed speed) { RoutingPath path = new RoutingPath();
	 * 
	 * PathNode node = p.getNodePath().get(0); RoadPoint a =
	 * from.getRoad().getPreviousRoadPoint(from.getPourcent()); RoadPoint b =
	 * from.getRoad().getNextRoadPoint(from.getPourcent()); RoadPoint current =
	 * b; if(a.getId().equals(node.getNodeId())){ current = a; }
	 * 
	 * double currentRate = Location.getAccuratePourcent(current.getLocation(),
	 * from.getRoad()); double gap = currentRate-from.getPourcent(); boolean
	 * direction; double time = 0; if(from.getDirection()){ if(gap >= 0){
	 * direction = true;
	 * 
	 * } else{ direction = false; time += speed.getTurnBackTime(); } } else{
	 * if(gap <= 0){ direction = false; } else{ direction = true; time +=
	 * speed.getTurnBackTime(); } }
	 * System.out.println("Firts turn back time = "+time);
	 * 
	 * double length = from.getLength(Math.abs(gap)); if(from.getRoad()
	 * instanceof Lane) length /= speed.getLaneSpeed(); else length /=
	 * speed.getSpeed(); length += time;
	 * 
	 * PathNode rn = new PathNode(new Location(from.getRoad(), currentRate,
	 * direction));
	 * 
	 * path.add(0,rn); int i = 1; for(PathNode r : p.getNodePath()){
	 * 
	 * path.add(i++,r);
	 * 
	 * } String currentId = path.getNodePath().get(path.size()-1).getNodeId();
	 * Road r = to.getRoad(); RoadPoint c =
	 * r.getPreviousRoadPoint(to.getPourcent()); RoadPoint d =
	 * r.getNextRoadPoint(to.getPourcent()); if(c.getId().equals(currentId)){
	 * current = c; } else current = d;
	 * 
	 * currentRate = Location.getAccuratePourcent(current.getLocation(), r);
	 * double pourcentDestination = to.getPourcent(); direction = false;
	 * if(currentRate<=pourcentDestination) direction = true;
	 * 
	 * time = to.getLength(Math.abs(currentRate-pourcentDestination));
	 * 
	 * if(r instanceof Lane) time /= speed.getLaneSpeed(); else time /=
	 * speed.getSpeed(); length+=time; rn = new PathNode(new Location(r,
	 * pourcentDestination, direction)); path.add(path.size(),rn);
	 * path.setLength(length); System.out.println("lengthInTime = "+length);
	 * return path; }
	 */

	public abstract void graphHasChanged();

	public boolean isLocked() {
		return locked;
	}

	public void lock() {
		locked = true;
	}

	@Override
	public void setTextDisplay(TextDisplay out) {
		this.out = out;
	}

	@Override
	public TextDisplay getTextDisplay() {
		return out;
	}

	@Override
	public String getId() {
		return rmiBindingName;
	}

	public void unlock() {
		locked = false;
	}

	public void destroy() {
		vehicle = null;
		straddleCarrierId = null;
		rmiBindingName = null;
		out = null;
	}
}
