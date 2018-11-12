import javax.media.j3d.*;
import javax.vecmath.*;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;

import com.sun.j3d.utils.geometry.*;
import com.sun.j3d.utils.universe.*;
import com.sun.j3d.utils.behaviors.vp.*;
import javax.swing.JFrame;
import java.util.*;
//import simpleParticleSystemPackage.*;
import java.awt.Font;

/**
 * Beispiel fuer eine dynamische Oberflaeche
 *
 * @author Frank Klawonn Letzte Aenderung 27.05.2005
 * @see StaticSceneExample
 */
public class DynamicSurface extends JFrame {

	private MatOfPoint2f[] sequenceTrackPoints;

	// Der Canvas, auf den gezeichnet wird.
	public Canvas3D myCanvas3D;

	public DynamicSurface(MatOfPoint2f[] sequenceTrackPoints) { // bis
																		// auf
																		// die
																		// erste
																		// zeile
																		// und
																		// dem
																		// Übergabeparameter
																		// ist
																		// der
																		// knstruktor
																		// von
																		// Herrn
																		// Klawonn
																		// übernommen
																		// worden

		// hier werden die übergebenen trackpunkte in die instanzvariable
		// geschrieben
		this.sequenceTrackPoints = sequenceTrackPoints;

		// Standardeinstellung fuer das Betrachteruniversum
		myCanvas3D = new Canvas3D(SimpleUniverse.getPreferredConfiguration());

		// Aufbau des SimpleUniverse:
		// Zuerst Erzeugen zusammen mit dem Canvas
		SimpleUniverse simpUniv = new SimpleUniverse(myCanvas3D);

		// Standardpositionierung des Betrachters
		simpUniv.getViewingPlatform().setNominalViewingTransform();

		// Die Szene wird in dieser Methode erzeugt.
		createSceneGraph(simpUniv);

		// Hinzufuegen von Licht
		addLight(simpUniv);

		// Hierdurch kann man mit der Maus den Betrachterstandpunkt veraendern
		OrbitBehavior ob = new OrbitBehavior(myCanvas3D);
		ob.setSchedulingBounds(new BoundingSphere(new Point3d(0.0, 0.0, 0.0), Double.MAX_VALUE));
		simpUniv.getViewingPlatform().setViewPlatformBehavior(ob);

		// Darstellung des Canvas/Fensters:
		setTitle("Surface morphing");
		setSize(1024, 1024);
		setLocationRelativeTo(null);
		getContentPane().add("Center", myCanvas3D);
		setVisible(true);

	}

	// In dieser Methode werden die Objekte der Szene aufgebaut, die Bewegungen
	// definiert und dem SimpleUniverse su hinzugefuegt.
	public void createSceneGraph(SimpleUniverse su) {

		// *** Die Wurzel des Graphen, der die Szene enthaelt. ***

		GeometryArray[] geoArr = new GeometryArray[sequenceTrackPoints.length]; // Erzeugen
																				// des
																				// GeometryArrays
																				// für
																				// die
																				// Interpolation
		for (int i = 0; i < geoArr.length; i++) {
			geoArr[i] = createSurface(sequenceTrackPoints[i]);
		}

		// Der Morph-Knoten, der die beiden Oberflaechen beinhaltet.
		Morph surfaceMorpher = new Morph(geoArr);
		surfaceMorpher.setCapability(Morph.ALLOW_WEIGHTS_WRITE);

		// Das Alpha, das die Interpolation steuert.
		Alpha alphaSurface = new Alpha(-1, Alpha.INCREASING_ENABLE, 0, 0,
				(sequenceTrackPoints.length / 25) * 1000, 0, 1000, (sequenceTrackPoints.length / 25) * 1000, 0, 1000);
		alphaSurface.setIncreasingAlphaRampDuration(10);

		// Eine Appearance fuer die beiden Oberflaechen.
		// die folgenden 4 zeilen dienen dazu backface culling auszuschalten
		PolygonAttributes polygonAttributes = new PolygonAttributes();
		polygonAttributes.setCullFace(PolygonAttributes.CULL_NONE);
		Appearance morphApp = new Appearance();
		morphApp.setPolygonAttributes(polygonAttributes);
		Color3f ambientColour = new Color3f(0.5f, 0.0f, 0.0f);
		Color3f emissiveColour = new Color3f(0.1f, 0.1f, 0.1f);
		Color3f diffuseColour = new Color3f(0.3f, 0.0f, 0.0f);
		Color3f specularColour = new Color3f(0.4f, 0.4f, 0.4f);
		float shininess = 100.0f;

		morphApp.setMaterial(new Material(ambientColour, emissiveColour, diffuseColour, specularColour, shininess));
		surfaceMorpher.setAppearance(morphApp);

		// Die Transformationsgruppe fuer den Morph.
		TransformGroup tgMorph = new TransformGroup();
		SimpleMorphBehaviour morpher = new SimpleMorphBehaviour(surfaceMorpher, alphaSurface,
				sequenceTrackPoints.length);
		BoundingSphere bounds = new BoundingSphere(new Point3d(0.0, 0.0, 0.0), Double.MAX_VALUE);
		morpher.setSchedulingBounds(bounds);

		tgMorph.addChild(surfaceMorpher);

		// Eine Transformationsgruppe zur Positionierung des Morph.
		Transform3D tfMorphPos = new Transform3D();
		tfMorphPos.setTranslation(new Vector3f(0.0f, 0.0f, 0.0f));

		TransformGroup tgMorphPos = new TransformGroup(tfMorphPos);
		tgMorphPos.addChild(tgMorph);

		BranchGroup theScene = new BranchGroup();

		theScene.addChild(tgMorphPos);
		theScene.addChild(morpher);

		// Die folgenden drei Zeilen erzeugen einen weißen Hintergrund.
		Background bg = new Background(new Color3f(1.0f, 1.0f, 1.0f));
		bg.setApplicationBounds(bounds);
		theScene.addChild(bg);

		theScene.compile();

		// Hinzufuegen der Szene
		su.addBranchGraph(theScene);
	}

