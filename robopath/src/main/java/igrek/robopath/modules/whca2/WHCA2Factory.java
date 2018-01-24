package igrek.robopath.modules.whca2;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(name = "simulation.mode", havingValue = "whca2")
public class WHCA2Factory {
	
	@Bean
	public SimulationParams provideSimulationParams() {
		return new SimulationParams();
	}
	
	@Bean
	public Controller provideController(Presenter presenter, SimulationParams params) {
		return new Controller(presenter, params);
	}
	
}
