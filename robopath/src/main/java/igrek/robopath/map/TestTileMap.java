package igrek.robopath.map;

import igrek.robopath.pathfinder.TileBasedMap;

public class TestTileMap implements TileBasedMap {
	
	private TileCellType[][] tiles;
	
	public TestTileMap() {
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
		return 10;
	}
	
	@Override
	public int getHeightInTiles() {
		return 10;
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