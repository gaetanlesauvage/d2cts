package org.display;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JToolBar;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingWorker;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.system.Terminal;
import org.time.Time;
import org.time.TimeController;
import org.time.TimeScheduler;

public class Toolbar extends JToolBar {
	public static final String TOOLBAR_PLAY_ICON_URL = "/etc/images/play.png";
	public static final String TOOLBAR_NEXTSTEP_ICON_URL = "/etc/images/nextStep.png";
	public static final String TOOLBAR_STOP_ICON_URL = "/etc/images/stop.png";
	public static final String TOOLBAR_PAUSE_ICON_URL = "/etc/images/pause.png";

	/**
	 * 
	 */
	private static final long serialVersionUID = 3462178669997221386L;
	private static final String initTime = "00:00:00";

	private static final int ITEM_HEIGHT = 45;
	// private static final Dimension separatorDimension = new Dimension(5,
	// ITEM_HEIGHT);

	// private static final Color transparent = new Color(0,0,0,0);

	private JButton playPause;
	private JButton stop;
	private JButton nextStep;

	private JLabel step;
	private JLabel time;

	private JCheckBox lh;
	private JCheckBox sync;
	private JSpinner normalized;
	private SpinnerNumberModel normalizedModel;
	private JCheckBox threaded;
	private JCheckBox viewLocked;

	private JButton resetView;
	private JButton resetTime;

	private JLabel jlNormalizedMs;

	private boolean paused;
	private boolean firstPlay;

	private TimeController controller;
	private MainFrame mainFrame;
	

	private volatile long stepValue;
	private Time timeValue;

