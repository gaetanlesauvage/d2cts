/*
 * This file is part of D²CTS : Dynamic and Distributed Container Terminal Simulator.
 *
 * Copyright (C) 2009-2012  Gaëtan Lesauvage
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

package org.util.generators;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.apache.log4j.Logger;
import org.conf.parameters.ReturnCodes;
import org.exceptions.ContainerDimensionException;
import org.exceptions.ContainerNotFoundException;
import org.exceptions.EmptySlotException;
import org.exceptions.NotAccessibleContainerException;
import org.exceptions.container_stocking.delivery.CollisionException;
import org.exceptions.container_stocking.delivery.FallingContainerException;
import org.exceptions.container_stocking.delivery.LevelException;
import org.exceptions.container_stocking.delivery.NotEnoughSpaceException;
import org.exceptions.container_stocking.delivery.SlotContainerIncompatibilityException;
import org.exceptions.container_stocking.delivery.UnstackableContainerException;
import org.system.Terminal;
import org.system.container_stocking.Container;
import org.system.container_stocking.ContainerAlignment;
import org.system.container_stocking.Slot;
import org.time.Time;
import org.time.TimeWindow;

/**
 * This object gathers all the reservations of a slot.
 * 
 * @author Ga&euml;tan Lesauvage
 * @since 2011
 */
class SlotReservations {
	private static final Logger log = Logger.getLogger(SlotReservations.class);

	/**
	 * Concerned slot
	 */
	private Slot slot;
	/**
	 * Schedule of the slot ordered by time
	 */
	private TreeMap<Time, ArrayList<SlotReservation>> schedule;

	/**
	 * Constructor
	 * 
	 * @param slot
	 *            The concerned slot @
	 */
	public SlotReservations(Slot slot) {
		this.slot = slot;
		schedule = new TreeMap<Time, ArrayList<SlotReservation>>();
		/*
		 * for(Level l : slot.getLevels()){ for(String contID :
		 * l.getContainersID()){ Container container =
		 * MissionsFileGenerator.rt.getContainer(contID); ContainerLocation cl =
		 * container.getContainerLocation();
		 * 
		 * TimeWindow tw = new TimeWindow(new Time(0), new Time(Time.MAXTIME));
		 * 
		 * SlotReservation reservation = new SlotReservation(container,
		 * this.slot, tw, cl.getAlign(), slot.getLevel(cl.getLevel()));
		 * addReservation(reservation); } }
		 */
	}

	/**
	 * This method tells if the slot will be empty during a given time interval
	 * 
	 * @param tw
	 *            The time window
	 * @return <b>true</b> if the slot will be empty during the given time
	 *         window, <b>flase</b> otherwise. @
	 */
	public boolean isSlotEmptyAt(TimeWindow tw) {
		int level = 0;
		ContainerAlignment align = ContainerAlignment.center;
		Container c;
		try {
			c = new Container("tmp", slot.getTEU());
			SlotReservation reservation = new SlotReservation(c, slot, tw,
					align.getValue(), slot.getLevel(level));

			if (canAddReservation(reservation))
				return true;
			else
				return false;
		} catch (ContainerDimensionException e) {
			e.printStackTrace();
		}
		return false;
	}

