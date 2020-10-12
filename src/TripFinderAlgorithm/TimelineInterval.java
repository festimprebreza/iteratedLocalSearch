package TripFinderAlgorithm;

public abstract class TimelineInterval {
	private float startsAt;
	private float endsAt;

	public TimelineInterval(float startingTime, float endingTime) {
		this.startsAt = startingTime;
		this.endsAt = endingTime;
	}

	public float getStartingTime() {
		return this.startsAt;
	}

	public void setStartingTime(float startingTime) {
		this.startsAt = startingTime;
	}

	public float getEndingTime() {
		return this.endsAt;
	}

	public void setEndingTime(float endingTime) {
		this.endsAt = endingTime;
	}

	public float getDuration() {
		return this.endsAt - this.startsAt;
	}
}
