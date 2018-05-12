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
import java.io.FileNotFoundException;
import java.lang.reflect.InvocationTargetException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JProgressBar;
import javax.swing.SwingUtilities;

import org.apache.log4j.Logger;
import org.com.DbMgr;
import org.com.dao.ContainerDAO;
import org.com.dao.EventDAO;
import org.com.dao.StraddleCarrierDAO;
import org.com.model.ContainerBean;
import org.com.model.EventBean;
import org.com.model.ScenarioBean;
import org.com.model.StraddleCarrierBean;
import org.conf.parameters.ReturnCodes;
import org.display.GraphicDisplay;
import org.display.MainFrame;
import org.exceptions.ContainerDimensionException;
import org.exceptions.EmptyLevelException;
import org.exceptions.NoPathFoundException;
import org.missions.Mission;
import org.missions.MissionKinds;
import org.routing.path.Path;
import org.routing.reservable.RDijkstraHandler;
import org.scheduling.LinearMissionScheduler;
import org.scheduling.MissionScheduler;
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
import org.time.TimeWindow;
import org.time.event.EventType;
import org.util.ContainerBICGenerator;
import org.util.Location;
import org.util.building.SimulationLoader;
import org.util.generators.parsers.ShipGenerationData;
import org.util.generators.parsers.StockGenerationData;
import org.util.generators.parsers.TrainGenerationData;
import org.util.generators.parsers.TruckGenerationData;
import org.vehicles.StraddleCarrier;

public class MissionsFileGenerator {
	private static final Logger log = Logger.getLogger(MissionsFileGenerator.class);

	// private static PrintWriter pw;
	// private XMLReader saxReader;

	public static final int WAITING_SLEEP_TIME = 20;

	// private static StraddleCarrier rsc;

	private ContainerBICGenerator bicGenerator;

	private Map<String, SlotReservations> slotReservations;
	private Map<String, List<ShipQuayReservation>> quaysReservations;
	private Map<String, String> containersOUT;

	private Time handlingTimeFromTruck;
	private Time handlingTimeFromGround;

	// private long seed = -1;

	private JProgressBar progress;
	private JDialog frame;
	private JFrame parentFrame;

	private ScenarioBean scenario;
	private long seed;
	private Collection<TrainGenerationData> trainsData;
	private Collection<TruckGenerationData> trucksData;
	private Collection<ShipGenerationData> shipsData;
	private Collection<StockGenerationData> stocksData;
	private static final String[] loadingSteps = { "parsing terminal", "initializing slots reservations", "initialazing BIC generator",
		"generating train missions", "generating trucks missions", "generating ships missions", "generating stock missions", "closing simulation" };

	private MissionsFileGenerator(ScenarioBean scenario, long seed) throws NoPathFoundException, ContainerDimensionException,
	EmptyLevelException {

		this.scenario = scenario;
		this.seed = seed;

	}

