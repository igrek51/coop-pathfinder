package igrek.robopath.ui.colavoid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import de.felixroske.jfxsupport.FXMLController;
import igrek.robopath.mazegen.MazeGenerator;
import igrek.robopath.model.Point;
import igrek.robopath.pathfinder.mystar.MyStarPathFinder;
import igrek.robopath.pathfinder.mystar.Path;
import igrek.robopath.pathfinder.mystar.ReservationTable;
import igrek.robopath.pathfinder.mystar.TileMap;
import igrek.robopath.ui.colavoid.robot.MobileRobot;
import igrek.robopath.ui.common.ResizableCanvas;
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

@FXMLController
public class CollisionAvoidController {
	
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
	
	// PARAMS
	@FXML
	private TextField paramMapSizeW;
	@FXML
	private TextField paramMapSizeH;
	@FXML
	private TextField paramRobotsCount;
	@FXML
	private CheckBox paramRobotAutoTarget;
	
	public CollisionAvoidController() {
		resetMap(null);
	}
	
	@FXML
	private void resetMap(final Event event) {
		if (event != null)
			readParams();
		map = new TileMap(params.mapSizeW, params.mapSizeH);
		robots.clear();
		if (event != null)
			drawAreaContainerResized();
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
		
		drawGrid(gc);
		drawCells(gc);
		drawRobots(gc);
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
		gc.setFill(robotColor(index, 0.3));
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
}
