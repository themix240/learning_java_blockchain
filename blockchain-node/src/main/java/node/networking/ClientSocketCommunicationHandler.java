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

public class ClientSocketCommunicationHandler implements Runnable {

    Blockchain blockchain;
    Socket socket;
    String path;
    List<User> users;
    ObjectOutputStream objectOutputStream;
    ObjectInputStream objectInputStream;

    ClientLogic client;

    public ClientSocketCommunicationHandler(Blockchain blockchain, Socket socket, String path, List<User> users) {
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

    private void wallet() throws IOException {
        objectOutputStream.writeInt(client.calculateWallet());
        objectOutputStream.flush();
    }

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
           blockchain.appendMessage(t);
       }
       else {
           objectOutputStream.writeByte(HEADERS.REGISTRATION_UNSUCCESFULL.data);
           objectOutputStream.flush();
       }
    }

    private void mined() throws IOException, ClassNotFoundException, InterruptedException {
        NewBlock block = (NewBlock) objectInputStream.readObject();
        blockchain.acceptBlock(block);
    }

    private void mining() throws IOException {
        objectOutputStream.writeObject(blockchain.getBlockchainData());
    }

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
