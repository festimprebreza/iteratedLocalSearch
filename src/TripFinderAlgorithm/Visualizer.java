package TripFinderAlgorithm;

import java.awt.*;
import javax.swing.*;

public class Visualizer extends JPanel {
	private static final long serialVersionUID = 1L;
	private Solution currentSolution;
	private String message = "Sample message";
	private boolean waiting = false;
	
	public Visualizer(Solution currentSolution) {
		this.currentSolution = currentSolution;
	}

	public void paint(Graphics g) {
		super.paintComponent(g);
		Graphics2D g2 = (Graphics2D)g;
		this.setBackground(Color.WHITE);
		g2.setColor(Color.BLACK);
		g2.setFont(new Font("Times New Roman", Font.PLAIN, 32));
		g2.drawString(String.format("%.2f", (currentSolution.getScore() / 100.0f)), 50, 50);
		g2.drawString(message, 50, 850);

		int startX = 50;
		int startY = 100;
		int height = 100;
		int height2 = 20;

		float pixelUnit = 1.45f;
		// float pixelUnit = 8f;

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
				g2.setColor(Color.orange);
				g2.fillRect(POIX, POIY, POIWidth, height);

				if(currentPOIInterval != endingPOIIntervals[tour]) {
					g2.setColor(Color.BLACK);
					int IDX = POIX + height / 2 - g2.getFont().getSize() / 2;
					int IDY = POIY + height / 2 + g2.getFont().getSize() / 2;
					g2.drawString(currentPOIInterval.getPOI().getID() + "", IDX, IDY);

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
}
