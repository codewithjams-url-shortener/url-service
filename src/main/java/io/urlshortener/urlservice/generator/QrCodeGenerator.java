package io.urlshortener.urlservice.generator;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import io.urlshortener.urlservice.constant.GeneratorConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * Generates QR code images encoding arbitrary text content.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class QrCodeGenerator {

	private final QRCodeWriter qrCodeWriter;

	/**
	 * Generates a square PNG QR code encoding the given content.
	 *
	 * @param content the text to encode (e.g. a short URL).
	 * @param size    the width and height of the generated image, in pixels.
	 * @return the PNG-encoded QR code image bytes.
	 * @throws IllegalStateException if encoding or image writing fails — not expected in practice,
	 *                               since {@code content} is always this service's own well-formed
	 *                               output, never raw external input.
	 */
	public byte[] generate(final String content, final int size) {
		final BitMatrix matrix;

		try {
			matrix = qrCodeWriter.encode(content, BarcodeFormat.QR_CODE, size, size);
			log.atDebug()
					.addKeyValue("content", content)
					.addKeyValue("size", size)
					.log("Finished QR-Code encoding");
		} catch (WriterException e) {
			log.atError()
					.setCause(e)
					.addKeyValue("content", content)
					.addKeyValue("size", size)
					.log("Error while encoding the content of the QR-Code");
			throw new IllegalStateException(e);
		}
		final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		try {
			MatrixToImageWriter.writeToStream(matrix, GeneratorConstants.QR_CODE_FILE_FORMAT, outputStream);
			log.atDebug()
					.addKeyValue("content", content)
					.addKeyValue("size", size)
					.log("Finished writing encoded QR-Code to output stream");
		} catch (IOException e) {
			log.atError()
					.setCause(e)
					.addKeyValue("content", content)
					.addKeyValue("size", size)
					.log("Error writing the QR-Code output to stream");
			throw new IllegalStateException(e);
		}
		return outputStream.toByteArray();
	}

}
