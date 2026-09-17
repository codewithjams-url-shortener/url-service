package io.urlshortener.urlservice.mapper.dbMapper;

import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

import java.util.Map;

/**
 * Converts a DAO type to and from a DynamoDB item's raw {@link AttributeValue} map.
 *
 * @param <T> the DAO type.
 */
public interface AttributeMapper<T> {

	/**
	 * Converts a DAO instance into a DynamoDB attribute-value map suitable for a put/update request.
	 *
	 * @param item the DAO instance to convert.
	 * @return the equivalent DynamoDB attribute-value map.
	 */
	Map<String, AttributeValue> toAttributeValue(final T item);

	/**
	 * Converts a raw DynamoDB item back into a DAO instance.
	 *
	 * @param attributeValueMap the raw DynamoDB item.
	 * @return the equivalent DAO instance.
	 */
	T toObject(final Map<String, AttributeValue> attributeValueMap);

}
