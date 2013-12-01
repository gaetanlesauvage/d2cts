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
package org.scheduling.onlineACO;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.lang.reflect.InvocationTargetException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;

import org.apache.log4j.Logger;
import org.com.model.scheduling.OnlineACOParametersBean;
import org.display.GraphicDisplay;
import org.exceptions.EmptyResourcesException;
import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.DefaultGraph;
import org.graphstream.ui.graphicGraph.GraphicElement;
import org.graphstream.ui.graphicGraph.GraphicNode;
import org.graphstream.ui.graphicGraph.stylesheet.StyleConstants.Units;
import org.graphstream.ui.spriteManager.Sprite;
import org.graphstream.ui.spriteManager.SpriteManager;
import org.graphstream.ui.swingViewer.GraphRenderer;
import org.graphstream.ui.swingViewer.View;
import org.graphstream.ui.swingViewer.Viewer;
import org.graphstream.ui.swingViewer.Viewer.ThreadingModel;
import org.graphstream.ui.swingViewer.util.Camera;
import org.graphstream.ui.swingViewer.util.GraphMetrics;
import org.missions.Load;
import org.missions.Mission;
import org.routing.path.Path;
import org.scheduling.MissionScheduler;
import org.scheduling.UpdateInfo;
import org.scheduling.aco.graph.AntEdge;
import org.scheduling.aco.graph.AntMissionNode;
import org.scheduling.aco.graph.AntNode;
import org.scheduling.aco.graph.DepotNode;
import org.scheduling.aco.graph.EndNode;
import org.scheduling.display.JMissionScheduler;
import org.system.Terminal;
import org.time.Time;
import org.time.TimeScheduler;
import org.util.Location;
import org.vehicles.StraddleCarrier;

/**
 * Mission Scheduler using an ACO based algorithm
 * 
 * @author Ga&euml;tan Lesauvage
 * @since 2011
 */
public final class OnlineACOScheduler extends MissionScheduler {
	public static final Logger log = Logger.getLogger(OnlineACOScheduler.class);

	// RMI
	public static final String rmiBindingName = "OnlineACOScheduler";

	public static OnlineACOScheduler getInstance() {
		return (OnlineACOScheduler) MissionScheduler.instance;
	}

	// Mission Graph
	private Graph graph;
	private static DepotNode depot;
	private static EndNode end;

	// Mission Graph GUI
	private SpriteManager spriteManager;
	private Viewer viewer;
	private View view;
	private GraphRenderer renderer;
	private ACOLayout layout;
	private GraphicNode selectedNode;
	private JToolBar jtb;

	// ACO Graph data
	private SortedMap<String, AntMissionNode> missionsNodes;
	private SortedMap<String, AntHill> hillsNodes;

	// ACO
	private ACOParameters globalParameters;

	// TIME SYNCHRONIZATION
	// private static JSpinner jsSyncSize;
	private int syncSize;
	// private static boolean interrupt = false;
	// private static Thread algoThread;

	// OPTIONS
	private JCheckBox jcbDisplayWeights;
	private JProgressBar jpb;
	private JDialog progressBarFrame;

	private Map<String, Location> lastLocations;
	private Map<String, String> currentMissions;

	/**
	 * Max number of ants in a colony
	 */
	private int nbAntMax; // Used to iterate one ant of each colony at a
	// time
	private List<AntMissionNode> toDestroy;

	/* ======================== CONSTRUCTORS ======================== */
	/**
	 * Constructor with default host
	 */
	public OnlineACOScheduler() {
		super();
		// FIXME check why this test is necessary
		if (globalParameters == null) {
			globalParameters = OnlineACOParametersBean.getACOParameters();
			evalParameters = OnlineACOParametersBean.getEvalParameters();
		}

		log.info("ACO PARAMETERS: " + globalParameters);
		log.info("EVAL PARAMETERS: "+ evalParameters);

		lastLocations = new HashMap<>();
		currentMissions = new HashMap<>();
		missionsNodes = new TreeMap<>();
		hillsNodes = new TreeMap<String, AntHill>();

		MissionScheduler.instance = this;
		if (!init)
			init();
	}

	public static void closeInstance() {
		depot = null;
		end = null;
	}

	/**
	 * Get the start node of the graph.
	 * 
	 * @return The start node of the graph.
	 */
	public static DepotNode getDepotNode() {
		return depot;
	}

	/**
	 * Get the end node of the graph.
	 * 
	 * @return The end node of the graph.
	 */
	public static EndNode getEndNode() {
		return end;
	}

	/**
	 * Update the max count of ants in a colony according to the given new ant
	 * count of the colony.
	 * 
	 * @param newAntCount
	 *            New ant count in the colony where an ant has been added.
	 */
	public void antAdded(int newAntCount) {
		if (nbAntMax < newAntCount)
			nbAntMax = newAntCount;
	}

