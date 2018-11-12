import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyVetoException;
import java.beans.VetoableChangeListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;

import javax.imageio.ImageIO;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.border.Border;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.MatOfFloat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.video.Video;

public class GUI extends JFrame {

	// JFrame fenster;

	JButton openISButton;
	JButton openProject;
	JButton save;
	JButton export;

	JCheckBox rgbHistCheck;
	JCheckBox traceCheck;

	JButton setPointsButton;
	JButton trackFeaturesInSequenceButton;
	JButton trackForwardButton;
	JButton clearButton;
	JButton show3DButton;

	JSlider timelineSlider;

	JPanel panel = new JPanel();
	JPanel panel2 = new JPanel();

	ImageOnePanel imgPanel;
	HistogramPanel histogramPanel = new HistogramPanel();

	BufferedImage sourceImage1 = new BufferedImage(1, 1, 5);

	MatOfPoint2f featurePoints;

	Mat[] imageSequence;
	Mat[] imageSequenceOriginal;
	MatOfPoint2f[] sequenceTrackPoints;
	int sequenceLength;

	int indexOfSelectedPoint = -1;

	Mat[] circles;

	ArrayList<String> pointNames;
	ArrayList<JLabel> pointNameLabels;

	private String openPath = "";
	private String savePath = "";
	private String lastDir = "";

	public GUI() {
		setTitle("FaceTune");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		panel.setBackground(new Color(83, 83, 83));
		panel2.setBackground(new Color(75, 75, 75));

		sourceImage1.setRGB(0, 0, new Color(83, 83, 83).getRGB());

		panel.setLayout(null);
		panel2.setLayout(new BoxLayout(panel2, BoxLayout.Y_AXIS));

		openISButton = new JButton("open imagesequence");
		openISButton.addActionListener(new OpenISListener());
		openISButton.setBounds(40, 50, 225, 25);
		openISButton.setBorderPainted(false);
		openISButton.setFocusPainted(false);
		openISButton.setContentAreaFilled(true);
		openISButton.setBackground(new Color(65, 65, 65));
		openISButton.setForeground(new Color(255, 255, 255));

		rgbHistCheck = new JCheckBox();
		rgbHistCheck.setText("RGB-Histogramm");
		rgbHistCheck.addActionListener(new RGBCheckListener());
		rgbHistCheck.setSelected(true);
		rgbHistCheck.setBounds(25, 100, 200, 25);
		rgbHistCheck.setBackground(new Color(83, 83, 83));
		rgbHistCheck.setForeground(new Color(255, 255, 255));
		rgbHistCheck.setEnabled(false);

		histogramPanel.setLayout(null);
		histogramPanel.setBounds(20, 125, 265, 150);
		histogramPanel.addMouseListener(new HistListener());
		histogramPanel.addMouseMotionListener(new MoveHistListener());
		histogramPanel.blackInField.setBounds(5, 115, 50, 25);
		histogramPanel.blackInField.setText("0");
		histogramPanel.whiteInField.setBounds(210, 115, 50, 25);
		histogramPanel.whiteInField.setText("255");
		histogramPanel.add(histogramPanel.blackInField);
		histogramPanel.add(histogramPanel.whiteInField);

		setPointsButton = new JButton("overlay points");
		setPointsButton.addActionListener(new OverlayPointsListener());
		setPointsButton.setBounds(40, 300, 225, 25);
		setPointsButton.setBorderPainted(false);
		setPointsButton.setFocusPainted(false);
		setPointsButton.setContentAreaFilled(true);
		setPointsButton.setBackground(new Color(65, 65, 65));
		setPointsButton.setForeground(new Color(255, 255, 255));
		setPointsButton.setEnabled(false);

		traceCheck = new JCheckBox();
		traceCheck.setText("show tracing");
		traceCheck.addActionListener(new TraceListener());
		traceCheck.setSelected(true);
		traceCheck.setBounds(25, 350, 200, 25);
		traceCheck.setBackground(new Color(83, 83, 83));
		traceCheck.setForeground(new Color(255, 255, 255));
		traceCheck.setEnabled(false);

		trackFeaturesInSequenceButton = new JButton("track features in sequence");
		trackFeaturesInSequenceButton.addActionListener(new TrackSequenceListener());
		trackFeaturesInSequenceButton.setBounds(40, 400, 225, 25);
		trackFeaturesInSequenceButton.setBorderPainted(false);
		trackFeaturesInSequenceButton.setFocusPainted(false);
		trackFeaturesInSequenceButton.setContentAreaFilled(true);
		trackFeaturesInSequenceButton.setBackground(new Color(65, 65, 65));
		trackFeaturesInSequenceButton.setForeground(new Color(255, 255, 255));
		trackFeaturesInSequenceButton.setEnabled(false);

		trackForwardButton = new JButton("track forward from here");
		trackForwardButton.addActionListener(new TrackForwardListener());
		trackForwardButton.setBounds(40, 450, 225, 25);
		trackForwardButton.setBorderPainted(false);
		trackForwardButton.setFocusPainted(false);
		trackForwardButton.setContentAreaFilled(true);
		trackForwardButton.setBackground(new Color(65, 65, 65));
		trackForwardButton.setForeground(new Color(255, 255, 255));
		trackForwardButton.setEnabled(false);
		trackForwardButton.setEnabled(false);

		clearButton = new JButton("clear points");
		clearButton.addActionListener(new ClearListener());
		clearButton.setBounds(40, 500, 225, 25);
		clearButton.setBorderPainted(false);
		clearButton.setFocusPainted(false);
		clearButton.setContentAreaFilled(true);
		clearButton.setBackground(new Color(65, 65, 65));
		clearButton.setForeground(new Color(255, 255, 255));
		clearButton.setEnabled(false);

		show3DButton = new JButton("animate 3D face");
		show3DButton.addActionListener(new Show3DListener());
		show3DButton.setBounds(40, 550, 225, 25);
		show3DButton.setBorderPainted(false);
		show3DButton.setFocusPainted(false);
		show3DButton.setContentAreaFilled(true);
		show3DButton.setBackground(new Color(65, 65, 65));
		show3DButton.setForeground(new Color(255, 255, 255));
		show3DButton.setEnabled(false);

		openProject = new JButton("open existing tracking-data");
		openProject.addActionListener(new OpenProjectListener());
		openProject.setBounds(40, 700, 225, 25);
		openProject.setBorderPainted(false);
		openProject.setFocusPainted(false);
		openProject.setContentAreaFilled(true);
		openProject.setBackground(new Color(65, 65, 65));
		openProject.setForeground(new Color(255, 255, 255));

		save = new JButton("save tracking-data");
		save.addActionListener(new SaveListener());
		save.setBounds(40, 750, 225, 25);
		save.setBorderPainted(false);
		save.setFocusPainted(false);
		save.setContentAreaFilled(true);
		save.setBackground(new Color(65, 65, 65));
		save.setForeground(new Color(255, 255, 255));
		save.setEnabled(false);

		export = new JButton("export for Autodesk Maya");
		export.addActionListener(new ExportListener());
		export.setBounds(40, 800, 225, 25);
		export.setBorderPainted(false);
		export.setFocusPainted(false);
		export.setContentAreaFilled(true);
		export.setBackground(new Color(65, 65, 65));
		export.setForeground(new Color(255, 255, 255));
		export.setEnabled(false);

		imgPanel = new ImageOnePanel();
		imgPanel.setSourceImage1(sourceImage1);

		panel.add(openISButton);
		panel.add(rgbHistCheck);
		panel.add(histogramPanel);
		panel.add(setPointsButton);
		panel.add(traceCheck);
		panel.add(trackFeaturesInSequenceButton);
		panel.add(trackForwardButton);
		panel.add(clearButton);
		panel.add(show3DButton);
		panel.add(openProject);
		panel.add(save);
		panel.add(export);
		panel.setPreferredSize(new Dimension(315, 300));

		timelineSlider = new JSlider();
		timelineSlider.addChangeListener(new TimeSliderListener());
		timelineSlider.setMinorTickSpacing(0);
		timelineSlider.setMajorTickSpacing(0);
		timelineSlider.setPaintTicks(true);
		timelineSlider.setPaintLabels(false);
		timelineSlider.setBounds(0, 0, 500, 25);
		timelineSlider.setLabelTable(timelineSlider.createStandardLabels(255));
		timelineSlider.setBackground(new Color(75, 75, 75));
		timelineSlider.setForeground(Color.white);
		timelineSlider.setEnabled(false);

		imgPanel.setTimelineSlider(timelineSlider);

		panel2.add(Box.createVerticalStrut(1));
		panel2.add(imgPanel);
		panel2.add(timelineSlider);
		panel2.addMouseListener(new SetPointListener());
		panel2.addMouseMotionListener(new MovePointListener());

		getContentPane().add(BorderLayout.WEST, panel);
		getContentPane().add(BorderLayout.CENTER, panel2);

		setSize(2560, 1440);
		setMinimumSize(new Dimension(1020, 800));
		setLocationRelativeTo(null);
		setVisible(true);
	}

