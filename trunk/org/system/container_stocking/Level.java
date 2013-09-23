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
import org.exceptions.container_stocking.delivery.CollisionException;
import org.exceptions.container_stocking.delivery.NotEnoughSpaceException;
import org.missions.Mission;
import org.positioning.Coordinates;





public class Level {
	class CollisionData {
		double fromRate, toRate;
		int align;

		public CollisionData(double fromRate, double toRate, int align){
			this.fromRate = fromRate;
			this.toRate = toRate;
			this.align = align;
		}

		public String toString(){
			return "<"+fromRate+" ; "+toRate+">";
		}

		public void destroy() {
			
		}
	}

	class ContainerAddingHelper {
		CollisionData data;
		int addingIndex;
		Container container;
		Coordinates coords;

		public ContainerAddingHelper (CollisionData data, Container container, Coordinates coords, int addingIndex){
			this.data = data;
			this.container = container;
			this.coords = coords;
			this.addingIndex = addingIndex;
		}

		public int getAddingIndex(){
			return addingIndex;
		}

		public Container getContainer(){
			return container;
		}

		public Coordinates getCoords(){
			return coords;
		}

		public CollisionData getData(){
			return data;
		}
	}
	
	private double teu, maxTEU;
	private Slot slot;
	private double z;
	private int levelIndex;

	private List<Container> content;
	private List<CollisionData> occupiedRates;
	
	public Level (Slot slot, double maxTEU, double z, int level){
		this.teu = 0;
		this.maxTEU = maxTEU;
		this.slot = slot;
		this.z = z;
		this.levelIndex = level;
		content = new ArrayList<Container>(2);
		occupiedRates = new ArrayList<CollisionData>(2);
		
		//System.out.println("---------------> New Level : "+levelIndex+" in thread : "+Thread.currentThread().getId());
	}

	public void addContainer(ContainerAddingHelper helper) {
		content.add(helper.getAddingIndex(), helper.getContainer());
		this.occupiedRates.add(helper.getAddingIndex(),helper.getData());
		helper.getContainer().setLevel(levelIndex);
		this.teu += helper.getContainer().getTEU();
		//System.out.println("Ajout de "+helper.container.getId()+" (teu="+helper.container.getTEU()+") sur "+slot.getId()+" level "+levelIndex+" : teu="+teu);
	}

	public boolean contains(String containerId){
		for(Container c : content){
			if(c.getId().equals(containerId)) {
				return true;
			}
		}
		return false;
	}

