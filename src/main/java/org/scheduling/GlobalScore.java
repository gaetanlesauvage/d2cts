package org.scheduling;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JFrame;
import javax.swing.JTree;

import org.time.Time;
import org.time.TimeScheduler;


public class GlobalScore implements Comparable<GlobalScore> {
	private Map<String, LocalScore> localScores;

	private double distance;
	private double distanceInSeconds;
	private double overspentTime;
	private int overspentTW;
	private double waitTime;

	public GlobalScore (){
		localScores = new HashMap<>();
		reset();
	}

	public GlobalScore (GlobalScore toCopy){
		this();
		distance = toCopy.distance;
		distanceInSeconds = toCopy.distanceInSeconds;
		overspentTime = toCopy.overspentTime;
		waitTime = toCopy.waitTime;
		overspentTW = toCopy.overspentTW;
		
		for(String k : toCopy.localScores.keySet()){
			localScores.put(k, new LocalScore(toCopy.localScores.get(k)));
		}
	}

	public void reset(){
		if(localScores!=null){
			localScores.clear();
		}
		distance = 0;
		distanceInSeconds = 0;
		overspentTime = 0;
		waitTime = 0;

	}

	public void addScore(LocalScore localScore){
		boolean b = false;
		//		if(localScores.containsKey(localScore.getResource().getID())){
		//			b = true;
		//			new Exception("LOCAL SCORE ALREADY PRESENT !").printStackTrace();
		//			System.out.println("BEFORE : "+this+"\n=> "+this.getSolutionString());
		//			LocalScore oldLocal = localScores.remove(localScore.getResource().getID());
		//			distance -= oldLocal.getDistance();
		//			distanceInSeconds -= oldLocal.getTravelTime();
		//			overspentTime -= oldLocal.getLateness();
		//			waitTime -= oldLocal.getEarliness();
		//			
		//		}
		localScores.put(localScore.getResource().getID(), localScore);
		distance = 0;
		distanceInSeconds = 0;
		overspentTime = 0;
		waitTime = 0;
		overspentTW = 0;
		for(LocalScore ls : localScores.values()){
			distance += ls.getDistance();
			distanceInSeconds += ls.getTravelTime();
			overspentTime += ls.getLateness();
			waitTime += ls.getEarliness();
			overspentTW += ls.getOverspentTW();
		}

		/*distance += localScore.getDistance();
		distanceInSeconds += localScore.getTravelTime();
		overspentTime += localScore.getLateness();
		waitTime += localScore.getEarliness();
		 */
		if(b) {
			System.out.println("AFTER : "+this+"\n=> "+this.getSolutionString()+"\n");
			new java.util.Scanner(System.in).nextLine();
		}
	}

	public double getDistance(){

		return distance;
	}

	public double getDistanceInSeconds(){
		return distanceInSeconds;
	}

	public double getOverspentTime(){
		return overspentTime;
	}

	public double getWaitTime(){
		return waitTime;
	}

	public Map<String, LocalScore> getSolution(){
		return localScores;
	}

	public double getScore(){
		MissionSchedulerEvalParameters evalParams = MissionScheduler.getEvalParameters();
		return distanceInSeconds*evalParams.getTravelTimeCoeff()+overspentTime*evalParams.getLatenessCoeff()+waitTime*evalParams.getEarlinessCoeff();
	}

	public int getOverspentTW() {
		return overspentTW;
	}
	
	public String toString(){

		StringBuilder sb = new StringBuilder();
		double score = getScore();
		sb.append("d="+distance+"m\tt(d)="+new Time(distanceInSeconds)+"\toverTime="+new Time(overspentTime)+"\twaitTime="+new Time(waitTime)+"\tscore="+score+" ("+new Time(score)+") :\n");
		for(LocalScore local : localScores.values()){
			sb.append("\t"+local+"\n");
		}
		return sb.toString();
	}

	/*public int compareTo (TSPGlobalScore score){
		//System.out.println("COMPARE "+this+" with "+score);
		if(this.overspentTime < score.overspentTime) return -1;
		else if(this.overspentTime > score.overspentTime) return 1;
		else if(this.distance < score.distance) return -1;
		else if(this.distance > score.distance) return 1;
		else return 0;
	}*/

	/**
	 * COMPARATOR : returns -1 if current score is less than the given score, 0 if they are equals, or 1 otherwise.
	 * @return -1 if current score is less than the given score, 0 if they are equals, or 1 otherwise.
	 */
	public int compareTo (GlobalScore score){
		//System.out.println("COMPARE "+this+" with "+score);
		double F = getScore();
		double score_F = score.getScore();
		if(F < score_F) return -1; 
		else if(F > score_F) return 1;
		else return 0;
	}

	public String getSolutionString() {
		StringBuilder sb = new StringBuilder();
		boolean first = true;
		for(String hill : localScores.keySet()){
			if(first){
				first = false;
			} else {
				sb.append("\n");
			}
			sb.append(hill+" : "+localScores.get(hill).getPathString());
		}
		return sb.toString();
	}

	public int getTaskCount(){
		int count = 0;
		for(LocalScore l : localScores.values()){
			count += l.getTaskCount();
		}
		return count;
	}

	public JTree getGUISolution () {
		JTree t = new JTree(localScores.values().toArray(new LocalScore[localScores.size()]));
		return t;
	}
	
	public void displayScore () {
		JFrame jf = new JFrame("GS at "+TimeScheduler.getInstance().getTime());
		jf.add(getGUISolution(),BorderLayout.CENTER);
		jf.setSize(new Dimension(400, 400));
		jf.setVisible(true);
	}
}