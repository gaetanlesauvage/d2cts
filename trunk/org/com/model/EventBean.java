package org.com.model;

import org.time.Time;
import org.time.event.EventType;

public class EventBean {
	private Integer id;
	private EventType type;
	private Time time;
	private String description;
	
	public EventBean() {

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

	
}
