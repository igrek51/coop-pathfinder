package igrek.robopath.ui.simpleastar;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.felixroske.jfxsupport.FXMLController;
import igrek.robopath.model.Point;
import igrek.robopath.pathfinder.AStarPathFinder;
import igrek.robopath.pathfinder.Path;
import igrek.robopath.pathfinder.PathFinder;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;

// examples: https://github.com/roskenet/spring-javafx-examples
@FXMLController
public class SimpleAstarController {
	
	private Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@FXML
	private Canvas drawArea;
	
	private TestTileMap map;
	private Path path;
	
	public SimpleAstarController() {
		map = new TestTileMap();
	}
	
	@FXML
	public void initialize() {
		logger.info("initializing controller " + this.getClass().getSimpleName());
		
		drawArea.addEventHandler(MouseEvent.MOUSE_PRESSED, event -> {
			mousePressedMap(event);
		});
		
		drawArea.addEventHandler(MouseEvent.MOUSE_DRAGGED, event -> {
		
		});
		
		drawArea.addEventHandler(MouseEvent.MOUSE_RELEASED, event -> {
		
		});
		
		drawMap();
	}
	
	private void mousePressedMap(MouseEvent event) {
		if (event.getButton() == MouseButton.PRIMARY) {
			
			Point point = locatePoint(event);
			if (point != null) {
				TileCellType type = getMapCellType(point);
				type = transformCellTypeLeftClicked(type);
				setMapCell(point, type);
				drawMap();
			}
			
		} else if (event.getButton() == MouseButton.SECONDARY) {
			
			Point point = locatePoint(event);
			if (point != null) {
				TileCellType type = getMapCellType(point);
				type = transformCellTypeRightClicked(type);
				setMapCell(point, type);
				drawMap();
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
	
	private boolean contains(Point seek, Point... collection) {
		for (Point c : collection) {
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
		double w2 = 0.8 * cellW;
		double h2 = 0.8 * cellH;
		if (type == TileCellType.BLOCKED || type == TileCellType.START || type == TileCellType.PATH) {
			gc.setFill(getCellColor(type));
			double x2 = x * cellW + (cellW - w2) / 2;
			double y2 = y * cellH + (cellH - h2) / 2;
			gc.fillRoundRect(x2, y2, w2, h2, 10, 10);
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
		gc.setStroke(Color.rgb(100, 100, 100));
		// vertical lines
		for (int x = 0; x <= map.getWidthInTiles(); x++) {
			double x2 = x * drawArea.getWidth() / map.getWidthInTiles();
			gc.strokeLine(x2, 0, x2, drawArea.getHeight());
		}
		// horizontal lines
		for (int y = 0; y <= map.getHeightInTiles(); y++) {
			double y2 = y * drawArea.getHeight() / map.getWidthInTiles();
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
	
}
