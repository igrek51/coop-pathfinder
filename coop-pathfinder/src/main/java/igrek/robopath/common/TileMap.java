package igrek.robopath.common;

import java.util.Arrays;

public class TileMap {
	
	private int width;
	private int height;
	/**
	 * is blocked
	 */
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
	
	/**
	 * copy constructor
	 * @param source
	 */
	public TileMap(TileMap source) {
		this.width = source.width;
		this.height = source.height;
		tiles = new boolean[this.width][];
		for (int x = 0; x < tiles.length; x++) {
			tiles[x] = Arrays.copyOf(source.tiles[x], source.tiles[x].length);
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
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder("Map:\n");
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				if (tiles[x][y]) {
					sb.append("X");
				} else {
					sb.append(".");
				}
			}
			sb.append("\n");
		}
		return sb.toString();
	}
}