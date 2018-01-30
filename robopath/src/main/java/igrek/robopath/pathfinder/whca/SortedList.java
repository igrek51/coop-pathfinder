package igrek.robopath.pathfinder.whca;

import java.util.ArrayList;
import java.util.Collections;

/**
 * A simple sorted list
 */
class SortedList<T extends Comparable<T>> extends ArrayList<T> {
	
	/**
	 * Retrieve the first element from the list
	 * @return The first element from the list
	 */
	public T first() {
		return get(0);
	}
	
	/**
	 * Add an element to the list - causes sorting
	 * @param node The element to add
	 */
	@Override
	public boolean add(T node) {
		boolean result = super.add(node);
		Collections.sort(this);
		return result;
	}
}