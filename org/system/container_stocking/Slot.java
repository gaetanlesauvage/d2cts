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
package org.system.container_stocking;

import java.util.ArrayList;
import java.util.List;

import org.exceptions.ContainerNotFoundException;
import org.exceptions.EmptyLevelException;
import org.exceptions.EmptySlotException;
import org.exceptions.NotAccessibleContainerException;
import org.exceptions.container_stocking.delivery.CollisionException;
import org.exceptions.container_stocking.delivery.FallingContainerException;
import org.exceptions.container_stocking.delivery.LevelException;
import org.exceptions.container_stocking.delivery.NotEnoughSpaceException;
import org.exceptions.container_stocking.delivery.SlotContainerIncompatibilityException;
import org.exceptions.container_stocking.delivery.UnstackableContainerException;
import org.missions.Mission;
import org.positioning.Coordinates;
import org.system.Terminal;
import org.system.container_stocking.Level.CollisionData;
import org.system.container_stocking.Level.ContainerAddingHelper;
import org.util.Location;



public class Slot {
	
	public static final double SLOT_20_FEET_LENGTH = 6.095975;
	public static final double SLOT_40_FEET_LENGTH = 12.19195;
	public static final double SLOT_45_FEET_LENGTH = 15.350734;

	public static final int ALIGN_ORIGIN = -1;
	public static final int ALIGN_CENTER = 0;
	public static final int ALIGN_DESTINATION = 1;

	public static final int SLOT_MAX_LEVEL = 2;
	public static final int SLOT_VEHICLE_MAX_LEVEL = 1;

	public static final double WIDTH = 2.44;
	private Location location;
	private Bay lane;
	private String id, paveID;
	private BlockType paveType;
	private boolean vehicleOn;

	private List<Level> levels;
	private double teuSize;

	private int maxLevels;

	public Slot(String id, Bay lane, Location location, double teuSize, BlockType paveType){
		this.paveType = paveType;

		if(paveType == BlockType.YARD || paveType == BlockType.DEPOT || paveType == BlockType.SHIP) vehicleOn = true;
		else vehicleOn = false;

		if(paveType == BlockType.RAILWAY || paveType == BlockType.ROAD) maxLevels = SLOT_VEHICLE_MAX_LEVEL;
		else maxLevels = SLOT_MAX_LEVEL;


		this.lane = lane;

		this.location = location;
		this.id = id;
		this.teuSize = teuSize;
		this.paveID = lane.getPaveId();
		//levels = Collections.synchronizedList(new ArrayList<Level>());
		levels = new ArrayList<Level>();
	}

	public Coordinates getCoords(){
		return location.getCoords();
	}

	public String getCSSStyle(){
		String style = "";
		/* CSS Save when box will be available
		 * style = "shape: box; sprite-orientation: projection; fill-mode: plain; size-mode: normal; size: "+SLOT_20_FEET_LENGTH+"gu,"+WIDTH+"gu; fill-color: "+
		 * "green" + "; stroke-mode: plain; stroke-color: black; stroke-width: 0.1gu;";
		 */
		String orientation = "from";
		if(location.getPourcent()>0.5) orientation = "to";
		
		
		
		
		if(vehicleOn) {
			if(paveType == BlockType.ROAD)	return "shape: box; fill-mode: image-scaled; fill-image: url('"+Terminal.IMAGE_FOLDER+"trailer.png"+"'); sprite-orientation: "+orientation+"; size: "+getLength()+"gu, "+(WIDTH+1)+"gu;";
			else	if(paveType == BlockType.RAILWAY) return "shape: box; fill-mode: image-scaled; fill-image: url('"+Terminal.IMAGE_FOLDER+"wagon2.png"+"'); sprite-orientation: "+orientation+"; size: "+getLength()+"gu, "+(WIDTH+1)+"gu;";
			else style="shape: box; fill-mode: plain; fill-color: rgba(255,255,255,100); ";
		}
		else style = "shape: box; fill-mode: plain; fill-color: rgba(255,255,0,100); ";
		
		
		style+="sprite-orientation: "+orientation+"; stroke-mode: dots; stroke-color: ";
		
		double length;
		double width;

		if(teuSize == 1f){
			style += "rgba(0,175,0,255)";
			length = ContainerKind.getLength(Container.TYPE_20_Feet);
			width = ContainerKind.getWidth(Container.TYPE_20_Feet);
		}
		else if(teuSize == 2f){
			style += "rgba(175,0,0,255)";
			length = ContainerKind.getLength(Container.TYPE_40_Feet);
			width = ContainerKind.getWidth(Container.TYPE_40_Feet);
		}
		else{
			style += "rgba(0,0,175,255)";
			length = ContainerKind.getLength(Container.TYPE_45_Feet);
			width = ContainerKind.getWidth(Container.TYPE_45_Feet);
		}
		style+= "; stroke-width: 0.1gu; size: "+length+"gu,"+width+"gu;";
		
		return style;
	}

