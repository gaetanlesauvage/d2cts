package org.vehicles.models;

import java.io.PrintWriter;
import java.io.Serializable;

import org.system.Terminal;

public class RandomSpeed implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 7294694160531365847L;
	private static PrintWriter pw;
	private static int nb = 0;
	private String id;
	private double min;
	private double max;
	
	private String unitDescription;

	public RandomSpeed (double min, double max, String unitDescription){
		this.min = min;
		this.max = max;
		this.unitDescription = unitDescription;
		id = "randomSpeed_"+nb++;
	}
	
	public RandomSpeed (double min, double max){
		this(min, max, "");
	}
	
	/*public static void setRandomGenerator(Random r){
		random = r;
		try {
			File f = new File("randomStack.dat");
			try {
				f.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
			pw = new PrintWriter(f);
			
			
		
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}*/
	
	public double getMin(){
		return min;
	}
	
	public double getMax(){
		return max;
	}
	
	public String getUnitDescription(){
		return unitDescription;
	}
	
	public double getAValue(){
		double value = min + Terminal.getInstance().getRandom().nextDouble()*(max-min);
		if(pw!=null){
			pw.append(id+"> "+value+"\n");
			pw.flush();
		}
		return value;
	}
	
	public static void close(){
		if(pw!=null){
			pw.flush();
			pw.close();
		}
		
	}
	
	public static void main(String [] args){
		RandomSpeed rs = new RandomSpeed(20, 60, "seconds");
		
		int[] counter = new int[60-20+1];
		
		for(int i=0; i<1000 ; i++){
			double v = rs.getAValue();
			counter[(int)Math.round(v)-20]++;
		}
		
		for(int i=0 ; i<counter.length; i++){
			System.out.println((i+20)+" "+counter[i]);
		}
	}
}
