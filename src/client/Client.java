package client;

import java.io.*;
import java.net.Socket;
import java.security.*;
import java.time.Duration;
import java.time.Instant;
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


    public final void start() {
        in = new Scanner(System.in);
        try {
            int port = 1337;
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
                    System.out.println("Wybrano złą opcje!");
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    private void loginUser() {
        System.out.println("Wprowadz nazwe uzytkownika: ");
        String username = in.nextLine();
        try {
            objectOutputStream.writeObject(username); //TODO verification with privateKey
            System.out.println("Podaj hasło");
            String passphrase = in.nextLine();
            PrivateKey privateKey = getPrivateKey(passphrase,username);
            String challange = null;
            challange = (String)objectInputStream.readObject();
            byte[] decrypted = CryptoUtils.decryptBytes(Base64.getDecoder().decode(challange),privateKey);
            objectOutputStream.write(decrypted);
            objectOutputStream.flush();
            byte x = objectInputStream.readByte();
            if (x == LOGIN_SUCCESFULL.data) {
                System.out.println("ZALOGOWANO");
                user = new User(username);
                userPanel();
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private PrivateKey getPrivateKey(String passphrase,String username) throws Exception {
        return CryptoUtils.getPrivate("/Users/themix240/blockchain/user_data/" + username + '/' + "privatekey.txt");
    }

    private void registerUser() {
        System.out.println("Wprowadz nazwe uzytkownika: ");
        String username = in.nextLine();
        try {
            objectOutputStream.writeObject(username);
            byte x = objectInputStream.readByte();
            if (x != REGISTRATION_SUCCESFULL.data) {
                System.out.println("Nazwa zajeta podaj inna!");
                client.close();
                return;
            }
            user = new User(username);
            System.out.println("Enter passphrase: ");
            PublicKey publicKey = CryptoUtils.keyGeneration(username,in.nextLine());
            objectOutputStream.writeObject(publicKey);
            System.out.println("Zarejestrowano pomyslnie - wygenerowano klucze");

        } catch (IOException | NoSuchAlgorithmException | NoSuchProviderException e) {
            throw new RuntimeException(e);
        }
    }

    void userPanel() {
        ExecutorService executorService = initExecutorService();
        boolean mining = false;
        String option = "";
        BlockingQueue<Block> minerQueue = new ArrayBlockingQueue<>(1);
        AtomicInteger startingZeros = new AtomicInteger();
        AtomicLong id = new AtomicLong();
        AtomicReference<String> prevHash = new AtomicReference<>();
        printUserPanel(mining);
        Instant start = Instant.now(), end;
        try {
            while (!Thread.currentThread().isInterrupted()) {
                if (mining) {
                    objectOutputStream.writeObject("mining");
                    getBlockchainData(startingZeros, id, prevHash);
                    if (!minerQueue.isEmpty()) {
                        end = Instant.now();
                        Block b = minerQueue.take();
                        String wykopano = "wykopano";
                        long time = Duration.between(start,end).toSeconds();
                        writeBlockData(b, wykopano,time);
                        start = Instant.now();
                    }
                }
                if (System.in.available() > 0) {
                    switch (in.nextLine()) {
                        case "m":
                            mining = !mining;
                            if (mining) {
                                start = Instant.now();
                                startMining(minerQueue, startingZeros, id, prevHash,executorService);
                            }
                            else{
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
        } catch (IOException | ClassNotFoundException | InterruptedException | NoSuchAlgorithmException |
                 InvalidKeyException e) {
            throw new RuntimeException(e);
        }
    }

    private void getBlockchainData(AtomicInteger startingZeros, AtomicLong id, AtomicReference<String> prevHash) throws IOException, ClassNotFoundException {
        id.set(objectInputStream.readLong());
        prevHash.set((String) objectInputStream.readObject());
        startingZeros.set((int) objectInputStream.readObject());
    }

    private void writeBlockData(Block b, String wykopano,long time) throws IOException, NoSuchAlgorithmException, InvalidKeyException {
        objectOutputStream.writeObject(wykopano);
        objectOutputStream.writeLong(b.timeStamp);
        objectOutputStream.writeObject(b.hash);
        objectOutputStream.writeInt(b.magicNumber);
        objectOutputStream.writeObject(user.username);
        //TODO generate coinbase transaction
    }

    private ExecutorService initExecutorService() {
        return Executors.newFixedThreadPool(8,
                new ThreadFactory() {
                    @Override
                    public Thread newThread(Runnable runnable) {
                        Thread n = Executors.defaultThreadFactory().newThread(runnable);
                        n.setDaemon(true);
                        return n;
                    }
                });
    }

    private void printUserPanel(boolean mining) {
        System.out.println("---PANEL UZYTKOWNIKA---");
        if (!mining)
            System.out.println("m - Zacznij kopać");
        else
            System.out.println("m - Przestań kopać");
        System.out.println("t - Dokonaj transakcji");
        System.out.println("w - Sprawdz stan portfela");
    }

    private void startMining(BlockingQueue<Block> minerQueue, AtomicInteger startingZeros, AtomicLong id, AtomicReference<String> prevHash, ExecutorService executorService) throws IOException, ClassNotFoundException {
        objectOutputStream.writeObject("mining");
        id.set(objectInputStream.readLong());
        prevHash.set((String) objectInputStream.readObject());
        startingZeros.set((int) objectInputStream.readObject());
        executorService.submit(new Miner(id, startingZeros, prevHash, minerQueue));
        executorService.submit(new Miner(id, startingZeros, prevHash, minerQueue));
    }

    private void transaction() throws IOException, ClassNotFoundException {
        objectOutputStream.writeObject("initTransaction");
        List<String> usernames = (List<String>) objectInputStream.readObject();
        usernames.forEach(System.out::println);
        System.out.println("Wpisz nazwe uzytkownika do przeslania pieniedzy");
        String username = in.nextLine();
        while (!usernames.contains(username)) {
            System.out.println("Podaj poprawnego uzytkownika");
            username = in.nextLine();
        }
        objectOutputStream.writeObject(username);

        System.out.println("Podaj ilosc do przelania");
        int ammount = Integer.parseInt(in.nextLine());
        objectOutputStream.writeInt(ammount);
        objectOutputStream.flush();
        //SIGN TRANSACTION
        byte result = objectInputStream.readByte();
        if (result != TRANSACTION_SUCCESFULL.data) {
            System.out.println("NIE UDALO SIE DOKONAC TRANSAKCJI!");
        }
        System.out.println("TRANSAKCJA DOKONANA");
    }

    private void stanPortfela() throws IOException {
        objectOutputStream.writeObject("checkWallet");
        System.out.println(objectInputStream.readInt());
    }
    private void printLoginMenu() {
        System.out.println("---MENU---\n" +
                "1.Zaloguj\n2.Zarejestruj się\n");
    }
}