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
package org.util.generators;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.Random;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JProgressBar;
import javax.swing.SwingUtilities;

import org.conf.parameters.ReturnCodes;
import org.display.GraphicDisplay;
import org.display.MainFrame;
import org.display.system.JTerminal;
import org.exceptions.SingletonException;
import org.positioning.LaserHead;
import org.positioning.LaserSystem;
import org.system.Terminal;
import org.time.Time;
import org.time.event.LaserHeadFailure;
import org.util.parsers.XMLTerminalComponentParser;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

public class LaserHeadRangeEventsGenerator {
	private XMLReader saxReader;

	public static final int WAITING_SLEEP_TIME = 20;
	private static final String[] loadingSteps = { "parsing terminal",
			"generating laser heads events" };

	private JProgressBar progress;
	private JDialog frame;
	private JFrame parentFrame;

	public LaserHeadRangeEventsGenerator(final String localHostName,
			final String xmlDeploymentFile, final String filename,
			final Time maxTime, final Time interval, final int max_variation,
			final double p_change, final double r_avg, final double p_inf_avg,
			final double p_sup_avg, final MainFrame parent)
			throws SAXException, IOException {
		System.err.println("Max time : " + Time.timeToStep(maxTime, 1f));
		System.err.println("Interval time : " + Time.timeToStep(interval, 1f));

		if (parent != null) {
			frame = new JDialog(parent.getFrame(), "Computing...", true);

			frame.setFont(GraphicDisplay.font);

			frame.setLayout(new BorderLayout());

			int nb = (int) (Time.timeToStep(maxTime, 1f) / Time.timeToStep(
					interval, 1f));

			progress = new JProgressBar(0, nb);
			progress.setString(loadingSteps[progress.getValue()]);
			progress.setFont(GraphicDisplay.font);
			progress.setStringPainted(true);
			frame.add(progress, BorderLayout.CENTER);
			frame.setSize(new Dimension(300, 70));

			parentFrame = parent.getFrame();
			frame.setLocation(
					parentFrame.getLocation().x
							+ (parentFrame.getSize().width / 2 - frame
									.getSize().width / 2),
					parentFrame.getLocation().y
							+ (parentFrame.getSize().height / 2 - frame
									.getSize().height / 2));

			frame.setAlwaysOnTop(true);

			frame.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
			frame.repaint();
			frame.enableInputMethods(false);
			frame.addWindowListener(new WindowAdapter() {
				@Override
				public void windowOpened(WindowEvent e) {
					new Thread() {
						public void run() {
							try {
								execute(localHostName, xmlDeploymentFile,
										filename, maxTime, interval,
										max_variation, p_change, r_avg,
										p_inf_avg, p_sup_avg);
							} catch (SAXException ex) {
								ex.printStackTrace();
							} catch (IOException ex) {
								ex.printStackTrace();
							}
						}
					}.start();

				}
			});
			frame.setVisible(true);
		} else
			execute(localHostName, xmlDeploymentFile, filename, maxTime,
					interval, max_variation, p_change, r_avg, p_inf_avg,
					p_sup_avg);

	}

	public LaserHeadRangeEventsGenerator(String localHostName,
			String xmlDeploymentFile, String filename, Time maxTime,
			Time interval, int max_variation, double p_change, double r_avg,
			double p_inf_avg, double p_sup_avg) throws SAXException,
			IOException {
		execute(localHostName, xmlDeploymentFile, filename, maxTime, interval,
				max_variation, p_change, r_avg, p_inf_avg, p_sup_avg);
	}

