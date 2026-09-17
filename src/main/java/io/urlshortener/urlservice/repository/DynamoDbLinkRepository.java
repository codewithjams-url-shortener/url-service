package io.urlshortener.urlservice.repository;

import io.urlshortener.linkscontract.Link;
import io.urlshortener.urlservice.constant.AwsConstants;
import io.urlshortener.urlservice.exception.AliasAlreadyExistsException;
import io.urlshortener.urlservice.mapper.dbMapper.LinkAttributeMapper;
import io.urlshortener.urlservice.mapper.dbMapper.LinkDbMapper;
import io.urlshortener.urlservice.model.domainObject.ShortLink;
import io.urlshortener.urlservice.property.AwsProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.ConditionalCheckFailedException;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;

/**
 * {@link LinkRepository} implementation backed by DynamoDB, using the raw SDK client and a conditional write to enforce
 * short-code uniqueness natively.
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class DynamoDbLinkRepository implements LinkRepository {

	/**
	 * Raw DynamoDB SDK client used to issue write.
	 */
	private final DynamoDbClient dynamoDbClient;

	/**
	 * Source of the configured DynamoDB table name.
	 */
	private final AwsProperties awsProperties;

	/**
	 * Converts between the {@link ShortLink} domain object and the {@code Link} DAO.
	 */
	private final LinkDbMapper linkDbMapper;

	/**
	 * Converts the {@code Link} DAO to and from a DynamoDB attribute-value map.
	 */
	private final LinkAttributeMapper linkAttributeMapper;

	/**
	 * Persists a new link, relying on a conditional write to enforce short-code uniqueness.
	 *
	 * @param shortLink the link to persist.
	 * @return the persisted link.
	 * @throws AliasAlreadyExistsException if an item with the same short code already exists.
	 */
	@Override
	public ShortLink save(final ShortLink shortLink) throws AliasAlreadyExistsException {
		final Link link = linkDbMapper.toEntity(shortLink);
		final PutItemRequest writeRequest = PutItemRequest.builder()
				.tableName(awsProperties.getDynamoDb().getTables().get(AwsConstants.TABLE_LINKS))
				.item(linkAttributeMapper.toAttributeValue(link))
				.conditionExpression("attribute_not_exists(shortCode)")
				.build();
		try {
			dynamoDbClient.putItem(writeRequest);
		} catch (ConditionalCheckFailedException e) {
			log.atError()
					.addKeyValue("reason", "Write Condition: 'attribute_not_exists' has failed for the shortCode")
					.addKeyValue("shortCode", link.shortCode())
					.setCause(e)
					.log("Error while saving Link");
			throw new AliasAlreadyExistsException(e, link.shortCode());
		}
		log.atInfo()
				.addKeyValue("shortCode", link.shortCode())
				.addKeyValue("longUrl", link.longUrl())
				.log("Link saved successfully");
		return linkDbMapper.toDomain(link);
	}

}
