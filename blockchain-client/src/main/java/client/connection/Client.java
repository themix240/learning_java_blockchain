package client.connection;

import client.miner.MinerMaster;
import utils.blockchain.BlockchainData;
import utils.crypto.CryptoUtils;
import utils.blockchain.NewBlock;
import utils.blockchain.User;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;
import java.util.Scanner;

import static utils.crypto.CryptoUtils.getPublic;
import static utils.crypto.CryptoUtils.signString;
import static utils.HEADERS.*;

/**
 * This class connects to blockchain node, and is used for communication with blockchain.
 *
 * @author Mikołaj Morozowski
 * @version 1.0-SNAPSHOT
 */
public class Client {
    private final Socket client;
    private final ObjectOutputStream objectOutputStream;
    private final ObjectInputStream objectInputStream;
    private User user;
    Scanner in;
    String path;
    int port;
    public boolean mining;
    MinerMaster minerMaster;
    Thread minerMasterThread;

    /**
     * Creates new Client which tries to connect to localhost at given port.
     * It starts Miner Master thread.
     * Starts with mining flag set to false.
     *
     * @param port port on which client tries to connect to localhost.
     * @param path path for saving and loading user data.
     * @throws IOException - something with creating socket or getting streams from socket went wrong.
     * @see MinerMaster
     */
    public Client(int port, String path) throws IOException {
        this.path = path;
        this.port = port;
        client = new Socket("localhost", port);
        objectOutputStream = new ObjectOutputStream(client.getOutputStream());
        objectOutputStream.flush();
        objectInputStream = new ObjectInputStream(client.getInputStream());
        mining = false;
        minerMaster = new MinerMaster(this);
        minerMasterThread = new Thread(minerMaster, "Blockchain Miner Master Thread");
        minerMasterThread.start();
    }

    /**
     * Method to connect to node with previously created credentials.
     * <p>Method is thread safe, uses lock on object streams.</p>
     *
     * @param username   Username used in registration process
     * @param passphrase Passphrase that should be used to decrypt file with private key.
     * @return true if login is successful; false if login is unsuccessful.
     * @throws IOException              something wrong with object streams.
     * @throws ClassNotFoundException   invalid data read by objectInputStream.
     * @throws GeneralSecurityException something with encryption goes wrong.
     */
    public boolean loginUser(String username, String passphrase) throws IOException, ClassNotFoundException, GeneralSecurityException {
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
                    user = new User(username, getPublic(path + '/' + username + '/' + "publicKey.txt"));
                    return true;
                }
            }
            return false;
        }
    }

    /**
     * Returns private key generated for user.
     * Uses path specified in {@link Client#Client(int, String)  Client} constructor.
     *
     * @param passphrase passphrase used to decrypt content of key file on disk (not working for now).
     * @param username   username of user for whom private key is needed.
     * @return {@link PrivateKey} of username specified in algorithm.
     * @throws IOException              - reading file with PrivateKey goes wrong.
     * @throws NoSuchAlgorithmException - error with creating PrivateKey from content of file, probably file is corrupted.
     * @throws InvalidKeySpecException  - invalid algorithm used to create key in file, method requires RSA-256 bit  private key file.
     * @see utils.crypto.CryptoUtils#getPrivate(String)
     */
    public PrivateKey getPrivateKey(String passphrase, String username) throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
        return CryptoUtils.getPrivate(path + '/' + username + "/privateKey.txt");
    }

    /**
     * Sends data of user to register to node.
     * <p>Then gets data from node with result of registration.</p>
     * <p>Method is thread safe, uses lock on object streams.</p>
     *
     * @param username   Username - must be unique for user.
     * @param passphrase passphrase - in future used to encrypt private key file
     * @return <code>true</code> if registration was successful; <code>false</code> otherwise.
     * @throws IOException              something wrong with <code>Object Stream</code>
     * @throws GeneralSecurityException key generation goes wrong.
     */
    public boolean registerUser(String username, String passphrase) throws IOException, GeneralSecurityException {
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

    /**
     * Method which send block to blockchain to be verified.
     * <p>Method is thread safe, uses lock on object streams.</p>
     *
     * @param b - new block which we want to add to blockchain, will be checked and if
     *          fulfils requirements of blockchain will be accepted.
     * @throws IOException something with objectOutputStream went wrong.
     */
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

    /**
     * Method to send transaction to blockchain.
     * <p>Transaction will be included in next accepted block in blockchain.</p>
     * <p>Method is thread safe, uses lock on object streams.</p>
     *
     * @param selected   - Username of user we want to send currency to.
     * @param amount     - amount of currency we wanna sent.
     * @param passphrase - passphrase for private key (encryption of private key not implemented yet) to sign transaction.
     * @return Returns <code>true</code> if transaction is accepted; <code>false</code> otherwise.
     * @throws IOException something with object streams went wrong.
     * @throws GeneralSecurityException something with getting private key went wrong.
     * @throws ClassNotFoundException unexpected object at <code>ObjectInputStream</code>
     * @see utils.blockchain.Transaction
     * @see Client#getPrivateKey(String, String)
     */

    public boolean transaction(String selected, int amount, String passphrase) throws IOException, GeneralSecurityException, ClassNotFoundException {
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

    /**
     * Sends request to blockchain to return current amount in wallet of user.
     * <p>Method is thread safe, uses lock on object streams.</p>
     * @return current amount in user wallet.
     * @throws IOException something went wrong with <code>ObjectStream</code>
     */
    public int checkWallet() throws IOException {
        synchronized (objectOutputStream) {
            objectOutputStream.writeObject("checkWallet");
        }
        synchronized (objectInputStream) {
            return (objectInputStream.readInt());
        }
    }

    /**
     * Switches mining flag.
     */

    public void switchMining() {
        mining = !mining;
    }

    /**
     * Gets user logged in this session.
     * User cannot be null.
     * @return user set in client constructor.
     * @see User
     */
    public User getUser() {
        return user;
    }

    /**
     * Closes <code>ObjectStreams</code> and interrupts <code>MinerMaster Thread</code>, at the end closes <code>Socket</code>.
     */
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