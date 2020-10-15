package TripFinderAlgorithm;

public class WaitInterval extends TimelineInterval {
	private POIInterval nextPOIInterval;

	public WaitInterval(int startingTime, int endingTime) {
		super(startingTime, endingTime);
	}

	public POIInterval getNextPOIInterval() {
		return this.nextPOIInterval;
	}

	public void setNextPOIInterval(POIInterval nextPOIInterval) {
		this.nextPOIInterval = nextPOIInterval;
	}
}
