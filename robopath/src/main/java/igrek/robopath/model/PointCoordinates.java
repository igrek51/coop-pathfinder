package igrek.robopath.model;

public class PointCoordinates {
	
	public int x;
	public int y;
	
	public PointCoordinates(int x, int y) {
		this.x = x;
		this.y = y;
	}
	
	public int getX() {
		return x;
	}
	
	public int getY() {
		return y;
	}
	
	@Override
	public String toString() {
		return "(" + x + ", " + y + ")";
	}
	
	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof PointCoordinates))
			return false;
		PointCoordinates p2 = (PointCoordinates) obj;
		return this.x == p2.x && this.y == p2.y;
	}
}
