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
package org.routing.path;

import java.util.ArrayList;

import org.routing.reservable.RDijkstraNode;
import org.system.Reservation;
import org.system.Reservations;
import org.system.Road;
import org.system.RoadPoint;
import org.system.Terminal;
import org.time.Time;
import org.time.TimeScheduler;
import org.time.TimeWindow;
import org.util.Location;
import org.vehicles.StraddleCarrier;
import org.vehicles.models.SpeedCharacteristics;



/**
 * Path for straddle carrier. Can only be created from a NodePath by adding origin location and destination location.
 * @author gaetan
 */
public class Path{
	private ArrayList<PathNode> path;
	private double cost;
	private double costInMeters;
	private String vehicleId;

	private int originalSize;
	
	public Path(String vehicleId){
		path = new ArrayList<PathNode>();
		cost = 0;
		costInMeters = 0;
		this.vehicleId = vehicleId;
	}

	public Path(String vehicleId, int size){
		path = new ArrayList<PathNode>(size);
		cost = 0;
		costInMeters = 0;
		this.vehicleId = vehicleId;
	}

	public Path(Path p){
		this.path = new ArrayList<PathNode>(p.size());
		this.cost = p.cost;
		this.costInMeters = p.costInMeters;
		for(int i=0 ; i<p.size() ; i++){
			path.add(p.get(i));
		}
		this.vehicleId = p.vehicleId;
	}
	public Path(Location a, Location b, Time startTime, RoutingPath p, StraddleCarrier vehicle) {
		this(a,b,startTime,p,vehicle,0.0);
	}
	/**
	 * Build a path from a to b passing by nodes of p 
	 * @param a Origin
	 * @param b Destination
	 * @param p RoutingPath
	 */
	public Path(Location a, Location b, Time startTime, RoutingPath p, StraddleCarrier vehicle, double lastWaitingTime) {
		this(vehicle.getId());
		costInMeters = 0;
		if(p.size()>0){
			 
			SpeedCharacteristics speed = vehicle.getModel().getSpeedCharacteristics();
			RoutingNode from = null;
			PathNode fromPN = null;
			RoadPoint fromRP = null;
			
			for(int i=0; i<p.size(); i++){
				RoutingNode n = p.get(i);
				//System.err.println("Path["+(i+1)+"/"+p.size()+"]="+n.getNodeId());
				RoadPoint rp = Terminal.getInstance().getNode(n.getNodeId());
				Time currentTime = null;
				if(n instanceof RDijkstraNode)
				 currentTime = ((RDijkstraNode)n).getArrivalTime();
				//else System.out.println(n+" is not a RRoutingNode !!!!");
				//else currentTime = startTime;
				//System.err.println("\tCurrentTime="+currentTime);
				Road r;
				if(from == null) {
					r = a.getRoad();

					RoadPoint rpA = r.getPreviousRoadPoint(a.getPourcent());
					RoadPoint rpB = r.getNextRoadPoint(a.getPourcent());
					RoadPoint current = rpB;
					if(rpA.getId().equals(n.getNodeId())){
						current = rpA;
					}

					double currentRate = Location.getAccuratePourcent(current.getLocation(), r);
					double gap = currentRate-a.getPourcent();
					boolean direction;
					double time = 0;
					if(a.getDirection()){
						if(gap >= 0){
							direction = true;

						}
						else{
							direction = false;
							time += speed.getTurnBackTime();
						}
					}
					else{
						if(gap <= 0){
							direction = false;
						}
						else{
							direction = true;
							time += speed.getTurnBackTime();
						}
					}


					double length = a.getLength(Math.abs(gap));
					costInMeters+= length;
					length /= vehicle.getSpeed(r, currentTime);
					double localCost = length+time;
					cost+=localCost;
					if(currentTime != null)
						fromPN = new PathNode(new Location(r, currentRate, direction),new TimeWindow(startTime,currentTime));
					else
						fromPN = new PathNode(new Location(r, currentRate, direction));
				}
				else {
					r = Terminal.getInstance().getRoadBetween(from.getNodeId(), n.getNodeId());

					//TODO Check if it works with other routing algorithms (APSP, AStar)
					double currentRate = Location.getAccuratePourcent(fromRP.getLocation(), r);
					double nextRate = Location.getAccuratePourcent(rp.getLocation(), r);


					boolean direction = false;
					if(nextRate>=currentRate) direction = true;

					//if(currentTime != null){
						
						//TODO Check for the +1step
						//Time fromPlusOne = new Time(from.getArrivalTime(), new Time(1));
						TimeWindow tw = null;
						if(from instanceof RDijkstraNode)
						 tw = new TimeWindow(((RDijkstraNode)from).getArrivalTime(), currentTime);
						else tw = new TimeWindow(new Time(0), new Time(0));
						
						fromPN = new PathNode(new Location(r, nextRate, direction), tw);
						
						//cost+=tw.getLength().getInSec();
				}
				path.add(fromPN);
				from = n;
				fromRP = rp;
			}
			//TODO
			cost+=p.getCost();
			costInMeters += p.getCostInMeters();
			
			//Last location
			Road r = b.getRoad();

			double currentRate = Location.getAccuratePourcent(fromRP.getLocation(), r);
			double pourcentDestination = b.getPourcent();
			boolean direction = false;
			if(currentRate<=pourcentDestination) direction = true;

			double time = b.getLength(Math.abs(currentRate-pourcentDestination));
			costInMeters += time;
			if(from instanceof RDijkstraNode && ((RDijkstraNode)from).getArrivalTime()!=null){
				//Time fromPlusOne = new Time(from.getArrivalTime(), new Time(1));
				//Time arrivalTime = ((RDijkstraNode)from).getArrivalTime();
				
				time/= vehicle.getSpeed(r, ((RDijkstraNode)from).getArrivalTime());
				cost+=time;
				//TODO : Do we take the container handling time into account in the cost of the path ? or just to reserve the road?
				time += Math.max(vehicle.getModel().getSpeedCharacteristics().getContainerHandlingTimeFromGroundMAX(), vehicle.getModel().getSpeedCharacteristics().getContainerHandlingTimeFromTruckMAX());
				//time+= container.handlingTime.getInSec();
				
				Time arrivalTime = new Time(((RDijkstraNode)from).getArrivalTime() , new Time(lastWaitingTime));
				
				TimeWindow tw = new TimeWindow(arrivalTime, new Time(arrivalTime, new Time(time)));
				Reservations rs = Terminal.getInstance().getReservations(r.getId());
				if(rs == null) tw = new TimeWindow(arrivalTime, new Time(Time.MAXTIME));
				else	tw = rs.getFirstAvailableTimeWindow(new Reservation(arrivalTime, vehicleId, r.getId(), tw, Reservation.PRIORITY_GO_IN));
				
				path.add(new PathNode(
						new Location(r, pourcentDestination, direction),
						tw));
			}
			else{
				time /= vehicle.getSpeed(r, null);
				//if(r instanceof Lane) time/=speed.getLaneSpeed();
				//else time/=speed.getSpeed();
				cost+=time;
				path.add(new PathNode(new Location(r, pourcentDestination, direction)));
			}
		}
		
		//TODO
		//System.out.println("1) Path.cost = "+cost+" Path.costINMeters = "+costInMeters+" RP.cost = "+p.getCost()+" RP.costInM = "+p.getCostInMeters());
		//updateCost();
		//System.out.println("2) Path.cost = "+cost+" Path.costINMeters = "+costInMeters+" RP.cost = "+p.getCost()+" RP.costInM = "+p.getCostInMeters());
	}
	
