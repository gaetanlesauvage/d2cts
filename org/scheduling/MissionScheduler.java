package org.scheduling;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.log4j.Logger;
import org.display.TextDisplay;
import org.exceptions.EmptyResourcesException;
import org.exceptions.NoPathFoundException;
import org.missions.Mission;
import org.routing.path.Path;
import org.scheduling.bb.BB;
import org.scheduling.branchAndBound.BranchAndBound;
import org.scheduling.display.IndicatorPane;
import org.scheduling.display.JMissionScheduler;
import org.scheduling.greedy.GreedyMissionScheduler;
import org.scheduling.offlineACO.OfflineACOScheduler;
import org.scheduling.offlineACO2.OfflineACOScheduler2;
import org.scheduling.onlineACO.OnlineACOScheduler;
import org.scheduling.random.RandomMissionScheduler;
import org.system.Terminal;
import org.system.container_stocking.Container;
import org.system.container_stocking.ContainerLocation;
import org.system.container_stocking.Slot;
import org.time.DiscretObject;
import org.time.Time;
import org.time.TimeScheduler;
import org.util.Location;
import org.vehicles.StraddleCarrier;

public abstract class MissionScheduler implements DiscretObject {
	public static final Logger log = Logger.getLogger(MissionScheduler.class);

	/**
	 * Tasks
	 */
	protected TreeSet<Mission> pool;

	/**
	 * Resources
	 */
	protected List<StraddleCarrier> resources;

	protected static MissionScheduler instance;

	public static final double CLOSE_TO_ZERO = 0.0001;

	// DEBUG
	// protected static PrintWriter debug;
	// protected static PrintWriter traceScores;

	// RMI
	public static String rmiBindingName = "MissionScheduler";

	protected TextDisplay out;

	protected JMissionScheduler jms;

	public static boolean DEBUG = false;

	/**
	 * Current step of the algorithm
	 */
	protected static volatile long step;

	/**
	 * Current step of the simulation
	 */
	protected static long sstep;

	// TASKS BUFFERS
	protected SortedMap<Time, List<Mission>> missionsToUpdate;
	protected SortedMap<Time, List<Mission>> missionsToAdd;
	protected SortedMap<Time, List<Mission>> missionsToRemove;
	protected SortedMap<Time, Set<MissionStartedHelper>> missionsToStart;

	// RESOURCES BUFFERS
	protected SortedMap<Time, Map<String, UpdateInfo>> resourceToUpdate;
	protected SortedMap<Time, List<StraddleCarrier>> resourceToAdd;
	protected SortedMap<Time, List<String>> resourceToRemove;

	// RESOURCES
	protected Map<String, List<String>> resourceModels;
	protected Map<String, StraddleCarrier> vehicles;

	// TASKS
	protected Map<String, ScheduleTask<? extends ScheduleEdge>> scheduleTasks;
	// RESOURCES
	protected Map<String, ScheduleResource> scheduleResources = new HashMap<>();

	protected GlobalScore currentScore;

	protected Lock lock;
	protected boolean precomputed;

	protected boolean graphChanged;
	protected int graphChangedByUpdate;

	protected long computeTime;

	protected MissionSchedulerEvalParameters evalParameters;
	protected GlobalScore best;
	protected Set<String> visited;

	public static ScheduleTask<? extends ScheduleEdge> SOURCE_NODE;

	protected static boolean init = false;

	// private GlobalSolution currentSolution;

	/* ======================== SINGLETON HANDLER ======================== */
	public static MissionScheduler getInstance() {
		if (instance == null) {
			switch (rmiBindingName) {
			case LinearMissionScheduler.rmiBindingName:
				instance = new LinearMissionScheduler();
				break;
			case RandomMissionScheduler.rmiBindingName:
				instance = new RandomMissionScheduler();
				break;
			case GreedyMissionScheduler.rmiBindingName:
				instance = new GreedyMissionScheduler();
				break;
			case BranchAndBound.rmiBindingName:
				instance = new BranchAndBound();
				break;
			case BB.rmiBindingName:
				instance = new BB();
				break;
			case OnlineACOScheduler.rmiBindingName:
				instance = new OnlineACOScheduler();
				break;
			case OfflineACOScheduler.rmiBindingName:
				instance = new OfflineACOScheduler();
				break;
			case OfflineACOScheduler2.rmiBindingName:
				instance = new OfflineACOScheduler2();
				break;
			default:
				Exception e = new Exception("WRONG MISSION SCHEDULER SPECIFIED !");
				log.fatal(e.getMessage(), e);
				break;
			}
		}
		return instance;
	}

