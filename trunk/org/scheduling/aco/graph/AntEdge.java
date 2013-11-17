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
package org.scheduling.aco.graph;

import java.awt.Color;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.SortedMap;
import java.util.TreeMap;

import org.graphstream.graph.Edge;
import org.graphstream.ui.spriteManager.Sprite;
import org.graphstream.ui.spriteManager.SpriteManager;
import org.scheduling.MissionScheduler;
import org.scheduling.ScheduleEdge;
import org.scheduling.onlineACO.OnlineACOScheduler;
import org.vehicles.StraddleCarrier;
import org.vehicles.StraddleCarrierColor;

/**
 * Edge of the ACO graph.
 *  
 * @author Ga&euml;tan Lesauvage
 * @since 2011
 */
public class AntEdge extends ScheduleEdge {
	/**
	 * Used to format the weight of the edge
	 */
	private static final NumberFormat format = new DecimalFormat("#.#######");
	/**
	 * Default color of the edge
	 */
	private static final String DEFAULT_COLOR_STRING = "white";
	private static final Color DEFAULT_COLOR = Color.white;
	/**
	 * Default CSS style for the edge
	 */
	private static final String STYLE = "shape: line; visibility-mode: normal; fill-mode: plain; fill-color: white; stroke-mode: plain;"+
			"stroke-color: rgba(0,0,0,200); stroke-width: 2; text-size: 9; text-color: black; text-mode: normal;"+
			"text-alignment: along; text-padding: 1px; text-background-mode: plain;"+
			"text-background-color: rgb(200,200,200); size: 1;";
	/**
	 * Default CSS style for the weights sprites
	 */
	private static final String WEIGHT_STYLE = "shape: box; fill-mode: none; visibility-mode: normal; text-visibility-mode: normal; text-background-mode: plain; " +
			"text-offset: -8px, -8px; text-size: 6; text-mode: normal; " +
			"text-style: bold;	text-padding: 1px; size-mode: fit;";

	/**
	 * Color of the edge (defined by the color of the origin and the destination nodes colors)
	 */
	private String color;

	/**
	 * Shape of the edge
	 */
	private String shape="line";

	/**
	 * Weights of the edge
	 */
	//private HashMap<String, Double> weights;

	/**
	 * Sprites used to display the weights along the edge
	 */
	private SortedMap<String, Sprite> weightsSprites;

	/**
	 * Used for layouting the sprites of the weights
	 */
	private SortedMap<String, Integer> indexes;

	/**
	 * Corresponding edge
	 */
	private Edge edge;

	/**
	 * Constructor
	 * @param from Origin of the edge
	 * @param to Destination of the edge
	 */
	public AntEdge (AntNode origin, AntNode destination){
		super(origin, destination);
		//		/*else*/ heuristic = new Fitness(this);//TimeWeightHeuristic(this);

		//		this.ID = "("+from.getId()+"->"+to.getId()+")";
		edge = OnlineACOScheduler.getInstance().getGraph().addEdge(getID(), origin.getID(), destination.getID(), true);
		edge.addAttribute("ui.style", STYLE+"");
		//weights = new HashMap<String, Double>(resourcesSize);
		weightsSprites = new TreeMap<>();
		indexes = new TreeMap<>();
		color = DEFAULT_COLOR_STRING;
		computeColor();
	}



