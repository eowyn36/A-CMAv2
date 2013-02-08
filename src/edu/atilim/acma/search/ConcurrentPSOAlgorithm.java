package edu.atilim.acma.search;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import edu.atilim.acma.RunConfig;
import edu.atilim.acma.design.Design;

public class ConcurrentPSOAlgorithm extends ConcurrentMultiRunAlgorithm {
	String hcAlgorithmName;
	private int swarmSize;
	private Double ac1;
	private Double ac2;
	int w;
	int iterationCount;
		
	public ConcurrentPSOAlgorithm() {
	}

	public ConcurrentPSOAlgorithm(String name, RunConfig config, Design initialDesign, int runCount, String hcAlgorithmName, int swarmSize, Double ac1, Double ac2, int w, int iterationCount) {
		super(name, config, initialDesign, runCount);
		this.hcAlgorithmName = hcAlgorithmName;
		this.swarmSize = swarmSize;
		this.ac1 = ac1;
		this.ac2 = ac2;
		this.w = w;
		this.iterationCount = iterationCount;

	}
	
	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		super.writeExternal(out);
		
		out.writeInt(0); //version
		out.writeChars(hcAlgorithmName);
		out.writeInt(swarmSize);
		out.writeDouble(ac1);
		out.writeDouble(ac2);
		out.writeInt(w);
		out.writeInt(iterationCount);
	}
	
	@Override
	public AbstractAlgorithm spawnAlgorithm() {
		AbstractAlgorithm hcAlgorithm = null;
		if (hcAlgorithmName.equals("Simple-HC"))
			hcAlgorithm = new HillClimbingForPSO(null, null);
		else if (hcAlgorithmName.equals("Stochastic-HC"))
			hcAlgorithm = new StochasticHCForPSO(null, null);
		else if (hcAlgorithmName.equals("FirsChoice-HC"))
			hcAlgorithm = new FirstChoiceHCForPSO(null, null);				
		
		PSOAlgorithm algo = new PSOAlgorithm(new SolutionDesign(getInitialDesign(), getConfig()), null, hcAlgorithm, iterationCount, swarmSize, ac1,
				ac2, w);
		return algo;
	}
	
	@Override
	public String getRunInfo() {
		return String.format("Particle Swarm Optimization. HC Algorithm: "+hcAlgorithmName+", Swarm Size: "+swarmSize+", ac1: "+ac1+", ac2: "+ac2+"w: "+w+", Iteration Count: "+iterationCount);
	}
	
	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		super.readExternal(in);
		
		in.readInt();
		hcAlgorithmName = in.readLine();
		swarmSize = in.readInt();
		ac1 = in.readDouble();
		ac2 = in.readDouble();
		w = in.readInt();
		iterationCount = in.readInt();

	}
}