	/**
	 * Tells if we can remove a container at a given time (if it is free) and
	 * without making any mess for further reservations
	 * 
	 * @param container
	 *            Container to remove
	 * @param pickupTimeWindow
	 *            Time window during which the container should be popped
	 * @return <b>true</b> if the container can be popped, <b>flase</b>
	 *         otherwise. @
	 */
	public boolean isContainerFreeAt(Container container,
			TimeWindow pickupTimeWindow) {
		// From TwP look every slotreservation and tell if we can remove the
		// container at the given time and without making mess for future
		// reservations...
		// So lets rebuild the slot through time !

		// Create the copy of the slot at the given time
		Slot copySlot = new Slot("copyOf" + slot.getId(), Terminal
				.getInstance().getBay(slot.getLocation().getRoad().getId()),
				slot.getLocation(), slot.getTEU(), Terminal.getInstance()
						.getBlock(slot.getPaveId()).getType());
		try {
			Container copyContainer = new Container("copyOf"
					+ container.getId(), container.getTEU());

			for (Time t : schedule.keySet()) {
				if (t.toStep() <= pickupTimeWindow.getMin().toStep()) {
					ArrayList<SlotReservation> reservations = schedule.get(t);
					for (int i = reservations.size() - 1; i >= 0; i--) {
						SlotReservation res = reservations.get(i);
						if (res.getTW().getMax().toStep() > pickupTimeWindow
								.getMax().toStep()) {
							// Place the container
							Container contRes = res.getContainer();
							Container copyContRes = new Container("copyOf"
									+ res.getContainer().getId(),
									contRes.getTEU());
							try {
								copySlot.stockContainer(copyContRes, res
										.getLevel().getLevelIndex(), res
										.getAlignment());
							} catch (SlotContainerIncompatibilityException e) {
								e.printStackTrace();
							} catch (FallingContainerException e) {
								e.printStackTrace();
							} catch (UnstackableContainerException e) {
								e.printStackTrace();
							} catch (NotEnoughSpaceException e) {
								e.printStackTrace();
							} catch (CollisionException e) {
								e.printStackTrace();
							} catch (LevelException e) {
								e.printStackTrace();
							}
						}
					}
				}
			}
			// Try to remove the container
			try {
				Container c = copySlot.pop(copyContainer.getId());
				if (c == null) {
					System.out.println("Container not popped : "
							+ copyContainer.getId() + " slot : " + copySlot);
					// BP
				}
			} catch (EmptySlotException e) {
				return false;
			} catch (NotAccessibleContainerException e) {
				return false;
			} catch (ContainerNotFoundException e) {
				return false;
			}
			try {
				copySlot.stockContainer(copyContainer, container
						.getContainerLocation().getLevel(), container
						.getContainerLocation().getAlign());
			} catch (SlotContainerIncompatibilityException e) {
				e.printStackTrace();
			} catch (FallingContainerException e) {
				e.printStackTrace();
			} catch (UnstackableContainerException e) {
				e.printStackTrace();
			} catch (NotEnoughSpaceException e) {
				e.printStackTrace();
			} catch (CollisionException e) {
				e.printStackTrace();
			} catch (LevelException e) {
				e.printStackTrace();
			}
			Time higherKey = schedule.higherKey(pickupTimeWindow.getMin());
			while (higherKey != null
					&& higherKey.toStep() <= pickupTimeWindow.getMax().toStep()) {
				ArrayList<SlotReservation> reservations = schedule
						.get(higherKey);
				for (SlotReservation res : reservations) {
					// Place the container
					Container contRes = res.getContainer();
					Container copyContRes = new Container("copyOf"
							+ res.getContainer().getId(), contRes.getTEU());

					try {
						copySlot.stockContainer(copyContRes, res.getLevel()
								.getLevelIndex(), res.getAlignment());
					} catch (SlotContainerIncompatibilityException e) {

					} catch (FallingContainerException e) {

					} catch (UnstackableContainerException e) {

					} catch (NotEnoughSpaceException e) {

					} catch (CollisionException e) {

					} catch (LevelException e) {

					}

					// Try to remove the container now !
					try {
						copySlot.pop(container.getId());
					} catch (EmptySlotException e) {
						return false;
					} catch (NotAccessibleContainerException e) {
						return false;
					} catch (ContainerNotFoundException e) {
						return false;
					}
					try {
						if (copyContainer.getContainerLocation() == null) {
							System.out.println("copyContainer "
									+ copyContainer.getId()
									+ " has no container location !");
							return false;
						}
						copySlot.stockContainer(copyContainer, copyContainer
								.getContainerLocation().getLevel(),
								copyContainer.getContainerLocation().getAlign());
					} catch (SlotContainerIncompatibilityException e) {
						e.printStackTrace();
					} catch (FallingContainerException e) {
						e.printStackTrace();
					} catch (UnstackableContainerException e) {
						e.printStackTrace();
					} catch (NotEnoughSpaceException e) {
						e.printStackTrace();
					} catch (CollisionException e) {
						e.printStackTrace();
					} catch (LevelException e) {
						e.printStackTrace();
					} catch (NullPointerException e) {
						System.out.println("NullPointer : ");

						if (copyContainer == null)
							System.out.println("copyContainer is null");
						else if (copyContainer.getContainerLocation() == null)
							System.out.println("copyContainer "
									+ copyContainer.getId()
									+ " has no container location !");
						else
							System.out.println("no reason !!! ("
									+ copyContainer.getContainerLocation()
									+ ")");

						e.printStackTrace();
						System.exit(ReturnCodes.EXIT_ON_SLOT_RESERVATION_ERROR
								.getCode());
					}

				}
				higherKey = schedule.higherKey(pickupTimeWindow.getMin());
			}
		} catch (ContainerDimensionException e1) {
			e1.printStackTrace();
		}
		// If we are here that's because we can remove our container at the
		// given time safely
		return true;
	}

