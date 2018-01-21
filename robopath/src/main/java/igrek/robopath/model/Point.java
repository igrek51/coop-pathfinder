package igrek.robopath.model;

public class Point {
	
	public int x;
	public int y;
	
	public Point(int x, int y) {
		this.x = x;
		this.y = y;
	}
	
	public int getX() {
		return x;
	}
	
	public int getY() {
		return y;
	}
	
	public void setX(int x) {
		this.x = x;
	}
	
	public void setY(int y) {
		this.y = y;
	}
	
	@Override
	public String toString() {
		return "(" + x + ", " + y + ")";
	}
	
	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof Point))
			return false;
		if (obj == this)
			return true;
		Point p2 = (Point) obj;
		return this.x == p2.x && this.y == p2.y;
	}
	
	private int abs(int x) {
		return x >= 0 ? x : -x;
	}
	
	public boolean isAdjacent(Point p2) {
		int dx = abs(p2.x - x);
		int dy = abs(p2.y - y);
		// adjacent doesn't mean the same
		if (dx == 0 && dy == 0)
			return false;
		return dx <= 1 && dy <= 1;
	}
	
	public double distance(Point p2) {
		return Math.hypot(p2.x - x, p2.y - y);
	}
}
