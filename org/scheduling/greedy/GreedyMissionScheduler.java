package org.scheduling.greedy;

import java.util.Iterator;

import org.com.model.scheduling.GreedyParametersBean;
import org.exceptions.MissionNotFoundException;
import org.exceptions.NoPathFoundException;
import org.missions.Mission;
import org.scheduling.MissionScheduler;
import org.scheduling.display.JMissionScheduler;
import org.system.Terminal;
import org.time.Time;
import org.time.TimeScheduler;
import org.time.event.AffectMission;
import org.vehicles.StraddleCarrier;

public class GreedyMissionScheduler extends MissionScheduler {
	public static final String rmiBindingName = "GreedyMissionScheduler";

	private boolean recompute = true;

	public GreedyMissionScheduler() {
		super();
		MissionScheduler.instance = this;
		if(!init)
			init();

		log.info("EVAL PARAMETERS: "+ evalParameters);
	}

	public static void closeInstance(){
		//Nothing to do
	}

	@Override
	public boolean apply() {
		boolean returnCode = NOTHING_CHANGED;
		step++;
		sstep++;
		if(precomputed){
			precomputed = false;
			returnCode = SOMETHING_CHANGED;
		}
		return returnCode;
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
		Time t = new Time(sstep);
		step = 0;
		if(evalParameters == null){
			evalParameters = GreedyParametersBean.getEvalParameters();
		}
		for (String s : Terminal.getInstance().getMissionsName()) {
			Mission m = Terminal.getInstance().getMission(s);
			addMission(t, m);
		}
		for (String s : Terminal.getInstance().getStraddleCarriersName()) {
			StraddleCarrier rsc = Terminal.getInstance().getStraddleCarrier(s);
			addResource(t, rsc);

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
		precomputed = true;
		razWorkloads();
		//int missionsToSkip = 0;
		//int i = 0;
		//int doneCount = 0;
		Iterator<Mission> itMissions = pool.iterator();
		while(itMissions.hasNext()){

			//while (doneCount + missionsToSkip <= pool.size()) {
			// Mission to schedule
			Mission m = itMissions.next();
			System.err.println("Scheduling mission "+m.getId()+" : ");
			if (Terminal.getInstance().getContainer(m.getContainerId()) != null) {
				//pool.remove(i);
				// Find best vehicle
				StraddleCarrier vBest = null;
				ScheduleScore sBest = null;
				for (StraddleCarrier v : resources) {
					System.err.println("--------------------------------------------");
					System.err.println("-> For vehicle "+v.getId()+" : ");
					v.getWorkload().insertAtRightPlace(m);
					ScheduleScore score;
					try {
						score = getScore(v);
						System.err.println("Plan : \n\t"+v.getWorkload().toString());
						System.err.println("Score : \n\t"+score);

						if (sBest == null || score.compareTo(sBest) > 0) {
							sBest = score;
							vBest = v;
							System.err.println("New best : "+sBest.getScore());
						} else {
							System.err.println("Not the best one !");
						}
					} catch (NoPathFoundException e1) {
						e1.printStackTrace();
					}
					System.err.println("--------------------------------------------");
					try {
						v.getWorkload().removeMission(m.getId());
						System.err.println("--------------------------------------------");
						System.err.println("Plan after removing mission "+m.getId()+" : \n\t"+v.getWorkload().toString());
						System.err.println("--------------------------------------------");
					} catch (MissionNotFoundException e) {
						e.printStackTrace();
					} catch (NoPathFoundException e) {
						e.printStackTrace();
					}
				}

				vBest.getWorkload().insertAtRightPlace(m);
				System.err.println("--------------------------------------------");
				System.err.println("Plan after inserting mission "+m.getId()+" for "+vBest.getId()+" : \n\t"+vBest.getWorkload().toString());
				System.err.println("--------------------------------------------");
				AffectMission am = new AffectMission(TimeScheduler.getInstance().getTime(), m.getId(),
						vBest.getId());
				am.writeEventInDb();

				if(Terminal.getInstance().getTextDisplay() != null){
					Terminal.getInstance().getTextDisplay().setVehicleToMission(m.getId(),
							vBest.getId());
				}
			} 

			/*else {
			System.err.println("Score : \n\t"+score);
				missionsToSkip++;
				i++;
			}*/
			//System.err.println("SCHEDULER : "+m.getId()+" affected to "+rsc.getId()+" !");
			System.err.println("===============================================================");
		}

		System.err.println("--------------------------------------------");
		System.err.println("END : ");
		for(StraddleCarrier s : Terminal.getInstance().getStraddleCarriers()){
			System.err.println(s.getId() +" : \n\t"+s.getWorkload()+"\n\tScore="+s.getWorkload().getScore());
		}
		System.err.println("--------------------------------------------");

		recompute = false;
		Terminal.getInstance().flushAllocations();
		long tEnd = System.nanoTime();
		computeTime += tEnd - tNow;

	}

	@Override
	public void incrementNumberOfCompletedMissions(String resourceID) {
		//		boolean terminated = true;
		//		lookup: for (StraddleCarrier rsc : resources) {
		//			Workload w = rsc.getWorkload();
		//			for (Load l : w.getLoads()) {
		//				if (l.getState() != MissionState.STATE_ACHIEVED) {
		//					terminated = false;
		//					break lookup;
		//				}
		//
		//			}
		//		}
		//		if (terminated)
		//			TimeScheduler.getInstance().computeEndTime();

		super.incrementNumberOfCompletedMissions(resourceID);
	}

	private ScheduleScore getScore(StraddleCarrier v)
			throws NoPathFoundException {
		return v.getWorkload().getScore();


		/*ScheduleScore score = new ScheduleScore();

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
				score.setDistance(score.getDistance() + p.getCostInMeters(), score.getDistanceInSec() + p.getCost());
				Time pickupTime = new Time(tOrigin, t);
				pickupTime = new Time(pickupTime,
						v.getMaxContainerHandlingTime(lane));
				long lateness = pickupTime.toStep()
						- l.getMission().getPickupTimeWindow().getMax()
								.toStep();
				// Pickup tardiness
				Time localTardiness = new Time(Math.max(0, lateness));
				score.setTardiness(new Time(score.getTardiness(), localTardiness));

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
				score.setDistance(p.getCostInMeters()+score.getDistance(), p.getCost() + score.getDistanceInSec());
				Time deliveryTime = new Time(tOrigin, t);
				deliveryTime = new Time(deliveryTime,
						v.getMaxContainerHandlingTime(lane));
				lateness = deliveryTime.toStep()
						- l.getMission().getDeliveryTimeWindow().getMax()
								.toStep();
				// Delivery tardiness
				score.setTardiness(new Time(score.getTardiness(), new Time(Math.max(
						0, lateness))));

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
		score.setDistance(score.getDistance() + p.getCostInMeters(), score.getDistanceInSec() + p.getCost());

		return score;*/
	}

}
