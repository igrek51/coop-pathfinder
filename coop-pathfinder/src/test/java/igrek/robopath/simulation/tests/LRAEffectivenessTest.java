package igrek.robopath.simulation.tests;

import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Random;

import ch.qos.logback.classic.Level;
import igrek.robopath.mazegenerator.MazeGenerator;
import igrek.robopath.mazegenerator.RandomFactory;
import igrek.robopath.simulation.lra.LRAController;
import igrek.robopath.simulation.lra.LRASimulationParams;
import igrek.robopath.simulation.lra.MobileRobot;


public class LRAEffectivenessTest {
	
	private Logger logger = LoggerFactory.getLogger(this.getClass());
	
	private static Random random;
	
	@BeforeClass
	public static void beforeAll() {
		RandomFactory randomFactory = new RandomFactory();
		randomFactory.randomSeed = "";
		random = randomFactory.provideRandom();
		// please, shut up
		((ch.qos.logback.classic.Logger) LoggerFactory.getLogger(LRAController.class)).setLevel(Level.INFO);
	}
	
	@Test
	public void testLRAEffectiveness() {
		int SIMS_COUNT = 100;
		int mapW = 15, mapH = 15;
		int robotsCount = 40;
		int stepsMax = (mapW + mapH) * 5;
		
		logger.info("Simulation params: map " + mapW + "x" + mapH + ", " + robotsCount + " robots, maxSteps=" + stepsMax);
		
		int successful = 0;
		for (int s = 0; s < SIMS_COUNT; s++) {
			if (runSimulation(mapW, mapH, robotsCount, stepsMax))
				successful++;
		}
		logger.info("successfull: " + successful + " / " + SIMS_COUNT);
		
	}
	
	private boolean runSimulation(int mapW, int mapH, int robotsCount, int stepsMax) {
		LRAController controller = createRandomSimulation(mapW, mapH, robotsCount);
		int steps = simulate(controller, stepsMax);
		if (steps <= 0) {
			logger.info("failed to reach all targets");
			return false;
		} else {
			logger.info("all targets reached in " + steps + " steps");
			return true;
		}
	}
	
	
	private LRAController createRandomSimulation(int mapW, int mapH, int robotsCount) {
		LRASimulationParams params = new LRASimulationParams();
		params.mapSizeW = mapW;
		params.mapSizeH = mapH;
		params.robotsCount = robotsCount;
		LRAController controller = new LRAController(null, params);
		controller.setRandom(random);
		controller.setMazegen(new MazeGenerator(random));
		
		controller.generateMaze();
		controller.placeRobots();
		controller.randomTargetPressed();
		return controller;
	}
	
	private int simulate(LRAController controller, int stepsMax) {
		for (int step = 0; step < stepsMax; step++) {
			//			logger.debug("simulation step " + step);
			try {
				controller.stepSimulation();
			} catch (LRAController.CollisionDetectedException e) {
				return -1;
			}
			boolean allReached = true;
			for (MobileRobot robot : controller.getRobots()) {
				//				logger.debug("robot " + robot.toString() + ": " + robot.getPosition() + " -> " + robot
				//						.getTarget());
				if (!robot.hasReachedTarget())
					allReached = false;
			}
			if (allReached)
				return step + 1;
		}
		return -1;
	}
	
}
