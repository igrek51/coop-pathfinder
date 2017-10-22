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
}
