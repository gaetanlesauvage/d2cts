package org.com.dao;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.log4j.Logger;
import org.com.DbMgr;
import org.com.model.BlockBean;
import org.positioning.Coordinates;

public class BlockDAO implements D2ctsDao<BlockBean> {
	private static final Logger log = Logger.getLogger(BlockDAO.class);

	private static Map<Integer, BlockDAO> instances;

	private static final String LOAD_QUERY = "SELECT b.NAME, b.TERMINAL, b.TYPE, b.SEA_ORIENTATION, BORDER_ROAD FROM BLOCK b WHERE TERMINAL = ?";
	private static final String LOAD_WALLS_QUERY = "SELECT w.BLOCK_NAME, w.TERMINAL, w.WALL_POINT_1, w.WALL_POINT_2 FROM BLOCK_WALL w WHERE TERMINAL = ? AND BLOCK_NAME = ?";
	private static final String LOAD_WALL_POINTS_QUERY = "SELECT p.NAME, p.TERMINAL, p.X, p.Y, p.Z FROM BLOCK_WALL_POINT p WHERE TERMINAL = ? AND NAME = ?";

	private PreparedStatement psLoad;
	private PreparedStatement psLoadWalls;
	private PreparedStatement psLoadWallPoints;

	private Map<String, BlockBean> beans;
	private Integer terminalID;

	private BlockDAO(Integer terminalID) {
		this.terminalID = terminalID;
	}

	public static BlockDAO getInstance(Integer terminalID) {
		if (instances == null) {
			instances = new HashMap<>();
		}
		if (instances.containsKey(terminalID))
			return instances.get(terminalID);
		else {
			BlockDAO instance = new BlockDAO(terminalID);
			instances.put(terminalID, instance);
			return instance;
		}
	}

	public static Iterator<BlockDAO> getInstances() {
		if (instances == null) {
			instances = new HashMap<>();
		}
		return instances.values().iterator();
	}

	@Override
	public Iterator<BlockBean> iterator() {
		if (beans == null)
			beans = new HashMap<>();
		return beans.values().iterator();
	}

	@Override
	public void close() throws SQLException {
		if (psLoad != null) {
			psLoad.close();
		}
		if (psLoadWalls != null) {
			psLoadWalls.close();
		}
		if (psLoadWallPoints != null) {
			psLoadWallPoints.close();
		}
		log.info("BlockDAO of terminal " + terminalID + " closed.");
	}

	@Override
	public void load() throws SQLException {
		if (psLoad == null) {
			psLoad = DbMgr.getInstance().getConnection()
					.prepareStatement(LOAD_QUERY);
		}
		if(psLoadWalls == null){
			psLoadWalls = DbMgr.getInstance().getConnection()
					.prepareStatement(LOAD_WALLS_QUERY);
		}
		if(psLoadWallPoints == null){
			psLoadWallPoints = DbMgr.getInstance().getConnection()
					.prepareStatement(LOAD_WALL_POINTS_QUERY);
		}

		beans = new HashMap<>();

		psLoad.setInt(1, terminalID);
		psLoadWalls.setInt(1, terminalID);
		psLoadWallPoints.setInt(1, terminalID);

		BlockTypeDAO typeDAO = BlockTypeDAO.getInstance();
		SeaOrientationDAO orientationDAO = SeaOrientationDAO.getInstance();

		ResultSet rs = psLoad.executeQuery();
		while (rs.next()) {
			BlockBean bean = new BlockBean();
			bean.setName(rs.getString("NAME"));
			bean.setTerminal(rs.getInt("TERMINAL"));
			Integer so = rs.getInt("SEA_ORIENTATION");
			if(!rs.wasNull()){
				bean.setSeaOrientation(orientationDAO.getSeaOrientation(so));
			}
			bean.setType(typeDAO.getType(rs.getInt("TYPE")));
			bean.setBorder_road(rs.getString("BORDER_ROAD"));

			//Walls
			psLoadWalls.setString(2, bean.getName());
			ResultSet rsWalls = psLoadWalls.executeQuery();
			while(rsWalls.next()){
				String from = rsWalls.getString("WALL_POINT_1");
				String to = rsWalls.getString("WALL_POINT_2");
				String[] wallPoints = {from,to};
				//Wall points
				for(int i=0;i<wallPoints.length; i++){
					psLoadWallPoints.setString(2, wallPoints[i]);
					ResultSet rsWallPoint = psLoadWallPoints.executeQuery();
					if(rsWallPoint.next()){
						Double x = rsWallPoint.getDouble("X");
						Double y = rsWallPoint.getDouble("Y");
						Double z = rsWallPoint.getDouble("Z");
						bean.addPoint(wallPoints[i], new Coordinates(x, y, z));
					}
					rsWallPoint.close();
				}
				bean.addWall(from, to);
			}
			rsWalls.close();

			beans.put(bean.getName(), bean);
		}
		if (rs != null) {
			rs.close();
		}
	}

	@Override
	public int insert(BlockBean bean) throws SQLException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public String getLoadQuery() {
		return LOAD_QUERY;
	}

	@Override
	public int size() {
		return beans.size();
	}
}