	public static void closeInstance() {
		if (instance != null) {
			switch (rmiBindingName) {
			case LinearMissionScheduler.rmiBindingName:
				LinearMissionScheduler.closeInstance();
				break;
			case RandomMissionScheduler.rmiBindingName:
				RandomMissionScheduler.closeInstance();
				break;
			case GreedyMissionScheduler.rmiBindingName:
				GreedyMissionScheduler.closeInstance();
				;
				break;
			case BranchAndBound.rmiBindingName:
				BranchAndBound.closeInstance();
				break;
			case BB.rmiBindingName:
				BB.closeInstance();
				break;
			case OnlineACOScheduler.rmiBindingName:
				OnlineACOScheduler.closeInstance();
				break;
			case OfflineACOScheduler.rmiBindingName:
				OfflineACOScheduler.closeInstance();
				break;
			case OfflineACOScheduler2.rmiBindingName:
				OfflineACOScheduler2.closeInstance();
				break;
			default:
				Exception e = new Exception("WRONG MISSION SCHEDULER SPECIFIED !");
				log.fatal(e.getMessage(), e);
				break;
			}
			instance.destroy();
			instance = null;
			init = false;
		}

	}

	/* ======================== CONSTRUCTORS ======================== */
	/**
	 * Constructor
	 */
	protected MissionScheduler() {
		this.pool = new TreeSet<>();
		this.resources = new ArrayList<>();
		this.vehicles = new HashMap<>();

		lock = new ReentrantLock();
		SOURCE_NODE = new ScheduleTask<ScheduleEdge>((Mission) null);

		missionsToUpdate = new TreeMap<>();
		missionsToAdd = new TreeMap<>();
		missionsToRemove = new TreeMap<>();
		missionsToStart = new TreeMap<>();

		// RESOURCES BUFFERS
		resourceToUpdate = new TreeMap<>();
		resourceToAdd = new TreeMap<>();
		resourceToRemove = new TreeMap<>();

		// RESOURCES
		resourceModels = new HashMap<>();
		vehicles = new HashMap<>();

		// TASKS
		scheduleTasks = new HashMap<>();
		// RESOURCES
		scheduleResources = new HashMap<>();

		currentScore = new GlobalScore();
		visited = new HashSet<String>();

		TimeScheduler.getInstance().recordMissionsScheduler(this);
	}

	public void resetVisited() {
		visited.clear();
	}

	public boolean hasBeenVisited(String taskID) {
		return visited.contains(taskID);
	}

	/**
	 * Get the travel time between two nodes (representing either missions or
	 * the depot) for a given straddle carrier
	 * 
	 * @param from
	 *            Origin node. If it is a mission node then it represents the
	 *            delivery location of this mission
	 * @param to
	 *            Target node. If it is a mission node then it represents the
	 *            pickup location of this mission
	 * @param rsc
	 *            The vehicle
	 * @return The time needed to go from the origin node to the destination
	 *         node for the given vehicle @ * @throws EmptyResourcesException
	 */
	public Path getTravelPath(Mission from, Mission to, StraddleCarrier rsc) throws EmptyResourcesException {
		if (resources.size() == 0)
			throw new EmptyResourcesException();

		Path shortestPath = null;
		Location fromLocation = null;
		Location toLocation = null;

		if (from != null) {
			ContainerLocation clFrom = from.getDestination();
			Slot slotFrom = Terminal.getInstance().getSlot(clFrom.getSlotId());
			fromLocation = slotFrom.getLocation();
		} else {
			if (rsc.getCurrentLoad() != null) {
				ContainerLocation cl = rsc.getCurrentLoad().getMission().getDestination();
				Slot slotFrom = Terminal.getInstance().getSlot(cl.getSlotId());
				fromLocation = slotFrom.getLocation();
			} else {
				fromLocation = rsc.getLocation();
			}
		}

		if (to != null) {
			Container cTo = to.getContainer();
			if (cTo == null || cTo.getLocation() == null){
					toLocation = null;	
			}
			
			else {
				toLocation = cTo.getLocation();
			}
		} else {
			toLocation = rsc.getSlot().getLocation();
		}

		try {
			shortestPath = rsc.getRouting().getShortestPath(fromLocation, toLocation);
		} catch (NoPathFoundException e) {
			e.printStackTrace();
			log.error(e.getMessage(), e);
		}

		return shortestPath;
	}

