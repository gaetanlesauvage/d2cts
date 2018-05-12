package org.util.dbLoading;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

public class dbLoadingGUI {
	private static final String propertyFile = "xml/defaultData/database.properties";
	
	public dbLoadingGUI () throws FileNotFoundException, IOException {
		Properties p = new Properties();
		p.load(new FileReader(new File(propertyFile)));
	}
}
