package igrek.robopath.simulation.container;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.felixroske.jfxsupport.FXMLController;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.SingleSelectionModel;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;

@FXMLController
public class ContainerController {
	
	private static final int DEFAULT_TAB_INDEX = 2; // beginning from 0
	
	private Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@FXML
	private TabPane containerTabPane;
	
	public ContainerController() {
	}
	
	@FXML
	public void initialize() {
		Platform.runLater(() -> { // fixing fxml retarded initialization
			try {
				logger.info("initializing container");
				
				Thread.sleep(100); // FIXME still view isn't guaranteed to be initialized :(
				
				containerTabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
				// select default tab
				SingleSelectionModel<Tab> selectionModel = containerTabPane.getSelectionModel();
				selectionModel.select(DEFAULT_TAB_INDEX);
			} catch (Throwable t) {
				logger.error(t.getMessage());
			}
		});
	}
	
}
