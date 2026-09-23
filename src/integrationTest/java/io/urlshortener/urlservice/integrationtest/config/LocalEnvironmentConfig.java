package io.urlshortener.urlservice.integrationtest.config;

import io.floci.testcontainers.FlociContainer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeDefinition;
import software.amazon.awssdk.services.dynamodb.model.BillingMode;
import software.amazon.awssdk.services.dynamodb.model.CreateTableRequest;
import software.amazon.awssdk.services.dynamodb.model.KeySchemaElement;
import software.amazon.awssdk.services.dynamodb.model.KeyType;
import software.amazon.awssdk.services.dynamodb.model.ScalarAttributeType;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.URI;
import java.time.Duration;
import java.time.Instant;

/**
 * Wires the shared {@link io.urlshortener.urlservice.integrationtest.LinksLifecycleIntegrationTest
 * LinksLifecycleIntegrationTest} to a real url-service instance, launched as a genuine separate OS process
 * (the built boot jar, not an in-process {@link org.springframework.boot.test.context.SpringBootTest @SpringBootTest}
 * context) against a Testcontainers-managed floci instance.<br/>
 *
 * <p>
 *     Active only under the {@code local} profile<br/>
 *     {@link DeployedEnvironmentConfig} supplies the equivalent bean for the {@code deployed} profile.
 * </p>
 *
 * <p>The table name, partition key, and health-check timing below are read from {@code application.yaml}
 * via {@link LocalTestSettings} rather than hardcoded - see its Javadoc for why that reads the file
 * standalone instead of through {@code @Value}.
 */
@Configuration
@Profile("local")
public class LocalEnvironmentConfig {

	private static final FlociContainer floci = new FlociContainer();

	static {
		floci.start();
		createLinksTable();
	}

	private static void createLinksTable() {
		try (DynamoDbClient client = buildDynamoDbClient()) {
			client.createTable(CreateTableRequest.builder()
					.tableName(LocalTestSettings.tableName())
					.keySchema(KeySchemaElement.builder()
							.attributeName(LocalTestSettings.partitionKey())
							.keyType(KeyType.HASH)
							.build())
					.attributeDefinitions(AttributeDefinition.builder()
							.attributeName(LocalTestSettings.partitionKey())
							.attributeType(ScalarAttributeType.S)
							.build())
					.billingMode(BillingMode.PAY_PER_REQUEST)
					.build());
		}
	}

	private static DynamoDbClient buildDynamoDbClient() {
		return DynamoDbClient.builder()
				.endpointOverride(URI.create(floci.getEndpoint()))
				.region(Region.of(floci.getRegion()))
				.credentialsProvider(StaticCredentialsProvider.create(
						AwsBasicCredentials.create(floci.getAccessKey(), floci.getSecretKey())))
				.build();
	}

	@Bean(destroyMethod = "shutdown")
	public LocalUrlServiceInstance localUrlServiceInstance(
			@Value("${integration-test.app-jar}") final String appJarPath
	) throws IOException {
		final int port = findFreePort();
		final String javaBinary = System.getProperty("java.home") + "/bin/java";
		final ProcessBuilder processBuilder = new ProcessBuilder(
				javaBinary,
				"-Dserver.port=" + port,
				"-Daws.dynamodb.endpoint-override=" + floci.getEndpoint(),
				"-Daws.region=" + floci.getRegion(),
				"-Daws.credential.access-key=" + floci.getAccessKey(),
				"-Daws.credential.secret-key=" + floci.getSecretKey(),
				"-Daws.dynamodb.tables.links-table=" + LocalTestSettings.tableName(),
				"-jar", appJarPath
		);
		processBuilder.redirectErrorStream(true);
		processBuilder.redirectOutput(ProcessBuilder.Redirect.INHERIT);
		final Process process = processBuilder.start();
		waitUntilHealthy(port, process);
		return new LocalUrlServiceInstance(process, port);
	}

	@Bean
	public RestClient linksApiClient(final LocalUrlServiceInstance instance) {
		return RestClient.builder()
				.baseUrl("http://localhost:" + instance.port())
				.build();
	}

	private static int findFreePort() throws IOException {
		try (ServerSocket socket = new ServerSocket(0)) {
			return socket.getLocalPort();
		}
	}

	private static void waitUntilHealthy(final int port, final Process process) {
		final RestClient healthClient = RestClient.create();
		final Duration timeout = LocalTestSettings.healthCheckTimeout();
		final Instant deadline = Instant.now().plus(timeout);
		while (Instant.now().isBefore(deadline)) {
			if (!process.isAlive()) {
				throw new IllegalStateException("url-service process exited before becoming healthy, exit code "
						+ process.exitValue());
			}
			try {
				healthClient.get()
						.uri("http://localhost:" + port + "/actuator/health")
						.retrieve()
						.toBodilessEntity();
				return;
			} catch (RestClientException e) {
				sleep();
			}
		}
		throw new IllegalStateException("url-service did not become healthy within " + timeout);
	}

	private static void sleep() {
		try {
			Thread.sleep(LocalTestSettings.healthCheckPollInterval().toMillis());
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new IllegalStateException(e);
		}
	}

}
