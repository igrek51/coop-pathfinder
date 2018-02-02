package igrek.robopath.simulation.potentialfield;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import de.felixroske.jfxsupport.FXMLController;
import igrek.robopath.common.Point;
import igrek.robopath.pathfinder.astar.Path;
import igrek.robopath.simulation.common.ResizableCanvas;
import igrek.robopath.simulation.potentialfield.robot.MobileRobot;
import igrek.robopath.simulation.potentialfield.robot.Vector2;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.util.Duration;

@FXMLController
public class PotentialFieldController {
	
	private Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@FXML
	private ResizableCanvas drawArea;
	
	@FXML
	private VBox drawAreaContainer;
	
	private TestTileMap map;
	private Path path;
	private List<MobileRobot> robots = new ArrayList<>();
	private SimulationParams params = new SimulationParams();
	
	private Random random;
	private TileCellType pressedTransformer;
	
	// PARAMS
	@FXML
	private TextField paramMapSizeW;
	@FXML
	private TextField paramMapSizeH;
	
	public PotentialFieldController(@Autowired Random random) {
		this.random = random;
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
			timeLapseRobot(t, robot);
		}
	}
	
	private void timeLapseRobot(double t, MobileRobot robot) {
		if (robot.getTarget() != null) {
			robot.zeroForce();
			// force attracting to target
			Vector2 diff = robot.getTarget().sub(robot.getPosition());
			final double F_TO_TARGET = 30; // constant force value
			Vector2 forceToTarget = diff.normalizeTo(F_TO_TARGET);
			robot.addForce(forceToTarget);
			
			// forces repelling from obstacles
			Vector2 obstaclesForce = Vector2.ZERO;
			for (int x = 0; x < map.getWidthInTiles(); x++) {
				for (int y = 0; y <= map.getHeightInTiles(); y++) {
					TileCellType type = map.get(x, y);
					if (type == TileCellType.BLOCKED) {
						double obstacleScale = obstacleScale(robot.getPosition(), x, y);
						// force from center
						Vector2 forceFromObstacle = forceFromObstacle(robot, x, y, 0, 0);
						obstaclesForce = obstaclesForce.add(forceFromObstacle.scale(obstacleScale));
						// force from corners
						obstaclesForce = obstaclesForce.add(forceFromObstacle(robot, x, y, -0.5, -0.5)
								.scale(obstacleScale));
						obstaclesForce = obstaclesForce.add(forceFromObstacle(robot, x, y, -0.5, +0.5)
								.scale(obstacleScale));
						obstaclesForce = obstaclesForce.add(forceFromObstacle(robot, x, y, +0.5, -0.5)
								.scale(obstacleScale));
						obstaclesForce = obstaclesForce.add(forceFromObstacle(robot, x, y, +0.5, +0.5)
								.scale(obstacleScale));
					}
				}
			}
			obstaclesForce = obstaclesForce.cutOff(32);
			robot.addForce(obstaclesForce);
			
			//			logger.info("pos: " + robot.getPosition() + ", v: " + robot.getVelocity() + ", F: " + robot
			//					.getForce());
			
			robot.timeLapse(t);
		}
	}
	
	private double distanceTo(Vector2 position, int x, int y) {
		return Math.hypot(position.getX() - x, position.getY() - y);
	}
	
	private double obstacleScale(Vector2 position, int x, int y) {
		double distance = distanceTo(position, x, y);
		double MIN_D = 0.7;
		double MAX_D = 5.0;
		if (distance < MIN_D) {
			return (MIN_D - distance) * 100.0 + 1.0;
		} else if (distance - MIN_D < MAX_D) {
			return (MAX_D - distance + MIN_D) / MAX_D;
		} else {
			return 0;
		}
	}
	
	private Vector2 forceFromObstacle(MobileRobot robot, int obstacleX, int obstacleY, double offsetX, double offsetY) {
		Vector2 diff = robot.getPosition().sub(new Vector2(obstacleX + 0.5, obstacleY + 0.5));
		double r = diff.length();
		final double K_FROM_OBSTACLE = 10;
		double forceValue = K_FROM_OBSTACLE / r / r;
		return diff.normalizeTo(forceValue);
	}
	
	private void mousePressedMap(MouseEvent event) {
		if (event.getButton() == MouseButton.PRIMARY) {
			
			Vector2 point = locateRealPoint(event);
			if (point != null) {
				for (MobileRobot robot : robots) {
					robot.setTarget(point);
				}
				drawMap();
			}
			
		} else if (event.getButton() == MouseButton.SECONDARY) {
			
			Point point = locateDiscretePoint(event);
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
			
			Point point = locateDiscretePoint(event);
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
	private void randomTargetPressed(final Event event) {
		for (MobileRobot robot : robots) {
			randomRobotTarget(robot);
		}
	}
	
	private void randomRobotTarget(MobileRobot robot) {
		Vector2 target = randomCell(map);
		robot.setTarget(target);
	}
	
	private Vector2 randomCell(TestTileMap map) {
		double x = random.nextDouble() * map.getWidthInTiles();
		double y = random.nextDouble() * map.getHeightInTiles();
		return new Vector2(x, y);
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
	
	private Point locateDiscretePoint(MouseEvent event) {
		return locateDiscretePoint(event.getX(), event.getY());
	}
	
	private Point locateDiscretePoint(double screenX, double screenY) {
		int mapX = ((int) (screenX * map.getWidthInTiles() / drawArea.getWidth()));
		int mapY = ((int) (screenY * map.getHeightInTiles() / drawArea.getHeight()));
		if (mapX < 0 || mapY < 0 || mapX >= map.getWidthInTiles() || mapY >= map.getHeightInTiles())
			return null;
		return new Point(mapX, mapY);
	}
	
	private Vector2 locateRealPoint(MouseEvent event) {
		return locateRealPoint(event.getX(), event.getY());
	}
	
	private Vector2 locateRealPoint(double screenX, double screenY) {
		double mapX = screenX * map.getWidthInTiles() / drawArea.getWidth();
		double mapY = screenY * map.getHeightInTiles() / drawArea.getHeight();
		if (mapX < 0 || mapY < 0 || mapX > map.getWidthInTiles() || mapY > map.getHeightInTiles())
			return null;
		return new Vector2(mapX, mapY);
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
		Vector2 target = robot.getTarget();
		if (target != null && !target.equals(robot.getPosition())) {
			gc.setStroke(Color.rgb(255, 0, 0));
			double targetX = target.getX() * cellW;
			double targetY = target.getY() * cellH;
			gc.strokeLine(targetX - w / 2, targetY - h / 2, targetX + w / 2, targetY + h / 2);
			gc.strokeLine(targetX - w / 2, targetY + h / 2, targetX + w / 2, targetY - h / 2);
		}
		// draw robot
		gc.setFill(Color.rgb(255, 0, 0));
		double x = robot.getPosition().getX() * cellW - w / 2;
		double y = robot.getPosition().getY() * cellH - h / 2;
		gc.fillOval(x, y, w, h);
		
	}
	
	private void updateParams() {
		paramMapSizeW.setText(Integer.toString(params.mapSizeW));
		paramMapSizeH.setText(Integer.toString(params.mapSizeH));
	}
	
	private void readParams() {
		try {
			params.mapSizeW = Integer.parseInt(paramMapSizeW.getText());
			params.mapSizeH = Integer.parseInt(paramMapSizeH.getText());
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
