package node;

import utils.CryptoUtils;
import utils.Transaction;
import utils.User;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.*;
import java.net.Socket;
import java.security.*;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;

import static node.BlockchainUtils.getWallet;

public class ClientLogic {
    private final List<User> users;

    private User user = null;

    private final String path;
    private final Blockchain bc;
    public ClientLogic(List<User> users, Blockchain bc, String path) {
        this.users = users;
        this.bc = bc;
        this.path = path;
    }

    public boolean register(String username, PublicKey publicKey) {
            User usr = new User(username, publicKey);
            saveUser(usr);
            return true;
    }

    public void saveUser(User userToSave) {
        users.add(userToSave);
        saveChanges();
    }

    public void saveChanges() {
        File f = new File(path);
        try {
            if (!f.exists()) {
                f.getParentFile().mkdirs();
                f.createNewFile();
            }
            FileOutputStream fileOutputStream = new FileOutputStream(f);
            ObjectOutputStream outputStream1 = new ObjectOutputStream(fileOutputStream);
            outputStream1.writeObject(users);
            outputStream1.close();
            fileOutputStream.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    public boolean transaction(String selectedUser, int amount) throws IOException, ClassNotFoundException, NoSuchAlgorithmException, SignatureException, InvalidKeyException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException {
        if (calculateWallet() - amount < 0) {
            return false;
        } else {
            return writeTransaction(selectedUser, amount);
        }
    }

    public int calculateWallet() {
        return getWallet(user.getPublicKey(), bc.getBlocks());
    }

    public boolean writeTransaction(String selectedUser, int amount) throws IOException, ClassNotFoundException, NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException {
        User r = findUser(selectedUser);
        assert r != null;
        Transaction t = new Transaction(user.getPublicKey(), r.getPublicKey(), amount);
        bc.appendMessage(t);
        return false;
    }

    public boolean login(String username, byte[] challenge, byte[] decrypted) throws IOException, GeneralSecurityException, ClassNotFoundException, InterruptedException {
        user = findUser(username);
        assert user != null;
        if (user.getUsername().equals(username)) {
            return Arrays.equals(challenge, decrypted);
        }
        return false;
    }

    public User getUser() {
        return user;
    }

    public User findUser(String username) {
        for (User u : users) {
            if (username.equals(u.getUsername())) {
                return u;
            }
        }
        return null;
    }
}
