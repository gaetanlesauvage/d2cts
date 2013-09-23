package org.time.event;

import org.system.Terminal;
import org.system.container_stocking.ContainerLocation;
import org.time.Time;
import org.time.TimeScheduler;

public class NewShipContainer extends NewContainer {
	private String shipQuay;
	private double shipBerthFromRate;
	private double shipBerthToRate;

	public NewShipContainer(Time time, String containerId, double teu,
			ContainerLocation location, String shipQuayID,
			double shipBerthFromRate, double shipBerthToRate) {
		super(time, containerId, teu, location);
		this.shipQuay = shipQuayID;
		this.shipBerthFromRate = shipBerthFromRate;
		this.shipBerthToRate = shipBerthToRate;
	}

	@Override
	public void execute() {
		try {
			Terminal.getInstance().addContainer(id, teu, location);
			System.out.println("NewShipContainer : " + id);
			Terminal.getInstance().getShip(shipQuay, shipBerthFromRate, shipBerthToRate)
					.containerUnloaded(id);
			writeEventInDb();
		} catch (Exception e) {
			// System.out.println("Event delayed : "+getType()+" "+location);
			TimeScheduler.getInstance().registerDynamicEvent(this);
		}
	}
}
