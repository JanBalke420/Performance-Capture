import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.BufferedImage;

import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JSlider;

import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint2f;

class ImageOnePanel extends JComponent {
	BufferedImage sourceImage1;
	Image image1;
	MatOfPoint2f[] sequenceTrackPoints;
	MatOfPoint2f featurePoints;
	Mat[] panelCircles;
	JSlider timelineSlider;
	private double scaleFactor = 1.0;
	private boolean showTrace = true;

	public void setSourceImage1(BufferedImage sourceImage1) {
		this.sourceImage1 = sourceImage1;
	}

	public BufferedImage getSourceImage1() {
		return sourceImage1;
	}

	public void setPanelCircles(Mat[] panelCircles) {
		this.panelCircles = panelCircles;
	}

	public Mat[] getPanelCircles() {
		return panelCircles;
	}

	public void setSequenceTrackPoints(MatOfPoint2f[] points) {
		this.sequenceTrackPoints = points;
	}

	public MatOfPoint2f[] getSequenceTrackPoints() {
		return sequenceTrackPoints;
	}

	public void setFeaturePoints(MatOfPoint2f featurePoints) {
		this.featurePoints = featurePoints;
	}

	public MatOfPoint2f getFeaturePoints() {
		return featurePoints;
	}

	public void setTimelineSlider(JSlider timelineSlider) {
		this.timelineSlider = timelineSlider;
	}

	public JSlider gettimelineSlider() {
		return timelineSlider;
	}

	public void paintComponent(Graphics g) {
		double widthFactor = 0.0; // berechnung eines skalierungs-faktors um das
									// darzustellende Bild dem Fenster
									// anzupassen
		double heightFactor = 0.0;
		widthFactor = (double) (this.getWidth() - 50) / (double) sourceImage1.getWidth();
		heightFactor = (double) (this.getHeight() - 50) / (double) sourceImage1.getHeight();
		if (widthFactor < heightFactor) {
			if (widthFactor < 1) {
				scaleFactor = widthFactor;
			} else {
				scaleFactor = 1.0;
			}
		} else {
			if (heightFactor < 1) {
				scaleFactor = heightFactor;
			} else {
				scaleFactor = 1.0;
			}
		}

		image1 = new ImageIcon(sourceImage1).getImage();

		int markerWidth = 25;
		int markerHeight = 25;
		int handleWidth = 15;
		int handleHeight = 15;

		g.drawImage(image1, 25, 25, (int) (sourceImage1.getWidth() * scaleFactor), (int) (sourceImage1.getHeight() * scaleFactor), this); // das
																																			// bild
																																			// zeichnen

		if (featurePoints != null && sequenceTrackPoints == null) { // zeichnen
																	// der
																	// featurepunkte,
																	// wenn
																	// diese
																	// noch
																	// nicht
																	// getrackt
																	// wurden
			g.setColor(new Color(255, 255, 255));
			for (int i = 0; i < featurePoints.rows(); i++) {
				double[] point = featurePoints.get(i, 0);
				g.drawLine((int) (point[0] * getScaleFactor()) + 25 - markerWidth / 2, (int) (point[1] * getScaleFactor()) + 25, (int) (point[0] * getScaleFactor()) + 25 + markerWidth / 2,
						(int) (point[1] * getScaleFactor()) + 25);
				g.drawLine((int) (point[0] * getScaleFactor()) + 25, (int) (point[1] * getScaleFactor()) + 25 - markerHeight / 2, (int) (point[0] * getScaleFactor()) + 25,
						(int) (point[1] * getScaleFactor()) + 25 + markerHeight / 2);
				g.drawRect((int) (point[0] * getScaleFactor()) + 25 - handleWidth / 2, (int) (point[1] * getScaleFactor()) + 25 - handleHeight / 2, handleWidth, handleHeight);
				g.drawString("" + (i + 1), (int) (int) (point[0] * scaleFactor) + 30, (int) (point[1] * scaleFactor) + 40);
			}
		}

		if (sequenceTrackPoints != null) { // zeichnen der featurepunkte, wenn
											// diese getrackt wurden
			g.setColor(new Color(255, 255, 255));
			for (int i = 1; i < sequenceTrackPoints.length; i++) {
				for (int j = 0; j < sequenceTrackPoints[i].rows(); j++) {

					if (isShowTrace() == true) { // wenn in der GUI der haken
													// bei "show trace" gesetzt
													// ist wird hier die
													// bewegung jedes getrackten
													// punktes gezeichnet
						g.setColor(new Color(0, 255, 0));
						double[] point = sequenceTrackPoints[i].get(j, 0);
						double[] legacyPoint = sequenceTrackPoints[i - 1].get(j, 0);
						g.drawLine((int) (legacyPoint[0] * getScaleFactor()) + 25, (int) (legacyPoint[1] * getScaleFactor()) + 25, (int) (point[0] * getScaleFactor()) + 25,
								(int) (point[1] * getScaleFactor()) + 25);
					}

					g.setColor(new Color(255, 255, 255));
					double[] currentPoint = sequenceTrackPoints[timelineSlider.getValue()].get(j, 0);
					g.drawLine((int) (currentPoint[0] * getScaleFactor()) + 25 - markerWidth / 2, (int) (currentPoint[1] * getScaleFactor()) + 25,
							(int) (currentPoint[0] * getScaleFactor()) + 25 + markerWidth / 2, (int) (currentPoint[1] * getScaleFactor()) + 25);
					g.drawLine((int) (currentPoint[0] * getScaleFactor()) + 25, (int) (currentPoint[1] * getScaleFactor()) + 25 - markerHeight / 2, (int) (currentPoint[0] * getScaleFactor()) + 25,
							(int) (currentPoint[1] * getScaleFactor()) + 25 + markerHeight / 2);
					g.drawRect((int) (currentPoint[0] * getScaleFactor()) + 25 - handleWidth / 2, (int) (currentPoint[1] * getScaleFactor()) + 25 - handleHeight / 2, handleWidth, handleHeight);
					g.drawString("" + (j + 1), (int) (currentPoint[0] * getScaleFactor()) + 30, (int) (currentPoint[1] * getScaleFactor()) + 40);
				}
			}
		}
	}

	public double getScaleFactor() {
		return scaleFactor;
	}

	public void setScaleFactor(double scaleFactor) {
		this.scaleFactor = scaleFactor;
	}

	public boolean isShowTrace() {
		return showTrace;
	}

	public void setShowTrace(boolean showTrace) {
		this.showTrace = showTrace;
	}
}