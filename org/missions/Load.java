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
package org.missions;


import org.apache.log4j.Logger;
import org.com.model.scheduling.LoadBean;
import org.conf.parameters.ReturnCodes;
import org.display.TextDisplay;
import org.scheduling.MissionScheduler;
import org.system.Terminal;
import org.time.Time;
import org.time.TimeScheduler;
import org.time.TimeWindow;

public class Load implements Comparable<Load> {
	private static final Logger log = Logger.getLogger(Load.class);

	private TimeWindow t;
	private Mission m;

	private MissionState state;
	private MissionPhase phase;

	private Time effectiveStartTime, pickupReachTime, loadStartTime,
	loadEndTime, deliveryStartTime, deliveryReachTime, unloadStartTime,
	endTime, waitTime;
	// private String containerFirstLocation;
	private String straddleID;

	// Prevent from executing this load without doing the linked one
	private PrethreadCondition linkedLoad;
	private Time startableTime;

	private Time waitTimeAtEndOfPickup;

	public Load(Load toCopy) {

		this.t = new TimeWindow(new Time(toCopy.t.getMin()), new Time(
				toCopy.t.getMax()));
		this.m = toCopy.m;
		this.state = toCopy.state;
		this.phase = toCopy.phase;
		if (toCopy.effectiveStartTime != null)
			this.effectiveStartTime = new Time(toCopy.effectiveStartTime);
		if (toCopy.pickupReachTime != null)
			this.pickupReachTime = new Time(toCopy.pickupReachTime);
		if (toCopy.loadStartTime != null)
			this.loadStartTime = new Time(toCopy.loadStartTime);
		if (toCopy.loadEndTime != null)
			this.loadEndTime = new Time(toCopy.loadEndTime);
		if (toCopy.deliveryStartTime != null)
			this.deliveryStartTime = new Time(toCopy.deliveryStartTime);
		if (toCopy.deliveryReachTime != null)
			this.deliveryReachTime = new Time(toCopy.deliveryReachTime);
		if (toCopy.unloadStartTime != null)
			this.unloadStartTime = new Time(toCopy.unloadStartTime);
		if (toCopy.endTime != null)
			this.endTime = new Time(toCopy.endTime);
		if (toCopy.waitTime != null)
			this.waitTime = new Time(toCopy.waitTime);
		if (toCopy.startableTime != null)
			this.startableTime = new Time(toCopy.startableTime);
	}

	public Load(TimeWindow tw, Mission m) {
		this(tw, m, tw.getMin());
	}

	public Load(LoadBean bean){
		this.t = new TimeWindow(bean.getTwMin(), bean.getTwMax());
		this.m = Terminal.getInstance().getMission(bean.getMission());
		this.startableTime = bean.getStartableTime();

		this.deliveryReachTime = bean.getDeliveryReachTime();
		this.deliveryStartTime = bean.getDeliveryStartTime();
		this.effectiveStartTime = bean.getEffectiveStartTime();
		this.endTime = bean.getEndTime();
		this.loadEndTime = bean.getLoadEndTime();
		this.loadStartTime = bean.getLoadStartTime();
		this.phase = bean.getPhase();
		this.pickupReachTime = bean.getPickupReachTime();
		this.state = bean.getState();
		this.straddleID = bean.getStraddleCarrierName();
		this.unloadStartTime = bean.getUnloadStartTime();
		this.waitTime = bean.getWaitTime();

		//WaitTime at end of pickup ???

		//this.linkedLoad ???
	}

	public Load(TimeWindow tw, Mission m, Time startableTime) {
		if (m == null) {
			log.error("Mission is null!");
			new Exception().printStackTrace();
			System.exit(ReturnCodes.MISSION_NULL_EXIT_CODE.getCode());
		}
		if (tw == null) {
			log.error("TimeWindow is null!");
			new Exception().printStackTrace();
			System.exit(ReturnCodes.TW_NULL_EXIT_CODE.getCode());
		}

		this.t = tw;
		this.m = m;
		this.startableTime = startableTime;
		state = MissionState.STATE_TODO;
		phase = MissionPhase.PHASE_PICKUP;
		waitTime = new Time(0);
		waitTimeAtEndOfPickup = new Time(0);
		/*
		 * if(sortedMap == null){ sortedMap =
		 * Collections.synchronizedSortedMap(new TreeMap<Time, String>()); }
		 */
		/*
		 * try { terminal.missionStatusChanged(phase, state, straddleID, new
		 * Time(0), m); } catch (RemoteException e) { e.printStackTrace(); }
		 */
	}

