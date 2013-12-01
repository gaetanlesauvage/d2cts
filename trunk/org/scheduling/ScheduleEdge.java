package org.scheduling;

import java.util.HashMap;
import java.util.Map;

import org.exceptions.EmptyResourcesException;
import org.routing.path.Path;
import org.scheduling.aco.graph.AntNode;
import org.scheduling.onlineACO.OnlineACOScheduler;
import org.time.Time;
import org.vehicles.StraddleCarrier;


public class ScheduleEdge {
	//	private static final NumberFormat format = new DecimalFormat("#.##");

	/**
	 * Origin task
	 */
	protected ScheduleTask<? extends ScheduleEdge> origin;
	/**
	 * Destination task
	 */
	protected ScheduleTask<? extends ScheduleEdge> destination;

	/**
	 * Travel times for going from origin node to destination according to the model of the vehicle
	 */
	protected Map<String, Double> cost;

	protected double maxCost;
	/**
	 * Distance between the two nodes
	 */
	protected double distance;

	public ScheduleEdge (ScheduleTask<? extends ScheduleEdge> origin, ScheduleTask<? extends ScheduleEdge> destination){
		this.origin = origin;
		this.destination = destination;

		cost = new HashMap<>(MissionScheduler.getInstance().getResources().size());
		maxCost = Double.NEGATIVE_INFINITY;

		if(origin == null) new Exception("ORIGIN IS NULL !!!").printStackTrace();
		if(destination == null) new Exception("DESTINATION IS NULL !!!").printStackTrace();

		distance = Double.POSITIVE_INFINITY;
	}

	/*public ScheduleEdge(ScheduleEdge edge) {
		this(new ScheduleTask(edge.getNodeFrom()), new ScheduleTask(edge.getNodeTo()));
	}*/
	@Override
	public int hashCode() {
		return getID().hashCode();
	}

	@Override
	public boolean equals(Object o){
		return o.hashCode() == hashCode();
	}

	public double getCost(String groupID){
		if(cost == null){
			System.err.println("COST IS NULL IN ");
			System.err.println(getID());
			System.err.println("FOR "+groupID);
		}
		else if(!cost.containsKey(groupID)){
			System.err.println("COST IS NULL IN "+getID()+" for "+groupID);
		}
		return cost.get(groupID);
	}

	/**
	 * Retrieve the max travel time required to go through the current arc.
	 * @return Max travel time required to go through the current arc.
	 */
	public double getMaxCost(){
		return maxCost;
	}

	public ScheduleTask<? extends ScheduleEdge> getNodeFrom(){
		return origin;
	}

	public ScheduleTask<? extends ScheduleEdge> getNodeTo(){
		return destination;
	}

	public void addCost(StraddleCarrier rsc) {
		if(origin == MissionScheduler.SOURCE_NODE || destination == MissionScheduler.SOURCE_NODE || destination == OnlineACOScheduler.getDepotNode()){
			double d = getCost(rsc);
			if(d>maxCost) {
				//System.err.println(getID()+" => d="+d+" > "+maxCost);
				maxCost = d;
			}
			cost.put(rsc.getId(), d);
		}
		else if(!cost.containsKey(rsc.getModel().getId())){
			double d = getCost(rsc);
			if(d>maxCost) {
				maxCost = d;
				//System.err.println(getID()+" => d="+d+" > "+maxCost);
			}
			cost.put(rsc.getModel().getId(), d);
		}
		else System.err.println("COST ALREADY COMPUTED FOR "+rsc.getModel().getId());

	}

	protected double getCost(StraddleCarrier rsc){
		double cost = 0;
		try {
			/*If container is not compatible with the straddle carrier then it won't choose the mission*/
			if(!destination.getID().equals(ScheduleTask.SOURCE_ID)&&!destination.getID().equals(AntNode.END_NODE_ID)){
				if(!rsc.getModel().isCompatible(destination.getMission().getContainer().getDimensionType())){
					return Double.POSITIVE_INFINITY;
				}
			}
			Path p = MissionScheduler.getInstance().getTravelPath(origin.getMission(), destination.getMission(), rsc);
			Time t = new Time(p.getCost());
			cost = t.getInSec();
			if(distance==Double.POSITIVE_INFINITY) distance = p.getCostInMeters();
		} catch (EmptyResourcesException e) {
			e.printStackTrace();
		}
		//System.out.println("TSP:> origin: "+origin.getID()+" destination: "+destination.getID()+" COST="+cost);
		return cost;
	}

	public double getDistance(){
		return distance;
	}

	public String getID(){
		if(origin == null || destination == null) return null;
		else return origin.getID()+"->"+destination.getID();
	}

	@Override
	public String toString(){
		return getID();
	}

	public void destroy() {
		//System.err.println("DESTROY SE");
		if(cost!=null) {
			cost.clear();
			cost = null;
		}
		destination = null;
		origin = null;
	}
}