	/**
	 * Get the reservation list in the schedule which have the least starting
	 * time strictly greater than the given time.
	 * 
	 * @param t
	 *            Time
	 * @return Reservation list in the schedule which have the least starting
	 *         time strictly greater than the given time.
	 */
	public ArrayList<SlotReservation> getHigherValue(Time t) {
		Entry<Time, ArrayList<SlotReservation>> val = schedule.higherEntry(t);
		if (val != null)
			return val.getValue();
		else
			return new ArrayList<SlotReservation>();
	}

	/**
	 * Update the end time of the reservation concerning the given container
	 * 
	 * @param container
	 *            Container concerned by the reservation
	 * @param TWP_max
	 *            New end time of the reservation
	 */
	public void updateReservationMaxTime(Container container, Time TWP_max) {
		boolean found = false;
		for (Time t : schedule.keySet()) {
			ArrayList<SlotReservation> l = schedule.get(t);
			for (int i = 0; i < l.size() && !found; i++) {
				SlotReservation s = l.get(i);
				if (s.getContainer().getId().equals(container.getId())) {
					// s.setTW(new TimeWindow(t, TWP_max));
					s = new SlotReservation(container, slot, new TimeWindow(t,
							TWP_max), s.getAlignment(), s.getLevel());
					l.set(i, s);
					found = true;
				}
			}
			if (found) {
				schedule.put(t, l);
				break;
			}
		}

	}

	/**
	 * Gives a reservation compatible with the given contID and timewindow
	 * 
	 * @param container
	 *            Container to stock
	 * @param tw
	 *            Time window of the reservation
	 * @return The slot reservation if any, <b>null</b> otherwise
	 */
	public SlotReservation giveFreeReservation(Container container,
			TimeWindow tw) {
		// Commencer en bas, chercher une align et ensuite si c bon regarder si
		// niveau TW ça colle
		for (int level = 0; level < slot.getMaxLevel(); level++) {
			for (ContainerAlignment align : ContainerAlignment.values()) {
				SlotReservation reservation = new SlotReservation(container,
						slot, tw, align.getValue(), slot.getLevel(level));
				if (canAddReservation(reservation)) {
					return reservation;
				}
			}
		}

		// If we've reached this line, then there is no space for the container
		// during the given time window
		return null;
	}

