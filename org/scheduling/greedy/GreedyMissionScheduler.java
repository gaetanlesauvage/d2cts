package org.scheduling.greedy;

import java.util.Iterator;

import org.com.model.scheduling.GreedyParametersBean;
import org.exceptions.MissionNotFoundException;
import org.exceptions.NoPathFoundException;
import org.missions.Load;
import org.missions.Mission;
import org.missions.MissionState;
import org.missions.Workload;
import org.routing.path.Path;
import org.scheduling.MissionScheduler;
import org.scheduling.display.JMissionScheduler;
import org.system.Terminal;
import org.system.container_stocking.Bay;
import org.system.container_stocking.ContainerLocation;
import org.time.Time;
import org.time.TimeScheduler;
import org.time.event.AffectMission;
import org.util.Location;
import org.vehicles.StraddleCarrier;

public class GreedyMissionScheduler extends MissionScheduler {
	public static final String rmiBindingName = "GreedyMissionScheduler";
	
	private boolean recompute = true;
	
	public GreedyMissionScheduler() {
		super();
		MissionScheduler.instance = this;
		if(!init)
			init();
	}

	public static void closeInstance(){
		//Nothing to do
	}
	
	@Override
	public void apply() {
		step++;
		sstep++;
	}

	@Override
	public void precompute() {
		if (graphChanged || graphChangedByUpdate > 0) {
			processEvents();
			lock.lock();
			graphChanged = false;
			graphChangedByUpdate = 0;
			lock.unlock();
			
			
		}
		
		if (recompute && resources.size() > 0 && pool.size() > 0) {
			compute();
		}
	}

	@Override
	public String getId() {
		return rmiBindingName;
	}

	protected void init() {
		init = true;
		sstep = TimeScheduler.getInstance().getStep() + 1;
		step = 0;
		if(evalParameters == null){
			evalParameters = GreedyParametersBean.getEvalParameters();
		}
		for (String s : Terminal.getInstance().getMissionsName()) {
			Mission m = Terminal.getInstance().getMission(s);
			addMission(new Time(step), m);
		}
		for (String s : Terminal.getInstance().getStraddleCarriersName()) {
			StraddleCarrier rsc = Terminal.getInstance().getStraddleCarrier(s);
			addResource(new Time(step), rsc);

		}

		jms = new JMissionScheduler();
		for (StraddleCarrier rsc : resources) {
			jms.addResource(rsc);
		}
		recompute = true;
	}

//	@Override
//	public void addMission(Time t, Mission m) {
//		/*int policy = 0; // 0 = sorted list according to TW_P
//		// Sorted mission list
//		if (policy == 0) {
//			long twP_min = m.getPickupTimeWindow().getMin().toStep();
//			int index = 0;
//			for (int i = 0; i < pool.size(); i++) {
//				index = i;
//				Mission mTmp = pool.get(i);
//				if (twP_min < mTmp.getPickupTimeWindow().getMin().toStep()) {
//					break;
//				}
//			}
//			pool.add(index, m);
//		} else {
//			pool.add(m);
//		}*/
//		//SortedSet :)
//		pool.add(m);
//	}

	@Override
	public void addMission(Time t, Mission m) {
		recompute = true;
		super.addMission(t, m);
	}

	@Override
	public void addResource(Time t, StraddleCarrier rsc) {
		recompute = true;
		super.addResource(t, rsc);
	}
	
	private void razWorkloads(){
		for(StraddleCarrier s : resources){
			s.clearWorkload();
		}
	}
	
