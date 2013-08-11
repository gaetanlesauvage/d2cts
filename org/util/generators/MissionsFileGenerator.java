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
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JProgressBar;
import javax.swing.SwingUtilities;

import org.apache.log4j.Logger;
import org.conf.parameters.ReturnCodes;
import org.display.GraphicDisplay;
import org.display.MainFrame;
import org.exceptions.ContainerDimensionException;
import org.exceptions.EmptyLevelException;
import org.exceptions.NoPathFoundException;
import org.missions.Mission;
import org.missions.MissionKinds;
import org.routing.path.Path;
import org.system.Terminal;
import org.system.container_stocking.Bay;
import org.system.container_stocking.Block;
import org.system.container_stocking.BlockType;
import org.system.container_stocking.Container;
import org.system.container_stocking.ContainerAlignment;
import org.system.container_stocking.ContainerKind;
import org.system.container_stocking.ContainerLocation;
import org.system.container_stocking.Level;
import org.system.container_stocking.Slot;
import org.time.Time;
import org.time.TimeScheduler;
import org.time.TimeWindow;
import org.util.ContainerBICGenerator;
import org.util.Location;
import org.util.generators.parsers.ShipGenerationData;
import org.util.generators.parsers.StockGenerationData;
import org.util.generators.parsers.TrainGenerationData;
import org.util.generators.parsers.TrucksGenerationData;
import org.util.generators.parsers.XMLMissionGeneratorParser;
import org.util.parsers.XMLTerminalComponentParser;
import org.vehicles.StraddleCarrier;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

public class MissionsFileGenerator {
	private static final Logger log = Logger
			.getLogger(MissionsFileGenerator.class);

	private static PrintWriter pw;
	private XMLReader saxReader;

	public static final int WAITING_SLEEP_TIME = 20;
	private static StraddleCarrier rsc;
	private ContainerBICGenerator bicGenerator;
	private Random r;
	private Map<String, SlotReservations> slotReservations;
	private Map<String, List<ShipQuayReservation>> quaysReservations;
	private Map<String, String> containersOUT;
	private Time handlingTimeFromTruck;
	private Time handlingTimeFromGround;
	private long seed = -1;
	private static int trainsCount = 1;

	private JProgressBar progress;
	private JDialog frame;
	private JFrame parentFrame;

	private static final String[] loadingSteps = { "parsing terminal",
			"initializing slots reservations", "initialazing BIC generator",
			"generating train missions", "generating trucks missions",
			"generating ships missions", "generating stock missions",
			"closing simulation" };

