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
		
		int mapW = 15;
		int mapH = 15;
		int robotsCount = 5;
		
		for(robotsCount = 1; robotsCount <= 30; robotsCount++) {
			// mapH = mapW;
			int stepsMax = (mapW + mapH) * 3;
			// logger.info("Simulation: map " + mapW + "x" + mapH + ", " + robotsCount + " robots, maxSteps=" + stepsMax);
			int[] successful = new int [4];
			int[] stepsCount = new int [4];
			long[] calcTimes = new long [4];
			for (int s = 1; s <= SIMS_COUNT; s++) {
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
				// WHCA variants
				WHCAController whcaControllerWS = createWHCARandomSimulation(mapW, mapH, robotsCount);
				whcaControllerWS.setPrioritiesPromotion(true);
				whcaControllerWS.setTimeWindowScaling(false);
				WHCAController whcaControllerWP = createWHCARandomSimulation(mapW, mapH, robotsCount);
				whcaControllerWP.setPrioritiesPromotion(false);
				whcaControllerWP.setTimeWindowScaling(false);
				//			prepare LRA
				LRAController lraController = createLRARandomSimulation(mapW, mapH, robotsCount);
				// same maze as in whca
				TileMap whcaMap = whcaController.getMap();
				TileMap lraMap = lraController.getMap();
				TileMap whcaMapWS = whcaControllerWS.getMap();
				TileMap whcaMapWP = whcaControllerWP.getMap();
				for (int x = 0; x < whcaMap.getWidthInTiles(); x++) {
					for (int y = 0; y < whcaMap.getHeightInTiles(); y++) {
						lraMap.setCell(x, y, whcaMap.getCell(x, y));
						whcaMapWS.setCell(x, y, whcaMap.getCell(x, y));
						whcaMapWP.setCell(x, y, whcaMap.getCell(x, y));
					}
				}
				// robots locations same as in whca
				for (int i = 0; i < robotsCount; i++) {
					igrek.robopath.simulation.whca.MobileRobot whcaRobot = whcaController.getRobots()
							.get(i);
					// set start point
					lraController.createMobileRobot(whcaRobot.getPosition(), i);
					whcaControllerWS.createMobileRobot(whcaRobot.getPosition());
					whcaControllerWP.createMobileRobot(whcaRobot.getPosition());
					//set target
					lraController.getRobots().get(i).setTarget(whcaRobot.getTarget());
					whcaControllerWS.getRobots().get(i).setTarget(whcaRobot.getTarget());
					whcaControllerWP.getRobots().get(i).setTarget(whcaRobot.getTarget());
				}
				//			simulate
				int steps;
				long startTime;
				// LRA* - 3
				startTime = System.currentTimeMillis();
				steps = simulateLRA(lraController, stepsMax);
				if (steps > 0) {
					stepsCount[3] += steps;
					calcTimes[3] += System.currentTimeMillis() - startTime;
					successful[3] += 1;
				}
				// WHCA*1 - 2
				whcaControllerWP.getParams().timeDimension = whcaControllerWP.getRobots().size() + 1;
				startTime = System.currentTimeMillis();
				steps = simulateWHCA(whcaControllerWP, stepsMax);
				if (steps > 0) {
					stepsCount[2] += steps;
					calcTimes[2] += System.currentTimeMillis() - startTime;
					successful[2] += 1;
				}
				// WHCA*2 - 1
				whcaControllerWS.getParams().timeDimension = whcaControllerWS.getRobots().size() + 1;
				startTime = System.currentTimeMillis();
				steps = simulateWHCA(whcaControllerWS, stepsMax);
				if (steps > 0) {
					stepsCount[1] += steps;
					calcTimes[1] += System.currentTimeMillis() - startTime;
					successful[1] += 1;
				}
				// WHCA*3 - 0
				startTime = System.currentTimeMillis();
				steps = simulateWHCA(whcaController, stepsMax);
				if (steps > 0) {
					stepsCount[0] += steps;
					calcTimes[0] += System.currentTimeMillis() - startTime;
					successful[0] += 1;
				}
				//			Summary
				// if (whcaSteps <= 0 && (whcaStepsWS > 0 || whcaStepsWP > 0 || lraSteps > 0)) {
				// 	logger.warn(String.format("whcaSteps: %d, whcaStepsWS: %d, whcaStepsWP: %d, lraSteps: %d", whcaSteps, whcaStepsWS, whcaStepsWP, lraSteps));
				// } else if (whcaStepsWS <= 0 && (whcaStepsWP > 0 || lraSteps > 0)) {
				// 	logger.warn(String.format("whcaSteps: %d, whcaStepsWS: %d, whcaStepsWP: %d, lraSteps: %d", whcaSteps, whcaStepsWS, whcaStepsWP, lraSteps));
				// } else if (whcaStepsWP <= 0 && lraSteps > 0) {
				// 	logger.warn(String.format("whcaSteps: %d, whcaStepsWS: %d, whcaStepsWP: %d, lraSteps: %d", whcaSteps, whcaStepsWS, whcaStepsWP, lraSteps));
				// }
				// for (int i = 0; i < 4; i++) {
				// 	if (stepsCount[i] > 0)
				// 		successful[i] += 1;
				// }
				// logger.info(String.format("both: %d/%d, WHCA: %d/%d, LRA: %d/%d, none: %d/%d", bothSuccessful, s, whcaSuccess, s, lraSuccess, s, bothFailed, s));
				// logger.info(String.format("whca: %d/%d, whcaWS: %d/%d, whcaWP: %d/%d, lra: %d/%d", successful[0], s, successful[1], s, successful[2], s, successful[3], s));
			}
			double[] avgFactor = new double [4];
			for (int i = 0; i < 4; i++) {
				if (successful[i] == 0){
					logger.warn("successful["+i+"] = 0");
					successful[i] = -1;
				}
				avgFactor[i] = 1.0 / successful[i];
			}
			String info = String.format("map %dx%d, robots %d", mapW, mapH, robotsCount);
			info += String.format(", successfulls: \t%d\t%d\t%d\t%d", successful[3], successful[2], successful[1], successful[0]);
			info += String.format(", steps: \t%f\t%f\t%f\t%f", avgFactor[3] * stepsCount[3], avgFactor[2] * stepsCount[2], avgFactor[1] * stepsCount[1], avgFactor[0] * stepsCount[0]);
			info += String.format(", time: \t%f\t%f\t%f\t%f", avgFactor[3] * calcTimes[3], avgFactor[2] * calcTimes[2], avgFactor[1] * calcTimes[1], avgFactor[0] * calcTimes[0]);
			logger.info(info);
		}
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
