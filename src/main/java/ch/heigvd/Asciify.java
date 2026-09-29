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

        out.write("BMP ok, pixel offset = " + pixelOffset);
        out.write(System.lineSeparator());

    }
}
