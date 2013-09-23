package org.time.event;

import org.system.Terminal;
import org.time.Time;
import org.time.TimeScheduler;

public class ShipContainerOut extends ContainerOut {
	private String destinationSlotID;
	private boolean alreadyAdviced = false;

	public ShipContainerOut(Time time, String containerId,
			String destinationSlotID) {
		super(time, containerId);
		this.destinationSlotID = destinationSlotID;
	}

	private String msg() {
		String s = "ShipContainerOut can't leave : " + containerId + " at "
				+ destinationSlotID + " time=" + time;
		return s;
	}

	@Override
	public void execute() {
		if (!Terminal.getInstance().shipCanLoadContainer(destinationSlotID, containerId)) {
			if (!alreadyAdviced) {
				System.out.println(msg());
				alreadyAdviced = true;
			}
			TimeScheduler.getInstance().registerDynamicEvent(this);
		} else
			super.execute();
	}

	public void setDestinationSlotID(String destinationSlotID) {
		System.out
				.println("Ship Container Out : destination slot changed from "
						+ this.destinationSlotID + " to " + destinationSlotID);
		this.destinationSlotID = destinationSlotID;
	}
}
