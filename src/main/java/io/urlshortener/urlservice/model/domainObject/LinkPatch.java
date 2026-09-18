package io.urlshortener.urlservice.model.domainObject;

import java.time.Instant;

/**
 * A partial edit to apply to an existing {@link ShortLink}. Fields left at their "not provided" state are left
 * unchanged by the update; {@link expiresAt} additionally supports being cleared back to never-expiring, distinct from
 * being left alone.
 *
 * @param longUrl           the new destination URL, or {@code null} to leave it unchanged.
 * @param expiresAtProvided whether the caller supplied an {@link expiresAt} value at all
 *                          (including explicitly clearing it) as opposed to omitting the field.
 * @param expiresAt         meaningful only when {@link expiresAtProvided} is {@code true}: the new
 *                          expiry, or {@code null} to clear it (make the link never-expire).
 */
public record LinkPatch(
		String longUrl,
		boolean expiresAtProvided,
		Instant expiresAt
) {
}
