package igrek.robopath.simulation.whca;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import java.util.LinkedList;
import java.util.List;

import de.felixroske.jfxsupport.FXMLController;
import igrek.robopath.common.Point;
import igrek.robopath.common.tilemap.TileMap;
import igrek.robopath.simulation.common.ResizableCanvas;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.Event;
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
@Qualifier("whca2Presenter")
public class Presenter {
	
	private final double FPS = 24;
	private final double MOVE_STEP_DURATION = 500;
	
	private Controller controller;
	private Logger logger = LoggerFactory.getLogger(this.getClass());
	private Boolean pressedTransformer;
	private long lastSimulationTime;
	private RobotsArrangementHistory arrangementHistory;
	private Timeline animationTimeline;
	private Timeline simulationTimeline;
	
	@FXML
	private ResizableCanvas drawArea;
	@FXML
	private VBox drawAreaContainer;
	
	@Autowired
	@Qualifier("whca2Params")
	private SimulationParams params;
	@FXML
	public TextField paramMapSizeW;
	@FXML
	public TextField paramMapSizeH;
	@FXML
	public TextField paramRobotsCount;
	@FXML
	public CheckBox paramRobotAutoTarget;
	@FXML
	public TextField paramTimeDimension;
	
	
	@Autowired
	public void setController(@Qualifier("whca2Controller") Controller controller) {
		this.controller = controller;
	}
	
	@FXML
	public void initialize() {
		logger.info("initializing " + this.getClass().getSimpleName());
		
		drawAreaContainer.widthProperty().addListener(o -> drawAreaContainerResized());
		drawAreaContainer.heightProperty().addListener(o -> drawAreaContainerResized());
		
		drawArea.addEventHandler(MouseEvent.MOUSE_PRESSED, event -> {
			mousePressed(event);
		});
		drawArea.addEventHandler(MouseEvent.MOUSE_DRAGGED, event -> {
			mouseDragged(event);
		});
		drawArea.addEventHandler(MouseEvent.MOUSE_RELEASED, event -> {
			mouseReleased(event);
		});
		
		params.init(this);
		params.sendToUI();
		startSimulationTimer();
		startRepaintTimer();
	}
	
	TileMap getMap() {
		return controller.getMap();
	}
	
	List<MobileRobot> getRobots() {
		return controller.getRobots();
	}
	
	@FXML
	private void resetMap(final Event event) {
		if (event != null)
			params.readFromUI();
		
		controller.resetMap();
		
		if (event != null)
			drawAreaContainerResized();
	}
	
	@FXML
	private void placeRobots(final Event event) {
		if (event != null)
			params.readFromUI();
		controller.placeRobots();
	}
	
	@FXML
	private void generateMaze(final Event event) {
		if (event != null)
			params.readFromUI();
		controller.generateMaze();
	}
	
	private void drawAreaContainerResized() {
		double containerWidth = drawAreaContainer.getWidth();
		double containerHeight = drawAreaContainer.getHeight();
		double maxCellW = containerWidth / getMap().getWidthInTiles();
		double maxCellH = containerHeight / getMap().getHeightInTiles();
		double cellSize = maxCellW < maxCellH ? maxCellW : maxCellH; // min
		drawArea.setWidth(cellSize * getMap().getWidthInTiles());
		drawArea.setHeight(cellSize * getMap().getHeightInTiles());
	}
	
	
	private void startRepaintTimer() {
		// animation timer
		animationTimeline = new Timeline(new KeyFrame(Duration.millis(1000 / FPS), event -> repaint()));
		animationTimeline.setCycleCount(Timeline.INDEFINITE);
		animationTimeline.play();
	}
	
	private void startSimulationTimer() {
		new Thread(() -> {
			try {
				simulationTimeline = new Timeline(new KeyFrame(Duration.millis(MOVE_STEP_DURATION), event -> {
					controller.stepSimulation();
					lastSimulationTime = System.currentTimeMillis();
				}));
				simulationTimeline.setCycleCount(Timeline.INDEFINITE);
				simulationTimeline.play();
			} catch (Throwable t) {
				logger.error(t.getMessage(), t);
			}
		}).start();
	}
	
	private void mousePressed(MouseEvent event) {
		TileMap map = getMap();
		List<MobileRobot> robots = getRobots();
		
		if (event.getButton() == MouseButton.PRIMARY) {
			
			Point point = locatePoint(map, event);
			if (point != null) {
				Boolean state = map.getCell(point);
				if (!state) {
					MobileRobot occupiedBy = controller.occupiedByRobot(point);
					if (occupiedBy != null) {
						robots.remove(occupiedBy);
					} else {
						controller.createMobileRobot(point);
					}
				}
				repaint();
			}
			
		} else if (event.getButton() == MouseButton.SECONDARY) {
			
			Point point = locatePoint(map, event);
			if (point != null) {
				Boolean state = !map.getCell(point);
				map.setCell(point, state);
				pressedTransformer = state;
				repaint();
			}
			
		}
	}
	
