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
import org.exceptions.container_stocking.delivery.MissionContainerDeliveryException;
import org.exceptions.container_stocking.delivery.UnreachableSlotException;

public interface StraddleCarrierProblemListener {
	public static final int ABORT_MISSION = 0;
	public static final int GO_TO_PICKUP_ORIGIN_AND_SEE = 1;
	public static final int GO_TO_PICKUP_DESTINATION_AND_SEE = 2;
	public static final int ASK_FOR_NEW_MISSION_TO_FREE_CONTAINER = 3;

	public DeliveryOrder cantDeliverContainer(String straddleCarrierId,
			MissionContainerDeliveryException e, TextDisplay out);

	public void cantDeliverContainerOnUnreachableSlot(String id,
			UnreachableSlotException unreachableSlotException, TextDisplay out);

	public int cantPickupContainer(String straddleCarrierId,
			ContainerPickupException e);
}
