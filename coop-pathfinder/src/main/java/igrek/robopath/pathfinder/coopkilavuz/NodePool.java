package igrek.robopath.pathfinder.coopkilavuz;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 *
 * @author hakan eryargi (r a f t)
 */
public class NodePool {
    
    static final boolean RESERVE_TWO = true;
    
    private final SortedMap<String, Node> usedNodes = new TreeMap<String, Node>();
    private final List<Node> pool = new ArrayList<Node>();
    
    private final SortedMap<String, Unit> reserved = new TreeMap<String, Unit>();
    
    private final SortedMap<Integer, Unit> units = new TreeMap<Integer, Unit>();
//    private final UnitLocations unitLocations = new UnitLocations();
    
    final Grid grid;
    
    /** Creates a new instance of NodePool */
    public NodePool(Grid grid) {
        this.grid = grid;
    }
    
    public void addUnit(Unit unit) {
        if (units.put(unit.id, unit) != null)
            throw new IllegalStateException("already has unit, id: " + unit.id);
//        unit.pool = this;
//        unitLocations.addUnit(unit);
    }
    
//    public void moveUnit(Unit unit, int fromX, int fromZ) {
//        unitLocations.moveUnit(unit, fromX, fromZ);
//    }
    
    Map<String, Unit> getReserved() {
        return Collections.unmodifiableMap(reserved);
    }
    
    public boolean isReserved(Node node) {
        return isReserved(node.x, node.z, node.t);
    }
    
    public boolean isReserved(int x, int y, long t) {
        String key = x + ":" + y + ":" + t;
        return reserved.containsKey(key);
    }
    
    public void reserve(Unit unit, Node node) {
        reserve(unit, node.x, node.z, node.t);
    }
    public void reserve(Unit unit, int x, int y, long t) {
        String key = x + ":" + y + ":" + t;
        Unit oldUnit = reserved.get(key);
        if (oldUnit != null)
            throw new IllegalStateException("already reserved: " + key + " by " + oldUnit.id + " attempting: " + unit.id);
        reserved.put(key, unit);
    }
    
    public void reclaim(Node node) {
        reclaim(node.x, node.z, node.t);
    }
    public void reclaim(int x, int y, long t) {
        String key = x + ":" + y + ":" + t;
        if (reserved.remove(key) == null)
            throw new IllegalStateException("not reserved: " + key);
    }
    public void reclaimAll() {
        reserved.clear();
    }
    
    public void releaseAllNodes() {
        pool.addAll(usedNodes.values());
        usedNodes.clear();
    }
    
    private int count = 0;
    public Node acquireNode(int x, int y, long t) {
        String key = x + ":" + y + ":" + t;
        Node node = usedNodes.get(key);
        if (node == null) {
            if (pool.isEmpty()) {
                node = new Node(x, y, t);
//                System.out.println("created new: " + count++ + " for " + key);
            } else {
                node = pool.remove(0);
                node.init(x, y, t);
//                System.out.println("using cached for " + key);
            }
            usedNodes.put(key, node);
        } else {
//            System.out.println("using created for " + key);
        }
        return node;
    }
    
    public class Node extends TimeAStar.Node {
        int x;
        int z;
        long t;
        
        private List<TimeAStar.Transition> transitions;
        
        private Node(int x, int z, long t) {
            init(x, z, t);
        }
        
        private void init(int x, int z, long t) {
            this.x = x;
            this.z = z;
            this.t = t;
            transitions = null;
        }
        
        public Collection<TimeAStar.Transition> getTransitions() {
            if (transitions == null) {
                transitions = new ArrayList<TimeAStar.Transition>();
                for (Grid.Node node : grid.getNeighbours(x, z)) {
                    transitions.add(new Transition(this, acquireNode(node.x, node.y, t + 1)));
                }
                // wait
                transitions.add(new Transition(this, acquireNode(x, z, t + 1)));
            }
            return transitions;
        }
        /** actual timeless cost */
        public float getActualTimelessCost(TimeAStar.Node dest) {
            Node node = (Node) dest;
            return grid.getActualCost(x, z, node.x, node.z);
        }
        
