package TripFinderAlgorithm;

public class IteratedLocalSearch {
	private final int MAXIMUM_NUMBER_OF_TIMES_WITH_NO_IMPROVEMENT = 150;
	private int startRemoveAt = 0;
	private int removeNConsecutiveVisits = 1;
	private Solution bestSolution;

	public void solve(ProblemInput problemInput) {
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
}
