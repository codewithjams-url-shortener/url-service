package io.urlshortener.urlservice.model.result;

import io.urlshortener.urlservice.model.domainObject.ShortLink;

/**
 * Result of creating a link: the persisted {@link ShortLink} together with its one-time raw
 * management token, which is never stored on the domain object itself.
 *
 * @param shortLink          the created, persisted link.
 * @param rawManagementToken the plaintext management token, to be returned to the caller exactly once.
 */
public record CreateLinkResult(
		ShortLink shortLink,
		String rawManagementToken
) {
}
