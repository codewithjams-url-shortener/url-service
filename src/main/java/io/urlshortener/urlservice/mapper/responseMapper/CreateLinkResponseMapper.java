package io.urlshortener.urlservice.mapper.responseMapper;

import io.urlshortener.urlservice.model.dataTransferObject.CreateLinkResponse;
import io.urlshortener.urlservice.model.domainObject.ShortLink;
import io.urlshortener.urlservice.model.result.CreateLinkResult;
import io.urlshortener.urlservice.property.ShortUrlBuilder;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.net.URI;

/**
 * Converts a {@link CreateLinkResult} into the outgoing {@link CreateLinkResponse}.
 */
@Component
@RequiredArgsConstructor
public class CreateLinkResponseMapper implements ResponseMapper<CreateLinkResult, CreateLinkResponse> {

	/**
	 * Resolves the created link's short code into its public short URL.
	 */
	private final ShortUrlBuilder urlBuilder;

	/**
	 * Converts a {@link CreateLinkResult} into a {@link CreateLinkResponse}.
	 *
	 * @param result the created link and its one-time raw management token.
	 * @return the response DTO, with {@code shortUrl} resolved against the configured base URL and
	 *         {@code managementToken} set to the raw (not hashed) token.
	 */
	@Override
	public CreateLinkResponse toDto(final CreateLinkResult result) {
		final ShortLink shortLink = result.shortLink();
		final URI shortUrl = urlBuilder.build(shortLink.getShortCode());
		return CreateLinkResponse.builder()
				.shortCode(shortLink.getShortCode())
				.shortUrl(shortUrl)
				.managementToken(result.rawManagementToken())
				.build();
	}

}
