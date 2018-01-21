package igrek.robopath.pathfinder.astar;

/**
 * A single node in the search graph
 */
class Node implements Comparable<Node> {
	/** The x coordinate of the node */
	private int x;
	/** The y coordinate of the node */
	private int y;
	/** The path cost for this node */
	private float cost;
	/** The parent of this node, how we reached it in the search */
	private Node parent;
	/** The heuristic cost of this node */
	private float heuristic;
	/** The search depth of this node */
	private int depth;
	
	/**
	 * Create a new node
	 * @param x The x coordinate of the node
	 * @param y The y coordinate of the node
	 */
	public Node(int x, int y) {
		this.x = x;
		this.y = y;
	}
	
	public int getX() {
		return x;
	}
	
	public int getY() {
		return y;
	}
	
	public float getCost() {
		return cost;
	}
	
	public Node getParent() {
		return parent;
	}
	
	public float getHeuristic() {
		return heuristic;
	}
	
	public int getDepth() {
		return depth;
	}
	
	public void setCost(float cost) {
		this.cost = cost;
	}
	
	public void setDepth(int depth) {
		this.depth = depth;
	}
	
	public void setHeuristic(float heuristic) {
		this.heuristic = heuristic;
	}
	
	/**
	 * Set the parent of this node
	 * @param parent The parent node which lead us to this node
	 * @return The depth we have no reached in searching
	 */
	public int setParent(Node parent) {
		this.parent = parent;
		if (parent == null) {
			depth = 0;
		} else {
			depth = parent.depth + 1;
		}
		return depth;
	}
	
	@Override
	public int compareTo(Node o) {
		
		float f = heuristic + cost;
		float of = o.heuristic + o.cost;
		
		if (f < of) {
			return -1;
		} else if (f > of) {
			return 1;
		} else {
			return 0;
		}
	}
}
