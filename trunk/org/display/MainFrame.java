package org.display;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.MouseMotionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.Scanner;

import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.plaf.basic.BasicSplitPaneUI;

import org.apache.log4j.Logger;
import org.com.DbMgr;
import org.com.model.SchedulingAlgorithmBean;
import org.com.model.SimulationBean;
import org.conf.parameters.ReturnCodes;
import org.display.system.JTerminal;
import org.graphstream.ui.swingViewer.View;
import org.runner.SimulationRunner;
import org.system.Terminal;
import org.time.TimeController;
import org.util.SimulationParameter;
import org.util.building.SimulationLoader;
import org.util.dbLoading.SimulationBuilder;

//import org.pushingpixels.substance.api.SubstanceLookAndFeel;

public class MainFrame {
	public static final String MAINFRAME_ICON_URL = "/etc/images/logo.jpeg";
	private static final Logger log = Logger.getLogger(MainFrame.class);

	private static MainFrame instance;
	public static final String baseDirectory = "xml";
	private JFrame frame;
	private JMenuBar menuBar;
	private JMenu menuFile;
	private JMenu menuOptions;
	private JMenu menuControl;
	private JMenuItem stepSize, newSimulation, open, openLast, replay,
			initialStateGenerator, missionsGenerator, lhEventsGenerator;
	private JMenuItem jmiHiddenPlayPause, jmiNextStep, jmiStop;

	private JFileChooser fc;

	private Toolbar toolbar;

	private JSplitPane splitPane;
	private SimulationRunner op;
	public static final String skinNAME = "org.pushingpixels.substance.api.skin.MistSilverSkin";

	// public static final SubstanceLookAndFeel skin = new
	// SubstanceMistSilverLookAndFeel();

	private String localhostName;
	private String lastOpen = "";
	private JDialog jdClosing;
	private View view;
	// TOTO
	public static final int WIDTH = 1180;
	public static final int HEIGHT = 720;

	public static MainFrame getInstance(){
		if(instance == null)
			instance = new MainFrame();
		return instance;
	}
	
