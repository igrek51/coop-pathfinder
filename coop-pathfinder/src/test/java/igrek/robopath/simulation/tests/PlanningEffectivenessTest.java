package igrek.robopath.simulation.tests;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Random;

import igrek.robopath.mazegenerator.MazeGenerator;
import igrek.robopath.mazegenerator.RandomFactory;
import igrek.robopath.simulation.whca.Controller;
import igrek.robopath.simulation.whca.MobileRobot;
import igrek.robopath.simulation.whca.SimulationParams;


public class PlanningEffectivenessTest {
	
	private Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@Test
	public void test() {
		SimulationParams params = new SimulationParams();
		Controller controller = new Controller(null, params);
		Random random = new RandomFactory().provideRandom();
		controller.setRandom(random);
		controller.setMazegen(new MazeGenerator(random));
		
		controller.placeRobots();
		params.timeDimension = controller.getRobots().size() + 1;
		
		controller.randomTargetPressed();
		
		int STEPS_MAX = 10;
		for (int i = 0; i < STEPS_MAX; i++) {
			logger.info("simulation step " + i);
			controller.stepSimulation();
			for (MobileRobot robot : controller.getRobots()) {
				logger.info("robot " + robot.toString() + ": " + robot.getPosition() + " -> " + robot
						.getTarget());
			}
		}
	}
	
}
