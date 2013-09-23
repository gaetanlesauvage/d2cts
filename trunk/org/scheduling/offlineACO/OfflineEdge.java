package org.scheduling.offlineACO;

import java.util.HashMap;
import java.util.Map;

import org.scheduling.MissionScheduler;
import org.scheduling.ScheduleEdge;
import org.scheduling.ScheduleResource;
import org.scheduling.ScheduleTask;
import org.vehicles.StraddleCarrier;

public class OfflineEdge extends ScheduleEdge {
	public static double overallPh = 0.0;

	// Pheromone of each vehicle
	private Map<String, Double> pheromone;

	public OfflineEdge(ScheduleTask<OfflineEdge> origin,
			ScheduleTask<OfflineEdge> destination) {
		super(origin, destination);
		this.pheromone = new HashMap<String, Double>();
		for (ScheduleResource hill : ScheduleResource.getScheduleResources()) {
			pheromone.put(hill.getID(), OfflineACOScheduler.getInstance().getGlobalParameters().getLambda());
		}
		// System.out.println("TSP:> EDGE CREATED : origin: "+origin.getID()+" destination: "+destination.getID());
	}

	@Override
	public void addCost(StraddleCarrier rsc) {
		super.addCost(rsc);
		pheromone.put(rsc.getId(), OfflineACOScheduler.getInstance().getGlobalParameters()
				.getLambda());

	}

	public double getPheromone(String hillID) {
		if (!pheromone.containsKey(hillID)) {
			pheromone.put(hillID, 0.0);
			new Exception("No ph for " + hillID + " at " + getID() + "!")
					.printStackTrace();
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
		// System.err.println("BEFORE "+getID()+" ("+getPHString()+")");
		double previousQuantity = 0;
		if (pheromone.containsKey(colony)) {
			previousQuantity = pheromone.get(colony);
		}
		pheromone.put(colony, previousQuantity + quantity);
		overallPh += quantity;
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
		OfflineSchedulerParameters params = OfflineACOScheduler.getInstance().getGlobalParameters();
		if (MissionScheduler.DEBUG)
			System.err.println("Evaporation on " + getID() + " : \n\tBefore = "
					+ getPHString());

		for (String colonyID : pheromone.keySet()) {
			double currentPH = pheromone.get(colonyID); //
			double currentExtraPH = currentPH - params.getLambda(); // Avoid
																	// getting
																	// ph <
																	// lambda
			double nextPH = (1.0 - params.getRho()) * currentExtraPH
					+ params.getLambda(); //
			// //if(nextPH < params.getLambda()) nextPH = params.getLambda();
			overallPh -= currentPH - nextPH; // Update of overall ph amounts
			pheromone.put(colonyID, nextPH);
		}
		if (MissionScheduler.DEBUG)
			System.err.println("\tAfter = " + getPHString());
	}

	public void destroy() {
		overallPh = 0.0;
		super.destroy();
	}
}