package org.missions;

import org.exceptions.ContainerDimensionException;
import org.system.Terminal;
import org.system.container_stocking.Container;
import org.system.container_stocking.ContainerAlignment;
import org.system.container_stocking.ContainerLocation;
import org.system.container_stocking.Slot;
import org.time.TimeScheduler;
import org.time.TimeWindow;
import org.vehicles.Truck;

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

	@Override
	public Container getContainer() {
		Container c = Terminal.getInstance().getContainer(containerId);
		if(c == null){
			try {
				c = new Container(containerId, TimeScheduler.getInstance().getIncomingContainerTeu(containerId));
			} catch (ContainerDimensionException e) {
				e.printStackTrace();
			}
			if(c != null){
				//Slot
				Truck truck = Terminal.getInstance().getTruck(truckID);
				if(truck != null){
					String slotID = truck.getSlotID();
					Slot slot = Terminal.getInstance().getSlot(slotID);
					if(missionKind == MissionKinds.IN || missionKind == MissionKinds.IN_AND_OUT)
						c.move(slot.getLocation());
					else
						c.setContainerLocation(new ContainerLocation(containerId, slot.getPaveId(), slot.getLocation().getRoad().getId(), slot.getId(), 0, ContainerAlignment.center.getValue()));
					
				}

			}
		}
		return c;
	}

}
