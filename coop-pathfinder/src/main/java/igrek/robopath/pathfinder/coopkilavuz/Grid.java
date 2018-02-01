package igrek.robopath.pathfinder.coopkilavuz;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import raft.kilavuz.runtime.AStar;
import raft.kilavuz.runtime.NoPathException;
import raft.kilavuz.runtime.PathContext;

/**
 * @author hakan eryargi (r a f t)
 */
public class Grid {
	//    static final String[] GRID = new String[] {
	//        "....X.",
	//        "..X...",
	//        ".X..X.",
	//        "....X.",
	//        "..XX..",
	//        ".X...X"
	//    };
	//    static final String[] GRID = new String[] {
	//        ".......",
	//        "XXX.XXX",
	//        "XXX.XXX",
	//        "XXX.XXX",
	//        "XXX.XXX",
	//        "XXX.XXX",
	//        "XXX.XXX",
	//        "XXX.XXX",
	//        "......."
	//    };
	//    static final String[] GRID = new String[] {
	//        "........XX",
	//        "...XXXX.XX",
	//        "....X.X.XX",
	//        "..X...X...",
	//        ".XXXXXXXX.",
	//        "......X...",
	//        ".X..XXX.XX",
	//        ".X........",
	//        ".XXXX.XXXX",
	//        "..........",
	//    };
	static final String[] GRID = new String[]{"........X..X.", "...XXXX.X..X.", "....X.X.X..X.", "..X...X......", ".XXXXXXXX..X.", "......X....X.", ".X..XXX.XX.X.", ".X...........", ".XXXX.XXXXXX.", ".............",};
	
	public final int rows;
	public final int columns;
	
	final Node[][] grid;
	public Set<Node> unwalkables = new HashSet<Node>(); // tmp
	
	public final SortedMap<Integer, Node> nodes = new TreeMap<Integer, Node>();
	private final SortedMap<NodePair, Float> actualCosts = new TreeMap<NodePair, Float>();
	
	/** Creates a new instance of Grid */
	public Grid() {
		this.rows = GRID.length;
		this.columns = GRID[0].length();
		
		this.grid = new Node[columns][rows];
		
		for (int x = 0; x < columns; x++) {
			for (int y = 0; y < rows; y++) {
				Node node = new Node(x, y);
				grid[x][y] = node;
				nodes.put(node.getId(), node);
				
				if (GRID[y].charAt(x) == 'X')
					unwalkables.add(node);
			}
		}
		
		update();
	}
	
	public void update() {
		initializeNeigbours();
		calculateActualCosts();
	}
	
	public Grid(int rows, int columns) {
		this.rows = rows;
		this.columns = columns;
		
		this.grid = new Node[columns][rows];
		
		for (int x = 0; x < columns; x++) {
			for (int y = 0; y < rows; y++) {
				Node node = new Node(x, y);
				grid[x][y] = node;
				nodes.put(node.getId(), node);
			}
		}
		
		update();
	}
	
	public void makeUnwalkable(int x, int y) {
		Node node = grid[x][y];
		if (node != null) {
			unwalkables.add(node);
		}
	}
	
	public void makeWalkable(int x, int y) {
		Node node = grid[x][y];
		if (node != null) {
			unwalkables.remove(node);
		}
	}
	
	public void setWalkable(int x, int y, boolean walkable) {
		Node node = grid[x][y];
		if (node != null) {
			if (!walkable && !unwalkables.contains(node)) {
				unwalkables.add(node);
			} else if (walkable && unwalkables.contains(node)) {
				unwalkables.remove(node);
			}
		}
	}
	
	
	private void initializeNeigbours() {
		for (int x = 0; x < columns; x++) {
			for (int y = 0; y < rows; y++) {
				Node node = grid[x][y];
				List<Node> neighbours = new ArrayList<Node>();
				for (int xi = -1; xi <= 1; xi++) {
					for (int yi = -1; yi <= 1; yi++) {
						if ((xi == 0) && (yi == 0))
							continue;
						if ((xi != 0) && (yi != 0))
							continue;
						try {
							Node neighbour = grid[x + xi][y + yi];
							if (getCost(node, neighbour) >= 0)
								neighbours.add(neighbour);
							
						} catch (ArrayIndexOutOfBoundsException e) {
						}
					}
				}
				//                System.out.println("neigbours of " + node);
				//                System.out.println("\t" + neighbours);
				node.setNeighbours(neighbours);
			}
		}
	}
	
	private void calculateActualCosts() {
		AStar astar = new AStar();
		PathContext context = new PathContext();
		
		for (Node from : nodes.values()) {
			for (Node to : nodes.values()) {
				NodePair pair = new NodePair(from, to);
				float cost = AStar.Transition.INFINITE_COST;
				
				try {
					AStar.Path path = astar.findPath(from, to, context);
					cost = path.cost;
					
					//                    System.out.println(pair + "\t" + cost);
					//                    System.out.print("\t\t" + path.startNode);
					//                    for (AStar.Transition t : path.transitions)
					//                        System.out.print(t.toNode());
					//                    System.out.println("");
				} catch (NoPathException npe) {
				}
				
				actualCosts.put(pair, cost);
			}
		}
	}
	
	
	private float getCost(Node from, Node to) {
		// tmp
		if (unwalkables.contains(to) || unwalkables.contains(from))
			return TimeAStar.Transition.INFINITE_COST;
		
		if ((from.x == to.x) || (from.y == to.y))
			return 1f;
		return 1.4f;
	}
	
