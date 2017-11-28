package igrek.robopath.ui.robomove;

import igrek.robopath.pathfinder.TileBasedMap;

public class TestTileMap implements TileBasedMap {
	
	private int width;
	private int height;
	private TileCellType[][] tiles;
	
	public TestTileMap(int width, int height) {
		this.width = width;
		this.height = height;
		tiles = new TileCellType[getWidthInTiles()][getHeightInTiles()];
		for (int x = 0; x < tiles.length; x++) {
			for (int y = 0; y < tiles[x].length; y++) {
				tiles[x][y] = TileCellType.EMPTY;
			}
		}
	}
	
	public void set(int x, int y, TileCellType type) {
		if (x < 0 || y < 0 || x >= getWidthInTiles() || y >= getHeightInTiles())
			return;
		tiles[x][y] = type;
	}
	
	public TileCellType get(int x, int y) {
		if (x < 0 || y < 0 || x >= getWidthInTiles() || y >= getHeightInTiles())
			return null;
		return tiles[x][y];
	}
	
	@Override
	public int getWidthInTiles() {
		return width;
	}
	
	@Override
	public int getHeightInTiles() {
		return height;
	}
	
	@Override
	public boolean blocked(int x, int y) {
		return tiles[x][y] == TileCellType.BLOCKED;
	}
	
	@Override
	public float getCost(int sx, int sy, int tx, int ty) {
		return 1;
	}
	
}