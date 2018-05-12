package org.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Scanner;
import java.util.StringTokenizer;
import java.util.TreeMap;

import org.time.Time;


public class GeneratorHelper {
	public static void makeStatic(String missionFile){
		File f = new File(missionFile);
		try {
			File f2 = File.createTempFile("mFile", "tmp");
			Scanner scan = new Scanner(f);
			PrintWriter pw = new PrintWriter(f2);
			while(scan.hasNextLine()){
				String line = scan.nextLine();
				if(!line.contains("event")){
					pw.append(line+"\n");
			
				}
			}
			scan.close();
			pw.close();

			boolean b = f.delete();
			if(!b) System.err.println("DELETE FAILED !");
			b = f.createNewFile();
			if(!b) System.err.println("CREATE FAILED !");

			scan = new Scanner(f2);
			pw = new PrintWriter(f);
			while(scan.hasNextLine()) pw.append(scan.nextLine()+"\n");
			scan.close();
			pw.close();

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}

	public static void sort(String missionFile){
		TreeMap<Time, String> tree = new TreeMap<Time, String>();
		File f = new File(missionFile);
		//StringBuilder sb = new StringBuilder();
		Scanner scan;
		try {
			scan = new Scanner(f);
			String currentMission = "";
			Time currentMissionTime = null;
			while(scan.hasNextLine()){
				String line = scan.nextLine();
				if(line.contains("</mission>")){
					currentMission+=line+"\n";
					//INSERT MISSION
					int i = currentMission.indexOf("start='")+"start='".length();
					int i2 = currentMission.indexOf("'",i);
					String time = currentMission.substring(i, i2);
					currentMissionTime = new Time(time);
					//INSERT LAST MISSION
					if(tree.containsKey(currentMissionTime)){
						currentMission = tree.get(currentMissionTime) + "\n" + currentMission;
					}
					tree.put(currentMissionTime, currentMission);

					//NEW CURRENT MISSION
					currentMission="";
					currentMissionTime = null;

				}
				else if(!line.contains("document")) currentMission+=line+"\n";
			}

			try {
				scan.close();
				f.delete();
				f.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
			PrintWriter pw = new PrintWriter(f);
			pw.append("<document>\n");
			int index = 0;
			for(Time t : tree.keySet()){
				String m = tree.get(t);
				int nb = 0;
				
				StringTokenizer st = new StringTokenizer(m, "\n");
				while(st.hasMoreTokens()){
					String line = st.nextToken();
					if(line.contains("<mission")) nb++;
				}
					
				int i = 0;
				for(int j=0; j<nb; j++){
					i = m.indexOf("[" , m.indexOf("<mission id='",i)+"<mission id='".length());
					int i2 = m.indexOf("]",i);
					m = m.substring(0, i+1) + index + m.substring(i2);
					index++;
				}
				
				pw.append(m+"\n");
			}
			pw.append("</document>");
			pw.flush();
			pw.close();

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	public static void main(String [] args){
		String file = "/home/nicoleta/workspace/d2cts/trunk/xml/testData/stocksFile.xml";
		makeStatic(file);
		sort(file);
	}
}