	/* ======================== WEIGHT ======================== */
	/*public void updateCost(StraddleCarrier resource){
		try {
			double d = 0.0;
			String key = resource.getId();
			Color color = StraddleCarrierColor.getColor(resource.getColor());
			int size = ACOMissionScheduler.resources.size();

			if(origin == ACOMissionScheduler.getDepotNode() || destination == ACOMissionScheduler.getEndNode()){
				d = getCost(resource);
			}
			else{
			     key = resource.getModel().getId();
			     color = Color.getColor(DEFAULT_COLOR);
			     size = MissionScheduler.getResourcesModelCount();
			}
			cost.put(key, d);

			if(!indexes.containsKey(key)) indexes.put(key, cost.size()-1);
				Sprite sprite = null;
				if(weightsSprites.containsKey(key)){
					sprite = weightsSprites.get(key);
				}
				else{
					sprite = ACOMissionScheduler.getSpriteManager().addSprite(getID()+"@"+key);
					sprite.addAttribute("ui.class", "weight");

					String sColor = "rgb("+color.getRed()+","+color.getGreen()+","+color.getBlue()+")";
					String bColor = "rgba(225,225,225,175)";
					if(((color.getRed()+color.getBlue()+color.getGreen())/3.0)>125){
						bColor = "rgba(25,25,25,175)";
					}
					String style = WEIGHT_STYLE;
					if(!ACOMissionScheduler.displayWeights()) style = style.replaceAll("visibility-mode: normal;", "visibility-mode: hidden;");

					sprite.addAttribute("ui.style", style+" text-color: "+sColor+"; text-background-color: "+bColor+";");
					sprite.attachToEdge(edge.getId());
					sprite.setPosition((1.0/(size+1.0))*(indexes.get(key)+1.0));
					weightsSprites.put(sprite.getId(), sprite);
				}
				sprite.addAttribute("ui.label", format.format(d));
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}*/
	/**
	 * Compute the weight of the edge for the given vehicle
	 * @param resource The resource concerned  by the computing of the weight of the edge
	 */
	@Override
	public void addCost(StraddleCarrier resource){
		double d = 0.0;
		String key = resource.getId();
		Color color = StraddleCarrierColor.getColor(resource.getColor());
		int size = OnlineACOScheduler.getInstance().getResources().size();

		if(origin == OnlineACOScheduler.getDepotNode() || destination == OnlineACOScheduler.getEndNode()){
			key = resource.getId();
			d = getCost(resource);

		}
		else{
			key = resource.getModel().getId();
			if(!cost.containsKey(key)) d = getCost(resource);
			else d = getCost(key);
			color = DEFAULT_COLOR;
			size = MissionScheduler.getInstance().getResourcesModelCount();
		}
		cost.put(key, d);

		if(!indexes.containsKey(key)) indexes.put(key, cost.size()-1);
		Sprite sprite = null;
		if(weightsSprites.containsKey(key)){
			sprite = weightsSprites.get(key);
		}
		else{
			sprite = OnlineACOScheduler.getInstance().getSpriteManager().addSprite(getID()+"@"+key);
			sprite.addAttribute("ui.class", "weight");

			String sColor = "rgb("+color.getRed()+","+color.getGreen()+","+color.getBlue()+")";
			String bColor = "rgba(225,225,225,175)";
			if(((color.getRed()+color.getBlue()+color.getGreen())/3.0)>125){
				bColor = "rgba(25,25,25,175)";
			}
			String style = WEIGHT_STYLE;
			if(!OnlineACOScheduler.getInstance().displayWeights()) style = style.replaceAll("visibility-mode: normal;", "visibility-mode: hidden;");

			sprite.addAttribute("ui.style", style+" text-color: "+sColor+"; text-background-color: "+bColor+";");
			sprite.attachToEdge(edge.getId());
			sprite.setPosition((1.0/(size+1.0))*(indexes.get(key)+1.0));
			weightsSprites.put(sprite.getId(), sprite);
		}
		sprite.addAttribute("ui.label", format.format(d));			
	}

	//TODO Could be grouped for every hills
	/**
	 * Compute weights for every vehicles
	 */
	public void computeWeight () {
		for(StraddleCarrier resource : OnlineACOScheduler.getInstance().getResources()){
			addCost(resource);
		}
	}

	/* ======================== GETTERS AND SETTERS ======================== */ 
	/**
	 * Get the color of the edge
	 * @return The color of the edge
	 */
	public String getColor(){
		return color;
	}

	/**
	 * Get the weight for the given vehicle
	 * @param resourceID ID of the vehicle 
	 * @return The weight for the given resource
	 */
	//	@Override
	//	public synchronized double getCost(String resourceID){
	//		double w = Double.POSITIVE_INFINITY;
	//		/*if(!weights.containsKey(resourceID)){ 
	//			computeWeight();
	//		}*/
	//		if(!cost.containsKey(resourceID)){
	//			System.err.println(getID()+" has no weight for "+resourceID);
	//		}
	//		w = cost.get(resourceID);
	//		
	//		return w;
	//	}

