package TripFinderAlgorithm;

public class WaitInterval extends TimelineInterval {
	private POIInterval nextPOIInterval;

	public WaitInterval(float startingTime, float endingTime) {
		super(startingTime, endingTime);
	}

	public POIInterval getNextPOIInterval() {
		return this.nextPOIInterval;
	}

	public void setNextPOIInterval(POIInterval nextPOIInterval) {
		this.nextPOIInterval = nextPOIInterval;
	}
}