	public void abort() {
		state = MissionState.STATE_TODO;
		log.info("Mission "+m.getId()+" aborted by "+straddleID+" in "+phase+" - "+state+".");
		Terminal.getInstance().missionStatusChanged(phase, state, straddleID,
				getOverspentTime(), getWaitTime(), m);
		new Exception().printStackTrace();
	}

	public int compareTo(Load l) {
		return t.getMin().compareTo(l.t.getMin());
	}

	/*
	 * public void displayForRiadh(){ StringBuilder sb = new StringBuilder();
	 * Time tP = new Time(pickupReachTime,effectiveStartTime,false); Time tL =
	 * new Time(loadEndTime,loadStartTime,false); Time tD = new
	 * Time(deliveryReachTime, deliveryStartTime, false); Time tU = new
	 * Time(endTime, unloadStartTime, false);
	 * 
	 * TimeWindow tw = new TimeWindow(m.getPickupTimeWindow().getMin(),
	 * m.getDeliveryTimeWindow().getMax());
	 * 
	 * sb.append(m.getContainerId()+"\t"); Time tPi = new Time(tP, tL); Time tIp
	 * = new Time(tD, tU);
	 * sb.append(tPi+"\t"+tIp+"\t"+m.getMissionKind()+"\t"+this
	 * .containerFirstLocation
	 * +"\t"+m.getDestination().getLaneId()+"\t"+tw+"\n");
	 * 
	 * sortedMap.put(tw.getMin(), sb.toString()); writeForRiadh(); }
	 */
	/*
	 * public void writeForRiadh(){ File f = new File(file); try { PrintWriter
	 * pw = new PrintWriter(f); for(String s : sortedMap.values()){
	 * pw.append(s); } pw.flush(); pw.close(); } catch (FileNotFoundException e)
	 * {
	 * 
	 * e.printStackTrace(); }
	 * 
	 * }
	 */
	public void displayMission() {
		StringBuilder sb = new StringBuilder();
		if (pickupReachTime == null) {
			log.error("Pickup reach time is null for "+m.getId()+".");
		}
		if (effectiveStartTime == null) {
			log.error("Effective start time is null for "+m.getId()+".");
		}
		Time tP = new Time(pickupReachTime, effectiveStartTime, false);
		Time tL = new Time(loadEndTime, loadStartTime, false);
		Time tD = new Time(deliveryReachTime, deliveryStartTime, false);
		Time tU = new Time(endTime, unloadStartTime, false);
		Time overall = new Time(tP, tL, tD, tU);
		sb.append(endTime + " - Mission " + m.getId()
				+ " ACHIEVED ! Duration : " + overall + " =\n");
		sb.append("\tStartableTime : " + startableTime + "\tStart time: "
				+ effectiveStartTime + "\n");
		sb.append("\tEnd of pickup at " + loadEndTime);
		boolean lateness = false;
		Time latenessTime = new Time(0);
		if (loadEndTime.getInSec() > m.getPickupTimeWindow().getMax().getInSec()) {
			Time t = new Time(loadEndTime, m.getPickupTimeWindow().getMax(),
					false);
			sb.append(" lateness: " + t);
			latenessTime = t;
			lateness = true;
		}
		sb.append("\n");
		sb.append("\tEnd of delivery at " + endTime);
		if (endTime.getInSec() > m.getDeliveryTimeWindow().getMax().getInSec()) {
			Time t = new Time(endTime, m.getDeliveryTimeWindow().getMax(),
					false);
			sb.append(" lateness: " + t);
			lateness = true;
			latenessTime = new Time(latenessTime, t);
		}

		sb.append("\n");
		boolean wait = false;
		if (waitTime.toStep() > 0) {
			sb.append("\tWait time : " + waitTime+" ("+waitTimeAtEndOfPickup+")");
			wait = true;
		}

		if (lateness) {
			if (wait)
				sb.append("\n");
			sb.append("\tGlobal Lateness: " + latenessTime);
		}

		/*
		 * sb.append("\tPICKUP : "+tP+"\n"); sb.append("\tLOAD : "+tL+"\n");
		 * sb.append("\tDELIVERY : "+tD+"\n"); sb.append("\tUNLOAD : "+tU+"\n");
		 * sb.append("\tSTART : "+effectiveStartTime+"\n");
		 * sb.append("\tPICKUP reached at : "+pickupReachTime+"\n");
		 * sb.append("\tLOAD started at : "+loadStartTime+"\n");
		 * sb.append("\tLOAD ended at : "+loadEndTime+"\n");
		 * sb.append("\tDELIVERY started at  : "+deliveryStartTime+"\n");
		 * sb.append("\tDELIVERY reached at : "+deliveryReachTime+"\n");
		 * sb.append("\tUNLOAD started at : "+unloadStartTime+"\n");
		 * sb.append("\tUNLOAD ended at : "+endTime);
		 * sb.append("\tWait time : "+waitTime);
		 */
		// System.out.println(sb.toString());

		TextDisplay rp = Terminal.getInstance().getTextDisplay();
		if (rp != null)
			rp.println(sb.toString());

		log.info(sb.toString());
	}

