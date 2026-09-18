package io.urlshortener.urlservice.repository;

import io.urlshortener.linkscontract.Link;
import io.urlshortener.urlservice.constant.AwsConstants;
import io.urlshortener.urlservice.exception.AliasAlreadyExistsException;
import io.urlshortener.urlservice.exception.ManagementTokenMismatchException;
import io.urlshortener.urlservice.exception.ShortLinkNotFoundException;
import io.urlshortener.urlservice.mapper.dbMapper.LinkAttributeMapper;
import io.urlshortener.urlservice.mapper.dbMapper.LinkDbMapper;
import io.urlshortener.urlservice.model.domainObject.LinkPatch;
import io.urlshortener.urlservice.model.domainObject.ShortLink;
import io.urlshortener.urlservice.property.AwsProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.*;

import java.util.Map;
import java.util.Optional;

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

	/**
	 * Looks up a link by its short code.
	 *
	 * @param shortCode the short code to look up.
	 * @return the matching link, or {@link Optional#empty()} if no item exists for that short code.
	 */
	@Override
	public Optional<ShortLink> findByShortCode(final String shortCode) {
		final GetItemRequest readRequest = GetItemRequest.builder()
				.tableName(awsProperties.getDynamoDb().getTables().get(AwsConstants.TABLE_LINKS))
				.key(linkAttributeMapper.createKeyAttribute(shortCode))
				.build();
		final GetItemResponse response = dynamoDbClient.getItem(readRequest);
		if (!response.hasItem()) {
			return Optional.empty();
		}
		final Link link = linkAttributeMapper.toObject(response.item());
		return Optional.of(linkDbMapper.toDomain(link));
	}

	/**
	 * Applies a partial edit to an existing link via a conditional {@code UpdateItem}, atomically checking both that
	 * the item exists and that its management token hash matches.
	 *
	 * @param shortCode           the short code of the link to update.
	 * @param managementTokenHash the caller-presented token's hash, checked against the link's own.
	 * @param patch               the fields to change; a field left unset is not modified.
	 * @return the link after the update has been applied.
	 * @throws ShortLinkNotFoundException       if no item exists for that short code.
	 * @throws ManagementTokenMismatchException if the item exists but {@code managementTokenHash}
	 *                                          does not match its stored hash.
	 */
	@Override
	public ShortLink update(final String shortCode, final String managementTokenHash, final LinkPatch patch)
			throws ShortLinkNotFoundException, ManagementTokenMismatchException {
		final String updateExpression = DynamoDbLinkRepositoryHelper.createLinkUpdateExpression(patch);
		final Map<String, AttributeValue> expressionAttributeValues =
				DynamoDbLinkRepositoryHelper.createLinkUpdateExpressionValue(patch, managementTokenHash);
		final UpdateItemRequest updateRequest = UpdateItemRequest.builder()
				.tableName(awsProperties.getDynamoDb().getTables().get(AwsConstants.TABLE_LINKS))
				.key(linkAttributeMapper.createKeyAttribute(shortCode))
				.updateExpression(updateExpression)
				.conditionExpression("attribute_exists(shortCode) AND managementTokenHash = :expectedHash")
				.expressionAttributeValues(expressionAttributeValues)
				.returnValues(ReturnValue.ALL_NEW)
				.build();
		final UpdateItemResponse response;
		try {
			response = dynamoDbClient.updateItem(updateRequest);
		} catch (ConditionalCheckFailedException e) {
			log.atError()
					.addKeyValue("reason", "One of the condition among 'attribute_exists(shortCode)' and "
							+ "'managementTokenHash = :expectedHash' has failed")
					.addKeyValue("shortCode", shortCode)
					.setCause(e)
					.log("Error while updating Link");
			// There's a small theoretical TOCTOU window between the failed write and this read.
			if (findByShortCode(shortCode).isPresent()) {
				throw new ManagementTokenMismatchException(shortCode);
			}
			throw new ShortLinkNotFoundException(shortCode);
		}
		final Link link = linkAttributeMapper.toObject(response.attributes());
		return linkDbMapper.toDomain(link);
	}

}
