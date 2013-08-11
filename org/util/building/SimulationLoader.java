package org.util.building;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.StringTokenizer;

import org.apache.log4j.Logger;
import org.com.DbMgr;
import org.com.dao.BlockDAO;
import org.com.dao.ContainerDAO;
import org.com.dao.CrossroadDAO;
import org.com.dao.EventDAO;
import org.com.dao.LaserHeadDAO;
import org.com.dao.RoadDAO;
import org.com.dao.ScenarioDAO;
import org.com.dao.SlotDAO;
import org.com.dao.StraddleCarrierDAO;
import org.com.dao.TerminalDAO;
import org.com.dao.scheduling.MissionLoadDAO;
import org.com.model.BlockBean;
import org.com.model.ContainerBean;
import org.com.model.CrossroadBean;
import org.com.model.EventBean;
import org.com.model.LaserHeadBean;
import org.com.model.RoadBean;
import org.com.model.RoadPointBean;
import org.com.model.ScenarioBean;
import org.com.model.SchedulingAlgorithmBean;
import org.com.model.SimulationBean;
import org.com.model.SlotBean;
import org.com.model.StraddleCarrierBean;
import org.com.model.StraddleCarrierModelBean;
import org.com.model.TerminalBean;
import org.com.model.scheduling.BBParametersBean;
import org.com.model.scheduling.BranchAndBoundParametersBean;
import org.com.model.scheduling.GreedyParametersBean;
import org.com.model.scheduling.LinearParametersBean;
import org.com.model.scheduling.LoadBean;
import org.com.model.scheduling.OfflineACO2ParametersBean;
import org.com.model.scheduling.OfflineACOParametersBean;
import org.com.model.scheduling.OnlineACOParametersBean;
import org.com.model.scheduling.RandomParametersBean;
import org.display.MainFrame;
import org.missions.Load;
import org.positioning.Coordinates;
import org.positioning.LaserSystem;
import org.positioning.Range;
import org.routing.Routing;
import org.routing.RoutingAlgorithmType;
import org.scheduling.LinearMissionScheduler;
import org.scheduling.MissionScheduler;
import org.scheduling.bb.BB;
import org.scheduling.branchAndBound.BranchAndBound;
import org.scheduling.greedy.GreedyMissionScheduler;
import org.scheduling.offlineACO.OfflineACOScheduler;
import org.scheduling.offlineACO2.OfflineACOScheduler2;
import org.scheduling.onlineACO.OnlineACOScheduler;
import org.scheduling.random.RandomMissionScheduler;
import org.system.Crossroad;
import org.system.Depot;
import org.system.Road;
import org.system.RoadPoint;
import org.system.StraddleCarrierSlot;
import org.system.Terminal;
import org.system.container_stocking.Bay;
import org.system.container_stocking.BayCrossroad;
import org.system.container_stocking.Block;
import org.system.container_stocking.BlockType;
import org.system.container_stocking.ContainerKind;
import org.system.container_stocking.ContainerLocation;
import org.system.container_stocking.Quay;
import org.system.container_stocking.Slot;
import org.time.TimeScheduler;
import org.time.TimeWindow;
import org.time.event.DynamicEvent;
import org.util.Location;
import org.vehicles.StraddleCarrier;
import org.vehicles.models.RandomSpeed;
import org.vehicles.models.StraddleCarrierModel;

public class SimulationLoader {
	private static final Logger log = Logger.getLogger(SimulationLoader.class);

	private static SimulationLoader instance;

	private Terminal terminal;

	private Map<String, List<Slot>> slots;

	private SimulationLoader() {
		terminal = Terminal.getInstance();
		slots = new HashMap<>(1000);
	}

	public static final SimulationLoader getInstance() {
		if (instance == null) {
			instance = new SimulationLoader();
		}
		return instance;
	}

