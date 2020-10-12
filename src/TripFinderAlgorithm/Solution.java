package TripFinderAlgorithm;

public class Solution implements Cloneable {
	private POIInterval[] startingPOIIntervals;
	private POIInterval[] endingPOIIntervals;
	private ProblemInput problemInput;
	private float score;

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
		}
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
		// if no tour has space left to put POIs
		return true;
	}

	public void insertStep() {

	}

	public void shakeStep() {

	}

	public int sizeOfSmallestTour() {
		return 2;
	}

	@Override
	public String toString() {
		String result = "Score: " + this.score + "\r\n";
		for(int tour = 0; tour < problemInput.getTourCount(); tour++) {
			POIInterval currentPOIInterval = this.startingPOIIntervals[tour];
			TravelInterval currentTravelInterval;
			while(currentPOIInterval != null) {
				currentTravelInterval = currentPOIInterval.getTravelInterval();
	
				result += "|" + currentPOIInterval.getStartingTime() + "____" + currentPOIInterval.getPOI().getID() + 
						"____" + currentPOIInterval.getEndingTime() + "|";
	
				if(currentTravelInterval != null) {
					result += "---->" + currentTravelInterval.getEndingTime();
					if(currentTravelInterval.getNextWaitInterval() != null) {
						result += ".....";
					}
				}
	
				currentPOIInterval = currentPOIInterval.getNextPOIInterval();
			}
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
