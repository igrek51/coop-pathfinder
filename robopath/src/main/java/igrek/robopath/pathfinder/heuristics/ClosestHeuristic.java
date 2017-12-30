package igrek.robopath.pathfinder.heuristics;

public class ClosestHeuristic implements AStarHeuristic {
	/**
	 * @see AStarHeuristic#getCost(int, int, int, int)
	 */
	public float getCost(int x, int y, int tx, int ty) {
		float dx = tx - x;
		float dy = ty - y;
		
		return (float) (Math.sqrt((dx * dx) + (dy * dy)));
	}
	
}