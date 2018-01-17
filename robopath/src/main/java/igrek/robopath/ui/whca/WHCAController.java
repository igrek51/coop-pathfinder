package igrek.robopath.ui.whca;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

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
import igrek.robopath.ui.whca.robot.MobileRobot;

public class WHCAController {
	
	private Logger logger = LoggerFactory.getLogger(this.getClass());
	
	private WHCAPresenter presenter;
	
	private TileMap map;
	private List<MobileRobot> robots = new ArrayList<>();
	
	private Random random = new Random();
	
	final int DEPTH = 32;
	int unitCount = 6;
	
	Coordinater coordinater;
	Grid grid;
	Map<Integer, PathPanel.Point> unitPositions = new HashMap<Integer, PathPanel.Point>();
	Map<Integer, NodePool.Point> unitTargets = new HashMap<Integer, NodePool.Point>();
	
	public WHCAController(WHCAPresenter presenter) {
		this.presenter = presenter;
		resetMap();
	}
	
	public TileMap getMap() {
		return map;
	}
	
	public List<MobileRobot> getRobots() {
		return robots;
	}
	
	public Coordinater getCoordinater() {
		return coordinater;
	}
	
	public Map<Integer, PathPanel.Point> getUnitPositions() {
		return unitPositions;
	}
	
	public Map<Integer, NodePool.Point> getUnitTargets() {
		return unitTargets;
	}
	
	public void resetMap() {
		map = new TileMap(presenter.params.mapSizeW, presenter.params.mapSizeH);
		robots.clear();
		
		coordinater = new Coordinater(DEPTH);
		grid = coordinater.grid;
		for (Unit unit : coordinater.units.values())
			unitPositions.put(unit.id, new PathPanel.Point(unit.getLocation()));
		reset();
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
	
	void placeRobots() {
		robots.clear();
		for (int i = 0; i < presenter.params.robotsCount; i++) {
			createMobileRobot(randomUnoccupiedCell(map), i);
		}
	}
	
	public void createMobileRobot(Point point, int i) {
		robots.add(new MobileRobot(point, robot -> onTargetReached(robot), i));
	}
	
	
	private void onTargetReached(MobileRobot robot) {
		if (presenter.params.robotAutoTarget) {
			//			randomRobotTarget(robot);
		}
	}
	
	void randomTargetPressed() {
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
	
	void generateMaze() {
		new MazeGenerator(map).generateMaze();
	}
	
	void timeLapse(double t) {
		for (MobileRobot robot : robots) {
			robot.timeLapse(t);
		}
	}
	
	MobileRobot occupiedByRobot(Point point) {
		for (MobileRobot robot : robots) {
			if (robot.getPosition().equals(point))
				return robot;
		}
		return null;
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
}
