package org.missions;

import org.system.container_stocking.ContainerLocation;
import org.time.TimeWindow;

public class TruckMission extends Mission {
	private String truckID;

	public TruckMission(String id, String truckID, int missionKind,
			TimeWindow pickupTW, TimeWindow deliveryTW, String containerId,
			ContainerLocation missionLocation) {
		super(id, missionKind, pickupTW, deliveryTW, containerId,
				missionLocation);
		this.truckID = truckID;
	}

	public String getTruckID() {
		return truckID;
	}
}
