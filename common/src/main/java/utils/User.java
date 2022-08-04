package utils;

import java.io.Serializable;
import java.security.PublicKey;
import java.util.Objects;

public class User implements Serializable {
    private static final long serialVersionUID = 1L;

    public User(String username) {
        this.username = username;
    }

    public String getUsername() {
        return username;
    }

    private String username;

    public PublicKey getPublicKey() {
        return publicKey;
    }

    private PublicKey publicKey;

    public User(String username, PublicKey publicKey) {
        this.username = username;
        this.publicKey = publicKey;
    }

    @Override
    public String toString() {
        return "User{" +
                "username='" + username + '\'' +
                '}';
    }

    @Override
    public int hashCode() {
        return Objects.hash(getUsername(), getPublicKey());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return Objects.equals(getUsername(), user.getUsername()) && Objects.equals(getPublicKey(), user.getPublicKey());
    }
}
