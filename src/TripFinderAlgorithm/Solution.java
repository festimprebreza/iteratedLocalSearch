package TripFinderAlgorithm;

public class Solution implements Cloneable {
	private POIInterval[] startingPOIIntervals;
	private POIInterval[] endingPOIIntervals;
	private ProblemInput problemInput;
	private int score;
	private boolean stuckInLocalOptimum;
	// FIX:
	// idea: add an array that holds sizes of all tours; then you just find the minimum of that array
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

	public void setStartingPOIInterval(POIInterval startingPOIInterval[]) {
		this.startingPOIIntervals = startingPOIInterval;
	}

	public void setEndingPOIInterval(POIInterval endingPOIInterval[]) {
		this.endingPOIIntervals = endingPOIInterval;
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

	// FIX:
	// make this function same as shakeStep
	// insertPOI() does not have to do all the shifting and stuff
	public void insertStep() {
		for(int tour = 0; tour < problemInput.getTourCount(); tour++) {
			for(POI currentPOI: problemInput.getVisitablePOIs()) {
				if(currentPOI.isAssigned()) {
					continue;
				}

				POIInterval currentPOIInterval = startingPOIIntervals[tour].getNextPOIInterval();

				while(currentPOIInterval != null) { // so basically until the end of day
					if(canInsertBeforeThisPOI(currentPOI, currentPOIInterval)) {
						insertPOI(currentPOI, currentPOIInterval);
						this.score += currentPOI.getScore();
						this.stuckInLocalOptimum = false;
						this.tourSizes[tour] += 1;
						return;
					}
					currentPOIInterval = currentPOIInterval.getNextPOIInterval();
				}
			}
		}
		// if I could not insert anything here, then I must have been stuck in a local optimum
		this.stuckInLocalOptimum = true;
	}

	public boolean canInsertBeforeThisPOI(POI POIToBeInserted, POIInterval POIIntervalAfterTheInsertionPlace) {
		// FIX:
		// maybe other checks are needed here too
		int shiftForNewPOI = getShift(POIToBeInserted, POIIntervalAfterTheInsertionPlace);

		if(shiftForNewPOI > POIIntervalAfterTheInsertionPlace.getWaitTime() + POIIntervalAfterTheInsertionPlace.getMaxShift()) {
			return false;
		}

		return true;
	}

	public int getShift(POI POIToBeInserted, POIInterval POIIntervalAfterInsertionPlace) {
		POIInterval POIIntervalBeforeInsertionPlace = POIIntervalAfterInsertionPlace.getPreviousPOIInterval();

		int travelTimeToNewPOI = POIIntervalBeforeInsertionPlace.getPOI().getTravelTimeToPOI(POIToBeInserted.getID());
		int travelTimeFromNewPOIToNext = POIToBeInserted.getTravelTimeToPOI(POIIntervalAfterInsertionPlace.getPOI().getID());
		int currentTravelTime = POIIntervalBeforeInsertionPlace.getTravelTime();

		int arrivalTimeForNewPOI = POIIntervalBeforeInsertionPlace.getEndingTime() + travelTimeToNewPOI;
		int waitComponent = POIToBeInserted.getOpeningTime() - arrivalTimeForNewPOI;
		int waitOfNewPOI = waitComponent > 0? waitComponent: 0;

		int shiftOfNewPOI = travelTimeToNewPOI + waitOfNewPOI + POIToBeInserted.getDuration() + travelTimeFromNewPOIToNext - 
								currentTravelTime;

		return shiftOfNewPOI;
	}

	// FIX:
	// create mini functions just to make the function more clearer
	public void insertPOI(POI POIToBeInserted, POIInterval POIIntervalAfterInsertionPlace) {
		POIToBeInserted.setAssigned();
		int lastShift = getShift(POIToBeInserted, POIIntervalAfterInsertionPlace);
		
		POIInterval POIIntervalBeforeInsertionPlace = POIIntervalAfterInsertionPlace.getPreviousPOIInterval();

		// define new arrival time
		int arrivalTime = POIIntervalBeforeInsertionPlace.getEndingTime() + 
								POIIntervalBeforeInsertionPlace.getPOI().getTravelTimeToPOI(POIToBeInserted.getID());
		// define when it starts
		int startingTime = arrivalTime;
		if(POIToBeInserted.getOpeningTime() > startingTime) {
			startingTime = POIToBeInserted.getOpeningTime();
		}

		// create POIInterval
		POIInterval newPOIInterval = new POIInterval(POIToBeInserted, startingTime);
		newPOIInterval.setArrivalTime(arrivalTime);
		newPOIInterval.updateWaitTime();

		// edit previousPOIInterval and nextPOIInterval for all attendants
		newPOIInterval.setPreviousPOIInterval(POIIntervalBeforeInsertionPlace);
		newPOIInterval.setNextPOIInterval(POIIntervalAfterInsertionPlace);
		POIIntervalBeforeInsertionPlace.setNextPOIInterval(newPOIInterval);
		POIIntervalAfterInsertionPlace.setPreviousPOIInterval(newPOIInterval);

		// update shift and wait for following POIs
		// update others maxshift
		POIInterval currentPOIInterval = POIIntervalAfterInsertionPlace;
		// FIX:
		// add a way here to identify when the end of tour is reached
		// in fact I need to update the last POI also
		while(true) {
			// update new wait
			int newWaitParameter = currentPOIInterval.getWaitTime() - lastShift;
			int newWaitForCurrentPOIInterval = newWaitParameter > 0? newWaitParameter: 0;
			currentPOIInterval.setWaitTime(newWaitForCurrentPOIInterval);
			currentPOIInterval.setArrivalTime(currentPOIInterval.getArrivalTime() + lastShift);
			// update this shift, and set lastShift to it
			int newLastShiftParameter = newWaitParameter * (-1);
			lastShift = newLastShiftParameter > 0? newLastShiftParameter: 0;
			if(lastShift == 0) {
				break;
			}

			currentPOIInterval.updateStartingAndEndingTime(lastShift);
			// update new maxShift
			currentPOIInterval.setMaxShift(currentPOIInterval.getMaxShift() - lastShift);
			currentPOIInterval = currentPOIInterval.getNextPOIInterval();
		}

		// special case for the last POI


		// update your and previous POIs maxshift
		currentPOIInterval = newPOIInterval;
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
			int currentPOIPosition = 0;

			POIInterval currentPOIInterval = startingPOIIntervals[tour].getNextPOIInterval();
			POIInterval nextPOIInterval = null;
			while(true) {
				if(currentPOIPosition != startRemoveAt) {
					currentPOIInterval = currentPOIInterval.getNextPOIInterval();
					currentPOIPosition++;
					continue;
				}
				else {
					break;
				}
			}

			while(currentDayRemovals < removeNConsecutiveVisits) {
				// FIX: 
				// check logic here again
				// i keep the next POI in memory, because the remove function just deletes it and then we have no access
				// to the next POI; 
				nextPOIInterval = currentPOIInterval.getNextPOIInterval();
				// if we reach the end of the tour, we go from the beginning
				if(currentPOIInterval == endingPOIIntervals[tour]) {
					if(this.tourSizes[tour] == 0) {
						break;
					}
					currentPOIInterval = startingPOIIntervals[tour].getNextPOIInterval();
					continue;
				}
				// FIX:
				// I could make the remove function return the next (or the previous)
				removePOIInterval(currentPOIInterval);
				this.tourSizes[tour] -= 1;
				currentDayRemovals++;				
				currentPOIInterval = nextPOIInterval;
			}

			if(nextPOIInterval == null) {
				nextPOIInterval = endingPOIIntervals[tour];
			}
			// shift for each tour
			currentPOIInterval = nextPOIInterval;
			while(currentPOIInterval != endingPOIIntervals[tour]) {
				// shift left
				// FIX: 
				// check how you can implement the insert formulas here, for the shake step
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
				currentPOIInterval.setMaxShift(currentPOIInterval.getMaxShift() + shiftTime);
				currentPOIInterval = currentPOIInterval.getNextPOIInterval();
			}
			// don't forget to update arrival time of endingPOIInterval
			// FIX:
			// if shiftTime == 0 up there, checks out, then I am doing this update for no reason
			int arrivalTime = endingPOIIntervals[tour].getPreviousPOIInterval().getEndingTime() + 
							endingPOIIntervals[tour].getPreviousPOIInterval().getPOI().getTravelTimeToPOI(endingPOIIntervals[tour].getPOI().getID());
			endingPOIIntervals[tour].setArrivalTime(arrivalTime);
			endingPOIIntervals[tour].updateWaitTime();
			

			currentPOIInterval = nextPOIInterval.getPreviousPOIInterval();
			while(currentPOIInterval != startingPOIIntervals[tour]) {
				// update maxshift
				currentPOIInterval.updateMaxShift();
				currentPOIInterval = currentPOIInterval.getPreviousPOIInterval();
			}
		}
	}

