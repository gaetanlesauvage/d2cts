/*
 * This file is paTerminal.getInstance() of D²CTS : Dynamic and Distributed Container Terminal Simulator.
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
 * MERCHANTABILITY or FITNESS FOR A PATerminal.getInstance()ICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package org.display.system;

import java.util.ArrayList;
import java.util.Collection;

import org.graphstream.graph.Edge;
import org.graphstream.graph.Node;
import org.graphstream.ui.spriteManager.Sprite;
import org.positioning.Coordinates;
import org.positioning.LaserHead;
import org.positioning.LaserSystem;
import org.system.Crossroad;
import org.system.Depot;
import org.system.Road;
import org.system.RoadPoint;
import org.system.StraddleCarrierSlot;
import org.system.Terminal;
import org.system.container_stocking.Bay;
import org.system.container_stocking.Block;
import org.system.container_stocking.Container;
import org.system.container_stocking.Slot;
import org.util.Location;
import org.vehicles.Ship;
import org.vehicles.StraddleCarrier;

public class GraphicTerminalListenerImpl implements GraphicTerminalListener {
	
	public GraphicTerminalListenerImpl() {

	}

	@Override
	public void containerAdded(Container container) {
		ArrayList<Container> l = new ArrayList<Container>(1);
		l.add(container);
		JTerminal.getInstance().addContainers(l);
	}

	@Override
	public void containerMoved(String containerId) {
		Container container = Terminal.getInstance().getContainer(containerId);
		String from = container.getLocation().getRoad()
				.getIdRoadPointOrigin(container.getLocation().getPourcent());
		String to = container
				.getLocation()
				.getRoad()
				.getIdRoadPointDestination(
						container.getLocation().getPourcent());
		Edge e = JTerminal.getInstance().getGraph().getNode(from).getEdgeToward(to);

		String cssStyle = container.getCSSStyle();
		JTerminal.getInstance().getSpriteManager().getSprite(container.getId())
		.addAttribute("ui.style", cssStyle);

		Location l = container.getLocation();
		double localPourcent = l.getRoad().getLocalRate(l.getPourcent());
		Sprite s = JTerminal.getInstance().getSpriteManager().getSprite(container.getId());
		s.detach();
		s.attachToEdge(e.getId());
		s.setPosition((float) localPourcent);
		s.setAttribute("percent", (float) localPourcent);
		if (s.getId().equals(JTerminal.getInstance().getSelectedContainer().getContainerId())) {
			Sprite spriteSC = JTerminal.getInstance().getSpriteManager().getSprite(
					JTerminal.getInstance().getSelectedContainer().getId());
			spriteSC.detach();
			spriteSC.attachToEdge(e.getId());

			spriteSC.setPosition((float) localPourcent);
		}
	}

	@Override
	public void containerRemoved(String containerId) {
		JTerminal.getInstance().removeContainer(containerId);
	}

	@Override
	public void crossroadAdded(Crossroad crossroad) {
		JTerminal.getInstance().addCrossroad(crossroad);
	}

	@Override
	public void depotAdded(Depot depot) {
		JTerminal.getInstance().addDepot(depot);
	}

	@Override
	public void hideLaserHeads() {
		// RemoteLaserSystem ls = LaserSystem.getRMIInstance();
		for (LaserHead lh : LaserSystem.getInstance().getHeads()) {
			JTerminal.getInstance().getGraph().removeNode(lh.getId());
		}
		/*
		 * for(Node n : JTerminal.getInstance().getGraph().getNodeSet()){
		 * if(n.hasAttribute("ui.class"
		 * )&&n.getAttribute("ui.class").equals("laserHead")){
		 * n.addAttribute("ui.style",
		 * ls.getLaserHead(n.getId()).getCSSStyle()+"visibility-mode:hidden;");
		 * } }
		 */

	}

	@Override
	public void laneAdded(Bay lane) {
		JTerminal.getInstance().addLane(lane);
	}

	@Override
	public void laserHeadAdded(String id) {
		LaserHead lh = LaserSystem.getInstance().getLaserHead(id);
		Node n = JTerminal.getInstance().getGraph().addNode(id);
		
	
			/*Map<String,Object> m = new HashMap<>(6);
			m.put("ui.class", "laserHead");
			m.put("x", lh.getLocation().x);
			m.put("y", lh.getLocation().y);
			m.put("z", lh.getLocation().z);
			m.put("ui.style", lh.getCSSStyle());
			m.put("label", n.getId());
			n.addAttributes(m);
			duration = System.nanoTime()-duration;
			System.err.println("Attributes : "+duration+"ns");
			duration = System.nanoTime()-duration;*/
			
			n.addAttribute("ui.class", "laserHead");
			n.addAttribute("x", lh.getLocation().x);
			n.addAttribute("y", lh.getLocation().y);
			n.addAttribute("z", lh.getLocation().z);
			n.addAttribute("ui.style", lh.getCSSStyle());
			n.addAttribute("label", n.getId());
					
	}

	public void laserHeadUpdated(String lhID) {
		Node n = JTerminal.getInstance().getGraph().getNode(lhID);
		if (n != null) {
			LaserSystem ls = LaserSystem.getInstance();
			LaserHead lh = ls.getLaserHead(lhID);
			n.setAttribute("ui.style", lh.getCSSStyle());
		}
	}

	/*
	 * @Override public void paveAdded(Pave pave) { List<URI>
	 * remoteTerminalsURI; try { remoteTerminalsURI = nc.getJTerminals();
	 * 
	 * for(URI uri : remoteTerminalsURI){ try{ JTerminal JTerminal.getInstance() =
	 * (JTerminal)nc.get(uri); JTerminal.getInstance().addPave(pave);
	 * 
	 * } catch (RegisteredObjectNotFoundException e) { e.printStackTrace(); } }
	 * } catch (RemoteException e1) { e1.printStackTrace(); } }
	 */
	@Override
	public void roadAdded(Road road) {
		JTerminal.getInstance().addRoad(road);
	}

	@Override
	public void roadRemoved(Road road) {
		JTerminal.getInstance().removeRoad(road);
	}

	/*
	 * @Override public void laneCrossroadAdded(LaneCrossroad crossroad) {
	 * roadRemoved(crossroad.getMainRoad()); roadAdded(crossroad.getMainRoad());
	 * }
	 */

	@Override
	public void straddleCarrierAdded(StraddleCarrier rsc) {
		JTerminal.getInstance().addStraddleCarrier(rsc);
	}

	@Override
	public void straddleCarrierMoved(String scID, Location l, String style) {
		String from = l.getRoad().getIdRoadPointOrigin(l.getPourcent());
		String to = l.getRoad().getIdRoadPointDestination(l.getPourcent());
		Edge e = JTerminal.getInstance().getGraph().getNode(from).getEdgeToward(to);

		Sprite spStraddle = JTerminal.getInstance().getStraddleCarrier(scID);
		double localPourcent = l.getRoad().getLocalRate(l.getPourcent());

		spStraddle.detach();
		// if(e == null)
		// System.out.println("E IS NULL !!! (there is no edge from "+from+" towards "+to+")");
		spStraddle.attachToEdge(e.getId());
		float rate = (float) localPourcent;
		spStraddle.setPosition(rate);
		spStraddle.addAttribute("percent", rate);
		if (scID.equals(JTerminal.getInstance().getSelectedVehicle().getVehicleId())) {
			Sprite spriteSV = JTerminal.getInstance().getSpriteManager().getSprite(
					JTerminal.getInstance().getSelectedVehicle().getId());
			if (spriteSV != null) {
				spriteSV.detach();
				spriteSV.attachToEdge(e.getId());
				spriteSV.setPosition(rate);
				if (JTerminal.getInstance().isViewLocked()) {
					Coordinates pos = l.getCoords();
					JTerminal.getInstance().setViewCenter(pos.x, pos.y, pos.z);
				}
			}

		}
		// if(!spStraddle.getAttribute("ui.style").equals(style))
		// spStraddle.addAttribute( "ui.style", style);
		if(!spStraddle.getAttribute("ui.style").equals(style))
			spStraddle.setAttribute("ui.style", style);
	}

	@Override
	public void straddleCarrierSlotAdded(StraddleCarrierSlot slot) {
		JTerminal.getInstance().addStraddleCarrierSlot(slot);
	}

	public void lockView(boolean locked) {
		JTerminal.getInstance().lockView(locked);
	}

	@Override
	public void unhideLaserHeads() {
		for (LaserHead lh : LaserSystem.getInstance().getHeads()) {
			laserHeadAdded(lh.getId());
		}
	}

	public void crossroadsAdded(Collection<RoadPoint> values) {
		JTerminal.getInstance().addCrossroads(new ArrayList<RoadPoint>(values));
	}

	public void roadsAdded(Collection<Road> roads) {
		JTerminal.getInstance().addRoads(new ArrayList<Road>(roads));
	}

	public void pavesAdded(Collection<Block> values) {
		JTerminal.getInstance().addPaves(new ArrayList<Block>(values));
	}

	public void lanesAdded(Collection<Bay> values) {
		JTerminal.getInstance().addLanes(new ArrayList<Bay>(values));
	}

	public void depotsAdded(Collection<Depot> values) {
		JTerminal.getInstance().addDepots(new ArrayList<Depot>(values));
	}

	public void containersAdded(Collection<Container> values) {
		JTerminal.getInstance().addContainers(new ArrayList<Container>(values));
	}

	public void straddleCarriersAdded(Collection<StraddleCarrier> values) {
		JTerminal.getInstance().addStraddleCarriers(
				new ArrayList<StraddleCarrier>(values));
	}

	public void containerSelected(String containerId) {
		JTerminal.getInstance().containerSelected(containerId);
	}

	public void vehicleSelected(String vehicleId) {
		JTerminal.getInstance().vehicleSelected(vehicleId);
	}

	public void slotUpdated(Slot slot) {
		JTerminal.getInstance().updateSlot(slot);
	}

	public void slotSelected(String slotId) {
		JTerminal.getInstance().slotSelected(slotId);
	}

	public void roadSelected(String roadId) {
		System.out
		.println("TODO GraphicTerminalListener.roadSelected(String roadId) !");
	}

	@Override
	public void resetView() {
		JTerminal.getInstance().resetView();
	}

	public void addShip(Ship ship) {
		JTerminal.getInstance().addShip(ship);
	}

	public void removeShip(Ship ship) {
		JTerminal.getInstance().removeShip(ship);
	}
	
}
