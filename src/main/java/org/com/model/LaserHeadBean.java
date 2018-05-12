package org.com.model;

import java.util.HashSet;
import java.util.Set;

public class LaserHeadBean {
	private static final LaserHeadBean[] defaultsHeads = {
		new LaserHeadBean("1", 443118.75, 198573.65, 22.84, 200, 200, 40),
		new LaserHeadBean("2", 443271.54, 198503.34, 23.30, 200, 200, 40),
		new LaserHeadBean("3", 443269.35, 198673.50, 22.89, 200, 200, 40),
		new LaserHeadBean("4", 443119.28, 198573.04, 22.82, 200, 200, 40),
		new LaserHeadBean("5", 443269.94, 198672.65, 22.88, 200, 200, 40),
		new LaserHeadBean("6", 443138.84, 198768.42, 23.11, 200, 200, 40),
		new LaserHeadBean("7", 443010.81, 198861.80, 23.01, 200, 200, 40),
		new LaserHeadBean("8", 442639.47, 198657.69, 22.85, 200, 200, 40),
		new LaserHeadBean("9", 442880.26, 198954.89, 23.21, 200, 200, 40),
		new LaserHeadBean("10", 442879.37, 198955.35, 23.25, 200, 200, 40),
		new LaserHeadBean("11", 442730.19, 198901.16, 23.00, 200, 200, 40),
		new LaserHeadBean("12", 442580.55, 198845.75, 23.21, 200, 200, 40),
		new LaserHeadBean("13", 442432.55, 198792.52, 23.01, 200, 200, 40),
		new LaserHeadBean("14", 442431.82, 198791.94, 22.99, 200, 200, 40),
		new LaserHeadBean("15", 442640.11, 198657.85, 22.86, 200, 200, 40),
		new LaserHeadBean("16", 442798.70, 198715.83, 23.03, 200, 200, 40),
		new LaserHeadBean("17", 442799.40, 198716.08, 23.04, 200, 200, 40),
		new LaserHeadBean("18", 442414.09, 198673.86, 23.60, 200, 200, 40)
	};
	
	private String name;
	private Integer scenario;
	private Double x;
	private Double y;
	private Double z;
	private Double rx;
	private Double ry;
	private Double rz;

	private LaserHeadBean(String name, Double x, Double y, Double z, Integer rx, Integer ry, Integer rz){
		this.name = name;
		this.x = x;
		this.y = y;
		this.z = z;
		this.rx = rx.doubleValue();
		this.ry = ry.doubleValue();
		this.rz = rz.doubleValue();
	}
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

	public static Set<LaserHeadBean> getDefaultHeads() {
		HashSet<LaserHeadBean> set = new HashSet<>(defaultsHeads.length);
		for(LaserHeadBean head : defaultsHeads){
			set.add(head);
		}
		return set;
	}

}
