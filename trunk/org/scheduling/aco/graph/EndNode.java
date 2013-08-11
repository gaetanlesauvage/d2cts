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

/**
 * Target node of the ACO graph. This is the node where the ants ends their foraging (source of food). As the start node, the end node represents the depot slot of the ant associated straddle carrier to reach when the vehicle is idle.
 *  
 * @author Ga&euml;tan Lesauvage
 * @since 2011
 */
public class EndNode extends AntNode{
	public static final String ID = AntNode.END_NODE_ID;
	private static final String DEFAULT_END_NODE_STYLE = "fill-mode: plain; fill-color: black; stroke-mode: plain; stroke-color: rgba(77,148,255,127);" +
			"stroke-width: 1; text-size: 10; text-color: white; text-style: bold; text-background-mode: none; text-background-color: rgba(77,148,255,127);" +
			"text-alignment: center; shape: circle; size-mode: normal; size: 40px; z-index:3; text-mode: normal; text-padding: 2px;";
	
	public EndNode() {
		super(null);
		node.addAttribute("xy", 0,0);
		node.addAttribute("ui.class", "endNode");
		node.addAttribute("label", ID);
		node.addAttribute("ui.style",DEFAULT_END_NODE_STYLE); 
	}
	@Override
	public String getID(){
		return ID;
	}
}