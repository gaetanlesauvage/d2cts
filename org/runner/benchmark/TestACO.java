package org.runner.benchmark;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.conf.parameters.ReturnCodes;
import org.exceptions.ContainerDimensionException;
import org.exceptions.EmptyLevelException;
import org.exceptions.NoPathFoundException;
import org.xml.sax.SAXException;


public class TestACO {
	/**
	 * @param args
	 * @throws IOException 
	 * @throws EmptyLevelException 
	 * @throws ContainerDimensionException 
	 * @throws NoPathFoundException 
	 * @throws SAXException 
	 */
	public static void main(String[] args) throws IOException, SAXException, NoPathFoundException, ContainerDimensionException, EmptyLevelException {
		String home = "../trunk/xml/results/";
		
		SimpleDateFormat sdf = new SimpleDateFormat("ddMMyyyy_HH:mm:ss");
		String date = sdf.format(new Date());
		String resultsFile = home+"results_"+date+".dat";
		String analyzeFile = home+"analyze_"+date+".csv";
		
		PrintWriter out = new PrintWriter(new File("../trunk/xml/results/BenchmarkRunner_"+date+".out"));
		

		
		long[] seeds = {42};
		long[] terminalSeeds = {100};
		int[] jobsSize = {10};
		int[][] resourcesSize = { {2,3,4}};
		double[] dods = {0.0, 0.5, 1.0};
		double[][] edods = {{0.0}, {0.33, 0.66, 1.0}, {0.33, 0.66, 1.0}};
		
		HashMap<String, String> aco1_1_100_Params = new HashMap<String, String>();
		aco1_1_100_Params.put("alpha", "1.0");
		aco1_1_100_Params.put("beta", "1.0");
		aco1_1_100_Params.put("gamma", "1.0");
		aco1_1_100_Params.put("delta", "0.0");
		aco1_1_100_Params.put("persistence", "0.99");
		aco1_1_100_Params.put("Q", "1");
		aco1_1_100_Params.put("QR", "0");
		aco1_1_100_Params.put("sync","100");
		aco1_1_100_Params.put("F1","1.0");
		aco1_1_100_Params.put("F2","1.0");
		aco1_1_100_Params.put("F3","1.0");
		
		HashMap<String, String> aco1_1_1000_Params = new HashMap<String, String>();
		aco1_1_1000_Params.put("alpha", "1.0");
		aco1_1_1000_Params.put("beta", "1.0");
		aco1_1_1000_Params.put("gamma", "1.0");
		aco1_1_1000_Params.put("delta", "0.0");
		aco1_1_1000_Params.put("persistence", "0.99");
		aco1_1_1000_Params.put("Q", "1");
		aco1_1_1000_Params.put("QR", "0");
		aco1_1_1000_Params.put("sync","1000");
		aco1_1_1000_Params.put("F1","1.0");
		aco1_1_1000_Params.put("F2","1.0");
		aco1_1_1000_Params.put("F3","1.0");
				
			
		BenchmarkAlgorithm greedy = new BenchmarkAlgorithm("Greedy", "Greedy", null);
		//BenchmarkAlgorithm[] algoTypes = {greedy};
	
		BenchmarkAlgorithm aco1 = new BenchmarkAlgorithm("ACO_F1=F2=1_Sync=100", "ACO", aco1_1_100_Params);
		BenchmarkAlgorithm aco2 = new BenchmarkAlgorithm("ACO_F1=F2=1_Sync=1000", "ACO", aco1_1_1000_Params);
		BenchmarkAlgorithm linear = new BenchmarkAlgorithm("Linear", "Linear", null);
		
		BenchmarkAlgorithm[] algoTypes = {aco1,aco2,linear,greedy};
		
		long before = System.nanoTime();
		BenchmarkRunner.prepare(home,jobsSize,resourcesSize,seeds, terminalSeeds, dods, edods, algoTypes);
		long after =  System.nanoTime();
		long computingTime = after - before;
		out.println("Tests preparing time: "+BenchmarkRunner.getTime(computingTime));
		out.flush();

		
		//resultsFile = home+"results_08032012_16:02:11.dat";
		//analyzeFile = home+"analyze_08032012_16:02:11.csv";
		//resultsFile = home+"results_08032012_19:08:16.dat";
		//analyzeFile = home+"analyze_08032012_19:08:16.csv";
		//resultsFile = home+"results_09032012_09:51:42.dat";
		//analyzeFile = home+"analyze_09032012_09:51:42.csv";
		//resultsFile = home+"results_09032012_15:00:28.dat";
		//analyzeFile = home+"analyze_09032012_15:00:28.csv";
		//resultsFile = home+"results_09032012_17:04:09.dat";
		//analyzeFile = home+"analyze_09032012_17:04:09.csv";
		//resultsFile = home+"results_11032012_11:56:35.dat";
		//analyzeFile = home+"analyze_11032012_11:56:35.csv";
		//resultsFile = home+"results_12032012_11:07:17.dat";
		//analyzeFile = home+"analyze_12032012_11:07:17.csv";
		
		before = System.nanoTime();
		List<ConfigFile> configFiles = BenchmarkRunner.getConfigFiles(home, jobsSize, resourcesSize, seeds, terminalSeeds, dods, edods, algoTypes);
		after =  System.nanoTime();
		computingTime = after - before;
		out.println("Getting config files time: "+BenchmarkRunner.getTime(computingTime));
		out.flush();

		long stepMAX = 6000;
		before = System.nanoTime();
		BenchmarkRunner.run(configFiles, resultsFile, stepMAX);
		after =  System.nanoTime();
		computingTime = after - before;
		out.println("Tests processing time: "+BenchmarkRunner.getTime(computingTime));
		out.flush();

		before = System.nanoTime();
		BenchmarkRunner.analyze(resultsFile, analyzeFile);
		after =  System.nanoTime();
		computingTime = after - before;
		out.println("Analyze processing time: "+BenchmarkRunner.getTime(computingTime));

		out.flush();
		out.close();
		System.exit(ReturnCodes.EXIT_ON_SUCCESS.getCode());
	}
}
