package org.time.event;

import org.time.Time;

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
		if (!terminal.shipCanLoadContainer(destinationSlotID, containerId)) {
			if (!alreadyAdviced) {
				System.out.println(msg());
				alreadyAdviced = true;
			}
			scheduler.registerDynamicEvent(this);
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
