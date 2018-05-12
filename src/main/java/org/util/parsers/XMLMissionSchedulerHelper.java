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
package org.util.parsers;

import org.scheduling.MissionSchedulerEvalParameters;

/**
 * Helper used to set the mission scheduler parameters
 * @author Ga&euml;tan Lesauvage
 * @since 2012
 */
public class XMLMissionSchedulerHelper {
	private MissionSchedulerEvalParameters evalParameters;
	
	public XMLMissionSchedulerHelper(MissionSchedulerEvalParameters evalParameters){
		this.evalParameters = evalParameters;
	}
	
	public MissionSchedulerEvalParameters getEvalParameters() {
		return evalParameters;
	}

}