	public void load(final SimulationBean simBean) {
		Thread t = new Thread(){
			public void run () {
				
				// Load Scenario
				ScenarioBean scenarioBean = ScenarioDAO.getInstance().getScenario(
						simBean.getContent());
				
				try {
					// Load Terminal
					TerminalBean terminalBean = TerminalDAO.getInstance().getTerminal(
							scenarioBean.getTerminal());

					if(simBean.getSeed() != null)
						terminal.setSeed(simBean.getSeed());

					// Load Terminal blocks
					BlockDAO blockDAO = BlockDAO.getInstance(terminalBean.getId());
					blockDAO.load();
					// Load crossroads
					CrossroadDAO crossroadDAO = CrossroadDAO.getInstance(terminalBean
							.getId());
					crossroadDAO.load();
					// Load roads
					RoadDAO roadDAO = RoadDAO.getInstance(terminalBean.getId());
					roadDAO.load();
					// Load slots
					SlotDAO slotDAO = SlotDAO.getInstance(terminalBean.getId());
					slotDAO.load();

					LaserHeadDAO laserDAO = LaserHeadDAO.getInstance(scenarioBean
							.getId());
					laserDAO.load();

					// TODO load containers
					ContainerDAO containerDAO = ContainerDAO.getInstance(scenarioBean
							.getId());
					containerDAO.load();

					// TODO load straddle carriers
					StraddleCarrierDAO straddleCarrierDAO = StraddleCarrierDAO
							.getInstance(scenarioBean.getId());
					straddleCarrierDAO.load();

					// TODO load mission scheduler
					// TODO load events
					EventDAO eventDAO = EventDAO.getInstance(scenarioBean.getId());
					eventDAO.load();


					log.info("Loading blocks...");
					for (Iterator<BlockBean> itBlocks = blockDAO.iterator(); itBlocks
							.hasNext();) {
						loadBlock(itBlocks.next());
					}

					// First: create (regular) crossroads
					log.info("Loading crossroads...");
					for (Iterator<CrossroadBean> itCrossroads = crossroadDAO.iterator(); itCrossroads
							.hasNext();) {
						loadCrossroad(itCrossroads.next());
					}
					// Then: create bay crossroads
					for (Iterator<CrossroadBean> itCrossroads = crossroadDAO.iterator(); itCrossroads
							.hasNext();) {
						loadBayCrossroad(itCrossroads.next());
					}
					// Next: create roads
					log.info("Loading roads...");
					for (Iterator<RoadBean> itRoads = roadDAO.iterator(); itRoads
							.hasNext();) {
						loadRoad(itRoads.next());
					}
					// Finally: create bays
					for (Iterator<RoadBean> itRoads = roadDAO.iterator(); itRoads
							.hasNext();) {
						loadBay(itRoads.next());
					}
					log.info("Loading slots...");
					for (Iterator<SlotBean> itSlots = slotDAO.iterator(); itSlots
							.hasNext();) {
						loadSlot(itSlots.next());
					}
					addSlots(); // Commit slots creation
					log.info("Loading laser heads...");
					for (Iterator<LaserHeadBean> itLaserHeads = laserDAO.iterator(); itLaserHeads
							.hasNext();) {
						loadLaserHead(itLaserHeads.next());
					}

					log.info("Loading mission scheduler...");
					SchedulingAlgorithmBean schedulingAlgorithm = simBean.getSchedulingAlgorithm();
					//Scheduling algorithm
					loadSchedulingAlgorithm(schedulingAlgorithm);
					
					log.info("Loading straddleCarriers...");
					for (Iterator<StraddleCarrierBean> itStraddleCarriers = straddleCarrierDAO
							.iterator(); itStraddleCarriers.hasNext();) {
						loadStraddleCarrier(itStraddleCarriers.next());
					}

					log.info("Loading containers...");
					for (Iterator<ContainerBean> itContainers = containerDAO.iterator(); itContainers
							.hasNext();) {
						loadContainer(itContainers.next());
					}

					log.info("Loading events...");
					for (Iterator<EventBean> itEvents = EventDAO.getInstance(simBean.getContent()).iterator(); itEvents
							.hasNext();) {
						loadEvent(itEvents.next());
					}



//					Road r = Terminal.getInstance().getRoad("c7-c12");
//					Bay b = Terminal.getInstance().getBay("M-18/62");
//					RoadPoint rp = r.getIntersectNode(b);
//					try {
//						Terminal.getInstance().getStraddleCarrier("straddle_carrier_1").getRouting().getShortestPath(new Location(b,0.5, false), new Location(r, 0.5, false), new Time("00:35:00"));
//					} catch (NoPathFoundException e) {
//						// TODO Auto-generated catch block
//						e.printStackTrace();
//					}



					log.info("Scenario " + scenarioBean.getId() + " loaded with "
							+ blockDAO.size() + " blocks, " + crossroadDAO.size()
							+ " crossroads, " + roadDAO.size() + " roads.");

					MainFrame.getInstance().setSimReady();
				} catch (SQLException e) {
					log.fatal(e.getMessage(), e);
				}				
			}


		};

		t.start();
	}


	private void loadEvent(EventBean bean) {
		DynamicEvent event = DynamicEvent.create(bean);
		TimeScheduler.getInstance().registerDynamicEvent(event);
		log.trace("Event "+event.toString()+" loaded.");
	}

