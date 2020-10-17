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
		Solution currentSolution = new Solution(problemInput);
		bestSolution = (Solution)currentSolution.clone();
		
		int removeNConsecutiveVisitsLimit = (int)(problemInput.getVisitablePOICount() / (3 * problemInput.getTourCount()));
		int numberOfTimesWithNoImprovement = 0;
		while(numberOfTimesWithNoImprovement < MAXIMUM_NUMBER_OF_TIMES_WITH_NO_IMPROVEMENT) {
			while(currentSolution.notStuckInLocalOptimum()) {
				currentSolution.insertStep();
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
			// System.out.println("SHAKE STEP; Sd parameter: " + startRemoveAt + "; Rd parameter: " + removeNConsecutiveVisitsLimit);
			// System.out.println(currentSolution);
			startRemoveAt += removeNConsecutiveVisits;
			removeNConsecutiveVisits++;

			if(startRemoveAt > currentSolution.sizeOfSmallestTour()) {
				if(currentSolution.sizeOfSmallestTour() == 0) {
					startRemoveAt = 0;
				}
				else {
					startRemoveAt %= currentSolution.sizeOfSmallestTour();
				}
			}

			if(removeNConsecutiveVisits == removeNConsecutiveVisitsLimit) {
				removeNConsecutiveVisits = 1;
			}
		}
	}

	public Solution getBestSolution() {
		return this.bestSolution;
	}
}
