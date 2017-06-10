package igrek.robopath.pathfinder;

public interface PathFinder {
	/**
	 * Find a path from the starting location provided (sx,sy) to the target
	 * location (tx,ty) avoiding blockages and attempting to honour costs
	 * provided by the tile map.
	 * @param sx The x coordinate of the start location
	 * @param sy The y coordinate of the start location
	 * @param tx The x coordinate of the target location
	 * @param ty Teh y coordinate of the target location
	 * @return The path found from start to end, or null if no path can be found.
	 */
	Path findPath(int sx, int sy, int tx, int ty);
}
