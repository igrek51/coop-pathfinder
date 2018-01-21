package igrek.robopath.modules.whca;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import igrek.robopath.mazegenerator.MazeGenerator;
import igrek.robopath.model.Point;
import igrek.robopath.modules.whca.robot.MobileRobot;
import igrek.robopath.pathfinder.coop.Coordinater;
import igrek.robopath.pathfinder.coop.Unit;
import igrek.robopath.pathfinder.mystar.ReservationTable;
import igrek.robopath.pathfinder.mystar.TileMap;
import raft.kilavuz.runtime.NoPathException;

public class Controller {
	
	private Logger logger = LoggerFactory.getLogger(this.getClass());
	
	private Presenter presenter;
	private SimulationParams params;
	
	private TileMap map;
	private List<MobileRobot> robots = new ArrayList<>();
	
	private Random random = new Random();
	
	final int DEPTH = 32;
	
	Coordinater coordinater;
	Map<MobileRobot, Unit> unitsMap = new HashMap<>();
	
	public Controller(Presenter presenter, SimulationParams params) {
		this.presenter = presenter;
		this.params = params;
		resetMap();
	}
	
	public TileMap getMap() {
		return map;
	}
	
	public List<MobileRobot> getRobots() {
		return robots;
	}
	
	Coordinater getCoordinater() {
		return coordinater;
	}
	
	void resetMap() {
		map = new TileMap(params.mapSizeW, params.mapSizeH);
		robots.clear();
		reset();
	}
	
	Coordinater provideCoordinater() {
		if (coordinater == null)
			coordinater = new CoordinaterFactory().provideCoordinater(DEPTH, params.mapSizeW, params.mapSizeH, unitsMap, robots, map);
		return coordinater;
	}
	
	void reset() {
		coordinater = null;
	}
	
	void placeRobots() {
		robots.clear();
		unitsMap.clear();
		for (int i = 0; i < params.robotsCount; i++) {
			Point cell = randomUnoccupiedCell(map);
			createMobileRobot(cell, i);
		}
	}
	
	MobileRobot createMobileRobot(Point point, int i) {
		MobileRobot robo = new MobileRobot(point, robot -> onTargetReached(robot), i);
		robots.add(robo);
		return robo;
	}
	
	
	private void onTargetReached(MobileRobot robot) {
		if (params.robotAutoTarget) {
			if (robot.getTarget() == null || robot.hasReachedTarget()) {
				logger.info("assigning new target");
				randomRobotTarget(robot);
				coordinater = null;
				stepTake();
			}
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
			robot.setTarget(null); // clear targets - not to block during randoming
		}
		for (MobileRobot robot : robots) {
			randomRobotTarget(robot);
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
	
	private void randomRobotTarget(MobileRobot robot) {
		robot.resetNextMoves();
		//		Point start = robot.lastTarget();
		Point target = randomUnoccupiedCell(map);
		robot.setTarget(target);
		Unit unit = unitsMap.get(robot);
		if (unit != null) {
			unit.setDestination(target.x, target.y);
		}
		//		MyStarPathFinder pathFinder = new MyStarPathFinder(reservationTable);
		//		Path path = pathFinder.findPath(start.getX(), start.getY(), target.getX(), target.getY());
		//		if (path != null) {
		//			// enque path
		//			int t = 0;
		//			reservationTable.setBlocked(start.x, start.y, t);
		//			reservationTable.setBlocked(start.x, start.y, t + 1);
		//			for (int i = 1; i < path.getLength(); i++) {
		//				Path.Step step = path.getStep(i);
		//				robot.enqueueMove(step.getX(), step.getY());
		//				t++;
		//				reservationTable.setBlocked(step.getX(), step.getY(), t);
		//				reservationTable.setBlocked(step.getX(), step.getY(), t + 1);
		//			}
		//		} else {
		//			reservationTable.setBlocked(start.x, start.y);
		//		}
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
		// remove occupied by other targets
		for (MobileRobot robot : robots) {
			Point target = robot.getTarget();
			if (target != null) {
				frees.remove(target);
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
	
	void stepTake() {
		try {
			coordinater = null;
			coordinater = provideCoordinater();
			coordinater.iterate();
			for (MobileRobot robot : robots) {
				Unit unit = unitsMap.get(robot);
				if (unit != null) {
					robot.setPosition(new Point(unit.getLocation().x, unit.getLocation().z));
					List<Unit.PathPoint> path = unit.getPath();
					for (Unit.PathPoint point : path) {
						robot.enqueueMove(point.x, point.z);
					}
					
					//					unitPositions.put(unit.id, new PathPanel.Point(unit.getLocation()));
					//					unit.next();
					//					unitTargets.put(unit.id, unit.getLocation());
					
					//					logger.info("position updated: " + unit.getLocation() + ", " + unit.getDestination());
				}
			}
		} catch (NoPathException npe) {
			npe.printStackTrace();
		}
	}
}
