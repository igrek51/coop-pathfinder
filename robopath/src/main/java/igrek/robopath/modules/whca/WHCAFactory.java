package igrek.robopath.modules.whca;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class WHCAFactory {
	
	@Bean(name = "whca2Params")
	public SimulationParams provideSimulationParams() {
		return new SimulationParams();
	}
	
	@Bean(name = "whca2Controller")
	public Controller provideController(@Qualifier("whca2Presenter") Presenter presenter, @Qualifier("whca2Params") SimulationParams params) {
		return new Controller(presenter, params);
	}
	
}
