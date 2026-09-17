package io.urlshortener.urlservice.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

/**
 * Provides a single shared, thread-safe {@link SecureRandom} bean for the application, preferring the
 * non-blocking native algorithm and falling back to the platform default when unavailable.
 */
@Slf4j
@Configuration
public class SecureRandomConfig {

	/**
	 * Builds the shared {@link SecureRandom} instance for the application.
	 *
	 * @return a {@link SecureRandom} instance using {@code NativePRNGNonBlocking} when available,
	 *         or the JDK's platform default algorithm otherwise.
	 */
	@Bean
	public SecureRandom secureRandom() {
		try {
			final SecureRandom secureRandom = SecureRandom.getInstance("NativePRNGNonBlocking");
			log.atDebug()
					.addKeyValue("algorithm", secureRandom.getAlgorithm())
					.log();
			log.atInfo()
					.addKeyValue("mode", "NORMAL")
					.log("SecureRandom bean created");
			return secureRandom;
		} catch (NoSuchAlgorithmException e) {
			final SecureRandom secureRandom = new SecureRandom();
			log.atDebug()
					.addKeyValue("algorithm", secureRandom.getAlgorithm())
					.log();
			log.atWarn()
					.addKeyValue("mode", "FALLBACK")
					.setCause(e)
					.log("NativePRNGNonBlocking unavailable, falling back to platform default");
			return secureRandom;
		}
	}

}
