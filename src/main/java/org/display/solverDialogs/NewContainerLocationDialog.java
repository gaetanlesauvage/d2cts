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
package org.display.solverDialogs;

import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Collection;
import java.util.StringTokenizer;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;

import org.exceptions.container_stocking.delivery.MissionContainerDeliveryException;
import org.system.Road;
import org.system.Terminal;
import org.system.container_stocking.Bay;
import org.system.container_stocking.BayCrossroad;
import org.system.container_stocking.Block;
import org.system.container_stocking.ContainerAlignment;
import org.system.container_stocking.ContainerLocation;
import org.system.container_stocking.Slot;
import org.time.Time;
import org.time.TimeScheduler;
import org.util.Location;
import org.vehicles.DeliveryOrder;

public class NewContainerLocationDialog implements ActionListener, ItemListener {

	private JDialog jd;

	public static DeliveryOrder getSelectedContainerLocation(
			MissionContainerDeliveryException exception,
			String currentExceptionMessage) {
		NewContainerLocationDialog n = new NewContainerLocationDialog(
				exception, currentExceptionMessage);
		n.waitTillOkIsPress();

		if (n.jcbPave.isEnabled()) {
			int align = Slot.ALIGN_CENTER;
			if (n.jcbAlign.getSelectedItem().equals("origin"))
				align = Slot.ALIGN_ORIGIN;
			else if (n.jcbAlign.getSelectedItem().equals("destination"))
				align = Slot.ALIGN_DESTINATION;

			return new DeliveryOrder(exception.getMission(),
					new ContainerLocation(n.containerId,
							n.jcbPave.getSelectedItem() + "",
							n.jcbLane.getSelectedItem() + "",
							n.jcbSlot.getSelectedItem() + "",
							n.jcbLevel.getSelectedIndex(), align));
		} else {
			Time t = n.getSelectedTime();
			Bay l = Terminal.getInstance().getBay(
					exception.getMission().getDestination().getLaneId());
			BayCrossroad origin = (BayCrossroad) l.getOrigin();
			Road mainRoadOrigin = Terminal.getInstance().getRoad(
					origin.getMainRoad());
			double rOrigin = Location.getAccuratePourcent(origin.getLocation(),
					mainRoadOrigin);
			Location location = new Location(mainRoadOrigin, rOrigin);
			System.out.println("LOCATION = " + location);
			return new DeliveryOrder(exception.getMission(), location, t);
		}

	}

	private JComboBox<String> jcbPave, jcbLane, jcbSlot, jcbAlign;
	private JComboBox<Integer> jcbLevel;
	private JSpinner jsH, jsM, jsS;

	private JButton jbOk;
	private boolean isReady = false;
	private String containerId;

