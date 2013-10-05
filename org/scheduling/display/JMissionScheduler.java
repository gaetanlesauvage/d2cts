package org.scheduling.display;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Point;
import java.awt.Toolkit;
import java.lang.reflect.InvocationTargetException;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;

import org.display.GraphicDisplay;
import org.display.MainFrame;
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

		JPanel glassPane = new JPanel();
		glassPane.setBackground(new Color(50,50,50,128));
		glassPane.setSize(frame.getSize());
		frame.getRootPane().setGlassPane(glassPane);
		setWaitMode(MainFrame.getInstance().getFrame().getRootPane().getGlassPane().isVisible());

		frame.setLocation(Math.max(0, Toolkit.getDefaultToolkit().getScreenSize().width-WIDTH),0);

		frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

		frame.setVisible(true);

		MainFrame.getInstance().setJMissionScheduler(this);
	}

	public void addTab(JPanel tab, String name){
		jtp.add(name,tab);
	}

	public void addResource(StraddleCarrier resource){
		indicatorPane.addResource(resource);
	}


	public void destroy(){
		frame.setVisible(false);
		frame.dispose();
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

	//Ensure to be in SwingThread...
	public void setWaitMode(final boolean on){
		if(SwingUtilities.isEventDispatchThread()) threadSafeWaitMode(on);
		else{
			try {
				SwingUtilities.invokeAndWait(new Runnable(){
					public void run(){
						threadSafeWaitMode(on);
					}
				});
			} catch (InvocationTargetException e) {
				e.printStackTrace();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	private void threadSafeWaitMode(final boolean on){ 
		frame.getRootPane().getGlassPane().setVisible(on);
		if(on){
			frame.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		} else {
			frame.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
		}
		SwingUtilities.updateComponentTreeUI(frame);
	}
}
