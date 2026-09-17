package io.urlshortener.urlservice.mapper.requestMapper;

/**
 * Converts an incoming request DTO to its domain object, at the controller/service boundary.
 *
 * @param <D> the request DTO type.
 * @param <T> the domain type.
 */
public interface RequestMapper<D, T> {

	/**
	 * Converts a request DTO into its domain representation.
	 *
	 * @param dto the request DTO to convert.
	 * @return the equivalent domain instance.
	 */
	T toDomain(final D dto);

}
