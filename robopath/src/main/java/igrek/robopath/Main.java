package igrek.robopath;

import org.springframework.boot.autoconfigure.SpringBootApplication;

import de.felixroske.jfxsupport.AbstractJavaFxApplicationSupport;
import igrek.robopath.ui.simpleastar.SimpleAstarView;

@SpringBootApplication
public class Main extends AbstractJavaFxApplicationSupport{

    public static void main(String[] args) {
        launchApp(Main.class, SimpleAstarView.class, args);
    }
}
