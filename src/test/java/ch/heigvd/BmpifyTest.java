package ch.heigvd;

import org.junit.jupiter.api.Test;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import static org.junit.jupiter.api.Assertions.*;

public class BmpifyTest {

    @Test
    void rejectsEmptyInput() {
        StringReader in = new StringReader("");
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        assertThrows(IOException.class, () -> Bmpify.process(in, out, 1));
    }

    @Test
    void rejectsInputWithOnlyNewlines() {
        StringReader in = new StringReader("\n\n");
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        assertThrows(IOException.class, () -> Bmpify.process(in, out, 1));
    }

    @Test
    void convertsDarkestCharToBlackPixel() throws IOException {
        StringReader in = new StringReader("@");
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        Bmpify.process(in, out, 1);
        byte[] bmp = out.toByteArray();

        // 54 header bytes + 3 pixel bytes + 1 padding byte = 58 bytes
        assertEquals(58, bmp.length);
        assertEquals('B', bmp[0]);
        assertEquals('M', bmp[1]);

        // Pixel data starts at byte 54: B, G, R should be 0 (black)
        assertEquals(0, bmp[54]);
        assertEquals(0, bmp[55]);
        assertEquals(0, bmp[56]);
        // Row padding byte should be 0
        assertEquals(0, bmp[57]);
    }

    @Test
    void convertsSpaceToWhitePixel() throws IOException {
        StringReader in = new StringReader(" ");
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        Bmpify.process(in, out, 1);
        byte[] bmp = out.toByteArray();

        assertEquals(58, bmp.length);
        assertEquals((byte) 255, bmp[54]);
        assertEquals((byte) 255, bmp[55]);
        assertEquals((byte) 255, bmp[56]);
        assertEquals(0, bmp[57]); // padding byte
    }

    @Test
    void scalesOutputAccordingToFactor() throws IOException {
        StringReader in = new StringReader("@");
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        Bmpify.process(in, out, 2);
        byte[] bmp = out.toByteArray();

        // 2x2 image: width=2, height=2
        // Row size = 2 * 3 + 2 (padding) = 8 bytes
        // Total size = 54 + 8 * 2 = 70 bytes
        assertEquals(70, bmp.length);

        ByteBuffer bb = ByteBuffer.wrap(bmp).order(ByteOrder.LITTLE_ENDIAN);
        assertEquals(2, bb.getInt(18)); // width
        assertEquals(2, bb.getInt(22)); // height
        assertEquals(24, bb.getShort(28)); // bpp
    }

    @Test
    void handlesRaggedLinesSafely() throws IOException {
        String input = "@\n@@@\n";
        StringReader in = new StringReader(input);
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        Bmpify.process(in, out, 1);
        byte[] bmp = out.toByteArray();

        ByteBuffer bb = ByteBuffer.wrap(bmp).order(ByteOrder.LITTLE_ENDIAN);
        assertEquals(3, bb.getInt(18)); // width should be max line length (3)
        assertEquals(2, bb.getInt(22)); // height should be 2
    }

    @Test
    void stripsMaxRampToCharactersPresentInArt() throws IOException {
        assertEquals("@# ", Bmpify.buildEffectiveRamp(java.util.List.of("@  ", "  # ")));
        assertEquals("*.", Bmpify.buildEffectiveRamp(java.util.List.of("****", "....")));
    }

    @Test
    void rejectsArtWithNoRecognizedCharacters() {
        StringReader in = new StringReader("🔥\n");
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        assertThrows(IOException.class, () -> Bmpify.process(in, out, 1));
    }

    @Test
    void normalizesDynamicRangeBasedOnStrippedRamp() throws IOException {
        // Input has only '#' and '.'
        // In MAX_RAMP, '#' appears before '.'
        // Stripped ramp is "#." where '#' is 0 (black) and '.' is 255 (white)
        StringReader in = new StringReader("#.");
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        Bmpify.process(in, out, 1);
        byte[] bmp = out.toByteArray();

        // 2 pixels: width=2, height=1, rowSize=2*3+2=8
        // Byte 54..56: pixel 0 ('#') -> (0, 0, 0)
        assertEquals(0, bmp[54]);
        assertEquals(0, bmp[55]);
        assertEquals(0, bmp[56]);

        // Byte 57..59: pixel 1 ('.') -> (255, 255, 255)
        assertEquals((byte) 255, bmp[57]);
        assertEquals((byte) 255, bmp[58]);
        assertEquals((byte) 255, bmp[59]);
    }
}