	class TraceListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent arg0) { // bestimmen ob der pfad
														// von getrackten
														// punkten angezeigt
														// werden soll oder
														// nicht
			if (traceCheck.isSelected() == true) {
				imgPanel.setShowTrace(true);
			} else {
				imgPanel.setShowTrace(false);
			}
			repaint();
		}
	}

	class Show3DListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {// Erstellen einer neuen
													// 3D-Animation anhand der
													// getrackten Daten
			DynamicSurface animation = new DynamicSurface(sequenceTrackPoints);
		}
	}

	class OverlayPointsListener implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent arg0) { // Initialisierung eines
														// festen Layouts f�r
														// Trackpunkte

			// Da die festen Koordinaten einer beispielhaften Anordnung
			// entspringen wird im folgenden ein skalierungsfaktor berechnet um
			// die punkte dem aktuell geladenen bild anzupassen

			double scaleFactor = 0.0;
			int width = 1080;
			int height = 1920;
			int offsetX = 0;
			int offsetY = 0;
			double widthFactor = 0.0;
			double heightFactor = 0.0;
			widthFactor = (double) imageSequenceOriginal[0].cols() / (double) width;
			heightFactor = (double) imageSequenceOriginal[0].rows() / (double) height;
			System.out.print("widthF: " + widthFactor + ", heightF: " + heightFactor);
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

			featurePoints = new MatOfPoint2f();
			MatOfPoint2f fp;
			fp = new MatOfPoint2f(new Point(535 * scaleFactor, 351 * scaleFactor));
			featurePoints.push_back(fp);
			fp = new MatOfPoint2f(new Point(166 * scaleFactor, 441 * scaleFactor));
			featurePoints.push_back(fp);
			fp = new MatOfPoint2f(new Point(917 * scaleFactor, 438 * scaleFactor));
			featurePoints.push_back(fp);
			fp = new MatOfPoint2f(new Point(352 * scaleFactor, 517 * scaleFactor));
			featurePoints.push_back(fp);
			fp = new MatOfPoint2f(new Point(676 * scaleFactor, 526 * scaleFactor));
			featurePoints.push_back(fp);
			fp = new MatOfPoint2f(new Point(253 * scaleFactor, 674 * scaleFactor));
			featurePoints.push_back(fp);
			fp = new MatOfPoint2f(new Point(395 * scaleFactor, 677 * scaleFactor));
			featurePoints.push_back(fp);
			fp = new MatOfPoint2f(new Point(674 * scaleFactor, 678 * scaleFactor));
			featurePoints.push_back(fp);
			fp = new MatOfPoint2f(new Point(827 * scaleFactor, 674 * scaleFactor));
			featurePoints.push_back(fp);
			fp = new MatOfPoint2f(new Point(339 * scaleFactor, 764 * scaleFactor));
			featurePoints.push_back(fp);
			fp = new MatOfPoint2f(new Point(732 * scaleFactor, 764 * scaleFactor));
			featurePoints.push_back(fp);
			fp = new MatOfPoint2f(new Point(339 * scaleFactor, 829 * scaleFactor));
			featurePoints.push_back(fp);
			fp = new MatOfPoint2f(new Point(732 * scaleFactor, 829 * scaleFactor));
			featurePoints.push_back(fp);
			fp = new MatOfPoint2f(new Point(243 * scaleFactor, 869 * scaleFactor));
			featurePoints.push_back(fp);
			fp = new MatOfPoint2f(new Point(440 * scaleFactor, 892 * scaleFactor));
			featurePoints.push_back(fp);
			fp = new MatOfPoint2f(new Point(536 * scaleFactor, 907 * scaleFactor));
			featurePoints.push_back(fp);
			fp = new MatOfPoint2f(new Point(631 * scaleFactor, 894 * scaleFactor));
			featurePoints.push_back(fp);
			fp = new MatOfPoint2f(new Point(828 * scaleFactor, 870 * scaleFactor));
			featurePoints.push_back(fp);
			fp = new MatOfPoint2f(new Point(339 * scaleFactor, 920 * scaleFactor));
			featurePoints.push_back(fp);
			fp = new MatOfPoint2f(new Point(736 * scaleFactor, 922 * scaleFactor));
			featurePoints.push_back(fp);
			fp = new MatOfPoint2f(new Point(238 * scaleFactor, 988 * scaleFactor));
			featurePoints.push_back(fp);
			fp = new MatOfPoint2f(new Point(850 * scaleFactor, 985 * scaleFactor));
			featurePoints.push_back(fp);
			fp = new MatOfPoint2f(new Point(458 * scaleFactor, 1091 * scaleFactor));
			featurePoints.push_back(fp);
			fp = new MatOfPoint2f(new Point(537 * scaleFactor, 1089 * scaleFactor));
			featurePoints.push_back(fp);
			fp = new MatOfPoint2f(new Point(610 * scaleFactor, 1091 * scaleFactor));
			featurePoints.push_back(fp);
			fp = new MatOfPoint2f(new Point(420 * scaleFactor, 1137 * scaleFactor));
			featurePoints.push_back(fp);
			fp = new MatOfPoint2f(new Point(645 * scaleFactor, 1135 * scaleFactor));
			featurePoints.push_back(fp);
			fp = new MatOfPoint2f(new Point(294 * scaleFactor, 1272 * scaleFactor));
			featurePoints.push_back(fp);
			fp = new MatOfPoint2f(new Point(383 * scaleFactor, 1286 * scaleFactor));
			featurePoints.push_back(fp);
			fp = new MatOfPoint2f(new Point(436 * scaleFactor, 1244 * scaleFactor));
			featurePoints.push_back(fp);
			fp = new MatOfPoint2f(new Point(533 * scaleFactor, 1237 * scaleFactor));
			featurePoints.push_back(fp);
			fp = new MatOfPoint2f(new Point(629 * scaleFactor, 1245 * scaleFactor));
			featurePoints.push_back(fp);
			fp = new MatOfPoint2f(new Point(694 * scaleFactor, 1290 * scaleFactor));
			featurePoints.push_back(fp);
			fp = new MatOfPoint2f(new Point(781 * scaleFactor, 1278 * scaleFactor));
			featurePoints.push_back(fp);
			fp = new MatOfPoint2f(new Point(450 * scaleFactor, 1330 * scaleFactor));
			featurePoints.push_back(fp);
			fp = new MatOfPoint2f(new Point(535 * scaleFactor, 1343 * scaleFactor));
			featurePoints.push_back(fp);
			fp = new MatOfPoint2f(new Point(627 * scaleFactor, 1327 * scaleFactor));
			featurePoints.push_back(fp);
			fp = new MatOfPoint2f(new Point(534 * scaleFactor, 1426 * scaleFactor));
			featurePoints.push_back(fp);
			fp = new MatOfPoint2f(new Point(425 * scaleFactor, 1485 * scaleFactor));
			featurePoints.push_back(fp);
			fp = new MatOfPoint2f(new Point(642 * scaleFactor, 1485 * scaleFactor));
			featurePoints.push_back(fp);
			System.out.println("points set...");
			imgPanel.setFeaturePoints(featurePoints);
			trackFeaturesInSequenceButton.setEnabled(true);
			clearButton.setEnabled(true);
			repaint();
		}

	}

	class RGBCheckListener implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {// Aktualisierung des
													// dargstellten Histogramms,
													// nachdem man zwischen
													// Grauwert- und
													// RGB-Histogramm gew�hlt
													// hat
			timelineSlider.setValue(timelineSlider.getValue() + 1);
			timelineSlider.setValue(timelineSlider.getValue() - 1);
		}
	}

	class HistListener implements MouseListener {

		@Override
		public void mouseClicked(MouseEvent click) {
			// TODO Auto-generated method stub

		}

		@Override
		public void mouseEntered(MouseEvent click) {
			// TODO Auto-generated method stub

		}

		@Override
		public void mouseExited(MouseEvent click) {
			// TODO Auto-generated method stub

		}

		@Override
		public void mousePressed(MouseEvent click) {// wenn die maustaste
													// gedr�ckt wird, wird
													// gepr�ft ob sich der
													// courser �ber einem der
													// Histogrammregler
													// befindet, wenn das der
													// fall ist wird eine
													// entsprechend
													// indentifizierende
													// variable auf true gesetzt
			if (click.getX() <= histogramPanel.getWhiteInPoint().getPosX() + 5 && click.getX() >= histogramPanel.getWhiteInPoint().getPosX() - 5
					&& click.getY() <= histogramPanel.getWhiteInPoint().getPosY() + 10 && click.getY() >= histogramPanel.getWhiteInPoint().getPosY()) {
				histogramPanel.setWhiteInPointSelected(true);
			} else if (click.getX() <= histogramPanel.getBlackInPoint().getPosX() + 5 && click.getX() >= histogramPanel.getBlackInPoint().getPosX() - 5
					&& click.getY() <= histogramPanel.getBlackInPoint().getPosY() + 10 && click.getY() >= histogramPanel.getBlackInPoint().getPosY()) {
				histogramPanel.setBlackInPointSelected(true);
			}

		}

		@Override
		public void mouseReleased(MouseEvent click) {// wird die maustaste
														// losgelassen wird die
														// variable wieder auf
														// false gestellt
			histogramPanel.setBlackInPointSelected(false);
			histogramPanel.setWhiteInPointSelected(false);
		}

	}

	class MoveHistListener implements MouseMotionListener {

		@Override
		public void mouseDragged(MouseEvent e) {// wenn einer der
												// Histogrammregler bewegt wird
												// werden die entsprechenden
												// GUI-Elemente aktualisiert und
												// die entsprechende
												// Grauwertspreitung auf das
												// aktuell dargestellte Bild
												// angewendet
			if (histogramPanel.isBlackInPointSelected()) {// pr�fen ob
															// blackInPoint
															// ausgew�hlt
															// wurde
				if (e.getX() < histogramPanel.getWhiteInPoint().getPosX() && e.getX() >= 5) {// pr�fen
																								// ob
																								// der
																								// courser
																								// sich
																								// zwischen
																								// minimum
																								// und
																								// anderem
																								// regler
																								// befindet
					histogramPanel.getBlackInPoint().setPosX(e.getX());
					histogramPanel.blackInField.setText(Integer.toString(histogramPanel.getBlackInPoint().getPosX() - 5));
					imageSequence[timelineSlider.getValue()] = increaseLevels(imageSequenceOriginal[timelineSlider.getValue()], Integer.parseInt(histogramPanel.blackInField.getText()),
							Integer.parseInt(histogramPanel.whiteInField.getText()));
					timelineSlider.setValue(timelineSlider.getValue() + 1);
					timelineSlider.setValue(timelineSlider.getValue() - 1);
				}
				repaint();
			}
			if (histogramPanel.isWhiteInPointSelected()) { // pr�fen ob
															// whiteInPoint
															// ausgew�hlt
															// wurde
				if (e.getX() <= 260 && e.getX() > histogramPanel.getBlackInPoint().getPosX()) {// pr�fen
																								// ob
																								// der
																								// courser
																								// sich
																								// zwischen
																								// maximum
																								// und
																								// anderem
																								// regler
																								// befindet
					histogramPanel.getWhiteInPoint().setPosX(e.getX());
					histogramPanel.whiteInField.setText(Integer.toString(histogramPanel.getWhiteInPoint().getPosX() - 5));
					imageSequence[timelineSlider.getValue()] = increaseLevels(imageSequenceOriginal[timelineSlider.getValue()], Integer.parseInt(histogramPanel.blackInField.getText()),
							Integer.parseInt(histogramPanel.whiteInField.getText()));
					timelineSlider.setValue(timelineSlider.getValue() + 1);
					timelineSlider.setValue(timelineSlider.getValue() - 1);
				}
				repaint();
			}
		}

		@Override
		public void mouseMoved(MouseEvent move) {
		}
	}

	public static Mat increaseLevels(Mat source, int blackLevel, int whiteLevel) {// Methode
																					// zur
																					// Grauwertspreizung
																					// mit
																					// frei
																					// w�hlbarem
																					// Minimum
																					// und
																					// Maximum
		Mat outImage = new Mat(source.rows(), source.cols(), source.type());

		for (int j = 0; j < source.rows(); j++) {
			for (int i = 0; i < source.cols(); i++) {
				double[] rgbValue = source.get(j, i); // ermitteln des
														// ausgangswertes des
														// pixels

				rgbValue[0] = (255.0 / (whiteLevel - blackLevel)) * (rgbValue[0] - blackLevel); // berechnung
																								// der
																								// grauwertspreizung
				rgbValue[1] = (255.0 / (whiteLevel - blackLevel)) * (rgbValue[1] - blackLevel);
				rgbValue[2] = (255.0 / (whiteLevel - blackLevel)) * (rgbValue[2] - blackLevel);

				if (rgbValue[0] < 0) {// alles unter 0 auf 0 setzen und alles
										// �ber 255 auf 255 setzen...
					rgbValue[0] = 0;
				} else if (rgbValue[0] > 255) {
					rgbValue[0] = 255;
				}
				;
				if (rgbValue[1] < 0) {
					rgbValue[1] = 0;
				} else if (rgbValue[0] > 255) {
					rgbValue[1] = 255;
				}
				;
				if (rgbValue[2] < 0) {
					rgbValue[2] = 0;
				} else if (rgbValue[0] > 255) {
					rgbValue[2] = 255;
				}
				;

				outImage.put(j, i, rgbValue);
			}
		}
		return outImage;
	}

	class TimeSliderListener implements ChangeListener {
		@Override
		public void stateChanged(ChangeEvent e) { // bestimmt anhand des Status
													// des JSliders die Stelle
													// in der eingeladenen
													// Bildsequenz und setzt
													// entsprechend das
													// dargestellte Bild und das
													// angezeigte Histogramm
			if (imageSequence != null) {
				imgPanel.setSourceImage1(Mat2BufferedImage(imageSequence[timelineSlider.getValue()]));
				if (rgbHistCheck.isSelected()) {
					histogramPanel.setHistogrammMat(imageSequence[timelineSlider.getValue()], HistogramPanel.HistType_RGB);
				} else {
					histogramPanel.setHistogrammMat(imageSequence[timelineSlider.getValue()], HistogramPanel.HistType_BW);
				}
			}
			repaint();
		}
	}

	class TrackSequenceListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) { // initialisierung aller
														// n�tigen Variablen
														// und
														// iterative Anwendung
														// der
														// OpticalFlow-Methode
														// auf die Einzelbilder
														// der Sequenz
			sequenceTrackPoints = new MatOfPoint2f[sequenceLength];
			MatOfByte[] statusMat = new MatOfByte[sequenceLength];
			MatOfFloat[] errorMat = new MatOfFloat[sequenceLength];
			circles = new Mat[sequenceLength];
			for (int i = 0; i < sequenceLength; i++) {
				sequenceTrackPoints[i] = new MatOfPoint2f();
				statusMat[i] = new MatOfByte();
				errorMat[i] = new MatOfFloat();
				circles[i] = new Mat();
			}
			sequenceTrackPoints[0] = featurePoints;
			imgPanel.setSequenceTrackPoints(sequenceTrackPoints);
			for (int i = 1; i < sequenceLength; i++) {
				Mat currentGray1 = new Mat();
				Mat currentGray2 = new Mat();
				Imgproc.cvtColor(imageSequence[i - 1], currentGray1, Imgproc.COLOR_BGR2GRAY);
				Imgproc.cvtColor(imageSequence[i], currentGray2, Imgproc.COLOR_BGR2GRAY);
				Video.calcOpticalFlowPyrLK(currentGray1, currentGray2, sequenceTrackPoints[i - 1], sequenceTrackPoints[i], statusMat[i], errorMat[i]);
			}
			traceCheck.setEnabled(true);
			save.setEnabled(true);
			export.setEnabled(true);
			show3DButton.setEnabled(true);
			trackForwardButton.setEnabled(true);
			repaint();
		}
	}

	class TrackForwardListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) { // tracking von Punkten ab
														// einer bestimmten
														// Stelle in der Sequenz
			MatOfByte[] statusMat = new MatOfByte[sequenceLength];
			MatOfFloat[] errorMat = new MatOfFloat[sequenceLength];
			for (int i = 0; i < sequenceLength; i++) {
				statusMat[i] = new MatOfByte();
				errorMat[i] = new MatOfFloat();
			}
			for (int i = (timelineSlider.getValue()) + 1; i < sequenceLength; i++) {
				Video.calcOpticalFlowPyrLK(imageSequence[i - 1], imageSequence[i], sequenceTrackPoints[i - 1], sequenceTrackPoints[i], statusMat[i], errorMat[i]);
			}
			repaint();
		}
	}

	class ClearListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {// l�schen aller im
													// Programm
													// vorhandenen Referenzen
													// auf trackpunkte
			featurePoints = null;
			imgPanel.setFeaturePoints(null);
			sequenceTrackPoints = null;
			imgPanel.setSequenceTrackPoints(null);
			trackFeaturesInSequenceButton.setEnabled(false);
			trackForwardButton.setEnabled(false);
			show3DButton.setEnabled(false);
			save.setEnabled(false);
			export.setEnabled(false);
			clearButton.setEnabled(false);
			repaint();
		}
	}

	public BufferedImage Mat2BufferedImage(Mat imgMat) { // �bernommen von
															// Prof.
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
			throw new IllegalArgumentException("Unknown matrix type. Only one byte per pixel (one channel) or three bytes pre pixel (three channels) are allowed.");
		}
		BufferedImage bufferedImage = new BufferedImage(imgMat.cols(), imgMat.rows(), bufferedImageType);
		final byte[] bufferedImageBuffer = ((DataBufferByte) bufferedImage.getRaster().getDataBuffer()).getData();
		imgMat.get(0, 0, bufferedImageBuffer);
		return bufferedImage;
	}

	class OpenISListener implements ActionListener {
		public void actionPerformed(ActionEvent event) {// �ffnen von mehreren
														// bildern als
														// bildsequenz
			JFileChooser file = new JFileChooser();// erster File-Chooser wird
													// initialisiert
			File dir = new File("G:/Studium - Lippstadt/Java/OpenCVTracking/face_sonia");
			file.setCurrentDirectory(dir);
			int result = file.showOpenDialog(null);
			if (result == JFileChooser.APPROVE_OPTION) {
				File im1 = file.getSelectedFile();
				openPath = im1.getAbsolutePath();
				System.out.println("name of first frame: " + im1.getName());
				JFileChooser file2 = new JFileChooser();// zweiter File-Chooser
														// wird initialisiert
				File dir2 = new File("G:/Studium - Lippstadt/Java/OpenCVTracking/face_sonia");
				file2.setCurrentDirectory(dir2);
				int result2 = file2.showOpenDialog(null);
				if (result2 == JFileChooser.APPROVE_OPTION) {
					File im2 = file2.getSelectedFile();
					openPath = im2.getAbsolutePath();
					System.out.println("name of last frame: " + im2.getName());
					int startIndex = 0;
					int endIndex = im1.getName().length() - 4;
					for (int i = 0; i < im1.getName().length(); i++) {// teil
																		// des
																		// Dateinames
																		// welcher
																		// eine
																		// fortlaufende
																		// nummer
																		// darstellt
																		// wird
																		// ermittelt
						if (im1.getName().charAt(i) != im2.getName().charAt(i)) {
							startIndex = i;
							break;
						}
					}
					System.out.println("sequence-length: 0-" + im2.getName().substring(startIndex, endIndex));
					sequenceLength = Integer.parseInt(im2.getName().substring(startIndex, endIndex)) + 1;// l�nge
																											// der
																											// sequenz
																											// wird
																											// berechnet
					imageSequence = new Mat[sequenceLength];
					imageSequenceOriginal = new Mat[sequenceLength];
					for (int i = 0; i < imageSequence.length; i++) {
						imageSequence[i] = new Mat();
						imageSequenceOriginal[i] = new Mat();
					}
					for (int i = 0; i < sequenceLength; i++) {// jedes
																// einzelbild
																// der sequenz
																// wird geladen
																// und in einer
																// matrix
																// gespeichert
						String counterLength = "" + i;
						int difference = im2.getName().substring(startIndex, endIndex).length() - counterLength.length();
						String filler = "";
						for (int j = 0; j < difference; j++) {
							filler = filler + 0;
						}
						String path = openPath.substring(0, openPath.length() - im1.getName().length()) + im1.getName().substring(0, startIndex) + filler + i
								+ im1.getName().substring(endIndex, im1.getName().length());
						filler = "";
						imageSequence[i] = Imgcodecs.imread(path);
						imageSequenceOriginal[i] = Imgcodecs.imread(path);
					}
				}
				imgPanel.setScaleFactor(1.0);
				timelineSlider.setMaximum(sequenceLength - 1);// timelineSlider
																// max value
																// wird der
																// l�nge der
																// eingeladenen
																// sequenz
																// angepasst
				setPointsButton.setEnabled(true);
				rgbHistCheck.setEnabled(true);
				timelineSlider.setEnabled(true);
				timelineSlider.setValue(0);
				repaint();
			}
		}
	}

	class OpenProjectListener implements ActionListener {
		public void actionPerformed(ActionEvent event) { // �ffnen von
															// tracking
															// daten in einem
															// eigenen
															// dateiformat
			JFileChooser file = new JFileChooser();
			File dir = new File("G:/Studium - Lippstadt/Java/OpenCVTracking");
			file.setCurrentDirectory(dir);
			int result = file.showOpenDialog(null);
			if (result == JFileChooser.APPROVE_OPTION) {
				File im1 = file.getSelectedFile();
				openPath = im1.getAbsolutePath();
				try {
					FileReader fr = new FileReader(im1);
					BufferedReader br = new BufferedReader(fr);
					String zeile1 = br.readLine();
					String zeile2 = br.readLine();
					String zeile3 = br.readLine();

					int numOfFrames = Integer.parseInt(zeile2.substring(9, zeile2.length()));
					int numOfPoints = Integer.parseInt(zeile3.substring(9, zeile3.length()));

					System.out.println("#frames: " + numOfFrames);
					System.out.println("#points: " + numOfPoints);

					sequenceTrackPoints = new MatOfPoint2f[numOfFrames];

					for (int i = 0; i < numOfFrames; i++) { // punkt-koordinaten
															// werden zeile
															// f�r
															// zeile eingelesen
						sequenceTrackPoints[i] = new MatOfPoint2f();
						for (int j = 0; j < numOfPoints; j++) {
							double[] point = new double[2];
							point[0] = Float.parseFloat(br.readLine());
							point[1] = Float.parseFloat(br.readLine());
							sequenceTrackPoints[i].push_back(new MatOfPoint2f(new Point(point[0], point[1])));
						}
					}
					imgPanel.setSequenceTrackPoints(sequenceTrackPoints);
					timelineSlider.setMaximum(numOfFrames - 1);
					traceCheck.setEnabled(true);
					trackFeaturesInSequenceButton.setEnabled(true);
					trackForwardButton.setEnabled(true);
					clearButton.setEnabled(true);
					show3DButton.setEnabled(true);
					save.setEnabled(true);
					export.setEnabled(true);
					timelineSlider.setEnabled(true);
					timelineSlider.setValue(0);
					repaint();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}

	class SaveListener implements ActionListener {
		public void actionPerformed(ActionEvent event) {// speichern von
														// tracking daten in
														// einem eigenen
														// dateiformat
			JFileChooser file = new JFileChooser();
			File dir = new File("G:/Studium - Lippstadt/Java/OpenCVTracking");
			file.setCurrentDirectory(dir);
			int result = file.showSaveDialog(null);
			if (result == JFileChooser.APPROVE_OPTION) {
				File im2 = file.getSelectedFile();
				savePath = im2.getAbsolutePath();
				PrintWriter writer;
				try {
					writer = new PrintWriter(savePath + ".ftd", "UTF-8");

					writer.println("saved tracking-data");
					writer.println("#frames: " + sequenceTrackPoints.length);
					writer.println("#points: " + sequenceTrackPoints[0].rows());

					for (int i = 0; i < sequenceTrackPoints.length; i++) {// punkt-koordinaten
																			// werden
																			// zeile
																			// f�r
																			// zeile
																			// in
																			// die
																			// datei
																			// geschrieben
						for (int j = 0; j < sequenceTrackPoints[i].rows(); j++) {
							double[] point = sequenceTrackPoints[i].get(j, 0);
							writer.println(point[0]);
							writer.println(point[1]);
						}
					}
					writer.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}

	class ExportListener implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent arg0) { // schreiben der
														// tracking daten in
														// eine Autodesk
														// Maya-Datei

			double minX = 0.0f;
			double maxX = 0.0f;
			double minY = 0.0f;
			double maxY = 0.0f;

			double[] point0 = new double[2];
			point0 = sequenceTrackPoints[0].get(0, 0);
			minX = point0[0];
			maxX = point0[0];
			minY = point0[1];
			maxY = point0[1];

			for (int i = 0; i < sequenceTrackPoints[0].rows(); i++) {
				double[] point = new double[2];
				point = sequenceTrackPoints[0].get(i, 0);
				if (point[0] < minX) {
					minX = point[0];
				}
				if (point[0] > maxX) {
					maxX = point[0];
				}
				if (point[1] < minY) {
					minY = point[0];
				}
				if (point[1] > maxY) {
					maxY = point[0];
				}
			}

			double offsetX = ((maxX - minX) / 2) + minX;
			double offsetY = ((maxY - minY) / 2) + minY;

			double scale = 30 / (maxX - minX);

			JFileChooser file = new JFileChooser();
			File dir = new File("G:/Studium - Lippstadt/Java/OpenCVTracking");
			file.setCurrentDirectory(dir);
			int result = file.showSaveDialog(null);
			if (result == JFileChooser.APPROVE_OPTION) {
				File im2 = file.getSelectedFile();
				savePath = im2.getAbsolutePath();
				PrintWriter writer;
				try {
					writer = new PrintWriter(savePath + ".ma", "UTF-8");
					writer.println("//Maya ASCII 2014 scene");
					writer.println("//Name: " + im2.getName() + ".ma");
					for (int i = 0; i < MayaASCIIFile.LINE1TO64.length; i++) {
						writer.println(MayaASCIIFile.LINE1TO64[i]);
					}
					for (int j = 0; j < sequenceTrackPoints[0].rows(); j++) {
						writer.println("createNode transform -n \"" + "locator" + (j + 1) + "\";");
						writer.println("createNode locator -n \"locatorShape" + (j + 1) + "\" -p \"locator" + (j + 1) + "\";");
						writer.println("	setAttr -k off \".v\";");
					}
					for (int i = 0; i < MayaASCIIFile.ZWISCHENLINES.length; i++) {
						writer.println(MayaASCIIFile.ZWISCHENLINES[i]);
					}
					writer.println("createNode script -n \"sceneConfigurationScriptNode\";");
					writer.println("	setAttr \".b\" -type \"string\" \"playbackOptions -min 1 -max " + sequenceTrackPoints.length + " -ast 1 -aet " + sequenceTrackPoints.length + " \";");
					writer.println("	setAttr \".st\" 6;");
					for (int j = 0; j < sequenceTrackPoints[0].rows(); j++) {
						writer.println("createNode animCurveTL -n \"locator" + (j + 1) + "_translateX\";");
						writer.println("	setAttr \".tan\" 18;");
						writer.println("	setAttr \".wgt\" no;");
						writer.print("	setAttr -s " + sequenceTrackPoints.length + " \".ktv[0:" + (sequenceTrackPoints.length - 1) + "]\" ");
						for (int i = 0; i < sequenceTrackPoints.length; i++) {
							writer.print(" " + (i + 1) + " " + (sequenceTrackPoints[i].get(j, 0)[0] - offsetX) * scale);
						}
						writer.println(";");
						writer.println("createNode animCurveTL -n \"locator" + (j + 1) + "_translateY\";");
						writer.println("	setAttr \".tan\" 18;");
						writer.println("	setAttr \".wgt\" no;");
						writer.print("	setAttr -s " + sequenceTrackPoints.length + " \".ktv[0:" + (sequenceTrackPoints.length - 1) + "]\" ");
						for (int i = 0; i < sequenceTrackPoints.length; i++) {
							writer.print(" " + (i + 1) + " " + -(sequenceTrackPoints[i].get(j, 0)[1] - (2 * offsetY)) * scale);
						}
						writer.println(";");
						writer.println("createNode animCurveTL -n \"locator" + (j + 1) + "_translateZ\";");
						writer.println("	setAttr \".tan\" 18;");
						writer.println("	setAttr \".wgt\" no;");
						writer.print("	setAttr -s " + sequenceTrackPoints.length + " \".ktv[0:" + (sequenceTrackPoints.length - 1) + "]\" ");
						for (int i = 0; i < sequenceTrackPoints.length; i++) {
							writer.print(" " + (i + 1) + " " + 0);
						}
						writer.println(";");
					}
					for (int i = 0; i < MayaASCIIFile.ZWISCHENLINES2.length; i++) {
						writer.println(MayaASCIIFile.ZWISCHENLINES2[i]);
					}
					for (int j = 0; j < sequenceTrackPoints[0].rows(); j++) {
						writer.println("connectAttr \"locator" + (j + 1) + "_translateX.o\" \"locator" + (j + 1) + ".tx\";");
						writer.println("connectAttr \"locator" + (j + 1) + "_translateY.o\" \"locator" + (j + 1) + ".ty\";");
						writer.println("connectAttr \"locator" + (j + 1) + "_translateZ.o\" \"locator" + (j + 1) + ".tz\";");
					}
					for (int i = 0; i < MayaASCIIFile.ENDLINES.length; i++) {
						writer.println(MayaASCIIFile.ENDLINES[i]);
					}
					writer.println("// End of " + im2.getName() + ".ma");
					writer.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}

	}

	class MovePointListener implements MouseMotionListener {

		@Override
		public void mouseDragged(MouseEvent e) {// aktualisieren des
												// verschobenen punktes

			if (indexOfSelectedPoint != -1) {
				if (sequenceTrackPoints != null) {
					double[] point = new double[2];
					point[0] = ((e.getX() - 25) / imgPanel.getScaleFactor() - 10);
					point[1] = ((e.getY() - 25) / imgPanel.getScaleFactor() - 10);
					sequenceTrackPoints[timelineSlider.getValue()].put(indexOfSelectedPoint, 0, point);
					repaint();
				} else {
					double[] point = new double[2];
					point[0] = ((e.getX() - 25) / imgPanel.getScaleFactor() - 10);
					point[1] = ((e.getY() - 25) / imgPanel.getScaleFactor() - 10);
					featurePoints.put(indexOfSelectedPoint, 0, point);
					repaint();
				}
			}
		}

		@Override
		public void mouseMoved(MouseEvent move) {

		}

	}

	class SetPointListener implements MouseListener {

		@Override
		public void mouseClicked(MouseEvent click) { // manuelles definieren von
														// trackpunkten
			if (click.getX() / imgPanel.getScaleFactor() - 25 >= 0 && click.getY() / imgPanel.getScaleFactor() - 25 >= 0 && click.getX() / imgPanel.getScaleFactor() - 25 < imageSequence[0].cols()
					&& click.getY() / imgPanel.getScaleFactor() - 25 < imageSequence[0].rows()) {

				MatOfPoint2f fp = new MatOfPoint2f(new Point(click.getX() - 25, click.getY() - 25));
				if (featurePoints == null) {
					featurePoints = new MatOfPoint2f();
				}
				featurePoints.push_back(fp);
				imgPanel.setFeaturePoints(featurePoints);

				clearButton.setEnabled(true);
				repaint();
			}
		}

		@Override
		public void mouseEntered(MouseEvent arg0) {
		}

		@Override
		public void mouseExited(MouseEvent arg0) {
		}

		@Override
		public void mousePressed(MouseEvent click) {// wenn die maustaste
													// gedr�ckt wird, wird
													// gepr�ft ob sich der
													// courser �ber einem der
													// definierten trackpunkte
													// befindet, wenn das der
													// fall ist wird der index
													// des entschprechenden
													// punktes gespeichert
			if (sequenceTrackPoints != null) {
				for (int i = 0; i < sequenceTrackPoints[timelineSlider.getValue()].rows(); i++) {

					if ((click.getX() - 25) / imgPanel.getScaleFactor() <= sequenceTrackPoints[timelineSlider.getValue()].get(i, 0)[0] + 20
							&& (click.getX() - 25) / imgPanel.getScaleFactor() >= sequenceTrackPoints[timelineSlider.getValue()].get(i, 0)[0] - 20
							&& (click.getY() - 25) / imgPanel.getScaleFactor() <= sequenceTrackPoints[timelineSlider.getValue()].get(i, 0)[1] + 20
							&& (click.getY() - 25) / imgPanel.getScaleFactor() >= sequenceTrackPoints[timelineSlider.getValue()].get(i, 0)[1] - 20) {
						indexOfSelectedPoint = i;
					}
				}
			} else if (featurePoints != null) {
				for (int i = 0; i < featurePoints.rows(); i++) {

					if ((click.getX() - 25) / imgPanel.getScaleFactor() <= featurePoints.get(i, 0)[0] + 20 && (click.getX() - 25) / imgPanel.getScaleFactor() >= featurePoints.get(i, 0)[0] - 20
							&& (click.getY() - 25) / imgPanel.getScaleFactor() <= featurePoints.get(i, 0)[1] + 20
							&& (click.getY() - 25) / imgPanel.getScaleFactor() >= featurePoints.get(i, 0)[1] - 20) {
						indexOfSelectedPoint = i;
					}
				}
			}
		}

		@Override
		public void mouseReleased(MouseEvent arg0) {
			indexOfSelectedPoint = -1;
		}

	}
}
