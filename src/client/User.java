package client;

import javax.crypto.SecretKey;
import java.io.Serializable;
import java.security.PrivateKey;
import java.security.PublicKey;

public class User implements Serializable {
    private static final long serialVersionUID = 1L;
    String username;
    PrivateKey privateKey;


   // int wallet;


    public User(String username) {
        this.username = username;
    }


    public User(String username, PrivateKey privateKey) {
        this.username = username;
        this.privateKey = privateKey;

        //this.wallet = wallet;
    }

    @Override
    public String toString() {
        return "User{" +
                "username='" + username + '\'' +
                '}';
    }
}
