package edu.atilim.acma.search;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

import edu.atilim.acma.Core;
import edu.atilim.acma.RunConfig;
import edu.atilim.acma.design.Design;
import edu.atilim.acma.metrics.MetricCalculator;
import edu.atilim.acma.metrics.MetricNormalizer;
import edu.atilim.acma.metrics.MetricRegistry;
import edu.atilim.acma.metrics.MetricSummary;
import edu.atilim.acma.transition.TransitionManager;
import edu.atilim.acma.transition.actions.Action;
import edu.atilim.acma.util.ACMAUtil;
import edu.atilim.acma.util.Log;
import edu.atilim.acma.util.Pair;

public class SolutionDesign implements Iterable<SolutionDesign>, Comparable<SolutionDesign> {
	private static final int numProcs = Runtime.getRuntime().availableProcessors();

	private Design design;
	private RunConfig config;
	private List<Action> actions;

	private double score = Double.NaN;
	private HashMap<String, Double> paretoMap;

	public Design getDesign() {
		return design;
	}

	public RunConfig getConfig() {
		return config;
	}

	public void ensureScore() {
		if (Core.paretoMode) {
			ensureParetoMap();
			return;
		}

		if (Double.isNaN(score)) {
			score = MetricCalculator.normalize(MetricCalculator.calculate(getDesign(), config), config);
		}
	}

	public void ensureParetoMap() {
		if (paretoMap == null) {
			paretoMap = MetricNormalizer.normalizeEach(MetricCalculator.calculate(getDesign(), config).getSummary(), config);

			score = 0.0;
			for (double item : paretoMap.values()) {
				score += item;
			}
		}
	}

	public double getScore() {
		ensureScore();
		return score;
	}

	public MetricSummary getMetricSummary() {
		return MetricCalculator.calculate(getDesign(), config).getSummary();
	}

	public HashMap<String, Double> getLocation() {
		return MetricCalculator.getAverages((MetricCalculator.calculate(getDesign(), config)));
	}

	public Double getLocation(String metricName) {
		return getLocation().get(metricName);
	}

	public SolutionDesign getClosestNeighbor(HashMap<String, Double> goal) {
		SolutionDesign best = this;

		if (numProcs == 1) {
			for (SolutionDesign sd : this) {
				if (sd.isCloserThan(best, goal))
					best = sd;
			}
			return best;
		}

		List<Action> actions = getAllActions();

		int perthread = actions.size() / numProcs;

		List<ClosestDesignFinder> cdf = new ArrayList<SolutionDesign.ClosestDesignFinder>(numProcs);
		for (int i = 0; i < numProcs; i++) {
			cdf.add(new ClosestDesignFinder(actions, i * perthread, perthread, goal));
		}

		try {
			// Submit to thread pool
			List<Future<SolutionDesign>> futures = ACMAUtil.threadPool.invokeAll(cdf);

			// Remainder
			for (int i = perthread * numProcs; i < actions.size(); i++) {
				SolutionDesign cur = apply(actions.get(i));
				if (cur.isBetterThan(best))
					best = cur;
			}

			for (Future<SolutionDesign> f : futures) {
				SolutionDesign cur = f.get();
				if (cur.isBetterThan(best))
					best = cur;
			}
		} catch (Exception e) {
			Log.severe("Exception in parallel design extraction: %s", e.getMessage());
			e.printStackTrace();
			return this;
		}

		return best;
	}

	public Boolean isCloserThan(SolutionDesign best, HashMap<String, Double> goal) {
		if ((Double.compare(best.getEuclidianDistance(goal), getEuclidianDistance(goal))) > 0)
			return true;
		return false;
	}

