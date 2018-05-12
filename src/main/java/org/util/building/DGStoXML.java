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
import java.io.PrintWriter;
import java.util.Scanner;
import java.util.StringTokenizer;

public class DGStoXML {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws IOException {
		File dgsFile = new File(args[0]);
		File xmlFile = new File(args[1]);
		if(!xmlFile.exists()) xmlFile.createNewFile();

		Scanner scanDGS = new Scanner(dgsFile);
		PrintWriter pwXML = new PrintWriter(xmlFile);

		pwXML.append("<document>\n");
		while(scanDGS.hasNextLine()){
			String dgsLine = scanDGS.nextLine();
			StringTokenizer dgsTokenizer = new StringTokenizer(dgsLine);
			if(dgsTokenizer.hasMoreTokens()){
				String tag = dgsTokenizer.nextToken();

				if(tag.equals("an")){
					String id = dgsTokenizer.nextToken();
					String x = dgsTokenizer.nextToken();
					x = x.substring(2);
					String y = dgsTokenizer.nextToken();
					y = y.substring(2);
					pwXML.append("<crossroad id=\""+id+"\" x=\""+x+"\" y=\""+y+"\"/>\n");
				}
				else if(tag.equals("ae")){
					String id = dgsTokenizer.nextToken();
					String origin = dgsTokenizer.nextToken();
					String destination = dgsTokenizer.nextToken();
					pwXML.append("<road id=\""+id+"\" origin=\""+origin+"\" destination=\""+destination+"\"/>\n");
				}
			}
		}
		scanDGS.close();
		pwXML.append("</document>\n");
		pwXML.flush();
		pwXML.close();
	}

}
