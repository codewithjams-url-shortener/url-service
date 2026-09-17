package io.urlshortener.urlservice.mapper.dbMapper;

/**
 * Converts between a domain object and its DAO/entity representation for repository persistence.
 *
 * @param <T> the domain type.
 * @param <E> the DAO/entity type.
 */
public interface DbMapper<T, E> {

	/**
	 * Converts a domain instance into its DAO/entity representation.
	 *
	 * @param domain the domain instance to convert.
	 * @return the equivalent DAO/entity instance.
	 */
	E toEntity(final T domain);

	/**
	 * Converts a DAO/entity instance into its domain representation.
	 *
	 * @param entity the DAO/entity instance to convert.
	 * @return the equivalent domain instance.
	 */
	T toDomain(final E entity);

}
