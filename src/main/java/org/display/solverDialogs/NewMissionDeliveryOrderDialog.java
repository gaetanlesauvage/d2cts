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
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.StringTokenizer;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JRadioButton;
import javax.swing.SwingUtilities;

import org.system.Terminal;
import org.system.container_stocking.Bay;
import org.system.container_stocking.ContainerLocation;
import org.vehicles.StraddleCarrierProblemListener;

public class NewMissionDeliveryOrderDialog implements ActionListener {
	private JDialog jd;

	public static int getChoice(ContainerLocation currentLocation,
			String currentExceptionMessage) {
		NewMissionDeliveryOrderDialog nmod = new NewMissionDeliveryOrderDialog(
				currentLocation, currentExceptionMessage);
		nmod.waitTillOkIsPress();

		if (nmod.jrbChangeLocation.isSelected())
			return StraddleCarrierProblemListener.ABORT_MISSION;
		else if (nmod.jrbGoToOrigin.isSelected())
			return StraddleCarrierProblemListener.GO_TO_PICKUP_ORIGIN_AND_SEE;
		else if (nmod.jrbGoToDest != null)
			return StraddleCarrierProblemListener.GO_TO_PICKUP_DESTINATION_AND_SEE;
		return -1;
	}

	private JButton jbOk;
	private JRadioButton jrbChangeLocation, jrbGoToOrigin, jrbGoToDest;

	private boolean isReady = false;

	public NewMissionDeliveryOrderDialog(
			final ContainerLocation currentLocation,
			final String currentExceptionMessage) {
		final ActionListener aListener = this;
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				jd = new JDialog();
				jd.setTitle("Mission order");
				ButtonGroup bg = new ButtonGroup();
				GridBagLayout gbl = new GridBagLayout();
				jd.getContentPane().setLayout(gbl);
				Container contentPane = jd.getContentPane();

				Terminal rt = Terminal.getInstance();
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
				c.gridwidth = GridBagConstraints.REMAINDER;
				jrbChangeLocation = new JRadioButton("Change delivery location");
				jrbChangeLocation.addActionListener(aListener);
				bg.add(jrbChangeLocation);
				contentPane.add(jrbChangeLocation, c);

				Bay l = rt.getBay(currentLocation.getLaneId());

				jrbGoToOrigin = new JRadioButton("Go to " + l.getOriginId());
				jrbGoToOrigin.addActionListener(aListener);
				contentPane.add(jrbGoToOrigin, c);
				bg.add(jrbGoToOrigin);
				if (!l.isDirected()) {
					jrbGoToDest = new JRadioButton("Go to "
							+ l.getDestinationId());
					jrbGoToDest.addActionListener(aListener);
					contentPane.add(jrbGoToDest, c);
					bg.add(jrbGoToDest);
				}

				jbOk = new JButton("Ok");
				jbOk.setEnabled(false);
				jbOk.addActionListener(aListener);
				jd.getContentPane().add(jbOk, c);

				jd.pack();
				jd.setLocation(
						(int) (Toolkit.getDefaultToolkit().getScreenSize().width / 2.0 - jd
								.getWidth() / 2.0),
						(int) (Toolkit.getDefaultToolkit().getScreenSize().height / 2.0 - jd
								.getHeight() / 2.0));
				jd.setVisible(true);

			}
		});
	}

	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == jbOk) {
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					jd.setVisible(false);
					isReady = true;
				}
			});
		}
		if (e.getSource() == jrbChangeLocation || e.getSource() == jrbGoToDest
				|| e.getSource() == jrbGoToOrigin) {
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					jbOk.setEnabled(true);
				}
			});
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
