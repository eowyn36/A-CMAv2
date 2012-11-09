package edu.atilim.acma.search;

public class HillClimbingAlgorithm extends AbstractAlgorithm {
	public HillClimbingAlgorithm(SolutionDesign initialDesign,
			AlgorithmObserver observer) {
		super(initialDesign, observer);
		
		current = best = initialDesign;
	}

	private SolutionDesign current;
	private SolutionDesign best;
	
	private int numRestarts = 0;
	private int restartCount = 10;			//bunu 1 yap
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
		
		log("Starting iteration %d. Current score: %.6f, Best score: %.6f", getStepCount(), current.getScore(), best.getScore());
		SolutionDesign bestNeighbor = null;
		
		//if (restartCount == 0)
		//	bestNeighbor = current.getBestNeighbor();
		//else
		//	bestNeighbor = current.getBetterNeighbor();
		
		bestNeighbor = current.getBestNeighbor();
		
		log("Found neighbor with score %.6f score", bestNeighbor.getScore());
		
		if (bestNeighbor.isBetterThan(best)) {
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
				log("Algorithm finished, the final design score: %.6f", best.getScore());
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
