package ch.heigvd;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Reader;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;

public final class Bmpify {

    private static final int BMP_HEADER_SIZE = 54;
    private static final int DIB_HEADER_SIZE = 40;
    private static final short COLOR_PLANES = 1;
    private static final short BITS_PER_PIXEL = 24;
    private static final int COMPRESSION_BI_RGB = 0;
    private static final int DEFAULT_DPI_PPM = 2835; // ~72 DPI in pixels per meter

    private Bmpify() {
    }

    /**
     * Converts ASCII art from a Reader to a 24-bit uncompressed BMP written to an OutputStream.
     */
    public static void process(Reader in, OutputStream out, int scale) throws IOException {
        int factor = Math.max(1, scale);
        List<String> lines = readAsciiArt(in);
        writeBmp(out, lines, factor);
    }

    private static List<String> readAsciiArt(Reader in) throws IOException {
        List<String> lines = new ArrayList<>();
        BufferedReader reader = in instanceof BufferedReader br ? br : new BufferedReader(in);
        String line;
        while ((line = reader.readLine()) != null) {
            lines.add(line);
        }
        if (lines.isEmpty()) {
            throw new IOException("Input ASCII art is empty");
        }
        return lines;
    }

    private static void writeBmp(OutputStream out, List<String> lines, int factor) throws IOException {
        int artWidth = getMaxLineWidth(lines);
        int artHeight = lines.size();

        int imgWidth = artWidth * factor;
        int imgHeight = artHeight * factor;

        int rowPadding = calculateRowPadding(imgWidth);
        int rowBytes = imgWidth * 3 + rowPadding;

        byte[] header = createBmpHeader(imgWidth, imgHeight, rowBytes);
        out.write(header);
        writePixelData(out, lines, factor, imgWidth, imgHeight, rowBytes, rowPadding);
        out.flush();
    }

    public static int getMaxLineWidth(List<String> lines) {
        return lines.stream().mapToInt(String::length).max().orElse(0);
    }

    public static int calculateRowPadding(int pixelWidth) {
        return (4 - (pixelWidth * 3) % 4) % 4;
    }

    public static byte[] createBmpHeader(int width, int height, int rowBytes) {
        int imageSize = rowBytes * height;
        int fileSize = BMP_HEADER_SIZE + imageSize;

        ByteBuffer buffer = ByteBuffer.allocate(BMP_HEADER_SIZE).order(ByteOrder.LITTLE_ENDIAN);

        // BITMAPFILEHEADER (14 bytes)
        buffer.put((byte) 'B').put((byte) 'M');  // Signature
        buffer.putInt(fileSize);                 // Total file size
        buffer.putInt(0);                        // Reserved
        buffer.putInt(BMP_HEADER_SIZE);          // Pixel data offset (54)

        // BITMAPINFOHEADER (40 bytes)
        buffer.putInt(DIB_HEADER_SIZE);          // DIB header size (40)
        buffer.putInt(width);                    // Width in pixels
        buffer.putInt(height);                   // Height in pixels (positive = bottom-up)
        buffer.putShort(COLOR_PLANES);           // Number of color planes (1)
        buffer.putShort(BITS_PER_PIXEL);         // Bits per pixel (24)
        buffer.putInt(COMPRESSION_BI_RGB);       // Compression (0 = uncompressed)
        buffer.putInt(imageSize);                // Image payload size
        buffer.putInt(DEFAULT_DPI_PPM);          // Horizontal resolution (~72 DPI)
        buffer.putInt(DEFAULT_DPI_PPM);          // Vertical resolution (~72 DPI)
        buffer.putInt(0);                        // Palette colors
        buffer.putInt(0);                        // Important colors

        return buffer.array();
    }

    private static void writePixelData(OutputStream out, List<String> lines, int factor,
                                       int imgWidth, int imgHeight, int rowBytes, int rowPadding) throws IOException {
        byte[] rowBuffer = new byte[rowBytes];

        // BMP rows are stored bottom-up: y goes from (imgHeight - 1) down to 0
        for (int y = imgHeight - 1; y >= 0; y--) {
            int artLineIndex = y / factor;
            String line = artLineIndex < lines.size() ? lines.get(artLineIndex) : "";

            for (int x = 0; x < imgWidth; x++) {
                int artColIndex = x / factor;
                char c = artColIndex < line.length() ? line.charAt(artColIndex) : ' ';
                byte gray = charToGrayscale(c);

                int offset = x * 3;
                rowBuffer[offset]     = gray; // Blue
                rowBuffer[offset + 1] = gray; // Green
                rowBuffer[offset + 2] = gray; // Red
            }

            // Zero out padding bytes at the end of the row
            for (int p = 0; p < rowPadding; p++) {
                rowBuffer[imgWidth * 3 + p] = 0;
            }

            out.write(rowBuffer);
        }
    }

    public static byte charToGrayscale(char c) {
        int idx = Main.RAMP.indexOf(c);
        if (idx == -1) {
            return (byte) 255;
        }
        return (byte) (idx * 255 / (Main.RAMP.length() - 1));
    }
}
