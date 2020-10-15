package TripFinderAlgorithm;

public abstract class TimelineInterval {
	private int startsAt;
	private int endsAt;

	public TimelineInterval(int startingTime, int endingTime) {
		this.startsAt = startingTime;
		this.endsAt = endingTime;
	}

	public int getStartingTime() {
		return this.startsAt;
	}

	public void setStartingTime(int startingTime) {
		this.startsAt = startingTime;
	}

	public int getEndingTime() {
		return this.endsAt;
	}

	public void setEndingTime(int endingTime) {
		this.endsAt = endingTime;
	}

	public int getDuration() {
		return this.endsAt - this.startsAt;
	}
}
