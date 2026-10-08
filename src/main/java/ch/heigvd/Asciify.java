package ch.heigvd;

import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class Asciify {
    private static final int FILE_HEADER_SIZE = 14;
    private static final int DIB_HEADER_SIZE = 40;
    private static final int PIXEL_OFFSET_INDEX = 10;
    private static final int WIDTH_INDEX = 4;
    private static final int HEIGHT_INDEX = 8;
    private static final int BITS_PER_PIXEL_INDEX = 14;
    private static final int BITS_PER_PIXEL_SUPPORTED = 24;
    private static final int BYTES_PER_PIXEL = 3;
    private static final int MAX_BRIGHTNESS = 255;

    private Asciify() {
    }

    public static void process(InputStream in, Writer out, Integer width, boolean invert)
            throws IOException {

        BmpGrayImage image = readBrightness(in);
        writeAscii(out, image, width, invert);

    }

    private static void writeAscii(Writer out, BmpGrayImage image, Integer width, boolean invert) throws IOException {
        int imgWidth = image.width();
        int absHeight = image.height();
        int[][] brightness = image.brightness();

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
                    value = MAX_BRIGHTNESS - value;
                }
                int index = value * (ramp.length() - 1) / MAX_BRIGHTNESS;
                out.write(ramp.charAt(index));

            }
            out.write(System.lineSeparator());
        }
    }

    private static BmpGrayImage readBrightness(InputStream in) throws IOException {
        // BMP file header is always 14 bytes
        byte[] fileHeader = in.readNBytes(FILE_HEADER_SIZE);

        // checks to see if BMP
        if (fileHeader.length < FILE_HEADER_SIZE) {
            throw new IOException("File too short to be a BMP");
        }

        if (fileHeader[0] != 'B' || fileHeader[1] != 'M') {
            throw new IOException("File is not a BMP file (missing BM signature).");
        }

        // Bytes 10-13 where pixel data starts
        int pixelOffset = ByteBuffer.wrap(fileHeader, PIXEL_OFFSET_INDEX, 4)
                .order(ByteOrder.LITTLE_ENDIAN)
                .getInt();

        // DIB header = width, height, bits/pixel
        byte[] dibHeader = in.readNBytes(DIB_HEADER_SIZE);

        // check
        if (dibHeader.length < DIB_HEADER_SIZE) {
            throw new IOException("File too short to be a BMP DIB header");
        }

        // width
        int imgWidth = ByteBuffer.wrap(dibHeader, WIDTH_INDEX, 4)
                .order(ByteOrder.LITTLE_ENDIAN)
                .getInt();

        // height. !can be negative (top down)!
        int imgHeight = ByteBuffer.wrap(dibHeader, HEIGHT_INDEX, 4)
                .order(ByteOrder.LITTLE_ENDIAN)
                .getInt();

        // bits/pixel reads 2 bytes and transforms into unsigned val (2beSafe)
        int bitsPerPixel = ByteBuffer.wrap(dibHeader, BITS_PER_PIXEL_INDEX, 2)
                .order(ByteOrder.LITTLE_ENDIAN)
                .getShort() & 0xFFFF;

        // we only handle 24 bit BMPs
        if (bitsPerPixel != BITS_PER_PIXEL_SUPPORTED) {
            throw new IOException("Only 24-bit BMP is supported, got: " + bitsPerPixel);
        }

        // areadyRead = how far we are in the file, skip = how many more bytes until the
        // pixels start
        int alreadyRead = FILE_HEADER_SIZE + DIB_HEADER_SIZE;
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
        int rowSize = ((imgWidth * BYTES_PER_PIXEL + 3) / 4) * 4;

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
                int i = col * BYTES_PER_PIXEL;
                int blue = rowBytes[i] & 0xFF;
                int green = rowBytes[i + 1] & 0xFF;
                int red = rowBytes[i + 2] & 0xFF;
                // avg grey
                brightness[targetRow][col] = (red + green + blue) / 3;

            }

        }

        return new BmpGrayImage(imgWidth, absHeight, brightness);
    }

    private record BmpGrayImage(int width, int height, int[][] brightness) {
    }
}
