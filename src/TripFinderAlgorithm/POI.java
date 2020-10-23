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
	private int E1Value;
	private int E2Value;

	private boolean isAssigned;
	private int[] lastRemovedIteration;

	public POI(int ID, long xCoordinate, long yCoordinate, int duration, int score, int openingTime, 
				int closingTime, int E1Value, int E2Value) {
		this.ID = ID;
		this.xCoordinate = xCoordinate;
		this.yCoordinate = yCoordinate;
		this.duration = duration;
		this.score = score;
		this.openingTime = openingTime;
		this.closingTime = closingTime;
		this.E1Value = E1Value;
		this.E2Value = E2Value;
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
	
	public int getE1Value() {
		return this.E1Value;
	}

	public int getE2Value() {
		return this.E2Value;
	}

	public boolean isAssigned() {
		return this.isAssigned;
	}

	public void setAssigned(boolean isAssigned) {
		this.isAssigned = isAssigned;
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

	public void createTabuInfo(int tourCount) {
		lastRemovedIteration = new int[tourCount];
		for(int tour = 0; tour < lastRemovedIteration.length; tour++) {
			lastRemovedIteration[tour] = -3;
		}
	}

	public int getLastRemovedIteration(int tour) {
		return lastRemovedIteration[tour];
	}

	public void updateLastRemovedIteration(int currentIteration, int tour) {
		this.lastRemovedIteration[tour] = currentIteration;
	}

	@Override
	public String toString() {
		return String.format("ID: %d; X: %.3f, Y: %.3f; Duration: %.2f; Score: %.2f; Opens at %.2f; Closes at %.2f; " +
							"E1 value: %.2f; E2 value: %.2f", 
							this.ID, this.xCoordinate / 1000.0f, this.yCoordinate / 1000.0f, this.duration / 100.0f, 
							this.score / 100.0f, this.openingTime / 100.0f, this.closingTime / 100.0f,
							this.E1Value / 100.0f, this.E2Value / 100.0f);
	}

	@Override
	public Object clone() throws CloneNotSupportedException {
		POI clonedPOI = (POI)super.clone();
		return clonedPOI;
	}
}
