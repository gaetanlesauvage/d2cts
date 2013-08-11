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

import java.util.HashMap;
import java.util.Map;

import org.exceptions.LaneTooSmallException;
import org.system.Reservation;
import org.system.Reservations;
import org.system.Road;
import org.system.Terminal;
import org.time.Time;
import org.time.TimeScheduler;
import org.time.TimeWindow;
import org.util.building.SlotBuilderHelper;
import org.util.building.SlotsRateHelper;
import org.vehicles.StraddleCarrier;

public class Bay extends Road {

	private String wayIn, wayOut;
	@SuppressWarnings("unused")
	private BayCrossroad in, out;
	private String paveId;
	//private Coordinates inPoint, outPoint;
	private String laneGroup = "";
	
	public Bay(String id, BayCrossroad cOrigin, BayCrossroad cDestination,
			boolean directed, String pave, String laneGroup) {
		this(id, cOrigin, cDestination, directed, pave);
		this.laneGroup = laneGroup;

	}

	public Bay(String id, BayCrossroad cOrigin, BayCrossroad cDestination,
			boolean directed, String pave) {
		super(id, cOrigin, cDestination, directed);
		
		this.paveId = pave;
		this.in = cOrigin;
		this.out = cDestination;
		this.wayIn = cOrigin.getMainRoad();
		this.wayOut = cOrigin.getMainRoad();
	}

	public static Map<String, SlotsRateHelper> getSlotsIdsAndRates(
			SlotBuilderHelper[] tSlots, double innerLength, double bayLength,
			String bayID, double inPointRate) throws LaneTooSmallException {

		double slotsLength = 0.0;
		int nbSltos = 0;
		Map<String, SlotsRateHelper> result = new HashMap<>();
		for (SlotBuilderHelper s : tSlots) {
			String type = s.getType();
			int quantity = s.getQuantity();
			double l = 0.0;
			if (type.equals("20"))
				l = Slot.SLOT_20_FEET_LENGTH;
			else if (type.equals("40"))
				l = Slot.SLOT_40_FEET_LENGTH;
			else
				l = Slot.SLOT_45_FEET_LENGTH;

			l = l * quantity;
			nbSltos += quantity;
			slotsLength += l;
		}

		double ecartTotal = innerLength - slotsLength;
		if (ecartTotal < 0)
			throw new LaneTooSmallException(bayID);
		else {
			double ecartMoyen = ecartTotal / (nbSltos + 1.0);

			// double inPointRate = Location.getAccuratePourcent(inPoint, this);
			double rateEcartMoyen = ecartMoyen / bayLength;
			// Let's build the slots
			double rate = inPointRate + rateEcartMoyen;
			for (SlotBuilderHelper sbh : tSlots) {
				double length;
				if (sbh.getType().equals("20")) {
					length = Slot.SLOT_20_FEET_LENGTH;
				} else if (sbh.getType().equals("40")) {
					length = Slot.SLOT_40_FEET_LENGTH;
				} else {
					length = Slot.SLOT_45_FEET_LENGTH;
				}
				double rateLength = length / bayLength;

				for (int i = 0; i < sbh.getQuantity(); i++) {
					rate += (rateLength / 2.0);
					SlotsRateHelper rh = new SlotsRateHelper(sbh.getType(),
							rate);
					result.put(bayID + "-" + result.size(), rh);
					rate += (rateEcartMoyen + (rateLength / 2.0));
				}
			}
		}
		return result;
	}

	
	/*public List<Slot> addSlots(SlotBuilderHelper[] tSlots, BlockType pType)
			throws LaneTooSmallException {
		// this.paveType = pType;

		double overalLength = this.getInnerLength();
		double slotsLength = 0.0;
		int nbSltos = 0;
		List<Slot> result = new ArrayList<Slot>();
		for (SlotBuilderHelper s : tSlots) {
			String type = s.getType();
			int quantity = s.getQuantity();
			double l = 0.0;
			if (type.equals("20"))
				l = Slot.SLOT_20_FEET_LENGTH;
			else if (type.equals("40"))
				l = Slot.SLOT_40_FEET_LENGTH;
			else
				l = Slot.SLOT_45_FEET_LENGTH;

			l = l * quantity;
			nbSltos += quantity;
			slotsLength += l;
		}

		double ecartTotal = overalLength - slotsLength;
		if (ecartTotal < 0)
			throw new LaneTooSmallException(this.getId());
		else {
			double ecartMoyen = ecartTotal / (nbSltos + 1.0);
			// System.out.println("Ecart moyen = "+ecartMoyen+" / Ecart total : "+ecartTotal+" / Inner : "+overalLength+" / Slots length : "+slotsLength);
			double inPointRate = Location.getPourcent(inPoint, this);
			// double inPointRate = Location.getAccuratePourcent(inPoint, this);
			double rateEcartMoyen = ecartMoyen / this.getLength();
			// Let's build the slots
			double rate = inPointRate + rateEcartMoyen;
			for (SlotBuilderHelper sbh : tSlots) {
				double length;
				double teuSize;

				if (sbh.getType().equals("20")) {
					length = Slot.SLOT_20_FEET_LENGTH;
					teuSize = 1f;
				} else if (sbh.getType().equals("40")) {
					length = Slot.SLOT_40_FEET_LENGTH;
					teuSize = 2f;
				} else {
					length = Slot.SLOT_45_FEET_LENGTH;
					teuSize = 2.25f;
				}
				double rateLength = length / this.getLength();

				// System.out.println("Length = "+length+" rateLength = "+rateLength+" RateEcartMoyen = "+rateEcartMoyen);
				for (int i = 0; i < sbh.getQuantity(); i++) {
					rate += (rateLength / 2.0);
					Slot s = new Slot(getId() + "-" + result.size(), this,
							new Location(this, rate, true), teuSize, pType);

					result.add(s);
					// slotsStack.push(s);
					// System.out.println("Slot "+s.getId()+" created at "+s.getRateOnLane());
					rate += (rateEcartMoyen + (rateLength / 2.0));
				}
			}
		}
		return result;
	}*/

