
public class IndexTriangle { // eigene Klasse zum Speichern von indizes die zur
								// bildung von Dreicken genutzt werden
	private int point1 = 0;
	private int point2 = 0;
	private int point3 = 0;

	public IndexTriangle(int a, int b, int c) {
		point1 = a;
		point2 = b;
		point3 = c;
	}

	public void adjust() { // diese Methode zieht von jedem gespeicherten index
							// 1 ab und dreht die Reihenfolge der gespeicherten
							// indizes um. Das geschieht zum Ausgleich
							// zweier Fehler die beim manuellen Abtippen fester
							// werte gescchehen sind.
		point1 = point1 - 1;
		point2 = point2 - 1;
		point3 = point3 - 1;

		int zwischen = point1;
		point1 = point3;
		point3 = zwischen;
	}

	public int getPoint1() {
		return point1;
	}

	public void setPoint1(int point1) {
		this.point1 = point1;
	}

	public int getPoint2() {
		return point2;
	}

	public void setPoint2(int point2) {
		this.point2 = point2;
	}

	public int getPoint3() {
		return point3;
	}

	public void setPoint3(int point3) {
		this.point3 = point3;
	}

}
