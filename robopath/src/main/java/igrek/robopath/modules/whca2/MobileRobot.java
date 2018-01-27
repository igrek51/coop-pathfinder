package igrek.robopath.modules.whca2;

import java.util.LinkedList;

import igrek.robopath.common.Point;

public class MobileRobot {
	
	private Point position;
	private Point target;
	private LinkedList<Point> movesQue = new LinkedList<>();
	private TargetReachedHandler targetReachedHandler;
	private int id;
	private int priority;
	
	public interface TargetReachedHandler {
		void onTargetReached(MobileRobot robot);
	}
	
	public MobileRobot(Point position, TargetReachedHandler targetReachedHandler, int id, int priority) {
		this.position = position;
		this.targetReachedHandler = targetReachedHandler;
		this.id = id;
		this.priority = priority;
	}
	
	@Override
	protected MobileRobot clone() {
		MobileRobot clone = new MobileRobot(this.position, this.targetReachedHandler, this.id, this.priority);
		clone.target = this.target;
		clone.movesQue = this.movesQue;
		return clone;
	}
	
	public Point getPosition() {
		return position;
	}
	
	public synchronized void setPosition(Point position) {
		this.position = position;
	}
	
	public Point getTarget() {
		return target;
	}
	
	public synchronized void setTarget(Point target) {
		this.target = target;
	}
	
	LinkedList<Point> getMovesQue() {
		return movesQue;
	}
	
	public int getPriority() {
		return priority;
	}
	
	public void setPriority(int priority) {
		this.priority = priority;
	}
	
	public int getId() {
		return id;
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
	
	synchronized MobileRobot enqueueMove(Point target) {
		if (!lastTarget().isAdjacentOrEqual(target))
			throw new IllegalArgumentException("appended move is not adjacent or equal to last target");
		movesQue.add(target);
		return this;
	}
	
	public MobileRobot enqueueMove(int x, int y) {
		return enqueueMove(new Point(x, y));
	}
	
	public void resetMovesQue() {
		movesQue.clear();
	}
	
	void resetNextMoves() {
		Point currentMove = movesQue.peekFirst();
		movesQue.clear();
		if (currentMove != null) {
			movesQue.add(currentMove);
		}
	}
	
	synchronized double getInterpolatedX(double moveProgress) {
		moveProgress = cutOff(moveProgress, 0, 1);
		Point nearest = nearestTarget();
		return position.x + (nearest.x - position.x) * moveProgress;
	}
	
	synchronized double getInterpolatedY(double moveProgress) {
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
	
	@Override
	public String toString() {
		return getId() + "." + getPriority();
	}
}
