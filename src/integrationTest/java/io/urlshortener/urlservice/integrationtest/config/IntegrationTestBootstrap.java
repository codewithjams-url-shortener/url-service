package io.urlshortener.urlservice.integrationtest.config;

import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Purpose-built, minimal Spring Boot context for the integration tests in this source set. Deliberately
 * not {@code UrlServiceApplication} itself: these tests treat url-service as an HTTP black box (a
 * separately launched process for the {@code local} profile, an already-deployed instance for
 * {@code deployed}), so this context exists only to host the {@code @Profile}-gated {@link
 * org.springframework.web.client.RestClient} beans, never to run the application under test in-process.
 */
@SpringBootApplication
public class IntegrationTestBootstrap {
}
