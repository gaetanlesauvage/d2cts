package org.com.model;

public class SlotBean {
	private String name;
	private Integer terminal;
	private String bay;
	private Integer len; //Corresponds to ContainerKind.type[len], ie 0, 1 or 2;
	private Double rate;

	public SlotBean() {

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

	public String getBay() {
		return bay;
	}

	public void setBay(String bay) {
		this.bay = bay;
	}

	public Integer getLen() {
		return len;
	}

	public void setLen(Integer len) {
		this.len = len;
	}

	public Double getRate() {
		return rate;
	}

	public void setRate(Double rate) {
		this.rate = rate;
	}

	@Override
	public int hashCode() {
		return this.name.hashCode();
	}

	@Override
	public boolean equals(Object o) {
		return o.hashCode() == this.hashCode();
	}
}
