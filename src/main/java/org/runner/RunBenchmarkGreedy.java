package org.runner;

import java.io.File;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.channels.FileChannel;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Random;
import java.util.Scanner;


import org.conf.parameters.ReturnCodes;
import org.exceptions.ContainerDimensionException;
import org.exceptions.EmptyLevelException;
import org.exceptions.NoPathFoundException;
import org.missions.Mission;
import org.time.Time;
import org.util.Edodizer;
import org.util.generators.MissionsFileGenerator;
import org.xml.sax.SAXException;



public class RunBenchmarkGreedy {
	private static final NumberFormat nf = new DecimalFormat("#.###");

	public static void copyFile(String fSource, String fDestination){
		FileChannel in = null; // canal d'entrée
		FileChannel out = null; // canal de sortie
		try {
			// Init
			FileInputStream ioIStream = new FileInputStream(fSource); 
			in = ioIStream.getChannel();
			ioIStream.close();
			File fOut = new File(fDestination);
			if(!fOut.exists()) {
				fOut.getParentFile().mkdirs();
			}
			FileOutputStream ioOStream = new FileOutputStream(fDestination);
			out = ioOStream.getChannel();
			ioOStream.close();
			// Copie depuis le in vers le out
			in.transferTo(0, in.size(), out);
		} catch (Exception e) {
			e.printStackTrace(); // n'importe quelle exception
		} finally { // finalement on ferme
			if(in != null) {
				try {
					in.close();
				} catch (IOException e) {}
			}
			if(out != null) {
				try {
					out.close();
				} catch (IOException e) {}
			}
		}
	}

	private static String getMGenFile(int seed, String outputFile, int jobSize){
		StringBuffer buffer = new StringBuffer();
		buffer.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<document>\n");
		buffer.append("\t<random seed='"+seed+"'/>\n"); 
		buffer.append("\t<terminalFile file='xml/defaultData/TN.xml'/>\n");
		buffer.append("\t<vehiclesFile file='xml/defaultData/vehicleLocal.xml'/>\n");
		buffer.append("\t<containersFile file='xml/results/containers.cont'/>\n");
		buffer.append("\t<stocks outputFile='"+outputFile+"' nb='"+jobSize+"' minTime='00:00:00' maxTime='00:45:00' marginTime='00:02:00' groupID='A'/>\n");
		buffer.append("</document>");
		buffer.trimToSize();
		return buffer.toString();
	}

	public static String getDeploymentFile(String algoType, String tnFileName, String vehicleFileName){
		StringBuffer buffer = new StringBuffer();
		buffer.append("<?xml version=\"1.0\" encoding=\"ISO-8859-1\" ?>\n<document>\n\t<networkConfiguration hostname=\"localhost\" port=\"2000\"/>\n");
		buffer.append("\t<remoteObject rmiBindingName=\"TerminalImpl\" host=\"localhost\"/>\n\t<remoteObject rmiBindingName=\"display\" host=\"localhost\"/>\n");
		buffer.append("\t<remoteObject rmiBindingName=\"JTerminal\" host=\"localhost\" id=\"JTerminal1\"/>\n");
		if(algoType.contains("ACO")){
			buffer.append("\t<remoteObject rmiBindingName='MissionScheduler'\n\t\ttype='OnlineACOScheduler'\n\t\talpha='1'\n\t\tbeta='1'\n\t\tgamma='1'\n\t\tdelta='0.1'\n\t\t"+
					"persistence='0.99'\n\t\tQ='1.5'\n\t\tQR='0'\n\t\thost='localhost'\n\t\tout='localhost'\n\t\tsync='100'\n\t\tF1='1'\n\t\tF2='1'\n\t/>\n");
		}
		else{
			buffer.append("\t<remoteObject rmiBindingName=\"MissionScheduler\" type='"+algoType+"Scheduler' host=\"localhost\" out=\"localhost\"/>\n");
		}
		buffer.append("\t<remoteObject rmiBindingName=\"LaserData\" host=\"localhost\"/>\n\t<remoteObject rmiBindingName=\"TimeScheduler\" host=\"localhost\"/>\n");
		buffer.append("\t<remoteObject rmiBindingName=\"TimeController\" host=\"localhost\"/>\n\t<remoteObject rmiBindingName=\"XMLTerminalComponentParser\" host=\"localhost\"/>\n");
		buffer.append("\t<random seed='21'/>\n\t<laserSystemFile file=\"xml/bornesTN-LARGE_RANGE.xml\"/>\n\t<terminalFile file=\""+tnFileName+"\"/>\n\t<clientFile file=\""+vehicleFileName+"\"/>\n");
		buffer.append("</document>");
		buffer.trimToSize();
		return buffer.toString();
	}

