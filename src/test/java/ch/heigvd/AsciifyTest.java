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

    // one black pixel
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

    @Test
    void convertsBlackPixelToDarkestRampChar() throws IOException {
        StringWriter out = new StringWriter();

        Asciify.process(new ByteArrayInputStream(oneBlackPixelBmp()), out, null, false);
        // brightness 0 is first char of Main.ramp
        assertEquals("@\n", out.toString().replace("\r\n", "\n"));
    }

}
