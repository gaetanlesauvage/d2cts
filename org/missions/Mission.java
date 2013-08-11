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

import org.system.Terminal;
import org.system.container_stocking.Container;
import org.system.container_stocking.ContainerLocation;
import org.time.TimeWindow;

public class Mission implements Comparable<Mission> {
	public static final String DEPOT = "DEPOT";

	private TimeWindow pickupTW, deliveryTW;
	private String containerId;
	private ContainerLocation destination;
	private String id;
	private MissionKinds missionKind;
	private static Terminal terminal;

	public Mission(String id, int missionKind, TimeWindow pickupTW,
			TimeWindow deliveryTW, String containerId,
			ContainerLocation missionLocation) {
		this.pickupTW = pickupTW;
		this.deliveryTW = deliveryTW;
		this.containerId = containerId;
		this.destination = missionLocation;
		this.missionKind = MissionKinds.getKind(missionKind);
		this.id = id;
		if (terminal == null)
			terminal = Terminal.getInstance();
	}

	public Container getContainer() {
		Container c = terminal.getContainer(containerId);
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
		return id + " move " + containerId + " to " + destination + " ("
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

	public void destroy() {
		terminal = null;
		deliveryTW = null;
		destination = null;
		missionKind = null;
		pickupTW = null;
	}
}