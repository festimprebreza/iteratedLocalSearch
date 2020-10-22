package TripFinderAlgorithm;

import java.util.ArrayList;

public class Solution implements Cloneable {
	private POIInterval[] startingPOIIntervals;
	private POIInterval[] endingPOIIntervals;
	private ProblemInput problemInput;
	private int score;
	private boolean stuckInLocalOptimum;
	private int totalMoneySpent;
	private int[] visitCountOfEachType;
	private int[] tourSizes;
	private int sizeOfSmallestTour = Integer.MAX_VALUE;
	private int[] availableTime;

	public Solution(ProblemInput problemInput) {
		this.problemInput = problemInput;
		POI startingPOI = problemInput.getStartingPOI();
		POI endingPOI = problemInput.getEndingPOI();
		startingPOIIntervals = new POIInterval[problemInput.getTourCount()];
		endingPOIIntervals = new POIInterval[problemInput.getTourCount()];
		this.availableTime = new int[problemInput.getTourCount()];
		for(int tour = 0; tour < this.availableTime.length; tour++) {
			this.availableTime[tour] = 0;
		}

		for(int tour = 0; tour < problemInput.getTourCount(); tour++) {
			this.startingPOIIntervals[tour] = new POIInterval(startingPOI, startingPOI.getOpeningTime(), -1);
			this.endingPOIIntervals[tour] = new POIInterval(endingPOI, endingPOI.getClosingTime(), -1);
	
			this.startingPOIIntervals[tour].setNextPOIInterval(this.endingPOIIntervals[tour]);
			this.endingPOIIntervals[tour].setPreviousPOIInterval(this.startingPOIIntervals[tour]);

			this.endingPOIIntervals[tour].setArrivalTime(this.startingPOIIntervals[tour].getPOI().getTravelTimeToPOI(
																			this.endingPOIIntervals[tour].getPOI().getID()));
			this.endingPOIIntervals[tour].updateWaitTime();
			this.availableTime[tour] += this.endingPOIIntervals[tour].getWaitTime();
		}

		tourSizes = new int[problemInput.getTourCount()];
		totalMoneySpent = 0;
		visitCountOfEachType = new int[problemInput.getMaxAllowedVisitsForEachType().length];
	}

	public int getScore() {
		return this.score;
	}

	public int getVisits() {
		int totalVisits = 0;
		for(int tour = 0; tour < tourSizes.length; tour++) {
			totalVisits += tourSizes[tour];
		}
		return totalVisits;
	}

	public boolean notStuckInLocalOptimum() {
		return !this.stuckInLocalOptimum;
	}

	public void insertStep() {
		ArrayList<Object> bestInsertInfo = getInfoForBestPOIToInsert();

		if(bestInsertInfo != null) {
			POI bestPOIToBeInserted = (POI)bestInsertInfo.get(0);
			POIInterval POIIntervalAfterBestInsertPosition = (POIInterval)bestInsertInfo.get(1);
			int tourToInsertIn = (int)bestInsertInfo.get(2);
			int shiftOfBestPOI = (int)bestInsertInfo.get(3);
			int assignedTypeOfBestPOI = (int)bestInsertInfo.get(4);

			POIInterval newPOIInterval = insertPOI(bestPOIToBeInserted, POIIntervalAfterBestInsertPosition, assignedTypeOfBestPOI);
			calculateArriveStartAndWaitForNewPOI(newPOIInterval);
			updateParametersForFollowingVisitsAfterInsert(newPOIInterval.getNextPOIInterval(), shiftOfBestPOI);
			updateMaxShiftForPreviousVisitsAndThis(newPOIInterval);
			this.score += bestPOIToBeInserted.getScore();
			this.totalMoneySpent += bestPOIToBeInserted.getEntranceFee();
			this.visitCountOfEachType[assignedTypeOfBestPOI] += 1;
			this.stuckInLocalOptimum = false;
			this.tourSizes[tourToInsertIn] += 1;
			this.availableTime[tourToInsertIn] -= (shiftOfBestPOI - newPOIInterval.getWaitTime());
			return;
		}

		this.stuckInLocalOptimum = true;
	}

