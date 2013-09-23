package org.scheduling;

import java.util.ArrayList;
import java.util.List;

import org.com.model.scheduling.ParameterBean;
import org.com.model.scheduling.ParameterType;


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
	
	public void setT(double t){
		this.travelTimeCoeff = t;
	}
	
	public void setL(double l){
		this.latenessCoeff = l;
	}
	
	public void setE(double e){
		this.earlinessCoeff = e;
	}
	
	public List<ParameterBean> getParameters(){
		List<ParameterBean> list = new ArrayList<>(3);
		ParameterBean t = new ParameterBean("T", ParameterType.DOUBLE);
		t.setValue(new Double(travelTimeCoeff));
		ParameterBean l = new ParameterBean("L", ParameterType.DOUBLE);
		l.setValue(new Double(latenessCoeff));
		ParameterBean e = new ParameterBean("E", ParameterType.DOUBLE);
		e.setValue(new Double(earlinessCoeff));
		list.add(t);
		list.add(l);
		list.add(e);
		return list;
	}
}
