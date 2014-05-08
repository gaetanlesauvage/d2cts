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
package org.positioning;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.com.dao.StraddleCarrierLocationDAO;
import org.com.model.StraddleCarrierLocationBean;
import org.conf.parameters.ReturnCodes;
import org.display.TextDisplay;
import org.system.Terminal;
import org.system.container_stocking.Bay;
import org.time.DiscretObject;
import org.time.TimeScheduler;
import org.util.Distances;
import org.util.Location;
import org.vehicles.StraddleCarrier;

public class LaserSystem implements DiscretObject {
	private static LaserSystem instance;

	private Map<String, LaserHead> heads;
	private Map<String, Location> locations;
	private Map<String, StraddleCarrier> vehicles;

	public static LaserSystem getInstance() {
		if (instance == null) {
			instance = new LaserSystem();
		}
		return instance;
	}

	public static void closeInstance() {
		instance = null;
	}

	public static final String rmiBindingName = "LaserData";

	public static String getStaticId() {
		return LaserSystem.id;
	}

	private TextDisplay out;

	private static final Logger log = Logger.getLogger(LaserSystem.class);

	private static String id = null;

	public void setTextDisplay(TextDisplay out) {
		this.out = out;
	}

	private LaserSystem() {
		heads = new HashMap<>();
		locations = new HashMap<>();
		vehicles = new HashMap<>();

		// RemoteNetworkConfiguration nc =
		// NetworkConfiguration.getRMIInstance();
		id = rmiBindingName;// this.getClass().getName()+":"+(nc.getCount(this.getClass().getName())+1);
		TimeScheduler.getInstance().recordDiscretObject(this);
	}

	public void addLaserHead(String id, Coordinates location, Range range) {
		LaserHead lh = new LaserHead(id, location, range);
		heads.put(lh.getId(), lh);

		Terminal.getInstance().addlaserHead(lh.getId());
		if (out != null)
			out.println("New Laser Head Created on " + lh.getLocation());
		else
			log.trace("New Laser Head Created on " + lh.getLocation());
	}

	// TODO improve the search of vehicle on the same road ?
	public double getCollisionRate(String straddleID, Location current, double goalRate) {
		if (current.getRoad() instanceof Bay) {
			StraddleCarrier rsc = Terminal.getInstance().getStraddleCarrier(straddleID);
			double currentRate = goalRate;
			double halfRate = current.getPourcent(rsc.getModel().getLength() / 2.0);
			if (current.getDirection())
				currentRate += halfRate;
			else
				currentRate -= halfRate;
			for (LaserHead lh : getVisibleHeads(current.getCoords())) {
				Set<String> list = lh.getVisibleStraddleCarriers();
				for (String id : list) {
					if (!id.equals(straddleID)) {
						StraddleCarrier rsc2 = Terminal.getInstance().getStraddleCarrier(id);

						Location l = lh.getLocation(id);
						if (l != null) {
							double rs2Rate = l.getPourcent();
							if (rs2Rate > 0 && rs2Rate < 1) {
								if (current.getDirection())
									rs2Rate -= current.getPourcent(rsc2.getModel().getLength() / 2.0);
								else
									rs2Rate += current.getPourcent(rsc2.getModel().getLength() / 2.0);

								if (l.getRoad().getId().equals(current.getRoad().getId())) {
									if (current.getDirection()) {
										// Add the length of the vehicle to the
										// distance computing and returning...
										if (rs2Rate < currentRate && l.getPourcent() >= current.getPourcent()) {
											return rs2Rate - halfRate;
										}
									} else {
										if (rs2Rate > currentRate && rs2Rate <= current.getPourcent()) {
											return rs2Rate + halfRate;
										}
									}
								}
							}
						} else
							System.out.println(lh.getId() + " can't locate " + id + " !");
					}
				}
			}
			/*
			 * for(String id :
			 * Terminal.getInstance().getStraddleCarriersName()){
			 * if(!id.equals(straddleID)){ StraddleCarrier rsc2 =
			 * Terminal.getInstance().getStraddleCarrier(id); //Location or
			 * future location ??? System.out.println("LZ 3"); Location l =
			 * rsc2.getLocation(); System.out.println("LZ 4"); double rs2Rate =
			 * l.getPourcent(); if(current.getDirection()) rs2Rate -=
			 * current.getPourcent(rsc2.getModel().getLength()/2.0); else
			 * rs2Rate += current.getPourcent(rsc2.getModel().getLength()/2.0);
			 * 
			 * if(l.getRoad().getId().equals(current.getRoad().getId())){
			 * if(current.getDirection()){ //Add the length of the vehicle to
			 * the distance computing and returning... if(rs2Rate<currentRate &&
			 * l.getPourcent()>= current.getPourcent()){ return rs2Rate -
			 * halfRate; } } else{ if(rs2Rate>currentRate && rs2Rate<=
			 * current.getPourcent()){ return rs2Rate + halfRate; } } } } }
			 */
		}
		return goalRate;
	}