	/*Called for update weight from a similar edge and by the way avoid heavy computations...*/
	/**
	 * Set the weight of the edge for the given resource
	 * @param resource Vehicle concerned by the weight update
	 * @param newWeight The new weight of the edge for the given resource
	 */
	/*public synchronized void setWeight(StraddleCarrier resource, double newWeight){
		try{
			String resourceID = resource.getId();
			weights.put(resourceID,newWeight);
			if(!indexes.containsKey(resource.getId())) indexes.put(resource.getId(), weights.size()-1);
			Sprite sprite = null;
			if(weightsSprites.containsKey(resourceID)){
				sprite = weightsSprites.get(resourceID);
			}
			else{
				sprite = from.getScheduler().getSpriteManager().addSprite(this.ID+"@"+resourceID);
				sprite.addAttribute("ui.class", "weight");
				Color color = StraddleCarrierColor.getColor(resource.getColor());
				String sColor = "rgb("+color.getRed()+","+color.getGreen()+","+color.getBlue()+")";
				String bColor = "rgba(225,225,225,175)";
				if(((color.getRed()+color.getBlue()+color.getGreen())/3.0)>125){
					bColor = "rgba(25,25,25,175)";
				}
				String style = WEIGHT_STYLE;
				if(!from.getScheduler().displayWeights()) style = style.replaceAll("visibility-mode: normal;", "visibility-mode: hidden;");

				sprite.addAttribute("ui.style", style+" text-color: "+sColor+"; text-background-color: "+bColor+";");
				sprite.attachToEdge(edge.getId());
				sprite.setPosition((1.0/(from.getScheduler().getResources().size()+1.0))*(indexes.get(resourceID)+1.0));
				weightsSprites.put(sprite.getId(), sprite);
			}
			sprite.addAttribute("ui.label", format.format(newWeight));
		}
		catch(RemoteException e){
			e.printStackTrace();
		}
	}*/

	/**
	 * Set the weight heuristic for the edge
	 * @param heuristic The weight heuristic
	 */
	/*public void setWeightHeristic(WeightHeuristic heuristic) {
		this.heuristic = heuristic;
		computeWeight();
	}*/

	/* ============================== GUI ============================== */
	/**
	 * Display or hide the weight along the edge on the GUI
	 * @param display
	 */
	public void displayWeight(boolean display){
		for(Sprite s : weightsSprites.values()){
			String newStyle = s.getAttribute("ui.style");
			if(display) newStyle = newStyle.replaceAll("visibility-mode: hidden;", "visibility-mode: normal;");
			else newStyle = newStyle.replaceAll("visibility-mode: normal;", "visibility-mode: hidden;");
			s.setAttribute("ui.style", newStyle);
		}
	}

	/**
	 * Compute the color of the node. If the origin node and the destination node have the same color then the edge is colored by this color. Else it take the default color. 
	 */
	public void computeColor(){
		color = AntEdge.DEFAULT_COLOR_STRING;

		if(origin instanceof DepotNode && destination instanceof AntMissionNode){
			color = ((AntMissionNode)destination).getUIColor();
		}
		else if(origin instanceof AntMissionNode && destination instanceof AntMissionNode){
			String colorFrom = ((AntMissionNode)origin).getUIColor();
			String colorTo =  ((AntMissionNode)destination).getUIColor();
			if(colorFrom.equals(colorTo)){
				color = colorFrom;
			}
		}
		else if(destination instanceof EndNode && !(origin instanceof DepotNode)){
			color = ((AntMissionNode)origin).getUIColor();
		}
		if(color == null || color.equals(AntNode.DEFAULT_STYLE_COLOR)) color = AntEdge.DEFAULT_COLOR_STRING;

		updateUI();
	}

	/**
	 * Update the CSS style of the edge
	 */
	private void updateUI(){
		String old_ui = edge.getAttribute("ui.style");
		int i1 = old_ui.indexOf("shape:");
		int i2 = old_ui.indexOf(";", i1);
		old_ui = old_ui.substring(0, i1) + "shape: "+shape+ ";"+old_ui.substring(i2+1, old_ui.length());
		i1 = old_ui.indexOf("fill-color:");
		i2 = old_ui.indexOf(";", i1);
		old_ui = old_ui.substring(0, i1) + "fill-color: "+color+ ";"+old_ui.substring(i2+1, old_ui.length());
		edge.setAttribute("ui.style", old_ui);
	}

	@Override
	public String toString(){
		//if(weights == null) System.err.println(getID()+" weight is null!");
		String s = getID()+" w=[";
		for(String key : cost.keySet()) s+="("+key+" : "+cost.get(key)+")";
		return s+"]";
	}

	/**
	 * Destructor
	 */
	public void destroy(){
		//		if(weights !=null){
		//			weights.clear();
		//			weights = null;
		//		}

		if(weightsSprites != null){
			SpriteManager manager = OnlineACOScheduler.getInstance().getSpriteManager();
			for(Sprite s : weightsSprites.values()){
				manager.removeSprite(s.getId());
			}
			weightsSprites.clear();
			weightsSprites = null;
		}

		if(edge != null){
			OnlineACOScheduler.getInstance().getGraph().removeEdge(origin.getID(), destination.getID());
			edge = null;
		}
		super.destroy();
	}
}