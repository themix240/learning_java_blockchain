package encryptdecrypt;

import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

class ArgsParserTest {

    @Test
    void goodArgsTest() {
        String[] args = {"-mode", "enc", "-key", "5", "-in", "input_path.txt", "-out", "output_path.txt", "-alg", "shift"};
        ArgsParser ap = new ArgsParser(args);
        assertEquals("enc",ap.mode);
        assertEquals(5, ap.key);
        assertEquals("input_path.txt", ap.in_path);
        assertEquals("output_path.txt", ap.out_path);
        assertEquals("shift", ap.alg);

    }
}