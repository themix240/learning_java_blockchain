package utils;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javax.crypto.Cipher;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.*;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Base64;


class CryptoUtilsTest {
    @TempDir
    private static Path tempDir;
    private static PublicKey generatedKey;

    @BeforeEach
    void checkTempDir() {
        assertTrue(Files.isDirectory(tempDir));
    }

    @Test
    @BeforeAll
    static void keyGeneration() throws NoSuchAlgorithmException, IOException, NoSuchProviderException {
        Path file = tempDir.resolve("keys");
        generatedKey = CryptoUtils.keyGeneration(file.toString(), "testUser", "testPassphrase");
        assertAll(
                () -> assertTrue(Files.exists(Path.of(file.toString(), "testUser"))),
                () -> assertTrue(Files.exists(Path.of(file.toString(), "testUser", "publicKey.txt"))),
                () -> assertTrue(Files.exists(Path.of(file.toString(), "testUser", "privateKey.txt"))));
    }

    @Test
    void getPrivate() throws Exception {
        PrivateKey privateKey = CryptoUtils.getPrivate(String.valueOf(Path.of(tempDir.toString(), "keys", "testUser", "privateKey.txt")));
        RSAPrivateKey privateKeyRSA = (RSAPrivateKey) privateKey;
        RSAPublicKey publicKeyRSA = (RSAPublicKey) generatedKey;
        assertEquals(publicKeyRSA.getModulus(), privateKeyRSA.getModulus());
        assertEquals(BigInteger.ONE,
                BigInteger.TWO.modPow(publicKeyRSA.getPublicExponent().multiply(privateKeyRSA.getPrivateExponent()).subtract(BigInteger.ONE),
                        publicKeyRSA.getModulus())); // RSA Algorithm Wiki m^e^d = m mod n
    }

    @Test
    void getPublic() throws Exception {
        assertEquals(generatedKey, CryptoUtils.getPublic(String.valueOf(Path.of(tempDir.toString(), "keys", "testUser", "publicKey.txt"))));
    }

    @Test
    void decryptAndEncryptBytesTest() throws Exception {
        byte[] bytes = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10};
        assertArrayEquals(bytes, CryptoUtils.decryptBytes(CryptoUtils.encryptBytes(bytes, generatedKey),
                CryptoUtils.getPrivate(String.valueOf(Path.of(tempDir.toString(), "keys", "testUser", "privateKey.txt")))));

    }

    @Test
    void signString() throws Exception {
        String toSign = "signature";
        String signature = CryptoUtils.signString(CryptoUtils.getPrivate(String.valueOf(Path.of(tempDir.toString(), "keys", "testUser", "privateKey.txt"))), toSign);
        assertEquals(Base64.getEncoder().encodeToString(toSign.getBytes()), Base64.getEncoder().encodeToString(CryptoUtils.decryptBytes(
                Base64.getDecoder().decode(signature), generatedKey)));
    }
}