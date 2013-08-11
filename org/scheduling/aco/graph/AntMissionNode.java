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
package org.scheduling.aco.graph;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import org.conf.parameters.ReturnCodes;
import org.exceptions.EmptyResourcesException;
import org.graphstream.graph.ElementNotFoundException;
import org.graphstream.graph.IdAlreadyInUseException;
import org.graphstream.ui.spriteManager.Sprite;
import org.missions.Mission;
import org.missions.MissionKinds;
import org.scheduling.onlineACO.ACOParameters;
import org.scheduling.onlineACO.Ant;
import org.scheduling.onlineACO.AntHill;
import org.scheduling.onlineACO.OnlineACOScheduler;
import org.system.Terminal;
import org.system.container_stocking.ContainerKind;
import org.time.Time;
import org.vehicles.StraddleCarrier;
import org.vehicles.models.StraddleCarrierModel;

/**
 * Node of the ACO graph representing a mission.
 * 
 * @author Ga&euml;tan Lesauvage
 * @since 2011
 */
public class AntMissionNode extends AntNode {
	private ConcurrentHashMap<String, AntHill> matchingHills;

	private Sprite pie;
	private boolean pieLocated;

	private ConcurrentHashMap<String, HashMap<String, Ant>> population;

	private String uicolor;

	private static final String DEFAULT_MISSION_NODE_STYLE = "fill-mode: none; text-size: 10; shape: box; size-mode: fit; text-background-mode: plain; text-background-color: rgba(25,25,25,255);"
			+ " z-index:3; text-mode: normal; text-color: rgb(200,200,200); stroke-mode: plain; stroke-width: 3; stroke-color: black;";

	private static final int PIE_DISTANCE = 0;

	// ACO
	private ConcurrentHashMap<String, Double> pheromone;
	private ConcurrentHashMap<String, Double> pheromoneToSpread;
	private ConcurrentHashMap<String, Integer> colorIndex;

	private String colorMAX = "";
	private String previousColorMAX = "";

	private Time travelTimeFromDepot, travelTimeToDepot;

	public AntMissionNode(Mission m) {
		super(m);
		this.pieLocated = false;
		this.uicolor = DEFAULT_STYLE_COLOR;
		population = new ConcurrentHashMap<String, HashMap<String, Ant>>();
		if (m.getMissionKind() == MissionKinds.IN)
			node.addAttribute("ui.class", "missionIN");
		else if (m.getMissionKind() == MissionKinds.OUT)
			node.addAttribute("ui.class", "missionOUT");
		else if (m.getMissionKind() == MissionKinds.IN_AND_OUT)
			node.addAttribute("ui.class", "missionIN_AND_OUT");
		else if (m.getMissionKind() == MissionKinds.STAY)
			node.addAttribute("ui.class", "missionSTAY");

		node.addAttribute("label", getID());
		node.addAttribute("ui.class", "missionNode");
		node.addAttribute("ui.style", DEFAULT_MISSION_NODE_STYLE);

		matchingHills = new ConcurrentHashMap<String, AntHill>();
		pheromone = new ConcurrentHashMap<String, Double>();
		colorIndex = new ConcurrentHashMap<String, Integer>();

		HashMap<String, Double> pheromoneRates = new HashMap<String, Double>();

		String colors = "";

		// Edge from hills
		int colorIndex = 0;

		List<AntHill> hills = OnlineACOScheduler.getHills();
		int hillsCount = hills.size();

		for (AntHill hill : hills) {
			try {
				StraddleCarrier rsc = hill.getStraddleCarrier();
				StraddleCarrierModel model = rsc.getModel();
				double teu = Terminal.getInstance().getContainerTEU(
						m.getContainerId());
				if (model.isCompatible(ContainerKind.getType(teu))) {
					if (colors.length() > 0)
						colors += ",";
					colors += hill.getStyleColor();
					matchingHills.put(hill.getID(), hill);
					pheromone.put(hill.getID(), OnlineACOScheduler
							.getGlobalParameters().getLambda());
					population.put(hill.getID(), new HashMap<String, Ant>());
					pheromoneRates.put(hill.getID(), 1.0 / (hillsCount + 0.0));
					this.colorIndex.put(hill.getID(), colorIndex++);
				}
			} catch (IdAlreadyInUseException e) {
				e.printStackTrace();
			} catch (ElementNotFoundException e) {
				e.printStackTrace();
			}
		}

		pheromoneToSpread = new ConcurrentHashMap<String, Double>(
				matchingHills.size());

		// Edge towards other missions
		try {
			connect();
		} catch (EmptyResourcesException e) {
			e.printStackTrace();
		}

		pie = OnlineACOScheduler.getSpriteManager().addSprite(getID());
		pie.addAttribute("ui.pie-values", pheromoneRates.values().toArray());
		pie.addAttribute("ui.style", "shape: pie-chart; fill-color: " + colors
				+ "; size: 20px, 20px; z-index: 4;");
		pie.attachToNode(getID());

	}

