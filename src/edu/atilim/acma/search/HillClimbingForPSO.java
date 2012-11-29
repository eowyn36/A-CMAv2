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
	
	private int numRestarts = 0;
	private int restartCount = 1;
	private int restartDepth = 100;
	
	public int getRestartCount() {
		return restartCount;
	}

	public void setRestartCount(int restartCount) {
		this.restartCount = restartCount;
	}

	public int getRestartDepth() {
		return restartDepth;
	}

	public void setRestartDepth(int restartDepth) {
		this.restartDepth = restartDepth;
	}

	@Override
	public String getName() {
		return "Hill Climbing";
	}
	
	@Override
	protected void beforeStart() {
		AlgorithmObserver observer = getObserver();
		if (observer != null) {
			observer.onStart(this, initialDesign);
			observer.onAdvance(this, 0, restartCount + 1);
			observer.onUpdateItems(this, current, best, AlgorithmObserver.UPDATE_BEST & AlgorithmObserver.UPDATE_CURRENT);
		}
	}
	
	@Override
	protected void afterFinish() {
		AlgorithmObserver observer = getObserver();
		if (observer != null) {
			observer.onAdvance(this, restartCount + 1, restartCount + 1);
			observer.onFinish(this, best);
		}
	}

	@Override
	public boolean step() {
		AlgorithmObserver observer = getObserver();
		current.getEuclidianDistance(goal);
		log("Starting iteration %d. Current distance: %.6f, Closest distance: %.6f", getStepCount(), current.getEuclidianDistance(goal), best.getEuclidianDistance(goal));
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
		
		if (bestNeighbor == current) {
			log("Found local best point.");
			
			if (numRestarts < restartCount) {
				numRestarts++;
				log("Restarting from random point with %d depth.", restartDepth);
				current = best.getRandomNeighbor(restartDepth);
				
				if (observer != null)
					observer.onAdvance(this, numRestarts, restartCount + 1);
			} else {
				log("Algorithm finished, the final design's distance to goal is: %.6f", best.getEuclidianDistance(goal));
				finalDesign = best;
				return true;
			}
		} else {
			current = bestNeighbor;
			
			if (observer != null) {
				observer.onUpdateItems(this, current, best, AlgorithmObserver.UPDATE_CURRENT);
			}
		}
		
		return false;
	}
}
