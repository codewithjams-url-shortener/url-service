package io.urlshortener.urlservice.repository;

import io.urlshortener.urlservice.model.domainObject.LinkPatch;
import lombok.experimental.UtilityClass;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

import java.util.*;

/**
 * General-purpose helper for {@link DynamoDbLinkRepository}, holding request-construction logic that doesn't belong
 * on the repository interface itself.
 */
@UtilityClass
public class DynamoDbLinkRepositoryHelper {

	/**
	 * Builds the {@code UpdateExpression} for a patch: a {@code SET} clause for each field being changed to a new
	 * value, and a {@code REMOVE} clause for {@code expiresAt} if it's being explicitly cleared. A field left unset in
	 * the patch has no clause at all, and is untouched.
	 *
	 * @param patch the fields to change.
	 * @return the DynamoDB update expression, e.g. {@code "SET longUrl = :longUrl REMOVE expiresAt"}.
	 */
	public String createLinkUpdateExpression(final LinkPatch patch) {
		final List<String> setClauses = new ArrayList<>();
		final List<String> removeClauses = new ArrayList<>();

		// Populate clauses based on presence of fields.
		if (Objects.nonNull(patch.longUrl())) {
			setClauses.add("longUrl = :longUrl");
		}
		if (patch.expiresAtProvided()) {
			if (Objects.isNull(patch.expiresAt())) {
				removeClauses.add("expiresAt");
			} else {
				setClauses.add("expiresAt = :expiresAt");
			}
		}

		// Generate expression from Clauses.
		final StringBuilder updateExpression = new StringBuilder();
		if (!setClauses.isEmpty()) {
			updateExpression.append("SET ").append(String.join(", ", setClauses));
		}
		if (!removeClauses.isEmpty()) {
			if (!updateExpression.isEmpty()) {
				updateExpression.append(' ');
			}
			updateExpression.append("REMOVE ").append(String.join(", ", removeClauses));
		}

		return updateExpression.toString();
	}

	/**
	 * Builds the {@code ExpressionAttributeValues} matching {@link #createLinkUpdateExpression}'s placeholders, plus
	 * the {@code :expectedHash} value used by the update's condition expression.
	 *
	 * @param patch               the fields to change.
	 * @param managementTokenHash the expected management token hash for the condition expression.
	 * @return the expression attribute values for the {@code UpdateItem} request.
	 */
	public Map<String, AttributeValue> createLinkUpdateExpressionValue(final LinkPatch patch,
																	   final String managementTokenHash) {
		final Map<String, AttributeValue> expressionValues = new HashMap<>();

		// Populate expression values based on presence of fields.
		if (Objects.nonNull(patch.longUrl())) {
			expressionValues.put(":longUrl", AttributeValue.builder().s(patch.longUrl()).build());
		}
		if (patch.expiresAtProvided() && Objects.nonNull(patch.expiresAt())) {
			final long expiresAtEpoch = patch.expiresAt().getEpochSecond();
			expressionValues.put(":expiresAt", AttributeValue.builder().n(String.valueOf(expiresAtEpoch)).build());
		}
		expressionValues.put(":expectedHash", AttributeValue.builder().s(managementTokenHash).build());

		return expressionValues;
	}

}
