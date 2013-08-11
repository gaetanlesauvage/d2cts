package org.runner.benchmark;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.Scanner;
import java.util.StringTokenizer;
import java.util.TreeMap;

import org.conf.parameters.ReturnCodes;
import org.exceptions.ContainerDimensionException;
import org.exceptions.EmptyLevelException;
import org.exceptions.NoPathFoundException;
import org.missions.Mission;
import org.runner.SimulationRunner;
import org.scheduling.MissionScheduler;
import org.scheduling.display.IndicatorPane;
import org.system.Terminal;
import org.time.Time;
import org.time.TimeController;
import org.util.Edodizer;
import org.util.generators.MissionsFileGenerator;
import org.xml.sax.SAXException;

public class BenchmarkRunner {
	private static final NumberFormat nf = new DecimalFormat("#.###");
	/*
	 * public static void copyFile(String fSource, String fDestination){
	 * FileChannel in = null; // canal d'entrée FileChannel out = null; // canal
	 * de sortie try { // Init in = new FileInputStream(fSource).getChannel();
	 * File fOut = new File(fDestination); if(!fOut.exists()) {
	 * fOut.getParentFile().mkdirs(); }
	 * 
	 * out = new FileOutputStream(fDestination).getChannel(); // Copie depuis le
	 * in vers le out in.transferTo(0, in.size(), out); } catch (Exception e) {
	 * e.printStackTrace(); // n'importe quelle exception } finally { //
	 * finalement on ferme if(in != null) { try { in.close(); } catch
	 * (IOException e) {} } if(out != null) { try { out.close(); } catch
	 * (IOException e) {} } } }
	 */

	private static String getMGenFile(long seed, String outputFile, int jobSize) {
		StringBuffer buffer = new StringBuffer();
		buffer.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<document>\n");
		buffer.append("\t<random seed='" + seed + "'/>\n");
		buffer.append("\t<terminalFile file='xml/defaultData/TN.xml'/>\n");
		buffer.append("\t<vehiclesFile file='xml/defaultData/vehicleLocal.xml'/>\n");
		buffer.append("\t<containersFile file='xml/results/containers.cont'/>\n");
		buffer.append("\t<stocks outputFile='"
				+ outputFile
				+ "' nb='"
				+ jobSize
				+ "' minTime='00:00:00' maxTime='00:45:00' marginTime='00:02:00' groupID='A'/>\n");
		buffer.append("</document>");
		buffer.trimToSize();
		return buffer.toString();
	}

	public static String getDeploymentFile(BenchmarkAlgorithm algoType,
			String tnFileName, String vehicleFileName, long terminalSeed) {
		StringBuffer buffer = new StringBuffer();
		buffer.append("<?xml version=\"1.0\" encoding=\"ISO-8859-1\" ?>\n<document>\n\t<networkConfiguration hostname=\"localhost\" port=\"2000\"/>\n");
		buffer.append("\t<remoteObject rmiBindingName=\"TerminalImpl\" host=\"localhost\"/>\n\t<remoteObject rmiBindingName=\"display\" host=\"localhost\"/>\n");
		buffer.append("\t<remoteObject rmiBindingName=\"JTerminal\" host=\"localhost\" id=\"JTerminal1\"/>\n");

		buffer.append("\t<remoteObject rmiBindingName=\"MissionScheduler\" type='"
				+ algoType.algo + "Scheduler' ");

		if (algoType.parameters != null) {
			for (String paramName : algoType.parameters.keySet()) {
				buffer.append(paramName + "='"
						+ algoType.parameters.get(paramName) + "' ");
			}
		}
		buffer.append("host=\"localhost\" out=\"localhost\"/>\n");

		buffer.append("\t<remoteObject rmiBindingName=\"LaserData\" host=\"localhost\"/>\n\t<remoteObject rmiBindingName=\"TimeScheduler\" host=\"localhost\"/>\n");
		buffer.append("\t<remoteObject rmiBindingName=\"TimeController\" host=\"localhost\"/>\n\t<remoteObject rmiBindingName=\"XMLTerminalComponentParser\" host=\"localhost\"/>\n");
		buffer.append("\t<random seed='"
				+ terminalSeed
				+ "'/>\n\t<laserSystemFile file=\"xml/bornesTN-LARGE_RANGE.xml\"/>\n\t<terminalFile file=\""
				+ tnFileName + "\"/>\n\t<clientFile file=\"" + vehicleFileName
				+ "\"/>\n");
		buffer.append("</document>");
		buffer.trimToSize();
		return buffer.toString();
	}

	/**
	 * Creates n tests files for given resources and jobs problem sizes and
	 * degree of dynamism.
	 */
	public static void prepare(String home, int[] jobsSize,
			int[][] resourcesSize, long[] seeds, long[] terminalSeeds,
			double[] dods, double[][] edods, BenchmarkAlgorithm[] algoTypes)
			throws SAXException, IOException, NoPathFoundException,
			ContainerDimensionException, EmptyLevelException {
		String contFileName = "xml/results/containers.cont";

		// Generate n file according to seed
		for (int seed = 1; seed <= seeds.length; seed++) {
			for (int i = 0; i < jobsSize.length; i++) {

				String baseDir = home + jobsSize[i] + "missions/";
				new File(baseDir).mkdirs();
				String fname = baseDir + "mGen_dynamic_" + jobsSize[i] + "_"
						+ seeds[seed - 1] + ".mgen";
				File f = new File(fname);

				String oFile = baseDir + "missions_" + seeds[seed - 1] + ".xml";
				File contFile = new File(contFileName);
				new File("xml/results/").mkdirs();
				if (!contFile.exists()) {
					org.apache.commons.io.FileUtils.copyFile(new File(
							"xml/testData/dynamic/containers.cont"), new File(
							"../trunk/" + contFileName));
					org.apache.commons.io.FileUtils.copyFile(new File(
							"xml/testData/dynamic/containers.cont"), new File(
							contFileName));
					// copyFile("xml/testData/dynamic/containers.cont",
					// "../trunk/"+contFileName);
					// copyFile("xml/testData/dynamic/containers.cont",
					// contFileName);
				}

				PrintWriter pw = new PrintWriter(f);
				pw.append(getMGenFile(seeds[seed - 1], oFile, jobsSize[i]));
				pw.flush();
				pw.close();

				System.out.println("BUILDING " + fname + " ...");
				new MissionsFileGenerator("localhost", fname);

				org.apache.commons.io.FileUtils.copyFile(new File(oFile),
						new File(oFile.replaceAll("../trunk/", "")));
				// copyFile(oFile, oFile.replaceAll("../trunk/", ""));
				HashMap<Mission, Time> missionMap = Edodizer
						.getMissionMap(oFile.replaceAll("../trunk/", ""));

				// EDODIZER
				for (int dodIndex = 0; dodIndex < dods.length; dodIndex++) {
					double dod = dods[dodIndex];
					String sDod = nf.format(dod);
					double[] edodOfDod = edods[dodIndex];
					for (int edodIndex = 0; edodIndex < edodOfDod.length; edodIndex++) {
						double edod = edodOfDod[edodIndex];
						String sEdod = nf.format(edod);
						HashMap<Mission, Time> edodizedMap = Edodizer.edodize(
								new HashMap<Mission, Time>(missionMap), dod,
								edod, new Random(seeds[seed - 1]));

						String edodizeFileName = oFile.substring(0,
								oFile.length() - ".xml".length())
								+ "_" + sDod + "_" + sEdod + ".xml";
						Edodizer.write(new File(edodizeFileName), edodizedMap);

						// Create TN.xml with created mission file
						String TNMissionlessFileName = "xml/testData/TN_missionless.xml";
						Scanner scan = new Scanner(new File(
								TNMissionlessFileName));
						String tnFileName = baseDir + "TN_" + seeds[seed - 1]
								+ "_" + sDod + "_" + sEdod + ".xml";
						pw = new PrintWriter(new File(tnFileName));
						while (scan.hasNextLine()) {
							String line = scan.nextLine();
							if (line.contains("</terminal>")) {
								pw.append("<include file='" + contFileName
										+ "'/>\n");
								pw.append("<include file='"
										+ edodizeFileName.replaceAll(
												"../trunk/", "") + "'/>\n");
							}
							pw.append(line + "\n");
						}
						scan.close();
						pw.flush();
						pw.close();

						// Create deployment files
						new File(baseDir).mkdirs();
						// Take care of vehicle files
						for (int j = 0; j < resourcesSize[i].length; j++) {

							int size = resourcesSize[i][j];
							String resourceDir = baseDir + size + "vehicles/";
							new File(resourceDir).mkdir();
							// Vehicle file
							String vName = resourceDir + "vehicles-" + size
									+ ".xml";

							for (BenchmarkAlgorithm algoType : algoTypes) {
								for (int iTerminalSeed = 0; iTerminalSeed < terminalSeeds.length; iTerminalSeed++) {
									String dName = resourceDir + "deployment"
											+ algoType.ID + "_"
											+ seeds[seed - 1] + "_"
											+ terminalSeeds[iTerminalSeed]
											+ "_" + sDod + "_" + sEdod
											+ ".d2cts";
									f = new File(dName);
									pw = new PrintWriter(f);
									pw.append(getDeploymentFile(algoType,
											tnFileName.replaceAll("../trunk/",
													""), vName.replaceAll(
													"../trunk/", ""),
											terminalSeeds[iTerminalSeed]));
									pw.flush();
									pw.close();
								}
							}
						}

					}
				}
				for (int j = 0; j < resourcesSize[i].length; j++) {
					int size = resourcesSize[i][j];
					String resourceDir = baseDir + size + "vehicles/";
					new File(resourceDir).mkdir();
					// Vehicle file
					String vName = resourceDir + "vehicles-" + size + ".xml";

					File vehicleFile = new File(vName);
					pw = new PrintWriter(vehicleFile);
					String standardVFile = "xml/testData/vehicles-20.xml";
					boolean commentOpen = false;
					Scanner scan = new Scanner(new File(standardVFile));
					int count = 0;
					while (scan.hasNextLine()) {
						String line = scan.nextLine();
						if (line.contains("<straddleCarrier id=")) {
							count++;
							if (count == size + 1) {
								commentOpen = true;
								pw.append("<comment>\n");
							}
						}
						if (line.contains("</vehicles>") && commentOpen) {
							pw.append("</comment>\n");
						}
						pw.append(line + "\n");
					}
					pw.flush();
					pw.close();
					scan.close();
				}
			}
		}
		org.apache.commons.io.FileUtils.copyDirectory(new File(home), new File(
				home.replaceAll("../trunk/", "")));
	}

