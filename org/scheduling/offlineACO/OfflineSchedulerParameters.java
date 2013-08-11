package org.scheduling.offlineACO;

public class OfflineSchedulerParameters {
	//IMPORTANCE OF THE PHEROMONE TRACK IN THE CHOICE OF THE DESTINATION
	private double alpha;
	//IMPORTANCE OF EDGE WEIGHT IN THE CHOICE OF THE DESTINATION
	private double beta;
	//IMPORTANCE OF FOREIGN PHEROMONE TRACK IN THE CHOICE OF THE DESTINATION
	private double gamma;

	//Quantity of pheromone to spread each time an ant choose a node
	private double lambda;
	
	//Evaporation rate
	private double rho;
	
	//Weight of the distance critera
	private double F1;
	//Weight of the lateness critera
	private double F2;
	//Weight of the earliness critera
	private double F3;
	
	//Synchronization time with the simulation (aco steps per simulation step)
	private int sync;
	
	/**
	 * Constructor
	 * @param alpha Importance of the pheromone track in the choice of the destination
	 * @param beta Importance of the edge weight in the choice of the destination
	 * @param gamma Importance of foreign pheromone track in the choice of the destination
	 * @param delta Threshold validation of a chosen destination (if( (ph(c)/sum(ph)) > delta) then validate choice else go back to depot)
	 * @param rho Persistence of the previous pheromone track (in [0,1])
	 * @param lambda Quantity of pheromone to spread each time an ant choose a node
	 * @param F1
	 * @param F2
	 * @param F3
	 */
	public OfflineSchedulerParameters (double alpha, double beta, double gamma/*, double delta*/, double rho, double lambda, int sync, double F1, double F2, double F3){
		this.alpha = alpha;
		this.beta = beta;
		this.gamma = gamma;
		this.rho = rho;
		this.lambda = lambda;
		this.sync = sync;
		this.F1 = F1;
		this.F2 = F2;
		this.F3 = F3;
	}
	
	public int getSync(){
		return sync;
	}
	
	public double getLambda(){
		return lambda;
	}
	/**
	 * @Discouraged  
	 * @param newLambda
	 */
	public void setLambda(double newLambda){
		this.lambda = newLambda;
	}
	
	public double getGamma(){
		return gamma;
	}
	
	public double getAlpha(){
		return alpha;
	}
	
	public double getBeta(){
		return beta;
	}
	
	public double getRho(){
		return rho;
	}
	
	public double getF1(){
		return F1;
	}
	
	public double getF2(){
		return F2;
	}
	
	public double getF3(){
		return F3;
	}
	
	@Override
	public String toString(){
		return "Q="+lambda+" ALPHA: "+alpha+" BETA: "+beta+" GAMMA: "+gamma+" PERSISTENCE: "+rho+" F1="+F1+" F2="+F2+" F3="+F3; 
	}
}
