package TripFinderAlgorithm;

public class POI {
	private int ID;
	private float xCoordinate;
	private float yCoordinate;
	private float duration;
	private float score;
	private float openingTime;
	private float closingTime;

	private boolean isAssigned;

	public POI(int ID, float xCoordinate, float yCoordinate, float duration, 
				float score, float openingTime, float closingTime) {
		this.ID = ID;
		this.xCoordinate = xCoordinate;
		this.yCoordinate = yCoordinate;
		this.duration = duration;
		this.score = score;
		this.openingTime = openingTime;
		this.closingTime = closingTime;
	}

	public int getID() {
		return this.ID;
	}

	public float getXCoordinate() {
		return this.xCoordinate;
	}

	public float getYCoordinate() {
		return this.yCoordinate;
	}

	public float getDuration() {
		return this.duration;
	}

	public float getScore() {
		return this.score;
	}

	public float getOpeningTime() {
		return this.openingTime;
	}

	public float getClosingTime() {
		return this.closingTime;
	}

	public boolean isAssigned() {
		return this.isAssigned;
	}

	public void setAssigned() {
		this.isAssigned = true;
	}

	public void unAssign() {
		this.isAssigned = false;
	}

	public float getTravelTimeToPOI(POI nextPOI) {
		return (float)Math.sqrt(Math.pow(this.xCoordinate - nextPOI.getXCoordinate(), 2) - 
						Math.pow(this.yCoordinate - nextPOI.getYCoordinate(), 2));
	}

	@Override
	public String toString() {
		return String.format("ID: %d; X: %.2f, Y: %.2f; Duration: %.2f; Score: %.2f; Opens at %.2f and closes at %.2f.", 
							this.ID, this.xCoordinate, this.yCoordinate, this.duration, this.score, this.openingTime,
							this.closingTime);
	}
}