	/**
	 * Update the max count of ants in a colony when an ant has been removed in
	 * a colony.
	 */
	public void antRemoved() {
		// FIND NEW MAX ANT COUNT
		nbAntMax = 0;
		for (AntHill hill : hillsNodes.values()) {
			if (hill.getAntCount() > nbAntMax)
				nbAntMax = hill.getAntCount();
		}
		// System.err.println("2) NB_ANT_MAX = "+nbAntMax);
	}

	/*
	 * ============================== INITIALIZATION
	 * ==============================
	 */
	/**
	 * Initialize both the algorithm and the GUI. @
	 */
	@Override
	protected void init() {
		init = true;
		computeTime = 0;
		step = 0;
		sstep = TimeScheduler.getInstance().getStep() + 1;

		lock.lock();
		graphChanged = true;
		lock.unlock();

		graph = new DefaultGraph("mission graph");
		layout = new ACOLayout();
		// layout.disable();

		depot = new DepotNode();
		SOURCE_NODE = depot;

		end = new EndNode();
		AntEdge depotEndEdge = new AntEdge(depot, end);
		depot.addOutgoingEdge(depotEndEdge);
		end.addIncomingEdge(depotEndEdge);

		spriteManager = new SpriteManager(graph);

		graph.addAttribute("ui.stylesheet", "url('org/scheduling/onlineACO/missionGraph.css')");
		graph.addAttribute("ui.antialias");
		graph.addAttribute("ui.quality");
		// System.setProperty(
		// "gs.ui.renderer","org.graphstream.ui.j2dviewer.J2DGraphRenderer" );
		// //DEPRECATED
		System.setProperty("org.graphstream.ui.renderer", "org.graphstream.ui.j2dviewer.J2DGraphRenderer");

		viewer = new Viewer(graph, ThreadingModel.GRAPH_IN_ANOTHER_THREAD);

		renderer = Viewer.newGraphRenderer();
		toDestroy = new ArrayList<AntMissionNode>();

		if (SwingUtilities.isEventDispatchThread()) {
			System.err.println("WRONG THREAD !");
		}

		try {
			SwingUtilities.invokeAndWait(new Runnable() {
				public void run() {
					syncSize = globalParameters.getSync();
					// jsSyncSize = new JSpinner();
					// jsSyncSize.setFont(GraphicDisplay.font);
					// int value = (int)syncSize;
					// SpinnerNumberModel numberModel = new
					// SpinnerNumberModel(value, 0, Integer.MAX_VALUE, 1);
					// jsSyncSize.setModel(numberModel);
					// ChangeListener cl = new ChangeListener() {
					// @Override
					// public void stateChanged(ChangeEvent e) {
					// String newStepString = jsSyncSize.getModel().getValue()
					// +"";
					// final int newStepSize = Integer.parseInt(newStepString);
					// setSyncSize(newStepSize);
					// }
					// };

					// jsSyncSize.addChangeListener(cl);
					// JLabel jlSyncSize = new JLabel("Synchronization size: ");
					// jlSyncSize.setFont(GraphicDisplay.fontBold);
					jtb = new JToolBar("MissionSchedulerToolBar", JToolBar.HORIZONTAL);
					// jtb.add(jlSyncSize);
					// jtb.add(jsSyncSize);
					// jtb.addSeparator();

					jpb = new JProgressBar(0, missionsNodes.size() + 1);

					jpb.setStringPainted(true);

					jcbDisplayWeights = new JCheckBox("Display weights", true);
					jcbDisplayWeights.setFont(GraphicDisplay.font);
					jcbDisplayWeights.addActionListener(new ActionListener() {
						@Override
						public void actionPerformed(ActionEvent e) {
							for (AntEdge edge : depot.getDestinations()) {
								edge.displayWeight(jcbDisplayWeights.isSelected());
							}
							for (AntMissionNode n : missionsNodes.values()) {
								for (AntEdge edge : n.getDestinations()) {
									edge.displayWeight(jcbDisplayWeights.isSelected());
								}
							}
						}
					});

					jtb.add(jcbDisplayWeights);

					view = viewer.addView("view", renderer, false);
					for (MouseMotionListener ml : view.getMouseMotionListeners()) {
						view.removeMouseMotionListener(ml);
					}
					view.addMouseListener(new MouseAdapter() {
						public void mousePressed(MouseEvent e) {

							if (e.getButton() == MouseEvent.BUTTON1) {
								ArrayList<GraphicElement> elts = renderer.allNodesOrSpritesIn(e.getX(), e.getY(), e.getX(), e.getY());
								if (elts != null && elts.size() > 0) {
									for (int i = 0; i < elts.size(); i++) {
										GraphicElement ge = elts.get(i);
										if (ge instanceof GraphicNode) {
											selectedNode = (GraphicNode) ge;
											break;
										}
									}
								}
							}
						}

						public void mouseReleased(MouseEvent e) {
							if (e.getButton() == MouseEvent.BUTTON1) {
								selectedNode = null;
							}
						}

						public void mouseClicked(MouseEvent e) {
							if (e.getButton() == MouseEvent.BUTTON1) {
								ArrayList<GraphicElement> elts = renderer.allNodesOrSpritesIn(e.getX(), e.getY(), e.getX(), e.getY());
								if (elts != null && elts.size() > 0) {
									for (int i = 0; i < elts.size(); i++) {
										String id = elts.get(i).getId();
										if (spriteManager.hasSprite(id)) {
											Sprite s = spriteManager.getSprite(id);
											String values = "";
											DecimalFormat df = new DecimalFormat("#.########");
											Object[] t = s.getAttribute("ui.pie-values");
											if (t != null) {
												int j = 0;
												for (Object o : t) {
													values += df.format((Double) o);
													if (j < t.length - 1)
														values += " , ";
													j++;
												}

												AntMissionNode amn = missionsNodes.get(id);
												String pop = "POP=(";
												for (AntHill hill : hillsNodes.values()) {
													pop += hill.getID() + ": " + amn.getAntCount(hill.getID()) + " , ";
												}
												pop.trim();
												if (hillsNodes.size() > 0)
													pop.substring(0, pop.length() - 3);
												pop += ")";
												System.out.println(pop + " PH = [" + amn.getPheromoneValues() + "]");

												System.out.println("sprite " + id + " @ " + s + " values : " + values);
											}
										}
									}
								}
							} else if (e.getButton() == MouseEvent.BUTTON3) {
								layout.apply();
							}
						}
					});
					view.addMouseMotionListener(new MouseMotionListener() {
						@Override
						public void mouseMoved(MouseEvent e) {

						}

						@Override
						public void mouseDragged(MouseEvent e) {
							if (selectedNode != null) {
								view.moveElementAtPx(selectedNode, e.getX(), e.getY());
							}
						}
					});
					view.setLayout(new BorderLayout());

				}
			});
		} catch (InvocationTargetException e2) {
			e2.printStackTrace();
		} catch (InterruptedException e2) {
			e2.printStackTrace();
		}

		jms = new JMissionScheduler();
		JPanel p = new JPanel(new BorderLayout());
		p.add(view, BorderLayout.CENTER);
		p.add(jtb, BorderLayout.PAGE_START);
		jms.addTab(p, "Mission Graph");
		jms.getJTabbedPane().setSelectedIndex(1);
		for (StraddleCarrier rsc : resources) {
			jms.addResource(rsc);
		}
		if (out != null) {
			out.println("MissionGraph created !");

		} else
			System.out.println("MissionGraph created !");

		int current = 1;
		List<StraddleCarrier> lResources = Terminal.getInstance().getStraddleCarriers();
		int overall = lResources.size();
		for (StraddleCarrier rsc : lResources) {
			System.out.println("Adding resource " + rsc.getId() + " (" + current + "/" + overall + ")");
			current++;
			insertResource(rsc);
		}
		current = 1;
		List<Mission> lTasks = Terminal.getInstance().getMissions();
		overall = lTasks.size();
		for (Mission m : lTasks) {
			System.out.println("Adding task " + m.getId() + " (" + current + "/" + overall + ")");
			current++;
			insertMission(m);
		}

		try {
			SwingUtilities.invokeAndWait(new Runnable() {
				@Override
				public void run() {
					progressBarFrame = new JDialog((JFrame) null, "Please wait...", false);
					progressBarFrame.setResizable(false);
					progressBarFrame.setAlwaysOnTop(true);
					progressBarFrame.add(jpb, BorderLayout.CENTER);
					progressBarFrame.setSize(250, 70);
					progressBarFrame.setLocation((int) (jms.getLocation().getX() + ((jms.getWidth() - progressBarFrame.getWidth()) / 2.0)),
							(int) (jms.getLocation().getY() + ((jms.getHeight() - progressBarFrame.getHeight()) / 2.0)));
				}
			});

		} catch (InvocationTargetException e1) {
			e1.printStackTrace();
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}

	}

