package igrek.robopath;

import org.springframework.boot.autoconfigure.SpringBootApplication;

import de.felixroske.jfxsupport.AbstractJavaFxApplicationSupport;
import igrek.robopath.ui.robomove.RobomoveView;

@SpringBootApplication
public class Main extends AbstractJavaFxApplicationSupport{

    public static void main(String[] args) {
		launchApp(Main.class, RobomoveView.class, args);
	}
}
