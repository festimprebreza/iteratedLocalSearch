package TripFinderAlgorithm;

public class POIInterval extends TimelineInterval implements Cloneable {
	private POI containedPOI;	
	private POIInterval nextPOIInterval;
	private POIInterval previousPOIInterval;
	private TravelInterval nextTravelInterval;
	private WaitInterval waitInterval;
	private int maxShift;

	// FIX:
	// you do not need to input endingTime as a parameter, you can just add startingTime and duration
	public POIInterval(POI containedPOI, int startingTime, int endingTime) {
		super(startingTime, endingTime);
		this.containedPOI = containedPOI;
	}

	public POI getPOI() {
		return this.containedPOI;
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

	public TravelInterval getTravelInterval() {
		return this.nextTravelInterval;
	}

	public void setTravelInterval(int startingTime, int endingTime) {
		// FIX: 
		// fix that round thing
		this.nextTravelInterval = new TravelInterval(startingTime, endingTime);
	}

	public WaitInterval getWaitInterval() {
		// FIX:
		// edit here to use this instead of multi accessing objects
		return this.waitInterval;
	}

	public void setWaitInterval(WaitInterval waitInterval) {
		this.waitInterval = waitInterval;
	}

	public int getMaxShift() {
		return maxShift;
	}

	public void setMaxShift(int maxShift) {
		this.maxShift = maxShift;
	}

	public void updateMaxShift() {
		int newMaxShiftParameter1 = this.getPOI().getClosingTime() - this.getStartingTime();
		int newMaxShiftParameter2 = this.getNextPOIInterval().getWaitTime() + this.getNextPOIInterval().getMaxShift();
		int newMaxShift = newMaxShiftParameter1 < newMaxShiftParameter2? newMaxShiftParameter1: newMaxShiftParameter2;
		this.maxShift = newMaxShift;
	}

	public int getWaitTime() {
		if(this.waitInterval == null) {
			return 0;
		}
		return this.waitInterval.getDuration();
	}

	@Override
	public Object clone() throws CloneNotSupportedException {
		POIInterval clonedPOIInterval = (POIInterval)super.clone();
		if(this.nextPOIInterval != null) {
			clonedPOIInterval.setNextPOIInterval((POIInterval)this.nextPOIInterval.clone());
			clonedPOIInterval.setTravelInterval(this.nextTravelInterval.getStartingTime(), 
												this.nextTravelInterval.getEndingTime());
		}		

		if(this.getTravelInterval() != null) {
			if(this.getTravelInterval().getNextWaitInterval() != null) {
				clonedPOIInterval.getTravelInterval().setNextWaitInterval(
							this.getTravelInterval().getNextWaitInterval().getStartingTime(), 
							this.getTravelInterval().getNextWaitInterval().getEndingTime()
				);
				clonedPOIInterval.getTravelInterval().getNextWaitInterval().setNextPOIInterval(
							clonedPOIInterval.getNextPOIInterval()
				);
			}
		}
		
		return clonedPOIInterval;
	}
}