	public boolean canAddReservation(SlotReservation toAdd) {
		Container container = toAdd.getContainer();

		if (slot.getTEU() < toAdd.getContainer().getTEU())
			return false;
		if (schedule.size() == 0)
			return true;

		TreeMap<Long, List<String>> toPop = new TreeMap<>();

		Slot copySlot = new Slot("copyOf" + slot.getId(), Terminal
				.getInstance().getBay(slot.getLocation().getRoad().getId()),
				slot.getLocation(), slot.getTEU(), Terminal.getInstance()
						.getBlock(slot.getPaveId()).getType());
		try {
			Container copyContainer = new Container("copyOf"
					+ container.getId(), container.getTEU());
			Time t = schedule.firstKey();
			while (t != null && t.toStep() <= toAdd.getTW().getMin().toStep()) {
				// for(Time t : schedule.keySet()){
				ArrayList<SlotReservation> l = schedule.get(t);
				for (int i = l.size() - 1; i >= 0; i--) {
					SlotReservation sr = l.get(i);

					// Pop something before ?
					if (toPop.size() > 0) {
						Time toPopTime = new Time(toPop.firstKey());

						while (toPopTime != null
								&& toPopTime.toStep() <= t.toStep()) {
							List<String> toPopList = toPop.remove(toPopTime
									.toStep());
							for (String contID : toPopList) {
								try {
									Container popped = copySlot.pop(contID);
									if (popped == null)
										return false;
								} catch (EmptySlotException e) {
									return false;
								} catch (NotAccessibleContainerException e) {
									return false;
								} catch (ContainerNotFoundException e) {
									return false;
								}
							}
							if (toPop.size() == 0)
								toPopTime = null;
							else
								toPopTime = new Time(toPop.firstKey());
						}
					}
					// Push container
					Container contRes = sr.getContainer();
					Container copyContRes = new Container("copyOf"
							+ sr.getContainer().getId(), contRes.getTEU());

					try {
						copySlot.stockContainer(copyContRes, sr.getLevel()
								.getLevelIndex(), sr.getAlignment());
					} catch (SlotContainerIncompatibilityException e) {
						e.printStackTrace();
						log.error(e.getMessage(), e);
					} catch (FallingContainerException e) {
						e.printStackTrace();
						log.error(e.getMessage(), e);
					} catch (UnstackableContainerException e) {
						e.printStackTrace();
						log.error(e.getMessage(), e);
					} catch (NotEnoughSpaceException e) {
						e.printStackTrace();
						log.error(e.getMessage(), e);
					} catch (CollisionException e) {
						e.printStackTrace();
						log.error(e.getMessage(), e);
					} catch (LevelException e) {
						e.printStackTrace();
						log.error(e.getMessage(), e);
					}
					// Add to pop list
					if (sr.getTW().getMax().toStep() <= toAdd.getTW().getMax()
							.toStep()) {
						List<String> toPopList;
						if (toPop.containsKey(t.toStep()))
							toPopList = new ArrayList<String>(toPop.get(t
									.toStep()));
						else
							toPopList = new ArrayList<String>();

						toPopList.add(copyContRes.getId());
						toPop.put(sr.getTW().getMax().toStep(), toPopList);

					}
				}
				t = schedule.higherKey(t);
			}
			// Pop something before ?
			if (toPop.size() > 0) {
				Time toPopTime = new Time(toPop.firstKey());

				while (toPopTime != null
						&& toPopTime.toStep() <= toAdd.getTW().getMin()
								.toStep()) {
					List<String> toPopList = toPop.remove(toPopTime.toStep());

					for (String contID : toPopList) {
						try {
							Container popped = copySlot.pop(contID);
							if (popped == null)
								return false;
						} catch (EmptySlotException e) {
							return false;
						} catch (NotAccessibleContainerException e) {
							return false;
						} catch (ContainerNotFoundException e) {
							return false;
						}
					}
					if (toPop.size() == 0) {
						toPopTime = null;
					} else {
						toPopTime = new Time(toPop.firstKey());
					}
					// }
				}
			}
			// Try to put container
			if (!copySlot.canAddContainer(copyContainer, toAdd.getLevel()
					.getLevelIndex(), toAdd.getAlignment())) {
				return false;
			} else {
				try {
					copySlot.stockContainer(copyContainer, toAdd.getLevel()
							.getLevelIndex(), toAdd.getAlignment());
				} catch (SlotContainerIncompatibilityException e) {
					e.printStackTrace();
					log.error(e.getMessage(), e);
				} catch (FallingContainerException e) {
					e.printStackTrace();
					log.error(e.getMessage(), e);
				} catch (UnstackableContainerException e) {
					e.printStackTrace();
					log.error(e.getMessage(), e);
				} catch (NotEnoughSpaceException e) {
					e.printStackTrace();
					log.error(e.getMessage(), e);
				} catch (CollisionException e) {
					e.printStackTrace();
					log.error(e.getMessage(), e);
				} catch (LevelException e) {
					e.printStackTrace();
					log.error(e.getMessage(), e);
				}
				List<String> toPopList;
				if (toPop.containsKey(toAdd.getTW().getMax().toStep()))
					toPopList = toPop.get(toAdd.getTW().getMax().toStep());
				else
					toPopList = new ArrayList<String>();

				toPopList.add(copyContainer.getId());
				toPop.put(toAdd.getTW().getMax().toStep(), toPopList);

			}
			// Try to pop container to pop
			t = schedule.higherKey(toAdd.getTW().getMin());
			while (t != null && t.toStep() <= toAdd.getTW().getMax().toStep()) {
				ArrayList<SlotReservation> l = schedule.get(t);
				for (int i = l.size() - 1; i >= 0; i--) {
					SlotReservation sr = l.get(i);
					// Pop something before ?
					if (toPop.size() > 0) {
						Time toPopTime = new Time(toPop.firstKey());
						while (toPopTime != null
								&& toPopTime.toStep() <= t.toStep()) {

							List<String> toPopList = toPop.remove(toPopTime
									.toStep());
							if (toPopList != null) {
								for (String contID : toPopList) {
									try {
										Container popped = copySlot.pop(contID);
										if (popped == null)
											return false;
									} catch (EmptySlotException e) {
										return false;
									} catch (NotAccessibleContainerException e) {
										return false;
									} catch (ContainerNotFoundException e) {
										return false;
									}
								}
							}
							if (toPop.size() == 0)
								toPopTime = null;
							else
								toPopTime = new Time(toPop.firstKey());
						}
					}
					// Push container
					Container contRes = sr.getContainer();
					Container copyContRes = new Container("copyOf"
							+ sr.getContainer().getId(), contRes.getTEU());

					try {
						copySlot.stockContainer(copyContRes, sr.getLevel()
								.getLevelIndex(), sr.getAlignment());
					} catch (SlotContainerIncompatibilityException e) {
						return false;
					} catch (FallingContainerException e) {
						return false;
					} catch (UnstackableContainerException e) {
						return false;
					} catch (NotEnoughSpaceException e) {
						return false;
					} catch (CollisionException e) {
						return false;
					} catch (LevelException e) {
						return false;
					}

					if (sr.getTW().getMax().toStep() <= toAdd.getTW().getMax()
							.toStep()) {
						List<String> toPopList;
						if (toPop.containsKey(t.toStep()))
							toPopList = toPop.get(t.toStep());
						else
							toPopList = new ArrayList<String>();

						toPopList.add(copyContRes.getId());
						toPop.put(sr.getTW().getMax().toStep(), toPopList);
					}

				}

				t = schedule.higherKey(t);
			}
			while (toPop.size() > 0) {
				t = new Time(toPop.firstKey());
				List<String> toPopList = toPop.remove(t.toStep());
				for (String contID : toPopList) {
					try {
						Container popped = copySlot.pop(contID);
						if (popped == null) {
							return false;
						}
					} catch (EmptySlotException e) {
						return false;
					} catch (NotAccessibleContainerException e) {
						return false;
					} catch (ContainerNotFoundException e) {
						return false;
					}
				}
			}
			return true;
		} catch (ContainerDimensionException e) {
			e.printStackTrace();
			log.error(e.getMessage(), e);
		}
		return true;

	}