	public List<CollisionData> getCollisionData(){
		return this.occupiedRates;
	}
	public ContainerAddingHelper getCoords(Mission m, Container container, int align) throws NotEnoughSpaceException, CollisionException{
		if(this.teu + container.getTEU() > maxTEU ) {
			//System.out.println("level teu = "+teu+" containerTeu = "+container.getTEU()+" > maxTEU = "+maxTEU);
			throw new NotEnoughSpaceException(m);
		}

		Coordinates containerCenter;
		Coordinates slotCoords = slot.getCoords();
		double rateContainer;
		
		double containerLength = ContainerKind.getLength(container.getDimensionType());
		double radiusRate = (containerLength/2.0)/slot.getLocation().getRoad().getLength();
		if(align == Slot.ALIGN_CENTER) {
			containerCenter = new Coordinates(slotCoords.x, slotCoords.y, z+(ContainerKind.getHeight(container.getDimensionType())/2f));
			//rateContainer = Location.getPourcent(containerCenter, slot.getLocation().getRoad());
			//rateContainer = Location.getAccuratePourcent(containerCenter, slot.getLocation().getRoad());
			rateContainer = slot.getLocation().getPourcent();
		}
		else {
			double leftRate = (slot.getLength()/2.0) / slot.getLocation().getRoad().getLength();
			//double leftRate = 0.0;
			//System.out.println("Container "+container.getId()+" LeftRate = "+leftRate);
			if(containerLength < slot.getLength()){
			if(align == Slot.ALIGN_ORIGIN)
				rateContainer = slot.getLocation().getPourcent() - leftRate + radiusRate;
				//rateContainer =  (containerLength / slot.getLength() ) / 2.0;

			else
				rateContainer = slot.getLocation().getPourcent() + leftRate - radiusRate;
				//rateContainer =  0.5 + ((containerLength / slot.getLength()) / 2.0);
			}
			else rateContainer = slot.getLocation().getPourcent();
			
			//System.out.println("Container "+container.getId()+" Rate = "+rateContainer);
			containerCenter = slot.getLocation().getCoords(rateContainer);
			containerCenter.z = z+(ContainerKind.getHeight(container.getDimensionType())/2f);
		}
		double leftRate = rateContainer - radiusRate;
		double rightRate = rateContainer + radiusRate;

		CollisionData containerCollisionData = new CollisionData(leftRate, rightRate,align);
		int otherILocation = 0;
		int insertionIndex = 0;
		if(content.size()>0){
			for(int otherIndex = 0 ; otherIndex < content.size() ; otherIndex++){
				//System.out.println("otherIndex = "+otherIndex);
				//Container other = content.get(otherIndex);
				CollisionData otherData = this.occupiedRates.get(otherIndex);
				//System.out.println("otherData = "+otherData);
				int iLocation = 0;
				if(align == Slot.ALIGN_CENTER) iLocation = 1;
				else if(align == Slot.ALIGN_DESTINATION) iLocation = 2;


				if(otherData.align==Slot.ALIGN_CENTER) otherILocation = 1;
				else if(otherData.align==Slot.ALIGN_DESTINATION) otherILocation = 2;

				int difference = iLocation - otherILocation;

				if(difference == 0){
				//	System.out.println("Difference = 0 ! ("+iLocation+" - "+otherILocation+")");
					throw new CollisionException(m,(otherData.toRate-otherData.fromRate));
				}
				else{
					//On se place a droite du conteneur precedent
					if(difference>0){
						if(containerCollisionData.fromRate<=otherData.toRate) {
							//System.out.println("Difference > 0 !");
							throw new CollisionException(m,otherData.toRate);
						}
						insertionIndex = otherIndex+1;
					}
					//On se place a gauche du conteneur precede
					else{
						if(containerCollisionData.toRate>=otherData.fromRate){
							//System.out.println("Difference < 0 !");
							throw new CollisionException(m,otherData.fromRate);
						}
						insertionIndex = otherIndex;
					}
				}
			}
			
		}
		//System.out.println("ContainerCenter = "+containerCenter);
		//If we're here, then it's alright
		return new ContainerAddingHelper(containerCollisionData, container, containerCenter, insertionIndex);
	}
	public ContainerAddingHelper getCoords(Container container, int align) throws NotEnoughSpaceException, CollisionException{
		Bay l = (Bay)slot.getLocation().getRoad();
	
		ContainerLocation location = new ContainerLocation(container.getId(), l.getPaveId(), l.getId() , slot.getId(), levelIndex, align);
		if(this.teu + container.getTEU() > maxTEU ) {
			//System.out.println("level teu = "+teu+" containerTeu = "+container.getTEU()+" > maxTEU = "+maxTEU);
			throw new NotEnoughSpaceException(location);
		}

		Coordinates containerCenter;
		Coordinates slotCoords = slot.getCoords();
		double rateContainer;
		
		double containerLength = ContainerKind.getLength(container.getDimensionType());
		double radiusRate = (containerLength/2.0)/slot.getLocation().getRoad().getLength();
		if(align == Slot.ALIGN_CENTER) {
			containerCenter = new Coordinates(slotCoords.x, slotCoords.y, z+(ContainerKind.getHeight(container.getDimensionType())/2f));
			//rateContainer = Location.getPourcent(containerCenter, slot.getLocation().getRoad());
			//rateContainer = Location.getAccuratePourcent(containerCenter, slot.getLocation().getRoad());
			rateContainer = slot.getLocation().getPourcent();
		}
		else {
			double leftRate = (slot.getLength()/2.0) / slot.getLocation().getRoad().getLength();
			//double leftRate = 0.0;
			//System.out.println("Container "+container.getId()+" LeftRate = "+leftRate);
			if(containerLength < slot.getLength()){
			if(align == Slot.ALIGN_ORIGIN)
				rateContainer = slot.getLocation().getPourcent() - leftRate + radiusRate;
				//rateContainer =  (containerLength / slot.getLength() ) / 2.0;

			else
				rateContainer = slot.getLocation().getPourcent() + leftRate - radiusRate;
				//rateContainer =  0.5 + ((containerLength / slot.getLength()) / 2.0);
			}
			else rateContainer = slot.getLocation().getPourcent();
			
			//System.out.println("Container "+container.getId()+" Rate = "+rateContainer);
			containerCenter = slot.getLocation().getCoords(rateContainer);
			containerCenter.z = z+(ContainerKind.getHeight(container.getDimensionType())/2f);
		}
		double leftRate = rateContainer - radiusRate;
		double rightRate = rateContainer + radiusRate;

		CollisionData containerCollisionData = new CollisionData(leftRate, rightRate,align);
		int otherILocation = 0;
		int insertionIndex = 0;
		if(content.size()>0){
			for(int otherIndex = 0 ; otherIndex < content.size() ; otherIndex++){
				//System.out.println("otherIndex = "+otherIndex);
				//Container other = content.get(otherIndex);
				CollisionData otherData = this.occupiedRates.get(otherIndex);
				//System.out.println("otherData = "+otherData);
				int iLocation = 0;
				if(align == Slot.ALIGN_CENTER) iLocation = 1;
				else if(align == Slot.ALIGN_DESTINATION) iLocation = 2;


				if(otherData.align==Slot.ALIGN_CENTER) otherILocation = 1;
				else if(otherData.align==Slot.ALIGN_DESTINATION) otherILocation = 2;

				int difference = iLocation - otherILocation;

				if(difference == 0){
				//	System.out.println("Difference = 0 ! ("+iLocation+" - "+otherILocation+")");
					throw new CollisionException(location,(otherData.toRate-otherData.fromRate));
				}
				else{
					//On se place a droite du conteneur precedent
					if(difference>0){
						if(containerCollisionData.fromRate<=otherData.toRate) {
							//System.out.println("Difference > 0 !");
							throw new CollisionException(location,otherData.toRate);
						}
						insertionIndex = otherIndex+1;
					}
					//On se place a gauche du conteneur precede
					else{
						if(containerCollisionData.toRate>=otherData.fromRate){
							//System.out.println("Difference < 0 !");
							throw new CollisionException(location,otherData.fromRate);
						}
						insertionIndex = otherIndex;
					}
				}
			}
			
		}
		//System.out.println("ContainerCenter = "+containerCenter);
		//If we're here, then it's alright
		return new ContainerAddingHelper(containerCollisionData, container, containerCenter, insertionIndex);
	}
	public Container getFirstContainer() throws EmptyLevelException {
		if(content.size() == 0) throw new EmptyLevelException();
		return content.get(0);
	}

