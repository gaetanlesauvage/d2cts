package org.util.dbLoading;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;


public class DatabaseIDsRetriever {
	private Map<String,Integer> crossroadTypes;
	private Map<String,Integer> roadTypes;
	private Map<String,Integer> blockTypes;
	private Map<String,Integer> seaOrientations;
	private Map<Double,Integer> containerTypes;
	private Map<String,Integer> alignments;
	private Map<Integer,String> availability;
	private Map<String,Integer> missionTypes;
	private Map<Integer,String> loadStates;
	private Map<Integer,String> loadPhases;
	
	public DatabaseIDsRetriever (Connection c) throws SQLException {
		this.crossroadTypes = new HashMap<>();
		this.roadTypes = new HashMap<>();
		this.blockTypes = new HashMap<>();
		this.seaOrientations = new HashMap<>();
		this.containerTypes = new HashMap<>();
		this.alignments = new HashMap<>();
		this.availability = new HashMap<>();
		this.missionTypes = new HashMap<>();
		this.loadStates = new HashMap<>();
		this.loadPhases = new HashMap<>();
		load(c);
	}
	
	private void load(Connection c) throws SQLException {
		Statement statement = null;
		statement = c.createStatement();
		ResultSet rs = null;
		
		String query = "SELECT CLASS_NAME, ID FROM CROSSROAD_TYPE";
		rs = statement.executeQuery(query);
		while(rs.next()){
			crossroadTypes.put(rs.getString("CLASS_NAME"), rs.getInt("ID"));
		}
		rs.close();
		
		query = "SELECT CLASS_NAME, ID FROM ROAD_TYPE";
		rs = statement.executeQuery(query);
		while(rs.next()){
			roadTypes.put(rs.getString("CLASS_NAME"), rs.getInt("ID"));
		}
		rs.close();
		
		query = "SELECT NAME, ID FROM BLOCK_TYPE";
		rs = statement.executeQuery(query);
		while(rs.next()){
			String blockName = rs.getString("NAME");
			Integer blockID = rs.getInt("ID");
			blockTypes.put(blockName, blockID);
		}
		rs.close();
		
		query = "SELECT NAME, ID FROM SEA_ORIENTATION";
		rs = statement.executeQuery(query);
		while(rs.next()){
			seaOrientations.put(rs.getString("NAME"), rs.getInt("ID"));
		}
		rs.close();
		
		query = "SELECT TEU, ID FROM CONTAINER_TYPE";
		rs = statement.executeQuery(query);
		while(rs.next()){
			containerTypes.put(rs.getDouble("TEU"), rs.getInt("ID"));
		}
		rs.close();
		
		query = "SELECT LABEL, ID FROM ALIGNMENT";
		rs = statement.executeQuery(query);
		while(rs.next()){
			alignments.put(rs.getString("LABEL"), rs.getInt("ID"));
		}
		rs.close();
		
		query = "SELECT LABEL, ID FROM AVAILABILITY";
		rs = statement.executeQuery(query);
		while(rs.next()){
			availability.put(rs.getInt("ID"), rs.getString("LABEL"));
		}
		rs.close();
		
		query = "SELECT ENUM_NAME, ID FROM MISSION_TYPE";
		rs = statement.executeQuery(query);
		while(rs.next()){
			missionTypes.put(rs.getString("ENUM_NAME"), rs.getInt("ID"));
		}
		rs.close();
		
		query = "SELECT CLASS_NAME, ID FROM LOAD_STATE";
		rs = statement.executeQuery(query);
		while(rs.next()){
			loadStates.put(rs.getInt("ID"),rs.getString("CLASS_NAME"));
		}
		rs.close();
		
		query = "SELECT CLASS_NAME, ID FROM LOAD_PHASE";
		rs = statement.executeQuery(query);
		while(rs.next()){
			loadPhases.put(rs.getInt("ID"),rs.getString("CLASS_NAME"));
		}
		rs.close();
		
		statement.close();
	}
	
	public Integer getCrossroadTypeID(String type){
		return crossroadTypes.get(type);
	}
	
	public Integer getRoadTypeID(String type){
		return roadTypes.get(type);
	}
	
	//FIXME CHANGE DATABASE TO ADD TYPE NAME
	public Integer getBlockTypeID(String type){
		return blockTypes.get(type);
	}
	
	public Integer getSeaOrientation(String orientation){
		return seaOrientations.get(orientation);
	}
	
	public Integer getContainerTypeID(Double teu){
		return containerTypes.get(teu);
	}
	
	public Integer getAlignment(String alignment){
		return alignments.get(alignment);
	}
	
	public String getAvailabilityLabel(Integer ID) {
		return availability.get(ID);
	}
	
	public Integer getMissionTypeID(String type){
		return missionTypes.get(type);
	}
	
	public String getLoadState(Integer ID) {
		return loadStates.get(ID);
	}
	
	public String getLoadPhase(Integer ID) {
		return loadPhases.get(ID);
	}
}