	List<Node> getNeighbours(int x, int y) {
		return grid[x][y].neighbours;
	}
	
	/** returns the timeless precalculated cost */
	float getActualCost(int fromX, int fromY, int toX, int toY) {
		Node from = grid[fromX][fromY];
		Node to = grid[toX][toY];
		float cost = actualCosts.get(new NodePair(from, to));
		//        System.out.println("\t" + fromX + "," + fromY + " - " + toX  + "," + toY + ":\t" + cost);
		return cost;
	}
	
	
	public class Node extends AStar.Node {
		public final int x;
		public final int y;
		private List<Node> neighbours;
		private List<AStar.Transition> transitions;
		
		private Node(int x, int y) {
			this.x = x;
			this.y = y;
		}
		
		private void setNeighbours(List<Node> neighbours) {
			this.neighbours = neighbours;
			
			this.transitions = new ArrayList<AStar.Transition>();
			for (Node neighbour : neighbours)
				this.transitions.add(new Transition(this, neighbour));
		}
		
		/** manhattan distance */
		public float getCostEstimate(AStar.Node dest, PathContext context) {
			Node node = (Node) dest;
			return (Math.abs(x - node.x) + Math.abs(y - node.y));
		}
		
		public Collection<AStar.Transition> getTransitions() {
			return transitions;
		}
		
		public String toString() {
			return "(" + x + ", " + y + ")";
		}
	}
	
	public class Transition implements AStar.Transition {
		final Node fromNode, toNode;
		
		private Transition(Node fromNode, Node toNode) {
			this.fromNode = fromNode;
			this.toNode = toNode;
		}
		
		public AStar.Node fromNode() {
			return fromNode;
		}
		
		public AStar.Node toNode() {
			return toNode;
		}
		
		/** manhattan distance */
		public float getCost(PathContext context) {
			return (Math.abs(fromNode.x - toNode.x) + Math.abs(fromNode.y - toNode.y));
		}
		
		public String toString() {
			return "tr: " + fromNode + " - " + toNode;
		}
	}
	
	private class NodePair implements Comparable<NodePair> {
		final Node from;
		final Node to;
		
		private NodePair(Node from, Node to) {
			this.from = from;
			this.to = to;
		}
		
		public int compareTo(NodePair other) {
			if ((from.getId() == other.from.getId()) && (to.getId() == other.to.getId())) {
				return 0;
			} else if (from.getId() < other.from.getId()) {
				return -1;
			} else if (from.getId() > other.from.getId()) {
				return 1;
			} else {
				return (to.getId() > other.to.getId()) ? 1 : -1;
			}
		}
		
		public String toString() {
			return "(" + from.x + ", " + from.y + ") - (" + to.x + ", " + to.y + ")";
		}
	}
	
	static void paintPath(Unit unit) {
		System.out.println(unit);
		List<Unit.PathPoint> path = unit.getPath();
		
		char[][] chars = new char[GRID.length][GRID[0].length()];
		for (int i = 0; i < GRID.length; i++) {
			chars[i] = GRID[i].toCharArray();
		}
		
		for (int i = 0; i < path.size(); i++) {
			Unit.PathPoint p = path.get(i);
			chars[p.z][p.x] = (char) ('a' + i);
		}
		System.out.println("0.2.4.6.8.0");
		for (char[] c : chars)
			System.out.println(new String(c));
		System.out.println("-----------");
	}
	
	
	static int[] oneFrom = {0, 0};
	static int[] oneTo = {5, 0};
	
	static int[] twoFrom = {5, 0};
	static int[] twoTo = {3, 0};
	
	public static void main(String[] args) throws Exception {
		//        Grid grid = new Grid();
		//        NodePool pool = new NodePool(grid);
		//        TimeAStar timeAStar = new TimeAStar();
		//        TimeAStar.Path path = null;
		
		Coordinater coordinater = new Coordinater(8);
		
		Unit unit1 = new Unit();
		unit1.setLocation(oneFrom[0], oneFrom[1]);
		unit1.setDestination(oneTo[0], oneTo[1]);
		
		Unit unit2 = new Unit();
		unit2.setLocation(twoFrom[0], twoFrom[1]);
		unit2.setDestination(twoTo[0], twoTo[1]);
		
		coordinater.addUnit(unit1);
		coordinater.addUnit(unit2);
		
		for (int i = 0; i < 30; i++) {
			if (coordinater.iterate()) {
				
				paintPath(unit1);
				paintPath(unit2);
			}
			unit1.next();
			unit2.next();
		}
		
		
		//        paintPath(path);
		//        reservePath(pool, path);
		//
		//        paintPath(path);
		//        reservePath(pool, path);
	}
}
