package io.urlshortener.urlservice.mapper.requestMapper;

import io.urlshortener.urlservice.exception.InvalidConversionInputException;
import io.urlshortener.urlservice.model.dataTransferObject.PatchLinkRequest;
import io.urlshortener.urlservice.model.domainObject.LinkPatch;
import lombok.extern.slf4j.Slf4j;
import org.openapitools.jackson.nullable.JsonNullable;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.Optional;

/**
 * Converts an incoming {@link PatchLinkRequest} into a {@link LinkPatch} domain object.
 */
@Slf4j
@Component
public class PatchLinkRequestMapper implements RequestMapper<PatchLinkRequest, LinkPatch> {

	/**
	 * Converts a {@link PatchLinkRequest} into a {@link LinkPatch}, unwrapping {@code expiresAt}'s
	 * {@code JsonNullable} into the domain's explicit provided/value pair.
	 *
	 * @param dto the patch request body.
	 * @return the equivalent {@link LinkPatch}.
	 * @throws InvalidConversionInputException if {@code dto} is {@code null}.
	 */
	@Override
	public LinkPatch toDomain(final PatchLinkRequest dto) {
		if (dto == null) {
			log.atError()
					.addKeyValue("reason", "No Request Body provided")
					.log("Domain: LinkPatch conversion failed");
			throw new InvalidConversionInputException("Request Body is missing");
		}
		final String longUrl = Optional.ofNullable(dto.getLongUrl())
				.map(URI::toString)
				.orElse(null);
		final JsonNullable<OffsetDateTime> expiresAtField = Optional.ofNullable(dto.getExpiresAt())
				.orElse(JsonNullable.undefined());
		final Instant expiresAt = Optional.of(expiresAtField)
				.filter(JsonNullable::isPresent)
				.map(JsonNullable::get)
				.map(OffsetDateTime::toInstant)
				.orElse(null);
		return new LinkPatch(longUrl, expiresAtField.isPresent(), expiresAt);
	}

}
