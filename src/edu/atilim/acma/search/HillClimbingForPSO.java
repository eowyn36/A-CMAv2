package edu.atilim.acma.search;

public class HillClimbingForPSO extends AbstractAlgorithm {
	public HillClimbingForPSO(SolutionDesign initialDesign, SolutionDesign goal, AlgorithmObserver observer) {
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

		log("Starting iteration %d. Current distance: %.6f, Closest distance: %.6f", getStepCount(), current.getEuclidianDistance(goal),
				best.getEuclidianDistance(goal));

		SolutionDesign bestNeighbor = null;

		bestNeighbor = current.getClosestNeighbor(goal);

		log("Found neighbor with distance %.6f", bestNeighbor.getEuclidianDistance(goal));

		if (bestNeighbor.isCloserThan(best, goal)) {
			best = bestNeighbor;

			if (observer != null) {
				observer.onUpdateItems(this, current, best, AlgorithmObserver.UPDATE_BEST);
			}
		}

		if (observer != null) {
			observer.onExpansion(this, current.getAllActions().size());
		}

		if (bestNeighbor == current || getStepCount() > maxIters) {
			log("Algorithm finished, the final design's distance to goal is: %.6f", best.getEuclidianDistance(goal));
			finalDesign = best;
			return true;
		} else {
			current = bestNeighbor;

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
