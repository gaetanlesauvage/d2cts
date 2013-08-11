package org.runner.benchmark;

import java.util.ArrayList;
import java.util.HashMap;

public class Results {
	HashMap<String, Result> results;
	//Simulation with same parameters which only have a different algorithm
	HashMap<String, ArrayList<Result> > resultsAlgo;
	
	public Results(){
		results = new HashMap<String, Result>();
		resultsAlgo = new HashMap<String, ArrayList<Result>>();
	}

	private String getNameAlgo(Result r){
		String s = r.configFile.file.replaceFirst("deployment","");
		s = s.replaceFirst(r.configFile.algo.ID+"_", ""); 
		return s;
	}
	
	public void add(Result r){
		results.put(r.configFile.file, r);
		String nameAlgo = getNameAlgo(r);
		ArrayList<Result> l = null;
		if(resultsAlgo.containsKey(nameAlgo)){
			l = resultsAlgo.get(nameAlgo);
		}
		else l = new ArrayList<Result>();
		boolean added = false;
		ConfigFile cr = r.configFile;
		for(int i=0; i<l.size()&&!added;i++){
			Result r2 = l.get(i);
			ConfigFile cr2 = r2.configFile;
			if(cr2.jobSize>=cr.jobSize&&cr2.resourceSize>=cr.resourceSize&&cr2.seed>=cr.seed&&cr2.dod>=cr.dod&&cr2.edod>=cr.edod&&cr2.terminalSeed>=cr.terminalSeed){
				l.add(i,r);
				added = true;
			}
		}
		if(!added) l.add(r);
		
		resultsAlgo.put(nameAlgo, l);
	}

	public HashMap<String, ArrayList<String>> getResultsByAlgoAndTSeed(){
		HashMap<Long, ArrayList<String>> map = new HashMap<Long, ArrayList<String>>();

		for(String k : resultsAlgo.keySet()){
			Long tSeed = resultsAlgo.get(k).get(0).configFile.terminalSeed;

			ArrayList<String> l = null;
			if(map.containsKey(tSeed)){
				l = map.get(tSeed);
			}
			else{
				l = new ArrayList<String>();
			}
			l.add(k);
			map.put(tSeed, l);
		}

		HashMap<String, ArrayList<String>> resultMap = new HashMap<String, ArrayList<String>>();
		for(Long tSeed : map.keySet()){
			ArrayList<String> l = map.get(tSeed);
			for(String s : l){
				ArrayList<Result> correspondingResults = resultsAlgo.get(s);
				//for(int i=0; i<correspondingResults.size(); i++){
					Result first = correspondingResults.get(0);
					String gpeName = first.configFile.jobSize+"_"+first.configFile.resourceSize+"_"+first.configFile.seed+"_"+first.configFile.dod+"_"+first.configFile.edod;
					ArrayList<String> commonList = null;
					if(resultMap.containsKey(gpeName)) commonList = resultMap.get(gpeName);
					else commonList = new ArrayList<String>();
					
					commonList.add(getNameAlgo(first));
					resultMap.put(gpeName, commonList);
			}
		}
		
		/*for(String key : resultMap.keySet()){
			ArrayList<String> value = resultMap.get(key);
			System.out.println("Gpe : "+key+": ");
			for(String algoName : value){
				System.out.println("\talgo name : "+algoName+": ");
				for(Result r : resultsAlgo.get(algoName)){
					System.out.println("\t\t * "+r.configFile.file+" tSeed="+r.configFile.terminalSeed);
				}
			}
				
		}*/
		return resultMap;
	}

}
