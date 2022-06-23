package blockchainnode;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import java.security.GeneralSecurityException;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;

public class CryptoUtils {
    private static final Cipher cipher;

    static {
        try {
            cipher = Cipher.getInstance("RSA");
        } catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
            throw new RuntimeException(e);
        }
    }

    public static byte[] encryptBytes(byte[] input, PublicKey key) throws GeneralSecurityException {
        cipher.init(Cipher.ENCRYPT_MODE, key);
        return cipher.doFinal(input);
    }

}
