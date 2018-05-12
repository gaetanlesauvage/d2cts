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
package org.routing.AStar;

import org.positioning.Coordinates;
import org.system.Road;
import org.system.RoadPoint;
import org.system.container_stocking.Bay;
import org.vehicles.models.SpeedCharacteristics;

public class DijkstraHeuristic implements AStarHeuristic {
	public static final String NAME = "DijkstraHeuristic";
	private SpeedCharacteristics speed;

	public DijkstraHeuristic(SpeedCharacteristics speed) {
		this.speed = speed;
	}

	@Override
	public double cost(RoadPoint from, RoadPoint to, Road by) {
		double speed = by instanceof Bay ? this.speed.getLaneSpeed()
				: this.speed.getSpeed();
		Coordinates posFrom = from.getLocation();
		Coordinates posTo = to.getLocation();
		double x = posTo.x - posFrom.x;
		double y = posTo.y - posFrom.y;
		double z = posTo.z - posFrom.z;
		double distance = Math.sqrt((x * x) + (y * y) + (z * z));

		return distance / speed;
	}

	@Override
	public double heuristic(RoadPoint from, RoadPoint to) {
		return 0;
	}

	@Override
	public String getHeuristicName() {
		return NAME;
	}
}
