package encryptdecrypt;

import java.nio.charset.StandardCharsets;

public class UnicodeEncryptor implements EncyptorDecryptor{
    @Override
    public String encrypt(String input, int key) {
        byte[] bytes_input = input.getBytes(StandardCharsets.UTF_8);
        StringBuilder output = new StringBuilder();
        for (byte b : bytes_input) {
            output.append((char)(b + key));
        }
        return output.toString();
    }
    @Override
    public String decrypt(String input, int key) {
        byte[] bytes_input = input.getBytes(StandardCharsets.UTF_8);
        StringBuilder output = new StringBuilder();
        for (byte b : bytes_input) {
            output.append((char)(b - key));
        }
        return output.toString();
    }
}

