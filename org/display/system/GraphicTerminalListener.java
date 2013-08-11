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
	
	
	public void containerAdded(Container container);
	public void containerMoved(String containerId) throws RemoteException;
	public void containerRemoved(String containerId);
	public void crossroadAdded(Crossroad c);
	public void depotAdded(Depot depot);
	public void hideLaserHeads() throws RemoteException;
	public void unhideLaserHeads() throws RemoteException;
	public void laneAdded(Bay lane);
	public void laserHeadAdded(String id) throws RemoteException;
	//public void paveAdded(Pave pave);
	public void roadAdded(Road road);
	public void roadRemoved(Road road);
	public void resetView() throws RemoteException;
	public void straddleCarrierAdded(StraddleCarrier rsc);
	public void straddleCarrierMoved(String scID, Location l, String style) throws RemoteException;
	public void straddleCarrierSlotAdded(StraddleCarrierSlot slot);
	public void destroy() throws RemoteException;
}
