package org.com.model;

import org.system.container_stocking.BlockType;

public class BlockTypeBean {
	private Integer id;
	private String name;
	private String enumName;
	private BlockType type;
	
	public BlockTypeBean(){
		
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
	public int hashCode(){
		return id.intValue();
	}
	
	@Override
	public boolean equals(Object o){
		return o.hashCode() == hashCode();
	}

	public void setBlockType(BlockType type) {
		this.type = type;
	}
	
	public BlockType getBlockType(){
		return this.type;
	}
	
	
}
