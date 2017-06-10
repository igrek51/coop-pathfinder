package igrek.robopath.pathfinder;

public class ClosestHeuristic implements AStarHeuristic {
	/**
	 * @see AStarHeuristic#getCost(TileBasedMap, int, int, int, int)
	 */
	public float getCost(TileBasedMap map, int x, int y, int tx, int ty) {
		float dx = tx - x;
		float dy = ty - y;
		
		return (float) (Math.sqrt((dx * dx) + (dy * dy)));
	}
	
}