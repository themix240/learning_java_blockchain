package client;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.nio.file.Files;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

public class CryptoUtils {
    private static final Cipher cipher;

    static {
        try {
            cipher = Cipher.getInstance("RSA");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        } catch (NoSuchPaddingException e) {
            throw new RuntimeException(e);
        }
    }

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

    public static byte[] encryptBytes(byte[] input, PublicKey key) throws IOException, GeneralSecurityException {
        Cipher encryptionCipher = Cipher.getInstance("RSA");
        encryptionCipher.init(Cipher.ENCRYPT_MODE, key);
        return encryptionCipher.doFinal(input);
    }

    public static byte[] decryptBytes(byte[] input, PrivateKey key) throws IOException, GeneralSecurityException {
        Cipher decryptionCipher = Cipher.getInstance("RSA");
        decryptionCipher.init(Cipher.DECRYPT_MODE,key);
        return decryptionCipher.doFinal(input);
    }

    public static PublicKey keyGeneration(String username,String passphrase) throws NoSuchAlgorithmException, NoSuchProviderException, IOException {
        GenerateKeys generateKeys = new GenerateKeys(1024);
        generateKeys.createKeys();
        generateKeys.writeToFile("/Users/themix240/blockchain/user_data/" + username + '/' + "pubKey.txt", generateKeys.getPublicKey().getEncoded());
        generateKeys.writeToFile("/Users/themix240/blockchain/user_data/" + username + '/' + "privatekey.txt", generateKeys.getPrivateKey().getEncoded());
        //TODO encrypt privateKeyFile
        return generateKeys.getPublicKey();
    }

    public static void encyrptPassword(User user, String passphrase) throws NoSuchAlgorithmException, InvalidKeySpecException, IOException, NoSuchPaddingException, InvalidKeyException {
        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        byte[] salt = new byte[8];
        SecureRandom random = new SecureRandom();
        random.nextBytes(salt);
        KeySpec keySpec = new PBEKeySpec(passphrase.toCharArray(), salt, 65536, 256);
        SecretKey tmp = factory.generateSecret(keySpec);
        SecretKey secretKey = new SecretKeySpec(tmp.getEncoded(), "AES/CBC/PKCS5Padding");
    }
    public static SecretKey encryptPassphrase(String passphrase,byte[] salt) throws NoSuchAlgorithmException, InvalidKeySpecException {
        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        KeySpec keySpec = new PBEKeySpec(passphrase.toCharArray(), salt, 65536, 256);
        SecretKey tmp = factory.generateSecret(keySpec);
        SecretKey secretKey = new SecretKeySpec(tmp.getEncoded(), "AES/CBC/PKCS5Padding");
        return secretKey;
    }
}