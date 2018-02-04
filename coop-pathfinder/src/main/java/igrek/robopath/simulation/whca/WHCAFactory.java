package igrek.robopath.simulation.whca;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class WHCAFactory {
	
	@Bean(name = "whca2Params")
	public WHCASimulationParams provideSimulationParams() {
		return new WHCASimulationParams();
	}
	
	@Bean(name = "whca2Controller")
	public WHCAController provideController(@Qualifier("whca2Presenter") WHCAPresenter presenter, @Qualifier("whca2Params") WHCASimulationParams params) {
		return new WHCAController(presenter, params);
	}
	
}
