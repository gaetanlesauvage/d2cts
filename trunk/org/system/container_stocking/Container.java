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

import javax.swing.ImageIcon;

import org.exceptions.ContainerDimensionException;
import org.system.Terminal;
import org.util.Location;

public class Container {
	public static final String CONTAINER_ICON_PREFIX_URL = "/etc/images/container_";
	public static final String CONTAINER_ICON_SUFFIX_URL = ".png";
	
	public static final int TYPE_IMPORT = 0;
	public static final int TYPE_EXPORT = 1;

	public static final int TYPE_20_Feet = 1;
	public static final int TYPE_40_Feet = 0;
	public static final int TYPE_45_Feet = 2;

	// public static Time handlingTime;

	private int dimensionType;
	private double teu;
	private Location location;
	private ContainerLocation containerLocation;
	private String id;
	private Slot slot;

	private boolean empty;

	public Container(String id, ContainerLocation cl, double teu)
			throws ContainerDimensionException {
		this(id, teu);

		slot = Terminal.getInstance().getSlot(cl.getSlotId());
		// if(handlingTime == null) handlingTime = new Time(0,0,30);
		containerLocation = cl;
		this.location = slot.getLocation();

	}

	public void setContainerLocation(ContainerLocation cl) {
		this.containerLocation = cl;
	}

	public Container(String id, double teu) throws ContainerDimensionException {
		this.id = id;

		if (teu == 1f)
			dimensionType = TYPE_20_Feet;
		else if (teu == 2f)
			dimensionType = TYPE_40_Feet;
		else if (teu == 2.25f)
			dimensionType = TYPE_45_Feet;
		else
			throw new ContainerDimensionException();

		this.teu = teu;
		slot = null;
	}

	// POUR QUAND LE CONTENEUR SE TROUVE SUR UN CHARIOT CAVALIER
	public Container(String id, double teu, String straddleCarrierId)
			throws ContainerDimensionException {
		this(id, teu);
		System.out.println("CONT 1");
		location = Terminal.getInstance().getStraddleCarrier(straddleCarrierId)
				.getLocation();
		System.out.println("CONT 2");
		containerLocation = null;
		slot = null;
	}

	public ContainerLocation getContainerLocation() {
		return containerLocation;
	}

	// TODO REVOIR LE STYLE EN FONCTION DU TEU
	public String getCSSStyle() {
		String orientation = location.getPourcent() < 0.5 ? "from" : "to";
		if (location.getPourcent() == 0)
			orientation = "to";
		else if (location.getPourcent() == 1)
			orientation = "from";

		String style = "shape: box; sprite-orientation: " + orientation
				+ "; fill-mode: plain; size-mode: normal; size: "
				+ ContainerKind.getLength(dimensionType) + "gu,"
				+ ContainerKind.getWidth(dimensionType) + "gu; fill-color: ";
		switch (dimensionType) {
		case Container.TYPE_20_Feet:
			style += "rgba(0,150,0,255)";
			break;
		case Container.TYPE_40_Feet:
			style += "rgba(150,0,0,255)";
			break;
		case Container.TYPE_45_Feet:
			style += "rgba(0,0,200,255)";
			break;
		}
		style += ";";
		int level = (int) (location.getCoords().z / ContainerKind
				.getHeight(this.dimensionType));
		style += " z-index: " + (50 + level) + ";";
		return style;
	}

	public int getDimensionType() {
		return dimensionType;
	}

	/*
	 * public Time getHandlingTime() { return handlingTime; }
	 */

	public String getId() {
		return id;
	}

	public Location getLocation() {
		return location;
	}

	public double getTEU() {
		return teu;
	}

	public boolean isEmpty() {
		return empty;
	}

	public void move(Location currentLocation) {
		this.location = currentLocation;
		this.containerLocation = null;
	}

	public void setLevel(int level) {
		if (containerLocation != null) {
			containerLocation.setLevel(level);
		}
	}

	public void setSlot(Slot slot) {
		this.slot = slot;

	}

	public String toString() {
		return id + " teu=" + teu + " dimensionType=" + dimensionType
				+ " location : " + containerLocation;
	}

	public ImageIcon getIcon() {
		int size = 20;
		if (teu == 2)
			size = 40;
		else if (teu > 2)
			size = 45;
		return new ImageIcon(this.getClass().getResource(
				CONTAINER_ICON_PREFIX_URL + size + CONTAINER_ICON_SUFFIX_URL));
	}
}
