package edu.atilim.acma.search;

import java.util.HashMap;

public class StochasticHCForPSO extends AbstractAlgorithm {
	public StochasticHCForPSO(SolutionDesign initialDesign, AlgorithmObserver observer) {
		super(initialDesign, observer);
		current = best = initialDesign;
	}

	private SolutionDesign current;
	private SolutionDesign best;
	private Double bestDistance;
	private HashMap<String, Double> goal;
	
	private int maxIters = 100;

	@Override
	public String getName() {
		return "Stochastic Hill Climbing";
	}
	
	@Override
	public void setGoal(HashMap<String, Double> goal) {
		this.goal = goal;
	}

	@Override
	public void setInitialDesign(SolutionDesign initialDesign){
		this.initialDesign = initialDesign;
		current = best = initialDesign;
		bestDistance = best.getEuclidianDistance(goal);
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

		SolutionDesign closerNeighbor = current.getCloserNeighbor(goal);

		log("Found neighbor with distance %.6f", closerNeighbor.getEuclidianDistance(goal));

		if (closerNeighbor.isCloserThan(bestDistance, goal)) {
			best = closerNeighbor;
			bestDistance = best.getEuclidianDistance(goal);

			if (observer != null) {
				observer.onUpdateItems(this, current, best, AlgorithmObserver.UPDATE_BEST);
			}
		}

		if (observer != null) {
			observer.onExpansion(this, current.getAllActions().size());
		}

		// if (closerNeighbor == current || getStepCount() > maxIters ) {
		if (closerNeighbor == current) {
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
