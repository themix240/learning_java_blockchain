package blockchain;

import javax.crypto.SecretKey;
import java.io.Serializable;
import java.security.PublicKey;

public class User implements Serializable {
    private static final long serialVersionUID = 1L;
    String username;
    PublicKey publicKey;

    public User(String username) {
        this.username = username;
    }


    public String getUsername() {
        return username;
    }

    public User(String username, PublicKey publicKey) {
        this.username = username;
        this.publicKey = publicKey;
    }

    @Override
    public String toString() {
        return "User{" +
                "username='" + username + '\'' +
                ", publicKey=" + publicKey +
                '}';
    }
}