	/*
	 * ============================== INITIALIZATION
	 * ==============================
	 */
	/**
	 * Initialize both the algorithm and the GUI. @
	 */
	protected abstract void init();

	/*
	 * ============================== DISCRETE PART OF THE SCHEDULER
	 * ==============================
	 */
	@Override
	/**
	 * Precompute is called by the TimeScheduler at each step of simulation.
	 */
	public abstract void precompute();

	@Override
	/**
	 * Apply is called by the TimeScheduler at each step of the simulation after calling precompute() method of each DiscretObject of the scheduler. 
	 */
	public abstract boolean apply();

	/*
	 * ============================== CONTINOUS PART OF THE SCHEDULER
	 * ==============================
	 */
	/**
	 * Compute the ACO algorithm $syncSize times
	 */
	public abstract void compute();

	/* ========================== FLUSHERS ========================== */
	protected void processEvents() {
		flushResources();
		flushMissions();
	}

	/**
	 * Flush buffers of events (add, update or remove) on missions
	 */
	private void flushMissions() {
		// TO START
		Set<Time> start = new HashSet<>(missionsToStart.keySet());
		for (Time t : start) {
			if (t.toStep() <= sstep) {
				Set<MissionStartedHelper> toStart = missionsToStart.remove(t);
				for (MissionStartedHelper m : toStart) {
					log.info("Removing started mission " + m.m.getId());
					missionStarted(m.m, m.resourceID);

				}
			} else {
				log.error("Time overspent!");
				break;
			}

		}
		// TO REMOVE
		List<Time> rm = new ArrayList<>(missionsToRemove.keySet());
		for (Time t : rm) {
			if (t.toStep() <= sstep) {
				List<Mission> toRemove = missionsToRemove.remove(t);
				for (Mission m : toRemove) {
					log.trace("Removing mission " + m.getId());
					removeMission(m);
				}
			} else {
				log.error("Time overspent!");
				break;
			}
		}

		// TO UPDATE
		List<Time> up = new ArrayList<>(missionsToUpdate.keySet());
		for (Time t : up) {
			if (t.toStep() <= sstep) {
				List<Mission> toUpdate = missionsToUpdate.remove(t);
				for (Mission m : toUpdate) {
					log.trace("Updating mission " + m.getId());
					updateMission(m);

				}
			} else {
				log.error("Time overspent!");
				break;
			}
		}

		// TO ADD
		List<Time> ad = new ArrayList<>(missionsToAdd.keySet());
		for (Time t : ad) {
			if (t.toStep() <= sstep) {
				List<Mission> toAdd = missionsToAdd.remove(t);
				for (Mission m : toAdd) {
					log.trace("Inserting mission " + m.getId());
					insertMission(m);
				}
			} else {
				log.error("Time overspent!");
				break;
			}
		}
	}

	/**
	 * Flush buffers of events on resources (add, update or remove)
	 */
	private void flushResources() {
		// TO REMOVE
		for (Time t : new ArrayList<>(resourceToRemove.keySet())) {
			if (t.toStep() <= sstep) {
				List<String> toRemove = resourceToRemove.remove(t);
				for (String s : toRemove) {
					removeResource(s);
				}
			} else {
				log.error("Time overspent!");
				break;
			}
		}

		// TO UPDATE
		for (Time t : new ArrayList<>(resourceToUpdate.keySet())) {
			if (t.toStep() <= sstep) {
				Map<String, UpdateInfo> toUpdate = resourceToUpdate.remove(t);
				for (String s : toUpdate.keySet()) {
					updateResourceLocation(s, toUpdate.get(s));
				}
			} else {
				//resourceToUpdate.remove(t);
				log.error("Time overspent!");
				break;
			}
		}

		// TO ADD
		for (Time t : new ArrayList<>(resourceToAdd.keySet())) {
			if (t.toStep() <= sstep) {
				List<StraddleCarrier> toAdd = resourceToAdd.remove(t);
				for (StraddleCarrier rsc : toAdd) {
					insertResource(rsc);
				}
			} else {
				log.error("Time overspent!");
				break;
			}
		}
	}

