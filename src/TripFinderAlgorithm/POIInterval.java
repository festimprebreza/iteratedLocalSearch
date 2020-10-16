package TripFinderAlgorithm;

public class POIInterval implements Cloneable {
	private POI containedPOI;	
	private int startsAt;
	private int endsAt;
	private POIInterval nextPOIInterval;
	private POIInterval previousPOIInterval;
	private int arrivalTime;
	private int wait;
	private int maxShift;

	public POIInterval(POI containedPOI, int startingTime) {
		this.containedPOI = containedPOI;
		this.startsAt = startingTime;
		this.endsAt = this.startsAt + this.containedPOI.getDuration();
	}

	public POI getPOI() {
		return this.containedPOI;
	}

	public int getStartingTime() {
		return this.startsAt;
	}

	public int getEndingTime() {
		return this.endsAt;
	}

	// shifting time could be negative also
	public void updateStartingAndEndingTime(int shiftingTime) {
		this.startsAt += shiftingTime;
		this.endsAt += shiftingTime;
	}

	public POIInterval getPreviousPOIInterval() {
		return this.previousPOIInterval;
	}

	public void setPreviousPOIInterval(POIInterval previousPOIInterval) {
		this.previousPOIInterval = previousPOIInterval;
	}
	
	public POIInterval getNextPOIInterval() {
		return this.nextPOIInterval;
	}
	
	public void setNextPOIInterval(POIInterval nextPOIInterval) {
		this.nextPOIInterval = nextPOIInterval;
	}

	public int getArrivalTime() {
		return this.arrivalTime;
	}

	public void setArrivalTime(int arrivalTime) {
		this.arrivalTime = arrivalTime;
	}

	public int getWaitTime() {
		return this.wait;
	}

	public void setWaitTime(int wait) {
		this.wait = wait;
	}

	public void updateWaitTime() {
		this.wait = this.startsAt - this.arrivalTime;
	}

	public int getMaxShift() {
		return maxShift;
	}

	public void updateMaxShift(int shift) {
		this.maxShift += shift;
	}
	
	public void updateMaxShift() {
		int newMaxShiftParameter1 = this.containedPOI.getClosingTime() - this.getStartingTime();
		int newMaxShiftParameter2 = this.nextPOIInterval.getWaitTime() + this.nextPOIInterval.getMaxShift();
		int newMaxShift = newMaxShiftParameter1 < newMaxShiftParameter2? newMaxShiftParameter1: newMaxShiftParameter2;
		this.maxShift = newMaxShift;
	}

	public int getTravelTime() {
		return this.nextPOIInterval.getArrivalTime() - this.endsAt;
	}

	@Override
	public Object clone() throws CloneNotSupportedException {
		POIInterval clonedPOIInterval = (POIInterval)super.clone();
		if(this.nextPOIInterval != null) {
			clonedPOIInterval.setNextPOIInterval((POIInterval)this.nextPOIInterval.clone());
		}
		
		return clonedPOIInterval;
	}
}
