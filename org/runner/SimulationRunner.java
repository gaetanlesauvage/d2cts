package org.runner;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Dimension;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JProgressBar;
import javax.swing.SwingUtilities;

import org.com.XMLWriterImpl;
import org.display.GraphicDisplay;
import org.display.GraphicDisplayPanel;
import org.display.MainFrame;
import org.display.system.GraphicTimeController;
import org.display.system.JTerminal;
import org.exceptions.SingletonException;
import org.positioning.LaserSystem;
import org.scheduling.MissionScheduler;
import org.scheduling.bb.BB;
import org.scheduling.branchAndBound.BranchAndBound;
import org.scheduling.offlineACO.OfflineACOScheduler;
import org.scheduling.offlineACO2.OfflineACOScheduler2;
import org.scheduling.onlineACO.OnlineACOScheduler;
import org.system.Terminal;
import org.time.TimeController;
import org.time.TimeScheduler;
import org.util.parsers.XMLACOMissionSchedulerHelper;
import org.util.parsers.XMLBBMissionSchedulerHelper;
import org.util.parsers.XMLMissionSchedulerHelper;
import org.util.parsers.XMLTSPMissionSchedulerHelper;
import org.util.parsers.XMLTerminalComponentParser;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

public class SimulationRunner implements Runnable {
	public static String hostTerminal;
	public static String[] xmlTerminal;

	public static String hostRemoteDisplay;
	public static String hostJTerminal;
	public static String hostLaserSystem;
	public static String[] xmlLaserSystem;

	public static String hostMissionGraph;
	public static String hostMissionScheduler;

	public static String hostTimeController;
	public static String hostXMLTerminalComponentParser;
	public static String hostTimeScheduler;
	public static String[] clientFiles;

	public static String uriDisplay = "";

	public String localHostName;
	public static final int WAITING_SLEEP_TIME = 20;

	private XMLReader saxReader, saxNetworkConfigReader;
	private XMLNetworkConfigurationParser NCParser;
	private MainFrame mainFrame;

	private JDialog frame;
	private JProgressBar progress;
	private GraphicDisplayPanel jd;

	private boolean x11 = true;
	private ArrayList<JTerminal> jts;

	private Terminal terminal;
	private TimeScheduler timeScheduler;

	private XMLTerminalComponentParser terminalParser;

	private boolean isReplay;

	private boolean done = false;

	private static final String[] loadingSteps = {
			"Reading Network Configuration... ", "Building JDisplay...",
			"Building Terminal 2D viewer", "Creating Terminal...",
			"Building Time Scheduler...", "Building Laser System...",
			"Parsing Terminal from xml file...",
			"Creating Straddle Carriers...", "Creating Mission Scheduler...",
			"Building Time Controller" };

