package igrek.robopath.pathfinder.astar;

/**
 * A single node in the search graph
 */
class Node implements Comparable<Node> {
	/** The x coordinate of the node */
	private int x;
	/** The y coordinate of the node */
	private int y;
	/** The path cost for this node (g) */
	private float cost;
	/** The parent of this node, how we reached it in the search */
	private Node parent;
	/** The heuristic cost of this node (h) */
	private float heuristic;
	
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
	
	public void setCost(float cost) {
		this.cost = cost;
	}
	
	public void setHeuristic(float heuristic) {
		this.heuristic = heuristic;
	}
	
	public void setParent(Node parent) {
		this.parent = parent;
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
	
	@Override
	public boolean equals(Object obj) {
		//		if (!(obj instanceof Node))
		//			return false;
		//		Node n = (Node) obj;
		//		return n.x == x && n.y == y;
		return super.equals(obj);
	}
	
	public boolean sameXY(Node n2) {
		return this.x == n2.x && this.y == n2.y;
	}
}
