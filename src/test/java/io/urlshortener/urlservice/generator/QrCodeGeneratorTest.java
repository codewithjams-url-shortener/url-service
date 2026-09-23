package io.urlshortener.urlservice.generator;

import com.google.zxing.BinaryBitmap;
import com.google.zxing.LuminanceSource;
import com.google.zxing.Result;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.QRCodeReader;
import com.google.zxing.qrcode.QRCodeWriter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class QrCodeGeneratorTest {

	private final QrCodeGenerator qrCodeGenerator = new QrCodeGenerator(new QRCodeWriter());

	@Mock
	private QRCodeWriter failingQrCodeWriter;

	@Test
	void generate_shouldReturnAValidPngImage_whenGivenContentAndSize() throws Exception {
		// Arrange
		// (content/size literals used directly below)

		// Act
		final byte[] pngBytes = qrCodeGenerator.generate("https://shortify.com/abc1234", 300);

		// Assert
		final BufferedImage image = ImageIO.read(new ByteArrayInputStream(pngBytes));
		assertThat(image).isNotNull();
	}

	@Test
	void generate_shouldReturnAnImageOfTheRequestedSize_whenGivenASize() throws Exception {
		// Arrange
		// (content/size literals used directly below)

		// Act
		final byte[] pngBytes = qrCodeGenerator.generate("https://shortify.com/abc1234", 300);

		// Assert
		final BufferedImage image = ImageIO.read(new ByteArrayInputStream(pngBytes));
		assertThat(image.getWidth()).isEqualTo(300);
		assertThat(image.getHeight()).isEqualTo(300);
	}

	@Test
	void generate_shouldEncodeTheGivenContent_whenDecodedBack() throws Exception {
		// Arrange
		final String content = "https://shortify.com/abc1234";

		// Act
		final byte[] pngBytes = qrCodeGenerator.generate(content, 300);

		// Assert
		assertThat(decode(pngBytes)).isEqualTo(content);
	}

	@Test
	void generate_shouldEncodeDifferentContentDistinctly_whenGivenTwoDifferentUrls() throws Exception {
		// Arrange
		final String firstContent = "https://shortify.com/abc1234";
		final String secondContent = "https://shortify.com/xyz9876";

		// Act
		final byte[] firstPngBytes = qrCodeGenerator.generate(firstContent, 300);
		final byte[] secondPngBytes = qrCodeGenerator.generate(secondContent, 300);

		// Assert
		assertThat(decode(firstPngBytes)).isEqualTo(firstContent);
		assertThat(decode(secondPngBytes)).isEqualTo(secondContent);
	}

	@Test
	void generate_shouldThrowIllegalStateException_whenEncodingFails() throws WriterException {
		// Arrange
		given(failingQrCodeWriter.encode(any(), any(), anyInt(), anyInt()))
				.willThrow(new WriterException("encoding failed"));
		final QrCodeGenerator generatorWithFailingWriter = new QrCodeGenerator(failingQrCodeWriter);

		// Act & Assert
		assertThatThrownBy(() -> generatorWithFailingWriter.generate("https://shortify.com/abc1234", 300))
				.isInstanceOf(IllegalStateException.class)
				.hasCauseInstanceOf(WriterException.class);
	}

	@Test
	void generate_shouldThrowIllegalStateException_whenWritingTheImageFails() {
		// Arrange
		try (MockedStatic<MatrixToImageWriter> mockedWriter = Mockito.mockStatic(MatrixToImageWriter.class)) {
			mockedWriter.when(() -> MatrixToImageWriter.writeToStream(any(), any(), any()))
					.thenThrow(new IOException("write failed"));

			// Act & Assert
			assertThatThrownBy(() -> qrCodeGenerator.generate("https://shortify.com/abc1234", 300))
					.isInstanceOf(IllegalStateException.class);
		}
	}

	private String decode(final byte[] pngBytes) throws Exception {
		final BufferedImage image = ImageIO.read(new ByteArrayInputStream(pngBytes));
		final LuminanceSource source = new BufferedImageLuminanceSource(image);
		final BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));
		final Result result = new QRCodeReader().decode(bitmap);
		return result.getText();
	}

}