	private void loadSchedulingAlgorithm(SchedulingAlgorithmBean bean){

		MissionScheduler.rmiBindingName = bean.getName();
		MissionScheduler scheduler = MissionScheduler.getInstance();
		TimeScheduler.getInstance().recordDiscretObject(scheduler);
		switch(bean.getName()){
		case OnlineACOScheduler.rmiBindingName:
			MissionScheduler.setEvalParameters(OnlineACOParametersBean.getEvalParameters());
			break;
		case OfflineACOScheduler.rmiBindingName:
			MissionScheduler.setEvalParameters(OfflineACOParametersBean.getEvalParameters());
			break;
		case OfflineACOScheduler2.rmiBindingName:
			MissionScheduler.setEvalParameters(OfflineACO2ParametersBean.getEvalParameters());
			break;
		case LinearMissionScheduler.rmiBindingName:
			MissionScheduler.setEvalParameters(LinearParametersBean.getEvalParameters());
			break;
		case GreedyMissionScheduler.rmiBindingName:
			MissionScheduler.setEvalParameters(GreedyParametersBean.getEvalParameters());
			break;
		case RandomMissionScheduler.rmiBindingName:
			MissionScheduler.setEvalParameters(RandomParametersBean.getEvalParameters());
			break;
		case BranchAndBound.rmiBindingName:
			MissionScheduler.setEvalParameters(BranchAndBoundParametersBean.getEvalParameters());
			break;
		case BB.rmiBindingName:
			MissionScheduler.setEvalParameters(BBParametersBean.getEvalParameters());
			break;
		}
		
		/*switch (bean.getName()) {
		case OnlineACOScheduler.rmiBindingName:
			// Retrieve parameters
			scheduler = OnlineACOScheduler.getInstance();

			break;
		case OfflineACOScheduler.rmiBindingName:
			// Retrieve parameters
			//for (OfflineACOParametersBean parameter : OfflineACOParametersBean
			//		.values()) {
			//	if (atts.getIndex(parameter.name()) > 0) {
			//		String sValue = atts.getValue(parameter.name());
			//		try {
			//			Double dValue = Double.parseDouble(sValue);
			//			parameter.setValue(dValue);
			//			OfflineACOParametersDAO.getInstance(
			//					simulationID).insert(parameter);
			//		} catch (NumberFormatException e) {
			//			e.printStackTrace();
			//			log.error(e.getMessage(), e);
			//		}
			//	}
			//}
			break;
		case OfflineACOScheduler2.rmiBindingName:
			// Retrieve parameters
			break;
		case LinearMissionScheduler.rmiBindingName:
			// Retrieve parameters
			break;
		case GreedyMissionScheduler.rmiBindingName:
			// Retrieve parameters
			break;
		case RandomMissionScheduler.rmiBindingName:
			// Retrieve parameters
			break;
		case BranchAndBound.rmiBindingName:
			// Retrieve parameters
			break;
		case BB.rmiBindingName:
			// Retrieve parameters
			break;
		}*/
		if(scheduler != null){
			//Add display... ?
		}
	}

