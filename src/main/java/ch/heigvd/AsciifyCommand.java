package ch.heigvd;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.Charset;
import java.util.concurrent.Callable;

@Command(name = "asciify", description = "Convert BMP to ASCII", mixinStandardHelpOptions = true)
public class AsciifyCommand implements Callable<Integer> {

    @Option(names = { "-i", "--input" }, required = true, description = "Input 24-bit uncompressed .bmp file")
    private File input;

    @Option(names = { "-o", "--output" }, required = true, description = "Output .txt file")
    private File output;

    @Option(names = { "-O",
            "--output-encoding" }, defaultValue = "UTF-8", description = "Character encoding for output")
    private String outputEncoding;

    @Option(names = { "-w", "--width" }, description = "Downscale width (preserves aspect ratio)")
    private Integer width;

    @Option(names = "--invert", description = "Invert brightness mapping")
    private boolean invert;

    @Override
    public Integer call() {
        try (var in = new BufferedInputStream(new FileInputStream(input));
                var out = new BufferedWriter(
                        new OutputStreamWriter(new FileOutputStream(output), Charset.forName(outputEncoding)))) {
            Asciify.process(in, out, width, invert);
            return 0;

        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            return 1;
        }
    }
}
