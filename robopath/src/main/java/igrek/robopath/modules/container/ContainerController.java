package igrek.robopath.modules.container;

import de.felixroske.jfxsupport.FXMLController;
import javafx.fxml.FXML;
import javafx.scene.control.SingleSelectionModel;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;

@FXMLController
public class ContainerController {
	
	private static final int DEFAULT_TAB_INDEX = 2; // beginning from 0
	
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