	public NewContainerLocationDialog(
			final MissionContainerDeliveryException exception,
			final String currentExceptionMessage) {

		final ItemListener iListener = this;
		final ActionListener aListener = this;
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {

				jd = new JDialog();
				jd.setTitle("Problem solver : Container Location");
				GridBagLayout gbl = new GridBagLayout();
				jd.getContentPane().setLayout(gbl);
				Container contentPane = jd.getContentPane();
				containerId = exception.getMission().getContainerId();

				ContainerLocation currentLocation = exception.getMission()
						.getDestination();

				GridBagConstraints c = new GridBagConstraints();

				String msg = currentExceptionMessage;
				StringTokenizer st = new StringTokenizer(msg, "\n");
				while (st.hasMoreTokens()) {
					// on utilise tout l'espace d'une cellule
					c.fill = GridBagConstraints.HORIZONTAL;
					c.gridwidth = GridBagConstraints.REMAINDER;
					c.weightx = 1.0;
					c.gridheight = 1;
					c.ipadx = 20;
					c.ipady = 20;
					JLabel title = new JLabel(st.nextToken(), JLabel.CENTER);
					contentPane.add(title, c);
				}

				// réinitialisation
				c.fill = GridBagConstraints.BOTH;
				c.gridheight = 1;
				c.ipady = 0;
				c.weightx = 0.0;
				c.weighty = 1.0;
				c.ipadx = 20;
				c.gridwidth = GridBagConstraints.RELATIVE;

				JRadioButton jrbNewDestination = new JRadioButton(
						"New location");
				JRadioButton jrbWait = new JRadioButton("Wait");
				ButtonGroup bg = new ButtonGroup();
				bg.add(jrbNewDestination);
				bg.add(jrbWait);

				jrbNewDestination.setSelected(true);
				JPanel pChoice = new JPanel(new GridLayout(1, 2));
				pChoice.add(jrbNewDestination);
				pChoice.add(jrbWait);

				c.fill = GridBagConstraints.HORIZONTAL;
				c.gridheight = 1;
				c.ipady = 20;
				c.weightx = 1.0;
				c.ipadx = 20;
				c.gridwidth = GridBagConstraints.REMAINDER;

				contentPane.add(pChoice, c);
				c.weighty = 0.0;
				c.gridwidth = GridBagConstraints.REMAINDER;

				// réinitialisation
				c.fill = GridBagConstraints.BOTH;
				c.gridheight = 1;
				c.ipady = 0;
				c.weightx = 0.0;
				c.weighty = 1.0;
				c.ipadx = 20;
				c.gridwidth = GridBagConstraints.RELATIVE;
				final JLabel jlPave = new JLabel("Pave :");
				contentPane.add(jlPave, c);
				// nouvelle réinitialisation
				c.weighty = 0.0;
				c.gridwidth = GridBagConstraints.REMAINDER;
				Terminal terminal = Terminal.getInstance();

				Block p = terminal.getBlock(currentLocation.getPaveId());
				Collection<Block> paves = terminal.getPaves(p.getType())
						.values();
				String[] pNames = new String[paves.size()];
				int i = 0;
				for (Block pTmp : paves)
					pNames[i++] = pTmp.getId();
				jcbPave = new JComboBox<String>(pNames);
				jcbPave.setSelectedItem(currentLocation.getPaveId());
				jcbPave.addItemListener(iListener);
				contentPane.add(jcbPave, c);

				/*
				 * c.weightx=0.0; c.weighty=1.0; c.ipadx = 20;
				 */
				c.gridwidth = GridBagConstraints.RELATIVE;
				final JLabel jlLane = new JLabel("Lane :");
				contentPane.add(jlLane, c);
				// nouvelle réinitialisation
				/* c.weighty=0.0; */
				c.gridwidth = GridBagConstraints.REMAINDER;
				jcbLane = new JComboBox<String>(terminal.getLaneNames(jcbPave
						.getSelectedItem() + ""));
				jcbLane.setSelectedItem(currentLocation.getLaneId());
				jcbLane.addItemListener(iListener);
				contentPane.add(jcbLane, c);

				/*
				 * c.weightx=0.0; c.weighty=1.0; c.ipadx = 20;
				 */
				c.gridwidth = GridBagConstraints.RELATIVE;
				final JLabel jlSlot = new JLabel("Slot :");
				contentPane.add(jlSlot, c);
				// nouvelle réinitialisation
				// c.weighty=0.0;
				c.gridwidth = GridBagConstraints.REMAINDER;
				jcbSlot = new JComboBox<String>(terminal.getSlotNames(jcbLane
						.getSelectedItem() + ""));
				jcbSlot.setSelectedItem(currentLocation.getSlotId());
				jcbSlot.addItemListener(iListener);
				jd.getContentPane().add(jcbSlot, c);

				/*
				 * c.weightx=0.0; c.weighty=1.0; c.ipadx = 20;
				 */
				c.gridwidth = GridBagConstraints.RELATIVE;
				final JLabel jlLevel = new JLabel("Level :");
				contentPane.add(jlLevel, c);
				// nouvelle réinitialisation
				// c.weighty=0.0;
				c.gridwidth = GridBagConstraints.REMAINDER;
				Integer[] levels = new Integer[Slot.SLOT_MAX_LEVEL];
				for (i = 0; i < Slot.SLOT_MAX_LEVEL; i++)
					levels[i] = i;
				jcbLevel = new JComboBox<Integer>(levels);
				jcbLevel.setSelectedItem(currentLocation.getLevel());
				jcbLevel.addItemListener(iListener);
				jd.getContentPane().add(jcbLevel, c);

				/*
				 * c.weightx=0.0; c.weighty=1.0; c.ipadx = 20;
				 */
				c.gridwidth = GridBagConstraints.RELATIVE;
				final JLabel jlAlign = new JLabel("Align :");
				contentPane.add(jlAlign, c);
				// nouvelle réinitialisation
				// c.weighty=0.0;
				c.gridwidth = GridBagConstraints.REMAINDER;
				String[] aligns = { "origin", "center", "destination" };
				jcbAlign = new JComboBox<String>(aligns);
				if (currentLocation.getAlign() == Slot.ALIGN_CENTER)
					jcbAlign.setSelectedIndex(1);
				else if (currentLocation.getAlign() == Slot.ALIGN_ORIGIN)
					jcbAlign.setSelectedIndex(0);
				else
					jcbAlign.setSelectedIndex(2);
				jd.getContentPane().add(jcbAlign, c);

				c.gridwidth = GridBagConstraints.RELATIVE;
				JPanel jpWait = new JPanel(new GridLayout(1, 6));
				final JLabel jlWait = new JLabel("Wait : ");
				contentPane.add(jlWait, c);

				c.gridwidth = GridBagConstraints.REMAINDER;

				jsH = new JSpinner(new SpinnerNumberModel(0, 0, 23, 1));
				jsM = new JSpinner(new SpinnerNumberModel(0, 0, 59, 1));
				jsS = new JSpinner(new SpinnerNumberModel(0.0, 0, 59.99,
						TimeScheduler.getInstance().getSecondsPerStep()));
				jpWait.add(jsH);
				jpWait.add(new JLabel("h"));
				jpWait.add(jsM);
				jpWait.add(new JLabel("m"));
				jpWait.add(jsS);
				jpWait.add(new JLabel("s"));
				jsH.setEnabled(false);
				jsM.setEnabled(false);
				jsS.setEnabled(false);
				jlWait.setEnabled(false);
				jd.getContentPane().add(jpWait, c);

				/*
				 * c.weightx=0.0; c.weighty=1.0; c.ipadx = 20;
				 */
				jbOk = new JButton("Ok");
				jbOk.addActionListener(aListener);
				jd.getContentPane().add(jbOk, c);

				jd.pack();
				jd.setLocation(
						(int) (Toolkit.getDefaultToolkit().getScreenSize().width / 2.0 - jd
								.getWidth() / 2.0),
						(int) (Toolkit.getDefaultToolkit().getScreenSize().height / 2.0 - jd
								.getHeight() / 2.0));
				jd.setVisible(true);

				jrbNewDestination.addItemListener(new ItemListener() {

					@Override
					public void itemStateChanged(ItemEvent e) {
						if (e.getStateChange() == ItemEvent.SELECTED) {
							// DESELECT WAIT
							jsH.setEnabled(false);
							jsM.setEnabled(false);
							jsS.setEnabled(false);
							jlWait.setEnabled(false);
							// SELECT NEW DESTINATION
							jcbPave.setEnabled(true);
							jcbLane.setEnabled(true);
							jcbSlot.setEnabled(true);
							jcbLevel.setEnabled(true);
							jcbAlign.setEnabled(true);
							jlPave.setEnabled(true);
							jlLane.setEnabled(true);
							jlSlot.setEnabled(true);
							jlLevel.setEnabled(true);
							jlAlign.setEnabled(true);
						}

					}
				});

				jrbWait.addItemListener(new ItemListener() {
					@Override
					public void itemStateChanged(ItemEvent e) {
						if (e.getStateChange() == ItemEvent.SELECTED) {
							// DESELECT NEW DESTINATION
							jcbPave.setEnabled(false);
							jcbLane.setEnabled(false);
							jcbSlot.setEnabled(false);
							jcbLevel.setEnabled(false);
							jcbAlign.setEnabled(false);
							jlPave.setEnabled(false);
							jlLane.setEnabled(false);
							jlSlot.setEnabled(false);
							jlLevel.setEnabled(false);
							jlAlign.setEnabled(false);

							// SELECT WAIT TIME
							jsH.setEnabled(true);
							jsM.setEnabled(true);
							jsS.setEnabled(true);
							jlWait.setEnabled(true);
						}

					}

				});

			}

		});

	}

	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == jbOk) {
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {

					jd.setVisible(false);
				}
			});
			isReady = true;
		}
	}

	private void fillAlign() {
		String[] aligns = new String[ContainerAlignment.values().length];
		int i = 0;
		for (ContainerAlignment ca : ContainerAlignment.values()) {
			aligns[i++] = ca.getStringValue();
		}
		jcbAlign.removeAllItems();
		for (String s : aligns)
			jcbAlign.addItem(s);
		jcbAlign.setSelectedIndex(0);
	}

	private void fillLane(String pave) {
		if (pave != null && !pave.equals("null")) {
			jcbLane.removeAllItems();
			String[] t = Terminal.getInstance().getLaneNames(pave);
			for (String s : t) {
				jcbLane.addItem(s);
			}
			jcbLane.setSelectedIndex(0);

		}
	}

	private void fillLevel(String slotName) {

		if (slotName != null && !slotName.equals("null")) {
			jcbLevel.removeAllItems();
			Slot s = Terminal.getInstance().getSlot(slotName);
			for (int i = 0; i < s.getMaxLevel(); i++)
				jcbLevel.addItem(i);
			jcbLevel.setSelectedIndex(0);

		}
	}

	private void fillSlot(String lane) {
		if (lane != null && !lane.equals("null")) {
			jcbSlot.removeAllItems();
			String[] t = Terminal.getInstance().getSlotNames(lane);
			for (String s : t) {
				jcbSlot.addItem(s);
			}
			jcbSlot.setSelectedIndex(0);

		}

	}

	public Time getSelectedTime() {
		return new Time(jsH.getValue() + ":" + jsM.getValue() + ":"
				+ jsS.getValue());
	}

	public boolean isReady() {
		return isReady;
	}

	@Override
	public void itemStateChanged(ItemEvent e) {

		if (e.getSource() == jcbPave) {
			fillLane(jcbPave.getSelectedItem() + "");
		} else if (e.getSource() == jcbLane) {
			fillSlot(jcbLane.getSelectedItem() + "");
		} else if (e.getSource() == jcbSlot) {
			fillLevel(jcbSlot.getSelectedItem() + "");
		} else if (e.getSource() == jcbLevel) {
			fillAlign();
		}
	}

	private void waitTillOkIsPress() {
		while (!isReady) {
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
}
