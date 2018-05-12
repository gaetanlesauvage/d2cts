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
package org.scheduling.aco.graph;


import org.scheduling.ScheduleTask;
/**
 * Start node of the ACO graph. This is the node where the ants starts their foraging, it represents the depot slot of the ant associated straddle carrier.
 *  
 * @author Ga&euml;tan Lesauvage
 * @since 2011
 */
public class DepotNode extends AntNode{
	public static final String ID = ScheduleTask.SOURCE_ID;
	
	private static final String DEFAULT_DEPOT_NODE_STYLE = "fill-mode: plain; fill-color: black; stroke-mode: plain; stroke-color: rgba(77,148,255,127);" +
			"stroke-width: 1; text-size: 10; text-color: white; text-style: bold; text-background-mode: none; text-background-color: rgba(77,148,255,127);" +
			"text-alignment: center; shape: circle; size-mode: normal; size: 40px; z-index:3; text-mode: normal; text-padding: 2px;";
	
	public DepotNode() {
		super(null);
		node.addAttribute("xy", 0,0);
		node.addAttribute("ui.class", "depotNode");
		node.addAttribute("label", ID);
		node.addAttribute("ui.style",DEFAULT_DEPOT_NODE_STYLE);
	}
	@Override
	public String getID(){
		return ID;
	}
}
