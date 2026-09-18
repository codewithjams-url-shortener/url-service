package io.urlshortener.urlservice.service;

import io.urlshortener.urlservice.exception.ShortLinkNotFoundException;
import io.urlshortener.urlservice.model.domainObject.ShortLink;
import io.urlshortener.urlservice.repository.LinkRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Objects;

/**
 * Orchestrates lookup of an existing short link: resolving it by short code and treating an expired link the same as a
 * missing one.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GetLinkService {

	/**
	 * Looks up the persisted link.
	 */
	private final LinkRepository linkRepository;

	/**
	 * Looks up a link by its short code.
	 *
	 * @param shortCode the short code to look up.
	 * @return the matching link.
	 * @throws ShortLinkNotFoundException if no link exists for that short code, or it exists but
	 *                                    has expired.
	 */
	public ShortLink getLink(final String shortCode) {
		final ShortLink shortLink = linkRepository.findByShortCode(shortCode)
				.orElseThrow(
						() -> {
							log.atInfo()
									.addKeyValue("shortCode", shortCode)
									.log("Short Code not found");
							return new ShortLinkNotFoundException(shortCode);
						}
				);
		final Instant expiresAt = shortLink.getExpiresAt();
		if (Objects.isNull(expiresAt)) {
			log.atDebug()
					.addKeyValue("shortCode", shortCode)
					.log("Non-Expirable Short Link found");
			return shortLink;
		}
		final Instant now = Instant.now();
		if (now.isAfter(expiresAt)) {
			log.atInfo()
					.addKeyValue("shortCode", shortCode)
					.log("Short Code is expired");
			throw new ShortLinkNotFoundException(shortCode);
		}
		log.atDebug()
				.addKeyValue("shortCode", shortCode)
				.log("Expirable Short Link found");
		return shortLink;
	}

}
