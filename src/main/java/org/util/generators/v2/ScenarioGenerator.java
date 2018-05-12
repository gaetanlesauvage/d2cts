package org.util.generators.v2;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.lang.reflect.InvocationTargetException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Random;

import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JProgressBar;
import javax.swing.SwingUtilities;

import org.apache.log4j.Logger;
import org.com.DbMgr;
import org.com.dao.ContainerDAO;
import org.com.dao.ScenarioDAO;
import org.com.dao.StraddleCarrierDAO;
import org.com.model.ContainerBean;
import org.com.model.ContainerType;
import org.com.model.ScenarioBean;
import org.com.model.TerminalBean;
import org.display.GraphicDisplay;
import org.display.MainFrame;
import org.exceptions.ContainerDimensionException;
import org.positioning.Coordinates;
import org.system.Terminal;
import org.system.container_stocking.Bay;
import org.system.container_stocking.BlockType;
import org.system.container_stocking.Container;
import org.system.container_stocking.ContainerAlignment;
import org.system.container_stocking.ExchangeBay;
import org.system.container_stocking.Slot;
import org.util.BIC;
import org.util.ContainerBICGenerator;
import org.util.building.SimulationLoader;

/**
 * 
 * Generate containers locations into a given terminal and store them into
 * database. Next scenario can be improved by generating events (missions,
 * straddleCarriersFailures...) thanks to the EventGenerator.
 * 
 * @author Gaëtan Lesauvage
 * 
 */
public class ScenarioGenerator {
	private static ScenarioGenerator instance;
	private JDialog frame;
	private JProgressBar progress;
	public static final int WAITING_SLEEP_TIME = 20;
	private static final String loadingSteps =  "generating containers";
	public static final Logger log = Logger.getLogger(ScenarioGenerator.class);

	public static ScenarioGenerator getInstance() {
		if (instance == null)
			instance = new ScenarioGenerator();
		return instance;
	}

	private ScenarioGenerator() {

	}

	public ScenarioBean generate(final String scenarioName, final TerminalBean terminal, final int nb20, final int nb40, final int nb45, final int straddleCarriersCount,
			final MainFrame parent) {
		if (parent != null) {
			frame = new JDialog(parent.getFrame(), "Computing...", true);

			frame.setFont(GraphicDisplay.font);

			frame.setLayout(new BorderLayout());
			progress = new JProgressBar(0, nb20 + nb40 + nb45);
			progress.setString(loadingSteps);
			progress.setFont(GraphicDisplay.font);
			progress.setStringPainted(true);
			frame.add(progress, BorderLayout.CENTER);
			frame.setSize(new Dimension(300, 70));

			// ContainersFileGenerator.xmlTerminal = xmlTerminal;

			frame.setLocation(parent.getFrame().getLocation().x + (parent.getFrame().getSize().width / 2 - frame.getSize().width / 2), parent
					.getFrame().getLocation().y + (parent.getFrame().getSize().height / 2 - frame.getSize().height / 2));

			frame.setAlwaysOnTop(true);

			frame.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
			frame.repaint();
			frame.enableInputMethods(false);
			frame.addWindowListener(new WindowAdapter() {
				@Override
				public void windowOpened(WindowEvent e) {
					new Thread() {
						public void run() {
							execute(scenarioName, terminal, nb20, nb40, nb45, straddleCarriersCount);
						}
					}.start();

				}
			});
			frame.setVisible(true);
		} else
			return execute(scenarioName, terminal, nb20, nb40, nb45, straddleCarriersCount);
		
		return null;
	}

