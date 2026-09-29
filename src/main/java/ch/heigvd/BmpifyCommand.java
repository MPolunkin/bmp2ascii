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
            return 0;
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            return 1;
        }
    }
}
