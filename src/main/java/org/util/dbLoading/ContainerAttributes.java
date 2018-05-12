package org.util.dbLoading;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.xml.sax.Attributes;

public class ContainerAttributes implements Attributes{
	private HashMap<String,Integer> names;
	private HashMap<Integer,String> indexes;
	private List<String> values; 
	
	public ContainerAttributes(){
		names = new HashMap<>();
		indexes = new HashMap<>();
		values = new ArrayList<>(10);
	}
	@Override
	public int getLength() {
		return values.size();
	}

	@Override
	public String getURI(int index) {
		return null;
	}

	@Override
	public String getLocalName(int index) {
		return indexes.get(indexes);
	}

	@Override
	public String getQName(int index) {
		return indexes.get(index);
	}

	@Override
	public String getType(int index) {
		return "String";
	}

	@Override
	public String getValue(int index) {
		return values.get(index);
	}

	@Override
	public int getIndex(String uri, String localName) {
		return names.get(localName);
	}

	@Override
	public int getIndex(String qName) {
		Integer i = names.get(qName);
		if(i == null) return -1;
		else return i.intValue();
	}

	@Override
	public String getType(String uri, String localName) {
		return "String";
	}

	@Override
	public String getType(String qName) {
		return "String";
	}

	@Override
	public String getValue(String uri, String localName) {
		return values.get(names.get(localName));
	}

	@Override
	public String getValue(String qName) {
		return values.get(names.get(qName));
	}
	
	public void set(String name, String value){
		int index = values.size();
		values.add(value);
		names.put(name, index);
		indexes.put(index, value);
	}

}