	/*
	 * ============================== DISCRETE PART OF THE SCHEDULER
	 * ==============================
	 */
	@Override
	/**
	 * Precompute is called by the TimeScheduler at each step of simulation.
	 */
	public void precompute() {
		if (graphChanged || graphChangedByUpdate > 0) {
			long before = System.nanoTime();
			processEvents();

			// GraphChanged may have been set to false in processEvents() sub
			// methods
			if (!pool.isEmpty() && !resources.isEmpty() && (graphChanged || graphChangedByUpdate > 0)) {
				lock.lock();
				precomputed = true;
				lock.unlock();

				compute();

				lock.lock();
				graphChanged = false;
				graphChangedByUpdate = 0;
				lock.unlock();
			}
			long after = System.nanoTime();
			computeTime += (after - before);
		}
	}

	@Override
	/**
	 * Apply is called by the TimeScheduler at each step of the simulation after calling precompute() method of each DiscretObject of the scheduler. 
	 */
	public boolean apply() {
		boolean returnCode = NOTHING_CHANGED;
		if (precomputed) {
			returnCode = SOMETHING_CHANGED;
			long before = System.nanoTime();

			for (AntMissionNode missionNode : missionsNodes.values()) {
				missionNode.commit();
			}

			if (missionsNodes.size() > 0) {
				for (AntHill h : hillsNodes.values())
					h.updateBestPath();
			}
			Terminal.getInstance().flushAllocations();

			lock.lock();
			precomputed = false;
			lock.unlock();

			long after = System.nanoTime();
			computeTime += (after - before);
		}
		sstep++;
		return returnCode;
	}

