package igrek.robopath.pathfinder.whca;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

import igrek.robopath.common.Point;
import igrek.robopath.common.tilemap.TileMap;
import igrek.robopath.modules.whca2.MobileRobot;
import igrek.robopath.pathfinder.mywhca.MyWHCAPathFinder;
import igrek.robopath.pathfinder.mywhca.Path;
import igrek.robopath.pathfinder.mywhca.ReservationTable;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;


public class WHCAPathFinderTest {
	
	private Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@Test
	public void testSimplePath() {
		/*
		 * TileMap:
		 * S1 .  .
		 * .  X  .
		 * .  X  G1
		 */
		TileMap map = new TileMap(3, 3);
		map.setCell(1, 1, true);
		map.setCell(1, 2, true);
		// robots
		List<MobileRobot> robots = new ArrayList<>();
		robots.add(createRobot(0, 0, 2, 2, 0));
		
		List<Path> paths = findPaths(map, robots, 4);
		assertEquals(1, paths.size());
		Path path = paths.get(0);
		assertNotNull(path);
		assertEquals("[(0, 0, 0), (1, 0, 1), (2, 0, 2), (2, 1, 3)]", path.toString());
		
		paths = findPaths(map, robots, 6);
		assertEquals("[(0, 0, 0), (1, 0, 1), (2, 0, 2), (2, 1, 3), (2, 2, 4), (2, 2, 5)]", paths.get(0)
				.toString());
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
		// robots
		List<MobileRobot> robots = new ArrayList<>();
		robots.add(createRobot(0, 0, 2, 2, 1));
		robots.add(createRobot(1, 0, 2, 1, 2));
		
		List<Path> paths = findPaths(map, robots, 8);
		assertNotNull(paths.get(0));
		assertNotNull(paths.get(1));
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
		// robots
		List<MobileRobot> robots = new ArrayList<>();
		robots.add(createRobot(0, 0, 2, 0, 1));
		robots.add(createRobot(1, 0, 1, 0, 2));
		
		List<Path> paths = findPaths(map, robots, 5);
		assertEquals("[(0, 0, 0), (1, 0, 1), (2, 0, 2), (2, 0, 3), (2, 0, 4)]", paths.get(0)
				.toString());
		assertEquals("[(1, 0, 0), (1, 1, 1), (1, 1, 2), (1, 0, 3), (1, 0, 4)]", paths.get(1)
				.toString());
		
		paths = findPaths(map, robots, 4);
		assertEquals("[(0, 0, 0), (1, 0, 1), (2, 0, 2), (2, 0, 3)]", paths.get(0).toString());
		assertEquals("[(1, 0, 0), (1, 1, 1), (1, 1, 2), (1, 0, 3)]", paths.get(1).toString());
		
		paths = findPaths(map, robots, 3);
		assertEquals("[(0, 0, 0), (1, 0, 1), (2, 0, 2)]", paths.get(0).toString());
		assertStaticPosition(paths.get(1), 1, 0);
		
		paths = findPaths(map, robots, 2);
		assertEquals("[(0, 0, 0), (1, 0, 1)]", paths.get(0).toString());
		assertStaticPosition(paths.get(1), 1, 0);
		
		paths = findPaths(map, robots, 1);
		assertEquals("[(0, 0, 0)]", paths.get(0).toString());
		assertStaticPosition(paths.get(1), 1, 0);
	}
	
	@Test
	public void testBlocking() {
		/*
		 * TileMap:
		 * S2 S1=G1 G2
		 * .    X   .
		 * .    X   .
		 */
		TileMap map = new TileMap(3, 3);
		map.setCell(1, 1, true);
		map.setCell(1, 2, true);
		// robots
		List<MobileRobot> robots = new ArrayList<>();
		robots.add(createRobot(1, 0, 1, 0, 1));
		robots.add(createRobot(0, 0, 2, 0, 2));
		
		List<Path> paths = findPaths(map, robots, 3);
		assertEquals("[(1, 0, 0), (1, 0, 1), (1, 0, 2)]", paths.get(0).toString());
		assertStaticPosition(paths.get(1), 0, 0);
	}
	
	@Test
	public void testDetour() {
		/*
		 * TileMap:
		 * S1=G1 S2  .
		 * G2    X   .
		 * .     .   .
		 */
		TileMap map = new TileMap(3, 3);
		map.setCell(1, 1, true);
		// robots
		List<MobileRobot> robots = new ArrayList<>();
		robots.add(createRobot(0, 0, 0, 0, 1));
		robots.add(createRobot(1, 0, 0, 1, 2));
		{
			List<Path> paths = findPaths(map, robots, 5);
			assertStaticPosition(paths.get(0), 0, 0);
			assertStaticPosition(paths.get(1), 1, 0);
		}
		{
			List<Path> paths = findPaths(map, robots, 6);
			assertStaticPosition(paths.get(0), 0, 0);
			assertEquals("[(1, 0, 0), (2, 0, 1), (2, 1, 2), (2, 2, 3), (1, 2, 4), (0, 2, 5)]", paths
					.get(1)
					.toString());
		}
		{
			List<Path> paths = findPaths(map, robots, 7);
			assertStaticPosition(paths.get(0), 0, 0);
			assertEquals("[(1, 0, 0), (2, 0, 1), (2, 1, 2), (2, 2, 3), (1, 2, 4), (0, 2, 5), (0, 1, 6)]", paths
					.get(1)
					.toString());
		}
	}
	
	private void assertStaticPosition(Path path, int expectedX, int expectedY) {
		assertTrue(path.getLength() > 0);
		try {
			for (int i = 0; i < path.getLength(); i++) {
				Path.Step step = path.getStep(i);
				assertEquals(expectedX, step.getX());
				assertEquals(expectedY, step.getY());
			}
		} catch (AssertionError e) {
			String message = "Expected static position: (" + expectedX + ", " + expectedY + ")\n";
			message += "Actual path: " + path;
			throw new AssertionError(message, e);
		}
	}
	
	private MobileRobot createRobot(int sx, int sy, int tx, int ty, int priority) {
		MobileRobot r = new MobileRobot(new Point(sx, sy), null, priority, priority);
		r.setTarget(new Point(tx, ty));
		return r;
	}
	
	private List<Path> findPaths(TileMap map, List<MobileRobot> robots, int tDim) {
		List<Path> paths = new ArrayList<>();
		TileMap map2 = new TileMap(map);
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
				paths.add(path);
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
				reservationTable.log();
			} else {
				reservationTable.setBlocked(start.x, start.y);
			}
		}
		return paths;
	}
	
}
