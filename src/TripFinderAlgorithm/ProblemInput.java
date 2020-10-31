package TripFinderAlgorithm;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
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
	private int[][] patternsForEachTour;
	private boolean solomon;
	private HashMap<Integer, ArrayList<POI>> POIsForEachPatternType;

	private ProblemInput() {

	}

	private ProblemInput(int tourCount, int visitablePOICount, POI startingPOI, POI endingPOI, POI[] visitablePOIs, 
						int budgetLimit, int[] maxAllowedVisitsForEachType, int[][] patternsForEachTour, 
						HashMap<Integer, ArrayList<POI>> POIsForEachPatternType, boolean solomon) { 
		this.tourCount = tourCount;
		this.visitablePOICount = visitablePOICount;
		this.startingPOI = startingPOI;
		this.endingPOI = endingPOI;
		this.visitablePOIs = visitablePOIs;
		this.solomon = solomon;
		this.budgetLimit = budgetLimit;
		this.maxAllowedVisitsForEachType = maxAllowedVisitsForEachType;
		this.patternsForEachTour = patternsForEachTour;
		this.POIsForEachPatternType = POIsForEachPatternType;

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
		int[][] patternsForEachTour = null;
		HashMap<Integer, ArrayList<POI>> POIsForEachPatternType =  new HashMap<>();
		
		int lineCounter = 0;
		int visitablePOICounter = 0;
		while(scanner.hasNextLine()) {
			String currentLine = scanner.nextLine();
			if(lineCounter == 0) {
				String[] firstLineComponents = currentLine.split(" ");
				tourCount = Integer.parseInt(firstLineComponents[0]);
				visitablePOICount = Integer.parseInt(firstLineComponents[1]);
				visitablePOIs = new POI[visitablePOICount];
				patternsForEachTour = new int[tourCount][];
				budgetLimit = Math.round(Float.parseFloat(firstLineComponents[2]) * 100);
			}
			else if(lineCounter == 1) {
				String[] typesLineComponents = currentLine.split(" ");
				maxAllowedVisitsForEachType = new int[typesLineComponents.length];
				for(int type = 0; type < typesLineComponents.length; type++) {
					maxAllowedVisitsForEachType[type] = Integer.parseInt(typesLineComponents[type]);
				}
			}
			else if(lineCounter == 2) {
				String[] patternsLineComponents = currentLine.split(" ");
				for(int tour = 0; tour < tourCount; tour++) {
					patternsForEachTour[tour] = new int[Integer.parseInt(patternsLineComponents[tour])];
				}
			}
			else if(lineCounter > 2 && lineCounter < 3 + tourCount) {
				String[] patternsLineForTourComponents = currentLine.split(" ");
				int currentTour = lineCounter - 3;
				for(int patternCount = 0; patternCount < patternsLineForTourComponents.length; patternCount++) {
					int patternType = Integer.parseInt(patternsLineForTourComponents[patternCount]) - 1;
					patternsForEachTour[currentTour][patternCount] = patternType;
					if(!POIsForEachPatternType.keySet().contains(patternType)) {
						POIsForEachPatternType.put(patternType, new ArrayList<POI>());
					}
				}
			}
			else if(lineCounter == 3 + tourCount) {
				startingPOI = parsePOIFromLine(currentLine);
				endingPOI = parsePOIFromLine(currentLine);
			}
			else {
				visitablePOIs[visitablePOICounter] = parsePOIFromLine(currentLine);
				for(int type: visitablePOIs[visitablePOICounter].getTypes()) {
					if(POIsForEachPatternType.keySet().contains(type)) {
						POIsForEachPatternType.get(type).add(visitablePOIs[visitablePOICounter]);
					}
				}
				visitablePOIs[visitablePOICounter].createTabuInfo(tourCount);
				visitablePOICounter++;
			}
			lineCounter++;
		}
		scanner.close();

		boolean solomon = filePath.contains("Solomon");

		return new ProblemInput(tourCount, visitablePOICount, startingPOI, endingPOI, visitablePOIs, 
								budgetLimit, maxAllowedVisitsForEachType, patternsForEachTour, POIsForEachPatternType, solomon);
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

	public int[][] getPatternsForEachTour() {
		return this.patternsForEachTour;
	}

	public int[] getPatternsForTour(int tour) {
		return this.patternsForEachTour[tour];
	}

	public HashMap<Integer, ArrayList<POI>> getPOIsForEachPatternType() {
		return this.POIsForEachPatternType;
	}

	public ArrayList<POI> getPOIsForPatternType(int type) {
		return this.POIsForEachPatternType.get(type);
	}
}
