package io.urlshortener.urlservice.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;
import software.amazon.awssdk.core.metrics.CoreMetric;
import software.amazon.awssdk.metrics.MetricCollection;
import software.amazon.awssdk.metrics.MetricPublisher;

/**
 * Counts DynamoDB request attempts that were throttled, exposed as a Micrometer counter so it's
 * visible in Prometheus/Grafana.
 *
 * <p>Registered as a {@link software.amazon.awssdk.core.client.config.ClientOverrideConfiguration}
 * metric publisher on the {@link software.amazon.awssdk.services.dynamodb.DynamoDbClient} bean (see
 * {@code AwsConfig}), so {@link #publish(MetricCollection)} is invoked once per logical API call -
 * counting per-attempt rather than catching exceptions in the repository layer matters here, since
 * the SDK's default retry policy can silently retry a throttled attempt to a successful outcome,
 * which would never surface as an exception at all.
 *
 * <p>{@link CoreMetric#ERROR_TYPE} takes exactly one of five fixed values per the AWS SDK's own design
 * docs (<a href="https://github.com/aws/aws-sdk-java-v2/blob/master/docs/design/core/metrics/MetricsList.md">
 * MetricsList.md</a>): {@code Throttling}, {@code ServerError}, {@code ConfiguredTimeout}, {@code IO},
 * and {@code Other}. Only {@code Throttling} is counted here.
 */
@Slf4j
public class DynamoDbThrottleMetricPublisher implements MetricPublisher {

	/**
	 * Counts throttled DynamoDB request attempts.
	 */
	private final Counter throttleCounter;

	/**
	 * Builds and registers the underlying counter.
	 *
	 * @param meterRegistry the registry to register the counter against.
	 */
	public DynamoDbThrottleMetricPublisher(final MeterRegistry meterRegistry) {
		this.throttleCounter = Counter.builder("dynamodb.throttle.count")
				.description("Number of DynamoDB request attempts rejected due to throttling")
				.register(meterRegistry);
	}

	/**
	 * Inspects every attempt recorded under the given call-level collection, incrementing
	 * {@link #throttleCounter} for each one whose {@link CoreMetric#ERROR_TYPE} indicates throttling.
	 *
	 * @param metricCollection the metrics recorded for one logical API call, with one child collection
	 *                         per HTTP attempt (including ones the SDK retried transparently).
	 */
	@Override
	public void publish(final MetricCollection metricCollection) {
		metricCollection.children().forEach(this::handleAttempt);
	}

	/**
	 * No resources to release - this publisher holds nothing beyond the counter reference.
	 */
	@Override
	public void close() {
	}

	private void handleAttempt(final MetricCollection attempt) {
		attempt.metricValues(CoreMetric.ERROR_TYPE).forEach(this::handleErrorType);
	}

	private void handleErrorType(final String errorType) {
		if (StringUtils.hasText(errorType) && errorType.toLowerCase().contains("throttl")) {
			log.atWarn()
					.addKeyValue("errorType", errorType)
					.log("DynamoDB request attempt was throttled");
			throttleCounter.increment();
		}
	}

}
