import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JPanel;
import javax.swing.JTextField;

import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfFloat;
import org.opencv.core.MatOfInt;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

public class HistogramPanel extends JPanel {

	public static final int HistType_RGB = 0;
	public static final int HistType_BW = 1;

	private Mat histogramMat;

	private Point blackInPointIni = new Point(5, 101);
	private Point whiteInPointIni = new Point(260, 101);

	private Point blackInPoint = new Point(5, 101);
	private boolean blackInPointSelected = false;
	private Point whiteInPoint = new Point(260, 101);
	private boolean whiteInPointSelected = false;

	JTextField blackInField = new JTextField();
	JTextField whiteInField = new JTextField();

	public void paintComponent(Graphics g) {
		if (histogramMat != null) {
			blackInField.setEnabled(true);
			whiteInField.setEnabled(true);
			BufferedImage histogramImage = Mat2BufferedImage(histogramMat); // zeichnen
																			// des
																			// Histogramms
			g.drawImage(histogramImage, 5, 0, this);
			g.setColor(new Color(0, 0, 0));
			g.fillPolygon(
					new int[] { blackInPoint.getPosX(), blackInPoint.getPosX() + 5, blackInPoint.getPosX() + 5,
							blackInPoint.getPosX() - 5, blackInPoint.getPosX() - 5 },
					new int[] { blackInPoint.getPosY(), blackInPoint.getPosY() + 5, blackInPoint.getPosY() + 10,
							blackInPoint.getPosY() + 10, blackInPoint.getPosY() + 5 },
					5); // zeichnen
						// der
						// histogrammregler
			g.setColor(new Color(255, 255, 255));
			g.fillPolygon(
					new int[] { whiteInPoint.getPosX(), whiteInPoint.getPosX() + 5, whiteInPoint.getPosX() + 5,
							whiteInPoint.getPosX() - 5, whiteInPoint.getPosX() - 5 },
					new int[] { whiteInPoint.getPosY(), whiteInPoint.getPosY() + 5, whiteInPoint.getPosY() + 10,
							whiteInPoint.getPosY() + 10, whiteInPoint.getPosY() + 5 },
					5);
		} else {
			blackInField.setEnabled(false);
			whiteInField.setEnabled(false);
			g.setColor(new Color(0, 0, 0));
			g.fillRect(5, 0, 255, 100);
		}
	}

	public Mat getHistogrammMat() {
		return histogramMat;
	}

