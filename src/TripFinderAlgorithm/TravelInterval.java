package TripFinderAlgorithm;

public class TravelInterval extends TimelineInterval {
	private WaitInterval nextWaitInterval;

	public TravelInterval(float startingTime, float endingTime) {
		super(startingTime, endingTime);
	}

	public WaitInterval getNextWaitInterval() {
		return this.nextWaitInterval;
	}

	public void setNextWaitInterval(float startingTime, float endingTime) {
		this.nextWaitInterval = new WaitInterval(startingTime, endingTime);
	}

	public void setNextWaitInterval(WaitInterval waitInterval) {
		this.nextWaitInterval = waitInterval;
	}
}
