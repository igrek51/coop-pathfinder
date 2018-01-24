package igrek.robopath.pathfinder;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

import igrek.robopath.common.Point;
import igrek.robopath.common.TileMap;
import igrek.robopath.modules.whca2.MobileRobot;
import igrek.robopath.pathfinder.mywhca.MyWHCAPathFinder;
import igrek.robopath.pathfinder.mywhca.Path;
import igrek.robopath.pathfinder.mywhca.ReservationTable;


public class WHCAPathFinderTest {
	
	private Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@Test
	public void testSimplePath() {
		
		/*
		 * TileMap:
		 * .  .  .
		 * .  X  .
		 * .  .  .
		 */
		TileMap map = new TileMap(3, 3);
		map.setCell(1, 1, true);
		// robot
		List<MobileRobot> robots = new ArrayList<>();
		MobileRobot robot = new MobileRobot(new Point(0, 0), null, 0);
		robot.setTarget(new Point(2, 2));
		robots.add(robot);
		
		int tDim = 4;
		TileMap map2 = mapWithRobots(robots, map);
		ReservationTable reservationTable = new ReservationTable(map2.getWidthInTiles(), map2.getHeightInTiles(), tDim);
		map2.foreach((x, y, occupied) -> {
			if (occupied)
				reservationTable.setBlocked(x, y);
		});
		// find path
		robot.resetMovesQue();
		Point start = robot.getPosition();
		Point target = robot.getTarget();
		if (target != null && !target.equals(start)) {
			MyWHCAPathFinder pathFinder = new MyWHCAPathFinder(reservationTable, map);
			Path path = pathFinder.findPath(start.getX(), start.getY(), target.getX(), target.getY());
			
			//			if (path != null) {
			//				// enque path
			//				int t = 0;
			//				reservationTable.setBlocked(start.x, start.y, t);
			//				reservationTable.setBlocked(start.x, start.y, t + 1);
			//				for (int i = 1; i < path.getLength(); i++) {
			//					Path.Step step = path.getStep(i);
			//					robot.enqueueMove(step.getX(), step.getY());
			//					t++;
			//					reservationTable.setBlocked(step.getX(), step.getY(), t);
			//					reservationTable.setBlocked(step.getX(), step.getY(), t + 1);
			//				}
			//			} else {
			//				reservationTable.setBlocked(start.x, start.y);
			//			}
			logger.debug("Found path: " + path);
		}
		
	}
	
	@Test
	public void test2RobotsPath() {
		/*
		 * TileMap:
		 * S1 S2 .
		 * X  .  G2
		 * X  X  G1
		 */
		TileMap map = new TileMap(3, 3);
		map.setCell(0, 1, true);
		map.setCell(0, 2, true);
		map.setCell(1, 2, true);
		// robot
		List<MobileRobot> robots = new ArrayList<>();
		robots.add(createRobot(0, 0, 2, 2, 1));
		robots.add(createRobot(1, 0, 2, 1, 2));
		
		int tDim = 8;
		findPaths(map, robots, tDim);
	}
	
	@Test
	public void testCooperativeHidingPath() {
		/*
		 * TileMap:
		 * S1 S2=G2 G1
		 * X   .    X
		 * X   X    X
		 */
		TileMap map = new TileMap(3, 3);
		map.setCell(0, 1, true);
		map.setCell(0, 2, true);
		map.setCell(1, 2, true);
		map.setCell(2, 2, true);
		map.setCell(2, 1, true);
		// robot
		List<MobileRobot> robots = new ArrayList<>();
		robots.add(createRobot(0, 0, 2, 0, 1));
		robots.add(createRobot(1, 0, 1, 0, 2));
		
		int tDim = 8;
		findPaths(map, robots, tDim);
	}
	
	private MobileRobot createRobot(int sx, int sy, int tx, int ty, int priority) {
		MobileRobot r = new MobileRobot(new Point(sx, sy), null, priority);
		r.setTarget(new Point(tx, ty));
		return r;
	}
	
	private void findPaths(TileMap map, List<MobileRobot> robots, int tDim) {
		TileMap map2 = mapWithRobots(robots, map);
		ReservationTable reservationTable = new ReservationTable(map2.getWidthInTiles(), map2.getHeightInTiles(), tDim);
		map2.foreach((x, y, occupied) -> {
			if (occupied)
				reservationTable.setBlocked(x, y);
		});
		// find path
		for (MobileRobot robot : robots) {
			robot.resetMovesQue();
			Point start = robot.getPosition();
			Point target = robot.getTarget();
			if (target != null) {
				MyWHCAPathFinder pathFinder = new MyWHCAPathFinder(reservationTable, map);
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
				logger.debug("Found path R" + robot.getPriority() + ": " + path);
				printReservationTable(reservationTable);
				
			}
		}
	}
	
	private void printReservationTable(ReservationTable reservationTable) {
		logger.debug("Reservation table:");
		for (int t = 0; t < reservationTable.getTimeDimension(); t++) {
			logger.debug("t = " + t);
			for (int y = 0; y < reservationTable.getHeight(); y++) {
				StringBuilder line = new StringBuilder("  ");
				for (int x = 0; x < reservationTable.getWidth(); x++) {
					line.append(reservationTable.isBlocked(x, y, t) ? "X" : ".");
					line.append(" ");
				}
				logger.debug(line.toString());
			}
		}
	}
	
	private TileMap mapWithRobots(List<MobileRobot> robots, TileMap map) {
		TileMap map2 = new TileMap(map);
		for (MobileRobot robot : robots) {
			//TODO			map2.setCell(robot.getPosition(), true);
		}
		return map2;
	}
	
	
}
