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
package org.time;

public class TimeController {
	public static final String rmiBindingName = "TimeController";
	
	public TimeController() {
	}

	public double getSecByStep() {
		return TimeScheduler.getInstance().getSecondsPerStep();
	}

	public long getStep() {
		return TimeScheduler.getInstance().getStep();
	}

	public Time getTime() {
		return TimeScheduler.getInstance().getTime();
	}

	public boolean nextStep(boolean isSynchronized) {
		return TimeScheduler.getInstance().step(isSynchronized);
	}

	public void setSecByStep(double newStepSize) {
		TimeScheduler.getInstance().setSecondsPerStep(newStepSize);
	}
}