	/*
	 * ============================== CONTINOUS PART OF THE SCHEDULER
	 * ==============================
	 */
	@Override
	/**
	 * Compute the ACO algorithm $syncSize times
	 */
	public void compute() {
		// setInterrupted(false);
		if (jms == null) {
			execute();
		} else {
			if (syncSize > 0) {
				for (int i = 0; i < syncSize/* &&!isInterrupted() */; i++) {
					execute();
				}
			}
			// else{
			// if(algoThread == null || !algoThread.isAlive()){
			// algoThread = new Thread(){
			// public void run(){
			// while(!isInterrupted()){
			// execute();
			// Thread.yield();
			// }
			// }
			// };
			// algoThread.start();
			// }
			// }
		}
	}

	/**
	 * Execute 1 step of the ACO algorithm
	 */
	private void execute() {
		for (int i = 0; i < nbAntMax; i++) {
			for (AntHill hill : hillsNodes.values()) {
				Ant a = hill.getAnt(i);
				// if(a!=null) {
				a.compute();
				// }

			}
		}
		for (AntMissionNode n : missionsNodes.values()) {
			n.compute();
		}
		step++;
	}

	/*
	 * ========================== TASKS AND RESOURCES OPERATIONS
	 * ==========================
	 */
	/**
	 * Insert a vehicle as a resource in the algorithm.
	 * 
	 * @param rsc
	 *            The straddle carrier to add.
	 */
	@Override
	protected void insertResource(StraddleCarrier rsc) {
		String modelID = rsc.getModel().getId();
		String rscID = rsc.getId();

		resources.add(rsc);

		vehicles.put(rscID, rsc);
		if (jms != null)
			jms.addResource(rsc);

		// Compute costs for rsc
		boolean computeCosts = true;
		for (String model : resourceModels.keySet()) {
			if (model.equals(modelID)) {
				computeCosts = false;
				break;
			}
		}

		List<String> lModel = null;
		if (computeCosts) {
			lModel = new ArrayList<String>();
		} else {
			lModel = resourceModels.get(modelID);

		}

		lModel.add(rscID);
		resourceModels.put(modelID, lModel);

		// /
		AntHill ahn = new AntHill(rsc);
		hillsNodes.put(ahn.getID(), ahn);
		scheduleResources.put(rscID, ahn);
		lastLocations.put(ahn.getID(), rsc.getLocation());
		for (AntMissionNode m : missionsNodes.values()) {
			if (rsc.getModel().isCompatible(m.getMission().getContainer().getDimensionType())) {
				ahn.addAntMissionNode(m);
			}
		}
	}

	/**
	 * Remove a vehicle from available resources of the algorithm.
	 * 
	 * @param rsc
	 *            The straddle carrier to remove.
	 */
	@Override
	protected boolean removeResource(String resourceID) {
		boolean b = super.removeResource(resourceID);
		if (b) {
			graph.removeNode(resourceID);
			hillsNodes.remove(resourceID);
		}
		return b;
	}