	private void execute(String localHostName, String xmlDeploymentFile,
			String filename, Time maxTime, Time interval, int max_variation,
			double p_change, double r_avg, double p_inf_avg, double p_sup_avg)
			throws SAXException, IOException {

		// First Step : Create Terminal
		Terminal.getInstance();
		JTerminal.getInstance();

		if (progress != null) {
			try {
				SwingUtilities.invokeAndWait(new Runnable() {
					@Override
					public void run() {
						progress.setValue(progress.getValue() + 1);
						progress.setString(loadingSteps[progress.getValue()]);
					}
				});
			} catch (InvocationTargetException e) {
				e.printStackTrace();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		generate(filename, maxTime, interval, max_variation, p_change, r_avg,
				p_inf_avg, p_sup_avg);

		// Stop simulation
		if (progress != null) {
			try {
				SwingUtilities.invokeAndWait(new Runnable() {
					@Override
					public void run() {
						progress.setValue(progress.getValue() + 1);
						progress.setString("closing simulation");
					}
				});
			} catch (InvocationTargetException e) {
				e.printStackTrace();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		destroy();

		if (progress != null) {
			try {
				SwingUtilities.invokeAndWait(new Runnable() {
					@Override
					public void run() {
						frame.setCursor(Cursor
								.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
						frame.setVisible(false);
						frame.dispose();
					}
				});
			} catch (InvocationTargetException e) {
				e.printStackTrace();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		} else
			System.exit(ReturnCodes.LASER_HEAD_RANGE_ERROR.getCode());
	}

	public void destroy() {
		new Thread() {
			public void run() {
				long t1 = System.currentTimeMillis();
				saxReader = null;

				long t2 = System.currentTimeMillis();
				System.out.println("Simulation closed in " + (t2 - t1) + "ms");

			}
		}.start();

	}

	/**
	 * Generate the laserHeadFailure events
	 * 
	 * @param filename
	 *            name of the generated XML file
	 * @param maxTime
	 *            Max occuring time of the events
	 * @param interval
	 *            Time interval between two events
	 * @param v_max
	 *            Max range variation between two successive ranges
	 * @param p_change
	 *            Probability of generating an event at each interval of time
	 * @param r_avg
	 *            Average range rate of a laser head
	 * @param p_inf_avg
	 *            Probability do create an event decreasing the range of a laser
	 *            head if the range is less than r_avg
	 * @param p_sup_avg
	 *            Probability do create an event increasing the range of a laser
	 *            head if the range is greater than r_avg
	 */
	public void generate(String filename, Time maxTime, Time interval,
			int v_max, double p_change, double r_avg, double p_inf_avg,
			double p_sup_avg) {
		// Third Step create and place the new containers
		File f = new File(filename);

		try {
			f.createNewFile();
			// int nb = (int)(maxTime.toStep()/interval.toStep());

			PrintWriter pw = new PrintWriter(f);
			pw.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<document>\n");

			LaserSystem ls = LaserSystem.getInstance();
			Collection<LaserHead> heads = ls.getHeads();
			String[] headsIDs = new String[heads.size()];
			int i = 0;
			for (LaserHead lh : heads) {
				headsIDs[i++] = lh.getId();
			}
			int[] p_rates = new int[headsIDs.length];
			// RATE INIT TO 100%
			for (i = 0; i < p_rates.length; i++)
				p_rates[i] = 100;
			Random random = Terminal.getInstance().getRandom();
			long current_step = 0;

			while (current_step < maxTime.toStep()) {
				for (i = 0; i < headsIDs.length; i++) {
					String headID = headsIDs[i];
					double p = random.nextDouble();
					if (p < p_change) {
						int o_range = p_rates[i];
						double p_dim = o_range < r_avg ? p_inf_avg : p_sup_avg;
						int dir = random.nextDouble() < p_dim ? -1 : 1;
						int n_range = o_range + dir * random.nextInt(v_max);
						if (n_range > 100)
							n_range = 100;
						else if (n_range < 0)
							n_range = 0;

						if (n_range != o_range) {
							// New event
							// Write in XML
							int l_time = random
									.nextInt((int) interval.toStep());
							long time = current_step + l_time;
							pw.append("<event time='" + new Time(time)
									+ "' type='"
									+ LaserHeadFailure.getEventType()
									+ "' laserHeadID='" + headID + "' range='"
									+ (n_range / 100.0) + "'/>\n");
							// Store power
							p_rates[i] = n_range;
						}
					}
				}
				// Next step
				current_step += interval.toStep();

				pw.flush();

				if (progress != null) {
					try {
						SwingUtilities.invokeAndWait(new Runnable() {
							@Override
							public void run() {
								progress.setValue(progress.getValue() + 1);
								if (progress.getValue() % 100 == 0)
									updateTitle();
							}
						});
					} catch (InvocationTargetException e) {
						e.printStackTrace();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}

				}
			}
			pw.append("</document>");
			pw.flush();
			pw.close();
		} catch (Exception e1) {
			e1.printStackTrace();
		}
	}

	public void runLaserSystem(String[] xmlLaserSystem) throws IOException,
			SAXException {
		LaserSystem.getInstance();
		XMLTerminalComponentParser terminalParser = new XMLTerminalComponentParser();
		saxReader.setContentHandler(terminalParser);
		for (String s : xmlLaserSystem) {
			System.out.println("Parsing xmlLaserSystem file " + s);
			saxReader.parse(new InputSource(this.getClass()
					.getResourceAsStream("/" + s)));
		}
	}

	private void updateTitle() {
		String newTitleString = "Computing";
		String toAdd = "";
		String title = frame.getTitle();
		if (title.contains("..."))
			toAdd = "";
		else
			for (int i = 0; i <= title.length() - newTitleString.length(); i++)
				toAdd += ".";
		frame.setTitle(newTitleString + toAdd);
	}

	public static void main(String[] args) throws SingletonException,
			URISyntaxException, SAXException, IOException, InterruptedException {
		if (args.length != 10)
			System.out
					.println("USAGE : java util.generators.LaserHeadRangeEventsGenerator localhostName deploymentFile events.xml maxTime interval max_variation p_change r_avg p_inf_avg p_sup_avg");
		else {
			Time maxTime = new Time(args[3]);
			Time interval = new Time(args[4]);
			int v_max = Integer.parseInt(args[5]);
			double p_change = Double.parseDouble(args[6]);
			double r_avg = Double.parseDouble(args[7]);
			double p_inf_avg = Double.parseDouble(args[8]);
			double p_sup_avg = Double.parseDouble(args[9]);
			new LaserHeadRangeEventsGenerator(args[0], args[1], args[2],
					maxTime, interval, v_max, p_change, r_avg, p_inf_avg,
					p_sup_avg);
		}
	}
}
