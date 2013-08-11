package org.com.model;

public class LaserHeadBean {
	private String name;
	private Integer scenario;
	private Double x;
	private Double y;
	private Double z;
	private Double rx;
	private Double ry;
	private Double rz;

	public LaserHeadBean() {

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

	public Double getX() {
		return x;
	}

	public void setX(Double x) {
		this.x = x;
	}

	public Double getY() {
		return y;
	}

	public void setY(Double y) {
		this.y = y;
	}

	public Double getZ() {
		return z;
	}

	public void setZ(Double z) {
		this.z = z;
	}

	public Double getRx() {
		return rx;
	}

	public void setRx(Double rx) {
		this.rx = rx;
	}

	public Double getRy() {
		return ry;
	}

	public void setRy(Double ry) {
		this.ry = ry;
	}

	public Double getRz() {
		return rz;
	}

	public void setRz(Double rz) {
		this.rz = rz;
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