	public static List<ConfigFile> getConfigFiles(String home, int[] jobsSize,
			int[][] resourcesSize, long[] seeds, long[] terminalSeeds,
			double[] dods, double[][] edods, BenchmarkAlgorithm[] algoTypes) {

		List<ConfigFile> configFiles = new ArrayList<ConfigFile>();

		for (int index = 1; index <= seeds.length; index++) {
			long seed = seeds[index - 1];
			for (int i = 0; i < jobsSize.length; i++) {
				int jSize = jobsSize[i];
				String baseDir = home + jSize + "missions/";

				for (int dodIndex = 0; dodIndex < dods.length; dodIndex++) {
					double dod = dods[dodIndex];
					String sDod = nf.format(dod);
					double[] edodOfDod = edods[dodIndex];
					for (int edodIndex = 0; edodIndex < edodOfDod.length; edodIndex++) {
						double edod = edodOfDod[edodIndex];
						String sEdod = nf.format(edod);
						for (int j = 0; j < resourcesSize[i].length; j++) {
							int rSize = resourcesSize[i][j];
							String resourceDir = baseDir + rSize + "vehicles/";
							for (BenchmarkAlgorithm algoType : algoTypes) {
								for (long terminalSeed : terminalSeeds) {
									String dName = resourceDir + "deployment"
											+ algoType.ID + "_"
											+ seeds[index - 1] + "_"
											+ terminalSeed + "_" + sDod + "_"
											+ sEdod + ".d2cts";
									configFiles.add(new ConfigFile(dName,
											rSize, jSize, seed, terminalSeed,
											dod, edod, algoType));
								}
							}

						}
					}
				}
			}
		}
		return configFiles;
	}

