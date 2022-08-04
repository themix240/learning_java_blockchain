package utils;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;

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
