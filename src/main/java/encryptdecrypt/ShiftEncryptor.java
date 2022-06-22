package encryptdecrypt;

public class ShiftEncryptor implements EncyptorDecryptor{
    private final char[] alphabet = "abcdefghijklmnopqrstuvwxyz".toCharArray();
    private final char[] Ualphabet = "abcdefghijklmnopqrstuvwxyz".toUpperCase().toCharArray();


    @Override
    public String encrypt(String input, int key) {
        StringBuilder output = new StringBuilder("");
        for(int i=0 ; i < input.length(); i++) {
            boolean letter = false;
            for (int j=0; j<alphabet.length;j++){
                if(alphabet[j] == input.charAt(i)) {
                    output.append(alphabet[(j+key)%(alphabet.length)]);
                    letter = true;
                    break;
                }
            }
            for (int j=0; j<alphabet.length;j++) {
                if (Ualphabet[j] == input.charAt(i)) {
                    output.append(Ualphabet[(j+key)%(alphabet.length)]);
                    letter = true;
                    break;
                }
            }
            if(!letter)
                output.append(input.charAt(i));
        }
        return output.toString();
    }


    @Override
    public String decrypt(String input,int key) {
        StringBuilder output = new StringBuilder("");
        for(int i=0 ; i < input.length(); i++) {
            boolean letter = false;
            for (int j=0; j<alphabet.length;j++){
                if(alphabet[j] == input.charAt(i)) {
                    if ( j- key < 0) j+= alphabet.length;
                    output.append(alphabet[(j-key)%(alphabet.length)]);
                    letter = true;
                    break;
                }
            }
            for (int j=0; j<alphabet.length;j++) {
                if (Ualphabet[j]== input.charAt(i)) {
                    if ( j- key < 0) j+= alphabet.length;
                    output.append(Ualphabet[(j-key)%(alphabet.length)]);
                    letter = true;
                    break;
                }
            }
            if(!letter)
                output.append(input.charAt(i));
        }
        System.out.println(input + ' '+ output.toString());
        return output.toString();
    }
}
