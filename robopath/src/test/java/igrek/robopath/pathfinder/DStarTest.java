package igrek.robopath.pathfinder;

import org.junit.Test;

import java.util.List;

import igrek.robopath.pathfinder.dstar.DStarLite;
import igrek.robopath.pathfinder.dstar.State;

public class DStarTest {
	
	@Test
	public void testSimplePath() {
		
		//Create pathfinder
		DStarLite pf = new DStarLite();
		//set start and goal nodes
		pf.init(0, 1, 3, 1);
		//set impassable nodes
		pf.updateCell(2, 1, -1);
		pf.updateCell(2, 0, -1);
		pf.updateCell(2, 2, -1);
		pf.updateCell(3, 0, -1);
		
		//perform the pathfinding
		pf.replan();
		
		//get and print the path
		List<State> path = pf.getPath();
		for (State i : path) {
			System.out.println("" + i.x + ", " + i.y);
		}
	}
	
	
}