	public MissionsFileGenerator(final String localHostName,
			final String configFile, final MainFrame parent)
			throws SAXException, IOException, NoPathFoundException,
			ContainerDimensionException, EmptyLevelException {

		if (parent != null) {
			frame = new JDialog(parent.getFrame(), "Computing...", true);

			frame.setFont(GraphicDisplay.font);

			frame.setLayout(new BorderLayout());
			progress = new JProgressBar(0, loadingSteps.length);
			progress.setString(loadingSteps[0]);
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
								try {
									execute(localHostName, configFile);
								} catch (NoPathFoundException e) {
									e.printStackTrace();
									log.error(e.getMessage(), e);
								} catch (ContainerDimensionException e) {
									e.printStackTrace();
									log.error(e.getMessage(), e);
								} catch (EmptyLevelException e) {
									e.printStackTrace();
									log.error(e.getMessage(), e);
								}
							} catch (SAXException ex) {
								ex.printStackTrace();
								log.error(ex.getMessage(), ex);
							} catch (IOException ex) {
								ex.printStackTrace();
								log.error(ex.getMessage(), ex);
							}
						}
					}.start();

				}
			});
			frame.setVisible(true);
		} else
			execute(localHostName, configFile);
	}

	private void execute(final String localHostName, final String configFile)
			throws SAXException, IOException, NoPathFoundException,
			ContainerDimensionException, EmptyLevelException {
		pw = new PrintWriter(new File("reservations.dat"));
		// First Step : parse the generator configuration
		XMLReader saxNetworkConfigReader = XMLReaderFactory
				.createXMLReader("com.sun.org.apache.xerces.internal.parsers.SAXParser");

		XMLMissionGeneratorParser parser = new XMLMissionGeneratorParser();
		saxNetworkConfigReader.setContentHandler(parser);
		saxNetworkConfigReader.parse(configFile);

		// Second Step : parse terminal configuration
		String terminalFile = parser.getTerminalFile();
		String vehiclesFile = parser.getVehiclesFile();
		String containersFile = parser.getContainersFile();
		if (seed == -1)
			seed = parser.getSeed();
		// Third Step : Create Terminal
		parseNetworkConfiguration(terminalFile, vehiclesFile, containersFile);
		if (progress != null) {
			try {
				SwingUtilities.invokeAndWait(new Runnable() {
					@Override
					public void run() {
						progress.setValue(progress.getValue() + 1);
						progress.setString(loadingSteps[1]);
					}
				});
			} catch (InvocationTargetException e) {
				e.printStackTrace();
				log.error(e.getMessage(), e);
			} catch (InterruptedException e) {
				e.printStackTrace();
				log.error(e.getMessage(), e);
			}
		}

		// Forth Step : data initialization

		Map<BlockType, List<String>> sortedContainersMap = new HashMap<>(
				BlockType.values().length);
		for (String s : Terminal.getInstance().getContainerNames()) {
			Container c = Terminal.getInstance().getContainer(s);

			String paveId = c.getContainerLocation().getPaveId();
			Block pave = Terminal.getInstance().getBlock(paveId);
			BlockType pt = pave.getType();
			List<String> list;
			if (sortedContainersMap.containsKey(pt))
				list = sortedContainersMap.get(pt);
			else
				list = new ArrayList<String>();

			list.add(c.getId());
			sortedContainersMap.put(pt, list);
		}
		System.out.print("Initialization ... ");
		if (parser.getSeed() != null)
			r = new Random(parser.getSeed());
		else {
			new Exception(
					"Seed not supply ! Random will be created without any fixed seed.")
					.printStackTrace();
			r = new Random();
		}

		initializeSlotReservations();

		if (progress != null) {
			Runnable rr = new Runnable() {
				public void run() {
					progress.setValue(progress.getValue() + 1);
					progress.setString(loadingSteps[2]);
				}
			};
			try {
				SwingUtilities.invokeAndWait(rr);
			} catch (InvocationTargetException e) {
				e.printStackTrace();
				log.error(e.getMessage(), e);
			} catch (InterruptedException e) {
				e.printStackTrace();
				log.error(e.getMessage(), e);
			}

		}

		quaysReservations = new HashMap<>();
		containersOUT = new HashMap<String, String>();
		initializeBICGenerator();
		if (progress != null) {
			try {
				SwingUtilities.invokeAndWait(new Runnable() {
					@Override
					public void run() {
						progress.setValue(progress.getValue() + 1);
						progress.setString(loadingSteps[3]);
					}
				});
			} catch (InvocationTargetException e) {
				e.printStackTrace();
				log.error(e.getMessage(), e);
			} catch (InterruptedException e) {
				e.printStackTrace();
				log.error(e.getMessage(), e);

			}
		}
		rsc = Terminal
				.getInstance()
				.getStraddleCarriers()
				.get(r.nextInt(Terminal.getInstance()
						.getStraddleCarriersCount()));
		// rsc = Terminal.getInstance().getStraddleCarriers().get(0);
		if (rsc.getModel() == null) {
			System.err.println("Model is null !");
			System.exit(ReturnCodes.EXIT_ON_NULL_MODEL_ERROR.getCode());
		} else if (rsc.getModel().getSpeedCharacteristics() == null) {
			System.err.println("Model.speedCharacteristics is null !");
			System.exit(ReturnCodes.EXIT_ON_NULL_SPEED_CHARACTERITICS.getCode());
		}
		handlingTimeFromGround = new Time(rsc.getModel()
				.getSpeedCharacteristics()
				.getContainerHandlingTimeFromGroundMAX());
		handlingTimeFromTruck = new Time(rsc.getModel()
				.getSpeedCharacteristics()
				.getContainerHandlingTimeFromTruckMAX());
		System.out.println("done.");

		// Fifth Step : TRAINS
		int i = 1;
		System.out.println("Generating Train Missions ... ");
		for (TrainGenerationData trainData : parser.getTrainsData()) {
			Time maxTimeInTime = new Time(trainData.getMaxTime());
			Time minTimeInTime = new Time(trainData.getMinTime());

			generateTrainMissions(trainData.getFile(), sortedContainersMap,
					minTimeInTime, maxTimeInTime, trainData.getMarginRate(),
					trainData.getFullRate(), trainData.getAfterUnload(),
					trainData.getAfterReload(), "train" + i);
			i++;
		}
		System.out.println("Generating Train Missions DONE !");
		if (progress != null) {
			try {
				SwingUtilities.invokeAndWait(new Runnable() {
					@Override
					public void run() {
						progress.setValue(progress.getValue() + 1);
						progress.setString(loadingSteps[4]);
					}
				});
			} catch (InvocationTargetException e) {
				e.printStackTrace();
				log.error(e.getMessage(), e);
			} catch (InterruptedException e) {
				e.printStackTrace();
				log.error(e.getMessage(), e);
			}
		}
		// Sixth Step : TRUCKS
		System.out.println("Generating Truck Missions ... ");
		for (TrucksGenerationData truckData : parser.getTrucksData()) {
			Time minTimeInTime = new Time(truckData.getMinTime());
			Time maxTimeInTime = new Time(truckData.getMaxTime());
			Time avgTruckTimeBeforeLeavingInTime = new Time(
					truckData.getAvgTruckTimeBeforeLeaving());
			generateTrucksMissions(truckData.getOutputFile(),
					truckData.getNb(), truckData.getRateComeEmpty(),
					truckData.getRateLeaveEmpty(), sortedContainersMap,
					minTimeInTime, maxTimeInTime,
					avgTruckTimeBeforeLeavingInTime, truckData.getGroupID());
		}
		System.out.println("Generating Truck Missions DONE !");
		if (progress != null) {
			try {
				SwingUtilities.invokeAndWait(new Runnable() {
					@Override
					public void run() {
						progress.setValue(progress.getValue() + 1);
						progress.setString(loadingSteps[5]);
					}
				});
			} catch (InvocationTargetException e) {
				e.printStackTrace();
				log.error(e.getMessage(), e);
			} catch (InterruptedException e) {
				e.printStackTrace();
				log.error(e.getMessage(), e);
			}
		}

		// Seventh Step : SHIPS
		System.out.println("Generating Ship Missions ... ");
		for (ShipGenerationData shipData : parser.getShipsData()) {
			Time maxArrivalTime = new Time(shipData.getMaxArrivalTime());
			Time maxDepartureTime = new Time(shipData.getMaxDepartureTime());
			Time minimalBerthTimeLength = new Time(
					shipData.getMinBerthTimeLength());
			Time timePerContainerOperation = new Time(
					shipData.getTimePerContainerOperation());

			generateShipsMissions(shipData.getOutputFile(),
					shipData.getMinTeuCapacity(), shipData.getMaxTeuCapacity(),
					shipData.getFullRate(), shipData.getTwentyFeetRate(),
					shipData.getFortyFeetRate(), shipData.getCapacityFactor(),
					maxArrivalTime, minimalBerthTimeLength, maxDepartureTime,
					timePerContainerOperation, shipData.getAfterUnload(),
					shipData.getAfterReload(), shipData.getMarginRate());
		}
		System.out.println("Generating Ship Missions DONE !");
		if (progress != null) {
			try {
				SwingUtilities.invokeAndWait(new Runnable() {
					@Override
					public void run() {
						progress.setValue(progress.getValue() + 1);
						progress.setString(loadingSteps[6]);
					}
				});
			} catch (InvocationTargetException e) {
				e.printStackTrace();
				log.error(e.getMessage(), e);
			} catch (InterruptedException e) {
				e.printStackTrace();
				log.error(e.getMessage(), e);
			}
		}

		// Eighth Step : STOCK
		System.out.println("Generating Stock Missions ... ");
		for (StockGenerationData stockData : parser.getStockData()) {
			generateStocksMissions(stockData.getOutputFile(),
					stockData.getNb(), new Time(stockData.getMinTime()),
					new Time(stockData.getMaxTime()),
					new Time(stockData.getMarginTime()),
					stockData.getGroupID(), sortedContainersMap);
		}
		System.out.println("Generating Stock Missions DONE !");
		if (progress != null) {
			try {
				SwingUtilities.invokeAndWait(new Runnable() {
					@Override
					public void run() {
						progress.setValue(progress.getValue() + 1);
						progress.setString(loadingSteps[7]);
					}
				});
			} catch (InvocationTargetException e) {
				e.printStackTrace();
				log.error(e.getMessage(), e);
			} catch (InterruptedException e) {
				e.printStackTrace();
				log.error(e.getMessage(), e);
			}
		}
		pw.close();

		if (parentFrame != null) {
			destroy();
		} else {
			destroyUnthreaded();
		}

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
				log.error(e.getMessage(), e);
			} catch (InterruptedException e) {
				e.printStackTrace();
				log.error(e.getMessage(), e);
			}
		}
	}

	public MissionsFileGenerator(final String localHostName,
			final String configFile, final long seed) throws SAXException,
			IOException, NoPathFoundException, ContainerDimensionException,
			EmptyLevelException {
		this.seed = seed;
		execute(localHostName, configFile);
	}

	public MissionsFileGenerator(final String localHostName,
			final String configFile) throws SAXException, IOException,
			NoPathFoundException, ContainerDimensionException,
			EmptyLevelException {
		execute(localHostName, configFile);
	}

	public void destroyUnthreaded() {
		long t1 = System.currentTimeMillis();
		MissionsFileGenerator.rsc = null;
		MissionsFileGenerator.pw = null;
		saxReader = null;
		bicGenerator = null;
		containersOUT = null;
		handlingTimeFromGround = null;
		handlingTimeFromTruck = null;
		quaysReservations = null;
		r = null;
		slotReservations = null;

		long t2 = System.currentTimeMillis();
		System.out.println("Simulation closed in " + (t2 - t1) + "ms");
	}

	public void destroy() {
		new Thread() {
			public void run() {
				long t1 = System.currentTimeMillis();
				MissionsFileGenerator.rsc = null;
				MissionsFileGenerator.pw = null;
				saxReader = null;
				bicGenerator = null;
				containersOUT = null;
				handlingTimeFromGround = null;
				handlingTimeFromTruck = null;
				quaysReservations = null;
				r = null;
				slotReservations = null;

				long t2 = System.currentTimeMillis();
				System.out.println("Simulation closed in " + (t2 - t1) + "ms");

			}
		}.start();

	}

	private void initializeBICGenerator() {
		bicGenerator = new ContainerBICGenerator(0, Terminal.getInstance()
				.getContainerNames());
	}

	private void initializeSlotReservations() {
		slotReservations = new HashMap<String, SlotReservations>();

		for (List<Slot> slots : Terminal.getInstance().getSlots()) {
			for (Slot s : slots) {
				SlotReservations srs = slotReservations.get(s.getId());
				if (srs == null)
					srs = new SlotReservations(s);
				slotReservations.put(s.getId(), srs);
				for (Level l : s.getLevels()) {
					for (String contID : l.getContainersID()) {
						Container container = Terminal.getInstance()
								.getContainer(contID);
						SlotReservation sr = new SlotReservation(Terminal
								.getInstance().getContainer(contID), s,
								new TimeWindow(new Time(0), new Time(
										Time.MAXTIME)), container
										.getContainerLocation().getAlign(), l);
						addReservation(sr);
						// srs.addReservation(sr);
					}
				}
			}
		}
		// Modify the reservations where containers are already stocked
		// Add reservations for the destination slots of the missions
		for (Mission m : Terminal.getInstance().getMissions()) {
			Container c = m.getContainer();
			Slot origin = Terminal.getInstance().getSlot(
					c.getContainerLocation().getSlotId());
			// TODO check if we can take the avg between TW.min and TW.max...
			updateReservationMaxTime(c, origin, m.getPickupTimeWindow()
					.getMax());

			Slot destination = Terminal.getInstance().getSlot(
					m.getDestination().getSlotId());
			// SlotReservations destinationsResas =
			// slotReservations.get(destination.getId());
			SlotReservation destResa = new SlotReservation(c, destination,
					new TimeWindow(m.getDeliveryTimeWindow().getMin(),
							new Time(Time.MAXTIME)), m.getDestination()
							.getAlign(), destination.getLevel(m
							.getDestination().getLevel()));

			addReservation(destResa);
		}
	}

	private void generateStocksMissions(String xmlFile, final int nb,
			Time minTime, Time maxTime, Time marginTime, String groupID,
			Map<BlockType, List<String>> sortedContainersMap) {
		StringBuilder sb = new StringBuilder();

		ArrayList<String> possibleContIDs = new ArrayList<String>(Terminal
				.getInstance().getContainerNames());
		ArrayList<Block> stockPaves = new ArrayList<Block>(Terminal
				.getInstance().getPaves(BlockType.YARD).values());
		if (progress != null) {
			try {
				SwingUtilities.invokeAndWait(new Runnable() {

					@Override
					public void run() {
						progress.setMaximum(progress.getMaximum() + nb);
					}
				});
			} catch (InvocationTargetException e) {
				e.printStackTrace();
				log.error(e.getMessage(), e);
			} catch (InterruptedException e) {
				e.printStackTrace();
				log.error(e.getMessage(), e);
			}
		}
		int i = 0;
		while (i < nb) {
			String contID = possibleContIDs.remove(r.nextInt(possibleContIDs
					.size()));
			if (!containersOUT.containsKey(contID)) {
				Container container = Terminal.getInstance().getContainer(
						contID);
				Slot originSlot = Terminal.getInstance().getSlot(
						container.getContainerLocation().getSlotId());
				try {
					if (Terminal.getInstance().getBlock(originSlot.getPaveId())
							.getType() == BlockType.YARD) {
						Time P_min = new Time(minTime, new Time(
								r.nextInt((int) (maxTime.toStep() - minTime
										.toStep()))));
						Path pDepotToPickUp;

						pDepotToPickUp = rsc.getRouting().getShortestPath(
								new Location(rsc.getSlot(), rsc.getSlot()
										.getCenterLocation(), false),
								originSlot.getLocation());
						Time travelTime = new Time(pDepotToPickUp.getCost());

						travelTime = new Time(travelTime, new Time(
								(long) (r.nextDouble() * marginTime.toStep())));
						Time P_max = new Time(
								P_min,
								new Time(
										travelTime,
										rsc.getMaxContainerHandlingTime((Bay) originSlot
												.getLocation().getRoad())/*
																		 * container
																		 * .
																		 * getHandlingTime
																		 * ())
																		 */));

						// Check if container will be on its slot at the pickup
						// time
						SlotReservations originReservations = slotReservations
								.get(originSlot.getId());
						if (originReservations == null)
							originReservations = new SlotReservations(
									originSlot);
						boolean free = originReservations.isContainerFreeAt(
								container, new TimeWindow(P_min, P_max));

						if (free) {
							Block pDestination = stockPaves.get(r
									.nextInt(stockPaves.size()));
							Bay lDestination = pDestination.getLanes().get(
									r.nextInt(pDestination.getLanes().size()));
							Slot destinationSlot = Terminal
									.getInstance()
									.getSlots(lDestination.getId())
									.get(r.nextInt(Terminal.getInstance()
											.getSlots(lDestination.getId())
											.size()));

							// Compute Delivery TW
							Path pPickUpToDepot = rsc.getRouting()
									.getShortestPath(originSlot.getLocation(),
											destinationSlot.getLocation());
							travelTime = new Time(pPickUpToDepot.getCost());
							travelTime = new Time(travelTime, new Time(
									(long) (r.nextDouble() * marginTime
											.toStep())));
							Time handlingTime = rsc
									.getMaxContainerHandlingTime(lDestination);
							Time D_min = new Time(P_min, new Time(travelTime,
									handlingTime));
							Time D_max = new Time(P_max, new Time(travelTime,
									handlingTime));

							SlotReservations reservations = slotReservations
									.get(destinationSlot.getId());
							if (reservations == null) {
								reservations = new SlotReservations(
										destinationSlot);
							}
							SlotReservation reservation = reservations
									.giveFreeReservation(container,
											new TimeWindow(D_min, new Time(
													Time.MAXTIME)));
							if (reservation != null) {
								// Mission ok !
								addReservation(reservation);
								updateReservationMaxTime(container, originSlot,
										P_max);

								Time knownTime = new Time(minTime, new Time(
										(r.nextInt((int) (Math.max(
												1,
												P_min.toStep()
														- minTime.toStep()))))));
								sb.append("<event time='" + knownTime
										+ "' type='newMission'>\n");

								sb.append("\t<mission id='stockMission_"
										+ groupID + "[" + (i++)
										+ "]' container='" + contID
										+ "' kind='"
										+ MissionKinds.STAY.getIntValue()
										+ "'>\n");
								sb.append("\t\t<timewindow start='" + P_min
										+ "' end='" + P_max + "'/>\n");
								sb.append("\t\t<timewindow start='" + D_min
										+ "' end='" + D_max + "'/>\n");
								sb.append("\t\t"
										+ reservation.getContainerLocation()
												.toXML() + "\n");
								sb.append("\t</mission>\n");

								sb.append("</event>\n");
								incrementProgressBar();

							}
						}
					}
				} catch (NoPathFoundException e) {
					// e.printStackTrace();
					System.out.println("Warning : " + e.getMessage());
					log.error(e.getMessage(), e);
				}
			}
		}
		System.out.println(nb + " Stock Missions Generated !");
		File stockFile = new File(xmlFile);
		try {
			stockFile.createNewFile();
			PrintWriter pw = new PrintWriter(stockFile);
			pw.append("<document>\n");
			pw.append(sb.toString());
			pw.append("</document>\n");
			pw.flush();
			pw.close();
		} catch (IOException e) {
			e.printStackTrace();
			log.error(e.getMessage(), e);
		}
	}

	/**
	 * Un navire arrive sur un quai (Asie ou Osaka) à une place
	 * (0<=berthFrom<berthTo<=1.0) en amennant des conteneurs (0 <= n <= capMax
	 * où capMax est la capacité max du navire et dépend notamment de la
	 * longueur de ce dernier) Tout ou partie de sa cargaison doit être
	 * déchargée Le navire repart avec une partie de sa cargaison d'arrivée (0<=
	 * n <= n2) plus d'autres conteneurs (0 <= n3 <= (maxCap - n2)) : n2 + n3 <
	 * capMax.
	 * 
	 * Capacite en TEU / CapacityFactor = Longueur du bateau !
	 * 
	 * @throws FileNotFoundException
	 * @throws NoPathFoundException
	 * @throws ContainerDimensionException
	 */
	private void generateShipsMissions(String xmlFile, int capaciteMin,
			int capaciteMax, double taux_remplissage, double rep20Pieds,
			double rep40Pieds, double capacityFactor, Time maxArrivalTime,
			Time minimalBerthTimeLength, Time maxDepartureTime,
			Time timePerContainerOperation,
			double taux_remplissage_apres_dechargement,
			double taux_remplissage_apres_chargement, double marginRate)
			throws FileNotFoundException, ContainerDimensionException {
		if (maxDepartureTime.toStep() <= maxArrivalTime.toStep()) {
			System.out
					.println("Departure time must be greater than the arrival time. Verify your parameters!");
		}
		if ((maxDepartureTime.toStep() - minimalBerthTimeLength.toStep()) < maxArrivalTime
				.toStep()) {
			System.out
					.println("Not enough time for unloading and loading the ship. Verify your parameters!");
		}

		StringBuffer sb = new StringBuffer();
		// HYPOTHESIS:
		// -> There is no ship on berths from 0:0:0s to the end of simulation!
		// -> There is no containers on quays from 0:0:0s to the end of
		// simulation!

		// QuayID ,
		boolean ok;

		// 1ere étape : générer le bateau :
		// -> Taille
		// -> Contenu
		// -> Emplacement
		// -> Dates
		int capacity = capaciteMin + r.nextInt((capaciteMax + 1) - capaciteMin);

		System.out.println("capaciteMin = " + capaciteMin);
		System.out.println("capaciteMax = " + capaciteMax);
		System.out.println("capacite = " + capacity);

		int currentLoad = (int) (taux_remplissage * capacity);
		System.out.println("CurrentLoad = " + currentLoad);

		int nb20Pieds = (int) (rep20Pieds * currentLoad);
		System.out.println("nb20Pieds = " + nb20Pieds);

		int nb40Pieds = ((int) (rep40Pieds * currentLoad) / 2);
		System.out.println("nb40Pieds = " + nb40Pieds);

		int nb45Pieds = (int) (((1.0 - (rep20Pieds + rep40Pieds)) * currentLoad) / 2.25);
		System.out.println("nb45Pieds = " + nb45Pieds);

		int nbContainers = nb20Pieds + nb40Pieds + nb45Pieds;
		System.out.println("nbContainers = " + nbContainers);

		double shipLength = capacity / capacityFactor;
		System.out.println("ShipLength = " + shipLength);

		bicGenerator.generateMore(nbContainers);
		// ContainerBICGenerator bicGenerator = new
		// ContainerBICGenerator(nbContainers, rt.getContainerNames());

		ArrayList<String> quayPave = new ArrayList<String>();
		List<String> quayIDs = new ArrayList<String>(Terminal.getInstance()
				.getPaves(BlockType.SHIP).keySet());
		for (String quayID : quayIDs) {
			quayPave.add(quayID);
		}

		// Find a quay
		String quayID = quayPave.get(r.nextInt(quayPave.size()));
		Block p = Terminal.getInstance().getPaves(BlockType.SHIP).get(quayID);
		// Time
		Time arrivalTime = new Time(r.nextInt((int) maxArrivalTime.toStep()));
		Time departureTime = new Time(arrivalTime, new Time(
				minimalBerthTimeLength.toStep()
						+ r.nextInt((int) (maxDepartureTime.toStep()
								- arrivalTime.toStep() - minimalBerthTimeLength
								.toStep()))));
		TimeWindow tw = new TimeWindow(arrivalTime, departureTime);

		double berthFrom = 0.0;
		double berthTo = 0.0;

		ok = false;
		List<Bay> lanes = p.getLanes();
		while (!ok) {
			ok = true;
			berthTo = berthFrom + shipLength / lanes.get(0).getLength();
			if (berthTo > 1.0) {
				ok = false;
				// Change quay !
				String newQuayID = quayID;
				while (newQuayID.equals(quayID)) {
					newQuayID = quayPave.get(r.nextInt(quayPave.size()));
				}
				quayID = newQuayID;
				p = Terminal.getInstance().getPaves(BlockType.SHIP).get(quayID);
			} else {
				ShipQuayReservation reservation = new ShipQuayReservation(
						quayID, tw, berthFrom, berthTo);
				List<ShipQuayReservation> reservations = quaysReservations
						.get(quayID);
				if (reservations == null)
					reservations = new ArrayList<ShipQuayReservation>();
				for (ShipQuayReservation sqr : reservations) {
					if (!reservation.isCompatibleWith(sqr)) {
						ok = false;
						break;
					}
				}
				if (ok) {
					// Write the reservation
					reservations.add(reservation);
					quaysReservations.put(quayID, reservations);
				}
			}
		}

		/**
		 * WRITING THE OPENING TAG
		 * 
		 * <event time='00:03:00.00' type='shipIn' capacity='1000' quay='Asie'
		 * from='0.0' to='0.33'>
		 * 
		 */
		sb.append("<event time='" + arrivalTime + "' type='shipIn' capacity='"
				+ capacity + "' quay='" + quayID + "' from='" + berthFrom
				+ "' to='" + berthTo + "'>\n");

		// Slots by lane ID between berthFrom and berthTo
		Map<String, List<Slot>> slots = new HashMap<>();

		int nSlots20 = 0;
		int nSlots40 = 0;
		int nSlots45 = 0;

		for (Bay l : lanes) {
			// Berth rate on this lane
			List<Slot> laneSlots = Terminal.getInstance().getSlots(l.getId());
			List<Slot> compatibleSlots = new ArrayList<Slot>();
			for (Slot s : laneSlots) {
				double sRate = s.getRateOnLane();
				if (sRate > berthFrom && sRate < berthTo) {
					if (s.getTEU() == 1.0)
						nSlots20++;
					else if (s.getTEU() == 2.0)
						nSlots40++;
					else
						nSlots45++;

					compatibleSlots.add(s);
				}
			}
			slots.put(l.getId(), compatibleSlots);
		}
		// If we count one container per slot at a time :
		int maxTeuToHandle = ((nSlots20 + nSlots40 + nSlots45) / 2)
				* (int) (new Time(departureTime, arrivalTime, false).toStep() / timePerContainerOperation
						.toStep());
		System.out.println("MAXTEU to handle = " + maxTeuToHandle);
		System.out.println("A = "
				+ new Time(departureTime, arrivalTime, false).toStep());
		System.out.println("B = " + timePerContainerOperation.toStep());
		System.out
				.println("A/B = "
						+ (new Time(departureTime, arrivalTime, false).toStep() / timePerContainerOperation
								.toStep()));
		System.out.println("C = " + ((nSlots20 + nSlots40 + nSlots45) / 2));

		System.out
				.println("D = "
						+ (int) (capacity * (taux_remplissage - taux_remplissage_apres_dechargement)));

		final int teuToUnload = Math
				.min((int) (capacity * (taux_remplissage - taux_remplissage_apres_dechargement)),
						maxTeuToHandle);
		System.out.println("TEU to unload = " + teuToUnload);

		// Unloading containers
		int nb20FeetAfterUnload = nb20Pieds;
		int nb40FeetAfterUnload = nb40Pieds;
		int nb45FeetAfterUnload = nb45Pieds;
		double teuUnloaded = 0;

		List<Slot> availableSlots = new ArrayList<>();
		for (String laneID : slots.keySet()) {
			for (Slot s : slots.get(laneID)) {
				availableSlots.add(s);
			}
		}

		final TreeMap<Time, ArrayList<SlotReservation>> slotsContainerreservations = new TreeMap<>();

		while (teuUnloaded < teuToUnload) {
			int i = r.nextInt(nb20FeetAfterUnload + nb40FeetAfterUnload
					+ nb45FeetAfterUnload);
			double teu = ContainerKind.getTeu(Container.TYPE_45_Feet);

			if (i < nb20FeetAfterUnload) {
				teu = ContainerKind.getTeu(Container.TYPE_20_Feet);

			} else if (i < nb20FeetAfterUnload + nb40FeetAfterUnload) {
				teu = ContainerKind.getTeu(Container.TYPE_40_Feet);
			}

			// Find a free slot at a given time
			Time containerUnloadTime = new Time(arrivalTime, new Time(
					r.nextInt((int) (departureTime.toStep() - arrivalTime
							.toStep()))));

			Slot s = availableSlots.get(r.nextInt(availableSlots.size()));
			if (s.getTEU() >= teu) {
				// Is the slot s available at containerUnloadTime and for
				// timePerContainerOperation period ?
				TimeWindow unloadTW = new TimeWindow(
						containerUnloadTime,
						new Time(containerUnloadTime, timePerContainerOperation));

				// TreeMap<Time, TimeWindow> tm =
				// slotReservations.get(s.getId());
				SlotReservations tm = slotReservations.get(s.getId());

				// if(tm == null) tm = new TreeMap<Time, TimeWindow>();
				if (tm == null)
					tm = new SlotReservations(s);
				Container cont = new Container("contID", teu);
				SlotReservation sr = tm.giveFreeReservation(cont, unloadTW);
				if (sr != null) {
					String contID = bicGenerator.giveMeBic().toString();
					sr.setContainer(new Container(contID, teu));
					// Reserve !
					addReservation(sr);

					if (teu == ContainerKind.getTeu(Container.TYPE_20_Feet))
						nb20FeetAfterUnload--;
					else if (teu == ContainerKind
							.getTeu(Container.TYPE_40_Feet))
						nb40FeetAfterUnload--;
					else
						nb45FeetAfterUnload--;

					// Stocker le conteneur + son emplacement + sa TW (ordonné
					// par date)
					// SlotReservation scsr = new SlotReservation(contID, s,
					// unloadTW, ContainerAlignment.center.getValue(),
					// s.getLevel(0));
					ArrayList<SlotReservation> slotResa;
					if (slotsContainerreservations.containsKey(unloadTW
							.getMin())) {
						slotResa = slotsContainerreservations.get(unloadTW
								.getMin());
					} else
						slotResa = new ArrayList<SlotReservation>();
					slotResa.add(sr);
					slotsContainerreservations.put(unloadTW.getMin(), slotResa);

					/**
					 * WRITING THE ShipContainerIn TAG
					 * 
					 * <shipContainerIn time='00:03:30.00' id='AAAA 000000 0'
					 * teu='2.0'> <containerLocation container='AAAA 000000 0'
					 * pave='Asie' lane='Asie-1/3' slot='Asie-1/3-0' level='0'
					 * align='center'/> </shipContainerIn>
					 */
					sb.append("\t<shipContainerIn time='" + unloadTW.getMin()
							+ "' id='" + contID + "' teu='" + teu + "'>\n");
					sb.append("\t\t<containerLocation container='" + contID
							+ "' pave='" + quayID + "' lane='"
							+ s.getLocation().getRoad().getId() + "' slot='"
							+ s.getId() + "' level='0' align='center'/>\n");
					sb.append("\t</shipContainerIn>\n");

					/*
					 * incrementProgressBar(); if(teu > 1.0)
					 * incrementProgressBar();
					 */
					teuUnloaded += teu;
				} else {

				}
			}
			// else choose another teu, time, slot...
		}

		/**
		 * WRITING THE ENDING TAG
		 * 
		 * "</event>"
		 */
		sb.append("</event>\n");

		// STOCK SLOTS FOR DELIVERY
		Map<String, Block> stockPaves = Terminal.getInstance().getPaves(
				BlockType.YARD);
		List<Slot> stockSlots = new ArrayList<Slot>();
		List<String> stockLanes = new ArrayList<String>();
		for (Block stockPave : stockPaves.values()) {
			for (Bay l : stockPave.getLanes()) {
				stockLanes.add(l.getId());
				for (String slotID : Terminal.getInstance().getSlotNames(
						l.getId())) {
					stockSlots.add(Terminal.getInstance().getSlot(slotID));
				}
			}
		}

		if (progress != null) {
			try {
				SwingUtilities.invokeAndWait(new Runnable() {
					@Override
					public void run() {
						progress.setMaximum(progress.getMaximum()
								+ slotsContainerreservations.size());
					}
				});
			} catch (InvocationTargetException e) {
				e.printStackTrace();
				log.error(e.getMessage(), e);
			} catch (InterruptedException e) {
				e.printStackTrace();
				log.error(e.getMessage(), e);
			}
		}
		// 2nde étape : générer les missions de déchargement
		while (slotsContainerreservations.size() > 0) {
			System.out.println("Unload Missions to compute : "
					+ slotsContainerreservations.size());

			ArrayList<SlotReservation> l = slotsContainerreservations
					.pollFirstEntry().getValue();
			for (SlotReservation scsr : l) {
				Container container = scsr.getContainer();

				Slot slotOrigin = scsr.getSlot();
				TimeWindow twP = scsr.getTW();

				// Gives an accurate TWP_Max
				ArrayList<SlotReservation> nextResa = slotReservations.get(
						slotOrigin.getId()).getHigherValue(twP.getMin());
				if (nextResa.size() > 0) {
					twP = new TimeWindow(twP.getMin(), nextResa.get(0).getTW()
							.getMin());
				}

				ok = false;
				ContainerLocation destination = null;
				SlotReservation res = null;
				while (!ok) {
					// Find a delivery slot
					String laneID = stockLanes
							.get(r.nextInt(stockLanes.size()));
					Slot s = Terminal.getInstance().getSlot(
							Terminal.getInstance().getSlotNames(laneID)[r
									.nextInt(Terminal.getInstance()
											.getSlotNames(laneID).length)]);

					if (s.getTEU() >= container.getTEU()) {
						try {
							Path pPickUpToDelivery = rsc.getRouting()
									.getShortestPath(slotOrigin.getLocation(),
											s.getLocation());

							Time startTime = twP.getMin();
							Time TWD_Min = new Time(startTime, new Time(
									pPickUpToDelivery.getCost()));
							Time TWD_Max = new Time(twP.getMax(), new Time(
									pPickUpToDelivery.getCost()));

							SlotReservations slotReservation = slotReservations
									.get(s.getId());
							if (slotReservation == null) {
								slotReservation = new SlotReservations(s);
							}

							res = slotReservation
									.giveFreeReservation(container,
											new TimeWindow(TWD_Min, TWD_Max));
							if (res == null) {
								ok = false;
							} else {
								ok = true;
								Slot slotDestination = res.getSlot();
								destination = new ContainerLocation(res
										.getContainer().getId(),
										slotDestination.getPaveId(),
										slotDestination.getLocation().getRoad()
												.getId(),
										slotDestination.getId(), res.getLevel()
												.getLevelIndex(),
										res.getAlignment());
								SlotReservations reservations = slotReservations
										.get(slotDestination.getId());
								if (reservations == null)
									reservations = new SlotReservations(
											slotDestination);
								addReservation(res);
							}
						} catch (NoPathFoundException e) {
							log.error(e.getMessage(), e);
						}
					}
				}
				// Générer la mission
				// slotOrigin -> slotDestination
				sb.append("<event time='" + arrivalTime
						+ "' type='newMission'>\n");
				sb.append("\t<mission id='unloadShip" + quayID + "["
						+ berthFrom + "-" + berthTo + "]With"
						+ container.getId() + "' container='"
						+ container.getId() + "' kind='"
						+ MissionKinds.IN.getIntValue() + "'>\n");
				sb.append("\t\t<timewindow start='" + twP.getMin() + "' end='"
						+ twP.getMax() + "'/>\n");
				sb.append("\t\t<timewindow start='" + res.getTW().getMin()
						+ "' end='" + res.getTW().getMax() + "'/>\n");
				sb.append("\t\t<containerLocation pave='"
						+ destination.getPaveId()
						+ "' lane='"
						+ destination.getLaneId()
						+ "' slot='"
						+ destination.getSlotId()
						+ "' level='"
						+ destination.getLevel()
						+ "' align='"
						+ ContainerAlignment.getStringValue(destination
								.getAlign()) + "'/>\n");
				sb.append("\t</mission>\n");
				sb.append("</event>\n");
				incrementProgressBar();
			}
		}

		// 3e étape : générer les missions de chargement
		final int teuToLoad = Math
				.min((int) (capacity * (taux_remplissage_apres_chargement - taux_remplissage_apres_dechargement)),
						maxTeuToHandle);
		System.out.println("TEU to load = " + teuToLoad);
		currentLoad = currentLoad - teuToUnload;
		System.out.println("Current Load = " + currentLoad);
		// double teuToReload = teuToLoad-currentLoad; // ?

		// Loading containers
		double teuReloaded = 0;

		// List of possible containers available from a given time
		HashMap<String, Time> possibleContainers = new HashMap<String, Time>();
		ArrayList<Container> loadedContainers = new ArrayList<Container>();

		// HashMap<String,Slot> stockSlots = new HashMap<String, Slot>();

		HashMap<String, Mission> missionSortedByContID = new HashMap<String, Mission>();
		for (Mission m : Terminal.getInstance().getMissions()) {
			missionSortedByContID.put(m.getContainerId(), m);
		}

		for (Container c : Terminal.getInstance().getContainersArray()) {
			Block pave = Terminal.getInstance().getBlock(
					c.getContainerLocation().getPaveId());
			if (pave.getType() == BlockType.YARD) {

				if (missionSortedByContID.containsKey(c.getId())) {
					Mission m = missionSortedByContID.get(c.getId());
					if (m.getMissionKind().getIntValue() != MissionKinds.OUT
							.getIntValue()
							&& m.getMissionKind().getIntValue() != MissionKinds.IN_AND_OUT
									.getIntValue()) {
						possibleContainers.put(c.getId(), m
								.getDeliveryTimeWindow().getMax());
						System.out.println("c.getID = " + c.getId());
					}
				} else {
					possibleContainers.put(c.getId(), arrivalTime);
				}
			}
		}

		if (progress != null) {
			try {
				SwingUtilities.invokeAndWait(new Runnable() {

					@Override
					public void run() {
						progress.setMaximum(progress.getMaximum() + teuToLoad);
					}
				});
			} catch (InvocationTargetException e) {
				e.printStackTrace();
				log.error(e.getMessage(), e);
			} catch (InterruptedException e) {
				e.printStackTrace();
				log.error(e.getMessage(), e);
			}
		}
		while (teuReloaded < teuToLoad) {
			// Pick a container in the stock
			// we should verify that the chosen container is not concerned by
			// another mission...
			String contID = new ArrayList<String>(possibleContainers.keySet())
					.get(r.nextInt(possibleContainers.size()));
			if (!containersOUT.containsKey(contID)) {
				Time availableTime = possibleContainers.get(contID);

				// TODO check if this container is not present at this time in
				// the terminal ! (if(container==null) container =
				// containerReservation.get(contID)...
				Container container = Terminal.getInstance().getContainer(
						contID);
				// TODO Idem ^
				Slot sPickup = Terminal.getInstance().getSlot(
						container.getContainerLocation().getSlotId());

				double teu = container.getTEU();

				// Compute TW belongs to [availableTime,departureTime-moveTime]
				Slot sDest = availableSlots
						.get(r.nextInt(availableSlots.size()));

				// Delivery time window : [belongs to [arrivalTime ,
				// departureTime - e] , belongs to [twd_min , departureTime -
				// e]]
				Time TWD_min = new Time(arrivalTime, new Time(
						r.nextInt((int) (departureTime.toStep() - arrivalTime
								.toStep()))));
				Time TWD_max = new Time(TWD_min, new Time(
						r.nextInt((int) (departureTime.toStep() - TWD_min
								.toStep()))));
				TimeWindow twD = new TimeWindow(TWD_min, TWD_max);

				if (sDest.getTEU() >= teu) {
					// TWP_min = Belongs to [arrivalTime of this container
					// within the terminal , TWD_min -
					// travelTime(pickup,delivery)]
					// TWP_min = Max ( arrival time , [TWD_min-travelTime -
					// (r.nextDouble()*margin) , TWD_min-traveltime];
					try {
						Path rp = rsc.getRouting().getShortestPath(
								sPickup.getLocation(), sDest.getLocation());

						Time travelTime = new Time(rp.getCost());
						// t1 = TWD_min - travelTime
						Time t1 = new Time(TWD_min, travelTime, false);
						// t2 belongs to [t1 , t1 - marginTime[
						double margin = marginRate * r.nextDouble();
						Time t2 = new Time(t1.toStep() * margin);
						// t3 = t1 - t2
						Time t3 = new Time(t1, t2, false);
						// T4 belongs to [t3,t1];
						Time t4 = new Time(t3, new Time(r.nextDouble()
								* (new Time(t1, t3, false).toStep())));
						Time TWP_min = new Time(Math.max(
								availableTime.toStep(), t4.toStep()));

						// TWP_max belongs to [TWP_min,TWD_min-travelTime]
						// => TWP_max belongs to [TWP_min,t1]
						Time t5 = new Time(t1, TWP_min, false);
						Time t6 = new Time((r.nextDouble() * t5.toStep()));
						Time TWP_max = new Time(TWP_min, t6);

						TimeWindow twP = new TimeWindow(TWP_min, TWP_max);

						SlotReservations srsP = slotReservations.get(sPickup
								.getId());
						if (srsP == null)
							System.out.println("SRS is null ! c.id = "
									+ container.getId() + " | "
									+ sPickup.getId() + " "
									+ container.getContainerLocation());

						boolean containerReachable = slotReservations.get(
								sPickup.getId()).isContainerFreeAt(container,
								twP);
						if (containerReachable) {
							// Choose a free destination slot in the quay called
							// quayID and between berthFrom and berthTo
							// available between TWD_min and TWD_max
							SlotReservation srD = slotReservations.get(
									sDest.getId()).giveFreeReservation(
									container, twD);
							if (srD != null) {
								// Make the reservations
								// -> Free Pickup slot location from twP_max
								updateReservationMaxTime(container, sPickup,
										TWP_max);

								// -> Reserve sDest slot at TWD
								addReservation(srD);
								loadedContainers.add(container);
								containersOUT.put(container.getId(),
										container.getId());
								// Write the mission
								sb.append("<event time='" + arrivalTime
										+ "' type='newMission'>\n");
								sb.append("\t<mission id='loadShip" + quayID
										+ "[" + berthFrom + "-" + berthTo
										+ "]With" + contID + "' container='"
										+ container.getId() + "' kind='"
										+ MissionKinds.IN.getIntValue()
										+ "'>\n");
								sb.append("\t\t<timewindow start='"
										+ twP.getMin() + "' end='"
										+ twP.getMax() + "'/>\n");
								sb.append("\t\t<timewindow start='"
										+ twD.getMin() + "' end='"
										+ twD.getMax() + "'/>\n");
								sb.append("\t\t<containerLocation pave='"
										+ sDest.getPaveId()
										+ "' lane='"
										+ srD.getSlot().getLocation().getRoad()
												.getId()
										+ "' slot='"
										+ sDest.getId()
										+ "' level='"
										+ srD.getLevel().getLevelIndex()
										+ "' align='"
										+ ContainerAlignment.getStringValue(srD
												.getAlignment()) + "'/>\n");
								sb.append("\t</mission>\n");
								sb.append("</event>\n");

								// Write the container out event
								sb.append("<event time='"
										+ twD.getMax()
										+ "' type='shipContainerOut' containerId='"
										+ container.getId() + "' slotId='"
										+ sDest.getId() + "'/>\n");

								incrementProgressBar();
								if (teu > 1)
									incrementProgressBar();
								// Validate the load
								teuReloaded += teu;
							}

						}
					} catch (NoPathFoundException e) {
						log.error(e.getMessage(), e);
					}
				}
			}

		}

		if (progress != null) {
			try {
				SwingUtilities.invokeAndWait(new Runnable() {
					@Override
					public void run() {
						progress.setMaximum(progress.getValue());
					}
				});
			} catch (InvocationTargetException e) {
				e.printStackTrace();
				log.error(e.getMessage(), e);
			} catch (InterruptedException e) {
				e.printStackTrace();
				log.error(e.getMessage(), e);
			}
		}
		// 4e étape : générer le départ

		sb.append("<event time='" + departureTime
				+ "' type='shipOut' capacity='" + capacity + "' quay='"
				+ quayID + "' from='" + berthFrom + "' to='" + berthTo + "'>\n");
		for (Container cont : loadedContainers) {
			sb.append("\t<container id='" + cont.getId() + "' teu='"
					+ cont.getTEU() + "'/>\n");
		}
		sb.append("</event>\n");

		// WRITE ALL IN THE XML FILE

		// copy all previous line of the file
		File tmpFile = new File("tmpMissions.xml");
		File missFile = new File(xmlFile);

		try {
			PrintWriter pw = new PrintWriter(tmpFile);
			pw.append("<document>\n");
			pw.append(sb.toString());
			pw.append("</document>");
			pw.close();
			tmpFile.renameTo(missFile);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			log.error(e.getMessage(), e);
		}
	}

	private void generateTrucksMissions(String missionFile,
			final int nbTruckMissions, double rateComeEmpty,
			double rateLeaveEmpty,
			Map<BlockType, List<String>> containersByPaveType, Time minTime,
			Time maxTime, Time unloadingTime, String groupID)
			throws ContainerDimensionException, IllegalArgumentException,
			NoPathFoundException {
		if (rateComeEmpty + rateLeaveEmpty > 1.0) {
			throw new IllegalArgumentException(
					"RateComeEmpty + RateLeaveEmpty should be equal or less than 1.0 !!!");
		}

		// Buffer for writing into containersFile.xml
		// StringBuilder sbContainers = new StringBuilder();

		// Buffer for writing into missionsFile.xml
		StringBuilder sb = new StringBuilder();

		// Time to long
		long fromTimeInLong = minTime.toStep();
		long toTimeInLong = maxTime.toStep();

		Hashtable<String, ArrayList<String>> slotsMapByLane = new Hashtable<String, ArrayList<String>>();
		ArrayList<String> roadLanes = new ArrayList<String>();
		Map<String, Block> pavesRoad = Terminal.getInstance().getPaves(
				BlockType.ROAD);
		for (Block p : pavesRoad.values()) {
			for (Bay l : p.getLanes()) {
				roadLanes.add(l.getId());
				ArrayList<String> lSlots = new ArrayList<String>(Terminal
						.getInstance().getSlotNames(l.getId()).length);
				for (String s : Terminal.getInstance().getSlotNames(l.getId())) {
					lSlots.add(s);
				}
				slotsMapByLane.put(l.getId(), lSlots);
			}
		}

		ArrayList<String> stockLanes = new ArrayList<String>();

		for (Block p : Terminal.getInstance().getPaves(BlockType.YARD).values()) {
			for (Bay l : p.getLanes()) {
				stockLanes.add(l.getId());
				ArrayList<String> lSlots = new ArrayList<String>(Terminal
						.getInstance().getSlotNames(l.getId()).length);
				for (String s : Terminal.getInstance().getSlotNames(l.getId())) {
					lSlots.add(s);
				}
				slotsMapByLane.put(l.getId(), lSlots);
			}
		}

		// ContainerBICGenerator bicGenerator = new
		// ContainerBICGenerator(nbTruckMissions, rt.getContainerNames());
		bicGenerator.generateMore(nbTruckMissions);
		if (progress != null) {
			try {
				SwingUtilities.invokeAndWait(new Runnable() {

					@Override
					public void run() {
						progress.setMaximum(progress.getMaximum()
								+ nbTruckMissions);
					}
				});
			} catch (InvocationTargetException e) {
				e.printStackTrace();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		int trucksCount = 1;

		// GENERATE INCOMING MISSIONS : Truck comes full and leaves empty
		System.out.println("Comes full leaves empty : "
				+ (nbTruckMissions * rateLeaveEmpty) + " missions to create !");
		for (int i = 0; i < nbTruckMissions * rateLeaveEmpty; i++) {
			// CREATE A TRUCK
			// String truckId = "truck_"+i;
			Time arrivalTime = new Time(fromTimeInLong
					+ r.nextInt((int) (toTimeInLong - fromTimeInLong)));

			double teu = ContainerKind.getTeu(r.nextInt(ContainerKind
					.getNbOfTypes()));

			Container container = new Container(bicGenerator.giveMeBic()
					.toString(), teu);

			// int absVal =
			// Math.abs((int)(unloadingTime.toStep()-container.getHandlingTime().toStep()));
			int absVal = Math
					.abs((int) (unloadingTime.toStep() - handlingTimeFromTruck
							.toStep()));

			Time t2 = null;
			if (absVal > 0)
				t2 = new Time(r.nextInt(absVal));
			else
				t2 = new Time(0);

			// Time departureTime = new Time(arrivalTime, new
			// Time(container.getHandlingTime(), t2));
			Time departureTime = new Time(arrivalTime, new Time(
					handlingTimeFromTruck, t2));

			// Find a location for the truck
			SlotReservation arrivalReservation = null;
			while (arrivalReservation == null) {
				String laneID = roadLanes.get(r.nextInt(roadLanes.size()));
				List<String> slots = slotsMapByLane.get(laneID);
				Slot slot = Terminal.getInstance().getSlot(
						slots.get(r.nextInt(slots.size())));

				SlotReservations reservations = slotReservations.get(slot
						.getId());
				if (reservations == null)
					reservations = new SlotReservations(slot);

				TimeWindow tw = new TimeWindow(arrivalTime, departureTime);
				arrivalReservation = reservations.giveFreeReservation(
						container, tw);
			}
			// Reserve the location
			addReservation(arrivalReservation);

			sb.append("<event time='"
					+ arrivalTime
					+ "' type='vehicleIn' id='truck_"
					+ groupID
					+ "["
					+ trucksCount
					+ "]' lanes='"
					+ arrivalReservation.getSlot().getLocation().getRoad()
							.getId() + "'>\n");
			sb.append("\t<container id='" + container.getId() + "' teu='"
					+ container.getTEU() + "'>\n");
			ContainerLocation cl = arrivalReservation.getContainerLocation();
			sb.append("\t\t" + cl.toXML() + "\n\t</container>\n</event>\n");

			// Create the stocking mision (from truck to stock)
			SlotReservation deliveryReservation = null;
			TimeWindow TWP = null;
			TimeWindow TWD = null;
			while (deliveryReservation == null) {
				// Find a destination in the stock
				Slot deliverySlot = pickASlot(stockLanes, slotsMapByLane, r);
				if (deliverySlot.getTEU() >= container.getTEU()) {
					// Compute Pickup and Delivery TimeWindows
					TWP = arrivalReservation.getTW();
					Path pPickUpToDelivery = rsc.getRouting().getShortestPath(
							arrivalReservation.getSlot().getLocation(),
							deliverySlot.getLocation());
					Time startTime = TWP.getMax();
					Time TWD_Min = new Time(startTime, new Time(
							pPickUpToDelivery.getCost()));
					// Time TWD_Max = new Time(TWD_Min,
					// container.getHandlingTime());
					Time TWD_Max = new Time(TWD_Min, handlingTimeFromGround);
					TWD = new TimeWindow(TWD_Min, TWD_Max);
					// Check if the place is free during TW
					// TWP = arrivalReservation.getTW with a modified TW_max
					// TWP_min = arrivalReservation.getTW().getMin(); TWP_max =
					// new
					// Time(arrivalReservation.getTW().getMin(),container.handlingTime);
					// TWD = [ TWP_max + move(pickup, delivery) - epsilon ,
					// TWD_min + container.handlingTime+epsilon ] (epsilon
					// belong to [ 0 .. marginTime [)
					SlotReservations deliveryReservations = slotReservations
							.get(deliverySlot.getId());
					if (deliveryReservations == null)
						deliveryReservations = new SlotReservations(
								deliverySlot);
					TimeWindow tw = new TimeWindow(TWD.getMin(), new Time(
							Time.MAXTIME));
					deliveryReservation = deliveryReservations
							.giveFreeReservation(container, tw);
					if (deliveryReservation == null) {
						System.out.println("Can't get a reservation for "
								+ container.getId() + " at " + tw + " on "
								+ deliverySlot.getId() + ". Retrying...");
					}
				}

			}

			addReservation(deliveryReservation);

			// If ok -> Write the mission
			String mId = "unload_truck_" + groupID + "[" + trucksCount + "]";

			// TODO Change the known time distribution
			Time missionKnownTime = new Time(minTime, new Time(
					r.nextInt((int) (arrivalTime.toStep() - minTime.toStep()))));

			sb.append("<event time='" + missionKnownTime
					+ "' type='newMission'>\n");
			sb.append("\t<mission id='" + mId + "' truck='truck_" + groupID
					+ "[" + trucksCount + "]' container='" + container.getId()
					+ "' kind='" + MissionKinds.IN.getIntValue() + "'>\n");
			sb.append("\t\t" + TWP.toXML() + "\n\t\t" + TWD.toXML() + "\n");
			sb.append("\t\t"
					+ deliveryReservation.getContainerLocation().toXML()
					+ "\n\t</mission>\n");
			sb.append("</event>\n");

			// Compute the truck departure time
			sb.append("<event time='"
					+ departureTime
					+ "' type='vehicleOut' id='truck_"
					+ groupID
					+ "["
					+ trucksCount
					+ "]' lanes='"
					+ arrivalReservation.getSlot().getLocation().getRoad()
							.getId() + "'/>\n");
			trucksCount++;

			incrementProgressBar();

			System.out
					.println("1 Mission Truck Comes Full and Leaves Empty DONE !");
		}

		// GENERATE OUTGOING MISSIONS : Truck comes empty and leaves full
		System.out.println("Comes empty leaves full : "
				+ (nbTruckMissions * rateComeEmpty) + " missions to create !");
		for (int i = 0; i < nbTruckMissions * rateComeEmpty; i++) {
			// CREATE A TRUCK
			Time arrivalTime = new Time(fromTimeInLong
					+ r.nextInt((int) (toTimeInLong - fromTimeInLong)));

			TimeWindow TWD = null;
			Slot deliverySlot = null;
			Slot pickupSlot = null;
			TimeWindow TWP = null;
			SlotReservations reservationsDelivery = null;
			SlotReservations reservationsPickup = null;
			Container container = null;

			boolean ok = false;
			while (!ok) {
				container = Terminal.getInstance().getContainer(
						containersByPaveType.get(BlockType.YARD).get(
								r.nextInt(containersByPaveType.get(
										BlockType.YARD).size())));
				if (!containersOUT.containsKey(container.getId())) {
					Time t2 = new Time(0);
					// int absVal =
					// Math.abs((int)(unloadingTime.toStep()-container.getHandlingTime().toStep()));
					int absVal = Math
							.abs((int) (unloadingTime.toStep() - handlingTimeFromGround
									.toStep()));
					if (absVal > 0) {
						t2 = new Time(r.nextInt(absVal));
					}
					// Time departureTime = new Time(arrivalTime, new
					// Time(container.getHandlingTime(), t2));
					Time departureTime = new Time(arrivalTime, new Time(
							handlingTimeFromGround, t2));
					TWD = new TimeWindow(arrivalTime, departureTime);

					// Choose a slot to park the truck
					deliverySlot = pickASlot(roadLanes, slotsMapByLane, r);
					if (deliverySlot.getTEU() >= container.getTEU()) {
						reservationsDelivery = slotReservations
								.get(deliverySlot.getId());
						if (reservationsDelivery == null)
							reservationsDelivery = new SlotReservations(
									deliverySlot);

						if (reservationsDelivery.isSlotEmptyAt(TWD)) {
							// Validate the container
							pickupSlot = Terminal.getInstance().getSlot(
									container.getContainerLocation()
											.getSlotId());
							try {
								Path pPickUpToDelivery = rsc.getRouting()
										.getShortestPath(
												pickupSlot.getLocation(),
												deliverySlot.getLocation());
								Time TWP_Max = new Time(TWD.getMin(), new Time(
										pPickUpToDelivery.getCost()),
										false);
								// Time TWP_Min = new
								// Time(TWP_Max,container.getHandlingTime(),false);
								Time TWP_Min = new Time(TWP_Max,
										handlingTimeFromGround, false);
								// if(unloadingTime.toStep()!=container.getHandlingTime().toStep()){
								if (unloadingTime.toStep() != handlingTimeFromGround
										.toStep()) {
									Time max = new Time(Math.max(
											unloadingTime.toStep(),
											handlingTimeFromGround.toStep()));
									Time min = new Time(Math.min(
											unloadingTime.toStep(),
											handlingTimeFromGround.toStep()));
									Time gap = new Time(r.nextInt((int) (max
											.toStep() - min.toStep())));
									TWP_Min = new Time(TWP_Max, new Time(min,
											gap), false);
								}
								TWP = new TimeWindow(TWP_Min, TWP_Max);
								if (TWP.getMin().toStep() > TWP.getMax()
										.toStep()) {
									System.out.println("MIN>MAX : " + TWP);
									System.exit(ReturnCodes.EXIT_ON_TIMEWINDOW_ERROR
											.getCode());
								}
								reservationsPickup = slotReservations
										.get(pickupSlot.getId());
								if (reservationsPickup == null)
									reservationsPickup = new SlotReservations(
											pickupSlot);
								if (reservationsPickup.isContainerFreeAt(
										container, TWP)) {
									ok = true;
								} else {
									if (TWP.getMin().toString().contains("-")) {
										System.out.println("HERE : "
												+ TWP.toString()
												+ " from time = "
												+ TWP.getMin().getMinutes()
												+ " "
												+ TWP.getMin().getSeconds());
									}
									System.out.println("Container "
											+ container.getId()
											+ " is not free at " + TWP
											+ "! Retrying... ");
								}
							} catch (NoPathFoundException e) {
								ok = false;
							}
						} else
							System.out.println("Slot " + deliverySlot.getId()
									+ " is not free at " + TWD
									+ "! Retrying... ");

					}
				}
			}
			containersOUT.put(container.getId(), container.getId());
			// ------ Reservations --------
			// | Delivery
			SlotReservation deliveryReservation = reservationsDelivery
					.giveFreeReservation(container, TWD);
			addReservation(deliveryReservation);
			// | Pickup
			updateReservationMaxTime(container, pickupSlot, TWP.getMax());
			// ---------------------------------------------

			// ------ Writing the truck -------
			// | Arrival
			sb.append("<event time='"
					+ TWD.getMin()
					+ "' type='vehicleIn' id='truck_"
					+ groupID
					+ "["
					+ trucksCount
					+ "]' lanes='"
					+ deliveryReservation.getSlot().getLocation().getRoad()
							.getId() + "'/>\n");
			// | Departure
			sb.append("<event time='"
					+ TWD.getMax()
					+ "' type='vehicleOut' id='truck_"
					+ groupID
					+ "["
					+ trucksCount
					+ "]'  lanes='"
					+ deliveryReservation.getSlot().getLocation().getRoad()
							.getId() + "'>\n");
			sb.append("\t<container id='" + container.getId() + "'/>\n");
			sb.append("</event>\n");
			// ---------------------------------------------

			// ------ Writing the mission --------
			String mId = "load_truck_" + groupID + "[" + trucksCount + "]";
			// | TODO Change the known time distribution
			Time missionKnownTime = new Time(minTime, new Time(
					r.nextInt((int) (arrivalTime.toStep() - minTime.toStep()))));
			sb.append("<event time='" + missionKnownTime
					+ "' type='newMission'>\n");
			sb.append("\t<mission id='" + mId + "' truck='truck_" + groupID
					+ "[" + trucksCount + "]' container='" + container.getId()
					+ "' kind='" + MissionKinds.OUT.getIntValue() + "'>\n");
			sb.append("\t\t" + TWP.toXML() + "\n\t\t" + TWD.toXML() + "\n");
			sb.append("\t\t"
					+ deliveryReservation.getContainerLocation().toXML()
					+ "\n\t</mission>\n");
			sb.append("</event>\n");
			trucksCount++;
			incrementProgressBar();
			// --------------------------------------------
			System.out
					.println("1 Mission Truck Comes Empty and Leaves Full DONE !");
		}

		// GENERATE OUTGOING MISSIONS : Truck comes full and leaves full
		System.out.println("Comes full leaves full : "
				+ ((1 - (rateComeEmpty + rateLeaveEmpty)) * nbTruckMissions)
				+ " missions to create !");
		for (int i = 0; i < (1 - (rateComeEmpty + rateLeaveEmpty))
				* nbTruckMissions; i++) {
			// CREATE A TRUCK
			Time arrivalTime = new Time(fromTimeInLong
					+ r.nextInt((int) (toTimeInLong - fromTimeInLong)));
			// CREATE A BROUGHT CONTAINER
			double teu = ContainerKind.getTeu(r.nextInt(ContainerKind
					.getNbOfTypes()));
			Container containerToUnload = new Container(bicGenerator
					.giveMeBic().toString(), teu);

			// Time unloadEndTime = new Time(arrivalTime, new
			// Time(Math.max(containerToUnload.getHandlingTime().toStep(),
			// r.nextInt((int)unloadingTime.toStep()))));
			Time unloadEndTime = new Time(arrivalTime, new Time(Math.max(
					handlingTimeFromTruck.toStep(),
					r.nextInt((int) unloadingTime.toStep()))));
			TimeWindow unloadingTW_P = new TimeWindow(arrivalTime,
					unloadEndTime);

			Time loadStartTime = new Time(unloadEndTime, new Time(
					r.nextInt((int) unloadingTime.toStep())));
			// Time loadEndTime = new Time(loadStartTime, new
			// Time(Math.max(containerToUnload.getHandlingTime().toStep() ,
			// r.nextInt((int)unloadingTime.toStep()))));
			Time loadEndTime = new Time(loadStartTime, new Time(Math.max(
					handlingTimeFromTruck.toStep(),
					r.nextInt((int) unloadingTime.toStep()))));
			TimeWindow loadingTW_D = new TimeWindow(loadStartTime, loadEndTime);

			TimeWindow truckTW = new TimeWindow(unloadingTW_P.getMin(),
					loadingTW_D.getMax());
			System.out.println("truck_" + groupID + "[" + trucksCount
					+ "] UNLOADING TW = " + unloadingTW_P);
			System.out.println("truck_" + groupID + "[" + trucksCount
					+ "] LOADING TW = " + loadingTW_D);
			boolean ok = false;
			Container containerToReload = null;
			SlotReservation truckReservation = null;
			SlotReservation containerToUnloadReservation = null;
			SlotReservations loadingPickupReservation = null;
			SlotReservation loadingDeliveryReservation = null;
			TimeWindow loadingTW_P = null;
			TimeWindow unloadingTW_D = null;
			// Check if the containerToReload will be ready
			Path pPickUpToDelivery = null;

			while (!ok) {
				containerToReload = Terminal.getInstance().getContainer(
						containersByPaveType.get(BlockType.YARD).get(
								r.nextInt(containersByPaveType.get(
										BlockType.YARD).size())));
				if (!containersOUT.containsKey(containerToReload.getId())
						&& !containerToReload.getId().equals(
								containerToUnload.getId())) {
					Slot pickupReloadSlot = Terminal.getInstance().getSlot(
							containerToReload.getContainerLocation()
									.getSlotId());

					// Find a slot for the truck
					truckReservation = null;
					while (truckReservation == null) {
						String laneID = roadLanes.get(r.nextInt(roadLanes
								.size()));
						List<String> slots = slotsMapByLane.get(laneID);
						Slot slot = Terminal.getInstance().getSlot(
								slots.get(r.nextInt(slots.size())));
						if (slot.getTEU() >= containerToUnload.getTEU()) {
							SlotReservations reservations = slotReservations
									.get(slot.getId());
							if (reservations == null)
								reservations = new SlotReservations(slot);
							if (reservations.isSlotEmptyAt(truckTW)) {
								truckReservation = reservations
										.giveFreeReservation(containerToUnload,
												truckTW);
							} else {
								System.out.println("Slot " + slot.getId()
										+ " is not free at " + truckTW
										+ "! Retrying... ");
							}
						}
					}

					SlotReservations truckReservations = slotReservations
							.get(truckReservation.getSlot().getId());

					// Path to go from the stock to the truck
					pPickUpToDelivery = rsc.getRouting().getShortestPath(
							pickupReloadSlot.getLocation(),
							truckReservation.getSlot().getLocation());

					Time loadingTWP_max = new Time(loadingTW_D.getMin(),
							new Time(pPickUpToDelivery.getCost()), false);
					// TODO check if it is realisable (it doesn't take into
					// account the path from the depot to the location)
					// Time loadingTWP_min = new Time(loadingTWP_max,new
					// Time(containerToReload.getHandlingTime()));
					Time loadingTWP_min = new Time(loadingTWP_max, new Time(
							handlingTimeFromGround));
					// if(unloadingTime.toStep()!=containerToReload.getHandlingTime().toStep()){
					if (unloadingTime.toStep() != handlingTimeFromGround
							.toStep()) {
						// Time max = new Time(Math.max(unloadingTime.toStep(),
						// containerToReload.getHandlingTime().toStep()));
						Time max = new Time(Math.max(unloadingTime.toStep(),
								handlingTimeFromGround.toStep()));
						// Time min = new Time(Math.min(unloadingTime.toStep(),
						// containerToReload.getHandlingTime().toStep()));
						Time min = new Time(Math.min(unloadingTime.toStep(),
								handlingTimeFromGround.toStep()));

						Time gap = new Time(r.nextInt((int) (max.toStep() - min
								.toStep())));
						loadingTWP_min = new Time(loadingTWP_max, new Time(min,
								gap), false);
					}
					loadingTW_P = new TimeWindow(loadingTWP_min, loadingTWP_max);
					if (loadingTW_P.getMin().toStep() > loadingTW_P.getMax()
							.toStep()) {
						System.out.println("2 MIN>MAX : " + loadingTW_P);
						System.exit(ReturnCodes.EXIT_ON_TIMEWINDOW_ERROR
								.getCode());
					}
					loadingPickupReservation = slotReservations
							.get(pickupReloadSlot.getId());
					if (loadingPickupReservation == null)
						loadingPickupReservation = new SlotReservations(
								pickupReloadSlot);

					if (loadingPickupReservation.isContainerFreeAt(
							containerToReload, loadingTW_P)) {
						// We can load the container to deliver
						loadingDeliveryReservation = truckReservations
								.giveFreeReservation(containerToReload,
										loadingTW_D);
						if (loadingDeliveryReservation != null) {
							// We can deliver it
							ok = true;
						} else {
							System.out
									.println("Can't get a free reservation for "
											+ containerToReload.getId()
											+ " on "
											+ pickupReloadSlot.getId()
											+ " at "
											+ loadingTW_D
											+ ". Retrying...");
						}
					} else {
						System.out.println("Slot " + pickupReloadSlot.getId()
								+ " is not free at " + loadingTW_D
								+ ". Retrying...");
					}
				}
			}
			containersOUT.put(containerToReload.getId(),
					containerToReload.getId());
			// Find a slot for the container to unload
			while (containerToUnloadReservation == null) {
				String laneID = stockLanes.get(r.nextInt(stockLanes.size()));
				List<String> slots = slotsMapByLane.get(laneID);
				Slot slot = Terminal.getInstance().getSlot(
						slots.get(r.nextInt(slots.size())));

				SlotReservations reservations = slotReservations.get(slot
						.getId());
				if (reservations == null)
					reservations = new SlotReservations(slot);

				// Compute unloadingTW_D
				// Path to go from the truck to the stock slot
				pPickUpToDelivery = rsc.getRouting().getShortestPath(
						truckReservation.getSlot().getLocation(),
						slot.getLocation());
				Time unloadingTWD_min = new Time(unloadingTW_P.getMax(),
						new Time(pPickUpToDelivery.getCost()));
				// TODO check if it is clever !
				// Time unloadingTWD_max = new Time(unloadingTWD_min,
				// containerToUnload.getHandlingTime());
				Time unloadingTWD_max = new Time(unloadingTWD_min,
						handlingTimeFromGround);
				// if(unloadingTime.toStep()!=containerToUnload.getHandlingTime().toStep()){
				if (unloadingTime.toStep() != handlingTimeFromGround.toStep()) {
					// unloadingTWD_max = new Time(unloadingTWD_min, new
					// Time(containerToUnload.getHandlingTime(), new
					// Time(r.nextInt(Math.abs((int)(unloadingTime.toStep()-containerToUnload.getHandlingTime().toStep()))))));
					unloadingTWD_max = new Time(unloadingTWD_min, new Time(
							handlingTimeFromGround, new Time(
									r.nextInt(Math.abs((int) (unloadingTime
											.toStep() - handlingTimeFromGround
											.toStep()))))));
				}

				unloadingTW_D = new TimeWindow(unloadingTWD_min,
						unloadingTWD_max);

				TimeWindow tw = new TimeWindow(unloadingTWD_min, maxTime);
				containerToUnloadReservation = reservations
						.giveFreeReservation(containerToUnload, tw);
				if (containerToUnloadReservation == null) {
					System.out.println("Can't get a free reservation for "
							+ containerToUnload.getId() + " on " + slot.getId()
							+ " at " + tw + ". Retrying...");
				}
			}

			// If ok then reserve !
			// | Truck reservation
			SlotReservations truckReservations = slotReservations
					.get(truckReservation.getSlot().getId());
			addReservation(truckReservation);
			updateReservationMaxTime(containerToUnload,
					truckReservations.getSlot(), unloadingTW_P.getMax());

			// | Unload -> Stock : stock reservation
			SlotReservations containerToUnLoadReservations = slotReservations
					.get(containerToUnloadReservation.getSlot().getId());
			if (containerToUnLoadReservations == null)
				containerToUnLoadReservations = new SlotReservations(
						containerToUnloadReservation.getSlot());
			addReservation(containerToUnloadReservation);
			// | Stock -> Load : load reservation
			addReservation(loadingDeliveryReservation);
			// | Stock -> Load : stock update max time
			updateReservationMaxTime(containerToReload,
					loadingPickupReservation.getSlot(), loadingTW_P.getMax());

			// If ok -> Write the mission
			// | Truck arrival
			sb.append("<event time='"
					+ truckReservation.getTW().getMin()
					+ "' type='vehicleIn' id='truck_"
					+ groupID
					+ "["
					+ trucksCount
					+ "]'  lanes='"
					+ truckReservation.getSlot().getLocation().getRoad()
							.getId() + "'>\n");
			sb.append("\t<container id='" + containerToUnload.getId()
					+ "' teu='" + containerToUnload.getTEU() + "'>\n");
			ContainerLocation cl = truckReservation.getContainerLocation();

			// TODO CHANGE HERE
			// ContainerLocation cl =
			// containerToUnloadReservation.getContainerLocation();
			sb.append("\t\t" + cl.toXML() + "\n\t</container>\n</event>\n");
			// | Unloading mission
			String mId = "unload_truck_" + groupID + "[" + trucksCount + "]";
			// TODO Change the known time distribution
			Time missionKnownTime = new Time(minTime,
					new Time(r.nextInt((int) (truckReservation.getTW().getMin()
							.toStep() - minTime.toStep()))));
			sb.append("<event time='" + missionKnownTime
					+ "' type='newMission'>\n");
			sb.append("\t<mission id='" + mId + "' truck='truck_" + groupID
					+ "[" + trucksCount + "]' container='"
					+ containerToUnload.getId() + "' kind='"
					+ MissionKinds.IN.getIntValue() + "'>\n");
			sb.append("\t\t" + unloadingTW_P.toXML() + "\n\t\t"
					+ unloadingTW_D.toXML() + "\n");
			sb.append("\t\t"
					+ containerToUnloadReservation.getContainerLocation()
							.toXML() + "\n\t</mission>\n");
			sb.append("</event>\n");

			// | Loading mission
			mId = "load_truck_" + groupID + "[" + trucksCount + "]";
			sb.append("<event time='" + missionKnownTime
					+ "' type='newMission'>\n");
			sb.append("\t<mission id='" + mId + "' truck='truck_" + groupID
					+ "[" + trucksCount + "]' container='"
					+ containerToReload.getId() + "' kind='"
					+ MissionKinds.OUT.getIntValue() + "'>\n");
			sb.append("\t\t" + loadingTW_P.toXML() + "\n\t\t"
					+ loadingTW_D.toXML() + "\n");
			sb.append("\t\t"
					+ loadingDeliveryReservation.getContainerLocation().toXML()
					+ "\n\t</mission>\n");
			sb.append("</event>\n");
			// | Vehicle departure
			sb.append("<event time='"
					+ truckReservation.getTW().getMax()
					+ "' type='vehicleOut' id='truck_"
					+ groupID
					+ "["
					+ trucksCount
					+ "]'  lanes='"
					+ truckReservation.getSlot().getLocation().getRoad()
							.getId() + "'>\n");
			sb.append("\t<container id='" + containerToReload.getId()
					+ "'/>\n</event>\n");

			trucksCount++;
			incrementProgressBar();
			System.out
					.println("1 Mission Truck Comes Full and Leaves Full DONE !");
		}

		// WRITE INTO FILE
		try {
			File tmpFile = new File("tmpMissions.xml");
			File missFile = new File(missionFile);
			PrintWriter pw = new PrintWriter(tmpFile);
			pw.append("<document>\n");
			pw.append(sb.toString());
			pw.append("</document>");
			pw.close();
			tmpFile.renameTo(missFile);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			log.error(e.getMessage(), e);
		}
	}

	private void incrementProgressBar() {
		if (progress != null) {
			try {
				SwingUtilities.invokeAndWait(new Runnable() {

					@Override
					public void run() {
						progress.setValue(progress.getValue() + 1);
					}
				});
			} catch (InvocationTargetException e) {
				e.printStackTrace();
				log.error(e.getMessage(), e);
			} catch (InterruptedException e) {
				e.printStackTrace();
				log.error(e.getMessage(), e);
			}
		}
	}

	private Slot pickASlot(ArrayList<String> lanesID,
			Hashtable<String, ArrayList<String>> slotMapByLane, Random r) {
		String lID = lanesID.get(r.nextInt(lanesID.size()));
		ArrayList<String> slotList = slotMapByLane.get(lID);
		String slotID = slotList.get(r.nextInt(slotList.size()));
		return Terminal.getInstance().getSlot(slotID);
	}

	private void updateReservationMaxTime(Container container, Slot slot,
			Time newMaxTime) {
		SlotReservations srs = slotReservations.get(slot.getId());
		srs.updateReservationMaxTime(container, newMaxTime);
		slotReservations.put(slot.getId(), srs);
		pw.append("Reservation updated : \n" + srs + "\n");
		pw.flush();
	}

	private void addReservation(SlotReservation reservation) {
		SlotReservations reservations = slotReservations.get(reservation
				.getSlot().getId());
		if (reservations == null)
			reservations = new SlotReservations(reservation.getSlot());
		reservations.addReservation(reservation);
		slotReservations.put(reservation.getSlot().getId(), reservations);
		pw.append("Reservation added : \n" + reservations + "\n");
		pw.flush();
	}

	/*
	 * private SlotReservation findEmptyLocationInTime(Container container,
	 * ArrayList<String> laneList, Random r, RemoteTerminal rt, Time fromTime,
	 * Slot fromSlot) , NoPathFoundException{ SlotReservation res = null; int
	 * i=0; do{ String laneID = laneList.get(r.nextInt(laneList.size())); Slot s
	 * =
	 * rt.getSlot(rt.getSlotNames(laneID)[r.nextInt(rt.getSlotNames(laneID).length
	 * )]); StraddleCarrier rsc = new
	 * ArrayList<StraddleCarrier>(Terminal.getInstance
	 * ().straddleCarriers.values(
	 * )).get(r.nextInt(Terminal.getInstance().straddleCarriers.size())); Path
	 * pPickUpToDelivery =
	 * rsc.getRouting().getShortestPath(fromSlot.getLocation(),
	 * s.getLocation());
	 * 
	 * Time startTime = fromTime; Time TWD_Min = new Time(startTime, new
	 * Time(pPickUpToDelivery.getCost()+"s")); Time TWD_Max = new Time(TWD_Min,
	 * container.getHandlingTime()); TimeWindow tw = new TimeWindow(TWD_Min,
	 * TWD_Max);
	 * 
	 * if(s.getTEU()>=container.getTEU()){ SlotReservations slotReservation =
	 * slotReservations.get(s.getId()); if(slotReservation == null){
	 * slotReservation = new SlotReservations(s); }
	 * 
	 * res = slotReservation.giveFreeReservation(container, tw); if(res !=
	 * null){ return res; } } i++; }while(i<laneList.size()*2); //To prevent
	 * from searching a slot in a pave which has only smaller ones...
	 * 
	 * return res; }
	 */

	private void generateTrainMissions(String missionsFile,
			Map<BlockType, List<String>> conteneurs, Time arrivalTime,
			Time maxTime, double marginRate, double fullRate,
			double fullRateAfterUnload, double fullRateAfterReload,
			String trainId) throws NoPathFoundException,
			ContainerDimensionException, EmptyLevelException {
		// Create an incoming train
		System.out.println("Creating TRAIN !");
		StringBuilder sbMissions = new StringBuilder();
		sbMissions.append("<event time='");
		Time t = arrivalTime;
		sbMissions.append(t.toString() + "' type='vehicleIn' id='train_"
				+ trainsCount + "' lanes='");

		// Get the laneGroups
		Map<String, Block> trainPaves = Terminal.getInstance().getPaves(
				BlockType.RAILWAY);
		// Gives the lanes of each railways
		Map<String, List<String>> mapGroup = new HashMap<>();
		// Gives max number of containers which can be stock on the train
		Map<String, Integer[]> mapGroupSlotCount = new HashMap<>();
		for (Block p : trainPaves.values()) {
			for (Bay l : p.getLanes()) {
				String laneGroup = l.getLaneGroup();
				if (!laneGroup.equals("")) {
					if (!mapGroup.containsKey(laneGroup)) {
						mapGroup.put(laneGroup, new ArrayList<String>());
						Integer[] tab = new Integer[3];
						for (int i = 0; i < tab.length; i++)
							tab[i] = 0;
						mapGroupSlotCount.put(laneGroup, tab);
					}
					if (!mapGroup.get(laneGroup).contains(l.getId())) {
						mapGroup.get(laneGroup).add(l.getId());
						List<Slot> lSlots = Terminal.getInstance().getSlots(
								l.getId());
						for (Slot s : lSlots) {
							int index;
							if (s.getTEU() == ContainerKind
									.getTeu(Container.TYPE_20_Feet))
								index = Container.TYPE_20_Feet;
							else if (s.getTEU() == ContainerKind
									.getTeu(Container.TYPE_40_Feet))
								index = Container.TYPE_40_Feet;
							else
								index = Container.TYPE_45_Feet;
							mapGroupSlotCount.get(laneGroup)[index] = mapGroupSlotCount
									.get(laneGroup)[index] + 1;
						}
					}
				}
			}
		}

		// Pick a railway for this train
		String group = "";
		for (String key : mapGroup.keySet()) {
			if (!group.equals(""))
				break;

			List<String> lanes = mapGroup.get(key);

			List<Slot> lSlots = Terminal.getInstance().getSlots(lanes.get(0));
			// TODO Pick any group no matter it is ready or not ?
			Slot s0 = lSlots.get(0);

			if (slotReservations.get(s0.getId()) == null)
				slotReservations.put(s0.getId(), new SlotReservations(s0));
			if (slotReservations.get(s0.getId()).isSlotEmptyAt(
					new TimeWindow(arrivalTime, maxTime))) {
				group = key;
			}
		}
		List<String> lanesIds = mapGroup.get(group);
		System.out.print("Lanes : ");
		for (int i = 0; i < lanesIds.size(); i++) {
			sbMissions.append(lanesIds.get(i));
			System.out.print(lanesIds.get(i) + " ");
			if (i < lanesIds.size() - 1)
				sbMissions.append(",");
			else
				sbMissions.append("'>\n");
		}
		System.out.println();

		// Trains have only one level !
		// 20 feets
		int nbContainers20 = mapGroupSlotCount.get(group)[Container.TYPE_20_Feet];
		nbContainers20 = (int) (fullRate * nbContainers20);
		// 40 feets
		int nbContainers40 = mapGroupSlotCount.get(group)[Container.TYPE_40_Feet];
		nbContainers40 = (int) (fullRate * nbContainers40);
		// 45 feets
		int nbContainers45 = mapGroupSlotCount.get(group)[Container.TYPE_45_Feet];
		nbContainers45 = (int) (fullRate * nbContainers45);
		System.out.println("Nb 45 : " + nbContainers45 + " Nb 40 : "
				+ nbContainers40 + " Nb 20 : " + nbContainers20);

		// Used for knowing which containers are in the train at the end of the
		// generation
		Map<String, Container> containersOnTrain = new HashMap<>(nbContainers20
				+ nbContainers40 + nbContainers45);

		// Lists of train slots by size
		List<Slot> slots20Feet = new ArrayList<>(
				mapGroupSlotCount.get(group)[Container.TYPE_20_Feet]);
		List<Slot> slots40Feet = new ArrayList<>(
				mapGroupSlotCount.get(group)[Container.TYPE_40_Feet]);
		List<Slot> slots45Feet = new ArrayList<>(
				mapGroupSlotCount.get(group)[Container.TYPE_45_Feet]);
		for (String lane : mapGroup.get(group)) {
			for (Slot s : Terminal.getInstance().getSlots(lane)) {
				if (s.getTEU() == ContainerKind.getTeu(Container.TYPE_20_Feet))
					slots20Feet.add(s);
				else if (s.getTEU() == ContainerKind
						.getTeu(Container.TYPE_40_Feet))
					slots40Feet.add(s);
				else if (s.getTEU() == ContainerKind
						.getTeu(Container.TYPE_45_Feet))
					slots45Feet.add(s);
			}
		}

		// Creation of the containers stocked on the train
		// ContainerBICGenerator bicGenerator = new
		// ContainerBICGenerator(nbContainers45+nbContainers20+nbContainers40,
		// rt.getContainerNames());
		bicGenerator.generateMore(nbContainers45 + nbContainers20
				+ nbContainers40);

		List<Container> containers45Feet = new ArrayList<>(nbContainers45);
		List<Container> containers40Feet = new ArrayList<>(nbContainers40);
		List<Container> containers20Feet = new ArrayList<>(nbContainers20);

		Map<String, ContainerLocation> containerLocationOnTrain = new HashMap<>();
		Map<String, List<ContainerLocation>> containerLocationBySlot = new HashMap<>();

		for (int i = 0; i < nbContainers45; i++) {
			Container c = new Container(bicGenerator.giveMeBic().toString(),
					ContainerKind.getTeu(Container.TYPE_45_Feet));

			// Choose a slot, a level, an alignment
			boolean slotOk = false;
			Slot slot = null;
			while (!slotOk) {
				slot = slots45Feet.get(r.nextInt(slots45Feet.size()));
				if (slotReservations.get(slot.getId()).isSlotEmptyAt(
						new TimeWindow(arrivalTime, maxTime)))
					slotOk = true;
			}

			// Put it
			SlotReservations reservations = slotReservations.get(slot.getId());
			SlotReservation reservation = reservations.giveFreeReservation(c,
					new TimeWindow(arrivalTime, maxTime));
			addReservation(reservation);

			containers45Feet.add(c);
			containersOnTrain.put(c.getId(), c);
			containerLocationOnTrain.put(c.getId(),
					reservation.getContainerLocation());

			List<ContainerLocation> l = containerLocationBySlot.get(slot
					.getId());
			if (l == null)
				l = new ArrayList<ContainerLocation>();
			l.add(Math.min(l.size(), reservation.getContainerLocation()
					.getLevel()), reservation.getContainerLocation());
			containerLocationBySlot.put(slot.getId(), l);

			System.out.println("Container : id='" + c.getId() + "' teu='"
					+ c.getTEU());

			sbMissions.append("\t<container id='" + c.getId() + "' teu='"
					+ c.getTEU() + "'>\n");
			sbMissions.append("\t\t"
					+ reservation.getContainerLocation().toXML()
					+ "\n\t</container>\n");
		}

		for (int i = 0; i < nbContainers40; i++) {
			Container c = new Container(bicGenerator.giveMeBic().toString(),
					ContainerKind.getTeu(Container.TYPE_40_Feet));
			// Choose a slot, a level, an alignment
			boolean slotOk = false;
			Slot slot = null;
			while (!slotOk) {
				slot = slots40Feet.get(r.nextInt(slots40Feet.size()));
				if (slotReservations.get(slot.getId()).isSlotEmptyAt(
						new TimeWindow(arrivalTime, maxTime)))
					slotOk = true;
			}

			// Put it
			SlotReservations reservations = slotReservations.get(slot.getId());
			SlotReservation reservation = reservations.giveFreeReservation(c,
					new TimeWindow(arrivalTime, maxTime));
			addReservation(reservation);

			containers40Feet.add(c);
			containersOnTrain.put(c.getId(), c);
			containerLocationOnTrain.put(c.getId(),
					reservation.getContainerLocation());

			List<ContainerLocation> l = containerLocationBySlot.get(slot
					.getId());
			if (l == null)
				l = new ArrayList<ContainerLocation>();
			l.add(Math.min(l.size(), reservation.getContainerLocation()
					.getLevel()), reservation.getContainerLocation());
			containerLocationBySlot.put(slot.getId(), l);

			System.out.println("Container : id='" + c.getId() + "' teu='"
					+ c.getTEU());
			sbMissions.append("\t<container id='" + c.getId() + "' teu='"
					+ c.getTEU() + "'>\n");
			sbMissions.append("\t\t"
					+ reservation.getContainerLocation().toXML()
					+ "\n\t</container>\n");
		}

		for (int i = 0; i < nbContainers20; i++) {
			Container c = new Container(bicGenerator.giveMeBic().toString(),
					ContainerKind.getTeu(Container.TYPE_20_Feet));
			// Choose a slot, a level, an alignment
			Slot slot = null;
			SlotReservation reservation = null;
			while (reservation == null) {
				slot = slots20Feet.get(r.nextInt(slots20Feet.size()));
				reservation = slotReservations.get(slot.getId())
						.giveFreeReservation(c,
								new TimeWindow(arrivalTime, maxTime));
			}

			// Put it
			addReservation(reservation);

			containersOnTrain.put(c.getId(), c);
			containers20Feet.add(c);
			containerLocationOnTrain.put(c.getId(),
					reservation.getContainerLocation());

			List<ContainerLocation> l = containerLocationBySlot.get(slot
					.getId());
			if (l == null)
				l = new ArrayList<ContainerLocation>();
			l.add(Math.min(l.size(), reservation.getContainerLocation()
					.getLevel()), reservation.getContainerLocation());
			containerLocationBySlot.put(slot.getId(), l);

			System.out.println("Container : id='" + c.getId() + "' teu='"
					+ c.getTEU());
			sbMissions.append("\t<container id='" + c.getId() + "' teu='"
					+ c.getTEU() + "'>\n");
			sbMissions.append("\t\t"
					+ reservation.getContainerLocation().toXML()
					+ "\n\t</container>\n");
		}
		sbMissions.append("</event>\n");
		// ---- END OF TRAIN CREATION -----
		// Now we have a train with containers asking to be unloaded and loaded
		// with other containers

		// Compute unload missions
		System.out.println("Computing unload missions !");
		int nbUnloadMissions45Feet = Math
				.max(0,
						(nbContainers45 - (int) (mapGroupSlotCount.get(group)[Container.TYPE_45_Feet] * fullRateAfterUnload)));
		int nbUnloadMissions40Feet = Math
				.max(0,
						(nbContainers40 - (int) (mapGroupSlotCount.get(group)[Container.TYPE_40_Feet] * fullRateAfterUnload)));
		int nbUnloadMissions20Feet = Math
				.max(0,
						(nbContainers20 - (int) (mapGroupSlotCount.get(group)[Container.TYPE_20_Feet] * fullRateAfterUnload)));
		System.out.println("Nb45Feet : " + nbUnloadMissions45Feet
				+ " Nb40Feet : " + nbUnloadMissions40Feet + " Nb20Feet : "
				+ nbUnloadMissions20Feet);

		// Lists of slots by size in the stocks areas
		ArrayList<Slot> stockSlots45Feet = new ArrayList<Slot>();
		ArrayList<Slot> stockSlots40Feet = new ArrayList<Slot>();
		ArrayList<Slot> stockSlots20Feet = new ArrayList<Slot>();

		for (Block p : Terminal.getInstance().getPaves(BlockType.YARD).values()) {
			for (Bay l : p.getLanes()) {
				for (Slot s : Terminal.getInstance().getSlots(l.getId())) {
					if (s.getTEU() == ContainerKind
							.getTeu(Container.TYPE_45_Feet))
						stockSlots45Feet.add(s);
					else if (s.getTEU() == ContainerKind
							.getTeu(Container.TYPE_40_Feet))
						stockSlots40Feet.add(s);
					else if (s.getTEU() == ContainerKind
							.getTeu(Container.TYPE_20_Feet))
						stockSlots20Feet.add(s);
				}
			}
		}

		// Usefull for computing the time windows of the reloading missions and
		// to prevent the reloading of the train untill unloading is over
		int nbUnloadingMissions = nbUnloadMissions20Feet
				+ nbUnloadMissions40Feet + nbUnloadMissions45Feet;

		int nbReloadMissions45Feet = Math.max(0,
				(int) (slots45Feet.size() * fullRateAfterReload)
						- (nbContainers45 - nbUnloadMissions45Feet));
		int nbReloadMissions40Feet = Math.max(0,
				(int) (slots40Feet.size() * fullRateAfterReload)
						- (nbContainers40 - nbUnloadMissions40Feet));
		int nbReloadMissions20Feet = Math.max(0,
				(int) (slots20Feet.size() * fullRateAfterReload)
						- (nbContainers20 - nbUnloadMissions20Feet));

		int nbLoadingMissions = nbReloadMissions20Feet + nbReloadMissions40Feet
				+ nbReloadMissions45Feet;

		final int nbMissionsOverall = nbUnloadingMissions + nbLoadingMissions;

		if (progress != null) {
			try {
				SwingUtilities.invokeAndWait(new Runnable() {

					@Override
					public void run() {
						progress.setMaximum(progress.getMaximum()
								+ nbMissionsOverall);
					}
				});
			} catch (InvocationTargetException e) {
				e.printStackTrace();
				log.error(e.getMessage(), e);
			} catch (InterruptedException e) {
				e.printStackTrace();
				log.error(e.getMessage(), e);
			}
		}

		Time maxTimeForUnload = new Time(
				arrivalTime,
				new Time(
						((maxTime.getInSec() - arrivalTime.getInSec()) * (nbUnloadingMissions / (nbMissionsOverall + 0.0)))));
		System.err.println("ArrivalTime = " + arrivalTime);
		System.err.println("MaxTime = " + maxTime);
		System.err.println("NB_UNLOADINGS = " + nbUnloadingMissions);
		System.err.println("NB_MISSIONS = " + nbMissionsOverall);
		System.err.println("MaxTimeForUnload = " + maxTimeForUnload);
		Time maxUnloadTime = new Time(0);

		int nbMissionsCreated = 0;
		// For the missions ids
		int nbMissionsCreatedOverall = 0;

		for (Slot slot : slots45Feet) {
			if (nbMissionsCreated >= nbUnloadMissions45Feet) {
				break;
			}

			Time fromTime = arrivalTime;

			List<ContainerLocation> l = containerLocationBySlot.get(slot
					.getId());
			if (l != null) {
				for (int i = l.size() - 1; i >= 0
						&& nbMissionsCreated < nbUnloadMissions45Feet; i--) {
					Container c = null;
					for (int j = 0; j < containers45Feet.size() && c == null; j++) {
						if (containers45Feet.get(j).getId()
								.equals(l.get(i).getContainerId())) {
							c = containers45Feet.get(j);
						}
					}

					String mId = "unload_" + trainId + "_"
							+ (nbMissionsCreatedOverall + 1);
					sbMissions.append("<mission id='" + mId + "' container='"
							+ c.getId() + "' kind='"
							+ MissionKinds.IN.getIntValue() + "'>\n");

					// Choose a destination in the stock area
					Slot sDestination = null;
					SlotReservation destinationReservation = null;
					TimeWindow twP = null;
					TimeWindow twD = null;

					while (destinationReservation == null) {
						sDestination = stockSlots45Feet.get(r
								.nextInt(stockSlots45Feet.size()));

						Time duration = new Time(maxTimeForUnload, fromTime,
								false); // Change false into true
						System.err.println("Duration = " + duration);
						Time startTime = new Time(fromTime, new Time(
								(r.nextDouble() * duration.getInSec())),
								true);
						System.err.println("StartTime : " + startTime);
						// Pickup
						Path pStartToPickUp = rsc.getRouting().getShortestPath(
								rsc.getLocation(), slot.getLocation());
						Time pickupEndTime = new Time(startTime, new Time(
								pStartToPickUp.getCost()));
						pickupEndTime = new Time(pickupEndTime,
								handlingTimeFromTruck);
						Time margin = new Time(
								(marginRate * pickupEndTime.getInSec()));
						Time puMin = new Time(pickupEndTime, margin, false);
						Time puMax = new Time(pickupEndTime, margin, true);
						twP = new TimeWindow(puMin, puMax);
						System.err.println("twP = " + twP);
						// Delivery
						Path pPickUpToDelivery = rsc.getRouting()
								.getShortestPath(slot.getLocation(),
										sDestination.getLocation());
						Time deliveryEndTime = new Time(pickupEndTime,
								new Time(pPickUpToDelivery.getCost()));
						deliveryEndTime = new Time(deliveryEndTime,
								handlingTimeFromGround);
						margin = new Time(
								(marginRate * deliveryEndTime.getInSec()));
						Time dMin = new Time(deliveryEndTime, margin, false);
						Time dMax = new Time(deliveryEndTime, margin, true);
						twD = new TimeWindow(dMin, dMax);
						System.err.println("twD = " + twD);
						if (puMax.toStep() > maxUnloadTime.toStep())
							maxUnloadTime = puMax;
						startTime = pickupEndTime; // TODO useless ?!

						destinationReservation = slotReservations.get(
								sDestination.getId()).giveFreeReservation(c,
								twD);
					}
					// Reservation for the delivery
					// SlotReservations destinationReservations =
					// slotReservations.get(sDestination.getId());
					addReservation(destinationReservation);
					// Reservation update for the pickup
					// SlotReservations pickupReservations =
					// slotReservations.get(slot.getId());
					updateReservationMaxTime(c, slot, twP.getMax());

					// Prevent from time windows inversions between containers
					// of the same slot
					fromTime = twP.getMax();
					containersOnTrain.remove(c.getId());

					sbMissions.append("\t" + twP.toXML() + "\n\t" + twD.toXML()
							+ "\n");
					sbMissions.append("\t"
							+ destinationReservation.getContainerLocation()
									.toXML() + "\n</mission>\n");

					incrementProgressBar();

					nbMissionsCreated++;
					nbMissionsCreatedOverall++;
				}

			}
		}
		System.out.println("nb45Created : " + nbMissionsCreated);
		nbMissionsCreated = 0;
		for (Slot slot : slots40Feet) {

			Time fromTime = arrivalTime;

			if (nbMissionsCreated >= nbUnloadMissions40Feet) {
				break;
			}

			List<ContainerLocation> l = containerLocationBySlot.get(slot
					.getId());
			if (l != null) {
				for (int i = l.size() - 1; i >= 0
						&& nbMissionsCreated < nbUnloadMissions40Feet; i--) {
					Container c = null;
					for (int j = 0; j < containers40Feet.size() && c == null; j++) {
						if (containers40Feet.get(j).getId()
								.equals(l.get(i).getContainerId())) {
							c = containers40Feet.get(j);
						}
					}
					String mId = "unload_" + trainId + "_"
							+ (nbMissionsCreatedOverall + 1);
					if (c == null) {

						break;
					}
					sbMissions.append("<mission id='" + mId + "' container='"
							+ c.getId() + "' kind='"
							+ MissionKinds.IN.getIntValue() + "'>\n");

					// Choose a destination in the stock area
					Slot sDestination = null;
					SlotReservation destinationReservation = null;
					TimeWindow twP = null;
					TimeWindow twD = null;
					while (destinationReservation == null) {
						// Choose a destination in the stock area
						sDestination = stockSlots40Feet.get(r
								.nextInt(stockSlots40Feet.size()));
						Time duration = new Time(maxTimeForUnload, fromTime,
								false); // Change false into true
						System.err.println("Duration = " + duration);
						Time startTime = new Time(fromTime, new Time(
								(r.nextDouble() * duration.getInSec())),
								true);
						System.err.println("StartTime : " + startTime);
						// Pickup
						Path pStartToPickUp = rsc.getRouting().getShortestPath(
								rsc.getLocation(), slot.getLocation());
						Time pickupEndTime = new Time(startTime, new Time(
								pStartToPickUp.getCost()));
						pickupEndTime = new Time(pickupEndTime,
								handlingTimeFromTruck);
						Time margin = new Time(
								(marginRate * pickupEndTime.getInSec()));
						Time puMin = new Time(pickupEndTime, margin, false);
						Time puMax = new Time(pickupEndTime, margin, true);
						twP = new TimeWindow(puMin, puMax);

						// Delivery
						Path pPickUpToDelivery = rsc.getRouting()
								.getShortestPath(slot.getLocation(),
										sDestination.getLocation());
						Time deliveryEndTime = new Time(pickupEndTime,
								new Time(pPickUpToDelivery.getCost()));
						deliveryEndTime = new Time(deliveryEndTime,
								handlingTimeFromGround);
						margin = new Time(
								(marginRate * deliveryEndTime.getInSec()));
						Time dMin = new Time(deliveryEndTime, margin, false);
						Time dMax = new Time(deliveryEndTime, margin, true);
						twD = new TimeWindow(dMin, dMax);

						if (puMax.compareTo(maxUnloadTime) > 0)
							maxUnloadTime = puMax;
						startTime = pickupEndTime;

						destinationReservation = slotReservations.get(
								sDestination.getId()).giveFreeReservation(c,
								twD);
					}
					// Reservation for the delivery
					// SlotReservations destinationReservations =
					// slotReservations.get(sDestination.getId());
					addReservation(destinationReservation);
					// Reservation update for the pickup
					// SlotReservations pickupReservations =
					// slotReservations.get(slot.getId());
					updateReservationMaxTime(c, slot, twP.getMax());

					// Prevent from time windows inversions between containers
					// of the same slot
					fromTime = twP.getMax();
					containersOnTrain.remove(c.getId());

					sbMissions.append("\t" + twP.toXML() + "\n\t" + twD.toXML()
							+ "\n");
					sbMissions.append("\t"
							+ destinationReservation.getContainerLocation()
									.toXML() + "\n</mission>\n");
					incrementProgressBar();
					nbMissionsCreated++;
					nbMissionsCreatedOverall++;
				}
			}
		}
		// nbMissionsCreatedOverall+=nbMissionsCreated;
		System.out.println("nb40Created : " + nbMissionsCreated);
		nbMissionsCreated = 0;
		for (Container c : containers20Feet) {
			Time fromTime = arrivalTime;
			Slot slot = Terminal.getInstance().getSlot(
					containerLocationOnTrain.get(c.getId()).getSlotId());

			if (nbMissionsCreated >= nbUnloadMissions20Feet) {
				break;
			}
			String mId = "unload_" + trainId + "_"
					+ (nbMissionsCreatedOverall + 1);
			sbMissions.append("<mission id='" + mId + "' container='"
					+ c.getId() + "' kind='" + MissionKinds.IN.getIntValue()
					+ "'>\n");

			Slot sDestination = null;
			SlotReservation destinationReservation = null;
			TimeWindow twP = null;
			TimeWindow twD = null;
			while (destinationReservation == null) {
				// Choose a destination in the stock area
				sDestination = stockSlots20Feet.get(r.nextInt(stockSlots20Feet
						.size()));
				Time duration = new Time(maxTimeForUnload, fromTime, false); // Change
																				// false
																				// into
																				// true
				System.err.println("Duration = " + duration);
				Time startTime = new Time(fromTime, new Time(
						(r.nextDouble() * duration.getInSec())), true);
				System.err.println("StartTime : " + startTime);
				// Pickup
				Path pStartToPickUp = rsc.getRouting().getShortestPath(
						rsc.getLocation(), slot.getLocation());
				Time pickupEndTime = new Time(startTime, new Time(
						pStartToPickUp.getCost()));
				pickupEndTime = new Time(pickupEndTime, handlingTimeFromTruck);
				Time margin = new Time((marginRate * pickupEndTime.getInSec()));
				Time puMin = new Time(pickupEndTime, margin, false);
				Time puMax = new Time(pickupEndTime, margin, true);
				twP = new TimeWindow(puMin, puMax);

				// Delivery
				Path pPickUpToDelivery = rsc.getRouting().getShortestPath(
						slot.getLocation(), sDestination.getLocation());
				Time deliveryEndTime = new Time(pickupEndTime, new Time(
						pPickUpToDelivery.getCost()));
				deliveryEndTime = new Time(deliveryEndTime,
						handlingTimeFromGround);
				margin = new Time((marginRate * deliveryEndTime.getInSec()));
				Time dMin = new Time(deliveryEndTime, margin, false);
				Time dMax = new Time(deliveryEndTime, margin, true);
				twD = new TimeWindow(dMin, dMax);

				if (puMax.compareTo(maxUnloadTime) > 0)
					maxUnloadTime = puMax;
				startTime = pickupEndTime;

				destinationReservation = slotReservations.get(
						sDestination.getId()).giveFreeReservation(c, twD);
			}
			// Reservation for the delivery
			// SlotReservations destinationReservations =
			// slotReservations.get(sDestination.getId());
			addReservation(destinationReservation);
			// Reservation update for the pickup
			// SlotReservations pickupReservations =
			// slotReservations.get(slot.getId());
			updateReservationMaxTime(c, slot, twP.getMax());

			// Prevent from time windows inversions between containers of the
			// same slot
			fromTime = twP.getMax();
			containersOnTrain.remove(c.getId());

			sbMissions.append("\t" + twP.toXML() + "\n\t" + twD.toXML() + "\n");
			sbMissions.append("\t"
					+ destinationReservation.getContainerLocation().toXML()
					+ "\n</mission>\n");
			incrementProgressBar();
			nbMissionsCreated++;
			nbMissionsCreatedOverall++;
		}
		System.out.println("nb20Created : " + nbMissionsCreated
				+ " nbUnloadingMissions = " + nbMissionsCreatedOverall);
		// ------ END OF UNLOADING ------

		// Compute load missions
		System.out.println("Computing load missions !");

		ArrayList<Mission> reloadingMissions = new ArrayList<Mission>(
				nbReloadMissions20Feet + nbReloadMissions40Feet
						+ nbReloadMissions45Feet);

		System.out.println("Nb45Feet : " + nbReloadMissions45Feet
				+ " Nb40Feet : " + nbReloadMissions40Feet + " Nb20Feet : "
				+ nbReloadMissions20Feet);

		nbMissionsCreated = 0;
		nbMissionsCreatedOverall = 0;
		for (Slot slot : stockSlots45Feet) {
			Time fromTime = maxUnloadTime;

			int maxLevel = slot.getLevels().size() - 1;

			if (nbMissionsCreated >= nbReloadMissions45Feet) {
				break;
			}

			for (int i = maxLevel; i >= 0
					&& nbMissionsCreated < nbReloadMissions45Feet; i--) {

				Level l = slot.getLevels().get(i);
				nbMissionsCreated++;
				nbMissionsCreatedOverall++;
				String mId = "reload_" + trainId + "_"
						+ nbMissionsCreatedOverall;
				if (l.getTEU() == l.getMaxTeu()) {

					i--;
					if (i > 0) {
						l = slot.getLevels().get(i);
					}
				}
				Container c = l.getFirstContainer();

				sbMissions.append("<mission id='" + mId + "' container='"
						+ c.getId() + "' kind='"
						+ MissionKinds.OUT.getIntValue() + "'>\n");

				Slot sDestination = null;
				SlotReservation destinationReservation = null;
				TimeWindow twP = null;
				TimeWindow twD = null;
				while (destinationReservation == null) {
					// Choose a destination in the stock area
					sDestination = slots45Feet
							.get(r.nextInt(slots45Feet.size()));
					// Pickup
					Path pStartToPickUp = rsc.getRouting().getShortestPath(
							rsc.getLocation(), c.getLocation());
					Time d1 = new Time(pStartToPickUp.getCost());
					// Delivery
					Path pPickUpToDelivery = rsc.getRouting().getShortestPath(
							c.getLocation(), sDestination.getLocation());
					Time d2 = new Time(pPickUpToDelivery.getCost());

					Time d1d2 = new Time(d1, d2);
					d1d2 = new Time(d1d2, handlingTimeFromGround);
					d1d2 = new Time(d1d2, handlingTimeFromTruck);

					Time duration = new Time(new Time(maxTime, d1d2, false),
							fromTime, false); // And again and again
					Time startTime = new Time(fromTime, new Time(
							(r.nextDouble() * duration.getInSec())));
					Time pickupEndTime = new Time(startTime, d1);
					pickupEndTime = new Time(pickupEndTime,
							handlingTimeFromTruck);
					Time margin = new Time((marginRate * d1.getInSec()));
					Time puMin = new Time(pickupEndTime, margin, false);
					Time puMax = new Time(pickupEndTime, margin, true);
					twP = new TimeWindow(puMin, puMax);

					Time deliveryEndTime = new Time(pickupEndTime, d2);
					deliveryEndTime = new Time(deliveryEndTime,
							handlingTimeFromGround);

					margin = new Time((marginRate * d2.getInSec()));
					Time dMin = new Time(deliveryEndTime, margin, false);
					Time dMax = new Time(deliveryEndTime, margin, true);
					twD = new TimeWindow(dMin, dMax);
					if (twD.getMax().toStep() > maxTime.toStep())
						System.out
								.println("MAX TIME OVERSPENT FOR TRAIN RELOADING MISSION CONCERNING "
										+ c.getId() + " " + twD.getMax());
					else {
						startTime = pickupEndTime;
						destinationReservation = slotReservations.get(
								sDestination.getId()).giveFreeReservation(c,
								twD);
						// Prevent from time windows inversions between
						// containers of the same slot
						fromTime = twP.getMax();
					}
				}

				Mission m = new Mission(mId, MissionKinds.OUT.getIntValue(),
						twP, twD, c.getId(),
						destinationReservation.getContainerLocation());
				reloadingMissions.add(m);

				// Reservation for the delivery
				addReservation(destinationReservation);
				// Reservation update for the pickup
				updateReservationMaxTime(c, slot, twP.getMax());

				// Prevent from time windows inversions between containers of
				// the same slot
				// fromTime = twP.getMax();
				containersOnTrain.put(c.getId(), c);
				containersOUT.put(c.getId(), c.getId());
				sbMissions.append("\t" + twP.toXML() + "\n\t" + twD.toXML()
						+ "\n");
				sbMissions.append("\t"
						+ destinationReservation.getContainerLocation().toXML()
						+ "\n</mission>\n");
				incrementProgressBar();
			}
		}
		System.out
				.println(nbMissionsCreated + " missions of 45 feet created !");
		nbMissionsCreated = 0;
		for (Slot slot : stockSlots40Feet) {
			// First Step : find a container in the yard
			Time fromTime = maxUnloadTime;
			int maxLevel = slot.getLevels().size() - 1;

			if (nbMissionsCreated >= nbReloadMissions40Feet) {
				break;
			}

			for (int i = maxLevel; i >= 0
					&& nbMissionsCreated < nbReloadMissions40Feet; i--) {
				Level l = slot.getLevels().get(i);

				// if(l.getTEU() < l.getMaxTeu()) {
				// If there is a container at this level
				if (l.getTEU() > 0) {
					Container c = l.getFirstContainer();
					if (c.getTEU() == ContainerKind
							.getTeu(Container.TYPE_40_Feet)) {
						// Next Step : find a destination slot
						Slot sDestination = null;
						SlotReservation destinationReservation = null;
						TimeWindow twP = null;
						TimeWindow twD = null;
						while (destinationReservation == null) {

							// Choose a destination in the train lanes
							sDestination = slots40Feet.get(r
									.nextInt(slots40Feet.size()));
							// Pickup
							Path pStartToPickUp = rsc.getRouting()
									.getShortestPath(rsc.getLocation(),
											c.getLocation());
							Time d1 = new Time(pStartToPickUp.getCost());
							// Delivery
							Path pPickUpToDelivery = rsc.getRouting()
									.getShortestPath(c.getLocation(),
											sDestination.getLocation());
							Time d2 = new Time(pPickUpToDelivery.getCost());

							Time d1d2 = new Time(d1, d2);
							d1d2 = new Time(d1d2, handlingTimeFromGround);
							d1d2 = new Time(d1d2, handlingTimeFromTruck);

							Time duration = new Time(new Time(maxTime, d1d2,
									false), fromTime, false); // And again and
																// again
							Time startTime = new Time(fromTime, new Time(
									(r.nextDouble() * duration.getInSec())));
							Time pickupEndTime = new Time(startTime, d1);
							pickupEndTime = new Time(pickupEndTime,
									handlingTimeFromTruck);
							Time margin = new Time((marginRate * d1.getInSec()));
							Time puMin = new Time(pickupEndTime, margin, false);
							Time puMax = new Time(pickupEndTime, margin, true);
							twP = new TimeWindow(puMin, puMax);

							Time deliveryEndTime = new Time(pickupEndTime, d2);
							deliveryEndTime = new Time(deliveryEndTime,
									handlingTimeFromGround);

							margin = new Time((marginRate * d2.getInSec()));
							Time dMin = new Time(deliveryEndTime, margin, false);
							Time dMax = new Time(deliveryEndTime, margin, true);
							twD = new TimeWindow(dMin, dMax);
							if (twD.getMax().toStep() > maxTime.toStep()) {
								System.err
										.println("MAX TIME OVERSPENT FOR TRAIN RELOADING MISSION CONCERNING "
												+ c.getId()
												+ " "
												+ twD.getMax());
							} else {
								startTime = pickupEndTime;
								destinationReservation = slotReservations.get(
										sDestination.getId())
										.giveFreeReservation(c, twD);
								// Prevent from time windows inversions between
								// containers of the same slot
								fromTime = twP.getMax();
							}

						}
						nbMissionsCreated++;
						nbMissionsCreatedOverall++;
						String mId = "reload_" + trainId + "_"
								+ nbMissionsCreatedOverall;
						sbMissions.append("<mission id='" + mId
								+ "' container='" + c.getId() + "' kind='"
								+ MissionKinds.OUT.getIntValue() + "'>\n");
						Mission m = new Mission(mId,
								MissionKinds.OUT.getIntValue(), twP, twD,
								c.getId(),
								destinationReservation.getContainerLocation());
						reloadingMissions.add(m);

						// Reservation for the delivery
						addReservation(destinationReservation);
						// Reservation update for the pickup
						updateReservationMaxTime(c, slot, twP.getMax());

						// Prevent from time windows inversions between
						// containers of the same slot
						// fromTime = twP.getMax();
						containersOnTrain.put(c.getId(), c);
						containersOUT.put(c.getId(), c.getId());
						sbMissions.append("\t" + twP.toXML() + "\n\t"
								+ twD.toXML() + "\n");
						sbMissions.append("\t"
								+ destinationReservation.getContainerLocation()
										.toXML() + "\n</mission>\n");
						incrementProgressBar();
					}
				}
			}
		}
		System.out
				.println(nbMissionsCreated + " missions of 40 feet created !");
		nbMissionsCreated = 0;
		Container[] tContainers = Terminal.getInstance().getContainersArray();
		boolean noMoreSpace = false;

		while (nbMissionsCreated < nbReloadMissions20Feet && !noMoreSpace) {
			Time fromTime = maxUnloadTime;
			TimeWindow tw20Feet = new TimeWindow(fromTime, maxTime);
			if (fromTime.toStep() > maxTime.toStep()) {
				tw20Feet = new TimeWindow(fromTime, fromTime);
				System.out.println("TW 20 FEET = [fromTime , fromTime] = ["
						+ fromTime + " , " + fromTime + "]");
			}

			Container c = null;
			Slot slot = null;
			while (c == null && tContainers.length > 0) {
				c = tContainers[r.nextInt(tContainers.length)];
				if (c.getDimensionType() != Container.TYPE_20_Feet
						|| Terminal.getInstance()
								.getBlock(c.getContainerLocation().getPaveId())
								.getType() != BlockType.YARD)
					c = null;
				else {
					slot = Terminal.getInstance().getSlot(
							c.getContainerLocation().getSlotId());
					if (!slotReservations.get(slot.getId()).isContainerFreeAt(
							c, tw20Feet))
						c = null;
				}
			}

			int maxLevel = slot.getLevels().size() - 1;

			if (nbMissionsCreated >= nbReloadMissions20Feet) {
				System.out.println("Break 20 !");
				break;
			}

			for (int i = maxLevel; i >= 0
					&& nbMissionsCreated < nbReloadMissions20Feet
					&& !noMoreSpace; i--) {
				String mId = "reload_" + trainId + "_"
						+ (nbMissionsCreatedOverall + 1);
				// Choose a destination in the stock area
				Slot sDestination = null;
				int n20Feet = 0;
				int n40Feet = 0;
				int n45Feet = 0;
				SlotReservation destinationReservation = null;
				TimeWindow twP = null;
				TimeWindow twD = null;

				while (destinationReservation == null) {
					if (n20Feet < slots20Feet.size())
						sDestination = slots20Feet.get(n20Feet++);
					else if (n40Feet < slots40Feet.size())
						sDestination = slots40Feet.get(n40Feet++);
					else if (n45Feet < slots45Feet.size())
						sDestination = slots45Feet.get(n45Feet++);
					else {
						System.out.println("No slot available !");
						noMoreSpace = true;
						break;
					}

					if (Terminal
							.getInstance()
							.getBlock(
									((Bay) sDestination.getLocation().getRoad())
											.getPaveId()).getType() != BlockType.RAILWAY) {
						System.out.println("Pave " + sDestination.getPaveId()
								+ " is not a train pave !!!");
					} else
						System.out.println("Pave " + sDestination.getPaveId()
								+ " is a train pave and contains lane "
								+ sDestination.getLocation().getRoad().getId()
								+ " for slot " + sDestination.getId());
					// Pickup
					Path pStartToPickUp = rsc.getRouting().getShortestPath(
							rsc.getLocation(), c.getLocation());
					Time d1 = new Time(pStartToPickUp.getCost());
					// Delivery
					Path pPickUpToDelivery = rsc.getRouting().getShortestPath(
							c.getLocation(), sDestination.getLocation());
					Time d2 = new Time(pPickUpToDelivery.getCost());

					Time d1d2 = new Time(d1, d2);
					d1d2 = new Time(d1d2, handlingTimeFromGround);
					d1d2 = new Time(d1d2, handlingTimeFromTruck);

					Time duration = new Time(new Time(maxTime, d1d2, false),
							fromTime, false); // And again and again
					Time startTime = new Time(fromTime, new Time(
							(r.nextDouble() * duration.getInSec())));
					Time pickupEndTime = new Time(startTime, d1);
					pickupEndTime = new Time(pickupEndTime,
							handlingTimeFromTruck);
					Time margin = new Time((marginRate * d1.getInSec()));
					Time puMin = new Time(pickupEndTime, margin, false);
					Time puMax = new Time(pickupEndTime, margin, true);
					twP = new TimeWindow(puMin, puMax);

					Time deliveryEndTime = new Time(pickupEndTime, d2);
					deliveryEndTime = new Time(deliveryEndTime,
							handlingTimeFromGround);

					margin = new Time((marginRate * d2.getInSec()));
					Time dMin = new Time(deliveryEndTime, margin, false);
					Time dMax = new Time(deliveryEndTime, margin, true);
					twD = new TimeWindow(dMin, dMax);
					if (twD.getMax().toStep() > maxTime.toStep())
						System.out
								.println("MAX TIME OVERSPENT FOR TRAIN RELOADING MISSION CONCERNING "
										+ c.getId() + " " + twD.getMax());
					else {
						startTime = pickupEndTime;
						destinationReservation = slotReservations.get(
								sDestination.getId()).giveFreeReservation(c,
								twD);
						// Prevent from time windows inversions between
						// containers of the same slot
						fromTime = twP.getMax();

					}
				}
				// if(destinationReservation!=null){
				sbMissions.append("<mission id='" + mId + "' container='"
						+ c.getId() + "' kind='"
						+ MissionKinds.OUT.getIntValue() + "'>\n");
				// Reservation for the delivery
				addReservation(destinationReservation);
				// Reservation update for the pickup
				updateReservationMaxTime(c, slot, twP.getMax());

				Mission m = new Mission(mId, MissionKinds.OUT.getIntValue(),
						twP, twD, c.getId(),
						destinationReservation.getContainerLocation());
				reloadingMissions.add(m);

				containersOnTrain.put(c.getId(), c);
				containersOUT.put(c.getId(), c.getId());
				sbMissions.append("\t" + twP.toXML() + "\n\t" + twD.toXML()
						+ "\n");
				sbMissions.append("\t"
						+ destinationReservation.getContainerLocation().toXML()
						+ "\n</mission>\n");
				nbMissionsCreated++;
				nbMissionsCreatedOverall++;

				incrementProgressBar();
			}

		}
		System.out
				.println(nbMissionsCreated + " missions of 20 feet created !");
		// ------ END OF RELOADING ------

		/* Computing VehicleOut */
		// The vehicle can leave when the last mission is over
		Time maxDTime = new Time(0);
		for (Mission m : reloadingMissions) {
			if (m.getDeliveryTimeWindow().getMax().compareTo(maxDTime) > 0)
				maxDTime = m.getDeliveryTimeWindow().getMax();
		}

		sbMissions.append("<event time='" + maxDTime
				+ "' type='vehicleOut' id='train_" + trainsCount + "' lanes='");
		for (int i = 0; i < lanesIds.size(); i++) {
			sbMissions.append(lanesIds.get(i));
			if (i < lanesIds.size() - 1)
				sbMissions.append(",");
			else
				sbMissions.append("'>\n");
		}
		// Containers onboard before leaving :
		for (String cID : containersOnTrain.keySet()) {
			sbMissions.append("\t<container id='" + cID + "'/>\n");
		}
		sbMissions.append("</event>\n");

		trainsCount++;
		// ------ END OF VEHICLE OUT ------

		// WRITING XML FILES !
		/*
		 * File tmpFile = new File("tmpContainers.xml"); File contFile = new
		 * File(containersFile); Scanner sc; try { sc = new Scanner(contFile);
		 * PrintWriter pw = new PrintWriter(tmpFile); while(sc.hasNextLine()){
		 * String nl = sc.nextLine(); if(!nl.contains("</document>")){
		 * pw.append(nl+"\n"); } } pw.flush(); sc.close();
		 * pw.append(sb.toString()); pw.append("</document>\n"); pw.flush();
		 * pw.close(); tmpFile.renameTo(contFile); } catch
		 * (FileNotFoundException e) { e.printStackTrace(); }
		 */

		File tmpFile = new File("tmpMissions.xml");
		File missFile = new File(missionsFile);

		try {
			PrintWriter pw = new PrintWriter(tmpFile);
			pw.append("<document>\n");
			pw.append(sbMissions.toString());
			pw.append("</document>\n");
			pw.flush();
			pw.close();
			tmpFile.renameTo(missFile);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			log.error(e.getMessage(), e);
		}
	}

	/*
	 * private String generateStockMission(String mId, Random r,
	 * HashMap<PaveType, ArrayList<String>> conteneurs, Time maxTime, double
	 * marginRate) , NoPathFoundException{ //Type //int type =
	 * MissionKinds.STAY; int type = MissionKinds.STAY.getIntValue();
	 * //Container Container c =
	 * rt.getContainer(conteneurs.get(PaveType.STOCK).get
	 * (r.nextInt(conteneurs.get(PaveType.STOCK).size())));
	 * 
	 * 
	 * //MissionLocation ArrayList<Lane> destLanes = new ArrayList<Lane>();
	 * if(type == MissionKinds.IN.getIntValue()){ for(String pId :
	 * Terminal.getInstance().paves.get(PaveType.STOCK).keySet()){ Pave p =
	 * Terminal.getInstance().paves.get(PaveType.STOCK).get(pId); List<Lane>
	 * lanes = p.getLanes(); for(Lane l : lanes) destLanes.add(l); } } else{
	 * for(String pId :
	 * Terminal.getInstance().paves.get(PaveType.STOCK).keySet()){ Pave p =
	 * Terminal.getInstance().paves.get(PaveType.STOCK).get(pId); List<Lane>
	 * lanes = p.getLanes(); for(Lane l : lanes) destLanes.add(l); } }
	 * ContainerLocation cl = null; Slot s = null; Lane l =null;
	 * ContainerLocation old = c.getContainerLocation(); Slot oldSlot =
	 * rt.getSlot(old.getSlotId()); int oldLevel = old.getLevel(); int oldAlign
	 * = old.getAlign();
	 * 
	 * boolean added = false; //On essaye Container c2 = null; try{ c2 =
	 * oldSlot.pop(c.getId());
	 * 
	 * }catch(Exception e1){ //Can't pop the container, then try with a new one
	 * !
	 * System.out.println("Can't pop "+c.getId()+" c2 == null ? = "+(c2==null)+
	 * " oldSlot.contains("+c.getId()+") ? = "+oldSlot.contains(c.getId()));
	 * return generateBoatMission(mId, r, conteneurs, maxTime, marginRate); }
	 * while(!added){ while(s == null){ l =
	 * destLanes.get(r.nextInt(destLanes.size())); List<Slot> lSlots =
	 * Terminal.getInstance().slots.get(l.getId()); s =
	 * lSlots.get(r.nextInt(lSlots.size())); if(s.getTEU() < c.getTEU()){ s =
	 * null; } }
	 * 
	 * for(int i=0 ; i<Slot.SLOT_MAX_LEVEL&&!added; i++){ for(int j=0;
	 * j<ContainerAlignment.values().length && !added; j++){ ContainerAlignment
	 * al = ContainerAlignment.values()[j]; if(s.canAddContainer(c, i,
	 * al.getValue())){ added = true; cl = new ContainerLocation(c.getId(),
	 * l.getPaveId(), l.getId(), s.getId(), i, al.getValue());
	 * //System.out.println
	 * ("SUCCES : "+c.getId()+" can be stocked on "+s.getId()
	 * +" at level "+i+" align "+al.getValue());
	 * //System.out.println("Content of "+s.getId()+" = "+s); } //else
	 * System.out
	 * .println("Can't add "+c.getId()+" on "+s.getId()+" level "+i+" align "
	 * +al); } } if(!added) s = null; } //on le remets à sa place Coordinates
	 * coords2 = null; try { coords2 = oldSlot.stockContainer(c, oldLevel,
	 * oldAlign); } catch (Exception e) {
	 * System.out.println("Can't repush "+c.getId
	 * ()+" coords2 == null ? = "+(coords2==null)); e.printStackTrace(); }
	 * //TimeWindows ! Time startTime = new
	 * Time(r.nextDouble()*maxTime.getInSec()+"s");
	 * 
	 * StraddleCarrier rsc = new
	 * ArrayList<StraddleCarrier>(Terminal.getInstance(
	 * ).straddleCarriers.values(
	 * )).get(r.nextInt(Terminal.getInstance().straddleCarriers.size()));
	 * //Pickup Path pStartToPickUp =
	 * rsc.getRouting().getShortestPath(rsc.getLocation(), c.getLocation());
	 * Time pickupEndTime = new Time(startTime, new
	 * Time(pStartToPickUp.getCost()+"s")); pickupEndTime = new
	 * Time(pickupEndTime, c.getHandlingTime()); Time margin = new
	 * Time((marginRate*pickupEndTime.getInSec())+"s"); Time puMin = new
	 * Time(pickupEndTime , margin, false); Time puMax = new Time(pickupEndTime
	 * , margin, true); TimeWindow twP = new TimeWindow(puMin, puMax);
	 * 
	 * //Delivery Path pPickUpToDelivery =
	 * rsc.getRouting().getShortestPath(c.getLocation(), s.getLocation()); Time
	 * deliveryEndTime = new Time(pickupEndTime, new
	 * Time(pPickUpToDelivery.getCost()+"s")); deliveryEndTime = new
	 * Time(deliveryEndTime, c.getHandlingTime()); margin = new
	 * Time((marginRate*deliveryEndTime.getInSec())+"s"); Time dMin = new
	 * Time(deliveryEndTime, margin, false); Time dMax = new
	 * Time(deliveryEndTime, margin, true); TimeWindow twD = new
	 * TimeWindow(dMin, dMax);
	 * 
	 * //Mission m = new Mission(mId, type, twP, twD, c.getId(), cl);
	 * StringBuilder sb = new StringBuilder();
	 * sb.append("<mission id=\""+mId+"\" container=\""
	 * +c.getId()+"\" kind=\""+type+"\">\n");
	 * sb.append("\t<timewindow start=\""+
	 * twP.getMin()+"\" end=\""+twP.getMax()+"\"/>\n");
	 * sb.append("\t<timewindow start=\""
	 * +twD.getMin()+"\" end=\""+twD.getMax()+"\"/>\n");
	 * sb.append("\t<containerLocation pave=\""
	 * +cl.getPaveId()+"\" lane=\""+cl.getLaneId
	 * ()+"\" slot=\""+cl.getSlotId()+"\" level=\""
	 * +cl.getLevel()+"\" align=\""+ContainerAlignment
	 * .getStringValue(cl.getAlign())+"\"/>\n"); sb.append("</mission>\n");
	 * return sb.toString(); }
	 */

	/**
	 * Used to run the terminal in order to create the environment of the data
	 * generation
	 */
	private void parseNetworkConfiguration(String terminalFile,
			String vehiclesFile, String containersFile) throws SAXException,
			IOException {
		saxReader = XMLReaderFactory
				.createXMLReader("com.sun.org.apache.xerces.internal.parsers.SAXParser");
		Terminal.getInstance().setSeed(seed);
		//RandomSpeed.setRandomGenerator(Terminal.getInstance().getRandom());

		TimeScheduler.getInstance().setSecondsPerStep(1.0);

		long now = System.currentTimeMillis();
		saxReader.setContentHandler(new XMLTerminalComponentParser());
		saxReader.parse(terminalFile);
		saxReader.parse(containersFile);
		long andNow = System.currentTimeMillis();
		System.out.println("Terminal built in " + (andNow - now) + "ms "
				+ Terminal.getInstance().getContainerNames().size());

		// Vehicles
		saxReader.setContentHandler(new XMLTerminalComponentParser());
		saxReader.parse(vehiclesFile);
	}

}
