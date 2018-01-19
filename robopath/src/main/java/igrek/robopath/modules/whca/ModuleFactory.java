package igrek.robopath.modules.whca;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ModuleFactory {

	@Bean
	public SimulationParams provideSimulationParams() {
		return new SimulationParams();
	}
	
	@Bean
	public ModuleController provideController(ModulePresenter presenter, SimulationParams params) {
		return new ModuleController(presenter, params);
	}
	
}