	/*
	 * ========================== TASKS AND RESOURCES OPERATIONS
	 * ==========================
	 */
	/**
	 * Insert a vehicle as a resource in the algorithm.
	 * 
	 * @param rsc
	 *            The straddle carrier to add.
	 */
	protected void insertResource(StraddleCarrier rsc) {
		String modelID = "";
		String rscID = "";

		modelID = rsc.getModel().getId();
		rscID = rsc.getId();

		resources.add(rsc);
		vehicles.put(rscID, rsc);
		if (jms != null)
			jms.addResource(rsc);

		ScheduleResource sr = new ScheduleResource(rsc);
		scheduleResources.put(rscID, sr);

		// Compute costs for rsc
		boolean computeCosts = true;
		for (String model : resourceModels.keySet()) {
			if (model.equals(modelID)) {
				computeCosts = false;
				break;
			}
		}

		List<String> lModel = null;
		if (computeCosts) {
			lModel = new ArrayList<String>();
			for (ScheduleTask<? extends ScheduleEdge> n : scheduleTasks.values()) {
				n.addCostFor(rsc);
			}
		} else {
			lModel = resourceModels.get(modelID);
		}

		lModel.add(rscID);
		resourceModels.put(modelID, lModel);
		System.out.println("MS:> Resource " + rscID + " (" + modelID + ") added");
	}

	/**
	 * Remove a vehicle from available resources of the algorithm.
	 * 
	 * @param rsc
	 *            The straddle carrier to remove.
	 */
	protected boolean removeResource(String resourceID) {
		for (int i = 0; i < resources.size(); i++) {

			if (resources.get(i).getId().equals(resourceID)) {
				resources.remove(i);
				String modelID = vehicles.get(resourceID).getModel().getId();
				List<String> vehiclesOfSameModel = resourceModels.remove(modelID);
				List<String> newList = new ArrayList<String>(vehiclesOfSameModel);
				for (String vID : resourceModels.get(modelID)) {
					if (!vID.equals(resourceID)) {
						newList.add(vID);
					}
				}
				if (newList.size() > 0)
					resourceModels.put(modelID, newList);
				return true;
			}

		}
		return false;
	}

	public void incrementNumberOfCompletedMissions(String resourceID) {
		IndicatorPane ip = jms.getIndicatorPane();
		if (ip != null) {
			ip.incNbOfCompletedMissions(resourceID);
		} else
			new Exception("INDICATOR PANE IS NULL").printStackTrace();
	}

	public void updateOverspentTime(String resourceID, Time timeToAdd) {
		IndicatorPane ip = getIndicatorPane();
		if (ip != null)
			ip.addOverspentTime(resourceID, timeToAdd);
	}

	public void updateWaitTime(final String resourceID, final Time timeToAdd) {
		IndicatorPane ip = getIndicatorPane();
		if (ip != null)
			ip.addWaitTime(resourceID, timeToAdd);
	}

	/**
	 * Ask for an update of the location of the given vehicle
	 * 
	 * @param resourceID
	 *            Vehicle ID
	 * @param distance
	 *            Distance made since the last update
	 */
	protected void updateResourceLocation(String resourceID, UpdateInfo updateInfo) {
		// System.err.println("URL : "+resourceID+" "+updateInfo.distance+" "+updateInfo.travelTime);
		// new Exception("UPDATE RESOURCE LOCATION AND DISTANCE");
		IndicatorPane ip = jms.getIndicatorPane();
		if (ip != null) {
			ip.addDistance(resourceID, updateInfo.distance, updateInfo.travelTime);

		}

	}

