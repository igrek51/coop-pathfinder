package igrek.robopath.ui.whca;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class WHCAFactory {
	
	@Bean
	public WHCAController provideController(WHCAPresenter presenter, SimulationParams params) {
		return new WHCAController(presenter, params);
	}
	
}