	/**
	 * Ask for an update of the location of the given vehicle
	 * 
	 * @param resourceID
	 *            Vehicle ID
	 * @param distance
	 *            Distance made since the last update
	 */
	@Override
	protected void updateResourceLocation(String resourceID, UpdateInfo updateInfo) {
		super.updateResourceLocation(resourceID, updateInfo);
		AntHill n = hillsNodes.get(resourceID);
		StraddleCarrier rsc = n.getStraddleCarrier();
		Location newLocation = rsc.getLocation();
		if (missionsNodes.size() > 0) {

			Load load = rsc.getCurrentLoad();
			if (load == null) {
				// UPDATE WEIGHT IF CHANGED OF ROAD
				if (!lastLocations.get(resourceID).getRoad().getId().equals(newLocation.getRoad().getId())) {
					// System.out.println("Update weights 1");
					for (AntMissionNode matchingMission : n.getMissions()) {
						if (matchingMission != null && matchingMission.in != null) {
							for (AntEdge e : matchingMission.in.values())
								e.addCost(n.getStraddleCarrier());
						}
					}
				} else {
					lock.lock();
					graphChangedByUpdate--;
					lock.unlock();
				}
			} else {
				String m = "";
				if (currentMissions.containsKey(resourceID))
					m = currentMissions.get(resourceID);
				if (!load.getMission().getId().equals(m)) {
					// System.out.println("Update weights 2");
					currentMissions.put(resourceID, load.getMission().getId());
					// UPDATE WEIGHTS
					for (AntMissionNode matchingMission : n.getMissions()) {
						if (matchingMission != null && matchingMission.in != null) {
							for (AntEdge e : matchingMission.in.values()) {
								e.addCost(rsc);
							}
						}
					}
				} else {
					lock.lock();
					graphChangedByUpdate--;
					lock.unlock();
				}

			}
		} else {
			lock.lock();
			graphChangedByUpdate--;
			lock.unlock();
		}
		lastLocations.put(resourceID, newLocation);

	}

	/**
	 * Insert mission
	 * 
	 * @param m
	 *            Mission to insert
	 */
	@Override
	protected void insertMission(Mission m) {
		// System.err.println("INSERT MISSION "+m.getId());
		if (pool.size() == 0) {
			spriteManager.removeSprite("lastSprite");
		}
		pool.add(m);

		AntMissionNode amn = new AntMissionNode(m);
		missionsNodes.put(amn.getID(), amn);
		for (AntHill hill : hillsNodes.values())
			hill.addAntMissionNode(amn);

		for (String modelID : resourceModels.keySet()) {
			try {
				Path p = getTravelPath(m, m, vehicles.get(resourceModels.get(modelID).get(0)));
				amn.addMissionCostFor(modelID, p.getCost());
				amn.setDistance(p.getCostInMeters());
			} catch (EmptyResourcesException e) {
				e.printStackTrace();
			}
		}

		for (AntEdge eIn : amn.in.values()) {
			for (StraddleCarrier rsc : vehicles.values()) {
				eIn.addCost(rsc);
			}
		}
		for (AntEdge eOut : amn.getDestinations()) {
			for (StraddleCarrier rsc : vehicles.values()) {
				eOut.addCost(rsc);
			}
		}
	}

	/**
	 * Remove mission
	 * 
	 * @param m
	 *            Mission to remove
	 * @return
	 */
	@Override
	protected boolean removeMission(Mission m) {
		if(pool.remove(m)){
			AntMissionNode n = missionsNodes.remove(m.getId());
			AntHill h = null;
			if (globalParameters.getLAMBDA() > 0) {
				h = hillsNodes.get(n.getColor());
				if (h != null) {
					StraddleCarrier resource = h.getStraddleCarrier();

					if (resource.getCurrentLoad() != null && resource.getCurrentLoad().getMission().getId().equals(m.getId())) {
						System.err.println("REMOVING " + m.getId() + " MARK BEST PATH WITH COLOR " + h.getID());
						h.markBestPath();
					} else if (resource.getCurrentLoad() == null) {
						System.err.println(resource.getId() + " is IDLE at " + m.getId() + " removal!");
					}
				}
			}

			n.delete();
			toDestroy.add(n);

			// PUT ANTS BACK IN DEPOT ++> for debug
			// resetAnts();

			if (pool.isEmpty()) {
				Sprite lastSprite = spriteManager.addSprite("lastSprite");
				lastSprite.addAttribute("ui.style", "visibility-mode: hidden;");
			}

			return true;
		} else {
			return false;
		}
		//		for (int i = 0; i < pool.size(); i++) {
		//			if (m.getId().equals(pool.get(i).getId())) {
		//				pool.remove(i);
		//			
		//			}
		//		}
		//		return false;
	}

	/**
	 * Update mission
	 * 
	 * @param m
	 *            Mission to update
	 */
	protected void updateMission(Mission m) {
		AntMissionNode amn = missionsNodes.get(m.getId());
		HashMap<String, Double> ph = amn.getPheromone();
		removeMission(m);
		insertMission(m);
		amn = missionsNodes.get(m.getId());
		amn.setPheromone(ph);
	}

