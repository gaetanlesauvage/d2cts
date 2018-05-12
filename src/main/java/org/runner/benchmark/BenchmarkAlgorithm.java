package org.runner.benchmark;

import java.util.HashMap;

public class BenchmarkAlgorithm{
	String algo;
	String ID;
	HashMap<String, String> parameters;
	
	public BenchmarkAlgorithm(String ID, String algo, HashMap<String, String> parameters){
		this.ID = ID;
		this.algo = algo;
		this.parameters = parameters;
	}
}