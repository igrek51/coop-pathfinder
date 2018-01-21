package igrek.robopath.modules.whca;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(name = "simulation.mode", havingValue = "whca")
public class WHCAFactory {

	@Bean
	public SimulationParams provideSimulationParams() {
		return new SimulationParams();
	}
	
	@Bean
	public Controller provideController(Presenter presenter, SimulationParams params) {
		return new Controller(presenter, params);
	}
	
}
