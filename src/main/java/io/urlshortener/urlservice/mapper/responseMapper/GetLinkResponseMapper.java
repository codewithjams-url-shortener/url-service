package io.urlshortener.urlservice.mapper.responseMapper;

import io.urlshortener.urlservice.model.dataTransferObject.GetLinkResponse;
import io.urlshortener.urlservice.model.domainObject.ShortLink;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Optional;

/**
 * Converts a {@link ShortLink} into the outgoing {@link GetLinkResponse}.
 */
@Component
public class GetLinkResponseMapper implements ResponseMapper<ShortLink, GetLinkResponse> {

	/**
	 * Converts a {@link ShortLink} into a {@link GetLinkResponse}.
	 *
	 * @param domain the link to convert.
	 * @return the response DTO, with {@code createdAt}/{@code expiresAt} expressed in UTC and
	 *         {@code expiresAt} left {@code null} for a never-expiring link.
	 */
	@Override
	public GetLinkResponse toDto(final ShortLink domain) {
		final URI longUrl = URI.create(domain.getLongUrl());
		final OffsetDateTime createdAt = OffsetDateTime.ofInstant(domain.getCreatedAt(), ZoneOffset.UTC);
		final OffsetDateTime expiresAt = Optional.ofNullable(domain.getExpiresAt())
				.map(instant -> OffsetDateTime.ofInstant(instant, ZoneOffset.UTC))
				.orElse(null);
		return GetLinkResponse.builder()
				.shortCode(domain.getShortCode())
				.longUrl(longUrl)
				.createdAt(createdAt)
				.expiresAt(expiresAt)
				.status(domain.getStatus())
				.build();
	}

}
