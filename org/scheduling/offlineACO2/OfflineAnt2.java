package org.scheduling.offlineACO2;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.scheduling.GlobalScore;
import org.scheduling.LocalScore;
import org.scheduling.MissionScheduler;
import org.scheduling.ScheduleEdge;
import org.scheduling.ScheduleResource;
import org.scheduling.offlineACO.OfflineDestinationChooserHelper;
import org.scheduling.offlineACO.OfflineSchedulerParameters;
import org.system.Terminal;

public class OfflineAnt2 {
	public static double maxPHSpread = 0;
	public static double maxAlpha = 0;
	public static double maxBeta = 0;
	public static double maxGamma = 0;

	public static double minPHSpread = Double.POSITIVE_INFINITY;
	public static double minAlpha = Double.POSITIVE_INFINITY;
	public static double minBeta = Double.POSITIVE_INFINITY;
	public static double minGamma = Double.POSITIVE_INFINITY;

	public static double sumPH = 0;
	public static double sumAlpha = 0;
	public static double sumBeta = 0;
	public static double sumGamma = 0;

	public static int phTimes = 0;
	public static int alphaTimes = 0;
	public static int betaTimes = 0;
	public static int gammaTimes = 0;

	public static int antCounter = 0; // TODO Automatic handling...
	private GlobalScore currentScore;
	private OfflineNode2 location;
	private ScheduleResource currentSalesman;
	private String ID;

	private List<OfflineNode2> nonVisited;
	private Set<String> visited;
	private int visitDepotAuthorizedTimes;
	private Set<String> usedSalesman;

	public OfflineAnt2() {
		antCounter++;
		this.ID = "Ant_" + antCounter;
		// reset();
	}

	public void reset() {
		location = (OfflineNode2) MissionScheduler.SOURCE_NODE;
		currentScore = new GlobalScore();
		nonVisited = OfflineACOScheduler2.getNodes();
		currentSalesman = OfflineACOScheduler2.getASalesman();
		visited = new HashSet<String>(nonVisited.size());
		usedSalesman = new HashSet<String>(MissionScheduler.getInstance().getResources().size());
		visitDepotAuthorizedTimes = MissionScheduler.getInstance().getResources().size() - 1;
	}

	public static void resetAll(){
		maxPHSpread = 0;
		maxAlpha = 0;
		maxBeta = 0;
		maxGamma = 0;

		minPHSpread = Double.POSITIVE_INFINITY;
		minAlpha = Double.POSITIVE_INFINITY;
		minBeta = Double.POSITIVE_INFINITY;
		minGamma = Double.POSITIVE_INFINITY;
		
		sumPH = 0;
		sumAlpha = 0;
		sumBeta = 0;
		sumGamma = 0;

		phTimes = 0;
		alphaTimes = 0;
		betaTimes = 0;
		gammaTimes = 0;
	}
	
	public String getID() {
		return ID;
	}

	public GlobalScore getGlobalScore() {
		return currentScore;
	}

	public void compute() {
		//reset();

		// System.err.println("CPTE ANT "+getID());
		while (nonVisited.size() > 0) {
			step();
		}
		gotoDepot();
		usedSalesman.add(currentSalesman.getID());
	}

	private void goTo(OfflineEdge2 choice) {
		LocalScore ls = currentScore.getSolution().get(currentSalesman.getID());
		if (ls == null)
			ls = new LocalScore(currentSalesman);

		currentScore.addScore(currentSalesman.simulateVisitNode(ls, (OfflineNode2) choice.getNodeTo()));
		visited.add(choice.getNodeTo().getID());
		for (int i = 0; i < nonVisited.size(); i++) {
			OfflineNode2 n = nonVisited.get(i);
			if (n == choice.getNodeTo()) {
				nonVisited.remove(i);
				break;
			}
		}
	}

	private void step() {
		OfflineEdge2 choice = chooseDestination();
		if (choice.getNodeTo() != MissionScheduler.SOURCE_NODE) {
			goTo(choice);
		} else {
			gotoDepot();
			usedSalesman.add(currentSalesman.getID());
			currentSalesman = OfflineACOScheduler2.getASalesman(usedSalesman);
			visitDepotAuthorizedTimes--;
		}
	}

	public void setCurrentSalesman(ScheduleResource r) {
		if (!usedSalesman.contains(r.getID()))
			usedSalesman.add(r.getID());
		this.currentSalesman = r;
	}