	private static boolean link(AntMissionNode n1, AntMissionNode n2)
			throws EmptyResourcesException {
		double s1 = n1.m.getDeliveryTimeWindow().getMin().getInSec();
		double s2 = n2.m.getPickupTimeWindow().getMax().getInSec();
		if (s1 < s2) {
			for (StraddleCarrier vehicle : OnlineACOScheduler.getInstance()
					.getResources()) {
				double travelTime = OnlineACOScheduler.getInstance()
						.getTravelPath(n1.m, n2.m, vehicle).getCost();
				if ((s1 + travelTime) <= s2) {
					// System.err.println(n1.getId()+"->"+n2.getId()+"="+travelTime);
					return true;
				}
			}
		}
		return false;
	}

	private void connect() throws EmptyResourcesException {

		List<AntMissionNode> nodes = OnlineACOScheduler.getMissionNodes();
		ArrayList<AntMissionNode> in = new ArrayList<AntMissionNode>(
				nodes.size());
		ArrayList<AntMissionNode> out = new ArrayList<AntMissionNode>(
				nodes.size());

		for (AntMissionNode n : nodes) {
			if (link(n, this))
				in.add(n);
			else if (link(this, n))
				out.add(n);
		}

		ArrayList<AntMissionNode> finalIn = new ArrayList<AntMissionNode>(
				in.size());
		for (AntMissionNode n : in) {
			if (inter(n.getOutNodes(), in).size() == 0) {
				// LINK
				finalIn.add(n);
			}
		}
		ArrayList<AntMissionNode> finalOut = new ArrayList<AntMissionNode>(
				out.size());
		for (AntMissionNode n : out) {
			if (inter(n.getInNodes(), out).size() == 0) {
				// LINK
				finalOut.add(n);
			}
		}

		for (AntMissionNode n : finalIn) {
			AntEdge inEdge = new AntEdge(n, this);
			n.addOutgoingEdge(inEdge);
			this.addIncomingEdge(inEdge);
		}
		for (AntMissionNode n : finalOut) {
			AntEdge outEdge = new AntEdge(this, n);
			this.addOutgoingEdge(outEdge);
			n.addIncomingEdge(outEdge);
		}

		// DEL INTERSECTION
		ArrayList<AntEdge> toRemove = new ArrayList<AntEdge>();
		for (AntMissionNode inNode : finalIn) {
			for (AntMissionNode outNode : finalOut) {
				if (outNode.in.containsKey(inNode.getID())) {
					// DEL
					toRemove.add(inNode.outgoingEdges.get(outNode.getID()));
				}
			}
		}

		for (AntEdge edge : toRemove) {
			AntNode from = (AntNode) edge.getNodeFrom();
			AntNode to = (AntNode) edge.getNodeTo();
			from.removeOutgoingEdge(to);
			to.removeIncomingEdge(from);
		}

		if (in.size() == 0) {
			AntEdge source = new AntEdge(OnlineACOScheduler.getDepotNode(),
					this);
			this.addIncomingEdge(source);
			OnlineACOScheduler.getDepotNode().addOutgoingEdge(source);
		}
		if (out.size() == 0) {
			AntEdge sink = new AntEdge(this, OnlineACOScheduler.getEndNode());
			this.addOutgoingEdge(sink);
			OnlineACOScheduler.getEndNode().addIncomingEdge(sink);
		}

		// MUST CALL CLEAN AFTER ADDING ALL NODES OF THE CURRENT STEP
	}

