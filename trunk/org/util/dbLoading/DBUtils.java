package org.util.dbLoading;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class DBUtils {
	private static Driver driver;
	
	public static Connection getConnection(Properties p) throws SQLException{
		Connection c = null;
		
		if(driver == null){
			 driver = new com.mysql.jdbc.Driver();
			 DriverManager.registerDriver(driver);
		}
		
		String url = p.getProperty("dbURL");
		String user= p.getProperty("dbUser");
		String password = p.getProperty("dbPassword");
		
		
		if(url!=null&&user!=null&&password!=null){
			c = DriverManager.getConnection(url, user, password);
			c.setAutoCommit(false); //TODO set in properties file
		}
		else{
			throw new SQLException("URL and/or USER and/or PASSWORD missing!");
		}
		return c;
	}

}
