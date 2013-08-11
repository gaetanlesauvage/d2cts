package org.scheduling.greedy;

import org.time.Time;

public class ScheduleScore implements Comparable<ScheduleScore>{
	public double distance;
	public Time tardiness;

	public ScheduleScore(){
		distance = 0;
		tardiness = new Time(0);
	}

	public int compareTo(ScheduleScore score){
		if(tardiness.compareTo(score.tardiness) == 0){
			if(distance == score.distance) return 0;
			else if(distance < score.distance) return -1;
			else return 1;
		}
		else{
			return tardiness.compareTo(score.tardiness);
		}
	}
}