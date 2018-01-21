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
	
	public interface MapCellExecutor {
		void execute(int x, int y, boolean occupied);
	}
	
	public void foreach(MapCellExecutor executor) {
		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				executor.execute(x, y, tiles[x][y]);
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
	
	public int getWidthInTiles() {
		return width;
	}
	
	public int getHeightInTiles() {
		return height;
	}
	
	/**
	 * Check if the given location is blocked
	 * @param x The x coordinate of the tile to check
	 * @param y The y coordinate of the tile to check
	 * @return True if the location is blocked
	 */
	public boolean blocked(int x, int y) {
		return getCell(x, y);
	}
	
}