	public static void spreadPheromone(GlobalScore gs) {
		// Spread pheromone according to local score (or global one ?)
		double lambda = OfflineACOScheduler2.getInstance().getGlobalParameters().getLambda();
		double denum = gs.getScore(); // Global score
		if (denum == 0.0) {
			denum = MissionScheduler.CLOSE_TO_ZERO;
			new Exception("DIV BY ZERO").printStackTrace();
		}

		// double q = lambda * Math.pow(lambda, 1/denum);
		double q = lambda / denum;
		sumPH += q;
		phTimes++;

		if (q < minPHSpread)
			minPHSpread = q;
		if (q > maxPHSpread)
			maxPHSpread = q;

		for (LocalScore score : gs.getSolution().values()) {
			List<ScheduleEdge> path = score.getPath();
			for (int i = 0; i < path.size(); i++) {
				OfflineEdge2 e = (OfflineEdge2) path.get(i);
				e.addPheromone(score.getResource().getID(), q);
			}
		}
	}

	public void spreadPheromone() {
		spreadPheromone(currentScore);
	}

	/**
	 * @return
	 */
	/**
	 * @return
	 */
	@SuppressWarnings("resource")
	private OfflineEdge2 chooseDestination() {
		// System.err.println("Choose destination for ant of "+colony.getID()+" from "+location.getID());
		OfflineEdge2 destination = null;

		// Among possible destinations, remove already visited ones
		List<OfflineEdge2> destinations = location.getDestinations();
		if (MissionScheduler.DEBUG)
			System.err.println("Ant " + ID + " at " + location.getID() + " with " + currentSalesman.getID());
		List<OfflineEdge2> available = new ArrayList<OfflineEdge2>(destinations.size());
		for (OfflineEdge2 e : destinations) {
			if ((e.getNodeTo() == MissionScheduler.SOURCE_NODE && visitDepotAuthorizedTimes > 0)
					|| ((e.getNodeTo() != MissionScheduler.SOURCE_NODE) && !visited.contains(e.getNodeTo().getID()))) {
				// if from depot
				if (e.getNodeFrom() == MissionScheduler.SOURCE_NODE) {
					// if to depot
					if (e.getNodeTo() == MissionScheduler.SOURCE_NODE) {
						available.add(e);
					} else {
						if (e.getCost(currentSalesman.getID()) != Double.POSITIVE_INFINITY) {
							available.add(0, e);
						}
					}
				} else {
					// if to depot
					if (e.getNodeTo() == MissionScheduler.SOURCE_NODE) {
						available.add(e);
					} else {
						if (e.getCost(currentSalesman.getModelID()) != Double.POSITIVE_INFINITY) {
							available.add(0, e);
						}
					}
				}
			}
		}

		if (available.size() == 0) {
			new Exception("No available node !").printStackTrace();
			destination = location.getEdgeTo((OfflineNode2) MissionScheduler.SOURCE_NODE);
		} else {
			// System.err.println("ELSE");
			OfflineSchedulerParameters parameters = OfflineACOScheduler2.getInstance().getGlobalParameters();
			ArrayList<OfflineDestinationChooserHelper> choices = new ArrayList<OfflineDestinationChooserHelper>(available.size());
			double handlingTime = currentSalesman.getHandlingTime();
			double currentTime = currentSalesman.getCurrentTime();
			boolean addSourceSourceProba = false;
			double sumProba = 0.0;

			double sumWeight = 0.0;
			double sumPheromone = 0.0;
			double sumForeignPheromone = 0.0;

			for (int i = 0; i < available.size(); i++) {
				OfflineEdge2 e = available.get(i);
				// if(MissionScheduler.DEBUG)
				// System.err.println("studying "+e.getID());
				// DISTANCE IN SECONDS IF FOLLOW THIS EDGE
				double cost = 0.0;
				if (e.getNodeFrom() != MissionScheduler.SOURCE_NODE)
					cost = e.getCost(currentSalesman.getModelID());
				else if (e.getNodeTo() == MissionScheduler.SOURCE_NODE)
					cost = 0.0;
				else
					cost = e.getCost(currentSalesman.getID());

				double over = 0;
				double under = 0;
				double ds = 0;
				if (e.getNodeTo() != MissionScheduler.SOURCE_NODE) {
					double dsP = cost;
					double pickupReachTime = currentTime + cost;

					double overTimeP = 0;
					double waitTimeP = 0;
					double twpMin = e.getNodeTo().getMission().getPickupTimeWindow().getMin().getInSec();
					double twpMax = e.getNodeTo().getMission().getPickupTimeWindow().getMax().getInSec();
					double pickupLeaveTime = twpMin + handlingTime;
					if (location != MissionScheduler.SOURCE_NODE && pickupReachTime < twpMin) {
						waitTimeP += twpMin - pickupReachTime;
					}

					// IF late @ P
					if ((pickupReachTime + handlingTime) > twpMax) {
						overTimeP = pickupReachTime + handlingTime - twpMax;
						pickupLeaveTime = pickupReachTime + handlingTime;
					}

					double dsPD = e.getNodeTo().getCost(currentSalesman.getModelID());
					double deliveryReachTime = pickupLeaveTime + dsPD;

					double twdMin = e.getNodeTo().getMission().getDeliveryTimeWindow().getMin().getInSec();
					double twdMax = e.getNodeTo().getMission().getDeliveryTimeWindow().getMax().getInSec();
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
					over = overTimeP + overTimeD;
					under = waitTimeP + waitTimeD;
					ds = dsP + dsPD;
				} else {
					ds = cost;
				}
				double weight = ds * parameters.getF1() + over * parameters.getF2() + under * parameters.getF3();
				if (weight > 0) {
					double ph = e.getPheromone(currentSalesman.getID());
					double foreignPH = e.getForeignPheromone(currentSalesman.getID());
					// double localProba =
					// Math.pow(ph,parameters.getAlpha())*Math.pow(1f/weight,
					// parameters.getBeta())*Math.pow(ph/(foreignPH+ph),parameters.getGamma());
					// System.err.println("for "+e.getID()+" T = "+time+" e.cost="+new
					// Time(cost+"s")+" Local proba = "+localProba+" under="+new
					// Time(under+"s")+" over="+new Time(over+"s"));
					// sumProba += localProba;

					OfflineDestinationChooserHelper helper = new OfflineDestinationChooserHelper(e, ph, weight, foreignPH);
					choices.add(helper);
					sumWeight += weight;
					sumPheromone += ph;
					sumForeignPheromone += foreignPH;
					// if(MissionScheduler.DEBUG)
					// System.err.println("studying "+e.getID()+" sumW="+sumWeight+" sumPh="+sumPheromone+" sumForPh="+sumForeignPheromone+" choices.size()="+choices.size()+" available.size()="+available.size());
					// choices.add(new OfflineDestinationChooserHelper(e,
					// localProba));
				} else {
					addSourceSourceProba = true;
				}
			}
			if (addSourceSourceProba) {
				/*
				 * if(MissionScheduler.DEBUG){
				 * System.err.println("ADD SOURCE PROBA : sumW="
				 * +sumWeight+" sumPh="
				 * +sumPheromone+" sumForPh="+sumForeignPheromone); }
				 */
				double avgWeight = sumWeight / (choices.size() + 0.0);
				double avgPheromone = sumPheromone / (choices.size() + 0.0);
				double avgForeignPheromone = sumForeignPheromone / (choices.size() + 0.0);
				/*
				 * if(MissionScheduler.DEBUG) {
				 * System.err.println("ADD SOURCE PROBA : avgW="
				 * +avgWeight+" avgPh="
				 * +avgPheromone+" avgForPh="+avgForeignPheromone); }
				 */
				sumWeight += avgWeight;
				sumPheromone += avgPheromone;
				sumForeignPheromone += avgPheromone;

				OfflineEdge2 e = ((OfflineNode2) MissionScheduler.SOURCE_NODE)
						.getEdgeTo((OfflineNode2) MissionScheduler.SOURCE_NODE);

				OfflineDestinationChooserHelper helper = new OfflineDestinationChooserHelper(e, avgPheromone, avgWeight, avgForeignPheromone);
				choices.add(helper);

			}

			if (choices.size() > 1) {
				for (OfflineDestinationChooserHelper dch : choices) {
					double probaAlpha = Math.pow(1.0 + (dch.getPheromone() / sumPheromone), parameters.getAlpha());
					if (probaAlpha > maxAlpha)
						maxAlpha = probaAlpha;
					if (probaAlpha < minAlpha)
						minAlpha = probaAlpha;
					sumAlpha += probaAlpha;
					alphaTimes++;

					double probaBeta = Math.pow(sumWeight / dch.getWeight(), parameters.getBeta());

					if (probaBeta > maxBeta)
						maxBeta = probaBeta;
					if (probaBeta < minBeta)
						minBeta = probaBeta;
					sumBeta += probaBeta;
					betaTimes++;

					// double probaGamma = Math.pow(1.0 +
					// ((dch.getPheromone()/(dch.getForeignPheromone()+dch.getPheromone()))/(sumPheromone+sumForeignPheromone)),parameters.getGamma());
					// TODO DEBUG SEES IF IT IS CORRECT THIS WAY:
					double probaGamma = Math
							.pow(1.0 + ((dch.getPheromone() / (dch.getForeignPheromone() + dch.getPheromone())) / (sumPheromone / (sumPheromone + sumForeignPheromone))),
									parameters.getGamma());
					if (probaGamma > maxGamma)
						maxGamma = probaGamma;
					if (probaGamma < minGamma)
						minGamma = probaGamma;
					sumGamma += probaGamma;
					gammaTimes++;

					double proba = probaAlpha * probaBeta * probaGamma;
					/**
					 * TODO TESTING LINEAR VERSION OF THE FORMULA... double
					 * probaAlpha = (1.0 +
					 * (dch.getPheromone()/sumPheromone))*parameters.getAlpha();
					 * double probaBeta = (sumWeight /
					 * dch.getWeight())*parameters.getBeta(); double probaGamma
					 * = (1.0 +
					 * ((dch.getPheromone()/(dch.getForeignPheromone()+dch
					 * .getPheromone
					 * ()))/(sumPheromone+sumForeignPheromone)))*parameters
					 * .getGamma(); double proba =
					 * probaAlpha+probaBeta+probaGamma;
					 */

					if (probaAlpha == Double.POSITIVE_INFINITY || probaBeta == Double.POSITIVE_INFINITY || probaGamma == Double.POSITIVE_INFINITY)
						new Exception("Infinity Exception in computation of probability of " + dch.getDestination().getID()).printStackTrace();
					dch.setProba(proba);
					if (MissionScheduler.DEBUG)
						System.err.println(dch.getDestination().getID() + " localProba=" + probaAlpha + " . " + probaBeta + " . " + probaGamma
								+ " = " + proba);
					sumProba += proba;
				}
				// System.err.println("2) IF");
				// Normalization for probabilities between [0,1]
				for (OfflineDestinationChooserHelper dch : choices) {
					dch.setProba(dch.getProba() / sumProba);
					if (MissionScheduler.DEBUG)
						System.err.println("1) " + dch.getDestination().getID() + " p=" + dch.getProba());
				}

				// Collections.sort(choices);

				// Cumulate the probabilities
				double overall = 0.0;
				for (OfflineDestinationChooserHelper dch : choices) {
					overall += dch.getProba();
					dch.setProba(overall);
					// System.err.println("2) "+dch.getDestination().getID()+" p="+dch.getProba());
				}

				double dChoice = Terminal.getInstance().getRandom().nextDouble();
				// System.err.println("RANDOM = "+dChoice);
				for (OfflineDestinationChooserHelper dch : choices) {
					// System.err.println(dch.getProba()+">= "+dChoice+"?");
					if (dch.getProba() >= dChoice) {
						// System.err.println("=> TRUE");
						destination = (OfflineEdge2) dch.getDestination();
						// System.err.println("=> "+destination.getID());
						// new
						// Exception("1) Destination = "+destination.getNodeFrom()+" -> "+destination.getNodeTo()).printStackTrace();
						break;
					}
					// else System.err.println("=> FALSE");
				}
				if (destination == null) {
					destination = (OfflineEdge2) choices.get(choices.size() - 1).getDestination();
					// System.err.println("=> "+destination.getID());
					// new
					// Exception("1-2) Destination = "+destination.getNodeFrom()+" -> "+destination.getNodeTo()).printStackTrace();
				}
			} else {
				// System.err.println("2) ELSE");
				destination = (OfflineEdge2) choices.get(choices.size() - 1).getDestination();
				// System.err.println("=> "+destination.getID());
				// new
				// Exception("2) Destination = "+destination.getNodeFrom()+" -> "+destination.getNodeTo()).printStackTrace();
			}

			for (OfflineDestinationChooserHelper choice : choices)
				choice.destroy();
		}

		if (MissionScheduler.DEBUG) {

			System.err.println("CHOICE : " + destination.getID() + "\nPRESS ENTER");
			new java.util.Scanner(System.in).nextLine();
		}

		return destination;
	}

	private void gotoDepot() {
		if (location != MissionScheduler.SOURCE_NODE) {
			OfflineEdge2 edge = location.getEdgeTo((OfflineNode2) MissionScheduler.SOURCE_NODE);
			// System.err.println("GoBack Edge : "+edge.getID());

			// Current Location to Pickup location of the next mission
			LocalScore score = currentScore.getSolution().get(currentSalesman.getID());
			score.addDistance(edge.getDistance());
			score.addTravelTime(edge.getCost(currentSalesman.getID()));

			// Update Time after reaching Depot
			currentSalesman.visitNode(MissionScheduler.SOURCE_NODE);
			score.addEdge(edge, currentSalesman.getCurrentTime());
			currentScore.addScore(score);
		}
	}

}
