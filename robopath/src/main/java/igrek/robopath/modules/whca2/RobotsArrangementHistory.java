package igrek.robopath.modules.whca2;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

class RobotsArrangementHistory {
	
	private List<MobileRobot> deepCopy;
	private Logger logger = LoggerFactory.getLogger(this.getClass());
	
	RobotsArrangementHistory(List<MobileRobot> robots) {
		deepCopy = new ArrayList<>();
		for (MobileRobot robot : robots) {
			deepCopy.add(robot.clone());
		}
	}
	
	void restore(List<MobileRobot> robots) {
		robots.clear();
		for (MobileRobot robot : deepCopy) {
			robot.resetMovesQue();
			robots.add(robot);
		}
		logger.info("robots arrangement has been restored from history");
	}
	
}
