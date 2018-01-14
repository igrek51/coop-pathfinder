package igrek.robopath.ui.whca;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.BasicStroke;
import java.awt.Stroke;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.swing.SwingUtilities;

import de.felixroske.jfxsupport.FXMLController;
import igrek.robopath.mazegen.MazeGenerator;
import igrek.robopath.model.Point;
import igrek.robopath.pathfinder.coop.Coordinater;
import igrek.robopath.pathfinder.coop.Grid;
import igrek.robopath.pathfinder.coop.NodePool;
import igrek.robopath.pathfinder.coop.PathPanel;
import igrek.robopath.pathfinder.coop.Unit;
import igrek.robopath.pathfinder.mystar.MyStarPathFinder;
import igrek.robopath.pathfinder.mystar.Path;
import igrek.robopath.pathfinder.mystar.ReservationTable;
import igrek.robopath.pathfinder.mystar.TileMap;
import igrek.robopath.ui.common.ResizableCanvas;
import igrek.robopath.ui.whca.robot.MobileRobot;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.VPos;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;
import javafx.util.Duration;
import raft.kilavuz.runtime.NoPathException;

@FXMLController
public class WHCAController {
	
	private Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@FXML
	private ResizableCanvas drawArea;
	
	@FXML
	private VBox drawAreaContainer;
	
	private TileMap map;
	private List<MobileRobot> robots = new ArrayList<>();
	private SimulationParams params = new SimulationParams();
	
	private Random random = new Random();
	private Boolean pressedTransformer;
	
	final double FPS = 30;
	final int DEPTH = 32;
	int unitCount = 6;
	
	Coordinater coordinater;
	Grid grid;
	Map<Integer, PathPanel.Point> unitPositions = new HashMap<Integer, PathPanel.Point>();
	Map<Integer, NodePool.Point> unitTargets = new HashMap<Integer, NodePool.Point>();
	boolean animating = false;
	
	// PARAMS
	@FXML
	private TextField paramMapSizeW;
	@FXML
	private TextField paramMapSizeH;
	@FXML
	private TextField paramRobotsCount;
	@FXML
	private CheckBox paramRobotAutoTarget;
	
	public WHCAController() {
		resetMap(null);
	}
	
