package TripFinderAlgorithm;

public class Solution implements Cloneable {
	private POIInterval startingPOIInterval;
	private POIInterval endingPOIInterval;
	private float score;

	public Solution(POI startingPOI, POI endingPOI) {
		this.startingPOIInterval = new POIInterval(startingPOI, startingPOI.getOpeningTime(), startingPOI.getOpeningTime());
		this.endingPOIInterval = new POIInterval(endingPOI, endingPOI.getClosingTime(), endingPOI.getClosingTime());

		this.startingPOIInterval.setNextPOIInterval(this.endingPOIInterval);
		this.endingPOIInterval.setPreviousPOIInterval(startingPOIInterval);

		this.startingPOIInterval.setTravelInterval(
										this.startingPOIInterval.getEndingTime(), 
										this.startingPOIInterval.getPOI().getTravelTimeToPOI(this.endingPOIInterval.getPOI())
										);
		this.startingPOIInterval.getTravelInterval().setNextWaitInterval(
									this.startingPOIInterval.getTravelInterval().getEndingTime(),
									this.endingPOIInterval.getStartingTime()
									);
	}

	public void shake() {

	}

	public float getScore() {
		return this.score;
	}

	public void setScore(float score) {
		this.score = score;
	}

	public void setStartingPOIInterval(POIInterval startingPOIInterval) {
		this.startingPOIInterval = startingPOIInterval;
	}

	public void setEndingPOIInterval(POIInterval endingPOIInterval) {
		this.endingPOIInterval = endingPOIInterval;
	}

	public boolean notStuckInLocalOptimum() {
		return true;
	}

	public void insert() {

	}

	public int sizeOfSmallestTour() {
		return 2;
	}

	@Override
	public String toString() {
		String result = "Score: " + this.score + "\r\n";
		POIInterval currentPOIInterval = this.startingPOIInterval;
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
		return result;
	}

	@Override
	public Object clone() throws CloneNotSupportedException {
		Solution clonedSolution = (Solution)super.clone();

		clonedSolution.setStartingPOIInterval((POIInterval)this.startingPOIInterval.clone());
		clonedSolution.setEndingPOIInterval((POIInterval)this.endingPOIInterval.clone());
		
		return clonedSolution;
	}
}