	public SimulationRunner(String configFile, MainFrame mainFrame, boolean x11) {

		this.x11 = x11;

		if (x11)
			this.mainFrame = mainFrame;

		try {
			saxNetworkConfigReader = XMLReaderFactory
					.createXMLReader("com.sun.org.apache.xerces.internal.parsers.SAXParser");
		} catch (SAXException e1) {
			e1.printStackTrace();
		}
		System.out.println("configFile = " + configFile);
		NCParser = new XMLNetworkConfigurationParser();
		saxNetworkConfigReader.setContentHandler(NCParser);
		if (configFile.startsWith("../")) {
			try {
				saxNetworkConfigReader.parse(configFile);
			} catch (IOException e) {
				e.printStackTrace();
			} catch (SAXException e) {
				e.printStackTrace();
			}
		} else {
			try {
				InputSource is = new InputSource(this.getClass()
						.getResourceAsStream("/" + configFile));
				saxNetworkConfigReader.parse(is);
			} catch (IOException e) {
				e.printStackTrace();
			} catch (SAXException e) {
				e.printStackTrace();
			}
		}
		if (NCParser.getNetworkConfigurationHostname().equals(localHostName)) {
			try {
				new NetworkConfiguration(configFile);
			} catch (SAXException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		jts = new ArrayList<JTerminal>();
		System.out.println("Simulation Openner with " + configFile);
		new Thread(this).start();
	}

	@Override
	public void run() {
		if (x11) {
			final Thread t = new Thread() {
				public void run() {

					frame = new JDialog(mainFrame.getFrame(), "Loading...",
							true);
					frame.setFont(GraphicDisplay.font);

					frame.setLayout(new BorderLayout());
					progress = new JProgressBar(0, 9);
					progress.setFont(GraphicDisplay.font);
					progress.setStringPainted(true);
					frame.add(progress, BorderLayout.CENTER);
					frame.setSize(new Dimension(300, 70));

					JFrame parent = mainFrame.getFrame();
					frame.setLocation(
							parent.getLocation().x
									+ (parent.getSize().width / 2 - frame
											.getSize().width / 2),
							parent.getLocation().y
									+ (parent.getSize().height / 2 - frame
											.getSize().height / 2));

					frame.setAlwaysOnTop(true);

					frame.setCursor(Cursor
							.getPredefinedCursor(Cursor.WAIT_CURSOR));

					frame.enableInputMethods(false);

					progress.setString(loadingSteps[progress.getValue()]);
					progress.setValue(0);
				}
			};

			try {
				SwingUtilities.invokeAndWait(t);
			} catch (InvocationTargetException e) {
				e.printStackTrace();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

			SwingUtilities.invokeLater(new Runnable() {

				@Override
				public void run() {
					frame.setVisible(true);
				}
			});

		}
		try {

			saxReader = XMLReaderFactory
					.createXMLReader("com.sun.org.apache.xerces.internal.parsers.SAXParser");
			if (x11)
				incrementProgressBar();
			runJDisplay();
			if (x11)
				incrementProgressBar();
			runTerminal();
			System.out.println("Second call to Terminal");
			terminal = Terminal.getInstance();
			System.out.println("OK ! On " + localHostName
					+ " waiting for Terminal on " + hostTerminal + " DONE !");

			// FIXME TERMINAL SEED

			if (x11)
				incrementProgressBar();

			try {
				runJTerminal();
			} catch (SingletonException e) {
				e.printStackTrace();
			}
			if (x11)
				incrementProgressBar();

			XMLWriterImpl.getInstance();
			System.out.println("XML Writer created !");

			buildScheduler();
			System.out.println("OK ! On " + localHostName
					+ " waiting for Scheduler on " + hostTimeScheduler
					+ " DONE !");
			timeScheduler = TimeScheduler.getInstance();

			if (x11)
				incrementProgressBar();

			runLaserSystem();
			System.out.println("LASER SYSTEM CREATED!");
			System.out.println("OK ! On " + localHostName
					+ " waiting for Scheduler on " + hostTimeScheduler
					+ " DONE !");
			if (x11)
				incrementProgressBar();

			buildTerminal();
			System.out.println("TERMINAL BUILT!");
			if (x11)
				incrementProgressBar();

			if (isReplay) {
				// FIXME
				/*
				 * NetworkConfiguration.databaseManager.setSimID(this.replaySimID
				 * ); NetworkConfiguration.databaseManager.setReplay(true);
				 * timeScheduler
				 * .setSecondsPerStep(NetworkConfiguration.databaseManager
				 * .getStepSize(replaySimID));
				 * Terminal.setDatabaseManager(NetworkConfiguration
				 * .databaseManager);
				 */
			}
			buildStraddleCarriers();
			System.out.println("STRADDLE CARRIERS CREATED!");
			/*
			 * if(isReplay){
			 * NetworkConfiguration.databaseManager.setSimID(this.replaySimID);
			 * NetworkConfiguration.databaseManager.setReplay(true);
			 * timeScheduler
			 * .setSecondsPerStep(NetworkConfiguration.databaseManager
			 * .getStepSize(replaySimID));
			 * 
			 * for(StraddleCarrier rsc :
			 * RemoteTerminal.straddleCarriers.values()){ try {
			 * rsc.setAutoDriven(true); } catch (DatabaseNotConfiguredException
			 * e) { e.printStackTrace();
			 * System.exit(DatabaseNotConfiguredException.EXIT_CODE); } } }
			 */
			if (x11)
				incrementProgressBar();
			buildMissionScheduler(
					NCParser.getMissionSchedulerBindingName(),
					new XMLMissionSchedulerHelper(evalParameters));
			System.out.println("MISSION SCHEDULER CREATED!");

			if (x11)
				incrementProgressBar();

		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		if (x11) {

			terminal.showLaserHeads(mainFrame.getToolbar().isLHSelected());
			timeScheduler.setSynchronized(mainFrame.getToolbar()
					.isSynchronizedSelected());
			timeScheduler.setThreaded(mainFrame.getToolbar()
					.isThreadedSelected());
			timeScheduler.setNormalizationTime(mainFrame.getToolbar()
					.getNormalizationTime());

		}
		if (jd != null) {
			if (x11) {
				mainFrame.setGraphicDisplay(jd.getPanel());
				mainFrame.getToolbar().setTimeControler(new TimeController());
			}
		}
		if (x11) {
			mainFrame.setJTerminal(JTerminal.getInstance().getPanel());

			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					// TODO Auto-generated method stub
					frame.setCursor(Cursor
							.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
					frame.dispose();
				}
			});
		}

		jts.clear();
		jts = null;

		if (terminalParser != null) {
			terminalParser.destroy();
			terminalParser = null;
		}

		NCParser.destroy();
		NCParser = null;
		saxNetworkConfigReader = null;
		saxReader = null;

		if (x11) {
			try {
				SwingUtilities.invokeAndWait(new Runnable() {
					@Override
					public void run() {
						mainFrame.setSimReady(true);
					}
				});
			} catch (InvocationTargetException e) {
				e.printStackTrace();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

		}
		if (isReplay && x11)
			mainFrame.getStepSizeMenuItem().setEnabled(false);
		System.out.println("------------------------ END OF TEST ON "
				+ localHostName + " -------------------------");
		done = true;
	}

	public boolean hasDone() {
		return done;
	}

	public MissionScheduler buildMissionScheduler(String type,
			XMLMissionSchedulerHelper helper) {
		MissionScheduler missionScheduler = null;

		if (type.equals(OnlineACOScheduler.rmiBindingName)) {
			OnlineACOScheduler
					.setGlobalParameters(((XMLACOMissionSchedulerHelper) helper)
							.getParameters());
		} else if (type.equals(BranchAndBound.rmiBindingName)) {
			XMLBBMissionSchedulerHelper bbHelper = (XMLBBMissionSchedulerHelper) helper;
			BranchAndBound.timeMatrixFile = bbHelper.getTimeMatrixFile();
			BranchAndBound.distanceMatrixFile = bbHelper
					.getDistanceMatrixFile();
			BranchAndBound.evalCosts = bbHelper.evalCosts();
			BranchAndBound.solutionInitFile = bbHelper.getSolutionInitFile();
			BranchAndBound.solutionFile = bbHelper.getSolutionFile();
		} else if (type.equals(BB.rmiBindingName)) {
			XMLBBMissionSchedulerHelper bbHelper = (XMLBBMissionSchedulerHelper) helper;
			BB.solutionInitFile = bbHelper.getSolutionInitFile();
			BB.solutionFile = bbHelper.getSolutionFile();
		} else if (type.equals(OfflineACOScheduler.rmiBindingName)) {
			OfflineACOScheduler
					.setGlobalParameters(((XMLTSPMissionSchedulerHelper) helper)
							.getParameters());
		} else if (type.equals(OfflineACOScheduler2.rmiBindingName)) {
			OfflineACOScheduler2
					.setGlobalParameters(((XMLTSPMissionSchedulerHelper) helper)
							.getParameters());
		}
		// else System.err.println("Wrong Mission Scheduler specified !!!");
		MissionScheduler.setEvalParameters(helper.getEvalParameters());
		MissionScheduler.rmiBindingName = type;
		missionScheduler = MissionScheduler.getInstance();

		return missionScheduler;
	}

	public void runJDisplay() {
		jd = new GraphicDisplayPanel(this.mainFrame);
		jd.println("Display created on : " + uriDisplay);
		terminal.setTextDisplay(jd);
	}

	public void runJTerminal() throws SingletonException {
		jts.add(JTerminal.getInstance());
	}

	public void buildTerminal() throws SAXException, IOException {
		long now = System.currentTimeMillis();
		terminalParser = new XMLTerminalComponentParser();
		saxReader.setContentHandler(terminalParser);
		for (final String s : xmlTerminal) {
			InputSource is = new InputSource(this.getClass()
					.getResourceAsStream("/" + s));
			saxReader.parse(is);
		}
		
		long andNow = System.currentTimeMillis();
		System.out.println("Terminal built in " + (andNow - now) + "ms");
	}

	public void runLaserSystem() throws IOException, SAXException {
		LaserSystem.getInstance();

		terminalParser = new XMLTerminalComponentParser();
		saxReader.setContentHandler(terminalParser);

		for (String s : xmlLaserSystem) {
			System.out.println("Parsing xmlLaserSystem file " + s);
			saxReader.parse(new InputSource(this.getClass()
					.getResourceAsStream("/" + s)));
			// saxReader.parse(this.getClass().getResource("/"+s).getFile());
		}

		/*
		 * RemoteLaserSystem ls = LaserSystem.getRMIInstance();
		 * if(!uriDisplay.equals("")) ls.setRemoteDisplay(uriDisplay);
		 * saxReader.setContentHandler(new
		 * XMLTerminalComponentParser(localHostName));
		 * 
		 * for(String s : xmlLaserSystem) saxReader.parse(s);
		 */
	}

	public void runTerminal() {
		// Creation du terminal
		terminal = Terminal.getInstance();
	}

	public void buildScheduler() {
		timeScheduler = TimeScheduler.getInstance();
	}

	public void buildStraddleCarriers() throws IOException, SAXException {
		terminalParser = new XMLTerminalComponentParser();
		saxReader.setContentHandler(terminalParser);
		for (String s : clientFiles) {
			saxReader.parse(new InputSource(this.getClass()
					.getResourceAsStream("/" + s)));
		}
	}

	public void buildTimeController() {
		// TimeControler
		new GraphicTimeController();
	}

	public void destroy() {
		done = false;
		new Thread() {
			public void run() {
				long t1 = System.currentTimeMillis();

				jd.destroy();
				jd = null;

				timeScheduler = null;
				terminal = null;
				// RandomSpeed.close();

				/*
				 * GraphicTerminalListenerImpl.setRemoteTerminal(null);
				 * JTerminal.setRemoteTerminal(null);
				 * Load.setRemoteTerminal(null);
				 * Mission.setRemoteTerminal(null);
				 * LaserSystem.setRemoteTerminal(null);
				 * RoutingAlgorithm.setRemoteTerminal(null);
				 * RRoutingAlgorithm.setRemoteTerminal(null);
				 * MissionScheduler.setRemoteTerminal(null);
				 * 
				 * StraddleCarrierPane.setRemoteTerminal(null);
				 * Reservations.setRemoteTerminal(null);
				 * XMLStraddleCarrierMessageParser.setRemoteTerminal(null);
				 * XMLTerminalComponentParser.setRemoteTerminal(null);
				 * StraddleCarrier.setRemoteTerminal(null);
				 * DynamicEvent.setRemoteTerminal(null);
				 * RandomSpeed.setRandomGenerator(null);
				 */
				/*
				 * Terminal.scheduler = null; Time.setRemoteTimeScheduler(null);
				 * Load.setRemoteTimeScheduler(null);
				 * Bay.setRemoteTimeScheduler(null);
				 * LaserSystem.setRemoteTimeScheduler(null);
				 * RoutingAlgorithm.setRemoteTimeScheduler(null);
				 * RRoutingAlgorithm.setRemoteTimeScheduler(null);
				 * MissionScheduler.setRemoteTimeScheduler(null);
				 * XMLTerminalComponentParser.setRemoteTimeScheduler(null);
				 * StraddleCarrier.setRemoteTimeScheduler(null);
				 * TimeController.setRemoteTimeScheduler(null);
				 * DynamicEvent.setRemoteTimeScheduler(null);
				 */
				/* rnc.closeSimulation(); */

				if (x11) {
					mainFrame.closeJDClosing();
					mainFrame = null;
				}

				System.gc();
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				System.gc();
				long t2 = System.currentTimeMillis();
				System.out.println("Simulation closed in " + (t2 - t1) + "ms");
				done = true;

			}
		}.start();

	}

	public void incrementProgressBar() {
		if (SwingUtilities.isEventDispatchThread()) {
			progress.getModel().setValue(progress.getModel().getValue() + 1);
			progress.setString(loadingSteps[progress.getValue()]);
		} else {
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {

					progress.getModel().setValue(
							progress.getModel().getValue() + 1);
					// progress.setValue(progress.getValue()+1);
					try {
						progress.setString(loadingSteps[progress.getValue()]);
					} catch (Exception e) {
						System.err.println("exception");
					}
				}
			});
		}

	}
}