	/**
	 * Erzeugt eine Standardoberflaechenstruktur in einer gewuenschten Farbe
	 *
	 * @param app
	 *            Die Appearance, mit der die Oberflaeche belegt werden soll
	 * @param col
	 *            Die gewuenschte Farbe
	 */
	public static void setToMyDefaultAppearance(Appearance app, Color3f col) {
		app.setMaterial(new Material(col, col, col, col, 150.0f));
	}

	// Hier wird etwas Licht zu der Szene hinzugefuegt.
	public void addLight(SimpleUniverse su) {

		BranchGroup bgLight = new BranchGroup();

		BoundingSphere bounds = new BoundingSphere(new Point3d(0.0, 0.0, 0.0), 100.0);
		Color3f lightColour1 = new Color3f(1.0f, 1.0f, 1.0f);
		Vector3f lightDir1 = new Vector3f(-1.0f, 0.0f, -0.5f);
		DirectionalLight light1 = new DirectionalLight(lightColour1, lightDir1);
		light1.setInfluencingBounds(bounds);

		bgLight.addChild(light1);
		su.addBranchGraph(bgLight);
	}

	public static GeometryArray createSurface(MatOfPoint2f featurePoints) { // in
																			// dieser
																			// methode
																			// sind
																			// die
																			// letzten
																			// 8
																			// zeilen
																			// von
																			// Herrn
																			// Klawonn
																			// übernommen
																			// worden

		IndexTriangle[] triangles = new IndexTriangle[55]; // hier werden alle
															// nötigen Dreiecke
															// angelegt und
															// gespeichert
		triangles[0] = new IndexTriangle(1, 4, 2);
		triangles[1] = new IndexTriangle(1, 5, 4);
		triangles[2] = new IndexTriangle(1, 3, 5);
		triangles[3] = new IndexTriangle(2, 4, 6);
		triangles[4] = new IndexTriangle(4, 7, 6);
		triangles[5] = new IndexTriangle(4, 5, 7);
		triangles[6] = new IndexTriangle(5, 8, 7);
		triangles[7] = new IndexTriangle(5, 9, 8);
		triangles[8] = new IndexTriangle(3, 9, 5);
		triangles[9] = new IndexTriangle(6, 7, 10);
		triangles[10] = new IndexTriangle(7, 8, 16);
		triangles[11] = new IndexTriangle(8, 9, 11);
		triangles[12] = new IndexTriangle(6, 12, 14);
		triangles[13] = new IndexTriangle(6, 10, 12);
		triangles[14] = new IndexTriangle(7, 12, 10);
		triangles[15] = new IndexTriangle(7, 15, 12);
		triangles[16] = new IndexTriangle(7, 16, 15);
		triangles[17] = new IndexTriangle(8, 17, 16);
		triangles[18] = new IndexTriangle(8, 13, 17);
		triangles[19] = new IndexTriangle(8, 11, 13);
		triangles[20] = new IndexTriangle(9, 13, 11);
		triangles[21] = new IndexTriangle(9, 18, 13);
		triangles[22] = new IndexTriangle(14, 19, 21);
		triangles[23] = new IndexTriangle(15, 23, 19);
		triangles[24] = new IndexTriangle(15, 16, 23);
		triangles[25] = new IndexTriangle(16, 17, 25);
		triangles[26] = new IndexTriangle(17, 20, 25);
		triangles[27] = new IndexTriangle(18, 22, 20);
		triangles[28] = new IndexTriangle(19, 23, 21);
		triangles[29] = new IndexTriangle(20, 22, 25);
		triangles[30] = new IndexTriangle(16, 24, 23);
		triangles[31] = new IndexTriangle(16, 25, 24);
		triangles[32] = new IndexTriangle(21, 23, 26);
		triangles[33] = new IndexTriangle(22, 27, 25);
		triangles[34] = new IndexTriangle(23, 24, 26);
		triangles[35] = new IndexTriangle(24, 25, 27);
		triangles[36] = new IndexTriangle(24, 27, 26);
		triangles[37] = new IndexTriangle(21, 26, 28);
		triangles[38] = new IndexTriangle(22, 34, 27);
		triangles[39] = new IndexTriangle(26, 29, 28);
		triangles[40] = new IndexTriangle(26, 30, 29);
		triangles[41] = new IndexTriangle(26, 31, 30);
		triangles[42] = new IndexTriangle(26, 27, 31);
		triangles[43] = new IndexTriangle(27, 32, 31);
		triangles[44] = new IndexTriangle(27, 33, 32);
		triangles[45] = new IndexTriangle(27, 34, 33);
		triangles[46] = new IndexTriangle(28, 29, 39);
		triangles[47] = new IndexTriangle(29, 35, 39);
		triangles[48] = new IndexTriangle(35, 38, 39);
		triangles[49] = new IndexTriangle(35, 36, 38);
		triangles[50] = new IndexTriangle(36, 37, 38);
		triangles[51] = new IndexTriangle(37, 40, 38);
		triangles[52] = new IndexTriangle(38, 40, 39);
		triangles[53] = new IndexTriangle(33, 40, 37);
		triangles[54] = new IndexTriangle(34, 40, 33);
		for (int i = 0; i < triangles.length; i++) {
			triangles[i].adjust(); // hier werden zwei Fehler beim abtippen der
									// festen werte ausgeglichen
		}

		Point3f[] surfaceVertices = new Point3f[featurePoints.rows()];
		int[] coordinateIndices = new int[triangles.length * 3];
		float zOwn[] = new float[featurePoints.rows()]; // hier werden feste
														// z-koordinaten für
														// jeden Punkt
														// festgelegt, da die
														// getrackten
														// Koordinaten nur
														// 2-Dimensional sind
		zOwn[0] = -50.0f;
		zOwn[1] = -141.0f;
		zOwn[2] = -141.0f;
		zOwn[3] = -45.0f;
		zOwn[4] = -45.0f;
		zOwn[5] = -70.0f;
		zOwn[6] = -36.0f;
		zOwn[7] = -36.0f;
		zOwn[8] = -70.0f;
		zOwn[9] = -53.0f;
		zOwn[10] = -53.0f;
		zOwn[11] = -61.0f;
		zOwn[12] = -61.0f;
		zOwn[13] = -80.0f;
		zOwn[14] = -63.0f;
		zOwn[15] = -27.0f;
		zOwn[16] = -63.0f;
		zOwn[17] = -80.0f;
		zOwn[18] = -64.0f;
		zOwn[19] = -64.0f;
		zOwn[20] = -87.0f;
		zOwn[21] = -87.0f;
		zOwn[22] = -30.0f;
		zOwn[23] = -0.0f;
		zOwn[24] = -30.0f;
		zOwn[25] = -48.0f;
		zOwn[26] = -48.0f;
		zOwn[27] = -83.0f;
		zOwn[28] = -59.0f;
		zOwn[29] = -36.0f;
		zOwn[30] = -19.0f;
		zOwn[31] = -36.0f;
		zOwn[32] = -59.0f;
		zOwn[33] = -83.0f;
		zOwn[34] = -36.0f;
		zOwn[35] = -25.0f;
		zOwn[36] = -36.0f;
		zOwn[37] = -37.0f;
		zOwn[38] = -73.0f;
		zOwn[39] = -73.0f;

		for (int i = 0; i < featurePoints.rows(); i++) { // hier werden anhand
															// der tracking
															// daten und der
															// zuvor
															// festgelegten
															// z-koordinaten die
															// Oberflächenpunkte
															// initialisiert
			surfaceVertices[i] = new Point3f((float) (featurePoints.get(i, 0)[0] - 540) / 600,
					(float) -(featurePoints.get(i, 0)[1] - 960) / 600, zOwn[i] / 400);
		}

		int zaehler = 0;
		for (int i = 0; i < triangles.length; i++) { // hier werden die
														// Koordinaten-Indices
														// initialisiert, welche
														// die Bildung der
														// Dreicke bestimmt
			coordinateIndices[zaehler] = triangles[i].getPoint1();
			zaehler++;
			coordinateIndices[zaehler] = triangles[i].getPoint2();
			zaehler++;
			coordinateIndices[zaehler] = triangles[i].getPoint3();
			zaehler++;
		}
		// bis hier wurde die Methode selbst geschrieben...

		// Ab hier ist der rest der Methode unverändert...
		GeometryInfo gi = new GeometryInfo(GeometryInfo.TRIANGLE_ARRAY);
		gi.setCoordinates(surfaceVertices);
		gi.setCoordinateIndices(coordinateIndices);
		NormalGenerator ng = new NormalGenerator();
		ng.setCreaseAngle(Math.PI);
		ng.generateNormals(gi);
		GeometryArray gaSurface = gi.getGeometryArray();

		return (gaSurface);

	}

	public MatOfPoint2f[] getSequenceTrackPoints() {
		return sequenceTrackPoints;
	}

	public void setSequenceTrackPoints(MatOfPoint2f[] sequenceTrackPoints) {
		this.sequenceTrackPoints = sequenceTrackPoints;
	}

}