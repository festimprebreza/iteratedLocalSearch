package TripFinderAlgorithm;

import java.awt.*;
import javax.swing.*;

public class Visualizer extends JPanel {
	private static final long serialVersionUID = 1L;
	private Solution currentSolution;
	private String message = "Sample message";
	private String ILSStatus = "Sample message";
	private boolean waiting = false;
	private int currentBestScore = 0;
	private boolean drawingMap = true;
	private Color[] colors = {
		Color.decode("#B2AA8E"),
		Color.decode("#0C1B33"),
		Color.decode("#7A306C"),
		Color.decode("#03B5AA"),
		Color.decode("#DBFE87"),
		Color.decode("#FF5D73"),
		Color.decode("#FFABB7"),
		Color.decode("#B0E9FF"),
		Color.decode("#7FBEEB"),
		Color.decode("#FF4365")};

	private Color[] mapColor = {
		Color.RED,
		Color.GREEN,
		Color.BLUE,
		Color.YELLOW
	};

	public Visualizer(Solution currentSolution) {
		this.currentSolution = currentSolution;
	}

	public void paint(Graphics g) {
		super.paintComponent(g);
		Graphics2D g2 = drawDefault(g);
		if(drawingMap) {
			drawMap(g2);
		}
		else {
			drawTimeline(g2);
		}		
	}

	public Graphics2D drawDefault(Graphics g) {
		Graphics2D g2 = (Graphics2D)g;
		this.setBackground(Color.WHITE);
		if(currentSolution.getScore() > currentBestScore) {
			g2.setColor(Color.RED);
			g2.setFont(new Font("Times New Roman", Font.BOLD, 32));
			g2.drawString("NEW BEST SOLUTION", 550, 50);
		}
		g2.setColor(Color.BLACK);
		g2.setFont(new Font("Times New Roman", Font.PLAIN, 32));
		g2.drawString(String.format("%.2f", (currentSolution.getScore() / 100.0f)), 50, 50);
		g2.drawString(message, 250, 950);
		g2.drawString(ILSStatus, 850, 950);

		return g2;
	}

	public void drawMap(Graphics2D g2) {
		// get extreme coordinates
		long lowestX = currentSolution.getProblemInput().getStartingPOI().getXCoordinate();
		long lowestY = currentSolution.getProblemInput().getStartingPOI().getYCoordinate();
		long highestY = currentSolution.getProblemInput().getStartingPOI().getYCoordinate();
		for(POI currentPOI: currentSolution.getProblemInput().getVisitablePOIs()) {
			if(currentPOI.getXCoordinate() < lowestX) {
				lowestX = currentPOI.getXCoordinate();
			}
			if(currentPOI.getYCoordinate() < lowestY) {
				lowestY = currentPOI.getYCoordinate();
			}
			if(currentPOI.getYCoordinate() > highestY) {
				highestY = currentPOI.getYCoordinate();
			}
		}
		long toShiftX = 0;
		long toShiftY = 0;
		if(lowestX < 0) {
			toShiftX = -lowestX;
		}
		if(lowestY < 0) {
			toShiftY = -lowestY;
		}
		// draw points
		int circleRadius = 10;
		int startingX = 50;
		int startingY = 930;
		g2.setColor(Color.GRAY);
		for(POI currentPOI: currentSolution.getProblemInput().getVisitablePOIs()) {
			int xCoordinate = startingX + (int)((currentPOI.getXCoordinate() + toShiftX) / 100);
			int yCoordinate = startingY - (int)((currentPOI.getYCoordinate() + toShiftY) / 100);
			g2.fillOval(xCoordinate - circleRadius, yCoordinate - circleRadius, circleRadius * 2, circleRadius * 2);
		}
		int xCoordinate = startingX + (int)((currentSolution.getProblemInput().getStartingPOI().getXCoordinate() + toShiftX) / 100);
		int yCoordinate = startingY - (int)((currentSolution.getProblemInput().getStartingPOI().getYCoordinate() + toShiftY) / 100);
		g2.setColor(Color.BLACK);
		g2.fillOval(xCoordinate - circleRadius, yCoordinate - circleRadius, circleRadius * 2, circleRadius * 2);
		// draw lines
		for(int tour = 0; tour < currentSolution.getProblemInput().getTourCount(); tour++) {
			POIInterval currentPOIInterval = currentSolution.getNthPOIIntervalInTourX(0, tour).getPreviousPOIInterval();
			while(currentPOIInterval.getPOI() != currentSolution.getProblemInput().getEndingPOI()) {
				g2.setStroke(new BasicStroke(2.5f));
				g2.setColor(mapColor[tour]);
				int startingXCoord = startingX + (int)((currentPOIInterval.getPOI().getXCoordinate() + toShiftX) / 100);
				int startingYCoord = startingY - (int)((currentPOIInterval.getPOI().getYCoordinate() + toShiftY) / 100);
				int endingXCoord = startingX + (int)((currentPOIInterval.getNextPOIInterval().getPOI().getXCoordinate() + toShiftX) / 100);
				int endingYCoord = startingY - (int)((currentPOIInterval.getNextPOIInterval().getPOI().getYCoordinate() + toShiftY) / 100);

				g2.drawLine(startingXCoord, startingYCoord, endingXCoord, endingYCoord);
				if(currentPOIInterval.getPOI().getID() != 0) {
					g2.setColor(colors[currentPOIInterval.getAssignedType()]);
					g2.fillOval(startingXCoord - circleRadius, startingYCoord - circleRadius, circleRadius * 2, circleRadius * 2);
				}
				currentPOIInterval = currentPOIInterval.getNextPOIInterval();
			}
		}
	}

