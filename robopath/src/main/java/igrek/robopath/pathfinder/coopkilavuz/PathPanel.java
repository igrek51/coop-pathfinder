package igrek.robopath.pathfinder.coopkilavuz;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.AbstractAction;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import raft.kilavuz.runtime.NoPathException;

/**
 *
 * @author hakan eryargi (r a f t)
 */
public class PathPanel extends JPanel {
    static int unitCount = 4;
    
    final Coordinater coordinater;
    final Grid grid;
    
    int cellSize = 40;
    
    /** Creates a new instance of PathPanel */
    public PathPanel(Coordinater coordinater) {
        this.coordinater = coordinater;
        this.grid = coordinater.grid;
        
        for (Unit unit : coordinater.units.values())
            unitPositions.put(unit.id, new Point(unit.getLocation()));
    }
    
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        
        Graphics2D g2d = (Graphics2D)g;
        g2d.translate(20, 20);
        
        paintGrid(g2d);
        paintUnits(g2d);
    }
    
    private Stroke thinStroke = new BasicStroke(1);
    private Stroke thickStroke = new BasicStroke(2);
    
    private void paintUnits(Graphics2D g2d) {
        int unitRadius = cellSize * 2 / 3;
        int pathRadius = cellSize / 3;
        
        boolean allReached = true;
        for (Unit unit : coordinater.units.values()) {
            if (!unit.reached())
                allReached = false;
            
            g2d.setStroke(thinStroke);
            g2d.setColor(getUnitColor(unit));
            g2d.setFont(g2d.getFont().deriveFont(cellSize/2f));
            //NodePool.Point point = unit.getLocation();
            Point point = unitPositions.get(unit.id);
            if (point != null) {
                g2d.fillOval((int)(point.x * cellSize + (cellSize-unitRadius)/2),
                        (int)(point.z * cellSize + (cellSize-unitRadius)/2),
                        unitRadius, unitRadius);
                g2d.setColor(Color.BLACK);
                g2d.drawString(String.valueOf(unit.id), point.x * cellSize + (cellSize/3),
                        point.z * cellSize + (cellSize*2/3));
            }
            g2d.setColor(getUnitColor(unit));
            g2d.setStroke(thickStroke);
            g2d.drawRect(unit.getDestination().x * cellSize + (cellSize/8),
                    unit.getDestination().z * cellSize + (cellSize/8),
                    cellSize*3/4, cellSize*3/4);
            
            g2d.setStroke(thinStroke);
            List<Unit.PathPoint> path = unit.getPath();
            for (int i = unit.getPathIndex(); i < path.size(); i++) {
                Unit.PathPoint pathPoint = path.get(i);
                g2d.drawOval(pathPoint.x * cellSize + (cellSize-pathRadius)/2,
                        pathPoint.z * cellSize + (cellSize-pathRadius)/2,
                        pathRadius, pathRadius);
                g2d.setFont(g2d.getFont().deriveFont((cellSize/4f)));
                g2d.drawString(String.valueOf(i), pathPoint.x * cellSize + cellSize/5, 
                        pathPoint.z * cellSize + cellSize/3);
            }
        }
        
        if (allReached) {
            g2d.setStroke(thinStroke);
            g2d.setColor(Color.RED);
            g2d.setFont(g2d.getFont().deriveFont(20f));
            String s = "all reached";
//            Rectangle r = g2d.getFontMetrics().getStringBounds(s, g2d).getBounds();
//            g2d.drawString(s, (getWidth()-r.width) /2, (getHeight()+r.height)/2);
            g2d.drawString(s, 100, 100);
        }
    }
    
    private void paintGrid(Graphics2D g2d) {
        g2d.setColor(Color.DARK_GRAY);
        
        for (int x = 0; x <= grid.columns; x++) {
            g2d.drawLine(x * cellSize, 0, x * cellSize, grid.rows * cellSize);
        }
        
        for (int y = 0; y <= grid.rows; y++) {
            g2d.drawLine(0, y * cellSize, grid.columns * cellSize, y * cellSize);
        }
        
//        g2d.setColor(Color.BLACK);
        for (Grid.Node node : grid.unwalkables) {
            g2d.fillRect(node.x * cellSize, node.y * cellSize, cellSize, cellSize);
        }
    }
    
    private Map<Integer, Color> unitColors = new HashMap<Integer, Color>();
    private List<Color> colors = Arrays.asList(Color.RED, Color.BLUE, Color.GREEN,
            Color.MAGENTA, Color.ORANGE, Color.YELLOW);
    int lastColorIndex = 0;
    
    private Color getUnitColor(Unit unit) {
        Color color = unitColors.get(unit.id);
        if (color == null) {
            color = colors.get(lastColorIndex);
            unitColors.put(unit.id, color);
            lastColorIndex++;
            if (lastColorIndex == colors.size())
                lastColorIndex = 0;
        }
        return color;
    }
    
    class ButtonPanel extends JPanel {
        JButton stepButton = new JButton(new AbstractAction("step") {
            public void actionPerformed(ActionEvent event) {
                try {
                    coordinater.iterate();
                    for (Unit unit : coordinater.units.values()) {
                        unit.next();
                        unitPositions.put(unit.id, new Point(unit.getLocation()));
                    }
                } catch (NoPathException npe) {
                    npe.printStackTrace();
                }
                PathPanel.this.paintImmediately(PathPanel.this.getBounds());
            }
        });
        
        JButton resetButton = new JButton(new AbstractAction("reset") {
            public void actionPerformed(ActionEvent event) {
                reset();
            }
        });
        
        JButton animateButton = new JButton(new AbstractAction("animate") {
            public void actionPerformed(ActionEvent event) {
                animate();
            }
        });
        
        JButton stopButton = new JButton(new AbstractAction("stop") {
            public void actionPerformed(ActionEvent event) {
                animating = false;
            }
        });
        
        ButtonPanel() {
            setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
            add(stepButton);
            add(resetButton);
            add(animateButton);
            add(stopButton);
            stepButton.setMnemonic('s');
        }
    }
    Map<Integer, Point> unitPositions = new HashMap<Integer, Point>();
    Map<Integer, NodePool.Point> unitTargets = new HashMap<Integer, NodePool.Point>();
    
    boolean animating = false;
    void animate() {
        if (animating)
            return;
        animating = true;
        new Thread(){
            public void run() {
                while (animating) {
                    try {
                        coordinater.iterate();
                        for (Unit unit : coordinater.units.values()) {
                            unit.next();
                            unitTargets.put(unit.id, unit.getLocation());
                        }
                        int fps = 25;
                        for (int i = 0; i < fps; i++) {
                            for (Unit unit : coordinater.units.values()) {
                                Point current = unitPositions.get(unit.id);
                                NodePool.Point target = unitTargets.get(unit.id);
                                
                                if (current == null) {
                                    current = new Point(target);
                                    unitPositions.put(unit.id, current);
                                }
                                float move = 1f / fps;
                                float dX = target.x - current.x;
                                float dZ = target.z - current.z;
                                
                                current.x = (Math.abs(dX) < move) ? target.x : current.x + Math.signum(dX) * move;
                                current.z = (Math.abs(dZ) < move) ? target.z : current.z + Math.signum(dZ) * move;
                                
                            }
                            SwingUtilities.invokeAndWait(new Runnable() {
                                public void run() {
                                    paintImmediately(PathPanel.this.getBounds());
                                }
                            });
                            Thread.sleep(1000/fps);
                        }
                    } catch (Exception npe) {
                        npe.printStackTrace();
                    }
                }
            }
        } .start();
    }
    
    void reset() {
        coordinater.reset();
        
        List<Grid.Node> nodes = new ArrayList<Grid.Node>(grid.nodes.values());
        Collections.shuffle(nodes);
        
        for (int i = 0; i < unitCount; i++) {
            Unit unit = new Unit();
            coordinater.addUnit(unit);
            
            Grid.Node node = nodes.remove(0);
            while (grid.unwalkables.contains(node)) {
                node = nodes.remove(0);
            }
            unit.setLocation(node.x, node.y);
            unitPositions.put(unit.id, new Point(unit.getLocation()));
            
            node = nodes.remove(0);
            while (grid.unwalkables.contains(node)) {
                node = nodes.remove(0);
            }
            unit.setDestination(node.x, node.y);
            
            unit.setPath(new ArrayList<Unit.PathPoint>());
        }
        paintImmediately(getBounds());
    }
    
    public static class Point {
        public float x, z;
        
        Point(float x, float z) {
            this.x = x;
            this.z = z;
        }
        
        public Point(NodePool.Point p) {
            this(p.x, p.z);
        }
    }
    
    public static void main(String[] args) throws Exception {
        System.out.println("usage PathPanel [-d <depth>] [-u <unitcount>]");
        
        ComLineArgs comLine = new ComLineArgs(args);
        
        int depth = comLine.containsArg("-d") ? 
            Integer.parseInt(comLine.getArg("-d")) : 32;
        int unitCount = comLine.containsArg("-u") ? 
            Integer.parseInt(comLine.getArg("-u")) : 6;
        
        PathPanel.unitCount = unitCount;
        Coordinater coordinater = new Coordinater(depth);
        
//        Unit unit1 = new Unit();
//        unit1.setLocation(0, 0);
//        unit1.setDestination(5, 0);
//        
//        Unit unit2 = new Unit();
//        unit2.setLocation(6, 0);
//        unit2.setDestination(1, 0);
//        
//        coordinater.addUnit(unit1);
//        coordinater.addUnit(unit2);
        
        PathPanel pathPanel = new PathPanel(coordinater);
        pathPanel.setPreferredSize(new Dimension(640, 480));
        pathPanel.reset();
        
        ButtonPanel buttonPanel = pathPanel.new ButtonPanel();
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(pathPanel, BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.EAST);
        
        
        JFrame frame = new JFrame("WHCA* demo");
        frame.add(panel);
        frame.pack();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }
    
}