	/**
	 * Creates n tests files for given resources and jobs problem sizes and degree of dynamism.
	 * @param jobsSize
	 * @param resourcesSize
	 * @param number_of_tests_per_file
	 * @param dods
	 * @param edods
	 * @throws SAXException
	 * @throws IOException
	 * @throws NoPathFoundException
	 * @throws ContainerDimensionException
	 * @throws EmptyLevelException
	 */
	public static void prepare(int[] jobsSize, int[][] resourcesSize, int number_of_tests_per_file, double[] dods, double[][] edods, String[] algoTypes)throws SAXException, IOException, NoPathFoundException, ContainerDimensionException, EmptyLevelException{
		String contFileName = "xml/results/containers.cont";
		String home = "../trunk/xml/results/";

		//Generate n file according to seed
		for(int seed=1 ; seed <= number_of_tests_per_file ; seed++ ){
			for(int i=0; i<jobsSize.length ; i++){

				String baseDir = home+jobsSize[i]+"missions/";
				new File(baseDir).mkdirs();
				String fname = baseDir+"mGen_dynamic_"+jobsSize[i]+"_"+seed+".mgen";
				File f = new File(fname);

				String oFile = baseDir+"missions_"+seed+".xml";
				File contFile = new File(contFileName);
				new File("xml/results/").mkdirs();
				if(!contFile.exists()){
					copyFile("xml/testData/dynamic/containers.cont", "../trunk/"+contFileName);
					copyFile("xml/testData/dynamic/containers.cont", contFileName);
				}

				PrintWriter pw = new PrintWriter(f);
				pw.append(getMGenFile(seed, oFile, jobsSize[i]));
				pw.flush();
				pw.close();

				System.out.println("BUILDING "+fname+" ...");
				new MissionsFileGenerator("localhost", fname);

				copyFile(oFile, oFile.replaceAll("../trunk/", ""));
				HashMap<Mission, Time> missionMap = Edodizer.getMissionMap(oFile.replaceAll("../trunk/", ""));

				//EDODIZER
				for(int dodIndex = 0 ; dodIndex < dods.length ; dodIndex++){
					double dod = dods[dodIndex];
					String sDod = nf.format(dod);
					double[] edodOfDod = edods[dodIndex];
					for(int edodIndex = 0 ; edodIndex < edodOfDod.length ; edodIndex++){
						double edod = edodOfDod[edodIndex];
						String sEdod = nf.format(edod);	
						HashMap<Mission, Time> edodizedMap = Edodizer.edodize(new HashMap<Mission, Time>(missionMap), dod, edod, new Random(seed));

						String edodizeFileName = oFile.substring(0, oFile.length()-".xml".length())+"_"+sDod+"_"+sEdod+".xml";
						Edodizer.write(new File(edodizeFileName), edodizedMap);

						//Create TN.xml with created mission file
						String TNMissionlessFileName = "xml/testData/TN_missionless.xml";
						Scanner scan = new Scanner(new File(TNMissionlessFileName));
						String tnFileName = baseDir+"TN_"+seed+"_"+sDod+"_"+sEdod+".xml";
						pw = new PrintWriter(new File(tnFileName));
						while(scan.hasNextLine()){
							String line = scan.nextLine();
							if(line.contains("</terminal>")){
								pw.append("<include file='"+contFileName+"'/>\n");
								pw.append("<include file='"+edodizeFileName.replaceAll("../trunk/", "")+"'/>\n"); 
							}
							pw.append(line+"\n");
						}
						scan.close();
						pw.flush();
						pw.close();


						//Create deployment files
						new File(baseDir).mkdirs();
						//Take care of vehicle files
						for(int j=0; j<resourcesSize[i].length ; j++){

							int size = resourcesSize[i][j];
							String resourceDir = baseDir+size+"vehicles/";
							new File(resourceDir).mkdir();
							//Vehicle file
							String vName = resourceDir+"vehicles-"+size+".xml";

							for(String algoType : algoTypes){
								String dName = resourceDir+"deployment"+algoType+"_"+seed+"_"+sDod+"_"+sEdod+".d2cts";
								f = new File(dName);
								pw = new PrintWriter(f);
								pw.append(getDeploymentFile(algoType, tnFileName.replaceAll("../trunk/", ""), vName.replaceAll("../trunk/", "")));
								pw.flush();
								pw.close();
							}
						}


					}	
				}
				for(int j=0; j<resourcesSize[i].length ; j++){
					int size = resourcesSize[i][j];
					String resourceDir = baseDir+size+"vehicles/";
					new File(resourceDir).mkdir();
					//Vehicle file
					String vName = resourceDir+"vehicles-"+size+".xml";

					File vehicleFile = new File(vName);
					pw = new PrintWriter(vehicleFile);
					String standardVFile = "xml/testData/vehicles-20.xml";
					boolean commentOpen = false;
					Scanner scan = new Scanner(new File(standardVFile));
					int count = 0;
					while(scan.hasNextLine()){
						String line = scan.nextLine();
						if(line.contains("<straddleCarrier id=")){
							count++;
							if(count==size+1){
								commentOpen = true;
								pw.append("<comment>\n");
							}
						}
						if(line.contains("</vehicles>")&&commentOpen){
							pw.append("</comment>\n");
						}
						pw.append(line+"\n");
					}
					pw.flush();
					pw.close();
					scan.close();
				}
			}
		}
	}


