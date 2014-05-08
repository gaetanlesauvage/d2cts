package org.com.model;

import org.time.Time;
import org.time.event.EventType;

public class EventBean implements Comparable<EventBean>{
	private Integer id;
	private EventType type;
	private Time time;
	private String description;
	
	public EventBean() {

	}

	/**
	 * Copy
	 */
	public EventBean(EventBean event) {
		this.type = event.getType();
		this.time = new Time(event.time);
		this.description = new String(event.getDescription());
	}
	
	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}
	public EventType getType() {
		return type;
	}

	public void setType(EventType type) {
		this.type = type;
	}

	public Time getTime() {
		return time;
	}

	public void setTime(Time time) {
		this.time = time;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	@Override
	public int compareTo (EventBean bean){
		return time.compareTo(bean.getTime());
	}
}
