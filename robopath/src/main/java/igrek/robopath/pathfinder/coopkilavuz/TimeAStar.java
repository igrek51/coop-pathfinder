package igrek.robopath.pathfinder.coopkilavuz;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import raft.kilavuz.runtime.NoPathException;
import raft.kilavuz.runtime.PathContext;

/**
 * an generic A* path finding implementation.
 * for those which are unfamiliar with A* we suggest this
 * <a href="http://www.policyalmanac.org/games/aStarTutorial.htm" target="_blank">A*
 * tutorial</a>.
 * @author hakan eryargi (r a f t)
 */
public class TimeAStar {
    private static final boolean DEBUG = false;
    /** closed list is maintained as a BitSet.
     * this is far more faster and normally requires less memory.
     * each Node is assigned an id during creation and if you create more and more
     * nodes in same JVM this may be a problem. more and more means more than ten million.
     * this is default
     * @see java.util.BitSet */
    public static final boolean USE_BITSET = true;
    /** closed list is maintained as a SortedSet<Integer>.
     * this is slower and generally requires more memory.
     * see discussion at USE_BITSET
     * @see #USE_BITSET
     * @see java.util.SortedSet */
    public static final boolean USE_INTSET = false;
    
    /** comparator used to sort nodes in open list.
     * two nodes are considered equal only if they are the same node (one.id == two.id)
     * otherwise they're first ordered with f values then with id's.
     * this is required for contains/remove methods to work properly.  */
    private static final Comparator<Node> comparator = new Comparator<Node>() {
        public int compare(Node one, Node two) {
            if (one.id == two.id)
                return 0;
            if (one.f < two.f)
                return -1;
            if (one.f > two.f)
                return 1;
            return (one.id < two.id) ? -1 : 1;
        }
    };
    
    /** open list in A* algorithm. TreeSet seems to be an efficient implementation for open list.
     * PriorityQueue was another candidate as it's a binary heap but TreeSet performs
     * better. remove and contains methods are much faster which are crucial */
    private final SortedSet<Node> openList = new TreeSet<Node>(comparator);
    
    private final BitSet bitSetClosedList = new BitSet(1024);
    private final SortedSet<Integer> intSetClosedList = new TreeSet<Integer>();
    
//    /** variable to mark usage */
//    private boolean inUse = false;
    
    private final boolean useBitSet;
    
    /** creates a new AStar pathfinder */
    public TimeAStar() {
        this(USE_BITSET);
    }
    
    /** creates a new AStar pathfinder.
     * @param useBitSet if true closed list is stored as a BitSet
     * @see #USE_BITSET
     * @see #USE_INTSET */
    public TimeAStar(boolean useBitSet) {
        this.useBitSet = useBitSet;
    }
    
    /** tries to find path between given nodes
     * @param from source node
     * @param to target node
     * @param context information about path finding enviroment
     * @return found path
     * @throws NoPathException if no path found
     * @see Transition#getCost(PathContext) */
    public synchronized Path findPath(Node from, Node to, Unit unit, int depth) throws NoPathException {
        if (unit == null)
            throw new NullPointerException("context is null");
        
        
        try {
            boolean solved = false;
            Node current = null;
            
            openList.clear();
            if (useBitSet)
                bitSetClosedList.clear();
            else intSetClosedList.clear();
            
            int maxOpenSize = 0; int maxCloseSize = 0; int reachedDepth = 0;
            
            from.transition = null;
            from.h = from.getActualTimelessCost(to);
            if (from.h < 0)
                throw new NoPathException("initial cost: " + from.h);
            from.g = 0;
            from.f = from.h;
            from.depth = 0;
            
            openList.add(from);
            
            while (! openList.isEmpty()) {
                
                current = openList.first();
                if (! openList.remove(current))
                    assert false;
                
                if (useBitSet) {
                    bitSetClosedList.set(current.id);
                } else {
                    intSetClosedList.add(current.id);
                }
                
                
                if (DEBUG) System.out.println("current " + current + ", g: " + current.g
                        + ", h: " + current.h + ", f: " + current.f + ", depth: " + current.depth
                        + " parent: " + ((current.transition == null) ? null : current.transition.fromNode()));
                
                if (current.depth > reachedDepth) {
                    reachedDepth = current.depth;
                    
                    if (reachedDepth == depth) {
                        solved = true;
                        break;
                    }
                }
                
//                if (current.id == to.id) {
//                    solved = true;
//                    break;
//                }
                
                for (Transition transition : current.getTransitions()) {
                    float cost = transition.getCost(unit);
                    if (cost < 0)
                        continue;
                    
                    Node neighbour = transition.toNode();
                    
                    if (useBitSet) {
                        if (bitSetClosedList.get(neighbour.id))
                            continue;
                    } else {
                        if (intSetClosedList.contains(neighbour.id))
                            continue;
                    }
                    
                    if (openList.contains(neighbour)) {
                        // check if this path is better
                        if (current.g + cost < neighbour.g) {
                            
                            if (! openList.remove(neighbour))
                                assert false;
                            
                            neighbour.transition = transition;
                            neighbour.g = current.g + cost;
                            neighbour.f = neighbour.g + neighbour.h;
                            neighbour.depth = current.depth + 1;
                            
                            openList.add(neighbour);
                            
                            if (DEBUG) System.out.println("updated " + neighbour + ", g: " + neighbour.g
                                    + ", h: " + neighbour.h + ", f: " + neighbour.f + ", depth: " + neighbour.depth
                                    + " parent: " + neighbour.transition);
                        }
                        
                    } else { // if neighbour in openList
                        
                        neighbour.transition = transition;
                        neighbour.g = current.g + cost;
                        neighbour.h = neighbour.getActualTimelessCost(to);
                        neighbour.f = neighbour.g + neighbour.h;
                        neighbour.depth = current.depth + 1;
                        
                        openList.add(neighbour);
                        
                        if (DEBUG) System.out.println("\tadded " + neighbour + ", g: " + neighbour.g
                                + ", h: " + neighbour.h + ", f: " + neighbour.f + ", depth: " + neighbour.depth
                                + " parent: " + neighbour.transition);
                        
                    } // if-else neighbour in openList
                    
                    if (DEBUG) {
                        if (openList.size() > maxOpenSize)
                            maxOpenSize = openList.size();
                    }
                }
            }
            if (DEBUG) {
                maxCloseSize = useBitSet ? bitSetClosedList.cardinality() : intSetClosedList.size();
                System.out.println("max open size: " + maxOpenSize + " close: " + maxCloseSize);
            }
            
            //if (reachedDepth >= depth) {
            if (solved) {
                float totalCost = current.g;
                List<Transition> transitions = new ArrayList<Transition>();
                while (current.transition != null) {
                    transitions.add(0, current.transition);
                    current = current.transition.fromNode();
                }
                return new Path(from, transitions, totalCost);
            } else {
                throw new NoPathException();
            }
        } finally {}
    }
    
