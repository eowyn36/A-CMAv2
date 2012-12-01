package edu.atilim.acma.search;

public class PSOAlgorithm extends AbstractAlgorithm {

	private AbstractAlgorithm algo;

	public PSOAlgorithm(SolutionDesign solutionDesign,
			AlgorithmObserver observer, int mi, int ac1, int ac2, int psvmax,
			int psvmin, String algoName) {
		// TODO Auto-generated constructor stub

		if (algoName == "Simple-HC") {
			algo = new HillClimbingForPSO();
		} else if (algoName == "Stochastic-HC") {
			algo = new StochasticHCForPSO();
		} else if (algoName == "FirsChoice-HC") {
			algo = new FirstChoiceHCForPSO();
		}

	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean step() {
		// TODO Auto-generated method stub
		return false;
	}
}