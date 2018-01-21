package igrek.robopath.modules.lra;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import igrek.robopath.mazegenerator.MazeGenerator;
import igrek.robopath.model.Point;
import igrek.robopath.pathfinder.coop.Coordinater;
import igrek.robopath.pathfinder.coop.Unit;
import igrek.robopath.pathfinder.mystar.TileMap;
import raft.kilavuz.runtime.NoPathException;

public class Controller {
	
	private Logger logger = LoggerFactory.getLogger(this.getClass());
	private Random random = new Random();
	
	private TileMap map;
	private List<MobileRobot> robots = new ArrayList<>();
	private SimulationParams params;
	
	Coordinater coordinater;
	Map<MobileRobot, Unit> unitsMap = new HashMap<>();
	final int DEPTH = 32;
	
	public Controller(Presenter presenter, SimulationParams params) {
		this.params = params;
		resetMap();
	}
	
	public TileMap getMap() {
		return map;
	}
	
	public List<MobileRobot> getRobots() {
		return robots;
	}
	
	synchronized void resetMap() {
		map = new TileMap(params.mapSizeW, params.mapSizeH);
		robots.clear();
		coordinater = null;
	}
	
	synchronized Coordinater provideCoordinater() {
		if (coordinater == null)
			coordinater = new CoordinaterFactory().provideCoordinater(DEPTH, params.mapSizeW, params.mapSizeH, unitsMap, robots, map);
		return coordinater;
	}
	
	synchronized void placeRobots() {
		robots.clear();
		unitsMap.clear();
		for (int i = 0; i < params.robotsCount; i++) {
			Point cell = randomUnoccupiedCellForRobot(map);
			createMobileRobot(cell, i);
		}
	}
	
	synchronized MobileRobot createMobileRobot(Point point, int i) {
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
				findPaths();
			}
		}
	}
	
	void randomTargetPressed() {
		for (MobileRobot robot : robots) {
			robot.setTarget(null); // clear targets - not to block each other during randoming
		}
		for (MobileRobot robot : robots) {
			randomRobotTarget(robot);
		}
	}
	
	synchronized void generateMaze() {
		new MazeGenerator(map).generateMaze();
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
		Point target = randomUnoccupiedCellForTarget(map);
		robot.setTarget(target);
		Unit unit = unitsMap.get(robot);
		if (unit != null) {
			unit.setDestination(target.x, target.y);
		}
	}
	
	private Point randomUnoccupiedCellForTarget(TileMap map) {
		// get all unoccupied cells
		List<Point> frees = new ArrayList<>();
		map.foreach((x, y, occupied) -> {
			if (!occupied)
				frees.add(new Point(x, y));
		});
		// remove occupied by other targets
		for (MobileRobot robot : robots) {
			Point target = robot.getTarget();
			if (target != null)
				frees.remove(target);
		}
		if (frees.isEmpty())
			return null;
		// random from list
		return frees.get(random.nextInt(frees.size()));
	}
	
	private Point randomUnoccupiedCellForRobot(TileMap map) {
		// get all unoccupied cells
		List<Point> frees = new ArrayList<>();
		map.foreach((x, y, occupied) -> {
			if (!occupied)
				frees.add(new Point(x, y));
		});
		// remove occupied by other robots
		for (MobileRobot robot : robots) {
			Point p = robot.getPosition();
			if (p != null)
				frees.remove(p);
		}
		if (frees.isEmpty())
			return null;
		// random from list
		return frees.get(random.nextInt(frees.size()));
	}
	
	synchronized void findPaths() {
		try {
			coordinater = null;
			coordinater = provideCoordinater();
			coordinater.iterate();
			for (MobileRobot robot : robots) {
				Unit unit = unitsMap.get(robot);
				if (unit != null) {
					//					robot.setPosition(new Point(unit.getLocation().x, unit.getLocation().z));
					List<Unit.PathPoint> path = unit.getPath();
					// enque moves
					robot.resetMovesQue();
					for (Unit.PathPoint point : path) {
						robot.enqueueMove(point.x, point.z);
					}
				}
			}
		} catch (NoPathException npe) {
			npe.printStackTrace();
		}
	}
	
	synchronized void stepSimulation() {
		for (MobileRobot robot : robots) {
			if (robot.hasNextMove())
				robot.setPosition(robot.pollNextMove());
		}
	}
}