	public ArrayList<Object> getInfoForBestPOIToInsert() {
		ArrayList<Object> insertInfo = new ArrayList<>();
		float highestRatio = -1;
		POI bestPOIToBeInserted = null;
		POIInterval POIIntervalAfterBestInsertPosition = null;
		int tourToInsertIn = -1;
		int shiftOfBestPOI = -1;
		int assignedTypeOfBestPOI = -1;
		insertInfo.add(bestPOIToBeInserted);
		insertInfo.add(POIIntervalAfterBestInsertPosition);
		insertInfo.add(tourToInsertIn);
		insertInfo.add(shiftOfBestPOI);
		insertInfo.add(assignedTypeOfBestPOI);

		for(int tour = 0; tour < problemInput.getTourCount(); tour++) {
			for(POI currentPOI: problemInput.getVisitablePOIs()) {
				if(currentPOI.isAssigned()) {
					continue;
				}

				POIInterval POIIntervalAfterBestInsertPositionForThisPOI = null;

				POIInterval POIIntervalAfterInsertPosition = startingPOIIntervals[tour].getNextPOIInterval();
				int shiftOnBestInsertForNewPOI = Integer.MAX_VALUE;
				int assignedType = -1;
				float bestDenominatorForNewPOI = Float.MAX_VALUE;
				while(POIIntervalAfterInsertPosition != null) {
					for(int type: currentPOI.getTypes()) {
						if(canInsertBecauseOfConstraints(currentPOI.getEntranceFee(), type)) {
							int shiftForNewPOI = getShift(currentPOI, POIIntervalAfterInsertPosition);
							if(canInsertBeforeThisPOI(currentPOI, POIIntervalAfterInsertPosition, shiftForNewPOI)) {
								float denominatorForNewPOI = calculateDenominator(shiftForNewPOI, currentPOI.getEntranceFee(), 
																					type, tour);
								if(Float.compare(denominatorForNewPOI, bestDenominatorForNewPOI) < 0) {
									bestDenominatorForNewPOI = denominatorForNewPOI;
									shiftOnBestInsertForNewPOI = shiftForNewPOI;
									POIIntervalAfterBestInsertPositionForThisPOI = POIIntervalAfterInsertPosition;
									assignedType = type;
								}
							}
						}
					}
					POIIntervalAfterInsertPosition = POIIntervalAfterInsertPosition.getNextPOIInterval();
				}
				float highestRatioForThisPOI = -1;
				if(shiftOnBestInsertForNewPOI != Integer.MAX_VALUE) {
					highestRatioForThisPOI = (float)Math.pow(currentPOI.getScore() / 100.0f, 2) / bestDenominatorForNewPOI;
				}

				if(Float.compare(highestRatioForThisPOI, highestRatio) > 0) {
					highestRatio = highestRatioForThisPOI;
					insertInfo.set(0, currentPOI);
					insertInfo.set(1, POIIntervalAfterBestInsertPositionForThisPOI);
					insertInfo.set(2, tour);
					insertInfo.set(3, shiftOnBestInsertForNewPOI);
					insertInfo.set(4, assignedType);
				}
			}
		}

		if(Float.compare(highestRatio, -1) != 0) {
			return insertInfo;
		}
		return null;
	}

