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
package org.display.system;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.Date;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.conf.parameters.ReturnCodes;
import org.display.GraphicDisplay;
import org.system.Terminal;
import org.time.Time;
import org.time.TimeController;

public class GraphicTimeController extends TimeController implements Runnable {

	private JFrame jf;
	private JButton jbPlayPause, jbStop, jbNextStep;
	private JSpinner jsSecByStep;
	private JLabel jlStep, jlTime;
	private Date initDate;
	private boolean paused = false, firstPlay = true;
	private Thread t;
	private JCheckBox drawLaserHeads;
	private JCheckBox jcbSynchronized;

	public static final int WIDTH = 750;
	public static final int HEIGHT = 75;
	private GraphicTimeController THIS = this;
	private long sumOfTime = 0;
	private int nbIts = 0;

	public GraphicTimeController() {
		try {
			// TODO Put in TimeScheduler in a constant field ?
			initDate = DateFormat.getTimeInstance().parse("00:00:00");
		} catch (ParseException e1) {
			e1.printStackTrace();
		}
		t = new Thread(this);

		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				jf = new JFrame("Time Controler");
				jf.setFont(GraphicDisplay.font);
				BorderLayout mainlayout = new BorderLayout();
				jf.getContentPane().setLayout(mainlayout);

				GridLayout upLayout = new GridLayout(1, 9);
				JPanel upPanel = new JPanel(upLayout);
				upPanel.setFont(GraphicDisplay.font);
				JPanel downPanel = new JPanel(new GridLayout(1, 2));
				downPanel.setFont(GraphicDisplay.font);

				JLabel jl = new JLabel("Step : ");
				jl.setFont(GraphicDisplay.font);
				upPanel.add(jl);
				jlStep = new JLabel("" + getStep(), SwingConstants.CENTER);
				jlStep.setFont(GraphicDisplay.font);
				upPanel.add(jlStep);

				jl = new JLabel("Time : ");
				jl.setFont(GraphicDisplay.font);
				upPanel.add(jl);
				jlTime = new JLabel();
				jlTime.setFont(GraphicDisplay.font);
				updateTimeLabel();
				upPanel.add(jlTime);

				jbPlayPause = new JButton("Play");
				jbPlayPause.setFont(GraphicDisplay.font);
				jbNextStep = new JButton("Next Step");
				jbNextStep.setFont(GraphicDisplay.font);
				jbNextStep.setEnabled(true);

				jbStop = new JButton("Stop");
				jbStop.setFont(GraphicDisplay.font);
				jbStop.setEnabled(true);

				jbPlayPause.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						if (jbPlayPause.getText().equals("Play")) {
							jbPlayPause.setText("Pause");
							jbNextStep.setEnabled(false);
							paused = false;
							if (firstPlay) {
								firstPlay = false;
								jsSecByStep.setEnabled(false);
								jbStop.setEnabled(true);

							}
							t = new Thread(THIS);
							t.start();
						} else {
							jbPlayPause.setText("Play");
							jbNextStep.setEnabled(true);
							paused = true;
							try {
								t.join();
							} catch (InterruptedException e1) {
								e1.printStackTrace();
							}
						}
					}
				});
				upPanel.add(jbPlayPause);

				jbStop.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						paused = true;
						try {
							t.join();
						} catch (InterruptedException e1) {
							e1.printStackTrace();
						}
						jf.dispose();
						computeAndDrawTime();
						System.exit(ReturnCodes.EXIT_ON_SUCCESS.getCode());
					}

				});
				upPanel.add(jbStop);

				jbNextStep.addActionListener(new ActionListener() {

					@Override
					public void actionPerformed(ActionEvent e) {
						if (firstPlay) {
							firstPlay = false;
							jsSecByStep.setEnabled(false);
						}
						if (SwingUtilities.isEventDispatchThread())
							nextStep();
						else {
							SwingUtilities.invokeLater(new Runnable() {
								@Override
								public void run() {
									nextStep();
								}
							});
						}

					}
				});
				upPanel.add(jbNextStep);

				jl = new JLabel("Sec by step : ");
				jl.setFont(GraphicDisplay.font);
				upPanel.add(jl);
				jsSecByStep = new JSpinner(new SpinnerNumberModel(
						getSecByStep(), 0.01, 10, 0.01));
				jsSecByStep.setFont(GraphicDisplay.font);
				jsSecByStep.addChangeListener(new ChangeListener() {
					@Override
					public void stateChanged(ChangeEvent e) {
						setSecByStep(((Double) jsSecByStep.getValue())
								.floatValue());
					}
				});
				upPanel.add(jsSecByStep);

				drawLaserHeads = new JCheckBox("Laser Heads");
				drawLaserHeads.setFont(GraphicDisplay.font);
				drawLaserHeads.setSelected(false);

				Terminal.getInstance().showLaserHeads(
						drawLaserHeads.isSelected());
				drawLaserHeads.addActionListener(new ActionListener() {

					@Override
					public void actionPerformed(ActionEvent e) {
						Terminal.getInstance().showLaserHeads(
								drawLaserHeads.isSelected());

					}
				});
				downPanel.add(drawLaserHeads);

				jcbSynchronized = new JCheckBox("Synchro");
				jcbSynchronized.setFont(GraphicDisplay.font);
				jcbSynchronized.setSelected(false);
				SCHEDULER.setSynchronized(jcbSynchronized.isSelected());
				jcbSynchronized.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						SCHEDULER.setSynchronized(jcbSynchronized.isSelected());
					}
				});
				downPanel.add(jcbSynchronized);

				jf.getContentPane().add(upPanel, BorderLayout.CENTER);
				jf.getContentPane().add(downPanel, BorderLayout.SOUTH);

				jf.setSize(WIDTH, HEIGHT);
				jf.setLocation(Math.max(0, Toolkit.getDefaultToolkit()
						.getScreenSize().width - WIDTH), JTerminal.HEIGHT);
				jf.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
				// SubstanceLookAndFeel.setSkin(new BusinessBlackSteelSkin());
				// JFrame.setDefaultLookAndFeelDecorated(true);
				// SubstanceLookAndFeel.setSkin("org.pushingpixels.substance.api.skin.BusinessSkin");
				// jf.repaint();
				jf.setVisible(true);

			}
		});
	}

	public Date getInitDate() {
		return initDate;
	}

	public void nextStep() {
		// final TimeController SUPER = super.getInstance();
		// UPDATE COMPONENTS
		long startTime = System.currentTimeMillis();

		boolean disabled = false;
		if (jbNextStep.isEnabled()) {
			jbNextStep.setEnabled(false);
			jbPlayPause.setEnabled(false);
			disabled = true;
		}
		super.nextStep(!disabled);

		updateStepLabel();
		updateTimeLabel();

		if (disabled) {
			jbNextStep.setEnabled(true);
			jbPlayPause.setEnabled(true);
		}

		// Stats
		long endTime = System.currentTimeMillis();
		long gap = endTime - startTime;
		sumOfTime += gap;
		nbIts++;

	}

	public void run() {
		while (!paused) {
			nextStep();
		}
	}

	public void updateStepLabel() {
		jlStep.setText(getStep() + "");
	}

	public void updateTimeLabel() {
		Time t = getTime();
		jlTime.setText(t.getHours() + "h" + t.getMinutes() + "m"
				+ t.getSeconds() + "s");

	}

	private void computeAndDrawTime() {
		double moy = (double) sumOfTime / (double) nbIts;
		System.out.println("> " + nbIts + " iterations in " + sumOfTime
				+ " ms : moy = " + moy + " ms/it");

	}
}
