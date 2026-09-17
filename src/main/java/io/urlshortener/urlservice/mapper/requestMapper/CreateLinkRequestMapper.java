package io.urlshortener.urlservice.mapper.requestMapper;

import io.urlshortener.urlservice.exception.InvalidConversionInputException;
import io.urlshortener.urlservice.model.dataTransferObject.CreateLinkRequest;
import io.urlshortener.urlservice.model.domainObject.ShortLink;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.Optional;

/**
 * Converts an incoming {@link CreateLinkRequest} into a {@link ShortLink} domain object.
 */
@Slf4j
@Component
public class CreateLinkRequestMapper implements RequestMapper<CreateLinkRequest, ShortLink> {

	/**
	 * Converts a {@link CreateLinkRequest} into a {@link ShortLink}.
	 *
	 * @param dto the create-link request body.
	 * @return a {@link ShortLink} populated with the fields supplied by the caller
	 *         ({@code shortCode}, {@code createdAt}, and {@code managementTokenHash} are left unset,
	 *         to be filled in by the service layer).
	 * @throws InvalidConversionInputException if {@code dto} is {@code null}.
	 */
	@Override
	public ShortLink toDomain(final CreateLinkRequest dto) {
		if (dto == null) {
			log.atError()
					.addKeyValue("reason", "No Request Body provided")
					.log("Domain: ShortLink conversion failed");
			throw new InvalidConversionInputException("Request Body is missing");
		}

		final Instant expiresAt = Optional.ofNullable(dto.getExpiresAt())
				.map(OffsetDateTime::toInstant)
				.orElse(null);

		log.atDebug()
				.addKeyValue("populated", Objects.nonNull(expiresAt))
				.log("Checking Expires At");

		return ShortLink.builder()
				.longUrl(dto.getLongUrl().toString())
				.customAlias(dto.getCustomAlias())
				.expiresAt(expiresAt)
				.build();
	}

}
