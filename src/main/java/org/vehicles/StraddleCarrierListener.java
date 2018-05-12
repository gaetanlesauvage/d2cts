package org.vehicles;

import org.util.Location;

public interface StraddleCarrierListener {
	/**
	 * Invoked when a straddle carrier move @
	 */
	public void straddleCarrierMoved(String straddleCarrierID,
			Location newLocation);

	/**
	 * Invoked when the status of a straddle carrier has changed @
	 */
	public void straddleCarrierStatusChanged(String straddleCarrierID,
			StraddleCarrierStatus oldStatus, StraddleCarrierStatus newStatus);

	/*
	 * public void straddleCarrierActivityChanged () ; public void
	 * straddleCarrierLoadChanged () ;
	 */
}
