package TripFinderAlgorithm;

public class IteratedLocalSearch {
	private final int FACTOR_NO_IMPROVEMENT = 10;
	private final int TABU_ITERATIONS = 2;
	private final int NUMBER_OF_PIVOT_CHANGES_DURING_ONE_FULL_EXECUTION = 4;
	private final int PROBABILITY_OF_NOT_REUSING_PIVOTS = 7;
	private int startRemoveAt = 0;
	private int removeNConsecutiveVisits = 1;
	private Solution bestSolution;

	public void solve(ProblemInput problemInput) {
		final int MAXIMUM_NUMBER_OF_TIMES_WITH_NO_IMPROVEMENT = FACTOR_NO_IMPROVEMENT * getSizeOfFirstRoute(problemInput);
		final int REMOVE_N_CONSECUTIVE_VISITS_LIMIT = (int)(problemInput.getVisitablePOICount() / (3 * problemInput.getTourCount()));
		final int PIVOT_CHANGE_LIMIT = MAXIMUM_NUMBER_OF_TIMES_WITH_NO_IMPROVEMENT / (NUMBER_OF_PIVOT_CHANGES_DURING_ONE_FULL_EXECUTION + 1);

		Solution currentSolution = new Solution(problemInput);
		bestSolution = (Solution)currentSolution.clone();

		if(!currentSolution.insertPivots(0, 0, PROBABILITY_OF_NOT_REUSING_PIVOTS)) {
			return;
		}

		int currentIteration = 0;
		int pivotChangeCounter = 0;
		int numberOfTimesWithNoImprovement = 0;
		while(numberOfTimesWithNoImprovement < MAXIMUM_NUMBER_OF_TIMES_WITH_NO_IMPROVEMENT) {
			if(pivotChangeCounter == PIVOT_CHANGE_LIMIT) {
				currentSolution.changePivots(PROBABILITY_OF_NOT_REUSING_PIVOTS);
				pivotChangeCounter = 0;
			}

			while(currentSolution.notStuckInLocalOptimum()) {
				currentSolution.insertStep();
				if(!currentSolution.isValid()) {
					System.out.println("Solution is not valid");
					return;
				}
			}

			if(currentSolution.getScore() > bestSolution.getScore()) {
				bestSolution = (Solution)currentSolution.clone();
				removeNConsecutiveVisits = 1;
				numberOfTimesWithNoImprovement = 0;
			}
			else {
				numberOfTimesWithNoImprovement++;
			}
			
			currentSolution.shakeStep(startRemoveAt, removeNConsecutiveVisits, TABU_ITERATIONS, currentIteration);
			if(!currentSolution.isValid()) {
				System.out.println("Solution is not valid");
					return;
			}

			startRemoveAt += removeNConsecutiveVisits;
			removeNConsecutiveVisits++;

			if(startRemoveAt >= currentSolution.sizeOfSmallestTour()) {
				startRemoveAt -= currentSolution.sizeOfSmallestTour();
			}
			if(removeNConsecutiveVisits == REMOVE_N_CONSECUTIVE_VISITS_LIMIT) {
				removeNConsecutiveVisits = 1;
			}

			currentIteration++;
			pivotChangeCounter++;
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
		// restore the data for the real algorithm execution
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
