package igrek.robopath.simulation.lra;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class LRAFactory {
	
	@Bean(name = "lraParams")
	public LRASimulationParams provideSimulationParams() {
		return new LRASimulationParams();
	}
	
	@Bean(name = "lraController")
	public LRAController provideController(@Qualifier("lraPresenter") LRAPresenter presenter, @Qualifier("lraParams") LRASimulationParams params) {
		return new LRAController(presenter, params);
	}
	
}