	public void drawTimeline(Graphics2D g2) {
		// draw patterns
		int patternSize = 20;
		for(int tour = 0; tour < currentSolution.getProblemInput().getTourCount(); tour++) {
			int[] patternForTourX = currentSolution.getProblemInput().getPatternsForTour(tour);
			for(int i = 0; i < patternForTourX.length; i++) {
				g2.setColor(colors[patternForTourX[i]]);
				g2.fillRect(50 + ((patternSize + 3) * i), 870 + ((patternSize + 3) * tour), patternSize, patternSize);
			}
		}

		int startX = 50;
		int startY = 95;
		int height = 100;
		int height2 = 20;

		int tourEnd = (int)(currentSolution.getEndingPOIInterval()[0].getEndingTime() / 100);
		// float pixelUnit = 1.45f;
		float pixelUnit = 1790 / (float)tourEnd;
		// float pixelUnit = 2.5f;

		POIInterval[] startingPOIIntervals = currentSolution.getStartingPOIInterval();
		POIInterval[] endingPOIIntervals = currentSolution.getEndingPOIInterval();
		for(int tour = 0; tour < startingPOIIntervals.length; tour++) {
			POIInterval currentPOIInterval = startingPOIIntervals[tour].getNextPOIInterval();
			while(currentPOIInterval != null) {
				g2.setFont(new Font("Times New Roman", Font.PLAIN, 18));
				int travelX = (int)((currentPOIInterval.getPreviousPOIInterval().getEndingTime() / 100) * pixelUnit) + startX;
				int travelWidth = (int)((currentPOIInterval.getArrivalTime() / 100) * pixelUnit) - travelX + startX;
				// draw travel
				g2.setColor(Color.GREEN);
				g2.fillRect(travelX, startY + (tour * 2 * startY) + (height / 2) - (height2 / 2), travelWidth, height2);
				if(currentPOIInterval.getWaitTime() != 0) {
					int waitX = travelX + travelWidth;
					int waitWidth = (int)((currentPOIInterval.getWaitTime() / 100) * pixelUnit);
					// draw wait
					g2.setColor(new Color(0, 0, 0, 40));
					g2.fillRect(waitX, startY + (tour * 2 * startY) + (height / 2) - (height2 / 2), waitWidth, height2);
				}			
				int POIX = (int)((currentPOIInterval.getStartingTime() / 100) * pixelUnit) + startX;
				int POIEndingX = (int)((currentPOIInterval.getEndingTime() / 100) * pixelUnit) + startX;
				int POIWidth = POIEndingX - POIX;
				int POIY = startY + (tour * 2 * startY);
				// draw POI
				if(currentPOIInterval.getAssignedType() == -1) {
					g2.setColor(Color.ORANGE);
				}
				else {
					g2.setColor(colors[currentPOIInterval.getAssignedType()]);
				}
				g2.fillRect(POIX, POIY, POIWidth, height);

				if(currentPOIInterval != endingPOIIntervals[tour]) {
					g2.setColor(Color.BLACK);
					int IDX = POIX + POIWidth / 2 - g2.getFont().getSize() / 2;
					int IDY = POIY + height / 2 + g2.getFont().getSize() / 2;
					if(currentPOIInterval.getAssignedType() == 1) {
						g2.setColor(Color.WHITE);
					}
					g2.drawString(currentPOIInterval.getPOI().getID() + "", IDX, IDY);
					if(currentPOIInterval.isPivot()) {
						g2.setFont(new Font("Times New Roman", Font.BOLD, 18));
						g2.drawString("PVT", IDX, IDY + g2.getFont().getSize());
					}

					g2.setColor(Color.BLACK);
					g2.setFont(new Font("Times New Roman", Font.PLAIN, 8));
					g2.drawString((currentPOIInterval.getStartingTime() / 100.0f) + "", POIX - 8, POIY + height + 20);
					g2.drawString((currentPOIInterval.getEndingTime() / 100.0f) + "", POIX - 8 + POIWidth, POIY + height + 40);
				}

				currentPOIInterval = currentPOIInterval.getNextPOIInterval();
			}
		}
	}

	public void setWaiting(boolean waiting) {
		this.waiting = waiting;
	}

	public boolean isWaiting() {
		return waiting;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public void setILSStatus(String message) {
		this.ILSStatus = message;
	}

	public void setNewBestScore(int newBest) {
		this.currentBestScore =  newBest;
	}
}