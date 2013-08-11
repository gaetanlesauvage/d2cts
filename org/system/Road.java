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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import org.positioning.Coordinates;
import org.positioning.LaserHead;
import org.positioning.LaserSystem;
import org.system.container_stocking.Bay;
import org.system.container_stocking.BayCrossroad;
import org.time.Time;
import org.util.Location;
import org.vehicles.StraddleCarrier;

/**
 * Road. A road links two crossroads and can contains several road points
 * 
 * @author Ga&euml;tan Lesauvage
 * @since 2009
 */
public class Road {
	/**
	 * Origin
	 */
	private Crossroad origin;
	/**
	 * Destination
	 */
	private Crossroad destination;
	/**
	 * True if the road is directed, false otherwise
	 */
	private boolean directed;
	/**
	 * ID of the road
	 */
	private String id;
	/**
	 * Length of the road in meters
	 */
	private double length;
	/**
	 * List of the road points of the road
	 */
	private List<RoadPoint> points;
	/**
	 * Map of the roadpoints according to their IDs
	 */
	private Map<String, Integer> dicPoints;
	/**
	 * Vectors coordinates of the different segments of the road. <b>Warning :
	 * </b> segments should be stocked by order from the origin of the road to
	 * the destination
	 */
	private List<Coordinates> vectorsCoordinates;
	/**
	 * Entrance or exit points of lanes connected to this road
	 */
	private Map<String, BayCrossroad> laneCrossroads;

	/**
	 * A road is named by an unique ID, an origin and a destination crossroad, a
	 * list of roadpoints and an orientation
	 * 
	 * @param id
	 *            Unique ID
	 * @param origin
	 *            Crossroad from where the road will start
	 * @param destination
	 *            Crossroad towards where the road will end
	 * @param points
	 *            Sorted list of road points connected to this road
	 * @param directed
	 *            <b>true</b> if it is a one-way road, <b>false</b> otherwise.
	 */
	public Road(String id, Crossroad origin, Crossroad destination,
			ArrayList<RoadPoint> points, boolean directed) {
		this.id = id;
		this.origin = origin;
		Terminal terminal = Terminal.getInstance();

		this.destination = destination;
		terminal.addConnexCrossroad(origin.getId(), destination.getId(), id);
		if (!directed)
			terminal.addConnexCrossroad(destination.getId(), origin.getId(), id);
		this.points = new ArrayList<>(points.size());
		this.dicPoints = new HashMap<>(points.size());
		for (RoadPoint p : points) {
			addRoadPoint(p);
		}
		this.directed = directed;
		laneCrossroads = new HashMap<>();
		updateLength();
	}

	/**
	 * A straight road is named by an unique ID, an origin and a destination
	 * crossroad and an orientation
	 * 
	 * @param id
	 *            Unique ID
	 * @param origin
	 *            Crossroad from where the road will start
	 * @param destination
	 *            Crossroad towards where the road will end
	 * @param directed
	 *            <b>true</b> if it is a one-way road, <b>false</b> otherwise.
	 */
	public Road(String id, Crossroad origin, Crossroad destination,
			boolean directed) {
		this.id = id;
		this.origin = origin;
		this.destination = destination;
		Terminal terminal = Terminal.getInstance();

		this.points = new ArrayList<>();
		this.dicPoints = new HashMap<>();
		this.directed = directed;
		if(origin == null)
			System.err.println("OUch!!!");
		if(destination == null)
			System.err.println("Atchh!!!");
		terminal.addConnexCrossroad(origin.getId(), destination.getId(), id);
		if (!directed)
			terminal.addConnexCrossroad(destination.getId(), origin.getId(), id);
		laneCrossroads = new HashMap<>();

		updateLength();
	}

	/**
	 * Add a lane crossroad to this road
	 * 
	 * @param laneCrossroad
	 *            LaneCrossroad to add
	 */
	public void addLaneCrossroad(BayCrossroad laneCrossroad) {
		addRoadPoint(getInsertionIndexOfRoadPoint(laneCrossroad), laneCrossroad);
		laneCrossroads.put(laneCrossroad.id, laneCrossroad);
	}

