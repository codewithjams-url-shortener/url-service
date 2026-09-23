package io.urlshortener.urlservice.integrationtest.constant;

/**
 * JSON field names and header names used when exercising the {@code /links} API contract.
 */
public class LinksApiConstants {

	/**
	 * Name of the {@code shortCode} field in create/get/patch responses.
	 */
	public static final String FIELD_SHORT_CODE = "shortCode";

	/**
	 * Name of the {@code longUrl} field in create/get/patch requests and responses.
	 */
	public static final String FIELD_LONG_URL = "longUrl";

	/**
	 * Name of the {@code managementToken} field in the create response.
	 */
	public static final String FIELD_MANAGEMENT_TOKEN = "managementToken";

	/**
	 * Name of the header carrying the management token on patch/delete requests.
	 */
	public static final String HEADER_MANAGEMENT_TOKEN = "X-Management-Token";

	/**
	 * Not instantiable.
	 */
	private LinksApiConstants() {
	}

}