	public int getShift(POI POIToBeInserted, POIInterval POIIntervalAfterInsertPosition) {
		POIInterval POIIntervalBeforeInsertPosition = POIIntervalAfterInsertPosition.getPreviousPOIInterval();

		int travelTimeToNewPOI = POIIntervalBeforeInsertPosition.getPOI().getTravelTimeToPOI(POIToBeInserted.getID());
		int travelTimeFromNewPOIToNext = POIToBeInserted.getTravelTimeToPOI(POIIntervalAfterInsertPosition.getPOI().getID());
		int currentTravelTime = POIIntervalBeforeInsertPosition.getTravelTime();

		int arrivalTimeForNewPOI = POIIntervalBeforeInsertPosition.getEndingTime() + travelTimeToNewPOI;
		int waitOfNewPOI = MathExtension.getMaxOfTwo(POIToBeInserted.getOpeningTime() - arrivalTimeForNewPOI, 0);

		int shiftOfNewPOI = travelTimeToNewPOI + waitOfNewPOI + POIToBeInserted.getDuration() + travelTimeFromNewPOIToNext - 
								currentTravelTime;

		return shiftOfNewPOI;
	}

	public boolean canInsertBecauseOfConstraints(int entranceFee, int typeToAssign) {
		if(this.totalMoneySpent + entranceFee > problemInput.getBudgetLimit()) {
			return false;
		}

		if(this.visitCountOfEachType[typeToAssign] + 1 > problemInput.getMaxAllowedVisitsForType(typeToAssign)) {
			return false;
		}

		return true;
	}

	public boolean canInsertBeforeThisPOI(POI POIToBeInserted, POIInterval POIIntervalAfterInsertPosition, int shiftForNewPOI) {
		if(shiftForNewPOI > POIIntervalAfterInsertPosition.getWaitTime() + POIIntervalAfterInsertPosition.getMaxShift()) {
			return false;
		}
		
		POIInterval POIIntervalBeforeInsertPosition = POIIntervalAfterInsertPosition.getPreviousPOIInterval();
		int arrivalTime = calculateArrivalTime(POIIntervalBeforeInsertPosition, POIToBeInserted.getID());

		int startingTime = MathExtension.getMaxOfTwo(arrivalTime, POIToBeInserted.getOpeningTime());
		if(startingTime > POIToBeInserted.getClosingTime()) {
			return false;
		}

		return true;
	}

	public float calculateDenominator(int shiftForNewPOI, int entranceFee, int type, int tour) {
		int availableBudget = problemInput.getBudgetLimit() - this.totalMoneySpent;
		int visitsOfTypeLeft = problemInput.getMaxAllowedVisitsForType(type) - this.visitCountOfEachType[type];

		float firstComponent = shiftForNewPOI / (float)availableTime[tour];
		float secondComponent = entranceFee / (float)availableBudget;
		float thirdComponent = 1 / (float)visitsOfTypeLeft;
		
		return firstComponent + 0.5f * secondComponent + 0.5f * thirdComponent;
	}

	public POIInterval insertPOI(POI POIToBeInserted, POIInterval POIIntervalAfterInsertPosition, int assignedTypeOfBestPOI) {
		POIToBeInserted.setAssigned(true);
		
		POIInterval POIIntervalBeforeInsertPosition = POIIntervalAfterInsertPosition.getPreviousPOIInterval();

		POIInterval newPOIInterval = new POIInterval(POIToBeInserted, 0, assignedTypeOfBestPOI);

		newPOIInterval.setPreviousPOIInterval(POIIntervalBeforeInsertPosition);
		newPOIInterval.setNextPOIInterval(POIIntervalAfterInsertPosition);
		POIIntervalBeforeInsertPosition.setNextPOIInterval(newPOIInterval);
		POIIntervalAfterInsertPosition.setPreviousPOIInterval(newPOIInterval);

		return newPOIInterval;
	}

	public void calculateArriveStartAndWaitForNewPOI(POIInterval newPOIInterval) {
		POIInterval previousPOIInterval = newPOIInterval.getPreviousPOIInterval();
		int arrivalTime = calculateArrivalTime(previousPOIInterval, newPOIInterval.getPOI().getID());
		newPOIInterval.setArrivalTime(arrivalTime);
		
		int startingTime = MathExtension.getMaxOfTwo(arrivalTime, newPOIInterval.getPOI().getOpeningTime());
		
		newPOIInterval.shiftStartingAndEndingTime(startingTime);
		newPOIInterval.updateWaitTime();
	}