	/**
	 * @param args
	 * @throws IOException 
	 * @throws EmptyLevelException 
	 * @throws ContainerDimensionException 
	 * @throws NoPathFoundException 
	 * @throws SAXException 
	 */
	public static void main(String[] args) throws IOException, SAXException, NoPathFoundException, ContainerDimensionException, EmptyLevelException {
		int number_of_tests_per_file = 10;
		int[] jobsSize = {10, 20,30,40,50,75,100};
		int[][] resourcesSize = { {2, 3} , {5, 7} , {7,10} , {7,10} , {7,10} , {15}, {20}};
		double[] dods = {0, 0.25, 0.5 , 0.75, 1};
		double[][] edods = {{0}, {0.33, 0.66, 1.0} , {0.33, 0.66, 1.0}, {0.33, 0.66, 1.0}, {0.33, 0.66, 1.0}};
		String[] algoTypes = {"Greedy", "Linear", "Random", "ACO"};
//		int[] jobsSize = {10, 20};
//		int[][] resourcesSize = { {2, 3} , {5, 7}};
//		double[] dods = {0, 1};
//		double[][] edods = {{0}, {0.33, 0.66, 1.0}};
//		
		long before = System.nanoTime();
		prepare(jobsSize,resourcesSize,number_of_tests_per_file, dods, edods, algoTypes);
		long after =  System.nanoTime();
		long computingTime = after - before;
		double time_in_s = computingTime/1000000000.0;
		int h = (int)(time_in_s / 3600);
		time_in_s = time_in_s % 3600;
		int m = (int)(time_in_s / 60);
		double s = (time_in_s % 60);
		NumberFormat nf = NumberFormat.getNumberInstance(new Locale("en"));
		nf.setMaximumFractionDigits(2);
		nf.setMinimumFractionDigits(2);
		String ss = nf.format(s);
		ss = "" + (h<10 ? "0"+h : h) +":"+ (m<10 ? "0"+m : m) +":"+ (s<10 ? "0"+ss : ss);
		System.out.println("Tests preparing time: "+ss);

		/*String[] configFiles = { "xml/testData/test2/deploymentLocal_ACO_10-GAMMA.d2cts"};
		int[] syncSizes = {5000 , 5000, 5000, 5000, 5000 , 5000, 5000, 5000, 5000, 5000};

		String resultsFile = args[0];
		File f = new File(resultsFile);
		f.createNewFile();
		PrintWriter writer = new PrintWriter(f);

		String localhostName = "localhost";
		writer.append("TEST SESSION OF "+new Date().toString()+"\n");
		writer.flush();
		for(int testI = 0 ; testI<configFiles.length ; testI++){
			//URL u = RunBenchmark.class.getResource("/"+configFiles[testI]);

			//TEST 1
			String configFile = configFiles[testI];

			writer.append("====================================================================================\n");
			writer.append("TEST "+testI+"\nCONFIG FILE: "+configFile+"\n");
			writer.flush();
			for(int syncSizesI=0; syncSizesI < syncSizes.length; syncSizesI++){
				SimulationRunner sc = new SimulationRunner(configFile,null,localhostName,null,false);

				while(!sc.hasDone()){
					try {
						Thread.sleep(100);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				TimeController tc = new TimeController();
				ACOMissionScheduler.setSyncSize(syncSizes[syncSizesI]);
				if(syncSizesI==0) writer.append("ACO parameters: "+ACOMissionScheduler.getGlobalParameters()+"\n");
				writer.append("                            -------------------------------                            \n");
				writer.append("SYNC SIZE = "+syncSizes[syncSizesI]+"\n");

				long i = 1;
				long startTime = System.nanoTime();
				while(i<=stepMAX){
					if(i%60==0) System.out.println("T="+tc.getStep());
					tc.nextStep(false);
					i++;
				}
				long endTime = System.nanoTime();
				IndicatorPane ip;
				try {
					ip = Terminal.missionScheduler.getIndicatorPane();
					writer.append("RESULTS :\n"+ip+"\n");
				} catch (RemoteException e) {
					e.printStackTrace();
				}
				writer.append("WORKLOADS :\n");
				for(StraddleCarrier rsc : RemoteTerminal.straddleCarriers.values()){
					writer.append(rsc.getWorkload().toString()+"\n");
				}
				double execTime = (endTime - startTime)/1000000000.0;
				writer.append("EXEC TIME : "+execTime+"s\n");

				tc = null;
				ip = null;

				sc.destroy();
				while(!sc.hasDone()){
					try {
						Thread.sleep(100);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}

				writer.flush();
				System.gc();
				try {
					Thread.sleep(3000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				System.gc();
			}
			writer.append("====================================================================================\n");
		}			
		writer.close();*/
		System.exit(ReturnCodes.EXIT_ON_SUCCESS.getCode());
	}
}
