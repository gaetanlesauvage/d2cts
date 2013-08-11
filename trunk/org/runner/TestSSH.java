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
package org.runner;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;

import org.util.NetworkConfiguration;
import org.xml.sax.SAXException;


public class TestSSH {
	private static final String testClass = "test.Test";
	private static final String policy = "policyFile";
	/**
	 * @param args
	 * @throws IOException 
	 * @throws InterruptedException 
	 * @throws SAXException 
	 */
	public static void main(String[] args) throws IOException, InterruptedException, SAXException {
		//System.setProperty("java.security.policy", "policyFile");
		final String deployFile = args[0];
		NetworkConfiguration n = new NetworkConfiguration(deployFile);
		/*final XMLReader saxNetworkConfigReader = XMLReaderFactory.createXMLReader("com.sun.org.apache.xerces.internal.parsers.SAXParser");
		final XMLNetworkConfigurationParser NCParser = new XMLNetworkConfigurationParser(); 
		saxNetworkConfigReader.setContentHandler(NCParser);
		saxNetworkConfigReader.parse(deployFile);
		//RUN RMIREGISTRY SERVER FIRST !
		LocateRegistry.createRegistry(NCParser.getNetworkConfigurationPort());
		NetworkConfiguration.getRMIInstance();*/
		for(String host : n.getHostnames()){
			/*new Thread(){
				public void run(){
					try {*/
						System.out.println("RUN : "+host);
						TestSSH.run(host, n.getBaseDir(host), n.getClasspath(host), deployFile);
					/*} catch (IOException e) {
	
						e.printStackTrace();
					} catch (InterruptedException e) {

						e.printStackTrace();
					}
				}
			}.start();*/
		}
	}
	public static void run (final String host, String baseDir, String classpath, String deployFile) throws IOException, InterruptedException{
		final Process process = Runtime.getRuntime().exec(new String[]{"ssh" ,"-X" , "gaetan@"+host});
		// Consommation de la sortie standard de l'application externe dans un Thread separe
		new Thread() {
			public void run() {
				try {
					BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
					String line = "";
					try {
						while((line = reader.readLine()) != null) {
							// Traitement du flux de sortie de l'application si besoin est
							System.out.println(host+" : "+line);
			
						}
					} finally {
						reader.close();
					}
				} catch(IOException ioe) {
					ioe.printStackTrace();
				}
			}
		}.start();
		
		// Consommation de la sortie d'erreur de l'application externe dans un Thread separe
		new Thread() {
			public void run() {
				try {
					BufferedReader reader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
					String line = "";
					try {
						while((line = reader.readLine()) != null) {
							System.err.println(host+" : "+line);
							// Traitement du flux d'erreur de l'application si besoin est
						}
					} finally {
						reader.close();
					}
				} catch(IOException ioe) {
					ioe.printStackTrace();
				}
			}
		}.start();
		PrintWriter pw = new PrintWriter(process.getOutputStream());
		pw.write("cd "+baseDir+"\n");
		pw.flush();
		pw.write("java -Djava.security.manager -Djava.security.policy="+policy+" -classpath "+classpath+" "+testClass+" "+host+" "+deployFile+"\n");
		pw.flush();
		/*try{
			process.waitFor();
		}
		catch (InterruptedException e){
			System.out.println("interrupted !");
		}*/
		
	}
}
