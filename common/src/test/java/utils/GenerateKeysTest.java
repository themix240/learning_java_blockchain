package utils;

import org.junit.jupiter.api.Test;
import utils.crypto.GenerateKeys;

import java.security.NoSuchAlgorithmException;

import static org.junit.jupiter.api.Assertions.*;

class GenerateKeysTest {

    @Test
    void createKeysTest() throws NoSuchAlgorithmException {
        GenerateKeys generateKeys = new GenerateKeys(1024);
        generateKeys.createKeys();
        assertNotNull(generateKeys.getPrivateKey());
        assertNotNull(generateKeys.getPublicKey());
    }
}
