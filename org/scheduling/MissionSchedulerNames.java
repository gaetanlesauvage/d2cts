package org.scheduling;

import org.scheduling.branchAndBound.BranchAndBound;
import org.scheduling.greedy.GreedyMissionScheduler;
import org.scheduling.offlineACO.OfflineACOScheduler;
import org.scheduling.offlineACO2.OfflineACOScheduler2;
import org.scheduling.onlineACO.OnlineACOScheduler;
import org.scheduling.random.RandomMissionScheduler;

public enum MissionSchedulerNames {
	LINEAR(LinearMissionScheduler.rmiBindingName), RANDOM(RandomMissionScheduler.rmiBindingName), GREEDY(GreedyMissionScheduler.rmiBindingName),
	OFFLINE(OfflineACOScheduler.rmiBindingName), OFFLINE2(OfflineACOScheduler2.rmiBindingName), ONLINE(OnlineACOScheduler.rmiBindingName),
	BRANCHANDBOUND(BranchAndBound.rmiBindingName), BB(org.scheduling.bb.BB.rmiBindingName);
	
	private String bindingName;
	
	private MissionSchedulerNames(String bindingName){
		this.bindingName = bindingName;
	}
	
	public String getBindingName(){
		return bindingName;
	}
}
