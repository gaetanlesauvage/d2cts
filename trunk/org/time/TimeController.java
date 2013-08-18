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
	protected static TimeScheduler SCHEDULER;

	public TimeController() {
		if (SCHEDULER == null)
			SCHEDULER = TimeScheduler.getInstance();
	}

	public double getSecByStep() {
		return SCHEDULER.getSecondsPerStep();
	}

	public long getStep() {
		return SCHEDULER.getStep();
	}

	public Time getTime() {
		return SCHEDULER.getTime();
	}

	public void nextStep(boolean isSynchronized) {
		SCHEDULER.step(isSynchronized);
	}

	public void setSecByStep(double newStepSize) {
		SCHEDULER.setSecondsPerStep(newStepSize);
	}

}