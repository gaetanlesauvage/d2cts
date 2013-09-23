package org.runner;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.display.MainFrame;

public class RunWithGUI {
	private static final Logger logger = Logger.getLogger(RunWithGUI.class);

	/*private static void testRandom(){
		for(int j=0; j<2; j++){
			Random r = new Random(1);
			logger.error("serie n°"+(j+1));
			for(int i=0; i<1000; i++){
				logger.error(r.nextDouble());
			}
		}
		System.exit(1);
	}*/
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		PropertyConfigurator.configure(RunWithGUI.class.getClassLoader().getResource(("conf/log4j.properties")));

		//testRandom();

		System.setProperty("sun.java2d.opengl", "true");
		System.setProperty("sun.java2d.noddraw", "true"); 
		System.setProperty("sun.java2d.opengl.fbobject", "false");
		System.setProperty("sun.java2d.translaccel", "true");
		System.setProperty("sun.java2d.ddforcevram", "true");
		//System.setProperty("awt.nativeDoubleBuffering", "true");
		System.setProperty("swing.aatext","true");


		logger.info("---------------------------------------------------------------");
		logger.info("---                         D²CTS                           ---");
		logger.info("---------------------------------------------------------------");

		MainFrame.getInstance();
	}

}
