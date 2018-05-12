package org.routing;

import org.routing.AStar.AStarHandler;
import org.routing.AStar.DijkstraHeuristic;
import org.routing.AStar.DistanceAndSpeedHeuristic;
import org.routing.AStar.YDistanceHeuristic;
import org.routing.dijkstra.DijkstraHandler;
import org.routing.reservable.RDijkstraHandler;
import org.vehicles.StraddleCarrier;

public enum RoutingAlgorithmType {
	DIJKSTRA("Dijkstra",null), APSP("APSP", null), ASTAR_DISTANCE("AStar","DijkstraHeuristic"),
	ASTAR_SPEED("AStar","DistanceAndSpeedHeuristic"),
	ASTAR_YDISTANCE("AStar","YDistanceHeuristic"),
	RDIJKSTRA("RDijkstra",null);
	
	private String name;
	private String heuristic;
	
	private RoutingAlgorithmType (String name, String heuristic){
		this.name = name;
		this.heuristic = heuristic;
	}
	
	public static RoutingAlgorithmType get(String name, String heuristic){
		for(RoutingAlgorithmType t : values()){
			if(t.name.equalsIgnoreCase(name) && ((heuristic == null && t.heuristic == null)||heuristic.equalsIgnoreCase(t.heuristic)))
				return t;
		}
		return null;
	}
	
	public Routing getNewRoutingAlgorithm(StraddleCarrier straddleCarrier){
		switch(this){
		case DIJKSTRA:
			return new DijkstraHandler(straddleCarrier);
		case APSP: 
			return new org.routing.APSP.APSP(straddleCarrier);
		case ASTAR_DISTANCE:
			AStarHandler a = new AStarHandler(straddleCarrier);
			a.setHeuristic(new DijkstraHeuristic(straddleCarrier.getModel().getSpeedCharacteristics()));
			return a;
		case ASTAR_SPEED:
			AStarHandler aS = new AStarHandler(straddleCarrier);
			aS.setHeuristic(new DistanceAndSpeedHeuristic(straddleCarrier.getModel().getSpeedCharacteristics()));
			return aS;
		case ASTAR_YDISTANCE:
			AStarHandler ay = new AStarHandler(straddleCarrier);
			ay.setHeuristic(new YDistanceHeuristic(straddleCarrier.getModel().getSpeedCharacteristics()));
			return ay;
		case RDIJKSTRA:
			return new RDijkstraHandler(straddleCarrier);
		}
		return null;
	}
}
