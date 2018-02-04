package igrek.robopath.simulation.tests;

import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Random;

import ch.qos.logback.classic.Level;
import igrek.robopath.common.TileMap;
import igrek.robopath.mazegenerator.MazeGenerator;
import igrek.robopath.mazegenerator.NoNextFieldException;
import igrek.robopath.mazegenerator.RandomFactory;
import igrek.robopath.simulation.lra.LRAController;
import igrek.robopath.simulation.lra.LRASimulationParams;
import igrek.robopath.simulation.lra.MobileRobot;
import igrek.robopath.simulation.whca.WHCAController;
import igrek.robopath.simulation.whca.WHCASimulationParams;


public class PlanningEffectivenessTest {
	
	private Logger logger = LoggerFactory.getLogger(this.getClass());
	
	private static Random random;
	
	@BeforeClass
	public static void beforeAll() {
		RandomFactory randomFactory = new RandomFactory();
		randomFactory.randomSeed = "";
		random = randomFactory.provideRandom();
		// please, shut up
		((ch.qos.logback.classic.Logger) LoggerFactory.getLogger(LRAController.class)).setLevel(Level.INFO);
		((ch.qos.logback.classic.Logger) LoggerFactory.getLogger(WHCAController.class)).setLevel(Level.INFO);
	}
	
	@Test
	public void testBothAlgorithmsEffectiveness() {
		int SIMS_COUNT = 100;
		
		int bothSuccessful = 0;
		int whcaSuccess = 0;
		int lraSuccess = 0;
		int bothFailed = 0;
		for (int s = 1; s <= SIMS_COUNT; s++) {
			// random params
			int mapW = randomInt(5, 39);
			int mapH = randomInt(5, 39);
			int robotsCount = randomInt(1, mapW * mapH / 40);
			int stepsMax = (mapW + mapH) * 2;
			logger.info("Simulation " + s + ": map " + mapW + "x" + mapH + ", " + robotsCount + " robots, maxSteps=" + stepsMax);
			
			//			prepare WHCA
			WHCAController whcaController = createWHCARandomSimulation(mapW, mapH, robotsCount);
			try {
				whcaController.generateMaze();
				whcaController.placeRobots();
				whcaController.getParams().timeDimension = whcaController.getRobots().size() + 1;
				whcaController.randomTargetPressed();
			} catch (NoNextFieldException e) {
				logger.warn(e.getMessage());
				continue;
			}
			//			prepare LRA
			LRAController lraController = createLRARandomSimulation(mapW, mapH, robotsCount);
			// same maze as in whca
			TileMap whcaMap = whcaController.getMap();
			TileMap lraMap = lraController.getMap();
			for (int x = 0; x < lraMap.getWidthInTiles(); x++) {
				for (int y = 0; y < lraMap.getHeightInTiles(); y++) {
					lraMap.setCell(x, y, whcaMap.getCell(x, y));
				}
			}
			// robots locations same as in whca
			for (int i = 0; i < robotsCount; i++) {
				igrek.robopath.simulation.whca.MobileRobot whcaRobot = whcaController.getRobots()
						.get(i);
				// set start point
				lraController.createMobileRobot(whcaRobot.getPosition(), i);
				//set target
				lraController.getRobots().get(i).setTarget(whcaRobot.getTarget());
			}
			//			simulate both
			int whcaSteps = simulateWHCA(whcaController, stepsMax);
			//			if (whcaSteps <= 0) {
			//				logger.info("WHCA: failed to reach all targets");
			//			} else {
			//				logger.info("WHCA: all targets reached in " + whcaSteps + " steps");
			//			}
			int lraSteps = simulateLRA(lraController, stepsMax);
			//			if (lraSteps <= 0) {
			//				logger.info("LRA: failed to reach all targets");
			//			} else {
			//				logger.info("LRA: all targets reached in " + lraSteps + " steps");
			//			}
			//			Summary
			if (lraSteps > 0 && whcaSteps > 0) {
				bothSuccessful++;
			} else if (lraSteps <= 0 && whcaSteps > 0) {
				whcaSuccess++;
			} else if (lraSteps > 0 && whcaSteps <= 0) {
				lraSuccess++;
			} else {
				bothFailed++;
			}
			logger.info(String.format("both: %d/%d, WHCA: %d/%d, LRA: %d/%d, none: %d/%d", bothSuccessful, s, whcaSuccess, s, lraSuccess, s, bothFailed, s));
		}
		logger.info("bothSuccessful: " + bothSuccessful + " / " + SIMS_COUNT);
		logger.info("whcaSuccess: " + whcaSuccess + " / " + SIMS_COUNT);
		logger.info("lraSuccess: " + lraSuccess + " / " + SIMS_COUNT);
		logger.info("bothFailed: " + bothFailed + " / " + SIMS_COUNT);
	}
	
	private int randomInt(int fromInclusive, int toInclusive) {
		if (toInclusive < fromInclusive)
			return fromInclusive;
		return fromInclusive + random.nextInt(toInclusive - fromInclusive + 1);
	}
	
	private LRAController createLRARandomSimulation(int mapW, int mapH, int robotsCount) {
		LRASimulationParams params = new LRASimulationParams();
		params.mapSizeW = mapW;
		params.mapSizeH = mapH;
		params.robotsCount = robotsCount;
		LRAController controller = new LRAController(null, params);
		controller.setRandom(random);
		controller.setMazegen(new MazeGenerator(random));
		return controller;
	}
	
	private WHCAController createWHCARandomSimulation(int mapW, int mapH, int robotsCount) {
		WHCASimulationParams params = new WHCASimulationParams();
		params.mapSizeW = mapW;
		params.mapSizeH = mapH;
		params.robotsCount = robotsCount;
		WHCAController controller = new WHCAController(null, params);
		controller.setRandom(random);
		controller.setMazegen(new MazeGenerator(random));
		return controller;
	}
	
	private int simulateLRA(LRAController controller, int stepsMax) {
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
	
	private int simulateWHCA(WHCAController controller, int stepsMax) {
		for (int step = 0; step < stepsMax; step++) {
			//			logger.debug("simulation step " + step);
			controller.stepSimulation();
			boolean allReached = true;
			for (igrek.robopath.simulation.whca.MobileRobot robot : controller.getRobots()) {
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
