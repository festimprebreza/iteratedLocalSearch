package TripFinderAlgorithm;

public class POIInterval extends TimelineInterval implements Cloneable {
	private POI containedPOI;	
	private POIInterval nextPOIInterval;
	private POIInterval previousPOIInterval;
	private TravelInterval nextTravelInterval;

	// FIX:
	// you do not need to input endingTime as a parameter, you can just add startingTime and duration
	public POIInterval(POI containedPOI, float startingTime, float endingTime) {
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

	public void setTravelInterval(float startingTime, float endingTime) {
		// FIX: 
		// fix that round thing
		this.nextTravelInterval = new TravelInterval(startingTime, Math.round(endingTime * 100) / 100f);
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
