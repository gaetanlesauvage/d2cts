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
import org.com.model.BlockTypeBean;
import org.system.container_stocking.BlockType;


public class BlockTypeDAO implements D2ctsDao<BlockTypeBean>{
	private static final Logger log = Logger.getLogger(BlockTypeDAO.class);

	private Map<Integer,BlockTypeBean> types;
	private static BlockTypeDAO instance;

	private static final String LOAD_QUERY = "SELECT ID, NAME, ENUM_NAME FROM BLOCK_TYPE";
	private PreparedStatement psLoad;

	public static BlockTypeDAO getInstance(){
		if(instance == null) {
			instance = new BlockTypeDAO();
			try{
				instance.load();
			}
			catch(SQLException e){
				log.fatal(e.getMessage(),e);
			}
		}
		return instance;
	}

	private BlockTypeDAO(){
		types = new HashMap<>(5);
	}

	@Override
	public Iterator<BlockTypeBean> iterator() {
		if(types == null) types = new HashMap<>();
		return types.values().iterator();
	}

	@Override
	public void close() throws SQLException {
		if(psLoad != null){
			psLoad.close();
		}
		instance = null;
	}

	@Override
	public void load() throws SQLException {
		if(psLoad == null){
			Connection c = DbMgr.getInstance().getConnection();
			psLoad = c.prepareStatement(LOAD_QUERY);
		}
		ResultSet rs = psLoad.executeQuery();
		while(rs.next()){
			BlockTypeBean bean = new BlockTypeBean();
			bean.setId(rs.getInt("ID"));
			bean.setName(rs.getString("NAME"));
			bean.setEnumName(rs.getString("ENUM_NAME"));
			String enumName = bean.getEnumName().substring(0, bean.getEnumName().lastIndexOf("."));
			if(!enumName.equals(BlockType.class.getName())){
				throw new SQLException("BlockType enum name undefined");
			}
			bean.setBlockType(BlockType.getType(bean.getEnumName().substring(bean.getEnumName().lastIndexOf('.')+1)));
			types.put(bean.getId(), bean);
		}
		if(rs != null){
			rs.close();
		}
	}

	@Override
	public int insert(BlockTypeBean bean) throws SQLException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public String getLoadQuery() {
		return LOAD_QUERY;
	}

	@Override
	public int size() {
		return types.size();
	}

	public BlockType getType(Integer id) {
		return types.get(id).getBlockType();
	}


}