	public void updateParametersForFollowingVisitsAfterInsert(POIInterval POIIntervalFollowingInsert, int shiftForNewPOI) {
		POIInterval currentPOIInterval = POIIntervalFollowingInsert;
		int lastShift = shiftForNewPOI;
		while(true) {
			int updateParameter = currentPOIInterval.getWaitTime() - lastShift;			
			currentPOIInterval.setWaitTime(MathExtension.getMaxOfTwo(0, updateParameter));

			currentPOIInterval.setArrivalTime(currentPOIInterval.getArrivalTime() + lastShift);

			int currentShift = MathExtension.getMaxOfTwo(0, updateParameter * -1);
			if(currentShift == 0) {
				break;
			}

			currentPOIInterval.shiftStartingAndEndingTime(currentShift);
			currentPOIInterval.updateMaxShift(-currentShift);

			currentPOIInterval = currentPOIInterval.getNextPOIInterval();
			lastShift = currentShift;
		}
	}

	public void updateMaxShiftForPreviousVisitsAndThis(POIInterval thisPOIInterval) {
		POIInterval currentPOIInterval = thisPOIInterval;
		while(currentPOIInterval.getPOI().getDuration() > 0) {
			currentPOIInterval.updateMaxShift();
			currentPOIInterval = currentPOIInterval.getPreviousPOIInterval();
		}
	}

	public void shakeStep(int startRemoveAt, int removeNConsecutiveVisits) {
		this.stuckInLocalOptimum = false;
		this.updateSizeOfSmallestTour();

		for(int tour = 0; tour < problemInput.getTourCount(); tour++) {
			int currentDayRemovals = 0;

			POIInterval currentPOIInterval = getNthPOIIntervalInTourX(startRemoveAt % tourSizes[tour], tour);
			POIInterval POIIntervalAfterRemovingEnds = null;

			while(currentDayRemovals < removeNConsecutiveVisits && this.tourSizes[tour] != 0) {
				this.score -= currentPOIInterval.getPOI().getScore();
				this.totalMoneySpent -= currentPOIInterval.getPOI().getEntranceFee();
				this.visitCountOfEachType[currentPOIInterval.getAssignedType()] -= 1;
				this.availableTime[tour] += currentPOIInterval.getPOI().getDuration() + 
										currentPOIInterval.getPOI().getTravelTimeToPOI(currentPOIInterval.getPreviousPOIInterval().getPOI().getID()) + 
										currentPOIInterval.getPOI().getTravelTimeToPOI(currentPOIInterval.getNextPOIInterval().getPOI().getID()) -
										currentPOIInterval.getPreviousPOIInterval().getPOI().getTravelTimeToPOI(currentPOIInterval.getNextPOIInterval().getPOI().getID());
				currentPOIInterval = removePOIInterval(currentPOIInterval);
				if(currentPOIInterval == endingPOIIntervals[tour] && currentDayRemovals != removeNConsecutiveVisits - 1) {
					currentPOIInterval = startingPOIIntervals[tour].getNextPOIInterval();
				}
				this.tourSizes[tour] -= 1;				
				currentDayRemovals++;
			}
			
			POIIntervalAfterRemovingEnds = currentPOIInterval;
			
			updateParametersForFollowingVisitsAfterRemoval(POIIntervalAfterRemovingEnds, endingPOIIntervals[tour]);
			updateMaxShiftForPreviousVisitsAndThis(endingPOIIntervals[tour].getPreviousPOIInterval());
		}
	}

	private void updateSizeOfSmallestTour() {
		int sizeOfSmallestTour = tourSizes[0];
		for(int tour = 1; tour < tourSizes.length; tour++) {
			if(tourSizes[tour] < sizeOfSmallestTour) {
				sizeOfSmallestTour = tourSizes[tour];
			}
		}
		this.sizeOfSmallestTour = sizeOfSmallestTour;
	}