	private MainFrame() {
		initialStateGenerator = new JMenuItem("Initial state generator");
		initialStateGenerator.setMnemonic('i');
		initialStateGenerator.setAccelerator(KeyStroke.getKeyStroke(
				KeyEvent.VK_I, InputEvent.CTRL_DOWN_MASK));
		initialStateGenerator.setFont(GraphicDisplay.font);
		initialStateGenerator.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				new Thread() {
					public void run() {
						new ContainerFileGeneratorMenu(MainFrame.this);
					}
				}.start();
			}
		});

		SwingUtilities.invokeLater(new Runnable() {

			@Override
			public void run() {
				// JDialog.setDefaultLookAndFeelDecorated(true);
				// JFrame.setDefaultLookAndFeelDecorated(true);
				// SubstanceLookAndFeel.setSkin(skinNAME);

				frame = new JFrame("D²CTS");

				frame.setFont(GraphicDisplay.font);
				frame.getRootPane().setFont(GraphicDisplay.font);

				toolbar = new Toolbar(MainFrame.this);
				frame.add(toolbar, BorderLayout.PAGE_START);

				fc = new JFileChooser(baseDirectory);
				FileNameExtensionFilter filter = new FileNameExtensionFilter(
						"D2CTS configuration files", "d2cts");
				fc.setFileFilter(filter);

				menuBar = new JMenuBar();
				menuBar.setOpaque(true);

				MouseListener focusMouseListener = new MouseAdapter() {
					@Override
					public void mouseClicked(MouseEvent e) {
						setFocusOnJTerminal();
						// System.out.println("Clicked");
					}
				};
				MouseMotionListener focusMouseMotionListener = new MouseMotionAdapter() {
					@Override
					public void mouseMoved(MouseEvent e) {
						setFocusOnJTerminal();
					}
				};

				menuFile = new JMenu("File");
				menuFile.setMnemonic('f');
				menuFile.setFont(GraphicDisplay.font);
				menuFile.addMouseListener(focusMouseListener);
				menuFile.addMouseMotionListener(focusMouseMotionListener);

				newSimulation = new JMenuItem("new");
				newSimulation.setMnemonic('n');
				newSimulation.setAccelerator(KeyStroke.getKeyStroke(
						KeyEvent.VK_N, InputEvent.CTRL_DOWN_MASK));
				newSimulation.setFont(GraphicDisplay.font);

				open = new JMenuItem("open");
				open.setMnemonic('o');
				open.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O,
						InputEvent.CTRL_DOWN_MASK));
				open.setFont(GraphicDisplay.font);

				menuControl = new JMenu("Control");
				menuControl.setFont(GraphicDisplay.font);
				menuControl.setMnemonic('c');
				menuControl.addMouseListener(focusMouseListener);
				menuControl.addMouseMotionListener(focusMouseMotionListener);

				jmiHiddenPlayPause = new JMenuItem("Play / Pause");
				jmiHiddenPlayPause.setAccelerator(KeyStroke.getKeyStroke(
						KeyEvent.VK_SPACE, 0));
				jmiHiddenPlayPause.setFont(GraphicDisplay.font);
				jmiHiddenPlayPause.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						toolbar.playPause();
					}
				});

				jmiNextStep = new JMenuItem("Next Step");
				jmiNextStep.setAccelerator(KeyStroke.getKeyStroke(
						KeyEvent.VK_ENTER, 0));
				jmiNextStep.setFont(GraphicDisplay.font);
				jmiNextStep.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						if (jmiStop.isEnabled()) {
							toolbar.makeStep();

						} else
							toolbar.makeFirstStep();
						splitPane.getTopComponent().requestFocus();

					}
				});

				jmiStop = new JMenuItem("Stop");
				jmiStop.setAccelerator(KeyStroke.getKeyStroke(
						KeyEvent.VK_ESCAPE, 0));
				jmiStop.setFont(GraphicDisplay.font);
				jmiStop.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						toolbar.stop();
					}
				});

				jmiHiddenPlayPause.setEnabled(false);
				jmiNextStep.setEnabled(false);
				jmiStop.setEnabled(false);

				menuControl.add(jmiHiddenPlayPause);
				menuControl.add(jmiNextStep);
				menuControl.add(jmiStop);

				openLast = new JMenuItem("open last");
				openLast.setEnabled(false);
				try {
					File f = new File(this.getClass()
							.getResource("/display/lastOpen.dat").getPath());
					Scanner scan = new Scanner(f);
					lastOpen = scan.nextLine();
					scan.close();
					openLast.setEnabled(true);
				} catch (Exception e1) {
					File f = new File("./display/lastOpen.dat");
					try {
						lastOpen = new Scanner(f).nextLine();
						openLast.setEnabled(true);
					} catch (Exception e2) {

					}

				}

				openLast.setMnemonic('l');
				openLast.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_L,
						InputEvent.CTRL_DOWN_MASK));
				openLast.setFont(GraphicDisplay.font);

				replay = new JMenuItem("replay simulation");
				replay.setMnemonic('r');
				replay.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_R,
						InputEvent.CTRL_DOWN_MASK));
				replay.setFont(GraphicDisplay.font);

				missionsGenerator = new JMenuItem("Missions generator");
				missionsGenerator.setMnemonic('m');
				missionsGenerator.setAccelerator(KeyStroke.getKeyStroke(
						KeyEvent.VK_M, InputEvent.CTRL_DOWN_MASK));
				missionsGenerator.setFont(GraphicDisplay.font);
				missionsGenerator.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						open.setEnabled(false);
						openLast.setEnabled(false);
						replay.setEnabled(false);
						initialStateGenerator.setEnabled(false);
						missionsGenerator.setEnabled(false);
						lhEventsGenerator.setEnabled(false);
						new MissionFileGeneratorMenu(MainFrame.this);
						open.setEnabled(true);
						openLast.setEnabled(true);
						replay.setEnabled(true);
						initialStateGenerator.setEnabled(true);
						missionsGenerator.setEnabled(true);
						lhEventsGenerator.setEnabled(true);
					}
				});

				lhEventsGenerator = new JMenuItem(
						"Laser head range events generator");
				lhEventsGenerator.setMnemonic('l');
				lhEventsGenerator.setAccelerator(KeyStroke.getKeyStroke(
						KeyEvent.VK_H, InputEvent.CTRL_DOWN_MASK));
				lhEventsGenerator.setFont(GraphicDisplay.font);
				lhEventsGenerator.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						open.setEnabled(false);
						openLast.setEnabled(false);
						replay.setEnabled(false);
						initialStateGenerator.setEnabled(false);
						missionsGenerator.setEnabled(false);
						lhEventsGenerator.setEnabled(false);
						new LHFileGeneratorMenu(MainFrame.this);
						open.setEnabled(true);
						openLast.setEnabled(true);
						replay.setEnabled(true);
						initialStateGenerator.setEnabled(true);
						missionsGenerator.setEnabled(true);
						lhEventsGenerator.setEnabled(true);
					}
				});

				replay.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						open.setEnabled(false);
						openLast.setEnabled(false);
						replay.setEnabled(false);
						initialStateGenerator.setEnabled(false);
						missionsGenerator.setEnabled(false);
						lhEventsGenerator.setEnabled(false);
						new StoredSimulationChooser(MainFrame.this);
					}
				});

				openLast.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						open.setEnabled(false);
						openLast.setEnabled(false);
						replay.setEnabled(false);
						initialStateGenerator.setEnabled(false);
						missionsGenerator.setEnabled(false);
						lhEventsGenerator.setEnabled(false);
						openSimulation(lastOpen);
					}
				});

				JMenuItem exit = new JMenuItem("exit");
				exit.setMnemonic('e');
				exit.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q,
						InputEvent.CTRL_DOWN_MASK));
				exit.setFont(GraphicDisplay.font);
				exit.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						try {
							DbMgr.getInstance().commitAndClose();
						} catch (SQLException se) {
							log.fatal(se.getMessage(), se);
						}
						System.exit(ReturnCodes.EXIT_ON_SUCCESS.getCode());
					}
				});

				newSimulation.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						final NewSimulationDialog simDialog = new NewSimulationDialog(
								frame);
						// Lock call
						SimulationParameter parameters = simDialog.getSelection(); 
						Integer scenarioID = parameters.getScenarioID(); 
						
						SchedulingAlgorithmBean schedulingAlgorithm = parameters.getSchedulingAlgorithmBean();
						if (scenarioID != null && schedulingAlgorithm != null) {
							// Create a simulation according to the given
							// scenario properties
							
							SimulationBuilder builder = new SimulationBuilder(
									scenarioID, schedulingAlgorithm, parameters.getSeed());
							builder.build();
							final SimulationBean bean = builder.getSimulationBean();
							if (bean != null) {
								SwingUtilities.invokeLater(new Runnable() {
									
									@Override
									public void run() {
										Terminal.getInstance().setTextDisplay(GraphicDisplayPanel.getInstance());

									}
								});
								Thread.currentThread().setName("D2ctsLoaderThread");
								SimulationLoader.getInstance().load(bean);								
								
								// Terminal.getInstance().drawElements();
							}
						}
					}
				});

				open.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {

						int returnVal = fc.showOpenDialog(frame);
						if (returnVal == JFileChooser.APPROVE_OPTION) {
							File configFile = fc.getSelectedFile();
							File base = new File("");

							System.out.println("Parent = "
									+ base.getAbsolutePath());
							lastOpen = base.toURI()
									.relativize(configFile.toURI()).getPath();
							System.out.println("Last Open : " + lastOpen);

							// URL url =
							// this.getClass().getResource("/display/");

							try {
								File f = new File("./display/lastOpen.dat");
								if (f.exists())
									f.delete();
								f.createNewFile();
								// new File(url.getPath()+"tmp.dat");
								PrintWriter pw;

								pw = new PrintWriter(f);

								pw.append(lastOpen);
								pw.flush();
								pw.close();

							} catch (FileNotFoundException e1) {
								e1.printStackTrace();
							} catch (IOException e2) {
								// TODO : FIX ISSUE : CANNOT CREATE THE FILE
								// WHEN EXECUTE PROGRAM FROM A JAR FILE
							}
							open.setEnabled(false);
							openLast.setEnabled(false);
							replay.setEnabled(false);
							initialStateGenerator.setEnabled(false);
							missionsGenerator.setEnabled(false);
							lhEventsGenerator.setEnabled(false);
							// This is where a real application would open the
							// file.
							System.out.println("Opening: "
									+ configFile.getName() + ".");
							openSimulation(lastOpen);
						} else {
							System.out
									.println("Open command cancelled by user.");
						}
					}
				});

				menuFile.add(newSimulation);
				menuFile.add(open);
				menuFile.add(openLast);
				menuFile.addSeparator();
				menuFile.add(replay);
				menuFile.addSeparator();
				menuFile.add(initialStateGenerator);
				menuFile.add(missionsGenerator);
				menuFile.add(lhEventsGenerator);
				menuFile.addSeparator();
				menuFile.add(exit);
				menuBar.add(menuFile);

				menuOptions = new JMenu("Options");
				menuOptions.setMnemonic('o');
				menuOptions.setFont(GraphicDisplay.font);
				menuOptions.addMouseListener(focusMouseListener);
				menuOptions.addMouseMotionListener(focusMouseMotionListener);

				stepSize = new JMenuItem("Set step size");
				stepSize.setMnemonic('s');
				stepSize.setFont(GraphicDisplay.font);
				stepSize.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S,
						InputEvent.CTRL_DOWN_MASK));
				stepSize.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent arg0) {
						new StepSizeChooser(MainFrame.this);
					}
				});
				stepSize.setEnabled(false);

				menuOptions.add(stepSize);
				menuBar.add(menuOptions);

				menuBar.add(menuControl);

				frame.setJMenuBar(menuBar);

				Dimension d = new Dimension(WIDTH, HEIGHT);
				frame.setSize(d);
				frame.setMinimumSize(d);
				frame.setMaximumSize(d);
				frame.setPreferredSize(d);
				// frame.setSize(new Dimension(1400, 1050));

				// Point p = new
				// Point(frame.getToolkit().getScreenSize().width/2
				// -frame.getSize().width/2,
				// frame.getToolkit().getScreenSize().height/2 -
				// frame.getSize().height/2);
				Point p = new Point(0, 0);
				frame.setLocation(p);

				frame.setIconImage(new ImageIcon(this.getClass().getResource(
						MAINFRAME_ICON_URL), "logo").getImage());

				splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, true);

				frame.getContentPane().setBackground(
						new Color(255, 255, 255, 0));
				frame.getContentPane().add(splitPane, BorderLayout.CENTER);

				// frame.setUndecorated(true);

				frame.addWindowListener(new WindowAdapter() {
					@Override
					public void windowClosing(WindowEvent e) {
						try {
							DbMgr.getInstance().commitAndClose();
						} catch (SQLException se) {
							log.fatal(se.getMessage(), se);
						}
						System.exit(ReturnCodes.EXIT_ON_SUCCESS.getCode());
					}
				});

				SwingUtilities.updateComponentTreeUI(frame);
				frame.setVisible(true);
			}

		});

	}

	/*
	 * public static final String getSkin(){ return skin; }
	 */

	public JFrame getFrame() {
		return frame;
	}

	public void setSimReady(boolean ready) {
		if (ready) {
			frame.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
			stepSize.setEnabled(true);
			toolbar.activate();

		}
	}

	public Toolbar getToolbar() {
		return toolbar;
	}

	public void openSimulation(final String configFile) {
		frame.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		op = new SimulationRunner(configFile, MainFrame.this, true);
		jmiHiddenPlayPause.setEnabled(true);
		jmiNextStep.setEnabled(true);

	}

	/*
	 * public void openReplaySimulation(final String configFile, final String
	 * simID){ frame.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
	 * op = new
	 * SimulationRunner(configFile,MainFrame.this,localhostName,simID,true);
	 * jmiHiddenPlayPause.setEnabled(true); jmiNextStep.setEnabled(true);
	 * 
	 * }
	 */

	public void setGraphicDisplay(final JPanel panel) {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				splitPane.setBottomComponent(panel);
				splitPane.setUI(new mySplitPaneUI());
				splitPane.setOneTouchExpandable(true);
				splitPane.setResizeWeight(0.7);
			}
		});
	}

	public void setJTerminal(final View panel) {

		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				view = panel;
				splitPane.setTopComponent(panel);
				splitPane.setDividerLocation(0.7);
			}
		});
	}

	private class mySplitPaneUI extends BasicSplitPaneUI {
		public mySplitPaneUI() {
			super();
			this.dividerSize = 10;
			this.setNonContinuousLayoutDivider(new JPanel(), true);
		}
	}

	public JMenuItem getStepSizeMenuItem() {
		return stepSize;
	}

	public void closeSimulation() {
		try {
			DbMgr.getInstance().commitAndClose();
		} catch (SQLException e) {
			log.fatal(e.getMessage(), e);
		}

		Thread t = new Thread() {
			public void run() {
				stepSize.setEnabled(false);
				jdClosing = new JDialog(frame, "Closing...", false);
				jdClosing.add(
						new JLabel("Closing simulation...", JLabel.CENTER),
						BorderLayout.CENTER);
				jdClosing.setSize(300, 75);
				frame.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
				jdClosing.setCursor(Cursor
						.getPredefinedCursor(Cursor.WAIT_CURSOR));
				jdClosing.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
				jdClosing.setLocation(
						frame.getLocation().x
								+ (frame.getSize().width / 2 - jdClosing
										.getSize().width / 2),
						frame.getLocation().y
								+ (frame.getSize().height / 2 - jdClosing
										.getSize().height / 2));
				jdClosing.setAlwaysOnTop(true);
				jdClosing.setVisible(true);

			}
		};
		SwingUtilities.invokeLater(t);
		try {
			t.join();
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}
		open.setEnabled(true);
		openLast.setEnabled(true);
		replay.setEnabled(true);
		initialStateGenerator.setEnabled(true);
		missionsGenerator.setEnabled(true);
		lhEventsGenerator.setEnabled(true);

		jmiHiddenPlayPause.setEnabled(false);
		jmiNextStep.setEnabled(false);
		jmiStop.setEnabled(false);

		op.destroy();

	}

	public String getLocalHostName() {
		return localhostName;
	}

	public void closeJDClosing() {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				jdClosing.setVisible(false);
				frame.setCursor(Cursor
						.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
				jdClosing.setCursor(Cursor
						.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
				jdClosing.dispose();
			}
		});
	}

	public void removeSplitPaneContent() {
		splitPane.removeAll();
		splitPane.validate();
		splitPane.updateUI();
	}

	public void enableMenus() {
		open.setEnabled(true);
		openLast.setEnabled(true);
		replay.setEnabled(true);
		initialStateGenerator.setEnabled(true);
		missionsGenerator.setEnabled(true);
		lhEventsGenerator.setEnabled(true);
	}

	public void enableStopMenuItem() {
		jmiStop.setEnabled(true);
	}

	public void setFocusOnJTerminal() {
		if (view != null) {
			view.requestFocus();
		}
	}

	public void setEnableMenus(boolean state) {
		menuFile.setEnabled(state);
		menuOptions.setEnabled(state);
		menuControl.setEnabled(state);
	}

	public void setSimReady () {
		SwingUtilities.invokeLater(new Runnable(){
			public void run () {
				setGraphicDisplay(GraphicDisplayPanel.getInstance().getPanel());
				getToolbar().setTimeControler(new TimeController());
				setJTerminal(JTerminal.getInstance().getPanel());
				setSimReady(true);
			}
		});	
	}
}