	/*private void updateCost(){
		if(path.size() == 0){
			cost = costInMeters = 0;
		}
		else{
			Location l = start;
			double cDist = 0.0;
			double cTime = 0.0;
			for(int i=0; i<path.size();i++){
				PathNode next = path.get(i);
				
				double length = Location.getLength(l.getCoords(),next.getLocation().getCoords());
				boolean lane = next.getLocation().getRoad() instanceof Lane;
				double v = model.getSpeed(true, false, lane);
			//	System.out.println("DISTANCE ( "+l+" => "+next.getLocation()+" : "+length);
				cDist+=length;
				cTime += length/v;
				l = next.getLocation();
			}
			double length = Location.getLength(l.getCoords(),end.getCoords()); 
			boolean lane = end.getRoad() instanceof Lane;
			double v = model.getSpeed(true, false, lane);
			cDist+= length;
			cTime += length/v;

			cost = cTime;
			costInMeters = cDist;
			
			//System.out.println("TOTAL ( "+start+" => "+end+" : "+costInMeters+"m so : "+new Time(cost+"s"));
			//System.out.println("TOTAL ("+start+" => "+end+" : "+c);
		}
	}*/
	public double getCostInMeters(){
		return costInMeters;
		/*if(path.size() == 0) return 0;
		else{
			Location l = start;
			double c = 0.0;
			for(int i=0; i<path.size();i++){
				PathNode next = path.get(i);
				
				double length = Location.getLength(l.getCoords(),next.getLocation().getCoords());
			//	System.out.println("DISTANCE ( "+l+" => "+next.getLocation()+" : "+length);
				c+=length;
				l = next.getLocation();
			}
			double length = Location.getLength(l.getCoords(),end.getCoords()); 
			
			c+= length;
			//System.out.println("DISTANCE ( "+l+" => "+end+" : "+length);
			//System.out.println("TOTAL ("+start+" => "+end+" : "+c);
			return c;
		}*/
		
	}


