package igrek.robopath.pathfinder.heuristics;


public class ManhattanHeuristic implements AStarHeuristic {
	/**
	 * @see AStarHeuristic#getCost(int, int, int, int)
	 */
	public float getCost(int x, int y, int tx, int ty) {
		
		int dx = abs(tx - x);
		int dy = abs(ty - y);
		
		return dx + dy;
	}
	
	private int abs(int x) {
		return x >= 0 ? x : -x;
	}
}