import javax.media.j3d.*;
import javax.vecmath.*;
import com.sun.j3d.utils.geometry.*;
import com.sun.j3d.utils.universe.*;
import com.sun.j3d.utils.behaviors.vp.*;
import javax.swing.JFrame;
import java.util.*;

/**
 * Beispiel fuer ein Morph-Behaviour
 *
 * @author Frank Klawonn Letzte Aenderung 31.07.2008
 * @see DynamicSurface
 */
public class SimpleMorphBehaviour extends Behavior {
	private Morph theMorph; // Der Morph, der animiert werden soll
	private Alpha theAlpha; // Das Alpha, das den Ablauf des Morph steuert

	private double[] weights; // Die Gewicht fuer die Interpolation zwischen den
								// beiden Objekten des Morph
	private WakeupCondition trigger = new WakeupOnElapsedFrames(0);
	// Es gibt kein spezielles Aufweckkriterium. Die Animation soll
	// kontinuirlich durchlaufen.

	// Der Standardkonstruktor zur Initialisierung der Werte
	public SimpleMorphBehaviour(Morph targetMorph, Alpha alpha, int numOfWeights) {
		theMorph = targetMorph;
		theAlpha = alpha;
		weights = new double[numOfWeights];
	}

	public void initialize() {
		wakeupOn(trigger);
	}

	// In dieser Methode werden die Gewichte entsprechend dem vom Alpha-Objekt
	// vorgegebenen Wert bestimmt.
	public void processStimulus(Enumeration criteria) { // diese Methode zur
														// berechnung der
														// benötigten gewichte
														// wurde ursprünglich
														// einer Ausarbitung von
														// Herrn Prof. Dr.
														// Heinzel übernommen,
														// jedoch in zeile 8-11
														// leciht verändert
		for (int i = 0; i < weights.length; i++) {
			weights[i] = 0.0;
		}

		float alphaValue = (float) weights.length * theAlpha.value() - 0.00001f;
		int alphaIndex = (int) alphaValue;
		weights[alphaIndex] = (double) alphaValue - (double) alphaIndex;
		if (alphaIndex > 0) {
			weights[alphaIndex - 1] = 1.0 - weights[alphaIndex];
		} else {
			weights[0] = 1.0; // - weights[alphaIndex];
		}

		theMorph.setWeights(weights);
		wakeupOn(trigger); // naechstes Aufweckkriterium (Es bleibt das Alte.)
	}
}