package org.com;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.com.dao.DAOMgr;
import org.util.dbLoading.DBUtils;
import org.util.dbLoading.DatabaseIDsRetriever;

public class DbMgr {
	private static final Logger logger = Logger.getLogger(DbMgr.class);
	
	public static DbMgr instance;

	private DatabaseIDsRetriever dbIdRetriever;
	
	private Connection connection;

	private DbMgr () throws SQLException {
		Properties p = new Properties();
		try {
			URL u = this.getClass().getResource("/conf/database.properties");
			p.load(new FileReader(new File(u.toURI())));
		} catch (FileNotFoundException e) {
			logger.fatal(e.getMessage(), e);			
		} catch (IOException e) {
			logger.fatal(e.getMessage(), e);
		} catch (URISyntaxException e) {
			logger.fatal(e.getMessage(), e);
		}
		this.connection = DBUtils.getConnection(p);
		this.dbIdRetriever = new DatabaseIDsRetriever(this.connection);  
	}

	public static DbMgr getInstance(){
		if(instance == null) {
			try{
				instance = new DbMgr();
			} catch(SQLException e){
				logger.fatal(e.getMessage(), e);
			}
		}
		return instance;
	}

	public final DatabaseIDsRetriever getDatabaseIDsRetriever(){
		return this.dbIdRetriever;
	}
	public final Connection getConnection() {
		return this.connection; 
	}

	public void commitAndClose() throws SQLException {
		DAOMgr.closeAllDAO();
		if(this.connection!=null){
			long duration = System.currentTimeMillis();
			connection.commit();
			duration = System.currentTimeMillis()-duration;
			logger.info("Commit done in "+duration+" ms");
			this.connection.close();
		}
		logger.info("Database connection closed.");
		
	}

	public void rollbackAndClose() throws SQLException{
		DAOMgr.closeAllDAO();		
		if(this.connection!=null){
			long duration = System.currentTimeMillis();
			this.connection.rollback();
			duration = System.currentTimeMillis()-duration;
			logger.info("Rollback done in "+duration+" ms");
			this.connection.close();
		}
		logger.info("Database connection closed.");		
	}  
}
