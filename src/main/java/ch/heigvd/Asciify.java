package ch.heigvd;

import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class Asciify {
    private Asciify() {
    }

    public static void process(InputStream in, Writer out, Integer width, boolean invert)
            throws IOException {

        byte[] fileHeader = in.readNBytes(14);

        if (fileHeader.length < 14) {
            throw new IOException("File too short to be a BMP:");
        }

        if (fileHeader[0] != 'B' || fileHeader[1] != 'M') {
            throw new IOException("File is not a BMP file:");
        }

        int pixelOffset = ByteBuffer.wrap(fileHeader, 10, 4)
                .order(ByteOrder.LITTLE_ENDIAN)
                .getInt();

        byte[] dibHeader = in.readNBytes(40);

        if (dibHeader.length < 40) {
            throw new IOException("File too short to be a BMP DIB header");
        }

        int imgWidth = ByteBuffer.wrap(dibHeader, 4, 4)
                .order(ByteOrder.LITTLE_ENDIAN)
                .getInt();

        int imgHeight = ByteBuffer.wrap(dibHeader, 8, 4)
                .order(ByteOrder.LITTLE_ENDIAN)
                .getInt();

        int bitsPerPixel = ByteBuffer.wrap(dibHeader, 14, 2)
                .order(ByteOrder.LITTLE_ENDIAN)
                .getShort() & 0xFFFF;

        if (bitsPerPixel != 24) {
            throw new IOException("Only 24-bit BMP is supported, got: " + bitsPerPixel);
        }

        out.write(
                "BMP ok, " + imgWidth + " X " + imgHeight + ", bpp= " + bitsPerPixel + ", pixelOffset= " + pixelOffset);
        out.write(System.lineSeparator());

    }
}