	private static boolean hasAPath(AntMissionNode from, AntMissionNode to) {
		if (from.outgoingEdges.containsKey(to.getID()))
			return true;
		for (AntEdge e : from.outgoingEdges.values()) {
			AntNode n = (AntNode) e.getNodeTo();
			if (n instanceof AntMissionNode) {
				AntMissionNode amn = (AntMissionNode) n;
				if (hasAPath(amn, to))
					return true;
			}
		}
		return false;
	}

	private static boolean hasMoreThanOnePath(AntMissionNode from,
			AntMissionNode to) {
		int count = 0;
		if (from.outgoingEdges.containsKey(to.getID()))
			count++;
		for (AntEdge e : from.outgoingEdges.values()) {
			AntNode n = (AntNode) e.getNodeTo();

			if (n != to && n instanceof AntMissionNode) {
				AntMissionNode amn = (AntMissionNode) n;
				if (hasAPath(amn, to)) {
					count++;
				}
			}
			if (count > 1)
				return true;
		}
		return false;
	}

	public static void clean() {
		for (AntMissionNode from : OnlineACOScheduler.getMissionNodes()) {
			for (AntMissionNode to : OnlineACOScheduler.getMissionNodes()) {
				if (from != to && from.outgoingEdges.containsKey(to.getID())) {
					clean(from, to);
				}
			}
		}
	}

	private static void clean(AntMissionNode from, AntMissionNode to) {
		if (hasMoreThanOnePath(from, to)) {
			// DEL from->to
			AntEdge toRemoveEdge = from.outgoingEdges.get(to.getID());
			from.removeOutgoingEdge((AntNode) toRemoveEdge.getNodeTo());
			to.removeIncomingEdge(from);
		}
	}

	private static ArrayList<AntMissionNode> inter(
			ArrayList<AntMissionNode> l1, ArrayList<AntMissionNode> l2) {
		ArrayList<AntMissionNode> l = new ArrayList<AntMissionNode>();
		for (AntMissionNode n1 : l1) {
			for (AntMissionNode n2 : l2) {
				if (n1 == n2)
					l.add(n1);
			}
		}
		return l;
	}

	private ArrayList<AntMissionNode> getInNodes() {
		ArrayList<AntMissionNode> in = new ArrayList<AntMissionNode>();
		for (String k : this.in.keySet()) {
			AntNode n = (AntNode) this.in.get(k).getNodeFrom();
			if (n instanceof AntMissionNode)
				in.add((AntMissionNode) n);
		}
		return in;
	}

	private ArrayList<AntMissionNode> getOutNodes() {
		ArrayList<AntMissionNode> out = new ArrayList<AntMissionNode>();
		for (String k : this.outgoingEdges.keySet()) {
			AntNode n = (AntNode) this.outgoingEdges.get(k).getNodeTo();
			if (n instanceof AntMissionNode)
				out.add((AntMissionNode) n);
		}
		return out;
	}

	public void addAnt(Ant a) {
		String color = a.getHill().getID();
		HashMap<String, Ant> map = population.get(color);
		map.put(a.getID(), a);
		population.put(color, map);
	}

	// APPLY = called each step of the algorithm, so $syncSize times each
	// simulation step
	public void apply() {
		// validate spread pheromone
		for (String color : pheromoneToSpread.keySet()) {
			if (pheromone.containsKey(color)) {
				pheromone.put(color,
						pheromone.get(color) + pheromoneToSpread.remove(color));
			} else {
				pheromone.put(color, pheromoneToSpread.remove(color));
			}
		}
		pheromoneToSpread.clear();
	}

