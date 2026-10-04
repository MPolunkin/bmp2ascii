package ch.heigvd;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.io.*;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

@Command(name = "bmpify", description = "Convert ASCII to BMP", mixinStandardHelpOptions = true)
public class BmpifyCommand implements Callable<Integer> {

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
}
