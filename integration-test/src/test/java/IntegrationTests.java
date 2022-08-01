import client.Client;
import node.Blockchain;
import node.BlockchainTxtFileManager;
import node.ConnectionHandler;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import utils.CryptoUtils;
import utils.GenerateKeys;
import utils.NewBlock;
import utils.User;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class IntegrationTests {
    @TempDir
    private static Path tempDir;

    @Test
    @Order(1)
    void clientRegisterTest() throws NoSuchAlgorithmException, IOException, NoSuchProviderException, ClassNotFoundException, InterruptedException {
        Path clientData = tempDir.resolve("client-data");
        Path serverData = tempDir.resolve("server-data");
        Path ipData = tempDir.resolve("ips.txt");
        Blockchain blockchainFirst = new Blockchain(
                new BlockchainTxtFileManager(serverData.toString()),
                new ArrayList<>(),
                1
        );
        Path userDatabase = Path.of(serverData.toString(), "userDB.txt");
        ConnectionHandler connectionHandler = new ConnectionHandler(1337,1338, blockchainFirst, String.valueOf(userDatabase), ipData.toString());
        Thread t = new Thread(connectionHandler);
        t.start();
        Client client =  new Client(1337, clientData.toString());
        assertTrue(client.registerUser("testUser2", "test"));
        client.quit();
        t.interrupt();
        try {
            t.join();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        assertTrue(Files.exists(Path.of(clientData.toString(), "testUser", "publicKey.txt")));
        assertTrue(Files.exists(Path.of(clientData.toString(), "testUser", "privateKey.txt")));
        assertTrue(Files.exists(userDatabase));}

    @Test
    @Order(2)
    void clientLoginTest() throws Exception {
        Path clientData = tempDir.resolve("client-data");
        Path serverData = tempDir.resolve("server-data");
        Path ipData = tempDir.resolve("ips.txt");
        Blockchain blockchainFirst = new Blockchain(
                new BlockchainTxtFileManager(serverData.toString()),
                new ArrayList<>(),
                1
        );
        Path userDatabase = Path.of(serverData.toString(), "userDB.txt");
        List<User> users = new ArrayList<>();
        User testUser = new User("testUser",CryptoUtils.keyGeneration(clientData.toString(), "testUser", "test"));
        users.add(testUser);
        File f = new File(String.valueOf(userDatabase));
        f.getParentFile().mkdirs();
        f.createNewFile();
        FileOutputStream fileOutputStream = new FileOutputStream(f);
        ObjectOutputStream outputStream1 = new ObjectOutputStream(fileOutputStream);
        outputStream1.writeObject(users);
        outputStream1.close();
        fileOutputStream.close();
        ConnectionHandler connectionHandler = new ConnectionHandler(1000,1001, blockchainFirst, String.valueOf(userDatabase), ipData.toString());
        Thread t = new Thread(connectionHandler);
        t.start();
        Client client =  new Client(1000, clientData.toString());
        assertTrue(client.loginUser("testUser", "test"));
        client.quit();
        t.interrupt();
        try {
            t.join();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
    @Test
    @Order(3)
    void clientMinedBlockTest() throws Exception {
        Path clientData = tempDir.resolve("client-data");
        Path serverData = tempDir.resolve("server-data");
        Path ipData = tempDir.resolve("ips.txt");
        Blockchain blockchainFirst = new Blockchain(
                new BlockchainTxtFileManager(serverData.toString()+"/blockchain.txt"),
                new ArrayList<>(),
                0
        );
        Path userDatabase = Path.of(serverData.toString(), "userDB.txt");
        List<User> users = new ArrayList<>();
        User testUser = new User("testUser",CryptoUtils.keyGeneration(clientData.toString(), "testUser", "test"));
        users.add(testUser);
        File f = new File(String.valueOf(userDatabase));
        f.getParentFile().mkdirs();
        f.createNewFile();
        FileOutputStream fileOutputStream = new FileOutputStream(f);
        ObjectOutputStream outputStream1 = new ObjectOutputStream(fileOutputStream);
        outputStream1.writeObject(users);
        outputStream1.close();
        fileOutputStream.close();
        ConnectionHandler connectionHandler = new ConnectionHandler(1004,1005, blockchainFirst, String.valueOf(userDatabase), ipData.toString());
        Thread t = new Thread(connectionHandler);
        t.start();
        Client client =  new Client(1004, clientData.toString());
        assertTrue(client.loginUser("testUser", "test"));
        NewBlock block = new NewBlock(new ArrayList<>(), "0", 120);
        client.writeBlockData(block);
        client.quit();
        t.interrupt();
        try {
            t.join();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        assertEquals(1, blockchainFirst.getSize());



    }
    @Test
    void clientTransactionTest() {

    }
    @Test
    void clientCheckWalletTest() {

    }
    @Test
    void p2pTest() {

    }

}
