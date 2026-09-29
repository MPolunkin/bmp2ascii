package ch.heigvd;

import picocli.CommandLine;
import picocli.CommandLine.Command;

@Command(
    name = "bmp2ascii",
    description = "Bidirectional conversion between 24-bit uncompressed BMP and ASCII art.",
    mixinStandardHelpOptions = true,
    subcommands = {AsciifyCommand.class, BmpifyCommand.class}
)
public class Main implements Runnable {
    public static final String RAMP = "@%#*+=-:. ";

    @Override
    public void run() {
        CommandLine.usage(this, System.out);
    }

    public static void main(String... args) {
        System.exit(new CommandLine(new Main()).execute(args));
    }
}
