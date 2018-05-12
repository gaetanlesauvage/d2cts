package org.runner;

import java.awt.BorderLayout;
import java.util.Scanner;

import javax.swing.JFrame;

import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.MultiGraph;
import org.graphstream.ui.graphicGraph.stylesheet.StyleConstants.Units;
import org.graphstream.ui.spriteManager.Sprite;
import org.graphstream.ui.spriteManager.SpriteManager;
import org.graphstream.ui.swingViewer.DefaultView;
import org.graphstream.ui.swingViewer.View;
import org.graphstream.ui.swingViewer.Viewer;
import org.graphstream.ui.swingViewer.Viewer.ThreadingModel;

public class TestSprite {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// System.setProperty(
		// "gs.ui.renderer","org.graphstream.ui.j2dviewer.J2DGraphRenderer" );
		// //DEPRECATED
		System.setProperty("org.graphstream.ui.renderer",
				"org.graphstream.ui.j2dviewer.J2DGraphRenderer");

		MultiGraph mg = new MultiGraph("monGraphe", true, false);

		Node a = mg.addNode("A");
		a.setAttribute("xyz", -1, -1, 0);
		Node b = mg.addNode("B");
		b.setAttribute("xyz", 0, 1, 0);
		Node c = mg.addNode("C");
		c.setAttribute("xyz", 1, -1, 0);

		mg.addEdge("AB", "A", "B");
		mg.addEdge("AC", "A", "C");
		mg.addEdge("BC", "B", "C");

		SpriteManager manager = new SpriteManager(mg);
		Sprite s = manager.addSprite("sprite");
		s.attachToEdge("AB");
		s.setPosition(Units.GU, 0.5, -0.5, 0);

		Viewer v = new Viewer(mg, ThreadingModel.GRAPH_IN_ANOTHER_THREAD);
		View vue = new DefaultView(v, "vue", Viewer.newGraphRenderer());
		v.addView(vue);

		JFrame jframe = new JFrame("Test pour les sprites");
		jframe.setSize(300, 300);

		jframe.getContentPane().add(vue, BorderLayout.CENTER);
		jframe.setVisible(true);

		Scanner scan = new Scanner(System.in);
		scan.nextLine();

		Sprite s1 = manager.addSprite("sprite2");
		s1.attachToEdge("AC");
		s1.setPosition(Units.GU, 0.5, -0.5, 0);
		double length = 1.5;
		double width = 0.5;
		String style = "shape: box; fill-mode: plain; size-mode: normal; size: "
				+ length
				+ "gu,"
				+ width
				+ "gu; fill-color: rgb(0,150,0); z-index: 50;";
		s1.setAttribute("ui.style", style);
		scan.close();
	}

}
