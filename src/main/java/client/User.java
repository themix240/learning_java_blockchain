package client;

import java.io.Serializable;

public class User implements Serializable {
    private static final long serialVersionUID = 1L;
    String username;
    public User(String username) {
        this.username = username;
    }

    @Override
    public String toString() {
        return "User{" +
                "username='" + username + '\'' +
                '}';
    }
}
