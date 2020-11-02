package TripFinderAlgorithm;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.concurrent.TimeUnit;

import javax.swing.JFrame;

public class IteratedLocalSearch {
	private final int FACTOR_NO_IMPROVEMENT = 10;
	private final int TABU_ITERATIONS = 2;
	private int startRemoveAt = 0;
	private int removeNConsecutiveVisits = 1;
	private Solution bestSolution;

	public void solve(ProblemInput problemInput) {
		final int MAXIMUM_NUMBER_OF_TIMES_WITH_NO_IMPROVEMENT = FACTOR_NO_IMPROVEMENT * getSizeOfFirstRoute(problemInput);
		Solution currentSolution = new Solution(problemInput);
		bestSolution = (Solution)currentSolution.clone();

		Visualizer visualizer = getFrame(currentSolution);
		int sleepingInterval = 200;

		int removeNConsecutiveVisitsLimit = (int)(problemInput.getVisitablePOICount() / (3 * problemInput.getTourCount()));
		int numberOfTimesWithNoImprovement = 0;
		while(numberOfTimesWithNoImprovement < MAXIMUM_NUMBER_OF_TIMES_WITH_NO_IMPROVEMENT) {
			while(currentSolution.notStuckInLocalOptimum()) {
				currentSolution.insertStep();
				if(!currentSolution.isValid()) {
					System.exit(1);
				}
				visualizer.setMessage("Insert step; Inserted: " + currentSolution.justInsertedID);
				visualizer.setWaiting(true);
				visualizer.repaint();
				while (visualizer.isWaiting()) {
					try {
						TimeUnit.MILLISECONDS.sleep(sleepingInterval);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}

			// System.out.println("INSERTION STEP; Number of times no improvement: " + numberOfTimesWithNoImprovement);
			// System.out.println(currentSolution);

			if(currentSolution.getScore() > bestSolution.getScore()) {
				bestSolution = (Solution)currentSolution.clone();
				removeNConsecutiveVisits = 1;
				numberOfTimesWithNoImprovement = 0;
			}
			else {
				numberOfTimesWithNoImprovement++;
			}
			currentSolution.shakeStep(startRemoveAt, removeNConsecutiveVisits, TABU_ITERATIONS);
			if(!currentSolution.isValid()) {
				System.exit(1);
			}
			visualizer.setWaiting(true);
			visualizer.setMessage("Shake step; Sd: " + startRemoveAt + "; Rd: " + removeNConsecutiveVisits);
			visualizer.repaint();
			while (visualizer.isWaiting()) {
				try {
					TimeUnit.MILLISECONDS.sleep(sleepingInterval);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			// System.out.println("SHAKE STEP; Sd parameter: " + startRemoveAt + "; Rd parameter: " + removeNConsecutiveVisits);
			// System.out.println(currentSolution);
			startRemoveAt += removeNConsecutiveVisits;
			removeNConsecutiveVisits++;

			if(startRemoveAt >= currentSolution.sizeOfSmallestTour()) {
				startRemoveAt -= currentSolution.sizeOfSmallestTour();
			}
			if(removeNConsecutiveVisits == removeNConsecutiveVisitsLimit) {
				removeNConsecutiveVisits = 1;
			}
		}
	}

	public Solution getBestSolution() {
		return this.bestSolution;
	}

	public static Visualizer getFrame(Solution currentSolution) {
		JFrame frame = new JFrame("Visualizer");
		frame.setLocationRelativeTo(null);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		Visualizer visualizer = new Visualizer(currentSolution);
		frame.setSize(1900, 950);
		frame.setVisible(true);
		frame.add(visualizer);
		frame.addKeyListener(new KeyListener() {
			@Override
			public void keyPressed(KeyEvent e) {
				if(e.getKeyCode() == 10) {
                    visualizer.setWaiting(false);
                }
                else if(e.getKeyCode() == 81) {
                    System.exit(0);
                }
			}

			@Override
			public void keyReleased(KeyEvent arg0) {

			}

			@Override
			public void keyTyped(KeyEvent e) {

			}
        });

		return visualizer;
	}

	public int getSizeOfFirstRoute(ProblemInput problemInput) {
		Solution currentSolution = new Solution(problemInput);
		while(currentSolution.notStuckInLocalOptimum()) {
			currentSolution.insertStep();
		}
		// clear the assignment set
		for(int tour = 0; tour < problemInput.getTourCount(); tour++) {
			POIInterval currentPOIInterval = currentSolution.getNthPOIIntervalInTourX(0, tour);
			while(currentPOIInterval != null) {
				if(currentPOIInterval.getPOI().getDuration() > 0) {
					currentPOIInterval.getPOI().setAssigned(false);
				}
				currentPOIInterval = currentPOIInterval.getNextPOIInterval();
			}
		}
		return currentSolution.getTourSizes()[0];
	}
}
