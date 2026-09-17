package io.urlshortener.urlservice.service;

import io.urlshortener.urlservice.generator.ManagementToken;
import io.urlshortener.urlservice.generator.ManagementTokenGenerator;
import io.urlshortener.urlservice.generator.ShortCodeGenerator;
import io.urlshortener.urlservice.model.domainObject.ShortLink;
import io.urlshortener.urlservice.model.result.CreateLinkResult;
import io.urlshortener.urlservice.repository.LinkRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.Instant;

/**
 * Orchestrates creation of a new short link: resolving its short code, generating a management
 * token, and persisting the result.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CreateLinkService {

	/**
	 * Persists the created link.
	 */
	private final LinkRepository linkRepository;

	/**
	 * Generates a random short code when the caller doesn't supply a custom alias.
	 */
	private final ShortCodeGenerator shortCodeGenerator;

	/**
	 * Generates the link's management token.
	 */
	private final ManagementTokenGenerator managementTokenGenerator;

	/**
	 * Creates a new short link: resolves its short code (custom alias or generated), generates a
	 * management token, stamps the creation time, and persists the result.
	 *
	 * @param link the link to create; its short code, management token hash, and created-at
	 *             timestamp are populated by this method.
	 * @return the persisted link together with its one-time raw management token.
	 * @throws io.urlshortener.urlservice.exception.AliasAlreadyExistsException if the resolved short code is already in use.
	 */
	public CreateLinkResult createLink(final ShortLink link) {
		final String shortCode;

		if (StringUtils.hasText(link.getCustomAlias())) {
			shortCode = link.getCustomAlias();
		} else {
			shortCode = shortCodeGenerator.generateShortCode();
		}

		final ManagementToken managementToken = managementTokenGenerator.generate();
		final Instant createdAt = Instant.now();

		link.setShortCode(shortCode);
		link.setManagementTokenHash(managementToken.tokenHash());
		link.setCreatedAt(createdAt);

		final ShortLink savedLink = linkRepository.save(link);

		return new CreateLinkResult(savedLink, managementToken.rawToken());
	}

}