	/*
	 * ========================== TASKS AND RESOURCES LISTENERS
	 * ==========================
	 */
	@Override
	public void missionStarted(Time t, Mission m, String resourceID) {
		lock.lock();
		graphChanged = true;
		lock.unlock();

		// REMOVE MISSION FROM SCHEDULER
		Set<MissionStartedHelper> toStartList = null;
		if (missionsToStart.containsKey(t))
			toStartList = missionsToStart.get(t);
		else
			toStartList = Collections.synchronizedSet(new HashSet<MissionStartedHelper>());

		toStartList.add(new MissionStartedHelper(m, resourceID));
		missionsToStart.put(t, toStartList);
	}

	@Override
	protected void missionStarted(Mission m, String resourceID) {
		// new
		// Exception("MISSION STARTED : "+m.getId()+" BY "+resourceID).printStackTrace();
		AntHill hill = hillsNodes.get(resourceID);
		AntMissionNode n = missionsNodes.get(m.getId());
		hill.getStraddleCarrier().addMissionInWorkload(m);
		// System.err.println("MISSION ADD IN WL : "+m.getId()+" BY "+resourceID);
		hill.visitNode(n);
		// System.err.println("NODE VISITED : "+n.getID()+" BY "+resourceID);
		// new java.util.Scanner(System.in).nextLine();

		removeMission(m);
		// System.err.println("MISSION REMOVED : "+m.getId()+" BY "+resourceID);
	}

	@Override
	public boolean removeMission(Time t, Mission m) {
		lock.lock();
		graphChanged = true;
		lock.unlock();

		List<Mission> toRemoveList = null;
		if (missionsToRemove.containsKey(t))
			toRemoveList = missionsToRemove.get(t);
		else
			toRemoveList = Collections.synchronizedList(new ArrayList<Mission>());

		int i = 0;
		for (Mission m2 : toRemoveList) {
			int comp = m2.getId().compareTo(m.getId());
			if (comp >= 0)
				break;
			else
				i++;
		}
		toRemoveList.add(i, m);

		missionsToRemove.put(t, toRemoveList);
		// TODO Watchout: private method is called directly here to avoid
		// scheduling pbs
		return true; // removeMission(m);
	}

	@Override
	public void updateMission(Time t, Mission m) {
		lock.lock();
		graphChanged = true;
		lock.unlock();

		List<Mission> toUpdateList = null;
		if (missionsToUpdate.containsKey(t))
			toUpdateList = missionsToUpdate.get(t);
		else
			toUpdateList = Collections.synchronizedList(new ArrayList<Mission>());

		int i = 0;
		for (Mission m2 : toUpdateList) {
			int comp = m2.getId().compareTo(m.getId());
			if (comp >= 0)
				break;
			else
				i++;
		}
		toUpdateList.add(i, m);

		missionsToUpdate.put(t, toUpdateList);
	}

	@Override
	public void addMission(Time t, Mission m) {
		lock.lock();
		graphChanged = true;
		lock.unlock();

		List<Mission> toAddList = null;
		if (missionsToAdd.containsKey(t))
			toAddList = missionsToAdd.get(t);
		else
			toAddList = Collections.synchronizedList(new ArrayList<Mission>());

		int i = 0;
		for (Mission m2 : toAddList) {
			int comp = m2.getId().compareTo(m.getId());
			if (comp >= 0)
				break;
			else
				i++;
		}
		toAddList.add(i, m);

		missionsToAdd.put(t, toAddList);
	}

	@Override
	public void addResource(Time t, StraddleCarrier rsc) {
		lock.lock();
		graphChanged = true;
		lock.unlock();

		List<StraddleCarrier> toAddList = null;
		if (resourceToAdd.containsKey(t))
			toAddList = resourceToAdd.get(t);
		else
			toAddList = Collections.synchronizedList(new ArrayList<StraddleCarrier>());

		int i = 0;
		for (StraddleCarrier rsc2 : toAddList) {
			int comp = rsc2.getId().compareTo(rsc.getId());
			if (comp >= 0)
				break;
			else
				i++;
		}
		toAddList.add(i, rsc);
		resourceToAdd.put(t, toAddList);
	}

	@Override
	public boolean removeResource(Time t, StraddleCarrier rsc) {
		lock.lock();
		graphChanged = true;
		lock.unlock();

		List<String> toRemoveList = null;
		if (resourceToRemove.containsKey(t))
			toRemoveList = resourceToRemove.get(t);
		else
			toRemoveList = Collections.synchronizedList(new ArrayList<String>());

		int i = 0;
		for (String rsc2 : toRemoveList) {
			int comp = rsc2.compareTo(rsc.getId());
			if (comp >= 0)
				break;
			else
				i++;
		}
		toRemoveList.add(i, rsc.getId());
		resourceToRemove.put(t, toRemoveList);

		return resources.contains(rsc);
	}

