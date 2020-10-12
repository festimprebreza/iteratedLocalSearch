package TripFinderAlgorithm;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class ProblemInput {
	private int tourCount;
	private int visitablePOICount;
	private POI startingPOI;
	private POI endingPOI;
	private POI[] visitablePOIs;

	private ProblemInput() {

	}

	private ProblemInput(int tourCount, int visitablePOICount, POI startingPOI, POI endingPOI, POI[] visitablePOIs) { 
		this.tourCount = tourCount;
		this.visitablePOICount = visitablePOICount;
		this.startingPOI = startingPOI;
		this.endingPOI = endingPOI;
		this.visitablePOIs = visitablePOIs;
	}

	public static ProblemInput getProblemInputFromFile(String filePath) throws FileNotFoundException {
		Scanner scanner = new Scanner(new File(filePath));
		int tourCount = 0;
		int visitablePOICount = 0;
		POI startingPOI = null;
		POI endingPOI = null;
		POI[] visitablePOIs = null;

		
		int lineCounter = 0;
		int visitablePOICounter = 0;
		while(scanner.hasNextLine()) {
			if(lineCounter == 0) {
				String firstLine = scanner.nextLine();
				String[] firstLineComponents = firstLine.split(" ");
				tourCount = Integer.parseInt(firstLineComponents[0]);
				visitablePOICount = Integer.parseInt(firstLineComponents[1]);
				visitablePOIs = new POI[visitablePOICount];
			}
			else if(lineCounter == 1) {
				String secondLine = scanner.nextLine();
				startingPOI = parsePOIFromLine(secondLine);
				// FIX: do something smarter
				endingPOI = parsePOIFromLine(secondLine);
			}
			else {
				String currentLine = scanner.nextLine();
				visitablePOIs[visitablePOICounter] = parsePOIFromLine(currentLine);
				visitablePOICounter++;
			}
			lineCounter++;
		}
		scanner.close();

		return new ProblemInput(tourCount, visitablePOICount, startingPOI, endingPOI, visitablePOIs);
	}
	
	public static POI parsePOIFromLine(String line) {
		String[] lineComponents = line.split(" ");
		int ID = Integer.parseInt(lineComponents[0]);
		float xCoordinate = Float.parseFloat(lineComponents[1]);
		float yCoordinate = Float.parseFloat(lineComponents[2]);
		float duration = Float.parseFloat(lineComponents[3]);
		float score = Float.parseFloat(lineComponents[4]);
		float openingTime = Float.parseFloat(lineComponents[5]);
		float closingTime = Float.parseFloat(lineComponents[6]);

		return new POI(ID, xCoordinate, yCoordinate, duration, score, openingTime, closingTime);
	}

	public int getTourCount() {
		return this.tourCount;
	}

	public int getVisitablePOICount() {
		return this.visitablePOICount;
	}

	public POI getStartingPOI() {
		return this.startingPOI;
	}

	public POI getEndingPOI() {
		return this.endingPOI;
	}

	public POI[] getVisitablePOIs() {
		return this.visitablePOIs;
	}
}