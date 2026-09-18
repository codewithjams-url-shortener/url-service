package io.urlshortener.urlservice.controller;

import io.urlshortener.urlservice.mapper.requestMapper.CreateLinkRequestMapper;
import io.urlshortener.urlservice.mapper.responseMapper.CreateLinkResponseMapper;
import io.urlshortener.urlservice.mapper.responseMapper.GetLinkResponseMapper;
import io.urlshortener.urlservice.model.dataTransferObject.CreateLinkRequest;
import io.urlshortener.urlservice.model.dataTransferObject.CreateLinkResponse;
import io.urlshortener.urlservice.model.dataTransferObject.GetLinkResponse;
import io.urlshortener.urlservice.model.domainObject.ShortLink;
import io.urlshortener.urlservice.model.result.CreateLinkResult;
import io.urlshortener.urlservice.service.CreateLinkService;
import io.urlshortener.urlservice.service.GetLinkService;
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

}
