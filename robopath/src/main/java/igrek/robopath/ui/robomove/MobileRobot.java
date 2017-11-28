package igrek.robopath.ui.robomove;

import java.util.LinkedList;

import igrek.robopath.model.Point;

public class MobileRobot {
	
	public static final double SPEED = 2;
	
	private Point position;
	private double moveProgress = 0;
	/**
	 * 0 - right direction
	 * 90 - up direction
	 */
	private double orientation = 0;
	
	private Point target;
	private LinkedList<Point> movesQue = new LinkedList<>();
	
	public MobileRobot(Point position) {
		this.position = position;
	}
	
	public void timeLapse(double t) {
		double pointsLength = t * SPEED;
		move1Step(pointsLength);
	}
	
	private void move1Step(double pointsLength) {
		if (movesQue.isEmpty())
			return;
		Point nearest = nearestTarget();
		double distance = position.distance(nearest);
		double remainder = distance * (1.0 - moveProgress);
		if (pointsLength >= remainder) {
			// whole move - end current, begin next
			position = movesQue.pollFirst();
			moveProgress = 0;
			move1Step(pointsLength - remainder);
		} else {
			// make part of move
			remainder -= pointsLength;
			moveProgress = 1.0 - remainder / distance;
		}
	}
	
	public MobileRobot enqueueMove(Point target) {
		if (!lastTarget().isAdjacent(target))
			throw new IllegalArgumentException("appended move is not adjacent to last target");
		movesQue.add(target);
		return this;
	}
	
	public MobileRobot enqueueMove(int x, int y) {
		return enqueueMove(new Point(x, y));
	}
	
	public LinkedList<Point> getMovesQue() {
		return movesQue;
	}
	
	public void resetMovesQue() {
		movesQue.clear();
	}
	
	public void resetNextMoves() {
		Point currentMove = movesQue.peekFirst();
		movesQue.clear();
		if (currentMove != null) {
			movesQue.add(currentMove);
		}
	}
	
	public Point nearestTarget() {
		if (movesQue.isEmpty())
			return position;
		return movesQue.getFirst();
	}
	
	public Point lastTarget() {
		if (movesQue.isEmpty())
			return position;
		return movesQue.getLast();
	}
	
	public Point getPosition() {
		return position;
	}
	
	public void setPosition(Point position) {
		this.position = position;
		// reset movement
		resetMovesQue();
		moveProgress = 0;
	}
	
	public double getMoveProgress() {
		return moveProgress;
	}
	
	public double getOrientation() {
		return orientation;
	}
	
	public void setOrientation(double orientation) {
		this.orientation = orientation;
	}
	
	public double getInterpolatedX() {
		Point nearest = nearestTarget();
		return position.x + (nearest.x - position.x) * moveProgress;
	}
	
	public double getInterpolatedY() {
		Point nearest = nearestTarget();
		return position.y + (nearest.y - position.y) * moveProgress;
	}
	
	public Point getTarget() {
		return target;
	}
	
	public void setTarget(Point target) {
		this.target = target;
	}
}
