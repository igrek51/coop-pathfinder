package igrek.robopath.pathfinder;

import org.junit.Test;

public class AStarPathFinderTest {
	
	@Test
	public void testSimplePath() {
		
		TileBasedMap map = new TileMap();
		PathFinder pathFinder = new AStarPathFinder(map, 0, true);
		Path path = pathFinder.findPath(1, 1, 5, 6);
		
		System.out.println("Found path: " + path);
	}
	
	private class TileMap implements TileBasedMap {
		
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
			if (x == 3 && y == 3)
				return true;
			return false;
		}
		
		@Override
		public float getCost(int sx, int sy, int tx, int ty) {
			return 1;
		}
	}
	
	
}
