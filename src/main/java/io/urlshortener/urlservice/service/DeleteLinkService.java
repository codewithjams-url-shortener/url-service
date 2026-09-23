package io.urlshortener.urlservice.service;

import io.urlshortener.urlservice.exception.ManagementTokenMismatchException;
import io.urlshortener.urlservice.generator.TokenHasher;
import io.urlshortener.urlservice.repository.LinkRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * Orchestrates deleting an existing link, gated by its management token.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DeleteLinkService {

	/** Deletes the link. */
	private final LinkRepository linkRepository;

	/**
	 * Deletes an existing link.
	 *
	 * @param shortCode          the short code of the link to delete.
	 * @param rawManagementToken the caller-presented management token; may be {@code null} if the
	 *                           header was omitted, treated the same as a mismatched token.
	 * @throws ManagementTokenMismatchException if {@code rawManagementToken} is missing or does not
	 *                                           match the link's stored token hash.
	 * @throws io.urlshortener.urlservice.exception.ShortLinkNotFoundException
	 *                                           if no link exists for that short code.
	 */
	public void deleteLink(final String shortCode, final String rawManagementToken) {
		if (!StringUtils.hasText(rawManagementToken)) {
			throw new ManagementTokenMismatchException(shortCode);
		}
		final String managementTokenHash = TokenHasher.hash(rawManagementToken);
		linkRepository.delete(shortCode, managementTokenHash);
	}

}
