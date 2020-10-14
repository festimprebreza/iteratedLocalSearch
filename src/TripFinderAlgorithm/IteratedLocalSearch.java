package TripFinderAlgorithm;

public class IteratedLocalSearch {
	private final int MAXIMUM_NUMBER_OF_TIMES_WITH_NO_IMPROVEMENT = 2;
	private int startRemoveAt = 0;
	private int removeNConsecutiveVisits = 3;
	private ProblemInput problemInput;
	private Solution bestSolution;

	public IteratedLocalSearch(ProblemInput problemInput) {
		this.problemInput = problemInput;
	}

	public void solve() {
		Solution currentSolution = new Solution(problemInput);
		bestSolution = (Solution)currentSolution.clone();

		int numberOfTimesWithNoImprovement = 0;
		while(numberOfTimesWithNoImprovement < MAXIMUM_NUMBER_OF_TIMES_WITH_NO_IMPROVEMENT) {
			while(currentSolution.notStuckInLocalOptimum()) {
				currentSolution.insertStep();
			}

			System.out.println(currentSolution);

			if(currentSolution.getScore() > bestSolution.getScore()) {
				bestSolution = (Solution)currentSolution.clone();
				removeNConsecutiveVisits = 3;
				numberOfTimesWithNoImprovement = 0;
			}
			else {
				numberOfTimesWithNoImprovement++;
			}

			currentSolution.shakeStep(startRemoveAt, removeNConsecutiveVisits);
			System.out.println(currentSolution);
			System.out.println("========================================================");
			System.out.println("========================================================");
			System.out.println("========================================================");
			System.out.println("========================================================");
			// startRemoveAt += removeNConsecutiveVisits;
			// removeNConsecutiveVisits++;

		// 	// FIX:
		// 	// maybe this update should be done with the size of the smallest tour before shake step, not
		// 	// after it
		// 	if(startRemoveAt > currentSolution.sizeOfSmallestTour()) {
		// 		startRemoveAt -= currentSolution.sizeOfSmallestTour();
		// 	}

		// 	if(removeNConsecutiveVisits == (int)(problemInput.getVisitablePOICount() / (3 * problemInput.getTourCount()))) {
		// 		removeNConsecutiveVisits = 1;
		// 	}
		}
	}

	public Solution getBestSolution() {
		return this.bestSolution;
	}
}
