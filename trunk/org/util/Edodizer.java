package org.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.rmi.RemoteException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import org.com.DbMgr;
import org.com.dao.EventDAO;
import org.com.model.EventBean;
import org.com.model.ScenarioBean;
import org.conf.parameters.ReturnCodes;
import org.missions.Mission;
import org.time.Time;
import org.time.TimeScheduler;
import org.time.event.DynamicEvent;
import org.time.event.EventType;
import org.time.event.NewMission;
import org.util.parsers.XMLMissionsParser;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

public class Edodizer {
	
	public static void edodize(ScenarioBean scenario, double dod, double edod, Random r) throws SQLException{
		List<EventBean> init = new ArrayList<>();
		EventDAO dao = EventDAO.getInstance(scenario.getId());
		for(Iterator<EventBean> beanIt = dao.iterator(); beanIt.hasNext(); ) {
			EventBean bean = beanIt.next();
			if(bean.getType() == EventType.NewMission){
				init.add(bean);
			}
		}
		Collections.sort(init);
		int initSize = init.size();
		int i = 0;
		while (init.size() > 0) {
			// Pick a mission
			EventBean bean = init.remove(r.nextInt(init.size()));
			NewMission missionEvent = (NewMission)DynamicEvent.create(bean);
			if (i < (1 - dod) * initSize) {
				bean.setTime(new Time(0));
			} else {
				Time oT = missionEvent.getMission().getPickupTimeWindow().getMin();
				double oldInSec = oT.getInSec();
				double newInSec = oldInSec * edod;
				Time nt = new Time(newInSec);
				bean.setTime(nt);
			}
			dao.update(bean);
			i++;
		}
		DbMgr.getInstance().getConnection().commit();
	}
	
	/**
	 * Set the event dates associated to the missions in the given xml file
	 * according to the defined DOD and EDOD
	 * 
	 * @param file
	 *            XML missions files
	 * @param dod
	 *            0..1
	 * @param edod
	 *            0..1
	 * @throws FileNotFoundException
	 *             @
	 */
	@Deprecated
	public static HashMap<Mission, Time> edodize(HashMap<Mission, Time> map,
			double dod, double edod, Random r) throws FileNotFoundException,
			RemoteException {
		int initSize = map.size();

		HashMap<Mission, Time> newTimes = new HashMap<Mission, Time>();
		ArrayList<Mission> l = new ArrayList<Mission>(map.size());
		for (Mission m : map.keySet()) {
			l.add(m);
		}
		Collections.sort(l);

		int i = 0;
		while (map.size() > 0) {
			// Pick a mission
			Mission m = l.remove(r.nextInt(l.size()));
			map.remove(m);

			if (i < (1 - dod) * initSize) {
				newTimes.put(m, new Time(0));
			} else {
				Time oT = m.getPickupTimeWindow().getMin();
				double oldInSec = oT.getInSec();
				double newInSec = oldInSec * edod;
				Time nt = new Time(newInSec);
				newTimes.put(m, nt);
			}
			i++;
		}
		TimeScheduler.closeInstance();
		
		return newTimes;
	}

	@Deprecated
	public static void write(File file, HashMap<Mission, Time> map)
			throws FileNotFoundException {
		ArrayList<NewMission> l = new ArrayList<NewMission>(map.size());

		if (!file.exists()) {
			file.getParentFile().mkdirs();
		}

		PrintWriter writer = new PrintWriter(file);
		writer.append("<document>\n");
		for (Mission m : map.keySet()) {
			NewMission evt = new NewMission(map.get(m), m);
			l.add(evt);

		}

		displayList(l);

		System.out.println("CMP...");
		Collections.sort(l);
		System.out.println("...CMP");
		displayList(l);
		for (int i = 0; i < l.size(); i++) {
			NewMission evt = l.get(i);
			writer.append(evt.toXML() + "\n");
		}

		writer.append("</document>");
		writer.flush();
		writer.close();
	}

	@Deprecated
	private static <T> void displayList(List<T> l) {
		System.out.println("Liste : ");
		for (int i = 0; i < l.size(); i++) {
			System.out.println(i + ") " + l.get(i));
		}
		System.out.println("---");
	}

	@Deprecated
	public static HashMap<Mission, Time> getMissionMap(String missionFileName)
			throws SAXException, IOException {
		XMLReader saxReader = XMLReaderFactory
				.createXMLReader("com.sun.org.apache.xerces.internal.parsers.SAXParser");
		XMLMissionsParser parser = new XMLMissionsParser();
		saxReader.setContentHandler(parser);
		// File f = new File(missionFileName);
		// System.out.println("MissionFileName : "+missionFileName+" absolutePath : "+f.getAbsolutePath());
		saxReader.parse(new InputSource(Edodizer.class.getResourceAsStream("/"
				+ missionFileName)));
		HashMap<Mission, Time> map = parser.getMap();

		TimeScheduler.closeInstance();
		return map;

	}

	/**
	 * @param args
	 * @throws SAXException
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException, SAXException {
		String fTest = "xml/testData/toEdodize.xml";
		String DTest = "bin/xml/testData/edodized.xml";

		HashMap<Mission, Time> map = getMissionMap(fTest);
		HashMap<Mission, Time> newTimes = Edodizer.edodize(map, 0.5, 0.33,
				new Random());
		Edodizer.write(new File(DTest), newTimes);

		double dod = Statistics.dod(newTimes);
		double edod = Statistics.edod(newTimes);

		System.out
				.println(DTest + " : \n\tDOD = " + dod + "\n\tEDOD = " + edod);

		System.exit(ReturnCodes.EXIT_ON_SUCCESS.getCode());
	}

}
