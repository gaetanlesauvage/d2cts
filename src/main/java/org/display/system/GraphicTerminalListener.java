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
package org.display.system;

import java.rmi.RemoteException;

import org.system.Crossroad;
import org.system.Depot;
import org.system.Road;
import org.system.StraddleCarrierSlot;
import org.system.container_stocking.Bay;
import org.system.container_stocking.Container;
import org.util.Location;
import org.vehicles.StraddleCarrier;

public interface GraphicTerminalListener {
	void containerAdded(Container container);
	void containerMoved(String containerId) throws RemoteException;
	void containerRemoved(String containerId);
	void crossroadAdded(Crossroad c);
	void depotAdded(Depot depot);
	void hideLaserHeads() throws RemoteException;
	void unhideLaserHeads();
	void laneAdded(Bay lane);
	void laserHeadAdded(String id);
	void roadAdded(Road road);
	void roadRemoved(Road road);
	void resetView();
	void straddleCarrierAdded(StraddleCarrier rsc);
	void straddleCarrierMoved(String scID, Location l, String style);
	void straddleCarrierSlotAdded(StraddleCarrierSlot slot);
}