	public void removePOIInterval(POIInterval currentPOIInterval) {
		POIInterval POIIntervalBeforeRemovePosition = currentPOIInterval.getPreviousPOIInterval();
		POIInterval POIIntervalAfterRemovePosition = currentPOIInterval.getNextPOIInterval();
		// set unassigned
		currentPOIInterval.getPOI().unassign();
		this.score -= currentPOIInterval.getPOI().getScore();
		// change pointers
		POIIntervalBeforeRemovePosition.setNextPOIInterval(POIIntervalAfterRemovePosition);
		POIIntervalAfterRemovePosition.setPreviousPOIInterval(POIIntervalBeforeRemovePosition);
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
			resultPart1 += currentPOIInterval.getPOI().getID() + " ";
			resultPart2 += "\r\n|" + (currentPOIInterval.getStartingTime() / 100.0f) + "____" + currentPOIInterval.getPOI().getID() + 
						"____" + (currentPOIInterval.getEndingTime() / 100.0f) + "|";
			currentPOIInterval = currentPOIInterval.getNextPOIInterval();
			while(currentPOIInterval != null) {	
				resultPart1 += currentPOIInterval.getPOI().getID() + " ";
				resultPart2 += "----->" + (currentPOIInterval.getArrivalTime() / 100.0f);
				if(currentPOIInterval.getWaitTime() != 0) {
					resultPart2 += ".....";
				}
				resultPart2 += "\r\n|" + (currentPOIInterval.getStartingTime() / 100.0f) + "____" + currentPOIInterval.getPOI().getID() + 
						"____" + (currentPOIInterval.getEndingTime() / 100.0f) + "|";
	
				currentPOIInterval = currentPOIInterval.getNextPOIInterval();
			}
			result += resultPart1 + "\r\n" + resultPart2;
			result += "\r\n";
		}
		result += "\r\n================================================================================";
		return result;
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