	private ScenarioBean execute(String scenarioName, TerminalBean terminal, final int nb20, final int nb40, final int nb45, final int straddleCarrierCount) {

		// Second Step : Create Terminal
		loadTerminal(terminal);

		if (progress != null) {
			try {
				SwingUtilities.invokeAndWait(new Runnable() {
					@Override
					public void run() {
						progress.setValue(progress.getValue() + 1);
						progress.setString(loadingSteps + " (" + progress.getValue() + "/" + (nb20 + nb40 + nb45) + ")");
					}
				});
			} catch (InvocationTargetException e) {
				e.printStackTrace();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		ScenarioBean scenario = generate(scenarioName, terminal, nb20, nb40, nb45, straddleCarrierCount);

		if (progress != null) {
			try {
				SwingUtilities.invokeAndWait(new Runnable() {
					@Override
					public void run() {
						frame.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
						frame.setVisible(false);
						frame.dispose();
					}
				});

			} catch (InvocationTargetException e) {
				e.printStackTrace();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			try {
				DbMgr.getInstance().getConnection().commit();
			} catch (SQLException e) {
				e.printStackTrace();
			}
			JOptionPane.showMessageDialog(progress, "Done!");
		}
		return scenario;
	}

	public ScenarioBean generate(String name, TerminalBean terminalBean, int nb20, int nb40, int nb45, int straddleCarrierCount) {
		// Third Step create and place the new containers
		ScenarioBean scenario = new ScenarioBean();

		scenario.setTerminal(terminalBean.getId());
		scenario.setName(name);
		// File ? => useless
		try {
			// Set the Scenario ID into the bean :)
			if (ScenarioDAO.getInstance().insert(scenario) != 1) {
				log.error("Cannot insert scenario into database!");
			}
		} catch (SQLException e1) {

			log.fatal(e1.getMessage(), e1); // In deep sheet!
		}

		int nb = nb45 + nb40 + nb20;
		ContainerBICGenerator bicGen = new ContainerBICGenerator(nb);
		log.info("BicGen = " + bicGen);

		Terminal rt = Terminal.getInstance();

		String[] paves = new String[rt.getPaveCount()];

		paves = rt.getPaveNames();
		int sum = 0;
		for (char c : "fixedSeed".toCharArray())
			sum += (int) c;
		Random r = new Random(sum);
		ArrayList<Slot> slots = new ArrayList<Slot>();
		for (String s : paves) {
			if (rt.getBlock(s).getType() == BlockType.YARD) {
				String[] t = rt.getLaneNames(s);
				for (String s2 : t) {
					Bay l = rt.getBay(s2);
					if (!(l instanceof ExchangeBay)) {
						for (Slot s3 : rt.getSlots(l.getId())) {
							slots.add(s3);
						}
					}
				}
			}
		}

		while (bicGen.size() > 0) {
			BIC bic = bicGen.giveMeBic();
			String containerId = bic.toString();
			log.info("---- Placing " + bic.toString() + " ! ----");
			Container c;
			try {
				// Which TEU ?
				ContainerType type = ContainerType.fortyFiveFeet;
				if (nb45 == 0) {
					type = ContainerType.fortyFeet;
					if (nb40 == 0) {
						type = ContainerType.twentyFeet;
						nb20--;
					} else
						nb40--;
				} else
					nb45--;

				double teuValue = type.getTEU();
				c = new Container(containerId, teuValue);
				ContainerBean containerBean = new ContainerBean();
				containerBean.setName(containerId);
				containerBean.setTeu(teuValue);
				containerBean.setScenario(scenario.getId());

				boolean ok = false;
				while (!ok) {

					Slot slot = slots.get(r.nextInt(slots.size()));
					int level = r.nextInt(slot.getMaxLevel());
					int align = r.nextInt(3) - 1;
					if (type == ContainerType.fortyFeet && slot.getTEU() == 2.0) {
						align = ContainerAlignment.center.getValue();
					} else if (slot.getTEU() == 1.0)
						align = ContainerAlignment.center.getValue();
					else if (slot.getTEU() == 2.25 && type == ContainerType.fortyFiveFeet)
						align = ContainerAlignment.center.getValue();
					if (slot.canAddContainer(c, level, align)) {

						try {
							Coordinates coords = slot.stockContainer(c, level, align);
							if (coords != null) {
								containerBean.setAlignment(ContainerAlignment.get(ContainerAlignment.getStringValue(align)));
								containerBean.setSlot(slot.getId());
								containerBean.setSlotLevel(level);
								containerBean.setType(type);
								if (ContainerDAO.getInstance(scenario.getId()).insert(containerBean) != 2) {
									log.error("Cannot insert container into database!");
								} else {
									ok = true;
									log.info("Container " + containerId + " added ! " + bicGen.size() + " remaining containers to place");
								}

							} else {
								log.warn("No exception but coordinates null for " + containerId);
							}
						} catch (Exception e) {
							e.printStackTrace();
							log.error("Exception while adding " + c.getId());
							try {
								DbMgr.getInstance().getConnection().rollback();
							} catch (SQLException rollBackE) {
								rollBackE.printStackTrace();
							}
						}
					}
				}
				if (progress != null) {
					try {
						SwingUtilities.invokeAndWait(new Runnable() {
							@Override
							public void run() {
								progress.setValue(progress.getValue() + 1);
								if (progress.getValue() % 100 == 0)
									updateTitle();
							}
						});
					} catch (InvocationTargetException e) {
						e.printStackTrace();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}

				}
			} catch (ContainerDimensionException e) {
				e.printStackTrace();
			}
			try {
				StraddleCarrierDAO.getInstance(scenario.getId()).add(straddleCarrierCount);
			} catch (SQLException e) {
				e.printStackTrace();
			}
			
		}
		return scenario;
	}

	private void loadTerminal(TerminalBean terminal) {
		SimulationLoader.getInstance().loadTerminal(terminal);
	}

	private void updateTitle() {
		String newTitleString = "Computing";
		String toAdd = "";
		String title = frame.getTitle();
		if (title.contains("..."))
			toAdd = "";
		else
			for (int i = 0; i <= title.length() - newTitleString.length(); i++)
				toAdd += ".";
		frame.setTitle(newTitleString + toAdd);
	}

}
