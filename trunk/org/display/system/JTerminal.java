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
package org.display.system;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Toolkit;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.ListSelectionModel;
import javax.swing.Popup;
import javax.swing.PopupFactory;
import javax.swing.SwingUtilities;
import javax.swing.border.BevelBorder;
import javax.swing.event.MouseInputAdapter;

import org.apache.log4j.Logger;
import org.display.GraphicDisplay;
import org.display.GraphicDisplayPanel;
import org.display.MainFrame;
import org.display.PopupListCell;
import org.display.PopupListCellRenderer;
import org.display.TextDisplay;
import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.MultiGraph;
import org.graphstream.ui.geom.Point2;
import org.graphstream.ui.geom.Point3;
import org.graphstream.ui.graphicGraph.GraphicElement;
import org.graphstream.ui.graphicGraph.stylesheet.StyleConstants.Units;
import org.graphstream.ui.spriteManager.Sprite;
import org.graphstream.ui.spriteManager.SpriteManager;
import org.graphstream.ui.swingViewer.DefaultView;
import org.graphstream.ui.swingViewer.GraphRenderer;
import org.graphstream.ui.swingViewer.View;
import org.graphstream.ui.swingViewer.Viewer;
import org.graphstream.ui.swingViewer.util.Camera;
import org.graphstream.ui.swingViewer.util.GraphMetrics;
import org.positioning.Coordinates;
import org.system.Crossroad;
import org.system.Depot;
import org.system.Road;
import org.system.RoadPoint;
import org.system.StraddleCarrierSlot;
import org.system.Terminal;
import org.system.container_stocking.Bay;
import org.system.container_stocking.BayCrossroad;
import org.system.container_stocking.Block;
import org.system.container_stocking.Container;
import org.system.container_stocking.ExchangeBay;
import org.system.container_stocking.Quay;
import org.system.container_stocking.SeaOrientation;
import org.system.container_stocking.Slot;
import org.util.Location;
import org.util.RecordableObject;
import org.vehicles.Ship;
import org.vehicles.StraddleCarrier;

public class JTerminal implements RecordableObject {
	private static final Logger logger = Logger.getLogger(JTerminal.class);

	private static JTerminal instance;

	public static final String rmiBindingName = "JTerminal";
	public static final int WIDTH = MainFrame.WIDTH;
	public static final int HEIGHT = 500;
	public static final int EPSILON = 0;

	private Graph graph;
	private SpriteManager spriteManager;
	private Viewer viewer;

	private Map<String, Sprite> ships;
	private Map<String, Sprite> straddleCarriers;
	private Map<String, Sprite> containers;
	private SelectedContainer sc;
	private SelectedVehicle sv;
	private SelectedSlot ss;
	private Map<String, Sprite> slots;

	private Point2 dragOrigin;
	private boolean firstDrag = false;

	private JFrame jframe;

	private String id;

	private Popup currentPopup;

	public static JTerminal getInstance() {
		if (instance == null) {
			instance = new JTerminal();
			instance.init(rmiBindingName,false);
			//			Terminal.getInstance().setTextDisplay(//TODO);
		}
		return instance;
	}

	public static void closeInstance() {
		if(instance != null){
			instance.destroy();
			instance = null;
		}
	}

	private JTerminal(){

	}

