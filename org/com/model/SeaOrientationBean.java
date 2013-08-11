package org.com.model;

import org.system.container_stocking.SeaOrientation;

public class SeaOrientationBean {
	private Integer id;
	private String name;
	private String enumName;
	private SeaOrientation seaOrientation;

	public SeaOrientationBean() {

	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getEnumName() {
		return enumName;
	}

	public void setEnumName(String enumName) {
		this.enumName = enumName;
	}

	@Override
	public int hashCode() {
		return id.intValue();
	}

	@Override
	public boolean equals(Object o) {
		return o.hashCode() == hashCode();
	}

	public void setSeaOrientation(SeaOrientation seaOrientation) {
		this.seaOrientation = seaOrientation;
	}

	public SeaOrientation getSeaOrientation() {
		return this.seaOrientation;
	}

}
