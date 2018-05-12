package org.time.event;

import org.system.Terminal;
import org.time.Time;
import org.time.TimeScheduler;


public class LaserHeadFailure extends DynamicEvent {
	private String lhID;
	private double range;
	private Time duration;

	private static final String TYPE = "LaserHeadFailure";

	public LaserHeadFailure (Time execTime, String lhID, double range, Time duration){
		super(execTime, TYPE);
		this.lhID = lhID;
		this.range = range;
		this.duration = duration;
	}
	@Override
	public void execute() {
		Terminal.getInstance().updateLaserHeadRange(lhID,range);
		writeEventInDb();
		if(duration!=null) {
			LaserHeadFailure lhf = new LaserHeadFailure(new Time(super.time, duration), lhID, 1.0, null);
			TimeScheduler.getInstance().registerDynamicEvent(lhf);
		}
	}

	public static String getEventType(){ 
		return TYPE;
	}

}
