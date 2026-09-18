package io.urlshortener.urlservice.repository;

import io.urlshortener.urlservice.exception.AliasAlreadyExistsException;
import io.urlshortener.urlservice.model.domainObject.ShortLink;

import java.util.Optional;

/**
 * Persistence port for {@link ShortLink}s.
 */
public interface LinkRepository {

	/**
	 * Persists a new link.
	 *
	 * @param shortLink the link to persist.
	 * @return the persisted link.
	 * @throws AliasAlreadyExistsException if the link's short code is already in use.
	 */
	ShortLink save(final ShortLink shortLink) throws AliasAlreadyExistsException;

	/**
	 * Looks up a link by its short code.
	 *
	 * @param shortCode the short code to look up.
	 * @return the matching link, or {@link Optional#empty()} if no link exists for that short code.
	 */
	Optional<ShortLink> findByShortCode(final String shortCode);

}