	public int getLevelIndex(){
		return levelIndex;
	}

	public double getMaxTeu() {
		return maxTEU;
	}

	public double getTEU(){
		return teu;
	}

	public double getZNext(){
		double h = 0;
		for(Container c : content){
			double height = ContainerKind.getHeight(c.getDimensionType());
			if(height>h) h = height;
		}
		return z+h;
	}

	public Container removeContainer(String id) throws ContainerNotFoundException{
		//System.out.println("IN remove container : ");
		for(int i=0 ; i<content.size() ; i++){
			Container container = content.get(i);
			//System.out.println("Container : "+container.getId()+" ID = "+id);
			if(container.getId().equals(id)){
				//System.out.println("Container found : "+container.getId());
				this.teu = this.teu - container.getTEU();
				
				this.occupiedRates.remove(i);
				//System.out.println("Suppression de "+id+" (teu="+container.getTEU()+") sur "+slot.getId()+" level "+levelIndex+" : teu="+teu);
				return content.remove(i);
			}
		}
		throw new ContainerNotFoundException(id);
	}

	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("Level "+levelIndex+" content : -- ");
		for(int i=0; i<content.size(); i++){
			Container c = content.get(i);
			CollisionData cd = occupiedRates.get(i);
			sb.append(c.getId()+" "+cd+" -- ");
		}
		sb.append(" RemainingTEU : "+(maxTEU-teu));
		return sb.toString();
	}
	
	public List<String> getContainersID(){
		List<String> l = new ArrayList<String>(content.size());
		for(Container c : content){
			l.add(c.getId());
		}
		return l;
	}
	
	public String getStringContent() {
		StringBuilder sb = new StringBuilder();
		if(content.size() == 0) sb.append("empty");
		else{
		for(int i=0; i<content.size(); i++){
			Container c = content.get(i);
			CollisionData cd = occupiedRates.get(i);
			sb.append(c.getId()+" "+ContainerAlignment.getStringValue(cd.align));
			if(i+1 < content.size()) sb.append(" -- ");
		}
		}
		return sb.toString();
	}
}