	public POIInterval getNthPOIIntervalInTourX(int N, int tour) {
		int currentPOIPosition = 0;
		POIInterval currentPOIInterval = startingPOIIntervals[tour].getNextPOIInterval();
		while(currentPOIPosition < N) {
			currentPOIInterval = currentPOIInterval.getNextPOIInterval();
			if(currentPOIInterval == endingPOIIntervals[tour]) {
				currentPOIInterval = startingPOIIntervals[tour].getNextPOIInterval();
			}
			currentPOIPosition++;
		}
		return currentPOIInterval;
	}

	public POIInterval removePOIInterval(POIInterval currentPOIInterval) {
		currentPOIInterval.getPOI().setAssigned(false);

		POIInterval POIIntervalBeforeRemovePosition = currentPOIInterval.getPreviousPOIInterval();
		POIInterval POIIntervalAfterRemovePosition = currentPOIInterval.getNextPOIInterval();
		
		POIIntervalBeforeRemovePosition.setNextPOIInterval(POIIntervalAfterRemovePosition);
		POIIntervalAfterRemovePosition.setPreviousPOIInterval(POIIntervalBeforeRemovePosition);

		return POIIntervalAfterRemovePosition;
	}

	public void updateParametersForFollowingVisitsAfterRemoval(POIInterval POIIntervalFollowingLastRemove, 
																POIInterval endingPOIInterval) {
		POIInterval currentPOIInterval = POIIntervalFollowingLastRemove;
		while(currentPOIInterval != endingPOIInterval) {
			int arrivalTime = calculateArrivalTime(currentPOIInterval.getPreviousPOIInterval(), currentPOIInterval.getPOI().getID());
			currentPOIInterval.setArrivalTime(arrivalTime);

			int newStartingTime = MathExtension.getMaxOfTwo(arrivalTime, currentPOIInterval.getPOI().getOpeningTime());
			int shiftTime = currentPOIInterval.getStartingTime() - newStartingTime;
			currentPOIInterval.shiftStartingAndEndingTime(-shiftTime);

			currentPOIInterval.updateWaitTime();
			currentPOIInterval = currentPOIInterval.getNextPOIInterval();
		}
		int arrivalTime = calculateArrivalTime(endingPOIInterval.getPreviousPOIInterval(), endingPOIInterval.getPOI().getID());
		endingPOIInterval.setArrivalTime(arrivalTime);
		endingPOIInterval.updateWaitTime();
	}

	public int sizeOfSmallestTour() {
		return this.sizeOfSmallestTour;
	}

	public static int calculateArrivalTime(POIInterval previousPOIInterval, int currentPOIID) {
		return previousPOIInterval.getEndingTime() + previousPOIInterval.getPOI().getTravelTimeToPOI(currentPOIID);
	}

