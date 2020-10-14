package TripFinderAlgorithm;

public class Solution implements Cloneable {
	private POIInterval[] startingPOIIntervals;
	private POIInterval[] endingPOIIntervals;
	private ProblemInput problemInput;
	private float score;
	private boolean stuckInLocalOptimum;

	private POI POIWithShortestVisitDuration;
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
			this.startingPOIIntervals[tour] = new POIInterval(startingPOI, startingPOI.getOpeningTime(), 
														startingPOI.getOpeningTime());
			this.endingPOIIntervals[tour] = new POIInterval(endingPOI, endingPOI.getClosingTime(), 
														endingPOI.getClosingTime());
	
			this.startingPOIIntervals[tour].setNextPOIInterval(this.endingPOIIntervals[tour]);
			this.endingPOIIntervals[tour].setPreviousPOIInterval(this.startingPOIIntervals[tour]);
	
			this.startingPOIIntervals[tour].setTravelInterval(
											this.startingPOIIntervals[tour].getEndingTime(), 
											this.startingPOIIntervals[tour].getPOI().getTravelTimeToPOI(
													this.endingPOIIntervals[tour].getPOI()
												)
											);
			this.startingPOIIntervals[tour].getTravelInterval().setNextWaitInterval(
										this.startingPOIIntervals[tour].getTravelInterval().getEndingTime(),
										this.endingPOIIntervals[tour].getStartingTime()
										);
			
