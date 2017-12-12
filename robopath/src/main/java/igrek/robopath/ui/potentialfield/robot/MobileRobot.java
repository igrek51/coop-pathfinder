package igrek.robopath.ui.potentialfield.robot;

public class MobileRobot {
	
	public static final double MAX_SPEED = 2;
	
	private Vector2 position = Vector2.ZERO;
	private Vector2 velocity = Vector2.ZERO;
	private Vector2 force = Vector2.ZERO;
	/**
	 * 0 - right direction
	 * 90 - up direction
	 */
	private double orientation = 0;
	private TargetReachedHandler targetReachedHandler;
	
	private Vector2 target = null;
	
	public MobileRobot(Vector2 position, TargetReachedHandler targetReachedHandler) {
		this.position = position;
		this.targetReachedHandler = targetReachedHandler;
	}
	
	public MobileRobot zeroForce() {
		force = Vector2.ZERO;
		return this;
	}
	
	public MobileRobot addForce(Vector2 f2) {
		force = force.add(f2);
		return this;
	}
	
	public void timeLapse(double t) {
		Vector2 deltaV = force.scale(t);
		// if max speed exceeded, cut off
		velocity = velocity.add(deltaV).cutOff(MAX_SPEED);
		Vector2 deltaX = velocity.scale(t);
		position = position.add(deltaX);
	}
	
	public Vector2 getPosition() {
		return position;
	}
	
	public Vector2 getVelocity() {
		return velocity;
	}
	
	public Vector2 getForce() {
		return force;
	}
	
	public void setPosition(Vector2 position) {
		this.position = position;
	}
	
	public double getOrientation() {
		return orientation;
	}
	
	public void setOrientation(double orientation) {
		this.orientation = orientation;
	}
	
	public Vector2 getTarget() {
		return target;
	}
	
	public void setTarget(Vector2 target) {
		this.target = target;
	}
}
