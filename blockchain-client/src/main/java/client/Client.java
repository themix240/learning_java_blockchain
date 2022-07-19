package client;

import utils.*;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.security.*;
import java.util.Base64;
import java.util.Scanner;
import java.util.concurrent.*;

import static utils.CryptoUtils.signString;
import static utils.HEADERS.*;

public class Client {
    private Socket client;
    private ObjectOutputStream objectOutputStream;
    private ObjectInputStream objectInputStream;
    private User user;
    Scanner in;
    String path;
    int port;
    boolean mining;

    public Client(int port, String path) {
        this.path = path;
        this.port = port;
    }

    public final void start() throws IOException {
        client = new Socket("localhost", port);
        objectOutputStream = new ObjectOutputStream(client.getOutputStream());
        objectInputStream = new ObjectInputStream(client.getInputStream());
        mining = false;
        Thread minerMaster = new Thread(new Runnable() {
            @Override
            public void run() {
                while(!Thread.currentThread().isInterrupted()) {
                    if (mining) {
                        ExecutorService es = initExecutorService();
                        try {
                            Future<NewBlock> minedBlock = es.submit(new MinerCallable(startMining()));
                            NewBlock mined = minedBlock.get();
                            System.out.println("WYKOPANO BLOK");
                            writeBlockData(mined);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        } catch (ClassNotFoundException e) {
                            throw new RuntimeException(e);
                        } catch (ExecutionException e) {
                            throw new RuntimeException(e);
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                    }
                    else {
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
            }
        });
        minerMaster.start();
    }

    public boolean loginUser(String username, String passphrase) throws Exception {
        objectOutputStream.writeByte(LOGIN_SELECTED.data);
        objectOutputStream.flush();
        objectOutputStream.writeObject(username);
        PrivateKey privateKey = getPrivateKey(passphrase, username);
        String challenge = (String) objectInputStream.readObject();
        byte[] decrypted = CryptoUtils.decryptBytes(Base64.getDecoder().decode(challenge), privateKey);
        objectOutputStream.write(decrypted);
        objectOutputStream.flush();
        byte x = objectInputStream.readByte();
        if (x == LOGIN_SUCCESFULL.data) {
            user = new User(username);
            return true;
        }
        return false;
    }

    public PrivateKey getPrivateKey(String passphrase, String username) throws Exception {
        return CryptoUtils.getPrivate(path + username + '/' + "privatekey.txt");
    }

    public boolean registerUser(String username, String passphrase) throws IOException, NoSuchAlgorithmException, NoSuchProviderException {
        objectOutputStream.writeByte(REGISTRATION_SELECTED.data);
        objectOutputStream.flush();
        objectOutputStream.writeObject(username);
        byte x = objectInputStream.readByte();
        if (x != REGISTRATION_SUCCESFULL.data) {
            client.close();
            return false;
        }
        user = new User(username);
        PublicKey publicKey = CryptoUtils.keyGeneration(path, username, passphrase);
        objectOutputStream.writeObject(publicKey);
        return true;

    }

    public BlockchainData getBlockchainData() throws IOException, ClassNotFoundException {
        return (BlockchainData) objectInputStream.readObject();
    }

    public void writeBlockData(NewBlock b) throws IOException {
        objectOutputStream.writeObject("mined");
        objectOutputStream.writeObject(b);
    }

    private ExecutorService initExecutorService() {
        return Executors.newFixedThreadPool(8,
                runnable -> {
                    Thread n = Executors.defaultThreadFactory().newThread(runnable);
                    n.setDaemon(true);
                    return n;
                });
    }

    private BlockchainData startMining() throws IOException, ClassNotFoundException {
        objectOutputStream.writeObject("mining");
        BlockchainData data = getBlockchainData();
        return data;
    }

    public boolean transaction(String selected, int amount, String passphrase) throws Exception {
        objectOutputStream.writeObject("initTransaction");
        objectOutputStream.writeObject(selected);
        objectOutputStream.writeInt(amount);
        objectOutputStream.flush();
        byte x = objectInputStream.readByte();
        if (x == TRANSACTION_SUCCESFULL.data) {
            PrivateKey pk = getPrivateKey(passphrase, user.getUsername());
            String toSign = (String) objectInputStream.readObject();
            String signature = signString(pk, toSign);
            objectOutputStream.writeObject(signature);
            return true;
        }
        return false;
    }

    public int checkWallet() throws IOException {
        objectOutputStream.writeObject("checkWallet");
        return (objectInputStream.readInt());
    }

    public void switchMining() {
        mining = !mining;
    }
}