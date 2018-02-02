package igrek.robopath.simulation.lra;

import java.util.LinkedList;

import igrek.robopath.common.Point;

class MobileRobot {
	
	private Point position;
	private Point target;
	private LinkedList<Point> movesQue = new LinkedList<>();
	private TargetReachedHandler targetReachedHandler;
	private int priority;
	
	public interface TargetReachedHandler {
		void onTargetReached(MobileRobot robot);
	}
	
	public MobileRobot(Point position, TargetReachedHandler targetReachedHandler, int priority) {
		this.position = position;
		this.targetReachedHandler = targetReachedHandler;
		this.priority = priority;
	}
	
	
	Point getPosition() {
		return position;
	}
	
	public void setPosition(Point position) {
		this.position = position;
	}
	
	Point getTarget() {
		return target;
	}
	
	void setTarget(Point target) {
		this.target = target;
	}
	
	LinkedList<Point> getMovesQue() {
		return movesQue;
	}
	
	int getPriority() {
		return priority;
	}
	
	Point getNextMove() {
		if (movesQue.isEmpty())
			return null;
		return movesQue.getFirst();
	}
	
	boolean hasNextMove() {
		return !movesQue.isEmpty();
	}
	
	Point pollNextMove() {
		if (movesQue.isEmpty())
			return null;
		return movesQue.pollFirst();
	}
	
	Point lastTarget() {
		if (movesQue.isEmpty())
			return position;
		return movesQue.getLast();
	}
	
	Point nearestTarget() {
		if (movesQue.isEmpty())
			return position;
		return movesQue.getFirst();
	}
	
	MobileRobot enqueueMove(Point target) {
		if (!lastTarget().isAdjacentOrEqual(target))
			throw new IllegalArgumentException("appended move is not adjacent or equal to last target");
		movesQue.add(target);
		return this;
	}
	
	MobileRobot enqueueMove(int x, int y) {
		return enqueueMove(new Point(x, y));
	}
	
	void resetMovesQue() {
		movesQue.clear();
	}
	
	void resetNextMoves() {
		Point currentMove = movesQue.peekFirst();
		movesQue.clear();
		if (currentMove != null) {
			movesQue.add(currentMove);
		}
	}
	
	double getInterpolatedX(double moveProgress) {
		moveProgress = cutOff(moveProgress, 0, 1);
		Point nearest = nearestTarget();
		return position.x + (nearest.x - position.x) * moveProgress;
	}
	
	double getInterpolatedY(double moveProgress) {
		moveProgress = cutOff(moveProgress, 0, 1);
		Point nearest = nearestTarget();
		return position.y + (nearest.y - position.y) * moveProgress;
	}
	
	private double cutOff(double num, double min, double max) {
		if (num < min)
			num = min;
		if (num > max)
			num = max;
		return num;
	}
	
	boolean hasReachedTarget() {
		return target == null || target.equals(position);
	}
	
	void targetReached() {
		if (targetReachedHandler != null) {
			targetReachedHandler.onTargetReached(this);
		}
	}
	
}
