package org.com.model;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class RoadBean {
	private String name;
	private Integer terminal;
	private Integer type;
	private String origin;
	private String destination;
	private boolean directed;
	private String block;
	private List<RoadPointBean> roadPoints;
	private String group;

	public RoadBean() {

	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Integer getTerminal() {
		return terminal;
	}

	public void setTerminal(Integer terminal) {
		this.terminal = terminal;
	}

	public Integer getType() {
		return type;
	}

	public void setType(Integer type) {
		this.type = type;
	}

	public String getOrigin() {
		return origin;
	}

	public void setOrigin(String origin) {
		this.origin = origin;
	}

	public String getDestination() {
		return destination;
	}

	public void setDestination(String destination) {
		this.destination = destination;
	}

	public boolean isDirected() {
		return directed;
	}

	public void setDirected(boolean directed) {
		this.directed = directed;
	}

	public String getBlock() {
		return block;
	}

	public void setBlock(String block) {
		this.block = block;
	}

	@Override
	public int hashCode() {
		return this.name.hashCode();
	}

	@Override
	public boolean equals(Object o) {
		return o.hashCode() == this.hashCode();
	}

	public void addRoadPoint(RoadPointBean rpBean) {
		if (roadPoints == null)
			roadPoints = new ArrayList<>(5);
		roadPoints.add(rpBean.getIndexInRoad(), rpBean);
	}

	public Iterator<RoadPointBean> roadPointsIterator() {
		if (roadPoints == null)
			roadPoints = new ArrayList<>(1);
		return roadPoints.iterator();
	}

	public void setGroup(String group) {
		this.group = group;
	}
	
	public String getGroup(){
		return this.group;
	}
}
