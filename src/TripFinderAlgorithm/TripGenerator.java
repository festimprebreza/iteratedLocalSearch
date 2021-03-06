package TripFinderAlgorithm;

import java.io.FileNotFoundException;

public class TripGenerator {
	public static void main(String[] args) {
		String instancePath = args[0];

		ProblemInput problemInput = null;

		try {
			problemInput = ProblemInput.getProblemInputFromFile(instancePath);
		} catch (FileNotFoundException ex) {
			System.out.println("Could not find file. " + ex.getMessage());
			System.exit(1);
		}

		IteratedLocalSearch ILSAlgorithm = new IteratedLocalSearch();
		ILSAlgorithm.solve(problemInput);
		Solution bestSolution = ILSAlgorithm.getBestSolution();
		if(bestSolution.isValid()) {
			System.out.println("BEST SOLUTION: ");
			System.out.println(bestSolution);
		}
	}
}
