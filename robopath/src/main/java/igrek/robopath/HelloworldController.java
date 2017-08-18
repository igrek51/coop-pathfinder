package igrek.robopath;

import de.felixroske.jfxsupport.FXMLController;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

@FXMLController
public class HelloworldController {
	
	@FXML
	private Label helloLabel;
	
	@FXML
	private TextField nameField;
	
	@FXML
	private void setHelloText(final Event event) {
		final String textToBeShown = processName(nameField.getText());
		helloLabel.setText(textToBeShown);
	}
	
	private String processName(final String name) {
		if (name.equals("dupa")) {
			return "Hello Dupa!";
		} else {
			return "Hello Unknown " + name + "!";
		}
	}
}