	// delete in the end
	public boolean isValid() {
		// opening and closing
		int score = 0;
		int totalMoneySpent = 0;
		for(int tour = 0; tour < problemInput.getTourCount(); tour++) {
			POIInterval currentPOIInterval = startingPOIIntervals[tour];
			while(currentPOIInterval != endingPOIIntervals[tour]) {
				if(currentPOIInterval.getStartingTime() < currentPOIInterval.getPOI().getOpeningTime()) {
					return false;
				}
				if(currentPOIInterval.getStartingTime() > currentPOIInterval.getPOI().getClosingTime()) {
					return false;
				}
				score += currentPOIInterval.getPOI().getScore();
				totalMoneySpent += currentPOIInterval.getPOI().getEntranceFee();
				currentPOIInterval = currentPOIInterval.getNextPOIInterval();
			}
		}
		// score
		if(score != this.getScore()) {
			return false;
		}
		// budget
		if(totalMoneySpent != this.totalMoneySpent || totalMoneySpent > problemInput.getBudgetLimit()) {
			return false;
		}
		// travel
		for(int tour = 0; tour < problemInput.getTourCount(); tour++) {
			POIInterval currentPOIInterval = startingPOIIntervals[tour].getNextPOIInterval();
			while(currentPOIInterval != null) {
				int currentTravelTime = currentPOIInterval.getPreviousPOIInterval().getPOI().getTravelTimeToPOI(currentPOIInterval.getPOI().getID());
				if(currentPOIInterval.getArrivalTime() - currentPOIInterval.getPreviousPOIInterval().getEndingTime() != currentTravelTime) {
					return false;
				}
				currentPOIInterval = currentPOIInterval.getNextPOIInterval();
			}
		}
		// maxshift
		int[] visitCountOfEachType = new int[this.visitCountOfEachType.length];
		for(int tour = 0; tour < problemInput.getTourCount(); tour++) {
			POIInterval currentPOIInterval = endingPOIIntervals[tour].getPreviousPOIInterval();
			while(currentPOIInterval != startingPOIIntervals[tour]) {
				int currentMaxShift1 = currentPOIInterval.getPOI().getClosingTime() - currentPOIInterval.getStartingTime();
				int currentMaxShift2 = currentPOIInterval.getNextPOIInterval().getWaitTime() + currentPOIInterval.getNextPOIInterval().getMaxShift();
				int currentMaxShift = MathExtension.getMinOfTwo(currentMaxShift1, currentMaxShift2);
				if(currentMaxShift != currentPOIInterval.getMaxShift()) {
					return false;
				}
				visitCountOfEachType[currentPOIInterval.getAssignedType()] += 1;
				currentPOIInterval = currentPOIInterval.getPreviousPOIInterval();
			}
		}
		// max allowed visit of type
		for(int type = 0; type < visitCountOfEachType.length; type++) {
			if(visitCountOfEachType[type] > this.visitCountOfEachType[type]) {
				return false;
			}
		}
		// wait
		for(int tour = 0; tour < problemInput.getTourCount(); tour++) {
			POIInterval currentPOIInterval = startingPOIIntervals[tour].getNextPOIInterval();
			while(currentPOIInterval != null) {
				int currentWaitTime = currentPOIInterval.getStartingTime() - currentPOIInterval.getArrivalTime();
				if(currentWaitTime != currentPOIInterval.getWaitTime()) {
					return false;
				}
				currentPOIInterval = currentPOIInterval.getNextPOIInterval();
			}
		}
		return true;
	}

