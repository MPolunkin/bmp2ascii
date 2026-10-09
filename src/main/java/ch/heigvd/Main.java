package ch.heigvd;

import picocli.CommandLine;
import picocli.CommandLine.Command;

/**
 * Main application entry point for the bmp2ascii CLI utility.
 * Configures the root command and registers the 'asciify' and 'bmpify' subcommands.
 */
@Command(
    name = "bmp2ascii",
    description = "Bidirectional conversion between 24-bit uncompressed BMP and ASCII art.",
    mixinStandardHelpOptions = true,
    subcommands = {AsciifyCommand.class, BmpifyCommand.class}
)
public class Main implements Runnable {

    /**
     * Complete 71-character optical density ramp ordered from darkest ink to lightest background.
     */
    public static final String MAX_RAMP = "@$B%8&WM#*oahkbdpqwmZO0QLCJUYXzcvunxrjft/\\|()1{}[]?-_+=~<>i!lI;:,\"^`'. ";

    /**
     * Alias to {@link #MAX_RAMP} preserved for backwards compatibility.
     */
    public static final String RAMP = MAX_RAMP;

    /**
     * Displays CLI command usage when invoked without a subcommand.
     */
    @Override
    public void run() {
        CommandLine.usage(this, System.out);
    }

    /**
     * Program entry point executing the Picocli command-line parser.
     *
     * @param args command-line arguments provided by the user
     */
    public static void main(String... args) {
        System.exit(new CommandLine(new Main()).execute(args));
    }
}
