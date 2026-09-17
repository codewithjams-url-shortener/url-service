package io.urlshortener.urlservice.repository;

import io.urlshortener.urlservice.exception.AliasAlreadyExistsException;
import io.urlshortener.urlservice.model.domainObject.ShortLink;

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

}
