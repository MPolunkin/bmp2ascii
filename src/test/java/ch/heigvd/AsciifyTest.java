package ch.heigvd;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringWriter;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class AsciifyTest {

    @Test
    void rejectsNonBmpData() {
        byte[] notBmp = { 0x00, 0x01, 0x02 }; // doesnt start with B M.
        StringWriter out = new StringWriter();

        assertThrows(IOException.class, () -> Asciify.process(new ByteArrayInputStream(notBmp), out, null, false));
    }

    private static byte[] oneBlackPixelBmp() {
        // headers + 1 pixel row : 3 bytes + 1 pad
        byte[] bmp = new byte[54 + 4];
        // file header
        bmp[0] = 'B';
        bmp[1] = 'M';
        // file size = 58 at bytes 2-5
        bmp[2] = 58;
        // pixel offset = 54 at bytes 10-13
        bmp[10] = 54;
        // DIB header size = 40 at bytes 14-17
        bmp[14] = 40;
        // width = 1 at bytes 18-21
        bmp[18] = 1;
        // height = 1 at bytes 22-25
        bmp[22] = 1;
        // planes = 1 at bytes 26-27
        bmp[26] = 1;
        // bits/pixel = 24 at bytes 28-29
        bmp[28] = 24;
        // pixel data starts at 54, BGR = (0,0,0) pad byte =0
        return bmp;
    }

    private static byte[] twoByTwoBlackBmp() {
        // headers + 1 pixel row : 3 bytes + 1 pad
        byte[] bmp = new byte[54 + 8 * 2];
        // file header
        bmp[0] = 'B';
        bmp[1] = 'M';
        // file size = 54 +16
        bmp[2] = 70;
        // pixel offset = 54 at bytes 10-13
        bmp[10] = 54;
        // DIB header size = 40 at bytes 14-17
        bmp[14] = 40;
        // width = 2 at bytes 18-21
        bmp[18] = 2;
        // height = 2 at bytes 22-25
        bmp[22] = 2;
        // planes = 1 at bytes 26-27
        bmp[26] = 1;
        // bits/pixel = 24 at bytes 28-29
        bmp[28] = 24;
        // pixel data starts at 54, BGR = (0,0,0) pad byte =0
        return bmp;
    }

    private static byte[] eightBitBmpHeader() {
        // just for header, should fail before pixels matter
        byte[] bmp = new byte[54];
        // file header
        bmp[0] = 'B';
        bmp[1] = 'M';
        // file size = 54 at bytes 2-5
        bmp[2] = 54;
        // pixel offset = 54 at bytes 10-13
        bmp[10] = 54;
        // DIB header size = 40 at bytes 14-17
        bmp[14] = 40;
        // width = 1 at bytes 18-21
        bmp[18] = 1;
        // height = 1 at bytes 22-25
        bmp[22] = 1;
        // planes = 1 at bytes 26-27
        bmp[26] = 1;
        // bits/pixel = 8 at bytes 28-29
        bmp[28] = 8;
        return bmp;
    }

    // feature test: successful asciify on minimal valid 24-bit Bmp
    @Test
    void convertsBlackPixelToDarkestRampChar() throws IOException {
        StringWriter out = new StringWriter();

        Asciify.process(new ByteArrayInputStream(oneBlackPixelBmp()), out, null, false);
        // brightness 0 is first char of Main.ramp
        assertEquals("@\n", out.toString().replace("\r\n", "\n"));
    }

    @Test
    void invertBlackPixelIntoLightestRampChar() throws IOException {
        StringWriter out = new StringWriter();
        Asciify.process(new ByteArrayInputStream(oneBlackPixelBmp()), out, null, true);
        // checks for invert 0 --> 255 = " " (space) last in Main.RAMP
        assertEquals(" \n", out.toString().replace("\r\n", "\n"));
    }

    @Test
    void widthDownscaleOutput() throws IOException {
        StringWriter full = new StringWriter();
        StringWriter small = new StringWriter();

        Asciify.process(new ByteArrayInputStream(twoByTwoBlackBmp()), full, null, false);
        Asciify.process(new ByteArrayInputStream(twoByTwoBlackBmp()), small, 1, false);

        // full 2x2 --> 2 lines of "@@"
        assertEquals("@@\n@@\n", full.toString().replace("\r\n", "\n"));
        // 1x1 --> "@"
        assertEquals("@\n", small.toString().replace("\r\n", "\n"));
    }

    @Test
    void rejectNon24BitBmp() {
        StringWriter out = new StringWriter();

        assertThrows(IOException.class,
                () -> Asciify.process(new ByteArrayInputStream(eightBitBmpHeader()), out, null, false));
    }

}
