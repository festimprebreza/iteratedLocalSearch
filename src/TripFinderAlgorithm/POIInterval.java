package TripFinderAlgorithm;

public class POIInterval extends TimelineInterval {
	private POI containedPOI;	
	private POIInterval nextPOIInterval;
	private POIInterval previousPOIInterval;
	private TravelInterval nextTravelInterval;

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
		this.nextTravelInterval = new TravelInterval(startingTime, endingTime);
	}
}