	/**
	 * Add a road point at the given index in the list of road points
	 * 
	 * @param index
	 *            Index of the road point insertion in the list
	 * @param rp
	 *            The road point to add
	 */
	public void addRoadPoint(int index, RoadPoint rp) {
		Terminal terminal = Terminal.getInstance();
		
		if (points.size() == 0) {
			terminal.removeConnexCrossroad(origin.getId(), destination.getId());
			if (!directed)
				terminal.removeConnexCrossroad(destination.getId(),
						origin.getId());
		} else {
			terminal.removeConnexCrossroad(origin.getId(), points.get(0)
					.getId());
			if (!directed) {
				terminal.removeConnexCrossroad(points.get(0).getId(),
						origin.getId());
			}
			for (int i = 1; i < points.size(); i++) {
				if (!directed) {
					terminal.removeConnexCrossroad(points.get(i).getId(),
							points.get(i - 1).getId());
				}
				terminal.removeConnexCrossroad(points.get(i - 1).getId(),
						points.get(i).getId());
			}
			terminal.removeConnexCrossroad(points.get(points.size() - 1)
					.getId(), destination.getId());
			if (!directed) {
				terminal.removeConnexCrossroad(destination.getId(),
						points.get(points.size() - 1).getId());
			}
		}

		// Add RoadPoint
		points.add(index, rp);
		dicPoints.put(rp.getId(), index);
		// Links !
		terminal.addConnexCrossroad(origin.getId(), points.get(0).getId(), id);

		if (!directed) {
			terminal.addConnexCrossroad(points.get(0).getId(), origin.getId(),
					id);
		}
		for (int i = 1; i < points.size(); i++) {
			if (!directed) {
				terminal.addConnexCrossroad(points.get(i).getId(),
						points.get(i - 1).getId(), id);
			}
			terminal.addConnexCrossroad(points.get(i - 1).getId(), points
					.get(i).getId(), id);
		}
		terminal.addConnexCrossroad(points.get(points.size() - 1).getId(),
				destination.getId(), id);
		if (!directed) {
			terminal.addConnexCrossroad(destination.getId(),
					points.get(points.size() - 1).getId(), id);
		}

		updateLength();

	}

	/**
	 * Add a road point to the end of the list of road points
	 * 
	 * @param rp
	 *            The road points to add
	 */
	public void addRoadPoint(RoadPoint rp) {
		addRoadPoint(points.size(), rp);
	}

	/**
	 * Compute the vector coordinates of the segments of the road
	 */
	private void computeVectorsCoordinates() {
		vectorsCoordinates = new ArrayList<Coordinates>(1 + points.size());
		Coordinates from = origin.getLocation();

		for (int i = 0; i < points.size(); i++) {
			RoadPoint rp = points.get(i);
			Coordinates to = rp.getLocation();
			vectorsCoordinates.add(new Coordinates(to.x - from.x,
					to.y - from.y, to.z - from.z));
			from = to;
		}
		Coordinates to = destination.getLocation();
		vectorsCoordinates.add(new Coordinates(to.x - from.x, to.y - from.y,
				to.z - from.z));
	}

	/**
	 * Tests if a road point belongs to the road
	 * 
	 * @param rpID
	 *            ID of the road point
	 * @return <b>true</b> if the given road point belongs to the road,
	 *         <b>false</b> otherwise
	 */
	public boolean contains(String rpID) {
		if (rpID.equals(origin.getId()))
			return true;
		if (rpID.equals(destination.getId()))
			return true;
		for (RoadPoint rp : points)
			if (rp.getId().equals(rpID))
				return true;
		return false;
	}

	/**
	 * Tests if a given location belongs to this road
	 * 
	 * @param coordinates
	 *            Coordinates to test
	 * @return <b>true</b> if the given road point belongs to the road,
	 *         <b>false</b> otherwise
	 */
	public boolean contains(Coordinates coordinates) {
		double epsilon = 0.01;
		// C is on AB if AC, AB are collinear
		double u1 = coordinates.x - origin.getLocation().x;
		double u2 = coordinates.y - origin.getLocation().y;
		computeVectorsCoordinates();
		for (Coordinates vector : vectorsCoordinates) {
			double v1 = vector.x;
			double v2 = vector.y;

			double u1v2 = u1 * v2;
			double u2v1 = u2 * v1;

			if (u1v2 - epsilon <= u2v1 && u1v2 + epsilon >= u2v1)
				return true;
		}
		return false;
	}

	/**
	 * Get the destination crossroad
	 * 
	 * @return Destination crossroad
	 */
	public Crossroad getDestination() {
		return destination;
	}

	/**
	 * Get the origin crossroad
	 * 
	 * @return Origin crossroad
	 */
	public String getDestinationId() {
		return destination.id;
	}

