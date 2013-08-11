package org.com.model;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.positioning.Coordinates;
import org.system.container_stocking.BlockType;
import org.system.container_stocking.SeaOrientation;

public class BlockBean {
	public static final String WALLS_SEPARATOR = "|";
	private String name;
	private Integer terminal;
	private BlockType type;
	private SeaOrientation seaOrientation;
	private String border_road;
	private Map<String,Coordinates> points;
	private Set<String> walls;

	public BlockBean() {

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

	public BlockType getType() {
		return type;
	}

	public void setType(BlockType type) {
		this.type = type;
	}

	public SeaOrientation getSeaOrientation() {
		return seaOrientation;
	}

	public void setSeaOrientation(SeaOrientation seaOrientation) {
		this.seaOrientation = seaOrientation;
	}

	public String getBorder_road() {
		return border_road;
	}

	public void setBorder_road(String border_road) {
		this.border_road = border_road;
	}
	
	public void addPoint(String name, Coordinates coords){
		if(this.points == null){
			this.points = new HashMap<>(10);
		}
		this.points.put(name, coords);
	}
	
	public Set<Entry<String,Coordinates>> getPoints(){
		if(this.points == null){
			this.points = new HashMap<>(1);
		}
		return this.points.entrySet();
	}
	
	public void addWall(String from, String to){
		if(this.walls == null){
			this.walls = new HashSet<>(9);
		}
		this.walls.add(from+WALLS_SEPARATOR+to);
	}
	
	public Iterator<String> getWalls(){
		if(this.walls == null){
			this.walls = new HashSet<>(1);
		}
		return this.walls.iterator();
	}

	@Override
	public int hashCode() {
		return name.hashCode();
	}

	@Override
	public boolean equals(Object o) {
		return o.hashCode() == hashCode();
	}
}
