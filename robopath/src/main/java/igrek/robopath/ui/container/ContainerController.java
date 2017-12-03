package igrek.robopath.ui.container;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.felixroske.jfxsupport.FXMLController;
import javafx.fxml.FXML;
import javafx.scene.control.TabPane;

@FXMLController
public class ContainerController {
	
	private Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@FXML
	private TabPane containerTabPane;
	
	public ContainerController() {
	
	}
	
	@FXML
	public void initialize() {
		containerTabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
	}
	
}
