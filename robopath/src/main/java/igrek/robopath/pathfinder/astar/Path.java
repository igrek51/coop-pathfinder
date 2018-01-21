package igrek.robopath.pathfinder.astar;

import java.util.ArrayList;
import java.util.List;

public class Path {
	/** The list of steps building up this path */
	private List<Step> steps = new ArrayList<>();
	
	/**
	 * Create an empty path
	 */
	public Path() {
	
	}
	
	/**
	 * Get the length of the path, i.e. the number of steps
	 * @return The number of steps in this path
	 */
	public int getLength() {
		return steps.size();
	}
	
	/**
	 * Get the step at a given index in the path
	 * @param index The index of the step to retrieve. Note this should
	 *              be >= 0 and < getLength();
	 * @return The step information, the position on the map.
	 */
	public Step getStep(int index) {
		return steps.get(index);
	}
	
	/**
	 * Get the x coordinate for the step at the given index
	 * @param index The index of the step whose x coordinate should be retrieved
	 * @return The x coordinate at the step
	 */
	public int getX(int index) {
		return getStep(index).x;
	}
	
	/**
	 * Get the y coordinate for the step at the given index
	 * @param index The index of the step whose y coordinate should be retrieved
	 * @return The y coordinate at the step
	 */
	public int getY(int index) {
		return getStep(index).y;
	}
	
	/**
	 * Append a step to the path.
	 * @param x The x coordinate of the new step
	 * @param y The y coordinate of the new step
	 */
	public void appendStep(int x, int y) {
		steps.add(new Step(x, y));
	}
	
	/**
	 * Prepend a step to the path.
	 * @param x The x coordinate of the new step
	 * @param y The y coordinate of the new step
	 */
	public void prependStep(int x, int y) {
		steps.add(0, new Step(x, y));
	}
	
	/**
	 * Check if this path contains the given step
	 * @param x The x coordinate of the step to check for
	 * @param y The y coordinate of the step to check for
	 * @return True if the path contains the given step
	 */
	public boolean contains(int x, int y) {
		return steps.contains(new Step(x, y));
	}
	
	@Override
	public String toString() {
		String stepsStr = steps.stream()
				.map(Step::toString)
				.reduce((s1, s2) -> s1 + ", " + s2)
				.orElse("");
		return "[" + stepsStr + "]";
	}
	
	/**
	 * A single step within the path
	 * @author Kevin Glass
	 */
	public class Step {
		/** The x coordinate at the given step */
		private int x;
		/** The y coordinate at the given step */
		private int y;
		
		/**
		 * Create a new step
		 * @param x The x coordinate of the new step
		 * @param y The y coordinate of the new step
		 */
		public Step(int x, int y) {
			this.x = x;
			this.y = y;
		}
		
		/**
		 * Get the x coordinate of the new step
		 * @return The x coodindate of the new step
		 */
		public int getX() {
			return x;
		}
		
		/**
		 * Get the y coordinate of the new step
		 * @return The y coodindate of the new step
		 */
		public int getY() {
			return y;
		}
		
		/**
		 * @see Object#hashCode()
		 */
		public int hashCode() {
			return x * y;
		}
		
		/**
		 * @see Object#equals(Object)
		 */
		public boolean equals(Object other) {
			if (other instanceof Step) {
				Step o = (Step) other;
				
				return (o.x == x) && (o.y == y);
			}
			
			return false;
		}
		
		@Override
		public String toString() {
			return "(" + x + ", " + y + ")";
		}
	}
}

