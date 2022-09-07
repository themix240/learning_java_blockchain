import client.Client;
import node.Blockchain;
import node.BlockchainTxtFileManager;
import node.ConnectionHandler;
import org.junit.jupiter.api.BeforeEach;
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
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class IntegrationTests {

    Path clientData;
    Path serverData;
    Path ipData;
    Path userDatabase;

    @TempDir
    private static Path tempDir;

    @BeforeEach
    public void setUp() {
        clientData = tempDir.resolve("client-data");
        serverData = tempDir.resolve("server-data");
        ipData = tempDir.resolve("ips.txt");
        userDatabase = Path.of(serverData.toString(), "userDB.txt");
    }

    private void initializeUserDatabase(List<User> users) throws IOException {
        File f = new File(String.valueOf(userDatabase));
        f.getParentFile().mkdirs();
        f.createNewFile();
        FileOutputStream fileOutputStream = new FileOutputStream(f);
        ObjectOutputStream outputStream1 = new ObjectOutputStream(fileOutputStream);
        outputStream1.writeObject(users);
        outputStream1.close();
        fileOutputStream.close();
    }

    @Test
    @Order(1)
    void clientRegisterTest() throws NoSuchAlgorithmException, IOException, NoSuchProviderException, ClassNotFoundException, InterruptedException {
        Blockchain blockchainFirst = new Blockchain(
                new BlockchainTxtFileManager(serverData.toString()),
                new ArrayList<>(),
                1
        );

        ConnectionHandler connectionHandler = new ConnectionHandler(10020, 10021, blockchainFirst, String.valueOf(userDatabase), ipData.toString());
        Thread t = new Thread(connectionHandler, "Blockchain Connection Handler");
        t.start();
        try {
            Client client = new Client(10020, clientData.toString());
            try {
                boolean registered = client.registerUser("testUser2", "test");
                assertTrue(registered);
            }
            finally {
                client.quit();
            }
        }
        finally {
            t.interrupt();
            try {
                t.join(100);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        assertTrue(Files.exists(Path.of(clientData.toString(), "testUser", "publicKey.txt")));
        assertTrue(Files.exists(Path.of(clientData.toString(), "testUser", "privateKey.txt")));
        assertTrue(Files.exists(userDatabase));
    }

    @Test
    @Order(2)
    void clientLoginTest() throws Exception {
        Blockchain blockchainFirst = new Blockchain(
                new BlockchainTxtFileManager(serverData.toString()),
                new ArrayList<>(),
                1
        );

        User testUser = new User("testUser", CryptoUtils.keyGeneration(clientData.toString(), "testUser", "test"));
        initializeUserDatabase(Arrays.asList(testUser));

        ConnectionHandler connectionHandler = new ConnectionHandler(1000, 1001, blockchainFirst, String.valueOf(userDatabase), ipData.toString());
        Thread t = new Thread(connectionHandler, "Blockchain Connection Handler");
        t.start();

        Client client = new Client(1000, clientData.toString());
        assertTrue(client.loginUser("testUser", "test"));

        client.quit();
        t.interrupt();
        try {
            t.join(100);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    @Order(3)
    void clientMinedBlockTest() throws Exception {
        Blockchain blockchainFirst = new Blockchain(
                new BlockchainTxtFileManager(serverData.toString() + "/blockchain.txt"),
                new ArrayList<>(),
                0
        );
        User testUser = new User("testUser", CryptoUtils.keyGeneration(clientData.toString(), "testUser", "test"));
        initializeUserDatabase(Arrays.asList(testUser));

        ConnectionHandler connectionHandler = new ConnectionHandler(1004, 1005, blockchainFirst, String.valueOf(userDatabase), ipData.toString());
        Thread t = new Thread(connectionHandler, "Blockchain Connection Handler");
        t.start();

        Client client = new Client(1004, clientData.toString());
        assertTrue(client.loginUser("testUser", "test"));

        NewBlock block = new NewBlock(new ArrayList<>(), "0", 120);
        client.writeBlockData(block);

        client.quit();
        t.interrupt();
        try {
            t.join(100);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        assertEquals(1, blockchainFirst.getSize());


    }

    @Test
    void clientTransactionTest() throws Exception {
        User testUser = new User("testUser", CryptoUtils.keyGeneration(clientData.toString(), "testUser", "test"));
        User testUser2 = new User("testUser2", CryptoUtils.keyGeneration(clientData.toString(), "testUser2", "test"));
        initializeUserDatabase(Arrays.asList(testUser, testUser2));

        Transaction firstTransaction = new Transaction(null, testUser.getPublicKey(), 100);
        List<Transaction> transactions =new ArrayList<>(Arrays.asList(firstTransaction));

        MinedBlock firstBlock = new MinedBlock(new NewBlock(transactions, "0", 0), 1);
        List<MinedBlock> blocks =new ArrayList<>(Arrays.asList(firstBlock));

        Blockchain blockchainFirst = new Blockchain(
                new BlockchainTxtFileManager(serverData.toString() + "/blockchain.txt"),
                blocks,
                0
        );

        ConnectionHandler connectionHandler = new ConnectionHandler(10100, 10110, blockchainFirst, String.valueOf(userDatabase), ipData.toString());
        Thread t = new Thread(connectionHandler, "Blockchain Connection Handler");
        t.start();

        Client client = new Client(10100, clientData.toString());
        assertTrue(client.loginUser("testUser", "test"));
        assertTrue(client.transaction("testUser2", 20, "test"));
        assertEquals(100, client.checkWallet()); // should be 100 until next block is mined
        BlockchainData blockchainData = blockchainFirst.getBlockchainData();
        client.writeBlockData(new NewBlock(blockchainData.getTransactions(), blockchainData.getPrevHash(), 100));
        assertEquals(80, client.checkWallet());

        client.quit();
        t.interrupt();
        try {
            t.join(100);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
