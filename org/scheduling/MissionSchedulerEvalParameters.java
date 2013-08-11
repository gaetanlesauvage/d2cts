package org.scheduling;

public class MissionSchedulerEvalParameters {
	double travelTimeCoeff;
	double latenessCoeff;
	double earlinessCoeff;
	
	public MissionSchedulerEvalParameters(double travelTimeCoeff, double latenessCoeff, double earlinessCoeff){
		this.travelTimeCoeff = travelTimeCoeff;
		this.latenessCoeff = latenessCoeff;
		this.earlinessCoeff = earlinessCoeff;
	}
	
	public double getTravelTimeCoeff(){
		return travelTimeCoeff;
	}
	
	public double getLatenessCoeff(){
		return latenessCoeff;
	}
	
	public double getEarlinessCoeff(){
		return earlinessCoeff;
	}
	
	public double T() {
		return travelTimeCoeff;
	}
	
	public double L() {
		return latenessCoeff;
	}
	
	public double E() {
		return earlinessCoeff;
	}
	
	public String toString(){
		return "t="+travelTimeCoeff+" l="+latenessCoeff+" e="+earlinessCoeff;
	}
	
	public static String[] names () {
		return new String[]{"T","L","E"};
	}
}
