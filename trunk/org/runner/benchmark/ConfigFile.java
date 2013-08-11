package org.runner.benchmark;

public class ConfigFile{
	String file;
	Long seed;
	Double dod;
	Double edod;
	Integer resourceSize;
	Integer jobSize;
	BenchmarkAlgorithm algo;
	Long terminalSeed;

	public ConfigFile(String file, Integer resourceSize, Integer jobSize, Long seed, Long terminalSeed, Double dod, Double edod, BenchmarkAlgorithm algo){
		this.file = file.replaceAll("../trunk/", "");
		this.seed = seed;
		this.terminalSeed = terminalSeed;
		this.dod = dod;
		this.edod = edod;
		this.resourceSize = resourceSize;
		this.jobSize = jobSize;
		this.algo = algo;
	}

	public String getAlgoParameters() {
		StringBuilder sb = new StringBuilder();
		if(algo.parameters!=null){
			for(String key : algo.parameters.keySet()){
				sb.append(key+"="+algo.parameters.get(key)+"\t");
			}
			sb.trimToSize();
		}
		return sb.toString();
	}

	public boolean areJustTerminalSeedDifferent(ConfigFile config){
		if(dod == config.dod && edod == config.edod && resourceSize == config.resourceSize && jobSize == config.jobSize && algo.ID.equals(config.algo.ID) && seed == config.seed){
			if(algo.parameters!=null){
				if(config.algo.parameters!=null){
					for(String s : algo.parameters.keySet()){
						if(config.algo.parameters.get(s)==null || !config.algo.parameters.get(s).equals(algo.parameters.get(s)))
							return false;
					}
				}
				else return false;
			}
			if(terminalSeed==config.terminalSeed) return false;
			else return true;
		}
		return false;
	}

	public boolean areJustAlgoDifferent(ConfigFile config){
		if(dod == config.dod && edod == config.edod && resourceSize == config.resourceSize && jobSize == config.jobSize && terminalSeed == config.terminalSeed && seed == config.seed){
			if(algo.parameters!=null){
				if(config.algo.parameters!=null){
					for(String s : algo.parameters.keySet()){
						if(config.algo.parameters.get(s)==null || !config.algo.parameters.get(s).equals(algo.parameters.get(s)))
							return false;
					}
				}
				else return false;
			}
			if(algo.ID.equals(config.algo.ID)) return false;
			else return true;
		}
		return false;
	}

	public boolean equals(ConfigFile f){
		if(jobSize == f.jobSize && resourceSize == f.resourceSize && seed == f.seed && dod == f.dod && edod == f.edod) return true;
		else return false;
	}
}