package io.urlshortener.urlservice.metrics;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.core.metrics.CoreMetric;
import software.amazon.awssdk.metrics.MetricCollection;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class DynamoDbThrottleMetricPublisherTest {

	private static final String COUNTER_NAME = "dynamodb.throttle.count";

	/**
	 * The exact value DynamoDB's {@code ERROR_TYPE} metric takes for a throttled attempt, per the
	 * AWS SDK's own design docs (see {@link DynamoDbThrottleMetricPublisher}'s Javadoc).
	 */
	private static final String ERROR_TYPE_THROTTLING = "Throttling";

	private static final String ERROR_TYPE_VALIDATION = "ValidationException";

	private final MeterRegistry meterRegistry = new SimpleMeterRegistry();

	@Mock
	private MetricCollection metricCollection;

	@Mock
	private MetricCollection attempt;

	private DynamoDbThrottleMetricPublisher publisher;

	@BeforeEach
	void setUp() {
		publisher = new DynamoDbThrottleMetricPublisher(meterRegistry);
	}

	@Test
	void publish_shouldIncrementCounter_whenAttemptErrorTypeIndicatesThrottling() {
		// Arrange
		given(metricCollection.children()).willReturn(List.of(attempt));
		given(attempt.metricValues(CoreMetric.ERROR_TYPE)).willReturn(List.of(ERROR_TYPE_THROTTLING));

		// Act
		publisher.publish(metricCollection);

		// Assert
		assertThat(meterRegistry.counter(COUNTER_NAME).count()).isEqualTo(1.0);
	}

	@Test
	void publish_shouldMatchCaseInsensitively_whenErrorTypeCasingDiffers() {
		// Arrange
		given(metricCollection.children()).willReturn(List.of(attempt));
		given(attempt.metricValues(CoreMetric.ERROR_TYPE)).willReturn(List.of(ERROR_TYPE_THROTTLING.toUpperCase()));

		// Act
		publisher.publish(metricCollection);

		// Assert
		assertThat(meterRegistry.counter(COUNTER_NAME).count()).isEqualTo(1.0);
	}

	@Test
	void publish_shouldNotIncrementCounter_whenAttemptErrorTypeIsNotThrottling() {
		// Arrange
		given(metricCollection.children()).willReturn(List.of(attempt));
		given(attempt.metricValues(CoreMetric.ERROR_TYPE)).willReturn(List.of(ERROR_TYPE_VALIDATION));

		// Act
		publisher.publish(metricCollection);

		// Assert
		assertThat(meterRegistry.counter(COUNTER_NAME).count()).isEqualTo(0.0);
	}

	@Test
	void publish_shouldNotIncrementCounter_whenAttemptHasNoErrorType() {
		// Arrange
		given(metricCollection.children()).willReturn(List.of(attempt));
		given(attempt.metricValues(CoreMetric.ERROR_TYPE)).willReturn(List.of());

		// Act
		publisher.publish(metricCollection);

		// Assert
		assertThat(meterRegistry.counter(COUNTER_NAME).count()).isEqualTo(0.0);
	}

	@Test
	void publish_shouldNotIncrementCounter_whenThereAreNoAttempts() {
		// Arrange
		given(metricCollection.children()).willReturn(List.of());

		// Act
		publisher.publish(metricCollection);

		// Assert
		assertThat(meterRegistry.counter(COUNTER_NAME).count()).isEqualTo(0.0);
	}

	@Test
	void publish_shouldIncrementCounterPerThrottledAttempt_whenMultipleAttemptsAreThrottled() {
		// Arrange
		final MetricCollection secondAttempt = mock(MetricCollection.class);
		given(metricCollection.children()).willReturn(List.of(attempt, secondAttempt));
		given(attempt.metricValues(CoreMetric.ERROR_TYPE)).willReturn(List.of(ERROR_TYPE_THROTTLING));
		given(secondAttempt.metricValues(CoreMetric.ERROR_TYPE)).willReturn(List.of(ERROR_TYPE_THROTTLING));

		// Act
		publisher.publish(metricCollection);

		// Assert
		assertThat(meterRegistry.counter(COUNTER_NAME).count()).isEqualTo(2.0);
	}

}