    // tmp
    public SortedSet<Node> getOpenList() {
        return Collections.unmodifiableSortedSet(openList);
    }
    
    public SortedSet<Integer> getClosedList() {
        if (useBitSet) {
            SortedSet<Integer> result = new TreeSet<Integer>();
            for (int i=bitSetClosedList.nextSetBit(0); i>=0; i=bitSetClosedList.nextSetBit(i+1))
                result.add(i);
            return Collections.unmodifiableSortedSet(result);
        } else {
            return Collections.unmodifiableSortedSet(intSetClosedList);
        }
    }
    
    /** result of a successfull path find attempt */
    public class Path {
        /** first node in path */
        public final Node startNode;
        /** transitions to next nodes in path */
        public final List<Transition> transitions;
        /** more information about pathfinding enviroment  */
//        public final PathContext context;
//        /** actual cost of this path */
        public final float cost;
        
        private Path(Node startNode, List<Transition> transitions, float cost) {
            this.startNode = startNode;
//            this.context = context;
            this.cost = cost;
            this.transitions = Collections.unmodifiableList(transitions);
        }

        public String toString() {
            return "path, cost: " + cost + ", " + transitions;
        }
        
    }
    
    /** base class of all A* nodes */
    public abstract static class Node implements java.io.Serializable {
        private static final long serialVersionUID = 1;
        
        private static int lastId = 0;
        private static synchronized int nextId() { return lastId++; }
        
        /** id of this node, assigned during creation */
        private final int id = nextId();
        
        /** total estimated cost from source to dest passing through this node: g + h */
        private transient float f;
        /** cost of the shortest path found till now from source to this node */
        private transient float g;
        /** estimated cost from this node to destination, ie: heuristic */
        private transient float h;
        /** depth of this node */
        private transient int depth = 0;
        /** transition that leads to this node */
        private transient Transition transition;
        
        /** only subclasses may call this constructor */
        protected Node() {}
        
        /** returns id of this node. id's are assigned during creation of nodes. */
        public final int getId() {
            return id;
        }
        
        /** returns a collection of transitions from this node.
         * if an adjacent node is unreachable result may contain it
         * with a negative cost */
        public abstract Collection<Transition> getTransitions();
        
        /** returns the cost estimate from this node to destination node.
         * if cannot be estimated, returning 0 will also result in finding a solution
         * but generally should take much time */
        public abstract float getActualTimelessCost(Node dest);
        
//        private void readObject(java.io.ObjectInputStream ois) throws
//                java.io.IOException, ClassNotFoundException {
//
//            ois.defaultReadObject();
//            // assign new id to make sure id will be unique
//            // even if nodes are deserialized
//            id = nextId();
//        }
    }
    
    /** a transition between two nodes */
    public static interface Transition {
        /** constant to indicate cost for transition is infinite and
         * hence transition is not possible */
        public static final float INFINITE_COST = -1f;
        
        /** returns the node this transition originates from */
        public Node fromNode();
        /** returns the node this transition leads to */
        public Node toNode();
        /** returns the cost, a negative value means transition is not possible.
         * @param context more information about pathfinding enviroment.
         * @see PathContext */
        public float getCost(Unit unit);
    }
    
}