	private void mouseDragged(MouseEvent event) {
		if (event.getButton() == MouseButton.SECONDARY) {
			TileMap map = getMap();
			Point point = locatePoint(map, event);
			if (point != null) {
				Boolean state = map.getCell(point);
				if (state != pressedTransformer) {
					map.setCell(point, pressedTransformer);
					repaint();
				}
			}
			
		}
	}
	
	private void mouseReleased(MouseEvent event) {
		TileMap map = getMap();
		List<MobileRobot> robots = getRobots();
		
		if (event.getButton() == MouseButton.PRIMARY) {
			Point point = locatePoint(map, event);
			if (point != null) {
				if (!robots.isEmpty()) {
					MobileRobot lastRobot = robots.get(robots.size() - 1);
					lastRobot.setTarget(point);
					repaint();
				}
			}
		}
	}
	
	@FXML
	private void randomTargetPressed(final Event event) {
		controller.randomTargetPressed();
		arrangementHistory = new RobotsArrangementHistory(getRobots()); // store history
	}
	
	@FXML
	private void eventReadParams(final Event event) {
		params.readFromUI();
	}
	
	
	//	VIEW
	synchronized void repaint() {
		drawMap();
	}
	
	private void drawMap() {
		GraphicsContext gc = drawArea.getGraphicsContext2D();
		drawGrid(gc);
		drawCells(gc);
		drawRobots(gc);
	}
	
	private void drawCells(GraphicsContext gc) {
		TileMap map = getMap();
		map.foreach((x, y, occupied) -> drawCell(gc, x, y, occupied));
	}
	
	private void drawCell(GraphicsContext gc, int x, int y, boolean occupied) {
		TileMap map = getMap();
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
		TileMap map = getMap();
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
	
	Point locatePoint(TileMap map, MouseEvent event) {
		double screenX = event.getX();
		double screenY = event.getY();
		int mapX = ((int) (screenX * map.getWidthInTiles() / drawArea.getWidth()));
		int mapY = ((int) (screenY * map.getHeightInTiles() / drawArea.getHeight()));
		if (mapX < 0 || mapY < 0 || mapX >= map.getWidthInTiles() || mapY >= map.getHeightInTiles())
			return null;
		return new Point(mapX, mapY);
	}
	
	private void drawRobots(GraphicsContext gc) {
		double simulationStepProgress = (System.currentTimeMillis() - lastSimulationTime) / MOVE_STEP_DURATION;
		if (controller.isCalculatingPaths())
			simulationStepProgress = 0;
		List<MobileRobot> robots = getRobots();
		for (MobileRobot robot : robots) {
			drawRobot(gc, robot, simulationStepProgress);
		}
	}
	
	private void drawRobot(GraphicsContext gc, MobileRobot robot, double stepProgress) {
		TileMap map = getMap();
		double cellW = drawArea.getWidth() / map.getWidthInTiles();
		double cellH = drawArea.getHeight() / map.getHeightInTiles();
		double w = 0.6 * cellW;
		double h = 0.6 * cellH;
		Color robotColor = robotColor(robot.getId());
		// draw target
		gc.setLineWidth(cellW / 18);
		if (robot.getTarget() != null) {
			Point target = robot.getTarget();
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
		double x = robot.getInterpolatedX(stepProgress) * cellW + cellW / 2 - w / 2;
		double y = robot.getInterpolatedY(stepProgress) * cellH + cellH / 2 - h / 2;
		gc.fillOval(x, y, w, h);
		// draw its priority
		gc.setFill(robotColor(robot.getId(), 0.5));
		gc.setTextAlign(TextAlignment.CENTER);
		gc.setTextBaseline(VPos.CENTER);
		gc.setFont(new Font("System", h / 2));
		String identifier = robot.getId() + "." + robot.getPriority();
		gc.fillText(identifier, x + w / 2, y + h / 2);
	}
	
	private Color robotColor(int index) {
		return robotColor(index, 1);
	}
	
	private Color robotColor(int index, double b) {
		List<MobileRobot> robots = getRobots();
		double hue = 360.0 * ((index - 1) % robots.size()) / robots.size();
		return Color.hsb(hue, 1, b);
	}
	
	@FXML
	private void buttonPathfind() {
		params.readFromUI();
		restartTimelines();
		new Thread(() -> controller.findPaths()).start();
	}
	
	private void restartTimelines() {
		lastSimulationTime = System.currentTimeMillis();
		animationTimeline.playFromStart();
		simulationTimeline.playFromStart();
	}
	
	@FXML
	private void buttonRestoreArrangement() {
		if (arrangementHistory != null) {
			controller.setRobots(arrangementHistory.restore(controller.getRobots()));
		}
	}
}
