package org.scheduling;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.missions.Mission;
import org.scheduling.onlineACO.OnlineACOScheduler;
import org.time.Time;
import org.vehicles.StraddleCarrier;
import org.vehicles.models.SpeedCharacteristics;

public class ScheduleResource {

	protected static final Map<String, Boolean> visited = new HashMap<String, Boolean>();
	
	protected static volatile int nonvisited = 0;

	protected static final List<ScheduleResource> resources = new ArrayList<ScheduleResource>();
	protected static GlobalScore globalScore = new GlobalScore();

	protected String ID;
	protected String modelID;
	protected double handlingTime;
	protected double currentTime;
	protected StraddleCarrier vehicle;

	protected LocalScore score;

	public ScheduleResource(StraddleCarrier vehicle) {
		this.vehicle = vehicle;
		this.ID = vehicle.getId();
		this.modelID = vehicle.getModel().getId();
		SpeedCharacteristics speeds = vehicle.getModel()
				.getSpeedCharacteristics();
		double d1 = (speeds.getContainerHandlingTimeFromGroundMAX() + speeds
				.getContainerHandlingTimeFromGroundMIN()) / 2.0;
		double d2 = (speeds.getContainerHandlingTimeFromTruckMAX() + speeds
				.getContainerHandlingTimeFromTruckMIN()) / 2.0;
		this.handlingTime = ((d1 + d2) / 2.0);
		this.currentTime = MissionScheduler.getInstance().getTimeFor(ID);
		score = new LocalScore(this);

		resources.add(this);
	}

	@SuppressWarnings("unchecked")
	public void visitNode(ScheduleTask<? extends ScheduleEdge> task) {
		// System.err.println("VISIT NODE "+task.getID()+" BY "+getID());
		visited.put(task.getID(), true);
		// System.err.println("1");
		nonvisited--;
		// System.err.println("2");
		// List<ScheduleEdge> path = score.getPath();
		List<ScheduleTask<? extends ScheduleEdge>> nodePath = score
				.getNodePath();
		// System.err.println("3 : "+path.size());
		ScheduleTask<ScheduleEdge> previousTask;
		// if(nodePath.size()>0){
		// System.err.println("4-1 PATH : "+score.getNodePathString());
		// ScheduleEdge previousEdge = path.get(path.size()-1);
		// System.err.println("4-1 : "+previousEdge.getID());
		previousTask = (ScheduleTask<ScheduleEdge>) nodePath.get(nodePath
				.size() - 1);

		// }
		// else{
		// System.err.println("4-2");
		// previousTask =
		// (ScheduleTask<ScheduleEdge>)MissionScheduler.SOURCE_NODE;
		// }
		// System.err.println("5");
		ScheduleTask<ScheduleEdge> t = (ScheduleTask<ScheduleEdge>) task;
		// System.err.println("5-1 : "+previousTask.getID());
		ScheduleEdge edge = previousTask.getEdgeTo(t);
		// System.err.println("6");
		if (edge == null)
			new Exception("Edge is null between " + previousTask.getID()
					+ " and " + t.getID()).printStackTrace();
		else if (edge.getNodeTo() == null)
			new Exception("Edge has no destination").printStackTrace();
		else if (edge.getNodeTo().getMission() == null)
			new Exception("Node " + edge.getNodeTo().getID()
					+ " has no attached mission").printStackTrace();
		// System.err.println("7");
		Mission m = edge.getNodeTo().getMission();
		// System.err.println("8");

		// Current Location to Pickup location of the next mission
		score.addDistance(edge.getDistance());
		double cost = 0.0;
		if (edge.getNodeFrom() == MissionScheduler.getInstance().SOURCE_NODE)
			cost = edge.getCost(getID());
		else
			cost = edge.getCost(getModelID());
		score.addTravelTime(cost);

		// Pickup to delivery locations
		score.addDistance(edge.getNodeTo().getDistance());
		score.addTravelTime(edge.getNodeTo().getCost(getModelID()));

		// System.err.println("3");
		// Update Time after P
		double tP = currentTime + cost;// + colony.getHandlingTime().getInSec();
		double tLeaveP = 0;
		if (tP < m.getPickupTimeWindow().getMin().getInSec()) {
			if (previousTask != MissionScheduler.getInstance().SOURCE_NODE)
				score.addEarliness(m.getPickupTimeWindow().getMin().getInSec()
						- tP);
			tP = m.getPickupTimeWindow().getMin().getInSec();
		}
		tLeaveP = tP + handlingTime;

		if (tLeaveP > m.getPickupTimeWindow().getMax().getInSec()) {
			score.addLateness(tLeaveP
					- m.getPickupTimeWindow().getMax().getInSec());
		}

		// Update Time after D
		double tD = tLeaveP + edge.getNodeTo().getCost(getModelID());// +
																		// colony.getHandlingTime().getInSec();
		if (tD < m.getDeliveryTimeWindow().getMin().getInSec()) {
			addEarliness(m.getDeliveryTimeWindow().getMin().getInSec() - tD);
			tD = m.getDeliveryTimeWindow().getMin().getInSec();
		}
		double tLeaveD = tD + handlingTime;

		// System.err.println("4");

		// time = new Time(tD+"s");
		if (tLeaveD > m.getDeliveryTimeWindow().getMax().getInSec()) {
			addLateness(tLeaveD - m.getDeliveryTimeWindow().getMax().getInSec());
		}

		currentTime = tLeaveD;
		// System.err.println("5");

		if(task.getID().equals(OnlineACOScheduler.getInstance().SOURCE_NODE.getID())){
			System.err.println("Wath out!!!");
		}
		score.addEdge(edge, currentTime);
		score.addNode(task);
		// System.err.println("6");
	}

