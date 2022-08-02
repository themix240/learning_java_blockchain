import client.Client;
import node.Blockchain;
import node.BlockchainTxtFileManager;
import node.ConnectionHandler;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import utils.*;

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
    void clientTransactionTest() throws Exception {
        Path clientData = tempDir.resolve("client-data");
        Path serverData = tempDir.resolve("server-data");
        Path ipData = tempDir.resolve("ips.txt");
        Path userDatabase = Path.of(serverData.toString(), "userDB.txt");
        List<User> users = new ArrayList<>();
        PublicKey publicKey = CryptoUtils.keyGeneration(clientData.toString(), "testUser", "test");
        User testUser = new User("testUser", publicKey);
        User testUser2 = new User("testUser2", CryptoUtils.keyGeneration(clientData.toString(), "testUser2", "test"));
        users.add(testUser);
        users.add(testUser2);
        File f = new File(String.valueOf(userDatabase));
        f.getParentFile().mkdirs();
        f.createNewFile();
        FileOutputStream fileOutputStream = new FileOutputStream(f);
        ObjectOutputStream outputStream1 = new ObjectOutputStream(fileOutputStream);
        outputStream1.writeObject(users);
        outputStream1.close();
        fileOutputStream.close();
        List<MinedBlock> blocks = new ArrayList<>();
        List<Transaction> transactions = new ArrayList<>();
       Transaction firstTransaction = new Transaction(null, publicKey, 100);
       transactions.add(firstTransaction);
        MinedBlock firstBlock = new MinedBlock(new NewBlock(transactions, "0", 0), 1);
        blocks.add(firstBlock);
        Blockchain blockchainFirst = new Blockchain(
                new BlockchainTxtFileManager(serverData.toString()+"/blockchain.txt"),
                blocks,
                0
        );
        ConnectionHandler connectionHandler = new ConnectionHandler(1010,1011, blockchainFirst, String.valueOf(userDatabase), ipData.toString());
        Thread t = new Thread(connectionHandler);
        t.start();
        Client client =  new Client(1010, clientData.toString());
        assertTrue(client.loginUser("testUser", "test"));
        assertTrue(client.transaction("testUser2", 20, "test"));
        assertEquals(100, client.checkWallet()); // should be 100 until next block is mined
        BlockchainData blockchainData = blockchainFirst.getBlockchainData();
        client.writeBlockData(new NewBlock(blockchainData.getTransactions(),blockchainData.getPrevHash(),100));
        assertEquals(80, client.checkWallet());
        client.quit();
        t.interrupt();
        try {
            t.join();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }


    }
    @Test
    void clientCheckWalletTest() {

    }
    @Test
    void p2pTest() {

    }

}
