package igrek.robopath.pathfinder.coop;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
/**
 *
 * @author hakan eryargi (r a f t)
 */
public class Unit {
    
    private static int lastId = 0;
    private static synchronized final int nextId() {
        return lastId++;
    }
    
    public final int id = nextId();
    
//    NodePool pool = null;
    NodePool.Point destination = null;
    
    private int x = 0;
    private int z = 0;
    
    private int pathIndex = 0;
    private final List<PathPoint> path = new ArrayList<PathPoint>();
    
    /** Creates a new instance of Unit */
    public Unit() {
    }
    
    public void setLocation(int x, int z) {
        this.x = x;
        this.z = z;
    }
    
    public NodePool.Point getLocation() {
        return new NodePool.Point(x, z);
    }
    public int getPathIndex() {
        return pathIndex;
    }
    
    public boolean reached() {
        return (x == destination.x) && (z == destination.z);
    }
    
    public void next() {
        pathIndex++;
        if (pathIndex < path.size()) {
            PathPoint location = path.get(pathIndex);
            setLocation(location.x, location.z);
        }
        
    }
    
    public void setDestination(int destX, int destZ) {
        this.destination = new NodePool.Point(destX, destZ);
    }
    
    public NodePool.Point getDestination() {
        return destination;
    }
    
    public void setPath(List<PathPoint> path) {
        this.path.clear();
        this.path.addAll(path);
        
        this.pathIndex = 0;
        if (! path.isEmpty()) {
            PathPoint location = path.get(0);
            setLocation(location.x, location.z);
        }
    }
    
    public List<PathPoint> getPath() {
        return Collections.unmodifiableList(path);
    }
    
//
//    void moveTo(int x, int z) {
//        int fromX = this.x;
//        int fromZ = this.z;
//
//        this.x = x;
//        this.z = z;
//
//        pool.moveUnit(this, fromX, fromZ);
//    }
//
//    boolean isAt(int x, int z) {
//        return (this.x == x) && (this.z == z);
//    }
    
    public int hashCode() {
        return id;
    }
    
    public boolean equals(Object o) {
        return (o instanceof Unit) ? equals((Unit)o) : false;
    }
    
    public boolean equals(Unit other) {
        return this.id == other.id;
    }
    
    public String toString() {
        return "Unit: " + id;
    }
    
    
    public static class PathPoint {
        public final int x;
        public final int z;
        public final long t;
        
        public PathPoint(NodePool.Node node) {
            this(node.x, node.z, node.t);
        }
        
        public PathPoint(int x, int z, long t) {
            this.x = x;
            this.z = z;
            this.t = t;
        }
        
        public boolean isSamePlace(PathPoint other) {
            return (this.x == other.x) && (this.z == other.z);
        }
    }
    
    
}