	@Override
	public void updateResourceLocation(Time t, String straddleID, double distance, double travelTime) {
		// System.err.println("URL : "+straddleID+" "+distance+" "+travelTime);
		lock.lock();
		graphChangedByUpdate++;
		lock.unlock();

		Map<String, UpdateInfo> toUpdateMap = null;
		if (resourceToUpdate.containsKey(t))
			toUpdateMap = resourceToUpdate.get(t);
		else
			toUpdateMap = Collections.synchronizedMap(new HashMap<String, UpdateInfo>());

		toUpdateMap.put(straddleID, new UpdateInfo(distance, travelTime));
		resourceToUpdate.put(t, toUpdateMap);
	}

	/* ========================== GETTERS AND SETTERS ========================== */
	/**
	 * Getter on the Graph
	 * 
	 * @return The graph
	 */
	public Graph getGraph() {
		return graph;
	}

	/**
	 * Getter on the list of nodes corresponding to missions
	 * 
	 * @return The list of nodes corresponding to missions
	 */
	public List<AntMissionNode> getMissionNodes() {
		return new ArrayList<AntMissionNode>(missionsNodes.values());
	}

	/**
	 * Getter on the list of hills
	 * 
	 * @return The list of hills
	 */
	public List<AntHill> getHills() {
		return new ArrayList<AntHill>(hillsNodes.values());
	}

	/**
	 * Getter on the SpriteManager of the graph viewer
	 * 
	 * @return The SpriteManager
	 */
	public SpriteManager getSpriteManager() {
		return spriteManager;
	}

	@Override
	public String getId() {
		return OnlineACOScheduler.rmiBindingName;
	}

	/**
	 * Getter on the layout
	 * 
	 * @return The layout
	 */
	public ACOLayout getLayout() {
		return layout;
	}

	/**
	 * Getter on the resources
	 * 
	 * @return The list of the available resources
	 */
	public List<StraddleCarrier> getResources() {
		return resources;
	}

	// /**
	// * Getter on the status of the algorithm
	// * @return True if the algorithm has been interrupted, false otherwise
	// */
	// public static boolean isInterrupted(){
	// return interrupt;
	// }

	/**
	 * Getter on the visibility of the edges weights
	 * 
	 * @return True if the weights are showed, False otherwise
	 */
	public boolean displayWeights() {
		return jcbDisplayWeights.isSelected();
	}

	/**
	 * Getter on the parameters of the algorithm
	 * 
	 * @return The parameters of the ACO algorithm
	 */
	public ACOParameters getGlobalParameters() {
		return globalParameters;
	}

	/**
	 * Return True if the weight of the edges is based on a distance heuristic,
	 * False otherwise
	 * 
	 * @return True if the weight of the edges is based on a distance heuristic,
	 *         False otherwise
	 * 
	 *         public boolean isDistanceWeight() { boolean b = true;
	 *         if(jrbDistance!=null) b = jrbDistance.isSelected(); return b; }
	 */

	/**
	 * Setter on the global parameters of the ACO algorithm
	 * 
	 * @param p
	 */
	public void setGlobalParameters(ACOParameters p) {
		globalParameters = p;
	}

	// /**
	// * (Dis)Interrupt the algorithm
	// * @param interrupted
	// */
	// public static void setInterrupted(boolean interrupted){
	// interrupt = interrupted;
	// }

	public int getSyncSize() {
		return syncSize;
	}

