package org.com.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.log4j.Logger;
import org.com.DbMgr;
import org.com.model.ScenarioBean;

public class ScenarioDAO implements D2ctsDao<ScenarioBean>{
	private static final Logger log = Logger.getLogger(ScenarioDAO.class);
	
	private static final String LOAD_QUERY = "SELECT ID, NAME, DATE_REC, TERMINAL, FILE FROM SCENARIO";
	private static ScenarioDAO instance;
	
	Map<Integer, ScenarioBean> beans;
	
	PreparedStatement scenariosStatement;
	
	public static final ScenarioDAO getInstance(){
		if(instance == null){
			instance = new ScenarioDAO();
		}
		return instance;
	}
	
	private  ScenarioDAO(){
		beans = new HashMap<>();
		try{
			load();
		} catch (SQLException e){
			log.fatal(e.getMessage(),e);
		}
	}
	
	@Override
	public void close() throws SQLException{
		if(scenariosStatement != null){
			scenariosStatement.close();
		}
		instance = null;
		log.info("ScenarioDAO closed.");
	}
	
	@Override
	public int size(){
		return beans.size();
	}
	
	@Override
	public void load() throws SQLException{
		Connection c = DbMgr.getInstance().getConnection();
		scenariosStatement = c.prepareStatement(getLoadQuery());
		ResultSet rs = scenariosStatement.executeQuery();
		while(rs.next()){
			ScenarioBean bean = new ScenarioBean();
			bean.setId(rs.getInt("ID"));
			bean.setName(rs.getString("NAME"));
			bean.setDate_rec(rs.getDate("DATE_REC"));
			bean.setTerminal(rs.getInt("TERMINAL"));
			bean.setFile(rs.getString("FILE"));
			
			beans.put(bean.getId(), bean);
		}
		if(rs != null){
			rs.close();
		}
	}
	
	@Override
	public String getLoadQuery(){
		return ScenarioDAO.LOAD_QUERY;
	}

	@Override
	public Iterator<ScenarioBean> iterator() {
		return beans.values().iterator();
	}

	public String[] getColumnsName(){
		return new String[]{"ID","NAME","DATE_REC","FILE"};
	}
	
	
	@Override
	public int insert(ScenarioBean bean) throws SQLException {
		//TODO
		return 0;
	}

	public ScenarioBean getScenario(Integer id) {
		return beans.get(id);
	}
}
