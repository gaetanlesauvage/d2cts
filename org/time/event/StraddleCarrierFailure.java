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
package org.time.event;

import org.conf.parameters.ReturnCodes;
import org.system.Terminal;
import org.time.Time;
import org.vehicles.StraddleCarrier;

public class StraddleCarrierFailure extends DynamicEvent {
	private Time repairDuration;
	private String straddleCarrierID;
	private String failureType;

	private static final String TYPE = "StraddleCarrierFailure";
	public static final double TOWING_SPEED_RATIO = 0.33;

	public StraddleCarrierFailure(Time time, String id, String type,
			Time duration) {
		super(time, TYPE, "<event time='" + time + "' type='" + TYPE
				+ "' duration='" + duration + "' straddleId='" + id + "'/>");
		this.repairDuration = duration;
		this.straddleCarrierID = id;
		this.failureType = type;
	}

	@Override
	public void execute() {
		StraddleCarrier rsc = Terminal.getInstance().getStraddleCarrier(straddleCarrierID);
		if (rsc == null) {
			new Exception("Straddle carrier " + straddleCarrierID
					+ " not found ! Program will exit.").printStackTrace();
			System.exit(ReturnCodes.STRADDLECARRIER_NOT_FOUND.getCode());
		}
		rsc.fail(failureType, repairDuration);

		writeEventInDb();
		System.out.println("Executing StraddleFailure Event !");
	}
}
