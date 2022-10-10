package node.networking;

import node.core.Blockchain;
import node.core.ClientLogic;
import utils.*;
import utils.blockchain.NewBlock;
import utils.blockchain.Transaction;
import utils.blockchain.User;
import utils.crypto.CryptoUtils;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.security.*;
import java.util.Base64;
import java.util.List;
import java.util.Random;

/**
 * Class which is responsible for communication between node and client,
 * Handles all data exchange between node and client.
 */
public class ClientNetworkingLogic implements Runnable {

    Blockchain blockchain;
    Socket socket;
    String path;
    List<User> users;
    ObjectOutputStream objectOutputStream;
    ObjectInputStream objectInputStream;

    ClientLogic client;

    /**
     * Default constructor.
     * @param blockchain reference to blockchain used in communication with client.
     * @param socket socket with connected client.
     * @param path path for saving user database.
     * @param users list of all users data saved in node, username, public key pairs.
     */
    public ClientNetworkingLogic(Blockchain blockchain, Socket socket, String path, List<User> users) {
        this.blockchain = blockchain;
        this.socket = socket;
        this.path = path;
        this.users = users;
        try {

            objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
            objectOutputStream.flush();
            objectInputStream = new ObjectInputStream(socket.getInputStream());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        client = new ClientLogic(users, blockchain, path);
    }

    public void registrationSuccessful() throws IOException {
        objectOutputStream.writeByte(HEADERS.REGISTRATION_SUCCESFULL.data);
    }

    public void registrationUnsuccessful() throws IOException {
        objectOutputStream.writeByte(HEADERS.REGISTRATION_UNSUCCESFULL.data);
    }

    /**
     * Main logic loop.
     * Starts with login or register. Then starts communication loop.
     */
    @Override
    public void run() {
        try {
            int x = objectInputStream.readByte();
            boolean success = false;
            switch (x) {
                case 9:
                    success = login();
                    break;
                case 10:
                    success = register();
                    break;
            }
            if (success) {
                while (!Thread.currentThread().isInterrupted()) {
                    String option = (String) objectInputStream.readObject();
                    switch (option) {
                        case "mining":
                            mining();
                            break;
                        case "mined":
                            mined();
                            break;
                        case "initTransaction":
                            transaction();
                            break;
                        case "checkWallet":
                            wallet();
                            break;
                    }
                }
            }
        } catch (Exception ex) {
            try {
                socket.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }


    }

    /**
     * Sends wallet value of user to connected client.
     * @throws IOException
     */
    private void wallet() throws IOException {
        objectOutputStream.writeInt(client.calculateWallet());
        objectOutputStream.flush();
    }

    /**
     * Method get transaction data from client then check if transaction is possible.
     * Then it digitally signs transaction with signature which client send.
     * If it is possible then it appends transaction to blockchain waiting queue.
     * Transaction is added to next mined block.
     * @throws IOException
     * @throws ClassNotFoundException
     * @throws NoSuchPaddingException
     * @throws IllegalBlockSizeException
     * @throws NoSuchAlgorithmException
     * @throws BadPaddingException
     * @throws InvalidKeyException
     * @throws SignatureException
     */
    private void transaction() throws IOException, ClassNotFoundException, NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException, SignatureException {
        String selected = (String) objectInputStream.readObject();
        int amount = objectInputStream.readInt();
       if(client.isTransactionPossible(selected, amount)){
           objectOutputStream.writeByte(HEADERS.TRANSACTION_SUCCESFULL.data);
           objectOutputStream.flush();
           Transaction t = new Transaction(client.getUser().getPublicKey(), client.findUser(selected).getPublicKey(), amount);
           objectOutputStream.writeObject(t.getHash());
           String signature = (String) objectInputStream.readObject();
           t.setSignature(Base64.getDecoder().decode(signature));
           blockchain.appendTransaction(t);
       }
       else {
           objectOutputStream.writeByte(HEADERS.REGISTRATION_UNSUCCESFULL.data);
           objectOutputStream.flush();
       }
    }

    /**
     * Calling blockchain validation method with data sent from user.
     * @see Blockchain#acceptBlock(NewBlock)
     * @throws IOException
     * @throws ClassNotFoundException
     * @throws InterruptedException
     */
    private void mined() throws IOException, ClassNotFoundException, InterruptedException {
        NewBlock block = (NewBlock) objectInputStream.readObject();
        blockchain.acceptBlock(block);
    }

    private void mining() throws IOException {
        objectOutputStream.writeObject(blockchain.getBlockchainData());
    }

    /**
     * Method for registering user, it checks if username is not taken.
     * @return True if registration is successful, False otherwise.
     * @see ClientLogic#register(String, PublicKey)
     * @throws IOException
     * @throws ClassNotFoundException
     */
    private boolean register() throws IOException, ClassNotFoundException {
        boolean success;
        String username = (String) objectInputStream.readObject();
        success = client.findUser(username) == null;
        if (success) {
            objectOutputStream.writeByte(HEADERS.REGISTRATION_SUCCESFULL.data);
        } else {
            objectOutputStream.writeByte(HEADERS.REGISTRATION_UNSUCCESFULL.data);
        }
        objectOutputStream.flush();
        PublicKey publicKey = (PublicKey) objectInputStream.readObject();
        success = client.register(username, publicKey);
        return success;
    }

    /**
     * Method for login users.
     * Sends challenge byte array encrypted with user public key and checks if user can decrypt it with correct private key.
     * @return True if login is successful, False otherwise.
     * @throws IOException
     * @throws ClassNotFoundException
     * @throws GeneralSecurityException
     * @throws InterruptedException
     */
    private boolean login() throws IOException, ClassNotFoundException, GeneralSecurityException, InterruptedException {
        boolean success;
        String username = (String) objectInputStream.readObject();
        Random random = new Random();
        byte[] challenge = new byte[64];
        random.nextBytes(challenge);

        byte[] encrypted = CryptoUtils.encryptBytes(challenge, client.findUser(username).getPublicKey());
        objectOutputStream.writeObject(Base64.getEncoder().encodeToString(encrypted));
        byte[] decrypted = objectInputStream.readNBytes(64);
        success = client.login(username, challenge, decrypted);
        if (success) {
            objectOutputStream.writeByte(HEADERS.LOGIN_SUCCESFULL.data);
        } else
            objectOutputStream.writeByte(HEADERS.LOGIN_UNSUCCESFULL.data);
        objectOutputStream.flush();
        return success;
    }
}
