package TripFinderAlgorithm;

public class IteratedLocalSearch {
	private final int MAXIMUM_NUMBER_OF_TIMES_WITH_NO_IMPROVEMENT = 150;
	private int startRemoveAt = 0;
	private int removeNConsecutiveVisits = 1;
	private ProblemInput problemInput;
	private Solution bestSolution;

	public IteratedLocalSearch(ProblemInput problemInput) {
		this.problemInput = problemInput;
	}

	public void solve() {
		Solution currentSolution = new Solution(problemInput.getStartingPOI(), problemInput.getEndingPOI());
		// FIX: 
		// deep clone here
		bestSolution = currentSolution;

		int numberOfTimesWithNoImprovement = 0;
		while(numberOfTimesWithNoImprovement < MAXIMUM_NUMBER_OF_TIMES_WITH_NO_IMPROVEMENT) {
			while(currentSolution.notStuckInLocalOptimum()) {
				currentSolution.insert();
			}

			if(currentSolution.getScore() > bestSolution.getScore()) {
				// FIX: 
				// deep clone here
				bestSolution = currentSolution;
				removeNConsecutiveVisits = 1;
				numberOfTimesWithNoImprovement = 0;
			}
			else {
				numberOfTimesWithNoImprovement++;
			}

			currentSolution.shake();
			startRemoveAt += removeNConsecutiveVisits;
			removeNConsecutiveVisits++;

			if(startRemoveAt > currentSolution.sizeOfSmallestTour()) {
				startRemoveAt -= currentSolution.sizeOfSmallestTour();
			}

			if(removeNConsecutiveVisits == (int)(problemInput.getVisitablePOICount() / (3 * problemInput.getTourCount()))) {
				removeNConsecutiveVisits = 1;
			}
		}
	}

	public Solution getBestSolution() {
		return this.bestSolution;
	}
}