	/**
	 * Get the ID of the road
	 * 
	 * @return ID of the road
	 */
	public String getId() {
		return id;
	}

	/**
	 * Finds the ID of the destination road point of the segment located at the
	 * given rate on this road
	 * 
	 * @param rate
	 *            Rate of the road (belongs to 0..1) where the segment is
	 *            located
	 * @return ID of the destination road point of the segment located at the
	 *         given rate on this road
	 */
	public String getIdRoadPointDestination(double rate) {
		String id = destination.getId();
		RoadPoint before = destination;
		for (int i = points.size() - 1; i >= 0; i--) {
			RoadPoint rp = points.get(i);
			Coordinates l = rp.getLocation();
			double rpPourcent = Location.getPourcent(l, this);
			if (rpPourcent < rate)
				return before.getId();
			before = rp;
			id = rp.getId();
		}

		return id;
	}

	/**
	 * Finds the ID of the origin road point of the segment located at the given
	 * rate on this road
	 * 
	 * @param rate
	 *            Rate of the road (belongs to 0..1) where the segment is
	 *            located
	 * @return ID of the origin road point of the segment located at the given
	 *         rate on this road
	 */
	public String getIdRoadPointOrigin(double rate) {
		String id = origin.getId();
		RoadPoint before = origin;
		for (RoadPoint rp : points) {
			Coordinates l = rp.getLocation();
			double rpPourcent = Location.getPourcent(l, this);
			if (rpPourcent >= rate)
				return before.getId();
			before = rp;
			id = rp.getId();
		}

		return id;
	}

	/**
	 * Compute the insertion index of a road point on this road
	 * 
	 * @param rp
	 *            RoadPoint to add
	 * @return The insertion index of a road point on this road
	 */
	public int getInsertionIndexOfRoadPoint(RoadPoint rp) {
		double pPourcent = Location.getPourcent(rp.getLocation(), this);
		int i = 0;
		for (RoadPoint existingRP : points) {
			double rpPourcent = Location.getPourcent(existingRP.getLocation(),
					this);
			if (rpPourcent >= pPourcent) {
				return i;
			}
			i++;
		}
		return i;
	}

	/**
	 * Get the intersection crossroad, in its RoadPoint form, of the given road
	 * with the current one
	 * 
	 * @param road
	 *            Intersected road
	 * @return Intersection crossroad, in its RoadPoint form, of the given road
	 *         with the current one
	 */
	public RoadPoint getIntersectNode(Road road) {
		if (road.getOriginId().equals(origin.getId())
				|| road.getOriginId().equals(destination.getId()))
			return road.getOrigin();
		else {
			if (road.getDestinationId().equals(origin.getId())
					|| road.getDestinationId().equals(destination.getId()))
				return road.getDestination();
			else {
				for (RoadPoint rp : road.getPoints()) {
					if (rp.getId().equals(origin.getId())
							|| rp.getId().equals(destination.getId()))
						return rp;
				}

			}
		}
		return null;
	}

	/**
	 * Get the length of the road in meters
	 * 
	 * @return Length of the road in meters
	 */
	public double getLength() {
		return length;
	}

	/**
	 * Get the segment local rate of the given road global rate
	 * 
	 * @param globalRate
	 *            Rate on the road (belongs to 0..1)
	 * @return Local rate (belongs to 0..1) on the segment located at the given
	 *         global rate
	 */
	public double getLocalRate(double globalRate) {
		RoadPoint r1 = origin;
		for (RoadPoint r2 : points) {
			double r2Pourcent = Location.getAccuratePourcent(r2.getLocation(),
					this);
			if (r2Pourcent >= globalRate) {
				double r1Pourcent = Location.getAccuratePourcent(
						r1.getLocation(), this);
				double gap = globalRate - r1Pourcent;
				double r1r2Length = Location.getLength(r1.getLocation(),
						r2.getLocation());
				double gapLength = gap * length;
				return gapLength / r1r2Length;
			}
			r1 = r2;
		}
		if (r1.getId().equals(origin.getId()))
			return globalRate;
		else {
			RoadPoint r2 = destination;
			double r1Pourcent = Location.getAccuratePourcent(r1.getLocation(),
					this);
			double gap = globalRate - r1Pourcent;
			double r1r2Length = Location.getLength(r1.getLocation(),
					r2.getLocation());
			double gapLength = gap * length;
			return gapLength / r1r2Length;
		}
	}

