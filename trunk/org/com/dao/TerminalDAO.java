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
import org.com.model.TerminalBean;

public class TerminalDAO implements D2ctsDao<TerminalBean>{
	private static final Logger log = Logger.getLogger(TerminalDAO.class);
	
	private static TerminalDAO instance;

	private Map<Integer, TerminalBean> beans;

	private PreparedStatement loadAllStatement;
	private PreparedStatement loadTerminalStatement;

	private static final String LOAD_ALL_QUERY = "SELECT ID, DATE_REC, NAME, FILE, LABEL FROM TERMINAL";
	private static final String LOAD_TERMINAL_QUERY = "SELECT ID, DATE_REC, NAME, FILE, LABEL FROM TERMINAL WHERE ID = ?";

	public static TerminalDAO getInstance(){
		if(instance == null){
			instance = new TerminalDAO();
		}
		return instance;
	}

	private TerminalDAO(){

	}

	@Override
	public Iterator<TerminalBean> iterator() {
		if(beans == null) beans = new HashMap<>();
		return beans.values().iterator();
	}

	@Override
	public void close() throws SQLException {
		if(loadAllStatement != null){
			loadAllStatement.close();
		}
		if(loadTerminalStatement!=null){
			loadTerminalStatement.close();
		}
		log.info("TerminalDAO closed.");
	}

	@Override
	public void load() throws SQLException {
		beans = new HashMap<>();
		if(loadAllStatement == null){
			Connection c = DbMgr.getInstance().getConnection();
			loadAllStatement = c.prepareStatement(LOAD_ALL_QUERY);
		}
		ResultSet rs = loadAllStatement.executeQuery();
		while(rs.next()){
			TerminalBean bean = new TerminalBean();
			bean.setId(rs.getInt("ID"));
			bean.setDate_rec(rs.getTimestamp("DATE_REC"));
			bean.setName(rs.getString("NAME"));
			bean.setFile(rs.getString("FILE"));
			bean.setLabel(rs.getString("LABEL"));
			beans.put(bean.getId(), bean);
		}
		if(rs != null){
			rs.close();
		}
	}

	public TerminalBean getTerminal(Integer id) throws SQLException{
		if(beans != null && beans.containsKey(id)) return beans.get(id);
		else {
			if(loadTerminalStatement == null){
				Connection c = DbMgr.getInstance().getConnection();
				loadTerminalStatement = c.prepareStatement(LOAD_TERMINAL_QUERY);
			}
			loadTerminalStatement.setInt(1, id);
			
			ResultSet rs = loadTerminalStatement.executeQuery();
			TerminalBean bean = null;
			if(rs.next()){
				bean = new TerminalBean();
				bean.setId(rs.getInt("ID"));
				bean.setId(rs.getInt("ID"));
				bean.setDate_rec(rs.getTimestamp("DATE_REC"));
				bean.setName(rs.getString("NAME"));
				bean.setFile(rs.getString("FILE"));
				bean.setLabel(rs.getString("LABEL"));
				if(beans==null) beans = new HashMap<>();
				beans.put(bean.getId(), bean);
			} else {
				log.error("Cannot find terminal "+id+"!");
			}
			if(rs!=null){
				rs.close();
			}
			return bean;
		}
	}
	
	@Override
	public int insert(TerminalBean bean) throws SQLException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public String getLoadQuery() {
		return TerminalDAO.LOAD_ALL_QUERY;
	}
	
	@Override
	public int size(){
		return beans.size();
	}
}