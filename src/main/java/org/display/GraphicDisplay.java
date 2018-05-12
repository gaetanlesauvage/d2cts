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

import java.awt.Dimension;
import java.awt.Font;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

import org.display.panes.ContainerPane;
import org.display.panes.MissionPane;
import org.display.panes.ReservationPane;
import org.display.panes.SlotPane;
import org.display.panes.StraddleCarrierPane;
import org.display.solverDialogs.NewContainerLocationDialog;
import org.display.solverDialogs.NewMissionDeliveryOrderDialog;
import org.display.solverDialogs.NewMissionOrderDialog;
import org.display.system.GraphicTimeController;
import org.display.system.JTerminal;
import org.exceptions.container_stocking.delivery.MissionContainerDeliveryException;
import org.missions.Load;
import org.missions.Mission;
import org.system.Reservation;
import org.system.container_stocking.Container;
import org.system.container_stocking.ContainerLocation;
import org.system.container_stocking.Slot;
import org.time.Time;
import org.util.Location;
import org.vehicles.DeliveryOrder;
import org.vehicles.StraddleCarrier;

public abstract class GraphicDisplay extends TextDisplay {
	public static final int WIDTH = 896;
	public static final int HEIGHT = 400;

	private JFrame frame;
	private JTabbedPane tabbedPane;
	private JTextArea jta;
	private MissionPane missionPanel;
	private StraddleCarrierPane straddleCarrierPane;
	private ReservationPane reservationsPanel;
	private SlotPane slotsPanel;
	private ContainerPane containerPanel;
	public static final Font font = new Font("Time New Roman", Font.PLAIN, 9);
	public static final Font fontTiny = new Font("Time New Roman", Font.PLAIN,
			7);
	public static final Font fontTinyBold = new Font("Time New Roman",
			Font.BOLD, 8);
	public static final Font fontBold = new Font("Time New Roman", Font.BOLD, 9);

	public GraphicDisplay() {
		this("");
	}

	public GraphicDisplay(final String frameTitle) {
		super();

		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				jta = new JTextArea();
				jta.setFont(font);
				jta.setAutoscrolls(true);
				jta.setEditable(false);

				frame = new JFrame(frameTitle);
				frame.setFont(font);

				tabbedPane = new JTabbedPane();

				missionPanel = new MissionPane();
				missionPanel.setSize(new Dimension(WIDTH, HEIGHT));

				reservationsPanel = new ReservationPane();
				reservationsPanel.setSize(new Dimension(WIDTH, HEIGHT));

				slotsPanel = new SlotPane();
				slotsPanel.setSize(new Dimension(WIDTH, HEIGHT));

				straddleCarrierPane = new StraddleCarrierPane();
				straddleCarrierPane.setSize(WIDTH, HEIGHT);

				containerPanel = new ContainerPane();
				containerPanel.setSize(WIDTH, HEIGHT);

				tabbedPane.add("Missions", missionPanel);
				tabbedPane.add("Reservations", reservationsPanel);
				tabbedPane.add("Slots", slotsPanel);
				tabbedPane.add("Laser System", new JScrollPane(jta));
				tabbedPane.add("Straddle Carriers", straddleCarrierPane);
				tabbedPane.add("Containers", containerPanel);

				frame.add(tabbedPane);
				frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
				frame.setSize(new Dimension(WIDTH, JTerminal.HEIGHT
						+ GraphicTimeController.HEIGHT));
				// frame.setVisible(true);
			}
		});

	}

	@Override
	public int askForDeliveryOrders(String msg,
			ContainerLocation containerLocation) {
		return NewMissionDeliveryOrderDialog.getChoice(containerLocation, msg);
	}

	@Override
	public DeliveryOrder askForNewContainerLocation(String msg,
			MissionContainerDeliveryException exception) {
		return NewContainerLocationDialog.getSelectedContainerLocation(
				exception, msg);
	}

	@Override
	public int askForOrders(String msg, ContainerLocation containerLocation) {
		return NewMissionOrderDialog.getChoice(containerLocation, msg);
	}

	public void caretToEnd() {
		jta.setCaretPosition(jta.getText().length() - 1);
	}

	public void print(final String txt) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				jta.setText(jta.getText() + txt);
				caretToEnd();
			}
		});

	}

	public void println(String txt) {
		print(txt + "\n");
	}

	@Override
	public void addMission(final Mission m) {
		missionPanel.addMission(m);
	}

	@Override
	public void addContainer(final Container c) {
		containerPanel.addContainer(c);
	}

	@Override
	public void addStraddleCarrier(final StraddleCarrier sc) {
		straddleCarrierPane.addStraddleCarrier(sc);
	}

	@Override
	public void setVehicleToMission(String missionId, String vehicleId) {
		missionPanel.setVehicle(missionId, vehicleId);
	}

	@Override
	public void setMissionToVehicle(String vehicleID, String missionID) {
		straddleCarrierPane.setMission(vehicleID, missionID);
	}

	@Override
	public void setVehicleSpeed(String vehicleID, String speed) {
		straddleCarrierPane.setSpeed(vehicleID, speed);
	}

	@Override
	public void setVehicleLoad(String vehicleID, String contID) {
		straddleCarrierPane.setContainer(vehicleID, contID);
	}

	@Override
	public void setVehicleLocation(String vehicleID, Location l) {
		straddleCarrierPane.setLocation(vehicleID, l);
	}

	@Override
	public void missionVehicleWaitTimeChanged(String missionId, Time t) {
		// missionPanel.missionVehicleWaitTimeChanged(missionId, t);
	}

	@Override
	public void missionStateChanged(String missionId, String newState) {
		missionPanel.setMissionState(missionId, newState);
	}

	@Override
	public void missionChanged(Load l) {
		missionPanel.setMission(l);
	}

	@Override
	public void containerLocationChanged(String containerID,
			ContainerLocation cl) {
		containerPanel.setContainerLocation(containerID, cl);
	}

	@Override
	public void addReservation(final Reservation r) {
		reservationsPanel.addReservation(r);
	}

	@Override
	public void removeReservation(Reservation r, Time unreservationTime) {
		reservationsPanel.removeReservation(r, unreservationTime);
	}

	@Override
	public void removeContainer(String containerID) {
		containerPanel.removeContainer(containerID);
	}

	@Override
	public void addSlots(final List<Slot> l) {
		for (Slot s : l)
			slotsPanel.addSlot(s);
	}

	@Override
	public void slotContentChanged(Slot s) {
		slotsPanel.contentChanged(s);
	}

	public void show() {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				frame.setVisible(true);
			}
		});

	}
}
