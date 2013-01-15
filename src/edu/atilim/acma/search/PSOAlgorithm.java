/*******************************
 * 
 * PSO 
 * A basic variant of the PSO algorithm works by having a population (called a swarm) of candidate solutions 
 * (called particles). These particles are moved around in the search-space according to a few simple formulae. 
 * The movements of the particles are guided by their own best known position in the search-space as well as 
 * the entire swarm's best known position. When improved positions are being discovered these will then come 
 * to guide the movements of the swarm. The process is repeated and by doing so it is hoped, but not guaranteed, 
 * that a satisfactory solution will eventually be discovered.
 * 
 */
package edu.atilim.acma.search;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import edu.atilim.acma.metrics.MetricRegistry;

public class PSOAlgorithm extends AbstractAlgorithm {

	private AbstractAlgorithm hcAlgorithm;
	private double vmin;
	private double vmax;
	private int iterations = 10;

	private int swarmSize = 50;
	private int randomDepth = 100;
	private int w = 1;
	private double ac1, ac2 = 20;
	private double globalBest = Double.MAX_VALUE;

	private Set<Particle> swarm;
	private SolutionDesign bestDesign;
	private HashMap<String, Double> bdLoc;

	public PSOAlgorithm(SolutionDesign initialDesign, AlgorithmObserver observer, AbstractAlgorithm hcAlgorithm, int iterations) {
		super(initialDesign, observer);
		//this.vmax = vmax;
		//this.vmin = vmin;
		this.hcAlgorithm = hcAlgorithm;
		this.iterations = iterations;
		this.swarm = new HashSet<Particle>();
		log("Initial Design Score:" + initialDesign.getScore());
		generateInitialSwarm();
	}
//for testing
	public PSOAlgorithm(SolutionDesign initialDesign, AlgorithmObserver observer, AbstractAlgorithm hcAlgorithm, int iterations, int w) {
		super(initialDesign, observer);
		//this.vmax = vmax;
		//this.vmin = vmin;
		this.hcAlgorithm = hcAlgorithm;
		this.iterations = iterations;
		this.swarm = new HashSet<Particle>();
		log("Initial Design Score:" + initialDesign.getScore());
		generateInitialSwarm();
	}
	private void generateInitialSwarm() {
		for (int i = 0; i < swarmSize; i++) {
			swarm.add(new Particle(initialDesign.getRandomNeighbor(randomDepth)));
		}
		for (Particle particle : swarm) {
			particle.updatePersonalBest(particle.getCurrentDesign());
			if (particle.getScore() < globalBest) {
				globalBest = particle.getScore();
				bestDesign = particle.getCurrentDesign();
				bdLoc = bestDesign.getLocation();
			}
		}
	}

	@Override
	public String getName() {
		return "Particle Swarm Optimization";
	}

	@Override
	protected void beforeStart() {
		AlgorithmObserver observer = getObserver();
		if (observer != null) {
			observer.onStart(this, initialDesign);
		}
	}

	@Override
	protected void afterFinish() {
		AlgorithmObserver observer = getObserver();
		if (observer != null) {
			observer.onFinish(this, finalDesign);
		}
	}

	@Override
	public boolean step() {
		// TODO Vmax, Vmin
		AlgorithmObserver observer = getObserver();

		for (Particle particle : swarm) {
			// update personal best
			if (particle.getScore() < particle.getPBestScore())
				particle.updatePersonalBest(particle.getCurrentDesign());

			// update global best
			if (particle.getScore() < globalBest) {
				globalBest = particle.getScore();
				bestDesign = particle.getCurrentDesign();
			}
		}
		log("Global best and Personel Bests are updated.");
		if (getStepCount() > iterations) {
			finalDesign = bestDesign;
			log("Algorithm finished, the final design score: %.6f", bestDesign.getScore());
			return true;
		}
		log("Starting iteration %d. Best score: %.6f", getStepCount(), bestDesign.getScore());

		// update velocity
		List<MetricRegistry.Entry> metrics = MetricRegistry.entries();
		Random rndm = new Random();
		double newV;
		String metricName;
		int j = 1;
		for (Particle particle : swarm) {
			// double newVelX = (w * vx) + (r1 * C1) * (pBestX - lx) + (r2 * C2)
			// * (gBestX - lx)
			for (MetricRegistry.Entry entry : MetricRegistry.entries()) {
				metricName = entry.getName();
				newV = (w * particle.getVelocity(metricName)) + (rndm.nextDouble() * ac1)
						* (particle.getPBestLocation(metricName) - particle.getLocation(metricName)) + (rndm.nextDouble() * ac2)
						* (bdLoc.get(metricName) - particle.getLocation(metricName));
				System.out.println("Particle: " + j +" V = "+ newV);
				j++;
				/*
				 * if(newV > vmax) newV = vmax; if(newV < vmin) newV = vmin;
				 */
				particle.setVelocity(entry.getName(), newV);
			}
		}
		log("Velocities are updated.");

		// update particle position
		HashMap<String, Double> goal = new HashMap<String, Double>();
		
		int i = 0;
		for (Particle particle : swarm) {
			// calculate the new position
			for (MetricRegistry.Entry entry : metrics) {
				goal.put(entry.getName(), particle.getLocation(entry.getName()) + particle.getVelocity(entry.getName()));
			}
			
			hcAlgorithm.setGoal(goal);
			hcAlgorithm.setInitialDesign(particle.getCurrentDesign());
			hcAlgorithm.start(false);
			log(hcAlgorithm.getName() + " for Particle " + i++ +"Before: "+ particle.getScore()+ ", After: "+ hcAlgorithm.finalDesign.getScore());
			
			particle.setCurrentDesign(hcAlgorithm.finalDesign);
		}
		
		log("Positions are updated.");

		log("Finished iteration %d. Best score: %.6f", getStepCount(), bestDesign.getScore());

		if (observer != null) {
			observer.onAdvance(this, getStepCount() + 1, iterations);
		}
		
		return false;

	}

	private static class Particle {

		private SolutionDesign currentDesign;
		private HashMap<String, Double> cdLoc;
		private HashMap<String, Double> velocity;
		private SolutionDesign personelBest;
		private HashMap<String, Double> pbLoc;

		public Particle(SolutionDesign currentDesign) {
			this.currentDesign = currentDesign;
			cdLoc = currentDesign.getLocation();

			setInitialVelocity();
		}

		public void setInitialVelocity() {
			velocity = new HashMap<String, Double>();
			List<MetricRegistry.Entry> metrics = MetricRegistry.entries();
			Random generator = new Random();
			for (MetricRegistry.Entry entry : metrics)
				velocity.put(entry.getName(), generator.nextDouble() * 2.0 - 1.0);
		}

		public void setCurrentDesign(SolutionDesign sd) {
			currentDesign = sd;
			cdLoc = sd.getLocation();
		}

		public void updatePersonalBest(SolutionDesign sd) {
			personelBest = sd;
			pbLoc = sd.getLocation();
		}

		public Double getPBestScore() {
			return personelBest.getScore();
		}

		public Double getPBestLocation(String metricName) {
			return pbLoc.get(metricName);
		}

		public Double getLocation(String metricName) {
			return cdLoc.get(metricName);
		}

		public Double getScore() {
			return currentDesign.getScore();
		}

		public SolutionDesign getCurrentDesign() {
			return currentDesign;
		}

		public Double getVelocity(String metricName) {
			return velocity.get(metricName);
		}

		public void setVelocity(String metricName, Double value) {
			velocity.put(metricName, value);
		}

	}

}