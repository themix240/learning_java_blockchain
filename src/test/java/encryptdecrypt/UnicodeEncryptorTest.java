package encryptdecrypt;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UnicodeEncryptorTest {

    @Test
    void encryptTest() {
        UnicodeEncryptor encryptor = new UnicodeEncryptor();
        String encrypted = encryptor.encrypt("abcdef",5);
        assertEquals("fghijk",encrypted);
    }

    @Test
    void decryptTest() {
        UnicodeEncryptor encryptor = new UnicodeEncryptor();
        String decrypted = encryptor.decrypt("fghijk",5);
        assertEquals("abcdef",decrypted);
    }
}