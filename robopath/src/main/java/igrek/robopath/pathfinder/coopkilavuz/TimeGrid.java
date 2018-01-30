package igrek.robopath.pathfinder.coopkilavuz;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import raft.kilavuz.runtime.AStar;
import raft.kilavuz.runtime.NoPathException;
import raft.kilavuz.runtime.PathContext;

/**
 *
 * @author hakan eryargi (r a f t)
 */
public class TimeGrid {
    public final int rows;
    public final int columns;
    
    private final Node[][] grid;
    private final SortedMap<Integer, Node> nodes = new TreeMap<Integer, Node>();
    private final SortedMap<NodePair, Float> actualCosts = new TreeMap<NodePair, Float>();
    private Set<Node> unwalkables = new HashSet<Node>(); // tmp
    
    /** Creates a new instance of TimeGrid */
    public TimeGrid(int rows, int columns, InputStream is) throws IOException {
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
        readGrid(is);
        
        // initialize neigbours
        for (int x = 0; x < columns; x++) {
            for (int y = 0; y < rows; y++) {
                Node node = grid[x][y];
                List<AStar.Transition> neighbours = new ArrayList<AStar.Transition>();
                for (int xi = -1; xi <=1; xi++) {
                    for (int yi = -1; yi <=1; yi++) {
                        if ((xi == 0) && (yi == 0))
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
        
        calculateActualCosts();
    }
    
    public void readGrid(InputStream is) throws IOException {
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            String line = null;
            int y = 0;
            while ((line = br.readLine()) != null && y < rows) {
                for (int x = 0; x < line.length() && x < columns; x++) {
                    int value = line.charAt(x);
                    if (value == 'X')
                        unwalkables.add(grid[x][y]);
                }
                y++;
            }
        } finally {
            if (is != null) is.close();
        }
        //System.out.println("unwalkables: " + unwalkables);
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
            return AStar.Transition.INFINITE_COST;
        
        if ((from.x == to.x) || (from.y == to.y))
            return 1f;
        return 1.4f;
    }
    
    
    
    public class Node extends AStar.Node {
        public final int x;
        public final int y;
        private List<AStar.Transition> transitions;
        
        private Node(int x, int y) {
            this.x = x;
            this.y = y;
        }
        
        public Collection<AStar.Transition> getTransitions() {
            return transitions;
        }
        /** manhattan distance */
        public float getCostEstimate(AStar.Node dest, PathContext context) {
            Node node = (Node) dest;
            return (Math.abs(x - node.x) + Math.abs(y - node.y));
        }
        
        public String toString() {
            return "(" + x + ", " + y + ")";
        }
        
    }
    
    public class Transition implements AStar.Transition {
        final Node fromNode, toNode;
        final float cost;
        
        private Transition(Node fromNode, Node toNode, float cost) {
            this.fromNode = fromNode;
            this.toNode = toNode;
            this.cost = cost;
        }
        
        public AStar.Node fromNode() {
            return fromNode;
        }
        
        public AStar.Node toNode() {
            return toNode;
        }
        public float getCost(PathContext context) {
            return cost;
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
    
    public static void main(String[] args) throws Exception {
        System.out.println("usage grid <rows> <columns> <grid file> ");
        
        int rows = Integer.parseInt(args[0]);
        int columns = Integer.parseInt(args[1]);
        
        TimeGrid grid = new TimeGrid(rows, columns, new FileInputStream(args[2]));
    }
}
