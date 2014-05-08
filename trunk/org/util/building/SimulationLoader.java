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
import org.com.model.scheduling.ParameterBean;
import org.com.model.scheduling.RandomParametersBean;
import org.com.model.scheduling.SchedulingParametersBeanInterface;
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

	private Map<String, List<Slot>> slots;

	private SimulationLoader() {
		slots = new HashMap<>(1000);
	}

	public static final SimulationLoader getInstance() {
		if (instance == null) {
			instance = new SimulationLoader();
		}
		return instance;
	}

	public static void closeInstance(){
		if(instance != null){
			instance.slots.clear();
			instance = null;
		}
	}

	public void loadTerminal (final TerminalBean terminalBean){
		try{
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

		} catch (SQLException e) {
			log.fatal(e.getMessage(), e);
		}

	}

	public void loadScenario(final ScenarioBean scenarioBean, Long seed, Integer simID) throws SQLException{
		// Load Terminal
		
		TerminalBean terminalBean = TerminalDAO.getInstance().getTerminal(
				scenarioBean.getTerminal());
		loadTerminal(terminalBean);
		
		if(simID != null){
			Terminal.getInstance().setSimulationID(simID);
		}
		
		if(seed != null)
			Terminal.getInstance().setSeed(seed);

		//LaserHeads
		LaserHeadDAO laserDAO = LaserHeadDAO.getInstance(scenarioBean
				.getId());
		laserDAO.load();

		// Load containers
		ContainerDAO containerDAO = ContainerDAO.getInstance(scenarioBean
				.getId());
		containerDAO.load();


		// Load straddle carriers
		StraddleCarrierDAO straddleCarrierDAO = StraddleCarrierDAO
				.getInstance(scenarioBean.getId());
		straddleCarrierDAO.load();

		log.info("Loading laser heads...");
		for (Iterator<LaserHeadBean> itLaserHeads = laserDAO.iterator(); itLaserHeads
				.hasNext();) {
			loadLaserHead(itLaserHeads.next());
		}

	}

	public void load(final SimulationBean simBean) {
		log.info("Loading simulation " + simBean.getId()+"...");
		// Load Scenario
		ScenarioBean scenarioBean = ScenarioDAO.getInstance().getScenario(
				simBean.getContent());

		try {
			loadScenario(scenarioBean, simBean.getSeed(), simBean.getId());
			// Load mission scheduler
			// Load events
			EventDAO eventDAO = EventDAO.getInstance(scenarioBean.getId());
			eventDAO.load();

			log.info("Loading mission scheduler...");
			SchedulingAlgorithmBean schedulingAlgorithm = simBean.getSchedulingAlgorithm();
			//Scheduling algorithm
			loadSchedulingAlgorithm(schedulingAlgorithm);

			log.info("Loading straddleCarriers...");
			for (Iterator<StraddleCarrierBean> itStraddleCarriers = StraddleCarrierDAO.getInstance(scenarioBean.getId()).iterator(); itStraddleCarriers.hasNext();) {
				loadStraddleCarrier(itStraddleCarriers.next());
			}

			log.info("Loading containers...");
			for (Iterator<ContainerBean> itContainers = ContainerDAO.getInstance(scenarioBean.getId()).iterator(); itContainers
					.hasNext();) {
				loadContainer(itContainers.next());
			}

			log.info("Loading events...");
			for (Iterator<EventBean> itEvents = EventDAO.getInstance(simBean.getContent()).iterator(); itEvents
					.hasNext();) {
				loadEvent(itEvents.next());
			}



			//					Road r = Terminal.getInstance().getInstance().getRoad("c7-c12");
			//					Bay b = Terminal.getInstance().getInstance().getBay("M-18/62");
			//					RoadPoint rp = r.getIntersectNode(b);
			//					try {
			//						Terminal.getInstance().getInstance().getStraddleCarrier("straddle_carrier_1").getRouting().getShortestPath(new Location(b,0.5, false), new Location(r, 0.5, false), new Time("00:35:00"));
			//					} catch (NoPathFoundException e) {
			//						// TODO Auto-generated catch block
			//						e.printStackTrace();
			//					}



			log.info("Scenario " + scenarioBean.getId() + " loaded with "
					+ BlockDAO.getInstance(scenarioBean.getTerminal()).size() + " blocks, " + CrossroadDAO.getInstance(scenarioBean.getTerminal()).size()
					+ " crossroads, " + RoadDAO.getInstance(scenarioBean.getTerminal()).size() + " roads.");

			MainFrame.getInstance().setSimReady();
			log.info("READY.");
		} catch (SQLException e) {
			log.fatal(e.getMessage(), e);
		}				
	}


	private void loadEvent(EventBean bean) {
		DynamicEvent event = DynamicEvent.create(bean);
		TimeScheduler.getInstance().registerDynamicEvent(event);
		log.trace("Event "+event.toString()+" loaded.");
	}

	private void loadSchedulingAlgorithm(SchedulingAlgorithmBean bean){
		MissionScheduler.rmiBindingName = bean.getName();
		for(Entry<String,ParameterBean> paramName : bean.getParameters().entrySet()){
			SchedulingParametersBeanInterface parameter = null;
			switch (bean.getName()) {
			case OnlineACOScheduler.rmiBindingName:
				parameter = OnlineACOParametersBean.get(paramName.getKey());
				break;
			case LinearMissionScheduler.rmiBindingName:
				parameter = LinearParametersBean.get(paramName.getKey());
				break;
			case RandomMissionScheduler.rmiBindingName:
				parameter = RandomParametersBean.get(paramName.getKey());
				break;
			case GreedyMissionScheduler.rmiBindingName:
				parameter = GreedyParametersBean.get(paramName.getKey());
				break;
			case BB.rmiBindingName:
				parameter = BBParametersBean.get(paramName.getKey());
				break;
			case BranchAndBound.rmiBindingName:
				parameter = BranchAndBoundParametersBean.get(paramName.getKey());
				break;
			case OfflineACOScheduler.rmiBindingName:
				parameter = OfflineACOParametersBean.get(paramName.getKey());
				break;
			case OfflineACOScheduler2.rmiBindingName:
				parameter = OfflineACO2ParametersBean.get(paramName.getKey());
				break;
			}
			if (parameter != null) {
				parameter.setValue(paramName.getValue().getValueAsString());
			}
		}

		switch (bean.getName()) {
		case OnlineACOScheduler.rmiBindingName:
			OnlineACOParametersBean.T.setValue(bean.getEvalParameters().T());
			OnlineACOParametersBean.L.setValue(bean.getEvalParameters().L());
			OnlineACOParametersBean.E.setValue(bean.getEvalParameters().E());
			break;
		case LinearMissionScheduler.rmiBindingName:
			LinearParametersBean.T.setValue(bean.getEvalParameters().T());
			LinearParametersBean.L.setValue(bean.getEvalParameters().L());
			LinearParametersBean.E.setValue(bean.getEvalParameters().E());
			break;
		case RandomMissionScheduler.rmiBindingName:
			RandomParametersBean.T.setValue(bean.getEvalParameters().T());
			RandomParametersBean.L.setValue(bean.getEvalParameters().L());
			RandomParametersBean.E.setValue(bean.getEvalParameters().E());
			break;
		case GreedyMissionScheduler.rmiBindingName:
			GreedyParametersBean.T.setValue(bean.getEvalParameters().T());
			GreedyParametersBean.L.setValue(bean.getEvalParameters().L());
			GreedyParametersBean.E.setValue(bean.getEvalParameters().E());
			break;
		case BB.rmiBindingName:
			BBParametersBean.T.setValue(bean.getEvalParameters().T());
			BBParametersBean.L.setValue(bean.getEvalParameters().L());
			BBParametersBean.E.setValue(bean.getEvalParameters().E());
			break;
		case BranchAndBound.rmiBindingName:
			BranchAndBoundParametersBean.T.setValue(bean.getEvalParameters().T());
			BranchAndBoundParametersBean.L.setValue(bean.getEvalParameters().L());
			BranchAndBoundParametersBean.E.setValue(bean.getEvalParameters().E());
			break;
		case OfflineACOScheduler.rmiBindingName:
			OfflineACOParametersBean.T.setValue(bean.getEvalParameters().T());
			OfflineACOParametersBean.L.setValue(bean.getEvalParameters().L());
			OfflineACOParametersBean.E.setValue(bean.getEvalParameters().E());
			break;
		case OfflineACOScheduler2.rmiBindingName:
			OfflineACO2ParametersBean.T.setValue(bean.getEvalParameters().T());
			OfflineACO2ParametersBean.L.setValue(bean.getEvalParameters().L());
			OfflineACO2ParametersBean.E.setValue(bean.getEvalParameters().E());
			break;
		}
		//		MissionScheduler.setEvalParameters(bean.getEvalParameters());
		/*MissionScheduler scheduler = */MissionScheduler.getInstance();
		//TimeScheduler.getInstance().recordDiscretObject(scheduler);
	}

	public void loadStraddleCarrier(StraddleCarrierBean next) {
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

			Terminal.getInstance().addStraddleCarrierModel(straddleCarrierModel);
			log.trace("StraddleCarrierModel "+straddleCarrierModel.getId()+" added.");
		}

		StraddleCarrier straddleCarrier = new StraddleCarrier(next.getName(),
				slot, next.getModel().getName(), next.getColor(), l/*,
				next.isAutoHandling()*/);

		//Workload
		MissionLoadDAO mld = MissionLoadDAO.getInstance(next.getName());
		Iterator<LoadBean> itLoads = mld.iterator();

		Map<Integer, Load> loads = new HashMap<>();

		while(itLoads.hasNext()){
			LoadBean loadBean = itLoads.next();
			Load load = new Load(new TimeWindow(loadBean.getTwMin(),loadBean.getTwMax()),
					Terminal.getInstance().getMission(loadBean.getMission()), 
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
			Path p = r.getShortestPath(new Location(Terminal.getInstance().getInstance().getRoad("c2-c5"),0.2), new Location(Terminal.getInstance().getInstance().getRoad("c59-c16"),0.5), new Time("00:00:02"));
			log.info(p.toString());
		} catch (NoPathFoundException e) {
			e.printStackTrace();
		}
		try {
			Path p2 = r.getShortestPath(new Location(Terminal.getInstance().getInstance().getRoad("A-2/40"),0.5), new Location(Terminal.getInstance().getInstance().getRoad("A-1/40"),0.2), new Time("00:00:02"));

			log.info(p2.toString());
		} catch (NoPathFoundException e) {
			e.printStackTrace();
		}*/
		log.trace("StraddleCarrier "+straddleCarrier.getId()+" added.");
	}

	public void loadContainer(ContainerBean bean) {
		try {
			if (bean.getVehicle() != null) {
				StraddleCarrier s = Terminal.getInstance().getStraddleCarrier(bean
						.getVehicle());
				s.setHandledContainerId(bean.getName());
				Terminal.getInstance().addContainer(bean.getName(), bean.getTeu(), null);
			} else {
				Slot s = Terminal.getInstance().getSlot(bean.getSlot());
				ContainerLocation cl = new ContainerLocation(bean.getName(),
						s.getPaveId(), s.getLocation().getRoad().getId(),
						s.getId(), bean.getSlotLevel(), bean.getAlignment()
						.getValue());
				Terminal.getInstance().addContainer(bean.getName(), bean.getTeu(), cl);
			}
			// if(Terminal.getInstance().listener!=null)
			// Terminal.getInstance().listener.containerAdded(c);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}

	private void loadLaserHead(LaserHeadBean bean) {
		LaserSystem.getInstance().addLaserHead(bean.getName(),
				new Coordinates(bean.getX(), bean.getY(), bean.getZ()),
				new Range(bean.getRx(), bean.getRy(), bean.getRz()));

		// Terminal.getInstance().addlaserHead(bean.getName());
	}

	private void loadSlot(SlotBean bean) {
		Bay b = Terminal.getInstance().getBay(bean.getBay());
		Block block = Terminal.getInstance().getBlock(b.getPaveId());
		BlockType type = block.getType();
		if (type == BlockType.DEPOT) {
			StraddleCarrierSlot s = new StraddleCarrierSlot(bean.getName(),
					new Location(b, bean.getRate()).getCoords(),
					(BayCrossroad) b.getOrigin(),
					(BayCrossroad) b.getDestination(), block.getId());
			Terminal.getInstance().addStraddleCarrierSlot(s);
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
			Terminal.getInstance().addSlots(e.getKey(), e.getValue());
		}
	}

	public void loadBayCrossroad(CrossroadBean bean) {
		if (bean.getType() == DbMgr.getInstance().getDatabaseIDsRetriever()
				.getCrossroadTypeID(BayCrossroad.class.getName())) {
			BayCrossroad c = new BayCrossroad(bean.getName(), new Coordinates(
					bean.getX(), bean.getY(), bean.getZ()), bean.getRoad());
			Terminal.getInstance().addBayCrossroad(c);
		}
		log.trace("BayCrossroad created: " + bean.getName());
	}

	public void loadCrossroad(CrossroadBean bean) {
		if (bean.getType() == DbMgr.getInstance().getDatabaseIDsRetriever()
				.getCrossroadTypeID(Crossroad.class.getName())) {
			Crossroad c = new Crossroad(bean.getName(), new Coordinates(
					bean.getX(), bean.getY(), bean.getZ()));
			Terminal.getInstance().addCrossroad(c);
		}
		log.trace("Crossroad created: " + bean.getName());
	}

	public void loadRoad(RoadBean bean) {
		if (bean.getType() == DbMgr.getInstance().getDatabaseIDsRetriever()
				.getRoadTypeID(Road.class.getName())) {
			Road r = new Road(bean.getName(), Terminal.getInstance().getCrossroad(bean
					.getOrigin()),
					Terminal.getInstance().getCrossroad(bean.getDestination()),
					bean.isDirected());
			Iterator<RoadPointBean> roadPointsIterator = bean
					.roadPointsIterator();
			while (roadPointsIterator.hasNext()) {
				RoadPointBean rpBean = roadPointsIterator.next();
				RoadPoint rp = new RoadPoint(rpBean.getName(), new Coordinates(
						rpBean.getX(), rpBean.getY(), rpBean.getZ()));
				Terminal.getInstance().addRoadPoint(rp);
				r.addRoadPoint(rp);

			}
			
			Terminal.getInstance().addRoad(r);
		}
		log.trace("Road created: " + bean.getName());
	}

	public void loadBay(RoadBean bean) {
		if (bean.getType() != DbMgr.getInstance().getDatabaseIDsRetriever()
				.getRoadTypeID(Road.class.getName())) {

			BayCrossroad origin = (BayCrossroad) Terminal
					.getInstance().getCrossroad(bean.getOrigin());


			BayCrossroad destination = (BayCrossroad) Terminal
					.getInstance().getCrossroad(bean.getDestination());


			Bay b = new Bay(bean.getName(), origin, destination, bean.isDirected(),
					bean.getBlock(), bean.getGroup());

			if(origin.getMainRoad()!=null){
				Road mrOrigin = Terminal.getInstance().getRoad(origin.getMainRoad());
				mrOrigin.addLaneCrossroad(origin);
			}

			if(destination.getMainRoad()!=null){
				Road mrDestination = Terminal.getInstance().getRoad(destination.getMainRoad());
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

			Terminal.getInstance().addBay(b);
		}
		log.trace("Bay created: " + bean.getName());
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
			Terminal.getInstance().addDepot((Depot) pave);
			log.trace("Depot created: " + pave.getId());
		} else {
			try {
				Terminal.getInstance().addPave(pave);
			} catch (Exception e) {
				log.error(e.getMessage(), e);
			}
			log.trace("Block created: " + pave.getId());
		}
	}

}
