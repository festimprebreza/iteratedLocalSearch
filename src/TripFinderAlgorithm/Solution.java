package TripFinderAlgorithm;

import java.util.ArrayList;

public class Solution implements Cloneable {
	private POIInterval[] startingPOIIntervals;
	private POIInterval[] endingPOIIntervals;
	private ProblemInput problemInput;
	private int score;
	private boolean stuckInLocalOptimum;
	private int[] tourSizes;

	public Solution(ProblemInput problemInput) {
		this.problemInput = problemInput;
		POI startingPOI = problemInput.getStartingPOI();
		POI endingPOI = problemInput.getEndingPOI();
		startingPOIIntervals = new POIInterval[problemInput.getTourCount()];
		endingPOIIntervals = new POIInterval[problemInput.getTourCount()];

		for(int tour = 0; tour < problemInput.getTourCount(); tour++) {
			this.startingPOIIntervals[tour] = new POIInterval(startingPOI, startingPOI.getOpeningTime());
			this.endingPOIIntervals[tour] = new POIInterval(endingPOI, endingPOI.getClosingTime());
	
			this.startingPOIIntervals[tour].setNextPOIInterval(this.endingPOIIntervals[tour]);
			this.endingPOIIntervals[tour].setPreviousPOIInterval(this.startingPOIIntervals[tour]);

			// set arrival time for ending
			this.endingPOIIntervals[tour].setArrivalTime(this.startingPOIIntervals[tour].getPOI().getTravelTimeToPOI(
														this.endingPOIIntervals[tour].getPOI().getID()
														));
			// set wait time for ending
			this.endingPOIIntervals[tour].updateWaitTime();
		}

		tourSizes = new int[problemInput.getTourCount()];
	}

	public int getScore() {
		return this.score;
	}

