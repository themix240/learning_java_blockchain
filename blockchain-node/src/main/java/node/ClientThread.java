package node;

import utils.*;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.*;
import java.net.Socket;
import java.security.*;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;

import static node.BlockchainUtils.getWallet;
import static utils.HEADERS.*;

public class ClientThread implements Runnable {
    private final Socket socket;
    private final ObjectInputStream objectInputStream;
    private final ObjectOutputStream objectOutputStream;
    private final List<User> users;
    private User user = null;
    private final String path;
    private final Blockchain bc;

    public ClientThread(Socket socket, List<User> users, Blockchain bc, String path) {
        this.socket = socket;
        this.users = users;
        this.bc = bc;
        this.path = path;
        try {
            objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
            objectOutputStream.flush();
            objectInputStream = new ObjectInputStream(socket.getInputStream());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void run() {
        try {
            int x = objectInputStream.readByte();
            switch (x) {
                case 9:
                    login();
                    break;
                case 10: //TODO integrate constants
                    rejestracja();
                    break;

            }
        } catch (IOException | ClassNotFoundException | GeneralSecurityException e) {
            throw new RuntimeException(e);
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            System.out.println("Logged out:" + user.getUsername());
        }
    }

    private void rejestracja() {
        try {
            Object recived = objectInputStream.readObject();
            String username = (String) recived;
            //TODO change usernames to publickeys
            user = findUser(username);
            if (user == null) {
                objectOutputStream.writeByte(REGISTRATION_SUCCESFULL.data);
                objectOutputStream.flush();
                PublicKey pk = (PublicKey) objectInputStream.readObject();
                User usr = new User(username, pk);
                saveUser(usr);
            } else {
                objectOutputStream.writeByte(REGISTRATION_UNSUCCESFULL.data);
                objectOutputStream.flush();
            }
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    private void saveUser(User userToSave) {
        users.add(userToSave);
        saveChanges();
    }

    private void saveChanges() {
        File f = new File(path);
        try {
            if (!f.exists()) {
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

    private void userInterface() throws IOException, ClassNotFoundException, NoSuchAlgorithmException, SignatureException, InvalidKeyException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException {
        String options;
        while (!Thread.currentThread().isInterrupted()) {
            options = (String) objectInputStream.readObject();
            switch (options) {
                case "mining":
                    writeBlockchainData();
                    break;
                case "mined":
                   NewBlock b = (NewBlock) objectInputStream.readObject();
                    //generateCoinbase(b);
                    if (bc.acceptBlock(b)) {
                        saveChanges();
                    }
                    break;
                case "checkWallet":
                    int wallet = calculateWallet();
                    objectOutputStream.writeInt(wallet);
                    objectOutputStream.flush();
                    break;
                case "initTransaction":
                    transaction();
                    break;
            }
        }
    }

    private void generateCoinbase(NewBlock b) {
        Transaction coinbase = new Transaction(null, user.getPublicKey(), 100);
        coinbase.setId(0);
        b.appendTransaction(coinbase);
    }

    private void transaction() throws IOException, ClassNotFoundException, NoSuchAlgorithmException, SignatureException, InvalidKeyException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException {
        String selectedUser = (String) objectInputStream.readObject();
        int amount = objectInputStream.readInt();
        if (calculateWallet() - amount < 0) {
            objectOutputStream.writeByte(NO_ENOUGH_MONEY.data);
        } else {
            objectOutputStream.writeByte(TRANSACTION_SUCCESFULL.data);
            writeTransaction(selectedUser, amount);
        }
        objectOutputStream.flush();
    }

    private int calculateWallet() {
        return getWallet(user.getPublicKey(), bc.getBlocks());
    }

    private void writeTransaction(String selectedUser, int ammount) throws IOException, ClassNotFoundException, NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException {
        User r = findUser(selectedUser);
        assert r != null;
        Transaction t = new Transaction(user.getPublicKey(), r.getPublicKey(), ammount);
        String toSign = t.getHash();
        objectOutputStream.writeObject(toSign);
        String signed = (String) objectInputStream.readObject();
        t.setSignature(Base64.getDecoder().decode(signed));
        bc.appendMessage(t);
    }

    private void writeBlockchainData() throws IOException {
        objectOutputStream.writeObject(bc.getBlockchainData());
    }

    private void login() throws IOException, GeneralSecurityException, ClassNotFoundException {
        Object recived = objectInputStream.readObject();
        String username = (String) recived;
        user = findUser(username);
        assert user != null;
        if (user.getUsername().equals(username)) {
            byte[] challange = new byte[64];
            SecureRandom sr = new SecureRandom();
            sr.nextBytes(challange);
            byte[] encrypted = CryptoUtils.encryptBytes(challange, user.getPublicKey());
            objectOutputStream.writeObject(Base64.getEncoder().encodeToString(encrypted));
            byte[] decrypted = objectInputStream.readNBytes(64);
            if (Arrays.equals(challange, decrypted)) {
                objectOutputStream.writeByte(LOGIN_SUCCESFULL.data);
                objectOutputStream.flush();
                userInterface();
            }
        }
    }

    private User findUser(String username) {
        for (User u : users) {
            if (username.equals(u.getUsername())) {
                return u;
            }
        }
        return null;
    }
}