	public Toolbar(MainFrame mainFrame) {
		super();

		Thread.currentThread().setName("D2ctsGuiThread");
		
		paused = firstPlay = true;

		this.mainFrame = mainFrame;
		this.setFont(GraphicDisplay.font);

		int h = 30;
		JPanel pTime = new JPanel(new BorderLayout());
		JPanel pStep = new JPanel(new BorderLayout());

		time = new JLabel(initTime, JLabel.CENTER);
		time.setMinimumSize(new Dimension(60, h));
		time.setMaximumSize(new Dimension(150, h));
		time.setFont(GraphicDisplay.fontBold);
		step = new JLabel("0", JLabel.CENTER);
		step.setMinimumSize(new Dimension(60, h));
		step.setMaximumSize(new Dimension(150, h));
		step.setFont(GraphicDisplay.fontBold);

		playPause = new JButton();
		playPause.setBorder(BorderFactory.createLineBorder(Color.black, 1));
		playPause.setFont(GraphicDisplay.font);
		playPause.setIcon(new ImageIcon(this.getClass().getResource(
				TOOLBAR_PLAY_ICON_URL), "play"));
		playPause.setMinimumSize(new Dimension(60, h));
		playPause.setMaximumSize(new Dimension(150, h));

		nextStep = new JButton();
		nextStep.setBorder(BorderFactory.createLineBorder(Color.black, 1));
		nextStep.setFont(GraphicDisplay.font);
		nextStep.setIcon(new ImageIcon(this.getClass().getResource(
				TOOLBAR_NEXTSTEP_ICON_URL), "step"));
		nextStep.setMinimumSize(new Dimension(60, h));
		nextStep.setMaximumSize(new Dimension(150, h));

		stop = new JButton();
		stop.setBorder(BorderFactory.createLineBorder(Color.black, 1));
		stop.setFont(GraphicDisplay.font);
		stop.setIcon(new ImageIcon(this.getClass().getResource(
				TOOLBAR_STOP_ICON_URL), "stop"));
		stop.setMinimumSize(new Dimension(60, h));
		stop.setMaximumSize(new Dimension(150, h));

		lh = new JCheckBox("show laser heads");
		lh.setFont(GraphicDisplay.font);
		lh.setSelected(false);
		lh.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				Terminal.getInstance().showLaserHeads(lh.isSelected());
				Toolbar.this.mainFrame.setFocusOnJTerminal();
			}
		});

		threaded = new JCheckBox("threaded");
		threaded.setFont(GraphicDisplay.font);
		threaded.setSelected(false);
		threaded.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				TimeScheduler.getInstance().setThreaded(threaded.isSelected());
				Toolbar.this.mainFrame.setFocusOnJTerminal();
			}
		});

		normalizedModel = new SpinnerNumberModel(0, 0, 1000, 1);
		normalized = new JSpinner(normalizedModel);
		normalized.setFont(GraphicDisplay.font);
		normalized.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				int value = normalizedModel.getNumber().intValue();
				
				TimeScheduler.getInstance().setNormalizationTime(value);
				Toolbar.this.mainFrame.setFocusOnJTerminal();
			}
		});
		sync = new JCheckBox("real time");
		sync.setFont(GraphicDisplay.font);
		sync.setSelected(false);
		sync.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				TimeScheduler.getInstance().setSynchronized(sync.isSelected());
				if (sync.isSelected()) {
					normalized.setEnabled(false);
					jlNormalizedMs.setEnabled(false);
				} else {
					normalized.setEnabled(true);
					jlNormalizedMs.setEnabled(true);
				}
				Toolbar.this.mainFrame.setFocusOnJTerminal();
			}
		});

		resetTime = new JButton("fastest");
		resetTime.setFont(GraphicDisplay.font);
		resetTime.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {

				normalized.setEnabled(true);
				jlNormalizedMs.setEnabled(true);
				Number n = new Double(0.0);
				try {
					normalizedModel.setValue(n.doubleValue());
				} catch (ClassCastException ex) {
					normalizedModel.setValue(n.intValue());
				}
				sync.setSelected(false);

				TimeScheduler.getInstance().setSynchronized(false);
				TimeScheduler.getInstance().setNormalizationTime(0);
				Toolbar.this.mainFrame.setFocusOnJTerminal();
			}
		});

		viewLocked = new JCheckBox("lock");
		viewLocked.setFont(GraphicDisplay.font);
		viewLocked.setSelected(false);
		viewLocked.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				Terminal.getInstance().lockView(viewLocked.isSelected());
				Toolbar.this.mainFrame.setFocusOnJTerminal();
			}
		});

		resetView = new JButton("reset");
		resetView.setFont(GraphicDisplay.font);
		resetView.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Terminal.getInstance().resetView();
				Toolbar.this.mainFrame.setFocusOnJTerminal();
			}
		});

		// this.setRollover(true);
		// this.setOpaque(true);

		JPanel p = buildPanel(300, "Time");
		p.setLayout(new GridLayout(1, 2, 5, 5));

		JLabel jlTime = new JLabel("Time :", JLabel.CENTER);
		jlTime.setFont(GraphicDisplay.font);

		pTime.add(jlTime, BorderLayout.WEST);
		pTime.add(time, BorderLayout.CENTER);
		pTime.setMaximumSize(new Dimension(150, h));
		// this.addSeparator();

		JLabel jlStep = new JLabel("Step :", JLabel.CENTER);
		jlStep.setFont(GraphicDisplay.font);
		pStep.add(jlStep, BorderLayout.WEST);
		pStep.add(step, BorderLayout.CENTER);
		pStep.setMaximumSize(new Dimension(150, h));
		p.add(pTime);
		p.add(pStep);

		this.add(p);
		this.add(new JLabel("    "));
		// this.addSeparator(separatorDimension);

		p = buildPanel(300, "Controller");
		p.setLayout(new GridLayout(1, 3, 5, 5));
		p.add(playPause);
		p.add(nextStep);
		p.add(stop);
		this.add(p);
		this.add(new JLabel("    "));
		// this.addSeparator(separatorDimension);

		p = buildPanel(260, "Time settings");
		p.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
		p.add(sync);
		normalized.setToolTipText("minimum time per step in milliseconds");

		p.add(normalized);
		jlNormalizedMs = new JLabel("ms");
		jlNormalizedMs.setFont(GraphicDisplay.font);
		p.add(jlNormalizedMs);
		p.add(resetTime);
		this.add(p);
		this.add(new JLabel("    "));
		// this.addSeparator(separatorDimension);

		p = buildPanel(250, "View");
		FlowLayout fl = new FlowLayout(FlowLayout.CENTER, 5, 5);
		p.setLayout(fl);
		p.add(lh);
		p.add(viewLocked);
		p.add(resetView);
		this.add(p);
		this.add(new JLabel("    "));
		// this.addSeparator(separatorDimension);

		p = buildPanel(100, "Optimization");
		p.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
		p.add(threaded);
		this.add(p);

		playPause.setEnabled(false);
		nextStep.setEnabled(false);
		stop.setEnabled(false);
		time.setEnabled(false);
		step.setEnabled(false);
		lh.setEnabled(false);
		sync.setEnabled(false);
		normalized.setEnabled(false);
		jlNormalizedMs.setEnabled(false);
		threaded.setEnabled(false);
		viewLocked.setEnabled(false);
		resetView.setEnabled(false);
		resetTime.setEnabled(false);

		playPause.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				playPause();
			}
		});

		stop.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				stop();
			}
		});

		nextStep.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				// Runnable SwingWorker<Void, Void> worker = null;

				if (firstPlay) {
					new Thread("firstThread") {
						public void run() {
							makeFirstStep();
						}
					}.start();
				} else {
					new Thread("stepThread") {
						public void run() {
							nextStep();
						}
					}.start();
				}

			}
		});
	}

	private JPanel buildPanel(int width, String borderTitle) {
		JPanel p = new JPanel();
		p.setMaximumSize(new Dimension(width, ITEM_HEIGHT));
		Border b = BorderFactory.createLineBorder(Color.white, 1, true);
		TitledBorder tb = BorderFactory.createTitledBorder(b, borderTitle);
		tb.setTitlePosition(TitledBorder.CENTER);
		tb.setTitleJustification(TitledBorder.TOP);

		tb.setTitleFont(GraphicDisplay.fontTinyBold);
		tb.setTitleColor(new Color(255, 255, 255, 125));
		p.setOpaque(true);
		p.setBorder(tb);
		return p;
	}

	public void setTimeControler(TimeController tc) {
		controller = tc;
	}

	public void makeFirstStep() {
		if (firstPlay) {
			firstPlay = false;
			stop.setEnabled(true);
			Toolbar.this.mainFrame.enableStopMenuItem();
			nextStep();
		} else
			new Exception("This is not the first step !!!").printStackTrace();
	}

	public void makeStep() {
		new Thread("makeStepThread") {
			public void run() {
				if (nextStep.isEnabled()) {
					nextStep();
				}
			}
		}.start();
	}

	public void stop() {
		if (stop.isEnabled()) {
			paused = true;

			Toolbar.this.mainFrame.getStepSizeMenuItem().setEnabled(true);
			Toolbar.this.mainFrame.removeSplitPaneContent();
			setStepLabel(0);
			setTimeLabel(new Time(0));
			firstPlay = true;
			playPause.setIcon(new ImageIcon(this.getClass().getResource(
					TOOLBAR_PLAY_ICON_URL), "play"));
			playPause.setEnabled(false);
			nextStep.setEnabled(false);
			stop.setEnabled(false);
			time.setEnabled(false);
			step.setEnabled(false);

			lh.setSelected(false);
			sync.setSelected(false);
			threaded.setSelected(false);

			try {
				normalizedModel.setValue(0);
			} catch (ClassCastException e) {
				normalizedModel.setValue(0.0);
			}

			viewLocked.setSelected(false);

			lh.setEnabled(false);
			sync.setEnabled(false);
			threaded.setEnabled(false);
			normalized.setEnabled(false);
			jlNormalizedMs.setEnabled(false);
			viewLocked.setEnabled(false);
			resetView.setEnabled(false);
			resetTime.setEnabled(false);

			Toolbar.this.mainFrame.closeSimulation();
		}
	}

	/* SHOULD NOT BE CALLED FROM EDT !!! */
	private synchronized void nextStep() {
		// UPDATE COMPONENTS
		// long startTime = System.currentTimeMillis();
		boolean disabled = false;
		if (nextStep.isEnabled()) {
			nextStep.setEnabled(false);
			playPause.setEnabled(false);
			disabled = true;
		}
		final boolean fdisabled = disabled;
		controller.nextStep(!fdisabled);

		stepValue = controller.getStep();
		timeValue = controller.getTime();

		setStepLabel(stepValue);
		setTimeLabel(timeValue);

		if (disabled) {
			nextStep.setEnabled(true);
			playPause.setEnabled(true);
			mainFrame.setFocusOnJTerminal();
		}

		// Stats
		// long endTime = System.currentTimeMillis();
		// long gap = endTime - startTime;
		// sumOfTime += gap;
		// nbIts++;

	}

	public void playPause() {
		if (playPause.isEnabled()) {
			if (paused) {
				playPause.setIcon(new ImageIcon(this.getClass().getResource(
						TOOLBAR_PAUSE_ICON_URL), "pause"));
				nextStep.setEnabled(false);
				paused = false;
				if (firstPlay) {
					Toolbar.this.mainFrame.getStepSizeMenuItem().setEnabled(
							false);

					normalizedModel = new SpinnerNumberModel(normalizedModel
							.getNumber().intValue(), 0, TimeScheduler
							.getInstance().getSecondsPerStep() * 1000, 1);
					normalized.setModel(normalizedModel);

					firstPlay = false;
					stop.setEnabled(true);
					mainFrame.enableStopMenuItem();
					mainFrame.setFocusOnJTerminal();
				}

				mainFrame.setFocusOnJTerminal();
				// TODO PUT IN ANOTHER THREAD ?
				SwingWorker<Void, Void> mainWorker = new SwingWorker<Void, Void>() {
					@Override
					protected Void doInBackground() throws Exception {
						Thread.currentThread().setName("PlayWorker");
						while (!paused) {
							nextStep();
						}
						return null;
					}
				};
				
				mainWorker.execute();

			} else {
				playPause.setIcon(new ImageIcon(this.getClass().getResource(
						TOOLBAR_PLAY_ICON_URL), "play"));
				nextStep.setEnabled(true);
				paused = true;

			}
		}
	}

	private void setStepLabel(long step) {
		this.step.setText(step + "");
	}

	private void setTimeLabel(Time t) {
		time.setText(t.getHours() + "h" + t.getMinutes() + "m" + t.getSeconds()
				+ "s");

	}

	public boolean isLHSelected() {
		return lh.isSelected();
	}

	public boolean isSynchronizedSelected() {
		return sync.isSelected();
	}

	public int getNormalizationTime() {
		/*
		 * int value = 0; try{ value = (Integer)normalized.getValue();
		 * }catch(ClassCastException e){ value =
		 * ((Double)normalized.getValue()).intValue(); }
		 */
		return normalizedModel.getNumber().intValue();
	}

	public boolean isThreadedSelected() {
		return threaded.isSelected();
	}

	public void activate() {
		playPause.setEnabled(true);
		nextStep.setEnabled(true);
		stop.setEnabled(false);
		time.setEnabled(true);
		step.setEnabled(true);
		lh.setEnabled(true);
		sync.setEnabled(true);
		threaded.setEnabled(true);
		normalized.setEnabled(true);
		jlNormalizedMs.setEnabled(true);
		viewLocked.setEnabled(true);
		resetView.setEnabled(true);
		resetTime.setEnabled(true);
	}
}
