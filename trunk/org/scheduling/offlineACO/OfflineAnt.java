package org.scheduling.offlineACO;

import java.util.ArrayList;
import java.util.List;

import org.scheduling.LocalScore;
import org.scheduling.MissionScheduler;
import org.scheduling.ScheduleEdge;
import org.scheduling.ScheduleResource;
import org.scheduling.ScheduleTask;
import org.system.Terminal;
import org.vehicles.StraddleCarrier;

public class OfflineAnt extends ScheduleResource {
	public OfflineAnt(StraddleCarrier vehicle) {
		super(vehicle);
	}

	public void spreadPheromoneOnPath(double q) {
		/*
		 * double lambda =
		 * TSPMissionScheduler.getGlobalParameters().getLambda(); double F1 =
		 * TSPMissionScheduler.getGlobalParameters().getF1(); double F2 =
		 * TSPMissionScheduler.getGlobalParameters().getF2(); double denum =
		 * (distanceInSeconds*F1)+(overspentTime.getInSec()*F2); if(denum ==
		 * 0.0){ denum = CLOSE_TO_ZERO; //new
		 * Exception("DIV BY ZERO").printStackTrace(); } double q =
		 * lambda/denum;
		 * System.err.println("lambda = "+lambda+" denum="+denum+" q="+q);
		 */
		List<ScheduleEdge> path = score.getPath();
		for (int i = 0; i < path.size(); i++) {
			OfflineEdge e = (OfflineEdge) path.get(i);
			e.addPheromone(getID(), q);
			if (MissionScheduler.DEBUG)
				System.err.println(getID() + " add " + q + " on " + e.getID()
						+ " ph = " + e.getPHString());
		}
	}

	@SuppressWarnings("unchecked")
	public OfflineEdge chooseRandomDestination() {
		OfflineEdge destination = null;

		// Among possible destinations, remove already visited ones

		List<OfflineEdge> destinations = getLocation().getDestinations();
		List<OfflineEdge> available = new ArrayList<OfflineEdge>(
				destinations.size());
		for (OfflineEdge e : destinations) {
			if (!hasBeenVisited(e.getNodeTo().getID())
					&& e.getNodeTo() != OfflineACOScheduler.getInstance().SOURCE_NODE) {
				if (e.getNodeFrom() != OfflineACOScheduler.getInstance().SOURCE_NODE
						&& e.getCost(getModelID()) != Double.POSITIVE_INFINITY)
					available.add(e);
				else if (e.getCost(getID()) != Double.POSITIVE_INFINITY)
					available.add(e);
			}
		}

		destination = available.get(Terminal.getInstance().getRandom().nextInt(available
				.size()));

		if (available.size() == 0) {
			// new Exception("No available node !").printStackTrace();
			destination = (OfflineEdge) getLocation()
					.getEdgeTo(
							(ScheduleTask<OfflineEdge>) MissionScheduler
									.getInstance().SOURCE_NODE);
		}
		return destination;
	}