	public void setHistogrammMat(Mat image, int histType) { // erstellen eines
															// Histogrammbildes
															// zur darstellung
															// des Histogramms
		ArrayList<Mat> imageList = new ArrayList<Mat>();
		imageList.add(image);
		Mat mask = new Mat();
		MatOfInt histSize = new MatOfInt(256);
		MatOfFloat ranges = new MatOfFloat();
		ranges.push_back(new Mat(1, 0, CvType.CV_32F, new Scalar(0.0)));
		ranges.push_back(new Mat(1, 0, CvType.CV_32F, new Scalar(256.0)));
		double max = 0.0;
		if (histType == 0) {
			Mat histR = new Mat();
			Mat histG = new Mat();
			Mat histB = new Mat();
			Imgproc.calcHist(imageList, new MatOfInt(2), mask, histR, histSize, ranges); // berechnung
																							// der
																							// Histogramms
																							// für
																							// Rot-Kanal
			Imgproc.calcHist(imageList, new MatOfInt(1), mask, histG, histSize, ranges); // berechnung
																							// der
																							// Histogramms
																							// für
																							// Grün-Kanal
			Imgproc.calcHist(imageList, new MatOfInt(0), mask, histB, histSize, ranges); // berechnung
																							// der
																							// Histogramms
																							// für
																							// Blau-Kanal
			histogramMat = new Mat(100, 256, CvType.CV_8UC3);
			for (int i = 0; i < histR.rows(); i++) {
				if (histR.get(i, 0)[0] > max) {
					max = histR.get(i, 0)[0];
				}
				if (histG.get(i, 0)[0] > max) {
					max = histG.get(i, 0)[0];
				}
				if (histB.get(i, 0)[0] > max) {
					max = histB.get(i, 0)[0];
				}
			}
			for (int x = 0; x < histogramMat.cols(); x++) { // "bemalen" einer
															// Matrix mit der
															// Abbildung des
															// berechneten
															// Histogramms
				for (int y = 0; y < histogramMat.rows(); y++) {
					double[] rgbValue = new double[3];
					if (y < histR.get(x, 0)[0] / max * 100) {
						rgbValue[2] = 255.0;
					} else {
						rgbValue[2] = 0.0;
					}
					if (y < histG.get(x, 0)[0] / max * 100) {
						rgbValue[1] = 255.0;
					} else {
						rgbValue[1] = 0.0;
					}
					if (y < histB.get(x, 0)[0] / max * 100) {
						rgbValue[0] = 255.0;
					} else {
						rgbValue[0] = 0.0;
					}
					histogramMat.put(99 - y, x, rgbValue);
				}
			}
		} else { // das gleiche wie im anderen teil der if-abfrage, nur für
					// einen farbkanal, anstatt für 3...
			Mat histAll = new Mat();
			Imgproc.calcHist(imageList, new MatOfInt(0), mask, histAll, histSize, ranges);
			histogramMat = new Mat(100, 256, CvType.CV_8UC1);
			for (int i = 0; i < histAll.rows(); i++) {
				if (histAll.get(i, 0)[0] > max) {
					max = histAll.get(i, 0)[0];
				}
			}
			for (int x = 0; x < histogramMat.cols(); x++) {
				for (int y = 0; y < histogramMat.rows(); y++) {
					double[] rgbValue = new double[3];
					if (y < histAll.get(x, 0)[0] / max * 100) {
						histogramMat.put(99 - y, x, 255.0);
					} else {
						histogramMat.put(99 - y, x, 0.0);
					}
				}
			}
		}

	}

	public BufferedImage Mat2BufferedImage(Mat imgMat) { // Übernommen von Prof.
															// Dr.-Ing. Karsten
															// Lehn
		int bufferedImageType = 0;
		switch (imgMat.channels()) {
		case 1:
			bufferedImageType = BufferedImage.TYPE_BYTE_GRAY;
			break;
		case 3:
			bufferedImageType = BufferedImage.TYPE_3BYTE_BGR;
			break;
		default:
			throw new IllegalArgumentException(
					"Unknown matrix type. Only one byte per pixel (one channel) or three bytes pre pixel (three channels) are allowed.");
		}
		BufferedImage bufferedImage = new BufferedImage(imgMat.cols(), imgMat.rows(), bufferedImageType);
		final byte[] bufferedImageBuffer = ((DataBufferByte) bufferedImage.getRaster().getDataBuffer()).getData();
		imgMat.get(0, 0, bufferedImageBuffer);
		return bufferedImage;
	}

	public Point getBlackInPoint() {
		return blackInPoint;
	}

	public void setBlackInPoint(Point blackInPoint) {
		this.blackInPoint = blackInPoint;
	}

	public Point getWhiteInPoint() {
		return whiteInPoint;
	}

	public void setWhiteInPoint(Point whiteInPoint) {
		this.whiteInPoint = whiteInPoint;
	}

	public boolean isBlackInPointSelected() {
		return blackInPointSelected;
	}

	public void setBlackInPointSelected(boolean blackInPointSelected) {
		this.blackInPointSelected = blackInPointSelected;
	}

	public boolean isWhiteInPointSelected() {
		return whiteInPointSelected;
	}

	public void setWhiteInPointSelected(boolean whiteInPointSelected) {
		this.whiteInPointSelected = whiteInPointSelected;
	}
}
