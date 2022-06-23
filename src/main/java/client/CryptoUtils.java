package client;

import javax.crypto.Cipher;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

public class CryptoUtils {
    public static PrivateKey getPrivate(String filename) throws Exception {
        byte[] keyBytes = Files.readAllBytes(new File(filename).toPath());
        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);
        KeyFactory kf = KeyFactory.getInstance("RSA");
        return kf.generatePrivate(spec);
    }

    // https://docs.oracle.com/javase/8/docs/api/java/security/spec/X509EncodedKeySpec.html
    public static PublicKey getPublic(String filename) throws Exception {
        byte[] keyBytes = Files.readAllBytes(new File(filename).toPath());
        X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);
        KeyFactory kf = KeyFactory.getInstance("RSA");
        return kf.generatePublic(spec);
    }
    public static byte[] decryptBytes(byte[] input, PrivateKey key) throws GeneralSecurityException {
        Cipher decryptionCipher = Cipher.getInstance("RSA");
        decryptionCipher.init(Cipher.DECRYPT_MODE, key);
        return decryptionCipher.doFinal(input);
    }

    public static PublicKey keyGeneration(String path, String username, String passphrase) throws NoSuchAlgorithmException, NoSuchProviderException, IOException {
        GenerateKeys generateKeys = new GenerateKeys(1024);
        generateKeys.createKeys();
        generateKeys.writeToFile(path + username + '/' + "pubKey.txt", generateKeys.getPublicKey().getEncoded());
        generateKeys.writeToFile(path + username + '/' + "privatekey.txt", generateKeys.getPrivateKey().getEncoded());
        return generateKeys.getPublicKey();
    }
}