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
	private int budgetLimit;
	private int[] maxAllowedVisitsForEachType;
	private boolean solomon;

	private ProblemInput() {

	}

	private ProblemInput(int tourCount, int visitablePOICount, POI startingPOI, POI endingPOI, POI[] visitablePOIs, 
						int budgetLimit, int[] maxAllowedVisitsForEachType, boolean solomon) { 
		this.tourCount = tourCount;
		this.visitablePOICount = visitablePOICount;
		this.startingPOI = startingPOI;
		this.endingPOI = endingPOI;
		this.visitablePOIs = visitablePOIs;
		this.solomon = solomon;
		this.budgetLimit = budgetLimit;
		this.maxAllowedVisitsForEachType = maxAllowedVisitsForEachType;

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
		double euclidianDistance = MathExtension.getEuclidianDistanceOfTwoPOIs(fromPOI, toPOI);
		if(solomon) {
			return ((int)(euclidianDistance / 100)) * 10;
		}
		else {
			return (int)(euclidianDistance / 10);
		}
	}

	public static ProblemInput getProblemInputFromFile(String filePath) throws FileNotFoundException {
		Scanner scanner = new Scanner(new File(filePath));
		int tourCount = 0;
		int visitablePOICount = 0;
		POI startingPOI = null;
		POI endingPOI = null;
		POI[] visitablePOIs = null;
		int budgetLimit = 0;
		int[] maxAllowedVisitsForEachType = null;
		
		int lineCounter = 0;
		int visitablePOICounter = 0;
		while(scanner.hasNextLine()) {
			if(lineCounter == 0) {
				String firstLine = scanner.nextLine();
				String[] firstLineComponents = firstLine.split(" ");
				tourCount = Integer.parseInt(firstLineComponents[0]);
				visitablePOICount = Integer.parseInt(firstLineComponents[1]);
				visitablePOIs = new POI[visitablePOICount];
				budgetLimit = Math.round(Float.parseFloat(firstLineComponents[2]) * 100);
			}
			else if(lineCounter == 1) {
				String secondLine = scanner.nextLine();
				String[] secondLineComponents = secondLine.split(" ");
				maxAllowedVisitsForEachType = new int[secondLineComponents.length];
				for(int type = 0; type < secondLineComponents.length; type++) {
					maxAllowedVisitsForEachType[type] = Integer.parseInt(secondLineComponents[type]);
				}
			}
			else if(lineCounter == 2) {
				String thirdLine = scanner.nextLine();
				startingPOI = parsePOIFromLine(thirdLine);
				endingPOI = parsePOIFromLine(thirdLine);
			}
			else {
				String currentLine = scanner.nextLine();
				visitablePOIs[visitablePOICounter] = parsePOIFromLine(currentLine);
				visitablePOICounter++;
			}
			lineCounter++;
		}
		scanner.close();

		boolean solomon = filePath.contains("Solomon");

		return new ProblemInput(tourCount, visitablePOICount, startingPOI, endingPOI, visitablePOIs, 
								budgetLimit, maxAllowedVisitsForEachType, solomon);
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
		int entranceFee = 0;
		boolean[] typeBitArray = {};

		if(lineComponents.length > 7) {
			entranceFee = Math.round(Float.parseFloat(lineComponents[7]) * 100);
			typeBitArray = new boolean[lineComponents.length - 7];
			for(int component = 8; component < lineComponents.length; component++) {
				if(lineComponents[component].equals("1")) {
					typeBitArray[component - 8] = true;
				}
				else {
					typeBitArray[component - 8] = false;
				}
			}
		}

		return new POI(ID, xCoordinate, yCoordinate, duration, score, openingTime, closingTime, entranceFee, typeBitArray);
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

	public int getBudgetLimit() {
		return this.budgetLimit;
	}

	public int[] getMaxAllowedVisitsForEachType() {
		return this.maxAllowedVisitsForEachType;
	}

	public int getMaxAllowedVisitsForType(int type) {
		return this.maxAllowedVisitsForEachType[type];
	}
}
