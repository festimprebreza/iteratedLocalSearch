package TripFinderAlgorithm;

import java.io.FileNotFoundException;

public class TripGenerator {
	public static void main(String[] args) {
		String instancePath = args[0];
		
		ProblemInput problemInput = null;
		try {
			problemInput = ProblemInput.getProblemInputFromFile(instancePath);
		}
		catch(FileNotFoundException ex) {
			System.out.println("Could not find file. " + ex.getMessage());
			System.exit(1);
		}

		IteratedLocalSearch ILSAlgorithm = new IteratedLocalSearch(problemInput);
		ILSAlgorithm.solve();
		Solution bestSolution = ILSAlgorithm.getBestSolution();
		System.out.println(bestSolution);
	}
}
