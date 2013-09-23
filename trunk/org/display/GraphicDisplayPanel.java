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

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.util.List;

import javax.swing.JPanel;
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

public class GraphicDisplayPanel extends TextDisplay{

	public static final int WIDTH = 896;
	public static final int HEIGHT = 400;
	
	private JPanel panel;
	private JTabbedPane tabbedPane;
	private JTextArea jta;
	private MissionPane missionPanel;
	private StraddleCarrierPane straddleCarrierPane;
	private ReservationPane reservationsPanel;
	private SlotPane slotsPanel;
	private ContainerPane containerPanel;
	public static final Font font = new Font("Time New Roman", Font.PLAIN, 9);
	public static final Font fontBold = new Font("Time New Roman", Font.BOLD, 9);
	
	private static GraphicDisplayPanel instance;

	public static GraphicDisplayPanel getInstance(){
		if(instance == null)
			instance = new GraphicDisplayPanel();
		return instance;
	}

	public static void closeInstance() {
		if(instance != null){
			instance = null;
		}
	}
	private GraphicDisplayPanel ()  {
		super();
		
		//try {
			/*SwingUtilities.invokeAndWait(new Runnable() {
				public void run() {*/
					jta = new JTextArea();
					jta.setFont(font);
					jta.setAutoscrolls(true);
					jta.setEditable(false);

					panel = new JPanel(new BorderLayout());
					panel.setFont(font);

					tabbedPane = new JTabbedPane();
					tabbedPane.setFont(GraphicDisplay.fontBold);

					missionPanel = new MissionPane();

					reservationsPanel = new ReservationPane();

					slotsPanel = new SlotPane();

					straddleCarrierPane = new StraddleCarrierPane();

					containerPanel = new ContainerPane();

					tabbedPane.add("Missions", missionPanel);
					tabbedPane.add("Reservations", reservationsPanel);
					tabbedPane.add("Slots", slotsPanel);

					tabbedPane.add("Straddle Carriers", straddleCarrierPane);
					tabbedPane.add("Containers", containerPanel);
					tabbedPane.add("Information", new JScrollPane(jta));
					panel.add(tabbedPane,BorderLayout.CENTER);
					panel.setPreferredSize(new Dimension(WIDTH,HEIGHT));
				/*}
			});
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/

	}

	
	public int askForDeliveryOrders(String msg, ContainerLocation containerLocation)  {
		return NewMissionDeliveryOrderDialog.getChoice(containerLocation,msg);
	}
	
	public DeliveryOrder askForNewContainerLocation(String msg, MissionContainerDeliveryException exception)  {
		return NewContainerLocationDialog.getSelectedContainerLocation(exception,msg);
	}

	
	public int askForOrders(String msg, ContainerLocation containerLocation)  {
		return NewMissionOrderDialog.getChoice(containerLocation,msg);
	}

	public void caretToEnd() {
		jta.setCaretPosition(jta.getText().length()-1);
	}

	public void print (final String txt)  {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				jta.setText(jta.getText()+txt);
				caretToEnd();
			}
		});

	}
	public void println( String txt )  {
		print(txt+"\n");
	}
	
	public void addMission(final Mission m)  {
		missionPanel.addMission(m);
	}
	
	public void addContainer(final Container c)  {
		containerPanel.addContainer(c);
	}
	
	public void addStraddleCarrier(final StraddleCarrier sc) {
		straddleCarrierPane.addStraddleCarrier(sc);
	}

	
	public void setVehicleToMission(final String missionId, final String vehicleId)  {
		missionPanel.setVehicle(missionId, vehicleId);
		//System.out.println(missionId+" -> "+vehicleId);
	}
	
	public void setMissionToVehicle(String vehicleID, String missionID)  {
		straddleCarrierPane.setMission(vehicleID, missionID);
	}
	
	public void setVehicleSpeed(String vehicleID, String speed)  {
		straddleCarrierPane.setSpeed(vehicleID, speed);
	}
	
	public void setVehicleLoad(String vehicleID, String contID)  {
		straddleCarrierPane.setContainer(vehicleID, contID);
	}
	
	public void setVehicleLocation(String vehicleID, Location l)  {
		straddleCarrierPane.setLocation(vehicleID, l);
	}

	
	public void missionVehicleWaitTimeChanged(String missionId, Time t)  {
		//missionPanel.missionVehicleWaitTimeChanged(missionId, t);
	}
	
	public void missionStateChanged(String missionId, String newState) {
		missionPanel.setMissionState(missionId, newState);
	}
	
	public void missionChanged(Load l) {
		missionPanel.setMission(l);
	}
	
	public void containerLocationChanged(String containerID, ContainerLocation cl) {
		containerPanel.setContainerLocation(containerID, cl);
	}
	
	public void addReservation(final Reservation r)  {
		reservationsPanel.addReservation(r);
	}
	
	public void removeReservation(Reservation r, Time unreservationTime) {
		reservationsPanel.removeReservation(r, unreservationTime);
	}
	
	public void removeContainer(String containerID) {
		containerPanel.removeContainer(containerID);
	}
	
	public void addSlots(final List<Slot> l) {
		for(Slot s : l) slotsPanel.addSlot(s);
	}
	
	public void slotContentChanged(Slot s)  {
		slotsPanel.contentChanged(s);
	}
	public JPanel getPanel() {
		return panel;
	}
	
	public void showVehicle(String id) {
		tabbedPane.setSelectedIndex(3);
		straddleCarrierPane.select(id);
	}

	public void showContainer(String id) {
		tabbedPane.setSelectedIndex(4);
		containerPanel.select(id);
	}

	public void showSlot(String id) {
		tabbedPane.setSelectedIndex(2);
		slotsPanel.select(id);
	}

	@Override
	public void setTextDisplay(TextDisplay display) {
		//Nothing to do here
	}
}
