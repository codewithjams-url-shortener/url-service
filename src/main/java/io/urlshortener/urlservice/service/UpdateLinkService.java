package io.urlshortener.urlservice.service;

import io.urlshortener.urlservice.exception.ManagementTokenMismatchException;
import io.urlshortener.urlservice.exception.NoUpdatableFieldsProvidedException;
import io.urlshortener.urlservice.generator.TokenHasher;
import io.urlshortener.urlservice.model.domainObject.LinkPatch;
import io.urlshortener.urlservice.model.domainObject.ShortLink;
import io.urlshortener.urlservice.repository.LinkRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * Orchestrates applying a partial edit to an existing link, gated by its management token.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UpdateLinkService {

	/**
	 * Applies the update.
	 */
	private final LinkRepository linkRepository;

	/**
	 * Applies a patch to an existing link.
	 *
	 * @param shortCode          the short code of the link to update.
	 * @param rawManagementToken the caller-presented management token; may be {@code null} if the
	 *                           header was omitted, treated the same as a mismatched token.
	 * @param patch              the fields to change.
	 * @return the link after the update has been applied.
	 * @throws NoUpdatableFieldsProvidedException                              if {@code patch} contains no changes
	 *                                                                         at all.
	 * @throws ManagementTokenMismatchException                                if {@code rawManagementToken} is missing
	 *                                                                         or does not match the link's stored
	 *                                                                         token hash.
	 * @throws io.urlshortener.urlservice.exception.ShortLinkNotFoundException if no link exists for that short code.
	 */
	public ShortLink updateLink(final String shortCode, final String rawManagementToken, final LinkPatch patch) {
		if (!StringUtils.hasText(patch.longUrl()) && !patch.expiresAtProvided()) {
			throw new NoUpdatableFieldsProvidedException("No Fields provided for update");
		}
		if (!StringUtils.hasText(rawManagementToken)) {
			throw new ManagementTokenMismatchException(shortCode);
		}
		final String managementTokenHash = TokenHasher.hash(rawManagementToken);
		return linkRepository.update(shortCode, managementTokenHash, patch);
	}

}