	@Override
	public void compute() {
		long tNow = System.nanoTime();
		razWorkloads();
		//int missionsToSkip = 0;
		//int i = 0;
		//int doneCount = 0;
		Iterator<Mission> itMissions = pool.iterator();
		while(itMissions.hasNext()){
		//while (doneCount + missionsToSkip <= pool.size()) {
			// Mission to schedule
			Mission m = itMissions.next();
			if (Terminal.getInstance().getContainer(m.getContainerId()) != null) {
				//pool.remove(i);
				// Find best vehicle
				StraddleCarrier vBest = null;
				ScheduleScore sBest = new ScheduleScore();
				sBest.tardiness = new Time(Time.MAXTIME);
				sBest.distance = Double.POSITIVE_INFINITY;

				for (StraddleCarrier v : resources) {
					v.getWorkload().insertAtRightPlace(m);
					ScheduleScore score;
					try {
						score = getScore(v);
						if (score.compareTo(sBest) <= 0) {
							sBest = score;
							vBest = v;
						}
					} catch (NoPathFoundException e1) {
						e1.printStackTrace();
					}

					try {
						v.getWorkload().removeMission(m.getId());
					} catch (MissionNotFoundException e) {
						e.printStackTrace();
					} catch (NoPathFoundException e) {
						e.printStackTrace();
					}
				}

				vBest.getWorkload().insertAtRightPlace(m);
				AffectMission am = new AffectMission(TimeScheduler.getInstance().getTime(), m.getId(),
						vBest.getId());
				am.writeEventInDb();

				Terminal.getInstance().getTextDisplay().setVehicleToMission(m.getId(),
						vBest.getId());
			} /*else {
				missionsToSkip++;
				i++;
			}*/
			// System.out.println("SCHEDULER : "+m.getId()+" affected to "+rsc.getId()+" !");
		}
		recompute = false;
		long tEnd = System.nanoTime();
		computeTime += tEnd - tNow;
		Terminal.getInstance().flushAllocations();
		
	}

	@Override
	public void incrementNumberOfCompletedMissions(String resourceID) {
		boolean terminated = true;
		lookup: for (StraddleCarrier rsc : resources) {
			Workload w = rsc.getWorkload();
			for (Load l : w.getLoads()) {
				if (l.getState() != MissionState.STATE_ACHIEVED) {
					terminated = false;
					break lookup;
				}

			}
		}
		if (terminated)
			TimeScheduler.getInstance().computeEndTime();

		super.incrementNumberOfCompletedMissions(resourceID);
	}

	private ScheduleScore getScore(StraddleCarrier v)
			throws NoPathFoundException {
		ScheduleScore score = new ScheduleScore();

		Location origin = v.getSlot().getLocation();
		Time tOrigin = TimeScheduler.getInstance().getTime();
		if (v.getCurrentLoad() != null) {
			ContainerLocation cl = v.getCurrentLoad().getMission()
					.getDestination();
			origin = new Location(Terminal.getInstance().getRoad(cl.getLaneId()),
					cl.getCoordinates(), true);
			tOrigin = v.getCurrentLoad().getMission().getDeliveryTimeWindow()
					.getMax();
		}

		Workload w = v.getWorkload();
		for (Load l : w.getLoads()) {
			if (l.getState() == MissionState.STATE_TODO) {
				ContainerLocation cl = l.getMission().getContainer()
						.getContainerLocation();
				Bay lane = Terminal.getInstance().getBay(cl.getLaneId());
				Location pickup = new Location(lane, cl.getCoordinates(), true);
				Path p = v.getRouting()
						.getShortestPath(origin, pickup, tOrigin);
				Time t = new Time(p.getCost());
				score.distance += p.getCostInMeters();
				Time pickupTime = new Time(tOrigin, t);
				pickupTime = new Time(pickupTime,
						v.getMaxContainerHandlingTime(lane));
				long lateness = pickupTime.toStep()
						- l.getMission().getPickupTimeWindow().getMax()
								.toStep();
				// Pickup tardiness
				Time localTardiness = new Time(Math.max(0, lateness));
				score.tardiness = new Time(score.tardiness, localTardiness);

				tOrigin = new Time(Math.max(l.getMission()
						.getPickupTimeWindow().getMax().toStep(),
						pickupTime.toStep()));

				// -> Delivery
				cl = l.getMission().getDestination();
				lane = Terminal.getInstance().getBay(cl.getLaneId());
				Location delivery = new Location(lane, cl.getCoordinates(),
						true);
				p = v.getRouting().getShortestPath(pickup, delivery, tOrigin);
				t = new Time(p.getCost());
				score.distance += p.getCostInMeters();
				Time deliveryTime = new Time(tOrigin, t);
				deliveryTime = new Time(deliveryTime,
						v.getMaxContainerHandlingTime(lane));
				lateness = deliveryTime.toStep()
						- l.getMission().getDeliveryTimeWindow().getMax()
								.toStep();
				// Delivery tardiness
				score.tardiness = new Time(score.tardiness, new Time(Math.max(
						0, lateness)));

				// Next load
				tOrigin = new Time(Math.max(l.getMission()
						.getDeliveryTimeWindow().getMax().toStep(),
						deliveryTime.toStep()));
				origin = delivery;
			}
		}

		// Delivery->Depot
		Path p = v.getRouting().getShortestPath(origin,
				v.getSlot().getLocation(), tOrigin);
		score.distance += p.getCostInMeters();

		return score;
	}

}