	@SuppressWarnings("unused")
	public void changeUIColor(String color) {
		uicolor = color;
		String newUI = DEFAULT_MISSION_NODE_STYLE;
		if (!color.equals(DEFAULT_STYLE_COLOR)) {
			String old_ui = node.getAttribute("ui.style");
			int i1 = old_ui.indexOf("stroke-color:");
			int i2 = old_ui.indexOf(";", i1);
			newUI = old_ui.substring(0, i1) + "stroke-color: " + color + ";"
					+ old_ui.substring(i2 + 1, old_ui.length());
		}

		if (!pieLocated) {
			double xgu = 0;
			if (PIE_DISTANCE > 0) {
				xgu = OnlineACOScheduler.toGu(PIE_DISTANCE);
			}
			pie.setPosition(xgu, 0, 90);
			pieLocated = true;
		}

		node.setAttribute("ui.style", newUI);
		for (AntEdge e : in.values())
			e.computeColor();
		for (AntEdge e : outgoingEdges.values())
			e.computeColor();
	}

	// COMMIT = apply of the discrete part of the algorithm
	// -> 1 commit each simulation step
	public void commit() {
		computeRates(); // This way rates are updated only at each step of the
						// simulation, not at each step of the aco algorithm
		previousColorMAX = colorMAX;
	}

	public void compute() {
		// evaporate tracks of previous steps
		evaporate();
		// apply pheromone spread
		if (pheromoneToSpread.size() > 0)
			apply();

	}

	public void computeRates() {

		Double[] values = new Double[matchingHills.size()];
		for (int i = 0; i < values.length; i++)
			values[i] = 0.0;
		double colorMAXValue = Double.NEGATIVE_INFINITY;
		double sum = 0.0;
		for (Double d : pheromone.values()) {
			sum += d;
		}
		String newColorMAX = "";
		Double[] rates = new Double[matchingHills.size()];
		for (int i = 0; i < rates.length; i++)
			rates[i] = 0.0;
		for (String color : pheromone.keySet()) {
			double value = pheromone.get(color) / sum;
			if (colorIndex.containsKey(color)) {
				int index = colorIndex.get(color);
				rates[index] = value;
			}
			if (value >= colorMAXValue) {
				if (value == colorMAXValue) {
					newColorMAX = "";
				} else {
					colorMAXValue = value;
					if (colorMAXValue > 0)
						newColorMAX = color;
				}
			}
		}
		pie.changeAttribute("ui.pie-values", (Object[]) rates);
		if (!newColorMAX.equals(colorMAX)) {
			colorMAX = newColorMAX;
			if (colorMAX == "") {
				changeUIColor(DEFAULT_STYLE_COLOR);
			} else {
				changeUIColor(matchingHills.get(colorMAX).getStyleColor());
			}
		}

	}

	public void delete() {
		for (AntHill hill : matchingHills.values()) {
			hill.removeAntMissionNode(this);
		}

		pie.detach();
		pie.clearAttributes();
		OnlineACOScheduler.getSpriteManager().removeSprite(pie.getId());

		if (travelTimeFromDepot != null) {
			travelTimeFromDepot.destroy();
			travelTimeFromDepot = null;
		}
		if (travelTimeToDepot != null) {
			travelTimeToDepot.destroy();
			travelTimeToDepot = null;
		}

		pie = null;

		matchingHills.clear();
		matchingHills = null;

		previousColorMAX = null;
		colorMAX = null;

		colorIndex.clear();
		colorIndex = null;

		pheromone.clear();
		pheromone = null;

		pheromoneToSpread.clear();
		pheromoneToSpread = null;

		population.clear();
		population = null;

		super.destroy();
	}

	public void evaporate() {
		ACOParameters parameters = OnlineACOScheduler.getGlobalParameters();
		double lambda = parameters.getLambda();
		double rho = parameters.getPersistence();
		for (String colony : pheromone.keySet()) {
			double ph = pheromone.get(colony);
			double newPh = lambda + rho * (ph - lambda);
			pheromone.put(colony, newPh);
		}
	}

