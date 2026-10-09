package ch.heigvd;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Reader;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Core image processing logic to convert ASCII art text streams into uncompressed 24-bit BMP images.
 */
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
     * Converts ASCII art from a Reader into a 24-bit uncompressed BMP image written to an OutputStream.
     *
     * @param in    the character reader providing the ASCII art lines
     * @param out   the destination output stream to write binary BMP data to
     * @param scale pixel scale factor per character (e.g. 1 means 1x1 px per char, 2 means 2x2 px per char)
     * @throws IOException if an I/O error occurs, if the input is empty, or if no recognized characters exist
     */
    public static void process(Reader in, OutputStream out, int scale) throws IOException {
        int factor = Math.max(1, scale);
        List<String> lines = readAsciiArt(in);
        writeBmp(out, lines, factor);
    }

    /**
     * Reads all lines of ASCII art from the reader and validates that the content is non-empty.
     *
     * @param in the character reader
     * @return a list of text lines representing the ASCII art
     * @throws IOException if an I/O error occurs or if the art contains no content or only empty lines
     */
    private static List<String> readAsciiArt(Reader in) throws IOException {
        List<String> lines = new ArrayList<>();
        BufferedReader reader = in instanceof BufferedReader br ? br : new BufferedReader(in);
        String line;
        while ((line = reader.readLine()) != null) {
            lines.add(line);
        }
        if (lines.isEmpty() || getMaxLineWidth(lines) == 0) {
            throw new IOException("Input ASCII art is empty");
        }
        return lines;
    }

    /**
     * Orchestrates generating BMP headers and streaming scanlines into the output stream.
     *
     * @param out    the output stream to write to
     * @param lines  the parsed ASCII art lines
     * @param factor the pixel scale factor per character
     * @throws IOException if an I/O error occurs during writing or ramp construction
     */
    private static void writeBmp(OutputStream out, List<String> lines, int factor) throws IOException {
        int artWidth = getMaxLineWidth(lines);
        int artHeight = lines.size();

        int imgWidth = artWidth * factor;
        int imgHeight = artHeight * factor;

        int rowPadding = calculateRowPadding(imgWidth);
        int rowBytes = imgWidth * 3 + rowPadding;

        String ramp = buildEffectiveRamp(lines);

        byte[] header = createBmpHeader(imgWidth, imgHeight, rowBytes);
        out.write(header);
        writePixelData(out, lines, factor, imgWidth, imgHeight, rowBytes, rowPadding, ramp);
        out.flush();
    }

    /**
     * Finds the maximum length across all ASCII art lines to establish the canvas width.
     *
     * @param lines the list of text lines
     * @return the maximum line length, or 0 if empty
     */
    public static int getMaxLineWidth(List<String> lines) {
        return lines.stream().mapToInt(String::length).max().orElse(0);
    }

    /**
     * Calculates the number of padding bytes (0 to 3) needed to align a BMP pixel row to a 4-byte boundary.
     *
     * @param pixelWidth the width of the row in pixels
     * @return the required padding bytes (between 0 and 3)
     */
    public static int calculateRowPadding(int pixelWidth) {
        return (4 - (pixelWidth * 3) % 4) % 4;
    }

    /**
     * Constructs a standard 54-byte BMP header (14-byte BITMAPFILEHEADER + 40-byte BITMAPINFOHEADER)
     * configured for 24-bit uncompressed RGB in little-endian byte order.
     *
     * @param width    the image width in pixels
     * @param height   the image height in pixels (positive for bottom-up scanlines)
     * @param rowBytes the total row size in bytes, including padding
     * @return a byte array containing the 54-byte BMP header
     */
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

    /**
     * Builds an effective ramp by stripping {@link Main#MAX_RAMP} to only the characters
     * present in the provided ASCII art, preserving their relative optical density order.
     *
     * @param lines the list of ASCII art lines
     * @return a string containing the subset of characters found in the art in density order
     * @throws IOException if no characters in the art match any character in {@link Main#MAX_RAMP}
     */
    public static String buildEffectiveRamp(List<String> lines) throws IOException {
        Set<Character> usedChars = new HashSet<>();
        for (String line : lines) {
            for (int i = 0; i < line.length(); i++) {
                usedChars.add(line.charAt(i));
            }
        }

        StringBuilder sb = new StringBuilder();
        for (char c : Main.MAX_RAMP.toCharArray()) {
            if (usedChars.contains(c)) {
                sb.append(c);
            }
        }

        if (sb.length() == 0) {
            throw new IOException("Input ASCII art contains no recognized characters from MAX_RAMP");
        }

        return sb.toString();
    }

    /**
     * Writes pixel scanlines in bottom-up order to the output stream, expanding each character
     * according to the scale factor and padding short lines with whitespace.
     *
     * @param out        the destination stream
     * @param lines      the ASCII art text lines
     * @param factor     the scale factor
     * @param imgWidth   the target image width in pixels
     * @param imgHeight  the target image height in pixels
     * @param rowBytes   the total byte size of each scanline including padding
     * @param rowPadding the number of trailing zero padding bytes per scanline
     * @param ramp       the stripped effective character density ramp
     * @throws IOException if an error occurs while writing to the stream
     */
    private static void writePixelData(OutputStream out, List<String> lines, int factor,
                                       int imgWidth, int imgHeight, int rowBytes, int rowPadding,
                                       String ramp) throws IOException {
        byte[] rowBuffer = new byte[rowBytes];

        // BMP rows are stored bottom-up: y goes from (imgHeight - 1) down to 0
        for (int y = imgHeight - 1; y >= 0; y--) {
            int artLineIndex = y / factor;
            String line = artLineIndex < lines.size() ? lines.get(artLineIndex) : "";

            for (int x = 0; x < imgWidth; x++) {
                int artColIndex = x / factor;
                char c = artColIndex < line.length() ? line.charAt(artColIndex) : ' ';
                byte gray = charToGrayscale(c, ramp);

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

    /**
     * Maps an ASCII character to an 8-bit grayscale brightness value [0, 255]
     * based on its position in the effective character density ramp.
     *
     * @param c    the ASCII character
     * @param ramp the active density ramp
     * @return the grayscale byte intensity (0 for darkest, 255 for lightest)
     */
    public static byte charToGrayscale(char c, String ramp) {
        int idx = ramp.indexOf(c);
        if (idx == -1) {
            return (byte) 255;
        }
        if (ramp.length() <= 1) {
            return (byte) (ramp.charAt(0) == ' ' ? 255 : 0);
        }
        return (byte) (idx * 255 / (ramp.length() - 1));
    }
}