			POIWithShortestVisitDuration = problemInput.getVisitablePOIs()[0];
		}

		tourSizes = new int[problemInput.getTourCount()];
	}

	public void setStartingPOIInterval(POIInterval startingPOIInterval[]) {
		this.startingPOIIntervals = startingPOIInterval;
	}

	public void setEndingPOIInterval(POIInterval endingPOIInterval[]) {
		this.endingPOIIntervals = endingPOIInterval;
	}

	public float getScore() {
		return this.score;
	}

	public void setScore(float score) {
		this.score = score;
	}


	public boolean notStuckInLocalOptimum() {
		// if(POIWithShortestVisitDuration.isAssigned()) {
		// 	POIWithShortestVisitDuration = getThePOIWithShortestDurationThatIsUnassigned();
		// }

		// // if the function returned null, then that means all POIs are assigned, so we are stuck in a local optimum
		// if(POIWithShortestVisitDuration == null) {
		// 	return true;
		// }

		// POIInterval currentPOIInterval = null;
		// for(int tour = 0; tour < problemInput.getTourCount(); tour++) {
		// 	currentPOIInterval = startingPOIIntervals[tour];
		// 	while(currentPOIInterval.getTravelInterval() != null) {
		// 		if(currentPOIInterval.getTravelInterval().getNextWaitInterval() == null) {
		// 			currentPOIInterval = currentPOIInterval.getNextPOIInterval();
		// 		}
		// 		else {
		// 			if(currentPOIInterval.getTravelInterval().getNextWaitInterval().getDuration() <= 
		// 						POIWithShortestVisitDuration.getDuration()) {
		// 				return true;
		// 			}
		// 		}
		// 	}
		// }

		// return false;
		// FIX:
		// check if this implementation is good? because for now the function insertStep does two things at once
		// which is not good usually; it's side effect is setting a flag; check to see if you can implement this smarter
		return !this.stuckInLocalOptimum;
	}

	public POI getThePOIWithShortestDurationThatIsUnassigned() {
		int minimalDuration = 10000;
		POI shortestPOI = null;
		for(POI currentPOI: problemInput.getVisitablePOIs()) {
			if(currentPOI.isAssigned()) {
				continue;
			}
			if(currentPOI.getDuration() < minimalDuration) {
				shortestPOI = currentPOI;
			}
		}
		return shortestPOI;
	}

	public void insertStep() {
		for(int tour = 0; tour < problemInput.getTourCount(); tour++) {
			POIInterval currentPOIInterval = startingPOIIntervals[tour];

			for(POI currentPOI: problemInput.getVisitablePOIs()) {
				if(currentPOI.isAssigned()) {
					continue;
				}
				while(currentPOIInterval.getTravelInterval() != null) {
					if(currentPOIInterval.getTravelInterval().getNextWaitInterval() == null) {
						currentPOIInterval = currentPOIInterval.getNextPOIInterval();
					}
					else {
						if(canInsertAfterThisPOI(currentPOI, currentPOIInterval)) {
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
		}
		this.stuckInLocalOptimum = true;
	}

	public boolean canInsertAfterThisPOI(POI POIToBeInserted, POIInterval POIIntervalBeforeTheInsertionPlace) {
		// FIX:
		// add shift and stuff here
		if(POIToBeInserted.getOpeningTime() + POIToBeInserted.getDuration() + 
			POIToBeInserted.getTravelTimeToPOI(POIIntervalBeforeTheInsertionPlace.getNextPOIInterval().getPOI()) > 
			POIIntervalBeforeTheInsertionPlace.getNextPOIInterval().getStartingTime()) {
			return false;
		}
		
		// FIX:
		// time check; maybe put into a function; also maybe just give as parameters, prevPOI, nextPOI and WaitInterval
		// oooor just extract them here at the beginning of the function
		if(POIToBeInserted.getDuration() + 
		POIIntervalBeforeTheInsertionPlace.getPOI().getTravelTimeToPOI(POIToBeInserted) + 
		POIToBeInserted.getTravelTimeToPOI(POIIntervalBeforeTheInsertionPlace.getNextPOIInterval().getPOI()) -
		POIIntervalBeforeTheInsertionPlace.getTravelInterval().getDuration() >
		POIIntervalBeforeTheInsertionPlace.getTravelInterval().getNextWaitInterval().getDuration()) {
			return false;
		}
		return true;
	}

	public void insertPOI(POI POIToBeInserted, POIInterval POIIntervalBeforeTheInsertionPlace) {
		// create traveling interval first
		POIIntervalBeforeTheInsertionPlace.setTravelInterval(POIIntervalBeforeTheInsertionPlace.getEndingTime(), 
									POIIntervalBeforeTheInsertionPlace.getPOI().getTravelTimeToPOI(POIToBeInserted) + 
									POIIntervalBeforeTheInsertionPlace.getEndingTime());
		// define when it starts (considering opening time)
		float startingTime = POIIntervalBeforeTheInsertionPlace.getTravelInterval().getEndingTime();
		if(POIToBeInserted.getOpeningTime() > startingTime) {
			startingTime = POIToBeInserted.getOpeningTime();
		}
		// create POIInterval
		POIInterval newPOIInterval = new POIInterval(POIToBeInserted, startingTime, 
													startingTime + POIToBeInserted.getDuration());
		// add travel interval from new POI to the next one
		newPOIInterval.setTravelInterval(newPOIInterval.getEndingTime(), 
						newPOIInterval.getEndingTime() + 
						POIToBeInserted.getTravelTimeToPOI(POIIntervalBeforeTheInsertionPlace.getNextPOIInterval().getPOI()));
		// edit previousPOIInterval and nextPOIInterval for all attendants
		newPOIInterval.setPreviousPOIInterval(POIIntervalBeforeTheInsertionPlace);
		newPOIInterval.setNextPOIInterval(POIIntervalBeforeTheInsertionPlace.getNextPOIInterval());
		POIIntervalBeforeTheInsertionPlace.setNextPOIInterval(newPOIInterval);
		newPOIInterval.getNextPOIInterval().setPreviousPOIInterval(newPOIInterval);

		// check if free interval before
		if(POIIntervalBeforeTheInsertionPlace.getTravelInterval().getEndingTime() < newPOIInterval.getStartingTime()) {
			POIIntervalBeforeTheInsertionPlace.getTravelInterval().setNextWaitInterval(
						POIIntervalBeforeTheInsertionPlace.getTravelInterval().getEndingTime(), 
						newPOIInterval.getStartingTime());
			POIIntervalBeforeTheInsertionPlace.getTravelInterval().getNextWaitInterval().setNextPOIInterval(newPOIInterval);
		}
		// check if free interval after
		if(newPOIInterval.getTravelInterval().getEndingTime() < newPOIInterval.getNextPOIInterval().getStartingTime()) {
			newPOIInterval.getTravelInterval().setNextWaitInterval(
				newPOIInterval.getTravelInterval().getEndingTime(),
				newPOIInterval.getNextPOIInterval().getStartingTime());
			newPOIInterval.getTravelInterval().getNextWaitInterval().setNextPOIInterval(newPOIInterval.getNextPOIInterval());
		}

		POIToBeInserted.setAssigned();
	}

	public void shakeStep(int startRemoveAt, int removeNConsecutiveVisits) {
		this.stuckInLocalOptimum = false;
		for(int tour = 0; tour < problemInput.getTourCount(); tour++) {
			int currentDayRemovals = 0;
			int currentPOIPosition = 0;
			POIInterval currentPOIInterval = startingPOIIntervals[tour].getNextPOIInterval();
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
				POIInterval nextPOIInterval = currentPOIInterval.getNextPOIInterval();
				if(nextPOIInterval == endingPOIIntervals[tour]) {
					nextPOIInterval = startingPOIIntervals[tour].getNextPOIInterval();
				}
				removePOIInterval(currentPOIInterval);
				this.tourSizes[tour] -= 1;
				currentDayRemovals++;
				currentPOIInterval = nextPOIInterval;
			}
			// shift for each tour
			while(true) {
				if(canShiftPOIInterval(currentPOIInterval)) {
					shiftPOIInterval(currentPOIInterval);
					currentPOIInterval = currentPOIInterval.getNextPOIInterval();
					if(currentPOIInterval == endingPOIIntervals[tour]) {
						break;
					}
				}
				else {
					break;
				}
			}
		}
	}

	public void removePOIInterval(POIInterval currentPOIInterval) {
		// set unassigned
		currentPOIInterval.getPOI().unassign();
		this.score -= currentPOIInterval.getPOI().getScore();
		// change pointers
		currentPOIInterval.getPreviousPOIInterval().setNextPOIInterval(currentPOIInterval.getNextPOIInterval());
		currentPOIInterval.getNextPOIInterval().setPreviousPOIInterval(currentPOIInterval.getPreviousPOIInterval());
		// create travel space
		currentPOIInterval.getPreviousPOIInterval().setTravelInterval(
							currentPOIInterval.getPreviousPOIInterval().getEndingTime(),
							currentPOIInterval.getPreviousPOIInterval().getPOI().getTravelTimeToPOI(currentPOIInterval.getNextPOIInterval().getPOI()));
		// create waiting time							
		currentPOIInterval.getPreviousPOIInterval().getTravelInterval().setNextWaitInterval(
										currentPOIInterval.getPreviousPOIInterval().getTravelInterval().getEndingTime(),
										currentPOIInterval.getNextPOIInterval().getStartingTime());
	}

	public boolean canShiftPOIInterval(POIInterval currentPOIInterval) {
		if(currentPOIInterval.getPOI().getOpeningTime() < currentPOIInterval.getStartingTime() &&
			currentPOIInterval.getPreviousPOIInterval().getTravelInterval().getEndingTime() < currentPOIInterval.getStartingTime()) {
			return true;
		}
		return false;
	}

	public void shiftPOIInterval(POIInterval currentPOIInterval) {
		// FIX: 
		// check how much can you shift(determine starting Time)
		float startingTime = currentPOIInterval.getPreviousPOIInterval().getTravelInterval().getEndingTime();
		if(currentPOIInterval.getPOI().getOpeningTime() > startingTime) {
			startingTime = currentPOIInterval.getPOI().getOpeningTime();
		}

		float shiftedFor = currentPOIInterval.getStartingTime() - startingTime;
		// update start and end time
		// FIX:
		// create a function in POIInterval that uses just the starting time to update the ending time also (using duration)
		// FIX:
		// maybe also create a functioin called shift() that does this automatically just using one parameter
		currentPOIInterval.setStartingTime(startingTime);
		currentPOIInterval.setEndingTime(startingTime + currentPOIInterval.getPOI().getDuration());
		// update ending of free time before this visit
		currentPOIInterval.getPreviousPOIInterval().getTravelInterval().getNextWaitInterval().setEndingTime(startingTime);
		// update travel interval times
		// FIX:
		// same here, create a function shift
		currentPOIInterval.getTravelInterval().setStartingTime(Math.round((currentPOIInterval.getTravelInterval().getStartingTime() - shiftedFor) * 100) / 100.0f);
		currentPOIInterval.getTravelInterval().setEndingTime(Math.round((currentPOIInterval.getTravelInterval().getEndingTime() - shiftedFor) * 100) / 100.0f);
		// check if there are any new free intervals left, etc. 
		if(currentPOIInterval.getTravelInterval().getEndingTime() < currentPOIInterval.getNextPOIInterval().getStartingTime()) {
			//(maybe don't create a new wait interval, 
			// just change the values of the current one)
			// if there was no free time, create a new one, of course
			// FIX:
			// i think this part is duplicate with the removePOIInterval() func
			if(currentPOIInterval.getTravelInterval().getNextWaitInterval() == null) {
				currentPOIInterval.getTravelInterval().setNextWaitInterval(currentPOIInterval.getTravelInterval().getEndingTime(), 
																	currentPOIInterval.getNextPOIInterval().getStartingTime());
				currentPOIInterval.getTravelInterval().getNextWaitInterval().setNextPOIInterval(currentPOIInterval.getNextPOIInterval());
			}
			else {
				currentPOIInterval.getTravelInterval().getNextWaitInterval().setStartingTime(currentPOIInterval.getTravelInterval().getEndingTime());
			}			
		}
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
		String result = "Score: " + this.score + "\r\n";
		for(int tour = 0; tour < problemInput.getTourCount(); tour++) {
			String resultPart1 = "";
			String resultPart2 = "";
			POIInterval currentPOIInterval = this.startingPOIIntervals[tour];
			TravelInterval currentTravelInterval;
			while(currentPOIInterval != null) {
				currentTravelInterval = currentPOIInterval.getTravelInterval();
	
				resultPart1 += currentPOIInterval.getPOI().getID() + " ";
				resultPart2 += "|" + currentPOIInterval.getStartingTime() + "____" + currentPOIInterval.getPOI().getID() + 
						"____" + currentPOIInterval.getEndingTime() + "|";
	
				if(currentTravelInterval != null) {
					resultPart2 += "---->" + currentTravelInterval.getEndingTime();
					if(currentTravelInterval.getNextWaitInterval() != null) {
						resultPart2 += ".....";
					}
				}
	
				currentPOIInterval = currentPOIInterval.getNextPOIInterval();
			}
			result += resultPart1 + "\r\n" + resultPart2;
			result += "\r\n\r\n\r\n";
		}
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