	public int getAntCount() {
		int sum = 0;
		for (HashMap<String, Ant> h : population.values())
			sum += h.size();
		return sum;
	}

	public int getAntCount(String colony) {
		return population.get(colony).size();
	}

	public String getColor() {
		return colorMAX;
	}

	public double getForeignPheromone(String id) {
		double q = 0;
		for (String color : pheromone.keySet()) {
			if (!color.equals(id)) {
				q += pheromone.get(color);
			}
		}
		return q;
	}

	public Mission getMission() {
		return m;
	}

	public double getPheromone(String color) {
		double ph = 0.0;
		ph = pheromone.get(color);
		return ph;
	}

	public double getPheromoneOverall() {
		double sum = 0.0;
		for (Double d : pheromone.values())
			sum += d;
		return sum;
	}

	public String getPheromoneValues() {
		StringBuilder sb = new StringBuilder();
		Iterator<String> it = pheromone.keySet().iterator();
		DecimalFormat df = new DecimalFormat("#.###############");
		for (int i = 0; i < pheromone.size(); i++) {
			String s = it.next();
			sb.append(s + ": " + df.format(pheromone.get(s)));
			if (i < pheromone.size() - 1)
				sb.append(" | ");
		}
		return sb.toString();
	}

	public String getPreviousColor() {
		return previousColorMAX;
	}

	/*
	 * public HashMap<String , Double> getTravelDistanceFromDepot(){
	 * HashMap<String, Double> map = new HashMap<String,
	 * Double>(matchingHills.size()); for(AntHill hill :
	 * matchingHills.values()){ try { map.put(hill.getID(),
	 * scheduler.getTravelDistance(scheduler.getDepotNode(), this,
	 * hill.getStraddleCarrier())); } catch (RemoteException e) {
	 * e.printStackTrace(); } catch (EmptyResourcesException e) {
	 * e.printStackTrace(); } } return map; }
	 */
	/*
	 * public double getTravelDistanceFromPickupToDelivery(StraddleCarrier
	 * rsc) { double distance = 0; try { Location p =
	 * m.getContainer().getLocation(); Location d =
	 * Terminal.getRMIInstance().getSlot
	 * (m.getDestination().getSlotId()).getLocation(); distance =
	 * rsc.getRemoteRouting().getShortestPath(p, d).getCostInMeters(); } catch
	 * (RemoteException e) { e.printStackTrace(); } catch (NoPathFoundException
	 * e) { e.printStackTrace(); } return distance; }
	 */

	/*
	 * public HashMap<String , Double> getTravelDistanceToDepot(){
	 * HashMap<String, Double> map = new HashMap<String,
	 * Double>(matchingHills.size()); for(AntHill hill :
	 * matchingHills.values()){ try { map.put(hill.getID(),
	 * scheduler.getTravelDistance(this, scheduler.getEndNode(),
	 * hill.getStraddleCarrier())); } catch (RemoteException e) {
	 * e.printStackTrace(); } catch (EmptyResourcesException e) {
	 * e.printStackTrace(); } } return map; }
	 */

	/*
	 * public Time getTravelTimeFromDepot() { if(travelTimeFromDepot == null){
	 * try{ travelTimeFromDepot =
	 * scheduler.getTravelTime(scheduler.getDepotNode(), this); }
	 * catch(Exception e){ e.printStackTrace(); } } return travelTimeFromDepot;
	 * }
	 */

	/*
	 * public Time getTravelTimeFromPickupToDelivery(StraddleCarrier rsc){
	 * Time t = null; try { Location p = m.getContainer().getLocation();
	 * Location d =
	 * Terminal.getRMIInstance().getSlot(m.getDestination().getSlotId
	 * ()).getLocation(); t = new Time(rsc.getRemoteRouting().getShortestPath(p,
	 * d).getCost()+"s"); } catch (RemoteException e) { e.printStackTrace(); }
	 * catch (NoPathFoundException e) { e.printStackTrace(); } return t; }
	 */

