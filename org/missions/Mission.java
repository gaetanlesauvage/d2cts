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
package org.missions;

import java.util.List;
import java.util.StringTokenizer;

import org.exceptions.ContainerDimensionException;
import org.system.Terminal;
import org.system.container_stocking.Container;
import org.system.container_stocking.ContainerAlignment;
import org.system.container_stocking.ContainerLocation;
import org.system.container_stocking.Slot;
import org.time.TimeScheduler;
import org.time.TimeWindow;
import org.vehicles.Ship;

public class Mission implements Comparable<Mission> {
	public static final String DEPOT = "DEPOT";

	protected TimeWindow pickupTW, deliveryTW;
	protected String containerId;
	protected ContainerLocation destination;
	protected String id;
	protected MissionKinds missionKind;

	public Mission(String id, int missionKind, TimeWindow pickupTW,
			TimeWindow deliveryTW, String containerId,
			ContainerLocation missionLocation) {
		this.pickupTW = pickupTW;
		this.deliveryTW = deliveryTW;
		this.containerId = containerId;
		this.destination = missionLocation;
		this.missionKind = MissionKinds.getKind(missionKind);
		this.id = id;
	}

	public Container getContainer() {
		Container c = Terminal.getInstance().getContainer(containerId);
		if(c == null){
			if(id.startsWith("unloadShip")){

				try {
					c = new Container(containerId, TimeScheduler.getInstance().getIncomingContainerTeu(containerId));
				} catch (ContainerDimensionException e) {
					e.printStackTrace();
				}
				if(c != null){
					//Quay
					String quay = id.substring("unloadShip".length(),id.indexOf(']'));
					String s = quay.substring(quay.indexOf("[")+1);
					StringTokenizer st = new StringTokenizer(s, "-");
					String from = st.nextToken();
					String to = st.nextToken();
					quay = s.substring(0, s.indexOf("["));
					Ship sh = Terminal.getInstance().getShip(quay, Double.parseDouble(from), Double.parseDouble(to));
					if(sh != null){
						List<String> concernedSlotsIDs = sh.getConcernedSlotsIDs();
						String slotID = concernedSlotsIDs.get(Terminal.getInstance().getRandom().nextInt(concernedSlotsIDs.size()));
						Slot slot = Terminal.getInstance().getSlot(slotID);
						c.setContainerLocation(new ContainerLocation(containerId, quay, slot.getLocation().getRoad().getId(), slot.getId(), 0, ContainerAlignment.center.getValue()));
					}
				}
			}
		}
		return c;
	}

	public TimeWindow getDeliveryTimeWindow() {
		return deliveryTW;
	}

	public ContainerLocation getDestination() {
		return destination;
	}

	public String getId() {
		return id;
	}

	public MissionKinds getMissionKind() {
		return missionKind;
	}

	public TimeWindow getPickupTimeWindow() {
		return pickupTW;
	}

	public void setDestination(ContainerLocation contLocation) {
		destination = contLocation;
	}

	public String getContainerId() {
		return containerId;
	}

	public String toString() {
		return id + " move " + containerId + " to " + destination.getSlotId() + " ("
				+ pickupTW + " - " + deliveryTW + ")";
	}

	public String toXML() {
		return "<mission id='" + id + "' container='" + containerId
				+ "' kind='" + missionKind.getIntValue() + "'>"
				+ pickupTW.toXML() + deliveryTW.toXML() + destination.toXML()
				+ "</mission>";
	}

	public void setDeliveryTimeWindow(TimeWindow d) {
		deliveryTW = d;
	}

	public void setPickupTimeWindow(TimeWindow p) {
		pickupTW = p;
	}

	public int hashCode(){
		return id.hashCode();
	}

	public boolean equals(Object o){
		return o.hashCode() == hashCode();
	}

	@Override
	public int compareTo(Mission m) {
		if (m.getId().equals(id))
			return 0;
		else {
			int cmp = pickupTW.getMin().compareTo(
					m.getPickupTimeWindow().getMin());
			if (cmp == 0) {
				cmp = pickupTW.getMax().compareTo(
						m.getPickupTimeWindow().getMax());
				if (cmp == 0) {
					cmp = deliveryTW.getMin().compareTo(
							m.getDeliveryTimeWindow().getMin());
					if (cmp == 0)
						cmp = deliveryTW.getMax().compareTo(
								m.getDeliveryTimeWindow().getMax());
				}
			}
			return cmp;
		}
	}
}