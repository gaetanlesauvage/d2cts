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
import org.com.model.SeaOrientationBean;
import org.system.container_stocking.SeaOrientation;

public class SeaOrientationDAO implements D2ctsDao<SeaOrientationBean> {
	private static final Logger log = Logger.getLogger(SeaOrientationDAO.class);

	private Map<Integer, SeaOrientationBean> orientations;
	private static SeaOrientationDAO instance;

	private static final String LOAD_QUERY = "SELECT ID, NAME, ENUM_NAME FROM SEA_ORIENTATION";
	private PreparedStatement psLoad;

	public static SeaOrientationDAO getInstance() {
		if (instance == null) {
			instance = new SeaOrientationDAO();
			try {
				instance.load();
			} catch (SQLException e) {
				log.fatal(e.getMessage(), e);
			}
		}
		return instance;
	}

	private SeaOrientationDAO() {
		orientations = new HashMap<>(5);
	}

	@Override
	public Iterator<SeaOrientationBean> iterator() {
		if (orientations == null)
			orientations = new HashMap<>();
		return orientations.values().iterator();
	}

	@Override
	public void close() throws SQLException {
		if (psLoad != null) {
			psLoad.close();
		}
		instance = null;
		log.info("SeaOrientationDAO closed.");
	}

	@Override
	public void load() throws SQLException {
		if (psLoad == null) {
			Connection c = DbMgr.getInstance().getConnection();
			psLoad = c.prepareStatement(LOAD_QUERY);
		}
		ResultSet rs = psLoad.executeQuery();
		while (rs.next()) {
			SeaOrientationBean bean = new SeaOrientationBean();
			bean.setId(rs.getInt("ID"));
			bean.setName(rs.getString("NAME"));
			bean.setEnumName(rs.getString("ENUM_NAME"));
			String enumName = bean.getEnumName().substring(0,
					bean.getEnumName().lastIndexOf("."));
			if (!enumName.equals(SeaOrientation.class.getName())) {
				throw new SQLException("SeaOrientation enum name undefined");
			}
			bean.setSeaOrientation(SeaOrientation.getOrientation(bean
					.getName()));
			orientations.put(bean.getId(), bean);
		}
		if (rs != null) {
			rs.close();
		}
	}

	@Override
	public int insert(SeaOrientationBean bean) throws SQLException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public String getLoadQuery() {
		return LOAD_QUERY;
	}

	@Override
	public int size() {
		return orientations.size();
	}

	public SeaOrientation getSeaOrientation(Integer id) {
		return orientations.get(id).getSeaOrientation();
	}

}