	@FXML
	private void resetMap(final Event event) {
		if (event != null)
			readParams();
		map = new TileMap(params.mapSizeW, params.mapSizeH);
		robots.clear();
		
		coordinater = new Coordinater(DEPTH);
		grid = coordinater.grid;
		for (Unit unit : coordinater.units.values())
			unitPositions.put(unit.id, new PathPanel.Point(unit.getLocation()));
		reset();
		
		if (event != null)
			drawAreaContainerResized();
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
			unitPositions.put(unit.id, new PathPanel.Point(unit.getLocation()));
			
			node = nodes.remove(0);
			while (grid.unwalkables.contains(node)) {
				node = nodes.remove(0);
			}
			unit.setDestination(node.x, node.y);
			
			unit.setPath(new ArrayList<Unit.PathPoint>());
		}
	}
	
	@FXML
	private void placeRobots(final Event event) {
		if (event != null)
			readParams();
		robots.clear();
		for (int i = 0; i < params.robotsCount; i++) {
			robots.add(new MobileRobot(randomUnoccupiedCell(map), robot -> onTargetReached(robot), i));
		}
	}
	
	@FXML
	private void generateMaze(final Event event) {
		if (event != null)
			readParams();
		new MazeGenerator(map).generateMaze();
	}
	
	private void drawAreaContainerResized() {
		double containerWidth = drawAreaContainer.getWidth();
		double containerHeight = drawAreaContainer.getHeight();
		double maxCellW = containerWidth / map.getWidthInTiles();
		double maxCellH = containerHeight / map.getHeightInTiles();
		double cellSize = maxCellW < maxCellH ? maxCellW : maxCellH; // min
		drawArea.setWidth(cellSize * map.getWidthInTiles());
		drawArea.setHeight(cellSize * map.getHeightInTiles());
	}
	
	@FXML
	public void initialize() {
		logger.info("initializing controller " + this.getClass().getSimpleName());
		
		drawAreaContainer.widthProperty().addListener(o -> drawAreaContainerResized());
		drawAreaContainer.heightProperty().addListener(o -> drawAreaContainerResized());
		
		drawArea.addEventHandler(MouseEvent.MOUSE_PRESSED, event -> {
			mousePressedMap(event);
		});
		
		drawArea.addEventHandler(MouseEvent.MOUSE_DRAGGED, event -> {
			mouseDraggedMap(event);
		});
		
		drawArea.addEventHandler(MouseEvent.MOUSE_RELEASED, event -> {
		});
		
		updateParams();
		repaint(map, robots);
		
		// animation timer
		Timeline fiveSecondsWonder = new Timeline(new KeyFrame(Duration.millis(1000 / FPS), new EventHandler<ActionEvent>() {
			
			private long lastTime = System.currentTimeMillis();
			
			@Override
			public void handle(ActionEvent event) {
				long current = System.currentTimeMillis();
				timeLapse(((double) (current - lastTime)) / 1000);
				lastTime = current;
				repaint(map, robots);
			}
		}));
		fiveSecondsWonder.setCycleCount(Timeline.INDEFINITE);
		fiveSecondsWonder.play();
	}
	
	private void timeLapse(double t) {
		for (MobileRobot robot : robots) {
			robot.timeLapse(t);
		}
	}
	
	private void mousePressedMap(MouseEvent event) {
		if (event.getButton() == MouseButton.PRIMARY) {
			
			Point point = locatePoint(map, event);
			if (point != null) {
				Boolean state = map.getCell(point);
				if (!state) {
					MobileRobot occupiedBy = occupiedByRobot(point);
					if (occupiedBy != null) {
						robots.remove(occupiedBy);
					} else {
						robots.add(new MobileRobot(point, robot -> onTargetReached(robot), robots.size()));
					}
				}
				repaint(map, robots);
			}
			
		} else if (event.getButton() == MouseButton.SECONDARY) {
			
			Point point = locatePoint(map, event);
			if (point != null) {
				Boolean state = !map.getCell(point);
				map.setCell(point, state);
				pressedTransformer = state;
				repaint(map, robots);
			}
			
		}
	}
	
	private MobileRobot occupiedByRobot(Point point) {
		for (MobileRobot robot : robots) {
			if (robot.getPosition().equals(point))
				return robot;
		}
		return null;
	}
	
	private void mouseDraggedMap(MouseEvent event) {
		if (event.getButton() == MouseButton.SECONDARY) {
			
			Point point = locatePoint(map, event);
			if (point != null) {
				Boolean state = map.getCell(point);
				if (state != pressedTransformer) {
					map.setCell(point, pressedTransformer);
					repaint(map, robots);
				}
			}
			
		}
	}
	
	@FXML
	private void randomTargetPressed(final Event event) {
		ReservationTable reservationTable = new ReservationTable(map.getWidthInTiles(), map.getHeightInTiles());
		for (int x = 0; x < map.getWidthInTiles(); x++) {
			for (int y = 0; y < map.getHeightInTiles(); y++) {
				boolean occupied = map.getCell(x, y);
				if (occupied)
					reservationTable.setBlocked(x, y);
			}
		}
		for (MobileRobot robot : robots) {
			randomRobotTarget(robot, reservationTable);
		}
	}
	
	private void randomRobotTarget(MobileRobot robot, ReservationTable reservationTable) {
		robot.resetNextMoves();
		Point start = robot.lastTarget();
		Point target = randomUnoccupiedCell(map);
		robot.setTarget(target);
		MyStarPathFinder pathFinder = new MyStarPathFinder(reservationTable);
		Path path = pathFinder.findPath(start.getX(), start.getY(), target.getX(), target.getY());
		if (path != null) {
			// enque path
			int t = 0;
			reservationTable.setBlocked(start.x, start.y, t);
			reservationTable.setBlocked(start.x, start.y, t + 1);
			for (int i = 1; i < path.getLength(); i++) {
				Path.Step step = path.getStep(i);
				robot.enqueueMove(step.getX(), step.getY());
				t++;
				reservationTable.setBlocked(step.getX(), step.getY(), t);
				reservationTable.setBlocked(step.getX(), step.getY(), t + 1);
			}
		} else {
			reservationTable.setBlocked(start.x, start.y);
		}
	}
	
	private Point randomUnoccupiedCell(TileMap map) {
		// get all unoccupied cells
		List<Point> frees = new ArrayList<>();
		for (int x = 0; x < map.getWidthInTiles(); x++) {
			for (int y = 0; y < map.getHeightInTiles(); y++) {
				boolean occupied = map.getCell(x, y);
				if (!occupied)
					frees.add(new Point(x, y));
			}
		}
		if (frees.isEmpty())
			return null;
		// random from list
		return frees.get(random.nextInt(frees.size()));
	}
	
	private Point randomCell(TileMap map) {
		int x = random.nextInt(map.getWidthInTiles());
		int y = random.nextInt(map.getHeightInTiles());
		return new Point(x, y);
	}
	
	private <T> boolean contains(T seek, T... collection) {
		for (T c : collection) {
			if (c.equals(seek))
				return true;
		}
		return false;
	}
	
	private void updateParams() {
		paramMapSizeW.setText(Integer.toString(params.mapSizeW));
		paramMapSizeH.setText(Integer.toString(params.mapSizeH));
		paramRobotsCount.setText(Integer.toString(params.robotsCount));
		paramRobotAutoTarget.setSelected(params.robotAutoTarget);
	}
	
	private void readParams() {
		try {
			params.mapSizeW = Integer.parseInt(paramMapSizeW.getText());
			params.mapSizeH = Integer.parseInt(paramMapSizeH.getText());
			params.robotsCount = Integer.parseInt(paramRobotsCount.getText());
			params.robotAutoTarget = paramRobotAutoTarget.isSelected();
		} catch (NumberFormatException e) {
			logger.error(e.getMessage());
		}
	}
	
	@FXML
	private void eventReadParams(final Event event) {
		readParams();
	}
	
	
	private void onTargetReached(MobileRobot robot) {
		if (params.robotAutoTarget) {
			//			randomRobotTarget(robot);
		}
	}
	
	//	VIEW
	public void repaint(TileMap map, List<MobileRobot> robots) {
		drawMap();
	}
	
	private void drawMap() {
		GraphicsContext gc = drawArea.getGraphicsContext2D();
		
//		drawGrid(gc);
//		drawCells(gc);
//		drawRobots(gc);
		
		gc.clearRect(0, 0, drawArea.getWidth(), drawArea.getHeight());
		
		gc.setLineWidth(1);
		gc.setStroke(Color.rgb(200, 200, 200));
		
		paintGrid(gc);
		paintUnits(gc);
	}
	
	private Stroke thinStroke = new BasicStroke(1);
	private Stroke thickStroke = new BasicStroke(2);
	
	int cellSize = 40;
	
	private void paintUnits(GraphicsContext gc) {
		int unitRadius = cellSize * 2 / 3;
		int pathRadius = cellSize / 3;
		
		boolean allReached = true;
		for (Unit unit : coordinater.units.values()) {
			if (!unit.reached())
				allReached = false;
			
			gc.setFill(getUnitColor(unit));
			//NodePool.Point point = unit.getLocation();
			PathPanel.Point point = unitPositions.get(unit.id);
			if (point != null) {
				gc.fillOval((int)(point.x * cellSize + (cellSize-unitRadius)/2),
						(int)(point.z * cellSize + (cellSize-unitRadius)/2),
						unitRadius, unitRadius);
			}
			gc.setStroke(getUnitColor(unit));
			gc.strokeRect(unit.getDestination().x * cellSize + (cellSize/8),
					unit.getDestination().z * cellSize + (cellSize/8),
					cellSize*3/4, cellSize*3/4);
			
			List<Unit.PathPoint> path = unit.getPath();
			for (int i = unit.getPathIndex(); i < path.size(); i++) {
				Unit.PathPoint pathPoint = path.get(i);
				gc.strokeOval(pathPoint.x * cellSize + (cellSize-pathRadius)/2,
						pathPoint.z * cellSize + (cellSize-pathRadius)/2,
						pathRadius, pathRadius);
			}
		}
		
		if (allReached) {
			gc.setStroke(Color.RED);
			String s = "all reached";
			gc.fillText(s, 100, 100);
		}
	}
	
	private void paintGrid(GraphicsContext gc) {
		gc.setStroke(Color.DARKGRAY);
		
		for (int x = 0; x <= grid.columns; x++) {
			gc.strokeLine(x * cellSize, 0, x * cellSize, grid.rows * cellSize);
		}
		
		for (int y = 0; y <= grid.rows; y++) {
			gc.strokeLine(0, y * cellSize, grid.columns * cellSize, y * cellSize);
		}
		
		gc.setFill(Color.BLACK);
		for (Grid.Node node : grid.unwalkables) {
			gc.fillRect(node.x * cellSize, node.y * cellSize, cellSize, cellSize);
		}
	}
	
	private Color getUnitColor(Unit unit) {
		int allCount = unitCount;
		int index = unit.id % allCount;
		double hue = 360.0 * index / allCount;
		return Color.hsb(hue, 1, 1);
	}
	
	
	private void drawCells(GraphicsContext gc) {
		for (int x = 0; x < map.getWidthInTiles(); x++) {
			for (int y = 0; y < map.getHeightInTiles(); y++) {
				Boolean occupied = map.getCell(x, y);
				if (occupied != null) {
					drawCell(gc, x, y, occupied);
				}
			}
		}
	}
	
	private void drawCell(GraphicsContext gc, int x, int y, boolean occupied) {
		double cellW = drawArea.getWidth() / map.getWidthInTiles();
		double cellH = drawArea.getHeight() / map.getHeightInTiles();
		double w2 = 0.9 * cellW;
		double h2 = 0.9 * cellH;
		if (occupied) {
			gc.setFill(Color.rgb(0, 0, 0));
			double x2 = x * cellW + (cellW - w2) / 2;
			double y2 = y * cellH + (cellH - h2) / 2;
			gc.fillRoundRect(x2, y2, w2, h2, w2 / 3, h2 / 3);
		}
	}
	
	private void drawGrid(GraphicsContext gc) {
		gc.clearRect(0, 0, drawArea.getWidth(), drawArea.getHeight());
		
		gc.setLineWidth(1);
		gc.setStroke(Color.rgb(200, 200, 200));
		// vertical lines
		for (int x = 0; x <= map.getWidthInTiles(); x++) {
			double x2 = x * drawArea.getWidth() / map.getWidthInTiles();
			gc.strokeLine(x2, 0, x2, drawArea.getHeight());
		}
		// horizontal lines
		for (int y = 0; y <= map.getHeightInTiles(); y++) {
			double y2 = y * drawArea.getHeight() / map.getHeightInTiles();
			gc.strokeLine(0, y2, drawArea.getWidth(), y2);
		}
	}
	
	public Point locatePoint(TileMap map, MouseEvent event) {
		return locatePoint(map, event.getX(), event.getY());
	}
	
	public Point locatePoint(TileMap map, double screenX, double screenY) {
		int mapX = ((int) (screenX * map.getWidthInTiles() / drawArea.getWidth()));
		int mapY = ((int) (screenY * map.getHeightInTiles() / drawArea.getHeight()));
		if (mapX < 0 || mapY < 0 || mapX >= map.getWidthInTiles() || mapY >= map.getHeightInTiles())
			return null;
		return new Point(mapX, mapY);
	}
	
	private void drawRobots(GraphicsContext gc) {
		int index = 0;
		for (MobileRobot robot : robots) {
			drawRobot(gc, robot, index++);
		}
	}
	
	private void drawRobot(GraphicsContext gc, MobileRobot robot, int index) {
		double cellW = drawArea.getWidth() / map.getWidthInTiles();
		double cellH = drawArea.getHeight() / map.getHeightInTiles();
		double w = 0.6 * cellW;
		double h = 0.6 * cellH;
		Color robotColor = robotColor(index);
		// draw target
		Point target = robot.getTarget();
		if (target != null && !target.equals(robot.getPosition())) {
			gc.setStroke(robotColor);
			double targetX = target.getX() * cellW + cellW / 2;
			double targetY = target.getY() * cellH + cellH / 2;
			gc.strokeLine(targetX - w / 2, targetY - h / 2, targetX + w / 2, targetY + h / 2);
			gc.strokeLine(targetX - w / 2, targetY + h / 2, targetX + w / 2, targetY - h / 2);
		}
		// draw path
		gc.setStroke(robotColor);
		LinkedList<Point> movesQue = robot.getMovesQue();
		Point previous = robot.getPosition();
		for (Point move : movesQue) {
			double fromX = previous.getX() * cellW + cellW / 2;
			double fromY = previous.getY() * cellH + cellH / 2;
			double toX = move.getX() * cellW + cellW / 2;
			double toY = move.getY() * cellH + cellH / 2;
			gc.strokeLine(fromX, fromY, toX, toY);
			previous = move;
		}
		// draw robot
		gc.setFill(robotColor);
		double x = robot.getInterpolatedX() * cellW + cellW / 2 - w / 2;
		double y = robot.getInterpolatedY() * cellH + cellH / 2 - h / 2;
		gc.fillOval(x, y, w, h);
		// draw its priority
		gc.setFill(robotColor(index, 0.5));
		gc.setTextAlign(TextAlignment.CENTER);
		gc.setTextBaseline(VPos.CENTER);
		gc.setFont(new Font("System", h / 2));
		gc.fillText(Integer.toString(robot.getPriority() + 1), x + w / 2, y + h / 2);
	}
	
	private Color robotColor(int index) {
		return robotColor(index, 1);
	}
	
	private Color robotColor(int index, double b) {
		double hue = 360.0 * index / robots.size();
		return Color.hsb(hue, 1, b);
	}
	
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
								PathPanel.Point current = unitPositions.get(unit.id);
								NodePool.Point target = unitTargets.get(unit.id);
								
								if (current == null) {
									current = new PathPanel.Point(target);
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
									// repaint();
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
	
	@FXML
	private void buttonStep(final Event event) {
		try {
			coordinater.iterate();
			for (Unit unit : coordinater.units.values()) {
				unit.next();
				unitPositions.put(unit.id, new PathPanel.Point(unit.getLocation()));
			}
		} catch (NoPathException npe) {
			npe.printStackTrace();
		}
		
//		repaint();
	}
	
	@FXML
	private void buttonReset(final Event event) {
		reset();
	}
	
	@FXML
	private void buttonAnimate(final Event event) {
		animate();
	}
	
	@FXML
	private void buttonStop(final Event event) {
		animating = false;
	}
}
