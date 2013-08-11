package org.com.model;

import java.sql.Timestamp;

public class TerminalBean {
	private Integer id;
	private Timestamp date_rec;
	private String name;
	private String file;
	private String label;
	
	public TerminalBean(){
		
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public Timestamp getDate_rec() {
		return date_rec;
	}

	public void setDate_rec(Timestamp date_rec) {
		this.date_rec = date_rec;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getFile() {
		return file;
	}

	public void setFile(String file) {
		this.file = file;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}
	
	@Override
	public int hashCode() {
		return id.hashCode();
	}
	
	@Override
	public boolean equals(Object o){
		return hashCode() == o.hashCode(); 
	}
	
}