	/**
	 * Return the index of a node on the road from the origin crossroad to the
	 * destination one
	 * 
	 * @param nodeID
	 *            Node ID
	 * @return -1 : origin crossroad, roadpoints.size() = destination crossroad,
	 *         [0 .. roadpoints.size()[ = a road point
	 */
	public int getNodeIndex(String nodeID) {
		if (nodeID.equals(origin.getId()))
			return -1;
		else if (nodeID.equals(destination.getId()))
			return points.size();
		else {
			for (int i = 0; i < points.size(); i++) {
				if (points.get(i).getId().equals(nodeID))
					return i;
			}
		}
		return -2;
	}

	/**
	 * Get the opposite crossroad ID.
	 * 
	 * @param id
	 *            Either the origin crossroad ID or the destination one.
	 * @return The destination crossroad ID if the given ID is the origin ID,
	 *         the origin ID otherwise.
	 */
	public String getOpposite(String id) {
		return origin.id.equals(id) ? destination.id : origin.id;
	}

	/**
	 * Get the origin crossroad
	 * 
	 * @return Origin crossroad
	 */
	public Crossroad getOrigin() {
		return origin;
	}

	/**
	 * Get the origin crossroad ID
	 * 
	 * @return Origin crossroad ID
	 */
	public String getOriginId() {
		return origin.id;
	}

	/**
	 * Get the list of road points
	 * 
	 * @return the list of road points
	 */
	public List<RoadPoint> getPoints() {
		return points;
	}

	/**
	 * Get the coordinates of the road point which have the specified ID
	 * 
	 * @param id
	 *            ID of the road point
	 * @return Coordinates of the road point which have the specified ID
	 */
	public Coordinates getRoadPointCoordinates(String id) {
		if (id.equals(origin.getId()))
			return origin.getLocation();
		else if (id.equals(destination.getId()))
			return destination.getLocation();
		for (RoadPoint p : points) {
			if (p.getId().equals(id))
				return p.getLocation();
		}
		return null;
	}

	/**
	 * Tests if this road is one way or not
	 * 
	 * @return <b>true</b> if it is a one way road (origin->destination),
	 *         <b>false</b> otherwise (origin<->destination)
	 */
	public boolean isDirected() {
		return directed;
	}

	/**
	 * Get the string representation of the reservation table of this road
	 * 
	 * @return String representation of the reservation table of this road
	 */
	public String getReservationsTableToString() {
		Reservations rs = Terminal.getInstance().getReservations(id);
		if (rs == null) {
			rs = new Reservations(this);
			Terminal.getInstance().putReservations(id, rs);

		}
		return rs.toString();
	}

