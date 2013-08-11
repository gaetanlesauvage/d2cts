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

import org.display.TextDisplay;
import org.exceptions.container_stocking.ContainerPickupException;
import org.exceptions.container_stocking.UnreachableContainerException;
import org.exceptions.container_stocking.delivery.CollisionException;
import org.exceptions.container_stocking.delivery.MissionContainerDeliveryException;
import org.exceptions.container_stocking.delivery.UnreachableSlotException;
import org.missions.Load;
import org.missions.Mission;
import org.missions.MissionPhase;
import org.missions.Workload;
import org.system.Road;
import org.system.Terminal;
import org.system.container_stocking.Bay;
import org.system.container_stocking.BayCrossroad;
import org.system.container_stocking.ContainerLocation;
import org.util.Location;

public class StraddleCarrierProblemListenerImpl implements
		StraddleCarrierProblemListener {

	@Override
	/**
	 *This method is run when a straddle carrier can not deliver his load. 
	 *There can be several reasons of this non delivery but the only purpose here is
	 * to compute the right decision and tell it to the straddle carrier.
	 */
	public DeliveryOrder cantDeliverContainer(String straddleCarrierId,
			MissionContainerDeliveryException e, TextDisplay out) {
		Mission m = e.getMission();
		String msg = m.getId() + " : Can't delivery container ! ";
		if (e instanceof CollisionException) {
			CollisionException ce = (CollisionException) e;
			msg += ce.getErrorMessage();
		}
		msg += "\nChange loctaion or wait?";

		if (out != null) {
			DeliveryOrder newOrder = out.askForNewContainerLocation(msg, e);
			System.out.println("User chose : " + newOrder);
			// What do I do now ??? -> Send response to straddleCarrier
			return newOrder;

		} else
			System.out.println("Terminal has no RemoteDisplay :(");
		return null;
	}

	public void cantDeliverContainerOnUnreachableSlot(String id,
			UnreachableSlotException unreachableSlotException, TextDisplay out) {
		String msg = "Can't delivery container ! ";
		msg += unreachableSlotException.getMessage();
		Terminal terminal = Terminal.getInstance();
		int choice = terminal.getTextDisplay().askForDeliveryOrders(msg,
				unreachableSlotException.getContainerLocation());
		StraddleCarrier rsc = terminal.getStraddleCarrier(id);
		Workload workload = rsc.getWorkload();
		Load l = workload.getCurrentLoad();
		Mission m = l.getMission();
		if (choice == ABORT_MISSION) {
			ContainerLocation cl = cantDeliverContainer(id,
					unreachableSlotException, out).getNewDeliveryLocation();
			rsc.messageReceived("<modMission id='" + m.getId() + "' phase='"
					+ MissionPhase.PHASE_DELIVERY.getCode() + "'>" + cl.toXML() + "</modMission>");
		} else {
			Bay lane = terminal.getBay(m.getDestination().getLaneId());
			if (choice == GO_TO_PICKUP_ORIGIN_AND_SEE) {
				BayCrossroad origin = (BayCrossroad) lane.getOrigin();
				Road mainRoadOrigin = terminal.getRoad(origin.getMainRoad());
				// double rOrigin = Location.getPourcent(origin.getLocation(),
				// mainRoadOrigin);
				double rOrigin = Location.getAccuratePourcent(
						origin.getLocation(), mainRoadOrigin);
				rsc.messageReceived("<goto road=\"" + mainRoadOrigin.getId()
						+ "\" rate=\"" + rOrigin + "\"/>");
			} else if (choice == GO_TO_PICKUP_DESTINATION_AND_SEE) {
				BayCrossroad destination = (BayCrossroad) lane.getDestination();
				Road mainRoadDestination = terminal.getRoad(destination
						.getMainRoad());
				// double rDest =
				// Location.getPourcent(destination.getLocation(),
				// mainRoadDestination);
				double rDest = Location.getAccuratePourcent(
						destination.getLocation(), mainRoadDestination);
				rsc.messageReceived("<goto road=\""
						+ mainRoadDestination.getId() + "\" rate=\"" + rDest
						+ "\"/>");
			}
		}
	}

	@Override
	public int cantPickupContainer(String straddleCarrierId,
			ContainerPickupException e) {
		String msg = "Can't pickup container ! ";
		if (e instanceof UnreachableContainerException) {
			msg += " There is actually no path to the container !";
			// Que faire ?
			Terminal terminal = Terminal.getInstance();
			int action = terminal.getTextDisplay().askForOrders(msg,
					e.getContainerLocation());
			return action;
		} else if (e instanceof ContainerNotFreeException) {
			// TODO
		}
		return -1;
	}

}