	// For Stochastic Hill Climbing
	public SolutionDesign getCloserNeighbor(HashMap<String, Double> goal) {
		
		//TODO threat
		
		SolutionDesign best = this;

		HashMap<SolutionDesign, Double> closerDesigns = new HashMap<SolutionDesign, Double>();
		Double sumOfDistances = 0.0;
		double currentDistance = this.getEuclidianDistance(goal);

		for (SolutionDesign sd : this) {
			if (sd.isCloserThan(this, goal)) {
				closerDesigns.put(sd, currentDistance - sd.getEuclidianDistance(goal));
				sumOfDistances += currentDistance - sd.getEuclidianDistance(goal);
			}
		}

		double randomnumber = new Random().nextDouble() * sumOfDistances;
		double total = 0.0;

		for (SolutionDesign key : closerDesigns.keySet()) {
			total = total + closerDesigns.get(key);
			if (randomnumber < total)
				return best = key;
		}
		return best;
	}

	// For First-Choice Hill Climbing
	public SolutionDesign getCloserRandomNeighbor(HashMap<String, Double> goal) {
		SolutionDesign closerRandomNeighbor = this;
		int rndmNo;
		List<Action> actions = getAllActions();
		SolutionDesign randomNeighbor;
		
		while(!actions.isEmpty()){
			rndmNo = ACMAUtil.RANDOM.nextInt(actions.size());
			randomNeighbor = apply(actions.get(rndmNo));
			actions.remove(rndmNo);
			if (randomNeighbor.getEuclidianDistance(goal) < this.getEuclidianDistance(goal))
				return closerRandomNeighbor = randomNeighbor;
		}
	
		return closerRandomNeighbor;
	}

	public double getEuclidianDistance(HashMap<String, Double> goal) {

		List<MetricRegistry.Entry> metrics = MetricRegistry.entries();
		HashMap<String, Double> averages = getLocation();
		double sum = 0;

		for (MetricRegistry.Entry entry : metrics) {
			sum += Math.pow((averages.get(entry.getName()) - goal.get(entry.getName())), 2);
		}
		return Math.sqrt(sum);
	}

	public SolutionDesign getBetterNeighbor() {
		for (SolutionDesign sd : this) {
			if (sd.isBetterThan(this))
				return sd;
		}
		return this;
	}

	public SolutionDesign getBestNeighbor() {
		SolutionDesign best = this;

		if (numProcs == 1) {
			for (SolutionDesign sd : this) {
				if (sd.isBetterThan(best))
					best = sd;
			}
			return best;
		}

		List<Action> actions = getAllActions();

		int perthread = actions.size() / numProcs;

		List<BestDesignFinder> bdf = new ArrayList<SolutionDesign.BestDesignFinder>(numProcs);
		for (int i = 0; i < numProcs; i++) {
			bdf.add(new BestDesignFinder(actions, i * perthread, perthread));
		}

		try {
			// Submit to thread pool
			List<Future<SolutionDesign>> futures = ACMAUtil.threadPool.invokeAll(bdf);

			// Remainder
			for (int i = perthread * numProcs; i < actions.size(); i++) {
				SolutionDesign cur = apply(actions.get(i));
				if (cur.isBetterThan(best))
					best = cur;
			}

			for (Future<SolutionDesign> f : futures) {
				SolutionDesign cur = f.get();
				if (cur.isBetterThan(best))
					best = cur;
			}
		} catch (Exception e) {
			Log.severe("Exception in parallel design extraction: %s", e.getMessage());
			e.printStackTrace();
			return this;
		}

		return best;
	}

	public SolutionDesign getRandomNeighbor() {
		List<Action> actions = getAllActions();
		if (actions.isEmpty())
			return this;
		return apply(actions.get(ACMAUtil.RANDOM.nextInt(actions.size())));
	}

	public SolutionDesign getRandomNeighbor(int depth) {
		SolutionDesign random = this;
		for (int i = 0; i < depth; i++)
			random = random.getRandomNeighbor();
		return random;
	}

	public List<Action> getAllActions() {
		if (actions == null) {
			actions = new ArrayList<Action>(TransitionManager.getPossibleActions(design, config));
		}
		return actions;
	}

	public SolutionDesign(Design design, RunConfig config) {
		this.design = design;
		this.config = config;
	}

