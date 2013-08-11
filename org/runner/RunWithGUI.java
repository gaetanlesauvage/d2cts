package org.runner;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.display.MainFrame;

public class RunWithGUI {
	private static final Logger logger = Logger.getLogger(RunWithGUI.class);
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		System.setProperty("sun.java2d.opengl", "true");
		System.setProperty("sun.java2d.noddraw", "true"); 
		System.setProperty("sun.java2d.opengl.fbobject", "false");
		System.setProperty("sun.java2d.translaccel", "true");
		System.setProperty("sun.java2d.ddforcevram", "true");
		//System.setProperty("awt.nativeDoubleBuffering", "true");
		System.setProperty("swing.aatext","true");
		
		PropertyConfigurator.configure(RunWithGUI.class.getClassLoader().getResource(("conf/log4j.properties")));
		logger.info("---------------------------------------------------------------");
		logger.info("---                         D²CTS                           ---");
		logger.info("---------------------------------------------------------------");
		
		MainFrame.getInstance();
	}

}
