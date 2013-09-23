package org.scheduling.offlineACO;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;

import org.scheduling.GlobalScore;
import org.time.Time;


public class OfflineSchedulerStats {
	private OfflineSchedulerParameters parameters;
	private ArrayList<GlobalScore> scores;
	private double sumDistance;
	private double sumOverspentTime;
	private double sumScore;
	
	public OfflineSchedulerStats (OfflineSchedulerParameters params){
		this.parameters = params;
		scores = new ArrayList<GlobalScore>();
		sumDistance = 0.0;
		sumOverspentTime = 0.0;
		sumScore = 0.0;
	}
	
	public void addScore(GlobalScore score){
		scores.add(score);
		sumDistance+=score.getDistance();
		sumOverspentTime+=score.getOverspentTime();
		sumScore+=score.getScore();
	}
	
	public double getAVG_Distance(){
		if(scores.size() > 0) return sumDistance / (0.0 + scores.size());
		else return 0.0; 
	}
	
	public double getAVG_OverspentTime(){
		if(scores.size() > 0) return sumOverspentTime / (0.0 + scores.size());
		else return 0.0;
	}
	
	public double getAVG_Score(){
		if(scores.size() > 0) return sumScore / (0.0 + scores.size());
		else return 0.0;
	}
	
	public void exportGNUPlot(String fileName){
		File f = new File(fileName);
		try {
			PrintWriter writer = new PrintWriter(f);
			writer.append("#PARAMS : "+parameters+"\n");
			writer.append("#STEP DISTANCE OVERSPENT_TIME SCORE\n");
			for(int i=0; i<scores.size(); i++){
				GlobalScore score = scores.get(i);
				writer.append(i+" "+score.getDistance()+" "+new Time(score.getOverspentTime())+" "+score.getScore()+"\n");
			}
			writer.flush();
			writer.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
	}

}
