package io.urlshortener.urlservice.model.domainObject;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

/**
 * Domain object representing a shortened link, independent of both the OpenAPI-generated DTOs and
 * the DynamoDB {@code Link} DAO.
 */
@Getter
@Setter
@Builder
@AllArgsConstructor
public class ShortLink {

	/**
	 * The link's short code (either caller-supplied via {@link #customAlias} or generated).
	 */
	private String shortCode;

	/**
	 * The original, full-length URL this short link redirects to.
	 */
	private String longUrl;

	/**
	 * The caller-requested alias to use as the short code, if any.
	 */
	private String customAlias;

	/**
	 * Identifier of the owning user/account, if the link was created by an authenticated caller.
	 */
	private String ownerId;

	/**
	 * When this link was created.
	 */
	private Instant createdAt;

	/**
	 * When this link expires and should no longer resolve, if set.
	 */
	private Instant expiresAt;

	/**
	 * SHA-256 hash of the management token authorizing updates/deletes of this link.
	 */
	private String managementTokenHash;

	/**
	 * The link's current lifecycle status.
	 */
	private String status;

}