	/**
	 * String representation of the road
	 */
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append(id + ": " + origin.getId());
		if (directed)
			sb.append(" --> ");
		else
			sb.append(" <-> ");
		sb.append(destination.getId());
		return sb.toString();
	}

	/**
	 * Recompute the length attribute
	 */
	private void updateLength() {
		Coordinates from = origin.getLocation();
		double overallLength = 0;
		for (int i = 0; i < points.size(); i++) {
			RoadPoint rp = points.get(i);
			Coordinates to = rp.getLocation();
			overallLength += Math
					.sqrt(Math.pow(to.x - from.x, 2f)
							+ Math.pow(to.y - from.y, 2f)
							+ Math.pow(to.z - from.z, 2f));
			from = to;
		}

		Coordinates to = destination.getLocation();
		overallLength += Math.sqrt(Math.pow(to.x - from.x, 2f)
				+ Math.pow(to.y - from.y, 2f) + Math.pow(to.z - from.z, 2f));
		length = overallLength;
		computeVectorsCoordinates();
	}

	/**
	 * Get an estimate length of the road (euclidian distance)!
	 * 
	 * @param fromNodeId
	 *            Origin node ID
	 * @param toNodeId
	 *            Destination node ID
	 * @return linear distance between the two given roadpoints
	 */
	public double getLengthBetween(String fromNodeId, String toNodeId) {
		Coordinates from = getRoadPointCoordinates(fromNodeId);
		Coordinates to = getRoadPointCoordinates(toNodeId);
		return Location.getLength(from, to);
	}

	/**
	 * Get the accurate signed length between two nodes
	 * 
	 * @param fromNodeId
	 *            Origin node ID
	 * @param toNodeId
	 *            Destination node ID
	 * @return Accurate signed length between two nodes
	 */
	public double getExactLengthBetweenWithSign(String fromNodeId,
			String toNodeId) {
		int indexFrom = getNodeIndex(fromNodeId);
		int indexTo = getNodeIndex(toNodeId);
		int way = 1;
		if (indexTo < indexFrom) {
			int tmp = indexFrom;
			indexFrom = indexTo;
			indexTo = tmp;
			way = -1;
		}
		double overallLength = 0;
		if (indexFrom == -1) {
			if (indexTo == points.size())
				return length;
			else {
				Coordinates from = origin.getLocation();

				for (int i = 0; i <= indexTo; i++) {
					RoadPoint rp = points.get(i);
					Coordinates to = rp.getLocation();
					overallLength += Math.sqrt(Math.pow(to.x - from.x, 2f)
							+ Math.pow(to.y - from.y, 2f)
							+ Math.pow(to.z - from.z, 2f));
					from = to;
				}
			}
		} else {
			if (indexTo == points.size()) {
				Coordinates from = points.get(indexFrom).getLocation();

				for (int i = indexFrom + 1; i < points.size(); i++) {
					RoadPoint rp = points.get(i);
					Coordinates to = rp.getLocation();
					overallLength += Math.sqrt(Math.pow(to.x - from.x, 2f)
							+ Math.pow(to.y - from.y, 2f)
							+ Math.pow(to.z - from.z, 2f));
					from = to;
				}
				Coordinates to = destination.getLocation();
				overallLength += Math.sqrt(Math.pow(to.x - from.x, 2f)
						+ Math.pow(to.y - from.y, 2f)
						+ Math.pow(to.z - from.z, 2f));
			} else {
				Coordinates from = points.get(indexFrom).getLocation();
				for (int i = indexFrom + 1; i <= indexTo; i++) {
					RoadPoint rp = points.get(i);
					Coordinates to = rp.getLocation();
					overallLength += Math.sqrt(Math.pow(to.x - from.x, 2f)
							+ Math.pow(to.y - from.y, 2f)
							+ Math.pow(to.z - from.z, 2f));
					from = to;
				}
			}
		}
		return way * overallLength;
	}

	/**
	 * Get the accurate length between two nodes
	 * 
	 * @param fromNodeId
	 *            Origin node ID
	 * @param toNodeId
	 *            Destination node ID
	 * @return Accurate length between two nodes
	 */
	public double getExactLengthBetween(String fromNodeId, String toNodeId) {
		if (fromNodeId.equals(toNodeId))
			return 0;
		List<RoadPoint> l = getRoadPoints();
		String start = "";
		String to = "";

		RoadPoint previous = null;
		double length = 0;
		for (int i = 0; i < l.size(); i++) {
			RoadPoint rp = l.get(i);
			if (start.equals("")) {
				if (rp.getId().equals(fromNodeId)) {
					start = fromNodeId;
					to = toNodeId;
				} else {
					if (rp.getId().equals(toNodeId)) {
						start = toNodeId;
						to = fromNodeId;
					}
				}
			}

			if (!start.equals("")) {
				if (previous != null) {
					length += Location.getLength(rp.getLocation(),
							previous.getLocation());
				}
				if (rp.getId().equals(to)) {
					return length;
				}
				previous = rp;
			}
		}
		return length;
	}

	/**
	 * Get the list of road points of the and the origin and destination
	 * crossroads
	 * 
	 * @return list of road points of the and the origin and destination
	 *         crossroads
	 */
	public List<RoadPoint> getRoadPoints() {
		ArrayList<RoadPoint> l = new ArrayList<RoadPoint>(points.size() + 2);
		l.add(origin);
		for (RoadPoint rp : points)
			l.add(rp);
		l.add(destination);
		return l;
	}

	/**
	 * Get the nearest road point of the list located before the given rate
	 * 
	 * @param rate
	 *            Belongs to [0..1]
	 * @return Nearest road point of the list located before the given rate
	 */
	public RoadPoint getPreviousRoadPoint(double rate) {
		List<RoadPoint> rpts = getRoadPoints();
		RoadPoint previous = origin;
		for (int i = 0; i < rpts.size(); i++) {
			RoadPoint rp = rpts.get(i);
			double p = Location.getAccuratePourcent(rp.getLocation(), this);
			if (p >= rate)
				return previous;
			else
				previous = rp;
		}
		return previous;
	}

	/**
	 * Get the nearest road point of the list located after the given rate
	 * 
	 * @param rate
	 *            Belongs to [0..1]
	 * @return Nearest road point of the list located after the given rate
	 */
	public RoadPoint getNextRoadPoint(double rate) {
		List<RoadPoint> rpts = getRoadPoints();
		RoadPoint next = destination;
		for (int i = rpts.size() - 1; i >= 0; i--) {
			RoadPoint rp = rpts.get(i);
			double p = Location.getAccuratePourcent(rp.getLocation(), this);
			if (p <= rate)
				return next;
			else
				next = rp;
		}
		return next;
	}

	/**
	 * Get the waiting time of a vehicle arriving at origin crossroad at time t
	 * according to a given priority
	 * 
	 * @param vehicle
	 *            Vehicle concerned
	 * @param t
	 *            Arrival time at the entrance of the road
	 * @param priority
	 *            Priority of the vehicle (Reservation.PRIORITY_GO_THROUGH,
	 *            Reservation.PRIORITY_GO_IN, Reservation.PRIORITY_GO_OUT)
	 * @return The waiting time (in seconds) of a vehicle arriving at origin
	 *         crossroad at time t according to a given priority @
	 */
	public double getWaitingCost(StraddleCarrier vehicle, Time t, int priority) {
		return 0;
	}

	/**
	 * Get the waiting time of a vehicle arriving at origin crossroad at time t
	 * according to a given priority and which will take a certain time to go
	 * through the road
	 * 
	 * @param vehicle
	 *            Vehicle concerned
	 * @param t
	 *            Arrival time at the entrance of the road
	 * @param priority
	 *            Priority of the vehicle (Reservation.PRIORITY_GO_THROUGH,
	 *            Reservation.PRIORITY_GO_IN, Reservation.PRIORITY_GO_OUT)
	 * @param duration
	 *            Time duration while the vehicle will drive on the road
	 * @return The waiting time (in seconds) of a vehicle arriving at origin
	 *         crossroad at time t according to a given priority @
	 */
	public double getWaitingCost(StraddleCarrier vehicle, Time t,
			double duration, int priority) {

		return 0;
	}

	// TODO to be tested
	public boolean isDrivable() {

		Location current = new Location(this, 0.0, true);
		for (LaserHead lh : LaserSystem.getInstance().getHeads()) {
			List<String> list = lh.getVisibleStraddleCarriers();
			for (String id : list) {
				Location l = lh.getLocation(id);
				if (l != null
						&& l.getRoad().getId()
								.equals(current.getRoad().getId())
						&& l.getPourcent() > 0 && l.getPourcent() < 1.0) {
					return false;
				}
			}
		}
		return true;
	}

	/**
	 * Get the list of the straddle carriers IDs driving on the road
	 * 
	 * @return The list of the straddle carriers IDs driving on the road @
	 */
	public ArrayList<String> getTraffic() {
		HashMap<String, String> result = new HashMap<String, String>();
		Location current = new Location(this, 0.0, true);
		for (LaserHead lh : LaserSystem.getInstance().getHeads()) {
			List<String> list = lh.getVisibleStraddleCarriers();
			for (String id : list) {
				if (!result.containsKey(id)) {
					Location l = lh.getLocation(id);

					if (l != null
							&& l.getRoad().getId()
									.equals(current.getRoad().getId())
							&& l.getPourcent() > 0 && l.getPourcent() < 1.0) {
						result.put(id, id);
					}
				}
			}
		}
		if (this instanceof Bay && result.size() > 1) {
			System.err.println("More than 1 straddle carrier on lane " + id
					+ " : ");
			for (String s : result.values())
				System.err.println("\t" + s);
			Scanner scan = new Scanner(System.in);
			scan.nextLine();
			scan.close();
		}
		return new ArrayList<String>(result.values());
	}

	public void destroy() {
		// destination.destroy();
		// dicPoints.clear();
		// for(LaneCrossroad lc : laneCrossroads.values()) lc.destroy();
		// origin.destroy();
		// for(RoadPoint rp : points)rp.destroy();
		// for(Coordinates c : vectorsCoordinates) c.destroy();

		// laneCrossroads.clear();
		// points.clear();
		// vectorsCoordinates.clear();

		laneCrossroads = null;
		points = null;
		vectorsCoordinates = null;
	}

	public List<BayCrossroad> getLaneCrossroads() {
		return new ArrayList<BayCrossroad>(laneCrossroads.values());
	}
}