        public String toString() {
            return "(" + x + ", " + z + ", " + t + ")";
        }
        
    }
    
    public class Transition implements TimeAStar.Transition {
        final Node fromNode;
        final Node toNode;
        final boolean wait;
        
        private Transition(Node fromNode, Node toNode) {
            this.fromNode = fromNode;
            this.toNode = toNode;
            this.wait = (fromNode.x == toNode.x) && (fromNode.z == toNode.z);
        }
        
        public TimeAStar.Node fromNode() {
            return fromNode;
        }
        
        public TimeAStar.Node toNode() {
            return toNode;
        }
        
        public float getCost(Unit unit) {
            if (isReserved(toNode))
                return INFINITE_COST;
            
            if (RESERVE_TWO) {
                if (!wait && isReserved(toNode.x, toNode.z, toNode.t - 1))
                    return INFINITE_COST;
            }
            
//            if (unitLocations.isThereUnitAt(unit, toNode.x, toNode.y))
//                return INFINITE_COST;
            
            if (wait && (unit.getDestination().x == fromNode.x) &&
                    (unit.getDestination().z == fromNode.z)) {
//                System.out.println("---------------");
                return 0;
            }
            return 1;
        }
        
        public String toString() {
            //return "tr: " + fromNode + " - " + toNode;
            return "tr to: " + toNode;
        }
    }
    
    
//    private class UnitLocations {
//
//        private final SortedMap<XZ, List<Unit>> units = new TreeMap<XZ, List<Unit>>();
//
//        private void addUnit(Unit unit) {
//            XZ xz = new XZ(unit);
//
//            List<Unit> units = this.units.get(xz);
//            if (units == null) {
//                units = new ArrayList<Unit>();
//                this.units.put(xz, units);
//            } else {
//                if (units.contains(unit))
//                    throw new IllegalStateException("already has unit, id: " + unit.id);
//            }
//            units.add(unit);
//        }
//
//        private void removeUnit(int x, int z, Unit unit) {
//            XZ xz = new XZ(x, z);
//
//            List<Unit> units = this.units.get(xz);
//            if ((units == null) && !units.remove(unit))
//                throw new IllegalStateException("unit wasnt at " + x + ", " + z);
//        }
//
//        private void moveUnit(Unit unit, int fromX, int fromZ) {
//            if ((fromX == unit.x) && (fromZ == unit.z))
//                return;
//
//            removeUnit(fromX, fromZ, unit);
//            addUnit(unit);
//        }
//
//        private boolean isThereUnitAt(Unit otherThen, int x, int z) {
//            XZ xz = new XZ(x, z);
//            List<Unit> units = this.units.get(xz);
//
//            if ((units == null))
//                return false;
//
//            switch (units.size()) {
//                case 0:
//                    return false;
//                case 1:
//                    return units.get(0).id != otherThen.id;
//                default:
//                    return true;
//            }
//        }
//
//        private class XZ implements Comparable<XZ> {
//            private final int x, z;
//
//            private XZ(Unit unit) {
//                this(unit.x, unit.z);
//            }
//
//            private XZ(int x, int z) {
//                this.x = x;
//                this.z = z;
//            }
//
//            public int compareTo(XZ other) {
//                if ((x == other.x) && (z == other.z))
//                    return 0;
//
//                if (x < other.x)
//                    return -1;
//                if (x > other.x)
//                    return 1;
//                if (z < other.z)
//                    return -1;
//                if (z > other.z)
//                    return 1;
//
//                throw new AssertionError(this + " " + other);
//            }
//        }
//    }
    
    public static class Point {
        public final int x;
        public final int z;
        
        Point(int x, int z) {
            this.x = x;
            this.z = z;
        }

        public boolean equals(Object o) {
            return (o instanceof Point) ? equals((Point)o) : false;
        }
        
        public boolean equals(Point other) {
            return (this.x == other.x) && (this.z == other.z);
        }
        
        public String toString() {
            return "P " + x + "," + z;
        }
        
    }
}