	@SuppressWarnings("unchecked")
	public OfflineEdge chooseDestination() {
		// System.err.println("Choose destination for ant of "+colony.getID()+" from "+location.getID());
		OfflineEdge destination = null;

		// Among possible destinations, remove already visited ones
		List<OfflineEdge> destinations = getLocation().getDestinations();
		List<OfflineEdge> available = new ArrayList<OfflineEdge>(
				destinations.size());
		for (OfflineEdge e : destinations) {
			if (e.getNodeTo() != OfflineACOScheduler.getInstance().SOURCE_NODE
					&& !hasBeenVisited(e.getNodeTo().getID())) {
				if (e.getNodeFrom() != OfflineACOScheduler.getInstance().SOURCE_NODE
						&& e.getCost(getModelID()) != Double.POSITIVE_INFINITY)
					available.add(e);
				else if (e.getCost(getID()) != Double.POSITIVE_INFINITY)
					available.add(e);
			}
		}

		if (available.size() == 0) {
			new Exception("No available node !").printStackTrace();
			destination = getLocation()
					.getEdgeTo(
							(ScheduleTask<OfflineEdge>) MissionScheduler
									.getInstance().SOURCE_NODE);
		} else {
			if (MissionScheduler.DEBUG) {
				System.err.println("Available destinations : ");
				for (OfflineEdge e : available) {
					if (e.getNodeFrom() == OfflineACOScheduler.getInstance().SOURCE_NODE
							|| e.getNodeTo() == OfflineACOScheduler
									.getInstance().SOURCE_NODE)
						System.err
								.println(e.getID() + " d=" + e.getCost(getID())
										+ " ph=" + e.getPHString());
					else
						System.err.println(e.getID() + " d="
								+ e.getCost(getModelID()) + " ph="
								+ e.getPHString());
				}
			}

			OfflineSchedulerParameters parameters = OfflineACOScheduler.getInstance().getGlobalParameters();
			ArrayList<OfflineDestinationChooserHelper> choices = new ArrayList<OfflineDestinationChooserHelper>(available.size());

			double sumWeight = 0.0;
			double sumPheromone = 0.0;
			double sumForeignPheromone = 0.0;
			for (OfflineEdge e : available) {
				// DISTANCE IN SECONDS IF FOLLOW THIS EDGE
				double cost = 0.0;
				if (e.getNodeFrom() != OfflineACOScheduler.getInstance().SOURCE_NODE)
					cost = e.getCost(getModelID());
				else
					cost = e.getCost(getID());

				double dsP = cost;
				double pickupReachTime = currentTime + cost;

				double overTimeP = 0;
				double waitTimeP = 0;
				double twpMin = e.getNodeTo().getMission()
						.getPickupTimeWindow().getMin().getInSec();
				double twpMax = e.getNodeTo().getMission()
						.getPickupTimeWindow().getMax().getInSec();
				double pickupLeaveTime = twpMin + handlingTime;
				if (getLocation() != OfflineACOScheduler.getInstance().SOURCE_NODE
						&& pickupReachTime < twpMin) {
					waitTimeP += twpMin - pickupReachTime;
				}

				// IF late @ P
				if ((pickupReachTime + handlingTime) > twpMax) {
					overTimeP = pickupReachTime + handlingTime - twpMax;
					pickupLeaveTime = pickupReachTime + handlingTime;
				}

				double dsPD = e.getNodeTo().getCost(getModelID());
				double deliveryReachTime = pickupLeaveTime + dsPD;
				double twdMin = e.getNodeTo().getMission()
						.getDeliveryTimeWindow().getMin().getInSec();
				double twdMax = e.getNodeTo().getMission()
						.getDeliveryTimeWindow().getMax().getInSec();
				double waitTimeD = 0;
				double overTimeD = 0;
				if (deliveryReachTime < twdMin) {
					waitTimeD += twdMin - deliveryReachTime;
				}

				// IF late @ D
				if ((deliveryReachTime + handlingTime) > twdMax) {
					overTimeD = deliveryReachTime + handlingTime - twdMax;
				}

				// Overspent time if follow this edge
				// System.err.println("for "+e.getID()+" T = "+time+" P="+new
				// Time(pickupReachTime+"s")+" PL="+new
				// Time(pickupLeaveTime+"s")+" D="+new
				// Time(deliveryReachTime+"s")+" underP="+new
				// Time(waitTimeP+"s")+" underD="+new
				// Time(waitTimeD+"s")+" overP="+new
				// Time(overTimeP+"s")+" overD="+new Time(overTimeD+"s"));
				double over = overTimeP + overTimeD;
				double under = waitTimeP + waitTimeD;

				// double weight = e.getCost(colony.getModelID()); //TODO TAKE
				// INTO ACCOUNT THE EVAL FUNCTION (with time, distance, distance
				// in sec...)
				double ds = dsP + dsPD;
				double weight = ds * parameters.getF1() + over
						* parameters.getF2() + under * parameters.getF3(); // TODO
																			// TAKE
																			// INTO
																			// ACCOUNT
																			// THE
																			// EVAL
																			// FUNCTION
																			// (with
																			// time,
																			// distance,
																			// distance
																			// in
																			// sec...)
				double ph = e.getPheromone(getID());
				double foreignPH = e.getForeignPheromone(getID());

				// double localProba =
				// Math.pow(ph,parameters.getAlpha())*Math.pow(1.0+
				// (1.0/weight),
				// parameters.getBeta())*Math.pow(1.0+(ph/(foreignPH+ph)),parameters.getGamma());
				if (MissionScheduler.DEBUG)
					System.err.println(e.getID() + " d=" + ds + " l=" + over
							+ " e=" + under + " weight = " + weight);
				// System.err.println("for "+e.getID()+" T = "+time+" e.cost="+new
				// Time(cost+"s")+" Local proba = "+localProba+" under="+new
				// Time(under+"s")+" over="+new Time(over+"s"));
				// sumProba += localProba;
				// choices.add(new OfflineDestinationChooserHelper(e,
				// localProba));
				OfflineDestinationChooserHelper helper = new OfflineDestinationChooserHelper(
						e, ph, weight, foreignPH);
				choices.add(helper);
				sumWeight += weight;
				sumPheromone += ph;
				sumForeignPheromone += foreignPH;
			}

			if (choices.size() > 1) {
				double sumProba = 0.0;
				// Compute probas
				for (OfflineDestinationChooserHelper dch : choices) {
					double probaAlpha = Math.pow(
							1.0 + (dch.getPheromone() / sumPheromone),
							parameters.getAlpha());
					double probaBeta = Math.pow(sumWeight / dch.getWeight(),
							parameters.getBeta());
					double probaGamma = Math
							.pow(1.0 + ((dch.getPheromone() / (dch
									.getForeignPheromone() + dch.getPheromone())) / (sumPheromone + sumForeignPheromone)),
									parameters.getGamma());
					double proba = probaAlpha * probaBeta * probaGamma;
					dch.setProba(proba);
					if (MissionScheduler.DEBUG)
						System.err.println(dch.getDestination().getID()
								+ " localProba=" + probaAlpha + " . "
								+ probaBeta + " . " + probaGamma + " = "
								+ proba);
					sumProba += proba;
				}

				if (MissionScheduler.DEBUG)
					System.err.println("Sum proba = " + sumProba);
				// Normalization for probabilities between [0,1]
				for (OfflineDestinationChooserHelper dch : choices) {
					dch.setProba(dch.getProba() / sumProba);
					if (MissionScheduler.DEBUG)
						System.err.println("1) " + dch.getDestination().getID()
								+ " p=" + dch.getProba());
				}

				// Collections.sort(choices);

				// Cumulate the probabilities
				double overall = 0.0;
				for (OfflineDestinationChooserHelper dch : choices) {
					overall += dch.getProba();
					dch.setProba(overall);
					if (MissionScheduler.DEBUG)
						System.err.println("2) " + dch.getDestination().getID()
								+ " p=" + dch.getProba());
				}

				double dChoice = Terminal.getInstance().getRandom().nextDouble();
				if (MissionScheduler.DEBUG)
					System.err.println("RANDOM = " + dChoice);
				for (OfflineDestinationChooserHelper dch : choices) {
					if (MissionScheduler.DEBUG)
						System.err
								.print(dch.getProba() + ">= " + dChoice + "?");
					if (dch.getProba() >= dChoice) {
						if (MissionScheduler.DEBUG)
							System.err.println("=> TRUE");
						destination = (OfflineEdge) dch.getDestination();
						if (MissionScheduler.DEBUG)
							System.err.println("=> " + destination.getID());
						// new
						// Exception("1) Destination = "+destination.getNodeFrom()+" -> "+destination.getNodeTo()).printStackTrace();
						break;
					} else if (MissionScheduler.DEBUG)
						System.err.println("=> FALSE");
				}
				if (destination == null) {
					destination = (OfflineEdge) choices.get(choices.size() - 1)
							.getDestination();
					if (MissionScheduler.DEBUG)
						System.err.println("=> " + destination.getID());
					// new
					// Exception("1-2) Destination = "+destination.getNodeFrom()+" -> "+destination.getNodeTo()).printStackTrace();
				}
			} else {

				if (MissionScheduler.DEBUG)
					System.err.println("2) ELSE");
				destination = (OfflineEdge) choices.get(choices.size() - 1)
						.getDestination();
				if (MissionScheduler.DEBUG)
					System.err.println("=> " + destination.getID());
				// new
				// Exception("2) Destination = "+destination.getNodeFrom()+" -> "+destination.getNodeTo()).printStackTrace();
			}

			for (OfflineDestinationChooserHelper choice : choices)
				choice.destroy();
		}
		if (MissionScheduler.DEBUG) {
			System.err.println("PRESS ENTER");
			java.util.Scanner scan = new java.util.Scanner(System.in);
			scan.nextLine();
			scan.close();
		}

		// new
		// Exception("3) Destination = "+destination.getNodeFrom()+" -> "+destination.getNodeTo()).printStackTrace();
		return destination;
	}

