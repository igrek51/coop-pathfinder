package igrek.robopath.ui.container;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.felixroske.jfxsupport.FXMLController;
import javafx.fxml.FXML;
import javafx.scene.control.SingleSelectionModel;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;

@FXMLController
public class ContainerController {
	
	private Logger logger = LoggerFactory.getLogger(this.getClass());
	
	private static final int DEFAULT_TAB_INDEX = 4; // beginning from 0
	
	@FXML
	private TabPane containerTabPane;
	
	public ContainerController() {
	
	}
	
	@FXML
	public void initialize() {
		containerTabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
		// select default tab
		SingleSelectionModel<Tab> selectionModel = containerTabPane.getSelectionModel();
		selectionModel.select(DEFAULT_TAB_INDEX);
	}
	
}
