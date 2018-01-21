package igrek.robopath.modules.robomove;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import de.felixroske.jfxsupport.FXMLController;
import igrek.robopath.model.Point;
import igrek.robopath.modules.common.ResizableCanvas;
import igrek.robopath.modules.robomove.robot.MobileRobot;
import igrek.robopath.pathfinder.PathFinder;
import igrek.robopath.pathfinder.astar.AStarPathFinder;
import igrek.robopath.pathfinder.astar.Path;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.util.Duration;

@FXMLController
public class RobomoveController {
	
	private Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@FXML
	private ResizableCanvas drawArea;
	
	@FXML
	private VBox drawAreaContainer;
	
	private TestTileMap map;
	private Path path;
	private List<MobileRobot> robots = new ArrayList<>();
	private SimulationParams params = new SimulationParams();
	
	private Random random = new Random();
	private TileCellType pressedTransformer;
	
	// PARAMS
	@FXML
	private TextField paramMapSizeW;
	@FXML
	private TextField paramMapSizeH;
	@FXML
	private TextField paramRobotsCount;
	@FXML
	private CheckBox paramRobotAutoTarget;
	
	public RobomoveController() {
		resetMap(null);
	}
	
	@FXML
	private void resetMap(final Event event) {
		if (event != null)
			readParams();
		map = new TestTileMap(params.mapSizeW, params.mapSizeH);
		robots.clear();
		for (int i = 0; i < params.robotsCount; i++) {
			robots.add(new MobileRobot(randomCell(map), robot -> onTargetReached(robot)));
		}
		if (event != null)
			drawAreaContainerResized();
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
		drawMap();
		
		// repainting timer
		final double FPS = 30;
		Timeline fiveSecondsWonder = new Timeline(new KeyFrame(Duration.millis(1000 / FPS), new EventHandler<ActionEvent>() {
			
			private long lastTime = System.currentTimeMillis();
			
			@Override
			public void handle(ActionEvent event) {
				long current = System.currentTimeMillis();
				timeLapse(((double) (current - lastTime)) / 1000);
				lastTime = current;
				drawMap();
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
			
			Point point = locatePoint(event);
			if (point != null) {
				TileCellType type = getMapCellType(point);
				type = transformCellTypeLeftClicked(type);
				setMapCell(point, type);
				pressedTransformer = type;
				drawMap();
			}
			
		} else if (event.getButton() == MouseButton.SECONDARY) {
			
			Point point = locatePoint(event);
			if (point != null) {
				TileCellType type = getMapCellType(point);
				type = transformCellTypeRightClicked(type);
				setMapCell(point, type);
				pressedTransformer = type;
				drawMap();
			}
			
		}
	}
	
	private void mouseDraggedMap(MouseEvent event) {
		if (event.getButton() == MouseButton.SECONDARY) {
			
			Point point = locatePoint(event);
			if (point != null) {
				TileCellType type = getMapCellType(point);
				if (type != pressedTransformer) {
					setMapCell(point, pressedTransformer);
					drawMap();
				}
			}
			
		}
	}
	
	private TileCellType getMapCellType(Point point) {
		return map.get(point.x, point.y);
	}
	
	private TileCellType getMapCellType(int x, int y) {
		return map.get(x, y);
	}
	
	private void setMapCell(Point point, TileCellType type) {
		map.set(point.x, point.y, type);
	}
	
	private TileCellType transformCellTypeLeftClicked(TileCellType type) {
		switch (type) {
			case START:
				return TileCellType.EMPTY;
			default:
				return TileCellType.START;
		}
	}
	
	private TileCellType transformCellTypeRightClicked(TileCellType type) {
		switch (type) {
			case BLOCKED:
				return TileCellType.EMPTY;
			default:
				return TileCellType.BLOCKED;
		}
	}
	
	@FXML
	private void findPathPressed(final Event event) {
		// remove previous paths
		replaceCellTypes(TileCellType.PATH, TileCellType.EMPTY);
		
		PathFinder pathFinder = new AStarPathFinder(map, 0, true);
		Point start = findFirstCellType(map, TileCellType.START);
		Point target = findFirstCellTypeButNot(map, TileCellType.START, start);
		if (start != null && target != null) {
			path = pathFinder.findPath(start.getX(), start.getY(), target.getX(), target.getY());
			if (path != null) {
				
				for (int i = 1; i < path.getLength() - 1; i++) {
					Path.Step step = path.getStep(i);
					map.set(step.getX(), step.getY(), TileCellType.PATH);
				}
				drawMap();
			}
		}
	}
	
	@FXML
	private void randomTargetPressed(final Event event) {
		for (MobileRobot robot : robots) {
			randomRobotTarget(robot);
		}
	}
	
	private void randomRobotTarget(MobileRobot robot) {
		robot.resetNextMoves();
		Point start = robot.lastTarget();
		Point target = randomCellButNotType(map, TileCellType.BLOCKED);
		robot.setTarget(target);
		PathFinder pathFinder = new AStarPathFinder(map, 0, true);
		path = pathFinder.findPath(start.getX(), start.getY(), target.getX(), target.getY());
		if (path != null) {
			// enque path
			for (int i = 1; i < path.getLength(); i++) {
				Path.Step step = path.getStep(i);
				robot.enqueueMove(step.getX(), step.getY());
			}
		}
	}
	
	private Point randomCell(TestTileMap map) {
		int x = random.nextInt(map.getWidthInTiles());
		int y = random.nextInt(map.getHeightInTiles());
		return new Point(x, y);
	}
	
	private Point randomCellButNotType(TestTileMap map, TileCellType... excludedTypes) {
		Point p1 = randomCell(map);
		Point current = p1;
		while (true) {
			TileCellType type = getMapCellType(current);
			if (!contains(type, excludedTypes))
				return current; // valid cell type
			// try next cell - that's not really random
			current = nextPointOnMap(map, current);
			// but check if we are not searching indefinitely
			if (current.equals(p1))
				return null;
		}
	}
	
	private Point nextPointOnMap(TestTileMap map, Point p1) {
		int x = p1.x;
		int y = p1.y;
		x++;
		if (x >= map.getWidthInTiles()) {
			x = 0;
			y++;
			if (y >= map.getHeightInTiles())
				y = 0;
		}
		return new Point(x, y);
	}
	
	private void replaceCellTypes(TileCellType replaceFrom, TileCellType replaceTo) {
		for (int x = 0; x < map.getWidthInTiles(); x++) {
			for (int y = 0; y <= map.getHeightInTiles(); y++) {
				TileCellType type = getMapCellType(x, y);
				if (type == replaceFrom)
					map.set(x, y, replaceTo);
			}
		}
	}
	
	private Point findFirstCellType(TestTileMap map, TileCellType wantedType) {
		for (int x = 0; x < map.getWidthInTiles(); x++) {
			for (int y = 0; y <= map.getHeightInTiles(); y++) {
				TileCellType type = map.get(x, y);
				if (type == wantedType)
					return new Point(x, y);
			}
		}
		return null;
	}
	
	private <T> boolean contains(T seek, T... collection) {
		for (T c : collection) {
			if (c.equals(seek))
				return true;
		}
		return false;
	}
	
	private Point findFirstCellTypeButNot(TestTileMap map, TileCellType wantedType, Point... excludePoints) {
		for (int x = 0; x < map.getWidthInTiles(); x++) {
			for (int y = 0; y <= map.getHeightInTiles(); y++) {
				TileCellType type = map.get(x, y);
				if (type == wantedType) {
					Point p = new Point(x, y);
					if (!contains(p, excludePoints))
						return p;
				}
			}
		}
		return null;
	}
	
	private void drawMap() {
		GraphicsContext gc = drawArea.getGraphicsContext2D();
		
		drawGrid(gc);
		drawCells(gc);
		drawRobots(gc);
	}
	
	private void drawCells(GraphicsContext gc) {
		for (int x = 0; x < map.getWidthInTiles(); x++) {
			for (int y = 0; y <= map.getHeightInTiles(); y++) {
				TileCellType type = map.get(x, y);
				drawCell(gc, x, y, type);
			}
		}
	}
	
	private void drawCell(GraphicsContext gc, int x, int y, TileCellType type) {
		double cellW = drawArea.getWidth() / map.getWidthInTiles();
		double cellH = drawArea.getHeight() / map.getHeightInTiles();
		double w2 = 0.9 * cellW;
		double h2 = 0.9 * cellH;
		if (type == TileCellType.BLOCKED || type == TileCellType.START || type == TileCellType.PATH) {
			gc.setFill(getCellColor(type));
			double x2 = x * cellW + (cellW - w2) / 2;
			double y2 = y * cellH + (cellH - h2) / 2;
			gc.fillRoundRect(x2, y2, w2, h2, w2 / 3, h2 / 3);
		}
	}
	
	private Color getCellColor(TileCellType type) {
		switch (type) {
			case BLOCKED:
				return Color.rgb(0, 0, 0);
			case START:
				return Color.rgb(0, 200, 0);
			case PATH:
				return Color.rgb(90, 127, 200);
			default:
				return Color.rgb(0, 0, 0, 0);
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
	
	private Point locatePoint(MouseEvent event) {
		return locatePoint(event.getX(), event.getY());
	}
	
	private Point locatePoint(double screenX, double screenY) {
		int mapX = ((int) (screenX * map.getWidthInTiles() / drawArea.getWidth()));
		int mapY = ((int) (screenY * map.getHeightInTiles() / drawArea.getHeight()));
		if (mapX < 0 || mapY < 0 || mapX >= map.getWidthInTiles() || mapY >= map.getHeightInTiles())
			return null;
		return new Point(mapX, mapY);
	}
	
	private void drawRobots(GraphicsContext gc) {
		for (MobileRobot robot : robots) {
			drawRobot(gc, robot);
		}
	}
	
	private void drawRobot(GraphicsContext gc, MobileRobot robot) {
		double cellW = drawArea.getWidth() / map.getWidthInTiles();
		double cellH = drawArea.getHeight() / map.getHeightInTiles();
		double w = 0.6 * cellW;
		double h = 0.6 * cellH;
		// draw target
		Point target = robot.getTarget();
		if (target != null && !target.equals(robot.getPosition())) {
			gc.setStroke(Color.rgb(0, 100, 0));
			double targetX = target.getX() * cellW + cellW / 2;
			double targetY = target.getY() * cellH + cellH / 2;
			gc.strokeLine(targetX - w / 2, targetY - h / 2, targetX + w / 2, targetY + h / 2);
			gc.strokeLine(targetX - w / 2, targetY + h / 2, targetX + w / 2, targetY - h / 2);
		}
		// draw path
		gc.setStroke(Color.rgb(0, 182, 0));
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
		gc.setFill(Color.rgb(255, 0, 0));
		double x = robot.getInterpolatedX() * cellW + cellW / 2 - w / 2;
		double y = robot.getInterpolatedY() * cellH + cellH / 2 - h / 2;
		gc.fillOval(x, y, w, h);
		
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
			randomRobotTarget(robot);
		}
	}
}