	@SuppressWarnings("unchecked")
	public LocalScore simulateVisitNode(LocalScore fromSituation,
			ScheduleTask<? extends ScheduleEdge> task) {
		// System.err.println(getID()+" SIMULATE VISITING "+task.getID());

		LocalScore score = new LocalScore(fromSituation);

		// List<ScheduleEdge> path = score.getPath();
		List<ScheduleTask<? extends ScheduleEdge>> nodePath = score
				.getNodePath();
		ScheduleTask<ScheduleEdge> previousTask = null;
		// if(nodePath.size()>0){
		// ScheduleEdge previousEdge = path.get(path.size()-1);
		previousTask = (ScheduleTask<ScheduleEdge>) nodePath.get(nodePath
				.size() - 1);
		// previousTask = (ScheduleTask<ScheduleEdge>)previousEdge.getNodeTo();
		// }

		// if(previousTask == null) previousTask =
		// (ScheduleTask<ScheduleEdge>)MissionScheduler.SOURCE_NODE;

		ScheduleEdge edge = previousTask
				.getEdgeTo((ScheduleTask<ScheduleEdge>) task);

		if (edge == null || edge.getID() == null)
			new Exception("Edge is null").printStackTrace();
		else if (edge.getNodeTo() == null)
			new Exception("Edge has no destination").printStackTrace();
		else if (edge.getNodeTo().getMission() == null) {
			// GO BACK TO DEPOT
			score.addDistance(edge.getDistance());
			score.addTravelTime(edge.getCost(getID()));
			// Update Time after reaching Depot
			double currentTime = score.getTimes().get(
					score.getTimes().size() - 1);
			currentTime += edge.getCost(getID());
			score.addEdge(edge, currentTime);
			return score;
		}

		Mission m = edge.getNodeTo().getMission();

		// Current Location to Pickup location of the next mission

		score.addDistance(edge.getDistance());
		double cost = 0.0;
		if (edge.getNodeFrom() == MissionScheduler.getInstance().SOURCE_NODE)
			cost = edge.getCost(getID());
		else
			cost = edge.getCost(getModelID());
		score.addTravelTime(cost);

		// Pickup to delivery locations
		score.addDistance(edge.getNodeTo().getDistance());
		score.addTravelTime(edge.getNodeTo().getCost(getModelID()));

		// Update Time after P
		double currentTime = score.getTimes().get(score.getTimes().size() - 1);
		// System.err.println("TIME = "+new Time(currentTime+"s")+" | "+new
		// Time(this.currentTime+"s"));

		double tP = currentTime + cost;// + colony.getHandlingTime().getInSec();
		// System.err.println("TIME@P = "+new Time(tP+"s"));

		double tLeaveP = 0;
		if (tP < m.getPickupTimeWindow().getMin().getInSec()) {
			if (previousTask != MissionScheduler.getInstance().SOURCE_NODE)
				score.addEarliness(m.getPickupTimeWindow().getMin().getInSec()
						- tP);
			tP = m.getPickupTimeWindow().getMin().getInSec();
		}
		tLeaveP = tP + handlingTime;
		// System.err.println("TIME@LeaveP = "+new Time(tLeaveP+"s"));

		if (tLeaveP > m.getPickupTimeWindow().getMax().getInSec()) {
			score.addLateness(tLeaveP
					- m.getPickupTimeWindow().getMax().getInSec());
		}

		// Update Time after D
		double tD = tLeaveP + edge.getNodeTo().getCost(getModelID());// +
																		// colony.getHandlingTime().getInSec();
		// System.err.println("TIME@D = "+new Time(tD+"s"));
		if (tD < m.getDeliveryTimeWindow().getMin().getInSec()) {
			score.addEarliness(m.getDeliveryTimeWindow().getMin().getInSec()
					- tD);
			tD = m.getDeliveryTimeWindow().getMin().getInSec();
		}
		double tLeaveD = tD + handlingTime;
		// System.err.println("TIME@LeaveD = "+new Time(tLeaveD+"s"));

		// time = new Time(tD+"s");
		if (tLeaveD > m.getDeliveryTimeWindow().getMax().getInSec()) {
			score.addLateness(tLeaveD
					- m.getDeliveryTimeWindow().getMax().getInSec());
		}

		currentTime = tLeaveD;
		// System.err.println("DOVERALL = "+dOverall);
		score.addEdge(edge, currentTime);
		score.addNode(task);
		return score;
	}