	public String getId() {
		return id;
	}

	public double getLength(){
		if(this.teuSize == 1) return SLOT_20_FEET_LENGTH;
		else if(this.teuSize == 2) return SLOT_40_FEET_LENGTH;
		else return SLOT_45_FEET_LENGTH;
	}

	public Location getLocation() {
		return location;
	}
	public String getPaveId(){
		return paveID;
	}
	public double getRateOnLane() {
		return Location.getLength(location.getCoords(), lane.getOrigin().getLocation())/lane.getLength();
	}
	public double getTEU(){
		return teuSize;
	}
	public Container pop (String id) throws EmptySlotException, NotAccessibleContainerException, ContainerNotFoundException {
		//System.out.println("Before pop : "+this);
		Container c = null;
		if(levels.size()==0) throw new EmptySlotException(this.id);
		Level lastLevel = levels.get(levels.size()-1);
		try {
			c = lastLevel.removeContainer(id);
			//System.out.println("Lastlevel.remove = "+c.getId());
			//TODO check if the TEU is set exactly to 0 when all containers have been removed!
			if(lastLevel.getTEU() < 1f){
				//	System.out.println("Removing level "+(levels.size()-1));
				levels.remove(levels.size()-1);
			}
			//else levels.set(levels.size()-1, lastLevel);

		} catch (ContainerNotFoundException e) {
			//System.out.println("Container not found :( "+id+" last level : "+lastLevel);
			for(int i=levels.size()-2; i>=0 ; i--){
				Level l = levels.get(i);
				//System.out.println("Level "+l.getLevelIndex()+" : "+l);
				if(l.contains(id)){
					//System.out.println("Container not accessible ! ("+l.getLevelIndex()+")");
					throw new NotAccessibleContainerException(id);
				}
			}
		}
		//System.out.println("After pop : "+this);
		return c;
	}

