package org.scheduling.offlineACO2;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.scheduling.MissionScheduler;
import org.scheduling.ScheduleEdge;
import org.scheduling.ScheduleResource;
import org.scheduling.offlineACO.OfflineSchedulerParameters;
import org.time.Time;
import org.time.TimeScheduler;
import org.vehicles.StraddleCarrier;

public class OfflineEdge2 extends ScheduleEdge {
	public static final Logger log = Logger.getLogger(OfflineEdge2.class);
	public static double overallPh = 0.0;

	// Pheromone of each vehicle
	private Map<String, Double> pheromone;

	public OfflineEdge2(OfflineNode2 origin, OfflineNode2 destination) {
		super(origin, destination);
		this.pheromone = new HashMap<String, Double>();
		for (ScheduleResource hill : MissionScheduler.getInstance().getScheduleResources()) {
			// pheromone.put(hill.getID(),
			// OfflineACOScheduler2.getGlobalParameters().getLambda());
			pheromone.put(hill.getID(), 1.0);
		}
	}

	@Override
	public void addCost(StraddleCarrier rsc) {
		super.addCost(rsc);
		// pheromone.put(rsc.getId(),
		// OfflineACOScheduler2.getGlobalParameters().getLambda());
		pheromone.put(rsc.getId(), 1.0);
	}

	public double getPheromone(String hillID) {
		if (!pheromone.containsKey(hillID)) {
			pheromone.put(hillID, 0.0);
			Exception e = new Exception("No ph for " + hillID + " at " + getID() + "!");
			log.error(e.getMessage(),e);
		}
		return pheromone.get(hillID);
	}

	public double getForeignPheromone(String hillID) {
		double d = 0;
		for (String colony : pheromone.keySet()) {
			if (!colony.equals(hillID))
				d += pheromone.get(colony);
		}
		return d;
	}

	public double getPheromoneOverall() {
		double d = 0;
		for (String colony : pheromone.keySet()) {
			d += pheromone.get(colony);
		}
		return d;
	}

	public void addPheromone(String colony, double quantity) {
		Time currentTime = TimeScheduler.getInstance().getTime();

		// if(MissionScheduler.DEBUG)
		// System.err.println("BEFORE "+getID()+" ("+getPHString()+")");
		double previousQuantity = 1.0;
		if (pheromone.containsKey(colony)) {
			previousQuantity = pheromone.get(colony);
		}
		pheromone.put(colony, previousQuantity + quantity);
		overallPh += quantity;

		if(MissionScheduler.DEBUG){
			log.debug(getID()+" addPheromone@"+currentTime+" quantity="+quantity+" for "+colony+" (overallPH="+overallPh+")");
		}
		// if(MissionScheduler.DEBUG)
		// System.err.println("AFTER "+colony+" spread "+quantity+"ph on "+getID()+" ("+getPHString()+")");
	}

	public String getPHString() {
		StringBuilder sb = new StringBuilder();
		for (String colID : pheromone.keySet()) {
			sb.append(colID + ": " + pheromone.get(colID) + "\t");
		}
		return sb.toString();
	}

	public void evaporate() {
		OfflineSchedulerParameters params = OfflineACOScheduler2.getInstance().getGlobalParameters();
		//if (MissionScheduler.DEBUG)
		//	MissionScheduler.log.debug("Evaporation on " + getID() + " : before=" + getPHString());
		for (String colonyID : pheromone.keySet()) {
			double currentPH = pheromone.get(colonyID);
			//			double currentExtraPH = currentPH - 1.0; // HERE REMPLACED '-
			// lambda' by '- 1.0'
			double currentExtraPH = currentPH -1.0; //Remplace la ligne précédente : TODO tester
			double nextPH = (params.getRho()) * currentExtraPH + 1.0;

			overallPh -= currentPH - nextPH;
			pheromone.put(colonyID, nextPH);
			if(MissionScheduler.DEBUG) {
				Time currentTime = TimeScheduler.getInstance().getTime();
				log.debug(getID()+" evaporate@"+currentTime+" for "+colonyID+" currentPH="+currentPH+" currentExtaPH="+currentExtraPH+" nextPH="+nextPH+" overallPH="+overallPh);
			}
		}
		//if (MissionScheduler.DEBUG)
		//	MissionScheduler.log.debug("After=" + getPHString());
	}

	public void destroy() {
		overallPh = 0.0;
		pheromone.clear();
		pheromone = null;
		super.destroy();
	}
}