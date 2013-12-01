package org.scheduling.offlineACO2;

import org.missions.Mission;
import org.scheduling.MissionScheduler;
import org.scheduling.ScheduleTask;


public class OfflineNode2 extends ScheduleTask<OfflineEdge2>{

	public OfflineNode2(Mission m) {
		super(m);
		if(m==null){
			MissionScheduler.log.info("ADDING DESTINATION FROM "+getID()+" TO "+getID());
			addDestination(new OfflineEdge2(this,this));
		}
	}

}
