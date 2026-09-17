package io.urlshortener.urlservice.mapper.dbMapper;

import io.urlshortener.linkscontract.Link;
import io.urlshortener.urlservice.exception.InvalidConversionInputException;
import io.urlshortener.urlservice.model.domainObject.ShortLink;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Optional;

/**
 * Converts between the {@link ShortLink} domain object and the {@link Link} DAO, including the
 * epoch-milliseconds ({@code createdAt}) / epoch-seconds ({@code expiresAt}) unit conversions the
 * two representations require.
 */
@Slf4j
@Component
public class LinkDbMapper implements DbMapper<ShortLink, Link> {

	/**
	 * Converts a {@link ShortLink} into its {@link Link} DAO representation.
	 *
	 * @param domain the domain instance to convert.
	 * @return the equivalent {@link Link}. Note that {@code customAlias} has no DAO equivalent and is dropped.
	 * @throws InvalidConversionInputException if {@code domain} is {@code null}.
	 */
	@Override
	public Link toEntity(final ShortLink domain) {
		if (domain == null) {
			log.atError()
					.addKeyValue("reason", "ShortLink not provided")
					.log("Domain to DAO Model conversion failed");
			throw new InvalidConversionInputException("Domain Model not provided for conversion");
		}

		final long createdAt = domain.getCreatedAt().toEpochMilli();
		final Long expiresAt = Optional.ofNullable(domain.getExpiresAt())
				.map(Instant::getEpochSecond)
				.orElse(null);

		return new Link(
				domain.getShortCode(), domain.getLongUrl(), domain.getOwnerId(), createdAt, expiresAt,
				domain.getManagementTokenHash(), domain.getStatus()
		);
	}

	/**
	 * Converts a {@link Link} DAO into its {@link ShortLink} domain representation.
	 *
	 * @param entity the DAO instance to convert.
	 * @return the equivalent {@link ShortLink}.
	 * @throws InvalidConversionInputException if {@code entity} is {@code null}.
	 */
	@Override
	public ShortLink toDomain(final Link entity) {
		if (entity == null) {
			log.atError()
					.addKeyValue("reason", "Link not provided")
					.log("DAO to Domain Model conversion failed");
			throw new InvalidConversionInputException("DAO Model not provided for conversion");
		}

		final Instant createdAt = Instant.ofEpochMilli(entity.createdAt());
		final Instant expiresAt = Optional.ofNullable(entity.expiresAt())
				.map(Instant::ofEpochSecond)
				.orElse(null);

		return ShortLink.builder()
				.shortCode(entity.shortCode())
				.longUrl(entity.longUrl())
				.ownerId(entity.ownerId())
				.createdAt(createdAt)
				.expiresAt(expiresAt)
				.managementTokenHash(entity.managementTokenHash())
				.status(entity.status())
				.build();
	}

}
