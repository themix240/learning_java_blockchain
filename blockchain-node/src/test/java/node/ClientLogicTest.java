package node;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import utils.GenerateKeys;
import utils.User;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.IOException;
import java.nio.file.Path;
import java.security.GeneralSecurityException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

class ClientLogicTest {
    @TempDir
    private static Path tempDir;

    @Test
    void registerTest() throws NoSuchAlgorithmException {
        Blockchain blockchain = new Blockchain(new BlockchainTxtFileManager(tempDir.toString()+"/blockchain.txt"),new ArrayList<>(),0);
        ClientLogic client =new  ClientLogic(new ArrayList<>(), blockchain, tempDir.toString()+"/userDB.txt");
        GenerateKeys generateKeys = new GenerateKeys(1024);
        generateKeys.createKeys();
        assertTrue(client.register("testUser", generateKeys.getPublicKey()));
        assertFalse(client.register("testUser", generateKeys.getPublicKey()));

    }


    @Test
    void transactionTest() throws GeneralSecurityException, IOException, ClassNotFoundException, InterruptedException {
        Blockchain blockchain = new Blockchain(new BlockchainTxtFileManager(tempDir.toString()+"/blockchain.txt"),new ArrayList<>(),0);
        ClientLogic client =new  ClientLogic(new ArrayList<>(), blockchain, tempDir.toString()+"/userDB.txt");
        GenerateKeys generateKeys = new GenerateKeys(1024);
        generateKeys.createKeys();
        client.register("testUser", generateKeys.getPublicKey());
        client.login("testUser", null, null);
        assertFalse(client.isTransactionPossible("testUser", 10));
        assertFalse(client.isTransactionPossible("testUser", -10));
        assertTrue(client.isTransactionPossible("testUser", 0));

    }



    @Test
    void loginTest() throws GeneralSecurityException, IOException, ClassNotFoundException, InterruptedException { Blockchain blockchain = new Blockchain(new BlockchainTxtFileManager(tempDir.toString()+"/blockchain.txt"),new ArrayList<>(),0);
        ClientLogic client =new  ClientLogic(new ArrayList<>(), blockchain, tempDir.toString()+"/userDB.txt");
        GenerateKeys generateKeys = new GenerateKeys(1024);
        generateKeys.createKeys();
        client.register("testUser", generateKeys.getPublicKey());
        assertTrue(client.login("testUser", null, null));
        byte[] test = "test".getBytes();
        assertFalse(client.login("testUser", test, null));
        assertTrue(client.login("testUser", test, test));

    }

    @Test
    void findUserTest() throws NoSuchAlgorithmException {
        Blockchain blockchain = new Blockchain(new BlockchainTxtFileManager(tempDir.toString()+"/blockchain.txt"),new ArrayList<>(),0);
        ClientLogic client =new  ClientLogic(new ArrayList<>(), blockchain, tempDir.toString()+"/userDB.txt");
        GenerateKeys generateKeys = new GenerateKeys(1024);
        generateKeys.createKeys();
        client.register("testUser", generateKeys.getPublicKey());
        assertEquals(new User("testUser", generateKeys.getPublicKey()), client.findUser("testUser"));

    }


}