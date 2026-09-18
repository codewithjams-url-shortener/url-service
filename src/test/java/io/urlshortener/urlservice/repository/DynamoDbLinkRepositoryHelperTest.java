package io.urlshortener.urlservice.repository;

import io.urlshortener.urlservice.model.domainObject.LinkPatch;
import org.junit.jupiter.api.Test;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

import java.time.Instant;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class DynamoDbLinkRepositoryHelperTest {

	@Test
	void createLinkUpdateExpression_shouldReturnEmptyString_whenPatchHasNoFieldsToUpdate() {
		// Arrange
		final LinkPatch patch = new LinkPatch(null, false, null);

		// Act
		final String updateExpression = DynamoDbLinkRepositoryHelper.createLinkUpdateExpression(patch);

		// Assert
		assertThat(updateExpression).isEmpty();
	}

	@Test
	void createLinkUpdateExpression_shouldContainOnlySetLongUrl_whenOnlyLongUrlIsProvided() {
		// Arrange
		final LinkPatch patch = new LinkPatch("https://example.com", false, null);

		// Act
		final String updateExpression = DynamoDbLinkRepositoryHelper.createLinkUpdateExpression(patch);

		// Assert
		assertThat(updateExpression).isEqualTo("SET longUrl = :longUrl");
	}

	@Test
	void createLinkUpdateExpression_shouldContainSetExpiresAt_whenExpiresAtIsProvidedWithAValue() {
		// Arrange
		final LinkPatch patch = new LinkPatch(null, true, Instant.parse("2030-01-01T00:00:00Z"));

		// Act
		final String updateExpression = DynamoDbLinkRepositoryHelper.createLinkUpdateExpression(patch);

		// Assert
		assertThat(updateExpression).isEqualTo("SET expiresAt = :expiresAt");
	}

	@Test
	void createLinkUpdateExpression_shouldContainRemoveExpiresAt_whenExpiresAtIsProvidedAsNull() {
		// Arrange
		final LinkPatch patch = new LinkPatch(null, true, null);

		// Act
		final String updateExpression = DynamoDbLinkRepositoryHelper.createLinkUpdateExpression(patch);

		// Assert
		assertThat(updateExpression).isEqualTo("REMOVE expiresAt");
	}

	@Test
	void createLinkUpdateExpression_shouldContainBothSetAndRemoveClauses_whenLongUrlIsProvidedAndExpiresAtIsCleared() {
		// Arrange
		final LinkPatch patch = new LinkPatch("https://example.com", true, null);

		// Act
		final String updateExpression = DynamoDbLinkRepositoryHelper.createLinkUpdateExpression(patch);

		// Assert
		assertThat(updateExpression).isEqualTo("SET longUrl = :longUrl REMOVE expiresAt");
	}

	@Test
	void createLinkUpdateExpression_shouldContainOnlyOneSetClause_whenLongUrlAndExpiresAtValueAreBothProvided() {
		// Arrange
		final LinkPatch patch = new LinkPatch("https://example.com", true, Instant.parse("2030-01-01T00:00:00Z"));

		// Act
		final String updateExpression = DynamoDbLinkRepositoryHelper.createLinkUpdateExpression(patch);

		// Assert
		assertThat(updateExpression).isEqualTo("SET longUrl = :longUrl, expiresAt = :expiresAt");
	}

	@Test
	void createLinkUpdateExpressionValue_shouldContainOnlyExpectedHash_whenPatchHasNoFieldsToUpdate() {
		// Arrange
		final LinkPatch patch = new LinkPatch(null, false, null);

		// Act
		final Map<String, AttributeValue> values =
				DynamoDbLinkRepositoryHelper.createLinkUpdateExpressionValue(patch, "hash-value");

		// Assert
		assertThat(values).containsOnlyKeys(":expectedHash");
		assertThat(values.get(":expectedHash").s()).isEqualTo("hash-value");
	}

	@Test
	void createLinkUpdateExpressionValue_shouldIncludeLongUrlValue_whenLongUrlIsProvided() {
		// Arrange
		final LinkPatch patch = new LinkPatch("https://example.com", false, null);

		// Act
		final Map<String, AttributeValue> values =
				DynamoDbLinkRepositoryHelper.createLinkUpdateExpressionValue(patch, "hash-value");

		// Assert
		assertThat(values.get(":longUrl").s()).isEqualTo("https://example.com");
	}

	@Test
	void createLinkUpdateExpressionValue_shouldIncludeExpiresAtAsEpochSeconds_whenExpiresAtIsProvidedWithAValue() {
		// Arrange
		final Instant expiresAt = Instant.parse("2030-01-01T00:00:00Z");
		final LinkPatch patch = new LinkPatch(null, true, expiresAt);

		// Act
		final Map<String, AttributeValue> values =
				DynamoDbLinkRepositoryHelper.createLinkUpdateExpressionValue(patch, "hash-value");

		// Assert
		assertThat(values.get(":expiresAt").n()).isEqualTo(String.valueOf(expiresAt.getEpochSecond()));
	}

	@Test
	void createLinkUpdateExpressionValue_shouldNotIncludeExpiresAtValue_whenExpiresAtIsProvidedAsNull() {
		// Arrange
		final LinkPatch patch = new LinkPatch(null, true, null);

		// Act
		final Map<String, AttributeValue> values =
				DynamoDbLinkRepositoryHelper.createLinkUpdateExpressionValue(patch, "hash-value");

		// Assert
		assertThat(values).doesNotContainKey(":expiresAt");
	}

}
