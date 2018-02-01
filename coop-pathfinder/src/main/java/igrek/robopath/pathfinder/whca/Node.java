package igrek.robopath.pathfinder.whca;

/**
 * A single node in the search graph
 */
class Node implements Comparable<Node> {
	/** The x coordinate of the node */
	private int x;
	/** The y coordinate of the node */
	private int y;
	private int t;
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
	public Node(int x, int y, int t) {
		this.x = x;
		this.y = y;
		this.t = t;
	}
	
	public int getX() {
		return x;
	}
	
	public int getY() {
		return y;
	}
	
	public int getT() {
		return t;
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
	
	/**
	 * Set the parent of this node
	 * @param parent The parent node which lead us to this node
	 * @return The depth we have no reached in searching
	 */
	public void setParent(Node parent) {
		this.parent = parent;
	}
	
	public float getF() {
		return heuristic + cost;
	}
	
	@Override
	public int compareTo(Node o) {
		float f = getF();
		float of = o.getF();
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
	
	@Override
	public String toString() {
		return "(" + x + ", " + y + ", " + t + "; f=" + getF() + "; h=" + getHeuristic() + ")";
	}
	
}
