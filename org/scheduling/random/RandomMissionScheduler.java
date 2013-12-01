package org.scheduling.random;

import java.util.Iterator;

import org.com.model.scheduling.RandomParametersBean;
import org.missions.Load;
import org.missions.Mission;
import org.missions.MissionState;
import org.missions.Workload;
import org.scheduling.MissionScheduler;
import org.scheduling.display.JMissionScheduler;
import org.system.Terminal;
import org.time.Time;
import org.time.TimeScheduler;
import org.time.event.AffectMission;
import org.vehicles.StraddleCarrier;

public class RandomMissionScheduler extends MissionScheduler {
	public static final String rmiBindingName = "RandomMissionScheduler";
	private boolean recompute = true;

	public RandomMissionScheduler() {
		super();
		MissionScheduler.instance = this;
		if(!init)
			init();
	}

	@Override
	public String getId() {
		return RandomMissionScheduler.rmiBindingName;
	}

	public static void closeInstance(){
		//Nothing to do
	}

	protected void init() {
		init = true;
		step = 0;
		if(evalParameters == null){
			evalParameters = RandomParametersBean.getEvalParameters();
		}

		sstep = TimeScheduler.getInstance().getStep() + 1;
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
	public void addMission(Time t, Mission m) {
		recompute = true;
		super.addMission(t, m);
	}

	@Override
	public void addResource(Time t, StraddleCarrier rsc) {
		recompute = true;
		super.addResource(t, rsc);
	}
	
	@Override
	public void compute() {
		precomputed = true;
		long now = System.nanoTime();
		razWorkloads();
		
		Iterator<Mission> itMissions = pool.iterator();
		while (itMissions.hasNext()) {
			StraddleCarrier rsc = pickAStraddleCarrier();
			Mission m = itMissions.next();
			rsc.addMissionInWorkload(m);

			AffectMission am = new AffectMission(TimeScheduler.getInstance().getTime(), m.getId(),
					rsc.getId());
			am.writeEventInDb();

			Terminal.getInstance().getTextDisplay().setVehicleToMission(m.getId(), rsc.getId());

			// System.out.println("SCHEDULER : "+m.getId()+" affected to "+rsc.getId()+" !");
		}
		computeTime += System.nanoTime() - now;
		Terminal.getInstance().flushAllocations();
		recompute = false;
	}

	private StraddleCarrier pickAStraddleCarrier() {
		StraddleCarrier rsc = resources.get(Terminal.getInstance().getRandom().nextInt(resources.size()));
		return rsc;
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
}