	/**
	 * A reservation can be added if : -> The slot is compatible with the
	 * container to stock -> No container prevent from placing a container at
	 * the given location (at the same place and above it) -> A container is (or
	 * is going to be) under the given location during the whole given time
	 * window
	 * 
	 * @param reservation
	 *            Reservation to check
	 * @return <b>true</b> if it is possible to add the given reservation,
	 *         <b>false</b> otherwise.
	 */
	// TODO ADD : watch if the reservation will prevent a container move ! Watch
	// if we can remove the container when the new one will be stacked
	/*
	 * public boolean canAddReservation2(SlotReservation reservation) {
	 * Container container = reservation.getContainer();
	 * 
	 * if(slot.getTEU() < container.getTEU()) return false;
	 * 
	 * //Create the copy of the slot at the given time Slot copySlot = new
	 * Slot("copyOf"+slot.getId(),
	 * MissionsFileGenerator.rt.getLane(slot.getLocation().getRoad().getId()),
	 * slot.getLocation(),
	 * slot.getTEU(),MissionsFileGenerator.rt.getPave(slot.getPaveId
	 * ()).getType()); try { Container copyContainer = new
	 * Container("copyOf"+container.getId(), container.getTEU());
	 * 
	 * 
	 * for(Time t : schedule.keySet()){ if(t.toStep() <=
	 * reservation.getTW().getMin().toStep() ){ ArrayList<SlotReservation>
	 * reservations = schedule.get(t); for(int i=reservations.size()-1 ; i>=0 ;
	 * i--){ SlotReservation res = reservations.get(i); //Has to be taken into
	 * account ? if(res.getTW().getMax().toStep() >=
	 * reservation.getTW().getMin().toStep()){ //Place the container Container
	 * contRes = res.getContainer(); Container copyContRes = new
	 * Container("copyOf"+res.getContainer().getId(), contRes.getTEU());
	 * 
	 * try { copySlot.stockContainer(copyContRes,
	 * res.getLevel().getLevelIndex(), res.getAlignment()); } catch
	 * (SlotContainerIncompatibilityException e) { e.printStackTrace(); } catch
	 * (FallingContainerException e) { e.printStackTrace(); } catch
	 * (UnstackableContainerException e) { e.printStackTrace(); } catch
	 * (NotEnoughSpaceException e) { e.printStackTrace(); } catch
	 * (CollisionException e) { e.printStackTrace(); } catch (LevelException e)
	 * { e.printStackTrace(); }
	 * 
	 * }
	 * if(res.getTW().getMax().toStep()<reservation.getTW().getMax().toStep()){
	 * //Can we remove the container ?
	 * 
	 * } //else the container will be gone when the new one will be stocked } }
	 * else break; }
	 * 
	 * //Look if we can add the container
	 * if(!copySlot.canAddContainer(copyContainer,
	 * reservation.getLevel().getLevelIndex(), reservation.getAlignment())){
	 * return false; } else{ try { copySlot.stockContainer(copyContainer,
	 * reservation.getLevel().getLevelIndex(), reservation.getAlignment()); }
	 * catch (SlotContainerIncompatibilityException e) { e.printStackTrace(); }
	 * catch (FallingContainerException e) { e.printStackTrace(); } catch
	 * (UnstackableContainerException e) { e.printStackTrace(); } catch
	 * (NotEnoughSpaceException e) { e.printStackTrace(); } catch
	 * (CollisionException e) { e.printStackTrace(); } catch (LevelException e)
	 * { e.printStackTrace(); }
	 * 
	 * // Look if it will block a previous stock reservation departure //TODO Do
	 * we have to do this for all previous reservations ? Or just for the first
	 * one (by recurrency...) ? Time t =
	 * schedule.lowerKey(reservation.getTW().getMin()); while(t != null){
	 * ArrayList<SlotReservation> reservations = schedule.get(t);
	 * for(SlotReservation res : reservations){ //Has to be taken into account ?
	 * if
	 * (res.getTW().getMax().toStep()<reservation.getTW().getMax().toStep()&&res
	 * .getTW().getMin().toStep()<=reservation.getTW().getMin().toStep()){ //Try
	 * to remove the container and look if there is any mess try {
	 * copySlot.pop(res.getContainer().getId()); } catch (EmptySlotException e)
	 * { return false; } catch (NotAccessibleContainerException e) { return
	 * false; } catch (ContainerNotFoundException e) { return false; } } } t =
	 * schedule.lowerKey(t); }
	 * 
	 * //Look if it will block a future stock reservation t =
	 * schedule.higherKey(reservation.getTW().getMin()); while (t != null){
	 * ArrayList<SlotReservation> reservations = schedule.get(t);
	 * for(SlotReservation res : reservations){ //Has to be taken into account ?
	 * if(res.getTW().getMin().toStep() <=
	 * reservation.getTW().getMax().toStep()){ //Place the container Container
	 * contRes = res.getContainer(); Container copyContRes = new
	 * Container("copyOf"+res.getContainer().getId(), contRes.getTEU());
	 * 
	 * try { if(!copySlot.canAddContainer(copyContRes,
	 * res.getLevel().getLevelIndex(), res.getAlignment())){ return false; }
	 * copySlot.stockContainer(copyContRes, res.getLevel().getLevelIndex(),
	 * res.getAlignment()); } catch (SlotContainerIncompatibilityException e) {
	 * e.printStackTrace(); } catch (FallingContainerException e) {
	 * e.printStackTrace(); } catch (UnstackableContainerException e) {
	 * e.printStackTrace(); } catch (NotEnoughSpaceException e) {
	 * e.printStackTrace(); } catch (CollisionException e) {
	 * e.printStackTrace(); } catch (LevelException e) { e.printStackTrace(); }
	 * } else break; } t = schedule.higherKey(t); } } } catch
	 * (ContainerDimensionException e) { e.printStackTrace(); } //If it reaches
	 * this line, then it's ok ! return true; }
	 */

