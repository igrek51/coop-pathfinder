package igrek.robopath.modules.whca1;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import igrek.robopath.common.tilemap.TileMap;
import igrek.robopath.pathfinder.coop.Coordinater;
import igrek.robopath.pathfinder.coop.Grid;
import igrek.robopath.pathfinder.coop.Unit;

public class CoordinaterFactory {
	
	Coordinater provideCoordinater(int depth, int mapW, int mapH, Map<MobileRobot, Unit> unitsMap, List<MobileRobot> robots, TileMap map) {
		// new coordinater
		Coordinater coordinater = new Coordinater(depth, mapW, mapH);
		Grid grid = coordinater.grid;
		// place robots
		unitsMap.clear();
		for (MobileRobot robot : robots) {
			Unit unit = new Unit();
			unitsMap.put(robot, unit);
			unit.setLocation(robot.getPosition().getX(), robot.getPosition().getY());
			//			unitPositions.put(unit.id, new PathPanel.Point(unit.getLocation()));
			unit.setDestination(robot.getTarget().getX(), robot.getTarget().getY());
			//			unitTargets.put(unit.id, unit.getLocation());
			unit.setPath(new ArrayList<>());
			coordinater.addUnit(unit);
		}
		// place obstacles
		for (int x = 0; x < map.getWidthInTiles(); x++) {
			for (int y = 0; y < map.getHeightInTiles(); y++) {
				Boolean occupied = map.getCell(x, y);
				if (occupied != null && occupied) {
					grid.makeUnwalkable(x, y);
				}
			}
		}
		grid.update();
		
		//			coordinater.reset();
		return coordinater;
	}
	
}
