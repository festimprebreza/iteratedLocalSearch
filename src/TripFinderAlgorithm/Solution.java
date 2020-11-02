package TripFinderAlgorithm;

import java.util.ArrayList;
import java.util.PriorityQueue;
import java.util.Random;

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
		tourSizes = new int[problemInput.getTourCount()];
		visitCountOfEachType = new int[problemInput.getMaxAllowedVisitsForEachType().length];
		totalMoneySpent = 0;

		for(int tour = 0; tour < problemInput.getTourCount(); tour++) {
			this.startingPOIIntervals[tour] = new POIInterval(startingPOI, startingPOI.getOpeningTime(), -1);
			this.endingPOIIntervals[tour] = new POIInterval(endingPOI, endingPOI.getClosingTime(), -1);
	
			this.startingPOIIntervals[tour].setNextPOIInterval(this.endingPOIIntervals[tour]);
			this.endingPOIIntervals[tour].setPreviousPOIInterval(this.startingPOIIntervals[tour]);

			this.endingPOIIntervals[tour].setArrivalTime(this.startingPOIIntervals[tour].getPOI().getTravelTimeToPOI(
																			this.endingPOIIntervals[tour].getPOI().getID()));
			this.endingPOIIntervals[tour].updateWaitTime();
			this.availableTime[tour] = this.endingPOIIntervals[tour].getWaitTime();
		}	
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

	public boolean insertPivots(int tour, int typeCount) {
		int nextTour = tour;
		int nextTypeCount = typeCount + 1;
		if(tour == problemInput.getTourCount()) {
			return true;
		}
		if(typeCount == problemInput.getPatternsForTour(tour).length - 1) {
			nextTour += 1;
			nextTypeCount = 0;
		}
		int currentType = problemInput.getPatternsForTour(tour)[typeCount];
		PriorityQueue<PivotInsertData> sortedPossibleInserts = getSortedListOfPossibleInserts(
																	problemInput.getPOIsForPatternType(currentType), 
																	currentType, tour);
		Random rand = new Random();
		int probabilityFactor = 7;
		while(true) {
			ArrayList<PivotInsertData> previouslyPivotPOIsNotInsertedInFirstIteration = new ArrayList<>();
			while(!sortedPossibleInserts.isEmpty()) {
				PivotInsertData currentBestPivotInsertData = sortedPossibleInserts.poll();

				if(currentBestPivotInsertData.containedPOI.isAssigned()) {
					continue;
				}
				if(currentBestPivotInsertData.containedPOI.hasAlreadyBeenUsedAsAPivotForType(currentType)) {
					if(rand.nextInt(10) < probabilityFactor) {
						previouslyPivotPOIsNotInsertedInFirstIteration.add(currentBestPivotInsertData);
						continue;
					}
				}

				int shiftOfBestPOI = getShift(currentBestPivotInsertData.containedPOI, endingPOIIntervals[tour]);
				POIInterval newPOIInterval = insertAndUpdateData(new POIInsertData(currentBestPivotInsertData, 
														endingPOIIntervals[tour], shiftOfBestPOI, tour, currentType));
				newPOIInterval.setIsPivot(true);
				newPOIInterval.getPOI().setUsedAsPivotForType(currentType);

				if(insertPivots(nextTour, nextTypeCount)) {
					return true;
				}

				updateSolutionParametersBeforeRemoval(newPOIInterval, tour);
				newPOIInterval.setIsPivot(false);
				removePOIInterval(newPOIInterval);
				updateParametersForFollowingVisitsAfterRemoval(startingPOIIntervals[tour].getNextPOIInterval(), endingPOIIntervals[tour]);
				updateMaxShiftForPreviousVisitsAndThis(endingPOIIntervals[tour].getPreviousPOIInterval());
			}
			if(previouslyPivotPOIsNotInsertedInFirstIteration.size() != 0) {
				for(PivotInsertData currentPivotInsertData: previouslyPivotPOIsNotInsertedInFirstIteration) {
					sortedPossibleInserts.add(currentPivotInsertData);
				}
				probabilityFactor = 0;
			}
			else {
				break;
			}
		}
		return false;
	}

	public PriorityQueue<PivotInsertData> getSortedListOfPossibleInserts(ArrayList<POI> POIsOfCurrentType, int type, int tour) {
		PriorityQueue<PivotInsertData> myPriorityQueue = new PriorityQueue<>();
		for(POI currentPOI: POIsOfCurrentType) {
			if(exceedsBudgetConstraint(currentPOI.getEntranceFee())) {
				continue;
			}

			if(exceedsTypeConstraint(type)) {
				continue;
			}

			int shiftForNewPOI = getShift(currentPOI, endingPOIIntervals[tour]);
			if(!canInsertBeforeThisPOI(currentPOI, endingPOIIntervals[tour], shiftForNewPOI)) {
				continue;
			}

			int waitTime = getWaitTimeIfAssigned(currentPOI, endingPOIIntervals[tour].getPreviousPOIInterval());
			float denominatorForNewPOI = calculateDenominator(calculateDenominatorComponent(shiftForNewPOI - waitTime, currentPOI.getEntranceFee(), tour), type);
			float ratio = (float)Math.pow(currentPOI.getScore() / 100.0f, 2) / denominatorForNewPOI;
			myPriorityQueue.add(new PivotInsertData(ratio, currentPOI));
		}
		return myPriorityQueue;
	}

	public int getWaitTimeIfAssigned(POI POIToBeInserted, POIInterval POIIntervalBeforeInsertPosition) {
		int arrivalTime = POIIntervalBeforeInsertPosition.getPOI().getTravelTimeToPOI(POIToBeInserted.getID()) + 
							POIIntervalBeforeInsertPosition.getEndingTime();
		return MathExtension.getMaxOfTwo(POIToBeInserted.getOpeningTime() - arrivalTime, 0);
	}

	public POIInterval insertAndUpdateData(POIInsertData bestPOIIInsertData) {
		POIInterval newPOIInterval = insertPOI(bestPOIIInsertData.containedPOI, bestPOIIInsertData.POIIntervalAfterInsertPosition, 
												bestPOIIInsertData.assignedType);
		calculateArriveStartAndWaitForNewPOI(newPOIInterval);
		updateParametersForFollowingVisitsAfterInsert(newPOIInterval.getNextPOIInterval(), bestPOIIInsertData.shiftOfPOI);
		updateMaxShiftForPreviousVisitsAndThis(newPOIInterval);		
		updateSolutionParametersAfterInsert(newPOIInterval, bestPOIIInsertData.tour, bestPOIIInsertData.shiftOfPOI);

		return newPOIInterval;
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

	public static int calculateArrivalTime(POIInterval previousPOIInterval, int currentPOIID) {
		return previousPOIInterval.getEndingTime() + previousPOIInterval.getPOI().getTravelTimeToPOI(currentPOIID);
	}

	public void updateSolutionParametersAfterInsert(POIInterval POIIntervalInserted, int tour, int shiftOfPOI) {
		this.score += POIIntervalInserted.getPOI().getScore();
		this.totalMoneySpent += POIIntervalInserted.getPOI().getEntranceFee();
		this.visitCountOfEachType[POIIntervalInserted.getAssignedType()] += 1;
		this.availableTime[tour] -= (shiftOfPOI - POIIntervalInserted.getWaitTime());
		this.tourSizes[tour] += 1;
		this.stuckInLocalOptimum = false;
	}

	public void updateSolutionParametersBeforeRemoval(POIInterval POIIntervalToBeRemoved, int tour) {
		this.score -= POIIntervalToBeRemoved.getPOI().getScore();
		this.totalMoneySpent -= POIIntervalToBeRemoved.getPOI().getEntranceFee();
		this.visitCountOfEachType[POIIntervalToBeRemoved.getAssignedType()] -= 1;
		this.availableTime[tour] += POIIntervalToBeRemoved.getPOI().getDuration() + 
						POIIntervalToBeRemoved.getPOI().getTravelTimeToPOI(POIIntervalToBeRemoved.getPreviousPOIInterval().getPOI().getID()) + 
						POIIntervalToBeRemoved.getPOI().getTravelTimeToPOI(POIIntervalToBeRemoved.getNextPOIInterval().getPOI().getID()) -
						POIIntervalToBeRemoved.getPreviousPOIInterval().getPOI().getTravelTimeToPOI(POIIntervalToBeRemoved.getNextPOIInterval().getPOI().getID());
		this.tourSizes[tour] -= 1;
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

	public void updateMaxShiftForPreviousVisitsAndThis(POIInterval thisPOIInterval) {
		POIInterval currentPOIInterval = thisPOIInterval;
		while(currentPOIInterval.getPOI().getDuration() > 0) {
			currentPOIInterval.updateMaxShift();
			currentPOIInterval = currentPOIInterval.getPreviousPOIInterval();
		}
	}

	public void changePivots() {
		removeAllPOIs();
		insertPivots(0, 0);
	}

	public void removeAllPOIs() {
		for(int tour = 0; tour < problemInput.getTourCount(); tour++) {
			POIInterval currentPOIInterval = startingPOIIntervals[tour].getNextPOIInterval();
			while(currentPOIInterval != endingPOIIntervals[tour]) {
				this.visitCountOfEachType[currentPOIInterval.getAssignedType()] -= 1;
				if(currentPOIInterval.isPivot()) {
					currentPOIInterval.setIsPivot(false);
				}
				currentPOIInterval = removePOIInterval(currentPOIInterval);
			}
			this.tourSizes[tour] = 0;
			this.score = 0;
			this.totalMoneySpent = 0;
			updateParametersForFollowingVisitsAfterRemoval(startingPOIIntervals[tour].getNextPOIInterval(), endingPOIIntervals[tour]);
			this.availableTime[tour] = endingPOIIntervals[tour].getWaitTime();
		}
	}

	public void insertStep() {
		POIInsertData bestPOIIInsertData = getInfoForBestPOIToInsert();
		if(bestPOIIInsertData != null) {
			insertAndUpdateData(bestPOIIInsertData);
			return;
		}

		this.stuckInLocalOptimum = true;
	}

	public POIInsertData getInfoForBestPOIToInsert() {
		float highestRatio = -1;
		POIInsertData bestPOIInsertData = new POIInsertData();

		for(POI currentPOI: problemInput.getVisitablePOIs()) {
			if(currentPOI.isAssigned()) {
				continue;
			}

			if(exceedsBudgetConstraint(currentPOI.getEntranceFee())) {
				continue;
			}

			POIInterval POIIntervalAfterBestInsertPositionForThisPOI = null;
			int shiftOnBestInsertForNewPOI = Integer.MAX_VALUE;
			int assignedType = -1;
			int tourToInsertIn = -1;
			float bestDenominatorForNewPOI = Float.MAX_VALUE;

			for(int tour = 0; tour < problemInput.getTourCount(); tour++) {
				POIInterval POIIntervalAfterInsertPosition = startingPOIIntervals[tour].getNextPOIInterval();				

				while(POIIntervalAfterInsertPosition != null &&	POIStillOpen(currentPOI, POIIntervalAfterInsertPosition.getPreviousPOIInterval())) {
					int shiftForNewPOI = getShift(currentPOI, POIIntervalAfterInsertPosition);

					if(!canInsertBeforeThisPOI(currentPOI, POIIntervalAfterInsertPosition, shiftForNewPOI)) {
						POIIntervalAfterInsertPosition = POIIntervalAfterInsertPosition.getNextPOIInterval();
						continue;
					}

					float denominatorComponent = calculateDenominatorComponent(shiftForNewPOI, currentPOI.getEntranceFee(), tour);

					for(int type: currentPOI.getTypes()) {
						if(exceedsTypeConstraint(type)) {
							continue;
						}

						float denominatorForNewPOI = calculateDenominator(denominatorComponent, type);
						if(Float.compare(denominatorForNewPOI, bestDenominatorForNewPOI) < 0) {
							bestDenominatorForNewPOI = denominatorForNewPOI;
							shiftOnBestInsertForNewPOI = shiftForNewPOI;
							POIIntervalAfterBestInsertPositionForThisPOI = POIIntervalAfterInsertPosition;
							assignedType = type;
							tourToInsertIn = tour;
						}
					}

					POIIntervalAfterInsertPosition = POIIntervalAfterInsertPosition.getNextPOIInterval();
				}
			}

			float highestRatioForThisPOI = -1;
			if(shiftOnBestInsertForNewPOI != Integer.MAX_VALUE) {
				highestRatioForThisPOI = (float)Math.pow(currentPOI.getScore() / 100.0f, 2) / bestDenominatorForNewPOI;
			}

			if(Float.compare(highestRatioForThisPOI, highestRatio) > 0) {
				highestRatio = highestRatioForThisPOI;
				bestPOIInsertData.ratio = highestRatio;
				bestPOIInsertData.containedPOI = currentPOI;
				bestPOIInsertData.POIIntervalAfterInsertPosition = POIIntervalAfterBestInsertPositionForThisPOI;
				bestPOIInsertData.tour = tourToInsertIn;
				bestPOIInsertData.shiftOfPOI = shiftOnBestInsertForNewPOI;
				bestPOIInsertData.assignedType = assignedType;
			}
		}

		if(Float.compare(highestRatio, -1) != 0) {
			return bestPOIInsertData;
		}
		return null;
	}
	
	public boolean POIStillOpen(POI POIToBeInserted, POIInterval POIIntervalBeforeInsertPosition) {
		return POIToBeInserted.getClosingTime() > POIIntervalBeforeInsertPosition.getEndingTime();
	}

	public boolean exceedsBudgetConstraint(int entranceFee) {
		if(this.totalMoneySpent + entranceFee > problemInput.getBudgetLimit()) {
			return true;
		}

		return false;
	}

	public boolean exceedsTypeConstraint(int typeToAssign) {
		if(this.visitCountOfEachType[typeToAssign] + 1 > problemInput.getMaxAllowedVisitsForType(typeToAssign)) {
			return true;
		}

		return false;
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

	public float calculateDenominatorComponent(int shiftForNewPOI, int entranceFee, int tour) {
		int availableBudget = problemInput.getBudgetLimit() - this.totalMoneySpent;

		float firstComponent = shiftForNewPOI / (float)availableTime[tour];
		float secondComponent = entranceFee / (float)availableBudget;
		
		return firstComponent + (1/11.0f) * secondComponent;
	}

	public float calculateDenominator(float denominatorComponent, int type) {
		int visitsOfTypeLeft = problemInput.getMaxAllowedVisitsForType(type) - this.visitCountOfEachType[type];
		float thirdComponent = 1 / (float)visitsOfTypeLeft;
		
		return denominatorComponent + (1/11.0f) * thirdComponent;
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

	public void shakeStep(int startRemoveAt, int removeNConsecutiveVisits, int tabuIterations, int currentIteration) {
		this.stuckInLocalOptimum = false;
		this.updateSizeOfSmallestTour();

		for(int tour = 0; tour < problemInput.getTourCount(); tour++) {
			int currentDayRemovals = 0;

			POIInterval currentPOIInterval = getNthPOIIntervalInTourX(startRemoveAt % tourSizes[tour], tour);

			while(currentDayRemovals < removeNConsecutiveVisits && this.tourSizes[tour] > problemInput.getPatternsForTour(tour).length) {
				int iterationsWithoutFindingRemovablePOI = 0;
				POIInterval lastNonPivotPOIInterval = null;
				while(!canRemovePOIInterval(currentPOIInterval, tabuIterations, tour, currentIteration)) {
					if(!currentPOIInterval.isPivot()) {
						lastNonPivotPOIInterval = currentPOIInterval;
					}
					if(iterationsWithoutFindingRemovablePOI == this.tourSizes[tour] - 1) {
						break;
					}
					currentPOIInterval = currentPOIInterval.getNextPOIInterval();
					if(currentPOIInterval == endingPOIIntervals[tour]) {
						currentPOIInterval = startingPOIIntervals[tour].getNextPOIInterval();
					}
					iterationsWithoutFindingRemovablePOI++;
				}

				if(lastNonPivotPOIInterval != null) {
					currentPOIInterval = lastNonPivotPOIInterval;
				}
				updateSolutionParametersBeforeRemoval(currentPOIInterval, tour);
				currentPOIInterval.getPOI().updateLastRemovedIteration(currentIteration, tour);
				currentPOIInterval = removePOIInterval(currentPOIInterval);
				if(currentPOIInterval == endingPOIIntervals[tour] && currentDayRemovals != removeNConsecutiveVisits - 1) {
					currentPOIInterval = startingPOIIntervals[tour].getNextPOIInterval();
				}
				currentDayRemovals++;
			}
			
			updateParametersForFollowingVisitsAfterRemoval(startingPOIIntervals[tour].getNextPOIInterval(), endingPOIIntervals[tour]);
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

	public boolean canRemovePOIInterval(POIInterval POIIntervalToBeRemoved, int tabuIterations, int tour, int currentIteration) {
		if(currentIteration - POIIntervalToBeRemoved.getPOI().getLastRemovedIteration(tour) > tabuIterations &&
			!POIIntervalToBeRemoved.isPivot()) {
			return true;
		}
		return false;
	}

	public int sizeOfSmallestTour() {
		return this.sizeOfSmallestTour;
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
					System.out.println("Opening time violated!");
					return false;
				}
				if(currentPOIInterval.getStartingTime() > currentPOIInterval.getPOI().getClosingTime()) {
					System.out.println("Closing time violated!");
					return false;
				}
				score += currentPOIInterval.getPOI().getScore();
				totalMoneySpent += currentPOIInterval.getPOI().getEntranceFee();
				currentPOIInterval = currentPOIInterval.getNextPOIInterval();
			}
		}
		// score
		if(score != this.getScore()) {
			System.out.println("Score does not match!");
			return false;
		}
		// budget
		if(totalMoneySpent != this.totalMoneySpent || totalMoneySpent > problemInput.getBudgetLimit()) {
			System.out.println("Budget exceeded!");
			return false;
		}
		// travel
		int[] availableTimes = new int[problemInput.getTourCount()];
		for(int tour = 0; tour < problemInput.getTourCount(); tour++) {
			availableTimes[tour]  = 0;
		}
		for(int tour = 0; tour < problemInput.getTourCount(); tour++) {
			POIInterval currentPOIInterval = startingPOIIntervals[tour].getNextPOIInterval();
			while(currentPOIInterval != null) {
				availableTimes[tour] += currentPOIInterval.getWaitTime();
				int currentTravelTime = currentPOIInterval.getPreviousPOIInterval().getPOI().getTravelTimeToPOI(currentPOIInterval.getPOI().getID());
				if(currentPOIInterval.getArrivalTime() - currentPOIInterval.getPreviousPOIInterval().getEndingTime() != currentTravelTime) {
					System.out.println("Arrival time does not match!");
					return false;
				}
				currentPOIInterval = currentPOIInterval.getNextPOIInterval();
			}
		}
		// available time
		for(int tour = 0; tour < problemInput.getTourCount(); tour++) {
			if(availableTimes[tour] != this.availableTime[tour]) {
				return false;
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
					System.out.println("Max shift does not match!");
					return false;
				}
				visitCountOfEachType[currentPOIInterval.getAssignedType()] += 1;
				currentPOIInterval = currentPOIInterval.getPreviousPOIInterval();
			}
		}
		// max allowed visit of type
		for(int type = 0; type < visitCountOfEachType.length; type++) {
			if(visitCountOfEachType[type] > this.visitCountOfEachType[type]) {
				System.out.println("Max allowed visits of a type exceeded!");
				return false;
			}
		}
		// wait
		for(int tour = 0; tour < problemInput.getTourCount(); tour++) {
			POIInterval currentPOIInterval = startingPOIIntervals[tour].getNextPOIInterval();
			while(currentPOIInterval != null) {
				int currentWaitTime = currentPOIInterval.getStartingTime() - currentPOIInterval.getArrivalTime();
				if(currentWaitTime != currentPOIInterval.getWaitTime()) {
					System.out.println("Wait time violated!");
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
						resultPart2 += "....";
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
						if(currentPOIInterval.getPreviousPOIInterval().isPivot()) {
							resultPart2 += "\tPIVOT";
						}
					}
				}
				resultPart2 += "\r\n|" + 
								String.format("%7s", (currentPOIInterval.getStartingTime() / 100.0f)) + 
								"____" + String.format("%3s", currentPOIInterval.getPOI().getID()) + 
								"*" + String.format("%2s", currentPOIInterval.getAssignedType()) + 
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

	public void printTour(int tour) {
		POIInterval currentPOIInterval = startingPOIIntervals[tour];
		while(currentPOIInterval != endingPOIIntervals[tour]) {
			System.out.printf("%d -> ", currentPOIInterval.getPOI().getID());
			currentPOIInterval = currentPOIInterval.getNextPOIInterval();
		}
		System.out.println("0");
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

		clonedSolution.startingPOIIntervals = clonedStartingPOIIntervals;
		clonedSolution.endingPOIIntervals = clonedEndingPOIIntervals;

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

	private class PivotInsertData implements Comparable<PivotInsertData> {
		protected float ratio;
		protected POI containedPOI;

		public PivotInsertData(float ratio, POI containtedPOI) {
			this.ratio = ratio;
			this.containedPOI = containtedPOI;
		}

		@Override
		public int compareTo(PivotInsertData otherPOIData) {
			if(Float.compare(this.ratio, otherPOIData.ratio) > 0) {
				return -1;
			}
			else if(Float.compare(this.ratio, otherPOIData.ratio) < 0) {
				return 1;
			}
			else {
				return 0;
			}
		}
	}

	private class POIInsertData extends PivotInsertData {
		private POIInterval POIIntervalAfterInsertPosition;
		private int shiftOfPOI;
		private int tour;
		private int assignedType;

		public POIInsertData() {
			super(-1, null);
			this.POIIntervalAfterInsertPosition = null;
			this.shiftOfPOI = -1;
			this.tour = -1;
			this.assignedType = -1;
		}

		public POIInsertData(PivotInsertData pivotInsertData, POIInterval POIIntervalAfterInsertPosition, int shiftOfPOI,
							int tour, int assignedType) {
			super(pivotInsertData.ratio, pivotInsertData.containedPOI);
			this.POIIntervalAfterInsertPosition = POIIntervalAfterInsertPosition;
			this.shiftOfPOI = shiftOfPOI;
			this.tour = tour;
			this.assignedType = assignedType;
		}
	}
}
