package org.util;

import java.io.IOException;

import org.graphstream.graph.Graph;
import org.graphstream.graph.implementations.MultiGraph;
import org.graphstream.stream.file.FileSourceDGS;
import org.graphstream.ui.swingViewer.Viewer;

public class GraphReader {
	public static void main(String [] args){
		Graph g = new MultiGraph("g");
		FileSourceDGS fs = new FileSourceDGS();
		fs.addSink(g);
		try{
			fs.readAll("/home/nicoleta/workspace/d2cts/bin/graph-2.dgs");
		}catch(IOException e){
			e.printStackTrace();
		}
		 try {
		      fs.end();
		    } catch( IOException e) {
		      e.printStackTrace();
		    } finally {
		      fs.removeSink(g);
		    }
		   
		   /*DEPRECATED*/// System.setProperty( "gs.ui.renderer","org.graphstream.ui.j2dviewer.J2DGraphRenderer" );
		   Viewer viewer = g.display(true);
		   viewer.addDefaultView(true);
		   
	}
}