	public void done() {
		state = MissionState.STATE_ACHIEVED;
		endTime = new Time(TimeScheduler.getInstance().getTime(), new Time(1), false);
		displayMission();
		// displayForRiadh();

		Terminal.getInstance().missionStatusChanged(phase, state, straddleID,
				getOverspentTime(), getWaitTime(), m);

	}

	public Time from() {
		return t.getMin();
	}

	public Mission getMission() {
		return m;
	}

	public MissionPhase getPhase() {
		return phase;
	}

	public Time getStartTime() {
		return startableTime;
		// return t.getMin();
	}

	public MissionState getState() {
		return state;
	}

	public void nextPhase() {
		if (effectiveStartTime == null)
			log.error("Effective start time is null for "+m.getId()+".");


		Time tPrevious = new Time(TimeScheduler.getInstance().getTime(), new Time(1), false);
		switch(phase){
		case PHASE_PICKUP:
			pickupReachTime = tPrevious;
			loadStartTime = tPrevious;
			waitTimeAtEndOfPickup = new Time(waitTime);
			break;
		case PHASE_LOAD:
			loadEndTime = tPrevious;
			deliveryStartTime = tPrevious;
			break;
		case PHASE_DELIVERY:
			deliveryReachTime = tPrevious;
			unloadStartTime = tPrevious;
			break;
		case PHASE_UNLOAD:
			log.error("Cannot add a phase after PHASE_UNLOAD!");
		}

		Time ot = getOverspentTime();
		this.phase = this.phase.next();
		Terminal.getInstance().missionStatusChanged(phase, state, straddleID, ot,
				getWaitTime(), m);
	}

	public void setPhase(MissionPhase phase) {
		this.phase = phase;
		Terminal.getInstance().missionStatusChanged(phase, state, straddleID,
				getOverspentTime(), getWaitTime(), m);

	}

	public void start(String straddleID) {
		state = MissionState.STATE_CURRENT;

		this.straddleID = straddleID;
		try {
			effectiveStartTime = TimeScheduler.getInstance().getTime();
			// containerFirstLocation =
			// m.getContainer().getContainerLocation().getSlotId();
			// System.out.println("STARTING MISSION : "+m.getId()+" phase = "+phase+" state = "+state+" straddleID = "+straddleID);
			Terminal.getInstance().missionStatusChanged(phase, state, straddleID,
					getOverspentTime(), getWaitTime(), m);
		} catch (Exception e) {
			log.error("M="+m);
			log.error("M.getContainer()=" + m.getContainerId() + " = "
					+ m.getContainer());
			log.error("M.getContainer().getContainerLocation()="
					+ m.getContainer().getContainerLocation());
			log.error(e.getMessage(),e);
		}
	}

