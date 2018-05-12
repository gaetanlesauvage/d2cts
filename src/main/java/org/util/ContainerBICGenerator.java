/*
 * This file is part of D²CTS : Dynamic and Distributed Container Terminal Simulator.
 *
 * Copyright (C) 2009-2012  Gaëtan Lesauvage
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package org.util;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.TreeMap;
import java.util.Random;

public class ContainerBICGenerator {
	private TreeMap<String, BIC> table;
	private ArrayList<BIC> list;
	private HashMap<String,BIC> memory;
	
	private static final Random r = new Random();
	
	public ContainerBICGenerator(int nb, String file) throws IOException{
		
		memory = new HashMap<String, BIC>();
		table = new TreeMap<String, BIC>();
		list = new ArrayList<BIC>(nb);
		
		char[] compagnyName = new char[4];
		byte[] number = new byte[6];
		
		
		while(table.size()<nb){
			for(int i=0; i<3; i++) compagnyName[i] = (char)(65+r.nextInt(91 - 65));
			compagnyName[3] = 'U';
			for(int i=0; i<number.length; i++){
				number[i] = (byte) r.nextInt(10);
			}
			byte controlNumber = (byte)r.nextInt(10);
			BIC bic = new BIC(compagnyName, number, controlNumber);
			BIC oldValue = table.put(new String(bic.toString()), bic);
			memory.put(new String(bic.toString()),bic);
			
			if(oldValue == null) list.add(bic);
			else{
				list.set(list.indexOf(oldValue), bic);
			}
		}
		
		File f = new File(file);
		if(!f.exists()) f.createNewFile();
		
		PrintWriter pw = new PrintWriter(f);
		for(String s : table.keySet()) pw.append(s+"\n");
		pw.flush();
		pw.close();
	}
	
	public ContainerBICGenerator(int nb){
		memory = new HashMap<String, BIC>();
		table = new TreeMap<String, BIC>();
		list = new ArrayList<BIC>(nb);
		
		
		while(table.size()<nb){
			char[] compagnyName = new char[4];
			byte[] number = new byte[6];
			
			for(int i=0; i<3; i++) compagnyName[i] = (char)(65+r.nextInt(91 - 65));
			compagnyName[3] = 'U';
			for(int i=0; i<number.length; i++){
				number[i] = (byte) r.nextInt(10);
			}
			byte controlNumber = (byte)r.nextInt(10);
			BIC bic = new BIC(compagnyName, number, controlNumber);
			BIC oldValue = table.put(new String(bic.toString()), bic);
			memory.put(new String(bic.toString()),bic);
			
			if(oldValue == null) list.add(bic);
			else{
				list.set(list.indexOf(oldValue), bic);
			}
		}
	}
	
	public ContainerBICGenerator(int nb, List<String> alreadyCreated){
		memory = new HashMap<String, BIC>();
		table = new TreeMap<String, BIC>();
		list = new ArrayList<BIC>(nb);
		int targetSize = nb + alreadyCreated.size();
		for(String s : alreadyCreated){
			BIC b = new BIC(s);
			table.put(s, b);
			//list.add(b);
		}
		while(table.size()<targetSize){
			char[] compagnyName = new char[4];
			byte[] number = new byte[6];
			
			for(int i=0; i<3; i++) compagnyName[i] = (char)(65+r.nextInt(91 - 65));
			compagnyName[3] = 'U';
			for(int i=0; i<number.length; i++){
				number[i] = (byte) r.nextInt(10);
			}
			byte controlNumber = (byte)r.nextInt(10);
			BIC bic = new BIC(compagnyName, number, controlNumber);
			BIC oldValue = table.put(new String(bic.toString()), bic);
			memory.put(new String(bic.toString()),bic);
			if(oldValue == null) list.add(bic);
			else{
				list.set(list.indexOf(oldValue), bic);
			}
		}
	}
	
	public BIC peek(){
		return table.get(table.firstKey());
	}
	public BIC giveMeBic(){
		BIC b = list.remove(r.nextInt(list.size()));
		table.remove(b.toString());
		return b;
	}
		
	public int size(){
		return table.size();
	}

	public void putBIC(BIC bic) {
		table.put(bic.toString(), bic);
		memory.put(bic.toString(), bic);
		list.add(bic);
	}
	
	public String toString(){
		StringBuilder sb = new StringBuilder();
		for(String key : table.keySet()){
			sb.append(key+" = "+table.get(key)+"\n");
		}
		return sb.toString();
		
	}
	
	public void generateMore(int nbMore){
		int nb=0;
		while(nb<nbMore){
			char[] compagnyName = new char[4];
			byte[] number = new byte[6];
			
			for(int i=0; i<3; i++) compagnyName[i] = (char)(65+r.nextInt(91 - 65));
			compagnyName[3] = 'U';
			for(int i=0; i<number.length; i++){
				number[i] = (byte) r.nextInt(10);
			}
			byte controlNumber = (byte)r.nextInt(10);
			BIC bic = new BIC(compagnyName, number, controlNumber);
			if(!memory.containsKey(bic.toString())){
				putBIC(bic);
				nb++;
			}
		}
	}
}
