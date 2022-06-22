package encryptdecrypt;

public interface EncyptorDecryptor {
      String encrypt(String input, int key);
      String decrypt(String input, int key);
}
