package igrek.robopath.pathfinder.mystar;

import igrek.robopath.model.Point;

public class TileMap {
	
	private int width;
	private int height;
	private boolean[][] tiles;
	
	public TileMap(int width, int height) {
		this.width = width;
		this.height = height;
		tiles = new boolean[getWidthInTiles()][getHeightInTiles()];
		for (int x = 0; x < tiles.length; x++) {
			for (int y = 0; y < tiles[x].length; y++) {
				tiles[x][y] = false;
			}
		}
	}
	
	public void setCell(int x, int y, boolean occupied) {
		if (x < 0 || y < 0 || x >= getWidthInTiles() || y >= getHeightInTiles())
			return;
		tiles[x][y] = occupied;
	}
	
	public void setCell(Point point, boolean occupied) {
		setCell(point.x, point.y, occupied);
	}
	
	public Boolean getCell(int x, int y) {
		if (x < 0 || y < 0 || x >= getWidthInTiles() || y >= getHeightInTiles())
			return null;
		return tiles[x][y];
	}
	
	public Boolean getCell(Point p) {
		return getCell(p.x, p.y);
	}
	
	/**
	 * Get the width of the tile map. The slightly odd name is used
	 * to distiguish this method from commonly used names in game maps.
	 * @return The number of tiles across the map
	 */
	public int getWidthInTiles() {
		return width;
	}
	
	/**
	 * Get the height of the tile map. The slightly odd name is used
	 * to distiguish this method from commonly used names in game maps.
	 * @return The number of tiles down the map
	 */
	public int getHeightInTiles() {
		return height;
	}
	
	/**
	 * Check if the given location is blocked, i.e. blocks movement of
	 * the supplied mover.
	 * @param x The x coordinate of the tile to check
	 * @param y The y coordinate of the tile to check
	 * @return True if the location is blocked
	 */
	public boolean blocked(int x, int y) {
		return getCell(x, y);
	}
	
}