	public String getId() {
		return id;
	}

	public LaserHead getLaserHead(String id) {
		return heads.get(id);
	}

	public TextDisplay getRemoteDisplay() {
		return out;
	}

	public List<LaserHead> getVisibleHeads(Coordinates location) {
		List<LaserHead> visibles = new ArrayList<LaserHead>();

		Iterator<String> itKeys = heads.keySet().iterator();
		while (itKeys.hasNext()) {
			LaserHead head = heads.get(itKeys.next());
			double distance = Distances.getDistance(head.getLocation(), location);
			if (distance <= head.getRange().x)
				visibles.add(head);
		}
		return visibles;
	}

	public void updateStraddleCarrierCoordinates(StraddleCarrier rsc, Location location) {
		Location old = locations.get(rsc.getId());
		locations.put(rsc.getId(), location);
		rsc.moveContainer();
		Terminal.getInstance().straddleCarrierMoved(rsc.getId(), old, location, rsc.getCurrentSpeed(), rsc.getCSSStyle());
	}

	public Set<LaserHead> getCommonHeads(String sc1, String sc2) {
		Set<LaserHead> l = new HashSet<>();
		for (LaserHead lh : heads.values()) {
			Set<String> visibles = lh.getVisibleStraddleCarriers();
			boolean first = false;
			boolean second = false;
			for (String s : visibles) {
				if (s.equals(sc1))
					first = true;
				if (s.equals(sc2))
					second = true;
				if (first && second)
					break;
			}
			if (first && second)
				l.add(lh);
		}
		return l;
	}

	@Override
	public boolean apply() {
		for (LaserHead lh : heads.values()) {
			lh.clearVisibles();
		}
		for (StraddleCarrier rsc : vehicles.values()) {
			String rscId = rsc.getId();

			Location l = rsc.getLocation();
			Coordinates c = l.getCoords();

			Location cBefore = locations.get(rscId);
			List<LaserHead> visibleHeads = getVisibleHeads(c);
			if (visibleHeads.size() > 0 && (cBefore == null || !cBefore.getCoords().equals(c))) {
				updateStraddleCarrierCoordinates(rsc, l);
				saveLocation(l, c, rscId);
			} else {
				Terminal.getInstance().straddleCarrierStopped(rsc.getId());
			}

			for (LaserHead lh : visibleHeads) {
				lh.detectStraddleCarrier(rsc.getId(), l);
			}
		}
		return NOTHING_CHANGED;
	}

	private void saveLocation(Location l, Coordinates c, String rscId) {
		// Sauvegarde des positions des vehicules
		StraddleCarrierLocationBean bean = new StraddleCarrierLocationBean();
		bean.setSimID(Terminal.getInstance().getSimulationID());
		bean.setStraddleCarrierName(rscId);
		bean.setRoad(l.getRoad().getId());
		bean.setDirection(l.getDirection());
		bean.setT(TimeScheduler.getInstance().getTime().getSQLTime());
		bean.setX(c.x);
		bean.setY(c.y);
		bean.setZ(c.z);
		try {
			StraddleCarrierLocationDAO.getInstance(Terminal.getInstance().getSimulationID()).insert(bean);
		} catch (SQLException e) {
			log.error(e.getMessage(), e);
		}
	}

	@Override
	public void precompute() {

	}

	public void addDetectableStraddleCarrier(StraddleCarrier rsc) {
		vehicles.put(rsc.getId(), rsc);
		// System.out.println(rsc.getId()+" is now detectable !!!");
	}

	public void updateLaserHeadRange(String lhID, double range) {
		LaserHead lh = heads.get(lhID);
		if (lh == null) {
			new Exception("Laser Head " + lhID + " not found!").printStackTrace();
			System.exit(ReturnCodes.LASER_HEAD_NOT_FOUND.getCode());
		}
		lh.setRangeRate(range);
	}

//	public void destroy() {
//		/*
//		 * for(LaserHead lh : heads.values()){ lh.destroy(); }
//		 */
//		heads.clear();
//		locations.clear();
//		vehicles.clear();
//		if (Terminal.getInstance().getListener() != null) {
//			Terminal.getInstance().getListener().hideLaserHeads();
//		}
//		id = null;
//		this.out = null;
//		// out = null;
//	}

	public Collection<LaserHead> getHeads() {
		return heads.values();
	}
	
	@Override
	public Integer getDiscretPriority () {
		return 1; //After straddlecarriers but before the other threads.
	}
	
	@Override
	public int hashCode(){
		return getId().hashCode();
	}
	
	@Override
	public boolean equals(Object o){
		return o.hashCode() == hashCode();
	}
}