	private void loadStraddleCarrier(StraddleCarrierBean next) {
		StraddleCarrierSlot slot = Terminal.getInstance()
				.getStraddleCarrierSlot(next.getSlot());
		Location l = new Location(Terminal.getInstance().getRoad(
				next.getOriginRoad()), next.getOriginRate(),
				next.isOriginDirection());
		if (Terminal.getInstance().getStraddleCarrierModel(
				next.getModel().getName()) == null) {
			StraddleCarrierModelBean modelBean = next.getModel();
			StraddleCarrierModel straddleCarrierModel = new StraddleCarrierModel(
					modelBean.getName(), modelBean.getWidth(),
					modelBean.getHeight(), modelBean.getLength(),
					modelBean.getInnerWidth(), modelBean.getInnerLength(),
					modelBean.getBackOverLength(),
					modelBean.getFrontOverLength(), modelBean.getCabWidth(),
					modelBean.getCompatibility(), modelBean.getEmptySpeed(),
					modelBean.getLoadedSpeed(), modelBean.getBaySpeed(),
					new RandomSpeed(modelBean
							.getContainerHandlingFromTruckMin(), modelBean
							.getContainerHandlingFromTruckMax()),
							new RandomSpeed(modelBean
									.getContainerHandlingFromGroundMin(), modelBean
									.getContainerHandlingFromGroundMax()),
									new RandomSpeed(modelBean.getEnterExitBayTimeMin(),
											modelBean.getEnterExitBayTimeMax()),
											modelBean.getTurnBackTime());

			Terminal.getInstance()
			.addStraddleCarrierModel(straddleCarrierModel);
			log.debug("StraddleCarrierModel "+straddleCarrierModel.getId()+" added.");
		}

		StraddleCarrier straddleCarrier = new StraddleCarrier(next.getName(),
				slot, next.getModel().getName(), next.getColor(), l,
				next.isAutoHandling());

		//Workload
		MissionLoadDAO mld = MissionLoadDAO.getInstance(next.getName());
		Iterator<LoadBean> itLoads = mld.iterator();

		Map<Integer, Load> loads = new HashMap<>();

		while(itLoads.hasNext()){
			LoadBean loadBean = itLoads.next();
			Load load = new Load(new TimeWindow(loadBean.getTwMin(),loadBean.getTwMax()),
					terminal.getMission(loadBean.getMission()), 
					loadBean.getStartableTime());

			straddleCarrier.getWorkload().insert(load);
			loads.put(loadBean.getID(), load);
		}

		itLoads = mld.iterator();
		while(itLoads.hasNext()){
			LoadBean loadBean = itLoads.next();
			straddleCarrier.getWorkload().getLoad(loadBean.getMission()).setLinkedLoad(loads.get(loadBean.getLinkedLoad()));
		}

		//Routing
		Routing r = RoutingAlgorithmType.get(next.getRoutingAlgorithm(),next.getRoutingHeuristic()).getNewRoutingAlgorithm(straddleCarrier);
		straddleCarrier.setRoutingAlgorithm(r);	

		/*try {
			Path p = r.getShortestPath(new Location(Terminal.getInstance().getRoad("c2-c5"),0.2), new Location(Terminal.getInstance().getRoad("c59-c16"),0.5), new Time("00:00:02"));
			log.info(p.toString());
		} catch (NoPathFoundException e) {
			e.printStackTrace();
		}
		try {
			Path p2 = r.getShortestPath(new Location(Terminal.getInstance().getRoad("A-2/40"),0.5), new Location(Terminal.getInstance().getRoad("A-1/40"),0.2), new Time("00:00:02"));

			log.info(p2.toString());
		} catch (NoPathFoundException e) {
			e.printStackTrace();
		}*/
		log.debug("StraddleCarrier "+straddleCarrier.getId()+" added.");
	}

