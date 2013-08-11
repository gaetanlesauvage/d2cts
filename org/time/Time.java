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

import java.text.NumberFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.StringTokenizer;

public class Time implements Comparable<Time> {
	public static final String MAXTIME = "23:59:59.9999";

	public static long timeToStep(Time time, float stepSize) {
		long step = (long) (((time.h * 3600) + (time.m * 60) + time.s) / stepSize);
		return step;
	}

	int h, m;

	double s;

	public Time(int h, int m) {
		this(h, m, 0);
	}

	public Time(Date sqlTime){
		Calendar c = Calendar.getInstance();
		c.setTimeInMillis(sqlTime.getTime());
		this.h = c.get(Calendar.HOUR_OF_DAY);
		this.m = c.get(Calendar.MINUTE);
		this.s = c.get(Calendar.SECOND);
		//Fixme add millisec support...

	}
	
	public Time(double secondes){
		h = (int) (secondes / 3600);
		secondes = secondes % 3600;
		m = (int) (secondes / 60);
		s = (secondes % 60);
	}
	
	public Time(int h, int m, double s) {
		this.h = h;
		this.m = m;
		this.s = s;

	}

	public Time(Time... t) {
		double inS = 0;
		for (int i = 0; i < t.length; i++) {
			Time time = t[i];
			inS += time.getInSec();
		}
		h = (int) (inS / 3600);
		inS = inS % 3600;
		m = (int) (inS / 60);
		s = (inS % 60);

	}

	public Time(long step, double stepSize) {
		double time_in_s = (step * stepSize);
		// System.out.println(step+" to s = "+time_in_s);
		h = (int) (time_in_s / 3600);
		// System.out.println("to h  = "+h);
		time_in_s = time_in_s % 3600;
		m = (int) (time_in_s / 60);
		// System.out.println("to m  = "+m);
		s = (time_in_s % 60);
		// System.out.println("to s  = "+s);

	}

	public Time(long step) {
			double time_in_s = (step * TimeScheduler.getInstance().getSecondsPerStep());
		// System.out.println(step+" to s = "+time_in_s);
		h = (int) (time_in_s / 3600);
		// System.out.println("to h  = "+h);
		time_in_s = time_in_s % 3600;
		m = (int) (time_in_s / 60);
		// System.out.println("to m  = "+m);
		s = (time_in_s % 60);
		// System.out.println("to s  = "+s);
	}

	public Time(String time) {
		
		if (time.contains(":")) {
			StringTokenizer st = new StringTokenizer(time, ":");
			h = Integer.parseInt(st.nextToken());
			m = Integer.parseInt(st.nextToken());
			s = Double.parseDouble(st.nextToken());
		} else {
			if (time.endsWith("s")) {
				double time_in_s = Double.parseDouble(time.substring(0,
						time.length() - 1));
				// System.out.println(time+" to s = "+time_in_s);
				h = (int) (time_in_s / 3600);
				// System.out.println("to h  = "+h);
				time_in_s = time_in_s % 3600;
				m = (int) (time_in_s / 60);
				// System.out.println("to m  = "+m);
				s = (time_in_s % 60);
				// System.out.println("to s  = "+s);
			}
		}
	}

	public Time(Time time, Time additionalTime) {
		double timeInS = (time.h * 3600) + (time.m * 60) + time.s;
		double additionalTimeInS = (additionalTime.h * 3600)
				+ (additionalTime.m * 60) + additionalTime.s;
		double time_in_s = timeInS + additionalTimeInS;
		h = (int) (time_in_s / 3600);
		time_in_s = time_in_s % 3600;
		m = (int) (time_in_s / 60);
		s = (time_in_s % 60);
		// System.out.println(time+" + "+additionalTime+" = "+this);
	}

	public Time(Time time, Time additionalTime, boolean toAdd) {
		if (additionalTime == null)
			System.out.println("additionalTime = null");
		else if (time == null)
			System.out.println("time = null");
		double timeInS = time.getInSec();
		double additionalTimeInS = additionalTime.getInSec();
		double time_in_s;
		time_in_s = (toAdd == true ? timeInS + additionalTimeInS : timeInS
				- additionalTimeInS);
		if (time_in_s < 0.0)
			time_in_s = 0.0;

		h = (int) (time_in_s / 3600.0);
		time_in_s = time_in_s % 3600.0;
		m = (int) (time_in_s / 60.0);
		s = (time_in_s % 60.0);
	}

	public int compareTo(Time t) {
		double inSec = h * 3600 + m * 60 + s;
		double t_inSec = t.h * 3600 + t.m * 60 + t.s;

		if (inSec == t_inSec)
			return 0;
		else
			return (int) Math.round(inSec - t_inSec);
	}

	public int getHours() {
		return h;
	}

	public int getMinutes() {
		return m;
	}

	public double getSeconds() {
		return s;
	}

	public void setHours(int h) {
		this.h = h;
	}

	public void setMinutes(int m) {
		this.m = m;
	}

	public void setSeconds(double s) {
		this.s = s;
	}

	public void setTime(int h, int m, double s) {
		this.h = h;
		this.m = m;
		this.s = s;
	}

	public double getInSec() {
		return (h * 3600) + (m * 60) + s;
	}

	public long toStep() {
		double stepSize = TimeScheduler.getInstance().getSecondsPerStep();
		return (long) ((((h * 3600) + (m * 60) + s) / stepSize));
	}

	public String toString() {

		/*
		 * try { if(toStep()==0) return "00:00:00.00"; } catch (RemoteException
		 * e) { e.printStackTrace(); }
		 */
		NumberFormat nf = NumberFormat.getNumberInstance(new Locale("en"));
		nf.setMaximumFractionDigits(2);
		nf.setMinimumFractionDigits(2);
		String ss = nf.format(s);
		return "" + (h < 10 ? "0" + h : h) + ":" + (m < 10 ? "0" + m : m) + ":"
				+ (s < 10 ? "0" + ss : ss);

	}

	public String toMString() {

		int left = (int) s;
		int right = (int) ((s - left) * 100);

		String time = "" + (h < 10 ? "0" + h : h) + ":"
				+ (m < 10 ? "0" + m : m) + ":"
				+ (left < 10 ? "0" + left + "." + right : left + "." + right);
		System.out.println("toMString = " + time);
		return time;
	}

	public void destroy() {

	}

	public java.sql.Time getSQLTime() {
		//FIXME
		Calendar c = Calendar.getInstance();
		c.set(0, Calendar.JANUARY, 1, h, m, (int)s);
		//return new java.sql.Time((int) ((getInSec()) * 1000));
		return new java.sql.Time(c.getTimeInMillis());
	}
}
