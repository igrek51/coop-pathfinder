package igrek.robopath.modules.whca2;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

import igrek.robopath.common.Point;

class RobotsArrangementHistory {
	
	private List<StartAndGoal> coordinates;
	private Logger logger = LoggerFactory.getLogger(this.getClass());
	
	RobotsArrangementHistory(List<MobileRobot> robots) {
		this.coordinates = new ArrayList<>();
		for (MobileRobot robot : robots) {
			this.coordinates.add(new StartAndGoal(robot.getPosition(), robot.getTarget()));
		}
	}
	
	void restore(List<MobileRobot> robots) {
		if (robots.size() != coordinates.size())
			throw new RuntimeException("invalid size");
		int i = 0;
		for (MobileRobot robot : robots) {
			StartAndGoal startAndGoal = coordinates.get(i++);
			robot.resetMovesQue();
			robot.setPosition(startAndGoal.start);
			robot.setTarget(startAndGoal.goal);
		}
		logger.info("robots arrangement has been restored from history");
	}
	
	private class StartAndGoal {
		private Point start;
		private Point goal;
		
		StartAndGoal(Point start, Point goal) {
			this.start = start;
			this.goal = goal;
		}
	}
}
