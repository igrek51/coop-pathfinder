package igrek.robopath.pathfinder.coopkilavuz;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
/**
 *
 * @author hakan eryargi (r a f t)
 */
public class DepthTest {
    static final String[] GRID = new String[] {
        ".....",
        ".X...",
        ".X...",
        ".X...",
        ".X..."
    };
    
    private final Node[][] grid;
    private Set<Node> unwalkables = new HashSet<Node>(); // tmp
    
    /** Creates a new instance of DepthTest */
    public DepthTest() {
        int rows = GRID.length;
        int columns = GRID[0].length();
        
        this.grid = new Node[columns][rows];
        
        for (int x = 0; x < columns; x++) {
            for (int y = 0; y < rows; y++) {
                Node node = new Node(x, y);
                grid[x][y] = node;
                
                if (GRID[y].charAt(x) == 'X')
                    unwalkables.add(node);
            }
        }
        
        // initialize neigbours
        for (int x = 0; x < columns; x++) {
            for (int y = 0; y < rows; y++) {
                Node node = grid[x][y];
                List<TimeAStar.Transition> neighbours = new ArrayList<TimeAStar.Transition>();
                for (int xi = -1; xi <=1; xi++) {
                    for (int yi = -1; yi <=1; yi++) {
                        if ((xi == 0) && (yi == 0))
                            continue;
                        if ((xi != 0) && (yi != 0))
                            continue;
                        try {
                            Node neigbour = grid[x+xi][y+yi];
                            neighbours.add(new Transition(node, neigbour, getCost(node, neigbour)));
                        } catch (ArrayIndexOutOfBoundsException e) {}
                    }
                }
//                System.out.println("neigbours of " + node);
//                System.out.println("\t" + neighbours);
                node.transitions = Collections.unmodifiableList(neighbours);
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
    
    public class Node extends TimeAStar.Node {
        public final int x;
        public final int y;
        private List<TimeAStar.Transition> transitions;
        
        private Node(int x, int y) {
            this.x = x;
            this.y = y;
        }
        
        public Collection<TimeAStar.Transition> getTransitions() {
            return transitions;
        }
        /** manhattan distance */
        public float getActualTimelessCost(TimeAStar.Node dest) {
            Node node = (Node) dest;
            return (Math.abs(x - node.x) + Math.abs(y - node.y));
        }
        
        public String toString() {
            return "(" + x + ", " + y + ")";
        }
        
    }
    
    public class Transition implements TimeAStar.Transition {
        final Node fromNode, toNode;
        final float cost;
        
        private Transition(Node fromNode, Node toNode, float cost) {
            this.fromNode = fromNode;
            this.toNode = toNode;
            this.cost = cost;
        }
        
        public TimeAStar.Node fromNode() {
            return fromNode;
        }
        
        public TimeAStar.Node toNode() {
            return toNode;
        }
        public float getCost(Unit init) {
            return cost;
        }
        
        public String toString() {
            return "tr: " + fromNode + " - " + toNode;
        }
    }

    
    public static void main(String[] args) throws Exception {
        System.out.println("0.2.4.6.8.0");
        for (String s : GRID)
            System.out.println(s);
        System.out.println("0.2.4.6.8.0");
        
        DepthTest depthTest = new DepthTest();
        TimeAStar timeAStar = new TimeAStar();
        Unit unit = new Unit();
        
        Node from = depthTest.grid[0][0];
        Node to = depthTest.grid[4][4];
        
        TimeAStar.Path path = timeAStar.findPath(from, to, unit, 7);
        System.out.println(path.startNode);
        for (TimeAStar.Transition t : path.transitions) {
            System.out.println(t.toNode());
        }
    }
    
    
}
