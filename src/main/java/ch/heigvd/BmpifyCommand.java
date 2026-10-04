package ch.heigvd;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

@Command(name = "bmpify", description = "Convert ASCII to BMP", mixinStandardHelpOptions = true)
public class BmpifyCommand implements Callable<Integer> {

    private static final int BMP_HEADER_SIZE = 54;
    private static final int DIB_HEADER_SIZE = 40;
    private static final short COLOR_PLANES = 1;
    private static final short BITS_PER_PIXEL = 24;
    private static final int COMPRESSION_BI_RGB = 0;
    private static final int DEFAULT_DPI_PPM = 2835; // ~72 DPI in pixels per meter

    @Option(names = {"-i", "--input"}, required = true, description = "Input ASCII art .txt file")
    private File input;

    @Option(names = {"-o", "--output"}, required = true, description = "Output .bmp file")
    private File output;

    @Option(names = {"-I", "--input-encoding"}, defaultValue = "UTF-8", description = "Character encoding for input")
    private String inputEncoding;

    @Option(names = {"-s", "--scale"}, defaultValue = "1", description = "Pixel scale factor per character")
    private int scale;

    @Override
    public Integer call() {
        try {
            List<String> lines = readAsciiArt(input, inputEncoding);
            int width = getMaxLineWidth(lines);
            int height = lines.size();
            int rowPadding = calculateRowPadding(width);
            int rowBytes = width * 3 + rowPadding;
            byte[] header = createBmpHeader(width, height, rowBytes);
            return 0;
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            return 1;
        }
    }

    private List<String> readAsciiArt(File file, String encoding) throws IOException {
        List<String> lines = new ArrayList<>();
        try (var reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), Charset.forName(encoding)))) {
            String line;
            while ((line = reader.readLine()) != null) {
                lines.add(line);
            }
        }
        if (lines.isEmpty()) {
            throw new IOException("Input file is empty: " + file.getName());
        }
        return lines;
    }

    private int getMaxLineWidth(List<String> lines) {
        return lines.stream().mapToInt(String::length).max().orElse(0);
    }

    private int calculateRowPadding(int pixelWidth) {
        return (4 - (pixelWidth * 3) % 4) % 4;
    }

    private byte[] createBmpHeader(int width, int height, int rowBytes) {
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
}
