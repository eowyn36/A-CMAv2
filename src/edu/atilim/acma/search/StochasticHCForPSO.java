package edu.atilim.acma.search;

public class StochasticHCForPSO extends AbstractAlgorithm {
	public StochasticHCForPSO(SolutionDesign initialDesign, SolutionDesign goal, AlgorithmObserver observer) {
		super(initialDesign, observer);
		this.goal = goal;
		current = best = initialDesign;
	}

	private SolutionDesign current;
	private SolutionDesign best;
	private SolutionDesign goal;
	
	private int maxIters = 100;

	@Override
	public String getName() {
		return "Hill Climbing";
	}

	@Override
	protected void beforeStart() {
		AlgorithmObserver observer = getObserver();
		if (observer != null) {
			observer.onStart(this, initialDesign);
			observer.onAdvance(this, 0, maxIters);
			observer.onUpdateItems(this, current, best, AlgorithmObserver.UPDATE_BEST & AlgorithmObserver.UPDATE_CURRENT);
		}
	}

	@Override
	protected void afterFinish() {
		AlgorithmObserver observer = getObserver();
		if (observer != null) {
			observer.onAdvance(this, maxIters, maxIters);
			observer.onFinish(this, best);
		}
	}

	@Override
	public boolean step() {
		AlgorithmObserver observer = getObserver();
		current.getEuclidianDistance(goal);

		log("Starting iteration %d. Current distance: %.6f, Closest distance: %.6f", getStepCount(), current.getEuclidianDistance(goal),
				best.getEuclidianDistance(goal));

		SolutionDesign closerNeighbor = current.getCloserNeighbor(goal);

		log("Found neighbor with distance %.6f", closerNeighbor.getEuclidianDistance(goal));

		if (closerNeighbor.isCloserThan(best, goal)) {
			best = closerNeighbor;

			if (observer != null) {
				observer.onUpdateItems(this, current, best, AlgorithmObserver.UPDATE_BEST);
			}
		}

		if (observer != null) {
			observer.onExpansion(this, current.getAllActions().size());
		}

		if (closerNeighbor == current || getStepCount() > maxIters) {
			log("Algorithm finished, the final design's distance to goal is: %.6f", best.getEuclidianDistance(goal));
			finalDesign = best;
			return true;
		} else {
			current = closerNeighbor;

			if (observer != null) {
				observer.onUpdateItems(this, current, best, AlgorithmObserver.UPDATE_CURRENT);
			}
		}
		
		if (observer != null) {
			observer.onAdvance(this, getStepCount(), maxIters);
		}
		return false;
	}
}