	public Time to() {
		return t.getMax();
	}

	public String toString() {
		String status = "TODO";
		if (state == MissionState.STATE_CURRENT) {
			switch(phase){
			case PHASE_PICKUP:
				status = "PICKUP";
				break;
			case PHASE_LOAD:
				status = "LOADING";
				break;
			case PHASE_DELIVERY:
				status = "DELIVERY";
				break;
			case PHASE_UNLOAD:
				status = "UNLOADING";
				break;
			}
		} else if (state == MissionState.STATE_ACHIEVED){
			status = "ACHIEVED";
		}
		String link = "";
		if (linkedLoad != null)
			link = " LINKED TO: " + linkedLoad.getConditionMission();
		return m + " [" + t.getMin() + " -> " + t.getMax() + " @ "+startableTime+"] STATUS: "
		+ status + link;
	}

	public void addWaitTime(double seconds) {
		double newWaitTime = waitTime.getInSec() + seconds;
		waitTime = new Time(newWaitTime);
		TextDisplay rd = Terminal.getInstance().getTextDisplay();
		if (rd != null)
			rd.missionVehicleWaitTimeChanged(m.getId(), waitTime);
	}

	public void removeWaitTime(double seconds) {
		double newWaitTime = waitTime.getInSec() - seconds;
		waitTime = new Time(newWaitTime);
		TextDisplay rd = Terminal.getInstance().getTextDisplay();
		if (rd != null)
			rd.missionVehicleWaitTimeChanged(m.getId(), waitTime);
	}


	public void reschedule() {
		this.straddleID = null;
		/*
		 * RemoteMissionScheduler rs; try { rs =
		 * NetworkConfiguration.getRMIInstance().getMissionScheduler();
		 * rs.addMission(scheduler.getTime(), m); } catch
		 * (RegisteredObjectNotFoundException e) { e.printStackTrace(); }
		 */
		MissionScheduler rms = MissionScheduler.getInstance();
		rms.addMission(TimeScheduler.getInstance().getTime(), m);

		log.info(TimeScheduler.getInstance().getTime() + "> Mission " + m.getId() + " rescheduled !");
	}

	// TODO use end time or reach time for computing the delay ?
	public Time getOverspentTime() {
		double over = 0;
		if (phase == MissionPhase.PHASE_LOAD) {
			double tEffectif = loadEndTime.getInSec();
			double tTheo = m.getPickupTimeWindow().getMax().getInSec();
			if (tEffectif > tTheo) {
				over += tEffectif - tTheo;
			}
		} else if (phase == MissionPhase.PHASE_UNLOAD) {
			double tEffectif = endTime.getInSec();
			double tTheo = m.getDeliveryTimeWindow().getMax().getInSec();
			if (tEffectif > tTheo)
				over += tEffectif - tTheo;
		}
		return new Time(over);
	}

	public Time getWaitTime() {
		if (phase == MissionPhase.PHASE_LOAD) {
			// System.out.println(m.getId()+" GET WTAEP : "+waitTimeAtEndOfPickup);
			return new Time(waitTimeAtEndOfPickup);
		} else {
			// double ws = waitTime.getInSec();
			// double wsaeps = waitTimeAtEndOfPickup.getInSec();
			// Time diff = new Time(ws-wsaeps+"s");
			// System.out.println(m.getId()+" GET WT : "+new Time(waitTime,
			// waitTimeAtEndOfPickup,
			// false)+" | "+diff+" (wt="+waitTime+" ("+ws+"s) | wtaep="+waitTimeAtEndOfPickup+" ("+wsaeps+"s))");
			return new Time(waitTime, waitTimeAtEndOfPickup, false);
		}
	}

	public boolean isLinked() {
		return linkedLoad != null;
	}

	public PrethreadCondition getPrethreadCondition() {
		return linkedLoad;
	}

	public void setLinkedLoad(Load l) {
		if(l!=null){
			this.linkedLoad = new PrethreadCondition(l);
		} else {
			this.linkedLoad = null;
		}
	}

	public String getStraddleCarrierID() {
		return straddleID;
	}

	public void setStartableTime(Time startableTime2) {
		this.startableTime = startableTime2;
	}
}