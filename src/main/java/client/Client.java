package client;

import javax.crypto.Cipher;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.security.*;
import java.util.Base64;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

import static client.HEADERS.*;

public class Client {
    private Socket client;
    private ObjectOutputStream objectOutputStream;
    private ObjectInputStream objectInputStream;
    private User user;
    Scanner in;
    String path;
    int port;

    public Client(int port, String path) {
        this.path = path;
        this.port = port;
    }

    public final void start() {
        in = new Scanner(System.in);
        try {
            client = new Socket("localhost", port);
            objectOutputStream = new ObjectOutputStream(client.getOutputStream());
            objectInputStream = new ObjectInputStream(client.getInputStream());
            printLoginMenu();
            switch (Integer.parseInt(in.nextLine())) {
                case 1:
                    objectOutputStream.writeByte(LOGIN_SELECTED.data);
                    objectOutputStream.flush();
                    loginUser();
                    break;
                case 2:
                    objectOutputStream.writeByte(REGISTRATION_SELECTED.data);
                    objectOutputStream.flush();
                    registerUser();
                    break;
                default:
                    System.out.println("Wrong option selected!");
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    private void loginUser() {
        System.out.println("Enter username");
        String username = in.nextLine();
        try {
            objectOutputStream.writeObject(username);
            System.out.println("Enter password for private key");
            String passphrase = in.nextLine();
            PrivateKey privateKey = getPrivateKey(passphrase, username);
            String challenge = (String) objectInputStream.readObject();
            byte[] decrypted = CryptoUtils.decryptBytes(Base64.getDecoder().decode(challenge), privateKey);
            objectOutputStream.write(decrypted);
            objectOutputStream.flush();
            byte x = objectInputStream.readByte();
            if (x == LOGIN_SUCCESFULL.data) {
                System.out.println("Welcome " + username + "!");
                user = new User(username);
                userPanel();
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private PrivateKey getPrivateKey(String passphrase, String username) throws Exception {
        return CryptoUtils.getPrivate(path + username + '/' + "privatekey.txt");
    }

    private void registerUser() {
        System.out.println("Enter username");
        String username = in.nextLine();
        try {
            objectOutputStream.writeObject(username);
            byte x = objectInputStream.readByte();
            if (x != REGISTRATION_SUCCESFULL.data) {
                System.out.println("Name already in database!\nPlease enter other nickname");
                client.close();
                return;
            }
            user = new User(username);
            System.out.println("Enter passphrase for private key:");
            PublicKey publicKey = CryptoUtils.keyGeneration(path, username, in.nextLine());
            objectOutputStream.writeObject(publicKey);
            System.out.println("Registration successful - keys generated in " + path + username);

        } catch (IOException | NoSuchAlgorithmException | NoSuchProviderException e) {
            throw new RuntimeException(e);
        }
    }

    void userPanel() {
        ExecutorService executorService = initExecutorService();
        boolean mining = false;
        BlockingQueue<Block> minerQueue = new ArrayBlockingQueue<>(1);
        AtomicInteger nonce = new AtomicInteger();
        AtomicLong id = new AtomicLong();
        AtomicReference<String> prevHash = new AtomicReference<>();
        printUserPanel(mining);
        try {
            while (!Thread.currentThread().isInterrupted()) {
                if (mining) {
                    objectOutputStream.writeObject("mining");
                    getBlockchainData(nonce, id, prevHash);
                    if (!minerQueue.isEmpty()) {
                        Block b = minerQueue.take();
                        String mined = "mined";
                        writeBlockData(b, mined);
                    }
                }
                if (System.in.available() > 0) {
                    switch (in.nextLine()) {
                        case "m":
                            mining = !mining;
                            if (mining) {
                                startMining(minerQueue, nonce, id, prevHash, executorService);
                            } else {
                                executorService.shutdownNow();
                                executorService = initExecutorService();
                            }
                            break;
                        case "t":
                            transaction();
                        case "w":
                            stanPortfela();
                            break;
                    }
                    printUserPanel(mining);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void getBlockchainData(AtomicInteger nonce, AtomicLong id, AtomicReference<String> prevHash) throws IOException, ClassNotFoundException {
        id.set(objectInputStream.readLong());
        prevHash.set((String) objectInputStream.readObject());
        nonce.set((int) objectInputStream.readObject());
    }

    private void writeBlockData(Block b, String mined) throws IOException {
        objectOutputStream.writeObject(mined);
        objectOutputStream.writeLong(b.timeStamp);
        objectOutputStream.writeInt(b.magicNumber);
        objectOutputStream.writeObject(user.username);
    }

    private ExecutorService initExecutorService() {
        return Executors.newFixedThreadPool(8,
                runnable -> {
                    Thread n = Executors.defaultThreadFactory().newThread(runnable);
                    n.setDaemon(true);
                    return n;
                });
    }

    private void printUserPanel(boolean mining) {
        System.out.println("---USER PANEL---");
        if (!mining)
            System.out.println("m - Start mining");
        else
            System.out.println("m - Stop mining");
        System.out.println("t - Make transaction");
        System.out.println("w - Check wallet");
    }

    private void startMining(BlockingQueue<Block> minerQueue, AtomicInteger nonce, AtomicLong id, AtomicReference<String> prevHash, ExecutorService executorService) throws IOException, ClassNotFoundException {
        objectOutputStream.writeObject("mining");
        id.set(objectInputStream.readLong());
        prevHash.set((String) objectInputStream.readObject());
        nonce.set((int) objectInputStream.readObject());
        executorService.submit(new Miner(id, nonce, prevHash, minerQueue));
        executorService.submit(new Miner(id, nonce, prevHash, minerQueue));
    }

    private void transaction() throws Exception {
        objectOutputStream.writeObject("initTransaction");
        List<String> users;
        users = (List<String>) objectInputStream.readObject();
        System.out.println("Select user");
        users.forEach(System.out::println);
        String selected = in.nextLine();
        objectOutputStream.writeObject(selected);
        System.out.println("Amount to transfer to " + selected);
        int amount = Integer.parseInt(in.nextLine());
        objectOutputStream.writeInt(amount);
        objectOutputStream.flush();
        byte x = objectInputStream.readByte();
        if (x == TRANSACTION_SUCCESFULL.data) {
            System.out.println("Password for private key");
            String passphrase = in.nextLine();
            PrivateKey pk = getPrivateKey(passphrase, user.username);
            String toSign = (String) objectInputStream.readObject();
            Cipher cipher = Cipher.getInstance("RSA");
            cipher.init(Cipher.ENCRYPT_MODE, pk);
            String signature = Base64.getEncoder().encodeToString(cipher.doFinal(toSign.getBytes()));
            objectOutputStream.writeObject(signature);

        }
    }

    private void stanPortfela() throws IOException {
        objectOutputStream.writeObject("checkWallet");
        System.out.println(objectInputStream.readInt());
    }

    private void printLoginMenu() {
        System.out.println("---MENU---\n" +
                "1.Log in\n2.Register\n");
    }
}