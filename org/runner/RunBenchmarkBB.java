package org.runner;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;

import org.conf.parameters.ReturnCodes;
import org.scheduling.MissionScheduler;
import org.scheduling.display.IndicatorPane;
import org.system.Terminal;
import org.time.TimeController;
import org.vehicles.StraddleCarrier;

public class RunBenchmarkBB {
	/**
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {
		long stepMAX = 5000;

		// String[] configFiles = {
		// "xml/testData/test1/deploymentLocal_ACO_10-GAMMA.d2cts"/* ,
		// "xml/testData/test2/deploymentLocal_ACO_10-GAMMA.d2cts" ,
		// "xml/testData/test3/deploymentLocal_ACO_10-GAMMA.d2cts",
		// "xml/testData/test4/deploymentLocal_ACO_10-GAMMA.d2cts","xml/testData/test5/deploymentLocal_ACO_10-GAMMA.d2cts"*/};
		// String[] configFiles = {
		// "xml/testData/test1/deploymentLocal_ACO_10-GAMMA.d2cts","xml/testData/test1/deploymentLocal_ACO_10-GAMMA.d2cts","xml/testData/test1/deploymentLocal_ACO_10-GAMMA.d2cts","xml/testData/test1/deploymentLocal_ACO_10-GAMMA.d2cts","xml/testData/test1/deploymentLocal_ACO_10-GAMMA.d2cts","xml/testData/test1/deploymentLocal_ACO_10-GAMMA.d2cts","xml/testData/test1/deploymentLocal_ACO_10-GAMMA.d2cts","xml/testData/test1/deploymentLocal_ACO_10-GAMMA.d2cts","xml/testData/test1/deploymentLocal_ACO_10-GAMMA.d2cts","xml/testData/test1/deploymentLocal_ACO_10-GAMMA.d2cts"};
		String[] configFiles = {
				"xml/testData/testBB/deploymentLocal_BB_10-2.d2cts",
				"xml/testData/testBB/deploymentLocal_BB_10-3.d2cts",
				"xml/testData/testBB/deploymentLocal_BB_10-4.d2cts" };
		String resultsFile = args[0];
		File f = new File(resultsFile);
		f.createNewFile();
		PrintWriter writer = new PrintWriter(f);

		
		writer.append("TEST SESSION OF " + new Date().toString() + "\n");
		writer.flush();
		for (int testI = 0; testI < configFiles.length; testI++) {
			// URL u = RunBenchmark.class.getResource("/"+configFiles[testI]);

			// TEST 1
			String configFile = configFiles[testI];

			writer.append("====================================================================================\n");
			writer.append("TEST " + testI + "\nCONFIG FILE: " + configFile
					+ "\n");
			writer.flush();
			SimulationRunner sc = new SimulationRunner(configFile, null, false);
			while (!sc.hasDone()) {
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			TimeController tc = new TimeController();

			writer.append("                            -------------------------------                            \n");

			long i = 1;
			long startTime = System.nanoTime();
			tc.nextStep(false);
			i++;

			while (i <= stepMAX) {
				if (i % 60 == 0)
					System.out.println("T=" + tc.getStep());
				tc.nextStep(false);
				i++;
			}
			long endTime = System.nanoTime();
			IndicatorPane ip = MissionScheduler.getInstance()
					.getIndicatorPane();
			writer.append("RESULTS :\n" + ip + "\n");

			writer.append("WORKLOADS :\n");
			for (StraddleCarrier rsc : Terminal.getInstance()
					.getStraddleCarriers()) {
				writer.append(rsc.getWorkload().toString() + "\n");
			}
			double execTime = (endTime - startTime) / 1000000000.0;
			writer.append("EXEC TIME : " + execTime + "s\n");

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
		}
		writer.append("====================================================================================\n");
		writer.close();
		System.exit(ReturnCodes.EXIT_ON_SUCCESS.getCode());
	}
}
