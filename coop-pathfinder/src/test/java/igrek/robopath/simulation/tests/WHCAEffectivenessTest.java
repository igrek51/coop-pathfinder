package igrek.robopath.simulation.tests;

import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Random;

import ch.qos.logback.classic.Level;
import igrek.robopath.mazegenerator.MazeGenerator;
import igrek.robopath.mazegenerator.RandomFactory;
import igrek.robopath.simulation.whca.MobileRobot;
import igrek.robopath.simulation.whca.WHCAController;
import igrek.robopath.simulation.whca.WHCASimulationParams;


public class WHCAEffectivenessTest {
	
	private Logger logger = LoggerFactory.getLogger(this.getClass());
	
	private static Random random;
	
	@BeforeClass
	public static void beforeAll() {
		RandomFactory randomFactory = new RandomFactory();
		randomFactory.randomSeed = "";
		random = randomFactory.provideRandom();
		// please, shut up
		((ch.qos.logback.classic.Logger) LoggerFactory.getLogger(WHCAController.class)).setLevel(Level.INFO);
	}
	
	@Test
	public void testWHCAEffectiveness() {
		int SIMS_COUNT = 100;
		int mapW = 15, mapH = 15;
		int robotsCount = 5;
		int stepsMax = (mapW + mapH) * 5;
		int timeDimension = 15;
		
		logger.info("Simulation params: map " + mapW + "x" + mapH + ", " + robotsCount + " robots, maxSteps=" + stepsMax);
		
		for (timeDimension = 1; timeDimension <= 30; timeDimension++) {
			int successful = 0;
			for (int s = 0; s < SIMS_COUNT; s++) {
				if (runSimulation(mapW, mapH, robotsCount, stepsMax, timeDimension))
					successful++;
			}
			logger.info("time window: " + timeDimension + ": successfull: " + successful + " / " + SIMS_COUNT);
		}
		
	}
	
	private boolean runSimulation(int mapW, int mapH, int robotsCount, int stepsMax, int timeDimension) {
		WHCAController controller = createRandomSimulation(mapW, mapH, robotsCount, timeDimension);
		int steps = simulate(controller, stepsMax);
		if (steps <= 0) {
			// logger.info("failed to reach all targets");
			return false;
		} else {
			// logger.info("all targets reached in steps: " + steps);
			return true;
		}
	}
	
	
	private WHCAController createRandomSimulation(int mapW, int mapH, int robotsCount, int timeDimension) {
		WHCASimulationParams params = new WHCASimulationParams();
		params.mapSizeW = mapW;
		params.mapSizeH = mapH;
		params.robotsCount = robotsCount;
		WHCAController controller = new WHCAController(null, params);
		controller.setRandom(random);
		controller.setMazegen(new MazeGenerator(random));
		
		controller.setPrioritiesPromotion(false); // z 23% -> 86%
		
		controller.generateMaze();
		controller.placeRobots();
		params.timeDimension = timeDimension;
		controller.randomTargetPressed();
		return controller;
	}
	
	private int simulate(WHCAController controller, int stepsMax) {
		for (int step = 0; step < stepsMax; step++) {
			//			logger.debug("simulation step " + step);
			controller.stepSimulation();
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
