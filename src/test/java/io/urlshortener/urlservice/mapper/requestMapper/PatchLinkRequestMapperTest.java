package io.urlshortener.urlservice.mapper.requestMapper;

import io.urlshortener.urlservice.exception.InvalidConversionInputException;
import io.urlshortener.urlservice.model.dataTransferObject.PatchLinkRequest;
import io.urlshortener.urlservice.model.domainObject.LinkPatch;
import org.junit.jupiter.api.Test;
import org.openapitools.jackson.nullable.JsonNullable;

import java.net.URI;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PatchLinkRequestMapperTest {

	private final PatchLinkRequestMapper mapper = new PatchLinkRequestMapper();

	@Test
	void toDomain_shouldThrowInvalidConversionInputException_whenRequestBodyIsNull() {
		// Arrange
		// (null is the input under test)

		// Act & Assert
		assertThatThrownBy(() -> mapper.toDomain(null))
				.isInstanceOf(InvalidConversionInputException.class);
	}

	@Test
	void toDomain_shouldLeaveLongUrlNull_whenRequestBodyHasNoLongUrl() {
		// Arrange
		final PatchLinkRequest dto = PatchLinkRequest.builder().build();

		// Act
		final LinkPatch patch = mapper.toDomain(dto);

		// Assert
		assertThat(patch.longUrl()).isNull();
	}

	@Test
	void toDomain_shouldMapLongUrl_whenRequestBodyHasALongUrl() {
		// Arrange
		final PatchLinkRequest dto = PatchLinkRequest.builder()
				.longUrl(URI.create("https://example.com/new/path"))
				.build();

		// Act
		final LinkPatch patch = mapper.toDomain(dto);

		// Assert
		assertThat(patch.longUrl()).isEqualTo("https://example.com/new/path");
	}

	@Test
	void toDomain_shouldMarkExpiresAtAsNotProvided_whenRequestBodyOmitsExpiresAt() {
		// Arrange
		final PatchLinkRequest dto = PatchLinkRequest.builder().build();

		// Act
		final LinkPatch patch = mapper.toDomain(dto);

		// Assert
		assertThat(patch.expiresAtProvided()).isFalse();
		assertThat(patch.expiresAt()).isNull();
	}

	@Test
	void toDomain_shouldMarkExpiresAtAsProvidedWithNullValue_whenRequestBodyExplicitlyClearsExpiresAt() {
		// Arrange
		final PatchLinkRequest dto = PatchLinkRequest.builder()
				.expiresAt(JsonNullable.of(null))
				.build();

		// Act
		final LinkPatch patch = mapper.toDomain(dto);

		// Assert
		assertThat(patch.expiresAtProvided()).isTrue();
		assertThat(patch.expiresAt()).isNull();
	}

	@Test
	void toDomain_shouldMarkExpiresAtAsProvidedWithValue_whenRequestBodySetsExpiresAt() {
		// Arrange
		final OffsetDateTime expiresAt = OffsetDateTime.of(2030, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC);
		final PatchLinkRequest dto = PatchLinkRequest.builder()
				.expiresAt(JsonNullable.of(expiresAt))
				.build();

		// Act
		final LinkPatch patch = mapper.toDomain(dto);

		// Assert
		assertThat(patch.expiresAtProvided()).isTrue();
		assertThat(patch.expiresAt()).isEqualTo(expiresAt.toInstant());
	}

}
