package igrek.robopath.mazegenerator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Random;

@Configuration
public class RandomFactory {
	
	Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@Value("${randomseed}")
	public String randomSeed;
	
	@Bean
	public Random provideRandom() {
		long seed = System.currentTimeMillis();
		if (randomSeed != null) {
			try {
				long num = Long.parseLong(randomSeed);
				if (num > 0) {
					seed = num;
					logger.info("custom random seed set");
				}
			} catch (NumberFormatException ignored) {
			}
		}
		Random random = new Random();
		random.setSeed(seed);
		logger.info("random seed: " + seed);
		return random;
	}
}
