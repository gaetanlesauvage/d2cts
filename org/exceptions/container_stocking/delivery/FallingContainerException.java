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
import org.system.container_stocking.ContainerLocation;


public class FallingContainerException extends MissionContainerDeliveryException {


	/**
	 * 
	 */
	private static final long serialVersionUID = -8407183878751573348L;

	public FallingContainerException(Mission m) {
		super(m, "There is no container on level "+(m.getDestination().getLevel()-1)+" to stack up "+m.getContainerId()+" on slot "+m.getDestination().getSlotId()+" !");
	}
	public FallingContainerException(ContainerLocation m) {
		super(m, "There is no container on level "+(m.getLevel()-1)+" to stack up "+m.getContainerId()+" on slot "+m.getSlotId()+" !");
	}
}
