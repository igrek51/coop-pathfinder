package igrek.robopath.pathfinder.coop;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import raft.kilavuz.runtime.NoPathException;

/**
 *
 * @author hakan eryargi (r a f t)
 */
public class Coordinater {
    
    public Grid grid = new Grid();
    NodePool pool = new NodePool(grid);
    TimeAStar timeAStar = new TimeAStar();
    
    final List<Unit> newUnits = new ArrayList<Unit>();
    public final SortedMap<Integer, Unit> units = new TreeMap<Integer, Unit>();
    
    private int depth = 10;
    private int currentWindow = 0;
    private Map<Integer, List<Unit>> window = new TreeMap<Integer, List<Unit>>();
    private long currentTime = 0;
    
    
    /** Creates a new instance of Coordinater */
    public Coordinater(int depth) {
        setDepth(depth);
    }
    
    public void reset() {
        units.clear();
        newUnits.clear();
        pool.releaseAllNodes();
        pool.reclaimAll();
    }
    
    public int getDepth() {
        return depth;
    }
    
    private void setDepth(int depth) {
        if (depth < 4)
            throw new IllegalArgumentException("depth: " + depth);
        this.depth = depth;
        distributeUnits();
    }
    
    public void addUnit(Unit unit) {
        if (units.put(unit.id, unit) != null)
            throw new IllegalStateException("already has unit, id: " + unit.id);
        newUnits.add(unit);
    }
    
    /** evenly and randomly distributes units among window */
    private void distributeUnits() {
        if (units.isEmpty())
            return;
        
        window.clear();
        
        List<Unit> remainingUnits = new ArrayList<Unit>(units.values());
        Collections.shuffle(remainingUnits);
        
        while (remainingUnits.size() >= depth/2) {
            for (int i = 0; i < depth/2; i++)
                putUnitInWindow(remainingUnits.remove(0), i);
        }
        float windowPerUnit = (float) depth / 2 / remainingUnits.size();
        for (int i = 0; i < remainingUnits.size(); i++) {
            int position = (int) (windowPerUnit * i);
            putUnitInWindow(remainingUnits.get(i), position);
        }
        
    }
    /** places unit at given window position */
    private void putUnitInWindow(Unit unit, int position) {
        List<Unit> unitsAtT = window.get(position);
        if (unitsAtT == null) {
            unitsAtT = new ArrayList<Unit>();
            window.put(position, unitsAtT);
        }
        unitsAtT.add(unit);
    }
    
    private void findPath(Unit unit) throws NoPathException {
        pool.releaseAllNodes();
        
        NodePool.Point location = unit.getLocation();
        NodePool.Node from = pool.acquireNode(location.x, location.z, currentTime);
        NodePool.Node to = pool.acquireNode(unit.getDestination().x, unit.getDestination().z, 0);
        
        try {
            TimeAStar.Path path = timeAStar.findPath(from, to, unit, depth);
            System.out.println("path for " + unit + "\n\t" + path);
            
            List<Unit.PathPoint> unitPath = new ArrayList<Unit.PathPoint>();
            unitPath.add(new Unit.PathPoint((NodePool.Node) path.startNode));
            
            for (TimeAStar.Transition t : path.transitions) {
                NodePool.Node nextNode = (NodePool.Node) t.toNode();
                pool.reserve(unit, nextNode);
                unitPath.add(new Unit.PathPoint((NodePool.Node) nextNode));
                
                if (NodePool.RESERVE_TWO) {
                    if (!((NodePool.Transition)t).wait) {
                        NodePool.Node prevNode = (NodePool.Node) t.fromNode();
                        pool.reserve(unit, nextNode.x, nextNode.z, nextNode.t - 1);
                    }
                }
            }
            printReservations();
            unit.setPath(unitPath);
        } catch (NoPathException npe) {
            System.out.println("couldnt found a path, setting empty for unit " + unit.id);
            npe.printStackTrace();
            reserveEmptyPath(unit);
        }
    }
    
    private void clearReservations(Unit unit) {
        if (NodePool.RESERVE_TWO) {
//         pair reservations for non wait
            List<Unit.PathPoint> path = unit.getPath();
            for (int i = 0; i < path.size(); i++) {
                Unit.PathPoint point = path.get(i);
                pool.reclaim(point.x, point.z, point.t);
                
                if (i < path.size()-1) {
                    Unit.PathPoint next = path.get(i+1);
                    if (! point.isSamePlace(next))
                        pool.reclaim(next.x, next.z, next.t - 1);
                }
            }
        } else {
//         single reservation
            for (Unit.PathPoint point : unit.getPath()) {
                pool.reclaim(point.x, point.z, point.t);
            }
        }
        NodePool.Point location = unit.getLocation();
        pool.reserve(unit, location.x, location.z, currentTime);
    }
    
    private void reserveEmptyPath(Unit unit) {
                NodePool.Point location = unit.getLocation();
                // reserve empty wait path
                List<Unit.PathPoint> path = new ArrayList<Unit.PathPoint>();
                for (int i = 0; i <= depth; i++) {
                    Unit.PathPoint point = new Unit.PathPoint(location.x, location.z, currentTime + i);
                    try {
                        pool.reserve(unit, point.x, point.z, point.t);
                        path.add(point);
                    } catch (Exception e) {
                        System.out.println("skipping " + point);
                    }
                    unit.setPath(path);
                }
    }
    
    private void printReservations() {
//        for (Map.Entry<String, Unit> res : pool.getReserved().entrySet()) {
//            System.out.println("\t" + res.getValue() + "\t -> \t" + res.getKey());
//        }
    }
    
    public boolean iterate() throws NoPathException {
        System.out.println("iterate time: " + currentTime);
        boolean iterated = false;
        
        if (! newUnits.isEmpty()) {
            distributeUnits();
            
            for (Unit unit : newUnits) 
                reserveEmptyPath(unit);
            
            printReservations();
            for (Unit unit : newUnits) {
                clearReservations(unit);
                findPath(unit);
            }
            iterated = true;
        }
        newUnits.clear();
        
        List<Unit> unitsToFindPath = window.get(currentWindow);
        if (unitsToFindPath != null) {
            for (Unit unit : unitsToFindPath) {
                clearReservations(unit);
                findPath(unit);
                
//                NodePool.Point location = unit.getLocation();
//                pool.reserve(unit, location.x, location.z, currentTime);
                
                iterated = true;
            }
//            for (Unit unit : unitsToFindPath)
//                findPath(unit);
        }
        currentTime++;
        currentWindow++;
        if (currentWindow == depth/2)
            currentWindow = 0;
        
        return iterated;
    }
    
    
    public static void main(String[] args) throws Exception {
        Coordinater c = new Coordinater(8);
        int count = Integer.parseInt(args[0]);
        
        for (int i = 0; i < count; i++) {
            Unit unit = new Unit();
            c.addUnit(unit);
        }
        c.distributeUnits();
        for (Map.Entry e : c.window.entrySet())
            System.out.println(e);
        System.out.println("");
        c.distributeUnits();
        for (Map.Entry e : c.window.entrySet())
            System.out.println(e);
    }
    
}
