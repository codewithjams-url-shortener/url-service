package io.urlshortener.urlservice.repository;

import io.urlshortener.urlservice.exception.AliasAlreadyExistsException;
import io.urlshortener.urlservice.exception.ManagementTokenMismatchException;
import io.urlshortener.urlservice.exception.ShortLinkNotFoundException;
import io.urlshortener.urlservice.model.domainObject.LinkPatch;
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

	/**
	 * Applies a partial edit to an existing link, gated by its management token hash.
	 *
	 * @param shortCode          the short code of the link to update.
	 * @param managementTokenHash the caller-presented token's hash, checked against the link's own.
	 * @param patch              the fields to change; a field left unset is not modified.
	 * @return the link after the update has been applied.
	 * @throws ShortLinkNotFoundException     if no link exists for that short code.
	 * @throws ManagementTokenMismatchException if the link exists but {@code managementTokenHash}
	 *                                           does not match its stored hash.
	 */
	ShortLink update(final String shortCode, final String managementTokenHash, final LinkPatch patch)
			throws ShortLinkNotFoundException, ManagementTokenMismatchException;

}
