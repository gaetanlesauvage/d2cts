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
package org.exceptions.container_stocking.delivery;

import org.missions.Mission;
import org.system.Terminal;
import org.system.container_stocking.Container;
import org.system.container_stocking.ContainerLocation;

public class CollisionException extends MissionContainerDeliveryException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 297924288075427563L;

	private double collisionRate;

	private String message;

	public CollisionException(Mission m, double collisionRate) {
		super(m, "Collision at rate " + collisionRate + " on slot "
				+ m.getDestination().getSlotId() + " level "
				+ m.getDestination().getLevel() + " while stacking container "
				+ m.getDestination().getContainerId() + " !");
		Container c = m.getContainer();

		this.collisionRate = collisionRate;
		ContainerLocation location = m.getDestination();
		message = "Collision at rate " + collisionRate + " on slot "
				+ location.getSlotId() + " level " + location.getLevel()
				+ " while stacking container " + c + " !";

	}

	public CollisionException(ContainerLocation location, double collisionRate) {
		super(location, "Collision at rate " + collisionRate + " on slot "
				+ location.getSlotId() + " level " + location.getLevel()
				+ " while stacking container " + location.getContainerId()
				+ " !");
		Container c = Terminal.getInstance().getContainer(
				location.getContainerId());
		this.collisionRate = collisionRate;
		message = "Collision at rate " + collisionRate + " on slot "
				+ location.getSlotId() + " level " + location.getLevel()
				+ " while stacking container " + c + " !";

	}

	public double getCollisionRate() {
		return collisionRate;
	}

	public String getErrorMessage() {
		return message;
	}
}