	public static void run(List<ConfigFile> configFiles, String resultFile,
			long stepMAX) throws IOException {
		File f = new File(resultFile);
		f.createNewFile();
		PrintWriter writer = new PrintWriter(f);

		writer.append("TEST SESSION OF " + new Date().toString() + "\n");
		writer.flush();
		for (int testI = 0; testI < configFiles.size(); testI++) {
			ConfigFile configFile = configFiles.get(testI);
			String fName = configFile.file;

			writer.append("====================================================================================\n");
			writer.append("TEST " + (testI + 1) + "/" + configFiles.size()
					+ "\nCONFIG FILE: " + fName + "\n");
			writer.append("J=" + configFile.jobSize + "\tM="
					+ configFile.resourceSize + "\tDOD=" + configFile.dod
					+ "\tEDOD=" + configFile.edod + "\tSEED=" + configFile.seed
					+ "\tTERMINALSEED=" + configFile.terminalSeed + "\tALGO="
					+ configFile.algo.algo + "\n");
			writer.flush();
			SimulationRunner sc = new SimulationRunner(fName, null, false);
			while (!sc.hasDone()) {
				// System.out.println("waiting...");
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			System.out.println("done!");
			TimeController tc = new TimeController();
			if (configFile.algo.parameters != null)
				writer.append(configFile.algo.ID + " parameters: "
						+ configFile.getAlgoParameters() + "\n");
			writer.append("                            -------------------------------                            \n");
			writer.flush();
			long i = 1;
			long startTime = System.nanoTime();
			System.out.println("T=" + tc.getStep());
			while (i <= stepMAX) {
				if (i % 60 == 0)
					System.out.println("T=" + tc.getStep());
				tc.nextStep(false);
				i++;
			}
			long endTime = System.nanoTime();
			IndicatorPane ip = MissionScheduler.getInstance().getIndicatorPane();
				writer.append("RESULTS :\n" + ip + "\n");
			writer.append("Mission scheduler computing time:\t"
					+ Terminal.getInstance()
							.getSchedulingAlgorithmComputingTime() + "\n");
			double execTime = (endTime - startTime) / 1000000000.0;
			writer.append("EXEC TIME: " + execTime + "s\n");
			tc = null;
			ip = null;
			sc.destroy();
			while (!sc.hasDone()) {
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			writer.flush();
			System.gc();
			try {
				Thread.sleep(5000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			System.gc();
			writer.append("====================================================================================\n");
		}
		writer.close();
	}

	public static void analyze(String resultsFile, String analyzeFile)
			throws FileNotFoundException {
		Results lResults = new Results();
		Scanner scan = new Scanner(new File(resultsFile));
		while (scan.hasNextLine()) {
			String line = scan.nextLine();
			if (line.startsWith("CONFIG FILE")) {
				StringTokenizer tokenizer = new StringTokenizer(line);
				tokenizer.nextToken();
				tokenizer.nextToken();
				String file = tokenizer.nextToken();
				line = scan.nextLine();
				tokenizer = new StringTokenizer(line);
				int jSize = Integer.parseInt(tokenizer.nextToken()
						.replaceFirst("J=", ""));
				int rSize = Integer.parseInt(tokenizer.nextToken()
						.replaceFirst("M=", ""));
				double dod = Double.parseDouble(tokenizer.nextToken()
						.replaceFirst("DOD=", ""));
				double edod = Double.parseDouble(tokenizer.nextToken()
						.replaceFirst("EDOD=", ""));
				long seed = Long.parseLong(tokenizer.nextToken().replaceFirst(
						"SEED=", ""));
				long terminalSeed = Long.parseLong(tokenizer.nextToken()
						.replaceFirst("TERMINALSEED=", ""));
				String algo = tokenizer.nextToken().replaceFirst("ALGO=", "");
				String algoID = algo;
				HashMap<String, String> parameters = null;
				do {
					line = scan.nextLine();
					if (line.contains("parameters")) {
						parameters = new HashMap<String, String>();
						StringTokenizer st = new StringTokenizer(line);
						algoID = st.nextToken();
						st.nextToken();
						while (st.hasMoreTokens()) {
							String tk = st.nextToken();
							StringTokenizer st2 = new StringTokenizer(tk, "=");
							parameters.put(st2.nextToken(), st2.nextToken());
						}
					}
				} while (!line.startsWith("RESULTS"));

				ConfigFile cf = new ConfigFile(file, rSize, jSize, seed,
						terminalSeed, dod, edod, new BenchmarkAlgorithm(algoID,
								algo, parameters));
				Result r = new Result(cf);
				scan.nextLine();
				scan.nextLine();
				for (int i = 0; i < rSize; i++) {
					line = scan.nextLine();
					tokenizer = new StringTokenizer(line);
					String tmp = "";
					String rID = tokenizer.nextToken();
					tmp = tokenizer.nextToken();
					double distance = Double.parseDouble(tmp);
					Time overSpentTime = new Time(tokenizer.nextToken());
					int overruns = Integer.parseInt(tokenizer.nextToken());
					int completed = Integer.parseInt(tokenizer.nextToken());
					r.setDistance(rID, distance);
					r.setOverspentTime(rID, overSpentTime);
					r.setOverruns(rID, overruns);
					r.setCompletedMissions(rID, completed);
				}
				scan.nextLine();
				line = scan.nextLine();
				line = line.replaceFirst("OVERALL:", "");
				tokenizer = new StringTokenizer(line);
				String tmp = tokenizer.nextToken();
				double distance = Double.parseDouble(tmp);
				Time overSpentTime = new Time(tokenizer.nextToken());
				int overruns = Integer.parseInt(tokenizer.nextToken());
				int completed = Integer.parseInt(tokenizer.nextToken());
				r.setDistance(distance);
				r.setOverspentTime(overSpentTime);
				r.setOverruns(overruns);
				r.setCompletedMissions(completed);
				line = scan.nextLine();
				line.replaceFirst("Mission scheduler computing time:", "");
				tokenizer = new StringTokenizer(line, "\t");
				tokenizer.nextToken();
				String cptTime = tokenizer.nextToken();
				Time msCptTime = new Time(cptTime);
				line = scan.nextLine();
				Time execTime = new Time(line.replaceAll("EXEC TIME: ", ""));
				r.setMSComputingTime(msCptTime);
				r.setExecTime(execTime);
				lResults.add(r);
			}
		}
		scan.close();
		// getTerminalSeedsAnalyze(lResults,analyzeFile);
		getAlgoAnalyze(lResults, analyzeFile);
	}

	/*
	 * private static ArrayList<ArrayList<Result>>
	 * getComparableResults(ArrayList<Result> results){ HashMap<Result,
	 * ArrayList<Result>> comparable = new HashMap<Result, ArrayList<Result>>();
	 * 
	 * for(Result r : results){ ArrayList<Result> comparableList = new
	 * ArrayList<Result>(); for(Result r2 : results){ if(r!=r2 &&
	 * r.configFile.areJustTerminalSeedDifferent(r2.configFile)){
	 * comparableList.add(r2); } } comparable.put(r, comparableList); }
	 * 
	 * ArrayList<Result> tabu = new ArrayList<Result>();
	 * 
	 * ArrayList<ArrayList<Result>> groups = new ArrayList<ArrayList<Result>>();
	 * for(Result r : comparable.keySet()){
	 * 
	 * if(!tabu.contains(r)){
	 * 
	 * ArrayList<Result> l = comparable.get(r); ArrayList<Result> group = new
	 * ArrayList<Result>(); group.add(r);
	 * //System.out.println(r.configFile.file+" with "); for(Result r2 : l){
	 * if(!tabu.contains(r2)){ group.add(r2); tabu.add(r2); //
	 * System.out.println("\t"+r2.configFile.file); } } groups.add(group); } }
	 * return groups; }
	 */

	public static void getAlgoAnalyze(Results results, String outFile)
			throws FileNotFoundException {
		HashMap<String, ArrayList<String>> groupsSeedAlgo = results
				.getResultsByAlgoAndTSeed();
		HashMap<String, ArrayList<Result>> groups = results.resultsAlgo;

		TreeMap<String, Integer> tmpMap = new TreeMap<String, Integer>();

		int index = 0;
		for (Result r : results.results.values()) {
			if (!tmpMap.containsKey(r.configFile.algo.ID)) {
				tmpMap.put(r.configFile.algo.ID, index);
				index++;
			}
		}

		String[] algos = new String[tmpMap.size()];
		index = 0;
		for (String algo : tmpMap.keySet()) {
			tmpMap.put(algo, index++);
			algos[tmpMap.get(algo)] = algo;
		}
		// Tableau recapitulatif
		String[][] t = new String[groups.size() + 2][algos.length * 5 + 6];
		for (int i = 0; i < t.length; i++) {
			for (int j = 0; j < t[i].length; j++) {
				t[i][j] = "";
			}
		}
		index = 6;
		for (String algo : algos) {
			t[0][index] = algo;
			index += 5;
		}
		t[1][0] = "Jobs";
		t[1][1] = "Machines";
		t[1][2] = "GSeed";
		t[1][3] = "ESeed";
		t[1][4] = "DOD";
		t[1][5] = "EDOD";
		for (index = 0; index < algos.length; index++) {
			t[1][6 + index * 5] = "DISTANCE";
			t[1][7 + index * 5] = "OVERSPENT TIME";
			t[1][8 + index * 5] = "TW OVERRUN";
			t[1][9 + index * 5] = "MS CPT TIME";
			t[1][10 + index * 5] = "EXEC TIME";
		}

		File f = new File(outFile);
		f.getParentFile().mkdirs();
		PrintWriter pw = new PrintWriter(f);

		int line = 2;
		for (ArrayList<Result> l : groups.values()) {
			ConfigFile cf = l.get(0).configFile;
			t[line][0] = "" + cf.jobSize;
			t[line][1] = "" + cf.resourceSize;
			t[line][2] = "" + cf.seed;
			t[line][3] = "" + cf.terminalSeed;
			t[line][4] = "" + cf.dod;
			t[line][5] = "" + cf.edod;
			for (Result r : l) {
				t[line][tmpMap.get(r.configFile.algo.ID) * 5 + 6] = r.overall_distance
						+ "";
				t[line][tmpMap.get(r.configFile.algo.ID) * 5 + 7] = r.overall_overspentTime
						.toString();
				t[line][tmpMap.get(r.configFile.algo.ID) * 5 + 8] = r.overall_overruns
						+ "";
				t[line][tmpMap.get(r.configFile.algo.ID) * 5 + 9] = r.msComputingTime
						+ "";
				t[line][tmpMap.get(r.configFile.algo.ID) * 5 + 10] = r.execTime
						+ "";
			}
			line++;
		}
		for (int i = 0; i < t.length; i++) {
			pw.append("\t");
			for (int j = 0; j < t[i].length; j++) {
				pw.append(t[i][j] + "\t");
			}
			pw.append("\n");
		}
		pw.flush();

		// Tableaux elt par elt
		// Distance
		t = new String[groups.size() + 1][algos.length * algos.length + 6];
		String[][] tAVG = new String[1 + groupsSeedAlgo.size()][algos.length
				* algos.length + 6];
		t[0][0] = "Jobs";
		t[0][1] = "Machines";
		t[0][2] = "GSeed";
		t[0][3] = "ESeed";
		t[0][4] = "DOD";
		t[0][5] = "EDOD";
		tAVG[0][0] = "Jobs";
		tAVG[0][1] = "Machines";
		tAVG[0][2] = "GSeed";
		tAVG[0][3] = "ESeed";
		tAVG[0][4] = "DOD";
		tAVG[0][5] = "EDOD";
		index = 6;
		for (String algo : algos) {
			t[0][index] = algo;
			tAVG[0][index] = algo;
			index++;
		}
		for (String algo : algos) {
			for (String algo2 : algos) {
				if (!algo.equals(algo2)) {
					t[0][index] = algo + "->" + algo2;
					tAVG[0][index++] = algo + "->" + algo2;
				}
			}
		}

		line = 1;
		int iAvg = 1;
		for (String gpeSeedAlgoName : groupsSeedAlgo.keySet()) {
			double[] sum = new double[t[line].length];
			for (String gpeName : groupsSeedAlgo.get(gpeSeedAlgoName)) {
				ArrayList<Result> l = groups.get(gpeName);
				double[][] compare = new double[algos.length][algos.length - 1];
				ConfigFile cf = l.get(0).configFile;
				t[line][0] = "" + cf.jobSize;
				t[line][1] = "" + cf.resourceSize;
				t[line][2] = "" + cf.seed;
				t[line][3] = "" + cf.terminalSeed;
				t[line][4] = "" + cf.dod;
				t[line][5] = "" + cf.edod;
				tAVG[iAvg][0] = "" + cf.jobSize;
				tAVG[iAvg][1] = "" + cf.resourceSize;
				tAVG[iAvg][2] = "" + cf.seed;
				tAVG[iAvg][3] = "";
				tAVG[iAvg][4] = "" + cf.dod;
				tAVG[iAvg][5] = "" + cf.edod;
				for (Result r : l) {
					t[line][tmpMap.get(r.configFile.algo.ID) + 6] = r.overall_distance
							+ "";
					sum[tmpMap.get(r.configFile.algo.ID) + 6] += r.overall_distance;
				}
				int i = 0;
				for (int iAlgo = 0; iAlgo < algos.length; iAlgo++) {
					String sAlgo1 = algos[iAlgo];
					int indexAlgo1 = tmpMap.get(sAlgo1);
					int j = 0;
					for (int jAlgo = 0; jAlgo < algos.length; jAlgo++) {
						String sAlgo2 = algos[jAlgo];
						int indexAlgo2 = tmpMap.get(sAlgo2);
						if (indexAlgo1 != indexAlgo2) {
							double gap = ((Double
									.parseDouble(t[line][indexAlgo2 + 6]) - Double
									.parseDouble(t[line][indexAlgo1 + 6])) / Double
									.parseDouble(t[line][indexAlgo1 + 6])) * 100;
							compare[i][j] = gap;// (gap>=0 ? "+" : "") +
												// nf.format(gap)+"%";
							j++;
						}
					}
					i++;
				}
				index = algos.length + 6;
				for (i = 0; i < compare.length; i++) {
					for (int j = 0; j < compare[i].length; j++) {
						// System.err.println("1)t["+line+"]["+index+"]="+t[line][index]);
						t[line][index] = "" + compare[i][j];
						// System.err.println("2)t["+line+"]["+index+"]="+t[line][index]);

						sum[index] += compare[i][j];
						index++;
					}
				}
				line++;
			}

			for (int i = 1; i <= algos.length; i++) {
				tAVG[iAvg][5 + i] = ""
						+ (sum[5 + i] / (groupsSeedAlgo.get(gpeSeedAlgoName)
								.size() + 0.0));
			}

			int i = algos.length + 6;
			for (int iAlgo = 0; iAlgo < algos.length; iAlgo++) {
				String sAlgo1 = algos[iAlgo];
				int indexAlgo1 = tmpMap.get(sAlgo1);

				for (int jAlgo = 0; jAlgo < algos.length; jAlgo++) {
					String sAlgo2 = algos[jAlgo];
					int indexAlgo2 = tmpMap.get(sAlgo2);
					if (indexAlgo1 != indexAlgo2) {
						double var = ((sum[indexAlgo2 + 6] - sum[indexAlgo1 + 6]) / sum[indexAlgo1 + 6]) * 100;
						tAVG[iAvg][i] = var + "";
						i++;
					}

				}
			}
			iAvg++;
		}

		pw.append("\nDISTANCES\n");
		for (int i = 0; i < t.length; i++) {
			pw.append("\t");
			for (int j = 0; j < t[i].length; j++) {
				pw.append(t[i][j] + "\t");
			}
			pw.append("\n");
		}
		pw.flush();

		pw.append("\nAVG DISTANCES\n");
		for (int i = 0; i < tAVG.length; i++) {
			pw.append("\t");
			for (int j = 0; j < tAVG[i].length; j++) {
				pw.append(tAVG[i][j] + "\t");
			}
			pw.append("\n");
		}
		pw.flush();

		// Overspent Time
		t = new String[groups.size() + 1][algos.length * algos.length + 6];
		tAVG = new String[1 + groupsSeedAlgo.size()][algos.length
				* algos.length + 6];
		t[0][0] = "Jobs";
		t[0][1] = "Machines";
		t[0][2] = "GSeed";
		t[0][3] = "ESeed";
		t[0][4] = "DOD";
		t[0][5] = "EDOD";
		tAVG[0][0] = "Jobs";
		tAVG[0][1] = "Machines";
		tAVG[0][2] = "GSeed";
		tAVG[0][3] = "ESeed";
		tAVG[0][4] = "DOD";
		tAVG[0][5] = "EDOD";
		index = 6;
		for (String algo : algos) {
			t[0][index] = algo;
			tAVG[0][index] = algo;
			index++;
		}
		for (String algo : algos) {
			for (String algo2 : algos) {
				if (!algo.equals(algo2)) {
					t[0][index] = algo + "->" + algo2;
					tAVG[0][index++] = algo + "->" + algo2;
				}
			}
		}

		line = 1;
		iAvg = 1;
		for (String gpeSeedAlgoName : groupsSeedAlgo.keySet()) {
			double[] sum = new double[t[line].length];
			for (String gpeName : groupsSeedAlgo.get(gpeSeedAlgoName)) {
				ArrayList<Result> l = groups.get(gpeName);
				double[][] compare = new double[algos.length][algos.length - 1];
				ConfigFile cf = l.get(0).configFile;
				t[line][0] = "" + cf.jobSize;
				t[line][1] = "" + cf.resourceSize;
				t[line][2] = "" + cf.seed;
				t[line][3] = "" + cf.terminalSeed;
				t[line][4] = "" + cf.dod;
				t[line][5] = "" + cf.edod;
				tAVG[iAvg][0] = "" + cf.jobSize;
				tAVG[iAvg][1] = "" + cf.resourceSize;
				tAVG[iAvg][2] = "" + cf.seed;
				tAVG[iAvg][3] = "";
				tAVG[iAvg][4] = "" + cf.dod;
				tAVG[iAvg][5] = "" + cf.edod;
				for (Result r : l) {
					t[line][tmpMap.get(r.configFile.algo.ID) + 6] = r.overall_overspentTime
							+ "";
					sum[tmpMap.get(r.configFile.algo.ID) + 6] += r.overall_overspentTime
							.getInSec();
				}
				int i = 0;
				for (int iAlgo = 0; iAlgo < algos.length; iAlgo++) {
					String sAlgo1 = algos[iAlgo];
					int indexAlgo1 = tmpMap.get(sAlgo1);
					int j = 0;
					for (int jAlgo = 0; jAlgo < algos.length; jAlgo++) {
						String sAlgo2 = algos[jAlgo];
						int indexAlgo2 = tmpMap.get(sAlgo2);
						if (indexAlgo1 != indexAlgo2) {
							double gap = ((new Time(t[line][indexAlgo2 + 6]
									+ "").getInSec() - new Time(
									t[line][indexAlgo1 + 6]).getInSec()) / new Time(
									t[line][indexAlgo1 + 6]).getInSec()) * 100;
							compare[i][j] = gap;
							j++;
						}
					}
					i++;
				}
				index = algos.length + 6;
				for (i = 0; i < compare.length; i++) {
					for (int j = 0; j < compare[i].length; j++) {
						t[line][index] = "" + compare[i][j];
						sum[index] += compare[i][j];
						index++;
					}
				}
				line++;
			}

			for (int i = 1; i <= algos.length; i++) {
				boolean neg = false;
				double sec = sum[5 + i];
				if (sum[5 + i] < 0) {
					sec = -1 * sum[5 + i];
					neg = true;
				}
				tAVG[iAvg][5 + i] = (neg ? "-" : "")
						+ new Time((sec / (groupsSeedAlgo.get(gpeSeedAlgoName)
								.size() + 0d))).toString();
			}

			int i = algos.length + 6;
			for (int iAlgo = 0; iAlgo < algos.length; iAlgo++) {
				String sAlgo1 = algos[iAlgo];
				int indexAlgo1 = tmpMap.get(sAlgo1);

				for (int jAlgo = 0; jAlgo < algos.length; jAlgo++) {
					String sAlgo2 = algos[jAlgo];
					int indexAlgo2 = tmpMap.get(sAlgo2);
					if (indexAlgo1 != indexAlgo2) {
						double var = ((sum[indexAlgo2 + 6] - sum[indexAlgo1 + 6]) / sum[indexAlgo1 + 6]) * 100;
						tAVG[iAvg][i] = var + "";
						i++;
					}

				}
			}
			iAvg++;
		}

		pw.append("\nOVERSPENT TIME\n");
		for (int i = 0; i < t.length; i++) {
			pw.append("\t");
			for (int j = 0; j < t[i].length; j++) {
				pw.append(t[i][j] + "\t");
			}
			pw.append("\n");
		}
		pw.append("\nAVG OVERSPENT TIME\n");
		for (int i = 0; i < tAVG.length; i++) {
			pw.append("\t");
			for (int j = 0; j < tAVG[i].length; j++) {
				pw.append(tAVG[i][j] + "\t");
			}
			pw.append("\n");
		}
		pw.flush();

		// TW OVERRUN
		t = new String[groups.size() + 1][algos.length * algos.length + 6];
		tAVG = new String[1 + groupsSeedAlgo.size()][algos.length
				* algos.length + 6];
		t[0][0] = "Jobs";
		t[0][1] = "Machines";
		t[0][2] = "GSeed";
		t[0][3] = "ESeed";
		t[0][4] = "DOD";
		t[0][5] = "EDOD";
		tAVG[0][0] = "Jobs";
		tAVG[0][1] = "Machines";
		tAVG[0][2] = "GSeed";
		tAVG[0][3] = "ESeed";
		tAVG[0][4] = "DOD";
		tAVG[0][5] = "EDOD";
		index = 6;
		for (String algo : algos) {
			t[0][index] = algo;
			tAVG[0][index] = algo;
			index++;
		}
		for (String algo : algos) {
			for (String algo2 : algos) {
				if (!algo.equals(algo2)) {
					t[0][index] = algo + "->" + algo2;
					tAVG[0][index++] = algo + "->" + algo2;
				}
			}
		}

		line = 1;
		iAvg = 1;
		for (String gpeSeedAlgoName : groupsSeedAlgo.keySet()) {
			double[] sum = new double[t[line].length];
			for (String gpeName : groupsSeedAlgo.get(gpeSeedAlgoName)) {
				ArrayList<Result> l = groups.get(gpeName);
				double[][] compare = new double[algos.length][algos.length - 1];
				ConfigFile cf = l.get(0).configFile;
				t[line][0] = "" + cf.jobSize;
				t[line][1] = "" + cf.resourceSize;
				t[line][2] = "" + cf.seed;
				t[line][3] = "" + cf.terminalSeed;
				t[line][4] = "" + cf.dod;
				t[line][5] = "" + cf.edod;
				tAVG[iAvg][0] = "" + cf.jobSize;
				tAVG[iAvg][1] = "" + cf.resourceSize;
				tAVG[iAvg][2] = "" + cf.seed;
				tAVG[iAvg][3] = "";
				tAVG[iAvg][4] = "" + cf.dod;
				tAVG[iAvg][5] = "" + cf.edod;
				for (Result r : l) {
					t[line][tmpMap.get(r.configFile.algo.ID) + 6] = r.overall_overruns
							+ "";
					sum[tmpMap.get(r.configFile.algo.ID) + 6] += r.overall_overruns;
				}
				int i = 0;
				for (int iAlgo = 0; iAlgo < algos.length; iAlgo++) {
					String sAlgo1 = algos[iAlgo];
					int indexAlgo1 = tmpMap.get(sAlgo1);
					int j = 0;
					for (int jAlgo = 0; jAlgo < algos.length; jAlgo++) {
						String sAlgo2 = algos[jAlgo];
						int indexAlgo2 = tmpMap.get(sAlgo2);
						if (indexAlgo1 != indexAlgo2) {
							double gap = ((Integer
									.parseInt(t[line][indexAlgo2 + 6]) - Integer
									.parseInt(t[line][indexAlgo1 + 6])) / Double
									.parseDouble(t[line][indexAlgo1 + 6])) * 100;
							compare[i][j] = gap;
							j++;
						}
					}
					i++;
				}
				index = algos.length + 6;
				for (i = 0; i < compare.length; i++) {
					for (int j = 0; j < compare[i].length; j++) {
						t[line][index] = "" + compare[i][j];
						sum[index] = compare[i][j];
						index++;
					}
				}
				line++;
			}

			for (int i = 1; i <= algos.length; i++) {
				tAVG[iAvg][5 + i] = ""
						+ (sum[5 + i] / (groupsSeedAlgo.get(gpeSeedAlgoName)
								.size() + 0.0));
			}

			int i = algos.length + 6;
			for (int iAlgo = 0; iAlgo < algos.length; iAlgo++) {
				String sAlgo1 = algos[iAlgo];
				int indexAlgo1 = tmpMap.get(sAlgo1);

				for (int jAlgo = 0; jAlgo < algos.length; jAlgo++) {
					String sAlgo2 = algos[jAlgo];
					int indexAlgo2 = tmpMap.get(sAlgo2);
					if (indexAlgo1 != indexAlgo2) {
						double var = ((sum[indexAlgo2 + 6] - sum[indexAlgo1 + 6]) / sum[indexAlgo1 + 6]) * 100;
						tAVG[iAvg][i] = var + "";
						i++;
					}

				}
			}
			iAvg++;
		}

		pw.append("\nTIME WINDOWS OVERRUN\n");
		for (int i = 0; i < t.length; i++) {
			pw.append("\t");
			for (int j = 0; j < t[i].length; j++) {
				pw.append(t[i][j] + "\t");
			}
			pw.append("\n");
		}
		pw.append("\nAVG TIME WINDOWS OVERRUN\n");
		for (int i = 0; i < tAVG.length; i++) {
			pw.append("\t");
			for (int j = 0; j < tAVG[i].length; j++) {
				pw.append(tAVG[i][j] + "\t");
			}
			pw.append("\n");
		}
		pw.flush();

		// MissionScheduler Computing Time
		t = new String[groups.size() + 1][algos.length * algos.length + 6];
		tAVG = new String[1 + groupsSeedAlgo.size()][algos.length
				* algos.length + 6];
		t[0][0] = "Jobs";
		t[0][1] = "Machines";
		t[0][2] = "GSeed";
		t[0][3] = "ESeed";
		t[0][4] = "DOD";
		t[0][5] = "EDOD";
		tAVG[0][0] = "Jobs";
		tAVG[0][1] = "Machines";
		tAVG[0][2] = "GSeed";
		tAVG[0][3] = "ESeed";
		tAVG[0][4] = "DOD";
		tAVG[0][5] = "EDOD";
		index = 6;
		for (String algo : algos) {
			t[0][index] = algo;
			tAVG[0][index] = algo;
			index++;
		}
		for (String algo : algos) {
			for (String algo2 : algos) {
				if (!algo.equals(algo2)) {
					t[0][index] = algo + "->" + algo2;
					tAVG[0][index++] = algo + "->" + algo2;
				}
			}
		}

		line = 1;
		iAvg = 1;
		for (String gpeSeedAlgoName : groupsSeedAlgo.keySet()) {
			double[] sum = new double[t[line].length];
			for (String gpeName : groupsSeedAlgo.get(gpeSeedAlgoName)) {
				ArrayList<Result> l = groups.get(gpeName);
				double[][] compare = new double[algos.length][algos.length - 1];
				ConfigFile cf = l.get(0).configFile;
				t[line][0] = "" + cf.jobSize;
				t[line][1] = "" + cf.resourceSize;
				t[line][2] = "" + cf.seed;
				t[line][3] = "" + cf.terminalSeed;
				t[line][4] = "" + cf.dod;
				t[line][5] = "" + cf.edod;
				tAVG[iAvg][0] = "" + cf.jobSize;
				tAVG[iAvg][1] = "" + cf.resourceSize;
				tAVG[iAvg][2] = "" + cf.seed;
				tAVG[iAvg][3] = "";
				tAVG[iAvg][4] = "" + cf.dod;
				tAVG[iAvg][5] = "" + cf.edod;
				for (Result r : l) {
					t[line][tmpMap.get(r.configFile.algo.ID) + 6] = r.msComputingTime
							+ "";
					sum[tmpMap.get(r.configFile.algo.ID) + 6] += r.msComputingTime
							.getInSec();
				}
				int i = 0;
				for (int iAlgo = 0; iAlgo < algos.length; iAlgo++) {
					String sAlgo1 = algos[iAlgo];
					int indexAlgo1 = tmpMap.get(sAlgo1);
					int j = 0;
					for (int jAlgo = 0; jAlgo < algos.length; jAlgo++) {
						String sAlgo2 = algos[jAlgo];
						int indexAlgo2 = tmpMap.get(sAlgo2);
						if (indexAlgo1 != indexAlgo2) {
							double tD = new Time(t[line][indexAlgo1 + 6])
									.getInSec();
							double tA = new Time(t[line][indexAlgo2 + 6])
									.getInSec();

							if (tD != 0 && tA != 0) {
								double gap = ((tA - tD) / tD) * 100;
								compare[i][j] = gap;
							} else
								compare[i][j] = Double.NaN;
							j++;
						}
					}
					i++;
				}
				index = algos.length + 6;
				for (i = 0; i < compare.length; i++) {
					for (int j = 0; j < compare[i].length; j++) {
						t[line][index] = "" + compare[i][j];
						sum[index] = compare[i][j];
						index++;
					}
				}
				line++;
			}
			for (int i = 1; i <= algos.length; i++) {
				boolean neg = false;
				double sec = sum[5 + i];
				if (sum[5 + i] < 0) {
					sec = -1 * sum[5 + i];
					neg = true;
				}
				tAVG[iAvg][5 + i] = (neg ? "-" : "")
						+ new Time((sec / (groupsSeedAlgo.get(gpeSeedAlgoName)
								.size() + 0d))).toString();
			}

			int i = algos.length + 6;
			for (int iAlgo = 0; iAlgo < algos.length; iAlgo++) {
				String sAlgo1 = algos[iAlgo];
				int indexAlgo1 = tmpMap.get(sAlgo1);

				for (int jAlgo = 0; jAlgo < algos.length; jAlgo++) {
					String sAlgo2 = algos[jAlgo];
					int indexAlgo2 = tmpMap.get(sAlgo2);
					if (indexAlgo1 != indexAlgo2) {
						double var = ((sum[indexAlgo2 + 6] - sum[indexAlgo1 + 6]) / sum[indexAlgo1 + 6]) * 100;
						tAVG[iAvg][i] = var + "";
						i++;
					}
				}
			}
			iAvg++;
		}

		pw.append("\nMISSION SCHEDULER COMPUTING TIME\n");
		for (int i = 0; i < t.length; i++) {
			pw.append("\t");
			for (int j = 0; j < t[i].length; j++) {
				pw.append(t[i][j] + "\t");
			}
			pw.append("\n");
		}
		pw.append("\nAVG MISSION SCHEDULER COMPUTING TIME\n");
		for (int i = 0; i < tAVG.length; i++) {
			pw.append("\t");
			for (int j = 0; j < tAVG[i].length; j++) {
				pw.append(tAVG[i][j] + "\t");
			}
			pw.append("\n");
		}
		pw.flush();

		// Exec time
		t = new String[groups.size() + 1][algos.length * algos.length + 6];
		tAVG = new String[1 + groupsSeedAlgo.size()][algos.length
				* algos.length + 6];
		t[0][0] = "Jobs";
		t[0][1] = "Machines";
		t[0][2] = "GSeed";
		t[0][3] = "ESeed";
		t[0][4] = "DOD";
		t[0][5] = "EDOD";
		tAVG[0][0] = "Jobs";
		tAVG[0][1] = "Machines";
		tAVG[0][2] = "GSeed";
		tAVG[0][3] = "ESeed";
		tAVG[0][4] = "DOD";
		tAVG[0][5] = "EDOD";
		index = 6;
		for (String algo : algos) {
			t[0][index] = algo;
			tAVG[0][index] = algo;
			index++;
		}
		for (String algo : algos) {
			for (String algo2 : algos) {
				if (!algo.equals(algo2)) {
					t[0][index] = algo + "->" + algo2;
					tAVG[0][index++] = algo + "->" + algo2;
				}
			}
		}

		line = 1;
		iAvg = 1;
		for (String gpeSeedAlgoName : groupsSeedAlgo.keySet()) {
			double[] sum = new double[t[line].length];
			for (String gpeName : groupsSeedAlgo.get(gpeSeedAlgoName)) {
				ArrayList<Result> l = groups.get(gpeName);
				double[][] compare = new double[algos.length][algos.length - 1];
				ConfigFile cf = l.get(0).configFile;
				t[line][0] = "" + cf.jobSize;
				t[line][1] = "" + cf.resourceSize;
				t[line][2] = "" + cf.seed;
				t[line][3] = "" + cf.terminalSeed;
				t[line][4] = "" + cf.dod;
				t[line][5] = "" + cf.edod;
				tAVG[iAvg][0] = "" + cf.jobSize;
				tAVG[iAvg][1] = "" + cf.resourceSize;
				tAVG[iAvg][2] = "" + cf.seed;
				tAVG[iAvg][3] = "";
				tAVG[iAvg][4] = "" + cf.dod;
				tAVG[iAvg][5] = "" + cf.edod;
				for (Result r : l) {
					t[line][tmpMap.get(r.configFile.algo.ID) + 6] = r.execTime
							.toString();
					sum[tmpMap.get(r.configFile.algo.ID) + 6] += r.execTime
							.getInSec();
				}
				int i = 0;
				for (int iAlgo = 0; iAlgo < algos.length; iAlgo++) {
					String sAlgo1 = algos[iAlgo];
					int indexAlgo1 = tmpMap.get(sAlgo1);
					int j = 0;
					for (int jAlgo = 0; jAlgo < algos.length; jAlgo++) {
						String sAlgo2 = algos[jAlgo];
						int indexAlgo2 = tmpMap.get(sAlgo2);
						if (indexAlgo1 != indexAlgo2) {
							double gap = ((new Time(t[line][indexAlgo2 + 6]
									+ "").getInSec() - new Time(
									t[line][indexAlgo1 + 6]).getInSec()) / new Time(
									t[line][indexAlgo1 + 6]).getInSec()) * 100;
							compare[i][j] = gap;
							j++;
						}
					}
					i++;
				}
				index = algos.length + 6;
				for (i = 0; i < compare.length; i++) {
					for (int j = 0; j < compare[i].length; j++) {
						t[line][index] = compare[i][j] + "";
						sum[index] = compare[i][j];
						index++;
					}
				}
				line++;
			}
			for (int i = 1; i <= algos.length; i++) {
				boolean neg = false;
				double sec = sum[5 + i];
				if (sum[5 + i] < 0) {
					sec = -1 * sum[5 + i];
					neg = true;
				}
				tAVG[iAvg][5 + i] = (neg ? "-" : "")
						+ new Time((sec / (groupsSeedAlgo.get(gpeSeedAlgoName)
								.size() + 0d))).toString();
			}

			int i = algos.length + 6;
			for (int iAlgo = 0; iAlgo < algos.length; iAlgo++) {
				String sAlgo1 = algos[iAlgo];
				int indexAlgo1 = tmpMap.get(sAlgo1);

				for (int jAlgo = 0; jAlgo < algos.length; jAlgo++) {
					String sAlgo2 = algos[jAlgo];
					int indexAlgo2 = tmpMap.get(sAlgo2);
					if (indexAlgo1 != indexAlgo2) {
						double var = ((sum[indexAlgo2 + 6] - sum[indexAlgo1 + 6]) / sum[indexAlgo1 + 6]) * 100;
						tAVG[iAvg][i] = var + "";
						i++;
					}

				}
			}
			iAvg++;
		}

		pw.append("\nOVERALL EXECUTION TIME\n");
		for (int i = 0; i < t.length; i++) {
			pw.append("\t");
			for (int j = 0; j < t[i].length; j++) {
				pw.append(t[i][j] + "\t");
			}
			pw.append("\n");
		}
		pw.append("\nAVG OVERALL EXECUTION TIME\n");
		for (int i = 0; i < tAVG.length; i++) {
			pw.append("\t");
			for (int j = 0; j < tAVG[i].length; j++) {
				pw.append(tAVG[i][j] + "\t");
			}
			pw.append("\n");
		}
		pw.flush();

		pw.close();
	}

	/*
	 * public static void getTerminalSeedsAnalyze(Results results, String
	 * outFile) throws FileNotFoundException { ArrayList<ArrayList<Result>>
	 * groups = getComparableResults(new
	 * ArrayList<Result>(results.results.values())); File f = new File(outFile);
	 * f.getParentFile().mkdirs(); PrintWriter pw = new PrintWriter(f);
	 * pw.append("FILE\tDISTANCE\tOVERSPENT TIME\tOVERRUN TW\n");
	 * 
	 * for(ArrayList<Result> l : groups){ double overallDistance = 0; Time
	 * overallTime = new Time(0,1); int overruns = 0; for(Result r : l){
	 * overallDistance += r.overall_distance; overallTime = new
	 * Time(overallTime, r.overall_overspentTime); overruns +=
	 * r.overall_overruns;
	 * pw.append(r.configFile.file+"\t"+r.overall_distance+"\t"
	 * +r.overall_overspentTime+"\t"+r.overall_overruns+"\n"); } overallDistance
	 * /= (l.size()+0.0); overallTime = new
	 * Time(overallTime.getInSec()/(l.size()+0.0)+"s"); overruns /=
	 * (l.size()+0.0);
	 * pw.append("AVERAGE\t"+overallDistance+"\t"+overallTime+"\t"
	 * +overruns+"\n"); pw.append("\n");
	 * //System.out.println("AVG group("+g+")="
	 * +(overallDistance/(l.size()+0.0)));
	 * //System.out.println("AVG group("+g+")="+new
	 * Time((overallTime.getInSec()/(l.size()+0.0)+"s"))); } pw.flush();
	 * pw.close(); }
	 */

	/**
	 * @param args
	 * @throws IOException
	 * @throws EmptyLevelException
	 * @throws ContainerDimensionException
	 * @throws NoPathFoundException
	 * @throws SAXException
	 */
	public static void main(String[] args) throws IOException, SAXException,
			NoPathFoundException, ContainerDimensionException,
			EmptyLevelException {
		String home = "../trunk/xml/results/";

		SimpleDateFormat sdf = new SimpleDateFormat("ddMMyyyy_HH:mm:ss");
		String date = sdf.format(new Date());
		String resultsFile = home + "results_" + date + ".dat";
		String analyzeFile = home + "analyze_" + date + ".csv";

		PrintWriter out = new PrintWriter(new File(
				"../trunk/xml/results/BenchmarkRunner_" + date + ".out"));

		// long[] seeds = {-22222, -2222, -222, -22, -2, 2, 222, 2222, 22222,
		// 222222};
		// int[] jobsSize = {10, 20,30,40,50,75,100};
		// int[][] resourcesSize = { {2, 3} , {5, 7} , {7,10} , {7,10} , {7,10}
		// , {15}, {20}};
		// double[] dods = {0, 0.25, 0.5 , 0.75, 1};
		// double[][] edods = {{0}, {0.33, 0.66, 1.0} , {0.33, 0.66, 1.0},
		// {0.33, 0.66, 1.0}, {0.33, 0.66, 1.0}};
		// String[] algoTypes = {"Greedy", "Linear", "Random", "ACO"};

		// long[] seeds = {-2, 2};
		// int[] jobsSize = {10, 20};
		// int[][] resourcesSize = { {2, 3} , {5, 7}};
		// double[] dods = {0, 1};
		// double[][] edods = {{0}, {0.33, 0.66, 1.0}};
		// String[] algoTypes = {"Greedy", "Linear", "Random", "ACO"};

		long[] seeds = { 21, 33, 42 };
		long[] terminalSeeds = { 100 };
		int[] jobsSize = { 50 };
		int[][] resourcesSize = { { 7, 10 } };
		double[] dods = { 0.0 };
		double[][] edods = { { 0.0 } };

		/*
		 * long[] seeds = {42}; long[] terminalSeeds = {100}; int[] jobsSize =
		 * {10}; int[][] resourcesSize = { {2,3,4}}; double[] dods = {0.0, 0.5,
		 * 1.0}; double[][] edods = {{0.0}, {0.33, 0.66, 1.0}, {0.33, 0.66,
		 * 1.0}};
		 */

		HashMap<String, String> aco1_1_100_Params = new HashMap<String, String>();
		aco1_1_100_Params.put("alpha", "1.0");
		aco1_1_100_Params.put("beta", "1.0");
		aco1_1_100_Params.put("gamma", "1.0");
		aco1_1_100_Params.put("delta", "0.1");
		aco1_1_100_Params.put("persistence", "0.99");
		aco1_1_100_Params.put("Q", "1.5");
		aco1_1_100_Params.put("QR", "0");
		aco1_1_100_Params.put("sync", "100");
		aco1_1_100_Params.put("F1", "1.0");
		aco1_1_100_Params.put("F2", "1.0");

		HashMap<String, String> aco1_1_1000_Params = new HashMap<String, String>();
		aco1_1_1000_Params.put("alpha", "1.0");
		aco1_1_1000_Params.put("beta", "1.0");
		aco1_1_1000_Params.put("gamma", "1.0");
		aco1_1_1000_Params.put("delta", "0.1");
		aco1_1_1000_Params.put("persistence", "0.99");
		aco1_1_1000_Params.put("Q", "1.5");
		aco1_1_1000_Params.put("QR", "0");
		aco1_1_1000_Params.put("sync", "1000");
		aco1_1_1000_Params.put("F1", "1.0");
		aco1_1_1000_Params.put("F2", "1.0");

		HashMap<String, String> aco1_5_100_Params = new HashMap<String, String>();
		aco1_5_100_Params.put("alpha", "1.0");
		aco1_5_100_Params.put("beta", "1.0");
		aco1_5_100_Params.put("gamma", "1.0");
		aco1_5_100_Params.put("delta", "0.1");
		aco1_5_100_Params.put("persistence", "0.99");
		aco1_5_100_Params.put("Q", "1.5");
		aco1_5_100_Params.put("QR", "0");
		aco1_5_100_Params.put("sync", "100");
		aco1_5_100_Params.put("F1", "1.0");
		aco1_5_100_Params.put("F2", "5.0");

		HashMap<String, String> aco1_5_1000_Params = new HashMap<String, String>();
		aco1_5_1000_Params.put("alpha", "1.0");
		aco1_5_1000_Params.put("beta", "1.0");
		aco1_5_1000_Params.put("gamma", "1.0");
		aco1_5_1000_Params.put("delta", "0.1");
		aco1_5_1000_Params.put("persistence", "0.99");
		aco1_5_1000_Params.put("Q", "1.5");
		aco1_5_1000_Params.put("QR", "0");
		aco1_5_1000_Params.put("sync", "1000");
		aco1_5_1000_Params.put("F1", "1.0");
		aco1_5_1000_Params.put("F2", "5.0");

		HashMap<String, String> aco5_1_100_Params = new HashMap<String, String>();
		aco5_1_100_Params.put("alpha", "1.0");
		aco5_1_100_Params.put("beta", "1.0");
		aco5_1_100_Params.put("gamma", "1.0");
		aco5_1_100_Params.put("delta", "0.1");
		aco5_1_100_Params.put("persistence", "0.99");
		aco5_1_100_Params.put("Q", "1.5");
		aco5_1_100_Params.put("QR", "0");
		aco5_1_100_Params.put("sync", "100");
		aco5_1_100_Params.put("F1", "5.0");
		aco5_1_100_Params.put("F2", "1.0");

		HashMap<String, String> aco5_1_1000_Params = new HashMap<String, String>();
		aco5_1_1000_Params.put("alpha", "1.0");
		aco5_1_1000_Params.put("beta", "1.0");
		aco5_1_1000_Params.put("gamma", "1.0");
		aco5_1_1000_Params.put("delta", "0.1");
		aco5_1_1000_Params.put("persistence", "0.99");
		aco5_1_1000_Params.put("Q", "1.5");
		aco5_1_1000_Params.put("QR", "0");
		aco5_1_1000_Params.put("sync", "1000");
		aco5_1_1000_Params.put("F1", "5.0");
		aco5_1_1000_Params.put("F2", "1.0");

		HashMap<String, String> aco50_1_1000_Params = new HashMap<String, String>();
		aco50_1_1000_Params.put("alpha", "1.0");
		aco50_1_1000_Params.put("beta", "1.0");
		aco50_1_1000_Params.put("gamma", "1.0");
		aco50_1_1000_Params.put("delta", "0.1");
		aco50_1_1000_Params.put("persistence", "0.99");
		aco50_1_1000_Params.put("Q", "1.5");
		aco50_1_1000_Params.put("QR", "0");
		aco50_1_1000_Params.put("sync", "1000");
		aco50_1_1000_Params.put("F1", "50.0");
		aco50_1_1000_Params.put("F2", "1.0");

		HashMap<String, String> aco50_1_100_Params = new HashMap<String, String>();
		aco50_1_100_Params.put("alpha", "1.0");
		aco50_1_100_Params.put("beta", "1.0");
		aco50_1_100_Params.put("gamma", "1.0");
		aco50_1_100_Params.put("delta", "0.1");
		aco50_1_100_Params.put("persistence", "0.99");
		aco50_1_100_Params.put("Q", "1.5");
		aco50_1_100_Params.put("QR", "0");
		aco50_1_100_Params.put("sync", "100");
		aco50_1_100_Params.put("F1", "50.0");
		aco50_1_100_Params.put("F2", "1.0");

		HashMap<String, String> aco1_50_1000_Params = new HashMap<String, String>();
		aco1_50_1000_Params.put("alpha", "1.0");
		aco1_50_1000_Params.put("beta", "1.0");
		aco1_50_1000_Params.put("gamma", "1.0");
		aco1_50_1000_Params.put("delta", "0.1");
		aco1_50_1000_Params.put("persistence", "0.99");
		aco1_50_1000_Params.put("Q", "1.5");
		aco1_50_1000_Params.put("QR", "0");
		aco1_50_1000_Params.put("sync", "1000");
		aco1_50_1000_Params.put("F1", "1.0");
		aco1_50_1000_Params.put("F2", "50.0");

		HashMap<String, String> aco1_50_100_Params = new HashMap<String, String>();
		aco1_50_100_Params.put("alpha", "1.0");
		aco1_50_100_Params.put("beta", "1.0");
		aco1_50_100_Params.put("gamma", "1.0");
		aco1_50_100_Params.put("delta", "0.1");
		aco1_50_100_Params.put("persistence", "0.99");
		aco1_50_100_Params.put("Q", "1.5");
		aco1_50_100_Params.put("QR", "0");
		aco1_50_100_Params.put("sync", "100");
		aco1_50_100_Params.put("F1", "1.0");
		aco1_50_100_Params.put("F2", "50.0");

		HashMap<String, String> aco1_0_100_Params = new HashMap<String, String>();
		aco1_0_100_Params.put("alpha", "1.0");
		aco1_0_100_Params.put("beta", "1.0");
		aco1_0_100_Params.put("gamma", "1.0");
		aco1_0_100_Params.put("delta", "0.1");
		aco1_0_100_Params.put("persistence", "0.99");
		aco1_0_100_Params.put("Q", "1.5");
		aco1_0_100_Params.put("QR", "0");
		aco1_0_100_Params.put("sync", "100");
		aco1_0_100_Params.put("F1", "1.0");
		aco1_0_100_Params.put("F2", "0.0");

		HashMap<String, String> aco1_0_1000_Params = new HashMap<String, String>();
		aco1_0_1000_Params.put("alpha", "1.0");
		aco1_0_1000_Params.put("beta", "1.0");
		aco1_0_1000_Params.put("gamma", "1.0");
		aco1_0_1000_Params.put("delta", "0.1");
		aco1_0_1000_Params.put("persistence", "0.99");
		aco1_0_1000_Params.put("Q", "1.5");
		aco1_0_1000_Params.put("QR", "0");
		aco1_0_1000_Params.put("sync", "1000");
		aco1_0_1000_Params.put("F1", "1.0");
		aco1_0_1000_Params.put("F2", "0.0");

		HashMap<String, String> aco0_1_100_Params = new HashMap<String, String>();
		aco0_1_100_Params.put("alpha", "1.0");
		aco0_1_100_Params.put("beta", "1.0");
		aco0_1_100_Params.put("gamma", "1.0");
		aco0_1_100_Params.put("delta", "0.1");
		aco0_1_100_Params.put("persistence", "0.99");
		aco0_1_100_Params.put("Q", "1.5");
		aco0_1_100_Params.put("QR", "0");
		aco0_1_100_Params.put("sync", "100");
		aco0_1_100_Params.put("F1", "0.0");
		aco0_1_100_Params.put("F2", "1.0");

		HashMap<String, String> aco0_1_1000_Params = new HashMap<String, String>();
		aco0_1_1000_Params.put("alpha", "1.0");
		aco0_1_1000_Params.put("beta", "1.0");
		aco0_1_1000_Params.put("gamma", "1.0");
		aco0_1_1000_Params.put("delta", "0.1");
		aco0_1_1000_Params.put("persistence", "0.99");
		aco0_1_1000_Params.put("Q", "1.5");
		aco0_1_1000_Params.put("QR", "0");
		aco0_1_1000_Params.put("sync", "1000");
		aco0_1_1000_Params.put("F1", "0.0");
		aco0_1_1000_Params.put("F2", "1.0");

		BenchmarkAlgorithm greedy = new BenchmarkAlgorithm("Greedy", "Greedy",
				null);
		// BenchmarkAlgorithm[] algoTypes = {greedy};

		BenchmarkAlgorithm aco1 = new BenchmarkAlgorithm(
				"ACO_F1=F2=1_Sync=100", "ACO", aco1_1_100_Params);
		BenchmarkAlgorithm aco2 = new BenchmarkAlgorithm(
				"ACO_F1=F2=1_Sync=1000", "ACO", aco1_1_1000_Params);
		BenchmarkAlgorithm aco3 = new BenchmarkAlgorithm(
				"ACO_F1=1_F2=5_Sync=100", "ACO", aco1_5_100_Params);
		BenchmarkAlgorithm aco4 = new BenchmarkAlgorithm(
				"ACO_F1=1_F2=5_Sync=1000", "ACO", aco1_5_1000_Params);
		BenchmarkAlgorithm aco5 = new BenchmarkAlgorithm(
				"ACO_F1=5_F2=1_Sync=100", "ACO", aco5_1_100_Params);
		BenchmarkAlgorithm aco6 = new BenchmarkAlgorithm(
				"ACO_F1=5_F2=1_Sync=1000", "ACO", aco50_1_100_Params);
		BenchmarkAlgorithm aco7 = new BenchmarkAlgorithm(
				"ACO_F1=50_F2=1_Sync=100", "ACO", aco50_1_100_Params);
		BenchmarkAlgorithm aco8 = new BenchmarkAlgorithm(
				"ACO_F1=50_F2=1_Sync=1000", "ACO", aco50_1_1000_Params);
		BenchmarkAlgorithm aco9 = new BenchmarkAlgorithm(
				"ACO_F1=1_F2=50_Sync=100", "ACO", aco1_50_100_Params);
		BenchmarkAlgorithm aco10 = new BenchmarkAlgorithm(
				"ACO_F1=1_F2=50_Sync=1000", "ACO", aco1_50_1000_Params);

		BenchmarkAlgorithm aco11 = new BenchmarkAlgorithm(
				"ACO_F1=1_F2=0_Sync=100", "ACO", aco1_0_100_Params);
		BenchmarkAlgorithm aco12 = new BenchmarkAlgorithm(
				"ACO_F1=1_F2=0_Sync=1000", "ACO", aco1_0_1000_Params);
		BenchmarkAlgorithm aco13 = new BenchmarkAlgorithm(
				"ACO_F1=0_F2=1_Sync=100", "ACO", aco0_1_100_Params);
		BenchmarkAlgorithm aco14 = new BenchmarkAlgorithm(
				"ACO_F1=0_F2=1_Sync=1000", "ACO", aco0_1_1000_Params);

		BenchmarkAlgorithm linear = new BenchmarkAlgorithm("Linear", "Linear",
				null);

		// BenchmarkAlgorithm[] algoTypes = {aco9,aco10,aco7,aco8,greedy, aco3,
		// aco4, aco5, aco6,aco1, aco2, linear, aco11, aco12, aco13, aco14};

		HashMap<String, String> bbParams21_2 = new HashMap<String, String>();
		bbParams21_2.put("timeMatrixFile",
				"../trunk/xml/results/10missions/timeM-10-21-2.dat");
		bbParams21_2.put("distanceMatrixFile",
				"../trunk/xml/results/10missions/distanceM_10-21-2.dat");
		bbParams21_2.put("computeCosts", "true");
		bbParams21_2.put("solutionFile", "xml/results/soluce_10_2_21.dat");

		HashMap<String, String> bbParams42_2 = new HashMap<String, String>();
		bbParams42_2.put("timeMatrixFile",
				"../trunk/xml/results/10missions/timeM-10-42-2.dat");
		bbParams42_2.put("distanceMatrixFile",
				"../trunk/xml/results/10missions/distanceM_10-42-2.dat");
		bbParams42_2.put("computeCosts", "true");
		bbParams42_2.put("solutionFile", "xml/results/soluce_10_2_42.dat");

		HashMap<String, String> bbParams33_2 = new HashMap<String, String>();
		bbParams33_2.put("timeMatrixFile",
				"../trunk/xml/results/10missions/timeM-10-33-2.dat");
		bbParams33_2.put("distanceMatrixFile",
				"../trunk/xml/results/10missions/distanceM_10-33-2.dat");
		bbParams33_2.put("computeCosts", "true");
		bbParams33_2.put("solutionFile", "xml/results/soluce_10_2_33.dat");

		HashMap<String, String> bbParams21_3 = new HashMap<String, String>();
		bbParams21_3.put("timeMatrixFile",
				"../trunk/xml/results/10missions/timeM-10-21-3.dat");
		bbParams21_3.put("distanceMatrixFile",
				"../trunk/xml/results/10missions/distanceM_10-21-3.dat");
		bbParams21_3.put("computeCosts", "true");
		bbParams21_3.put("solutionFile", "xml/results/soluce_10_3_21.dat");

		HashMap<String, String> bbParams42_3 = new HashMap<String, String>();
		bbParams42_3.put("timeMatrixFile",
				"../trunk/xml/results/10missions/timeM-10-42-3.dat");
		bbParams42_3.put("distanceMatrixFile",
				"../trunk/xml/results/10missions/distanceM_10-42-3.dat");
		bbParams42_3.put("computeCosts", "true");
		bbParams42_3.put("solutionFile", "xml/results/soluce_10_3_42.dat");

		HashMap<String, String> bbParams33_3 = new HashMap<String, String>();
		bbParams33_3.put("timeMatrixFile",
				"../trunk/xml/results/10missions/timeM-10-33-3.dat");
		bbParams33_3.put("distanceMatrixFile",
				"../trunk/xml/results/10missions/distanceM_10-33-3.dat");
		bbParams33_3.put("computeCosts", "true");
		bbParams33_3.put("solutionFile", "xml/results/soluce_10_3_33.dat");

		HashMap<String, String> bbParams21_4 = new HashMap<String, String>();
		bbParams21_4.put("timeMatrixFile",
				"../trunk/xml/results/10missions/timeM-10-21-4.dat");
		bbParams21_4.put("distanceMatrixFile",
				"../trunk/xml/results/10missions/distanceM_10-21-4.dat");
		bbParams21_4.put("computeCosts", "true");
		bbParams21_4.put("solutionFile", "xml/results/soluce_10_4_21.dat");

		HashMap<String, String> bbParams42_4 = new HashMap<String, String>();
		bbParams42_4.put("timeMatrixFile",
				"../trunk/xml/results/10missions/timeM-10-42-4.dat");
		bbParams42_4.put("distanceMatrixFile",
				"../trunk/xml/results/10missions/distanceM_10-42-4.dat");
		bbParams42_4.put("computeCosts", "true");
		bbParams42_4.put("solutionFile", "xml/results/soluce_10_4_42.dat");

		HashMap<String, String> bbParams33_4 = new HashMap<String, String>();
		bbParams33_4.put("timeMatrixFile",
				"../trunk/xml/results/10missions/timeM-10-33-4.dat");
		bbParams33_4.put("distanceMatrixFile",
				"../trunk/xml/results/10missions/distanceM_10-33-4.dat");
		bbParams33_4.put("computeCosts", "true");
		bbParams33_4.put("solutionFile", "xml/results/soluce_10_4_33.dat");
		BenchmarkAlgorithm bb_21_2 = new BenchmarkAlgorithm("bb",
				"BranchAndBound", bbParams21_2);
		BenchmarkAlgorithm bb_21_3 = new BenchmarkAlgorithm("bb",
				"BranchAndBound", bbParams21_3);
		BenchmarkAlgorithm bb_21_4 = new BenchmarkAlgorithm("bb",
				"BranchAndBound", bbParams21_4);
		BenchmarkAlgorithm bb_42_2 = new BenchmarkAlgorithm("bb",
				"BranchAndBound", bbParams42_2);
		BenchmarkAlgorithm bb_42_3 = new BenchmarkAlgorithm("bb",
				"BranchAndBound", bbParams42_3);
		BenchmarkAlgorithm bb_42_4 = new BenchmarkAlgorithm("bb",
				"BranchAndBound", bbParams42_4);
		BenchmarkAlgorithm bb_33_2 = new BenchmarkAlgorithm("bb",
				"BranchAndBound", bbParams33_2);
		BenchmarkAlgorithm bb_33_3 = new BenchmarkAlgorithm("bb",
				"BranchAndBound", bbParams33_3);
		BenchmarkAlgorithm bb_33_4 = new BenchmarkAlgorithm("bb",
				"BranchAndBound", bbParams33_4);

		// BenchmarkAlgorithm[] algoTypes = {bb_33_4};

		// BenchmarkAlgorithm[] algoTypes = {linear};
		HashMap<String, String> tspParams = new HashMap<String, String>(8);
		tspParams.put("alpha", "22500");
		tspParams.put("beta", "1");
		tspParams.put("gamma", "1");
		tspParams.put("rho", "0.1");
		tspParams.put("Q", "1");
		tspParams.put("sync", "1000");
		tspParams.put("F1", "1");
		tspParams.put("F2", "5");
		tspParams.put("F3", "10");
		tspParams.put("t", "1");
		tspParams.put("l", "5");
		tspParams.put("e", "0");
		BenchmarkAlgorithm tsp = new BenchmarkAlgorithm("tsp", "TSP", tspParams);
		BenchmarkAlgorithm[] algoTypes = { tsp };
		long before = System.nanoTime();
		prepare(home, jobsSize, resourcesSize, seeds, terminalSeeds, dods,
				edods, algoTypes);
		long after = System.nanoTime();
		long computingTime = after - before;
		out.println("Tests preparing time: " + getTime(computingTime));
		out.flush();

		// resultsFile = home+"results_08032012_16:02:11.dat";
		// analyzeFile = home+"analyze_08032012_16:02:11.csv";
		// resultsFile = home+"results_08032012_19:08:16.dat";
		// analyzeFile = home+"analyze_08032012_19:08:16.csv";
		// resultsFile = home+"results_09032012_09:51:42.dat";
		// analyzeFile = home+"analyze_09032012_09:51:42.csv";
		// resultsFile = home+"results_09032012_15:00:28.dat";
		// analyzeFile = home+"analyze_09032012_15:00:28.csv";
		// resultsFile = home+"results_09032012_17:04:09.dat";
		// analyzeFile = home+"analyze_09032012_17:04:09.csv";
		// resultsFile = home+"results_11032012_11:56:35.dat";
		// analyzeFile = home+"analyze_11032012_11:56:35.csv";
		// resultsFile = home+"results_12032012_11:07:17.dat";
		// analyzeFile = home+"analyze_12032012_11:07:17.csv";

		before = System.nanoTime();
		List<ConfigFile> configFiles = getConfigFiles(home, jobsSize,
				resourcesSize, seeds, terminalSeeds, dods, edods, algoTypes);
		after = System.nanoTime();
		computingTime = after - before;
		out.println("Getting config files time: " + getTime(computingTime));
		out.flush();

		long stepMAX = 6000;
		before = System.nanoTime();
		run(configFiles, resultsFile, stepMAX);
		after = System.nanoTime();
		computingTime = after - before;
		out.println("Tests processing time: " + getTime(computingTime));
		out.flush();

		before = System.nanoTime();
		analyze(resultsFile, analyzeFile);
		after = System.nanoTime();
		computingTime = after - before;
		out.println("Analyze processing time: " + getTime(computingTime));

		out.flush();
		out.close();
		System.exit(ReturnCodes.EXIT_ON_SUCCESS.getCode());
	}

	static String getTime(long timeInNS) {
		double time_in_s = timeInNS / 1000000000.0;
		int h = (int) (time_in_s / 3600);
		time_in_s = time_in_s % 3600;
		int m = (int) (time_in_s / 60);
		double s = (time_in_s % 60);
		NumberFormat nf = NumberFormat.getNumberInstance(new Locale("en"));
		nf.setMaximumFractionDigits(2);
		nf.setMinimumFractionDigits(2);
		String ss = nf.format(s);
		ss = "" + (h < 10 ? "0" + h : h) + ":" + (m < 10 ? "0" + m : m) + ":"
				+ (s < 10 ? "0" + ss : ss);
		return ss;
	}
}
