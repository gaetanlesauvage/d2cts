package org.scheduling;

import java.util.ArrayList;
import java.util.List;

import org.time.Time;


public class LocalScore {
	private double distance;
	private double distanceInSeconds;
	private double overspentTime;
	private double waitTime;
	private int overspentTW;
	
	private ScheduleResource resource;
	private List<ScheduleEdge> path;
	private List<ScheduleTask<? extends ScheduleEdge>> pathNode;
	private List<Double> times;

	public LocalScore(ScheduleResource resource){
		this(resource, new ArrayList<ScheduleEdge>(), new ArrayList<Double>(), 0, 0, 0, 0, 0);
	}
	
	public LocalScore(ScheduleResource resource, List<ScheduleEdge> path, List<Double> times, double distance, double distanceInSeconds, double overspentTime, double waitTime, int overspentTW){
		this.resource = resource;
		this.path = path;
		this.pathNode = new ArrayList<ScheduleTask<? extends ScheduleEdge>>(path.size()+1);
		this.pathNode.add(MissionScheduler.SOURCE_NODE);
		for(ScheduleEdge e : path){
			this.pathNode.add(e.getNodeTo());
		}
		this.distance = distance;
		this.distanceInSeconds = distanceInSeconds;
		this.overspentTime = overspentTime;
		this.waitTime = waitTime;
		this.overspentTW = overspentTW;
		this.times = times;
		if(times.size()==0){
			times.add(resource.currentTime);
//			System.err.println("TIME.SIZE=0 => ADD "+new Time(resource.currentTime+"s"));
		}
	}

	public LocalScore(LocalScore score) {
		this.resource = score.resource;
		this.path = new ArrayList<ScheduleEdge>(score.getPath().size());
		for(ScheduleEdge e : score.getPath()){
			this.path.add(e);
		}
		this.pathNode = new ArrayList<ScheduleTask<? extends ScheduleEdge>>(path.size()+1);
		for(ScheduleTask<? extends ScheduleEdge> n : score.pathNode){
			this.pathNode.add(n);
		}
		this.distance = score.distance;
		this.distanceInSeconds = score.distanceInSeconds;
		this.overspentTime = score.overspentTime;
		this.waitTime = score.waitTime;
		this.overspentTW = score.overspentTW;
		this.times = new ArrayList<Double>(score.getTimes().size());
		for(Double d : score.getTimes()){
			this.times.add(d);
		}
	}
	
	public double getDistance(){
		return distance;
	}

	public double getTravelTime(){
		return distanceInSeconds;
	}

	public double getLateness(){
		return overspentTime;
	}

	public ScheduleResource getResource(){
		return resource;
	}

	public List<ScheduleEdge> getPath(){
		return path;
	}

	public List<ScheduleTask<? extends ScheduleEdge>> getNodePath(){
		return pathNode;
	}
	
	public List<Double> getTimes(){
		return times;
	}
	
	public double getEarliness(){
		return waitTime;
	}
	
	public int getOverspentTW() {
		return overspentTW;
	}
	
	public String toString(){
		StringBuilder sb = new StringBuilder();
		Double t = times.get(times.size()-1);
		
		sb.append(resource.getID()+"\t time="+new Time(t == null ? Double.NaN : t)+" d="+distance+"m\tt(d)="+new Time(distanceInSeconds)+"\toverTime="+new Time(overspentTime)+"\twaitTime="+new Time(waitTime));
		return sb.toString();
	}

	public String getNodePathString(){
		if(pathNode.size()>0){
			StringBuilder sb = new StringBuilder();
			for(ScheduleTask<? extends ScheduleEdge> n : pathNode){
				System.err.println("N = "+n.getID());
				sb.append(n.getID()+" -> ");
			}
			String sbString = sb.toString();
			return sbString;
		}
		else return "nil";
	}
	public String getPathString() {
		if(path.size()>0){
			StringBuilder sb = new StringBuilder();
			for(ScheduleEdge e : path){
				ScheduleTask<? extends ScheduleEdge> nTo = e.getNodeTo();
				sb.append(nTo.getID()+" -> ");
			}
			String sbString = sb.toString();
			return sbString.substring(0,sbString.length()-" -> ".length());
		}
		else return "nil";
	}

	public void addDistance(double distance){
		this.distance += distance;
	}
	
	public void addTravelTime(double travelTimeToAdd){
		this.distanceInSeconds += travelTimeToAdd;
	}
	
	public void addLateness(double latenessToAdd){
		this.overspentTime += latenessToAdd;
	}
	
	public void addEarliness(double earlinessToAdd){
		this.waitTime += earlinessToAdd;
	}

	public void addNode(ScheduleTask<? extends ScheduleEdge> n){
		pathNode.add(n);
	}
	
	public void addOverspentTW(int overspentTW){
		this.overspentTW += overspentTW;
	}
	
	public void addEdge(ScheduleEdge edge, double time) {
//		System.err.println("Add time "+new Time(time+"s")+" in "+resource.getID()+"'s LOCAL SCORE");
		path.add(edge);
		//pathNode.add(edge.getNodeTo());
		times.add(time);
	}
	
	public int getTaskCount(){
		return path.size();
	}
}
