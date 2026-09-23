package io.urlshortener.urlservice.constant;

/**
 * Constants shared by the token/short-code generators.
 */
public class GeneratorConstants {

	/**
	 * Length, in bytes, of a generated raw management token (256 bits).
	 */
	public static final int TOKEN_BYTE_LENGTH = 32; // 256 Bits

	/**
	 * Number of characters in the base62 alphabet used for generated short codes.
	 */
	public static final int CHARACTER_BOUND = 62; // 62 = 26 a-z + 26 A-Z + 10 0-9

	public static final String QR_CODE_FILE_FORMAT = "PNG";

	/**
	 * Not instantiable.
	 */
	private GeneratorConstants() {
	}

}
