package org.runner;

public class Run {
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		String configFile = args[0];
		new SimulationRunner(configFile,null,false);
	}
}
