package io.urlshortener.urlservice.controller;

import io.urlshortener.urlservice.mapper.requestMapper.CreateLinkRequestMapper;
import io.urlshortener.urlservice.mapper.requestMapper.PatchLinkRequestMapper;
import io.urlshortener.urlservice.mapper.responseMapper.CreateLinkResponseMapper;
import io.urlshortener.urlservice.mapper.responseMapper.GetLinkResponseMapper;
import io.urlshortener.urlservice.model.dataTransferObject.CreateLinkRequest;
import io.urlshortener.urlservice.model.dataTransferObject.CreateLinkResponse;
import io.urlshortener.urlservice.model.dataTransferObject.GetLinkResponse;
import io.urlshortener.urlservice.model.dataTransferObject.PatchLinkRequest;
import io.urlshortener.urlservice.model.domainObject.LinkPatch;
import io.urlshortener.urlservice.model.domainObject.ShortLink;
import io.urlshortener.urlservice.model.result.CreateLinkResult;
import io.urlshortener.urlservice.service.CreateLinkService;
import io.urlshortener.urlservice.service.DeleteLinkService;
import io.urlshortener.urlservice.service.GetLinkService;
import io.urlshortener.urlservice.service.UpdateLinkService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller implementing the {@code /links} API contract defined in {@code links.yaml}.
 */
@Slf4j
@RestController
@RequiredArgsConstructor
public class LinksController implements LinksApi {

	/**
	 * Orchestrates the actual link-creation business logic.
	 */
	private final CreateLinkService createLinkService;

	/**
	 * Orchestrates the link-lookup business logic, including expiry checking.
	 */
	private final GetLinkService getLinkService;

	/**
	 * Orchestrates the link-edit business logic, including management-token verification.
	 */
	private final UpdateLinkService updateLinkService;

	/**
	 * Orchestrates the link-deletion business logic, including management-token verification.
	 */
	private final DeleteLinkService deleteLinkService;

	/**
	 * Converts the incoming request DTO into a domain object.
	 */
	private final CreateLinkRequestMapper createLinkRequestMapper;

	/**
	 * Converts the service result into the outgoing response DTO.
	 */
	private final CreateLinkResponseMapper createLinkResponseMapper;

	/**
	 * Converts the looked-up link into the outgoing response DTO.
	 */
	private final GetLinkResponseMapper getLinkResponseMapper;

	/**
	 * Converts the incoming patch request DTO into a domain object.
	 */
	private final PatchLinkRequestMapper patchLinkRequestMapper;

	/**
	 * Creates a new short link.
	 *
	 * @param request the incoming create-link request body.
	 * @return {@code 201 Created} with the created link's short code, short URL, and one-time management token.
	 */
	@Override
	public ResponseEntity<CreateLinkResponse> createLink(final CreateLinkRequest request) {
		final ShortLink link = createLinkRequestMapper.toDomain(request);
		final CreateLinkResult result = createLinkService.createLink(link);
		final CreateLinkResponse response = createLinkResponseMapper.toDto(result);
		return ResponseEntity.status(HttpStatus.CREATED).body(response);
	}

	/**
	 * Fetches public metadata for an existing short link.
	 *
	 * @param shortCode the short code to look up.
	 * @return {@code 200 OK} with the link's public metadata.
	 */
	@Override
	public ResponseEntity<GetLinkResponse> getLink(final String shortCode) {
		final ShortLink link = getLinkService.getLink(shortCode);
		final GetLinkResponse response = getLinkResponseMapper.toDto(link);
		return ResponseEntity.ok(response);
	}

	/**
	 * Applies a partial edit to an existing short link.
	 *
	 * @param shortCode          the short code of the link to update.
	 * @param patchLinkRequest   the fields to change.
	 * @param xManagementToken   the caller-presented management token; may be {@code null} if the
	 *                           header was omitted.
	 * @return {@code 200 OK} with the link's public metadata after the update.
	 */
	@Override
	public ResponseEntity<GetLinkResponse> updateLink(final String shortCode, final PatchLinkRequest patchLinkRequest,
													  final String xManagementToken) {
		final LinkPatch patch = patchLinkRequestMapper.toDomain(patchLinkRequest);
		final ShortLink link = updateLinkService.updateLink(shortCode, xManagementToken, patch);
		final GetLinkResponse response = getLinkResponseMapper.toDto(link);
		return ResponseEntity.ok(response);
	}

	/**
	 * Deletes an existing short link.
	 *
	 * @param shortCode        the short code of the link to delete.
	 * @param xManagementToken the caller-presented management token; may be {@code null} if the
	 *                         header was omitted.
	 * @return {@code 204 No Content} once the link has been deleted.
	 */
	@Override
	public ResponseEntity<Void> deleteLink(final String shortCode, final String xManagementToken) {
		deleteLinkService.deleteLink(shortCode, xManagementToken);
		return ResponseEntity.noContent().build();
	}

}
