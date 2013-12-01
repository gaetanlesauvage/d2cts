package org.scheduling;

import java.util.Iterator;

import org.com.model.scheduling.LinearParametersBean;
import org.display.TextDisplay;
import org.missions.Load;
import org.missions.Mission;
import org.missions.MissionState;
import org.missions.Workload;
import org.scheduling.display.JMissionScheduler;
import org.system.Terminal;
import org.time.DiscretObject;
import org.time.Time;
import org.time.TimeScheduler;
import org.time.event.AffectMission;
import org.vehicles.StraddleCarrier;

public class LinearMissionScheduler extends MissionScheduler {
	public static final String rmiBindingName = "LinearMissionScheduler";

	private boolean recompute = true;
	private int index = 0;

	public LinearMissionScheduler() {
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
	public void addMission(Time t, Mission m) {
		recompute = true;
		super.addMission(t, m);
	}

	@Override
	public void addResource(Time t, StraddleCarrier rsc) {
		recompute = true;
		super.addResource(t, rsc);
	}
	
	/*@Override
	public boolean removeMission(Time t, Mission m) {
		return super.removeMission(t, m);
	}
	
	@Override
	public boolean removeResource(Time t, StraddleCarrier rsc) {
		return super.removeResource(t, rsc);
	}*/
	
	@Override
	public String getId() {
		return LinearMissionScheduler.rmiBindingName;
	}

	protected void init() {
		init = true;
		if (evalParameters == null){
			evalParameters = LinearParametersBean.getEvalParameters();
		}
		step = 0;
		sstep = TimeScheduler.getInstance().getStep() + 1;
		for (String s : Terminal.getInstance().getMissionsName()) {
			Mission m = Terminal.getInstance().getMission(s);
			if (!pool.contains(m))
				pool.add(m);
			else
				System.err.println("Mission " + m.getId()
						+ " not added because already present in the pool");
			// addMission(new Time(step), m);
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
		//Collections.sort(pool);
	}

	@Override
	public boolean apply() {
		boolean returnCode =  DiscretObject.NOTHING_CHANGED;
		step++;
		sstep++;
		if(precomputed){
			precomputed = false;
			Terminal.getInstance().flushAllocations();
			returnCode = DiscretObject.SOMETHING_CHANGED;
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

	private void razWorkloads(){
		for(StraddleCarrier s : resources){
			s.clearWorkload();
		}
	}
	@Override
	public void compute() {
		precomputed = true;
		long tNow = System.nanoTime();
		// System.out.println("COMPUTE : "+resources.size()+" ; "+pool.size());
		razWorkloads();
		
		Iterator<Mission> itMissions = pool.iterator();
		while (itMissions.hasNext()) {
			StraddleCarrier rsc = pickAStraddleCarrier();
			
			if (rsc.isAvailable()) {
				Mission m = itMissions.next();
				rsc.addMissionInWorkload(m);
				AffectMission am = new AffectMission(TimeScheduler.getInstance().getTime(),
						m.getId(), rsc.getId());
				am.writeEventInDb();
				TextDisplay rtOut = Terminal.getInstance().getTextDisplay();
				if (rtOut != null) {
					rtOut.setVehicleToMission(m.getId(), rsc.getId());
				}
			}

			// System.out.println("SCHEDULER : "+m.getId()+" affected to "+rsc.getId()+" !");
		}
		computeTime = System.nanoTime() - tNow;
		Terminal.getInstance().flushAllocations();
		recompute = false;
	}

	private StraddleCarrier pickAStraddleCarrier() {
		StraddleCarrier rsc = resources.get(index);
		index++;
		if (index == resources.size())
			index = 0;
		return rsc;
	}

	public void destroy() {
		super.destroy();
		System.out.println("Linear Mission Scheduler DESTROYED!");
	}

	@Override
	public void incrementNumberOfCompletedMissions(final String resourceID) {
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
}
