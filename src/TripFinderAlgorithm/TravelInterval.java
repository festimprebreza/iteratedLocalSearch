package TripFinderAlgorithm;

public class TravelInterval extends TimelineInterval {
	private WaitInterval nextWaitInterval;

	public TravelInterval(int startingTime, int endingTime) {
		super(startingTime, endingTime);
	}

	public WaitInterval getNextWaitInterval() {
		return this.nextWaitInterval;
	}

	public void setNextWaitInterval(int startingTime, int endingTime) {
		this.nextWaitInterval = new WaitInterval(startingTime, endingTime);
	}

	public void setNextWaitInterval(WaitInterval waitInterval) {
		this.nextWaitInterval = waitInterval;
	}
}
