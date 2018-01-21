package igrek.robopath.modules.whca;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class Factory {

	@Bean
	public SimulationParams provideSimulationParams() {
		return new SimulationParams();
	}
	
	@Bean
	public Controller provideController(Presenter presenter, SimulationParams params) {
		return new Controller(presenter, params);
	}
	
}
