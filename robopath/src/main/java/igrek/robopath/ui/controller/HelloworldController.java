package igrek.robopath.ui.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.felixroske.jfxsupport.FXMLController;
import igrek.robopath.map.TestTileMap;
import igrek.robopath.map.TileCellType;
import igrek.robopath.model.PointCoordinates;
import igrek.robopath.pathfinder.AStarPathFinder;
import igrek.robopath.pathfinder.Path;
import igrek.robopath.pathfinder.PathFinder;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;

// examples: https://github.com/roskenet/spring-javafx-examples
@FXMLController
public class HelloworldController {
	
	private Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@FXML
	private Label helloLabel;
	
	@FXML
	private TextField nameField;
	
	@FXML
	private Canvas drawArea;
	
	private TestTileMap map;
	private Path path;
	
	public HelloworldController() {
		map = new TestTileMap();
	}
	
	@FXML
	public void initialize() {
		drawArea.addEventHandler(MouseEvent.MOUSE_PRESSED, event -> {
			mousePressedMap(event);
		});
		
		drawArea.addEventHandler(MouseEvent.MOUSE_DRAGGED, event -> {
		
		});
		
		drawArea.addEventHandler(MouseEvent.MOUSE_RELEASED, event -> {
		
		});
	}
	
	private void mousePressedMap(MouseEvent event) {
		if (event.getButton() == MouseButton.PRIMARY) {
			
			PointCoordinates point = locatePoint(event);
			if (point != null) {
				TileCellType type = getMapCellType(point);
				type = transformCellTypeLeftClicked(type);
				setMapCell(point, type);
				drawMap();
			}
			
		} else if (event.getButton() == MouseButton.SECONDARY) {
			
			PointCoordinates point = locatePoint(event);
			if (point != null) {
				TileCellType type = getMapCellType(point);
				type = transformCellTypeRightClicked(type);
				setMapCell(point, type);
				drawMap();
			}
			
		}
	}
	
	private TileCellType getMapCellType(PointCoordinates point) {
		return map.get(point.x, point.y);
	}
	
	private TileCellType getMapCellType(int x, int y) {
		return map.get(x, y);
	}
	
	private void setMapCell(PointCoordinates point, TileCellType type) {
		map.set(point.x, point.y, type);
	}
	
	private TileCellType transformCellTypeLeftClicked(TileCellType type) {
		switch (type) {
			case START:
				return TileCellType.TARGET;
			case TARGET:
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
	private void setHelloText(final Event event) {
		final String textToBeShown = processName(nameField.getText());
		helloLabel.setText(textToBeShown);
		
		drawMap();
	}
	
	@FXML
	private void findPathPressed(final Event event) {
		// remove previous paths
		replaceCellTypes(TileCellType.PATH, TileCellType.EMPTY);
		
		PathFinder pathFinder = new AStarPathFinder(map, 0, true);
		PointCoordinates start = findFirstCellType(map, TileCellType.START);
		PointCoordinates target = findFirstCellType(map, TileCellType.TARGET);
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
	
	private PointCoordinates findFirstCellType(TestTileMap map, TileCellType wantedType) {
		for (int x = 0; x < map.getWidthInTiles(); x++) {
			for (int y = 0; y <= map.getHeightInTiles(); y++) {
				TileCellType type = map.get(x, y);
				if (type == wantedType)
					return new PointCoordinates(x, y);
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
		if (type == TileCellType.BLOCKED || type == TileCellType.START || type == TileCellType.TARGET || type == TileCellType.PATH) {
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
			case TARGET:
				return Color.rgb(200, 0, 0);
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
	
	private String processName(final String name) {
		if (name.equals("dupa")) {
			return "Hello Dupa!";
		} else {
			return "Hello Unknown " + name + "!";
		}
	}
	
	private PointCoordinates locatePoint(MouseEvent event) {
		return locatePoint(event.getX(), event.getY());
	}
	
	private PointCoordinates locatePoint(double screenX, double screenY) {
		int mapX = ((int) (screenX * map.getWidthInTiles() / drawArea.getWidth()));
		int mapY = ((int) (screenY * map.getHeightInTiles() / drawArea.getHeight()));
		if (mapX < 0 || mapY < 0 || mapX >= map.getWidthInTiles() || mapY >= map.getHeightInTiles())
			return null;
		return new PointCoordinates(mapX, mapY);
	}
	
}
