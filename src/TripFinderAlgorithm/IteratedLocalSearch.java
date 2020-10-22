package TripFinderAlgorithm;

public class IteratedLocalSearch {
	private final int FACTOR_NO_IMPROVEMENT = 10;
	private int startRemoveAt = 0;
	private int removeNConsecutiveVisits = 1;
	private Solution bestSolution;

	public void solve(ProblemInput problemInput) {
		final int MAXIMUM_NUMBER_OF_TIMES_WITH_NO_IMPROVEMENT = FACTOR_NO_IMPROVEMENT * getSizeOfFirstRoute(problemInput);
		Solution currentSolution = new Solution(problemInput);
		bestSolution = (Solution)currentSolution.clone();
		
		int removeNConsecutiveVisitsLimit = (int)(problemInput.getVisitablePOICount() / (3 * problemInput.getTourCount()));
		int numberOfTimesWithNoImprovement = 0;
		while(numberOfTimesWithNoImprovement < MAXIMUM_NUMBER_OF_TIMES_WITH_NO_IMPROVEMENT) {
			while(currentSolution.notStuckInLocalOptimum()) {
				currentSolution.insertStep();
				if(!currentSolution.isValid()) {
					System.exit(1);
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
			currentSolution.shakeStep(startRemoveAt, removeNConsecutiveVisits);
			if(!currentSolution.isValid()) {
				System.exit(1);
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
