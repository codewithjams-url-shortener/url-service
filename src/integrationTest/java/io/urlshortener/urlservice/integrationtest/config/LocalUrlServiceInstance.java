package io.urlshortener.urlservice.integrationtest.config;

import java.util.concurrent.TimeUnit;

/**
 * A url-service instance launched as a genuine separate OS process by {@link LocalEnvironmentConfig},
 * together with the port it is listening on.
 */
public record LocalUrlServiceInstance(Process process, int port) {

	/**
	 * Stops the process, escalating to a forceful kill if it does not exit promptly.
	 */
	public void shutdown() {
		process.destroy();
		try {
			if (!process.waitFor(10, TimeUnit.SECONDS)) {
				process.destroyForcibly();
			}
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			process.destroyForcibly();
		}
	}

}
