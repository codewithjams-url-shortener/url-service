package io.urlshortener.urlservice.mapper.dbMapper;

import io.urlshortener.linkscontract.Link;
import io.urlshortener.urlservice.exception.InvalidConversionInputException;
import io.urlshortener.urlservice.model.domainObject.ShortLink;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LinkDbMapperTest {

	private final LinkDbMapper mapper = new LinkDbMapper();

	@Test
	void toEntity_shouldThrowInvalidConversionInputException_whenDomainIsNull() {
		// Arrange
		// (null is the input under test)

		// Act & Assert
		assertThatThrownBy(() -> mapper.toEntity(null))
				.isInstanceOf(InvalidConversionInputException.class);
	}

	@Test
	void toDomain_shouldThrowInvalidConversionInputException_whenEntityIsNull() {
		// Arrange
		// (null is the input under test)

		// Act & Assert
		assertThatThrownBy(() -> mapper.toDomain(null))
				.isInstanceOf(InvalidConversionInputException.class);
	}

	@Test
	void toEntity_shouldConvertCreatedAtToEpochMilliseconds_whenDomainHasACreatedAtInstant() {
		// Arrange
		final Instant createdAt = Instant.parse("2030-01-01T00:00:00Z");
		final ShortLink domain = ShortLink.builder()
				.shortCode("abc1234")
				.longUrl("https://example.com")
				.createdAt(createdAt)
				.managementTokenHash("hash")
				.build();

		// Act
		final Link entity = mapper.toEntity(domain);

		// Assert
		assertThat(entity.createdAt()).isEqualTo(createdAt.toEpochMilli());
	}

	@Test
	void toEntity_shouldConvertExpiresAtToEpochSeconds_whenDomainHasAnExpiresAtInstant() {
		// Arrange
		final Instant expiresAt = Instant.parse("2030-01-01T00:00:00Z");
		final ShortLink domain = ShortLink.builder()
				.shortCode("abc1234")
				.longUrl("https://example.com")
				.createdAt(Instant.now())
				.expiresAt(expiresAt)
				.managementTokenHash("hash")
				.build();

		// Act
		final Link entity = mapper.toEntity(domain);

		// Assert
		assertThat(entity.expiresAt()).isEqualTo(expiresAt.getEpochSecond());
	}

	@Test
	void toEntity_shouldLeaveExpiresAtNull_whenDomainHasNoExpiresAt() {
		// Arrange
		final ShortLink domain = ShortLink.builder()
				.shortCode("abc1234")
				.longUrl("https://example.com")
				.createdAt(Instant.now())
				.managementTokenHash("hash")
				.build();

		// Act
		final Link entity = mapper.toEntity(domain);

		// Assert
		assertThat(entity.expiresAt()).isNull();
	}

	@Test
	void toDomain_shouldConvertCreatedAtFromEpochMilliseconds_whenEntityHasACreatedAtValue() {
		// Arrange
		final long createdAtMillis = Instant.parse("2030-01-01T00:00:00Z").toEpochMilli();
		final Link entity = new Link("abc1234", "https://example.com", null, createdAtMillis, null, "hash", null);

		// Act
		final ShortLink domain = mapper.toDomain(entity);

		// Assert
		assertThat(domain.getCreatedAt()).isEqualTo(Instant.ofEpochMilli(createdAtMillis));
	}

	@Test
	void toDomain_shouldConvertExpiresAtFromEpochSeconds_whenEntityHasAnExpiresAtValue() {
		// Arrange
		final long expiresAtSeconds = Instant.parse("2030-01-01T00:00:00Z").getEpochSecond();
		final Link entity = new Link(
				"abc1234", "https://example.com", null, Instant.now().toEpochMilli(), expiresAtSeconds, "hash", null
		);

		// Act
		final ShortLink domain = mapper.toDomain(entity);

		// Assert
		assertThat(domain.getExpiresAt()).isEqualTo(Instant.ofEpochSecond(expiresAtSeconds));
	}

	@Test
	void toDomain_shouldLeaveExpiresAtNull_whenEntityHasNoExpiresAt() {
		// Arrange
		final Link entity = new Link("abc1234", "https://example.com", null, Instant.now().toEpochMilli(), null, "hash", null);

		// Act
		final ShortLink domain = mapper.toDomain(entity);

		// Assert
		assertThat(domain.getExpiresAt()).isNull();
	}

	@Test
	void toDomain_shouldLeaveCustomAliasNull_whenEntityHasNoCustomAliasEquivalent() {
		// Arrange
		final Link entity = new Link("abc1234", "https://example.com", null, Instant.now().toEpochMilli(), null, "hash", null);

		// Act
		final ShortLink domain = mapper.toDomain(entity);

		// Assert
		assertThat(domain.getCustomAlias()).isNull();
	}

	@Test
	void toEntityThenToDomain_shouldPreserveAllSharedFields_whenRoundTrippingADomainObject() {
		// Arrange
		final Instant createdAt = Instant.parse("2030-01-01T00:00:00Z");
		final Instant expiresAt = Instant.parse("2031-01-01T00:00:00Z");
		final ShortLink original = ShortLink.builder()
				.shortCode("abc1234")
				.longUrl("https://example.com")
				.ownerId("owner-1")
				.createdAt(createdAt)
				.expiresAt(expiresAt)
				.managementTokenHash("hash")
				.status("FLAGGED")
				.build();

		// Act
		final ShortLink roundTripped = mapper.toDomain(mapper.toEntity(original));

		// Assert
		assertThat(roundTripped.getShortCode()).isEqualTo(original.getShortCode());
		assertThat(roundTripped.getLongUrl()).isEqualTo(original.getLongUrl());
		assertThat(roundTripped.getOwnerId()).isEqualTo(original.getOwnerId());
		assertThat(roundTripped.getCreatedAt()).isEqualTo(original.getCreatedAt());
		assertThat(roundTripped.getExpiresAt()).isEqualTo(original.getExpiresAt());
		assertThat(roundTripped.getManagementTokenHash()).isEqualTo(original.getManagementTokenHash());
		assertThat(roundTripped.getStatus()).isEqualTo(original.getStatus());
	}

}
