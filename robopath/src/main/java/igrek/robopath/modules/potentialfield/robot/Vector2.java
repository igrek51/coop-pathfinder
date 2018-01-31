package igrek.robopath.modules.potentialfield.robot;

/**
 * Immutable 2D vector
 */
public class Vector2 {
	
	public final double x;
	public final double y;
	
	public static final Vector2 ZERO = new Vector2(0, 0);
	
	public Vector2(double x, double y) {
		this.x = x;
		this.y = y;
	}
	
	public double getX() {
		return x;
	}
	
	public double getY() {
		return y;
	}
	
	@Override
	public String toString() {
		return "(" + x + ", " + y + ")";
	}
	
	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof Vector2))
			return false;
		if (obj == this)
			return true;
		Vector2 p2 = (Vector2) obj;
		return Double.compare(this.x, p2.x) == 0 && Double.compare(this.y, p2.y) == 0;
	}
	
	public double length() {
		return Math.hypot(x, y);
	}
	
	public double distance(Vector2 p2) {
		return Math.hypot(p2.x - x, p2.y - y);
	}
	
	public Vector2 add(double x, double y) {
		return new Vector2(this.x + x, this.y + y);
	}
	
	public Vector2 add(Vector2 v2) {
		return new Vector2(this.x + v2.getX(), this.y + v2.getY());
	}
	
	public Vector2 sub(Vector2 v2) {
		return new Vector2(this.x - v2.getX(), this.y - v2.getY());
	}
	
	public Vector2 scale(double sc) {
		return new Vector2(this.x * sc, this.y * sc);
	}
	
	public Vector2 inverse(double sc) {
		return new Vector2(-x, -y);
	}
	
	public Vector2 normalizeTo(double normalLength) {
		double length = length();
		return scale(normalLength / length);
	}
	
	public Vector2 cutOff(double maxLength) {
		double length = length();
		if (length == 0.0)
			return ZERO;
		if (length <= maxLength)
			return this;
		return scale(maxLength / length);
	}
	
	public Vector2 versor() {
		// normalize to length 1
		return normalizeTo(1); // be careful to division by 0
	}
	
}
