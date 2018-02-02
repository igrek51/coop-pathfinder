package igrek.robopath.simulation.whca;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;


public class SimulationParams {
	
	public int mapSizeW = 11;
	public int mapSizeH = 11;
	
	public int robotsCount = 5;
	
	public boolean robotAutoTarget = false;
	
	public int timeDimension = 8;
	
	private TextField paramMapSizeW;
	private TextField paramMapSizeH;
	private TextField paramRobotsCount;
	private CheckBox paramRobotAutoTarget;
	private TextField paramTimeDimension;
	
	public void init(Presenter presenter) {
		this.paramMapSizeW = presenter.paramMapSizeW;
		this.paramMapSizeH = presenter.paramMapSizeH;
		this.paramRobotsCount = presenter.paramRobotsCount;
		this.paramRobotAutoTarget = presenter.paramRobotAutoTarget;
		this.paramTimeDimension = presenter.paramTimeDimension;
	}
	
	public synchronized void sendToUI() {
		paramMapSizeW.setText(Integer.toString(mapSizeW));
		paramMapSizeH.setText(Integer.toString(mapSizeH));
		paramRobotsCount.setText(Integer.toString(robotsCount));
		paramRobotAutoTarget.setSelected(robotAutoTarget);
		paramTimeDimension.setText(Integer.toString(timeDimension));
	}
	
	public synchronized void readFromUI() {
		try {
			mapSizeW = Integer.parseInt(paramMapSizeW.getText());
			mapSizeH = Integer.parseInt(paramMapSizeH.getText());
			robotsCount = Integer.parseInt(paramRobotsCount.getText());
			robotAutoTarget = paramRobotAutoTarget.isSelected();
			timeDimension = Integer.parseInt(paramTimeDimension.getText());
		} catch (NumberFormatException e) {
			Logger logger = LoggerFactory.getLogger(this.getClass());
			logger.error(e.getMessage());
		}
	}
}
