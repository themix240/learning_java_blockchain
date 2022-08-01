package node;

import utils.HEADERS;
import utils.MinedBlock;
import utils.NewBlock;
import utils.User;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.security.GeneralSecurityException;
import java.security.PublicKey;
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
            objectInputStream = new ObjectInputStream(socket.getInputStream());
            objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
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
        objectOutputStream.writeInt(BlockchainUtils.getWallet(client.getUser().getPublicKey(), blockchain.getBlocks()));
    }

    private void transaction() throws IOException, ClassNotFoundException {
        String selected = (String) objectInputStream.readObject();
        int amount = objectInputStream.readInt();


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
        objectOutputStream.write(challenge);
        objectOutputStream.flush();
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
