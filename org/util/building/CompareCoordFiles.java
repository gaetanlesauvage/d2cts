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
package org.util.building;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;
import java.util.StringTokenizer;

public class CompareCoordFiles {
	public static void compareFiles(String home, List<String> fichiers) {
		if (!home.endsWith("/"))
			home += "/";
		for (String f1 : fichiers) {
			for (String f2 : fichiers) {
				if (f1 != f2)
					compareFiles(home + f1, home + f2);
			}
		}
	}

	public static void compareFiles(String fich1, String fich2) {
		File f1 = new File(fich1);
		File f2 = new File(fich2);
		Scanner scan1 = null;
		Scanner scan2 = null;

		try {
			f1 = new File(fich1);
			f2 = new File(fich2);
			scan1 = new Scanner(f1);
			scan2 = new Scanner(f2);
			HashMap<Long, String> map = new HashMap<Long, String>();
			System.out.println("Comparing " + fich1 + " and " + fich2);
			while (scan1.hasNextLine()) {
				StringTokenizer st = new StringTokenizer(scan1.nextLine());
				Long l = Long.parseLong(st.nextToken());
				Float x = Float.parseFloat(st.nextToken());
				Float y = Float.parseFloat(st.nextToken());
				String rest = x + " " + y;
				map.put(l, rest);
			}

			while (scan2.hasNextLine()) {
				StringTokenizer st = new StringTokenizer(scan2.nextLine());
				Long l = Long.parseLong(st.nextToken());
				Float x = Float.parseFloat(st.nextToken());
				Float y = Float.parseFloat(st.nextToken());
				String rest = x + " " + y;
				if (map.containsKey(l)) {
					System.out.println("Cle identique detectee : \n1> " + l
							+ " : " + map.get(l) + "\2> " + l + " : " + rest);
					if (map.get(l).equals(rest))
						System.out.println("LIGNE IDENTIQUE !!!");
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (scan1 != null)
				scan1.close();
			if (scan2 != null)
				scan2.close();
		}
	}

	public static void main(String[] args) {
		File homeFolder = new File(args[0]);
		List<String> files = new ArrayList<String>();
		for (String s : homeFolder.list()) {
			if (s.startsWith("coord") && s.endsWith(".dat")) {
				files.add(s);
			}
		}
		compareFiles(args[0], files);
	}
}
