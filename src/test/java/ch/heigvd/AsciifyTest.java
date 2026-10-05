package ch.heigvd;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringWriter;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class AsciifyTest {

    @Test
    void rejectsNonBmpData() {
        byte[] notBmp = { 0x00, 0x01, 0x02 }; // doesnt start with B M.
        StringWriter out = new StringWriter();

        assertThrows(IOException.class, () -> Asciify.process(new ByteArrayInputStream(notBmp), out, null, false));
    }
}