	public int size(){
		return path.size();
	}

	public PathNode get(int index){
		return path.get(index);
	}

	public PathNode peek(){
		return get(0);
	}
	
	public void push(PathNode head)  {
		path.add(0, head);
		
		//Should'nt we recompute the cost of the path ?
		
		if(head.getTimeWindow() != null){
			//boolean b = head.getLocation().getRoad().unreserve(vehicleId, head.getTimeWindow());
			int priority = Reservation.PRIORITY_GO_IN;
			if(path.size() == originalSize && originalSize > 1) priority = Reservation.PRIORITY_GO_OUT;
			else if(path.size() > 1) priority = Reservation.PRIORITY_GO_THROUGH;
			boolean b = Terminal.getInstance().reserveRoad(head.getLocation().getRoad().getId(),vehicleId, head.getTimeWindow(), priority);
			//System.out.println("UNRESERVE "+head.getLocation().getRoad()+" for "+vehicleId);
			if(!b) System.out.println("Problem when re-reserving "+head.getLocation().getRoad().getId()+" for "+vehicleId+" at "+head.getTimeWindow());
		}
		else {
			System.err.println("Debug!!!");
		}
		//updateCost();
	}
	
	public PathNode poll()  {
		//Should'nt we recompute the cost of the path ?
		PathNode head = path.remove(0);
		if(head.getTimeWindow() != null){
			/*boolean b = */Terminal.getInstance().unreserve(head.getLocation().getRoad().getId(),vehicleId, head.getTimeWindow());
			//System.out.println("UNRESERVE "+head.getLocation().getRoad()+" for "+vehicleId);
			//if(!b) System.out.println("Problem when unreserving "+head.getLocation().getRoad().getId()+" for "+vehicleId+" at "+head.getTimeWindow());
		}
		//updateCost();
		return head;
	}

	/*public void add(PathNode n){
		path.add(n);
		//updateCost();
	}*/

	/*public void add(int index , PathNode n){
		path.add(index, n);
		//updateCost();
	}*/

	
	public double getCost(){
		return cost;
	}

	public void simplify(){
		if(path.size()>0){
			ArrayList<PathNode> newPath = new ArrayList<PathNode>(path.size());
			newPath.add(path.get(0));

			if(path.size()>1){
				PathNode current = path.get(0);

				for(int i=1 ; i<path.size() ; i++){
					PathNode next = path.get(i);
					if(current.getLocation().getRoad().getId().equals(next.getLocation().getRoad().getId())){
						newPath.remove(newPath.size()-1);
					}
					newPath.add(next);
					current = next;
				}
				path = newPath;
			}
		}
		originalSize = path.size();
		//updateCost();
	}

	public boolean reserve() {
		/*if(path.size()>0){
			PathNode pn = path.get(0);
			boolean b = pn.getLocation().getRoad().forceReservation(vehicleId, pn.getTimeWindow());
			
		}*/
		for(int i=0; i<path.size() ; i++){
			PathNode pn = path.get(i);
			int priority = Reservation.PRIORITY_GO_THROUGH;
			if(i==0) priority = Reservation.PRIORITY_GO_OUT;
			else if(i==path.size()-1) priority = Reservation.PRIORITY_GO_IN;
			boolean b = Terminal.getInstance().reserveRoad(pn.getLocation().getRoad().getId(),vehicleId, pn.getTimeWindow(), priority);
			//boolean b = pn.getLocation().getRoad().reserve(vehicleId, pn.getTimeWindow(), priority);
			
			if(!b){
				//if(
				System.out.println(TimeScheduler.getInstance().getTime()+
						"> CAN'T RESERVE ROAD "+pn.getLocation().getRoad()+
						" at "+pn.getTimeWindow()+
						" for "+vehicleId+" and priority "+priority+" RESERVATIONS : \n"+
						pn.getLocation().getRoad().getReservationsTableToString()+
						" PATH : \n");
				
				for(int j=0; j<path.size() ; j++){
					PathNode pn2 = path.get(j);
					System.out.println(pn2);
				}
				return false; 
			}
		}
		return true;
	}

	public String toString(){
		StringBuilder sb = new StringBuilder();
		sb.append(" Follow this path : \n");
		for(int i=0; i<path.size(); i++) {
			PathNode pn = path.get(i);
			Location l = pn.getLocation();
			sb.append(i+"> | take road "+l.getRoad()+" in direction "+l.getDirection()+" to "+l.getPourcent()+"\n");
		}
		sb.append("----\n");
		sb.trimToSize();
		return sb.toString();
	
	}
	
}