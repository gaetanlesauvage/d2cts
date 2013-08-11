package org.com.model;

import org.system.container_stocking.ContainerAlignment;

public class ContainerBean {
	private String name;
	private Integer type;
	private Integer scenario;
	private Double teu;
	private String slot;
	private Integer slotLevel;
	private ContainerAlignment alignment;
	private String vehicle;

	public ContainerBean() {

	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Integer getScenario() {
		return scenario;
	}

	public void setScenario(Integer scenario) {
		this.scenario = scenario;
	}

	public Integer getType() {
		return type;
	}

	public void setType(Integer type) {
		this.type = type;
	}

	public Double getTeu() {
		return teu;
	}

	public void setTeu(Double teu) {
		this.teu = teu;
	}

	public String getSlot() {
		return slot;
	}

	public void setSlot(String slot) {
		this.slot = slot;
	}

	public Integer getSlotLevel() {
		return slotLevel;
	}

	public void setSlotLevel(Integer slotLevel) {
		this.slotLevel = slotLevel;
	}

	public ContainerAlignment getAlignment() {
		return alignment;
	}

	public void setAlignment(ContainerAlignment alignment) {
		this.alignment = alignment;
	}

	public String getVehicle() {
		return vehicle;
	}

	public void setVehicle(String vehicle) {
		this.vehicle = vehicle;
	}

	@Override
	public int hashCode() {
		return name.hashCode();
	}

	@Override
	public boolean equals(Object o) {
		return hashCode() == o.hashCode();
	}

}
