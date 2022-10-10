package utils.crypto;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

/**
 * Consists static methods for reading RSA keys from files, encrypting and decrypting data with them and generating new keys.
 */
public class CryptoUtils {
    /**
     * Loads RSA private key from specified file.
     * Private key should be 1024 bits to allow compatiblity with blockchain.
     *
     * @param filename path to file to be loaded
     * @return loaded private key
     * @throws IOException              something with reading file goes wrong
     * @throws NoSuchAlgorithmException file data is corrupted.
     * @throws InvalidKeySpecException  file data is corrupted.
     */
    public static PrivateKey getPrivate(String filename) throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
        byte[] keyBytes = Files.readAllBytes(Path.of(filename));
        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);
        KeyFactory kf = KeyFactory.getInstance("RSA");
        return kf.generatePrivate(spec);
    }

    /**
     * Loads RSA public key from specified file.
     * Private key should be 1024 bits to allow compatiblity with blockchain.
     *
     * @param filename path to file to be loaded
     * @return loaded public key
     * @throws IOException              something with reading file goes wrong
     * @throws NoSuchAlgorithmException file data is corrupted.
     * @throws InvalidKeySpecException  file data is corrupted.
     */
    public static PublicKey getPublic(String filename) throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
        byte[] keyBytes = Files.readAllBytes(new File(filename).toPath());
        X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);
        KeyFactory kf = KeyFactory.getInstance("RSA");
        return kf.generatePublic(spec);
    }

    /**
     * Decrypts given data with given key.
     * Key could be public or private key.
     *
     * @param input bytes to decrypt
     * @param key   key to decrypt data
     * @return decrypted byte array
     * @throws GeneralSecurityException byte array was not encrypted with matching key.
     */
    public static byte[] decryptBytes(byte[] input, Key key) throws GeneralSecurityException {
        Cipher decryptionCipher = Cipher.getInstance("RSA");
        decryptionCipher.init(Cipher.DECRYPT_MODE, key);
        return decryptionCipher.doFinal(input);
    }

    /**
     * Encrypts given data with given key.
     *
     * Key could be public or private key.
     *
     * @param input bytes to encrypt
     * @param key   key to encrypt data
     * @return encrypted byte array
     * @throws GeneralSecurityException given byte array is not valid.
     */
    public static byte[] encryptBytes(byte[] input, PublicKey key) throws GeneralSecurityException {
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.ENCRYPT_MODE, key);
        return cipher.doFinal(input);
    }

    /**
     * Generates a new pair of keys for given user.
     * Keys are RSA 1024 bit.
     * Saves generated keys in specified path+username as directory.
     * Files are saved in files <code>publicKey.txt</code> and <code>privateKey</code>.
     * @param path path to dir where user data should be stored (client side).
     * @param username name of user
     * @param passphrase doesn't work for now.
     * @return generated publicKye
     * @throws NoSuchAlgorithmException generation of keys goes wrong
     * @throws NoSuchProviderException generation of keys goes wrong
     * @throws IOException saving file goes wrong
     */
    public static PublicKey keyGeneration(String path, String username, String passphrase) throws NoSuchAlgorithmException, NoSuchProviderException, IOException {
        GenerateKeys generateKeys = new GenerateKeys(1024);
        generateKeys.createKeys();
        generateKeys.writeToFile(String.valueOf(Path.of(path, username, "publicKey.txt")), generateKeys.getPublicKey().getEncoded());
        generateKeys.writeToFile(String.valueOf(Path.of(path, username, "privateKey.txt")), generateKeys.getPrivateKey().getEncoded());
        return generateKeys.getPublicKey();
    }

    /**
     * Signs given String with privateKey of user.
     * Signature is needed to verify transactions.
     * Hash of transaction is signed.
     * @param pk private key of user.
     * @param toSign string to be signed.
     * @return text with signed string.
     * @throws NoSuchAlgorithmException
     * @throws NoSuchPaddingException
     * @throws InvalidKeyException
     * @throws IllegalBlockSizeException
     * @throws BadPaddingException
     */
    public static String signString(PrivateKey pk, String toSign) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.ENCRYPT_MODE, pk);
        return Base64.getEncoder().encodeToString(cipher.doFinal(toSign.getBytes()));
    }
}