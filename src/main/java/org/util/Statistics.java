package org.util;

import java.io.IOException;
import java.util.HashMap;

import org.conf.parameters.ReturnCodes;
import org.missions.Mission;
import org.time.Time;
import org.time.TimeWindow;
import org.util.parsers.XMLMissionsParser;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

public class Statistics {
	/**
	 * Number of missions not known at the beginning of the simulation / Total
	 * number of missions
	 * 
	 * @param missions
	 * @return
	 */
	public static double dod(HashMap<Mission, Time> missions) {
		double dod = 0;

		int dynCount = 0;
		Time tZero = new Time(0);

		for (Mission m : missions.keySet()) {
			if (missions.get(m).compareTo(tZero) > 0) {
				dynCount++;
			}
		}
		// System.err.println("Dyn = "+dynCount+" Tot = "+missions.size());
		dod = dynCount / (missions.size() + 0.0);
		return dod;
	}

	/*
	 * private static Time getMaxTime(Set<Mission> l){ Time max = new Time(0);
	 * for(Mission m : l) if(m.getPickupTimeWindow().getMin().compareTo(max)>0)
	 * max = m.getPickupTimeWindow().getMin();
	 * 
	 * return max; }
	 */

	/*
	 * public static double edod(HashMap<Mission, Time> missions) { double edod
	 * = 0;
	 * 
	 * Time max = getMaxTime(missions.keySet()); long T = max.toStep();
	 * 
	 * 
	 * for(Mission m : missions.keySet()){ edod += 1 -
	 * ((0.0+m.getPickupTimeWindow
	 * ().getMin().toStep()-missions.get(m).toStep())/(0.0+T)); }
	 * 
	 * edod = (1/(missions.size()+0.0))*edod;
	 * 
	 * return edod; }
	 */
	public static double edod(HashMap<Mission, Time> missions) {
		double edod = 0;
		int nCount = 0;
		for (Mission m : missions.keySet()) {
			long T = m.getPickupTimeWindow().getMin().toStep();

			long tEvt = missions.get(m).toStep();
			if (tEvt > 0) {
				double d = 1 - ((0.0 + (T - tEvt)) / (0.0 + T));
				System.out.println("d = " + d);
				edod += d;
				nCount++;
			}
		}
		if (nCount > 0)
			edod = (1 / (nCount + 0.0)) * edod;
		System.out.println("edod = " + edod);
		return edod;
	}

	/*
	 * public static double edod3(HashMap<Mission, Time> missions) { double edod
	 * = 0;
	 * 
	 * 
	 * 
	 * 
	 * for(Mission m : missions.keySet()){ long T =
	 * m.getPickupTimeWindow().getMin().toStep(); double ri double d = 1 -
	 * ((0.0+
	 * m.getPickupTimeWindow().getMin().toStep()-missions.get(m).toStep())/
	 * (0.0+T)); edod += d; System.err.println("edod += "+d+" edod = "+edod); }
	 * 
	 * edod = (1/(missions.size()+0.0))*edod;
	 * 
	 * return edod; }
	 */

	public static void main(String[] args) throws SAXException, IOException {
		// String[] files = { "xml/generatedData/stocksFile.xml" };
		// String[] files = { "xml/generatedData/stocksFile.xml" ,
		// "xml/testData/15_stock_missions.xml" };
		// String[] files = { "xml/generatedData/stocksFile.xml" ,
		// "xml/generatedData/trucksFile.xml",
		// "xml/generatedData/trainFile.xml", "xml/generatedData/shipFile.xml"
		// };
		// String[] files = {
		// "xml/testData/dynamic/100missions/stocksFile_edod1.xml"};// ,
		// "xml/testData/dynamic/20missions/trucksFile.xml"};
		String[] files = { "xml/results/10missions/missions_1_0,5_1.xml" };

		HashMap<Mission, Time> mapGlobal = new HashMap<Mission, Time>();
		for (String f : files) {
			XMLReader saxReader = XMLReaderFactory
					.createXMLReader("com.sun.org.apache.xerces.internal.parsers.SAXParser");
			XMLMissionsParser parser = new XMLMissionsParser();
			saxReader.setContentHandler(parser);

			saxReader.parse(new InputSource(Statistics.class
					.getResourceAsStream("/" + f)));

			HashMap<Mission, Time> map = parser.getMap();
			System.err.println("Missions count : " + map.size());

			for (Mission m : map.keySet()) {
				mapGlobal.put(
						new Mission(m.getId(),
								m.getMissionKind().getIntValue(),
								new TimeWindow(new Time(m.getPickupTimeWindow()
										.getMin()), new Time(m
										.getPickupTimeWindow().getMax())),
								new TimeWindow(new Time(m
										.getDeliveryTimeWindow().getMin()),
										new Time(m.getDeliveryTimeWindow()
												.getMax())),
								m.getContainerId(), m.getDestination()),
						new Time(map.get(m)));
			}

			double dod = Statistics.dod(map);
			System.err.println("Missions count : " + map.size());
			double edod = Statistics.edod(map);
			// double edod2 = Statistics.edod2(map);
			System.out
					.println(f + " : \n\tDOD = " + dod + "\n\tEDOD = " + edod);
		}
		System.out
				.println("========================================================");
		double dod = Statistics.dod(mapGlobal);
		double edod = Statistics.edod(mapGlobal);
		System.out.println("GLOBAL : \n\tDOD = " + dod + "\n\tEDOD = " + edod);
		System.exit(ReturnCodes.EXIT_ON_SUCCESS.getCode());
	}
}
