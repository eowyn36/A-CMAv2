package edu.atilim.acma.search;

import java.util.HashMap;

public class HillClimbingForPSO extends AbstractAlgorithm {
	public HillClimbingForPSO(SolutionDesign initialDesign, AlgorithmObserver observer) {
		super(initialDesign, observer);
		current = best = initialDesign;
	}

	private SolutionDesign current;
	private SolutionDesign best;
	private Double bestDistance;
	private HashMap<String, Double> goal;

	//private int maxIters = 100;

	@Override
	public void setGoal(HashMap<String, Double> goal) {
		this.goal = goal;
	}

	@Override
	public void setInitialDesign(SolutionDesign initialDesign) {
		this.initialDesign = initialDesign;
		current = best = initialDesign;
		bestDistance = best.getEuclidianDistance(goal);
	}

	@Override
	public String getName() {
		return "Hill Climbing For PSO";
	}

	/*
	 * @Override protected void beforeStart() { AlgorithmObserver observer =
	 * getObserver(); if (observer != null) { observer.onStart(this,
	 * initialDesign); observer.onAdvance(this, 0, maxIters);
	 * observer.onUpdateItems(this, current, best, AlgorithmObserver.UPDATE_BEST
	 * & AlgorithmObserver.UPDATE_CURRENT); } }
	 * 
	 * @Override protected void afterFinish() { AlgorithmObserver observer =
	 * getObserver(); if (observer != null) { observer.onAdvance(this, maxIters,
	 * maxIters); observer.onFinish(this, best); } }
	 */
	@Override
	public boolean step() {
		AlgorithmObserver observer = getObserver();

		//System.out.printf("Starting iteration %d. Current distance: %.6f, Closest distance: %.6f\n", getStepCount(), current.getEuclidianDistance(goal),
		//		best.getEuclidianDistance(goal));
		SolutionDesign bestNeighbor = null;

		bestNeighbor = current.getClosestNeighbor(goal);

		//System.out.printf("Found neighbor with distance %.6f\n", bestNeighbor.getEuclidianDistance(goal));
		if (bestNeighbor.isCloserThan(bestDistance, goal)) {
			best = bestNeighbor;
			bestDistance = best.getEuclidianDistance(goal);
			
			if (observer != null) {
				observer.onUpdateItems(this, current, best, AlgorithmObserver.UPDATE_BEST);
			}
		}

		if (observer != null) {
			observer.onExpansion(this, current.getAllActions().size());
		}

		if (bestNeighbor == current) {
			finalDesign = best;
			return true;

		} else {
			current = bestNeighbor;

			if (observer != null) {
				observer.onUpdateItems(this, current, best, AlgorithmObserver.UPDATE_CURRENT);
			}
		}

		return false;
	}
}