	public void setScore(int score) {
		this.score = score;
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

			POIInterval newPOIInterval = insertPOI(bestPOIToBeInserted, POIIntervalAfterBestInsertPosition);
			calculateArriveStartAndWaitForNewPOI(newPOIInterval);
			updateParametersForFollowingVisitsAfterInsert(newPOIInterval.getNextPOIInterval(), shiftOfBestPOI);
			updateMaxShiftForPreviousVisitsAndThis(newPOIInterval);
			this.score += bestPOIToBeInserted.getScore();
			this.stuckInLocalOptimum = false;
			this.tourSizes[tourToInsertIn] += 1;
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
		insertInfo.add(bestPOIToBeInserted);
		insertInfo.add(POIIntervalAfterBestInsertPosition);
		insertInfo.add(tourToInsertIn);
		insertInfo.add(shiftOfBestPOI);

		for(int tour = 0; tour < problemInput.getTourCount(); tour++) {
			for(POI currentPOI: problemInput.getVisitablePOIs()) {
				if(currentPOI.isAssigned()) {
					continue;
				}

				float highestRatioForThisPOI = -1;
				POIInterval POIIntervalAfterBestInsertPositionForThisPOI = null;

				POIInterval POIIntervalAfterInsertPosition = startingPOIIntervals[tour].getNextPOIInterval();
				int bestShiftForThisPOI = -1;
				while(POIIntervalAfterInsertPosition != null) {
					int shiftForNewPOI = getShift(currentPOI, POIIntervalAfterInsertPosition);
					if(canInsertBeforeThisPOI(currentPOI, POIIntervalAfterInsertPosition, shiftForNewPOI)) {
						float ratio = (float)Math.pow(currentPOI.getScore() / 100.0f, 2) / (shiftForNewPOI / 100.0f);
						if(Float.compare(ratio, highestRatioForThisPOI) > 0) {
							highestRatioForThisPOI = ratio;
							POIIntervalAfterBestInsertPositionForThisPOI = POIIntervalAfterInsertPosition;
							bestShiftForThisPOI = shiftForNewPOI;
						}
					}
					POIIntervalAfterInsertPosition = POIIntervalAfterInsertPosition.getNextPOIInterval();
				}

				if(Float.compare(highestRatioForThisPOI, highestRatio) > 0) {
					highestRatio = highestRatioForThisPOI;
					insertInfo.set(0, currentPOI);
					insertInfo.set(1, POIIntervalAfterBestInsertPositionForThisPOI);
					insertInfo.set(2, tour);
					insertInfo.set(3, bestShiftForThisPOI);
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
		int waitComponent = POIToBeInserted.getOpeningTime() - arrivalTimeForNewPOI;
		int waitOfNewPOI = waitComponent > 0? waitComponent: 0;

		int shiftOfNewPOI = travelTimeToNewPOI + waitOfNewPOI + POIToBeInserted.getDuration() + travelTimeFromNewPOIToNext - 
								currentTravelTime;

		return shiftOfNewPOI;
	}

	public boolean canInsertBeforeThisPOI(POI POIToBeInserted, POIInterval POIIntervalAfterTheInsertPosition, int shiftForNewPOI) {
		if(shiftForNewPOI > POIIntervalAfterTheInsertPosition.getWaitTime() + POIIntervalAfterTheInsertPosition.getMaxShift()) {
			return false;
		}

		int arrivalTime = POIIntervalAfterTheInsertPosition.getPreviousPOIInterval().getEndingTime() +
				POIIntervalAfterTheInsertPosition.getPreviousPOIInterval().getPOI().getTravelTimeToPOI(POIToBeInserted.getID());
		int startingTime = arrivalTime;
		if(POIToBeInserted.getOpeningTime() > startingTime) {
			startingTime = POIToBeInserted.getOpeningTime();
		}
		if(startingTime > POIToBeInserted.getClosingTime()) {
			return false;
		}

		return true;
	}

	public POIInterval insertPOI(POI POIToBeInserted, POIInterval POIIntervalAfterInsertPosition) {
		POIToBeInserted.setAssigned();
		
		POIInterval POIIntervalBeforeInsertPosition = POIIntervalAfterInsertPosition.getPreviousPOIInterval();

		POIInterval newPOIInterval = new POIInterval(POIToBeInserted, 0);

		newPOIInterval.setPreviousPOIInterval(POIIntervalBeforeInsertPosition);
		newPOIInterval.setNextPOIInterval(POIIntervalAfterInsertPosition);
		POIIntervalBeforeInsertPosition.setNextPOIInterval(newPOIInterval);
		POIIntervalAfterInsertPosition.setPreviousPOIInterval(newPOIInterval);

		return newPOIInterval;
	}

	public void calculateArriveStartAndWaitForNewPOI(POIInterval newPOIInterval) {
		int arrivalTime = newPOIInterval.getPreviousPOIInterval().getEndingTime() +
						newPOIInterval.getPreviousPOIInterval().getPOI().getTravelTimeToPOI(newPOIInterval.getPOI().getID());
		
		int startingTime = arrivalTime;
		if(newPOIInterval.getPOI().getOpeningTime() > startingTime) {
			startingTime = newPOIInterval.getPOI().getOpeningTime();
		}
		
		newPOIInterval.setArrivalTime(arrivalTime);
		newPOIInterval.updateStartingAndEndingTime(startingTime);
		newPOIInterval.updateWaitTime();
	}

	public void updateParametersForFollowingVisitsAfterInsert(POIInterval POIIntervalFollowingInsert, int shiftForNewPOI) {
		POIInterval currentPOIInterval = POIIntervalFollowingInsert;
		int lastShift = shiftForNewPOI;
		while(true) {
			int newWaitParameter = currentPOIInterval.getWaitTime() - lastShift;
			int newWaitForCurrentPOIInterval = newWaitParameter > 0? newWaitParameter: 0;
			currentPOIInterval.setWaitTime(newWaitForCurrentPOIInterval);

			currentPOIInterval.setArrivalTime(currentPOIInterval.getArrivalTime() + lastShift);

			int newLastShiftParameter = newWaitParameter * (-1);
			lastShift = newLastShiftParameter > 0? newLastShiftParameter: 0;
			if(lastShift == 0) {
				break;
			}

			currentPOIInterval.updateStartingAndEndingTime(lastShift);
			currentPOIInterval.updateMaxShift(-lastShift);

			currentPOIInterval = currentPOIInterval.getNextPOIInterval();
		}
	}

	public void updateMaxShiftForPreviousVisitsAndThis(POIInterval thisPOIInterval) {
		POIInterval currentPOIInterval = thisPOIInterval;
		while(currentPOIInterval.getPOI().getDuration() > 0) {
			currentPOIInterval.updateMaxShift();
			if(currentPOIInterval.getMaxShift() == 0) {
				break;
			}
			currentPOIInterval = currentPOIInterval.getPreviousPOIInterval();
		}
	}

	public void shakeStep(int startRemoveAt, int removeNConsecutiveVisits) {
		this.stuckInLocalOptimum = false;

		for(int tour = 0; tour < problemInput.getTourCount(); tour++) {
			int currentDayRemovals = 0;

			POIInterval currentPOIInterval = getNthPOIIntervalInTourX(startRemoveAt, tour);
			POIInterval POIIntervalAfterRemovingEnds = null;

			while(currentDayRemovals < removeNConsecutiveVisits && this.tourSizes[tour] != 0) {
				this.score -= currentPOIInterval.getPOI().getScore();
				currentPOIInterval = removePOIInterval(currentPOIInterval);
				if(currentPOIInterval == endingPOIIntervals[tour]) {
					currentPOIInterval = startingPOIIntervals[tour].getNextPOIInterval();
				}
				this.tourSizes[tour] -= 1;
				currentDayRemovals++;
			}
			
			POIIntervalAfterRemovingEnds = currentPOIInterval;
			
			updateParametersForFollowingVisitsAfterRemoval(currentPOIInterval, endingPOIIntervals[tour]);
			updateMaxShiftForPreviousVisitsAndThis(POIIntervalAfterRemovingEnds.getPreviousPOIInterval());
		}
	}

	public POIInterval getNthPOIIntervalInTourX(int N, int tour) {
		int currentPOIPosition = 0;
		POIInterval currentPOIInterval = startingPOIIntervals[tour].getNextPOIInterval();
		while(currentPOIPosition != N) {
			currentPOIInterval = currentPOIInterval.getNextPOIInterval();
			currentPOIPosition++;
		}
		return currentPOIInterval;
	}

	public POIInterval removePOIInterval(POIInterval currentPOIInterval) {
		currentPOIInterval.getPOI().unassign();

		POIInterval POIIntervalBeforeRemovePosition = currentPOIInterval.getPreviousPOIInterval();
		POIInterval POIIntervalAfterRemovePosition = currentPOIInterval.getNextPOIInterval();
		
		POIIntervalBeforeRemovePosition.setNextPOIInterval(currentPOIInterval.getNextPOIInterval());
		POIIntervalAfterRemovePosition.setPreviousPOIInterval(currentPOIInterval.getPreviousPOIInterval());

		return POIIntervalAfterRemovePosition;
	}

	public void updateParametersForFollowingVisitsAfterRemoval(POIInterval POIIntervalFollowingLastRemove, POIInterval endingPOIInterval) {
		POIInterval currentPOIInterval = POIIntervalFollowingLastRemove;
		while(currentPOIInterval != endingPOIInterval) {
			int arrivalTime = currentPOIInterval.getPreviousPOIInterval().getEndingTime() + 
							currentPOIInterval.getPreviousPOIInterval().getPOI().getTravelTimeToPOI(currentPOIInterval.getPOI().getID());
			currentPOIInterval.setArrivalTime(arrivalTime);
			int newStartingTime = currentPOIInterval.getArrivalTime();
			if(currentPOIInterval.getPOI().getOpeningTime() > newStartingTime) {
				newStartingTime = currentPOIInterval.getPOI().getOpeningTime();
			}
			int shiftTime = currentPOIInterval.getStartingTime() - newStartingTime;
			if(shiftTime == 0) {
				currentPOIInterval.updateWaitTime();
				break;
			}
			currentPOIInterval.updateStartingAndEndingTime(-shiftTime);
			currentPOIInterval.updateWaitTime();
			currentPOIInterval.updateMaxShift(shiftTime);
			currentPOIInterval = currentPOIInterval.getNextPOIInterval();
		}
		int arrivalTime = endingPOIInterval.getPreviousPOIInterval().getEndingTime() + 
						endingPOIInterval.getPreviousPOIInterval().getPOI().getTravelTimeToPOI(endingPOIInterval.getPOI().getID());
		endingPOIInterval.setArrivalTime(arrivalTime);
		endingPOIInterval.updateWaitTime();
	}

	public int sizeOfSmallestTour() {
		int sizeOfSmallestTour = tourSizes[0];
		for(int tour = 1; tour < tourSizes.length; tour++) {
			if(tourSizes[tour] < sizeOfSmallestTour) {
				sizeOfSmallestTour = tourSizes[tour];
			}
		}
		return sizeOfSmallestTour;
	}

	@Override
	public String toString() {
		String result = "Score: " + (this.score / 100.0f) + "\r\n";
		for(int tour = 0; tour < problemInput.getTourCount(); tour++) {
			String resultPart1 = "";
			String resultPart2 = "";
			POIInterval currentPOIInterval = this.startingPOIIntervals[tour];
			while(currentPOIInterval != null) {	
				resultPart1 += currentPOIInterval.getPOI().getID();
				if(currentPOIInterval.getNextPOIInterval() != null) {
					resultPart1 += " -> ";
				}
				if(currentPOIInterval.getArrivalTime() != 0) {
					resultPart2 += "----->" + (currentPOIInterval.getArrivalTime() / 100.0f);
					if(currentPOIInterval.getWaitTime() != 0) {
						resultPart2 += ".....";
					}
				}
				resultPart2 += "\r\n|" + (currentPOIInterval.getStartingTime() / 100.0f) + "____" + currentPOIInterval.getPOI().getID() + 
						"____" + (currentPOIInterval.getEndingTime() / 100.0f) + "|";
	
				currentPOIInterval = currentPOIInterval.getNextPOIInterval();
			}
			result += resultPart1 + resultPart2;
			result += "\r\n\r\n";
		}
		result += "================================================================================";
		return result;
	}

	public void setStartingPOIInterval(POIInterval startingPOIInterval[]) {
		this.startingPOIIntervals = startingPOIInterval;
	}

	public void setEndingPOIInterval(POIInterval endingPOIInterval[]) {
		this.endingPOIIntervals = endingPOIInterval;
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

		return clonedSolution;
	}
}
