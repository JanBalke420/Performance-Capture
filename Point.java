
public class Point { // einfache eigene Klasse zum speichern von 2-dimensionalen
						// Koordinaten

	private int posX = 0;
	private int posY = 0;

	public Point(int posX, int posY) {
		this.setPosX(posX);
		this.setPosY(posY);
	}

	public int getPosX() {
		return posX;
	}

	public void setPosX(int posX) {
		this.posX = posX;
	}

	public int getPosY() {
		return posY;
	}

	public void setPosY(int posY) {
		this.posY = posY;
	}

}
