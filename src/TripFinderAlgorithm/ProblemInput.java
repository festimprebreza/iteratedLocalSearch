package TripFinderAlgorithm;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
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

		assignTravelDistances();
	}

	public void assignTravelDistances() {
		HashMap<Integer, Integer> travelDistancesForPOINumberX;
		for(POI fromPOI: visitablePOIs) {
			travelDistancesForPOINumberX = new HashMap<>();
			for(POI toPOI: visitablePOIs) {
				if(fromPOI == toPOI) {
					continue;
				}
				travelDistancesForPOINumberX.put(toPOI.getID(), getDistance(fromPOI, toPOI));
			}
			// put data for starting POI
			travelDistancesForPOINumberX.put(startingPOI.getID(), getDistance(fromPOI, startingPOI));
			// put data for endingPOI
			travelDistancesForPOINumberX.put(endingPOI.getID(), getDistance(fromPOI, endingPOI));
			fromPOI.setTravelDistances(travelDistancesForPOINumberX);
		}

		travelDistancesForPOINumberX = new HashMap<>();
		// get data for startingPOI
		for(POI toPOI: visitablePOIs) {
			travelDistancesForPOINumberX.put(toPOI.getID(), getDistance(startingPOI, toPOI));
		}
		startingPOI.setTravelDistances(travelDistancesForPOINumberX);
		travelDistancesForPOINumberX = new HashMap<>();
		// get data for endingPOI
		for(POI toPOI: visitablePOIs) {
			travelDistancesForPOINumberX.put(toPOI.getID(), getDistance(endingPOI, toPOI));
		}
		endingPOI.setTravelDistances(travelDistancesForPOINumberX);
	}

	public int getDistance(POI fromPOI, POI toPOI) {
		long insideSquareRoot = (long)(Math.pow(fromPOI.getXCoordinate() - toPOI.getXCoordinate(), 2) + 
								Math.pow(fromPOI.getYCoordinate() - toPOI.getYCoordinate(), 2));
		double squareRoot = Math.sqrt((double)insideSquareRoot);
		// FIX:
		// check that /10 here
		return (int)Math.round(squareRoot / 10);
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

		// FIX:
		// add here for each POI the distances to other POIs

		return new ProblemInput(tourCount, visitablePOICount, startingPOI, endingPOI, visitablePOIs);
	}
	
	public static POI parsePOIFromLine(String line) {
		String[] lineComponents = line.split(" ");
		int ID = Integer.parseInt(lineComponents[0]);
		long xCoordinate = Math.round(Double.parseDouble(lineComponents[1]) * 1000);
		long yCoordinate = Math.round(Double.parseDouble(lineComponents[2]) * 1000);
		int duration = Math.round(Float.parseFloat(lineComponents[3]) * 100);
		int score = Math.round(Float.parseFloat(lineComponents[4]) * 100);
		int openingTime = Math.round(Float.parseFloat(lineComponents[5]) * 100);
		int closingTime = Math.round(Float.parseFloat(lineComponents[6]) * 100);

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