	@Override
	public String toString() {
		String result = "";
		int visits = 0;
		for(int tour = 0; tour < problemInput.getTourCount(); tour++) {
			result += "\t\t\t\t\t\t\tArrT\tMaxSh\tMaxSh\tMaxSh1\tMaxSh2\tNxtMxSh\tWait\n";
			String resultPart1 = "";
			String resultPart2 = "";
			POIInterval currentPOIInterval = this.startingPOIIntervals[tour];
			while(currentPOIInterval != null) {	
				visits++;
				resultPart1 += currentPOIInterval.getPOI().getID();
				if(currentPOIInterval.getNextPOIInterval() != null) {
					resultPart1 += " -> ";
				}
				if(currentPOIInterval.getArrivalTime() != 0) {
					resultPart2 += "----->" + String.format("%7s", (currentPOIInterval.getArrivalTime() / 100.0f));
					if(currentPOIInterval.getWaitTime() != 0) {
						resultPart2 += ".....";
					}
					if(currentPOIInterval.getPreviousPOIInterval().getArrivalTime() != 0) {
						int expectedArrivalTime = currentPOIInterval.getPreviousPOIInterval().getEndingTime() + currentPOIInterval.getPreviousPOIInterval().getPOI().getTravelTimeToPOI(currentPOIInterval.getPOI().getID());
						String resultComponent1 = expectedArrivalTime == currentPOIInterval.getArrivalTime()? "true": "FALSEE!";
						int lastMaxShift = currentPOIInterval.getPreviousPOIInterval().getMaxShift();
						int lastMaxShift1 = currentPOIInterval.getPreviousPOIInterval().getPOI().getClosingTime() - currentPOIInterval.getPreviousPOIInterval().getStartingTime();
						int lastMaxShift2 = currentPOIInterval.getWaitTime() + currentPOIInterval.getMaxShift();
						String resultComponent2 = (lastMaxShift == lastMaxShift1 || lastMaxShift == lastMaxShift2)? "true": "FALSEE!";
						int maxShift = currentPOIInterval.getMaxShift();
						int wait = currentPOIInterval.getWaitTime();
						resultPart2 += "\t\t" + resultComponent1 + "\t" +
									resultComponent2 + "\t" + (lastMaxShift / 100.0f) + "\t" + (lastMaxShift1 / 100.0f) + "\t" 
									+ (lastMaxShift2 / 100.0f) + "\t" + (maxShift / 100.0f) + "\t" + (wait / 100.0f);
					}
				}
				resultPart2 += "\r\n|" + 
								String.format("%7s", (currentPOIInterval.getStartingTime() / 100.0f)) + 
								"____" + String.format("%3s", currentPOIInterval.getPOI().getID()) + 
								"____" + String.format("%7s", (currentPOIInterval.getEndingTime() / 100.0f)) + "|";
	
				currentPOIInterval = currentPOIInterval.getNextPOIInterval();
			}
			result += resultPart1 + resultPart2;
			result += "\r\n\r\n";
		}
		result += "Score: " + (this.score / 100.0f) + "; Visits: " + (visits - 2 * problemInput.getTourCount()) + "\r\n";
		result += "Money spent: " + (this.totalMoneySpent / 100.0f) + "; Visits of each type: ";
		for(int type = 0; type < this.visitCountOfEachType.length; type++) {
			result += this.visitCountOfEachType[type] + " ";
		}
		result += "\r\n================================================================================";
		return result;
	}

	public void setStartingPOIInterval(POIInterval startingPOIInterval[]) {
		this.startingPOIIntervals = startingPOIInterval;
	}

	public void setEndingPOIInterval(POIInterval endingPOIInterval[]) {
		this.endingPOIIntervals = endingPOIInterval;
	}

	public int[] getTourSizes() {
		return this.tourSizes;
	}

	@Override
	public Object clone() {
		Solution clonedSolution = null;
		try {
			clonedSolution = (Solution)super.clone();
		}
		catch(CloneNotSupportedException ex) {
			System.out.println("Could not clone Solution. " + ex.getMessage());
			return null;
		}

		POIInterval[] clonedStartingPOIIntervals = new POIInterval[this.startingPOIIntervals.length];
		POIInterval[] clonedEndingPOIIntervals = new POIInterval[this.endingPOIIntervals.length];

		for(int tour = 0; tour < this.problemInput.getTourCount(); tour++) {
			try {
				clonedStartingPOIIntervals[tour] = (POIInterval)this.startingPOIIntervals[tour].clone();
				clonedEndingPOIIntervals[tour] = (POIInterval)this.endingPOIIntervals[tour].clone();
			}
			catch(CloneNotSupportedException ex) {
				System.out.println("Could not clone POIInterval's. " + ex.getMessage());
				return null;
			}
		}

		clonedSolution.setStartingPOIInterval(clonedStartingPOIIntervals);
		clonedSolution.setEndingPOIInterval(clonedEndingPOIIntervals);

		clonedSolution.tourSizes = new int[tourSizes.length];
		for(int tour = 0; tour < tourSizes.length; tour++) {
			clonedSolution.tourSizes[tour] = tourSizes[tour];
		}

		clonedSolution.visitCountOfEachType = new int[this.visitCountOfEachType.length];
		for(int type = 0; type < this.visitCountOfEachType.length; type++) {
			clonedSolution.visitCountOfEachType[type] = this.visitCountOfEachType[type];
		}

		return clonedSolution;
	}
}