	public boolean isReady(){
		if(paveType == BlockType.YARD || paveType == BlockType.DEPOT || paveType == BlockType.SHIP) return true;
		else{
			if(vehicleOn){
				return true;
			}
			else{
				//System.out.println("Slot "+getId()+" is not ready !");
				return false;
			}
		}
	}
	public void setVehicleOn(boolean value){
		vehicleOn = value;
	}
	public boolean canAddContainer(Container container, int level, int align) {
		//ContainerLocation location = new ContainerLocation(container.getId(), lane.getPaveId(), lane.getId(), id, level, align);
		//Si le conteneur est + gd que le slot
		if(container.getTEU()>teuSize) return false;
		else{

			Level current = null;
			Level inf = null;
			//UnstackableContainer
			if(level>0){
				if(levels.size()<level) return false;
				else{
					inf = levels.get(level-1);

					//Si le niveau n'a pas ete cree
					if(levels.size()==level){
						//TODO check > || >= !
						//Si le niveau max a été atteint
						if(level>maxLevels) return false;

						current = new Level(this, teuSize, inf.getZNext(), level);
						//On ajoute le level
						levels.add(current);
						//System.out.println("Adding level "+level+" in slot : "+current);
					}
					//Sinon on le recupere
					else{
						current = levels.get(level);
					}

				}
			}
			//Si c le rez de chausse qu'on veut
			else {
				//S'il n'a pas deja ete cree : 
				if(levels.size() == 0) {
					current = new Level(this, teuSize, this.location.getCoords().z, level);

					levels.add(current);
					//System.out.println("Adding level "+level+" in slot : "+current);
				}
				//Sinon on le recupere
				else current = levels.get(level);
			}

			ContainerAddingHelper helper;
			try {
				helper = current.getCoords(container, align);
			} catch (NotEnoughSpaceException e1) {
				return false;
			} catch (CollisionException e1) {
				return false;
			}
			if(helper == null) return false;

			if(inf != null){		
				try {
					if(inf.getFirstContainer().getTEU()<container.getTEU()) return false;
				} catch (EmptyLevelException e) {
					return false;
				}
				//verifier qu'il y a qqch en dessous
				//FallingContainer
				List<CollisionData> dataInfList = inf.getCollisionData();
				CollisionData data = helper.data;
				boolean fromOk = false;
				boolean toOk = false;
				for(CollisionData dataInf : dataInfList){
					double from = dataInf.fromRate;
					double to = dataInf.toRate;
					if(from <= data.fromRate){
						fromOk = true;
						//	System.out.println("from<=data.from : "+from+"<="+data.fromRate);
					}
					if(to>=data.toRate){
						toOk = true;
						//	System.out.println("to>=data.to : "+to+">="+data.toRate);
					}
					if(fromOk&&toOk) break;
				}
				if(!fromOk||!toOk) return false;
			}
			//If we get there, we can add container
			//current.addContainer(helper);
			//levels.set(level, current);
			/*try {
				Terminal.getRMIInstance().setContainerLocation(container.getId(), location);
			} catch (RemoteException e) {
				e.printStackTrace();
			}*/
			//			System.out.println("StockCoords.coords = "+helper.getCoords());
			return true;
		}
	}
	public Coordinates stockContainer(Mission m) throws SlotContainerIncompatibilityException, FallingContainerException, UnstackableContainerException, NotEnoughSpaceException, CollisionException, LevelException {
		Container container = m.getContainer();
		int level = m.getDestination().getLevel();
		int align = m.getDestination().getAlign();
		ContainerLocation location = new ContainerLocation(container.getId(), lane.getPaveId(), lane.getId(), id, level, align);

		if(container.getTEU()>teuSize) throw new SlotContainerIncompatibilityException(m);
		else{
			Level current = null;
			Level inf = null;
			//UnstackableContainer
			if(level>0){
				if(levels.size()<level) {
					//System.out.println("HERE !!!");
					throw new FallingContainerException(m);
				}
				else{
					inf = levels.get(level-1);

					if(levels.size()==level){
						//TODO check > || >= !
						if(level>getMaxLevel()) throw new LevelException(m,maxLevels);
						current = new Level(this, teuSize, inf.getZNext(), level);
						levels.add(current);
						//System.out.println("Adding level "+level+" in slot : "+current);
					}
					else{
						current = levels.get(level);
					}

				}
			}
			else {
				if(levels.size() == 0) {
					current = new Level(this, teuSize, this.location.getCoords().z, level);

					levels.add(current);
					//System.out.println("Adding level "+level+" in slot : "+current);
				}
				else current = levels.get(level);
			}

			ContainerAddingHelper helper = current.getCoords(m,container, align);
			if(inf != null){		
				try {
					if(inf.getFirstContainer().getTEU()<container.getTEU()) throw new UnstackableContainerException(m);
				} catch (EmptyLevelException e) {
					throw new FallingContainerException(location);
				}
				//verifier qu'il y a qqch en dessous
				//FallingContainer
				List<CollisionData> dataInfList = inf.getCollisionData();
				CollisionData data = helper.data;
				boolean fromOk = false;
				boolean toOk = false;
				for(CollisionData dataInf : dataInfList){
					double from = dataInf.fromRate;
					double to = dataInf.toRate;
					if(from <= data.fromRate){
						fromOk = true;
						//	System.out.println("from<=data.from : "+from+"<="+data.fromRate);
					}
					if(to>=data.toRate){
						toOk = true;
						//	System.out.println("to>=data.to : "+to+">="+data.toRate);
					}
					if(fromOk&&toOk) break;
				}
				if(!fromOk||!toOk) {
					System.out.println("HERE 3 fromOk = "+fromOk+" toOk = "+toOk+" !!!!");
					for(CollisionData dataInf : dataInfList){
						double from = dataInf.fromRate;
						double to = dataInf.toRate;
						System.out.println("dataInf.from = "+from+" dataInf.to = "+to+" data.from = "+data.fromRate+" data.to = "+data.toRate);
						if(from <= data.fromRate){
							fromOk = true;
							//	System.out.println("from<=data.from : "+from+"<="+data.fromRate);
						}
						if(to>=data.toRate){
							toOk = true;
							//	System.out.println("to>=data.to : "+to+">="+data.toRate);
						}
					}
					throw new FallingContainerException(m);
				}
			}
			//If we get there, we can add container
			current.addContainer(helper);
			//levels.set(level, current);
			/*try {
				Terminal.getRMIInstance().setContainerLocation(container.getId(), location);
			} catch (RemoteException e) {
				e.printStackTrace();
			}*/
			//			System.out.println("StockCoords.coords = "+helper.getCoords());
			return helper.getCoords();
		}
	}


