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
package org.util.generators;

import java.io.Serializable;

import org.system.container_stocking.Container;
import org.system.container_stocking.ContainerLocation;
import org.system.container_stocking.Level;
import org.system.container_stocking.Slot;
import org.time.TimeWindow;

/**
 * Slot Reservation. This object reserves a container location through time
 * @author Ga&euml;tan Lesauvage
 * @since 2011
 */
class SlotReservation implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 3289226178840887909L;

	/**
	 * Time window of the reservation
	 */
	private TimeWindow tw;
	/**
	 * Slot of the reservation
	 */
	private Slot slot;
	/**
	 * Container of the reservation
	 */
	private Container container;
	/**
	 * ContainerAlignement int value of the reservation
	 */
	private int align;
	/**
	 * Level of the reservation
	 */
	private Level level;

	/**
	 * Constructor
	 * @param container Container
	 * @param slot Slot
	 * @param tw Time windox
	 * @param align Int value of the container alignment
	 * @param level Level of the reservation
	 */
	public SlotReservation (Container container, Slot slot, TimeWindow tw, int align, Level level){
		this.tw = tw;
		this.slot = slot;
		this.container = container;
		this.align = align;
		this.level = level;
	}

	/**
	 * Get the container location of the reservation
	 * @return the container location of the reservation
	 */
	public ContainerLocation getContainerLocation() {
		ContainerLocation cl = new ContainerLocation(container.getId(), slot.getPaveId(), slot.getLocation().getRoad().getId(), slot.getId(), level.getLevelIndex(), align);
		return cl;
	}

	/**
	 * Set the container of the reservation
	 * @param cont Container
	 */
	public void setContainer(Container cont) {
		this.container = cont;
	}

	/**
	 * Get the Time Window of the reservation
	 * @return The Time Window of the reservation
	 */
	public TimeWindow getTW() {
		return tw;
	}

	/**
	 * Get the slot of the reservation
	 * @return The slot of the reservation
	 */
	public Slot getSlot() {
		return slot;
	}

	/**
	 * Get the container of the reservation
	 * @return The container of the reservation 
	 */
	public Container getContainer() {
		return container;
	}

	/**
	 * Get the level of the reservation
	 * @return The level of the reservation
	 */
	public Level getLevel(){
		return level;
	}

	/**
	 * Get the int value of the container alignment of the reservation
	 * @return the int value of the container alignment of the reservation
	 */
	public int getAlignment(){
		return align;
	}

	/**
	 * Set the time window of the reservation
	 * @param tw New Time Window of the reservation
	 */
	public void setTW(TimeWindow tw) {
		this.tw = tw;
	}
	
	public String toString(){
		return container.getId()+" level "+level.getLevelIndex()+" "+align+" "+tw;
	}
}