	private void init(String id, final boolean frame) {
		graph = new MultiGraph("Graph", true, false);
		spriteManager = new SpriteManager(graph);
		viewer = new Viewer(graph,
				Viewer.ThreadingModel.GRAPH_IN_ANOTHER_THREAD);

		ships = new HashMap<>();
		straddleCarriers = new HashMap<>();
		containers = new HashMap<>();
		sc = new SelectedContainer();
		sv = new SelectedVehicle();
		ss = new SelectedSlot();
		slots = new HashMap<>();

		try {
			this.id = id;

			InputStream is = this.getClass().getResourceAsStream(
					"/org/display/system/jterminal.css");
			StringBuilder sb = new StringBuilder();
			Scanner scan = null;
			try {
				scan = new Scanner(is);

				while (scan.hasNextLine()) {
					sb.append(scan.nextLine() + "\n");
				}
			} catch (Exception e) {
				logger.error(e);
			} finally {
				if (scan != null)
					scan.close();
			}
			graph.addAttribute("ui.stylesheet", sb.toString());
			graph.addAttribute("ui.quality");
			graph.addAttribute("ui.antialias");

			graph.addAttribute("viewLocked", false);

			/**
			 * For scala viewer...
			 */
			// System.setProperty(
			// "gs.ui.renderer","org.graphstream.ui.j2dviewer.J2DGraphRenderer"
			// ); //DEPRECATED
			System.setProperty("org.graphstream.ui.renderer",
					"org.graphstream.ui.j2dviewer.J2DGraphRenderer");

			final GraphRenderer gr = Viewer.newGraphRenderer();
			final PopupFactory pf = PopupFactory.getSharedInstance();
			final GraphMetrics metrics = new GraphMetrics();

			final View maVue = new DefaultView(viewer, "view_"
					+ JTerminal.this.id, gr);

			// TODO change to false?true
			maVue.setIgnoreRepaint(true);
			for (MouseMotionListener mml : maVue.getMouseMotionListeners())
				maVue.removeMouseMotionListener(mml);
			maVue.openInAFrame(false);

			MouseListener ml = new MouseAdapter() {
				@Override
				public void mouseClicked(MouseEvent e) {
					if (e.getButton() == MouseEvent.BUTTON1) {
						if (currentPopup != null) {
							closePopupMenu();
						} else {
							ArrayList<GraphicElement> l = maVue
									.allNodesOrSpritesIn(e.getX() - EPSILON,
											e.getY() - EPSILON, e.getX()
											+ EPSILON, e.getY()
											+ EPSILON);
							Vector<String> vSC = new Vector<String>();
							Vector<String> vContainers = new Vector<String>();
							Vector<String> vSlots = new Vector<String>();
							for (GraphicElement ge : l) {
								if (straddleCarriers.containsKey(ge.getId())) {
									vSC.add(ge.getId());
								} else if (containers.containsKey(ge.getId())) {
									vContainers.add(ge.getId());
								} else if (slots.containsKey(ge.getId())) {
									vSlots.add(ge.getId());
								}
							}

							if (vSC.size() + vContainers.size() + vSlots.size() > 0) {
								PopupListCell[] t = new PopupListCell[vSC
								                                      .size()
								                                      + vContainers.size()
								                                      + vSlots.size()];
								int i = 0;

								for (String s : vSC) {
									PopupListCell jlTmp;
									jlTmp = new PopupListCell(s,
											"straddleCarrier", Terminal
											.getInstance()
											.getStraddleCarrier(s)
											.getIcon());

									jlTmp.setFont(GraphicDisplay.font);
									jlTmp.setOpaque(true);
									jlTmp.setBackground(new Color(255, 238,
											106, 100));
									t[i++] = jlTmp;
								}
								for (String s : vContainers) {
									PopupListCell jlTmp = new PopupListCell(s,
											"container", Terminal.getInstance()
											.getContainer(s).getIcon());
									jlTmp.setFont(GraphicDisplay.font);
									jlTmp.setOpaque(true);
									jlTmp.setBackground(new Color(106, 238,
											255, 100));
									t[i++] = jlTmp;
								}
								for (String s : vSlots) {
									PopupListCell jlTmp = new PopupListCell(s,
											"slot", null);
									jlTmp.setFont(GraphicDisplay.font);
									jlTmp.setOpaque(true);
									jlTmp.setBackground(new Color(238, 255,
											106, 100));
									t[i++] = jlTmp;
								}
								final JList<PopupListCell> jl = new JList<PopupListCell>(
										t);
								jl.setBorder(BorderFactory
										.createBevelBorder(BevelBorder.RAISED));
								jl.setCellRenderer(new PopupListCellRenderer());
								jl.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

								MouseInputAdapter mia = new MouseInputAdapter() {
									public void mouseMoved(MouseEvent e) {
										int index = jl.locationToIndex(e
												.getPoint());
										if (index >= 0)
											jl.setSelectedIndex(index);
									}

									public void mouseClicked(MouseEvent e) {
										if (jl.getSelectedIndex() >= 0) {

											String type = jl.getSelectedValue()
													.getType();

											if (type.equals("straddleCarrier")) {
												vehicleSelected(jl
														.getSelectedValue()
														.getText());
												containerSelected("");
												slotSelected("");

												if (e.isAltDown()) {
													// Show in JTable
													GraphicDisplayPanel.getInstance()
													.showVehicle(jl
															.getSelectedValue()
															.getText());
												}
											} else if (type.equals("container")) {
												vehicleSelected("");
												containerSelected(jl
														.getSelectedValue()
														.getText());
												slotSelected("");
												if (e.isAltDown()) {
													// Show in JTable
													GraphicDisplayPanel.getInstance()
													.showContainer(jl
															.getSelectedValue()
															.getText());
												}
											} else if (type.equals("slot")) {
												vehicleSelected("");
												containerSelected("");
												slotSelected(jl
														.getSelectedValue()
														.getText());
												if (e.isAltDown()) {
													// Show in JTable
													GraphicDisplayPanel.getInstance()
													.showSlot(jl
															.getSelectedValue()
															.getText());
												}
											}
											closePopupMenu();

										}
									}
								};

								jl.addMouseListener(mia);
								jl.addMouseMotionListener(mia);

								int cy = e.getYOnScreen();
								int cx = e.getXOnScreen();
								currentPopup = pf.getPopup(null, jl, cx, cy);
								currentPopup.show();
							}
						}
					} else if (e.getButton() == MouseEvent.BUTTON3) {
						vehicleSelected("");
						containerSelected("");
						slotSelected("");
					}
				}

				@Override
				public void mousePressed(MouseEvent e) {
					firstDrag = true;
				}
			};
			maVue.addMouseListener(ml);

			maVue.addMouseWheelListener(new MouseWheelListener() {
				@Override
				public void mouseWheelMoved(MouseWheelEvent e) {
					Camera c = maVue.getCamera();
					// System.out.println("ZOOM(1) : "+c.getViewPercent());
					c.setViewPercent(c.getViewPercent()
							+ (e.getWheelRotation() / 20.0));
					// System.out.println("ZOOM(2) : "+c.getViewPercent());
				}
			});

			maVue.addMouseMotionListener(new MouseMotionAdapter() {
				@Override
				public void mouseDragged(MouseEvent e) {
					if (firstDrag) {
						dragOrigin = new Point2(e.getX(), e.getY());
						firstDrag = false;
					} else {
						Point2 v = new Point2(e.getX() - dragOrigin.x, e.getY()
								- dragOrigin.y);
						Point2 vGU = new Point2(metrics.lengthToGu(v.x,
								Units.PX), metrics.lengthToGu(v.y, Units.PX));
						// System.err.println("Before : "+maVue.getViewCenter().x+" "+maVue.getViewCenter().y+" "+maVue.getViewCenter().z);
						maVue.getCamera().setViewCenter(
								maVue.getCamera().getViewCenter().x - vGU.x,
								maVue.getCamera().getViewCenter().y + vGU.y,
								maVue.getCamera().getViewCenter().z);
						// System.err.println("After : "+maVue.getViewCenter().x+" "+maVue.getViewCenter().y+" "+maVue.getViewCenter().z);
						dragOrigin = new Point2(e.getX(), e.getY());
					}
				}
			});
			viewer.addView(maVue);

			viewer.getGraphicGraph().computeBounds();
			Point3 lo = viewer.getGraphicGraph().getMinPos();
			Point3 hi = viewer.getGraphicGraph().getMaxPos(); // graph
			// est
			// le
			// GraphicGraph
			metrics.setBounds(lo.x, lo.y, lo.z, hi.x, hi.y, hi.z);
			double widthGU = hi.x - lo.x;
			double ratio = widthGU / maVue.getWidth();

			metrics.setRatioPx2Gu(ratio);

			if (frame) {
				jframe = new JFrame("Viewer2D");
				jframe.setSize(WIDTH, HEIGHT);
				jframe.setLocation(Math.max(0, Toolkit.getDefaultToolkit()
						.getScreenSize().width - WIDTH), 0);
				jframe.getContentPane().add(maVue, BorderLayout.CENTER);
			}
			init();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void closePopupMenu() {
		if (currentPopup != null) {
			currentPopup.hide();
			currentPopup = null;
		}
	}

	public void addContainers(List<Container> list) {
		for (Container c : list)
			addContainer(c);
	}

	public void addContainer(Container container) {
		Sprite spContainer = spriteManager.addSprite(container.getId());
		StringBuilder ui_class = new StringBuilder();
		ui_class.append("container");
		if (container.getDimensionType() == Container.TYPE_20_Feet)
			ui_class = ui_class.append(",twenty");
		else if (container.getDimensionType() == Container.TYPE_40_Feet)
			ui_class = ui_class.append(",fourty");
		String from = container.getLocation().getRoad()
				.getIdRoadPointOrigin(container.getLocation().getPourcent());
		String to = container
				.getLocation()
				.getRoad()
				.getIdRoadPointDestination(
						container.getLocation().getPourcent());
		if (graph.getNode(from) != null
				&& graph.getNode(from).getEdgeToward(to) != null) {
			Edge e = graph.getNode(from).getEdgeToward(to);
			spContainer.addAttribute("ui.class", ui_class.toString());

			String cssStyle = container.getCSSStyle();

			spContainer.attachToEdge(e.getId());

			float rate = (float) container.getLocation().getPourcent();
			spContainer.setPosition(rate);
			spContainer.addAttribute("percent", rate);
			spContainer.addAttribute("ui.style", cssStyle);
		}
		containers.put(container.getId(), spContainer);
	}

	public void addCrossroad(RoadPoint crossroad) {
		Node n = graph.addNode(crossroad.getId());

		if (crossroad instanceof BayCrossroad) {
			n.addAttribute("ui.class", "laneCrossroad");
		} else {
			n.addAttribute("ui.class", "crossroad");
		}
		n.addAttribute("x", crossroad.getLocation().x);
		n.addAttribute("y", crossroad.getLocation().y);
		n.addAttribute("z", crossroad.getLocation().z);
		n.addAttribute("id", crossroad.getId());
		n.addAttribute("label", n.getAttribute("id"));
	}

	public void addCrossroads(List<RoadPoint> l) {
		for (RoadPoint c : l) {
			addCrossroad(c);
		}
	}

	public void addRoad(Road road) {
		// System.out.println("Adding road "+road.getId());
		if (graph.getNode(road.getOriginId()) == null) {
			if (road.getOrigin() instanceof BayCrossroad) {
				BayCrossroad origin = (BayCrossroad) road.getOrigin();
				if (!origin.getMainRoad().equals(""))
					addRoad(Terminal.getInstance()
							.getRoad(origin.getMainRoad()));
			}
		}
		if (graph.getNode(road.getDestinationId()) == null) {
			if (road.getDestination() instanceof BayCrossroad) {
				BayCrossroad destination = (BayCrossroad) road.getDestination();
				if (!destination.getMainRoad().equals(""))
					addRoad(Terminal.getInstance().getRoad(
							destination.getMainRoad()));
			}
		}
		// Add a node for each roadpoint
		String last = road.getOrigin().getId();
		if (graph.getNode(last) == null) {
			// System.out.println("Le graph ne contient pas "+last);
			Crossroad lastCrossroad = Terminal.getInstance().getCrossroad(last);
			Node n = graph.addNode(last);

			// n.addAttribute("type", "roadpoint");
			n.addAttribute("x", lastCrossroad.getLocation().x);
			n.addAttribute("y", lastCrossroad.getLocation().y);
			n.addAttribute("z", lastCrossroad.getLocation().z);
			n.addAttribute("id", last);
			// n.addAttribute("label", n.getAttribute("id"));
			if (lastCrossroad instanceof BayCrossroad)
				n.addAttribute("ui.class", "laneCrossroad");
			else
				n.addAttribute("ui.class", "roadpoint");

		}
		// double lastRadius = road.getOrigin().getRadius();
		int i = 1;
		for (RoadPoint rp : road.getPoints()) {
			if (graph.getNode(rp.getId()) == null) {
				Node n = graph.addNode(rp.getId());
				n.addAttribute("x", rp.getLocation().x);
				n.addAttribute("y", rp.getLocation().y);
				n.addAttribute("z", rp.getLocation().z);
				n.addAttribute("id", rp.getId());
				if (rp instanceof BayCrossroad)
					n.addAttribute("ui.class", "laneCrossroad");
				else
					n.addAttribute("ui.class", "roadpoint");
			}
			Edge e = null;
			if (graph.getNode(last).hasEdgeFrom(rp.getId())) {
				e = graph.getNode(last).getEdgeFrom(rp.getId());
			} else if (graph.getNode(last).hasEdgeToward(rp.getId())) {
				e = graph.getNode(last).getEdgeToward(rp.getId());
			} else {
				String id = road.getId() + "-" + i + "/"
						+ (road.getPoints().size() + 1);
				if (graph.getEdge(id) == null) {
					e = graph.addEdge(id, last, rp.getId(), road.isDirected());
					if (road instanceof Bay)
						if (road instanceof ExchangeBay)
							e.addAttribute("ui.class", "exchangeLane");
						else
							e.addAttribute("ui.class", "lane");
					else
						e.addAttribute("ui.class", "road");
				}
			}
			if (e != null)
				e.addAttribute("label", e.getId());
			last = rp.getId();
			i++;
		}
		Edge e = null;
		String destinationId = road.getDestinationId();
		if (graph.getNode(destinationId) == null) {
			Crossroad lastCrossroad = Terminal.getInstance().getCrossroad(
					destinationId);
			Node n = graph.addNode(destinationId);

			n.addAttribute("x", lastCrossroad.getLocation().x);
			n.addAttribute("y", lastCrossroad.getLocation().y);
			n.addAttribute("z", lastCrossroad.getLocation().z);
			n.addAttribute("id", destinationId);
			if (lastCrossroad instanceof BayCrossroad)
				n.addAttribute("ui.class", "laneCrossroad");
			else
				n.addAttribute("ui.class", "roadpoint");

		}
		if (graph.getNode(last).hasEdgeFrom(road.getDestination().getId())) {
			e = graph.getNode(last).getEdgeFrom(road.getDestination().getId());
		} else if (graph.getNode(last).hasEdgeToward(
				road.getDestination().getId())) {
			e = graph.getNode(last)
					.getEdgeToward(road.getDestination().getId());
		} else {

			if (i == 1) {
				if (graph.getEdge(road.getId()) == null)
					e = graph.addEdge(road.getId(), last, road.getDestination()
							.getId(), road.isDirected());
			} else {
				if (graph.getEdge(road.getId() + "-" + i + "/"
						+ (road.getPoints().size() + 1)) == null)
					e = graph.addEdge(road.getId() + "-" + i + "/"
							+ (road.getPoints().size() + 1), last, road
							.getDestination().getId(), road.isDirected());
			}

			if (e != null) {
				if (road instanceof Bay)
					if (road instanceof ExchangeBay)
						e.addAttribute("ui.class", "exchangeLane");
					else
						e.addAttribute("ui.class", "lane");
				else
					e.addAttribute("ui.class", "road");
			}
		}
		if (e != null) {
			e.addAttribute("label", e.getId());
		} else
			System.out.println("Road " + road.getId() + " NOT added !!!");
	}

	public void addRoads(List<Road> l) {
		for (Road r : l)
			addRoad(r);
	}

	public void addDepot(Depot d) {

		Map<String, Coordinates> coords = d.getCoordinates();
		Map<String, String> walls = d.getWalls();

		for (Iterator<String> keys = coords.keySet().iterator(); keys.hasNext();) {
			String id = keys.next();
			Node n = graph.addNode(id);
			Coordinates c = coords.get(id);
			n.addAttribute("x", c.x);
			n.addAttribute("y", c.y);
			n.addAttribute("z", c.z);
			n.addAttribute("ui.class", "depotPoint");
		}
		for (Iterator<String> keys = walls.keySet().iterator(); keys.hasNext();) {
			String from = keys.next();
			String to = walls.get(from);
			Edge e = graph.addEdge(from + "-" + to, from, to);
			e.addAttribute("ui.class", "depotWall");
		}
		if(d.getStraddleCarrierSlots()!=null){
			for (Iterator<StraddleCarrierSlot> slots = d.getStraddleCarrierSlots().values().iterator(); slots.hasNext();) {
				StraddleCarrierSlot slot = slots.next();
				addStraddleCarrierSlot(slot);
			}
		}
	}

	public void addDepots(List<Depot> list) {
		for (Depot d : list)
			addDepot(d);
	}

	public void addLane(Bay l) {
		addRoad(l);
		Collection<Slot> slotsList = Terminal.getInstance().getSlots(l.getId());
		if(slotsList!=null){
			for (Slot slot : slotsList) {

				Sprite spSlot = spriteManager.addSprite(slot.getId());
				String ui_class = "slot_" + slot.getTEU();

				String from = l.getIdRoadPointOrigin(slot.getRateOnLane());
				String to = l.getIdRoadPointDestination(slot.getRateOnLane());
				if (graph.getNode(from) != null
						&& graph.getNode(from).getEdgeToward(to) != null) {
					Edge e = graph.getNode(from).getEdgeToward(to);
					spSlot.addAttribute("ui.class", ui_class);

					String cssStyle = slot.getCSSStyle();
					spSlot.addAttribute("ui.style", cssStyle);

					spSlot.attachToEdge(e.getId());

					float rate = (float) slot.getRateOnLane();
					spSlot.setPosition(rate);
					spSlot.addAttribute("percent", rate);
				}
				slots.put(slot.getId(), spSlot);
			}
		}
	}

	public void addLanes(List<Bay> list) {
		for (Bay l : list)
			addLane(l);
	}

	public void addPave(Block p) {
		Map<String, Coordinates> coords = p.getCoordinates();
		Map<String, String> walls = p.getWalls();
		if (coords != null) {
			for (Iterator<String> keys = coords.keySet().iterator(); keys.hasNext();) {
				String id = keys.next();
				Node n = graph.addNode(id);
				Coordinates c = coords.get(id);
				n.addAttribute("x", c.x);
				n.addAttribute("y", c.y);
				n.addAttribute("z", c.z);
				n.addAttribute("ui.class", "pavePoint");
			}
			if (walls != null) {
				for (Iterator<String> keys = walls.keySet().iterator(); keys.hasNext();) {
					String from = keys.next();
					String to = walls.get(from);
					Edge e = graph.addEdge(from + "-" + to, from, to);

					e.addAttribute("ui.class", "paveWall_" + p.getType());

				}
			}
		}
	}

	public void addShip(Ship b) {
		Quay q = b.getQuay();

		Edge e = graph.getEdge(q.getBorderID());
		double length = (b.getRateTo() - b.getRateFrom())
				* Terminal.getInstance().getRoad(q.getBorderID()).getLength();
		float rateCenter = (float) (b.getRateFrom() + ((b.getRateTo() - b
				.getRateFrom()) / 2f));
		double width = length / 6.1875;

		int d = -1;
		if (q.getSeaOrientation() == SeaOrientation.N
				|| q.getSeaOrientation() == SeaOrientation.NE
				|| q.getSeaOrientation() == SeaOrientation.NW)
			d = 1;
		double margin = 1.0;

		String orientation = "projection";
		String style = "shape: box; fill-mode: image-scaled; fill-image: url('"
				+ Terminal.IMAGE_FOLDER/*.substring(1)*/
				+ "containerVessel2.png'); sprite-orientation: " + orientation
				+ "; size: " + width + "gu, " + length + "gu; z-index: 100;";
		Sprite s = spriteManager.addSprite(b.getID().replaceAll("\\W", "_"));
		s.setAttribute("ui.style", style);
		s.attachToEdge(e.getId());
		s.setPosition(Units.GU, rateCenter, d * ((width / 2) + margin), 0);
		ships.put(s.getId(), s);
	}

	public void removeShip(Ship ship) {
		Sprite s = ships.remove(ship.getID().replaceAll("\\W", "_"));
		spriteManager.removeSprite(s.getId());
	}

	public void addPaves(List<Block> list) {
		for (Block p : list) {
			addPave(p);
		}
	}

	public void addStraddleCarrier(StraddleCarrier rsc) {
		Sprite spStraddle = spriteManager.addSprite(rsc.getId());
		spStraddle.addAttribute("ui.class", "straddleCarrier");
		Location l = rsc.getLocation();
		String from = l.getRoad().getIdRoadPointOrigin(l.getPourcent());
		String to = l.getRoad().getIdRoadPointDestination(l.getPourcent());
		Edge e = graph.getNode(from).getEdgeToward(to);

		String style = rsc.getCSSStyle();
		spStraddle.addAttribute("ui.style", style);
		spStraddle.attachToEdge(e.getId());
		float rate = (float) l.getPourcent();
		spStraddle.setPosition(rate);
		spStraddle.addAttribute("percent", rate);
		straddleCarriers.put(spStraddle.getId(), spStraddle);
	}

	public void addStraddleCarriers(List<StraddleCarrier> l) {
		for (StraddleCarrier rsc : l)
			addStraddleCarrier(rsc);
	}

	public void addStraddleCarrierSlot(StraddleCarrierSlot slot) {
		if (graph.getNode(slot.getOriginId()) == null) {
			if (slot.getOrigin() instanceof BayCrossroad) {
				BayCrossroad origin = (BayCrossroad) slot.getOrigin();
				if (!origin.getMainRoad().equals(""))
					addRoad(Terminal.getInstance()
							.getRoad(origin.getMainRoad()));
			}
		}
		if (graph.getNode(slot.getDestinationId()) == null) {
			if (slot.getDestination() instanceof BayCrossroad) {
				BayCrossroad destination = (BayCrossroad) slot.getDestination();
				if (!destination.getMainRoad().equals(""))
					addRoad(Terminal.getInstance().getRoad(
							destination.getMainRoad()));
			}
		}
		// Add a node for each roadpoint
		String last = slot.getOrigin().getId();
		if (graph.getNode(last) == null) {
			Crossroad lastCrossroad = Terminal.getInstance().getCrossroad(last);
			Node n = graph.addNode(last);
			n.addAttribute("x", lastCrossroad.getLocation().x);
			n.addAttribute("y", lastCrossroad.getLocation().y);
			n.addAttribute("z", lastCrossroad.getLocation().z);
			n.addAttribute("id", last);
			if (lastCrossroad instanceof BayCrossroad)
				n.addAttribute("ui.class", "laneCrossroad");
			else
				n.addAttribute("ui.class", "roadpoint");

		}
		int i = 1;
		for (RoadPoint rp : slot.getPoints()) {
			if (graph.getNode(rp.getId()) == null) {
				Node n = graph.addNode(rp.getId());

				n.addAttribute("x", rp.getLocation().x);
				n.addAttribute("y", rp.getLocation().y);
				n.addAttribute("z", rp.getLocation().z);
				n.addAttribute("id", rp.getId());

				if (rp instanceof BayCrossroad)
					n.addAttribute("ui.class", "laneCrossroad");
				else
					n.addAttribute("ui.class", "roadpoint");

			}
			Edge e = null;
			if (graph.getNode(last).hasEdgeFrom(rp.getId())) {
				e = graph.getNode(last).getEdgeFrom(rp.getId());
			} else if (graph.getNode(last).hasEdgeToward(rp.getId())) {
				e = graph.getNode(last).getEdgeToward(rp.getId());
			} else {
				String id = slot.getId() + "-" + i + "/"
						+ (slot.getPoints().size() + 1);
				if (graph.getEdge(id) == null) {
					e = graph.addEdge(id, last, rp.getId(), slot.isDirected());
					e.addAttribute("ui.class", "scSlot");
				}
			}
			if (e != null)
				e.addAttribute("label", e.getId());
			last = rp.getId();
			i++;
		}
		Edge e = null;
		String destinationId = slot.getDestinationId();
		if (graph.getNode(destinationId) == null) {
			Crossroad lastCrossroad = Terminal.getInstance().getCrossroad(
					destinationId);
			Node n = graph.addNode(destinationId);
			n.addAttribute("x", lastCrossroad.getLocation().x);
			n.addAttribute("y", lastCrossroad.getLocation().y);
			n.addAttribute("z", lastCrossroad.getLocation().z);
			n.addAttribute("id", destinationId);
			if (lastCrossroad instanceof BayCrossroad)
				n.addAttribute("ui.class", "laneCrossroad");
			else
				n.addAttribute("ui.class", "roadpoint");
		}
		if (graph.getNode(last).hasEdgeFrom(slot.getDestination().getId())) {
			e = graph.getNode(last).getEdgeFrom(slot.getDestination().getId());
		} else if (graph.getNode(last).hasEdgeToward(
				slot.getDestination().getId())) {
			e = graph.getNode(last)
					.getEdgeToward(slot.getDestination().getId());
		} else {

			if (i == 1) {
				if (graph.getEdge(slot.getId()) == null)
					e = graph.addEdge(slot.getId(), last, slot.getDestination()
							.getId(), slot.isDirected());
			} else {
				if (graph.getEdge(slot.getId() + "-" + i + "/"
						+ (slot.getPoints().size() + 1)) == null)
					e = graph.addEdge(slot.getId() + "-" + i + "/"
							+ (slot.getPoints().size() + 1), last, slot
							.getDestination().getId(), slot.isDirected());
			}
		}
		if (e != null) {
			e.addAttribute("label", e.getId());
			e.addAttribute("ui.class", "scSlot");
		} else
			System.out.println("Road " + slot.getId() + " NOT added !!!");
	}

	public View getPanel() {
		return viewer.getView("view_" + id);
	}

	public String getId() {
		return id;
	}

	@Override
	public TextDisplay getTextDisplay() {
		return null;
	}

	public Sprite getStraddleCarrier(String id) {
		return straddleCarriers.get(id);
	}

	private void init() {
		Terminal.getInstance().drawElements();
		Terminal.getInstance().drawVehicles();
	}

	public void removeContainer(String containerId) {
		spriteManager.removeSprite(containerId);
		containers.remove(containerId);
	}

	public void removeRoad(Road road) {
		String last = road.getOrigin().getId();

		for (RoadPoint rp : road.getPoints()) {

			Edge e = null;
			if (graph.getNode(last) == null) {
			} else {
				if (graph.getNode(last).hasEdgeFrom(rp.getId())) {

					e = graph.getNode(last).getEdgeFrom(rp.getId());
				} else if (graph.getNode(last).hasEdgeToward(rp.getId())) {
					e = graph.getNode(last).getEdgeToward(rp.getId());
				}
			}
			if (e != null) {
				graph.removeEdge(e.getId());

			}

			if (graph.getNode(rp.getId()) != null) {
				graph.removeNode(rp.getId());
			}
			last = rp.getId();
		}
		Edge e = null;
		if (graph.getNode(last) == null)
			last = road.getOriginId();

		if (graph.getNode(last).hasEdgeFrom(road.getDestination().getId())) {
			e = graph.getNode(last).getEdgeFrom(road.getDestination().getId());
		} else if (graph.getNode(last).hasEdgeToward(
				road.getDestination().getId())) {
			e = graph.getNode(last)
					.getEdgeToward(road.getDestination().getId());
		}
		if (e != null) {
			graph.removeEdge(e.getId());
		}
	}

	public void containerSelected(String containerId) {
		if (containerId.equals("")) {
			if (!sc.getId().equals("")) {
				spriteManager.removeSprite(sc.getId());
				sc.setId("");
				sc.setContainerId("");
			}

		}
		if (!sc.getId().equals("")) {
			spriteManager.removeSprite(sc.getId());
		}
		Sprite spContainer = containers.get(containerId);
		if (spContainer != null) {
			Sprite nSC = spriteManager.addSprite(containerId + "Selected");
			String orientation = "from";
			String style = spContainer.getAttribute("ui.style");
			if (style.contains("sprite-orientation: to;"))
				orientation = "to";

			String styleSelected = "shape: box; sprite-orientation: "
					+ orientation
					+ "; fill-mode: image-scaled; fill-image: url('"
					+ Terminal.IMAGE_FOLDER/*.substring(1)*/
					+ "purple_square.png'); size: " + 25 + "gu, " + 25
					+ "gu; z-index: 100;";
			nSC.addAttribute("ui.style", styleSelected);

			nSC.attachToEdge(spContainer.getAttachment().getId());
			nSC.setPosition(Float.parseFloat(""
					+ spContainer.getAttribute("percent")));
			sc.setId(nSC.getId());
			sc.setContainerId(containerId);
		}
	}

	public void vehicleSelected(String vehicleId) {
		if (vehicleId.equals("")) {
			if (!sv.getId().equals("")) {
				spriteManager.removeSprite(sv.getId());
				sv.setId("");
				sv.setVehicleId("");
			}

		} else {
			if (!sv.getId().equals("")) {
				spriteManager.removeSprite(sv.getId());
			}
			Sprite spVehicle = straddleCarriers.get(vehicleId);
			Sprite nSV = spriteManager.addSprite(vehicleId + "Selected");
			String style = "shape: box; fill-mode: image-scaled; fill-image: url('"
					+ Terminal.IMAGE_FOLDER/*.substring(1)*/
					+ "down_arrow_cropped.png'); size: "
					+ 25
					+ "gu, "
					+ 70
					+ "gu; z-index: 102;";
			nSV.addAttribute("ui.style", style);
			nSV.attachToEdge(spVehicle.getAttachment().getId());
			nSV.setPosition(Float.parseFloat(""
					+ spVehicle.getAttribute("percent")));
			sv.setId(nSV.getId());
			sv.setVehicleId(vehicleId);
			if (isViewLocked()) {
				StraddleCarrier rsc = Terminal.getInstance()
						.getStraddleCarrier(vehicleId);
				Coordinates pos = rsc.getLocation().getCoords();
				setViewCenter(pos.x, pos.y, pos.z);
			}

		}
	}

	public void updateSlot(final Slot slot) {
		Sprite spSlot = slots.get(slot.getId());
		if (spSlot == null) {
			new Thread() {
				public void run() {
					Sprite spSlot = null;
					do {
						spSlot = slots.get(slot.getId());
					} while (spSlot == null);
					spSlot.changeAttribute("ui.style", slot.getCSSStyle());
				}
			}.start();

		} else
			spSlot.changeAttribute("ui.style", slot.getCSSStyle());

	}

	public void slotSelected(String slotId) {
		if (slotId.equals("")) {
			if (!ss.getId().equals("")) {
				if (spriteManager.hasSprite(ss.getId()))
					spriteManager.removeSprite(ss.getId());
				ss.setId("");
				ss.setSlotId("");
			}
		} else {
			if (!ss.getId().equals("")) {
				if (spriteManager.hasSprite(ss.getId()))
					spriteManager.removeSprite(ss.getId());
			}
			Sprite spSlot = slots.get(slotId);
			Sprite nSS = spriteManager.addSprite(slotId + "Selected");
			String orientation = "from";
			String style = spSlot.getAttribute("ui.style");

			if (style.contains("sprite-orientation: to;"))
				orientation = "to";

			String styleSelected = "shape: box; sprite-orientation: "
					+ orientation
					+ "; fill-mode: image-scaled; fill-image: url('"
					+ Terminal.IMAGE_FOLDER/*.substring(1)*/
					+ "orange_corned_square.png'); size: " + 25 + "gu, " + 25
					+ "gu; z-index: 101;";
			nSS.addAttribute("ui.style", styleSelected);

			// nSS.addAttribute("ui.style",
			// "shape: circle; fill-mode: plain; size-mode: normal; size: 20gu, 20gu; fill-color: rgba(0,0,0,100); z-index: 51;");
			// nSS.setPosition(spSlot.getX(),spSlot.getY(),spSlot.getZ());
			nSS.attachToEdge(spSlot.getAttachment().getId());
			nSS.setPosition(Float.parseFloat(""
					+ spSlot.getAttribute("percent")));
			ss.setId(nSS.getId());
			ss.setSlotId(slotId);
		}
	}

	@Override
	public void setTextDisplay(TextDisplay display) {

	}

	public void show() {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				if (jframe != null)
					jframe.setVisible(true);
			}
		});

	}

	public synchronized void destroy() {
//		List<String> ids = new ArrayList<String>(spriteManager.getSpriteCount());
//		for (Sprite s : spriteManager.sprites())
//			ids.add(s.getId());
//		for (String sId : ids)
//			spriteManager.removeSprite(sId);
//		ids = new ArrayList<String>(graph.getEdgeCount());
//		for (Edge e : graph.getEdgeSet()) {
//			ids.add(e.getId());
//		}
//		for (String id : ids)
//			graph.removeEdge(id);
//		ids = new ArrayList<String>(graph.getNodeCount());
//		for (Node n : graph.getNodeSet()) {
//			ids.add(n.getId());
//		}
//		for (String id : ids)
//			graph.removeNode(id);

//		containers.clear();
		
//		sc.destroy();
//		slots.clear();
//		ss.destroy();
//		straddleCarriers.clear();
//		sv.destroy();

//		View maVue = viewer.getView("view_" + id);
//		for (MouseListener ml : maVue.getMouseListeners()) {
//			maVue.removeMouseListener(ml);
//		}
//		for (MouseMotionListener mml : maVue.getMouseMotionListeners()) {
//			maVue.removeMouseMotionListener(mml);
//		}
//		for (MouseWheelListener mwl : maVue.getMouseWheelListeners()) {
//			maVue.removeMouseWheelListener(mwl);
//		}
//
//		viewer.removeView("view_" + id);

		if (this.jframe != null) {
			jframe.setVisible(false);
			jframe.dispose();
		}
//		id = null;
		// System.gc();
	}

	public void lockView(boolean locked) {
		graph.addAttribute("viewLocked", locked);
		if (!sv.getId().equals("")) {
			StraddleCarrier rsc = Terminal.getInstance().getStraddleCarrier(
					sv.vehicleId);
			Coordinates pos = rsc.getLocation().getCoords();
			setViewCenter(pos.x, pos.y, pos.z);
		}
	}

	public void setViewCenter(double x, double y, double z) {
		View v = viewer.getView("view_" + id);
		v.getCamera().setViewCenter(x, y, z);
	}

	public void resetView() {
		View v = viewer.getView("view_" + id);
		v.getCamera().resetView();
	}

	public boolean isViewLocked() {
		return graph.getAttribute("viewLocked");
	}

	public Graph getGraph() {
		return this.graph;
	}

	public SpriteManager getSpriteManager() {
		return this.spriteManager;
	}

	public SelectedVehicle getSelectedVehicle() {
		return this.sv;
	}

	public SelectedContainer getSelectedContainer() {
		return this.sc;
	}
}
