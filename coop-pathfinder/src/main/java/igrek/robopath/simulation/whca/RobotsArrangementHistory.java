package igrek.robopath.simulation.whca;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

class RobotsArrangementHistory {
	
	private Logger logger = LoggerFactory.getLogger(this.getClass());
	
	private List<MobileRobot> deepCopy;
	private int timeWindow;
	
	RobotsArrangementHistory(List<MobileRobot> robots, int timeWindow) {
		deepCopy = new ArrayList<>();
		for (MobileRobot robot : robots) {
			deepCopy.add(robot.clone());
		}
		this.timeWindow = timeWindow;
	}
	
	List<MobileRobot> restoreRobots(List<MobileRobot> robots) {
		robots.clear();
		for (MobileRobot robot : deepCopy) {
			// clone once again to keep original here
			robots.add(robot.clone());
		}
		logger.info("robots arrangement has been restored from history");
		return robots;
	}
	
	public int restoreTimeWindow() {
		return timeWindow;
	}
}
