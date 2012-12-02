/*package edu.atilim.acma.search;

public class PSOAlgorithm extends AbstractAlgorithm {

	private AbstractAlgorithm algo;
	private double v;
	private double location;
	private double vmin;
	private double vmax;

	public PSOAlgorithm(SolutionDesign initialDesign, AlgorithmObserver observer, int mi, int ac1, int ac2, int psvmax,
			int psvmin, String algoName) {
		super(initialDesign, observer);
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
}*/