	public LocalScore getLocalScore() {
		return score;
	}

	public void reset() {
		this.currentTime = MissionScheduler.getInstance().getTimeFor(ID);

		// PUT AT THE DEPOT
		score = new LocalScore(this);
	}

	public static void resetVisited() {
		for (String taskID : MissionScheduler.getInstance().getNodesID()) {
			visited.put(taskID, false);
		}
		nonvisited = visited.size();
	}

	public static boolean hasBeenVisited(String taskID) {
		return visited.get(taskID);
	}

	public String getModelID() {
		return modelID;
	}

	public String getID() {
		return ID;
	}

	public static List<ScheduleResource> getScheduleResources() {
		return new ArrayList<ScheduleResource>(resources);
	}

	public static GlobalScore getScore() {
		return globalScore;
	}

	public void addEarliness(double earlinessToAdd) {
		score.addEarliness(earlinessToAdd);
	}

	public void addLateness(double latenessToAdd) {
		score.addLateness(latenessToAdd);
	}

	public void addDistance(double distanceToAdd) {
		score.addDistance(distanceToAdd);
	}

	public void addTravelTime(double travelTimeToAdd) {
		score.addTravelTime(travelTimeToAdd);
	}

	public void destroy() {
		if (globalScore != null) {
			globalScore = null;
		}

		resources.clear();

		if (visited.size() > 0) {
			visited.clear();
		}
	}

	protected ScheduleTask<? extends ScheduleEdge> getLocation() {
		List<ScheduleEdge> path = score.getPath();
		if (path.size() == 0)
			return MissionScheduler.getInstance().SOURCE_NODE;
		else
			return path.get(path.size() - 1).getNodeTo();
	}

	public double getCurrentTime() {
		return currentTime;
	}

	public String getCurrentTimeString() {
		return new Time(currentTime).toString();
	}

	public double getHandlingTime() {
		return handlingTime;
	}
}