package igrek.robopath.pathfinder.mystar;

public class ReservationTable {
	
	private int width;
	private int height;
	private int timeDimension;
	
	private boolean availability[][][];
	
	public ReservationTable(int width, int height, int timeDimension) {
		this.width = width;
		this.height = height;
		this.timeDimension = timeDimension;
		availability = new boolean[width][height][timeDimension];
		clear();
	}
	
	private void clear() {
		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				for (int t = 0; t < timeDimension; t++) {
					availability[x][y][t] = true;
				}
			}
		}
	}
	
	public int getWidth() {
		return width;
	}
	
	public int getHeight() {
		return height;
	}
	
	public int getTimeDimension() {
		return timeDimension;
	}
	
	public boolean isBlocked(int x, int y, int t) {
		return !availability[x][y][t];
	}
	
	public boolean isBlocked(int x, int y) {
		// is blocked all the time
		for (int t = 0; t < timeDimension; t++) {
			if (availability[x][y][t])
				return false;
		}
		return true;
	}
	
	public void setBlocked(int x, int y, int t) {
		if (xytValid(x, y, t))
			availability[x][y][t] = false;
	}
	
	public void setBlocked(int x, int y) {
		for (int t = 0; t < timeDimension; t++) {
			setBlocked(x, y, t);
		}
	}
	
	public boolean xytValid(int x, int y, int t) {
		return x >= 0 && y >= 0 && t >= 0 && x < width && y < height && t < timeDimension;
	}
	
}
