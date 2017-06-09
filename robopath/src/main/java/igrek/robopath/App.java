package igrek.robopath;

import javafx.application.Preloader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Lazy;

import igrek.robopath.components.MainLayout;

@Lazy
@SpringBootApplication
public class App extends AbstractJavaFxApplicationSupport {

	/**
	 * Note that this is configured in application.properties
	 */
	@Value("${app.ui.title:Example App}")//
	private String windowTitle;

	@Autowired
	private MainLayout mainLayout;

	@Override
	public void start(Stage stage) throws Exception {

		notifyPreloader(new Preloader.StateChangeNotification(Preloader.StateChangeNotification.Type.BEFORE_START));

		stage.setTitle(windowTitle);
		stage.setScene(new Scene(mainLayout));
		stage.setResizable(true);
		stage.centerOnScreen();
		stage.show();
	}

	public static void main(String[] args) {
		launchApp(App.class, args);
	}

}