	/**
	 * Add the reservation in the schedule
	 * 
	 * @param reservation
	 *            Reservation to add @
	 */
	public void addReservation(SlotReservation reservation) {
		if (reservation == null) {
			new Exception("Reservation is null!").printStackTrace();
			System.exit(ReturnCodes.EXIT_NULL_RESERVATION_ERROR.getCode());
		}
		ArrayList<SlotReservation> reservations = schedule.get(reservation
				.getTW().getMin());
		int iInsertion = 0;
		if (reservations == null)
			reservations = new ArrayList<SlotReservation>();
		else {
			int i = 0;
			for (SlotReservation res : reservations) {
				if (res.getLevel().getLevelIndex() == reservation.getLevel()
						.getLevelIndex()) {
					if (res.getTW().getMax().toStep() <= reservation.getTW()
							.getMax().toStep()) {
						iInsertion = i;
						break;
					}
				} else {
					if (res.getLevel().getLevelIndex() < reservation.getLevel()
							.getLevelIndex()) {
						iInsertion = i;
						break;
					}
				}
				i++;
			}
		}
		reservations.add(iInsertion, reservation);
		schedule.put(reservation.getTW().getMin(), reservations);
	}

	public Slot getSlot() {
		return slot;
	}

	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("SlotReservations of " + slot.getId() + " : \n");
		for (Time t : schedule.keySet()) {
			sb.append("\t" + t + " : ");
			ArrayList<SlotReservation> resa = schedule.get(t);
			// TODO WARNING !!! IS RESA CAN BE NULL ?
			if (resa != null) {
				for (SlotReservation s : resa) {
					sb.append(s + "\t");
				}
				sb.append("\n");
			}
		}
		return sb.toString();
	}
}