	/**
	 * Insert mission
	 * 
	 * @param m
	 *            Mission to insert
	 */
	@SuppressWarnings("unchecked")
	protected void insertMission(Mission m) {
		pool.add(m);

		ScheduleTask<ScheduleEdge> n = new ScheduleTask<ScheduleEdge>(m);
		for (String modelID : resourceModels.keySet()) {
			try {
				Path p = getTravelPath(m, m, vehicles.get(resourceModels.get(modelID).get(0)));
				n.addMissionCostFor(modelID, p.getCost());
				n.setDistance(p.getCostInMeters());
			} catch (EmptyResourcesException e) {
				e.printStackTrace();
			}
		}

		for (Mission m2 : pool) {
			if (!m2.getId().equals(m.getId())) {
				ScheduleTask<ScheduleEdge> n2 = (ScheduleTask<ScheduleEdge>) scheduleTasks.get(m2.getId());
				ScheduleEdge e = new ScheduleEdge(n, n2);
				ScheduleEdge e2 = new ScheduleEdge(n2, n);

				for (String modelID : resourceModels.keySet()) {
					e.addCost(vehicles.get(resourceModels.get(modelID).get(0)));
					n.addDestination(e);
					e2.addCost(vehicles.get(resourceModels.get(modelID).get(0)));
					n2.addDestination(e2);
				}
			}
		}

		ScheduleEdge e3 = new ScheduleEdge(MissionScheduler.SOURCE_NODE, n);
		ScheduleEdge e4 = new ScheduleEdge(n, MissionScheduler.SOURCE_NODE);

		for (String vehicleID : vehicles.keySet()) {
			e3.addCost(vehicles.get(vehicleID));
			((ScheduleTask<ScheduleEdge>) MissionScheduler.SOURCE_NODE).addDestination(e3);
			e4.addCost(vehicles.get(vehicleID));
			n.addDestination(e4);
		}

		scheduleTasks.put(m.getId(), n);
		System.out.println("MS:> Task " + m.getId() + " added");
	}

	/**
	 * Remove mission
	 * 
	 * @param m
	 *            Mission to remove
	 * @return
	 */
	protected boolean removeMission(Mission m) {
		return pool.remove(m);
	}

	/**
	 * Update mission
	 * 
	 * @param m
	 *            Mission to update
	 */
	protected void updateMission(Mission m) {
		// TODO
		// WHAT TO DO IN THIS CASE ?
		new Exception("MissionScheduler:> WARNING : UPDATE MISSION CALLED ! ("+m.getId()+")").printStackTrace();
	}

	protected class MissionStartedHelper {
		public Mission m;
		public String resourceID;

		public MissionStartedHelper(Mission m, String resourceID) {
			this.m = m;
			this.resourceID = resourceID;
		}

		@Override
		public int hashCode() {
			return m.getId().hashCode();
		}

		@Override
		public boolean equals(Object o) {
			return o.hashCode() == hashCode();
		}

		public String toString() {
			return m.getId() + "@" + resourceID;
		}
	}

	/*
	 * ========================== TASKS AND RESOURCES LISTENERS
	 * ==========================
	 */
	public void missionStarted(Time t, Mission m, String resourceID) {
		// DO NOT RECOMPUTE SOLUTION WHEN THE CURRENT ONE IS GOING ON
		 //lock.lock();
		 //graphChanged = true;
		 //lock.unlock();

		// REMOVE MISSION FROM SCHEDULER
		Set<MissionStartedHelper> toStartList = null;
		if (missionsToStart.containsKey(t))
			toStartList = missionsToStart.get(t);
		else
			toStartList = Collections.synchronizedSet(new HashSet<MissionStartedHelper>());

		toStartList.add(new MissionStartedHelper(m, resourceID));
		missionsToStart.put(t, toStartList);
	}

	protected void missionStarted(Mission m, String resourceID) {
		// System.err.println("STARTED MISSION "+m.getId()+" by "+resourceID);
		removeMission(m);
		// TODO:
		/*
		 * try {
		 * hillsNodes.get(resourceID).getStraddleCarrier().addMissionInWorkload
		 * (m); } catch (RemoteException e) { e.printStackTrace(); }
		 */
		// System.err.println("MISSION "+m.getId()+" REMOVED FROM GRAPH AND READDED IN "+resourceID+"'s WORKLOAD");
	}

	public boolean removeMission(Time t, Mission m) {
		lock.lock();
		graphChanged = true;
		lock.unlock();

		List<Mission> toRemoveList = null;
		if (missionsToRemove.containsKey(t))
			toRemoveList = missionsToRemove.get(t);
		else
			toRemoveList = Collections.synchronizedList(new ArrayList<Mission>());

		int i = 0;
		for (Mission m2 : toRemoveList) {
			int comp = m2.getId().compareTo(m.getId());
			if (comp >= 0)
				break;
			else
				i++;
		}
		toRemoveList.add(i, m);

		missionsToRemove.put(t, toRemoveList);
		// TODO Watchout: private method is called directly here to avoid
		// scheduling pbs
		return true; // removeMission(m);
	}

