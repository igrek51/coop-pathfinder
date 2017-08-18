package igrek.robopath;

import org.springframework.boot.autoconfigure.SpringBootApplication;

import de.felixroske.jfxsupport.AbstractJavaFxApplicationSupport;
import igrek.robopath.ui.view.HelloworldView;

@SpringBootApplication
public class Main extends AbstractJavaFxApplicationSupport{

    public static void main(String[] args) {
        launchApp(Main.class, HelloworldView.class, args);
    }
}