	private void loadContainer(ContainerBean bean) {
		try {
			if (bean.getVehicle() != null) {
				StraddleCarrier s = terminal.getStraddleCarrier(bean
						.getVehicle());
				s.setHandledContainerId(bean.getName());
				terminal.addContainer(bean.getName(), bean.getTeu(), null);
			} else {
				Slot s = terminal.getSlot(bean.getSlot());
				ContainerLocation cl = new ContainerLocation(bean.getName(),
						s.getPaveId(), s.getLocation().getRoad().getId(),
						s.getId(), bean.getSlotLevel(), bean.getAlignment()
						.getValue());
				terminal.addContainer(bean.getName(), bean.getTeu(), cl);
			}
			// if(terminal.listener!=null)
			// terminal.listener.containerAdded(c);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}

	private void loadLaserHead(LaserHeadBean bean) {
		LaserSystem.getInstance().addLaserHead(bean.getName(),
				new Coordinates(bean.getX(), bean.getY(), bean.getZ()),
				new Range(bean.getRx(), bean.getRy(), bean.getRz()));

		// terminal.addlaserHead(bean.getName());
	}

	private void loadSlot(SlotBean bean) {
		Bay b = terminal.getBay(bean.getBay());
		Block block = terminal.getBlock(b.getPaveId());
		BlockType type = block.getType();
		if (type == BlockType.DEPOT) {
			StraddleCarrierSlot s = new StraddleCarrierSlot(bean.getName(),
					new Location(b, bean.getRate()).getCoords(),
					(BayCrossroad) b.getOrigin(),
					(BayCrossroad) b.getDestination(), block.getId());
			terminal.addStraddleCarrierSlot(s);
		} else {
			Slot s = new Slot(bean.getName(), b,
					new Location(b, bean.getRate()), ContainerKind.getTeu(bean
							.getLen()), type);
			List<Slot> l = null;
			if (slots.containsKey(b.getId()))
				l = slots.get(b.getId());
			else {
				l = new ArrayList<>(20);
				slots.put(b.getId(), l);
			}
			l.add(s);
		}
	}

	private void addSlots() {
		for (Entry<String, List<Slot>> e : slots.entrySet()) {
			terminal.addSlots(e.getKey(), e.getValue());
		}
	}

	public void loadBayCrossroad(CrossroadBean bean) {
		if (bean.getType() == DbMgr.getInstance().getDatabaseIDsRetriever()
				.getCrossroadTypeID(BayCrossroad.class.getName())) {
			BayCrossroad c = new BayCrossroad(bean.getName(), new Coordinates(
					bean.getX(), bean.getY(), bean.getZ()), bean.getRoad());
			terminal.addBayCrossroad(c);
		}
		log.debug("BayCrossroad created: " + bean.getName());
	}

	public void loadCrossroad(CrossroadBean bean) {
		if (bean.getType() == DbMgr.getInstance().getDatabaseIDsRetriever()
				.getCrossroadTypeID(Crossroad.class.getName())) {
			Crossroad c = new Crossroad(bean.getName(), new Coordinates(
					bean.getX(), bean.getY(), bean.getZ()));
			terminal.addCrossroad(c);
		}
		log.debug("Crossroad created: " + bean.getName());
	}

	public void loadRoad(RoadBean bean) {
		if (bean.getType() == DbMgr.getInstance().getDatabaseIDsRetriever()
				.getRoadTypeID(Road.class.getName())) {
			Road r = new Road(bean.getName(), terminal.getCrossroad(bean
					.getOrigin()),
					terminal.getCrossroad(bean.getDestination()),
					bean.isDirected());
			Iterator<RoadPointBean> roadPointsIterator = bean
					.roadPointsIterator();
			while (roadPointsIterator.hasNext()) {
				RoadPointBean rpBean = roadPointsIterator.next();
				RoadPoint rp = new RoadPoint(rpBean.getName(), new Coordinates(
						rpBean.getX(), rpBean.getY(), rpBean.getZ()));
				terminal.addRoadPoint(rp);
				r.addRoadPoint(rp);

			}

			terminal.addRoad(r);
		}
		log.debug("Road created: " + bean.getName());
	}

	public void loadBay(RoadBean bean) {
		if (bean.getType() != DbMgr.getInstance().getDatabaseIDsRetriever()
				.getRoadTypeID(Road.class.getName())) {
			
			BayCrossroad origin = (BayCrossroad) Terminal
					.getInstance().getCrossroad(bean.getOrigin());
			
			
			BayCrossroad destination = (BayCrossroad) Terminal
					.getInstance().getCrossroad(bean.getDestination());
			
			
			Bay b = new Bay(bean.getName(), origin, destination, bean.isDirected(),
							bean.getBlock());
			
			if(origin.getMainRoad()!=null){
				Road mrOrigin = terminal.getRoad(origin.getMainRoad());
				mrOrigin.addLaneCrossroad(origin);
			}
			
			if(destination.getMainRoad()!=null){
				Road mrDestination = terminal.getRoad(destination.getMainRoad());
				mrDestination.addLaneCrossroad(destination);
			}
			
			
			/*if(origin.getMainRoad().equals(destination.getMainRoad())){
				//Use closest one
				double o = Location.getPourcent(origin.getLocation(), mrOrigin);
				double d = Location.getPourcent(destination.getLocation(), mrOrigin);
				if(o < d) mrOrigin.addLaneCrossroad(origin);
				else mrDestination.addLaneCrossroad(destination); 
			} else {
				
			}*/
			
			terminal.addBay(b);
		}
		log.debug("Bay created: " + bean.getName());
	}

	public void loadBlock(BlockBean bean) {
		Block pave = null;

		BlockType type = bean.getType();
		if (type == BlockType.DEPOT) {
			pave = new Depot(bean.getName());
		} else if (type == BlockType.SHIP) {
			pave = new Quay(bean.getName(), bean.getSeaOrientation(),
					bean.getBorder_road());
		} else
			pave = new Block(bean.getName(), bean.getType());

		for (Entry<String, Coordinates> e : bean.getPoints()) {
			pave.addCoords(e.getKey(), e.getValue());
		}
		Iterator<String> walls = bean.getWalls();
		while (walls.hasNext()) {
			StringTokenizer st = new StringTokenizer(walls.next(),
					BlockBean.WALLS_SEPARATOR);
			String from = st.nextToken();
			String to = st.nextToken();
			pave.addWall(from, to);
		}

		// System.out.println("Adding pave "+pave.getId()+" type "+pave.getType());
		if (type == BlockType.DEPOT) {
			terminal.addDepot((Depot) pave);
			log.debug("Depot created: " + pave.getId());
		} else {
			try {
				terminal.addPave(pave);
			} catch (Exception e) {
				log.error(e.getMessage(), e);
			}
			log.debug("Block created: " + pave.getId());
		}
	}

}
