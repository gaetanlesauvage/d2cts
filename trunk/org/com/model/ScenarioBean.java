package org.com.model;

import java.sql.Timestamp;
import java.util.Date;


public class ScenarioBean implements Comparable<ScenarioBean>{
	private Integer id;
	private String name;
	private Date date_rec;
	private Integer terminal;
	private String file;
	
	public ScenarioBean (){
		
	}

	public int getId() {
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

	public Date getDate_rec() {
		return date_rec;
	}

	public void setDate_rec(Timestamp date_rec) {
		this.date_rec = new Date(date_rec.getTime());
	}

	public String getFile() {
		return file;
	}

	public void setFile(String file) {
		this.file = file;
	}
	
	@Override
	public int hashCode (){
		return id.hashCode();
	}
	
	@Override
	public boolean equals(Object o){
		return hashCode() == o.hashCode();
	}
	
	@Override
	public String toString(){
		return id+" | "+name+" | "+date_rec+" | "+file;
	}

	public void setTerminal(int idTerminal) {
		this.terminal = idTerminal;
	}
	
	public Integer getTerminal(){
		return terminal;
	}
	
	@Override
	public int compareTo(ScenarioBean bean){
		return id.compareTo(bean.getId());
	}
}
