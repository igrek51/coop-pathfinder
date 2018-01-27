package igrek.robopath.pathfinder.mywhca;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import igrek.robopath.common.Point;
import igrek.robopath.common.tilemap.TileMap;
import igrek.robopath.modules.whca2.MobileRobot;

public class WHCAUtils {
	
	private static Logger logger = LoggerFactory.getLogger(WHCAUtils.class);
	
	public static void findPath(MobileRobot robot, ReservationTable reservationTable, TileMap map) {
		logger.info("robot: " + (robot.getPriority() + 1) + " - planning path");
		robot.resetMovesQue();
		Point start = robot.getPosition();
		Point target = robot.getTarget();
		if (target != null) {
			MyWHCAPathFinder pathFinder = new MyWHCAPathFinder(reservationTable, map);
			Path path = pathFinder.findPath(start.getX(), start.getY(), target.getX(), target.getY());
			logger.info("path: " + path);
			if (path != null) {
				// enque path
				int t = 0;
				reservationTable.setBlocked(start.x, start.y, t);
				reservationTable.setBlocked(start.x, start.y, t + 1);
				Path.Step step = null;
				for (int i = 1; i < path.getLength(); i++) {
					step = path.getStep(i);
					robot.enqueueMove(step.getX(), step.getY());
					t++;
					reservationTable.setBlocked(step.getX(), step.getY(), t);
					reservationTable.setBlocked(step.getX(), step.getY(), t + 1);
				}
				// fill the rest with last position
				if (step != null) {
					for (int i = t + 1; i < reservationTable.getTimeDimension(); i++) {
						reservationTable.setBlocked(step.getX(), step.getY(), i);
					}
				}
			} else {
				logger.warn("path is null");
				reservationTable.setBlocked(start.x, start.y);
			}
		}
	}
	
}
