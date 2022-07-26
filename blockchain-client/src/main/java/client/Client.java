package client;

import utils.BlockchainData;
import utils.CryptoUtils;
import utils.NewBlock;
import utils.User;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Base64;
import java.util.Scanner;

import static utils.CryptoUtils.getPublic;
import static utils.CryptoUtils.signString;
import static utils.HEADERS.*;

public class Client {
    private final Socket client;
    private final ObjectOutputStream objectOutputStream;
    private final ObjectInputStream objectInputStream;
    private User user;
    Scanner in;
    String path;
    int port;
    boolean mining;
    MinerMaster minerMaster;
    Thread minerMasterThread;

    public Client(int port, String path) throws IOException {
        this.path = path;
        this.port = port;
        client = new Socket("localhost", port);
        objectOutputStream = new ObjectOutputStream(client.getOutputStream());
        objectInputStream = new ObjectInputStream(client.getInputStream());
        mining = false;
        minerMaster = new MinerMaster(this);
        minerMasterThread = new Thread(minerMaster);
        minerMasterThread.start();
    }

    public boolean loginUser(String username, String passphrase) throws Exception {
        synchronized (objectOutputStream) {
            objectOutputStream.writeByte(LOGIN_SELECTED.data);
            objectOutputStream.flush();
            objectOutputStream.writeObject(username);
            PrivateKey privateKey = getPrivateKey(passphrase, username);
            String challenge = (String) objectInputStream.readObject();
            byte[] decrypted = CryptoUtils.decryptBytes(Base64.getDecoder().decode(challenge), privateKey);
            objectOutputStream.write(decrypted);
            objectOutputStream.flush();
            synchronized (objectInputStream) {
                byte x = objectInputStream.readByte();
                if (x == LOGIN_SUCCESFULL.data) {
                    user = new User(username, getPublic(path+username+'/'+"pubKey.txt"));
                    return true;
                }
            }
            return false;
        }
    }

    public PrivateKey getPrivateKey(String passphrase, String username) throws Exception {
        return CryptoUtils.getPrivate(path + username + '/' + "privatekey.txt");
    }

    public boolean registerUser(String username, String passphrase) throws IOException, NoSuchAlgorithmException, NoSuchProviderException {
        synchronized (objectOutputStream) {
            objectOutputStream.writeByte(REGISTRATION_SELECTED.data);
            objectOutputStream.flush();
            objectOutputStream.writeObject(username);
            synchronized (objectInputStream) {
                byte x = objectInputStream.readByte();
                if (x != REGISTRATION_SUCCESFULL.data) {
                    client.close();
                    return false;
                }
            }
            user = new User(username);
            PublicKey publicKey = CryptoUtils.keyGeneration(path, username, passphrase);
            objectOutputStream.writeObject(publicKey);
            return true;
        }

    }

    public void writeBlockData(NewBlock b) throws IOException {
        synchronized (objectOutputStream) {
            objectOutputStream.writeObject("mined");
            objectOutputStream.writeObject(b);
        }
    }

    public BlockchainData startMining() throws IOException, ClassNotFoundException {
        synchronized (objectOutputStream) {
            objectOutputStream.writeObject("mining");
            synchronized (objectInputStream) {
                return (BlockchainData) objectInputStream.readObject();
            }
        }
    }

    public boolean transaction(String selected, int amount, String passphrase) throws Exception {
        synchronized (objectOutputStream) {
            objectOutputStream.writeObject("initTransaction");
            objectOutputStream.writeObject(selected);
            objectOutputStream.writeInt(amount);
            objectOutputStream.flush();
            synchronized (objectInputStream) {
                byte x = objectInputStream.readByte();
                if (x == TRANSACTION_SUCCESFULL.data) {
                    PrivateKey pk = getPrivateKey(passphrase, user.getUsername());
                    String toSign = (String) objectInputStream.readObject();
                    String signature = signString(pk, toSign);
                    objectOutputStream.writeObject(signature);
                    return true;
                }
            }
            return false;
        }
    }

    public int checkWallet() throws IOException {
        synchronized (objectOutputStream) {
            objectOutputStream.writeObject("checkWallet");
        }
        synchronized (objectInputStream) {
            return (objectInputStream.readInt());
        }
    }

    public void switchMining() {
        mining = !mining;
    }

    public User getUser() {
        return user;
    }
    public void quit() {
        try {
            minerMasterThread.interrupt();
            objectOutputStream.close();
            objectInputStream.close();
            client.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}