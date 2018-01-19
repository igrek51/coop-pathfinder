package igrek.robopath.modules.whca;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;

public class SimulationParams {
	
	public int mapSizeW = 11;
	public int mapSizeH = 11;
	
	public int robotsCount = 1;
	
	public boolean robotAutoTarget = false;
	
	private TextField paramMapSizeW;
	private TextField paramMapSizeH;
	private TextField paramRobotsCount;
	private CheckBox paramRobotAutoTarget;
	
	public void init(ModulePresenter presenter) {
		this.paramMapSizeW = presenter.paramMapSizeW;
		this.paramMapSizeH = presenter.paramMapSizeH;
		this.paramRobotsCount = presenter.paramRobotsCount;
		this.paramRobotAutoTarget = presenter.paramRobotAutoTarget;
	}
	
	public void sendToUI() {
		paramMapSizeW.setText(Integer.toString(mapSizeW));
		paramMapSizeH.setText(Integer.toString(mapSizeH));
		paramRobotsCount.setText(Integer.toString(robotsCount));
		paramRobotAutoTarget.setSelected(robotAutoTarget);
	}
	
	public void readFromUI() {
		try {
			mapSizeW = Integer.parseInt(paramMapSizeW.getText());
			mapSizeH = Integer.parseInt(paramMapSizeH.getText());
			robotsCount = Integer.parseInt(paramRobotsCount.getText());
			robotAutoTarget = paramRobotAutoTarget.isSelected();
		} catch (NumberFormatException e) {
			Logger logger = LoggerFactory.getLogger(this.getClass());
			logger.error(e.getMessage());
		}
	}
}
