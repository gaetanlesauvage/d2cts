package org.scheduling;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import org.missions.Mission;
import org.scheduling.onlineACO.OnlineACOScheduler;
import org.vehicles.StraddleCarrier;

public class ScheduleTask<E extends ScheduleEdge> {
	public static final String SOURCE_ID = "SOURCE";

	/** Corresponding mission */
	protected Mission m;

	// Cost from P to D;
	private Map<String, Double> cost;
	private Double distance;

	// Map outgoing edges by target node ID
	protected SortedMap<String, E> outgoingEdges;

	public ScheduleTask(ScheduleTask<E> toCopy) {
		this(toCopy.m);
		for (E e : toCopy.outgoingEdges.values()) {
			addDestination(e);
		}
		for (String s : toCopy.cost.keySet()) {
			cost.put(s, toCopy.cost.get(s));
		}
		distance = toCopy.distance;
	}

	public ScheduleTask(Mission m) {
		this.m = m;
		outgoingEdges = new TreeMap<>();
		cost = new HashMap<>();
		distance = Double.NEGATIVE_INFINITY;
	}

	public void addDestination(E edge) {
		outgoingEdges.put(edge.getNodeTo().getID(), edge);
	}

	public String getID() {
		if (m != null) {
			return m.getId();
		} else
			return SOURCE_ID;
	}

	public void addCostFor(StraddleCarrier rsc) {
		for (ScheduleEdge e : outgoingEdges.values()) {
			e.addCost(rsc);
		}
	}

	public void setDistance(double distance) {
		this.distance = distance;
	}

	public void addMissionCostFor(String modelID, double cost) {
		this.cost.put(modelID, cost);
		// System.out.println("TSP:> "+getID()+" -> "+getID()+" COST = "+cost);
	}

	public Mission getMission() {
		return m;
	}

	public List<E> getDestinations() {
		return new ArrayList<E>(outgoingEdges.values());
	}

	public E getEdgeTo(ScheduleTask<E> n) {
		// System.err.println("5-2");
		String ID = n.getID();
		E e = outgoingEdges.get(ID);
		return e;
	}

	/**
	 * Get the travel time needed to go from P to D for a vehicle of the given
	 * model
	 * 
	 * @param modelID
	 *            Model of the vehicle
	 * @return Travel time needed to go from P to D for a vehicle of the given
	 *         model
	 */
	public double getCost(String modelID) {
		return cost.get(modelID);
	}

	/**
	 * Get the distance in meters for going from P to D
	 * 
	 * @return Distance in meters for going from P to D
	 */
	public double getDistance() {
		return distance;
	}

	public void removeEdgeTo(ScheduleTask<E> n) {
		ScheduleEdge e = outgoingEdges.remove(n.getID());
		if (e == null)
			new Exception("EDGE NOT FOUND").printStackTrace();
		e.destroy();
	}

	public boolean exists() {
		return cost != null;
	}

	public void destroy() {
		cost = null;
		if (outgoingEdges != null) {
			if (outgoingEdges.size() > 0) {

				if (this == MissionScheduler.SOURCE_NODE
						|| this == OnlineACOScheduler.getDepotNode()
						|| this == OnlineACOScheduler.getEndNode()) {
					for (ScheduleEdge e : outgoingEdges.values()) {
						e.destroy();
					}
					outgoingEdges.clear();
				} else {
					new Exception(
							"OUT MAP SHOULD BE EMPTY WHEN CALLING THE DESTROY METHOD OF A TSPNODE!")
							.printStackTrace();
					for (String s : outgoingEdges.keySet()) {
						System.err.println("STAY : " + s);
					}
				}
			}
			outgoingEdges = null;
		}
		m = null;
		distance = null;
	}
	
	@Override
	public String toString() {
		return m.getId();
	}
}