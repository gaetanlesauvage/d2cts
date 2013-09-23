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
package org.time;



public class TimeWindow implements Comparable<TimeWindow> {
	private Time min, max;
	
	public TimeWindow (Time min, Time max){
		this.min = min;
		this.max = max;
	}
	
	public Time getMax(){
		return max;
	}
	
	public Time getMin(){
		return min;
	}
	
	public String toString(){
		return "["+min+" - "+max+"]";
	}
	
	public String toXML(){
		return "<timewindow start='"+min+"' end='"+max+"'/>";
	}
	/**
	 * Compare the <b>begining</b> of these time windows
	 */
	public int compareBeginTo(TimeWindow tw){
		return min.compareTo(tw.min);
	}
	
	/**
	 * Compare the <b>end/b> of these time windows
	 */
	public int compareEndTo(TimeWindow tw){
		return max.compareTo(tw.max);
	}
	
	public static TimeWindow union (TimeWindow tw1 ,  TimeWindow tw2){
		Time left, right;
		int compBegin = tw1.compareBeginTo(tw2);
		int compEnd = tw1.compareEndTo(tw2);
		if(compBegin < 0) left = tw1.min;
		else left = tw2.min;
		
		if(compEnd > 0) right = tw1.max;
		else right = tw2.max;
		
		return new TimeWindow(left, right);
	}
	
	public static TimeWindow intersection (TimeWindow tw1, TimeWindow tw2){
		if(tw1 == null || tw2 == null) return null;
		
		Time left, right;
		int compBegin = tw1.compareBeginTo(tw2);
		int compEnd = tw1.compareEndTo(tw2);
		if(compBegin < 0) left = tw2.min;
		else left = tw1.min;
		
		if(compEnd > 0) right = tw2.max;
		else right = tw1.max;
		
		if(left.compareTo(right) <= 0) return new TimeWindow(left, right);
		else return null;
	}
	
	public Time getLength(){
		return new Time(max, min, false);
	}
	
	/**
	 * CHECK IF THIS IS THE BEST COMPARATOR EVER !
	 */
	public int compareTo(TimeWindow tw){
		return getMin().compareTo(tw.getMin());
	}
}