	/*
	 * public HashMap<String, Time> getTravelTimesFromDepot(){ HashMap<String,
	 * Time> map = new HashMap<String, Time>(); for(AntHill h :
	 * matchingHills.values()){ try { map.put(h.getID(),
	 * scheduler.getTravelTime(scheduler.getDepotNode(), this,
	 * h.getStraddleCarrier())); } catch (RemoteException e) {
	 * e.printStackTrace(); } catch (EmptyResourcesException e) {
	 * e.printStackTrace(); } } return map; }
	 */

	/*
	 * public HashMap<String, Time> getTravelTimesToDepot(){ HashMap<String,
	 * Time> map = new HashMap<String, Time>(); for(AntHill h :
	 * matchingHills.values()){ try { map.put(h.getID(),
	 * scheduler.getTravelTime(this, scheduler.getDepotNode(),
	 * h.getStraddleCarrier())); } catch (RemoteException e) {
	 * e.printStackTrace(); } catch (EmptyResourcesException e) {
	 * e.printStackTrace(); } } return map; }
	 */

	/*
	 * public Time getTravelTimeToDepot() { if(travelTimeToDepot == null){ try{
	 * travelTimeToDepot =
	 * scheduler.getTravelTime(this,scheduler.getDepotNode()); } catch(Exception
	 * e){ e.printStackTrace(); } } return travelTimeToDepot; }
	 */

	public String getUIColor() {
		return uicolor;
	}

	public boolean hasColorChanged() {
		return !previousColorMAX.equals(colorMAX);
	}

	public void removeAnt(Ant a) {
		if (population != null) {
			String color = a.getHill().getID();
			HashMap<String, Ant> map = population.get(color);
			map.remove(a.getID());
			population.put(color, map);
		}
	}

	public void spreadInstantPheromone(String color, double quantity) {
		if (pheromone != null) {
			if (pheromone.containsKey(color)) {
				pheromone.put(color, pheromone.get(color) + quantity);
			} else {
				pheromone.put(color, quantity);
			}
		}
	}

	public void spreadPheromone(String color, double quantity) {
		try {
			if (pheromoneToSpread.get(color) != null) {
				pheromoneToSpread.put(color, pheromoneToSpread.get(color)
						+ quantity);
			} else {
				pheromoneToSpread.put(color, quantity);
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.err.println("Node: " + getID() + " => Ph : "
					+ pheromoneToSpread + "\nColor : " + color);
			System.exit(ReturnCodes.PHEROMONE_SPREAD_ERROR.getCode());
		}
	}

	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(getID() + "\n\tIN :\n");
		for (AntEdge e : in.values()) {
			sb.append("\t\t" + e + "\n");
		}
		sb.append("\tOUT :\n");

		for (AntEdge e : outgoingEdges.values()) {
			sb.append("\t\t" + e + "\n");
		}
		return sb.toString();
	}

	// Can happen if the destination changed
	public void updateMission(Mission m2) {
		m = m2;
		List<AntEdge> toRemove = new ArrayList<AntEdge>();
		for (AntEdge e : outgoingEdges.values()) {
			toRemove.add(e);
		}
		for (AntEdge e : in.values()) {
			toRemove.add(e);
		}

		for (AntEdge e : toRemove) {
			AntNode from = (AntNode) e.getNodeFrom();
			AntNode to = (AntNode) e.getNodeTo();
			from.removeOutgoingEdge(to);
			to.removeIncomingEdge(from);
		}

		try {
			connect();
		} catch (EmptyResourcesException e1) {
			e1.printStackTrace();
		}
		clean();
	}

	public HashMap<String, Double> getPheromone() {
		HashMap<String, Double> ph = new HashMap<String, Double>(
				pheromone.size());
		for (String s : pheromone.keySet()) {
			ph.put(s, pheromone.get(s));
		}
		return ph;
	}

	public void setPheromone(HashMap<String, Double> map) {
		for (String s : map.keySet()) {
			pheromone.put(s, map.get(s));
		}
	}
}