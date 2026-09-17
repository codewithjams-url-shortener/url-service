package io.urlshortener.urlservice.mapper.dbMapper;

import io.urlshortener.linkscontract.Link;
import io.urlshortener.urlservice.exception.CorruptedDataException;
import org.junit.jupiter.api.Test;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LinkAttributeMapperTest {

	private final LinkAttributeMapper mapper = new LinkAttributeMapper();

	@Test
	void toAttributeValue_shouldWriteAllFields_whenLinkHasEveryFieldPopulated() {
		// Arrange
		final Link link = new Link(
				"abc1234",
				"https://example.com",
				"owner-1",
				1_000L,
				2_000L,
				"hash",
				"FLAGGED"
		);

		// Act
		final Map<String, AttributeValue> attributeMap = mapper.toAttributeValue(link);

		// Assert
		assertThat(attributeMap.get("shortCode").s()).isEqualTo("abc1234");
		assertThat(attributeMap.get("longUrl").s()).isEqualTo("https://example.com");
		assertThat(attributeMap.get("ownerId").s()).isEqualTo("owner-1");
		assertThat(attributeMap.get("createdAt").n()).isEqualTo("1000");
		assertThat(attributeMap.get("expiresAt").n()).isEqualTo("2000");
		assertThat(attributeMap.get("managementTokenHash").s()).isEqualTo("hash");
		assertThat(attributeMap.get("status").s()).isEqualTo("FLAGGED");
	}

	@Test
	void toAttributeValue_shouldOmitOwnerId_whenLinkHasNoOwnerId() {
		// Arrange
		final Link link = new Link(
				"abc1234",
				"https://example.com",
				null,
				1_000L,
				null,
				"hash",
				null
		);

		// Act
		final Map<String, AttributeValue> attributeMap = mapper.toAttributeValue(link);

		// Assert
		assertThat(attributeMap).doesNotContainKey("ownerId");
	}

	@Test
	void toAttributeValue_shouldOmitExpiresAt_whenLinkHasNoExpiresAt() {
		// Arrange
		final Link link = new Link(
				"abc1234",
				"https://example.com",
				null,
				1_000L,
				null,
				"hash",
				null
		);

		// Act
		final Map<String, AttributeValue> attributeMap = mapper.toAttributeValue(link);

		// Assert
		assertThat(attributeMap).doesNotContainKey("expiresAt");
	}

	@Test
	void toAttributeValue_shouldOmitStatus_whenLinkHasNoStatus() {
		// Arrange
		final Link link = new Link(
				"abc1234",
				"https://example.com",
				null,
				1_000L,
				null,
				"hash",
				null
		);

		// Act
		final Map<String, AttributeValue> attributeMap = mapper.toAttributeValue(link);

		// Assert
		assertThat(attributeMap).doesNotContainKey("status");
	}

	@Test
	void toObject_shouldThrowCorruptedDataException_whenCreatedAtIsMissing() {
		// Arrange
		final Map<String, AttributeValue> attributeMap = new HashMap<>();
		attributeMap.put("shortCode", AttributeValue.builder().s("abc1234").build());

		// Act & Assert
		assertThatThrownBy(() -> mapper.toObject(attributeMap))
				.isInstanceOf(CorruptedDataException.class);
	}

	@Test
	void toObject_shouldReadAllFields_whenAttributeMapHasEveryFieldPresent() {
		// Arrange
		final Map<String, AttributeValue> attributeMap = new HashMap<>();
		attributeMap.put("shortCode", AttributeValue.builder().s("abc1234").build());
		attributeMap.put("longUrl", AttributeValue.builder().s("https://example.com").build());
		attributeMap.put("ownerId", AttributeValue.builder().s("owner-1").build());
		attributeMap.put("createdAt", AttributeValue.builder().n("1000").build());
		attributeMap.put("expiresAt", AttributeValue.builder().n("2000").build());
		attributeMap.put("managementTokenHash", AttributeValue.builder().s("hash").build());
		attributeMap.put("status", AttributeValue.builder().s("FLAGGED").build());

		// Act
		final Link link = mapper.toObject(attributeMap);

		// Assert
		assertThat(link.shortCode()).isEqualTo("abc1234");
		assertThat(link.longUrl()).isEqualTo("https://example.com");
		assertThat(link.ownerId()).isEqualTo("owner-1");
		assertThat(link.createdAt()).isEqualTo(1000L);
		assertThat(link.expiresAt()).isEqualTo(2000L);
		assertThat(link.managementTokenHash()).isEqualTo("hash");
		assertThat(link.status()).isEqualTo("FLAGGED");
	}

	@Test
	void toObject_shouldLeaveOptionalFieldsNull_whenAttributeMapHasThemAbsent() {
		// Arrange
		final Map<String, AttributeValue> attributeMap = new HashMap<>();
		attributeMap.put("shortCode", AttributeValue.builder().s("abc1234").build());
		attributeMap.put("longUrl", AttributeValue.builder().s("https://example.com").build());
		attributeMap.put("createdAt", AttributeValue.builder().n("1000").build());
		attributeMap.put("managementTokenHash", AttributeValue.builder().s("hash").build());

		// Act
		final Link link = mapper.toObject(attributeMap);

		// Assert
		assertThat(link.ownerId()).isNull();
		assertThat(link.expiresAt()).isNull();
		assertThat(link.status()).isNull();
	}

	@Test
	void toAttributeValueThenToObject_shouldPreserveAllFields_whenRoundTrippingALink() {
		// Arrange
		final Link original = new Link(
				"abc1234",
				"https://example.com",
				"owner-1",
				1_000L,
				2_000L,
				"hash",
				"FLAGGED"
		);

		// Act
		final Link roundTripped = mapper.toObject(mapper.toAttributeValue(original));

		// Assert
		assertThat(roundTripped).isEqualTo(original);
	}

}
