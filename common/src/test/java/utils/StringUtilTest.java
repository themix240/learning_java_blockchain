package utils;

import org.junit.jupiter.api.Test;
import utils.crypto.StringUtil;

import static org.junit.jupiter.api.Assertions.*;

class StringUtilTest {

    public static final String correct = "9f86d081884c7d659a2feaa0c55ad015a3bf4f1b2b0b822cd15d6c15b0f00a08";

    @Test
    void applySha256() {
        String test = "test";
        assertEquals(correct, StringUtil.applySha256(test));
    }
}