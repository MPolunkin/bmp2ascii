package ch.heigvd;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.io.*;
import java.nio.charset.Charset;
import java.util.concurrent.Callable;

/**
 * Picocli command handler for the 'bmpify' subcommand, converting ASCII art text files to 24-bit BMP images.
 */
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

    /**
     * Executes the 'bmpify' command by opening file stream resources and delegating to {@link Bmpify#process}.
     *
     * @return 0 on successful conversion, 1 on validation or I/O failure
     */
    @Override
    public Integer call() {
        if (scale < 1) {
            System.err.println("Error: --scale must be at least 1.");
            return 1;
        }

        try (var in = new BufferedReader(new InputStreamReader(new FileInputStream(input), Charset.forName(inputEncoding)));
             var out = new BufferedOutputStream(new FileOutputStream(output))) {
            Bmpify.process(in, out, scale);
            return 0;

        } catch (FileNotFoundException e) {
            System.err.println("Error: file not found: " + e.getMessage());
            return 1;

        } catch (java.nio.charset.UnsupportedCharsetException e) {
            System.err.println("Error: unknown input encoding: " + inputEncoding);
            return 1;

        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            return 1;
        }
    }
}
