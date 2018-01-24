package igrek.robopath.pathfinder.my2dastar;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.PriorityQueue;

import igrek.robopath.common.TileMap;

public class My2DPathFinder {
	
	private Logger logger = LoggerFactory.getLogger(this.getClass());
	
	/** The set of nodes that have been searched through */
	private List<Node> closed = new ArrayList<>();
	/** The set of nodes that we do not yet consider fully searched */
	private PriorityQueue<Node> open = new PriorityQueue<>();
	/** The complete set of nodes across the map */
	private Node[][] nodes;
	
	private TileMap map;
	
	public My2DPathFinder(TileMap map) {
		this.map = map;
	}
	
	private int width() {
		return map.getWidthInTiles();
	}
	
	private int height() {
		return map.getHeightInTiles();
	}
	
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
	public Path findPath(int sx, int sy, int tx, int ty) {
		// initial state for A*. The closed group is empty. Only the starting
		// tile is in the open list and it'e're already there
		closed.clear();
		open.clear();
		
		nodes = new Node[width()][height()];
		for (int x = 0; x < width(); x++) {
			for (int y = 0; y < height(); y++) {
				nodes[x][y] = new Node(x, y);
			}
		}
		
		nodes[tx][ty].setParent(null);
		nodes[sx][sy].setCost(0);
		//Dodajemy pole startowe (lub węzeł) do Listy Otwartych.
		open.add(nodes[sx][sy]);
		
		// first check, if the destination is blocked, we can't get there
		if (map.blocked(tx, ty))
			return null;
		//jeśli punkt docelowy jest punktem startowym - brak ścieżki
		if (sx == tx && sy == ty)
			return null;
		
		//dopóki lista otwartych nie jest pusta
		while (!open.isEmpty()) {
			// pull out the first node in our open list, this is determined to
			// be the most likely to be the next step based on our heuristic
			//Szukamy pola o najniższej wartości F na Liście Otwartych. Czynimy je aktualnym polem
			Node current = open.peek();
			//jeśli current jest węzłem docelowym
			if (current.getX() == tx && current.getY() == ty) {
				// At this point we've definitely found a path so we can uses the parent
				// references of the nodes to find out way from the target location back
				// to the start recording the nodes on the way.
				//Zapisujemy ścieżkę. Krocząc w kierunku od pola docelowego do startowego, przeskakujemy z kolejnych pól na im przypisane pola rodziców, aż do osiągnięcia pola startowego.
				Path path = new Path();
				Node target = nodes[tx][ty];
				while (target != nodes[sx][sy]) {
					path.prependStep(target.getX(), target.getY());
					target = target.getParent();
					if (target == null) {
						logger.warn("dupa");
					}
				}
				path.prependStep(sx, sy);
				return path;
			}
			//Aktualne pole przesuwamy do Listy Zamkniętych.
			open.remove(current);
			closed.add(current);
			// search through all the neighbours of the current node evaluating
			// them as next steps
			//Dla każdego z wybranych przyległych pól (sasiad) do pola aktualnego
			List<Node> neighbours = availableNeighbours(current);
			for (Node neighbour : neighbours) {
				
				//jeśli NIE-MOŻNA go przejść, ignorujemy je.
				if (!isValidLocation(sx, sy, neighbour.getX(), neighbour.getY()))
					continue;
				
				if (!isValidMove(current.getX(), current.getY(), neighbour.getX(), neighbour.getY()))
					continue;
				
				// the cost to get to this node is cost the current plus the movement
				// cost to reach this node. Note that the heursitic value is only used
				// in the sorted open list
				float nextStepCost = current.getCost() + getMovementCost(current.getX(), current.getY(), neighbour
						.getX(), neighbour.getY());
				
				// if the new cost we've determined for this node is lower than
				// it has been previously makes sure the node hasn'e've
				// determined that there might have been a better path to get to
				// this node so it needs to be re-evaluated
				if (nextStepCost < neighbour.getCost()) {
					if (open.contains(neighbour)) {
						open.remove(neighbour);
					}
					if (closed.contains(neighbour)) {
						closed.remove(neighbour);
					}
				}
				// if the node hasn't already been processed and discarded then
				// reset it's cost to our current cost and add it as a next possible
				// step (i.e. to the open list)
				if (!open.contains(neighbour) && !closed.contains(neighbour)) {
					neighbour.setCost(nextStepCost);
					neighbour.setHeuristic(getHeuristicCost(neighbour.getX(), neighbour.getY(), tx, ty));
					neighbour.setParent(current);
					open.add(neighbour);
				}
				
			}
		}
		
		// since we'e've run out of search there was no path
		return null;
	}
	
	/**
	 * Check if a given location is valid for the supplied mover
	 * @param sx The starting x coordinate
	 * @param sy The starting y coordinate
	 * @param x  The x coordinate of the location to check
	 * @param y  The y coordinate of the location to check
	 * @return True if the location is valid for the given mover
	 */
	protected boolean isValidLocation(int sx, int sy, int x, int y) {
		if (x < 0 || y < 0 || x >= width() || y >= height())
			return false;
		
		if (map.blocked(x, y))
			return false;
		
		return true;
	}
	
	/**
	 * @param sx from x
	 * @param sy from y
	 * @param x  to x
	 * @param y  to y
	 * @return is move allowed
	 */
	protected boolean isValidMove(int sx, int sy, int x, int y) {
		if (!isValidLocation(sx, sy, x, y)) {
			return false;
		}
		// diagonal move not possible when one cell is blocked
		int dx = abs(sx - x);
		int dy = abs(sy - y);
		// diagonal move
		if (dx == 1 && dy == 1) {
			if (map.blocked(x, y) || map.blocked(sx, sy) || map.blocked(sx, y) || map.blocked(x, sy)) {
				return false;
			}
		}
		
		return true;
	}
	
	private int abs(int x) {
		return x >= 0 ? x : -x;
	}
	
	protected float getMovementCost(int sx, int sy, int tx, int ty) {
		//		float dx = tx - sx;
		//		float dy = ty - sy;
		//		return (float) (Math.sqrt((dx * dx) + (dy * dy)));
		return Math.max(Math.abs(tx - sx), Math.abs(ty - sy));
	}
	
	protected float getHeuristicCost(int x, int y, int tx, int ty) {
		//		return (float) Math.max(Math.abs(tx - x), Math.abs(ty - y));
		return (float) Math.hypot(tx - x, ty - y);
	}
	
	private List<Node> availableNeighbours(Node current) {
		List<Node> neighbours = new LinkedList<>();
		for (int dx = -1; dx <= 1; dx++) {
			for (int dy = -1; dy <= 1; dy++) {
				if (dx == 0 && dy == 0)
					continue;
				// determine the location of the neighbour and evaluate it
				int xp = current.getX() + dx;
				int yp = current.getY() + dy;
				// validate out of bounds
				if ((xp < 0) || (yp < 0) || (xp >= width()) || (yp >= height()))
					continue;
				neighbours.add(nodes[xp][yp]);
			}
		}
		// możliwe czekanie w tym samym miejscu - jako ostatnia propozycja
		neighbours.add(nodes[current.getX()][current.getY()]);
		return neighbours;
	}
	
}
