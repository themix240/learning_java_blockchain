package encryptdecrypt;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ShiftEncryptorTest {

    @Test
    void encryptTest() {
        ShiftEncryptor se = new ShiftEncryptor();
        String encrypted = se.encrypt("abcdef", 5);
        assertEquals("fghijk",encrypted);
    }
    @Test
    void decryptTest(){
        ShiftEncryptor se = new ShiftEncryptor();
        String decrypted = se.decrypt("fghijk",5);
        assertEquals("abcdef",decrypted);
    }
}