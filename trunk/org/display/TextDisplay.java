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
package org.display;

import java.util.List;
import java.util.Scanner;

import org.apache.log4j.Logger;
import org.exceptions.container_stocking.delivery.MissionContainerDeliveryException;
import org.missions.Load;
import org.missions.Mission;
import org.system.Reservation;
import org.system.Terminal;
import org.system.container_stocking.Bay;
import org.system.container_stocking.Container;
import org.system.container_stocking.ContainerAlignment;
import org.system.container_stocking.ContainerLocation;
import org.system.container_stocking.Slot;
import org.time.Time;
import org.time.TimeScheduler;
import org.util.Location;
import org.util.RecordableObject;
import org.vehicles.DeliveryOrder;
import org.vehicles.StraddleCarrier;

public abstract class TextDisplay implements RecordableObject{
	private static final Logger log = Logger.getLogger(TextDisplay.class);

	private static int counter = 0;
	private String id;

	public TextDisplay() {
		counter++;
		id = this.getClass().getName() + ":" + counter;
	}

	public int askForDeliveryOrders(String msg,
			ContainerLocation containerLocation) {
		Scanner scan = null;
		int choix = 0;
		try {
			scan = new Scanner(System.in);

			println(msg);

			Bay l = Terminal.getInstance().getBay(
					containerLocation.getLaneId());
			boolean ok = false;
			int choixMax = 1;
			while (!ok) {
				println("Change destination : 0");
				println("GOTO " + l.getOriginId() + " : 1");
				if (!l.isDirected()) {
					println("GOTO " + l.getDestinationId() + " : 2");
					choixMax = 2;
				}

				choix = Integer.parseInt(scan.nextLine());
				if (choix >= 0 && choix <= choixMax)
					ok = true;
				else
					System.out.println("Wrong choice !");
			}
		} finally {
			if (scan != null)
				scan.close();
		}
		return choix;
	}

	public DeliveryOrder askForNewContainerLocation(String msg,
			MissionContainerDeliveryException exception) {
		Scanner scan = null;
		String choice = "";

		try {
			scan = new Scanner(System.in);

			println(msg);

			while (choice.equals("")) {
				println("New location = 1");
				println("Wait = 2");
				choice = scan.nextLine();
				if (!choice.equals("1") && !choice.equals("2"))
					choice = "";
			}
		} finally {
			if (scan != null) {
				scan.close();
			}
		}
		if (choice.equals("1")) {
			println("Pave id : ");
			String paveId = scan.nextLine();
			println("Lane id : ");
			String laneId = scan.nextLine();
			println("Slot id : ");
			String slotId = scan.nextLine();
			println("Level : ");
			int level = Integer.parseInt(scan.nextLine());
			println("Alignment : ");
			String alignString = scan.nextLine();

			return new DeliveryOrder(exception.getMission(),
					new ContainerLocation(exception.getMission()
							.getContainerId(), paveId, laneId, slotId, level,
							ContainerAlignment.valueOf(alignString).getValue()));
		} else {
			println("Wait time duration (hh:mm:ss) : ");
			String steps = scan.nextLine();
			Time t = new Time(TimeScheduler.getInstance().getTime(),
					new Time(steps));
			return new DeliveryOrder(exception.getMission(), new Location(
					Terminal.getInstance()
							.getBay(
									exception.getMission().getDestination()
											.getLaneId()), 0), t);
		}
	}

	public int askForOrders(String msg, ContainerLocation containerLocation) {
		Scanner scan = null;
		int choix = 0;

		try {
			scan = new Scanner(System.in);

			println(msg);

			Bay l = Terminal.getInstance().getBay(
					containerLocation.getLaneId());
			boolean ok = false;
			int choixMax = 1;

			while (!ok) {
				println("Abort mission : 0");
				println("GOTO " + l.getOriginId() + " : 1");
				if (!l.isDirected()) {
					println("GOTO " + l.getDestinationId() + " : 2");
					choixMax = 2;
				}

				choix = Integer.parseInt(scan.nextLine());
				if (choix >= 0 && choix <= choixMax)
					ok = true;
				else
					log.info("Wrong choice !");
			}
		} finally {
			if (scan != null)
				scan.close();
		}
		return choix;
	}

	public String getId() {
		return id;
	}

	public void print(String s) {
		log.info(s);
	}

	public void println(String s) {
		log.info(s + "\n");
	}

	/*
	 * public void setRemoteDisplay(String uri) { //do it itself !
	 * 
	 * }
	 * 
	 * public void addMission(Mission m) { //Do nothing! }
	 * 
	 * public void setVehicleToMission(String missionId, String vehicleId) {
	 * //Do nothing! }
	 * 
	 * public void missionVehicleWaitTimeChanged(String missionId, Time t) {
	 * //Do nothing }
	 * 
	 * public void missionStateChanged(String missionId, String newState) { //Do
	 * nothing! }
	 * 
	 * public void addReservation(Reservation r) { //Do nothing! }
	 * 
	 * public void removeReservation(Reservation r, Time unreservationTime) {
	 * //Do nothing! }
	 * 
	 * public void addSlots(List<Slot> l) { //Do nothing! }
	 * 
	 * public void slotContentChanged(Slot s) { //Do nothing! }
	 * 
	 * 
	 * public void setMissionToVehicle(String vehicleID, String missionID) {
	 * //Do nothing! }
	 * 
	 * 
	 * public void setVehicleLoad(String vehicleID, String contID) { //Do
	 * nothing! }
	 * 
	 * 
	 * public void addStraddleCarrier(StraddleCarrier sc) { //Do nothing!
	 * }
	 * 
	 * public void addContainer(Container c) { // TODO Auto-generated method
	 * stub
	 * 
	 * }
	 * 
	 * public void containerLocationChanged(String containerID,
	 * ContainerLocation cl) {
	 * 
	 * }
	 * 
	 * public void removeContainer(String containerID) {
	 * 
	 * }
	 * 
	 * 
	 * public void missionChanged(Load l) {
	 * 
	 * }
	 */
	public void destroy() {
		id = null;
	}

	@Override
	public TextDisplay getTextDisplay() {
		return this;
	}

	
	public abstract void setTextDisplay(TextDisplay display);

	public abstract void addMission(Mission m);

	public abstract void addContainer(Container c);

	public abstract void addStraddleCarrier(StraddleCarrier sc);

	public abstract void setVehicleToMission(String missionId, String vehicleId);

	public abstract void setMissionToVehicle(String vehicleID, String missionID);

	public abstract void setVehicleSpeed(String vehicleID, String speed);

	public abstract void setVehicleLoad(String vehicleID, String contID);

	public abstract void setVehicleLocation(String vehicleID, Location l);

	public abstract void missionVehicleWaitTimeChanged(String missionId, Time t);

	public abstract void missionStateChanged(String missionId, String newState);

	public abstract void missionChanged(Load l);

	public abstract void containerLocationChanged(String containerID,ContainerLocation cl);

	public abstract void addReservation(Reservation r);

	public abstract void removeReservation(Reservation r, Time unreservationTime);

	public abstract void removeContainer(String containerID);

	public abstract void addSlots(List<Slot> l);

	public abstract void slotContentChanged(Slot s);
	
	
	/*
	 * public void setVehicleLocation(String vehicleID, Location l) {
	 * 
	 * }
	 * 
	 * public void setVehicleSpeed(String vehicleID, String speed) {
	 * 
	 * }
	 */
}
