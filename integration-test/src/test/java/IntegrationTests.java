import client.Client;
import node.Blockchain;
import node.BlockchainTxtFileManager;
import node.ConnectionHandler;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import utils.CryptoUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PublicKey;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

public class IntegrationTests {
    @TempDir
    private static Path tempDir;

    @Test
    void ClientRegisterTest() throws NoSuchAlgorithmException, IOException, NoSuchProviderException, ClassNotFoundException {
        Path clientData = tempDir.resolve("client-data");
        Path serverData = tempDir.resolve("server-data");
        Path ipData = tempDir.resolve("ips.txt");
        Blockchain blockchainFirst = new Blockchain(
                new BlockchainTxtFileManager(serverData.toString()),
                new ArrayList<>(),
                1
        );
        Path userDatabase = Path.of(serverData.toString(), "userDB.txt");
        ConnectionHandler connectionHandler = new ConnectionHandler(1000,1001, blockchainFirst, String.valueOf(userDatabase), ipData.toString());
        Thread t = new Thread(connectionHandler);
        t.start();
        Client client =  new Client(1000, clientData.toString());

        assertTrue(client.registerUser("testUser", "test"));
        assertTrue(Files.exists(Path.of(clientData.toString(), "testUser", "publicKey.txt")));
        assertTrue(Files.exists(Path.of(clientData.toString(), "testUser", "privateKey.txt")));
        assertTrue(Files.exists(userDatabase));
        client.quit();
        t.interrupt();
        try {
            t.join();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

    }

}
