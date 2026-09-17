package io.urlshortener.urlservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

/**
 * Entry point for the url-service Spring Boot application.
 */
@SpringBootApplication
@ConfigurationPropertiesScan
public class UrlServiceApplication {

	/**
	 * Boots the Spring application context.
	 *
	 * @param args command-line arguments, passed through to Spring Boot.
	 */
	public static void main(String[] args) {
		SpringApplication.run(UrlServiceApplication.class, args);
	}

}
