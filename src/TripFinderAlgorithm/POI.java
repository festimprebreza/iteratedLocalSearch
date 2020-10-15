package TripFinderAlgorithm;

import java.util.HashMap;

public class POI implements Cloneable {
	private int ID;
	private long xCoordinate;
	private long yCoordinate;
	private int duration;
	private int score;
	private int openingTime;
	private int closingTime;
	private HashMap<Integer, Integer> travelDistances;

	private boolean isAssigned;

	public POI(int ID, long xCoordinate, long yCoordinate, int duration, 
				int score, int openingTime, int closingTime) {
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

	public long getXCoordinate() {
		return this.xCoordinate;
	}

	public long getYCoordinate() {
		return this.yCoordinate;
	}

	public int getDuration() {
		return this.duration;
	}

	public int getScore() {
		return this.score;
	}

	public int getOpeningTime() {
		return this.openingTime;
	}

	public int getClosingTime() {
		return this.closingTime;
	}

	public boolean isAssigned() {
		return this.isAssigned;
	}

	public void setAssigned() {
		this.isAssigned = true;
	}

	public void unassign() {
		this.isAssigned = false;
	}

	public void setTravelDistances(HashMap<Integer, Integer> travelDistances) {
		this.travelDistances = travelDistances;
	}

	public int getTravelTimeToPOI(int toPOIID) {
		if(this.ID == toPOIID) {
			return 0;
		}
		return travelDistances.get(toPOIID);
	}

	@Override
	public String toString() {
		return String.format("ID: %d; X: %.3f, Y: %.3f; Duration: %.2f; Score: %.2f; Opens at %.2f and closes at %.2f.", 
							this.ID, this.xCoordinate / 1000.0f, this.yCoordinate / 1000.0f, this.duration / 100.0f, 
							this.score / 100.0f, this.openingTime / 100.0f, this.closingTime / 100.0f);
	}

	@Override
	public Object clone() throws CloneNotSupportedException {
		POI clonedPOI = (POI)super.clone();
		return clonedPOI;
	}
}