	/*public double getInnerLength() {
		return Location.getLength(inPoint, outPoint);
	}*/

	public String getPaveId() {
		return paveId;
	}

	public String getWayIn() {
		return wayIn;
	}

	public String getWayOut() {
		return wayOut;
	}

	@Override
	public double getWaitingCost(StraddleCarrier vehicle, Time t, int priority) {
		Reservations reservations = Terminal.getInstance().getReservations(
				getId());
		if (reservations == null) {
			reservations = new Reservations(this);
			Terminal.getInstance().putReservations(getId(), reservations);
			return 0;
		} else {

			Time duration = new Time((getLength() / vehicle.getSpeed(this, t)));
			Time tmax = new Time(t, duration);
			TimeWindow window = new TimeWindow(t, tmax);
			TimeWindow tw = reservations
					.getFirstAvailableTimeWindow(new Reservation(TimeScheduler.getInstance()
							.getTime(), vehicle.getId(), this.getId(), window,
							priority));
			// System.out.println("end -");
			double gap = tw.getMin().getInSec() - t.getInSec();
			if (gap < 0)
				System.out.println("GAP IS NEGATIVE !!!");
			return gap;
		}
	}

	@Override
	public double getWaitingCost(StraddleCarrier vehicle, Time t,
			double duration, int priority) {
		Reservations reservations = Terminal.getInstance().getReservations(
				getId());
		if (reservations == null) {
			reservations = new Reservations(this);
			Terminal.getInstance().putReservations(getId(), reservations);
			return 0;
		} else {
			Time tDuration = new Time(duration);
			Time tmax = new Time(t, tDuration);
			TimeWindow window = new TimeWindow(t, tmax);
			TimeWindow tw = reservations
					.getFirstAvailableTimeWindow(new Reservation(TimeScheduler.getInstance()
							.getTime(), vehicle.getId(), this.getId(), window,
							priority));
			// System.out.println("end -");
			double gap = tw.getMin().getInSec() - t.getInSec();
			if (gap < 0)
				System.out.println("GAP IS NEGATIVE !!!");
			return gap;
		}
	}

	public String getLaneGroup() {
		return laneGroup;
	}

	public void destroy() {
		in = null;
		out = null;
	}
}