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
package org.system.container_stocking;

import org.positioning.Coordinates;
import org.system.Terminal;

public class ContainerLocation {

	private String containerId, paveId, laneId, slotId;
	private int level, align;

	public ContainerLocation(String containerId, String paveId, String laneId,
			String slotId, int level, int align) {
		this.containerId = containerId;
		this.laneId = laneId;
		this.slotId = slotId;
		this.level = level;
		this.align = align;
		this.paveId = paveId;

	}

	public Coordinates getCoordinates() {
		Coordinates coords;
		Slot s = Terminal.getInstance().getSlot(slotId);
		Container container = Terminal.getInstance().getContainer(containerId);

		try {
			coords = s.stockContainer(container, level, align);
			s.pop(containerId);
			// System.out.println("Can stock container : "+containerId+" so coords = "+coords);
		} catch (Exception e) {
			coords = s.getCoords();
			// System.out.println("Can't stock container : "+containerId+" so coords = "+slotId+".getCoords()="+coords);
		}
		return coords;
	}

	public int getAlign() {
		return align;
	}

	public String getContainerId() {
		return containerId;
	}

	public String getLaneId() {
		return laneId;
	}

	public int getLevel() {
		return level;
	}

	public String getPaveId() {
		return paveId;
	}

	public String getSlotId() {
		return slotId;
	}

	public void setLevel(int level) {
		this.level = level;
	}

	public String toString() {
		return paveId + "-> " + laneId + "-> " + slotId + "-> level:" + level
				+ " -> align:" + align;
	}

	public String toXML() {
		return "<containerLocation container='" + containerId + "' pave='"
				+ paveId + "' lane='" + laneId + "' slot='" + slotId
				+ "' level='" + level + "' align='"
				+ ContainerAlignment.getStringValue(align) + "'/>";
	}
}
