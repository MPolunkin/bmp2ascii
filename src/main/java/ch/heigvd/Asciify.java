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

        // BMP file header is always 14 bytes
        byte[] fileHeader = in.readNBytes(14);

        // checks to see if BMP
        if (fileHeader.length < 14) {
            throw new IOException("File too short to be a BMP");
        }

        if (fileHeader[0] != 'B' || fileHeader[1] != 'M') {
            throw new IOException("File is not a BMP file (missing BM signature).");
        }

        // Bytes 10-13 where pixel data starts
        int pixelOffset = ByteBuffer.wrap(fileHeader, 10, 4)
                .order(ByteOrder.LITTLE_ENDIAN)
                .getInt();

        // DIB header = width, height, bits/pixel
        byte[] dibHeader = in.readNBytes(40);

        // check
        if (dibHeader.length < 40) {
            throw new IOException("File too short to be a BMP DIB header");
        }

        // width
        int imgWidth = ByteBuffer.wrap(dibHeader, 4, 4)
                .order(ByteOrder.LITTLE_ENDIAN)
                .getInt();

        // height. !can be negative (top down)!
        int imgHeight = ByteBuffer.wrap(dibHeader, 8, 4)
                .order(ByteOrder.LITTLE_ENDIAN)
                .getInt();

        // bits/pixel reads 2 bytes and transforms into unsigned val (2beSafe)
        int bitsPerPixel = ByteBuffer.wrap(dibHeader, 14, 2)
                .order(ByteOrder.LITTLE_ENDIAN)
                .getShort() & 0xFFFF;

        // we only handle 24 bit BMPs
        if (bitsPerPixel != 24) {
            throw new IOException("Only 24-bit BMP is supported, got: " + bitsPerPixel);
        }

        // areadyRead = how far we are in the file, skip = how many more bytes until the
        // pixels start
        int alreadyRead = 14 + 40;
        int skip = pixelOffset - alreadyRead;
        if (skip < 0) {
            throw new IOException("Invalid BMP pixel offset");
        }

        if (skip > 0) {
            in.skipNBytes(skip);
        }

        // height can be neg in BMP, for row count positive needed.
        int absHeight = Math.abs(imgHeight);

        // each row is padded to a multiple of 4 bytes
        int rowSize = ((imgWidth * 3 + 3) / 4) * 4;

        // Gray val/pixel : 0 -> black , 255 -> white
        int[][] brightness = new int[absHeight][imgWidth];

        // height > 0 = rows bottom up i.e. the first row of the pixel data is the
        // bottom of the image
        boolean topDown = imgHeight < 0;

        //
        for (int row = 0; row < absHeight; row++) {
            byte[] rowBytes = in.readNBytes(rowSize);
            if (rowBytes.length < rowSize) {
                throw new IOException("Unexpected end of BMP pixel data");
            }

            // row 0 is the bottom if image is bottom up
            int targetRow = topDown ? row : absHeight - 1 - row;

            for (int col = 0; col < imgWidth; col++) {
                // each pixel is 3 bytes
                int i = col * 3;
                int blue = rowBytes[i] & 0xFF;
                int green = rowBytes[i + 1] & 0xFF;
                int red = rowBytes[i + 2] & 0xFF;
                // avg grey
                brightness[targetRow][col] = (red + green + blue) / 3;

            }

        }

        // output size full image or downscaled if -w arg.
        int outWidth = imgWidth;
        int outHeight = absHeight;

        if (width != null && width > 0 && width < imgWidth) {
            outWidth = width;
            outHeight = Math.max(1, absHeight * outWidth / imgWidth);
        }

        // turning brightness into ASCII chars
        String ramp = Main.RAMP;
        for (int row = 0; row < outHeight; row++) {
            for (int col = 0; col < outWidth; col++) {
                // map output cell back to a pixel in the original image
                int srcRow = row * absHeight / outHeight;
                int srcCol = col * imgWidth / outWidth;
                int value = brightness[srcRow][srcCol];
                // flips dark/light
                if (invert) {
                    value = 255 - value;
                }
                int index = value * (ramp.length() - 1) / 255;
                out.write(ramp.charAt(index));

            }
            out.write(System.lineSeparator());
        }

        // // temp debug
        // out.write(
        // "BMP ok, " + imgWidth + " X " + imgHeight + ", bpp= " + bitsPerPixel + ",
        // pixelOffset= " + pixelOffset);
        // out.write(System.lineSeparator());

    }
}
