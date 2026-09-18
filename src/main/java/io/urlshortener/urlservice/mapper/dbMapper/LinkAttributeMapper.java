package io.urlshortener.urlservice.mapper.dbMapper;

import io.urlshortener.linkscontract.Link;
import io.urlshortener.urlservice.exception.CorruptedDataException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Converts {@link Link} DAOs to and from the raw {@link AttributeValue} map format used by the
 * DynamoDB SDK.
 */
@Slf4j
@Component
public class LinkAttributeMapper implements AttributeMapper<Link> {

	/**
	 * Converts a {@link Link} into a DynamoDB attribute-value map, omitting optional fields that
	 * are {@code null} rather than writing an explicit null attribute.
	 *
	 * @param link the DAO instance to convert.
	 * @return the equivalent DynamoDB attribute-value map.
	 */
	@Override
	public Map<String, AttributeValue> toAttributeValue(final Link link) {
		final Map<String, AttributeValue> map = new HashMap<>();
		map.put("shortCode", AttributeValue.builder().s(link.shortCode()).build());
		map.put("longUrl", AttributeValue.builder().s(link.longUrl()).build());
		if (Objects.nonNull(link.ownerId())) {
			map.put("ownerId", AttributeValue.builder().s(link.ownerId()).build());
		} else {
			log.atDebug()
					.addKeyValue("field", "ownerId")
					.log("Value not provided when converting to Attribute Value");
		}
		map.put("createdAt", AttributeValue.builder().n(String.valueOf(link.createdAt())).build());
		if (Objects.nonNull(link.expiresAt())) {
			map.put("expiresAt", AttributeValue.builder().n(String.valueOf(link.expiresAt())).build());
		} else {
			log.atDebug()
					.addKeyValue("field", "expiresAt")
					.log("Value not provided when converting to Attribute Value");
		}
		map.put("managementTokenHash", AttributeValue.builder().s(link.managementTokenHash()).build());
		if (Objects.nonNull(link.status())) {
			map.put("status", AttributeValue.builder().s(link.status()).build());
		} else {
			log.atDebug()
					.addKeyValue("field", "status")
					.log("Value not provided when converting to Attribute Value");
		}
		return map;
	}

	/**
	 * Converts a raw DynamoDB item back into a {@link Link}.
	 *
	 * @param map the raw DynamoDB item.
	 * @return the equivalent {@link Link}.
	 * @throws CorruptedDataException if the required {@code createdAt} attribute is missing.
	 */
	@Override
	public Link toObject(final Map<String, AttributeValue> map) {
		final String shortCode = map.containsKey("shortCode") ? map.get("shortCode").s() : null;
		final String longUrl = map.containsKey("longUrl") ? map.get("longUrl").s() : null;
		final String ownerId = map.containsKey("ownerId") ? map.get("ownerId").s() : null;

		if (!map.containsKey("createdAt")) {
			log.atError()
					.addKeyValue("field", "createdAt")
					.log("Required value missing from DB");
			throw new CorruptedDataException("Expecting value at createdAt, value is absent instead");
		}

		final long createdAt = Long.parseLong(map.get("createdAt").n());
		final Long expiresAt = map.containsKey("expiresAt") ? Long.parseLong(map.get("expiresAt").n()) : null;
		final String managementTokenHash = map.containsKey("managementTokenHash") ? map.get("managementTokenHash").s() :
				null;
		final String status = map.containsKey("status") ? map.get("status").s() : null;
		return new Link(shortCode, longUrl, ownerId, createdAt, expiresAt, managementTokenHash, status);
	}

	/**
	 * Builds the DynamoDB key attribute map identifying a link by its short code (the table's
	 * partition key), for use in a {@code GetItem} request.
	 *
	 * @param shortCode the short code to build a key for.
	 * @return a single-entry attribute-value map suitable as a DynamoDB item key.
	 */
	public Map<String, AttributeValue> createKeyAttribute(final String shortCode) {
		final Map<String, AttributeValue> map = new HashMap<>();
		map.put("shortCode", AttributeValue.builder().s(shortCode).build());
		return map;
	}

}
