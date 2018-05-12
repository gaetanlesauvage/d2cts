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

import java.util.StringTokenizer;
/**
 * Test to see if svn works on Cyrodiil !
 * @author gaetan
 *
 */
public class BIC {
	private char[] compagnyName;
	private byte[] number;
	private byte controlNumber;
	
	public BIC(char[] name, byte[] number, byte control){
		this.compagnyName = name;
		this.number = number;
		this.controlNumber = control;
	}
	
	public BIC (String stringBIC){
		StringTokenizer st = new StringTokenizer(stringBIC);
		String cName = st.nextToken();
		compagnyName = cName.toCharArray();
		String nber = st.nextToken();
		number = new byte[nber.length()];
		int i=0;
		for(char c : nber.toCharArray()){
			byte b = Byte.parseByte(c+"");
			number [i++] = b;
		}
		controlNumber = Byte.parseByte(st.nextToken());
	}
	
	public char[] getCompagnyName() {
		return compagnyName;
	}

	public byte[] getNumber() {
		return number;
	}

	public byte getControlNumber() {
		return controlNumber;
	}
	
	public String toString(){
		StringBuilder sb = new StringBuilder(10);
		sb.append(new String(compagnyName));
		/*for(char c : compagnyName){
			sb.append(c);
		}*/
		sb.append(" ");
		for(byte b : number){
			sb.append((int)b+"");
		}
		//sb.append(new String(number));
		
		sb.append(" "+controlNumber);
		return sb.toString();
	}
}
