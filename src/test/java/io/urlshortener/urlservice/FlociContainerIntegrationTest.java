package io.urlshortener.urlservice;

import org.junit.jupiter.api.Test;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

import java.net.URI;

import io.floci.testcontainers.FlociContainer;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
public class FlociContainerIntegrationTest {

	@Container
	static FlociContainer floci = new FlociContainer();

	@Test
	void isolatedFlociInstanceIsReachable() {
		DynamoDbClient dynamoDb = DynamoDbClient.builder()
				.endpointOverride(URI.create(floci.getEndpoint()))
				.region(Region.of(floci.getRegion()))
				.credentialsProvider(StaticCredentialsProvider.create(
						AwsBasicCredentials.create(floci.getAccessKey(), floci.getSecretKey())))
				.build();

		assertThat(dynamoDb.listTables().tableNames()).isEmpty();
	}

}