	public MissionsFileGenerator(ScenarioBean scenario, long seed, Collection<TrainGenerationData> trainsData, Collection<TruckGenerationData> trucksData,
			Collection<ShipGenerationData> shipsData, Collection<StockGenerationData> stocksData, MainFrame parent) throws NoPathFoundException, ContainerDimensionException,
			EmptyLevelException {
		this(scenario, seed);

		this.trainsData = trainsData;
		this.trucksData = trucksData;
		this.shipsData = shipsData;
		this.stocksData = stocksData;

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
			frame.setLocation(parentFrame.getLocation().x + (parentFrame.getSize().width / 2 - frame.getSize().width / 2),
					parentFrame.getLocation().y + (parentFrame.getSize().height / 2 - frame.getSize().height / 2));

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
								execute();
							} catch (NoPathFoundException e) {
								e.printStackTrace();
								log.error(e.getMessage(), e);
							} catch (ContainerDimensionException e) {
								e.printStackTrace();
								log.error(e.getMessage(), e);
							} catch (EmptyLevelException e) {
								e.printStackTrace();
								log.error(e.getMessage(), e);
							} catch (SQLException e) {
								e.printStackTrace();
								log.error(e.getMessage(), e);
							}
						}
					}.start();

				}
			});
			frame.setVisible(true);
		} else{
			try {
				execute();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}

	public MissionsFileGenerator(ScenarioBean scenario2, long seed2, TrainGenerationData trainGenerationData,
			TruckGenerationData truckGenerationData, ShipGenerationData shipGenerationData, StockGenerationData stockGenerationData) throws NoPathFoundException, ContainerDimensionException, EmptyLevelException {
		this(scenario2, seed2);
		trainsData = new ArrayList<>(1);
		trucksData = new ArrayList<>(1);
		shipsData = new ArrayList<>(1);
		stocksData = new ArrayList<>(1);
		trainsData.add(trainGenerationData);
		trucksData.add(truckGenerationData);
		shipsData.add(shipGenerationData);
		stocksData.add(stockGenerationData);
		try {
			execute();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	private void execute() throws NoPathFoundException,
	ContainerDimensionException, EmptyLevelException, SQLException {
		MissionScheduler.rmiBindingName = LinearMissionScheduler.rmiBindingName;
		// Load scenario
		try{
			SimulationLoader.getInstance().loadScenario(scenario, seed, null);
			if("2) Test n2".equals(scenario.getName())){
				System.err.println("DEBUG!");
			}
			for (Iterator<StraddleCarrierBean> itStraddleCarriers = StraddleCarrierDAO.getInstance(scenario.getId()).iterator(); itStraddleCarriers.hasNext();) {
				SimulationLoader.getInstance().loadStraddleCarrier(itStraddleCarriers.next());
			}
			for(StraddleCarrier vehicle : Terminal.getInstance().getStraddleCarriers()){
				vehicle.setRoutingAlgorithm(new RDijkstraHandler(vehicle));
			}
			for (Iterator<ContainerBean> itContainers = ContainerDAO.getInstance(scenario.getId()).iterator(); itContainers
					.hasNext();) {
				SimulationLoader.getInstance().loadContainer(itContainers.next());
			}
			incrementProgressBar();

			// pw = new PrintWriter(new File("reservations.dat"));

			// Forth Step : data initialization
			Map<BlockType, List<String>> sortedContainersMap = new HashMap<>(BlockType.values().length);
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

			initializeSlotReservations();

			incrementProgressBar();

			quaysReservations = new HashMap<>();
			containersOUT = new HashMap<String, String>();
			initializeBICGenerator();

			incrementProgressBar();

			log.info("Done.");

			handlingTimeFromGround = new Time(getAStraddleCarrier().getModel().getSpeedCharacteristics().getContainerHandlingTimeFromGround());
			handlingTimeFromTruck = new Time(getAStraddleCarrier().getModel().getSpeedCharacteristics().getContainerHandlingTimeFromTruck());

			// Fifth Step : TRAINS
			int i = 1;

			log.info("Generating Train Missions ... ");
			for (TrainGenerationData trainData : trainsData) {
				if(trainData != null){
					Time maxTimeInTime = new Time(trainData.getMaxTime());
					Time minTimeInTime = new Time(trainData.getMinTime());

					generateTrainMissions(sortedContainersMap, minTimeInTime, maxTimeInTime, trainData.getMarginRate(),
							trainData.getFullRate(), trainData.getAfterUnload(), trainData.getAfterReload(), "train" + i);
					i++;
				}
			}
			log.info("Generating Train Missions DONE !");
			incrementProgressBar();

			// Sixth Step : TRUCKS
			log.info("Generating Truck Missions ... ");
			for (TruckGenerationData truckData : trucksData) {
				if(truckData != null){
					Time minTimeInTime = new Time(truckData.getMinTime());
					Time maxTimeInTime = new Time(truckData.getMaxTime());
					Time avgTruckTimeBeforeLeavingInTime = new Time(truckData.getAvgTruckTimeBeforeLeaving());
					generateTrucksMissions(truckData.getNb(), truckData.getRateComeEmpty(), truckData.getRateLeaveEmpty(),
							sortedContainersMap, minTimeInTime, maxTimeInTime, avgTruckTimeBeforeLeavingInTime, truckData.getGroupID());
				}
			}
			log.info("Generating Truck Missions DONE !");

			incrementProgressBar();

			// Seventh Step : SHIPS
			log.info("Generating Ship Missions ... ");
			for (ShipGenerationData shipData : shipsData) {
				if(shipData != null){
					Time maxArrivalTime = new Time(shipData.getMaxArrivalTime());
					Time maxDepartureTime = new Time(shipData.getMaxDepartureTime());
					Time minimalBerthTimeLength = new Time(shipData.getMinBerthTimeLength());
					Time timePerContainerOperation = new Time(shipData.getTimePerContainerOperation());

					generateShipsMissions(shipData.getMinTeuCapacity(), shipData.getMaxTeuCapacity(), shipData.getFullRate(),
							shipData.getTwentyFeetRate(), shipData.getFortyFeetRate(), shipData.getCapacityFactor(), maxArrivalTime, minimalBerthTimeLength,
							maxDepartureTime, timePerContainerOperation, shipData.getAfterUnload(), shipData.getAfterReload(), shipData.getMarginRate());
				}
			}
			log.info("Generating Ship Missions DONE !");
			incrementProgressBar();

			// Eighth Step : STOCK
			log.info("Generating Stock Missions ... ");
			for (StockGenerationData stockData : stocksData) {
				if(stockData != null){
					generateStocksMissions(stockData.getNb(), new Time(stockData.getMinTime()), new Time(stockData.getMaxTime()),
							new Time(stockData.getMarginTime()), stockData.getGroupID(), sortedContainersMap);
				}
			}
			log.info("Generating Stock Missions DONE !");

			incrementProgressBar();

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
							frame.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
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
		} finally {
			SimulationLoader.closeInstance();
		}
	}

	/*
	 * public MissionsFileGenerator(final String localHostName, final String
	 * configFile, final long seed) throws SAXException, IOException,
	 * NoPathFoundException, ContainerDimensionException, EmptyLevelException {
	 * this.seed = seed; execute(localHostName, configFile); }
	 */

	/*
	 * public MissionsFileGenerator(final String localHostName, final String
	 * configFile) throws SAXException, IOException, NoPathFoundException,
	 * ContainerDimensionException, EmptyLevelException { execute(localHostName,
	 * configFile); }
	 */



	public void destroyUnthreaded() {
		bicGenerator = null;
		containersOUT = null;
		handlingTimeFromGround = null;
		handlingTimeFromTruck = null;
		quaysReservations = null;
		slotReservations = null;
	}

	public void destroy() {
		new Thread() {
			public void run() {
				bicGenerator = null;
				containersOUT = null;
				handlingTimeFromGround = null;
				handlingTimeFromTruck = null;
				quaysReservations = null;
				slotReservations = null;
				try {
					DbMgr.getInstance().getConnection().commit();
				} catch (SQLException e) {
					log.error(e.getMessage(),e);
				}
			}
		}.start();

	}

	private void initializeBICGenerator() {
		bicGenerator = new ContainerBICGenerator(0, Terminal.getInstance().getContainerNames());
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
						Container container = Terminal.getInstance().getContainer(contID);
						SlotReservation sr = new SlotReservation(Terminal.getInstance().getContainer(contID), s, new TimeWindow(new Time(0),
								new Time(Time.MAXTIME)), container.getContainerLocation().getAlign(), l);
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
			Slot origin = Terminal.getInstance().getSlot(c.getContainerLocation().getSlotId());
			// TODO check if we can take the avg between TW.min and TW.max...
			updateReservationMaxTime(c, origin, m.getPickupTimeWindow().getMax());

			Slot destination = Terminal.getInstance().getSlot(m.getDestination().getSlotId());
			// SlotReservations destinationsResas =
			// slotReservations.get(destination.getId());
			SlotReservation destResa = new SlotReservation(c, destination,
					new TimeWindow(m.getDeliveryTimeWindow().getMin(), new Time(Time.MAXTIME)), m.getDestination().getAlign(), destination.getLevel(m
							.getDestination().getLevel()));

			addReservation(destResa);
		}
	}

	private StraddleCarrier getAStraddleCarrier() {
		return Terminal.getInstance().getStraddleCarriers().get(Terminal.getInstance().getRandom().nextInt(Terminal.getInstance().getStraddleCarriersCount()));
	}

	private void generateStocksMissions(final int nb, Time minTime, Time maxTime, Time marginTime, String groupID,
			Map<BlockType, List<String>> sortedContainersMap) {
		List<String> possibleContIDs = new ArrayList<>(Terminal.getInstance().getContainerNames());
		List<Block> stockPaves = new ArrayList<>(Terminal.getInstance().getPaves(BlockType.YARD).values());

		incrementProgressBar();
		int i = 0;
		StraddleCarrier vehicle = getAStraddleCarrier();
		while (i < nb && !possibleContIDs.isEmpty()) {
			String contID = possibleContIDs.remove(Terminal.getInstance().getRandom().nextInt(possibleContIDs.size()));
			if (!containersOUT.containsKey(contID)) {
				Container container = Terminal.getInstance().getContainer(contID);
				Slot originSlot = Terminal.getInstance().getSlot(container.getContainerLocation().getSlotId());
				try {
					if (Terminal.getInstance().getBlock(originSlot.getPaveId()).getType() == BlockType.YARD) {
						Time P_min = new Time(minTime, new Time(Terminal.getInstance().getRandom().nextInt((int) (maxTime.toStep() - minTime.toStep()))));
						Path pDepotToPickUp;

						pDepotToPickUp = vehicle.getRouting().getShortestPath(
								new Location(vehicle.getSlot(), vehicle.getSlot().getCenterLocation(), false), originSlot.getLocation());
						Time travelTime = new Time(pDepotToPickUp.getCost());

						travelTime = new Time(travelTime, new Time((long) (Terminal.getInstance().getRandom().nextDouble() * marginTime.toStep())));
						Time P_max = new Time(P_min, new Time(travelTime, vehicle.getMaxContainerHandlingTime((Bay) originSlot.getLocation()
								.getRoad())));

						// Check if container will be on its slot at the pickup
						// time
						SlotReservations originReservations = slotReservations.get(originSlot.getId());
						if (originReservations == null)
							originReservations = new SlotReservations(originSlot);
						boolean free = originReservations.isContainerFreeAt(container, new TimeWindow(P_min, P_max));

						if (free) {
							Block pDestination = stockPaves.get(Terminal.getInstance().getRandom().nextInt(stockPaves.size()));
							Bay lDestination = pDestination.getLanes().get(Terminal.getInstance().getRandom().nextInt(pDestination.getLanes().size()));
							Slot destinationSlot = Terminal.getInstance().getSlots(lDestination.getId())
									.get(Terminal.getInstance().getRandom().nextInt(Terminal.getInstance().getSlots(lDestination.getId()).size()));

							// Compute Delivery TW
							Path pPickUpToDepot = vehicle.getRouting().getShortestPath(originSlot.getLocation(), destinationSlot.getLocation());
							travelTime = new Time(pPickUpToDepot.getCost());
							travelTime = new Time(travelTime, new Time((long) (Terminal.getInstance().getRandom().nextDouble() * marginTime.toStep())));
							Time handlingTime = vehicle.getMaxContainerHandlingTime(lDestination);
							Time D_min = new Time(P_min, new Time(travelTime, handlingTime));
							Time D_max = new Time(P_max, new Time(travelTime, handlingTime));

							SlotReservations reservations = slotReservations.get(destinationSlot.getId());
							if (reservations == null) {
								reservations = new SlotReservations(destinationSlot);
							}
							SlotReservation reservation = reservations.giveFreeReservation(container, new TimeWindow(D_min, new Time(Time.MAXTIME)));
							if (reservation != null) {
								// Mission ok !
								addReservation(reservation);
								updateReservationMaxTime(container, originSlot, P_max);

								Time knownTime = new Time(minTime, new Time((Terminal.getInstance().getRandom().nextInt((int) (Math.max(1, P_min.toStep() - minTime.toStep()))))));

								StringBuilder description = new StringBuilder();
								String id = groupID + "[" + (i++) + "]";
								description.append("id="+id+",kind="+MissionKinds.STAY.getIntValue()+",container="+ contID+",truckID=null");
								description.append(",twPMin="+P_min +",twPMax="+ P_max);
								description.append(",twDMin="+D_min +",twDMax=" + D_max);
								description.append(",slot=" + reservation.getSlot().getId()+",level="+reservation.getLevel().getLevelIndex()+",alignement="+reservation.getAlignment());

								EventBean mission = new EventBean();
								mission.setTime(knownTime);
								mission.setType(EventType.NewMission);
								mission.setDescription(description.toString());

								try {
									EventDAO.getInstance(scenario.getId()).insert(mission);
									incrementProgressBar();
								} catch (SQLException e) {
									log.error(e.getMessage(), e);
								}
							}
						}
					}
				} catch (NoPathFoundException e) {
					log.warn(e.getMessage(), e);
				}
			}
		}
		log.info(nb + " Stock Missions Generated !");
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
	 * @throws SQLException
	 */
	private void generateShipsMissions(int capaciteMin, int capaciteMax, double taux_remplissage, double rep20Pieds,
			double rep40Pieds, double capacityFactor, Time maxArrivalTime, Time minimalBerthTimeLength, Time maxDepartureTime,
			Time timePerContainerOperation, double taux_remplissage_apres_dechargement, double taux_remplissage_apres_chargement, double marginRate)
					throws ContainerDimensionException, SQLException {
		if (maxDepartureTime.toStep() <= maxArrivalTime.toStep()) {
			log.warn("Departure time must be greater than the arrival time. Check your parameters!");
		}
		if ((maxDepartureTime.toStep() - minimalBerthTimeLength.toStep()) < maxArrivalTime.toStep()) {
			log.warn("Not enough time for unloading and loading the ship. Check your parameters!");
		}

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
		int capacity = capaciteMin + Terminal.getInstance().getRandom().nextInt((capaciteMax + 1) - capaciteMin);

		log.info("capaciteMin = " + capaciteMin);
		log.info("capaciteMax = " + capaciteMax);
		log.info("capacite = " + capacity);

		int currentLoad = (int) (taux_remplissage * capacity);
		log.info("CurrentLoad = " + currentLoad);

		int nb20Pieds = (int) (rep20Pieds * currentLoad);
		log.info("nb20Pieds = " + nb20Pieds);

		int nb40Pieds = ((int) (rep40Pieds * currentLoad) / 2);
		log.info("nb40Pieds = " + nb40Pieds);

		int nb45Pieds = (int) (((1.0 - (rep20Pieds + rep40Pieds)) * currentLoad) / 2.25);
		log.info("nb45Pieds = " + nb45Pieds);

		int nbContainers = nb20Pieds + nb40Pieds + nb45Pieds;
		log.info("nbContainers = " + nbContainers);

		double shipLength = capacity / capacityFactor;
		log.info("ShipLength = " + shipLength);

		bicGenerator.generateMore(nbContainers);
		// ContainerBICGenerator bicGenerator = new
		// ContainerBICGenerator(nbContainers, rt.getContainerNames());

		StraddleCarrier vehicle = getAStraddleCarrier();

		List<String> quayPave = new ArrayList<>();
		List<String> quayIDs = new ArrayList<>(Terminal.getInstance().getPaves(BlockType.SHIP).keySet());
		for (String quayID : quayIDs) {
			quayPave.add(quayID);
		}

		// Find a quay
		String quayID = quayPave.get(Terminal.getInstance().getRandom().nextInt(quayPave.size()));
		Block p = Terminal.getInstance().getPaves(BlockType.SHIP).get(quayID);
		// Time
		Time arrivalTime = new Time(Terminal.getInstance().getRandom().nextInt((int) maxArrivalTime.toStep()));
		Time departureTime = new Time(arrivalTime, new Time(minimalBerthTimeLength.toStep()
				+ Terminal.getInstance().getRandom().nextInt((int) (maxDepartureTime.toStep() - arrivalTime.toStep() - minimalBerthTimeLength.toStep()))));
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
					newQuayID = quayPave.get(Terminal.getInstance().getRandom().nextInt(quayPave.size()));
				}
				quayID = newQuayID;
				p = Terminal.getInstance().getPaves(BlockType.SHIP).get(quayID);
			} else {
				ShipQuayReservation reservation = new ShipQuayReservation(quayID, tw, berthFrom, berthTo);
				List<ShipQuayReservation> reservations = quaysReservations.get(quayID);
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

		EventBean event = new EventBean();
		event.setTime(arrivalTime);
		event.setType(EventType.ShipIn);
		StringBuilder sbShipIn = new StringBuilder();

		sbShipIn.append("capacity=" + capacity + ",quay=" + quayID + ",from=" + berthFrom + ",to=" + berthTo+",containers={");

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
				* (int) (new Time(departureTime, arrivalTime, false).toStep() / timePerContainerOperation.toStep());
		log.info("MAXTEU to handle = " + maxTeuToHandle);
		log.info("A = " + new Time(departureTime, arrivalTime, false).toStep());
		log.info("B = " + timePerContainerOperation.toStep());
		log.info("A/B = " + (new Time(departureTime, arrivalTime, false).toStep() / timePerContainerOperation.toStep()));
		log.info("C = " + ((nSlots20 + nSlots40 + nSlots45) / 2));

		log.info("D = " + (int) (capacity * (taux_remplissage - taux_remplissage_apres_dechargement)));

		final int teuToUnload = Math.min((int) (capacity * (taux_remplissage - taux_remplissage_apres_dechargement)), maxTeuToHandle);
		log.info("TEU to unload = " + teuToUnload);

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
		final Map<String, Time> datesIn = new HashMap<>();

		while (teuUnloaded < teuToUnload) {
			int i = Terminal.getInstance().getRandom().nextInt(nb20FeetAfterUnload + nb40FeetAfterUnload + nb45FeetAfterUnload);
			double teu = ContainerKind.getTeu(Container.TYPE_45_Feet);

			if (i < nb20FeetAfterUnload) {
				teu = ContainerKind.getTeu(Container.TYPE_20_Feet);

			} else if (i < nb20FeetAfterUnload + nb40FeetAfterUnload) {
				teu = ContainerKind.getTeu(Container.TYPE_40_Feet);
			}

			// Find a free slot at a given time
			Time containerUnloadTime = new Time(arrivalTime, new Time(Terminal.getInstance().getRandom().nextInt((int) (departureTime.toStep() - arrivalTime.toStep()))));

			Slot s = availableSlots.get(Terminal.getInstance().getRandom().nextInt(availableSlots.size()));
			if (s.getTEU() >= teu) {
				// Is the slot s available at containerUnloadTime and for
				// timePerContainerOperation period ?
				TimeWindow unloadTW = new TimeWindow(containerUnloadTime, new Time(containerUnloadTime, timePerContainerOperation));

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
					else if (teu == ContainerKind.getTeu(Container.TYPE_40_Feet))
						nb40FeetAfterUnload--;
					else
						nb45FeetAfterUnload--;

					// Stocker le conteneur + son emplacement + sa TW (ordonné
					// par date)
					// SlotReservation scsr = new SlotReservation(contID, s,
					// unloadTW, ContainerAlignment.center.getValue(),
					// s.getLevel(0));
					ArrayList<SlotReservation> slotResa;
					if (slotsContainerreservations.containsKey(unloadTW.getMin())) {
						slotResa = slotsContainerreservations.get(unloadTW.getMin());
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
					EventBean containerIn = new EventBean();
					containerIn.setTime(unloadTW.getMin());
					containerIn.setType(EventType.NewShipContainer);
					StringBuilder sb = new StringBuilder();
					sb.append("id=" + contID + ",teu=" + teu+",quay="+quayID+",slot="+s.getId()+",level=0,alignement="+ContainerAlignment.center.ordinal()+",from="+berthFrom+",to="+berthTo);
					sbShipIn.append(contID+";");
					containerIn.setDescription(sb.toString());
					EventDAO.getInstance(scenario.getId()).insert(containerIn);					
					datesIn.put(contID, containerIn.getTime());
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
		String sShipIn = sbShipIn.toString();
		if(sShipIn.endsWith(";")){
			sShipIn = sShipIn.substring(0,sShipIn.length()-1);
		}
		sShipIn+="}";
		event.setDescription(sShipIn);
		EventDAO.getInstance(scenario.getId()).insert(event);
		//event.setDescription(sb.toString());

		//EventDAO.getInstance(scenario.getId()).insert(event);

		// STOCK SLOTS FOR DELIVERY
		Map<String, Block> stockPaves = Terminal.getInstance().getPaves(BlockType.YARD);
		List<Slot> stockSlots = new ArrayList<Slot>();
		List<String> stockLanes = new ArrayList<String>();
		for (Block stockPave : stockPaves.values()) {
			for (Bay l : stockPave.getLanes()) {
				stockLanes.add(l.getId());
				for (String slotID : Terminal.getInstance().getSlotNames(l.getId())) {
					stockSlots.add(Terminal.getInstance().getSlot(slotID));
				}
			}
		}

		incrementProgressBar();

		// 2nde étape : générer les missions de déchargement
		while (slotsContainerreservations.size() > 0) {
			log.info("Unload Missions to compute : " + slotsContainerreservations.size());

			List<SlotReservation> l = slotsContainerreservations.pollFirstEntry().getValue();
			for (SlotReservation scsr : l) {
				Container container = scsr.getContainer();

				Slot slotOrigin = scsr.getSlot();
				TimeWindow twP = scsr.getTW();

				// Gives an accurate TWP_Max
				List<SlotReservation> nextResa = slotReservations.get(slotOrigin.getId()).getHigherValue(twP.getMin());
				if (nextResa.size() > 0) {
					twP = new TimeWindow(twP.getMin(), nextResa.get(0).getTW().getMin());
				}
				if(datesIn.get(container.getId()).compareTo(twP.getMin()) != 0){
					System.err.println(datesIn.get(container.getId())+" != "+twP.getMin());
				}
				ok = false;
				ContainerLocation destination = null;
				SlotReservation res = null;
				while (!ok) {
					// Find a delivery slot
					String laneID = stockLanes.get(Terminal.getInstance().getRandom().nextInt(stockLanes.size()));
					Slot s = Terminal.getInstance().getSlot(
							Terminal.getInstance().getSlotNames(laneID)[Terminal.getInstance().getRandom().nextInt(Terminal.getInstance().getSlotNames(laneID).length)]);

					if (s.getTEU() >= container.getTEU()) {
						try {
							Path pPickUpToDelivery = vehicle.getRouting().getShortestPath(slotOrigin.getLocation(), s.getLocation());

							Time startTime = twP.getMin();
							Time TWD_Min = new Time(startTime, new Time(pPickUpToDelivery.getCost()));
							Time TWD_Max = new Time(twP.getMax(), new Time(pPickUpToDelivery.getCost()));

							SlotReservations slotReservation = slotReservations.get(s.getId());
							if (slotReservation == null) {
								slotReservation = new SlotReservations(s);
							}

							res = slotReservation.giveFreeReservation(container, new TimeWindow(TWD_Min, TWD_Max));
							if (res == null) {
								ok = false;
							} else {
								ok = true;
								Slot slotDestination = res.getSlot();
								destination = new ContainerLocation(res.getContainer().getId(), slotDestination.getPaveId(), slotDestination
										.getLocation().getRoad().getId(), slotDestination.getId(), res.getLevel().getLevelIndex(), res.getAlignment());
								SlotReservations reservations = slotReservations.get(slotDestination.getId());
								if (reservations == null)
									reservations = new SlotReservations(slotDestination);
								addReservation(res);
							}
						} catch (NoPathFoundException e) {
							log.error(e.getMessage(), e);
						}
					}
				}
				EventBean mission = new EventBean();
				mission.setTime(datesIn.get(container.getId()));
				mission.setType(EventType.NewMission);
				StringBuilder sb = new StringBuilder();
				// Générer la mission
				// slotOrigin -> slotDestination
				sb.append("id=unloadShip" + quayID + "[" + berthFrom + "-" + berthTo + "]With" + container.getId());
				sb.append(",container="+ container.getId());
				sb.append(",kind=" + MissionKinds.IN.getIntValue());
				sb.append(",twPMin="+ twP.getMin());
				sb.append(",twPMax=" + twP.getMax());
				sb.append(",twDMin=" + res.getTW().getMin());
				sb.append(",twDMax=" + res.getTW().getMax());
				sb.append(",slot="+destination.getSlotId());
				sb.append(",level="+destination.getLevel());
				sb.append(",alignement="+destination.getAlign());
				mission.setDescription(sb.toString());
				EventDAO.getInstance(scenario.getId()).insert(mission);

				incrementProgressBar();
			}
		}

		// 3e étape : générer les missions de chargement
		final int teuToLoad = Math.min((int) (capacity * (taux_remplissage_apres_chargement - taux_remplissage_apres_dechargement)), maxTeuToHandle);
		log.info("TEU to load = " + teuToLoad);
		currentLoad = currentLoad - teuToUnload;
		log.info("Current Load = " + currentLoad);
		// double teuToReload = teuToLoad-currentLoad; // ?

		// Loading containers
		double teuReloaded = 0;

		// List of possible containers available from a given time
		Map<String, Time> possibleContainers = new HashMap<>();
		List<Container> loadedContainers = new ArrayList<>();

		Map<String, Mission> missionSortedByContID = new HashMap<>();
		for (Mission m : Terminal.getInstance().getMissions()) {
			missionSortedByContID.put(m.getContainerId(), m);
		}

		for (Container c : Terminal.getInstance().getContainersArray()) {
			Block pave = Terminal.getInstance().getBlock(c.getContainerLocation().getPaveId());
			if (pave.getType() == BlockType.YARD) {

				if (missionSortedByContID.containsKey(c.getId())) {
					Mission m = missionSortedByContID.get(c.getId());
					if (m.getMissionKind().getIntValue() != MissionKinds.OUT.getIntValue()
							&& m.getMissionKind().getIntValue() != MissionKinds.IN_AND_OUT.getIntValue()) {
						possibleContainers.put(c.getId(), m.getDeliveryTimeWindow().getMax());
						log.info("c.getID = " + c.getId());
					}
				} else {
					possibleContainers.put(c.getId(), arrivalTime);
				}
			}
		}

		incrementProgressBar();

		while (teuReloaded < teuToLoad) {
			// Pick a container in the stock
			// we should verify that the chosen container is not concerned by
			// another mission...
			String contID = new ArrayList<String>(possibleContainers.keySet()).get(Terminal.getInstance().getRandom().nextInt(possibleContainers.size()));
			if (!containersOUT.containsKey(contID)) {
				Time availableTime = possibleContainers.get(contID);

				// TODO check if this container is not present at this time in
				// the terminal ! (if(container==null) container =
				// containerReservation.get(contID)...
				Container container = Terminal.getInstance().getContainer(contID);
				// TODO Idem ^
				Slot sPickup = Terminal.getInstance().getSlot(container.getContainerLocation().getSlotId());

				double teu = container.getTEU();

				// Compute TW belongs to [availableTime,departureTime-moveTime]
				Slot sDest = availableSlots.get(Terminal.getInstance().getRandom().nextInt(availableSlots.size()));

				// Delivery time window : [belongs to [arrivalTime ,
				// departureTime - e] , belongs to [twd_min , departureTime -
				// e]]
				Time TWD_min = new Time(arrivalTime, new Time(Terminal.getInstance().getRandom().nextInt((int) (departureTime.toStep() - arrivalTime.toStep()))));
				Time TWD_max = new Time(TWD_min, new Time(Terminal.getInstance().getRandom().nextInt((int) (departureTime.toStep() - TWD_min.toStep()))));
				TimeWindow twD = new TimeWindow(TWD_min, TWD_max);

				if (sDest.getTEU() >= teu) {
					// TWP_min = Belongs to [arrivalTime of this container
					// within the terminal , TWD_min -
					// travelTime(pickup,delivery)]
					// TWP_min = Max ( arrival time , [TWD_min-travelTime -
					// (Terminal.getInstance().getRandom().nextDouble()*margin) , TWD_min-traveltime];
					try {
						Path rp = vehicle.getRouting().getShortestPath(sPickup.getLocation(), sDest.getLocation());

						Time travelTime = new Time(rp.getCost());
						// t1 = TWD_min - travelTime
						Time t1 = new Time(TWD_min, travelTime, false);
						// t2 belongs to [t1 , t1 - marginTime[
						double margin = marginRate * Terminal.getInstance().getRandom().nextDouble();
						Time t2 = new Time(t1.toStep() * margin);
						// t3 = t1 - t2
						Time t3 = new Time(t1, t2, false);
						// T4 belongs to [t3,t1];
						Time t4 = new Time(t3, new Time(Terminal.getInstance().getRandom().nextDouble() * (new Time(t1, t3, false).toStep())));
						Time TWP_min = new Time(Math.max(availableTime.toStep(), t4.toStep()));

						// TWP_max belongs to [TWP_min,TWD_min-travelTime]
						// => TWP_max belongs to [TWP_min,t1]
						Time t5 = new Time(t1, TWP_min, false);
						Time t6 = new Time((Terminal.getInstance().getRandom().nextDouble() * t5.toStep()));
						Time TWP_max = new Time(TWP_min, t6);

						TimeWindow twP = new TimeWindow(TWP_min, TWP_max);

						SlotReservations srsP = slotReservations.get(sPickup.getId());
						if (srsP == null) {
							log.info("SRS is null ! c.id = " + container.getId() + " | " + sPickup.getId() + " " + container.getContainerLocation());
						}

						boolean containerReachable = slotReservations.get(sPickup.getId()).isContainerFreeAt(container, twP);
						if (containerReachable) {
							// Choose a free destination slot in the quay called
							// quayID and between berthFrom and berthTo
							// available between TWD_min and TWD_max
							SlotReservation srD = slotReservations.get(sDest.getId()).giveFreeReservation(container, twD);
							if (srD != null) {
								// Make the reservations
								// -> Free Pickup slot location from twP_max
								updateReservationMaxTime(container, sPickup, TWP_max);

								// -> Reserve sDest slot at TWD
								addReservation(srD);
								loadedContainers.add(container);
								containersOUT.put(container.getId(), container.getId());

								// Write the mission
								StringBuilder sb = new StringBuilder();
								sb.append("id=loadShip" + quayID + "[" + berthFrom + "-" + berthTo + "]With" + contID);
								sb.append(",container="+container.getId());
								sb.append(",kind="+MissionKinds.IN.getIntValue());
								sb.append(",twPMin="+twP.getMin());
								sb.append(",twPMax="+twP.getMax());
								sb.append(",twDMin="+twD.getMin());
								sb.append(",twDMax="+twD.getMax());
								sb.append(",slot="+sDest.getId());
								sb.append(",level="+srD.getLevel().getLevelIndex());
								sb.append(",alignement="+srD.getAlignment());
								EventBean mission = new EventBean();
								mission.setTime(arrivalTime);
								mission.setType(EventType.NewMission);
								mission.setDescription(sb.toString());
								EventDAO.getInstance(scenario.getId()).insert(mission);

								// Write the container out event
								sb = new StringBuilder();
								EventBean containerOut = new EventBean();
								containerOut.setTime(twD.getMax());
								containerOut.setType(EventType.ShipContainerOut);
								sb.append("containerId=" + container.getId() + ",slotId=" + sDest.getId());
								containerOut.setDescription(sb.toString());
								EventDAO.getInstance(scenario.getId()).insert(containerOut);

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

		incrementProgressBar();

		// 4e étape : générer le départ
		EventBean shipOut = new EventBean();
		shipOut.setTime(departureTime);
		shipOut.setType(EventType.ShipOut);
		StringBuilder sbShipOut = new StringBuilder();
		sbShipOut.append("capacity=" + capacity + ",quay=" + quayID + ",from=" + berthFrom + ",to=" + berthTo+",containers={");
		boolean first = true;
		for (Container cont : loadedContainers) {
			if(!first){
				sbShipOut.append(";");
			}
			else{
				first = false;
			}
			sbShipOut.append(cont.getId());
		}
		sbShipOut.append("}");
		shipOut.setDescription(sbShipOut.toString());
		EventDAO.getInstance(scenario.getId()).insert(shipOut);
	}

	private void generateTrucksMissions(final int nbTruckMissions, double rateComeEmpty, double rateLeaveEmpty,
			Map<BlockType, List<String>> containersByPaveType, Time minTime, Time maxTime, Time unloadingTime, String groupID)
					throws ContainerDimensionException, NoPathFoundException, SQLException {
		if (rateComeEmpty + rateLeaveEmpty > 1.0) {
			throw new IllegalArgumentException("RateComeEmpty + RateLeaveEmpty should be equal or less than 1.0 !!!");
		}
		StraddleCarrier vehicle = getAStraddleCarrier();
		// Buffer for writing into containersFile.xml
		// StringBuilder sbContainers = new StringBuilder();

		// Time to long
		long fromTimeInLong = minTime.toStep();
		long toTimeInLong = maxTime.toStep();

		Map<String, List<String>> slotsMapByLane = new HashMap<>();
		List<String> roadLanes = new ArrayList<String>();
		Map<String, Block> pavesRoad = Terminal.getInstance().getPaves(BlockType.ROAD);
		for (Block p : pavesRoad.values()) {
			for (Bay l : p.getLanes()) {
				roadLanes.add(l.getId());
				ArrayList<String> lSlots = new ArrayList<String>(Terminal.getInstance().getSlotNames(l.getId()).length);
				for (String s : Terminal.getInstance().getSlotNames(l.getId())) {
					lSlots.add(s);
				}
				slotsMapByLane.put(l.getId(), lSlots);
			}
		}

		List<String> stockLanes = new ArrayList<>();

		for (Block p : Terminal.getInstance().getPaves(BlockType.YARD).values()) {
			for (Bay l : p.getLanes()) {
				stockLanes.add(l.getId());
				ArrayList<String> lSlots = new ArrayList<String>(Terminal.getInstance().getSlotNames(l.getId()).length);
				for (String s : Terminal.getInstance().getSlotNames(l.getId())) {
					lSlots.add(s);
				}
				slotsMapByLane.put(l.getId(), lSlots);
			}
		}

		bicGenerator.generateMore(nbTruckMissions);

		incrementProgressBar();

		int trucksCount = 1;

		// GENERATE INCOMING MISSIONS : Truck comes full and leaves empty
		log.info("Comes full leaves empty : " + (nbTruckMissions * rateLeaveEmpty) + " missions to create !");
		for (int i = 0; i < nbTruckMissions * rateLeaveEmpty; i++) {
			// CREATE A TRUCK
			// String truckId = "truck_"+i;
			Time arrivalTime = new Time(fromTimeInLong + Terminal.getInstance().getRandom().nextInt((int) (toTimeInLong - fromTimeInLong)));

			double teu = ContainerKind.getTeu(Terminal.getInstance().getRandom().nextInt(ContainerKind.getNbOfTypes()));

			Container container = new Container(bicGenerator.giveMeBic().toString(), teu);

			// int absVal =
			// Math.abs((int)(unloadingTime.toStep()-container.getHandlingTime().toStep()));
			int absVal = Math.abs((int) (unloadingTime.toStep() - handlingTimeFromTruck.toStep()));

			Time t2 = null;
			if (absVal > 0){
				t2 = new Time(Terminal.getInstance().getRandom().nextInt(absVal));
			}
			else{
				t2 = new Time(0);
			}

			// Time departureTime = new Time(arrivalTime, new
			// Time(container.getHandlingTime(), t2));
			Time departureTime = new Time(arrivalTime, new Time(handlingTimeFromTruck, t2));

			// Find a location for the truck
			SlotReservation arrivalReservation = null;
			while (arrivalReservation == null) {
				String laneID = roadLanes.get(Terminal.getInstance().getRandom().nextInt(roadLanes.size()));
				List<String> slots = slotsMapByLane.get(laneID);
				Slot slot = Terminal.getInstance().getSlot(slots.get(Terminal.getInstance().getRandom().nextInt(slots.size())));

				SlotReservations reservations = slotReservations.get(slot.getId());
				if (reservations == null)
					reservations = new SlotReservations(slot);

				TimeWindow tw = new TimeWindow(arrivalTime, departureTime);
				arrivalReservation = reservations.giveFreeReservation(container, tw);
			}
			// Reserve the location
			addReservation(arrivalReservation);

			EventBean arrival = new EventBean();
			arrival.setTime(arrivalTime);
			arrival.setType(EventType.VehicleIn);

			StringBuilder sb = new StringBuilder();
			ContainerLocation cl = arrivalReservation.getContainerLocation();
			sb.append("id=truck_" + groupID + "[" + trucksCount + "],lanes={" + arrivalReservation.getSlot().getLocation().getRoad().getId()
					+ "},containers:{id:" + container.getId() +";teu:" + container.getTEU()
					+ ";slot:" + cl.getSlotId()
					+ ";level:" + cl.getLevel()
					+ ";alignement:" + cl.getAlign()+"}"); 
			arrival.setDescription(sb.toString());
			EventDAO.getInstance(scenario.getId()).insert(arrival);

			// Create the stocking mision (from truck to stock)
			SlotReservation deliveryReservation = null;
			TimeWindow TWP = null;
			TimeWindow TWD = null;
			while (deliveryReservation == null) {
				// Find a destination in the stock
				Slot deliverySlot = pickASlot(stockLanes, slotsMapByLane, Terminal.getInstance().getRandom());
				if (deliverySlot.getTEU() >= container.getTEU()) {
					// Compute Pickup and Delivery TimeWindows
					TWP = arrivalReservation.getTW();
					Path pPickUpToDelivery = vehicle.getRouting().getShortestPath(arrivalReservation.getSlot().getLocation(),
							deliverySlot.getLocation());
					Time startTime = TWP.getMax();
					Time TWD_Min = new Time(startTime, new Time(pPickUpToDelivery.getCost()));
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
					SlotReservations deliveryReservations = slotReservations.get(deliverySlot.getId());
					if (deliveryReservations == null)
						deliveryReservations = new SlotReservations(deliverySlot);
					TimeWindow tw = new TimeWindow(TWD.getMin(), new Time(Time.MAXTIME));
					deliveryReservation = deliveryReservations.giveFreeReservation(container, tw);
					if (deliveryReservation == null) {
						log.info("Can't get a reservation for " + container.getId() + " at " + tw + " on " + deliverySlot.getId() + ". Retrying...");
					}
				}

			}

			addReservation(deliveryReservation);

			// If ok -> Write the mission
			String mId = "unload_truck_" + groupID + "[" + trucksCount + "]";

			// TODO Change the known time distribution
			Time missionKnownTime = new Time(minTime, new Time(Terminal.getInstance().getRandom().nextInt((int) (arrivalTime.toStep() - minTime.toStep()))));


			EventBean mission = new EventBean();
			mission.setTime(missionKnownTime);
			mission.setType(EventType.NewMission);
			sb = new StringBuilder();
			sb.append("id=" + mId + ",truck=truck_"+ groupID + "[" + trucksCount + "],container=" + container.getId()
					+ ",kind="+ MissionKinds.IN.getIntValue()
					+ ",twPMin="+TWP.getMin()+",twPMax="+TWP.getMax()
					+ ",twDMin="+TWD.getMin()+",twDMax="+TWD.getMax());
			mission.setDescription(sb.toString());
			EventDAO.getInstance(scenario.getId()).insert(mission);

			// Compute the truck departure time
			sb = new StringBuilder();
			sb.append("id=truck_" + groupID + "[" + trucksCount + "],lanes=" + arrivalReservation.getSlot().getLocation().getRoad().getId());
			EventBean vehicleOut = new EventBean();
			mission.setTime(departureTime);
			mission.setType(EventType.VehicleOut);
			mission.setDescription(sb.toString());
			EventDAO.getInstance(scenario.getId()).insert(vehicleOut);

			trucksCount++;
			incrementProgressBar();
			log.info("1 Mission Truck Comes Full and Leaves Empty DONE !");
		}

		// GENERATE OUTGOING MISSIONS : Truck comes empty and leaves full
		log.info("Comes empty leaves full : " + (nbTruckMissions * rateComeEmpty) + " missions to create !");
		for (int i = 0; i < nbTruckMissions * rateComeEmpty; i++) {
			// CREATE A TRUCK
			Time arrivalTime = new Time(fromTimeInLong + Terminal.getInstance().getRandom().nextInt((int) (toTimeInLong - fromTimeInLong)));

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
						containersByPaveType.get(BlockType.YARD).get(Terminal.getInstance().getRandom().nextInt(containersByPaveType.get(BlockType.YARD).size())));
				if (!containersOUT.containsKey(container.getId())) {
					Time t2 = new Time(0);
					// int absVal =
					// Math.abs((int)(unloadingTime.toStep()-container.getHandlingTime().toStep()));
					int absVal = Math.abs((int) (unloadingTime.toStep() - handlingTimeFromGround.toStep()));
					if (absVal > 0) {
						t2 = new Time(Terminal.getInstance().getRandom().nextInt(absVal));
					}
					// Time departureTime = new Time(arrivalTime, new
					// Time(container.getHandlingTime(), t2));
					Time departureTime = new Time(arrivalTime, new Time(handlingTimeFromGround, t2));
					TWD = new TimeWindow(arrivalTime, departureTime);

					// Choose a slot to park the truck
					deliverySlot = pickASlot(roadLanes, slotsMapByLane, Terminal.getInstance().getRandom());
					if (deliverySlot.getTEU() >= container.getTEU()) {
						reservationsDelivery = slotReservations.get(deliverySlot.getId());
						if (reservationsDelivery == null)
							reservationsDelivery = new SlotReservations(deliverySlot);

						if (reservationsDelivery.isSlotEmptyAt(TWD)) {
							// Validate the container
							pickupSlot = Terminal.getInstance().getSlot(container.getContainerLocation().getSlotId());
							try {
								Path pPickUpToDelivery = vehicle.getRouting().getShortestPath(pickupSlot.getLocation(), deliverySlot.getLocation());
								Time TWP_Max = new Time(TWD.getMin(), new Time(pPickUpToDelivery.getCost()), false);
								// Time TWP_Min = new
								// Time(TWP_Max,container.getHandlingTime(),false);
								Time TWP_Min = new Time(TWP_Max, handlingTimeFromGround, false);
								// if(unloadingTime.toStep()!=container.getHandlingTime().toStep()){
								if (unloadingTime.toStep() != handlingTimeFromGround.toStep()) {
									Time max = new Time(Math.max(unloadingTime.toStep(), handlingTimeFromGround.toStep()));
									Time min = new Time(Math.min(unloadingTime.toStep(), handlingTimeFromGround.toStep()));
									Time gap = new Time(Terminal.getInstance().getRandom().nextInt((int) (max.toStep() - min.toStep())));
									TWP_Min = new Time(TWP_Max, new Time(min, gap), false);
								}
								TWP = new TimeWindow(TWP_Min, TWP_Max);
								if (TWP.getMin().toStep() > TWP.getMax().toStep()) {
									log.error("MIN>MAX : " + TWP);
									System.exit(ReturnCodes.EXIT_ON_TIMEWINDOW_ERROR.getCode());
								}
								reservationsPickup = slotReservations.get(pickupSlot.getId());
								if (reservationsPickup == null)
									reservationsPickup = new SlotReservations(pickupSlot);
								if (reservationsPickup.isContainerFreeAt(container, TWP)) {
									ok = true;
								} else {
									if (TWP.getMin().toString().contains("-")) {
										log.error("HERE : " + TWP.toString() + " from time = " + TWP.getMin().getMinutes() + " "
												+ TWP.getMin().getSeconds());
									}
									log.info("Container " + container.getId() + " is not free at " + TWP + "! Retrying... ");
								}
							} catch (NoPathFoundException e) {
								ok = false;
							}
						} else
							log.info("Slot " + deliverySlot.getId() + " is not free at " + TWD + "! Retrying... ");

					}
				}
			}
			containersOUT.put(container.getId(), container.getId());
			// ------ Reservations --------
			// | Delivery
			SlotReservation deliveryReservation = reservationsDelivery.giveFreeReservation(container, TWD);
			addReservation(deliveryReservation);
			// | Pickup
			updateReservationMaxTime(container, pickupSlot, TWP.getMax());
			// ---------------------------------------------

			// ------ Writing the truck -------
			// | Arrival
			EventBean arrival = new EventBean();
			arrival.setTime(TWD.getMin());
			arrival.setType(EventType.VehicleIn);
			StringBuilder sb = new StringBuilder();
			sb.append("id=truck_" + groupID + "[" + trucksCount + "],lanes={" + deliveryReservation.getSlot().getLocation().getRoad().getId()+"}");
			arrival.setDescription(sb.toString());
			EventDAO.getInstance(scenario.getId()).insert(arrival);

			// | Departure
			EventBean departure = new EventBean();
			departure.setTime(TWD.getMax());
			departure.setType(EventType.VehicleOut);
			sb = new StringBuilder();
			sb.append("id=truck_" + groupID + "[" + trucksCount + "],lanes={" + deliveryReservation.getSlot().getLocation().getRoad().getId()
					+"},containers={"+container.getId()+"}");
			departure.setDescription(sb.toString());
			EventDAO.getInstance(scenario.getId()).insert(departure);

			// ---------------------------------------------

			// ------ Writing the mission --------
			String mId = "load_truck_" + groupID + "[" + trucksCount + "]";
			// | TODO Change the known time distribution
			Time missionKnownTime = new Time(minTime, new Time(Terminal.getInstance().getRandom().nextInt((int) (arrivalTime.toStep() - minTime.toStep()))));
			EventBean mission = new EventBean();
			mission.setType(EventType.NewMission);
			mission.setTime(missionKnownTime);

			sb = new StringBuilder();
			sb.append("id=" + mId + ",truck=truck_" + groupID + "[" + trucksCount + "],container=" + container.getId() 
					+ ",kind=" + MissionKinds.OUT.getIntValue()
					+ ",twPMin=" + TWP.getMin() + ",twPMax=" + TWP.getMax()
					+ ",twDMin=" + TWD.getMin() + ",twDMax=" + TWD.getMax()
					+ ",slot=" + deliveryReservation.getContainerLocation().getSlotId()
					+ ",level=" + deliveryReservation.getContainerLocation().getLevel()
					+ ",alignement=" + deliveryReservation.getAlignment());
			mission.setDescription(sb.toString());
			EventDAO.getInstance(scenario.getId()).insert(mission);

			trucksCount++;
			incrementProgressBar();
			// --------------------------------------------
			log.info("1 Mission Truck Comes Empty and Leaves Full DONE !");
		}

		// GENERATE OUTGOING MISSIONS : Truck comes full and leaves full
		//		log.info("Comes full leaves full : " + ((1 - (rateComeEmpty + rateLeaveEmpty)) * nbTruckMissions) + " missions to create !");
		//		for (int i = 0; i < (1 - (rateComeEmpty + rateLeaveEmpty)) * nbTruckMissions; i++) {
		//			// CREATE A TRUCK
		//			Time arrivalTime = new Time(fromTimeInLong + Terminal.getInstance().getRandom().nextInt((int) (toTimeInLong - fromTimeInLong)));
		//			// CREATE A BROUGHT CONTAINER
		//			double teu = ContainerKind.getTeu(Terminal.getInstance().getRandom().nextInt(ContainerKind.getNbOfTypes()));
		//			Container containerToUnload = new Container(bicGenerator.giveMeBic().toString(), teu);
		//
		//			// Time unloadEndTime = new Time(arrivalTime, new
		//			// Time(Math.max(containerToUnload.getHandlingTime().toStep(),
		//			// Terminal.getInstance().getRandom().nextInt((int)unloadingTime.toStep()))));
		//			Time unloadEndTime = new Time(arrivalTime, new Time(Math.max(handlingTimeFromTruck.toStep(), Terminal.getInstance().getRandom().nextInt((int) unloadingTime.toStep()))));
		//			TimeWindow unloadingTW_P = new TimeWindow(arrivalTime, unloadEndTime);
		//
		//			Time loadStartTime = new Time(unloadEndTime, new Time(Terminal.getInstance().getRandom().nextInt((int) unloadingTime.toStep())));
		//			// Time loadEndTime = new Time(loadStartTime, new
		//			// Time(Math.max(containerToUnload.getHandlingTime().toStep() ,
		//			// Terminal.getInstance().getRandom().nextInt((int)unloadingTime.toStep()))));
		//			Time loadEndTime = new Time(loadStartTime, new Time(Math.max(handlingTimeFromTruck.toStep(), Terminal.getInstance().getRandom().nextInt((int) unloadingTime.toStep()))));
		//			TimeWindow loadingTW_D = new TimeWindow(loadStartTime, loadEndTime);
		//
		//			TimeWindow truckTW = new TimeWindow(unloadingTW_P.getMin(), loadingTW_D.getMax());
		//			log.info("truck_" + groupID + "[" + trucksCount + "] UNLOADING TW = " + unloadingTW_P);
		//			log.info("truck_" + groupID + "[" + trucksCount + "] LOADING TW = " + loadingTW_D);
		//			boolean ok = false;
		//			Container containerToReload = null;
		//			SlotReservation truckReservation = null;
		//			SlotReservation containerToUnloadReservation = null;
		//			SlotReservations loadingPickupReservation = null;
		//			SlotReservation loadingDeliveryReservation = null;
		//			TimeWindow loadingTW_P = null;
		//			TimeWindow unloadingTW_D = null;
		//			// Check if the containerToReload will be ready
		//			Path pPickUpToDelivery = null;
		//
		//			while (!ok) {
		//				containerToReload = Terminal.getInstance().getContainer(
		//						containersByPaveType.get(BlockType.YARD).get(Terminal.getInstance().getRandom().nextInt(containersByPaveType.get(BlockType.YARD).size())));
		//				if (!containersOUT.containsKey(containerToReload.getId()) && !containerToReload.getId().equals(containerToUnload.getId())) {
		//					Slot pickupReloadSlot = Terminal.getInstance().getSlot(containerToReload.getContainerLocation().getSlotId());
		//
		//					// Find a slot for the truck
		//					truckReservation = null;
		//					while (truckReservation == null) {
		//						String laneID = roadLanes.get(Terminal.getInstance().getRandom().nextInt(roadLanes.size()));
		//						List<String> slots = slotsMapByLane.get(laneID);
		//						Slot slot = Terminal.getInstance().getSlot(slots.get(Terminal.getInstance().getRandom().nextInt(slots.size())));
		//						if (slot.getTEU() >= containerToUnload.getTEU()) {
		//							SlotReservations reservations = slotReservations.get(slot.getId());
		//							if (reservations == null)
		//								reservations = new SlotReservations(slot);
		//							if (reservations.isSlotEmptyAt(truckTW)) {
		//								truckReservation = reservations.giveFreeReservation(containerToUnload, truckTW);
		//							} else {
		//								log.info("Slot " + slot.getId() + " is not free at " + truckTW + "! Retrying... ");
		//							}
		//						}
		//					}
		//
		//					SlotReservations truckReservations = slotReservations.get(truckReservation.getSlot().getId());
		//
		//					// Path to go from the stock to the truck
		//					pPickUpToDelivery = vehicle.getRouting()
		//							.getShortestPath(pickupReloadSlot.getLocation(), truckReservation.getSlot().getLocation());
		//
		//					Time loadingTWP_max = new Time(loadingTW_D.getMin(), new Time(pPickUpToDelivery.getCost()), false);
		//					// TODO check if it is realisable (it doesn't take into
		//					// account the path from the depot to the location)
		//					// Time loadingTWP_min = new Time(loadingTWP_max,new
		//					// Time(containerToReload.getHandlingTime()));
		//					Time loadingTWP_min = new Time(loadingTWP_max, new Time(handlingTimeFromGround));
		//					// if(unloadingTime.toStep()!=containerToReload.getHandlingTime().toStep()){
		//					if (unloadingTime.toStep() != handlingTimeFromGround.toStep()) {
		//						// Time max = new Time(Math.max(unloadingTime.toStep(),
		//						// containerToReload.getHandlingTime().toStep()));
		//						Time max = new Time(Math.max(unloadingTime.toStep(), handlingTimeFromGround.toStep()));
		//						// Time min = new Time(Math.min(unloadingTime.toStep(),
		//						// containerToReload.getHandlingTime().toStep()));
		//						Time min = new Time(Math.min(unloadingTime.toStep(), handlingTimeFromGround.toStep()));
		//
		//						Time gap = new Time(Terminal.getInstance().getRandom().nextInt((int) (max.toStep() - min.toStep())));
		//						loadingTWP_min = new Time(loadingTWP_max, new Time(min, gap), false);
		//					}
		//					loadingTW_P = new TimeWindow(loadingTWP_min, loadingTWP_max);
		//					if (loadingTW_P.getMin().toStep() > loadingTW_P.getMax().toStep()) {
		//						log.fatal("2 MIN>MAX : " + loadingTW_P);
		//						System.exit(ReturnCodes.EXIT_ON_TIMEWINDOW_ERROR.getCode());
		//					}
		//					loadingPickupReservation = slotReservations.get(pickupReloadSlot.getId());
		//					if (loadingPickupReservation == null)
		//						loadingPickupReservation = new SlotReservations(pickupReloadSlot);
		//
		//					if (loadingPickupReservation.isContainerFreeAt(containerToReload, loadingTW_P)) {
		//						// We can load the container to deliver
		//						loadingDeliveryReservation = truckReservations.giveFreeReservation(containerToReload, loadingTW_D);
		//						if (loadingDeliveryReservation != null) {
		//							// We can deliver it
		//							ok = true;
		//						} else {
		//							log.info("Can't get a free reservation for " + containerToReload.getId() + " on " + pickupReloadSlot.getId() + " at "
		//									+ loadingTW_D + ". Retrying...");
		//						}
		//					} else {
		//						log.info("Slot " + pickupReloadSlot.getId() + " is not free at " + loadingTW_D + ". Retrying...");
		//					}
		//				}
		//			}
		//			containersOUT.put(containerToReload.getId(), containerToReload.getId());
		//			// Find a slot for the container to unload
		//			while (containerToUnloadReservation == null) {
		//				String laneID = stockLanes.get(Terminal.getInstance().getRandom().nextInt(stockLanes.size()));
		//				List<String> slots = slotsMapByLane.get(laneID);
		//				Slot slot = Terminal.getInstance().getSlot(slots.get(Terminal.getInstance().getRandom().nextInt(slots.size())));
		//
		//				SlotReservations reservations = slotReservations.get(slot.getId());
		//				if (reservations == null)
		//					reservations = new SlotReservations(slot);
		//
		//				// Compute unloadingTW_D
		//				// Path to go from the truck to the stock slot
		//				pPickUpToDelivery = vehicle.getRouting().getShortestPath(truckReservation.getSlot().getLocation(), slot.getLocation());
		//				Time unloadingTWD_min = new Time(unloadingTW_P.getMax(), new Time(pPickUpToDelivery.getCost()));
		//				// TODO check if it is clever !
		//				// Time unloadingTWD_max = new Time(unloadingTWD_min,
		//				// containerToUnload.getHandlingTime());
		//				Time unloadingTWD_max = new Time(unloadingTWD_min, handlingTimeFromGround);
		//				// if(unloadingTime.toStep()!=containerToUnload.getHandlingTime().toStep()){
		//				if (unloadingTime.toStep() != handlingTimeFromGround.toStep()) {
		//					// unloadingTWD_max = new Time(unloadingTWD_min, new
		//					// Time(containerToUnload.getHandlingTime(), new
		//					// Time(Terminal.getInstance().getRandom().nextInt(Math.abs((int)(unloadingTime.toStep()-containerToUnload.getHandlingTime().toStep()))))));
		//					unloadingTWD_max = new Time(unloadingTWD_min, new Time(handlingTimeFromGround, new Time(Terminal.getInstance().getRandom().nextInt(Math.abs((int) (unloadingTime
		//							.toStep() - handlingTimeFromGround.toStep()))))));
		//				}
		//
		//				unloadingTW_D = new TimeWindow(unloadingTWD_min, unloadingTWD_max);
		//
		//				TimeWindow tw = new TimeWindow(unloadingTWD_min, maxTime);
		//				containerToUnloadReservation = reservations.giveFreeReservation(containerToUnload, tw);
		//				if (containerToUnloadReservation == null) {
		//					log.info("Can't get a free reservation for " + containerToUnload.getId() + " on " + slot.getId() + " at " + tw + ". Retrying...");
		//				}
		//			}
		//
		//			// If ok then reserve !
		//			// | Truck reservation
		//			SlotReservations truckReservations = slotReservations.get(truckReservation.getSlot().getId());
		//			addReservation(truckReservation);
		//			updateReservationMaxTime(containerToUnload, truckReservations.getSlot(), unloadingTW_P.getMax());
		//
		//			// | Unload -> Stock : stock reservation
		//			SlotReservations containerToUnLoadReservations = slotReservations.get(containerToUnloadReservation.getSlot().getId());
		//			if (containerToUnLoadReservations == null)
		//				containerToUnLoadReservations = new SlotReservations(containerToUnloadReservation.getSlot());
		//			addReservation(containerToUnloadReservation);
		//			// | Stock -> Load : load reservation
		//			addReservation(loadingDeliveryReservation);
		//			// | Stock -> Load : stock update max time
		//			updateReservationMaxTime(containerToReload, loadingPickupReservation.getSlot(), loadingTW_P.getMax());
		//
		//			// If ok -> Write the mission
		//			// | Truck arrival
		//			EventBean arrival = new EventBean();
		//			arrival.setType(EventType.VehicleIn);
		//			arrival.setTime(truckReservation.getTW().getMin());
		//			StringBuilder sb = new StringBuilder();
		//			ContainerLocation cl = truckReservation.getContainerLocation();
		//			sb.append("id=truck_" + groupID + "[" + trucksCount + "],lanes={" + truckReservation.getSlot().getLocation().getRoad().getId()
		//					+ "},containers={id:"+containerToUnload.getId()+";teu:"+containerToUnload.getTEU()
		//					+ ";slot:" + cl.getSlotId()
		//					+ ";level:" + cl.getLevel()
		//					+ ";alignement:" + cl.getAlign()+"}");
		//			arrival.setDescription(sb.toString());
		//			EventDAO.getInstance(scenario.getId()).insert(arrival);
		//
		//			// | Unloading mission
		//			String mId = "unload_truck_" + groupID + "[" + trucksCount + "]";
		//			// TODO Change the known time distribution
		//			Time missionKnownTime = new Time(minTime, new Time(Terminal.getInstance().getRandom().nextInt((int) (truckReservation.getTW().getMin().toStep() - minTime.toStep()))));
		//			EventBean unloadMission = new EventBean();
		//			unloadMission.setType(EventType.NewMission);
		//			unloadMission.setTime(missionKnownTime);
		//			sb = new StringBuilder();
		//			sb.append("id=" + mId + ",truck=truck_" + groupID + "[" + trucksCount + "],container=" + containerToUnload.getId()
		//					+ ",kind=" + MissionKinds.IN.getIntValue()
		//					+ ",twPMin=" + unloadingTW_P.getMin() + ",twPMax=" + unloadingTW_P.getMax()
		//					+ ",twDMin=" + unloadingTW_D.getMin() + ",twDMax=" + unloadingTW_D.getMax() 
		//					+ ",slot=" + containerToUnloadReservation.getContainerLocation().getSlotId()
		//					+ ",level=" + containerToUnloadReservation.getContainerLocation().getLevel()
		//					+ ",alignement=" + containerToUnloadReservation.getContainerLocation().getAlign());
		//			unloadMission.setDescription(sb.toString());
		//			EventDAO.getInstance(scenario.getId()).insert(unloadMission);
		//
		//			// | Loading mission
		//			mId = "load_truck_" + groupID + "[" + trucksCount + "]";
		//			EventBean loadMission = new EventBean();
		//			loadMission.setType(EventType.NewMission);
		//			loadMission.setTime(missionKnownTime);
		//			sb = new StringBuilder();
		//			sb.append("id=" + mId + ",truck=truck_" + groupID + "[" + trucksCount + "],container=" + containerToReload.getId()
		//					+ ",kind=" + MissionKinds.OUT.getIntValue()
		//					+ ",twPMin=" + loadingTW_P.getMin() + ",twPMax=" + loadingTW_P.getMax()
		//					+ ",twDMin=" + loadingTW_D.getMin() + ",twDMax=" + loadingTW_D.getMax()
		//					+ ",slot=" + loadingDeliveryReservation.getContainerLocation().getSlotId()
		//					+ ",level=" + loadingDeliveryReservation.getContainerLocation().getLevel()
		//					+ ",alignement=" + loadingDeliveryReservation.getContainerLocation().getAlign());
		//			loadMission.setDescription(sb.toString());
		//			EventDAO.getInstance(scenario.getId()).insert(loadMission);
		//
		//			// | Vehicle departure
		//			EventBean departure = new EventBean();
		//			departure.setType(EventType.VehicleOut);
		//			departure.setTime(truckReservation.getTW().getMax());
		//			sb = new StringBuilder();
		//			sb.append("id=truck_" + groupID + "[" + trucksCount + "],lanes={" + truckReservation.getSlot().getLocation().getRoad().getId()
		//			+"},containers={" + containerToReload.getId()+"}");
		//			departure.setDescription(sb.toString());
		//			EventDAO.getInstance(scenario.getId()).insert(departure);
		//
		//			trucksCount++;
		//			incrementProgressBar();
		//			log.info("1 Mission Truck Comes Full and Leaves Full DONE !");
		//		}

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

	private Slot pickASlot(List<String> lanesID, Map<String, List<String>> slotMapByLane, Random r) {
		String lID = lanesID.get(Terminal.getInstance().getRandom().nextInt(lanesID.size()));
		List<String> slotList = slotMapByLane.get(lID);
		String slotID = slotList.get(Terminal.getInstance().getRandom().nextInt(slotList.size()));
		return Terminal.getInstance().getSlot(slotID);
	}

	private void updateReservationMaxTime(Container container, Slot slot, Time newMaxTime) {
		SlotReservations srs = slotReservations.get(slot.getId());
		srs.updateReservationMaxTime(container, newMaxTime);
		slotReservations.put(slot.getId(), srs);
		// pw.append("Reservation updated : \n" + srs + "\n");
		// pw.flush();
	}

	private void addReservation(SlotReservation reservation) {
		SlotReservations reservations = slotReservations.get(reservation.getSlot().getId());
		if (reservations == null)
			reservations = new SlotReservations(reservation.getSlot());
		reservations.addReservation(reservation);
		slotReservations.put(reservation.getSlot().getId(), reservations);
		// pw.append("Reservation added : \n" + reservations + "\n");
		// pw.flush();
	}

	/*
	 * private SlotReservation findEmptyLocationInTime(Container container,
	 * ArrayList<String> laneList, Random r, RemoteTerminal rt, Time fromTime,
	 * Slot fromSlot) , NoPathFoundException{ SlotReservation res = null; int
	 * i=0; do{ String laneID = laneList.get(Terminal.getInstance().getRandom().nextInt(laneList.size())); Slot s
	 * =
	 * rt.getSlot(rt.getSlotNames(laneID)[Terminal.getInstance().getRandom().nextInt(rt.getSlotNames(laneID).length
	 * )]); StraddleCarrier rsc = new
	 * ArrayList<StraddleCarrier>(Terminal.getInstance
	 * ().straddleCarriers.values(
	 * )).get(Terminal.getInstance().getRandom().nextInt(Terminal.getInstance().straddleCarriers.size())); Path
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

	private void generateTrainMissions(Map<BlockType, List<String>> conteneurs, Time arrivalTime, Time maxTime,
			double marginRate, double fullRate, double fullRateAfterUnload, double fullRateAfterReload, String trainId) throws NoPathFoundException,
			ContainerDimensionException, EmptyLevelException, SQLException {
		StraddleCarrier vehicle = getAStraddleCarrier();
		// Create an incoming train
		log.info("Creating TRAIN !");
		EventBean train = new EventBean();
		train.setType(EventType.VehicleIn);
		train.setTime(arrivalTime);
		StringBuilder trainSB = new StringBuilder();
		trainSB.append("id="+ trainId + ",lanes={");

		// Get the laneGroups
		Map<String, Block> trainPaves = Terminal.getInstance().getPaves(BlockType.RAILWAY);
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
						List<Slot> lSlots = Terminal.getInstance().getSlots(l.getId());
						for (Slot s : lSlots) {
							int index;
							if (s.getTEU() == ContainerKind.getTeu(Container.TYPE_20_Feet))
								index = Container.TYPE_20_Feet;
							else if (s.getTEU() == ContainerKind.getTeu(Container.TYPE_40_Feet))
								index = Container.TYPE_40_Feet;
							else
								index = Container.TYPE_45_Feet;
							mapGroupSlotCount.get(laneGroup)[index] = mapGroupSlotCount.get(laneGroup)[index] + 1;
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
			if (slotReservations.get(s0.getId()).isSlotEmptyAt(new TimeWindow(arrivalTime, maxTime))) {
				group = key;
			}
		}
		List<String> lanesIds = mapGroup.get(group);
		System.out.print("Lanes : ");
		for (int i = 0; i < lanesIds.size(); i++) {
			trainSB.append(lanesIds.get(i));
			System.out.print(lanesIds.get(i) + " ");
			if (i < lanesIds.size() - 1)
				trainSB.append(";");
			else
				trainSB.append("}");
		}

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
		log.info("Nb 45 : " + nbContainers45 + " Nb 40 : " + nbContainers40 + " Nb 20 : " + nbContainers20);

		// Used for knowing which containers are in the train at the end of the
		// generation
		Map<String, Container> containersOnTrain = new HashMap<>(nbContainers20 + nbContainers40 + nbContainers45);

		// Lists of train slots by size
		List<Slot> slots20Feet = new ArrayList<>(mapGroupSlotCount.get(group)[Container.TYPE_20_Feet]);
		List<Slot> slots40Feet = new ArrayList<>(mapGroupSlotCount.get(group)[Container.TYPE_40_Feet]);
		List<Slot> slots45Feet = new ArrayList<>(mapGroupSlotCount.get(group)[Container.TYPE_45_Feet]);
		for (String lane : mapGroup.get(group)) {
			for (Slot s : Terminal.getInstance().getSlots(lane)) {
				if (s.getTEU() == ContainerKind.getTeu(Container.TYPE_20_Feet))
					slots20Feet.add(s);
				else if (s.getTEU() == ContainerKind.getTeu(Container.TYPE_40_Feet))
					slots40Feet.add(s);
				else if (s.getTEU() == ContainerKind.getTeu(Container.TYPE_45_Feet))
					slots45Feet.add(s);
			}
		}

		// Creation of the containers stocked on the train
		// ContainerBICGenerator bicGenerator = new
		// ContainerBICGenerator(nbContainers45+nbContainers20+nbContainers40,
		// rt.getContainerNames());
		bicGenerator.generateMore(nbContainers45 + nbContainers20 + nbContainers40);

		List<Container> containers45Feet = new ArrayList<>(nbContainers45);
		List<Container> containers40Feet = new ArrayList<>(nbContainers40);
		List<Container> containers20Feet = new ArrayList<>(nbContainers20);

		Map<String, ContainerLocation> containerLocationOnTrain = new HashMap<>();
		Map<String, List<ContainerLocation>> containerLocationBySlot = new HashMap<>();
		trainSB.append(",containers={");
		for (int i = 0; i < nbContainers45; i++) {
			Container c = new Container(bicGenerator.giveMeBic().toString(), ContainerKind.getTeu(Container.TYPE_45_Feet));

			// Choose a slot, a level, an alignment
			boolean slotOk = false;
			Slot slot = null;
			while (!slotOk) {
				slot = slots45Feet.get(Terminal.getInstance().getRandom().nextInt(slots45Feet.size()));
				if (slotReservations.get(slot.getId()).isSlotEmptyAt(new TimeWindow(arrivalTime, maxTime)))
					slotOk = true;
			}

			// Put it
			SlotReservations reservations = slotReservations.get(slot.getId());
			SlotReservation reservation = reservations.giveFreeReservation(c, new TimeWindow(arrivalTime, maxTime));
			addReservation(reservation);

			containers45Feet.add(c);
			containersOnTrain.put(c.getId(), c);
			containerLocationOnTrain.put(c.getId(), reservation.getContainerLocation());

			List<ContainerLocation> l = containerLocationBySlot.get(slot.getId());
			if (l == null)
				l = new ArrayList<ContainerLocation>();
			l.add(Math.min(l.size(), reservation.getContainerLocation().getLevel()), reservation.getContainerLocation());
			containerLocationBySlot.put(slot.getId(), l);

			log.info("Container : id='" + c.getId() + "' teu='" + c.getTEU());

			trainSB.append("id:" + c.getId() + ",teu:" + c.getTEU()+",slot:"+reservation.getContainerLocation().getSlotId()
					+ ",level:"+reservation.getContainerLocation().getLevel()
					+ ",alignement:"+reservation.getContainerLocation().getAlign()+"|");
		}

		for (int i = 0; i < nbContainers40; i++) {
			Container c = new Container(bicGenerator.giveMeBic().toString(), ContainerKind.getTeu(Container.TYPE_40_Feet));
			// Choose a slot, a level, an alignment
			boolean slotOk = false;
			Slot slot = null;
			while (!slotOk) {
				slot = slots40Feet.get(Terminal.getInstance().getRandom().nextInt(slots40Feet.size()));
				if (slotReservations.get(slot.getId()).isSlotEmptyAt(new TimeWindow(arrivalTime, maxTime)))
					slotOk = true;
			}

			// Put it
			SlotReservations reservations = slotReservations.get(slot.getId());
			SlotReservation reservation = reservations.giveFreeReservation(c, new TimeWindow(arrivalTime, maxTime));
			addReservation(reservation);

			containers40Feet.add(c);
			containersOnTrain.put(c.getId(), c);
			containerLocationOnTrain.put(c.getId(), reservation.getContainerLocation());

			List<ContainerLocation> l = containerLocationBySlot.get(slot.getId());
			if (l == null)
				l = new ArrayList<ContainerLocation>();
			l.add(Math.min(l.size(), reservation.getContainerLocation().getLevel()), reservation.getContainerLocation());
			containerLocationBySlot.put(slot.getId(), l);

			log.info("Container : id='" + c.getId() + "' teu='" + c.getTEU());
			trainSB.append("id:" + c.getId() + ",teu:" + c.getTEU()+",slot:"+reservation.getContainerLocation().getSlotId()
					+ ",level:"+reservation.getContainerLocation().getLevel()
					+ ",alignement:"+reservation.getContainerLocation().getAlign()+"|");
		}

		for (int i = 0; i < nbContainers20; i++) {
			Container c = new Container(bicGenerator.giveMeBic().toString(), ContainerKind.getTeu(Container.TYPE_20_Feet));
			// Choose a slot, a level, an alignment
			Slot slot = null;
			SlotReservation reservation = null;
			while (reservation == null) {
				slot = slots20Feet.get(Terminal.getInstance().getRandom().nextInt(slots20Feet.size()));
				reservation = slotReservations.get(slot.getId()).giveFreeReservation(c, new TimeWindow(arrivalTime, maxTime));
			}

			// Put it
			addReservation(reservation);

			containersOnTrain.put(c.getId(), c);
			containers20Feet.add(c);
			containerLocationOnTrain.put(c.getId(), reservation.getContainerLocation());

			List<ContainerLocation> l = containerLocationBySlot.get(slot.getId());
			if (l == null)
				l = new ArrayList<ContainerLocation>();
			l.add(Math.min(l.size(), reservation.getContainerLocation().getLevel()), reservation.getContainerLocation());
			containerLocationBySlot.put(slot.getId(), l);

			log.info("Container : id='" + c.getId() + "' teu='" + c.getTEU());
			trainSB.append("id:" + c.getId() + ",teu:" + c.getTEU()+",slot:"+reservation.getContainerLocation().getSlotId()
					+ ",level:"+reservation.getContainerLocation().getLevel()
					+ ",alignement:"+reservation.getContainerLocation().getAlign()+"|");
		}
		String str = trainSB.toString();
		if(str.endsWith("|")){
			str = str.substring(0, str.length()-1);
		}
		train.setDescription(str);
		EventDAO.getInstance(scenario.getId()).insert(train);
		// ---- END OF TRAIN CREATION -----

		// Now we have a train with containers asking to be unloaded and loaded
		// with other containers

		// Compute unload missions
		log.info("Computing unload missions !");
		int nbUnloadMissions45Feet = Math.max(0,
				(nbContainers45 - (int) (mapGroupSlotCount.get(group)[Container.TYPE_45_Feet] * fullRateAfterUnload)));
		int nbUnloadMissions40Feet = Math.max(0,
				(nbContainers40 - (int) (mapGroupSlotCount.get(group)[Container.TYPE_40_Feet] * fullRateAfterUnload)));
		int nbUnloadMissions20Feet = Math.max(0,
				(nbContainers20 - (int) (mapGroupSlotCount.get(group)[Container.TYPE_20_Feet] * fullRateAfterUnload)));
		log.info("Nb45Feet : " + nbUnloadMissions45Feet + " Nb40Feet : " + nbUnloadMissions40Feet + " Nb20Feet : " + nbUnloadMissions20Feet);

		// Lists of slots by size in the stocks areas
		List<Slot> stockSlots45Feet = new ArrayList<>();
		List<Slot> stockSlots40Feet = new ArrayList<>();
		List<Slot> stockSlots20Feet = new ArrayList<>();

		for (Block p : Terminal.getInstance().getPaves(BlockType.YARD).values()) {
			for (Bay l : p.getLanes()) {
				for (Slot s : Terminal.getInstance().getSlots(l.getId())) {
					if (s.getTEU() == ContainerKind.getTeu(Container.TYPE_45_Feet))
						stockSlots45Feet.add(s);
					else if (s.getTEU() == ContainerKind.getTeu(Container.TYPE_40_Feet))
						stockSlots40Feet.add(s);
					else if (s.getTEU() == ContainerKind.getTeu(Container.TYPE_20_Feet))
						stockSlots20Feet.add(s);
				}
			}
		}

		// Usefull for computing the time windows of the reloading missions and
		// to prevent the reloading of the train untill unloading is over
		int nbUnloadingMissions = nbUnloadMissions20Feet + nbUnloadMissions40Feet + nbUnloadMissions45Feet;

		int nbReloadMissions45Feet = Math.max(0, (int) (slots45Feet.size() * fullRateAfterReload) - (nbContainers45 - nbUnloadMissions45Feet));
		int nbReloadMissions40Feet = Math.max(0, (int) (slots40Feet.size() * fullRateAfterReload) - (nbContainers40 - nbUnloadMissions40Feet));
		int nbReloadMissions20Feet = Math.max(0, (int) (slots20Feet.size() * fullRateAfterReload) - (nbContainers20 - nbUnloadMissions20Feet));

		int nbLoadingMissions = nbReloadMissions20Feet + nbReloadMissions40Feet + nbReloadMissions45Feet;

		final int nbMissionsOverall = nbUnloadingMissions + nbLoadingMissions;

		incrementProgressBar();

		Time maxTimeForUnload = new Time(arrivalTime, new Time(
				((maxTime.getInSec() - arrivalTime.getInSec()) * (nbUnloadingMissions / (nbMissionsOverall + 0.0)))));
		log.info("ArrivalTime = " + arrivalTime);
		log.info("MaxTime = " + maxTime);
		log.info("NB_UNLOADINGS = " + nbUnloadingMissions);
		log.info("NB_MISSIONS = " + nbMissionsOverall);
		log.info("MaxTimeForUnload = " + maxTimeForUnload);
		Time maxUnloadTime = new Time(0);

		int nbMissionsCreated = 0;
		// For the missions ids
		int nbMissionsCreatedOverall = 0;

		for (Slot slot : slots45Feet) {
			if (nbMissionsCreated >= nbUnloadMissions45Feet) {
				break;
			}

			Time fromTime = arrivalTime;

			List<ContainerLocation> l = containerLocationBySlot.get(slot.getId());
			if (l != null) {
				for (int i = l.size() - 1; i >= 0 && nbMissionsCreated < nbUnloadMissions45Feet; i--) {
					Container c = null;
					for (int j = 0; j < containers45Feet.size() && c == null; j++) {
						if (containers45Feet.get(j).getId().equals(l.get(i).getContainerId())) {
							c = containers45Feet.get(j);
						}
					}

					String mId = "unload_" + trainId + "_" + (nbMissionsCreatedOverall + 1);
					EventBean mission = new EventBean();
					mission.setType(EventType.NewMission);
					mission.setTime(arrivalTime);
					StringBuilder sb = new StringBuilder();
					sb.append("id=" + mId + ",container=" + c.getId() + ",kind=" + MissionKinds.IN.getIntValue());

					// Choose a destination in the stock area
					Slot sDestination = null;
					SlotReservation destinationReservation = null;
					TimeWindow twP = null;
					TimeWindow twD = null;

					while (destinationReservation == null) {
						sDestination = stockSlots45Feet.get(Terminal.getInstance().getRandom().nextInt(stockSlots45Feet.size()));

						Time duration = new Time(maxTimeForUnload, fromTime, false); // Change
						// false
						// into
						// true
						System.err.println("Duration = " + duration);
						Time startTime = new Time(fromTime, new Time((Terminal.getInstance().getRandom().nextDouble() * duration.getInSec())), true);
						System.err.println("StartTime : " + startTime);
						// Pickup
						Path pStartToPickUp = vehicle.getRouting().getShortestPath(vehicle.getLocation(), slot.getLocation());
						Time pickupEndTime = new Time(startTime, new Time(pStartToPickUp.getCost()));
						pickupEndTime = new Time(pickupEndTime, handlingTimeFromTruck);
						Time margin = new Time((marginRate * pickupEndTime.getInSec()));
						Time puMin = new Time(pickupEndTime, margin, false);
						Time puMax = new Time(pickupEndTime, margin, true);
						twP = new TimeWindow(puMin, puMax);
						System.err.println("twP = " + twP);
						// Delivery
						Path pPickUpToDelivery = vehicle.getRouting().getShortestPath(slot.getLocation(), sDestination.getLocation());
						Time deliveryEndTime = new Time(pickupEndTime, new Time(pPickUpToDelivery.getCost()));
						deliveryEndTime = new Time(deliveryEndTime, handlingTimeFromGround);
						margin = new Time((marginRate * deliveryEndTime.getInSec()));
						Time dMin = new Time(deliveryEndTime, margin, false);
						Time dMax = new Time(deliveryEndTime, margin, true);
						twD = new TimeWindow(dMin, dMax);
						System.err.println("twD = " + twD);
						if (puMax.toStep() > maxUnloadTime.toStep())
							maxUnloadTime = puMax;
						startTime = pickupEndTime; // TODO useless ?!

						destinationReservation = slotReservations.get(sDestination.getId()).giveFreeReservation(c, twD);
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

					sb.append(",twPMin=" + twP.getMin() + ",twPMax=" + twP.getMax());
					sb.append(",twDMin=" + twD.getMin() + ",twDMax=" + twD.getMax());
					sb.append(",slot=" + destinationReservation.getContainerLocation().getSlotId());
					sb.append(",level=" + destinationReservation.getContainerLocation().getLevel());
					sb.append(",alignement=" + destinationReservation.getContainerLocation().getAlign());
					mission.setDescription(sb.toString());
					EventDAO.getInstance(scenario.getId()).insert(mission);

					incrementProgressBar();

					nbMissionsCreated++;
					nbMissionsCreatedOverall++;
				}

			}
		}
		log.info("nb45Created : " + nbMissionsCreated);
		nbMissionsCreated = 0;
		for (Slot slot : slots40Feet) {

			Time fromTime = arrivalTime;

			if (nbMissionsCreated >= nbUnloadMissions40Feet) {
				break;
			}

			List<ContainerLocation> l = containerLocationBySlot.get(slot.getId());
			if (l != null) {
				for (int i = l.size() - 1; i >= 0 && nbMissionsCreated < nbUnloadMissions40Feet; i--) {
					Container c = null;
					for (int j = 0; j < containers40Feet.size() && c == null; j++) {
						if (containers40Feet.get(j).getId().equals(l.get(i).getContainerId())) {
							c = containers40Feet.get(j);
						}
					}
					String mId = "unload_" + trainId + "_" + (nbMissionsCreatedOverall + 1);
					if (c == null) {

						break;
					}
					EventBean mission = new EventBean();
					mission.setType(EventType.NewMission);
					mission.setTime(arrivalTime);
					StringBuilder sb = new StringBuilder();
					sb.append("id=" + mId + ",container=" + c.getId() + ",kind=" + MissionKinds.IN.getIntValue());

					// Choose a destination in the stock area
					Slot sDestination = null;
					SlotReservation destinationReservation = null;
					TimeWindow twP = null;
					TimeWindow twD = null;
					while (destinationReservation == null) {
						// Choose a destination in the stock area
						sDestination = stockSlots40Feet.get(Terminal.getInstance().getRandom().nextInt(stockSlots40Feet.size()));
						Time duration = new Time(maxTimeForUnload, fromTime, false); // Change
						// false
						// into
						// true
						System.err.println("Duration = " + duration);
						Time startTime = new Time(fromTime, new Time((Terminal.getInstance().getRandom().nextDouble() * duration.getInSec())), true);
						System.err.println("StartTime : " + startTime);
						// Pickup
						Path pStartToPickUp = vehicle.getRouting().getShortestPath(vehicle.getLocation(), slot.getLocation());
						Time pickupEndTime = new Time(startTime, new Time(pStartToPickUp.getCost()));
						pickupEndTime = new Time(pickupEndTime, handlingTimeFromTruck);
						Time margin = new Time((marginRate * pickupEndTime.getInSec()));
						Time puMin = new Time(pickupEndTime, margin, false);
						Time puMax = new Time(pickupEndTime, margin, true);
						twP = new TimeWindow(puMin, puMax);

						// Delivery
						Path pPickUpToDelivery = vehicle.getRouting().getShortestPath(slot.getLocation(), sDestination.getLocation());
						Time deliveryEndTime = new Time(pickupEndTime, new Time(pPickUpToDelivery.getCost()));
						deliveryEndTime = new Time(deliveryEndTime, handlingTimeFromGround);
						margin = new Time((marginRate * deliveryEndTime.getInSec()));
						Time dMin = new Time(deliveryEndTime, margin, false);
						Time dMax = new Time(deliveryEndTime, margin, true);
						twD = new TimeWindow(dMin, dMax);

						if (puMax.compareTo(maxUnloadTime) > 0)
							maxUnloadTime = puMax;
						startTime = pickupEndTime;

						destinationReservation = slotReservations.get(sDestination.getId()).giveFreeReservation(c, twD);
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

					sb.append(",twPMin=" + twP.getMin() + ",twPMax=" + twP.getMax());
					sb.append(",twDMin=" + twD.getMin() + ",twDMax=" + twD.getMax());
					sb.append(",slot=" + destinationReservation.getContainerLocation().getSlotId());
					sb.append(",level=" + destinationReservation.getContainerLocation().getLevel());
					sb.append(",alignement=" + destinationReservation.getContainerLocation().getAlign());

					mission.setDescription(sb.toString());
					EventDAO.getInstance(scenario.getId()).insert(mission);

					incrementProgressBar();
					nbMissionsCreated++;
					nbMissionsCreatedOverall++;
				}
			}
		}
		// nbMissionsCreatedOverall+=nbMissionsCreated;
		log.info("nb40Created : " + nbMissionsCreated);
		nbMissionsCreated = 0;
		for (Container c : containers20Feet) {
			Time fromTime = arrivalTime;
			Slot slot = Terminal.getInstance().getSlot(containerLocationOnTrain.get(c.getId()).getSlotId());

			if (nbMissionsCreated >= nbUnloadMissions20Feet) {
				break;
			}
			String mId = "unload_" + trainId + "_" + (nbMissionsCreatedOverall + 1);
			EventBean mission = new EventBean();
			mission.setType(EventType.NewMission);
			mission.setTime(arrivalTime);
			StringBuilder sb = new StringBuilder();

			sb.append("id=" + mId + ",container=" + c.getId() + ",kind=" + MissionKinds.IN.getIntValue());

			Slot sDestination = null;
			SlotReservation destinationReservation = null;
			TimeWindow twP = null;
			TimeWindow twD = null;
			while (destinationReservation == null) {
				// Choose a destination in the stock area
				sDestination = stockSlots20Feet.get(Terminal.getInstance().getRandom().nextInt(stockSlots20Feet.size()));
				Time duration = new Time(maxTimeForUnload, fromTime, false); // Change
				// false
				// into
				// true
				System.err.println("Duration = " + duration);
				Time startTime = new Time(fromTime, new Time((Terminal.getInstance().getRandom().nextDouble() * duration.getInSec())), true);
				System.err.println("StartTime : " + startTime);
				// Pickup
				Path pStartToPickUp = vehicle.getRouting().getShortestPath(vehicle.getLocation(), slot.getLocation());
				Time pickupEndTime = new Time(startTime, new Time(pStartToPickUp.getCost()));
				pickupEndTime = new Time(pickupEndTime, handlingTimeFromTruck);
				Time margin = new Time((marginRate * pickupEndTime.getInSec()));
				Time puMin = new Time(pickupEndTime, margin, false);
				Time puMax = new Time(pickupEndTime, margin, true);
				twP = new TimeWindow(puMin, puMax);

				// Delivery
				Path pPickUpToDelivery = vehicle.getRouting().getShortestPath(slot.getLocation(), sDestination.getLocation());
				Time deliveryEndTime = new Time(pickupEndTime, new Time(pPickUpToDelivery.getCost()));
				deliveryEndTime = new Time(deliveryEndTime, handlingTimeFromGround);
				margin = new Time((marginRate * deliveryEndTime.getInSec()));
				Time dMin = new Time(deliveryEndTime, margin, false);
				Time dMax = new Time(deliveryEndTime, margin, true);
				twD = new TimeWindow(dMin, dMax);

				if (puMax.compareTo(maxUnloadTime) > 0)
					maxUnloadTime = puMax;
				startTime = pickupEndTime;

				destinationReservation = slotReservations.get(sDestination.getId()).giveFreeReservation(c, twD);
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

			sb.append(",twPMin=" + twP.getMin() + ",twPMax=" + twP.getMax());
			sb.append(",twDMin=" + twD.getMin() + ",twDMax=" + twD.getMax());
			sb.append(",slot=" + destinationReservation.getContainerLocation().getSlotId());
			sb.append(",level=" + destinationReservation.getContainerLocation().getLevel());
			sb.append(",alignement=" + destinationReservation.getContainerLocation().getAlign());
			mission.setDescription(sb.toString());
			EventDAO.getInstance(scenario.getId()).insert(mission);

			incrementProgressBar();
			nbMissionsCreated++;
			nbMissionsCreatedOverall++;
		}
		log.info("nb20Created : " + nbMissionsCreated + " nbUnloadingMissions = " + nbMissionsCreatedOverall);
		// ------ END OF UNLOADING ------

		// Compute load missions
		log.info("Computing load missions !");

		List<Mission> reloadingMissions = new ArrayList<>(nbReloadMissions20Feet + nbReloadMissions40Feet + nbReloadMissions45Feet);

		log.info("Nb45Feet : " + nbReloadMissions45Feet + " Nb40Feet : " + nbReloadMissions40Feet + " Nb20Feet : " + nbReloadMissions20Feet);

		nbMissionsCreated = 0;
		nbMissionsCreatedOverall = 0;
		for (Slot slot : stockSlots45Feet) {
			Time fromTime = maxUnloadTime;

			int maxLevel = slot.getLevels().size() - 1;

			if (nbMissionsCreated >= nbReloadMissions45Feet) {
				break;
			}

			for (int i = maxLevel; i >= 0 && nbMissionsCreated < nbReloadMissions45Feet; i--) {

				Level l = slot.getLevels().get(i);
				nbMissionsCreated++;
				nbMissionsCreatedOverall++;
				String mId = "reload_" + trainId + "_" + nbMissionsCreatedOverall;
				if (l.getTEU() == l.getMaxTeu()) {

					i--;
					if (i > 0) {
						l = slot.getLevels().get(i);
					}
				}
				Container c = l.getFirstContainer();
				EventBean mission = new EventBean();
				mission.setType(EventType.NewMission);
				mission.setTime(arrivalTime);
				StringBuilder sb = new StringBuilder();
				sb.append("id=" + mId + ",container=" + c.getId() + ",kind=" + MissionKinds.OUT.getIntValue());

				Slot sDestination = null;
				SlotReservation destinationReservation = null;
				TimeWindow twP = null;
				TimeWindow twD = null;
				while (destinationReservation == null) {
					// Choose a destination in the stock area
					sDestination = slots45Feet.get(Terminal.getInstance().getRandom().nextInt(slots45Feet.size()));
					// Pickup
					Path pStartToPickUp = vehicle.getRouting().getShortestPath(vehicle.getLocation(), c.getLocation());
					Time d1 = new Time(pStartToPickUp.getCost());
					// Delivery
					Path pPickUpToDelivery = vehicle.getRouting().getShortestPath(c.getLocation(), sDestination.getLocation());
					Time d2 = new Time(pPickUpToDelivery.getCost());

					Time d1d2 = new Time(d1, d2);
					d1d2 = new Time(d1d2, handlingTimeFromGround);
					d1d2 = new Time(d1d2, handlingTimeFromTruck);

					Time duration = new Time(new Time(maxTime, d1d2, false), fromTime, false); // And
					// again
					// and
					// again
					Time startTime = new Time(fromTime, new Time((Terminal.getInstance().getRandom().nextDouble() * duration.getInSec())));
					Time pickupEndTime = new Time(startTime, d1);
					pickupEndTime = new Time(pickupEndTime, handlingTimeFromTruck);
					Time margin = new Time((marginRate * d1.getInSec()));
					Time puMin = new Time(pickupEndTime, margin, false);
					Time puMax = new Time(pickupEndTime, margin, true);
					twP = new TimeWindow(puMin, puMax);

					Time deliveryEndTime = new Time(pickupEndTime, d2);
					deliveryEndTime = new Time(deliveryEndTime, handlingTimeFromGround);

					margin = new Time((marginRate * d2.getInSec()));
					Time dMin = new Time(deliveryEndTime, margin, false);
					Time dMax = new Time(deliveryEndTime, margin, true);
					twD = new TimeWindow(dMin, dMax);
					if (twD.getMax().toStep() > maxTime.toStep())
						log.info("MAX TIME OVERSPENT FOR TRAIN RELOADING MISSION CONCERNING " + c.getId() + " " + twD.getMax());
					else {
						startTime = pickupEndTime;
						destinationReservation = slotReservations.get(sDestination.getId()).giveFreeReservation(c, twD);
						// Prevent from time windows inversions between
						// containers of the same slot
						fromTime = twP.getMax();
					}
				}

				Mission m = new Mission(mId, MissionKinds.OUT.getIntValue(), twP, twD, c.getId(), destinationReservation.getContainerLocation());
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

				sb.append(",twPMin=" + twP.getMin() + ",twPMax=" + twP.getMax());
				sb.append(",twDMin=" + twD.getMin() + ",twDMax=" + twD.getMax());
				sb.append(",slot=" + destinationReservation.getContainerLocation().getSlotId());
				sb.append(",level=" + destinationReservation.getContainerLocation().getLevel());
				sb.append(",alignement=" + destinationReservation.getContainerLocation().getAlign());

				mission.setDescription(sb.toString());
				EventDAO.getInstance(scenario.getId()).insert(mission);
				incrementProgressBar();
			}
		}

		log.info(nbMissionsCreated + " missions of 45 feet created !");
		nbMissionsCreated = 0;
		for (Slot slot : stockSlots40Feet) {
			// First Step : find a container in the yard
			Time fromTime = maxUnloadTime;
			int maxLevel = slot.getLevels().size() - 1;

			if (nbMissionsCreated >= nbReloadMissions40Feet) {
				break;
			}

			for (int i = maxLevel; i >= 0 && nbMissionsCreated < nbReloadMissions40Feet; i--) {
				Level l = slot.getLevels().get(i);

				// if(l.getTEU() < l.getMaxTeu()) {
				// If there is a container at this level
				if (l.getTEU() > 0) {
					Container c = l.getFirstContainer();
					if (c.getTEU() == ContainerKind.getTeu(Container.TYPE_40_Feet)) {
						// Next Step : find a destination slot
						Slot sDestination = null;
						SlotReservation destinationReservation = null;
						TimeWindow twP = null;
						TimeWindow twD = null;
						while (destinationReservation == null) {

							// Choose a destination in the train lanes
							sDestination = slots40Feet.get(Terminal.getInstance().getRandom().nextInt(slots40Feet.size()));
							// Pickup
							Path pStartToPickUp = vehicle.getRouting().getShortestPath(vehicle.getLocation(), c.getLocation());
							Time d1 = new Time(pStartToPickUp.getCost());
							// Delivery
							Path pPickUpToDelivery = vehicle.getRouting().getShortestPath(c.getLocation(), sDestination.getLocation());
							Time d2 = new Time(pPickUpToDelivery.getCost());

							Time d1d2 = new Time(d1, d2);
							d1d2 = new Time(d1d2, handlingTimeFromGround);
							d1d2 = new Time(d1d2, handlingTimeFromTruck);

							Time duration = new Time(new Time(maxTime, d1d2, false), fromTime, false); // And
							// again
							// and
							// again
							Time startTime = new Time(fromTime, new Time((Terminal.getInstance().getRandom().nextDouble() * duration.getInSec())));
							Time pickupEndTime = new Time(startTime, d1);
							pickupEndTime = new Time(pickupEndTime, handlingTimeFromTruck);
							Time margin = new Time((marginRate * d1.getInSec()));
							Time puMin = new Time(pickupEndTime, margin, false);
							Time puMax = new Time(pickupEndTime, margin, true);
							twP = new TimeWindow(puMin, puMax);

							Time deliveryEndTime = new Time(pickupEndTime, d2);
							deliveryEndTime = new Time(deliveryEndTime, handlingTimeFromGround);

							margin = new Time((marginRate * d2.getInSec()));
							Time dMin = new Time(deliveryEndTime, margin, false);
							Time dMax = new Time(deliveryEndTime, margin, true);
							twD = new TimeWindow(dMin, dMax);
							if (twD.getMax().toStep() > maxTime.toStep()) {
								System.err.println("MAX TIME OVERSPENT FOR TRAIN RELOADING MISSION CONCERNING " + c.getId() + " " + twD.getMax());
							} else {
								startTime = pickupEndTime;
								destinationReservation = slotReservations.get(sDestination.getId()).giveFreeReservation(c, twD);
								// Prevent from time windows inversions between
								// containers of the same slot
								fromTime = twP.getMax();
							}

						}
						nbMissionsCreated++;
						nbMissionsCreatedOverall++;
						String mId = "reload_" + trainId + "_" + nbMissionsCreatedOverall;
						EventBean mission = new EventBean();
						mission.setType(EventType.NewMission);
						mission.setTime(arrivalTime);
						StringBuilder sb = new StringBuilder();
						sb.append("id=" + mId + ",container=" + c.getId() + ",kind=" + MissionKinds.OUT.getIntValue());
						Mission m = new Mission(mId, MissionKinds.OUT.getIntValue(), twP, twD, c.getId(),
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


						sb.append(",twPMin=" + twP.getMin() + ",twPMax=" + twP.getMax());
						sb.append(",twDMin=" + twD.getMin() + ",twDMax=" + twD.getMax());
						sb.append(",slot=" + destinationReservation.getContainerLocation().getSlotId());
						sb.append(",level=" + destinationReservation.getContainerLocation().getLevel());
						sb.append(",alignement=" + destinationReservation.getContainerLocation().getAlign());

						mission.setDescription(sb.toString());
						EventDAO.getInstance(scenario.getId()).insert(mission);
						incrementProgressBar();
					}
				}
			}
		}
		log.info(nbMissionsCreated + " missions of 40 feet created !");
		nbMissionsCreated = 0;
		Container[] tContainers = Terminal.getInstance().getContainersArray();
		boolean noMoreSpace = false;

		while (nbMissionsCreated < nbReloadMissions20Feet && !noMoreSpace) {
			Time fromTime = maxUnloadTime;
			TimeWindow tw20Feet = new TimeWindow(fromTime, maxTime);
			if (fromTime.toStep() > maxTime.toStep()) {
				tw20Feet = new TimeWindow(fromTime, fromTime);
				log.info("TW 20 FEET = [fromTime , fromTime] = [" + fromTime + " , " + fromTime + "]");
			}

			Container c = null;
			Slot slot = null;
			while (c == null && tContainers.length > 0) {
				c = tContainers[Terminal.getInstance().getRandom().nextInt(tContainers.length)];
				if (c.getDimensionType() != Container.TYPE_20_Feet
						|| Terminal.getInstance().getBlock(c.getContainerLocation().getPaveId()).getType() != BlockType.YARD)
					c = null;
				else {
					slot = Terminal.getInstance().getSlot(c.getContainerLocation().getSlotId());
					if (!slotReservations.get(slot.getId()).isContainerFreeAt(c, tw20Feet))
						c = null;
				}
			}

			int maxLevel = slot.getLevels().size() - 1;

			if (nbMissionsCreated >= nbReloadMissions20Feet) {
				log.info("Break 20 !");
				break;
			}

			for (int i = maxLevel; i >= 0 && nbMissionsCreated < nbReloadMissions20Feet && !noMoreSpace; i--) {
				String mId = "reload_" + trainId + "_" + (nbMissionsCreatedOverall + 1);
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
						log.info("No slot available !");
						noMoreSpace = true;
						break;
					}

					if (Terminal.getInstance().getBlock(((Bay) sDestination.getLocation().getRoad()).getPaveId()).getType() != BlockType.RAILWAY) {
						log.info("Pave " + sDestination.getPaveId() + " is not a train pave !!!");
					} else
						log.info("Pave " + sDestination.getPaveId() + " is a train pave and contains lane "
								+ sDestination.getLocation().getRoad().getId() + " for slot " + sDestination.getId());
					// Pickup
					Path pStartToPickUp = vehicle.getRouting().getShortestPath(vehicle.getLocation(), c.getLocation());
					Time d1 = new Time(pStartToPickUp.getCost());
					// Delivery
					Path pPickUpToDelivery = vehicle.getRouting().getShortestPath(c.getLocation(), sDestination.getLocation());
					Time d2 = new Time(pPickUpToDelivery.getCost());

					Time d1d2 = new Time(d1, d2);
					d1d2 = new Time(d1d2, handlingTimeFromGround);
					d1d2 = new Time(d1d2, handlingTimeFromTruck);

					Time duration = new Time(new Time(maxTime, d1d2, false), fromTime, false); // And
					// again
					// and
					// again
					Time startTime = new Time(fromTime, new Time((Terminal.getInstance().getRandom().nextDouble() * duration.getInSec())));
					Time pickupEndTime = new Time(startTime, d1);
					pickupEndTime = new Time(pickupEndTime, handlingTimeFromTruck);
					Time margin = new Time((marginRate * d1.getInSec()));
					Time puMin = new Time(pickupEndTime, margin, false);
					Time puMax = new Time(pickupEndTime, margin, true);
					twP = new TimeWindow(puMin, puMax);

					Time deliveryEndTime = new Time(pickupEndTime, d2);
					deliveryEndTime = new Time(deliveryEndTime, handlingTimeFromGround);

					margin = new Time((marginRate * d2.getInSec()));
					Time dMin = new Time(deliveryEndTime, margin, false);
					Time dMax = new Time(deliveryEndTime, margin, true);
					twD = new TimeWindow(dMin, dMax);
					if (twD.getMax().toStep() > maxTime.toStep())
						log.info("MAX TIME OVERSPENT FOR TRAIN RELOADING MISSION CONCERNING " + c.getId() + " " + twD.getMax());
					else {
						startTime = pickupEndTime;
						destinationReservation = slotReservations.get(sDestination.getId()).giveFreeReservation(c, twD);
						// Prevent from time windows inversions between
						// containers of the same slot
						fromTime = twP.getMax();

					}
				}
				// if(destinationReservation!=null){
				EventBean mission = new EventBean();
				mission.setType(EventType.NewMission);
				mission.setTime(arrivalTime);
				StringBuilder sb = new StringBuilder();
				sb.append("id=" + mId + ",container=" + c.getId() + ",kind=" + MissionKinds.OUT.getIntValue());
				// Reservation for the delivery
				addReservation(destinationReservation);
				// Reservation update for the pickup
				updateReservationMaxTime(c, slot, twP.getMax());

				Mission m = new Mission(mId, MissionKinds.OUT.getIntValue(), twP, twD, c.getId(), destinationReservation.getContainerLocation());
				reloadingMissions.add(m);

				containersOnTrain.put(c.getId(), c);
				containersOUT.put(c.getId(), c.getId());

				sb.append(",twPMin=" + twP.getMin() + ",twPMax=" + twP.getMax());
				sb.append(",twDMin=" + twD.getMin() + ",twDMax=" + twD.getMax());
				sb.append(",slot=" + destinationReservation.getContainerLocation().getSlotId());
				sb.append(",level=" + destinationReservation.getContainerLocation().getLevel());
				sb.append(",alignement=" + destinationReservation.getContainerLocation().getAlign());

				mission.setDescription(sb.toString());
				EventDAO.getInstance(scenario.getId()).insert(mission);
				nbMissionsCreated++;
				nbMissionsCreatedOverall++;

				incrementProgressBar();
			}

		}
		log.info(nbMissionsCreated + " missions of 20 feet created !");
		// ------ END OF RELOADING ------

		/* Computing VehicleOut */
		// The vehicle can leave when the last mission is over
		Time maxDTime = new Time(0);
		for (Mission m : reloadingMissions) {
			if (m.getDeliveryTimeWindow().getMax().compareTo(maxDTime) > 0)
				maxDTime = m.getDeliveryTimeWindow().getMax();
		}

		EventBean departure = new EventBean();
		departure.setType(EventType.VehicleOut);
		departure.setTime(maxDTime);
		StringBuilder sb= new StringBuilder();
		sb.append("id=" + trainId + ",lanes={");
		for (int i = 0; i < lanesIds.size(); i++) {
			sb.append(lanesIds.get(i));
			if (i < lanesIds.size() - 1)
				sb.append(";");
			else
				sb.append("}");
		}
		// Containers onboard before leaving :
		sb.append("containers={");
		for (String cID : containersOnTrain.keySet()) {
			sb.append("id:"+cID+";");
		}
		String strOut = sb.toString();
		if(strOut.endsWith(";")){
			strOut = strOut.substring(0, strOut.length()-1);
		}
		strOut += "}";
		departure.setDescription(sb.toString());
		EventDAO.getInstance(scenario.getId()).insert(departure);
		// ------ END OF VEHICLE OUT ------
	}

}


