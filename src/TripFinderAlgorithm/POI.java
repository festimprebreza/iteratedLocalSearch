package TripFinderAlgorithm;

import java.util.ArrayList;
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
	private int entranceFee;
	private boolean[] typeBitArray;
	private ArrayList<Integer> types;

	private boolean isAssigned;

	public POI(int ID, long xCoordinate, long yCoordinate, int duration, int score, int openingTime, 
				int closingTime, int entranceFee, boolean[] typeBitArray) {
		this.ID = ID;
		this.xCoordinate = xCoordinate;
		this.yCoordinate = yCoordinate;
		this.duration = duration;
		this.score = score;
		this.openingTime = openingTime;
		this.closingTime = closingTime;
		this.entranceFee = entranceFee;
		this.typeBitArray = typeBitArray;

		types = new ArrayList<>();
		for(int type = 0; type < typeBitArray.length; type++) {
			if(typeBitArray[type]) {
				types.add(type);
			}
		}
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

	public int getEntranceFee() {
		return this.entranceFee;
	}

	public ArrayList<Integer> getTypes() {
		return this.types;
	}

	public boolean isOfType(int type) {
		return typeBitArray[type];
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

	@Override
	public String toString() {
		String container = String.format("ID: %d; X: %.3f, Y: %.3f; Duration: %.2f; Score: %.2f; Opens at %.2f; Closes at %.2f; " +
							"Entrance fee: %.2f; Types: ", 
							this.ID, this.xCoordinate / 1000.0f, this.yCoordinate / 1000.0f, this.duration / 100.0f, 
							this.score / 100.0f, this.openingTime / 100.0f, this.closingTime / 100.0f,
							this.entranceFee / 100.0f);
		for(int index = 0; index < types.size(); index++) {
			container += types.get(index);
			if(index != types.size() - 1) {
				container += ", ";
			}
		}
		container += ".";
		return container;
	}

	@Override
	public Object clone() throws CloneNotSupportedException {
		POI clonedPOI = (POI)super.clone();
		return clonedPOI;
	}
}
