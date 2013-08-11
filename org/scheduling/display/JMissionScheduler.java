package org.scheduling.display;

import java.awt.BorderLayout;
import java.awt.Point;
import java.awt.Toolkit;
import java.lang.reflect.InvocationTargetException;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;

import org.display.GraphicDisplay;
import org.vehicles.StraddleCarrier;



public class JMissionScheduler implements Runnable{
	//GUI
	public static final int WIDTH = 750;
	public static final int HEIGHT = 720;

	private JFrame frame;
	private JTabbedPane jtp;
	
	private IndicatorPane indicatorPane;
	
	public JMissionScheduler(){
		if(SwingUtilities.isEventDispatchThread()) this.run();
		else{
			try {
				SwingUtilities.invokeAndWait(this);
			} catch (InvocationTargetException e) {
				e.printStackTrace();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	public void run(){
		frame = new JFrame("Remote MissionScheduler Viewer");
		
		jtp = new JTabbedPane();
		jtp.setFont(GraphicDisplay.fontBold);
		
		
		indicatorPane = new IndicatorPane();
		jtp.add("Indicators", indicatorPane);

		
		frame.add(jtp,BorderLayout.CENTER);

		
		frame.setSize(WIDTH,HEIGHT);
		frame.setLocation(Math.max(0, Toolkit.getDefaultToolkit().getScreenSize().width-WIDTH),0);
		
		frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		
		frame.setVisible(true);
	}
	public void addTab(JPanel tab, String name){
		jtp.add(name,tab);
		
	}
	
	public void addResource(StraddleCarrier resource){
		indicatorPane.addResource(resource);
	}
	
		
	public void destroy(){
		
		indicatorPane.destroy();
		jtp.removeAll();
		jtp = null;
		
		frame.setVisible(false);
		frame.dispose();
		frame = null;
		
		
	}

	public IndicatorPane getIndicatorPane() {
		return indicatorPane;
	}

	public JTabbedPane getJTabbedPane() {
		return jtp;
	}

	public Point getLocation() {
		return frame.getLocation();
	}
	
	public int getWidth(){
		return frame.getWidth();
	}
	
	public int getHeight(){
		return frame.getHeight();
	}

	public JFrame getJFrame() {
		return frame;
	}
}
