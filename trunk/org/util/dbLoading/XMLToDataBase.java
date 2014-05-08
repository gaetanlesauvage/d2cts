package org.util.dbLoading;

import java.io.FileInputStream;
import java.sql.Connection;
import java.sql.SQLException;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.com.DbMgr;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

public class XMLToDataBase {
	private static Logger logger = Logger.getLogger(XMLToDataBase.class);;

	private static String getFileNames(String... files) {
		String returnValue = "";
		for (String s : files) {
			returnValue += s + ", ";
		}
		if (returnValue.length() > 0)
			returnValue = returnValue.substring(0, returnValue.length() - 2);
		return returnValue;
	}

	public static void parse(String... filesToParse) throws Exception {
		Connection connection = null;
		try {

			connection = DbMgr.getInstance().getConnection();

			// System.out.println(logger.getAppender("log4j.appender.LogDefault").getName());
			logger.trace("---------------------------------------------------------------");
			logger.trace("---                   D²CTS XML->DATABASE                   ---");
			logger.trace("---------------------------------------------------------------");
			logger.trace("Connected to database.");

			logger.trace("Parsing XML files " + getFileNames(filesToParse));

			XMLTerminalParser4DB parser = new XMLTerminalParser4DB(connection);// ,
			// terminalFile,
			// vehiclesFile);
			XMLReader saxReader;

			saxReader = XMLReaderFactory.createXMLReader("com.sun.org.apache.xerces.internal.parsers.SAXParser");
			saxReader.setContentHandler(parser);

			for (String f : filesToParse) {
				// Scenario
				InputSource is = null;
				if(!f.startsWith("/"))
					is =  new InputSource(XMLToDataBase.class.getResourceAsStream("/" + f));
				else {
					is =  new InputSource(new FileInputStream(f));
				}
				long duration = System.currentTimeMillis();
				saxReader.parse(is);
				duration = System.currentTimeMillis() - duration;
				logger.info("File " + f + " parsed in " + duration + " ms");
			}

		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e.getMessage(), e);
			try {
				DbMgr.getInstance().rollbackAndClose();
			} catch (SQLException e1) {
				e1.printStackTrace();
				logger.fatal(e1.getMessage(), e1);
			}
			throw e;
		} finally {
			try {
				DbMgr.getInstance().commitAndClose();
			} catch (SQLException e) {
				e.printStackTrace();
				logger.fatal(e.getMessage(), e);
			}

		}
	}

	public static void main(String[] args) {
		PropertyConfigurator.configure(XMLToDataBase.class.getClassLoader().getResource(("conf/log4j.properties")));

		//String[] fileURL = { /*"etc/xml/terminals/tn/TN.terminal",*/ "etc/xml/scenario/vehicles/tn-sc-10.xml"/*, "etc/xml/scheduling/aco.xml" */};
		String[] fileURL = { "etc/xml/scenario/lasersystems/tn-large-range.xml"};
		try {
			parse(fileURL);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