	public void step() {
		if (MissionScheduler.DEBUG)
			System.err.println(getID() + " is located at "
					+ getLocation().getID());
		OfflineEdge choice = chooseDestination();
		if (MissionScheduler.DEBUG)
			System.err.println(getID() + " chose " + choice.getID());
		visitNode(choice.getNodeTo());
		if (MissionScheduler.DEBUG)
			System.err.println(getID() + " is now located at "
					+ getLocation().getID());
	}

	@SuppressWarnings("unchecked")
	public void gotoDepot() {
		ScheduleTask<OfflineEdge> location = getLocation();
		if (location != OfflineACOScheduler.getInstance().SOURCE_NODE) {
			OfflineEdge edge = location
					.getEdgeTo((ScheduleTask<OfflineEdge>) MissionScheduler
							.getInstance().SOURCE_NODE);
			// System.err.println("GoBack Edge : "+edge.getID());

			// Current Location to Pickup location of the next mission
			score.addDistance(edge.getDistance());
			score.addTravelTime(edge.getCost(getID()));

			// Update Time after reaching Depot
			currentTime += edge.getCost(getID());

			score.addEdge(edge, currentTime);
		}
	}

	@SuppressWarnings("unchecked")
	public static void compute() {
		resetVisited();
		for (ScheduleResource ant : ScheduleResource.resources) {
			ant.reset();
		}
		while (nonvisited > 0) {
			// Choose a colony
			/*
			 * ----- There is a point here : may be change the way an ant is
			 * chosen to be less impacted by the random ----
			 */
			OfflineAnt ant = (OfflineAnt) resources.get(Terminal.getInstance().getRandom()
					.nextInt(resources.size()));
			if (MissionScheduler.DEBUG)
				System.err.println("Ant " + ant.getID() + " is chosen");
			ant.step();
		}

		// All nodes had been visited => We have a solution :
		// Add the way back to the depot
		for (ScheduleResource ant : resources) {
			((OfflineAnt) ant).gotoDepot();
		}

		// Eval the solution
		globalScore.reset();
		for (ScheduleResource hill : resources) {
			OfflineAnt ant = (OfflineAnt) hill;
			LocalScore localScore = new LocalScore(ant.score);
			globalScore.addScore(localScore);
		}

		// Evaporation
		for (ScheduleTask<OfflineEdge> n : MissionScheduler.getInstance().getTasks()) {
			List<OfflineEdge> destinations = n.getDestinations();
			for (OfflineEdge edge : destinations) {
				edge.evaporate();
			}
		}
		// System.out.println("Solution score = "+globalScore);
	}