	// /**
	// * Change the synchronization size of the algorithm ($syncSize ACO
	// iterations / 1 simulation step)
	// * @param newSyncSize New synchronization size
	// */
	// public static void setSyncSize(final int newSyncSize){
	// syncSize = newSyncSize;
	// interrupt = true;
	// if(SwingUtilities.isEventDispatchThread()){
	//
	// }
	// else{
	// SwingUtilities.invokeLater(new Runnable(){
	// public void run(){
	// jsSyncSize.setValue(syncSize);
	// }
	// });
	// }
	// }

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		for (AntHill hill : hillsNodes.values()) {
			sb.append(hill + "\n");
		}
		return sb.toString();
	}

	/* ========================== DESTRUCTOR ========================== */
	/**
	 * Destruct the object
	 */
	public void destroy() {
		super.destroy();

		//jtb = null;
		// for(ChangeListener listener : jsSyncSize.getChangeListeners())
		// jsSyncSize.removeChangeListener(listener);
		// jsSyncSize = null;

		//for (ActionListener listener : jcbDisplayWeights.getActionListeners())
		//			jcbDisplayWeights.removeActionListener(listener);
		//		jcbDisplayWeights = null;


		//		for (AntMissionNode n : missionsNodes.values()) {
		//			n.destroy();
		//		}
		//		missionsNodes.clear();
		//		for (AntMissionNode n : toDestroy) {
		//			n.destroy();
		//		}

		//		for (AntHill ah : hillsNodes.values()) {
		//			ah.destroy();
		//		}
		//		hillsNodes.clear();

		//		toDestroy.clear();
		//		toDestroy = null;

		//		depot.destroy();
		//		end.destroy();

		//		Iterator<? extends Sprite> sprites = spriteManager.spriteIterator();
		//		List<String> toRemove = new ArrayList<String>(spriteManager.getSpriteCount());
		//		while (sprites.hasNext()) {
		//			toRemove.add(sprites.next().getId());
		//		}
		//		for (String sID : toRemove) {
		//			spriteManager.removeSprite(sID);
		//		}

		//		globalParameters = null;

		// algoThread = null;
		//		graph = null;

		//		lastLocations.clear();
		//		lastLocations = null;
		//		layout = null;

		progressBarFrame.dispose();
		//		jpb = null;

		//		progressBarFrame = null;

		//		selectedNode = null;
		//		spriteManager = null;

		//		for (MouseMotionListener mml : view.getMouseMotionListeners())
		//			view.removeMouseMotionListener(mml);
		//		for (MouseListener ml : view.getMouseListeners())
		//			view.removeMouseListener(ml);
		//		view = null;
		//		viewer = null;
		//		renderer = null;

		//		depot = null;
		//		end = null;
	}

	/* ========================== OTHERS ========================== */
	/**
	 * Remove the edge between the two given nodes of the graph
	 * 
	 * @param from
	 *            Source node of the edge to remove
	 * @param to
	 *            Destination node of the edge to remove
	 */
	public void removeEdge(AntNode from, AntNode to) {
		from.removeOutgoingEdge(to);
		to.removeIncomingEdge(from);
	}

	public void removeNode(String ID) {
		graph.removeNode(ID);
	}

	/**
	 * Transform PX length to GU
	 * 
	 * @param px
	 *            Length in PX
	 * @return Length in GU
	 */
	public double toGu(final double px) {
		Camera cam = view.getCamera();
		GraphMetrics metrics = cam.getMetrics();
		return metrics.lengthToGu(px, Units.PX);
	}

	public List<String> getColoredNodes(String color) {
		List<String> l = new ArrayList<String>(missionsNodes.size());
		for (AntMissionNode node : missionsNodes.values()) {
			if (node.getColor().equals(color)) {
				l.add(node.getID());
			}
		}
		return l;
	}

	public Graph getSubGraph(String color) {
		Graph g = new DefaultGraph("subGraph(" + color + ")");
		Node start = g.addNode(depot.getID());
		start.addAttribute("weight", 0);

		for (AntMissionNode amn : missionsNodes.values()) {
			// System.out.print("amn.color = "+amn.getColor()+" | color = "+color);
			if (graph.getNode(amn.getID()) != null && amn.getColor().equals(color)) {

				Node n = g.addNode(amn.getID());
				n.addAttribute("weight", 0.0 - amn.getPheromone(color));
				// System.out.println(amn.getId()+" => "+(1.0/amn.getPheromone(color)));
			}
			// else System.out.println(" => 2");
		}
		Node end = g.addNode(OnlineACOScheduler.end.getID());
		end.addAttribute("weight", 0);

		for (Node n : g.getNodeSet()) {
			for (Node n2 : g.getNodeSet()) {
				if (n != n2) {
					AntEdge edge = getAntEdge(n.getId(), n2.getId());
					if (edge != null) {
						Edge e = g.addEdge(n.getId() + "-" + n2.getId(), n, n2, true);
						e.addAttribute("weight", edge.getCost(color));
					}
				}
			}
		}

		return g;
	}

	public static void debug(Graph g) {
		// DEBUG
		Iterator<Node> nIT = g.getNodeIterator();
		System.out.println("GRAPH : ");
		while (nIT.hasNext()) {
			Node n = nIT.next();
			System.out.println(n.getId() + " : W=" + n.getAttribute("weight"));
			Iterator<Edge> eIT = n.getLeavingEdgeIterator();
			while (eIT.hasNext()) {
				Edge e = eIT.next();
				System.out.println("\t" + e.getTargetNode().getId() + " : W=" + e.getAttribute("weight"));
			}
		}

		System.out.println("---------------- END GRAPH --------------");
	}

	private AntEdge getAntEdge(String origin, String destination) {
		AntNode nFrom = getNode(origin);
		AntNode to = getNode(destination);
		if (origin.equals(end.getID()))
			return null;
		else
			return nFrom.getEdgeTo(to);
	}

	public AntNode getNode(String ID) {
		AntNode n = null;
		if (ID.equals(depot.getID()))
			n = depot;
		else if (ID.equals(end.getID()))
			n = end;
		else
			n = missionsNodes.get(ID);

		return n;
	}

	public static long getAlgorithmStep() {
		return step;
	}
}