	public Coordinates stockContainer(
			Container container,
			int level,
			int align) throws SlotContainerIncompatibilityException, FallingContainerException, UnstackableContainerException, NotEnoughSpaceException, CollisionException, LevelException {
		ContainerLocation location = new ContainerLocation(container.getId(), lane.getPaveId(), lane.getId(), id, level, align);

		if(container.getTEU()>teuSize) throw new SlotContainerIncompatibilityException(location);
		else{
			Level current = null;
			Level inf = null;
			//UnstackableContainer
			if(level>0){
				if(levels.size()<level) {
					//System.out.println("HERE !!!");
					throw new FallingContainerException(location);
				}
				else{
					inf = levels.get(level-1);

					if(levels.size()==level){
						//TODO check > || >= !
						if(level>getMaxLevel()) throw new LevelException(location,maxLevels);
						current = new Level(this, teuSize, inf.getZNext(), level);
						levels.add(current);
						//System.out.println("Adding level "+level+" in slot : "+current);
					}
					else{
						current = levels.get(level);
					}

				}
			}
			else {
				if(levels.size() == 0) {
					current = new Level(this, teuSize, this.location.getCoords().z, level);

					levels.add(current);
					//System.out.println("Adding level "+level+" in slot : "+current);
				}
				else current = levels.get(level);
			}

			ContainerAddingHelper helper = current.getCoords(container, align);
			if(inf != null){		
				try {
					if(inf.getFirstContainer().getTEU()<container.getTEU()) throw new UnstackableContainerException(location);
				} catch (EmptyLevelException e) {
					throw new FallingContainerException(location);
				}
				//verifier qu'il y a qqch en dessous
				//FallingContainer
				List<CollisionData> dataInfList = inf.getCollisionData();
				CollisionData data = helper.data;
				boolean fromOk = false;
				boolean toOk = false;
				for(CollisionData dataInf : dataInfList){
					double from = dataInf.fromRate;
					double to = dataInf.toRate;
					if(from <= data.fromRate){
						fromOk = true;
						//	System.out.println("from<=data.from : "+from+"<="+data.fromRate);
					}
					if(to>=data.toRate){
						toOk = true;
						//	System.out.println("to>=data.to : "+to+">="+data.toRate);
					}
					if(fromOk&&toOk) break;
				}
				if(!fromOk||!toOk) {
					System.out.println("HERE 3 fromOk = "+fromOk+" toOk = "+toOk+" !!!!");
					for(CollisionData dataInf : dataInfList){
						double from = dataInf.fromRate;
						double to = dataInf.toRate;
						System.out.println("dataInf.from = "+from+" dataInf.to = "+to+" data.from = "+data.fromRate+" data.to = "+data.toRate);
						if(from <= data.fromRate){
							fromOk = true;
							//	System.out.println("from<=data.from : "+from+"<="+data.fromRate);
						}
						if(to>=data.toRate){
							toOk = true;
							//	System.out.println("to>=data.to : "+to+">="+data.toRate);
						}
					}
					throw new FallingContainerException(location);
				}
			}
			//If we get there, we can add container
			current.addContainer(helper);
			//levels.set(level, current);
			/*try {
				Terminal.getRMIInstance().setContainerLocation(container.getId(), location);
			} catch (RemoteException e) {
				e.printStackTrace();
			}*/
			//			System.out.println("StockCoords.coords = "+helper.getCoords());
			return helper.getCoords();
		}
	}
	public boolean contains(String containerId){
		for(Level l : levels){
			if(l.contains(containerId)) return true;
		}
		return false;
	}

	public List<Level> getLevels(){
		return levels;
	}

	public Level getLevel(int z){
		for(Level l : levels) if(l.getLevelIndex()==z) return l;

		if(z < maxLevels) return new Level(this, this.teuSize, this.getLocation().getCoords().z, z);
		else return null;
	}

	public String toString(){
		StringBuilder sb = new StringBuilder();
		sb.append("Slot "+id+" "+levels.size()+" levels : \n");
		for(Level l : levels){
			sb.append(l);
		}
		sb.append("\n"+levels.size()+" levels.");
		return sb.toString();
	}

	public int getMaxLevel(){
		return maxLevels;
	}

	public void destroy() {
		id=null;
		lane.destroy();
		lane=null;
		for(Level l : levels) l.destroy();
		levels.clear();
		levels=null;
		location.destroy();
		location=null;
		paveID=null;
		paveType=null;
		
	}
}