	public static void spreadPheromone() {
		// Spread pheromone according to local score (or global one ?)
		double lambda = OfflineACOScheduler.getInstance().getGlobalParameters().getLambda();
		// double F1 = TSPMissionScheduler.getGlobalParameters().getF1();
		// double F2 = TSPMissionScheduler.getGlobalParameters().getF2();
		double denum = globalScore.getScore(); // (globalScore.getDistanceInSeconds()*F1)+(globalScore.getOverspentTime()*F2);//+globalScore.getWaitTime()*TSPAnt.F3;
		if (denum == 0.0) {
			denum = MissionScheduler.CLOSE_TO_ZERO;
			new Exception("DIV BY ZERO").printStackTrace();
		}
		double q = lambda / denum;
		// System.err.println("lambda = "+lambda+" denum="+denum+" q="+q);
		for (ScheduleResource resource : resources) {
			OfflineAnt ant = (OfflineAnt) resource;
			ant.spreadPheromoneOnPath(q);
		}
	}

	@SuppressWarnings("unchecked")
	protected ScheduleTask<OfflineEdge> getLocation() {
		List<ScheduleEdge> path = score.getPath();
		if (path.size() == 0)
			return (ScheduleTask<OfflineEdge>) MissionScheduler.getInstance().SOURCE_NODE;
		else
			return (ScheduleTask<OfflineEdge>) (path.get(path.size() - 1)
					.getNodeTo());
	}
}