	public boolean isBetterThan(SolutionDesign other) {
		return Core.paretoMode ? isParetoBetterThan(other) : compareTo(other) > 0;
	}

	public boolean isParetoBetterThan(SolutionDesign other) {
		ensureParetoMap();
		other.ensureParetoMap();

		boolean foundbetter = false;

		for (Entry<String, Double> pi : paretoMap.entrySet()) {
			double otherval = other.paretoMap.get(pi.getKey());

			if (pi.getValue() < otherval) {
				foundbetter = true;
			} else if (pi.getValue() > otherval) {
				return false;
			}
		}

		return foundbetter;
	}

	@Override
	public int compareTo(SolutionDesign o) {
		return Double.compare(compareScoreTo(o), 0.0);
	}

	public double compareScoreTo(SolutionDesign o) {
		return o.getScore() - getScore();
	}

	@Override
	public Iterator<SolutionDesign> iterator() {
		return new Iter();
	}

	public Iterator<Pair<Action, Double>> pairIterator() {
		return new PairIter();
	}

	public SolutionDesign apply(Action action) {
		Design copyDesign = applyInternal(action);
		SolutionDesign newDesign = new SolutionDesign(copyDesign, config);
		copyDesign.logModification(String.format("[%.6f]%s", newDesign.getScore(), action.toString()));
		return newDesign;
	}

	protected Design applyInternal(Action action) {
		Design copyDesign = getDesign().copy();
		action.perform(copyDesign);
		return copyDesign;
	}

	private class BestDesignFinder implements Callable<SolutionDesign> {
		private List<Action> actions;
		private int offset;
		private int count;

		private BestDesignFinder(List<Action> actions, int offset, int count) {
			this.actions = actions;
			this.offset = offset;
			this.count = count;
		}

		@Override
		public SolutionDesign call() throws Exception {
			SolutionDesign best = SolutionDesign.this;
			for (int i = offset; i < offset + count; i++) {
				Action action = actions.get(i);
				SolutionDesign newDesign = apply(action);
				if (newDesign.isBetterThan(best))
					best = newDesign;
			}

			return best;
		}

	}

	// BestDesignFinder'dan sadece bi kac satiri farkli belki BestDesignFinder'i
	// kullanmanin bi yolunu bulabiliriz ?
	private class ClosestDesignFinder implements Callable<SolutionDesign> {
		private List<Action> actions;
		private int offset;
		private int count;
		private HashMap<String, Double> goal;

		private ClosestDesignFinder(List<Action> actions, int offset, int count, HashMap<String, Double> goal) {
			this.actions = actions;
			this.offset = offset;
			this.count = count;
			this.goal = goal;
		}

		@Override
		public SolutionDesign call() throws Exception {
			SolutionDesign best = SolutionDesign.this;
			for (int i = offset; i < offset + count; i++) {
				Action action = actions.get(i);
				SolutionDesign newDesign = apply(action);
				if (newDesign.isCloserThan(best, goal))
					best = newDesign;
			}

			return best;
		}

	}

	private class Iter implements Iterator<SolutionDesign> {
		private Iterator<Action> innerIterator;

		private Iter() {
			innerIterator = getAllActions().iterator();
		}

		@Override
		public boolean hasNext() {
			return innerIterator.hasNext();
		}

		@Override
		public SolutionDesign next() {
			return apply(innerIterator.next());
		}

		@Override
		public void remove() {
			innerIterator.remove();
		}
	}

	private class PairIter implements Iterator<Pair<Action, Double>> {
		private Iterator<Action> innerIterator;

		private PairIter() {
			innerIterator = getAllActions().iterator();
		}

		@Override
		public boolean hasNext() {
			return innerIterator.hasNext();
		}

		@Override
		public Pair<Action, Double> next() {
			Action action = innerIterator.next();
			SolutionDesign nd = apply(action);
			return Pair.create(action, nd.getScore());
		}

		@Override
		public void remove() {
			innerIterator.remove();
		}
	}
}
