package igrek.robopath.pathfinder.heuristics;

import igrek.robopath.pathfinder.TileBasedMap;

public class ManhattanHeuristic implements AStarHeuristic {
	/**
	 * @see AStarHeuristic#getCost(TileBasedMap, int, int, int, int)
	 */
	public float getCost(TileBasedMap map, int x, int y, int tx, int ty) {
		
		int dx = abs(tx - x);
		int dy = abs(ty - y);
		
		return dx + dy;
	}
	
	private int abs(int x) {
		return x >= 0 ? x : -x;
	}
}