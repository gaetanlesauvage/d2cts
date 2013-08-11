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
import java.util.ArrayList;
import java.util.Random;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JProgressBar;
import javax.swing.SwingUtilities;

import org.display.GraphicDisplay;
import org.display.MainFrame;
import org.exceptions.ContainerDimensionException;
import org.exceptions.SingletonException;
import org.positioning.Coordinates;
import org.system.Terminal;
import org.system.container_stocking.Bay;
import org.system.container_stocking.BlockType;
import org.system.container_stocking.Container;
import org.system.container_stocking.ContainerAlignment;
import org.system.container_stocking.ContainerKind;
import org.system.container_stocking.ExchangeBay;
import org.system.container_stocking.Slot;
import org.util.BIC;
import org.util.ContainerBICGenerator;
import org.util.parsers.XMLTerminalComponentParser;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

public class ContainersFileGenerator {
	private XMLReader saxReader;

	private static String[] xmlTerminal;

	public static final int WAITING_SLEEP_TIME = 20;
	private static final String[] loadingSteps = { "parsing terminal",
			"generating containers" };

	private JProgressBar progress;
	private JDialog frame;
	private JFrame parentFrame;

	public ContainersFileGenerator(final String filename, final int nb20,
			final int nb40, final int nb45, final String[] xmlTerminal,
			final MainFrame parent) throws SAXException, IOException {
		if (parent != null) {
			frame = new JDialog(parent.getFrame(), "Computing...", true);

			frame.setFont(GraphicDisplay.font);

			frame.setLayout(new BorderLayout());
			progress = new JProgressBar(0, nb20 + nb40 + nb45 + 3);
			progress.setString(loadingSteps[progress.getValue()]);
			progress.setFont(GraphicDisplay.font);
			progress.setStringPainted(true);
			frame.add(progress, BorderLayout.CENTER);
			frame.setSize(new Dimension(300, 70));

			ContainersFileGenerator.xmlTerminal = xmlTerminal;

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
								execute(filename, nb20, nb40, nb45);
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
			execute(filename, nb20, nb40, nb45);

	}

	public ContainersFileGenerator(String filename, int nb20, int nb40,
			int nb45, String[] xmlDeploymentFile) throws SAXException,
			IOException {
		ContainersFileGenerator.xmlTerminal = xmlDeploymentFile;
		execute(filename, nb20, nb40, nb45);
	}

	private void execute(String filename, int nb20, int nb40, int nb45)
			throws SAXException, IOException {

		// Second Step : Create Terminal
		parseXMLTerminal();

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
		generate(filename, nb20, nb40, nb45);

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
		}
	}

	public void destroy() {
		new Thread() {
			public void run() {
				long t1 = System.currentTimeMillis();
				xmlTerminal = null;
				saxReader = null;

				long t2 = System.currentTimeMillis();
				System.out.println("Simulation closed in " + (t2 - t1) + "ms");
			}
		}.start();

	}

	public void generate(String filename, int nb20, int nb40, int nb45) {
		// Third Step create and place the new containers
		File f = new File(filename);

		try {
			f.createNewFile();

			int nb = nb45 + nb40 + nb20;
			ContainerBICGenerator bicGen = new ContainerBICGenerator(nb);
			System.out.println("BicGen = " + bicGen);
			PrintWriter pw = new PrintWriter(f);
			pw.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<document>\n");
			Terminal rt = Terminal.getInstance();

			String[] paves = new String[rt.getPaveCount()];

			paves = rt.getPaveNames();
			int sum = 0;
			for (char c : "fixedSeed".toCharArray())
				sum += (int) c;
			Random r = new Random(sum);
			ArrayList<Slot> slots = new ArrayList<Slot>();
			for (String s : paves) {
				if (rt.getBlock(s).getType() == BlockType.YARD) {
					String[] t = rt.getLaneNames(s);
					for (String s2 : t) {
						Bay l = rt.getBay(s2);
						if (!(l instanceof ExchangeBay)) {
							for (Slot s3 : rt.getSlots(l.getId())) {
								slots.add(s3);
							}
						}
					}
				}
			}

			while (bicGen.size() > 0) {
				BIC bic = bicGen.giveMeBic();
				String containerId = bic.toString();
				System.out
						.println("---- Placing " + bic.toString() + " ! ----");
				Container c;
				try {
					// Which TEU ?
					int type = 2;
					if (nb45 == 0) {
						type = 0;
						if (nb40 == 0) {
							type = 1;
							nb20--;
						} else
							nb40--;
					} else
						nb45--;

					if (type == 0)
						nb40--;

					double teuValue = ContainerKind.getTeu(type);
					c = new Container(containerId, teuValue);

					boolean ok = false;
					while (!ok) {

						StringBuilder sb = new StringBuilder();
						sb.append("<container id=\"");
						Slot slot = slots.get(r.nextInt(slots.size()));
						String pave = slot.getPaveId();
						// Pave p = rt.getPave(pave);

						String lane = slot.getLocation().getRoad().getId();

						int level = r.nextInt(slot.getMaxLevel());
						int align = r.nextInt(3) - 1;
						if (type == Container.TYPE_40_Feet
								&& slot.getTEU() == 2.0) {
							align = ContainerAlignment.center.getValue();
						} else if (slot.getTEU() == 1.0)
							align = ContainerAlignment.center.getValue();
						else if (slot.getTEU() == 2.25
								&& type == Container.TYPE_45_Feet)
							align = ContainerAlignment.center.getValue();
						if (slot.canAddContainer(c, level, align)) {

							try {
								Coordinates coords = slot.stockContainer(c,
										level, align);
								if (coords != null) {
									sb.append(bic.toString() + "\" teu=\""
											+ teuValue + "\">\n");
									sb.append("\t<containerLocation pave=\""
											+ pave
											+ "\" lane=\""
											+ lane
											+ "\" slot=\""
											+ slot.getId()
											+ "\" level=\""
											+ level
											+ "\" align=\""
											+ ContainerAlignment
													.getStringValue(align)
											+ "\"/>\n");
									sb.append("</container>\n");
									pw.append(sb.toString());
									pw.flush();
									ok = true;
									System.out.println("Container "
											+ containerId + " added ! "
											+ bicGen.size()
											+ " remaining containers to place");

								} else {
									System.out
											.println("No exception but coordinates null for "
													+ containerId);
								}
							} catch (Exception e) {
								System.out.println("Exception while adding "
										+ c.getId());
							}
						}
					}
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
				} catch (ContainerDimensionException e) {
					e.printStackTrace();
				}

			}
			pw.append("</document>");
			pw.flush();
			pw.close();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}

	private void parseXMLTerminal() throws SAXException, IOException {
		saxReader = XMLReaderFactory
				.createXMLReader("com.sun.org.apache.xerces.internal.parsers.SAXParser");

		saxReader.setContentHandler(new XMLTerminalComponentParser());
		for (final String s : xmlTerminal) {
			saxReader.parse(s);
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
		if (args.length != 6)
			System.out
					.println("USAGE : java util.generators.ContainersFileGenerator newContainerFileName.xml nbContainer20Feet nbContainer40Feet nbContainer45Feet deploymentFiles...");
		else {
			int nb20 = Integer.parseInt(args[1]);
			int nb40 = Integer.parseInt(args[2]);
			int nb45 = Integer.parseInt(args[3]);
			String[] terminalFiles = new String[args.length - 4];
			System.arraycopy(args, 4, terminalFiles, 0, args.length - 4);
			new ContainersFileGenerator(args[0], nb20, nb40, nb45,
					terminalFiles);
		}
	}
}