	public void updateMission(Time t, Mission m) {
		lock.lock();
		System.err.println("UPDATE MISSION");
		graphChanged = true;
		lock.unlock();

		List<Mission> toUpdateList = null;
		if (missionsToUpdate.containsKey(t))
			toUpdateList = missionsToUpdate.get(t);
		else
			toUpdateList = Collections.synchronizedList(new ArrayList<Mission>());

		int i = 0;
		for (Mission m2 : toUpdateList) {
			int comp = m2.getId().compareTo(m.getId());
			if (comp >= 0)
				break;
			else
				i++;
		}
		toUpdateList.add(i, m);

		missionsToUpdate.put(t, toUpdateList);
	}

	public void addMission(Time t, Mission m) {
		lock.lock();
		// System.err.println("ADD MISSION " + m.getId());
		graphChanged = true;
		lock.unlock();

		List<Mission> toAddList = null;
		if (missionsToAdd.containsKey(t))
			toAddList = missionsToAdd.get(t);
		else
			toAddList = Collections.synchronizedList(new ArrayList<Mission>());

		int i = 0;
		for (Mission m2 : toAddList) {
			int comp = m2.getId().compareTo(m.getId());
			if (comp >= 0)
				break;
			else
				i++;
		}
		toAddList.add(i, m);

		missionsToAdd.put(t, toAddList);
	}

	public void addResource(Time t, StraddleCarrier rsc) {
		lock.lock();
		// System.err.println("ADD RESOURCE");
		graphChanged = true;
		lock.unlock();

		List<StraddleCarrier> toAddList = null;
		if (resourceToAdd.containsKey(t))
			toAddList = resourceToAdd.get(t);
		else
			toAddList = new ArrayList<>();

		int i = 0;
		for (StraddleCarrier rsc2 : toAddList) {
			int comp = rsc2.getId().compareTo(rsc.getId());
			if (comp >= 0)
				break;
			else
				i++;
		}
		toAddList.add(i, rsc);
		resourceToAdd.put(t, toAddList);
	}

	public boolean removeResource(Time t, StraddleCarrier rsc) {
		lock.lock();
		System.err.println("REMOVE RESOURCE");
		graphChanged = true;
		lock.unlock();

		List<String> toRemoveList = null;
		if (resourceToRemove.containsKey(t))
			toRemoveList = resourceToRemove.get(t);
		else
			toRemoveList = new ArrayList<>();

		int i = 0;
		for (String rsc2 : toRemoveList) {
			int comp = rsc2.compareTo(rsc.getId());
			if (comp >= 0)
				break;
			else
				i++;
		}
		toRemoveList.add(i, rsc.getId());
		resourceToRemove.put(t, toRemoveList);

		return resources.contains(rsc);
	}

	public void updateResourceLocation(Time t, String straddleID, double distance, double travelTime) {
		// System.err.println("URL : "+straddleID+" "+distance+" "+travelTime);
		lock.lock();
		// System.err.println("UPDATE RESOURCE LOCATION");
		graphChangedByUpdate++;
		lock.unlock();

		Map<String, UpdateInfo> toUpdateMap = null;
		if (resourceToUpdate.containsKey(t))
			toUpdateMap = resourceToUpdate.get(t);
		else
			toUpdateMap = new HashMap<>();

		toUpdateMap.put(straddleID, new UpdateInfo(distance, travelTime));
		resourceToUpdate.put(t, toUpdateMap);
	}

	/* ========================== GETTERS AND SETTERS ========================== */
	public IndicatorPane getIndicatorPane() {
		return jms != null ? jms.getIndicatorPane() : null;
	}

	/**
	 * Getter on the RemoteTerminal
	 * 
	 * @return The RemoteTerminal
	 */
	// public static RemoteTerminal getRemoteTerminal() {
	// return rt;
	// }

	public long getStep() {
		return step;
	}

	public TextDisplay getRemoteDisplay() {
		return out;
	}

	public String getId() {
		return MissionScheduler.rmiBindingName;
	}

	/**
	 * Getter on the resources
	 * 
	 * @return The list of the available resources
	 */
	public List<StraddleCarrier> getResources() {
		return resources;
	}

	/**
	 * Return True if the weight of the edges is based on a distance heuristic,
	 * False otherwise
	 * 
	 * @return True if the weight of the edges is based on a distance heuristic,
	 *         False otherwise
	 * 
	 *         public boolean isDistanceWeight() { boolean b = true;
	 *         if(jrbDistance!=null) b = jrbDistance.isSelected(); return b; }
	 */

	/**
	 * Setter on the eval parameters
	 * 
	 * @param p
	 */
	public static void setEvalParameters(MissionSchedulerEvalParameters p) {
		MissionScheduler.getInstance().evalParameters = p;
	}

	public static MissionSchedulerEvalParameters getEvalParameters() {
		return MissionScheduler.getInstance().evalParameters;
	}

	public double getComputingTime() {
		return computeTime / 1000000000.0;
	}

	/* ========================== DESTRUCTOR ========================== */
	/**
	 * Destruct the object
	 */
	public void destroy() {
		if (jms != null) {
			jms.destroy();
		}

		if (lock != null) {
			/*// TODO DEBUG ILLEGAL MONITOR
			try {
				lock.unlock();

			} catch (IllegalMonitorStateException e) {
				// Nothing to do.
				System.err.println("FIXME : IllegalMonitorStateException on MissionScheduler lock");
			} finally {*/
				lock = null;
			//}

		}
		// pool.clear();
		// resources.clear();

		// out = null;

		// missionsToAdd.clear();
		// missionsToRemove.clear();
		// missionsToUpdate.clear();
		// missionsToStart.clear();

		// jms = null;
		// SOURCE_NODE = null;

		// resourceToAdd.clear();
		// resourceToRemove.clear();
		// resourceToUpdate.clear();
		// vehicles.clear();
		// resourceModels.clear();

		// scheduleResources.clear();
		// scheduleTasks.clear();

		// lock = null;

		currentScore = null;
		best = null;
		visited = null;
	}

	public double getTimeFor(String id) {
		StraddleCarrier rsc = vehicles.get(id);
		if (rsc.getCurrentLoad() == null)
			return TimeScheduler.getInstance().getTime().getInSec();
		else
			return rsc.getCurrentLoad().getMission().getDeliveryTimeWindow().getMax().getInSec();
	}

	public Collection<String> getNodesID() {
		return new ArrayList<String>(scheduleTasks.keySet());
	}

	@SuppressWarnings("rawtypes")
	public Collection<ScheduleTask> getTasks() {
		//List<ScheduleTask> l = new ArrayList<>(scheduleTasks.size());
//		for(Mission m : pool){
//			if(scheduleTasks.containsKey(m.getId()))
//				l.add(scheduleTasks.get(m.getId()));
//		}
//		return l;
		return new ArrayList<ScheduleTask>(scheduleTasks.values());
	}

	// TODO TEST FOR THE l.remove(obj)
	public List<ScheduleTask<? extends ScheduleEdge>> getUnallocatedTasks(GlobalScore solution) {
		// System.err.println("GET UNALLOCATED : "+solution+" && "+solution.getSolutionString());

		ArrayList<ScheduleTask<? extends ScheduleEdge>> l = new ArrayList<ScheduleTask<? extends ScheduleEdge>>(scheduleTasks.size());
		HashMap<String, String> allocated = new HashMap<String, String>(scheduleTasks.size());

		for (LocalScore local : solution.getSolution().values()) {
			for (ScheduleEdge e : local.getPath()) {
				if (!e.getNodeTo().getID().equals(MissionScheduler.SOURCE_NODE.getID())) {
					allocated.put(e.getNodeTo().getID(), "");
				}
			}
		}
		for (ScheduleTask<? extends ScheduleEdge> t : scheduleTasks.values()) {
			if (!allocated.containsKey(t.getID())) {
				l.add(t);
				// System.err.println(t.getID());
			}
		}
		// System.err.println("----- END GET UNALLOCATED.");
		return l;
	}

	public int getResourcesModelCount() {
		return resourceModels.size();
	}

	public List<ScheduleResource> getScheduleResources() {
		return new ArrayList<ScheduleResource>(scheduleResources.values());
	}

	public GlobalScore getCurrentGlobalScore() {
		return currentScore;
	}

	public int getPoolSize() {
		return this.pool.size();
	}

	public final Collection<Mission> getPool() {
		return pool;
	}
	
	@Override
	public Integer getDiscretPriority () {
		return 2; //Scheduler should be executed after both straddlecarriers and lasersystem threads
	}
	
	@Override
	public int hashCode(){
		return getId().hashCode();
	}
	
	@Override
	public boolean equals(Object o){
		return o.hashCode() == hashCode();
	}
}
